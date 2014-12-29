(function(angular) {
    "use strict"
    var app = angular.module("integrationApp");

    /**
     * generating a simpler JavaScript object that represents an integration. This object is a proposal for a DTO
     */
    function _dumpObject(integration) {
        var out = {
            title : "untitled",
            description : "",
            columns : [],
            datasets : [],
            dataTables : [],
            ontologies : []
        };

        if (integration.description != undefined) {
            out.description = integration.description;
        }

        if (integration.title != undefined) {
            out.title = integration.title;
        }

        integration.columns.forEach(function(column) {
            var outputColumn = {
                name : column.title,
                type : column.type.toUpperCase(),
                dataTableColumns : []
            };

            var tempList = column.selectedDataTableColumns;
            if (tempList == undefined) {
                tempList = [];
                column.dataTableColumnSelections.forEach(function(col) {
                    tempList.push(col.dataTableColumn);
                });
            }

            tempList.forEach(function(dtc) {
                if (dtc.id != undefined) {
                    var dtc_ = {
                        id : dtc.id,
                        name : dtc.name
                    }
                }
                outputColumn.dataTableColumns.push(dtc_);
            });

            if (column.type == 'integration') {
                outputColumn.ontology = {
                    id : column.ontology.id,
                    title : column.ontology.title
                }
                if (!(column.nodeSelections == undefined)) {
                    outputColumn.nodeSelection = [];
                    column.nodeSelections.forEach(function(node) {
                        if (node.selected) {
                            var node_ = {
                                id : node.node.id,
                                iri : node.node.iri
                            };
                            outputColumn.nodeSelection.push(node_);
                        }
                        ;
                    });
                }
                ;
            }

            out.columns.push(outputColumn);
        });

        integration.ontologies.forEach(function(ontology) {
            var ont = {
                id : ontology.id,
                title : ontology.title
            };
            out.ontologies.push(ont);
        });

        integration.dataTables.forEach(function(dataTable) {
            var table = {
                id : dataTable.id,
                name : dataTable.name,
                displayName : dataTable.displayName
            };
            out.dataTables.push(table);
        });
        return out;
    }

    function _loadDocumentData() {
        var dataElements = $('[type="application/json"][id]').toArray();
        var map = {};
        dataElements.forEach(function(elem) {
            var key = elem.id;
            var val = JSON.parse(elem.innerHTML);
            map[key] = val;
        });
        return map;
    }

    function _dedupe(items) {
        var uniqueItems = [];
        items.forEach(function(item) {
            if (uniqueItems.indexOf(item) === -1) {
                uniqueItems.push(item);
            }
        })
        return uniqueItems;
    }

    function DataService($http, $cacheFactory, $q) {
        var self = this, documentData = _loadDocumentData(), ontologyCache = $cacheFactory('ontologyCache'), dataTableCache = $cacheFactory('dataTableCache'), ontologyNodeCache = $cacheFactory('ontologyNodeCache'), dataTableColumnCache = $cacheFactory('dataTableColumnCache');

        // expose these caches for now, though a better solution is have find() methods that consult cache before making ajax call
        this.ontologyCache = ontologyCache;
        this.ontologyNodeCache = ontologyNodeCache;
        this.dataTableCache = dataTableCache;
        this.dataTableColumnCache = dataTableColumnCache;

        /**
         * Return a map<str, obj> of objects that were embedded in the DOM using script tags of type "application/json". for each entry in the map, the entry
         * key is the script element's ID attribute, and the key value is the parsed value of the script's JSON data.
         * 
         * @returns {*}
         */
        this.getDocumentData = function() {
            return documentData
        }

        /**
         * Create a subset of a specified array that does not contain duplicates (using object identity for equality)
         * 
         * @param items:{Array}
         * @returns {Array}
         */
        this.dedupe = _dedupe;
        this.dumpObject = _dumpObject;
        this.loadExistingIntegration = _loadExistingIntegration;
        this.saveIntegration = _saveIntegration;

        /**
         * Takes a JSON representation of the integration and saves it to the server. Server will pass back an ID and status if an ID is not already defined
         */
        function _saveIntegration(integration) {
            var futureData = $q.defer();
            var jsonData = self.dumpObject(integration);
            var path = '/api/integration/save';
            var done = false;

            // if we're doing an update
            if (parseInt(integration.id) > -1) {
                path += "/" + integration.id;
            }
            console.log(jsonData);
            var httpPromise = $http({
                method : "POST",
                url : path,
                data : $.param({
                    integration : JSON.stringify(jsonData)
                }),
                headers : {
                    'Content-Type' : 'application/x-www-form-urlencoded'
                }
            });

            // promise is not 100% used here, but in the future, perhaps needed
            httpPromise.success(function(data) {
                integration.id = data.id;
                futureData.resolve(done);
            });

            return futureData.promise;
        }

        /**
         * Based on the specified JSON representation of a data object, try and rebuild the integration
         */
        function _loadExistingIntegration(json, integration) {

            var dataTableIds = [];
            json.dataTables.forEach(function(dataTable) {
                dataTableIds.push(dataTable.id);
            });
            integration.title = json.title;
            integration.description = json.description;
            console.log("starting...");
            // Load the datasets and then use the results to build the columns out
            self.loadTableDetails(dataTableIds).then(function(dataTables) {
                integration.addDataTables(dataTables);
                json.columns.forEach(function(column) {
                    var name = column.name;
                    if (name == undefined) {
                        name = "column";
                    }

                    var ids = [];
                    column.dataTableColumns.forEach(function(col) {
                        ids.push(col.id);
                    });

                    if (column.type == 'DISPLAY') {
                        integration.addDisplayColumn(name);
                        self._loadDisplayIntegrationColumns(ids, integration.columns[integration.columns.length - 1]);
                    }
                    if (column.type == 'COUNT') {
                        integration.addCountColumn(name);
                        self._loadDisplayIntegrationColumns(ids, integration.columns[integration.columns.length - 1]);
                    }
                    if (column.type == 'INTEGRATION') {
                        var ontology = undefined;
                        integration.ontologies.forEach(function(ont) {
                            if (ont.id == column.ontology.id) {
                                ontology = ont;
                            }
                        });

                        integration.addIntegrationColumn(name, ontology);
                        var col = integration.columns[integration.columns.length - 1];

                        // FIXME: I'm less sure about this direct replacement -- is this okay? It appears to work, change to setter
                        col.selectedDataTableColumns = self.getCachedDataTableColumns(ids);
                        col.nodeSelections.forEach(function(node) {
                            column.nodeSelection.forEach(function(nodeRef) {
                                if (nodeRef.id == node.node.id) {
                                    node.selected = true;
                                }
                            });
                        });
                    }
                });
            });
        }

        /**
         * internal method to initialize a Display or Count column in loading
         */
        self._loadDisplayIntegrationColumns = function(ids, col) {

            col.dataTableColumnSelections.forEach(function(selection) {
                console.log(selection);
                selection.dataTable.dataTableColumns.forEach(function(dtc) {
                    ids.forEach(function(id) {
                        if (dtc.id == id) {
                            selection.dataTableColumn = dtc;
                            ids.splice(ids.indexOf(id), 1);
                        }
                    });
                });
            });
            if (ids.length > 0) {
                console.error("did not restore all ids");
            }

        };

        // fixme: refactor to return a promise of return data instead of modifying integration directly
        this.loadIntegrationColumnDetails = function(integration) {
            // get column participation for all dataTableColumns across all shared ontologies
            var promises = [];
            var configs = [];

            integration.ontologies.forEach(function(ontology) {
                var mappedCols = integration.getMappedDataTableColumns(ontology.id);
                var config = {};
                var params = {
                    "integrationColumn.columnType" : "INTEGRATION",
                    "integrationColumn.sharedOntology.id" : ontology.id
                };
                mappedCols.forEach(function(mappedCol, i) {
                    params["integrationColumn.columns[" + i + "].id"] = mappedCol.id
                });
                config.params = params;
                configs.push(config);
                promises.push($http.get("/api/integration/integration-column-details", config));
            });
            $q.all(promises).then(function(arResults) {
                arResults.forEach(function(result, ontologyIdx) {
                    var ontology = integration.ontologies[ontologyIdx];
                    var mappedCols = integration.getMappedDataTableColumns(ontology.id);
                    integration.updateNodeParticipationInfo(ontology, mappedCols, result.data.flattenedNodes);
                });
            });
        };

        /**
         * Returns httpPromise of an object containing: dataTable objects corresponding to the specified array of dataTableId's
         * 
         * @param dataTableIds
         * @return HttpPromise futureDataTables:httppromise<{dataTables: Array<dataTable>, sharedOntologies: Array<ontology>}>
         * 
         */
        this.loadTableDetails = function(dataTableIds) {
            var futureData = $q.defer();

            // only load tables that aren't already in the cache
            var missingTableIds = dataTableIds.filter(function(dataTableId) {
                return !dataTableCache.get(dataTableId)
            });
            // not sure if dupe tableIds will ever occur, but dedupe anyway.
            missingTableIds = _dedupe(missingTableIds);

            if (missingTableIds.length > 0) {
                var httpPromise = $http.get('/api/integration/table-details?' + $.param({
                    dataTableIds : missingTableIds
                }, true));

                httpPromise.success(function(data) {

                    // add this new data to our caches
                    data.dataTables.forEach(function(dataTable) {
                        dataTableCache.put(dataTable.id, dataTable);
                        dataTable.dataTableColumns.forEach(function(dtc) {
                            dataTableColumnCache.put(dtc.id, dtc);
                        });
                    });
                    data.mappedOntologies.forEach(function(ontology) {
                        ontologyCache.put(ontology.id, ontology);
                        ontology.nodes.forEach(function(ontologyNode) {
                            ontologyNodeCache.put(ontologyNode.id, ontologyNode);
                        });
                    });

                    // now that we have everything in the cache, return the requested dataTables back to the caller
                    futureData.resolve(self.getCachedDataTables(dataTableIds));

                });

            } else {
                // in the event that there's nothing to load, callback immediately with the results
                futureData.resolve(self.getCachedDataTables(dataTableIds));

            }

            // TODO: create recursive 'unroll' function that emits params in struts-friendly syntax

            return futureData.promise;
        };

        /**
         * send $http.get to specified url, return promise<transformedData> if transformer specified, otherwise return promise<data>;
         * 
         * @param url
         * @param searchFilter
         * @param transformer
         * @private
         */
        // fixme: move SearchFilter class to DataService.js
        function _doSearch(url, searchFilter, transformer, prefix) {
            var futureData = $q.defer();
            var config = {
                params : searchFilter.toStrutsParams()
            };
            $http.get(url, config).success(function(rawData_) {
                var rawData = rawData_;
                if (prefix !== undefined) {
                    rawData = rawData_[prefix];
                }
                var data = !!transformer ? transformer(rawData) : data;
                futureData.resolve(data);
            });
            return futureData.promise;
        }

        /**
         * Search for datasets(actually, dataTables) filtered according to specified SearchFilter. returns promise<Array<searchResult>>
         * 
         * @param searchFilter
         *            (note: searchFilter.categoryId ignored in dataset search)
         * @returns {*}
         */
        this.findDatasets = function(searchFilter) {
            var transformer = function(data) {
                return data.map(function(item) {
                    var d = new Date(0);
                    d.setUTCSeconds(parseInt(item.dataset.dateCreated) / 1000);
                    var result = item;
                    result.title = item.dataset.title;
                    result.title += ' - ' + item.dataTable.displayName;
                    result.id = item.dataTable.id;
                    result.submitter_display_name = item.submitter.properName;
                    result.date_created = d;
                    return result;
                })
            };
            return _doSearch("/api/integration/find-datasets", searchFilter, transformer, "dataTables");
        };

        // todo: Note that the UI for this feature is disabled, but this function ostensibly still "works"
        /**
         * Search for ontologies according to specified SearchFilter. returns promise<Array<searchResult>>
         * 
         * @param searchFilter
         * @returns {*}
         */
        this.findOntologies = function(searchFilter) {
            // FIXME: write transformer
            return _doSearch("/api/integration/find-ontologies", searchFilter, null, "ontology");
        };

        /**
         * Return an array previously-loaded ontologies in the same order of the specified array of ontologyId's
         * 
         * @param ontologyIds
         */
        this.getCachedOntologies = function _getCachedOntologies(ontologyIds) {
            return ontologyIds.map(function(ontologyId) {
                return self.ontologyCache.get(ontologyId);
            });
        };

        /**
         * 
         * Return an array of previously-loaded dataTables in the same order of the specified dataTableIds. Note it is not necessary to actively consult the
         * cache before attempting to load them. DataService.loadTableDetails() will automagically pull from cached results when available.
         * 
         * @param dataTableIds
         */
        this.getCachedDataTables = function _getCachedDataTables(dataTableIds) {
            return dataTableIds.map(function(id) {
                return dataTableCache.get(id);
            });
        };

        /**
         * Return an array previously-loaded data table columns in the same order of the specified array of dataTableColumnId's
         * 
         * @param dataTableColumnIds
         */
        this.getCachedDataTableColumns = function _getDataTableColumns(dataTableColumnIds) {
            return dataTableColumnIds.map(function(dataTableColumnId) {
                return self.dataTableColumnCache.get(dataTableColumnId);
            });
        };

        /**
         * Returns a promise of the ontologyNodeValues that occur in each specified dataTableColumn
         * 
         * @param dataTableColumnIds
         */
        this.loadNodeParticipation = function(dataTableColumnIds) {
            var url = '/api/integration/node-participation?' + $.param({
                dataTableColumnIds : dataTableColumnIds
            }, true);
            var httpPromise = $http.get(url);
            var futureWork = $q.defer();
            httpPromise.success(function(nodesByColumn) {
                nodesByColumn.forEach(function(container) {
                    var dataTableColumn = dataTableColumnCache.get(container.dataTableColumn.id);
                    var nodes = [];
                    container.flattenedNodes.forEach(function(nodeRef) {
                        nodes.push(ontologyNodeCache.get(nodeRef.id));
                    });
                    dataTableColumn.transientNodeParticipation = nodes;
                });
                // Note that we mutate the data directly, so there is not anything to "return". We're just notifying the caller that we are done.
                futureWork.resolve();
            });
            return futureWork.promise;
        };

        // FIXME: HACK: for debugging purposes only ;
        window.__ds = this;
    }

    // build parms for single request to integration-column-details endpoint
    function _integrationColumnRequestConfig(ontologyId, dataTableColumnIds) {
        var params = {
            "integrationColumn.columnType" : "INTEGRATION",
            "integrationColumn.sharedOntology.id" : ontologyId
        };
        dataTableColumnIds.forEach(function(dataTableColumnId, i) {
            params["integrationColumn.columns[" + i + "].id"] = dataTableColumnId
        });
        return {
            "params" : params
        };
    }

    app.service("DataService", [ '$http', '$cacheFactory', '$q', DataService ]);

    /* global angular */
})(angular);