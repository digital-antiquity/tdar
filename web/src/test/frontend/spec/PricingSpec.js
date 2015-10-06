/* global describe, it, xit, expect, beforeEach, loadFixtures, TDAR */
describe("tests for TDAR.pricing methods", function() {

    beforeEach(function(){
        loadFixtures("pricing.html");
    });

    it("initializes pricing page", function() {
        var expectedVal = null;
        expect(TDAR.pricing.initPricing).toBeDefined();
        TDAR.pricing.initPricing($('#MetadataForm')[0], "/cart/api");
    });


    it("should work when we call updateProgress", function() {
        var expectedVal = null;

        //var result = TDAR.pricing.updateProgress();
    });

    xit("should work when we call initPolling", function() {
        var expectedVal = null;

        //var result = TDAR.pricing.initPolling();
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call initBillingChoice", function() {
        var expectedVal = null;

        //var result = TDAR.pricing.initBillingChoice();
        expect(true).toBe(false); //fixme: implement this test
    });

});
