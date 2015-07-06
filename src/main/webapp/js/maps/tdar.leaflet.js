TDAR.leaflet = {};
TDAR.leaflet = (function(console, $, ctx) {

    function _initLeafletMaps() {
        $(".leaflet-map").each(
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
                        var poly = [ [ $maxy, $maxx ], [ $maxy, $minx ], [ $miny, $minx ], [ $miny, $maxx ], [ $maxy, $maxx ] ];
                        console.log(poly);
                        var polygon = L.polygon(poly).addTo(map);
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

        });
    }

    return {
        initLeafletMaps : _initLeafletMaps
    }
})(console, jQuery, window);
$(function() {
    TDAR.leaflet.initLeafletMaps();
});