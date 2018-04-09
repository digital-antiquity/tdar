/* global describe, it, expect, $j */
describe("SanitySpec.js: some sanity tests to ensure that we have configured the test framwork correctly", function(){

    it("should define TDAR and packages ", function() {
        expect(TDAR).toBeDefined();
        expect(TDAR.common).toBeDefined();
    });

    it("has jasmine fixtures", function() {
        //Karma webserver rootUrl for static content is  "/base", so a file located in /src/test/frontend/fixtures
        //will have a url of localhost:9876/base/src/test/frontent/fixtures
        jasmine.getFixtures().fixturesPath = "base/src/test/frontend/fixtures";
        loadFixtures("example-fixture.html");
        expect($j(".foo.bar").length).toBe(1);
    });
    
    it("should isolate fixtures from other tests", function() {
        expect($j(".foo.bar").length).toBe(0);
    }); 

    it("has html2js fixtures", function() {
        //html2js processes all fixtures and loads them into window.__html__
        var strHtml = window.__html__["src/test/frontend/html2js/example-fixture2.html"]
        expect(strHtml).toBeDefined();
    });

});