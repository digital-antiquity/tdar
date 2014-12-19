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
     * Create a subset of a specified array that does not contain duplicates (using object identity for equality)
     *
     * @param items:{Array}
     * @returns {Array}
     * @private
     */
    function _dedupe(items) {
        var uniqueItems = [];
        items.forEach(function(item){
            if(uniqueItems.indexOf(item) === -1) {
                uniqueItems.push(item);
            }
        })
        return uniqueItems;
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
    function Integration(dataService) {
        console.debug("Integration()::");
        console.debug(dataService);

        var self = this;
        var _sharedOntologyIds = null;

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
        //fixme: rename to outputColumns. We've got too many "columns" around here to be ambiguous.
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

        /** transient datatable participation information, keyed by ontologyId -> Array<DatatableInfo> **/
        self.mappedDatatables = {};

        /** transient ontology node participation information **/
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
        function _buildMappedDatatables() {
            self.mappedDatatables = self.getMappedDatatables();
        }

        self.getMappedDatatableColumns =  function _getMappedDatatableColumns(ontologyId) {
            var cols = [];
            self.mappedDatatables[ontologyId].forEach(function(compatTable){
                cols = cols.concat(compatTable.compatCols);
            });
            return cols;
        };


        self.getMappedDatatables = function() {
            var mappedTables = {};
            _getSharedOntologyIds().forEach(function(ontologyId){
                //list of maps
                var compatTables = [];

                self.datatables.forEach(function(datatable) {
                    compatTables.push({
                        datatable: datatable,
                        compatCols:datatable.dataTableColumns.filter(function(col){
                            if (col == undefined || col.mappedOntology == undefined) {
                                return false;
                            }
                            return col.mappedOntology.id === ontologyId;
                        })
                    });
                });
                mappedTables[ontologyId] = compatTables;
            });
            return mappedTables;
        };




        /**
         * Append an 'integration column' to the columns list.
         * @param title
         * @param ontology
         * @returns {{type: string, title: *, ontologyId: *, nodeSelections: Array, datatableColumnIds: Array}}
         */
        self.addIntegrationColumn =  function _addIntegrationColumn(title, ontology) {
            var col = {
                type: "integration",
                title: title,
                ontologyId: ontology.id,
                ontology: ontology,
                nodeSelections: [],
                isValidMapping: true,
                selectedDatatableColumns: null
            };

            //fixme: try to keep model devoid of angular conventions (angular excludes $-prefix when evaluating/copying objects)
            //initialize, or update,  the selected datatable column selections.
            col.$getSelectedDatatableColumns = function() {
                if(this.selectedDatatableColumns === null) this.selectedDatatableColumns = [];
                if(this.selectedDatatableColumns.length !== self.datatables.length) {
                    //todo: if we're updating thie structure (e.g. adding a datatable to integration workflow) try to retain the previous selections

                    var mappedTables = self.getMappedDatatables()[ontology.id];
                    if(!mappedTables) {
                        mappedTables = [];
                    }

                    this.selectedDatatableColumns = mappedTables.map(function(dt){
                        if(!dt.compatCols.length) return null;
                        return dt.compatCols[0];
                    });
                }
            };
            //init selected columns
            col.$getSelectedDatatableColumns();

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
            _buildMappedDatatables();
        };

        /**
         * Remove specified datatables from the viewmodel.  This impacts all integration columns and any display columns that have datatableColumns
         * which belong to any of the specified datatables.
         * @param datatables
         * @private
         */
        self.removeDatatables = function _removeDatatables(datatables) {
            console.debug("removeDatatables::");

            if(!datatables) {return;}
            if(datatables.length === 0){return;}

            datatables.forEach(function(datatable) {
                _setRemove(self.datatables, datatable);
            });

            _datatablesRemoved(datatables);
            _rebuildSharedOntologies();

        }

        function _getOutputColumns(type) {
            return self.columns.filter(function(outputColumn){return outputColumn.type===type});
        }

        /**
         * Returns just the displayColumns from the list of outputColumns
         * @private
         */
        function _getDisplayColumns() {
            return _getOutputColumns("display");
        }

        /**
         * Returns just the displayColumns from the list of outputColumns
         * @private
         */
        function _getCountColumns() {
            return _getOutputColumns("count");
        }

        /**
         * Returns just the integrationColumns from the outputColumns
         * @private
         */
        function _getIntegrationColumns() {
            return _getOutputColumns("integration");
        }

        //update derived properties when user removes a datatable
        function _datatablesRemoved(removedDatatables) {
            console.debug("_datatablesRemoved::");
            var removedDatatableColumnIds = [];
            removedDatatables.forEach(function(datatable){
               datatable.dataTableColumns.forEach(function(column){
                   removedDatatableColumnIds.push(column.id);
               });
            });

            //todo: if any integration columns, update selected datatableColumn
            //todo: remove any paricipating datatableColumns that belong to the datatable we are removing

            //clean up the mappedDatatables
            for(var ontologyId in self.mappedDatatables) {
                self.mappedDatatables[ontologyId] = self.mappedDatatables[ontologyId].filter(function(mappedDatatable){
                    return removedDatatables.indexOf(mappedDatatable.datatable) === -1;
                });
            }

            //update any affected integration output columns
            _getIntegrationColumns().forEach(function(integrationColumn) {

                //clean up integrationColumn.selectedDatatableColumn
                integrationColumn.selectedDatatableColumns = integrationColumn.selectedDatatableColumns.filter(function(datatableColumn){
                    return removedDatatableColumnIds.indexOf(datatableColumn.id) === -1
                });

                //clean up the nodeParticipation information
                for(var ontologyId in self.ontologyParticipation) {
                    var ontologyParticipation = self.ontologyParticipation[ontologyId];
                    var nodeInfoList = ontologyParticipation.nodeInfoList;
                    nodeInfoList.forEach(function(nodeInfo){
                        //remove any columnIds that belong to the datatables we are removing
                        nodeInfo.colIds = nodeInfo.colIds.filter(function(colId){
                            var idx = removedDatatableColumnIds.indexOf(colId);
                            if(idx === -1) {
                                console.debug("removing %s from nodeInfo.colIds", colId);
                            }
                            return idx === -1;
                        });
                    });
                }
            });

            //if any display columns, remove all affected datatableColumnSelections
            _getDisplayColumns().forEach(function(displayColumn){
                displayColumn.datatableColumnSelections = displayColumn.datatableColumnSelections.filter(function(datatableColumnSelection){
                    return removedDatatables.indexOf(datatableColumnSelection.datatable) === -1;
                });
            });

            //if any display columns, remove all affected datatableColumnSelections
            _getCountColumns().forEach(function(displayColumn){
                displayColumn.datatableColumnSelections = displayColumn.datatableColumnSelections.filter(function(datatableColumnSelection){
                    return removedDatatables.indexOf(datatableColumnSelection.datatable) === -1;
                });
            });
}

        function _datatablesAdded(addedDatatables) {
            console.debug("_datatablesAdded::");

            //Step 1: account for integration columns that refer to ontologies that are no longer shared by all of the datatables
            //Calculate the new list of shared ontologies, find out if any ontologies should
            var currentSharedOntologyIds = self.ontologies.map(function(ontology){return ontology.id});
            var newSharedOntologyIds = _getSharedOntologyIds();
            //var defunctOntologyIds = currentSharedOntologyIds.filter(function(ontologyId){return newSharedOntologyIds.indexOf(ontologyId) === -1});
            _sharedOntologiesUpdated(newSharedOntologyIds, currentSharedOntologyIds);

            //Step 2: account for integration columns that refer to still-shared ontologies
            _buildMappedDatatables();
            //update selected datatableColumns
            _getIntegrationColumns().forEach(function(integrationColumn){integrationColumn.$getSelectedDatatableColumns()});
            //todo: need to update the nodeParticipation information for all the integrationColumns that aren't removed




            //Step 3: account for display columns that need an additional selectedDatatableColumn entry.
            //todo: need to update the selectedDatatables information for all displayColumns

        }

        /**
         * Called if the user implicitly/explitly modifies the current shared ontologies
         * @param newSharedOntologyIds
         * @param oldSharedOntologyIds
         * @private
         */
        function _sharedOntologiesUpdated(newSharedOntologyIds, oldSharedOntologyIds) {
            console.debug("_sharedOntologiesUpdated::", newSharedOntologyIds);
            var invalidIntegrationColumns = _getIntegrationColumns().filter(function(column){
                return newSharedOntologyIds.indexOf(column.ontologyId) === -1;
            });
        }

        function _getSharedOntologyIds() {
            //todo: punch jim in the face for writing this 'one-liner'
            var ids =  _dedupe(self.datatables
                    //reduce the list of all datatables into a list of all datatableColumns
                    .reduce(function(a,b){return a.concat(b.dataTableColumns)}, [])
                    //and then filter-out the unmapped columns
                    .filter(function(col){
                        return !(col == undefined) && col.actuallyMapped})
                    //then convert that list of columns to a list of ids
                    .map(function(c){return c.mappedOntology.id})
            )
                // We now have a deduped list of all mapped ontology id's,
                // Now we remove the ids that do not appear in every datatable at least once.
                .filter(function(ontologyId){
                    return self.datatables.every(function(datatable){
                        return datatable.dataTableColumns.some(function(dtc){return ontologyId === dtc.mappedOntology.id});
                    });
                })
            //And... scene!  Here are your shared ontology id's.
            _sharedOntologyIds = ids;
            return _sharedOntologyIds;
        }

        /**
         * Return an array of ongologies shared by the datatables used by this integration.
         * @return Array<ontology>
         * @private
         */
        function _getSharedOntologies() {
            return dataService.getCachedOntologies(_getSharedOntologyIds());
        }

        /**
         * Replace the current list of ontologies w/ a computed list of shared ontologies
         * @private
         */
        function _rebuildSharedOntologies() {
            //todo: only rebuild if we detect a change
            self.ontologies = _getSharedOntologies();

            //update integrationColumnStatus
            //fixme: this really should be a computed property on integrationColumn.
            //fixme: integrationColumn should be a proper IntegrationColumn class w/ methods and stuff.
            _getIntegrationColumns().forEach(function(integrationColumn) {
                integrationColumn.isValidMapping = (_sharedOntologyIds.indexOf(integrationColumn.ontologyId) > -1);
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

        /**
         * Add a 'count column'
         */
        self.addCountColumn = function _addCountColumn(title) {

            var countColumn = {
                type: 'count',
                title: title,
                datatableColumnSelections: []
            };

            self.datatables.forEach(function(table){
                var selection = {
                    datatable: table,
                    datatableColumn: null
                }
                countColumn.datatableColumnSelections.push(selection);
            });
            self.columns.push(countColumn);
        };
        
        self.isCountColumnEnabled = function() {
            var cols = _getCountColumns();
            if (cols == undefined || cols.length == 1) {
                return false;
            }
            var hasCountColumn = false;
            self.datatables.forEach(function(table){
                table.dataTableColumns.forEach(function(col){
                    if (col.columnEncodingType == 'COUNT') {
                        hasCountColumn = true;
                    }
                });
            });
            return hasCountColumn;
        };


        self.updateNodeParticipationInfo = function _updateParticipationInfo(ontology, mappedCols, arNodeInfo) {
            var ontologyParticipation = {
                ontology:ontology,
                nodeInfoList: []
            };

            arNodeInfo.forEach(function(nodeInfo, nodeIdx) {
                var arbPresent = nodeInfo.columnHasValueArray;
                var nodeParticipation = {
                    node: ontology.nodes[nodeIdx],
                    colIds: mappedCols.filter(function(b,i){return arbPresent[i]}).map(function(col){return col.id})
                };
                ontologyParticipation.nodeInfoList.push(nodeParticipation);
            });
            self.ontologyParticipation[ontology.id] = ontologyParticipation;
        };

        self.addDatatables = function(datatables) {
            _setAddAll(self.datatables, datatables, "id");
            _datatablesAdded(datatables)
            _rebuildSharedOntologies();
        };

        /**
         * remove the outputColumn at the specified index
         * @param idx
         * @private
         */
        self.removeOutputColumn = function _removeOutputColumn(idx) {
            self.columns.splice(idx, 1);
        };

    }

    //expose a singleton instance to the application
    app.service("IntegrationService",["DataService", Integration]);

    ////expose the class to the application in case another component wants to create a unique instance
    //app.value("IntegrationClass", Integration);

})(angular)