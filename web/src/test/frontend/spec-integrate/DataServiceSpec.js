/* global describe, it, expect, beforeEach, jasmine */

describe("DataService", function() {
    "use strict";

    var dataService;

    beforeEach(module('integrationApp'));
    beforeEach(inject(function(_DataService_){
        dataService = _DataService_;
    }));

    describe("dataservice tests", function() {

        it("should be defined", function(){
            expect(dataService).toBeDefined();

        });



    });


});