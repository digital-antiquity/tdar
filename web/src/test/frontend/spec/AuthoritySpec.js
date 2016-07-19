/* global describe, it, expect */
describe("tests for TDAR.authority methods", function() {

    var responses = {
        '/lookup/institution': {
            "status": {
                "startRecord": 0,
                "sortField": "ID",
                "totalRecords": 80104,
                "recordsPerPage": 10
            },
            "institutions": [{
                "dateCreated": null,
                "url": null,
                "properName": "Arizona State Parks",
                "institutionName": null,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/11001/arizona-state-parks",
                "id": 11001,
                "status": "ACTIVE",
                "name": "Arizona State Parks"
            }, {
                "dateCreated": null,
                "url": null,
                "properName": "Arizona State Parks Board",
                "institutionName": null,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/11002/arizona-state-parks-board",
                "id": 11002,
                "status": "ACTIVE",
                "name": "Arizona State Parks Board"
            }, {
                "dateCreated": null,
                "url": null,
                "properName": "Arizona State University (ASU)",
                "institutionName": null,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/11003/arizona-state-university-asu",
                "id": 11003,
                "status": "ACTIVE",
                "name": "Arizona State University (ASU)"
            }, {
                "dateCreated": null,
                "url": null,
                "properName": "Arizona State University Museum of Anthropology",
                "institutionName": null,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/11004/arizona-state-university-museum-of-anthropology",
                "id": 11004,
                "status": "ACTIVE",
                "name": "Arizona State University Museum of Anthropology"
            }, {
                "dateCreated": null,
                "url": null,
                "properName": "Instituto de Investigaciones Antropologicas, UNAM",
                "institutionName": null,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/11007/instituto-de-investigaciones-antropologicas-unam",
                "id": 11007,
                "status": "ACTIVE",
                "name": "Instituto de Investigaciones Antropologicas, UNAM"
            }, {
                "dateCreated": null,
                "url": null,
                "properName": "National Science Foundation",
                "institutionName": null,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/11008/national-science-foundation",
                "id": 11008,
                "status": "ACTIVE",
                "name": "National Science Foundation"
            }, {
                "dateCreated": null,
                "url": null,
                "properName": "Pueblo of Zuni",
                "institutionName": null,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/11009/pueblo-of-zuni",
                "id": 11009,
                "status": "ACTIVE",
                "name": "Pueblo of Zuni"
            }, {
                "dateCreated": null,
                "url": null,
                "properName": "Soil Systems, Inc.",
                "institutionName": null,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/11010/soil-systems-inc",
                "id": 11010,
                "status": "ACTIVE",
                "name": "Soil Systems, Inc."
            }, {
                "dateCreated": null,
                "url": null,
                "properName": "SUNY Buffalo",
                "institutionName": null,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/11011/suny-buffalo",
                "id": 11011,
                "status": "ACTIVE",
                "name": "SUNY Buffalo"
            }, {
                "dateCreated": null,
                "url": null,
                "properName": "University of California, Los Angeles",
                "institutionName": null,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/11012/university-of-california-los-angeles",
                "id": 11012,
                "status": "ACTIVE",
                "name": "University of California, Los Angeles"
            }
            ]
        },
        'lookup/person': {
            "status": {
                "startRecord": 0,
                "sortField": "ID",
                "totalRecords": 72406,
                "recordsPerPage": 10
            },
            "people": [{
                "dateCreated": null,
                "url": null,
                "lastName": "Lee",
                "firstName": "Allen",
                "email": "allen.lee@asu.edu",
                "institution": {
                    "dateCreated": null,
                    "url": null,
                    "properName": "ASU",
                    "institutionName": null,
                    "urlNamespace": "browse/creators",
                    "detailUrl": "/browse/creators/11006/asu",
                    "id": 11006,
                    "status": "DUPLICATE",
                    "name": "ASU"
                },
                "registered": true,
                "properName": "Allen Lee",
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/1/allen-lee",
                "id": 1,
                "status": "ACTIVE"
            }, {
                "dateCreated": null,
                "url": null,
                "lastName": "Cao",
                "firstName": "Huiping",
                "email": "hcao11@asu.edu",
                "institution": {
                    "dateCreated": null,
                    "url": null,
                    "properName": "ASU",
                    "institutionName": null,
                    "urlNamespace": "browse/creators",
                    "detailUrl": "/browse/creators/11006/asu",
                    "id": 11006,
                    "status": "DUPLICATE",
                    "name": "ASU"
                },
                "registered": true,
                "properName": "Huiping Cao",
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/2/huiping-cao",
                "id": 2,
                "status": "ACTIVE"
            }, {
                "dateCreated": null,
                "url": null,
                "lastName": "Howard",
                "firstName": "John",
                "email": "john_howard@asu.edu",
                "institution": {
                    "dateCreated": null,
                    "url": null,
                    "properName": "Arizona State University (ASU)",
                    "institutionName": null,
                    "urlNamespace": "browse/creators",
                    "detailUrl": "/browse/creators/11003/arizona-state-university-asu",
                    "id": 11003,
                    "status": "ACTIVE",
                    "name": "Arizona State University (ASU)"
                },
                "registered": true,
                "properName": "John Howard",
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/3/john-howard",
                "id": 3,
                "status": "ACTIVE"
            }, {
                "dateCreated": null,
                "url": "",
                "lastName": "Whelan",
                "firstName": "Mary",
                "email": "mary.whelan@asu.edu",
                "institution": {
                    "dateCreated": null,
                    "url": null,
                    "properName": "Arizona State University (ASU)",
                    "institutionName": null,
                    "urlNamespace": "browse/creators",
                    "detailUrl": "/browse/creators/11003/arizona-state-university-asu",
                    "id": 11003,
                    "status": "ACTIVE",
                    "name": "Arizona State University (ASU)"
                },
                "registered": true,
                "properName": "Mary Whelan",
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/4/mary-whelan",
                "id": 4,
                "status": "ACTIVE"
            }, {
                "dateCreated": null,
                "url": "",
                "lastName": "Kintigh",
                "firstName": "Keith",
                "email": "kintigh@asu.edu",
                "institution": {
                    "dateCreated": null,
                    "url": null,
                    "properName": "Arizona State University (ASU)",
                    "institutionName": null,
                    "urlNamespace": "browse/creators",
                    "detailUrl": "/browse/creators/11003/arizona-state-university-asu",
                    "id": 11003,
                    "status": "ACTIVE",
                    "name": "Arizona State University (ASU)"
                },
                "registered": true,
                "properName": "Keith Kintigh",
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/6/keith-kintigh",
                "id": 6,
                "status": "ACTIVE"
            }, {
                "dateCreated": null,
                "url": null,
                "lastName": "Schiavitti",
                "firstName": "Vincent",
                "email": "schiavitti@hotmail.com",
                "institution": {
                    "dateCreated": null,
                    "url": null,
                    "properName": "ASU",
                    "institutionName": null,
                    "urlNamespace": "browse/creators",
                    "detailUrl": "/browse/creators/11006/asu",
                    "id": 11006,
                    "status": "DUPLICATE",
                    "name": "ASU"
                },
                "registered": true,
                "properName": "Vincent Schiavitti",
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/20/vincent-schiavitti",
                "id": 20,
                "status": "ACTIVE"
            }, {
                "dateCreated": null,
                "url": null,
                "lastName": "Nelson",
                "firstName": "Ben",
                "email": "b.nelson@asu",
                "institution": null,
                "properName": "Ben Nelson",
                "registered": false,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/24/ben-nelson",
                "id": 24,
                "status": "ACTIVE"
            }, {
                "dateCreated": null,
                "url": null,
                "lastName": "Hatch",
                "firstName": "Mallorie",
                "email": "mallorie.hatch@asu.edu",
                "institution": {
                    "dateCreated": null,
                    "url": null,
                    "properName": "ASU",
                    "institutionName": null,
                    "urlNamespace": "browse/creators",
                    "detailUrl": "/browse/creators/11006/asu",
                    "id": 11006,
                    "status": "DUPLICATE",
                    "name": "ASU"
                },
                "registered": true,
                "properName": "Mallorie Hatch",
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/38/mallorie-hatch",
                "id": 38,
                "status": "ACTIVE"
            }, {
                "dateCreated": null,
                "url": null,
                "lastName": "Peeples",
                "firstName": "Matthew",
                "email": "matthew.peeples@asu.edu",
                "institution": {
                    "dateCreated": 1334041200000,
                    "url": null,
                    "properName": "Archaeology Southwest",
                    "institutionName": null,
                    "urlNamespace": "browse/creators",
                    "detailUrl": "/browse/creators/137601/archaeology-southwest",
                    "id": 137601,
                    "status": "ACTIVE",
                    "name": "Archaeology Southwest"
                },
                "registered": true,
                "properName": "Matthew Peeples",
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/60/matthew-peeples",
                "id": 60,
                "status": "ACTIVE"
            }, {
                "dateCreated": null,
                "url": null,
                "lastName": "Nelson",
                "firstName": "Margaret",
                "email": "mnelson@asu.edu",
                "institution": null,
                "properName": "Margaret Nelson",
                "registered": false,
                "urlNamespace": "browse/creators",
                "detailUrl": "/browse/creators/63/margaret-nelson",
                "id": 63,
                "status": "ACTIVE"
            }
            ]
        }
    };

    beforeEach(function(){
        loadFixtures("authority-management.html");
        jasmine.Ajax.install();
    });

    afterEach(function() {
        jasmine.Ajax.uninstall();
    });

    it("initializes the authtable", function() {
        TDAR.authority.initAuthTable();

        //default lookup is institution
        expect(jasmine.Ajax.requests.mostRecent().url).toContain('/lookup/institution');

        //after returning results, the table should have stuff in it.
        var request = jasmine.Ajax.requests.mostRecent();
        var response = jsonpEncode(responses['/lookup/institution'], request);
        jasmine.Ajax.requests.mostRecent().respondWith({
            status:200,
            contentType: 'text/json',
            responseText: response
        });

        expect($('#dupe_datatable tbody tr')).toHaveLength(10);
        expect($("#divInstitutionSearchControl")).toBeVisible();
    });

    it("lists selected dupes", function(){
        TDAR.authority.initAuthTable();
        var request = jasmine.Ajax.requests.mostRecent();
        var response = jsonpEncode(responses['/lookup/institution'], request);
        jasmine.Ajax.requests.mostRecent().respondWith({
            status:200,
            contentType: 'text/json',
            responseText: response
        });

        expect($('input[type=checkbox]').length).toBeGreaterThan(8);

        //select all the rows
        $('#dupe_datatable input[type=checkbox]').click();
        var checkboxCount = $('#dupe_datatable input[type=checkbox]').length;

        expect(Object.keys($('#dupe_datatable').data('selectedRows')).length).toBe(checkboxCount);


    });

    xit("should work when we call clearDupeList", function() {
        TDAR.authority.initAuthTable();
        //var result = TDAR.authority.clearDupeList();
    });

    xit("should work when we call updateSearchControl", function() {
        var expectedVal = null;

        //var result = TDAR.authority.updateSearchControl();
        expect(true).toBe(false); //fixme: implement this test
    });

});
