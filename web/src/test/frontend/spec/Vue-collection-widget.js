
/* global jasmine,  describe, it, expect, setFixtures, beforeEach, afterEach */
describe("Vue-collection-widget.js: collection widget test", function() {

    beforeEach(function() {
    });

    afterEach(function() {
        // uninstall the moxios proxy
        moxios.uninstall(axios);
    });

    
    //saveAddToCollectionChanges
    
    
    //cancelAddToCollectionChanges
    it("presses the cancel button to hide the modal window and reset the form.", function(done){
    	var fix = setupFixture();

        // only install the moxios proxy ONCE the fixture has been setup
        moxios.install(axios);
        // stub out moxios resquest/responses
        moxios.stubRequest('/api/collection/resourcecollections?resourceId=111', {
                status: 200,
                response: {
                	    "managed": [],
                	    "unmanaged": [
                	        {
                	            "verified": false,
                	            "status": "ACTIVE",
                	            "dateCreated": "2017-10-29T22:15:39-07:00",
                	            "dateUpdated": "2017-12-18T00:41:38-07:00",
                	            "collectionRevisionLog": [],
                	            "sortBy": "TITLE",
                	            "orientation": "LIST",
                	            "properties": {
                	                "id": null
                	            },
                	            "name": "Brian's Test Collection",
                	            "description": "",
                	            "unmanagedResources": [
                	                407547,
                	                407544,
                	                407545
                	            ],
                	            "slug": "brians-test-collection",
                	            "title": "Brian's Test Collection",
                	            "urlNamespace": "collection",
                	            "detailUrl": "/collection/65989/brians-test-collection",
                	            "alternateParentNameList": [],
                	            "supportsThumbnails": false,
                	            "id": 65989,
                	            "systemManaged": false,
                	            "ownerIdRef": "TdarUser:198283",
                	            "updaterIdRef": "TdarUser:198283",
                	            "hidden": false,
                	            "authorizedUsers": [
                	                {
                	                    "generalPermission": "ADMINISTER_COLLECTION",
                	                    "effectiveGeneralPermission": 5000,
                	                    "dateCreated": "2017-10-29T22:15:39-07:00",
                	                    "dateExpires": null,
                	                    "collectionId": 65989,
                	                    "resourceId": null,
                	                    "enabled": false,
                	                    "id": 507379,
                	                    "personRef": "TdarUser:198283",
                	                    "createdByRef": "TdarUser:198283"
                	                }
                	            ],
                	            "resources": []
                	        },
                	        {
                	            "verified": false,
                	            "status": "ACTIVE",
                	            "dateCreated": "2013-08-30T07:16:22-07:00",
                	            "dateUpdated": "2017-12-14T15:36:48-07:00",
                	            "collectionRevisionLog": [
                	                {
                	                    "timestamp": "2017-05-30T11:21:04-07:00",
                	                    "type": "EDIT",
                	                    "logMessage": "Fernandez, Rachel [186481 | rachel.fernandez.1@asu.edu | No institution specified.] modified TEST (Brian)",
                	                    "timeInSeconds": 25,
                	                    "id": 1438,
                	                    "resourceRef": "ResourceCollection:23471",
                	                    "personRef": "TdarUser:186481"
                	                },
                	                {
                	                    "timestamp": "2017-05-24T08:52:18-07:00",
                	                    "type": "EDIT",
                	                    "logMessage": "brin, adam [8344 | adam.brin@asu.edu | Center for Digital Antiquity] modified TEST",
                	                    "timeInSeconds": 23,
                	                    "id": 1330,
                	                    "resourceRef": "ResourceCollection:23471",
                	                    "personRef": "TdarUser:8344"
                	                },
                	                {
                	                    "timestamp": "2017-05-24T09:04:22-07:00",
                	                    "type": "EDIT",
                	                    "logMessage": "Fernandez, Rachel [186481 | rachel.fernandez.1@asu.edu | No institution specified.] modified TEST (Brian)",
                	                    "timeInSeconds": 144,
                	                    "id": 1331,
                	                    "resourceRef": "ResourceCollection:23471",
                	                    "personRef": "TdarUser:186481"
                	                },
                	                {
                	                    "timestamp": "2017-05-30T11:20:06-07:00",
                	                    "type": "EDIT",
                	                    "logMessage": "Fernandez, Rachel [186481 | rachel.fernandez.1@asu.edu | No institution specified.] modified TEST (Brian)",
                	                    "timeInSeconds": 118,
                	                    "id": 1436,
                	                    "resourceRef": "ResourceCollection:23471",
                	                    "personRef": "TdarUser:186481"
                	                }
                	            ],
                	            "sortBy": "TITLE",
                	            "orientation": "LIST",
                	            "properties": {
                	                "id": null
                	            },
                	            "name": "TEST (Brian)",
                	            "description": "",
                	            "formattedDescription": "",
                	            "unmanagedResources": [
                	                56291,
                	                190440,
                	                361448,
                	                361456,
                	                361459
                	            ],
                	            "slug": "test-brian",
                	            "title": "TEST (Brian)",
                	            "urlNamespace": "collection",
                	            "detailUrl": "/collection/23471/test-brian",
                	            "alternateParentNameList": [],
                	            "supportsThumbnails": false,
                	            "id": 23471,
                	            "systemManaged": false,
                	            "ownerIdRef": "TdarUser:8344",
                	            "updaterIdRef": "TdarUser:198283",
                	            "hidden": true,
                	            "authorizedUsers": [
                	                {
                	                    "generalPermission": "ADMINISTER_COLLECTION",
                	                    "effectiveGeneralPermission": 5000,
                	                    "dateCreated": "2013-08-30T07:16:22-07:00",
                	                    "dateExpires": null,
                	                    "collectionId": 23471,
                	                    "resourceId": null,
                	                    "enabled": false,
                	                    "id": 73334,
                	                    "personRef": "TdarUser:186481",
                	                    "createdByRef": "TdarUser:8344"
                	                },
                	                {
                	                    "generalPermission": "ADMINISTER_COLLECTION",
                	                    "effectiveGeneralPermission": 5000,
                	                    "dateCreated": "2017-10-13T14:27:56-07:00",
                	                    "dateExpires": null,
                	                    "collectionId": 23471,
                	                    "resourceId": null,
                	                    "enabled": false,
                	                    "id": 73349,
                	                    "personRef": "TdarUser:198283",
                	                    "createdByRef": "TdarUser:8344"
                	                },
                	                {
                	                    "generalPermission": "ADMINISTER_COLLECTION",
                	                    "effectiveGeneralPermission": 5000,
                	                    "dateCreated": "2017-06-06T10:14:15-07:00",
                	                    "dateExpires": null,
                	                    "collectionId": 23471,
                	                    "resourceId": null,
                	                    "enabled": false,
                	                    "id": 504152,
                	                    "personRef": "TdarUser:8344",
                	                    "createdByRef": "TdarUser:8344"
                	                }
                	            ],
                	            "resources": [
                	                377141,
                	                432277,
                	                376849,
                	                433427,
                	                425996,
                	                425961
                	            ]
                	        }
                	    ]
                }
        });
        
        var vapp = TDAR.vuejs.collectionwidget.init("#add-resource-form");
        
       // vapp.cancelAddToCollectionChanges();
        vapp.$forceUpdate();
        
        // make VueJS "tick" or re-render
        Vue.nextTick(function() {
        	expect(vapp.unmanagedCollectionsToRemove).toHaveLength(0);
            expect(fix.find("#existing-collections-list li")).not.toHaveLength(0);
            expect(fix.find("#existing-collections-list")).toContainText('TEST (Brian)');
            expect(vapp.collections.unmanaged).toHaveLength(2);
            
            vapp.removeResourceFromCollection(23471,'UNMANAGED');
 
            Vue.nextTick(function() {
               	 expect(vapp.unmanagedCollectionsToRemove).toHaveLength(1);
            	 //expect(vapp.collections.unmanaged).toHaveLength(1);
            	vapp.cancelAddToCollectionChanges();
            });
            
            Vue.nextTick(function() {
            	expect(vapp.unmanagedCollectionsToRemove).toHaveLength(0);
           	 	expect(vapp.collections.unmanaged).toHaveLength(2);
           	 	expect(fix.find("#existing-collections-list")).toContainText('TEST (Brian)');
           	 	expect(vapp.collections.unmanaged).toHaveLength(2);
            });
            done();
        });
        
    });
    
    
    
    //addToCollection
    
    //removeResourceFromCollection
    ///api/lookup/collection?permission=ADMINISTER_COLLECTION
    ///api/collection/resourcecollections?resourceId=111
    
    
    
    

    it("gracefully handles page with no map elements", function(done) {
    	var fix = setupFixture();

        // only install the moxios proxy ONCE the fixture has been setup
        moxios.install(axios);

        var vapp = TDAR.vuejs.collectionwidget.init("#add-resource-form");

        // stub out moxios resquest/responses
        moxios.stubRequest('/api/collection/resourcecollections?resourceId=111', {
                status: 200,
                response: {managed:[{id:5,name:'manhattan'}],unmanaged:[]}
        });
        
        expect($("#collection-list")).toHaveLength(1);
        // force vue to re-render
        vapp.$forceUpdate();
            
        // make VueJS "tick" or re-render
        Vue.nextTick(function() {
//              console.error(vapp.$el.querySelector("#existing-collections-list").innerHTML);
            // });
            //console.log(fix.find("#existing-collections-list").html()); 
            expect(fix.find("#existing-collections-list")).not.toHaveLength(0);
            expect(fix.find("#existing-collections-list")).toContainText('manhattan');
            
            done();
        });

        // console.error(vapp.$el.querySelector("#existing-collections-list").innerHTML);
        // console.log(moxios.requests.mostRecent());
        // console.log(request.url);
        // })
        
        console.info("------------------------------------- vue ---------------------------------------");
    });

    function setupFixture(){
        // change the fixture path to point to our template
        jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/resource/";
        
        // read the fixture as a string
        var fixture = jasmine.getFixtures().read("vue-collection-widget.html");
        
        // set some IDs
        fixture = fixture.replace("${editable?c}","false");
        fixture = fixture.replace("${resource.id?c}","111");

        // apply the fixxture
        var fix = jasmine.getFixtures().set(fixture);
        return fix;
    };
    
});