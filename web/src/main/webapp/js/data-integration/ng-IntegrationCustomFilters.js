(function(angular, $, console) {
    "use strict";

    var app = angular.module('integrationApp');

    // prettify ontology
    app.filter('ontDisplayName', function() {
        return function(ontology) {
            var name = ontology.title;
            // avoid the 'Alan Parsons Project Project' effect
            name = name.replace(/\s*Ontology\s*$/i, '');
            name = $.trim(name);
            return name;
        }
    });

    // prettify dataTables
    app.filter('dtDisplayName', function() {
        return function(dataTable) {
            console.log(dataTable);
            return dataTable.datasetTitle + ' \u25B8 ' + dataTable.displayName;
        }
    });

    app.filter('titleCase', function() {
        return function(s) {
            s = (s === undefined || s === null) ? '' : s;
            return s.toString().toLowerCase().replace(/\b([a-z])/g, function(ch) {
                return ch.toUpperCase();
            });
        };
    });

    /* global angular, jQuery */
})(angular, jQuery, console);