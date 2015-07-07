TDAR.leaflet = {};
TDAR.leaflet = (function(console, $, ctx) {

    function _initLeafletMaps() {
        $(".leaflet-map:not([data-attr-bind-ids='true'])").each(
                function() {
                    var $el = $(this);
                    var $minx = parseFloat($el.data("minx"));
                    var $miny = parseFloat($el.data("miny"));
                    var $maxx = parseFloat($el.data("maxx"));
                    var $maxy = parseFloat($el.data("maxy"));
                    if ($minx != undefined && $miny != undefined && $maxx != undefined && $maxy != undefined) {
                        console.log($minx, $maxx, $miny, $maxy);
                        var map = L.map(this).setView([ 51.505, -0.09 ], 13);
                        var tile = L.tileLayer('https://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png', {
                            maxZoom : 17,
                            attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, '
                                    + '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, '
                                    + 'Imagery © <a href="http://mapbox.com">Mapbox</a>',
                            id : 'examples.map-i875mjb7'
                        });
                        tile.addTo(map);
                        var poly = [ [ $maxy, $maxx ], [ $miny, $minx ]];
                        console.log(poly);
                        var rectangle = L.rectangle(poly).addTo(map);
                        map.fitBounds(polygon.getBounds());
                    }
                });
        _initEditableMaps();
    }
    
    function _initEditableMaps() {
        $(".leaflet-map[data-attr-bind-ids='true']").each(function(){
            var $el = $(this);
            var $minx = parseFloat($el.data("minx"));
            var $miny = parseFloat($el.data("miny"));
            var $maxx = parseFloat($el.data("maxx"));
            var $maxy = parseFloat($el.data("maxy"));
            console.log($minx, $maxx, $miny, $maxy);
            var map = L.map(this).setView([ 51.505, -0.09 ], 13);
            var tile = L.tileLayer('https://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png', {
                maxZoom : 17,
                attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, '
                        + '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, '
                        + 'Imagery © <a href="http://mapbox.com">Mapbox</a>',
                id : 'examples.map-i875mjb7'
            });
            tile.addTo(map);
            var poly = [ [ $maxy, $maxx ], [ $miny, $minx ]];
            var rectangle = L.rectangle(poly).addTo(map);
            
            // Initialise the FeatureGroup to store editable layers
            var drawnItems = new L.FeatureGroup();
            drawnItems.addLayer(rectangle);
            map.fitBounds(drawnItems.getBounds());
            map.addLayer(drawnItems);

            var options = {
                    position: 'topleft',
                    draw: {
                        polyline: false,
                        polygon: false,
                        circle: false, // Turns off this drawing tool
                        rectangle: {
                            shapeOptions: {
                                clickable: true
                            },
                            repeatMode: false
                        },
                        marker: false
                    },
                    edit: {
                        featureGroup: drawnItems,
                        remove: true
                    }
                };
            
            // Initialise the draw control and pass it the FeatureGroup of editable layers
            var drawControl = new L.Control.Draw(options);
            map.addControl(drawControl);
            
            /**
             * Assumption of only one bounding box
             */
            map.on('draw:created', function (e) {
                var type = e.layerType,
                    layer = e.layer;

                drawnItems.addLayer(layer);
                var b = layer.getBounds();
                $("#minx").val(b.getWest());
                $("#miny").val(b.getSouth());
                $("#maxx").val(b.getEast());
                $("#maxy").val(b.getNorth());
                map.addLayer(layer);
//                $(".leaflet-draw-draw-rectangle")
            });
            
            /**
             * Assumption of only one bounding box
             */
            map.on('draw:edited', function (e) {
                var layers = e.layers;
                layers.eachLayer(function (layer) {
                    var b = layer.getBounds();
                    $("#minx").val(b.getWest());
                    $("#miny").val(b.getSouth());
                    $("#maxx").val(b.getEast());
                    $("#maxy").val(b.getNorth());
                });
            });
            
            /**
             * Assumption of only one bounding box
             */
            map.on('draw:deleted', function (e) {
                var layers = e.layers;
                layers.eachLayer(function (layer) {
                    drawnItems.removeLayer(layer);
                    $("#minx").val('');
                    $("#miny").val('');
                    $("#maxx").val('');
                    $("#maxy").val('');
                });
            });
        });
    }

    return {
        initLeafletMaps : _initLeafletMaps
    }
})(console, jQuery, window);
$(function() {
    TDAR.leaflet.initLeafletMaps();
});