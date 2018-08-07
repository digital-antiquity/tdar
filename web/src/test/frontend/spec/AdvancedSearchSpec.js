/* global describe, it, expect */
describe("AdvancedSearchSpec.js: advanced search form", function() {
    "use strict";
    var submitCallback, $map, $form, $autosave;

    beforeEach(function(){
        jasmine.getFixtures().fixturesPath  =  "base/src/test/frontend/fixtures/";
        loadFixtures('advanced-search.html', 'advanced-search-template.html');
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
        $('.searchType').last().val("TITLE");
        TDAR.advancedSearch.serializeFormState();
        $form.submit();
        var $form2 = $($autosave.val());
        expect($form2.find('.searchType').val()).toBe("TITLE");
    })

    it("should display/hide  certain form fields based when user chooses a searchType ", function() {
        TDAR.advancedSearch.initAdvancedSearch();

        //the searchtype dropdown
        var $select = $('.searchType').last();

        //container div that holds the form fields associated with the current searchType.
        var $termContainer = $form.find('div.term-container');

        //For each choice in the searchType dropdown,  select that choice and  then check to see if we
        //are displaying the right fields.
        $select.find('option').map(function(){return this.value}).get()
            .forEach(function(searchType, i){

                var htmlBefore = $termContainer.html();

                //select dropdown option and trigger change event
                $select.val(searchType).change();

                var htmlAfter = $termContainer.html();

                //confirm that the contents of the termcontainer have changed
                expect(htmlBefore).not.toEqual(htmlAfter);
            });
    });

    it("lets the user add search terms", function() {
        TDAR.advancedSearch.initAdvancedSearch();
        //we should start out with one term
        expect($form.find('.termrow')).toHaveLength(1);

        //after clicking the 'add another' button, we should have two terms.
        $('#groupTable0AddAnotherButton').click();
        expect($form.find('.termrow')).toHaveLength(2);

        //make sure  the dropdowns still work correctly for both terms
        $form.find('.termrow').each(function(){
            var $termrow = $(this);

            var htmlBefore = $termrow.find('div.term-container').html();
            $termrow.find('select.searchType').val('RESOURCE_CREATOR_PERSON').change();
            var htmlAfter = $termrow.find('div.term-container').html();
            expect(htmlBefore).not.toEqual(htmlAfter);
        });

        //now try deleting the first termrow
        $form.find('.termrow').first().find('button.repeat-row-delete').click();
        expect($form.find('.termrow')).toHaveLength(1);
        expect($form.find('select.searchType').get(0).id).toEqual('group0searchType_1_');
    });
});

describe("search results page tests", function(){
    it("updates the results page when you change sorting, records-per-page", function() {
        loadFixtures("advanced-search-results.html");
        TDAR.advancedSearch.initializeResultsPage();
        spyOn(TDAR, 'windowLocation');

        //if you pick a new records-per-page value, the page should  should 'reload' w/ updated  querystring
        $('#recordsPerPage').val('50').change();
        expect(TDAR.windowLocation).toHaveBeenCalledWith('?&recordsPerPage=50');
        TDAR.windowLocation.calls.reset();

        //page should similarly reload if you choose a new sort option
        $('#sortField').val('TITLE').change();
        expect(TDAR.windowLocation).toHaveBeenCalledWith('?&sortField=TITLE');
    });
});