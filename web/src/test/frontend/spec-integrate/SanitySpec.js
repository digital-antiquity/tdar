describe("sanity checks for angular integration testing", function() {
    "use strict";

    var $controller = null;

    beforeEach(function() {
       angular.module("integrationApp");
    });

    it("runs this test when we use the karma-integration.conf.js config ", function() {
        expect(5).toBe(5);
    });

    it("loads json fixtures", function() {
        //more info on overloading http backend here:
        var obj = getJSONFixture("sanity.json");
        expect(obj.foo).toBe("bar");
    });

});