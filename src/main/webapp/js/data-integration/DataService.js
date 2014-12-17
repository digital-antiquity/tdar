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
                promises.push($http.get("/workspace/ajax/integration-column-details", config));
            });
            $q.all(promises).then(function(arResults){
                arResults.forEach(function(result, ontologyIdx){
                    var ontology = integration.ontologies[ontologyIdx];
                    var mappedCols = integration.getMappedDatatableColumns(ontology.id);
                    integration.updateNodeParticipationInfo(ontology, mappedCols, result.data);
                });
            }) ;
        };


        /**
         * Returns httpPromise of an object containing: datatable objects corresponding to the specified array of
         * datatableId's, and
         * @param datatableIds
         * @return HttpPromise futureDatatables:httppromise<{dataTables: Array<datatable>,  sharedOntologies: Array<ontology>}>
         *
         */
        this.loadTableDetails = function(datatableIds) {
            //TODO: create recursive 'unroll' function that emits params in struts-friendly syntax
            var httpPromise =  $http.get('/workspace/ajax/table-details?' + $.param({dataTableIds: datatableIds}, true));

            //we also want to cache the results, so we tack on a callback of our own
            httpPromise.success(function(data){
                data[0].dataTables.forEach(function(datatable){self.datatableCache.put(datatable.data_table_id, datatable)});
                data[0].sharedOntologies.forEach(function(ontology){self.ontologyCache.put(ontology.id, ontology)});
            });

            return httpPromise;
        };


    }

    app.service("DataService", ['$http', '$cacheFactory', '$q',  DataService]);

/* global angular */
})(angular);