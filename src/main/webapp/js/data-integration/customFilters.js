(function(angular, $,  console) {
    "use strict";

    var app = angular.module('integrationApp');

    //prettify ontology
    app.filter('ontDisplayName', function(){
        return function(ontology) {
            var name = ontology.displayName;

            //designated displayName trumps all
            if(name) return name;

            name  = ontology.name;

            //avoid the 'Alan Parsons Project Project' effect
            name = name.replace(/\s*Ontology\s*$/i, '');

            name = $.trim(name);
            return name;
        }
    });

    //prettify datatables
    app.filter ('dtDisplayName',  function() {
        return function(datatable) {
            var name = (datatable.data_table_name
                //ditch the ugly prefix
                .replace(/[de]_\d+_/,'')
                //strip filename from table name
                .replace(/^.*?_(mdb)_/, '')
                //deslug
                .replace(/_/g, ' ')
            );

            return datatable.dataset_name + ' :: ' + name;
        };
    });

    app.filter('titleCase', function() {
        return function(s) {
            s = ( s === undefined || s === null ) ? '' : s;
            return s.toString().toLowerCase().replace( /\b([a-z])/g, function(ch) {
                return ch.toUpperCase();
            });
        };
    });


/* global angular, jQuery */
})(angular, jQuery,  console);