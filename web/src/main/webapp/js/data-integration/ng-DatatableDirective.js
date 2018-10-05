const angular = require("angular");

var app = angular.module('integrationApp');
require('datatables.net/js/jquery.dataTables.js');
require('datatables.net-bs4/js/dataTables.bootstrap4.js');
require('datatables.net-bs4/css/dataTables.bootstrap4.css');
// AngularJS directive that provides binding to a simple Datatables.net table component.

// Based on example here http://jsfiddle.net/zdam/pb9ba/
// probably needs to be updated for angular v1.3+ and datatables v1.10
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
        link: function(scope, element, attrs, ctrl){
            console.debug("linking to dom element:%s", attrs.id);
            var widget = false;
            // If THEAD has columns, we are ready to init datatable
            if(element.find("thead th").length) {
                widget = element.DataTable();
            }

            //if controller sets/modifies  $scope[attrs.aoColumns], (re)initialize the datatable
            if(typeof attrs.aoColumns !== "undefined") {
                scope.$watch(attrs.aoColumns, function(val){
                    console.debug("%s:: ao-columns:'", attrs.id, attrs);
                    if(!val){return}
                    console.debug(scope);
                    //aoColumns needs to be in specific format. for now we define all columns
                    var aoColumns = val.map(function(colname){
                        return {sTitle: colname};
                    });

                    //if datatable already exists, we need to destroy it and start over
                    if(widget && widget.clear != undefined) {
                        widget.clear();
                        widget.destroy();
                        widget = null;
                    } 
                    var options = $.extend({}, _defaultOptions, {
                        aoColumns: aoColumns,
                        aaData: val.aoData
                    });
                    widget = element.DataTable(options);
                    console.debug(widget);
                    element.widget = widget;
                });
            }
            console.debug("%s:: ", attrs.id, element, self);
            //look for changes to the aa-data attribute (which corresponds to the options.aaData value you would pass to $.Datatable() )
            scope.$watch(attrs.aaData, function(val){
                console.debug("%s:: aa-data:", attrs.id,  val, widget);
                self.updateRowData(element.widget, val);
            });
        },

        updateRowData: function(widget, rowData) {
                if(!rowData) {return}
                    widget.clear();
                    console.log(widget);
                    widget.rows.add(rowData);
            }
    };
    return self;

}]);


module.exports = {}