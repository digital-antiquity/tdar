/* global describe, it, expect, beforeEach, jasmine */

require("JS/data-integration/app.js");
const angular = require('angular');
// require('angular-mocks/angular-mocks.js')
xdescribe("DataService", function() {
    "use strict";

    var dataService;

    beforeEach(angular.mock.module('integrationApp'));
    beforeEach(angular.mock.inject(function(_DataService_) {
        dataService = _DataService_;
    }));

    xdescribe("dataservice tests", function() {

        it("should be defined", function() {
            expect(dataService).toBeDefined();

        });

    });

});