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


    //fixme: If we like this directive, either rename this file or put this directive into it's own file (TDAR-5530)

    //Tooltip directive (beware: it will properly render {{interpolated}} strings but *will not* update them)
    app.directive('popover', function(){
        return {
            restrict: 'A',
            link: function(scope, element, attrs){
                $(element).popover({
                    placement: 'left',
                    trigger: 'hover',
                    html: false,
                    content: attrs.content
                });
            }
        };
    });

})(angular);