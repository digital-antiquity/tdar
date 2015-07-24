TDAR.leaflet = {};
TDAR.leaflet = (function(console, $, ctx) {
    
    var _defaults = {
            isGeoLocationToBeUsed: false,
            center: {
                lat: 0,
                lng: 0
            },
            zoomLevel: 4
    }
    
    var _rectangleDefaults = {
            fitToBounds : true
    }
    // -1 -- not initialized ; -2 -- bad rectangle ; 1 -- initialized ; 2 -- rectangle setup
	var _initialized = -1;
	
    var _map;
    var _dc;
    /**
     * Init the leaflet map, and bind it to the element
     */
    function _initMap(elem) {
        var map = L.map(elem).setView([ _defaults.center.lat, _defaults.center.lng ], _defaults.zoomLevel);

        map.setMaxBounds([[-85,-180.0],[85,180.0]]);
        var tile = L.tileLayer('https://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png', {
            maxZoom : 17,
            minZoom : 2,
            attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, '
                + '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, '
                + 'Imagery © <a href="http://mapbox.com">Mapbox</a>',
                id : 'examples.map-i875mjb7'
        });
        tile.addTo(map);
		//FIXME: WARN if DIV DOM HEIGHT IS EMPTY
        _map = map;
		_initialized = 0;
        return map;
    }

    function _initResultsMaps() {
        $(".leaflet-map-results").each(function(){
            var $el = $(this);
            var map  = _initMap(this);
            var markers = new L.MarkerClusterGroup();
            var recDefaults = $.extend(_rectangleDefaults, {
                fillOpacity: 0.08,
                fitToBounds : true
            });
            _initFromDataAttr($el,map, recDefaults);
            $(".resource-list.MAP .listItem").each(function() {
                var $t = $(this);
                var title = $(".resourceLink",$t);
                var lat = $t.data("lat");
                var lng = $t.data("long");
                if (!isNaN(lat) && !isNaN(lng)) {
                    var marker = L.marker(new L.LatLng(lat, lng), { title: title.text().trim() });
                    marker.bindPopup(title.html() + "<br><a href='"+title.attr('href')+"'>view</a>");
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
                    var map  = _initMap(this);
                    _initFromDataAttr($el,map, _rectangleDefaults);
                });
    }
    
    
    function _initFromDataAttr($el,map, rectangleSettings) {
        var $minx = parseFloat($el.data("minx"));
        var $miny = parseFloat($el.data("miny"));
        var $maxx = parseFloat($el.data("maxx"));
        var $maxy = parseFloat($el.data("maxy"));
        _initRectangle(map, $minx,$miny,$maxx,$maxy, rectangleSettings);

    }
	/**
     * create the rectangle based on the bounds
     */
	function _initRectangle(map, minx, miny, maxx, maxy,rectangleSettings) {
        if (minx != undefined && miny != undefined && maxx != undefined && maxy != undefined &&
			!isNaN(minx) && !isNaN(miny) && !isNaN(maxy) && !isNaN(maxx)) {
            console.log(minx, maxx, miny, maxy);
            var poly = [ [ maxy, maxx ], [ miny, minx ]];
            var rectangle = L.rectangle(poly,rectangleSettings).addTo(map);
            console.log("fitToBounds:", rectangleSettings.fitToBounds);
			if (rectangleSettings.fitToBounds) {
	            map.fitBounds(rectangle.getBounds());
			}
			_initialized =2;
			return rectangle;
        } else if (minx == undefined && miny == undefined && maxx == undefined && maxy == undefined) {
            // skipping, we're just not configured at all;
            return;
        }
		_initialized = -2;
        console.log("check map init bounds ["+ minx + "," + miny + "] [" + maxx + "," + maxy +"]");
	}
	
    /**
     * Updates a leaflet layer (removes/adds) based on the .minx, .miny, .maxx, .maxy vals
     */
    function _updateLayerFromFields($el,map,drawnItems) {
        var $minx = parseFloat($(".minx",$el).val());
        var $miny = parseFloat($(".miny",$el).val());
        var $maxx = parseFloat($(".maxx",$el).val());
        var $maxy = parseFloat($(".maxy",$el).val());
        // Initialise the FeatureGroup to store editable layers
        var layers = drawnItems.getLayers();

        // remove the old layer
        if (layers.length > 0) {
            drawnItems.removeLayer(layers[0]);
        }
		
		var rectangle = _initRectangle(map, $minx,$miny,$maxx,$maxy,_rectangleDefaults);
		
		if (rectangle != undefined) {
            _disableRectangleCreate();
            drawnItems.addLayer(rectangle);
		} else {
		    _enableRectangleCreate();
		    for (var i=0;i> drawnItems.getLayers().length ; i++) {
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
        $(".leaflet-map-editable").each(function(){
            var $el = $(this);

            // we create a div just before the div and copy the styles from the container. This is so that we can bind to class seletion for the fields.
            // Currently, this is a bit of overkill but it would enable multiple forms on the same page
            
            // we're using raw javascript because jquery didn't like the prepend or "before" method.
           var div = document.createElement("div");
           // copy styles
           div.setAttribute("style",$el.attr("style"));
           $el.attr("style","");
           if ($(div).height() == 0) {
               $(div).height(400);
           }
           $el.before(div);
           var map = _initMap(div);

           var drawnItems = new L.FeatureGroup();
            $(".locateCoordsButton",$el).click(function(){
                var rec = _updateLayerFromFields($el,map,drawnItems);
            });

            // bind ids
            var $dmx = $(".d_maxx",$el);
            var $dix = $(".d_minx",$el);
            var $dmy = $(".d_maxy",$el);
            var $diy = $(".d_miny",$el);
            // handling text based formats too    Geo.parseDMS("51°33′39″N" ); ...
            $dmx.change(function(){$(".maxx",$el).val(Geo.parseDMS($dmx.val()));});
            $dmy.change(function(){$(".maxy",$el).val(Geo.parseDMS($dmy.val()));});
            $dix.change(function(){$(".minx",$el).val(Geo.parseDMS($dix.val()));});
            $diy.change(function(){$(".miny",$el).val(Geo.parseDMS($diy.val()));});
            
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
            _updateLayerFromFields($el,map,drawnItems);
            map.addLayer(drawnItems);
            
            /**
             * Assumption of only one bounding box
             */
            map.on('draw:created', function (e) {
                var type = e.layerType,
                    layer = e.layer;

                drawnItems.addLayer(layer);
                var b = layer.getBounds();
                var bnds = _setValuesFromBounds($el, b);
                layer.setBounds(bnds);
                map.addLayer(layer);
                _disableRectangleCreate();
            });
            
            /**
             * Assumption of only one bounding box
             */
            map.on('draw:edited', function (e) {
                var layers = e.layers;
                layers.eachLayer(function (layer) {
                    var b = layer.getBounds();
                    var bnds = _setValuesFromBounds($el, b);
                    layer.setBounds(bnds);
                });
            });
            
            /**
             * Assumption of only one bounding box
             */
            map.on('draw:deleted', function (e) {
                var layers = e.layers;
                layers.eachLayer(function (layer) {
                    drawnItems.removeLayer(layer);
                    // the change() watch deosn't always pay attention to these explicit calls
                    $(".minx",$el).val('');
                    $(".miny",$el).val('');
                    $(".maxx",$el).val('');
                    $(".maxy",$el).val('');
                    $(".d_minx",$el).val('');
                    $(".d_miny",$el).val('');
                    $(".d_maxx",$el).val('');
                    $(".d_maxy",$el).val('');
                });
                _enableRectangleCreate();
            });
        });
    }

    function _enableRectangleCreate() {
        var $drawRec = $(".leaflet-draw-draw-rectangle");
        $drawRec.fadeTo(1,1);
// $drawRec.click($drawRec.clickHandler);
        
    }
    
    
    function _disableRectangleCreate() {
        var $drawRec = $(".leaflet-draw-draw-rectangle");
        $drawRec.fadeTo(1,.5);
// $drawRec.off("click");
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
        $(".minx",$el).val(bnds.getWest());
        $(".miny",$el).val(bnds.getSouth());
        $(".maxx",$el).val(bnds.getEast());
        $(".maxy",$el).val(bnds.getNorth());
        $(".d_minx",$el).val(bnds.getWest());
        $(".d_miny",$el).val(bnds.getSouth());
        $(".d_maxx",$el).val(bnds.getEast());
        $(".d_maxy",$el).val(bnds.getNorth());
        return bnds;
    }
	
	
	function _isIntialized() {
		return _initialized;
	}
	
	function _getMap() {
	    return _map;
	}
	function _getDc() {
	    return _dc;
	}

    return {
        initLeafletMaps : _initLeafletMaps,
        initEditableLeafletMaps : _initEditableMaps,
        initResultsMaps: _initResultsMaps,
		initialized : _isIntialized,
        defaults : _defaults,
        map : _getMap,
        dc : _getDc
    }
})(console, jQuery, window);
$(function() {
    TDAR.leaflet.initLeafletMaps();
    TDAR.leaflet.initEditableLeafletMaps();
    TDAR.leaflet.initResultsMaps();
});