/* global describe, it, expect */

describe("collection autocomplete", function(){
    

    var responses = {
        "/lookup/collection": {
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
    

    var applyCollectionAutocomplete = TDAR.autocomplete.applyCollectionAutocomplete;

    beforeEach(function(){
        loadFixtures("autocomplete-collection.html");
        jasmine.Ajax.install();
    });

    afterEach(function(){
        jasmine.Ajax.uninstall();
    });


    it("collection autocomplete1", function(){

        var $elem = $('#txtResourceCollectionRow_0_id');
        applyCollectionAutocomplete($elem, false);

        $elem.focus();
        //force a lookup
        $elem.autocomplete('search', 'hot dogs');
        var req = jasmine.Ajax.requests.mostRecent()
        expect(req.url).toContain('/lookup/collection');

        var response  = jsonpEncode(responses["/lookup/collection"], req);


        jasmine.Ajax.requests.mostRecent().respondWith({
            "status": 200,
            "contentType": 'application/json',
            "responseText": response
        });

        

    });
});

describe("tests for TDAR.autocomplete methods", function() {  

xit("should work when we call applyPersonAutoComplete", function() {
   var $elements = null;
   var usersOnly = null;
   var showCreate = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.applyPersonAutoComplete($elements, usersOnly, showCreate);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call evaluateAutocompleteRowAsEmpty", function() {
   var element = null;
   var minCount = null;
   var expectedVal = null;

   //var result = TDAR.autocomplete.evaluateAutocompleteRowAsEmpty(element, minCount);
   expect(true).toBe(false); //fixme: implement this test
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
