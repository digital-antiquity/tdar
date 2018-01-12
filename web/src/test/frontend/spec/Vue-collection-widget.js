
/* global jasmine,  describe, it, expect, setFixtures, beforeEach, afterEach */
describe("Vue-collection-widget.js: collection widget test", function() {

    beforeEach(function() {
    });

    afterEach(function() {
        // uninstall the moxios proxy
        moxios.uninstall(axios);
    });


    it("gracefully handles page with no map elements", function(done) {

        // change the fixture path to point to our template
        jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/resource/";
        
        // read the fixture as a string
        var fixture = jasmine.getFixtures().read("vue-collection-widget.html");
        
        // set some IDs
        fixture = fixture.replace("${editable?c}","false");
        fixture = fixture.replace("${resource.id?c}","111");

        // apply the fixxture
        var fix = jasmine.getFixtures().set(fixture);

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
            console.log(fix.find("#existing-collections-list").html()); 
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

});