TDAR.leaflet = (function(console, $, ctx, L) {
    "use strict";

    L.drawLocal.draw.toolbar.buttons.rectangle = 'Create bounding box';
    L.drawLocal.edit.toolbar.buttons.edit = 'Edit';
    L.drawLocal.edit.toolbar.buttons.editDisabled = 'No box to edit';
    L.drawLocal.edit.toolbar.buttons.remove = 'Delete';
    L.drawLocal.edit.toolbar.buttons.removeDisabled = 'No boxes to delete';
    L.Icon.Default.imagePath = TDAR.assetsUri('/components/leaflet/dist/images/');

    var $body = $('body');

    var _tileProviders = {
        osm: {
            url: "http://{s}.tile.osm.org/{z}/{x}/{y}.png",
            attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        },

        mapbox: {
            url: "https://{s}.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={apiKey}",
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
        leafletTileProvider: 'mapbox',
        minZoom: 2,
        maxZoom: 17,
        id: 'abrin.n9j4f56m',
        // config for leaflet.sleep
        sleep: true,
        sleepOpacity: 1,
        // time(ms) for the map to fall asleep upon mouseout
        sleepTime: 750,
        // time(ms) until map wakes on mouseover
        wakeTime: 750,
        // defines whether or not the user is prompted oh how to wake map
        sleepNote: false,
        // should hovering wake the map? (clicking always will)
        hoverToWake: true

    };

    var _rectangleDefaults = {
        fitToBounds: true
    };


    /**
     * Wire up an Evented object to every freaking event we can think of.  Log the name of the event when it fires.
     * @param object eventedd object
     * @param object tag  tag to help  differentiate event messages
     */
    function _logEventedClass(object, tag) {
        var eventNames = [
           'baselayerchange','overlayadd','overlayremove','layeradd','layerremove','zoomlevelschange','resize','unload','viewreset','load','zoomstart',
            'movestart','zoom','move','zoomend','moveend','popupopen','popupclose','autopanstart','locationerror'
        ];

        if(!!object['on']) {
            eventNames.forEach(function(s) {
                object.on(s, function(evt) {
                    console.log("event: %s\t tag:", evt.type, tag);
                });
            });
        } else {
            console.warn("logEventedClass: can't add events because object has no 'on' method", tag)
            return;

        }


    }


    // -1 -- not initialized ; -2 -- bad rectangle ; 1 -- initialized ; 2 -- rectangle setup
    //fixme: global _inialized unreliable if more than one map on page
    //fixme: seems like you really want to track two things: 1) initialize status and 2) rectangle validity.
    var _initialized = -1;

    /**
     * Initialize a map,  bind it to the specified element,  and return a promise of the map object.
     * @param elem the init function binds the map to the specified element via  data-attribute named 'map'
     * @returns {*} promise of the initialized, loaded map object.
     * @private
     */
    function _initMap(elem) {
        // create effective settings from defaults, then body data-attrs, then elem data-attrs.
        var $elem = $(elem);
        var _elemData = $elem.data();
        // bootstrap stores a lot of data in BODY. We only want a subset
        var _bdata = $('body').data();
        var _bodyData = {leafletApiKey: _bdata.leafletApiKey, leafletTileProvider: _bdata.leafletTileProvider};
        if (_bdata.centerlat && _bdata.centerlong) {
            _bodyData.center =  {lat: _bdata.centerlat, lng: _bdata.centerlong}
        };
        var settings = $.extend({}, _defaults, _bodyData, _elemData);
        var deferred = $.Deferred();


        var map = new L.map(elem, settings);
        // setView implicitly triggers 'load' event, so register the listener beforehand

        //console.log('setting map obj on', $elem)
		$elem.data("map",map);

        var tp = _tileProviders[settings.leafletTileProvider];
        var tile = L.tileLayer(tp.url, {
            maxZoom: settings.maxZoom,
            minZoom: settings.minZoom,
            attribution: tp.attribution,
            id: settings.id,
            apiKey:settings.leafletApiKey
        });


        // _logEventedClass(map, 'map');
        // _logEventedClass(tile, 'tile')
        tile.once('load', function _mapLoaded(){
            console.log("map loaded");
            deferred.resolve(map);
        });
        tile.addTo(map);
        map.setView([settings.center.lat, settings.center.lng], settings.zoomLevel);
        map.setMaxBounds(settings.maxBounds);
        //console.log('adding tile to map');


        //FIXME: WARN if DIV DOM HEIGHT IS EMPTY
        _initialized = 0;

        if (settings.geojson != undefined) {
            var geoJson = $(settings.geojson);
//            console.log(settings.geojson);
            if (geoJson.length > 0) {
                var gj = JSON.parse(geoJson.html());
                console.log("parsed");
                var glayer = L.geoJson(gj);
                console.log("loaded");
                glayer.addTo(map);
                console.log("added");

                _fitTo(map, glayer);
            }
        }
        if (settings.search) {
            L.Control.geocoder({}).addTo(map);
        }

        return deferred.promise();
    }

    /**
     * Initialize any 'results style' maps and return a list of promises equal in size to the number of maps.  Each promise resolves when leaflet
     * completes loading.
     *
     * @returns {Array|*}
     * @private
     */
    function _initResultsMaps() {
        return $(".leaflet-map-results").map(function() {
            var $el = $(this);
            return _initMap(this).done(function(map){
                console.log('_initResultsMaps');
                var markers = new L.MarkerClusterGroup({maxClusterRadius:150, removeOutsideVisibleBounds:true, chunkedLoading: true});
                $el.data("markers", markers);
                map.markers = markers;

                var recDefaults = $.extend(_rectangleDefaults, {
                    fillOpacity: 0.08,
                    fitToBounds: true
                });
                _initFromDataAttr($el, map, recDefaults);
                var hasBounds = false;
                var allPoints = new Array();
                var infiniteUrl = $el.data("infinite-url");
                if (infiniteUrl) {
                    _dynamicUpdateMap($el, infiniteUrl,0);
                    var zoom = L.control({
                        position : 'topright'
                    });

                    zoom.onAdd = function(map) {
                        var topRight = L.DomUtil.create('div', 'topright');
                        var loading = L.DomUtil.create('div', 'mapLoading');
                        loading.id="mapLoading";
                        var $loading = $(loading);
                        $loading.append("<i class='icon-refresh'></i> Loading");
//                    $loading.hide();
                        var resetBounds = L.DomUtil.create('div', 'mapResetBounds');
                        resetBounds.id="mapResetBounds";
                        var $resetBounds = $(resetBounds);
                        $resetBounds.append("<i class='icon-map-marker'></i> Fit map to all results");
                        $resetBounds.hide();

                        $resetBounds.click(function() {
                            map.fitBounds(map.markers.getBounds());
                            $resetBounds.hide();
                        });
                        topRight.appendChild(resetBounds);
                        topRight.appendChild(loading);
                        return topRight;
                    };
                    zoom.addTo(map);

                } else {
                    $(".resource-list.MAP .listItem").each(function() {
                        var $t = $(this);
                        var title = $(".resourceLink", $t);
                        var lat = $t.data("lat");
                        var lng = $t.data("long");
                        if (!isNaN(lat) && !isNaN(lng)) {
                            hasBounds = true;
                            var marker = L.marker(new L.LatLng(lat, lng), {title: title.text().trim()});
                            marker.bindPopup(title.html() + "<br><a href='" + title.attr('href') + "'>view</a>");
                            allPoints.push(marker);

                        }
                    });
                    markers.clearLayers();
                    markers.addLayers(allPoints);
                    if (hasBounds) {
                        _fitTo(map, markers);
                    }
                }
                map.addLayer(markers);
            });
           // return deferred.promise();
        });
    }

    function _dynamicUpdateMap($el, baseUrl, startRecord_) {
        var startRecord = startRecord_;
        if (!startRecord) {
            startRecord = 0;
        }
        console.log(baseUrl + " --> " + startRecord);
        $.ajax({
        dataType: "json",
        url: baseUrl + "&startRecord="+startRecord,
        success: function(data) {
            console.log(data);
            _update($el.data("map"), $el.data("markers"), data,startRecord, $el.is('[data-fit-bounds]', startRecord === 0));
            var nextPage = data.properties.startRecord + data.properties.recordsPerPage;
            var $loading = $("#mapLoading");
            if (data.properties && (nextPage) < data.properties.totalRecords) {
                _dynamicUpdateMap($el, baseUrl, nextPage);
                $loading.show();
                $("#mapResetBounds").show();
            } else {
                $loading.hide();
            }
        }
        }).error(function(e) {
            console.log("error loading json: ", e);
        });

    }


    /**
     * Update the map control associated w/ a specified dom element by appending markers contained in a specified data object
     * @param map the leaflet map
     * @param markers clustergroup (jtd: I think) of existing map markers
     * @param data object containing list of leaflet feature objects (in data.features).  These features represent one "page" of data
     * @param startRecord record number of the first record in the current page
     * @param bFitBounds if true, this method tells the map to pan/zoom to fit updated set of markers
     * @private
     */
    function _update(map, markers, data,startRecord, bFitBounds) {

        //translate datapoints to list of markers
        var layers = data.features
            .filter(function(feature){return feature.geometry.hasOwnProperty("type");})
            .map(function(feature){
                var title = feature.properties.title;
                var c = feature.geometry.coordinates;
                var marker = L.marker(new L.LatLng(c[1], c[0]), {title: $.trim(title)});
                marker.bindPopup(title + "<br><a href='" + feature.properties.detailUrl + "'>view</a>");
                return marker;
            });

        // clear any existing layers if starting w/ first page of data
        if (startRecord === 0) {
            markers.clearLayers();
        }

        // append the markers to the existing leaflet.cluster layer
        markers.addLayers(layers);

        // if we fit to bounds...
        // fixme: if user-interaction happens we probably shouldn't call fit-bounds
        if(bFitBounds && layers.length) {
            map.fitBounds(markers.getBounds());
        }
    }

    function _fitTo(map, layer) {
        map.fitBounds(layer.getBounds());
        map.zoomOut(1);
    }

    /**
     * init a "view" only map, binds to data-attribute minx, miny, maxx, maxy
     *
     * Return list of promises (one for each map on page).  Promise resolves when leaflet finishes loading the map.
     */
    function _initLeafletMaps() {
        return $(".leaflet-map").map(
            function() {
                var $el = $(this);
                return _initMap(this).done(function(map){
                    console.log('_initLeafletMaps')
                    _initFromDataAttr($el, map, _rectangleDefaults);
                });
            });
    }


    function _initFromDataAttr($el, map, rectangleSettings) {
        console.log('$el is ', $el);
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
            // LEAFLET HANDLES WRAPPING AROUND THE DATELINE SPECIALLY, , SO WE NEED TO TRANSLATE FOR IT
            // http://www.macwright.org/2015/03/23/geojson-second-bite.html IS A GOOD EXPLANATION, BUT BASICALLY ADD 360
            if (parseFloat(maxx) < parseFloat(minx)) {
                maxx = parseFloat(maxx) +  360.0;
            }
            var poly = [[maxy, minx], [miny, maxx]];
            console.log(poly);
            var rectangle = L.rectangle(poly, rectangleSettings).addTo(map);
            console.log("fitToBounds:", rectangleSettings.fitToBounds);
            console.log("fitToBounds (rec):", rectangle.getBounds());
            console.log("fitToBounds (map):", map.getBounds());

            if (rectangleSettings.fitToBounds) {
                // Really frustrating race condition whereby map sometimes zoom's wrong, so we need to add a timeout to make this work
                map.fitBounds(rectangle.getBounds());
            }
            _initialized = 2;
            return rectangle;
        } else if (minx == undefined && miny == undefined && maxx == undefined && maxy == undefined) {
            // skipping, we're just not configured at all;
            return;
        }
        _initialized = -2;
        //console.log("check map init bounds [" + minx + "," + miny + "] [" + maxx + "," + maxy + "]");
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

        //if rectangle is invalid, remove all rectangles and re-enable rectangle create
        if (typeof rectangle === "undefined") {
            _enableRectangleCreate();
            for (var i = 0; i > drawnItems.getLayers().length; i++) {
                drawnItems.removeLayer(drawnItems.getLayer(i));
            }
        } else {
            _disableRectangleCreate($mapDiv);
            drawnItems.addLayer(rectangle);
        }
        return rectangle;
    }

    /**
     * Init an editable map, it uses a set of classes to determine values .minx, .maxx, .miny, .maxy for actual values; .d_* for display values, and
     * .locateCoordsButton for the locate button
     *
     * this will create the map just before the element specified
     *
     * Returns list of promises with size equal to number of maps on page.  Each promise resolves after leaflet loads and initializes the map.
     */
    function _initEditableMaps() {
        return $(".leaflet-map-editable").map(function() {
            _initEditableMap(this, undefined);
        });
    }

    function _initEditableMap($target, onChange) {
        
        var $el = $target;
        $el._onChange = onChange;
        $el.data("_onChange", onChange);
        console.log($el, onChange);
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

        //merge  data attributes from parent container to the actual map container
        $.extend($mapDiv.data(), $el.data());

        if ($mapDiv.height() == 0) {
            $mapDiv.height(400);
        }
        return _initMap(div).done(function(map){
            console.log('_initEditableMaps');
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
            $(div).data('drawControl', drawControl);
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
                console.log("deleted fired");
                var layers = e.layers;
                var size = 1;
                layers.eachLayer(function(layer) {
                    drawnItems.removeLayer(layer);
                    size--;
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

                if (size == 0 ) {
                    _enableRectangleCreate($mapDiv);
                }
            });

            //dirty the form if rectangle created, edited, or deleted
            map.on('draw:created draw:edited draw:deleted', function(e){
                $mapDiv.closest('form').FormNavigate('dirty');
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
            //console.log("> 360:" + b.toBBoxString() + "  -->> ", bnds.toBBoxString());
        }

        // the change() watch deosn't always pay attention to these explicit calls
        $(".miny", $el).val(bnds.getSouth());
        $(".maxy", $el).val(bnds.getNorth());
        $(".d_miny", $el).val(bnds.getSouth());

        console.log("west: " + bnds.getWest() + " east:" + bnds.getEast());
        var x = bnds.getWest();
        x = _correctForWorldWrap(x);
        $(".minx", $el).val(x);
        $(".d_minx", $el).val(x);
        if (x < -180) {
            x = parseFloat(x) + 360.0;

            $(".d_minx", $el).val(x );
            $(".minx", $el).val(x );
        }

        x = bnds.getEast();
        x = _correctForWorldWrap(x);
        $(".maxx", $el).val(x);
        $(".d_maxx", $el).val(x);
        if (x > 180) {
            // LEAFLET HANDLES WRAPPING AROUND THE DATELINE SPECIALLY, , SO WE NEED TO TRANSLATE FOR IT
            // http://www.macwright.org/2015/03/23/geojson-second-bite.html IS A GOOD EXPLANATION, BUT BASICALLY HERE, THE REVERSE AS ABOVE
            //
            x = parseFloat(x) - 360.0;
            $(".d_maxx", $el).val(x);
            $(".maxx", $el).val(x);
        }
        console.log("west(set): " + $(".d_minx").val() + " east(set):" + $(".d_maxx").val());
        $(".d_maxy", $el).val(bnds.getNorth());
        
        var $change = $el.data("_onChange");
        console.log($el, $change);
        if ($change != undefined ) {
            console.log("change");
            $change({maxx:$(".d_maxx", $el).val(), maxy:$(".d_maxy", $el).val(), minx: $(".d_minx", $el).val(), miny: $(".d_miny", $el).val() });
        }
        return bnds;
    }

    /**
     * Leaflet corrects for world wrapping by adding or subtracting 360... to get the valid Lat/Long we need to correct for this
     */
    function _correctForWorldWrap(x_) {
        var x = x_;
        while (x > 180) {
            x -= 360;
        }
        while (x < -180) {
            x += 360;
        }
        if (x != x_) {
            console.log("   " + x_ + " --> " + x);
        }
        return x;
    }


    function _isIntialized() {
        return _initialized;
    }

	function _getMaps() {
        //note:  The author apologizes for the alternating meaning of the word "map" that follows.
        return $('.leaflet-container').map(function(i, elem){return $(elem).data().map;});
	}

    return {
        initLeafletMaps: _initLeafletMaps,
        initEditableLeafletMaps: _initEditableMaps,
        initEditableMap: _initEditableMap,
        initResultsMaps: _initResultsMaps,
        initialized: _isIntialized,
        defaults: _defaults,
        dynamicUpdateMap: _dynamicUpdateMap,
        getMaps : _getMaps,
        update: _update,
        main : function() {
            TDAR.leaflet.initLeafletMaps();
            TDAR.leaflet.initEditableLeafletMaps();
            TDAR.leaflet.initResultsMaps();
        }
    }
})(console, jQuery, window, L);
