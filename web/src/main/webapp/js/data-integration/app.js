/**
 * This file defines the top-level angular module for this app (and potentially any configuration information).
 * 
 * 
 */
(function($, angular) {
    "use strict";

    // Ajax request timeout, in millis. This varies across browser vendors (FF: infinite,  Chrome: 5 mins) , so it's a good idea to set it explicitly to 20 minutes.
    // pro tip: to test out how your app handles broken sockets, set timeout to 1 (or another small, nonzero number)
    var AJAX_REQUEST_TIMEOUT_MS = 1000 * 60 * 20;

    var app = angular.module('integrationApp', []);

    app.config(['$compileProvider', '$httpProvider', function ($compileProvider, $httpProvider) {
        $compileProvider.debugInfoEnabled(false);



        $httpProvider.interceptors.push(function() {
            return {
                'request': function(config) {
                    console.log("interceptor.request called.  Timeout is currently %s", config.timeout);
                    config.timeout = AJAX_REQUEST_TIMEOUT_MS;
                    return config;
                }

            };
        });

    }]);

    /* global jQuery, angular */
})(jQuery, angular);
