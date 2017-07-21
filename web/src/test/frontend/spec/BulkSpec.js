/* global describe, it, expect */
describe("BulkSpec.js: tests for TDAR.bulk methods", function() {

    beforeEach(function() {
        loadFixtures("bulk-upload-form.html");
        jasmine.Ajax.install();
        jasmine.clock().install();
    });

    afterEach(function(){
        jasmine.Ajax.uninstall();
        jasmine.clock().uninstall();
    });

    it("should work when we call updateProgress", function() {
        spyOn(TDAR.bulk, "updateProgress");
        var settings = TDAR.bulk.init($("#divUploadStatus"));
        expect(TDAR.bulk.updateProgress).not.toHaveBeenCalled();
        expect(settings.asyncUrl).toBe('/checkstatus');
        jasmine.clock().tick(501);
        expect(TDAR.bulk.updateProgress).toHaveBeenCalled();
    });

    it("should update progress", function() {
        spyOn(TDAR.bulk, "updateProgress").and.callThrough();
        var settings = TDAR.bulk.init($("#divUploadStatus"));
        jasmine.clock().tick(501);
        expect(jasmine.Ajax.requests.mostRecent().url).toBe('/checkstatus');

        jasmine.Ajax.requests.mostRecent().respondWith({
            "status": 200,
            "responseText": JSON.stringify(
                {
                    percentDone: 45,
                    errors: [],
                    phase: "testing phase"
                })});

        expect($j("#buildStatus")).toHaveText("testing phase");

    });

    it("knows when to stop", function() {
        spyOn(TDAR.bulk, "updateProgress").and.callThrough();
        var settings = TDAR.bulk.init($("#divUploadStatus"));
        jasmine.clock().tick(501);
        jasmine.Ajax.requests.mostRecent().respondWith({
            "status": 200,
            "responseText": JSON.stringify(
                {
                    percentDone: 100,
                    errors: [],
                    phase: "should be done now"
                })});

        expect($j("#buildStatus")).toHaveText("Upload complete.");

    });

    it("displays errors", function() {
        spyOn(TDAR.bulk, "updateProgress").and.callThrough();
        var settings = TDAR.bulk.init($("#divUploadStatus"));
        jasmine.clock().tick(501);
        expect($j("#unspecifiedError")).not.toBeVisible();
        jasmine.Ajax.requests.mostRecent().respondWith({
            "status": 500,
            "responseText": JSON.stringify(
                {
                    percentDone: 88,
                    errors: ['yikes'],
                    phase: "choking"
                })});

        expect($j("#unspecifiedError")).toBeVisible();

    });



});
