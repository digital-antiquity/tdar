TDAR.leaflet = (function(console, $, ctx, L) {

    L.drawLocal.draw.toolbar.buttons.rectangle = 'Create bounding box';
    L.drawLocal.edit.toolbar.buttons.edit = 'Edit';
    L.drawLocal.edit.toolbar.buttons.editDisabled = 'No box to edit';
    L.drawLocal.edit.toolbar.buttons.remove = 'Delete';
    L.drawLocal.edit.toolbar.buttons.removeDisabled = 'No boxes to delete';
    var $body = $('body');

    var _tileProviders = {
        osm: {
            url: "http://{s}.tile.osm.org/{z}/{x}/{y}.png",
            attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        },

        mapbox: {
            //fixme: I assume we need a api token? If so perhaps we define this in data-attr of container elem?
            url: "https://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png",
            //fixme: consider pulling attribution html from hidden div or script[type=text/html]
            attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, '
            + '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, '
            + 'Imagery © <a href="http://mapbox.com">Mapbox</a>'
        }

    };

    var _defaults = {
        isGeoLocationToBeUsed: false,
        center: {
            lat: 0,
            lng: 0
        },
        zoomLevel: 4,
        tileProvider: 'osm',
        maxBounds: [[[-85, -180.0], [85, 180.0]]],
        minZoom: 2,
        maxZoom: 17

    };

    var _rectangleDefaults = {
        fitToBounds: true
    };

    // -1 -- not initialized ; -2 -- bad rectangle ; 1 -- initialized ; 2 -- rectangle setup
    var _initialized = -1;
    var _map;
    var _dc;

    /**
     * Init the leaflet map, and bind it to the element
     */
    function _initMap(elem) {
        // create effective settings from defaults, then body data-attrs, then elem data-attrs.
        var $elem = $(elem);
        var _elemData = $elem.data();
        // bootstrap stores a lot of data in BODY. We only want a subset
        var _bodyData = {leafletId: $('body').data().leafletId};
        var settings = $.extend({}, _defaults, _bodyData, _elemData);

        console.log('creating L.map:');
        var map = L.map(elem).setView([settings.center.lat, settings.center.lng], settings.zoomLevel);
        map.setMaxBounds(settings.maxBounds);
		$elem.data("map",map);
        var mapId = settings.leafletId;

        var tp = _tileProviders[settings.tileProvider];
        var tile = L.tileLayer(tp.url, {
            maxZoom: settings.maxZoom,
            minZoom: settings.minZoom,
            attribution: tp.attribution,
            id: mapId
        });
        console.log('adding tile to map');
        tile.addTo(map);
        //FIXME: WARN if DIV DOM HEIGHT IS EMPTY
        _map = map;
        _initialized = 0;
        return map;
    }

    function _initResultsMaps() {
        $(".leaflet-map-results").each(function() {
            var $el = $(this);
            var map = _initMap(this);
            var markers = new L.MarkerClusterGroup();
            var recDefaults = $.extend(_rectangleDefaults, {
                fillOpacity: 0.08,
                fitToBounds: true
            });
            _initFromDataAttr($el, map, recDefaults);
            $(".resource-list.MAP .listItem").each(function() {
                var $t = $(this);
                var title = $(".resourceLink", $t);
                var lat = $t.data("lat");
                var lng = $t.data("long");
                if (!isNaN(lat) && !isNaN(lng)) {
                    var marker = L.marker(new L.LatLng(lat, lng), {title: title.text().trim()});
                    marker.bindPopup(title.html() + "<br><a href='" + title.attr('href') + "'>view</a>");
                    markers.addLayer(marker);
                }
            });
            map.addLayer(markers);
        });
    }

    /**
     * init a "view" only map, binds to data-attribute minx, miny, maxx, maxy
     */
    function _initLeafletMaps() {
        $(".leaflet-map").each(
            function() {
                var $el = $(this);
                var map = _initMap(this);
                _initFromDataAttr($el, map, _rectangleDefaults);
            });
    }


    function _initFromDataAttr($el, map, rectangleSettings) {
        var $minx = parseFloat($el.data("minx"));
        var $miny = parseFloat($el.data("miny"));
        var $maxx = parseFloat($el.data("maxx"));
        var $maxy = parseFloat($el.data("maxy"));
        _initRectangle(map, $minx, $miny, $maxx, $maxy, rectangleSettings);

    }

    /**
     * create the rectangle based on the bounds
     */
    function _initRectangle(map, minx, miny, maxx, maxy, rectangleSettings) {
        if (minx != undefined && miny != undefined && maxx != undefined && maxy != undefined && !isNaN(minx) && !isNaN(miny) && !isNaN(maxy) && !isNaN(maxx)) {
            console.log(minx, maxx, miny, maxy);
            var poly = [[maxy, maxx], [miny, minx]];
            var rectangle = L.rectangle(poly, rectangleSettings).addTo(map);
            console.log("fitToBounds:", rectangleSettings.fitToBounds);
            if (rectangleSettings.fitToBounds) {
                map.fitBounds(rectangle.getBounds());
            }
            _initialized = 2;
            return rectangle;
        } else if (minx == undefined && miny == undefined && maxx == undefined && maxy == undefined) {
            // skipping, we're just not configured at all;
            return;
        }
        _initialized = -2;
        console.log("check map init bounds [" + minx + "," + miny + "] [" + maxx + "," + maxy + "]");
    }

    /**
     * Updates a leaflet layer (removes/adds) based on the .minx, .miny, .maxx, .maxy vals
     */
    function _updateLayerFromFields($el, map, drawnItems, $mapDiv) {
        var $minx = parseFloat($(".minx", $el).val());
        var $miny = parseFloat($(".miny", $el).val());
        var $maxx = parseFloat($(".maxx", $el).val());
        var $maxy = parseFloat($(".maxy", $el).val());
        // Initialise the FeatureGroup to store editable layers
        var layers = drawnItems.getLayers();

        // remove the old layer
        if (layers.length > 0) {
            drawnItems.removeLayer(layers[0]);
        }

        var rectangle = _initRectangle(map, $minx, $miny, $maxx, $maxy, _rectangleDefaults);

        if (rectangle != undefined) {
            _disableRectangleCreate($mapDiv);
            drawnItems.addLayer(rectangle);
        } else {
            _enableRectangleCreate();
            for (var i = 0; i > drawnItems.getLayers().length; i++) {
                drawnItems.removeLayer(drawnItems.getLayer(i));
            }
        }
        return rectangle;

    }

    /**
     * Init an editable map, it uses a set of classes to determine values .minx, .maxx, .miny, .maxy for actual values; .d_* for display values, and
     * .locateCoordsButton for the locate button
     *
     * this will create the map just before the element specified
     */
    function _initEditableMaps() {
        $(".leaflet-map-editable").each(function() {
            var $el = $(this);
            // we create a div just before the div and copy the styles from the container. This is so that we can bind to class seletion for the fields.
            // Currently, this is a bit of overkill but it would enable multiple forms on the same page

            // An 'editable' map container needs two child nodes.
            // - The first child node is a container for the latlong box form fields. Designate this node with
            //   the .latlong-fields css class
            //
            // - The second child node contains the actual leaflet map (e.g. the mapdiv). Designate this node with
            //   the .mapdiv css class.
            var div = $el.find('div.mapdiv')[0];
            //var div = document.createElement("div");
            //$el.append(div);

            // copy styles
            div.setAttribute("style", $el.attr("style"));
            $el.attr("style", "");
            var $mapDiv = $(div);
            $mapDiv.data("leaflet-id", $el.data("leaflet-id"));
            if ($mapDiv.height() == 0) {
                $mapDiv.height(400);
            }
            var map = _initMap(div);

            var drawnItems = new L.FeatureGroup();

            // bind ids
            var $dmx = $(".d_maxx", $el);
            var $dix = $(".d_minx", $el);
            var $dmy = $(".d_maxy", $el);
            var $diy = $(".d_miny", $el);
            // handling text based formats too    Geo.parseDMS("51°33′39″N" ); ...
            $dmx.change(function() {
                $(".maxx", $el).val(Geo.parseDMS($dmx.val()));
            });
            $dmy.change(function() {
                $(".maxy", $el).val(Geo.parseDMS($dmy.val()));
            });
            $dix.change(function() {
                $(".minx", $el).val(Geo.parseDMS($dix.val()));
            });
            $diy.change(function() {
                $(".miny", $el).val(Geo.parseDMS($diy.val()));
            });

            // create a toolbar with just the rectangle and edit tool
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
            _dc = drawControl;
            _updateLayerFromFields($el, map, drawnItems, $mapDiv);
            map.addLayer(drawnItems);

            /**
             * Assumption of only one bounding box
             */
            map.on('draw:created', function(e) {
                var type = e.layerType,
                    layer = e.layer;
                drawnItems.addLayer(layer);
                var b = layer.getBounds();
                var bnds = _setValuesFromBounds($el, b);
                layer.setBounds(bnds);
                map.addLayer(layer);
                _disableRectangleCreate($mapDiv);
            });

            /**
             * Assumption of only one bounding box
             */
            map.on('draw:edited', function(e) {
                var layers = e.layers;
                layers.eachLayer(function(layer) {
                    var b = layer.getBounds();
                    var bnds = _setValuesFromBounds($el, b);
                    layer.setBounds(bnds);
                });
            });

            /**
             * Assumption of only one bounding box
             */
            map.on('draw:deleted', function(e) {
                var layers = e.layers;
                layers.eachLayer(function(layer) {
                    drawnItems.removeLayer(layer);
                    // the change() watch deosn't always pay attention to these explicit calls
                    $(".minx", $el).val('');
                    $(".miny", $el).val('');
                    $(".maxx", $el).val('');
                    $(".maxy", $el).val('');
                    $(".d_minx", $el).val('');
                    $(".d_miny", $el).val('');
                    $(".d_maxx", $el).val('');
                    $(".d_maxy", $el).val('');
                });
                _enableRectangleCreate($mapDiv);
            });


            $(".locateCoordsButton", $el).click(function() {
                var rec = _updateLayerFromFields($el, map, drawnItems, $mapDiv);
            });

        });
    }

    function _enableRectangleCreate($el) {
        var $drawRec = $(".leaflet-draw-draw-rectangle", $el);
        $drawRec.fadeTo(1, 1);
        $(".disable-draw-rect", $el).remove();
    }


    function _disableRectangleCreate($el) {
        var $drawRec = $(".leaflet-draw-draw-rectangle", $el);
        $drawRec.fadeTo(1, .5);
        var disableCreateDiv = document.createElement("div");
        disableCreateDiv.setAttribute("style", "z-index:1000000");
        disableCreateDiv.setAttribute("class", "disable-draw-rect");
        var $dcd = $(disableCreateDiv);
        var $dr = $(".leaflet-draw-draw-rectangle", $el);

        $dcd.width($dr.width());
        $dcd.height($dr.height());
        $el.append($dcd);
        $dcd.offset($dr.offset());
    }

    /**
     * set the input values based on the bounding box
     */
    function _setValuesFromBounds($el, b) {
        var bnds = b;
        // correct for bounding box being greater than 1 full world rotation
        if (Math.abs(b.getWest() - b.getEast()) > 360) {
            bnds = L.latLngBounds(L.latLng(b.getSouth(), -179.999999), L.latLng(b.getNorth(), 180.0000));
            console.log("> 360:" + b.toBBoxString() + "  -->> ", bnds.toBBoxString());
        }

        // the change() watch deosn't always pay attention to these explicit calls
        $(".minx", $el).val(bnds.getWest());
        $(".miny", $el).val(bnds.getSouth());
        $(".maxx", $el).val(bnds.getEast());
        $(".maxy", $el).val(bnds.getNorth());
        $(".d_minx", $el).val(bnds.getWest());
        $(".d_miny", $el).val(bnds.getSouth());
        $(".d_maxx", $el).val(bnds.getEast());
        $(".d_maxy", $el).val(bnds.getNorth());
        return bnds;
    }


    function _isIntialized() {
        return _initialized;
    }

    function _getDc() {
        return _dc;
    }

    return {
        initLeafletMaps: _initLeafletMaps,
        initEditableLeafletMaps: _initEditableMaps,
        initResultsMaps: _initResultsMaps,
        initialized: _isIntialized,
        defaults: _defaults,
        dc: _getDc
    }
})(console, jQuery, window, L);
$(function() {
    TDAR.leaflet.initLeafletMaps();
    TDAR.leaflet.initEditableLeafletMaps();
    TDAR.leaflet.initResultsMaps();
});