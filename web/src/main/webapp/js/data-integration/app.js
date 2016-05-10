/**
 * This file defines the top-level angular module for this app (and potentially any configuration information).
 * 
 * 
 */
(function($, angular) {
    "use strict";

    var app = angular.module('integrationApp', []);

    app.config(['$compileProvider', function ($compileProvider) {
        $compileProvider.debugInfoEnabled(false);
    }]);

    /* global jQuery, angular */
})(jQuery, angular);
