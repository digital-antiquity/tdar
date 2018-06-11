/* global jasmine, describe, it, expect, loadFixtures, $j, $, beforeEach, afterEach, TDAR */
describe("BookmarkSpec.js: edit page tests", function () {
    "use strict";

    beforeEach(function() {
        jasmine.Ajax.install();
    });

    afterEach(function() {
        jasmine.Ajax.uninstall();
    });


    it("updates the server when you add/remove a bookmark", function () {
        var $elem = $('<span resource-id="12345" bookmark-state="bookmark">click me</span>');
        var $elem2 = $('<span resource-id="12345" bookmark-state="bookmarked">click me</span>');
        TDAR.bookmark.init();
        setFixtures($elem);
        TDAR.bookmark.ajaxBookmark.call($elem);
        expect(jasmine.Ajax.requests.mostRecent().url).toContain('resource/bookmarkAjax?resourceId=12345');            

        TDAR.bookmark.ajaxBookmark.call($elem2);
        jasmine.Ajax.requests.mostRecent().respondWith({
            status:200,
            contentType: 'text/json',
            responseText: '{"success": true}'    
        });
        expect(jasmine.Ajax.requests.mostRecent().url).toContain('resource/removeBookmarkAjax?resourceId=12345');



    });
});