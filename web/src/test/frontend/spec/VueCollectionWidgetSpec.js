/* global jasmine,  describe, it, expect, setFixtures, beforeEach, afterEach */
const Vue = require("vue/dist/vue.esm.js").default;
const TDAR = require("JS/tdar.master");
const axios = require("axios");


xdescribe("Vue-collection-widget.js: collection widget test", function() {
    beforeEach(function() {
        moxios.install(axios);
    });

    afterEach(function() {
        // uninstall the moxios proxy
        moxios.uninstall(axios);
    });
    
    it("Creates a new collection", function(done){
    	var fix = setupFixture("true","true","1");
        moxios.stubRequest('api/collection/newcollection', {
        	    "name": "abc",
        	    "id": 66061,
        	    "status": "success"
        });
        
        moxios.stubRequest('/api/collection/resourcecollections?resourceId=1', {
            status: 200,
            response: {
            	    "managed": [],
            	    "unmanaged": [
            	        {
        	            "sortBy": "TITLE",
        	            "id": 66061,
        	            "name": "Test",
        	            "description": "",
        	            "unmanagedResources": [
            	                407547,
            	                407544,
            	                407545
            	            ],
            	            "resources": []
            	        }
            	    ]
            }
    });
        
    var vapp = TDAR.vuejs.collectionwidget.init("#add-resource-form");
        
    vapp.newCollectionName = "ABC";
    vapp.addToCollection();
    
    Vue.nextTick(function() {
        	expect(vapp.collections.unmanaged[0].name).toBe("Test");
    });

    done();
    });
    
    //cancelAddToCollectionChanges
    it("presses the cancel button to hide the modal window and reset the form.", function(done){
    	var fix = setupFixture("true","true","2");
    	
        // only install the moxios proxy ONCE the fixture has been setup
       // moxios.install(axios);
        
        //Mock a result with 2 existing Unmanaged collections. 
        moxios.stubRequest('/api/collection/resourcecollections?resourceId=2', {
                status: 200,
                response: {
                	    "managed": [{
            	            "sortBy": "TITLE",
            	            "id": 65989,
            	            "name": "Test Collection",
            	            "description": "",
            	            "unmanagedResources": [
                	                407547,
                	                407544,
                	                407545
                	            ],
                	            "resources": []
                	        },{
                	            "sortBy": "TITLE",
                	            "id": 65989,
                	            "name": "Test Collection",
                	            "description": "",
                	            "unmanagedResources": [
                    	                407547,
                    	                407544,
                    	                407545
                    	            ],
                    	            "resources": []
                    	        },{
                    	            "sortBy": "TITLE",
                    	            "id": 65989,
                    	            "name": "Test Collection",
                    	            "description": "",
                    	            "unmanagedResources": [
                        	                407547,
                        	                407544,
                        	                407545
                        	            ],
                        	            "resources": []
                        	        }],
                	    "unmanaged": [
                	        {
            	            "sortBy": "TITLE",
            	            "id": 65989,
            	            "name": "Test Collection",
            	            "description": "",
            	            "unmanagedResources": [
                	                407547,
                	                407544,
                	                407545
                	            ],
                	            "resources": []
                	        },
                	        {
                	            "name": "Existing Unmanaged Collection",
                	            "id": 23471,
                	            "description": "",
                	            "formattedDescription": "",
                	            "unmanagedResources": [
                	                56291,
                	                190440,
                	                361448,
                	                361456,
                	                361459
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
        	//The initial state is to have one existing collection.
        	//Verify that the collection is in the vue model.
        	expect(vapp.unmanagedCollectionsToRemove).toHaveLength(0);
            expect(fix.find("#existing-collections-list li")).not.toHaveLength(0);
            expect(fix.find("#existing-collections-list")).toContainText('Existing Unmanaged Collection');
            expect(vapp.collections.unmanaged).toHaveLength(2);
            
            //Simulate clicking the remove link.
            vapp.removeResourceFromCollection(23471,'UNMANAGED');
 
            
            //Verify that the object is removed, and theres only one collection displayed. 
            Vue.nextTick(function() {
            	 expect(vapp.unmanagedCollectionsToRemove).toHaveLength(1);
            	 
                expect(vapp.collections.unmanaged).toHaveLength(2);
            	vapp.cancelAddToCollectionChanges();
            });
            
            Vue.nextTick(function() {
            	expect(vapp.unmanagedCollectionsToRemove).toHaveLength(0);
           	 	expect(vapp.collections.unmanaged).toHaveLength(2);
           	 	expect(fix.find("#existing-collections-list")).toContainText('Existing Unmanaged Collection');
           	 	expect(vapp.collections.unmanaged).toHaveLength(2);
            });
            
            done();
        });
        
    });
    
    //removeResourceFromCollection
    it("removes a resource from a collection", function(done){
    	var fix = setupFixture("true","true","3");

        // only install the moxios proxy ONCE the fixture has been setup
        //moxios.install(axios);
        
        // stub out moxios resquest/responses
        moxios.stubRequest('/api/collection/resourcecollections?resourceId=3', {
                status: 200,
                response: {
                	    "managed": [  {
            	            "sortBy": "TITLE",
            	            "id": 65989,
            	            "name": "Test Collection",
            	            "description": "",
            	            "unmanagedResources": [
                	                407547,
                	                407544,
                	                407545
                	            ],
                	            "resources": []
                	        }],
                	    "unmanaged": [
                	        {
            	            "sortBy": "TITLE",
            	            "id": 65989,
            	            "name": "Test Collection",
            	            "description": "",
            	            "unmanagedResources": [
                	                407547,
                	                407544,
                	                407545
                	            ],
                	            "resources": []
                	        },
                	        {
                	            "name": "Existing Unmanaged Collection",
                	            "id": 23471,
                	            "description": "",
                	            "formattedDescription": "",
                	            "unmanagedResources": [
                	                56291,
                	                190440,
                	                361448,
                	                361456,
                	                361459
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
        vapp.$forceUpdate();

        Vue.nextTick(function(){
        	vapp.removeResourceFromCollection(vapp.collections.managed[0],"MANAGED");
        	expect(vapp.collections.managed.length).toBe(0);
        	expect(vapp.managedCollectionsToRemove.length).toBe(1);
        	done();
        });
    });
    
    it("gets a list of collections the user has permissions to", function(done){
    	var fix = setupFixture("false","true","4");
       
        moxios.stubRequest('/api/collection/resourcecollections?resourceId=4', {
        	status: 200,
            response:{
            "managed": [
                {
                    "status": "ACTIVE",
                    "name": "Test 1234",
                    "description": "",
                    "detailUrl": "/collection/66024/test-1234",
                    "id": 66024
                },
                {
                    "status": "ACTIVE",
                    "name": "Cibola Prehistory Project (Collection)",
                    "description": "",
                    "detailUrl": "/collection/6995/cibola-prehistory-project-collection",
                    "id": 6995
                }
            ],
            "unmanaged": [
                {
                    "status": "ACTIVE",
                    "name": "abc",
                    "description": "",
                    "detailUrl": "/collection/66061/abc",
                    "id": 66061
                },
                {
                    "status": "ACTIVE",
                    "name": "New Test Collection",
                    "description": "",
                    "detailUrl": "/collection/66056/new-test-collection",
                    "id": 66056
                },
                {
                    "status": "ACTIVE",
                    "name": "Test",
                    "description": "",
                    "detailUrl": "/collection/66057/test",
                    "id": 66057
                }
            ]
            }
        });
        
        var vapp = TDAR.vuejs.collectionwidget.init("#add-resource-form");
        
        vapp.$forceUpdate();
        
        Vue.nextTick(function() {
	       	expect(vapp.collections.managed.length).toBe(2);
	       	expect(vapp.collections.unmanaged.length).toBe(3);
	    	done();
        });
    });
    
    
    it("prevents non-admins from selecting the box.",function(done){
    	var fix = setupFixture("false","true","5");
        var vapp = TDAR.vuejs.collectionwidget.init("#add-resource-form");
        
        vapp.$forceUpdate();
        
        Vue.nextTick(function() {
        	expect(fix.find("#managedResource").is(":disabled")).toBe(true);
        	done();
        });
        
    });

    it("gracefully handles page with no map elements", function(done) {
    	var fix = setupFixture("true","true","5");

        //moxios.install(axios);

        var vapp = TDAR.vuejs.collectionwidget.init("#add-resource-form");
        
        // stub out moxios resquest/responses
        moxios.stubRequest('/api/collection/resourcecollections?resourceId=5', {
                status: 200,
                response: {managed:[{id:5,name:'manhattan'}],unmanaged:[]}
        });
        
        expect($("#collection-list")).toHaveLength(1);
        // force vue to re-render
        vapp.$forceUpdate();
            
        // make VueJS "tick" or re-render
        Vue.nextTick(function() {
        	// console.error(vapp.$el.querySelector("#existing-collections-list").innerHTML);
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

    function setupFixture(admin, editable, resourceid){
        // change the fixture path to point to our template
        jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/resource/";
        
        // read the fixture as a string
        var fixture = jasmine.getFixtures().read("vue-collection-widget.html");
        
        // set some IDs
        fixture = fixture.replace("${administrator?c}",admin);
        fixture = fixture.replace("${editor?c}",admin);
        fixture = fixture.replace("${editable?c}",editable);
        fixture = fixture.replace("${resource.id?c}",resourceid);

        // apply the fixxture
        var fix = jasmine.getFixtures().set(fixture);
        return fix;
    };
});