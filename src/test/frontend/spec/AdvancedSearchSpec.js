/* global describe, it, expect */
describe("advanced search form", function() {
    "use strict";
    var submitCallback, $map, $form, $autosave;
    beforeEach(function(){
        loadFixtures('advanced-search.html');
        submitCallback = spyOn(document.forms.searchGroups, 'submit').and.returnValue(false);
        $map = $('#large-google-map');
        $form = $('#searchGroups');
        $autosave = $('#autosave');
    });

    it("serializes form state on submit", function() {
        //bind some data to the 'map'.  it should be purged after serialization.
        $('#large-google-map').data('foo', 'bar');
        //Same goes for dom artifacts added by the map renderer.
        $('#large-google-map').append('<div>remove me</div>');

        TDAR.advancedSearch.serializeFormState();
        $form.submit();

        //did we save something in the formstate
        expect($autosave.val().length).toBeGreaterThan(100);

        //did we purge unwanted data & map artifacts?
        expect($map.data('foo')).not.toBeDefined();
        expect($map.text()).not.toContain('remove me')
    });

    it("serializes current form field values", function() {
        $('#selSearchType0').val("TITLE");
        TDAR.advancedSearch.serializeFormState();
        $form.submit();

        var $form2 = $($autosave.val());
        expect($form2.find('.searchType').val()).toBe("TITLE");
    })


    xit("should work when we call initAdvancedSearch", function() {
        var $map = $('#large-google-map');



        TDAR.advancedSearch.initAdvancedSearch();
    });

    xit("should work when we call initializeResultsPage", function() {
        var expectedVal = null;

        //var result = TDAR.advancedSearch.initializeResultsPage();
        expect(true).toBe(false); //fixme: implement this test
    });

});
