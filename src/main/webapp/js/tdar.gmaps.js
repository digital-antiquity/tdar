/**
 * Map rendering / edit support.
 *
 * Requires:  jquery,  latLongUtil-1.0.js
 */
TDAR.namespace("maps");
//FIXME: TDAR.maps.googleApiKey is defined in layout-bootstrap.dec. Move googleApiKey definition here, and expose getter that layout-bootstrap can use instead. (then remove namespace() call above)
TDAR.maps = function ($, TDAR) {
    "use strict";

    //default map canvas properties
    var _defaults = {
        isGeoLocationToBeUsed: false,
        center: {
            lat: 0,
            lng: 0
        },
        zoomLevel: 4,
        rectStyleOptions: {
            PARENT: {
                strokeColor: "#FF8000",
                strokeOpacity: 0.8,
                strokeWeight: 2,
                fillColor: "#FF8800",
                fillOpacity: 0.35
            },
            RESOURCE: {
                strokeColor: "#FF0000",
                strokeOpacity: 0.8,
                strokeWeight: 2,
                fillColor: "#FF0000",
                fillOpacity: 0.15
            }
        }
    };

    //$.Deferred object for the init process.  initGmapApi() returns a promise, which _apiLoaded() resolves when the
    // google maps api loading is complete (and calls our _apiLoaded() callback)
    var _deferredApi = null;

    //deferred representing map preparation. resolved when map is loaded and becomes 'idle' 
    var _deferredMap = $.Deferred();

    /**
     * Notify TDAR.maps API that the Google Maps API is available.  Technically this is a "public" method, but it
     * should really only be called by the dynamically-generated <script> element that we receive from Google.
     */
    var apiLoaded = function () {
        _deferredApi.resolve();
    };

    /**
     * Asynchronously initialize the Google Maps v3 API.  This method returns a jQuery Promise object.  The method
     * resolves the promise when
     *
     *
     * @returns {*} jQuery Promise object.
     */
    var initGmapApi = function () {
        //FIXME: reject promise if this process fails (harder problem: how do we detect failure?)
        // At the very least we need a timeout of some kind, where we indicate failure if gmap api doesn't load in less than N seconds.
        if (_deferredApi) {
            return _deferredApi.promise();
        }
        _deferredApi = $.Deferred();

        var gmapUrl = "//maps.googleapis.com/maps/api/js?libraries=drawing&sensor=false&callback=TDAR.maps.apiLoaded";
        if (TDAR.maps.googleApiKey) {
            gmapUrl += "&key=" + TDAR.maps.googleApiKey;
        }

        //google adds even more scripts - so we need to rely on them calling our _apiLoaded callback to know when api is ready
        var prm = TDAR.loadScript(gmapUrl);
        return _deferredApi.promise();
    };

    /**
     * Map initialization functionality common to setupMap() and setupEditMap()
     * @param mapDiv DIV element that will contain map control
     * @param inputContainer element that contains  the "manual latlong entry" fields
     * @returns {goog.structs.Map} a google maps v3 Map object.
     * @private
     */
    var _setupMapInner = function (mapDiv, inputContainer) {
        console.log("running  setupmap");
        var mapOptions = $.extend({}, _defaults.mapOptions, {
            zoom: _defaults.zoomLevel,
            center: new google.maps.LatLng(_defaults.center.lat, _defaults.center.lng),
            mapTypeControlOptions: {
                mapTypeIds: [
                    google.maps.MapTypeId.TERRAIN, google.maps.MapTypeId.SATELLITE, google.maps.MapTypeId.ROADMAP, google.maps.MapTypeId.HYBRID
                ]
            },
            mapTypeId: google.maps.MapTypeId.TERRAIN,
            streetViewControl: false,
            //scrollwheel zooming frustrates efforts to scroll vertically in the form.
            scrollwheel: false
        });

        var $mapDiv = $(mapDiv);
        // if we have not specified a height, setting the height to the height of the parent DIV
        if ($mapDiv.height() < 5) {
            $mapDiv.height($mapDiv.parent().height() - 5);
        }

        var map = new google.maps.Map(mapDiv, mapOptions);

        if (_defaults.isGeoLocationToBeUsed && navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function (position) {
                var initialLocation = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
                map.setCenter(initialLocation);
            });
        }

        $mapDiv.data("gmap", map);

        if (inputContainer) {
            _setupLatLongBoxes(mapDiv, inputContainer);
        }

        //indicate the map is ready and dom elements loaded (we wrap this because the google.maps api may not be available to the listener at time of call)
        google.maps.event.addListenerOnce(map, 'idle', function () {
            console.log("map ready");
            $(mapDiv).trigger("mapready", [map, $mapDiv.data("resourceRect")]);
            _deferredMap.resolveWith($mapDiv[0], [map, $mapDiv.data("resourceRect")]);
        });
        return map;
    };

    /**
     * Initialize a gmap inside of the specified div element.  If hidden inputs define spatial bounds,
     * draw a box and pan/zoom the map to fit the bounds. This method is non-blocking and may be called at any time,
     * but the method will not begin until the Google Map API initializtion is complete.
     *
     * @param mapDiv DIV element that will contain the Google map control
     * @param inputContainer (optional) element that contains manual latlong input fields.  If defined,  the map API will
     *        Listen to changes to the latlong inputs, so that the API can redraw any effected bounding boxes.
     */
    var setupMap = function (mapDiv, inputContainer) {
        initGmapApi().done(function () {
            _setupMapInner(mapDiv, inputContainer);
        });
    };

    /**
     * Setup bindings that Create/Modify a bounding box  if the fields in the specified inputContainer contain
     * valid bounding coordinates.
     *
     * @param mapDiv DIV element that will contain the map control
     * @param inputContainer element that contains the "manual latlong entry" fields.
     * @private
     */
    var _setupLatLongBoxes = function (mapDiv, inputContainer) {
        'use strict';
        var style = _defaults.rectStyleOptions.RESOURCE;
        var gmap = $(mapDiv).data("gmap");

        if (parseInt($('.sw-lat').val())) {
            var lat1 = $('.sw-lat', inputContainer).val();
            var lng1 = $('.sw-lng', inputContainer).val();
            var lat2 = $('.ne-lat', inputContainer).val();
            var lng2 = $('.ne-lng', inputContainer).val();

            var rect = _addBound(mapDiv, style, lat1, lng1, lat2, lng2);
            if (rect) {
                gmap.fitBounds(rect.getBounds());
            }
            //pan/zoom the 
        }
        ;

        $(mapDiv).data("resourceRect", rect);

        //TODO: draw a rect for parent project (but don't pan/zoom to it)

        //TODO: add "snap back" control, for when the user pans/zooms away from resource bounds
    };

    /**
     * Add a bounding box overlay to a map.
     *
     * @param mapDiv  map container element
     * @param rectStyleOptions options used when calling google.maps.Rectangle()
     * @param lat1 southwest latitude
     * @param lng1 southwest longitude
     * @param lat2 northeast lattitude
     * @param lng2 northeast longitude
     * @returns {google.maps.Rectangle} new gmapv3 Rectangle instance
     * @private
     */
    var _addBound = function (mapDiv, rectStyleOptions, lat1, lng1, lat2, lng2) {
        var p1 = new google.maps.LatLng(lat1, lng1);
        var p2 = new google.maps.LatLng(lat2, lng2);
        var bounds = new google.maps.LatLngBounds(p1, p2);
        var map = $(mapDiv).data("gmap");

        var rectOptions = $.extend({
            bounds: bounds,
            map: map
        }, rectStyleOptions);

        var rect = new google.maps.Rectangle(rectOptions);
        return rect;

    };

    /**
     * Modify size/position of a google.maps.Rectangle
     * @param rect google.maps.Rectangle to modify
     * @param lat1 southwest latitude
     * @param lng1 southwest longitude
     * @param lat2 northeast lattitude
     * @param lng2 northeast longitude
     * @private
     */
    var _updateBound = function (rect, lat1, lng1, lat2, lng2) {
        var p1 = new google.maps.LatLng(lat1, lng1);
        var p2 = new google.maps.LatLng(lat2, lng2);
        var bounds = new google.maps.LatLngBounds(p1, p2);
        rect.setBounds(bounds);
    };

    /**
     * Initialize an "editable" Google Map v3 control inside of the specified div element.  An editable map control
     * includes simple drawing controls which allow the user to create/modify bounding boxes. This method is non-blocking
     * and has no restrictions on when it may be called, however this method will not execute until the Google Map
     * API is ready.
     *
     * @param mapDiv DIV element that will contain the Google map control
     * @param inputContainer (optional) element that contains "manual entry" latlong input fields.  If defined,
     *        this method performs binding in two ways:
     *              1) Listen to changes to the latlong inputs, so that the API can redraw any effected bounding
     *                 boxes.
     *              2) Conversely, modify the value of the latlong inputs if the user modifies a bounding box using
     *                 GUI controls.
     */
    var setupEditMap = function (mapDiv, inputContainer) {
        initGmapApi().done(function () {
            _setupMapInner(mapDiv, inputContainer);
            var gmap = $(mapDiv).data("gmap");

            //add "select region" button
            var $controlDiv = $('<div class="tdar-gmap-control"></div>');
            //var $controlUi = $('<div class="tdar-gmap-control-ui"></div>');
            var $selectButton = $('<button type="button" id="btnSelectRegion" class="btn btn-small btn-primary">Select Region</button>');
            var $clearButton = $('<button type="button" id="btnClearRect" class="btn btn-small ">Clear Region</button>');
            $controlDiv.append($selectButton).append($clearButton);

            var drawingManager = _setupDrawingManager(mapDiv);

            //handle select click
            google.maps.event.addDomListener($selectButton[0], 'click', function () {
                var existingRect = $(mapDiv).data("resourceRect");
                console.log(" select region");
                //remove any existing rectangle
                if (existingRect) {
                    existingRect.setMap();
                    $(mapDiv).removeData("resourceRect");
                }
                drawingManager.setDrawingMode(google.maps.drawing.OverlayType.RECTANGLE);
                $(mapDiv).find('.tdar-gmap-control button').prop("disabled", true);
            });

            //handle clear click
            google.maps.event.addDomListener($clearButton[0], 'click', function () {
                var existingRect = $(mapDiv).data("resourceRect");
                if (!existingRect) {
                    return;
                }
                existingRect.setMap();
                $(mapDiv).removeData("resourceRect");

                //tell the DOM that rect is gone
                _fireBoundsModified(mapDiv, null);
            });

            //add control to map
            gmap.controls[google.maps.ControlPosition.TOP_CENTER].push($controlDiv[0]);

            //if rect already present,  make it editable
            var rect = $(mapDiv).data('resourceRect');
            if (rect) {
                rect.setEditable(true);
                google.maps.event.addDomListener(rect, 'bounds_changed', function () {
                    _fireBoundsModified(mapDiv, rect);
                });

            }

            //bind resource rectangle to the manual latlong input controls
            _registerInputs(mapDiv, inputContainer);

        });
    };

    /**
     * Fire a "resourceboundschanged" event. Gmap events are not 'seen' by the DOM. We trigger this event so that other
     * parts of the code hook into map changes without having to directly interact with the gmaps api.
     *
     * @param mapDiv DIV that contains the map.  This will be the event target.
     * @param rect bounding box rectangle, if present.  This is passed to the event handler via the  "extraParameters" argument.
     * @private
     */
    var _fireBoundsModified = function (mapDiv, rect) {
        console.log("resource rect created/changed:: map:%s,  rect:%s", mapDiv.id, rect);
        var bounds = null;
        if (rect) {
            bounds = rect.getBounds();
        }
        $(mapDiv).trigger("resourceboundschanged", bounds);
    };

    /**
     * Initialize the DrawingManager control (for editing bounding boxes)
     *
     * @param mapDiv
     * @returns {google.maps.drawing.DrawingManager}
     * @private
     */
    var _setupDrawingManager = function (mapDiv) {
        var gmap = $(mapDiv).data("gmap");

        //add drawing manager to map,
        var drawingManager = new google.maps.drawing.DrawingManager({
            drawingMode: null, //drawing mode off
            drawingControl: false, //drawing toolbar hidden
            drawingControlOptions: {
                position: google.maps.ControlPosition.TOP_CENTER,
                drawingModes: [
                    google.maps.drawing.OverlayType.RECTANGLE
                ]
            },
            rectangleOptions: $.extend({}, _defaults.rectStyleOptions.RESOURCE)
        });
        drawingManager.setMap(gmap);

        //as soon as rectangle is complete, turn off drawing mode, make rect editable
        google.maps.event.addDomListener(drawingManager, 'rectanglecomplete', function (rect) {
            drawingManager.setDrawingMode();
            rect.setEditable(true);
            $(mapDiv).data("resourceRect", rect);
            $(mapDiv).find('.tdar-gmap-control button').prop("disabled", false);

            //fire bounds-modified event for this new rect right now, and again whenever the bounds change
            _fireBoundsModified(mapDiv, rect);
            google.maps.event.addDomListener(rect, 'bounds_changed', function () {
                _fireBoundsModified(mapDiv, rect);
            });
        });

        $(mapDiv).data("drawingManager", drawingManager);
        return drawingManager;
    };

    /**
     * Populate the latlon "manual entry" display fields based on the provided latLngBounds object.
     * @param latLngBounds
     * @param $swLatDisplay
     * @param $swLngDisplay
     * @param $neLatDisplay
     * @param $neLngDisplay
     * @private
     */
    var _populateLatLngDisplay = function (latLngBounds, $swLatDisplay, $swLngDisplay, $neLatDisplay, $neLngDisplay) {
        var sw = latLngBounds.getSouthWest();
        var ne = latLngBounds.getNorthEast();

        $swLatDisplay.val(Geo.toLat(sw.lat()));
        $swLngDisplay.val(Geo.toLon(sw.lng()));
        $neLatDisplay.val(Geo.toLat(ne.lat()));
        $neLngDisplay.val(Geo.toLon(ne.lng()));
    };

    //public: update input boxes  when bounds change, and vice versa
    //FIXME: requires latLongUtil.js - this dependency should be declared via require() or as an argument to the IIFE that creates this module
    var _registerInputs = function (mapDiv, inputContainer) {

        //FIXME:  This is super-brittle.  Try to refactor so that it doesn't rely on hard-coded selectors for the manual-entry form fields.
        var $swLatInput = $('.sw-lat', inputContainer);
        var $swLngInput = $('.sw-lng', inputContainer);
        var $neLatInput = $('.ne-lat', inputContainer);
        var $neLngInput = $('.ne-lng', inputContainer);
        var $swLatDisplay = $('.sw-lat-display', inputContainer);
        var $swLngDisplay = $('.sw-lng-display', inputContainer);
        var $neLatDisplay = $('.ne-lat-display', inputContainer);
        var $neLngDisplay = $('.ne-lng-display', inputContainer);
        var $btnLocate = $('.locateCoordsButton', inputContainer);
        //update form inputs and 'display' inputs when the bounds have changed.
        $(mapDiv).bind("resourceboundschanged", function (e, latLngBounds) {
            //if no bounds, user clicked 'clear' button -- clear all input textboxes
            if (!latLngBounds) {
                $('input[type=text]', inputContainer).val("");
                return;
            }
            ;

            var sw = latLngBounds.getSouthWest();
            var ne = latLngBounds.getNorthEast();

            //update the actual inputs 
            $swLatInput.val(sw.lat());
            $swLngInput.val(sw.lng());
            $neLatInput.val(ne.lat());
            $neLngInput.val(ne.lng());

            //update 'display' inputs
            _populateLatLngDisplay(latLngBounds, $swLatDisplay, $swLngDisplay, $neLatDisplay, $neLngDisplay);
        });

        //if editing existing rect, populate the values
        if ($(mapDiv).data("resourceRect")) {

            _populateLatLngDisplay($(mapDiv).data("resourceRect").getBounds(), $swLatDisplay, $swLngDisplay, $neLatDisplay, $neLngDisplay);
        }

        /**
         * update the bounding box overlay  based on current value of manual-entry input fields
         */
        var updateRectFromInputs = function () {

            //trim the input, and if all non-blank then update the region
            var parseErrors = 0;
            $('.sw-lat-display, .sw-lng-display, .ne-lat-display, .ne-lng-display', inputContainer).each(function () {
                this.value = $.trim(this.value);
                if (("" + this.value) === "") {
                    parseErrors++;
                } else if (isNaN(Geo.parseDMS(this.value))) {
                    parseErrors++;
                }
            });

            if (!parseErrors) {
                //parse the values and update the form values
                $swLatInput.val(Geo.parseDMS($swLatDisplay.val()));
                $swLngInput.val(Geo.parseDMS($swLngDisplay.val()));
                $neLatInput.val(Geo.parseDMS($neLatDisplay.val()));
                $neLngInput.val(Geo.parseDMS($neLngDisplay.val()));

                var rect = $(mapDiv).data("resourceRect");
                var gmap = $(mapDiv).data('gmap');

                //FIXME: REPLACE block w/ call to updateResourceRect
                if (!rect) {
                    rect = _addBound(mapDiv, _defaults.rectStyleOptions.RESOURCE, $swLatInput.val(), $swLngInput.val(), $neLatInput.val(), $neLngInput.val());
                } else {
                    _updateBound(rect, $swLatInput.val(), $swLngInput.val(), $neLatInput.val(), $neLngInput.val());
                }

                if (rect) {
                    $(mapDiv).data("resourceRect", rect);
                    gmap.fitBounds(rect.getBounds());
                }
            }
            ;
        };

        //locate button clicked or manual-entry coords have changed.  Update the rectangle
        $btnLocate.click(updateRectFromInputs);
        $swLatDisplay.change(updateRectFromInputs);
        $swLngDisplay.change(updateRectFromInputs);
        $neLatDisplay.change(updateRectFromInputs);
        $neLngDisplay.change(updateRectFromInputs);
    };

    /**
     * Convenience function for creating a google.maps.LatLngBounds object.
     *
     * @param swlat
     * @param swlng
     * @param nelat
     * @param nelng
     * @returns {google.maps.LatLngBounds}
     * @private
     */
    var _bounds = function (swlat, swlng, nelat, nelng) {
        var sw = new google.maps.LatLng(swlat, swlng);
        var ne = new google.maps.LatLng(nelat, nelng);
        var bounds = new google.maps.LatLngBounds(sw, ne);
        return bounds;
    };

    /**
     * Bind the google "bounds_changed" event to a jquery custom event.
     *
     * @param mapDiv
     * @param rect
     * @private
     */
    var _bindRectEvents = function (mapDiv, rect) {
        google.maps.event.addDomListener(rect, 'bounds_changed', function () {
            _fireBoundsModified(mapDiv, rect);
        });
    };

    /**
     * Create a bounding box (or modify a bounding box, if it already exists) in the map conatained by the specified
     * DIV, given the specified latlong coordinates
     * @param mapDiv DIV that contains the google map control
     * @param swlat lattitude, southwest corner
     * @param swlng longitude, southwest corner
     * @param nelat lattitude, northeast corner
     * @param nelng longitude, northeast corner
     */
    var updateResourceRect = function (mapDiv, swlat, swlng, nelat, nelng) {
        var gmap = $(mapDiv).data("gmap");
        var rect = $(mapDiv).data("resourceRect");
        if (!rect) {
            rect = _addBound(mapDiv, _defaults.rectStyleOptions.RESOURCE, swlat, swlng, nelat, nelng);
            _bindRectEvents(mapDiv, rect);
            $(mapDiv).data("resourceRect", rect);
        } else {
            var bounds = _bounds(swlat, swlng, nelat, nelng);
            rect.setMap(gmap);
            rect.setBounds(bounds);
        }
        gmap.fitBounds(rect.getBounds());
    };

    /**
     * Remove a bounding box, if present.
     *
     * @param mapDiv DIV element that contains the Google map control
     * @returns {boolean} true if a bounding box was present, otherwise false.
     */
    var clearResourceRect = function (mapDiv) {
        var rect = $(mapDiv).data("resourceRect");
        if (rect) {
            rect.setMap();
        }
        return !!rect;
    };

    /**
     * Render map "pins" that correspond to the bounding boxes of resources listed on a search results page. This method
     * has no arguments. Instead, the page embeds pin locations via data-attributes in ordered-list elements.
     *
     * To ensure proper rendering, the page should:
     *
     * - specify the "MAP" css class in the top-level OL node containing the search result list
     * - for each list-item node, specify the bounding box centerpoint latitude using a "data-lat" attribute
     * - for each list-item node, specify the bounding box centerpoing longitude using a "data-long" attribute
     *
     * Note: Callers of setupMapResult() must also call either setupEditMap() or setupMap(), as setupMapResult() will
     * not execute until the page contains a fully-initialized map control.
     *
     */
    var setupMapResult = function () {
        //$(".google-map", '#articleBody').one("mapready", function(e, myMap) {
        _deferredMap.done(function (myMap, ignoredRect) {
            console.log("setup map results");
            var bounds = new google.maps.LatLngBounds();
            var markers = new Array();
            var infowindows = new Array();
            var i = 0;
            $("ol.MAP li").each(function () {
                i++;
                var $this = $(this);
                if ($this.attr("data-lat") && $this.attr('data-long')) {
                    console.log($this.attr("data-lat") + " " + $this.attr('data-long'));
                    var infowindow = new google.maps.InfoWindow({
                        content: $this.html()
                    });
                    var marker = new google.maps.Marker({
                        position: new google.maps.LatLng($this.attr("data-lat"), $this.attr("data-long")),
                        map: myMap,
                        icon: 'http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=' + i + '|7a1501|FFFFFF',
                        title: $("a.resourceLink", $this).text()
                    });

                    $(this).click(function () {
                        myMap.panTo(marker.getPosition());
                        $(infowindows).each(function () {
                            this.close(myMap);
                        });
                        infowindow.open(myMap, marker);
                        return false;
                    });

                    google.maps.event.addListener(marker, 'click', function () {
                        $(infowindows).each(function () {
                            this.close(myMap);
                        });
                        infowindow.open(myMap, marker);
                    });

                    markers[markers.length] = marker;
                    infowindows[infowindows.length] = infowindow;
                    bounds.extend(marker.position);
                }
                ;
            });
            myMap.fitBounds(bounds);
        });
    };

    return {
        apiLoaded: apiLoaded,
        initMapApi: initGmapApi,
        setupMap: setupMap,
        googleApiKey: false,
        defaults: _defaults,
        updateResourceRect: updateResourceRect,
        clearResourceRect: clearResourceRect,
        setupEditMap: setupEditMap,
        setupMapResult: setupMapResult,
        addBound: _addBound,

        /**
         * A jQuery promise object bound to the progress of setupMap() and setupEditMap().  Use this promise to
         * perform any operations that must only occur after a map has successfully rendered on the page.
         */
        mapPromise: _deferredMap.promise()
    };
}(jQuery, TDAR);
