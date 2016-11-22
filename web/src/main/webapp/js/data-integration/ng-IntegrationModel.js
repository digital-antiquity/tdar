(function(angular) {
    "use strict";

    var app = angular.module("integrationApp");

    /**
     * Add specified object to specified array if specified array does not already contain the object.
     * 
     * @param arr
     *            an array of objects
     * @param obj
     *            the object to potentially add to the array
     * @param propName
     *            the property to consider when looking for duplicate items. If undefined, function uses strict identity to find dupes.
     * @private
     */
    function _setAdd(arr, obj, propName) {
        var fn, dupes = [];
        if (propName) {
            fn = function(testObj) {
                return testObj[propName] === obj[propName]
            };
        } else {
            fn = function(testObj) {
                return testObj === obj;
            }
        }
        dupes = arr.filter(fn);
        if (dupes.length === 0) {
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
     * 
     * @param arr
     * @param item
     * @returns {*} original, mutated array
     * @private
     */
    function _setRemove(arr, item) {
        if (arr.indexOf(item) === -1)
            return arr;
        arr.splice(arr.indexOf(item), 1);
        return arr;
    }

    // Our integration model
    function Integration(dataService) {
        //console.debug("Integration()::");
        //console.debug(dataService);

        var self = this;
        var _sharedOntologyIds = null;

        /**
         * Name for the current integration workflow, specified by the user. This is for organizational purposes only(e.g. picking a previous integration out of
         * a list). This name does not appear in the final integration report.
         * 
         * @type {string}
         */
        self.title = "";

        /**
         * Description for the current workflow. For organizational purposes (it does not appear in final integration report).
         * 
         * @type {string}
         */
        self.description = "";

        /**
         * This list describes columns in the final integration results. Here a column may be either an "integration" column or a "display" column.
         * 
         * @type {Array}
         */
        // fixme: rename to outputColumns. We've got too many "columns" around here to be ambiguous.
        self.columns = [];

        /**
         * List of dataTables included in the integration.
         * 
         * @type {Array}
         */
        self.dataTables = [];

        self.id = -1;

        /**
         * List of ontologies that the user may integrate over. To be added as an integration column, the ontology must be "shared" by all of this integration
         * object's dataTables. That is, the system can only create an integration column for a given ontology if, for every dataTable in
         * Integration#dataTables, there is at least one datataneColumn with a default_ontology_id that equals ontology.id.
         * 
         * @type {Array}
         */
        self.ontologies = [];

        /** transient dataTable participation information, keyed by ontologyId -> Array<DataTableInfo> * */
        self.mappedDataTables = {};

        /** transient ontology node participation information * */
        // self.ontologyParticipation = {};
        /**
         * Compute, then cache, the mappedDataTables structure.
         * 
         * @private
         */
        function _buildMappedDataTables() {
            self.mappedDataTables = self.getMappedDataTables();
        }

        /**
         * Return a list of dataTable columns that have a mapped ontology. If caller specifies ontologyId, filter the list to columns that map to the specified
         * ontologyId.
         * 
         * @param {number}
         *            [ontologyId]
         * @returns {*}
         * @private
         */
        self.getMappedDataTableColumns = function _getMappedDataTableColumns(ontologyId) {
            var mappedCols = self.dataTables.reduce(function(cols, dataTable) {
                return cols.concat(dataTable.dataTableColumns.filter(function(col) {
                    return !!col.mappedOntologyId;
                }))
            }, []);
            if (typeof ontologyId !== "undefined") {
                mappedCols = mappedCols.filter(function(col) {
                    return col.mappedOntologyId === ontologyId
                });
            }
            return mappedCols;
        };

        self.getMappedDataTables = function() {
            var mappedTables = {};
            self.getSharedOntologyIds().forEach(function(ontologyId) {
                // list of maps
                var compatTables = [];

                self.dataTables.forEach(function(dataTable) {
                    compatTables.push({
                        dataTable : dataTable,
                        compatCols : dataTable.dataTableColumns.filter(function(col) {
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
         * 
         * @param title
         * @param ontology
         * @returns {{type: string, title: *, ontologyId: *, nodeSelections: Array, dataTableColumnIds: Array}}
         */
        self.addIntegrationColumn = function _addIntegrationColumn(title, ontology) {
            var col = {
                type : "integration",
                name : title,
                ontologyId : ontology.id,
                ontology : ontology,
                nodeSelections : [],
                isValidMapping : true,
                selectedDataTableColumns : null
            };

            // fixme: try to keep model devoid of angular conventions (angular excludes $-prefix when evaluating/copying objects)
            /**
             * Initialize, or update, the selected dataTable column selections.  Each columnSelection object contains a 'dataTable' and a 'dataTableColumn'
             * property. If a dataTable has no compatible dataTableColumns,  selection.dataTableColumn is null.
             *
             */
            col.$getSelectedDataTableColumns = function() {
                if (this.selectedDataTableColumns === null)
                    this.selectedDataTableColumns = [];
                if (this.selectedDataTableColumns.length !== self.dataTables.length) {
                    // todo: if we're updating thie structure (e.g. adding a dataTable to integration workflow) try to retain the previous selections

                    var mappedTables = self.getMappedDataTables()[ontology.id];
                    if (!mappedTables) {
                        mappedTables = [];
                    }

                    this.selectedDataTableColumns = mappedTables.map(function(dt) {
                        var columnSelection = {
                            dataTable: dt,
                            dataTableColumn: null
                        };

                        if (dt.compatCols.length)
                            columnSelection.dataTableColumn = dt.compatCols[0];
                        return columnSelection;
                    });
                }
            };
            // init selected columns
            col.$getSelectedDataTableColumns();

            col.nodeSelections = ontology.nodes.map(function(node, i) {
                return {
                    selected : false,
                    node : node,
                    nodeIndex : i
                }
            });
            self.columns.push(col);
            return col;
        };

        self.updateSharedOntologies = function _updateSharedOntologies(ontologies) {
            _setAddAll(self.ontologies, ontologies, "id");
            _buildMappedDataTables();
        };

        /**
         * Remove specified dataTables from the viewmodel. This impacts all integration columns and any display columns that have dataTableColumns which belong
         * to any of the specified dataTables.
         * 
         * @param dataTables
         * @private
         */
        self.removeDataTables = function _removeDataTables(dataTables) {
            //console.debug("removeDataTables::");

            if (!dataTables) {
                return;
            }
            if (dataTables.length === 0) {
                return;
            }

            dataTables.forEach(function(dataTable) {
                _setRemove(self.dataTables, dataTable);
            });

            _dataTablesRemoved(dataTables);
        }

        function _getOutputColumns(type) {
            return self.columns.filter(function(outputColumn) {
                return outputColumn.type === type
            });
        }

        /**
         * Returns just the displayColumns from the list of outputColumns
         * 
         * @private
         */
        function _getDisplayColumns() {
            return _getOutputColumns("display");
        }

        /**
         * Returns just the displayColumns from the list of outputColumns
         * 
         * @private
         */
        function _getCountColumns() {
            return _getOutputColumns("count");
        }

        self.getCountColumns  = function(){
            return _getOutputColumns("count");
        }

        /**
         * Returns just the integrationColumns from the outputColumns
         * 
         * @private
         */
        self.getIntegrationColumns = function getIntegrationColumns() {
            return _getOutputColumns("integration");
        }

        // update derived properties when user removes a dataTable
        function _dataTablesRemoved(removedDataTables) {
            //console.debug("_dataTablesRemoved::");
            var removedDataTableColumnIds = [];
            removedDataTables.forEach(function(dataTable) {
                dataTable.dataTableColumns.forEach(function(column) {
                    removedDataTableColumnIds.push(column.id);
                });
            });

            // todo: if any integration columns, update selected dataTableColumn
            // todo: remove any paricipating dataTableColumns that belong to the dataTable we are removing

            // clean up the mappedDataTables
            for ( var ontologyId in self.mappedDataTables) {
                self.mappedDataTables[ontologyId] = self.mappedDataTables[ontologyId].filter(function(mappedDataTable) {
                    return removedDataTables.indexOf(mappedDataTable.dataTable) === -1;
                });
            }

            // update any affected integration output columns
            self.getIntegrationColumns().forEach(function(integrationColumn) {

                // clean up integrationColumn.selectedDataTableColumn
                integrationColumn.selectedDataTableColumns = integrationColumn.selectedDataTableColumns.filter(function(selectedDataTableColumn) {
                    return removedDataTables.indexOf(selectedDataTableColumn.dataTable) == -1;
                    //return removedDataTableColumnIds.indexOf(dataTableColumn.id) === -1
                });

            });

            // if any display columns, remove all affected dataTableColumnSelections
            _getDisplayColumns().forEach(function(displayColumn) {
                displayColumn.dataTableColumnSelections = displayColumn.dataTableColumnSelections.filter(function(dataTableColumnSelection) {
                    return removedDataTables.indexOf(dataTableColumnSelection.dataTable) === -1;
                });
            });

            // if any display columns, remove all affected dataTableColumnSelections
            _getCountColumns().forEach(function(displayColumn) {
                displayColumn.dataTableColumnSelections = displayColumn.dataTableColumnSelections.filter(function(dataTableColumnSelection) {
                    return removedDataTables.indexOf(dataTableColumnSelection.dataTable) === -1;
                });
            });
        }

        function _dataTablesAdded(addedDataTables) {
            //console.debug("_dataTablesAdded::");

            // Step 1: account for integration columns that refer to ontologies that are no longer shared by all of the dataTables
            // Calculate the new list of shared ontologies, find out if any ontologies should
            var currentSharedOntologyIds = self.ontologies.map(function(ontology) {
                console.log('shared ontology id:{}', ontology.id);
                return ontology.id
            });
            var newSharedOntologyIds = self.getSharedOntologyIds();
            // var defunctOntologyIds = currentSharedOntologyIds.filter(function(ontologyId){return newSharedOntologyIds.indexOf(ontologyId) === -1});
            _sharedOntologiesUpdated(newSharedOntologyIds, currentSharedOntologyIds);

            // Step 2: account for integration columns that refer to still-shared ontologies
            _buildMappedDataTables();
            // update selected dataTableColumns
            self.getIntegrationColumns().forEach(function(integrationColumn) {
                integrationColumn.$getSelectedDataTableColumns()
            });
        }

        /**
         * Called if the user implicitly/explitly modifies the current shared ontologies
         * 
         * @param newSharedOntologyIds
         * @param oldSharedOntologyIds
         * @private
         */
        function _sharedOntologiesUpdated(newSharedOntologyIds, oldSharedOntologyIds) {
            //console.debug("_sharedOntologiesUpdated::", newSharedOntologyIds);
            var invalidIntegrationColumns = self.getIntegrationColumns().filter(function(column) {
                return newSharedOntologyIds.indexOf(column.ontologyId) === -1;
            });
        }

        /**
         * Rebuild any computed/cached fields in this object
         */
        self.reset = function() {
            //rely on the fact that  _dataTablesAdded will rebuild cached fields even if no tables actually added
            _dataTablesAdded([]);
        }

        /**
         * Return the list of "shared" ontology id's.  An ontology is considered to be shared if it is mapped to at
         * least two data table columns in our list of data tables.
         * @returns {*}
         */
        self.getSharedOntologyIds = function() {
            var ids;
            var mappedOntologyIds = dataService.dedupe(self.dataTables
                // reduce the list of all dataTables into a list of all dataTableColumns
                .reduce(function(a, b) {
                    return a.concat(b.dataTableColumns)
                }, [])
                // and then filter-out the unmapped columns
                .filter(function(col) {
                    return !!col.mappedOntologyId
                })
                // then convert that list of columns to a list of ids
                .map(function(c) {
                    return c.mappedOntologyId;
                }));

                ids = mappedOntologyIds.filter(function(ontologyId) {
                    var participatingDataTables = self.dataTables.filter(function(dataTable){
                        return dataTable.dataTableColumns.some(function(dtc){
                            //console.log('ontologyId === dtc.mappedOntologyId: {}', ontologyId === dtc.mappedOntologyId);
                            return ontologyId === dtc.mappedOntologyId;
                        });
                    });
                    return participatingDataTables.length > 0;
                });

            // We now have a deduped list of all mapped ontology id's,
            // Now we remove the ids that do not appear in at least two data tables.
            // And... scene! Here are your shared ontology id's.
            _sharedOntologyIds = ids;
            return _sharedOntologyIds;
        };


        /**
         * Add a 'display column' to the columns list. A display column contains a list of dataTableColumn selections, which the system will include in the
         * final integration report file. The user may choose 0 or 1 dataTableColumn from each dataTable. This method primes the dataTableColumnSelections list
         * to be size N with each slot containing null (where N is the count of data tables in the current integration workflow.
         * 
         * @param title
         * @private
         */
        self.addDisplayColumn = function _addDisplayColumn(title) {

            var displayColumn = {
                type : 'display',
                name : title,
                dataTableColumnSelections : []
            };

            self.dataTables.forEach(function(table) {
                var selection = {
                    dataTable : table,
                    dataTableColumn : null
                };
                displayColumn.dataTableColumnSelections.push(selection);
            });
            self.columns.push(displayColumn);
            return displayColumn;
        };

        /**
         * Add a 'count column'
         */
        self.addCountColumn = function _addCountColumn(title) {

            var countColumn = {
                type : 'count',
                name : title,
                dataTableColumnSelections : []
            };

            self.dataTables.forEach(function(table) {
                var selection = {
                    dataTable : table,
                    dataTableColumn : null
                }
                countColumn.dataTableColumnSelections.push(selection);
            });
            self.columns.push(countColumn);
            return countColumn;
        };

        self.isCountColumnEnabled = function() {
            var cols = _getCountColumns();
            if (cols == undefined || cols.length == 1) {
                return false;
            }
            var hasCountColumn = false;
            self.dataTables.forEach(function(table) {
                table.dataTableColumns.forEach(function(col) {
                    if (col.columnEncodingType == 'COUNT') {
                        hasCountColumn = true;
                    }
                });
            });
            return hasCountColumn;
        };


        /**
         * Add specified dataTable to the viewModel.dataTables array. Duplicates are ignored. This is the recommended way of manipulating this list, as opposed
         * to direct array manipulation.
         * 
         * @param dataTables
         */
        self.addDataTables = function(dataTables) {
            _setAddAll(self.dataTables, dataTables, "id");
            _dataTablesAdded(dataTables)
        };

        /**
         * Return 'tri-state' value indicating the participation status of the specified node w.r.t. the specified dataTableColumn. True means the node is
         * present in the column, False means it does not, and NULL means that IntegrationModel is currently loading participation data for the specified
         * dataTableColumn.
         * 
         * @param dataTableColumn
         * @param {object}
         *            ontologyNode
         * @private
         */
        self.isNodePresent = function _isNodePresent(dataTableColumn, ontologyNodeId) {
            if(!dataTableColumn) return false;
            if (!dataTableColumn.transientNodeParticipation) {
                return false;
            }
            return dataTableColumn.transientNodeParticipation.indexOf(ontologyNodeId) >= 0;
        }

        /**
         * remove the outputColumn at the specified index
         * 
         * @param idx
         * @private
         */
        self.removeOutputColumn = function _removeOutputColumn(idx) {
            self.columns.splice(idx, 1);
        };

    }

    // expose a singleton instance to the application
    app.service("IntegrationService", [ "DataService", Integration ]);

    // //expose the class to the application in case another component wants to create a unique instance
    // app.value("IntegrationClass", Integration);

})(angular);