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

    function DataService() {
        var documentData = _loadDocumentData();
        var self = this;

        this.getDocumentData = function() {
            return documentData
        }
    }

    app.service("DataService", DataService);

/* global angular */
})(angular);