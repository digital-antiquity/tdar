/*
Funcitional Testing ideas:

name: consistency test
instructions:
    - Create a valid integration workflow that has exactly 3 datatables, 1+ integration, and 1+ display columns
    - Make sure the workflow is ready to integrate (all datatable columns selected in your display columns,  more than
        ontology node value checked,  that sort of thing.
    - remove one of your datatables (select a datatable and click 'remove selected datatable').  you should now have
        *two* datatables.
    - Click the integration button
results:
    The integration works without errors, ana that the integration results include *2* datatables.

name: integration column stress test
instructions:
    - find 2 datasets that use a REALLY HUGE ontology (like 900+ nodes or more)
    - add 2-3 integration columns (even if the use the same ontology)
    - (check out the results page,  add a few more integration columns, repeat)
results:
    Look out for extreme sluggishness, hiccups.  Scroll up an down the page,  click on
    on different tabs,  check/uncheck node values, etc.

    Look out for UI issues:  is there a point at which the page becomes unreadable,  poorly formatted,
      or generally ugly?

    Note: we are specifically *trying*  to stess the app do get a rough idea of it's "breaking point"
        and identify any performance hotspots.

name: validation test
instructions:
    - Choose some (integratable) datatables and Perform any of the following actions
        * add multiple integration columns for the same ontology
        * add a display column and choose "no column selected"  for ALL dropdowns.
        * (if display name support enabled)  leave integration column "Name:" field blank
        * Delete *all* of your output columns: display, integration, and count columns
results:
    - The "Integrate" button should be disabled (e.g. the system should not allow you to
        the 'integration results' page.







 */




/**
 * This section describes a hypothetical way to do some barebones tests on our angular app (w/o using jasmine, karma).
 * They will only run against a running tDAR instance, so you could consider them 'integration' tests
 *
 * When included, this section adds "run blocks" to the integrationApp module.  Angular executes these after it
 * compiles your template(s) and has resolved the dependency graph.
 *
 * WARNING: Consider this entire file to be one giant hack.  This is not the recommended way to do angularjs  testing, javascript testing,
 * or testing in general.  Still, in lieu of anything better it's a way to perform some sanity checks on the app.
 */
(function(angular, $, console, someFancyTestingFramework, globalContext){

    var app = angular.module("integrationApp");


    app.run(['DataService', function(dataService){
        console.log("Is DataService  injected and ready for testing: %s", typeof dataService !== "undefined");
    }]);

    app.run(['IntegrationService', function(integration){
        console.log("Is integration object njected and ready for testing: %s", typeof integration !== "undefined");
    }])



})(angular, jQuery, console, null, window);