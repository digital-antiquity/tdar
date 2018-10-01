/* global jasmine,  describe, it, expect, setFixtures, beforeEach, afterEach */

const TDAR = require("JS/tdar.root");

describe("WorldMapSpec.js: worldmap tests (tdar.worldmap.js)", function() {
    var mapdata = {
            "geographic.ISO,resourceType": [
                {
                    "field": "geographic.ISO",
                    "count": 1,
                    "pivot": [
                        {
                            "field": "resourceType",
                            "count": 1,
                            "value": "DOCUMENT"
                        }
                    ],
                    "value": "ABW"
                },
                {
                    "field": "geographic.ISO",
                    "count": 3,
                    "pivot": [
                        {
                            "field": "resourceType",
                            "count": 1,
                            "value": "DOCUMENT"
                        }
                    ],
                    "value": "AIA"
                },
                {
                    "field": "geographic.ISO",
                    "count": 1,
                    "pivot": [
                        {
                            "field": "resourceType",
                            "count": 1,
                            "value": "DOCUMENT"
                        }
                    ],
                    "value": "ANT"
                }
            ]
        }
    
    var locales = {
            "DOCUMENT": "Document",
            "DOCUMENT_PLURAL" : "Documents"
    }
    
    beforeEach(function() {
        //jasmine.Ajax.install();
    });

    afterEach(function() {
        //jasmine.Ajax.uninstall();
    });

    it("gracefully handles page with no map elements", function() {
        //basically we just want to run this on a blank page, which should do nothing.  Jasmine will fail if it throws an error.
        var elemcount = $('body').find('*').length;
        TDAR.worldmap.initWorldMap();
        expect($('body').find('*').length, elemcount);
    });

    function _setupFixture(){
        var $mapcontainer = $j('<div id="mapcontainer"></div>');
        var $script = $j('<script type="application/json" data-mapdata></script>');
        $script.html(JSON.stringify(mapdata));
        var $script2 = $j('<script type="application/json" data-locales></script>');
        $script2.html(JSON.stringify(locales));
        var $mapdiv = $j('<div id="worldmap"></div>');
        $mapcontainer.append($script).append($script2).append($mapdiv);
        setFixtures($mapcontainer);
    };

    it("initializes maps", function(done) {
        _setupFixture();
        var $mapdiv = $j('#worldmap');
        expect($mapdiv.find('*')).toHaveLength(0);

        var map = TDAR.worldmap.initWorldMap();
        map.whenReady(function(){
            expect($mapdiv.find('*')).not.toHaveLength(0);
            expect($mapdiv).toHaveClass('leaflet-container');
            done();
        });

    });

    xit("shows the country you clicked on", function() {
        _setupFixture();
        var map = TDAR.worldmap.initWorldMap();

        //fixme: for some reason the $.getJSON callback  in tdar.worldmap.js is never called
        //fixme: when countries are loaded use map.fireEvent() to 'click' on them and confirm the UI updates accordingly
    });

    it("has a valid worlds.json file", function(done){
        var xhr = $j.getJSON("/js/maps/world.json");

        xhr.done(function(data){
            expect(typeof data).toBe("object");
        });

        xhr.fail(function(data){
            fail("jquery.getJSON failed - probably due to malformed json");
        });

        xhr.always(function(){done();});
    });


});