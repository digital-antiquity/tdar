
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
    	var fix = setupFixture("true","true","111");

        // only install the moxios proxy ONCE the fixture has been setup
        moxios.install(axios);
        // stub out moxios resquest/responses
        moxios.stubRequest('/api/collection/resourcecollections?resourceId=111', {
                status: 200,
                response: {
                	    "managed": [],
                	    "unmanaged": [
                	        {
            	            "sortBy": "TITLE",
            	            "id": 65989,
            	            "name": "Brian's Test Collection",
            	            "description": "",
            	            "unmanagedResources": [
                	                407547,
                	                407544,
                	                407545
                	            ],
                	            "resources": []
                	        },
                	        {
                	            "name": "TEST (Brian)",
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
    it("removes a resource from a collection", function(done){
    	var fix = setupFixture("true","true","111");

        // only install the moxios proxy ONCE the fixture has been setup
        moxios.install(axios);
        // stub out moxios resquest/responses
        moxios.stubRequest('/api/collection/resourcecollections?resourceId=111', {
                status: 200,
                response: {
                	    "managed": [],
                	    "unmanaged": [
                	        {
            	            "sortBy": "TITLE",
            	            "id": 65989,
            	            "name": "Brian's Test Collection",
            	            "description": "",
            	            "unmanagedResources": [
                	                407547,
                	                407544,
                	                407545
                	            ],
                	            "resources": []
                	        },
                	        {
                	            "name": "TEST (Brian)",
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
    	done();
    });
    
    ///api/lookup/collection?permission=ADMINISTER_COLLECTION
    it("gets a list of collections the user has permissions to", function(done){
    	done();
    });
    
    
    it("prevents non-admins from selecting the box.",function(done){
    	var fix = setupFixture("false","true","111");
        var vapp = TDAR.vuejs.collectionwidget.init("#add-resource-form");
        
        vapp.$forceUpdate();
        
        Vue.nextTick(function() {
        	expect(fix.find("#managedResource").is(":disabled")).toBe(true);
        });
    	done();
    })
    
  

    it("gracefully handles page with no map elements", function(done) {
    	var fix = setupFixture("true","true","111");

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

    function setupFixture(admin, editable, resourceid){
        // change the fixture path to point to our template
        jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/resource/";
        
        // read the fixture as a string
        var fixture = jasmine.getFixtures().read("vue-collection-widget.html");
        
        // set some IDs
        fixture = fixture.replace("${administrator?c}",admin);
        fixture = fixture.replace("${editable?c}",editable);
        fixture = fixture.replace("${resource.id?c}",resourceid);

        // apply the fixxture
        var fix = jasmine.getFixtures().set(fixture);
        return fix;
    };
    
});