/* global describe, it, xit, expect, beforeEach, loadFixtures, TDAR */
describe("tests for TDAR.pricing methods", function() {
    xit("test init", function() {
        loadFixtures("pricing.html");
        var expectedVal = null;
        expect(TDAR.pricing.initPricing).toExist();
        //TDAR.pricing.initPricing($('#MetadataForm')[0], "/cart/api");
    });


    xit("should work when we call initPricing", function() {
        var form = null;
        var ajaxUrl = null;
        var expectedVal = null;

        //var result = TDAR.pricing.initPricing(form, ajaxUrl);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call updateProgress", function() {
        var expectedVal = null;

        //var result = TDAR.pricing.updateProgress();
        expect(true).toBe(false); //fixme: implement this test
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
