(function($, angular, console){
    "use strict";
    var app = angular.module('integrationApp');

    //AngularJS directive that provides binding to a simple Datatables.net table component.

    // Based on example here http://jsfiddle.net/zdam/pb9ba/
    // probably needs to be updated for angular v1.3+ and datatables v1.10


    app.directive("tdarDatatable", [function() {
        //options used by the $.DataTable constructor
        var _defaultOptions = {
            bPaginate: false

            //unfortunately you can't specify height in separate css (this may change in v1.1+)
            //,sScrollY: "25em"
            //,bScrollCollapse: true



        };

        var self =  {
            restrict: 'A',
            link: function(scope, element, attrs){
                console.debug("linking to dom element:%s", attrs.id);
                var widget = false;

                // If THEAD has columns, we are ready to init datatable
                if(element.find("thead th").length) {
                    widget = element.DataTable();
                }

                //if controller sets/modifies  $scope[attrs.aoColumns], (re)initialize the datatable
                if(typeof attrs.aoColumns !== "undefined") {
                    scope.$watch(attrs.aoColumns, function(val){
                        console.debug("%s:: ao-columns:'%s'", attrs.id, val);
                        if(!val){return}

                        //aoColumns needs to be in specific format. for now we define all columns
                        var aoColumns = val.map(function(colname){
                            return {sTitle: colname};
                        });

                        //if datatable already exists, we need to destroy it and start over
                        if(widget) {
                            widget.fnClearTable();
                            widget.fnDestroy();
                            widget = null;
                        }
                        var options = $.extend({}, _defaultOptions, {
                            aoColumns: aoColumns,
                            aaData: []
                        });

                        widget = element.DataTable(options);

                    });
                }

                //look for changes to the aa-data attribute (which corresponds to the options.aaData value you would pass to $.Datatable() )
                scope.$watch(attrs.aaData, function(val){
                    console.debug("%s:: aa-data:%s", attrs.id,  val);
                    self.updateRowData(widget, val);
                });
            },

            updateRowData: function(widget, rowData) {
                    if(!rowData) {return}
                    widget.fnClearTable();
                    widget.fnAddData(rowData);
                }
        };
        return self;

    }]);


})(jQuery, angular, console);