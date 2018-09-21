/* global describe, it, expect, beforeEach, jasmine */
require("JS/data-integration/app.js");

xdescribe("DataService", function() {
    "use strict";

    var integrationService;

    beforeEach(angular.mock.module('integrationApp'));
    beforeEach(angular.mock.inject(function(_IntegrationService_){
        integrationService = _IntegrationService_;
    }));

    describe("dataservice tests", function() {

        it("should not be null", function() {
            expect(integrationService).toBeDefined();
        });


    });


});