(function($, angular, console){
    "use strict";
    var app,_projects, _collections, _categories, _documentData;
    app = angular.module('integrationApp', ['angularModalService']);

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

    //FIXME: break model out into seperate file
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
    }


    /*
     Make the integration viewmodel available to all components in this angular app.  Currently overkill (we could put this object in iife scope), but we may choose
     to break out this code into multiple .js files later.

     More info:
        more info: http://stackoverflow.com/questions/16508666/sharing-a-variable-between-controllers-in-angular-js
        http://stackoverflow.com/questions/21919962/angular-share-data-between-controllers
     */
    app.service('integrationService', Integration);

    //top-level controller for the integration viewmodel
    app.controller('IntegrationController', ['$scope', 'ModalService',  '$http', 'integrationService', '$q',  function($scope, ModalService, $http, integration, $q){
        var self = this,
            openModal;

        //fixme: remove later, expose viewmodel for debugging
        window.__viewModel= self;

        //controller public fields
        self.integration = integration;
        self.tab = 0;
        self.sharedOntologies = [];

        //'private' methods
        openModal = function(options) {
            ModalService.showModal({
                templateUrl: "workspace/modal-dialog.html",
                controller: "ModalDialogController",
                inputs: {
                    options: $.extend({categoryFilter: false}, options)
                }
            }).then(function(modal){
                //model.element is a jqSelection containing the top-level element for this control (e.g. $("#modalContainer")
                modal.element.modal();

                //model.close is a promise that is resolved when the modal closes
                modal.close.then(function(result){
                    //modal.element.modal('hide');
                    console.log("modal destroyed  result:%s", result);
                    $scope.message = result ? "result was yes" : "result was no";

                    if(options.close) {
                        options.close(result);
                    }
                });

            //if the service cannot create the modal or catches an exception, the promise calls an error callback
            }).catch(function(error) {
                console.error("ModalService error: %s", error);
            });
        };

        //controller public methods
        self.setTab  = function(idx) {
            this.tab = idx;
        }

        self.isTabSet = function(idx) {
            return this.tab === idx;
        }

        self.closeTab = function(idx) {
            integration.columns.splice(idx, 1);
        }

        self.saveClicked = function() {
            console.log("Saving.")
            console.log(JSON.stringify(integration, null, 4));
        };

        /**
         * Called after user selects list of dataset id's from 'add datasets' modal.
         * @param datasetIds
         */
        self.addDatasets = function(datasetIds) {
            if(datasetIds.length === 0) return;
            //TODO: create recursive 'unroll' function that emits params in struts-friendly syntax
            $http.get('/workspace/ajax/table-details?' + $.param({dataTableIds: datasetIds}, true)
            ).success(function(data) {
                _setAddAll(self.integration.datatables, data[0].dataTables, "data_table_id");
                self.integration.updateSharedOntologies(data[0].sharedOntologies);

                //gather the updated node participation data
                $scope.loadIntegrationColumnDetails(self.integration);
            });

        };

        //add and initialize an integration column associated with with the specified ontology ID
        var processAddedIntegrationColumns = function(ontologies) {
            console.debug("processAddedIntegrationColumns ::");
            ontologies.forEach(function(ontology){
                self.integration.addIntegrationColumn('intcol' + ontology.id, ontology);
            });
        };


        self.findDataTableColumns = function(integrationColumn) {
            var cols = [];
            var ontologyId = integrationColumn.ontologyId;
            //troll through each column of each datatable and determine which ones match the specified ontology in the integrationColumn
            self.integration.datatables.forEach(function(table) {
                console.debug("table:%s", table.display_name);
                table.columns.forEach(function(col){
                    if(ontologyId === col.id) {
                        console.log("\t matching ontology:%s table:%s  column:%s", ontologyId, table.display_name, col.name);
                        cols.push({
                            id: col.id,
                            name: col.name,
                            dataTableColumn: col,
                            dataTable: table
                        });
                    }
                });
            });
            return cols;
        };

        self.integrateClicked = function() {
            console.debug('integrate clicked');
            //FIXME: HACK: NEVERDOTHIS: This is absolutely not the correct way to invoke a form submission, for a number of reasons.
            $("#btnSubmitLegacyForm").click();

        };

        self.addDatasetsClicked = function(arg) {
            console.debug('Add Datasets clicked');
            openModal({
                title: "Add Datasets",
                searchType: "dataset",
                url: "/workspace/ajax/find-datasets",
                transformData: function(data) {
                    return data.map(function(item){
                        var result = item;
                        result.title = item.dataset_name;
                        result.title += ' - ' + item.data_table_name;
                        result.id = item.data_table_id;
                        result.submitter_display_name  = item.dataset.submitter.properName;
                        result.date_registered = item.dataset_date_created;
                        return result;
                    })
                },
                close: function(data) {
                    console.debug("datasetsClicked.close::");
                    self.addDatasets(data);
                }

            });
        };

        self.addIntegrationColumnsClicked = function(arg) {
            console.debug('Add Integration Columns Clicked');
            openModal({
                title: "Add Ontologies",
                searchType: "ontology",
                url: "/workspace/ajax/find-ontologies",
                categoryFilter: true,
                close: function(data) {
                    self.addIntegrationColumns(data);
                }
            });
        };

        self.addDisplayColumnClicked = function(arg) {
            integration.addDisplayColumn("display column");
        };

        self.removeSelectedDatasetClicked = function() {
            integration.removeDatatables($scope.selectedDatatables);
            _viewModelDirty();
        };

        self.addIntegrationColumnsMenuItemClicked = function(ontology) {
            self.integration.addIntegrationColumn(ontology.name, ontology);
        }

        $scope.lookupCompatibleColumns = function(id) {
            return self.integration.mappedDatatables[id];
        };

        $scope.loadIntegrationColumnDetails = function(integration) {
            //get column participation for all dataTableColumns across all shared ontologies
            var promises = [];
            var configs = [];

            integration.ontologies.forEach(function(ontology){
                var mappedCols = integration.getMappedDatatableColumns(ontology.id);
                var config = {};
                var params = {
                    "integrationColumn.columnType": "INTEGRATION",
                    "integrationColumn.sharedOntology.id": ontology.id
                };
                mappedCols.forEach(function(mappedCol, i){
                    params["integrationColumn.columns[" + i + "].id"] = mappedCol.id
                });
                config.params = params;
                configs.push(config);
                promises.push($http.get("/workspace/ajax/integration-column-details", config));
            });
            $q.all(promises).then(function(arResults){
                arResults.forEach(function(result, ontologyIdx){
                    var ontology = integration.ontologies[ontologyIdx];
                    var mappedCols = integration.getMappedDatatableColumns(ontology.id);
                    integration.updateNodeParticipationInfo(ontology, mappedCols, result.data);
                });
            }) ;
        }

        //FIXME: proper validation required
        $scope.isValid = function() {
            console.log("isValid:: %s", self.integration.columns.length);
            return self.integration.columns.length > 0;
        }

        /**
         * convenience method:  does the current ontologyNodeValue occur in the current datatableColumn.
         *
         * @param datatableColumnId
         */
        $scope.ontologyValuePresent = function(ontologyId, nodeIdx, dtcId) {
            var ontologyParticipation = self.integration.ontologyParticipation[ontologyId];
            var nodeInfo = ontologyParticipation.nodeInfoList[nodeIdx];
            return nodeInfo.colIds.indexOf(dtcId) > -1;
        }

        /**
         * If the user modifies the list of datatables it will put the viewModel in an inconsistent state.   This method returns the viewModel to a consistent
         * state with as little destruction as possible.
         *
         * @private
         */
        var _viewModelDirty = function() {
            console.log('viewModelDirty::');
            self.integration.ontologies.length = 0;

            //hack: rebuild shared ontologies.
            var datasetIds = [];
            datasetIds = self.integration.datatables.map(function(dt){return dt.data_table_id});
            $http.get('/workspace/ajax/table-details?' + $.param({dataTableIds: datasetIds}, true))
                    .success(function(data){
                        console.debug("resetting list of shared ontologies");
                        console.table(data[0].sharedOntologies);
                        self.integration.ontologies = data[0].sharedOntologies;
                    });

            //TODO: handle integration columns for ontologies that are no longer shared (e.g. if a user adds a datatable)
            self.integration.columns = []
            self.setTab(0);

            //TODO: handle display column when source datatableColumn no longer exists
        };


    }]);

/* global jQuery, angular  */
})(jQuery, angular, console);
