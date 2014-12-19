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

    function DataService($http, $cacheFactory, $q) {
        var self = this,
            documentData = _loadDocumentData(),
            ontologyCache = $cacheFactory('ontology'),
            datatableCache = $cacheFactory('datatable');



        /**
         * Return a map<str, obj> of objects that were embedded in the DOM using script tags of type "application/json".
         * for each entry in the map, the  entry key is the script element's ID attribute, and the key value is the
         * parsed value of the script's JSON data.
         * @returns {*}
         */
        this.getDocumentData = function() {
            return documentData
        }

        this.ontologyCache = ontologyCache;
        this.datatableCache = datatableCache;

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
                data.dataTables.forEach(function(datatable){self.datatableCache.put(datatable.data_table_id, datatable)});
                data.sharedOntologies.forEach(function(ontology){self.ontologyCache.put(ontology.id, ontology)});
            });

            return httpPromise;
        };


        /**
         * send $http.get to specified url, return promise<transformedData> if transformer specified, otherwise return
         * promise<data>;
         * @param url
         * @param searchFilter
         * @param transformer
         * @returns {*}
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




    }

    app.service("DataService", ['$http', '$cacheFactory', '$q',  DataService]);

/* global angular */
})(angular);