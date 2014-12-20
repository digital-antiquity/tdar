//FIXME: this should be a service responsible for loading/storing data to/from the server
//TODO: migrate $http calls here
//TODO: add an ontology cache
//TODO: add a datatable cache
(function(angular){
    "use strict"
    var app = angular.module("integrationApp");


    function _loadDocumentData() {
        var dataElements = $('[type="application/json"][id]').toArray();
        var map = {};
        dataElements.forEach(function(elem){
            var key = elem.id;
            var val = JSON.parse(elem.innerHTML);
            map[key] = val;
        });
        return map;
    }

    function _dedupe(items) {
        var uniqueItems = [];
        items.forEach(function(item){
            if(uniqueItems.indexOf(item) === -1) {
                uniqueItems.push(item);
            }
        })
        return uniqueItems;
    }

    function DataService($http, $cacheFactory, $q) {
        var self = this,
            documentData = _loadDocumentData(),
            ontologyCache = $cacheFactory('ontologyCache'),
            datatableCache = $cacheFactory('datatableCache'),
            ontologyNodeCache = $cacheFactory('ontologyNodeCache'),
            datatableColumnCache = $cacheFactory('datatableColumnCache');

        // expose these caches for now,  though a better solution is have find() methods that consult cache before making ajax call
        this.ontologyCache = ontologyCache;
        this.ontologyNodeCache = ontologyNodeCache;
        this.datatableCache = datatableCache;
        this.datatableColumnCache = datatableColumnCache;




        /**
         * Return a map<str, obj> of objects that were embedded in the DOM using script tags of type "application/json".
         * for each entry in the map, the  entry key is the script element's ID attribute, and the key value is the
         * parsed value of the script's JSON data.
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


            //fixme: refactor to return a promise of return data instead of modifying integration directly
        this.loadIntegrationColumnDetails = function(integration) {
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
                promises.push($http.get("/api/integration/integration-column-details", config));
            });
            $q.all(promises).then(function(arResults){
                arResults.forEach(function(result, ontologyIdx){
                    var ontology = integration.ontologies[ontologyIdx];
                    var mappedCols = integration.getMappedDatatableColumns(ontology.id);
                    console.log(result);
                    integration.updateNodeParticipationInfo(ontology, mappedCols, result.data.flattenedNodes);
                });
            }) ;
        };

        /**
         * Returns httpPromise of an object containing: datatable objects corresponding to the specified array of
         * datatableId's
         * @param datatableIds
         * @return HttpPromise futureDatatables:httppromise<{dataTables: Array<datatable>,  sharedOntologies: Array<ontology>}>
         *
         */
        this.loadTableDetails = function(datatableIds) {
            //TODO: create recursive 'unroll' function that emits params in struts-friendly syntax
            var httpPromise =  $http.get('/api/integration/table-details?' + $.param({dataTableIds: datatableIds}, true));

            //we also want to cache the results, so we tack on a callback of our own
            httpPromise.success(function(data){
                data.dataTables.forEach(function(datatable){
                    datatableCache.put(datatable.id, datatable);
                    datatable.dataTableColumns.forEach(function(dtc){
                        datatableColumnCache.put(dtc.id, dtc);
                    });
                });
                data.sharedOntologies.forEach(function(ontology){
                    ontologyCache.put(ontology.id, ontology);
                    ontology.nodes.forEach(function(ontologyNode){
                        ontologyNodeCache.put(ontologyNode.id, ontologyNode);
                    });
                });
            });

            return httpPromise;
        };

        /**
         * send $http.get to specified url, return promise<transformedData> if transformer specified, otherwise return
         * promise<data>;
         * @param url
         * @param searchFilter
         * @param transformer
         * @private
         */
        //fixme: move SearchFilter class to DataService.js
        function _doSearch(url, searchFilter, transformer, prefix) {
            var futureData = $q.defer();
            var config = {
                params: searchFilter.toStrutsParams()
            };
            $http.get(url, config).success(function(rawData_){
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
         * Search for datasets(actually, datatables) filtered according to specified SearchFilter.  returns promise<Array<searchResult>>
         * @param searchFilter (note: searchFilter.categoryId ignored in dataset search)
         * @returns {*}
         */
        this.findDatasets = function(searchFilter) {
            var transformer =  function(data) {
                return data.map(function(item){
                    var d = new Date(0);
                    d.setUTCSeconds(parseInt(item.dataset.dateCreated) / 1000);
                    var result = item;
                    result.title = item.dataset.title;
                    result.title += ' - ' + item.dataTable.displayName;
                    result.id = item.dataTable.id;
                    result.submitter_display_name  = item.submitter.properName;
                    result.date_created = d;
                    return result;
                })
            };
            return _doSearch("/api/integration/find-datasets", searchFilter, transformer, "dataTables");
        };

        //todo: Note that the UI for this feature is disabled, but this function ostensibly still "works"
        /**
         * Search for ontologies according to specified SearchFilter.  returns promise<Array<searchResult>>
         * @param searchFilter
         * @returns {*}
         */
        this.findOntologies = function(searchFilter)  {
            // FIXME: write transformer
            return _doSearch("/api/integration/find-ontologies", searchFilter, null, "ontology");
        };

        /**
         * Return an array previously-loaded ontologies in the same order of the specified array of ontologyId's
         * @param ontologyIds
         */
        this.getCachedOntologies = function(ontologyIds) {
            return ontologyIds.map(function(ontologyId) {
                return self.ontologyCache.get(ontologyId);
            });
        };

        /**
         * Returns promise of ontology node participation data, stored as a map (keyed by datatableColumnId) of lists of ontologyNodeIds.  Each ontologyNodeId
         * in the list indicates that the node value occurs in the dataTable in the column indicated by the datatableColumnId.
         *
         * @param datatableColumns
         * @returns promise<<map<ontologyId,list<ontologyNodeId>>>
         */
        this.loadDatatableColumnParticipation = function(datatableColumns) {
            var futureData = $q.defer();
            var endpointUrl = "/workspace/ajax/integration-column-details";

            //Here we are (mis)using the integration-column-details endpoint to get us the data we need.  So to do this we need to construct an
            //"IntegrationColumn object" that holds an ontologyId, and all of the dataTableColumns that map to that ontology.  Furthermore,  the endpoint
            //only accepts one integrationColumn per request, so we need to make N requests (one for each mapped ontology), wait for N responses to arrive,
            //then stitch the data back together, then transform that data into format that we need.


            //first, lists build ontology-to-columns map;  map<ontologyId, list<datatableColumnId>>
            var dtcIdLists =  datatableColumns
                    .filter(function(dtc) {
                        if (dtc.mappedOntology == undefined) {
                            return false;
                        };
                        return true;
                    }
                        )
                    .reduce(function(obj,dtc){
                        if(!obj[dtc.mappedOntology.id]){
                            obj[dtc.mappedOntology.id] = [];
                        }
                        obj[dtc.mappedOntology.id].push(dtc.id);
                        return obj;
                    }, {});

            //now, let's build request parameters for request we need to make
            var ontologyIds = Object.keys(dtcIdLists);
            var promises = ontologyIds.map(function(ontologyId){
                console.debug("integration-column-details::  ontologyId:%s  colcount:%s", ontologyId, dtcIdLists[ontologyId].length);
                var config = _integrationColumnRequestConfig(ontologyId, dtcIdLists[ontologyId]);
                return $http.get(endpointUrl, config);
            });

            $q.all(promises).then(function(responses) {
                var participationLists = {};

                responses.forEach(function(response, ontIdx){
                    var ontologyId = ontologyIds[ontIdx];
                    var ontology = ontologyCache.get(ontologyId);
                    var nodeProxies = response.data;
                    var dtcIds = dtcIdLists[ontologyId];

                    dtcIds.forEach(function(dtcId){
                        participationLists[dtcId] = [];
                    });

                    //the layout of the data is kinda like this
                    //  data := array<nodeProxy>
                    //  nodeProxy := {id:<ontologyId>,  mapping_list:array<bool>}
                    //  - the top array follows same sequence as ontology.nodes  (fixme: confirm this is correct)
                    //  - the mapping_list array follows same sequence as datatable.columns
                    //so now let's transpose the data to be datatableColumn centric,  and convert the bit-array to a list of participating ontologyNodeId's
                    nodeProxies.forEach(function(nodeProxy, nodeIdx){
                        var ontologyNode = ontology.nodes[nodeIdx];
                        if(nodeProxy.id !== ontologyNode.id) {
                            console.error ("ontology mismatch::  idx:%s \tnodeProxy:%s \tontologyNode:%s", nodeIdx, nodeProxy.id, ontologyNode.id);
                        }
                        nodeProxy.mapping_list.forEach(function(isPresent, idx){
                            if(isPresent) {
                                var dtcid = dtcIds[idx];
                                participationLists[dtcid].push(ontologyNode.id);
                            }
                        });
                    });

                });
                futureData.resolve(participationLists);
            });


            return futureData.promise;
        };

        //FIXME: HACK: for debugging purposes only ;
        window.__ds = this;
    }

    //build parms for single request to integration-column-details endpoint
    function _integrationColumnRequestConfig(ontologyId, datatableColumnIds) {
        var params = {
            "integrationColumn.columnType": "INTEGRATION",
            "integrationColumn.sharedOntology.id": ontologyId
        };
        datatableColumnIds.forEach(function(datatableColumnId, i){
            params["integrationColumn.columns[" + i + "].id"] = datatableColumnId
        });
        return {"params": params};
    }

    app.service("DataService", ['$http', '$cacheFactory', '$q',  DataService]);

/* global angular */
})(angular);