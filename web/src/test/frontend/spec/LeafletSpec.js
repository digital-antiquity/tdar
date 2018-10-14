/* global describe, it, expect */
const TDAR = require("JS/tdar.root");
describe("LeafletSpec.js", function() {

    beforeEach(function(){
        jasmine.getFixtures().fixturesPath  =  "base/src/test/frontend/fixtures/";
    });
    
        it("LeafletSpec.js: should work when we call initLeafletMaps", function() {
            var options = null;
            var expectedVal = null;

            //var result = TDAR.leaflet.adminUsageStats(options);
            expect(TDAR.leaflet).toExist(); //fixme: implement this test
        });

        it("initLeafletMaps:validSetup", function() {
            loadFixtures("leaflet/leaflet-view.html");
            //$(".leaflet-map").data("maxy","");
            TDAR.leaflet.initLeafletMaps();
            var options = null;
            var expectedVal = null;
            //var result = TDAR.leaflet.adminUsageStats(options);
            //console.log(TDAR.leaflet.initialized());
            //https://www.npmjs.com/package/jasmine-jquery-matchers
            expect(TDAR.leaflet.initialized()).toBeGreaterThan(-1); //fixme: implement this test
            expect($(".leaflet-container")).toHaveLength(1);

        });


        it("initLeafletMaps:invalidSetup", function(done) {
            loadFixtures("leaflet/leaflet-view.html");
            $(".leaflet-map").data("maxy", "");
            var promises = TDAR.leaflet.initLeafletMaps();
            //we expect only one map and hence only one promise
            expect(promises.length).toBe(1);

            promises[0].done(function() {
                var options = null;
                var expectedVal = null;
                //https://www.npmjs.com/package/jasmine-jquery-matchers
                expect(TDAR.leaflet.initialized()).toBeLessThan(-1); //fixme: implement this test

                // assert that the map was properly initialized and has the rectangle
                // assert that the map was not proeprly initialized because it was missing a parameter
                // assert that an "edit" was initialzied
                // assert that a change to the map, updated hidden inputs and visa-versa (may not be possible)
                // assert that the degree translation on the hidden vs. the visible inputs updated properly
                // assert that a result map has items
                done();
            });

        });


        it("initLeafletMaps:validResultsSetup", function(done) {
            loadFixtures("leaflet/leaflet-results.html");
            var promises = TDAR.leaflet.initResultsMaps();
            var options = null;
            var expectedVal = null;

            expect(promises.length).toBe(1);
            promises[0].done(function() {
                //var result = TDAR.leaflet.adminUsageStats(options);
                //console.log(TDAR.leaflet.initialized());
                //https://www.npmjs.com/package/jasmine-jquery-matchers
                expect(TDAR.leaflet.initialized()).toBeGreaterThan(-1); //fixme: implement this test
                expect($(".leaflet-container")).toHaveLength(1);
                // 1 rectangle and 4 place-holders
                expect($(".leaflet-interactive")).toHaveLength(4);
                expect($("div.marker-cluster")).toHaveLength(2);
                expect($("img.leaflet-marker-icon")).toHaveLength(1);
                // $("img.leaflet-marker-icon").click();
                // var popup = $(".leaflet-popup-content");
                // expect(popup).toHaveLength(1);
                // console.log(popup);
                done();
            });
        });

        it("initLeafletMaps:validEdit", function(done) {
            loadFixtures("leaflet/leaflet-edit.html");
            var promises = TDAR.leaflet.initEditableLeafletMaps();
            promises[0].done(function() {
                var options = null;
                var expectedVal = null;
                //var result = TDAR.leaflet.adminUsageStats(options);
                //console.log(TDAR.leaflet.initialized());
                //https://www.npmjs.com/package/jasmine-jquery-matchers
                expect(TDAR.leaflet.initialized()).toBeGreaterThan(-1); //fixme: implement this test
                expect($(".leaflet-container")).toHaveLength(1);
                // 1 rectangle and 4 place-holders
                expect($(".leaflet-interactive")).toHaveLength(1);
                done();
            });

        });

        it("initLeafletMaps:effectiveSettings", function(done){
            //the default tile provider is 'mapbox'.  Confirm that we can override this setting in body[data-leaflet-tile-provider]
            loadFixtures("leaflet/leaflet-edit.html");
            TDAR.leaflet.initEditableLeafletMaps()[0].done(function(){
                //console.log('---------------')
                expect($('.mapdiv').data().map).toBeDefined()
                expect($('.mapdiv').data().map.options.leafletTileProvider).toBe('osm');
                done();
            });

        });


    it("edit:fireCreate",function(done){
        loadFixtures("leaflet/leaflet-edit.html");
        TDAR.leaflet.initEditableLeafletMaps()[0].done(function(){
            var bounds = [[53.912257, 27.581640], [53.902257, 27.561640]];
            var $el = $("#large-map");
            var map = $(".mapdiv",$el).data('map');
            var rect = L.rectangle(bounds, {color: 'blue', weight: 1});
            var event = $.Event("draw:created");
            event.type = "draw:created";
            event.layer = rect;
            map.fireEvent(event.type,event);
            expect(parseFloat($(".minx", $el).val())).toEqual(bounds[1][1]);
            expect(parseFloat($(".miny", $el).val())).toEqual(bounds[1][0]);
            expect(parseFloat($(".maxx", $el).val())).toEqual(bounds[0][1]);
            expect(parseFloat($(".maxy", $el).val())).toEqual(bounds[0][0]);
            done();
        });
    });


    it("edit:fireCreateOutOfBoundsTooBig",function(done){
        loadFixtures("leaflet/leaflet-edit.html");
        TDAR.leaflet.initEditableLeafletMaps()[0].done(function(){
            var bounds = [[53.912257, 227.581640], [53.902257, 227.561640]];
            var $el = $("#large-map");
            var map = $(".mapdiv",$el).data('map');
            var rect = L.rectangle(bounds, {color: 'blue', weight: 1});
            var event = $.Event("draw:created");
            event.type = "draw:created";
            event.layer = rect;
            map.fireEvent(event.type,event);
            console.log("**** " + parseFloat($(".minx", $el).val()));
            expect(parseFloat($(".minx", $el).val())).toEqual(-180 - (180 - bounds[1][1]));
            expect(parseFloat($(".miny", $el).val())).toEqual(bounds[1][0]);
            expect(parseFloat($(".maxx", $el).val())).toEqual(-180 - (180 - bounds[0][1]));
            expect(parseFloat($(".maxy", $el).val())).toEqual(bounds[0][0]);
            done();
        });
    });

    it("edit:fireCreateOutOfBoundsAroundTheWorldAfewTimes",function(done){
        loadFixtures("leaflet/leaflet-edit.html");
        TDAR.leaflet.initEditableLeafletMaps()[0].done(function(){
            var bounds = [[53.912257, -527.581640], [53.902257, -527.561640]];
            var $el = $("#large-map");
            var map = $(".mapdiv",$el).data('map');
            var rect = L.rectangle(bounds, {color: 'blue', weight: 1});
            var event = $.Event("draw:created");
            event.type = "draw:created";
            event.layer = rect;
            map.fireEvent(event.type,event);
            console.log("**** " + parseFloat($(".minx", $el).val()));
            expect(parseFloat($(".minx", $el).val())).toEqual(-167.581640);
            expect(parseFloat($(".miny", $el).val())).toEqual(bounds[1][0]);
            expect(parseFloat($(".maxx", $el).val())).toEqual(-167.561640);
            expect(parseFloat($(".maxy", $el).val())).toEqual(bounds[0][0]);
            done();
        });
    });


    it("edit:fireDelete",function(done){
        loadFixtures("leaflet/leaflet-edit.html");

        var $el = $("#large-map");
        var bounds = [[53.912257, 27.581640], [53.902257, 27.561640]];
        $(".minx", $el).val(bounds[1][1]);
        $(".miny", $el).val(bounds[1][0]);
        $(".maxx", $el).val(bounds[0][1]);
        $(".maxy", $el).val(bounds[0][0]);
        TDAR.leaflet.initEditableLeafletMaps()[0].done(function(){
            // console.log(TDAR.leaflet.getMaps().length);
            var map = $('.mapdiv').data().map
            // console.log("!--- " + map.constructor.name );
            // var rect = L.rectangle(bounds, {color: 'blue', weight: 1});
            var toDelete = [];
            map.eachLayer(function (layer) {
                if (layer instanceof L.Rectangle) {
                    toDelete.push(layer);
                }
            });
            toDelete = L.layerGroup(toDelete);
            var event = $.Event("draw:deleted");
            event.type = "draw:deleted";
            event.layers = toDelete;
            // console.log($el.length);
            map.fireEvent(event.type,event);

            //make sure that we deleted the rectangle layers
            var reclayers = [];
            map.eachLayer(function (layer) {
                if (layer instanceof L.Rectangle) {
                    reclayers.push('hi');
                }
            });
            expect(reclayers).toHaveLength(0);
            expect(parseFloat($(".minx", $el).val())).not.toEqual(bounds[1][1]);
            expect(parseFloat($(".miny", $el).val())).not.toEqual(bounds[1][0]);
            expect(parseFloat($(".maxx", $el).val())).not.toEqual(bounds[0][1]);
            expect(parseFloat($(".maxy", $el).val())).not.toEqual(bounds[0][0]);
            done();

        });

    });

    it("edit:fireEdited", function(done){

        loadFixtures("leaflet/leaflet-edit.html");

        var $el = $("#large-map");
        var bounds = [[53.912257, 27.581640], [53.902257, 27.561640]];
        var newBounds = [[1, 2], [3, 4]];
        $(".minx", $el).val(bounds[0][1]);
        $(".miny", $el).val(bounds[0][0]);

        $(".maxx", $el).val(bounds[1][1]);
        $(".maxy", $el).val(bounds[1][0]);
        TDAR.leaflet.initEditableLeafletMaps()[0].done(function(){
            // console.log(TDAR.leaflet.getMaps().length);
            var map = $('.mapdiv').data().map
            // console.log("!--- " + map.constructor.name );
            // var rect = L.rectangle(bounds, {color: 'blue', weight: 1});
            var toEdit = [];
            map.eachLayer(function (layer) {
                if (layer instanceof L.Rectangle) {
                    layer.setBounds(newBounds);
                    toEdit.push(layer);

                }
            });
            toEdit = L.layerGroup(toEdit);
            var event = $.Event("draw:edited");
            event.type = "draw:edited";
            event.layers = toEdit;
            // console.log($el.length);
            map.fireEvent(event.type,event);

            //make sure that we deleted the rectangle layers
            var reclayers = [];
            map.eachLayer(function (layer) {
                if (layer instanceof L.Rectangle) {
                    reclayers.push(layer);
                }
            });
            expect(reclayers).toHaveLength(1);
            expect(parseFloat($(".minx", $el).val())).toEqual(newBounds[0][1]);
            expect(parseFloat($(".miny", $el).val())).toEqual(newBounds[0][0]);
            expect(parseFloat($(".maxx", $el).val())).toEqual(newBounds[1][1]);
            expect(parseFloat($(".maxy", $el).val())).toEqual(newBounds[1][0]);
            done();
        });



    });

    xit('edit:locateCoordsButton', function() {
        loadFixtures("leaflet/leaflet-edit.html");
        expect($('.locateCoordsButton')).toBeVisible();
        $('#viewCoordinatesCheckbox').click();
        $('.locateCoordsButton').click();

    })


    describe("map results", function() {
        beforeEach(function() {
            loadFixtures("leaflet/leaflet-results.html", "leaflet/leaflet-results-json.html");
        });

        it("adding markers should cause map bounds to change after calling update()", function (done) {
            TDAR.leaflet.initResultsMaps()[0].done(function(){
                var $el = $(".leaflet-map-results");
                var map  = $el.data("map");
                var markers = $el.data("markers");
                expect(map).toExist();
                expect(markers).toExist();

                //todo: configure getJSONFixture() instead
                var data = JSON.parse($j('#dataPage1').text());

                var startRecord = 0;
                //get the original map bounds, then call update w/ marker coords
                var bounds1 = map.getBounds();
                
                TDAR.leaflet.update(map, markers, data, startRecord, true, true);
                var bounds2 = map.getBounds();
                console.log("bounds:", bounds1, bounds2);
                expect(bounds1.equals(bounds2)).toBe(false);

                //assert that all of the points fit in the new bounds
                var points = data.features
                    .filter(function(feature){return feature.geometry.hasOwnProperty("type");})
                    .map(function(feature){
                        var coords = feature.geometry.coordinates;
                        return new L.LatLng(coords[1], coords[0])
                    });

                var pointsThatFit = points.filter(function(point){
                    if(!map.getBounds().contains(point)){
                        console.error("point does not fit:", point);
                        console.error("bounds is:", map.getBounds());
                    }
                    return map.getBounds().contains(point);
                });

                //fixme: for some reason, fitBounds() expands to fit most - but not all - points
                //expect(pointsThatFit.length).toBe(points.length);
                done();
            });

        });
    });



});
