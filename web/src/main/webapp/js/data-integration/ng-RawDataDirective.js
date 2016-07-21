/**
 * Directive for registering key/value data with the DataService.  Data is expected to be in json format (presumably using script[type=application/json] tags)
 */
(function(angular) {
    "use strict";
    var app = angular.module('integrationApp');

    app.directive("rawData", ['DataService',
        function(dataService){
            return {
                restrict: 'A',
                link: function(scope, element, attrs) {
                    var json = element[0].innerHTML;
                    var key = attrs.id;
                    console.log("adding to dataservice:: %s", key);
                    dataService.setDocumentData(key, JSON.parse(json));
                }

            }
        }]);
})(angular);