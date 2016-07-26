/* global describe, it, expect, beforeEach, jasmine */

describe("DataService", function() {
    "use strict";

    var integrationService;

    beforeEach(module('integrationApp'));
    beforeEach(inject(function(_IntegrationService_){
        integrationService = _IntegrationService_;
    }));

    describe("dataservice tests", function() {

        it("should not be null", function() {
            expect(integrationService).toBeDefined();
        });


    });


});