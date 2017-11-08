
/* global jasmine,  describe, it, expect, setFixtures, beforeEach, afterEach */
describe("Vue-collection-widget.js: collection widget test", function() {

    var _fixturePath = '';
    beforeEach(function() {
        // _fixturePath = jasmine.getFixtures().fixturesPath;
        // jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/";
        
        //jasmine.Ajax.install();
    });

    afterEach(function() {
        moxios.uninstall(axios);
        // jasmine.getFixtures().fixturesPath = _fixturePath;
        //jasmine.Ajax.uninstall();
    });




    it("gracefully handles page with no map elements", function(done) {
        //basically we just want to run this on a blank page, which should do nothing.  Jasmine will fail if it throws an error.
        console.info("------------------------------------- vue ---------------------------------------");
        
        // moxios.withMock(function () {
            jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/resource/";
        var fixture = jasmine.getFixtures().read("vue-collection-widget.html");
        
        fixture = fixture.replace("${editable?c}","false");
        fixture = fixture.replace("${resource.id?c}","111");

        var fix = jasmine.getFixtures().set(fixture);

        moxios.install(axios);

        var vapp = TDAR.vuejs.collectionwidget.init("#add-resource-form");

        moxios.stubRequest('/api/collection/resourcecollections?resourceId=111', {
                status: 200,
                response: {managed:[{id:5,name:'manhattan'}],unmanaged:[]}
            });
        
                expect($("#collection-list")).toHaveLength(1);

                vapp.$forceUpdate();

 Vue.nextTick(function() {
    vapp.$forceUpdate();
        console.error(vapp.$el.querySelector("#existing-collections-list").innerHTML);
    // });
    console.log(fix.find("#existing-collections-list")); 
    done();
});
            // console.error(vapp.$el.querySelector("#existing-collections-list").innerHTML);
            
            // console.log(moxios.requests.mostRecent());
            // console.log(request.url);
        // })


        
        console.info("------------------------------------- vue ---------------------------------------");
    // });
        // var elemcount = $('body').find('*').length;
        // TDAR.worldmap.initWorldMap();
        // expect($('body').find('*').length, elemcount);
    });

    // function _setupFixture(){
    //     var $mapcontainer = $j('<div id="mapcontainer"></div>');
    //     var $script = $j('<script type="application/json" data-mapdata></script>');
    //     $script.html(JSON.stringify(mapdata));
    //     var $script2 = $j('<script type="application/json" data-locales></script>');
    //     $script2.html(JSON.stringify(locales));
    //     var $mapdiv = $j('<div id="worldmap"></div>');
    //     $mapcontainer.append($script).append($script2).append($mapdiv);
    //     setFixtures($mapcontainer);
    // };
    //
    // it("initializes maps", function(done) {
    //     _setupFixture();
    //     var $mapdiv = $j('#worldmap');
    //     expect($mapdiv.find('*')).toHaveLength(0);
    //
    //     var map = TDAR.worldmap.initWorldMap();
    //     map.whenReady(function(){
    //         expect($mapdiv.find('*')).not.toHaveLength(0);
    //         expect($mapdiv).toHaveClass('leaflet-container');
    //         done();
    //     });
    //
    // });
    //
    // xit("shows the country you clicked on", function() {
    //     _setupFixture();
    //     var map = TDAR.worldmap.initWorldMap();
    //
    //     //fixme: for some reason the $.getJSON callback  in tdar.worldmap.js is never called
    //     //fixme: when countries are loaded use map.fireEvent() to 'click' on them and confirm the UI updates accordingly
    // });
    //
    // it("has a valid worlds.json file", function(done){
    //     var xhr = $j.getJSON("/js/maps/world.json");
    //
    //     xhr.done(function(data){
    //         expect(typeof data).toBe("object");
    //     });
    //
    //     xhr.fail(function(data){
    //         fail("jquery.getJSON failed - probably due to malformed json");
    //     });
    //
    //     xhr.always(function(){done();});
    // });


});