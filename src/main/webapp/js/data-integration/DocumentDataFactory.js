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
    app.factory("DocumentData", _loadDocumentData);

/* global angular */
})(angular);