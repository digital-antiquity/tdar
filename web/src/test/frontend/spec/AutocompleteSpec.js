/* global describe, it, expect, beforeEach */
const TDAR = require("JS/tdar.master");
require("./SpecHelper");

describe("AutocompleteSpec.js: collection autocomplete", function(){
    

    var responses = {
        "/api/lookup/collection": {
            "status": {
                "startRecord": 0,
                "sortField": "TITLE",
                "totalRecords": 5,
                "recordsPerPage": 25
            },
            "collections": [
                {
                    "name": "Central Arizona Project Hot Dogs along the Gila River",
                    "description": "This is a description for 10001",
                    "detailUrl": "/collection/10001/central-arizona-project-hot-dogs-along-the-gila-river",
                    "id": 10001
                },
                {
                    "name": "Central Arizona Project Hot Dogs of Theodore Roosevelt Lake",
                    "description": "This is a description for 10002",
                    "detailUrl": "/collection/10002/central-arizona-project-hot-dogs-of-theodore-roosevelt-lake",
                    "id": 10002
                },
                {
                    "name": "Central Arizona Project Hot Dogs on the Ak-Chin Indian Community",
                    "description": "This is a description for 10003",
                    "detailUrl": "/collection/10003/central-arizona-project-hot-dogs-on-the-ak-chin-indian-community",
                    "id": 10003
                },
                {
                    "name": "Central Arizona Project Hot Dogs on the Gila River Indian Community",
                    "description": "This is a description for 10004",
                    "detailUrl": "/collection/10004/central-arizona-project-hot-dogs-on-the-gila-river-indian-community",
                    "id": 10004
                },
                {
                    "name": "Central Arizona Project Hot Dogs on the Pascua Yaqui Reservation",
                    "description": "This is a description for 10005",
                    "detailUrl": "/collection/10005/central-arizona-project-hot-dogs-on-the-pascua-yaqui-reservation",
                    "id": 10005
                }
            ]
        }
    };
    
    console.debug(TDAR.autocomplete.applyCollectionAutocomplete);

    var applyCollectionAutocomplete = TDAR.autocomplete.applyCollectionAutocomplete;

    beforeEach(function(){
        jasmine.getFixtures().fixturesPath = "base/src/test/frontend/fixtures";
        loadFixtures("autocomplete-collection.html");
        jasmine.Ajax.install();
    });

    afterEach(function(){
        jasmine.Ajax.uninstall();
    });


    it("collection autocomplete1", function(){

        var $elem = $('#txtResourceCollectionRow_0_id');
        applyCollectionAutocomplete($elem, {});

        $elem.focus();
        //force a lookup
        $elem.autocomplete('search', 'hot dogs');
        var req = jasmine.Ajax.requests.mostRecent()
        expect(req.url).toContain('/api/lookup/collection');

        var response  = jsonpEncode(responses["/api/lookup/collection"], req);


        jasmine.Ajax.requests.mostRecent().respondWith({
            "status": 200,
            "contentType": 'application/json',
            "responseText": response
        });

        

    });
});

describe("cached objects", function() {
    var cache;
    var $form;
    beforeEach(function(){
        setFixtures('<form id="testform"></form>');
        cache = new TDAR.autocomplete.ObjectCache({});
        $form = $("#testform");
        jasmine.Ajax.install();
    });

    afterEach(function() {
        jasmine.Ajax.uninstall();
    });

    it("registers input fields", function(){
        $form.append('<div id="obj1"><input type="text" name="firstName" value="bob"><input type="text" name="lastName" value="loblaw"></div>');
        cache.register(document.getElementById("obj1"));
        var vals = cache.getValues();
        expect(vals.length).toBe(1);

        cache.unregister("obj1");
        expect(cache.getValues().length).toBe(0);
    });

    it("searches for objects containing term", function() {
        cache.search = TDAR.autocomplete.ObjectCache.basicSearch;
        $form.append('<div id="obj1"><input type="text" class="ui-autocomplete-input" autocompleteName="firstName" '
            + 'name="firstName" value="bob"><input class="ui-autocomplete-input" autocompletename="lastName" '
            + 'type="text" name="lastName" value="loblaw"></div>');
        $form.append('<div id="obj2"><input type="text" class="ui-autocomplete-input" autocompleteName="firstName" '
            + 'name="firstName" value="gob"><input class="ui-autocomplete-input" autocompletename="lastName" '
            + 'type="text" name="lastName" value="bluth"></div>');
        $form.append('<div id="obj3"><input type="text" class="ui-autocomplete-input" autocompleteName="firstName" '
            + 'name="firstName" value="michael"><input class="ui-autocomplete-input" autocompletename="lastName" '
            + 'type="text" name="lastName" value="bluth"></div>');

        cache.register(document.getElementById("obj1"));
        cache.register(document.getElementById("obj2"));
        cache.register(document.getElementById("obj3"));

        //search in all object fields
        var results = cache.search("bob");
        expect(results.length).toBe(1);

        //search particular object field
        expect(cache.search("bob", "firstName").length).toBe(1);
        expect(cache.search("bob", "lastName").length).toBe(0);
        expect(cache.search("loblaw", "lastName").length).toBe(1);
    })

});

describe("tests for TDAR.autocomplete methods", function() {

    beforeEach(function(){
        jasmine.getFixtures().fixturesPath = "base/src/test/frontend/fixtures";
        jasmine.Ajax.install();
    });

    afterEach(function() {
        jasmine.Ajax.uninstall();
    });


    it("should treat record as 'not blank' ", function() {
        setFixtures('<form id="testform">'
            + '<div id="obj1" autocompleteIdElement="#pid">'
            + ' <input type="text"  autocompleteParentElement="#testform" class="ui-autocomplete-input" autocompleteName="firstName" name="firstName" value="bob">'
            + ' <input  autocompleteParentElement="#testform" class="ui-autocomplete-input" autocompleteName="lastName" type="text" name="lastName" value="loblaw">'
            + ' <input type="hidden" autocompleteParentElement="#testform" autocompleteName="id"  id="pid" value="1234">'
            + '</div>'
            + '</form>');
        var $form = $("#testform");
        var evaluateAutocompleteRowAsEmpty = TDAR.autocomplete.evaluateAutocompleteRowAsEmpty;
        var element = $form.find('input:first')[0];
        var result = evaluateAutocompleteRowAsEmpty(element, 0);
        expect(result).toBe(false);


    });


    //TODO: figure out why this doesn't return true. Better yet, rip out it's black heart and replace it w/ sensible vmmv framework.
    xit("should treat record as blank", function() {
        setFixtures('<form id="testform">'
            + '<div id="obj1" autocompleteIdElement="#pid">'
            + ' <input type="text"  autocompleteParentElement="#testform" class="ui-autocomplete-input" autocompleteName="firstName" name="firstName" value="">'
            + ' <input  autocompleteParentElement="#testform" class="ui-autocomplete-input" autocompleteName="lastName" type="text" name="lastName" value="">'
            + ' <input type="hidden" autocompleteParentElement="#testform" autocompleteName="id"  id="pid" value="">'
            + '</div>'
            + '</form>');
        var $form = $("#testform");
        var evaluateAutocompleteRowAsEmpty = TDAR.autocomplete.evaluateAutocompleteRowAsEmpty;
        var element = $form.find('input:first')[0];
        var result = evaluateAutocompleteRowAsEmpty(element, 1);
        expect(result).toBe(true);


    });

    xit("should work when we call applyKeywordAutocomplete", function() {
   var selector = null;
   var lookupType = null;
   var extraData = null;
   var newOption = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.applyKeywordAutocomplete(selector, lookupType, extraData, newOption);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call applyCollectionAutocomplete", function() {
   var $elements = null;
   var options = null;
   var extraData = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.applyCollectionAutocomplete($elements, options, extraData);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call applyResourceAutocomplete", function() {
   var $elements = null;
   var type = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.applyResourceAutocomplete($elements, type);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call applyInstitutionAutocomplete", function() {
   var $elements = null;
   var newOption = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.applyInstitutionAutocomplete($elements, newOption);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call applyComboboxAutocomplete", function() {
   var $elements = null;
   var type = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.applyComboboxAutocomplete($elements, type);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call objectFromAutocompleteParent", function() {
   var parentElem = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.objectFromAutocompleteParent(parentElem);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call delegateCreator", function() {
   var id = null;
   var user = null;
   var showCreate = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.delegateCreator(id, user, showCreate);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call delegateAnnotationKey", function() {
   var id = null;
   var prefix = null;
   var delim = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.delegateAnnotationKey(id, prefix, delim);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call delegateKeyword", function() {
   var id = null;
   var prefix = null;
   var type = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.delegateKeyword(id, prefix, type);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call buildRequestData", function() {
   var element = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.buildRequestData(element);
   expect(true).toBe(false); //fixme: implement this test
});

});
