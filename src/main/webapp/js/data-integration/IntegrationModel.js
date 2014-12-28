(function(angular){
    "use strict";

    var app = angular.module("integrationApp");

    function _restore() {

    }

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
         * List of dataTables included in the integration.
         * @type {Array}
         */
        self.dataTables = [];

        /**
         * List of ontologies that the user may integrate over. To be added as an integration column,   the ontology must be "shared" by all of this integration
         * object's dataTables.  That is, the system can only create an integration column for a given ontology if, for every dataTable in Integration#dataTables, there is at least one
         * datataneColumn with a default_ontology_id that equals ontology.id.
         * @type {Array}
         */
        self.ontologies = [];

        /** transient dataTable participation information, keyed by ontologyId -> Array<DatatableInfo> **/
        self.mappedDatatables = {};

        /** transient ontology node participation information **/
        //self.ontologyParticipation = {};

        /**
         * Compute, then cache,  the mappedDatatables structure.
         * @private
         */
        function _buildMappedDatatables() {
            self.mappedDatatables = self.getMappedDatatables();
        }

        /**
         * Return a list of dataTable columns that have a mapped ontology.  If caller specifies ontologyId,  filter the list to columns that map
         * to the specified ontologyId.
         *
         * @param {number} [ontologyId]
         * @returns {*}
         * @private
         */
        self.getMappedDatatableColumns =  function _getMappedDatatableColumns(ontologyId) {
            var mappedCols = self.dataTables.reduce(function(cols, dataTable){
                return cols.concat(dataTable.dataTableColumns.filter(function(col){
                            return !!col.mappedOntologyId;
                        }
                ))}, []);
            if(typeof ontologyId !== "undefined") {
                mappedCols = mappedCols.filter(function(col){return col.mappedOntologyId === ontologyId});
            }
            return mappedCols;
        };


        self.getMappedDatatables = function() {
            var mappedTables = {};
            _getSharedOntologyIds().forEach(function(ontologyId){
                //list of maps
                var compatTables = [];

                self.dataTables.forEach(function(dataTable) {
                    compatTables.push({
                        dataTable: dataTable,
                        compatCols:dataTable.dataTableColumns.filter(function(col){
                            return col.mappedOntologyId === ontologyId;
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
         * @returns {{type: string, title: *, ontologyId: *, nodeSelections: Array, dataTableColumnIds: Array}}
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
            //initialize, or update,  the selected dataTable column selections.
            col.$getSelectedDatatableColumns = function() {
                if(this.selectedDatatableColumns === null) this.selectedDatatableColumns = [];
                if(this.selectedDatatableColumns.length !== self.dataTables.length) {
                    //todo: if we're updating thie structure (e.g. adding a dataTable to integration workflow) try to retain the previous selections

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
         * Remove specified dataTables from the viewmodel.  This impacts all integration columns and any display columns that have dataTableColumns
         * which belong to any of the specified dataTables.
         * @param dataTables
         * @private
         */
        self.removeDatatables = function _removeDatatables(dataTables) {
            console.debug("removeDatatables::");

            if(!dataTables) {return;}
            if(dataTables.length === 0){return;}

            dataTables.forEach(function(dataTable) {
                _setRemove(self.dataTables, dataTable);
            });

            _dataTablesRemoved(dataTables);
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

        //update derived properties when user removes a dataTable
        function _dataTablesRemoved(removedDatatables) {
            console.debug("_dataTablesRemoved::");
            var removedDatatableColumnIds = [];
            removedDatatables.forEach(function(dataTable){
               dataTable.dataTableColumns.forEach(function(column){
                   removedDatatableColumnIds.push(column.id);
               });
            });

            //todo: if any integration columns, update selected dataTableColumn
            //todo: remove any paricipating dataTableColumns that belong to the dataTable we are removing

            //clean up the mappedDatatables
            for(var ontologyId in self.mappedDatatables) {
                self.mappedDatatables[ontologyId] = self.mappedDatatables[ontologyId].filter(function(mappedDatatable){
                    return removedDatatables.indexOf(mappedDatatable.dataTable) === -1;
                });
            }

            //update any affected integration output columns
            _getIntegrationColumns().forEach(function(integrationColumn) {

                //clean up integrationColumn.selectedDatatableColumn
                integrationColumn.selectedDatatableColumns = integrationColumn.selectedDatatableColumns.filter(function(dataTableColumn){
                    return removedDatatableColumnIds.indexOf(dataTableColumn.id) === -1
                });

            });

            //if any display columns, remove all affected dataTableColumnSelections
            _getDisplayColumns().forEach(function(displayColumn){
                displayColumn.dataTableColumnSelections = displayColumn.dataTableColumnSelections.filter(function(dataTableColumnSelection){
                    return removedDatatables.indexOf(dataTableColumnSelection.dataTable) === -1;
                });
            });

            //if any display columns, remove all affected dataTableColumnSelections
            _getCountColumns().forEach(function(displayColumn){
                displayColumn.dataTableColumnSelections = displayColumn.dataTableColumnSelections.filter(function(dataTableColumnSelection){
                    return removedDatatables.indexOf(dataTableColumnSelection.dataTable) === -1;
                });
            });
}

        function _dataTablesAdded(addedDatatables) {
            console.debug("_dataTablesAdded::");

            addedDatatables.forEach(function(dataTable) {
                console.log(dataTable);
            });
            //Step 1: account for integration columns that refer to ontologies that are no longer shared by all of the dataTables
            //Calculate the new list of shared ontologies, find out if any ontologies should
            var currentSharedOntologyIds = self.ontologies.map(function(ontology){return ontology.id});
            var newSharedOntologyIds = _getSharedOntologyIds();
            //var defunctOntologyIds = currentSharedOntologyIds.filter(function(ontologyId){return newSharedOntologyIds.indexOf(ontologyId) === -1});
            _sharedOntologiesUpdated(newSharedOntologyIds, currentSharedOntologyIds);

            //Step 2: account for integration columns that refer to still-shared ontologies
            _buildMappedDatatables();
            //update selected dataTableColumns
            _getIntegrationColumns().forEach(function(integrationColumn){integrationColumn.$getSelectedDatatableColumns()});

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
            var ids =  dataService.dedupe(self.dataTables
                    //reduce the list of all dataTables into a list of all dataTableColumns
                    .reduce(function(a,b){return a.concat(b.dataTableColumns)}, [])
                    //and then filter-out the unmapped columns
                    .filter(function(col){return !!col.mappedOntologyId})
                    //then convert that list of columns to a list of ids
                    .map(function(c){return c.mappedOntologyId})
            )
                // We now have a deduped list of all mapped ontology id's,
                // Now we remove the ids that do not appear in every dataTable at least once.
                .filter(function(ontologyId){
                    return self.dataTables.every(function(dataTable){
                        return dataTable.dataTableColumns.some(function(dtc){return ontologyId === dtc.mappedOntologyId});
                    });
                })
            //And... scene!  Here are your shared ontology id's.
            _sharedOntologyIds = ids;
            return _sharedOntologyIds;
        }

        /**
         * Return an array of ongologies shared by the dataTables used by this integration.
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
            self.ontologies = _getSharedOntologies();

            //update integrationColumnStatus
            //fixme: this really should be a computed property on integrationColumn.
            //fixme: integrationColumn should be a proper IntegrationColumn class w/ methods and stuff.
            _getIntegrationColumns().forEach(function(integrationColumn) {
                integrationColumn.isValidMapping = (_sharedOntologyIds.indexOf(integrationColumn.ontologyId) > -1);
            });
        }
        
        

        /**
         * Add a 'display column' to the columns list.  A display column contains a list of dataTableColumn selections, which the system
         * will include in the final integration report file.   The user may choose 0 or 1 dataTableColumn from each
         * dataTable.  This method primes the dataTableColumnSelections list to be size N with each slot containing null (where
         * N is the count of data tables in the current integration workflow.
         * @param title
         * @private
         */
        self.addDisplayColumn = function _addDisplayColumn(title) {

            var displayColumn = {
                type: 'display',
                title: title,
                dataTableColumnSelections: []
            };

            self.dataTables.forEach(function(table){
                var selection = {
                    dataTable: table,
                    dataTableColumn: null
                }
                displayColumn.dataTableColumnSelections.push(selection);
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
                dataTableColumnSelections: []
            };

            self.dataTables.forEach(function(table){
                var selection = {
                    dataTable: table,
                    dataTableColumn: null
                }
                countColumn.dataTableColumnSelections.push(selection);
            });
            self.columns.push(countColumn);
        };
        
        self.isCountColumnEnabled = function() {
            var cols = _getCountColumns();
            if (cols == undefined || cols.length == 1) {
                return false;
            }
            var hasCountColumn = false;
            self.dataTables.forEach(function(table){
                table.dataTableColumns.forEach(function(col){
                    if (col.columnEncodingType == 'COUNT') {
                        hasCountColumn = true;
                    }
                });
            });
            return hasCountColumn;
        };

        //self.updateNodeParticipationInfo = function _updateParticipationInfo(ontology, mappedCols, arNodeInfo) {
        //    var ontologyParticipation = {
        //        ontology:ontology,
        //        nodeInfoList: []
        //    };
        //
        //    arNodeInfo.forEach(function(nodeInfo, nodeIdx) {
        //        var arbPresent = nodeInfo.columnHasValueArray;
        //        var nodeParticipation = {
        //            node: ontology.nodes[nodeIdx],
        //            colIds: mappedCols.filter(function(b,i){return arbPresent[i]}).map(function(col){return col.id})
        //        };
        //        ontologyParticipation.nodeInfoList.push(nodeParticipation);
        //    });
        //    self.ontologyParticipation[ontology.id] = ontologyParticipation;
        //};

        /**
         * Gather any dataTableColumns that have missing participation information and tell dataService to fetch it.
         * @private
         */
        var _loadUpdatedParticipationInformation = function() {
            //find dataTableColumns that are missing transientNodeParticipation,  request it from dataService
            var unprocessedDatatableColumns = self.getMappedDatatableColumns().filter(function(col){return !col.transientNodeParticipation});
            var unprocessedIds  = unprocessedDatatableColumns.map(function(col) {return col.id});
            if(unprocessedIds.length === 0) { return}

            //todo: signal to the UI we are waiting on data for these columns
            //fixme: jtd full-disclosure,  I'm torn on whether the angular controller should be loading this data or whether the 'model' should be.  leaving be for now.
            dataService.loadNodeParticipation(unprocessedIds).then(function(){
                //todo: signal to the UI that these columns are ready
            });
        };

        /**
         * Add specified dataTable to the viewModel.dataTables array. Duplicates are ignored.  This is the recommended way of manipulating this list, as
         * opposed to direct array manipulation.
         * @param dataTables
         */
        self.addDatatables = function(dataTables) {
            _setAddAll(self.dataTables, dataTables, "id");
            _dataTablesAdded(dataTables)
            _rebuildSharedOntologies();
            _loadUpdatedParticipationInformation();
        };

        /**
         * Return 'tri-state' value indicating the participation status of the specified node w.r.t. the specified dataTableColumn.  True means the node
         * is present in the column, False means it does not, and NULL means that IntegrationModel is currently loading participation data for
         * the specified dataTableColumn.
         * @param dataTableColumn
         * @param {object} ontologyNode
         * @private
         */
        self.isNodePresent = function _isNodePresent(dataTableColumn, ontologyNode) {
            if(!dataTableColumn.transientNodeParticipation) return null;
            return dataTableColumn.transientNodeParticipation.indexOf(ontologyNode) >= 0;
        }


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