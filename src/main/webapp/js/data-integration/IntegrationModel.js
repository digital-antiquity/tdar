(function(angular){
    "use strict";

    var app = angular.module("integrationApp");

    /**
     * Add specified object to specified array if specified array does not already contain the object.
     *
     * @param arr an array of objects
     * @param obj the object to potentially add to the array
     * @param propName the property to consider when looking for duplicate items. If undefined, function uses strict identity to find dupes.
     * @private
     */
    function _setAdd(arr, obj, propName) {
        var fn, dupes=[];
        if(propName) {
            fn = function(testObj) {
                return testObj[propName] === obj[propName]
            };
        } else {
            fn = function(testObj) {
                return testObj === obj;
            }
        }
        dupes = arr.filter(fn);
        if(dupes.length === 0) {
            arr.push(obj);
        }
        return dupes.length === 0
    }

    function _setAddAll(arr, objs, propName) {
        objs.forEach(function(obj) {
            _setAdd(arr, obj, propName);
        });
    }

    /**
     * Retrieve item from an array (by key), treating array as a set.  Yes, I realize setGet is a stupid function name.  Go away.
     * @param arr
     * @param keyName
     * @param key
     * @returns  object located by key, or null if not found
     * @private
     */
    function _setGet(arr, keyName, key) {
        var val = null;
        for(var i = 0; i < arr.length; i++) {
            if(arr[i][keyName] === key) {
                val = arr[i];
                break;
            }
        }
        return val;
    }

    /**
     * Remove specified item from the specified array
     * @param arr
     * @param item
     * @returns {*} original, mutated array
     * @private
     */
    function _setRemove(arr, item) {
        if(arr.indexOf(item) === -1) return arr;
        arr.splice(arr.indexOf(item), 1);
        return arr;
    }



    //Our integration model
    function Integration() {
        var self = this;

        /**
         * Name for the current integration workflow, specified by the user. This is for organizational purposes only(e.g. picking a previous integration out
         * of a list).  This name does not appear in the final integration report.
         * @type {string}
         */
        self.title = "";

        /**
         * Description for the current workflow.  For organizational purposes (it does not appear in final integration report).
         * @type {string}
         */
        self.description = "";

        /**
         * This list describes columns in the final integration results.  Here a column may be either an "integration" column or a "display" column.
         * @type {Array}
         */
        self.columns = [];

        /**
         * List of datatables included in the integration.
         * @type {Array}
         */
        self.datatables = [];

        /**
         * List of ontologies that the user may integrate over. To be added as an integration column,   the ontology must be "shared" by all of this integration
         * object's datatables.  That is, the system can only create an integration column for a given ontology if, for every datatable in Integration#datatables, there is at least one
         * datataneColumn with a default_ontology_id that equals ontology.id.
         * @type {Array}
         */
        self.ontologies = [];

        //derived datatable participation information, keyed by info
        self.mappedDatatables = {};

        //derived ontology node participation information
        self.ontologyParticipation = {};

        /**
         * Build the datatable participation information for the specified ongology and store the results in ontology.compatibleDatatableColumns
         *
         * compatibleDatatableColumns is a 2d array of datatableColumns that map to the specified ontology.
         *
         * Not to be
         * confused with datatableColumn list in an integration context.
         * @param ontology
         * @private
         */
        function _buildCompatibleDatatableColumns(ontology) {

            //list of maps
            var compatTables = [];

            self.datatables.forEach(function(datatable) {
                compatTables.push({
                    datatable: datatable,
                    compatCols:datatable.columns.filter(function(col){
                        return col.default_ontology_id === ontology.id;
                    })
                });
            });
            self.mappedDatatables[ontology.id] = compatTables;
        }

        self.getMappedDatatableColumns =  function _getMappedDatatableColumns(ontologyId) {
            var cols = [];
            self.mappedDatatables[ontologyId].forEach(function(compatTable){
                cols = cols.concat(compatTable.compatCols);
            });
            return cols;
        }


        /**
         * Append an 'integration column' to the columns list.
         * @param title
         * @param ontology
         * @returns {{type: string, title: *, ontologyId: *, nodeSelections: Array, datatableColumnIds: Array}}
         */
        self.addIntegrationColumn =  function _addIntegrationColumn(title, ontology) {
            var self = this;
            var col = {
                type: "integration",
                title: title,
                ontologyId: ontology.id,
                ontology: ontology,
                nodeSelections: [],
                selectedDatatableColumns: self.mappedDatatables[ontology.id].map(function(dt){
                    if(!dt.compatCols.length) return null;
                    return dt.compatCols[0];
                })
            }

            col.nodeSelections = ontology.nodes.map(function(node,i){
                return {
                    selected: false,
                    node: node,
                    nodeIndex:i
                }
            });
            self.columns.push(col);
            return col;
        };

        self.updateSharedOntologies = function _updateSharedOntologies(ontologies) {
            _setAddAll(self.ontologies, ontologies, "id");
            ontologies.forEach(_buildCompatibleDatatableColumns);
        };

        /**
         * Remove specified datatables from the viewmodel.  This impacts all integration columns and any display columns that have datatableColumns
         * which belong to any of the specified datatables.
         * @param datatables
         * @private
         */
        self.removeDatatables = function _removeDatatable(datatables) {
            if(!datatables) {return;}
            if(datatables.length === 0){return;}

            datatables.forEach(function(datatable) {
                _setRemove(self.datatables, datatable);
            });

        }


        /**
         * Add a 'display column' to the columns list.  A display column contains a list of datatableColumn selections, which the system
         * will include in the final integration report file.   The user may choose 0 or 1 datatableColumn from each
         * datatable.  This method primes the datatableColumnSelections list to be size N with each slot containing null (where
         * N is the count of data tables in the current integration workflow.
         * @param title
         * @private
         */
        self.addDisplayColumn = function _addDisplayColumn(title) {

            var displayColumn = {
                type: 'display',
                title: title,
                datatableColumnSelections: []
            };

            self.datatables.forEach(function(table){
                var selection = {
                    datatable: table,
                    datatableColumn: null
                }
                displayColumn.datatableColumnSelections.push(selection);
            });
            self.columns.push(displayColumn);
        };

        self.updateNodeParticipationInfo = function _updateParticipationInfo(ontology, mappedCols, arNodeInfo) {
            var ontologyParticipation = {
                ontology:ontology,
                nodeInfoList: []
            };

            arNodeInfo.forEach(function(nodeInfo, nodeIdx) {
                var arbPresent = nodeInfo.mapping_list;

                var nodeParticipation = {
                    node: ontology.nodes[nodeIdx],
                    colIds: mappedCols.filter(function(b,i){return arbPresent[i]}).map(function(col){return col.id})
                };
                ontologyParticipation.nodeInfoList.push(nodeParticipation);
            });
            self.ontologyParticipation[ontology.id] = ontologyParticipation;
        };

        self.addDatatables = function(dataTables) {
            _setAddAll(self.datatables, dataTables, "data_table_id");

            //FIXME: update dependencies
        };


        //FIXME: implement me
        self.removeDatatable = function(){};

        //FIXME: implement me
        self.addOntologies = function() {};
        //FIXME: implement me
        self.removeOntology = function() {};
    }

    app.service("IntegrationService", Integration);

})(angular)