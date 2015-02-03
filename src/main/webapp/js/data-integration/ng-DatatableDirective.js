(function($, angular, console){
    "use strict";
    var app = angular.module('integrationApp');

    //AngularJS directive that provides binding to a simple Datatables.net table component.

    // Based on example here http://jsfiddle.net/zdam/pb9ba/
    // probably needs to be updated for angular v1.3+ and datatables v1.10


    app.directive("tdarDatatable", [function() {
        return {
            restrict: 'A',
            link: function(scope, element, attrs){

                console.debug("linking to dom element:%s", attrs.id);

                //step 1: initialize datatable widget

                //look for changes to the aa-data attribute (which corresponds to the options.aaData value you would pass to $.Datatable() )
                scope.$watch(attrs.aaData, function(val){
                    console.debug("%s:: new value for aa-data:%s",attrs.id,  val);

                    //step 2a: destroy existing widget (if it exists)
                    //dataTable.fnClearTable();

                    //step 2b: create  new widget (if aaData is not null)

                });
            }
        }

    }]);


})(jQuery, angular, console);