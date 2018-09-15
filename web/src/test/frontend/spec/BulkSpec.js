/* global describe, it, expect */
const TDAR = require("JS/tdar.master");

describe("BulkSpec.js: tests for TDAR.bulk methods", function() {

    beforeEach(function() {
        jasmine.getFixtures().fixturesPath  =  "base/src/test/frontend/fixtures/";
        loadFixtures("bulk-upload-form.html");
        jasmine.Ajax.install();
//        jasmine.clock().uninstall();
//        jasmine.clock().install();
        spyOn(window, 'setTimeout');
        spyOn(window, 'alert');
    });

    afterEach(function(){
        jasmine.Ajax.uninstall();
//        jasmine.clock().uninstall();
    });

    it("should update progress", function() {
        spyOn(TDAR.bulk, "updateProgress").and.callThrough();
        var settings = TDAR.bulk.init($("#divUploadStatus"));
//        jasmine.clock().tick(501);
        var cb = window.setTimeout.calls.mostRecent().args[0];
        cb();
        expect(jasmine.Ajax.requests.mostRecent().url).toBe('/checkstatus');

        jasmine.Ajax.requests.mostRecent().respondWith({
            "status": 200,
            "responseText": JSON.stringify(
                {
                    percentDone: 45,
                    errors: [],
                    message: "testing phase"
                })});

        expect($j("#buildStatus")).toHaveText("testing phase");

    });

    it("knows when to stop", function() {
        spyOn(TDAR.bulk, "updateProgress").and.callThrough();
        var settings = TDAR.bulk.init($("#divUploadStatus"));
//        jasmine.clock().tick(501);
        var cb = window.setTimeout.calls.mostRecent().args[0];
        cb();

        jasmine.Ajax.requests.mostRecent().respondWith({
            "status": 200,
            "responseText": JSON.stringify(
                {
                    percentDone: 100,
                    errors: [],
                    message: "should be done now"
                })});

        expect($j("#buildStatus")).toHaveText("Upload complete.");

    });

    it("displays errors", function() {
        spyOn(TDAR.bulk, "updateProgress").and.callThrough();
        var settings = TDAR.bulk.init($("#divUploadStatus"));
//        jasmine.clock().tick(501);
        var cb = window.setTimeout.calls.mostRecent().args[0];
        cb();

        expect($j("#unspecifiedError")).not.toBeVisible();
        jasmine.Ajax.requests.mostRecent().respondWith({
            "status": 500,
            "responseText": JSON.stringify(
                {
                    percentDone: 88,
                    errors: ['yikes'],
                    message: "choking"
                })});

        expect($j("#unspecifiedError")).toBeVisible();

    });



});
