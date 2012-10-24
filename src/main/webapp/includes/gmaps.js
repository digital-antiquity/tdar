/**
 * Map rendering / edit support.
 * 
 * Requires:  jquery,  latLongUtil-1.0.js
 */
TDAR.namespace("maps");
TDAR.maps = function() {
    "use strict";
    
    var _isApiLoaded = false;
    var _pendingOps = [];
    var _defaults = {
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
    
    
    
    //fire the mapapi-ready event, but only after onready
    //FIXME: this may be over-kill. is it conceivable that we want multiple gmap-api event handlers to run?
    var _apiLoaded = function() {
        $(function(){
            console.debug("v3 map api loaded");
            $('body').trigger("mapapiready");
            _isApiLoaded = true;
            
            while(_pendingOps[0]) {
                var op = _pendingOps.shift();
                op();
            }
            
        });
    };
    
    //private: execute a function only when google api is ready
    var _executeWhenAPILoaded = function(callback) {
        if(_isApiLoaded) {
            callback();
        } else {
            _pendingOps.push(callback);
        }
        
    }
    
    //public: dynamically load the gmap v3 api
    var _initGmapApi = function() {
        var script = document.createElement("script");
        script.type = "text/javascript";
        script.src = "http://maps.googleapis.com/maps/api/js?libraries=drawing&key=" +
        		TDAR.maps.googleApiKey +
        		"&sensor=false&callback=TDAR.maps._apiLoaded";
        document.body.appendChild(script);
        console.log("api loaded");
    }
    
    //public: initialize a gmap inside of the specified div element.  If hidden inputs define spatial bounds,  draw
    //          a box and pan/zoom the map to fit the bounds.
    var _setupMap = function(mapDiv, inputContainer) {
        _executeWhenAPILoaded(function() {
            console.log("running  setupmap");
            var mapOptions = $.extend({}, _defaults.mapOptions, {
                    zoom: _defaults.zoomLevel,
                    center: new google.maps.LatLng(_defaults.center.lat, _defaults.center.lng),
                    mapTypeControlOptions:{
                        mapTypeIds: [
                            google.maps.MapTypeId.TERRAIN,
                            google.maps.MapTypeId.SATELLITE,
                            google.maps.MapTypeId.ROADMAP,
                            google.maps.MapTypeId.HYBRID
                        ]
                    },
                    mapTypeId: google.maps.MapTypeId.TERRAIN,
                    streetViewControl: false,
                    //scrollwheel zooming frustrates efforts to scroll vertically in the form. 
                    scrollwheel: false

            });
            var map = new google.maps.Map(mapDiv, mapOptions);
            $(mapDiv).data("gmap", map);

            if(inputContainer) {
                _setupLatLongBoxes(mapDiv, inputContainer);
            }
            return map;
        });
    };

    //private: look for resource latlongboxes and draw rectangles if found.
    var _setupLatLongBoxes = function(mapDiv, inputContainer){
        var style = _defaults.rectStyleOptions.RESOURCE;
        var gmap = $(mapDiv).data("gmap");
        if($('.sw-lat').val() !=="") {
            var lat1 = $('.sw-lat', inputContainer).val();
            var lng1 = $('.sw-lng', inputContainer).val();
            var lat2 = $('.ne-lat', inputContainer).val();
            var lng2 = $('.ne-lng', inputContainer).val();
            
            var rect = _addBound(mapDiv, style, lat1, lng1, lat2, lng2);
            if (rect) {
            	gmap.fitBounds(rect.getBounds());
            }
            //pan/zoom the 
        };
        
        $(mapDiv).data("resourceRect", rect);
        
        
        //TODO: draw a rect for parent project (but don't pan/zoom to it)

        //TODO: add "snap back" control, for when the user pans/zooms away from resource bounds
    };

    //private: add rect to map, returns: google.maps.Rectangle
    var _addBound = function(mapDiv, rectStyleOptions, lat1, lng1, lat2, lng2) {
//    	console.debug("%s %s %s %s", lat1, lng1, lat2, lng2);
    	if (!(parseInt(lat1) && parseInt(lat2) && parseInt(lng1) && parseInt(lng2))) 
    		return;
    	
        var p1 = new google.maps.LatLng(lat1, lng1);
        var p2 = new google.maps.LatLng(lat2, lng2);
        var bounds = new google.maps.LatLngBounds(p1, p2);
        var map = $(mapDiv).data("gmap");
        
        var rectOptions = $.extend({
                bounds: bounds, 
                map: map
            }, rectStyleOptions);
       
        var rect = new google.maps.Rectangle(rectOptions);
        
        console.debug("added rect:%s  to map:%s", rect, map);
        
        //move/pan the map to contain the rectangle w/ context
        return rect;
	        
    }
    
    var _updateBound = function(rect, lat1, lng1, lat2, lng2) {
        var p1 = new google.maps.LatLng(lat1, lng1);
        var p2 = new google.maps.LatLng(lat2, lng2);
        var bounds = new google.maps.LatLngBounds(p1, p2);
        rect.setBounds(bounds);
    }

    //public: setup a map in an editing context (after map has been initialized for viewing)
    var _setupEditMap = function(mapDiv, inputContainer) {
        _setupMap(mapDiv, inputContainer);
        _executeWhenAPILoaded(function(){
            var gmap = $(mapDiv).data("gmap");
    
            //add "select region" button
            var $controlDiv = $('<div class="tdar-gmap-control"></div>');
            //var $controlUi = $('<div class="tdar-gmap-control-ui"></div>');
            var $selectButton = $('<button type="button" id="btnSelectRegion" class="btn btn-small btn-primary">Select Region</button>');
            var $clearButton = $('<button type="button" id="btnClearRect" class="btn btn-small ">Clear Region</button>');
            $controlDiv.append($selectButton).append($clearButton);
    
            var drawingManager = _setupDrawingManager(mapDiv);
    
            //handle select click
            google.maps.event.addDomListener($selectButton[0], 'click', function() {
                var existingRect = $(mapDiv).data("resourceRect");
                console.log(" select region");
                //remove any existing rectangle
                if(existingRect) {
                    existingRect.setMap();
                    $(mapDiv).removeData("resourceRect");
                }
                drawingManager.setDrawingMode(google.maps.drawing.OverlayType.RECTANGLE);
                $(mapDiv).find('.tdar-gmap-control button').prop("disabled", true);
            });
    
            //handle clear click
            google.maps.event.addDomListener($clearButton[0], 'click', function() {
                var existingRect = $(mapDiv).data("resourceRect");
                if(!existingRect) return;
                existingRect.setMap();
                $(mapDiv).removeData("resourceRect");
    
                //tell the DOM that rect is gone
                _fireBoundsModified(mapDiv, null);
            });
    
            //add control to map
            gmap.controls[google.maps.ControlPosition.TOP_CENTER].push($controlDiv[0]);
    
            //if rect already present,  make it editable
            var rect = $(mapDiv).data('resourceRect');
            if(rect) {
                rect.setEditable(true);
                google.maps.event.addDomListener(rect, 'bounds_changed', function() {
                    _fireBoundsModified(mapDiv, rect);
                });
    
            }
            
            //bind resource rectangle to the manual latlong input controls
            _registerInputs(mapDiv, inputContainer);

    })};

    //gmap events are not 'seen' by the DOM.  bubble them up by firing custom event on the container div
    var _fireBoundsModified = function(mapDiv, rect) {
        console.log("resource rect created/changed:: map:%s,  rect:%s", mapDiv.id, rect);
        var bounds = null;
        if(rect) {
            bounds = rect.getBounds();
        }
        $(mapDiv).trigger("resourceboundschanged", bounds);
    };

    var _setupDrawingManager = function(mapDiv){
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
            rectangleOptions:$.extend({}, _defaults.rectStyleOptions.RESOURCE)
        });
        drawingManager.setMap(gmap);

        //as soon as rectangle is complete, turn off drawing mode, make rect editable
        google.maps.event.addDomListener(drawingManager, 'rectanglecomplete', function(rect) {
            drawingManager.setDrawingMode();
            rect.setEditable(true);
            $(mapDiv).data("resourceRect", rect);
            $(mapDiv).find('.tdar-gmap-control button').prop("disabled", false);

            //fire bounds-modified event for this new rect right now, and again whenever the bounds change
            _fireBoundsModified(mapDiv, rect);
            google.maps.event.addDomListener(rect, 'bounds_changed', function() {
                _fireBoundsModified(mapDiv, rect);
            });
        });

        $(mapDiv).data("drawingManager", drawingManager);
        return drawingManager;
    };
    
    //private: populate the latlon display input boxes
    var _populateLatLngDisplay = function(latLngBounds, $swLatDisplay, $swLngDisplay, $neLatDisplay, $neLngDisplay) {
        var sw = latLngBounds.getSouthWest();
        var ne = latLngBounds.getNorthEast();

        $swLatDisplay.val(Geo.toLat(sw.lat()));
        $swLngDisplay.val(Geo.toLon(sw.lng()));
        $neLatDisplay.val(Geo.toLat(ne.lat()));
        $neLngDisplay.val(Geo.toLon(ne.lng()));
    } ;

    //public: update input boxes  when bounds change, and vice versa
    //FIXME:  there's gotta be a better way to do this.
    //FIXME: requires latLongUtil.js
    var _registerInputs = function(mapDiv, inputContainer) {
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
        $(mapDiv).bind("resourceboundschanged", function(e, latLngBounds){
            //if no bounds, user clicked 'clear' button -- clear all input textboxes
            if(!latLngBounds) {
                $('input[type=text]', inputContainer).val("");
                return;
            };
            
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
        if($(mapDiv).data("resourceRect")) {
            _populateLatLngDisplay($(mapDiv).data("resourceRect").getBounds(), $swLatDisplay, $swLngDisplay, $neLatDisplay, $neLngDisplay);
        }

        //locate button clicked
        $btnLocate.click(function() {
            //trim the input, and if all non-blank then update the region
            var blankCount = 0;
            $('.sw-lat-display, .sw-lng-display, .ne-lat-display, .ne-lng-display', inputContainer).each(function(){
                this.value = $.trim(this.value);
                if(!this.value) blankCount++;
            });
            
            if(!blankCount)  {
                //parse the values and update the form values
                $swLatInput.val(Geo.parseDMS($swLatDisplay.val()));
                $swLngInput.val(Geo.parseDMS($swLngDisplay.val()));
                $neLatInput.val(Geo.parseDMS($neLatDisplay.val()));
                $neLngInput.val(Geo.parseDMS($neLngDisplay.val()));
                
                var rect = $(mapDiv).data("resourceRect");
                var gmap = $(mapDiv).data('gmap');
                
                //FIXME: REPLACE block w/ call to updateResourceRect
                if(!rect) {
                    rect = _addBound(mapDiv, _defaults.rectStyleOptions.RESOURCE, $swLatInput.val(), $swLngInput.val(), $neLatInput.val(), $neLngInput.val());
                } else {
                    _updateBound(rect, $swLatInput.val(), $swLngInput.val(), $neLatInput.val(), $neLngInput.val());
                }

                if (rect) {
                	$(mapDiv).data("resourceRect", rect);
                	gmap.fitBounds(rect.getBounds());
                }
            };
        });
    };
    
    
    var _bounds = function(swlat, swlng, nelat, nelng) {
        var sw = new google.maps.LatLng(swlat, swlng);
        var ne = new google.maps.LatLng(nelat, nelng);
        var bounds = new google.maps.LatLngBounds(sw, ne);
        return bounds;
    };
    
    var _bindRectEvents = function (mapDiv, rect) {
        google.maps.event.addDomListener(rect, 'bounds_changed', function() {
            _fireBoundsModified(mapDiv, rect);
        });
    }
    
    var _updateResourceRect = function(mapDiv, swlat, swlng, nelat, nelng) {
        var gmap = $(mapDiv).data("gmap");
        var rect = $(mapDiv).data("resourceRect");
        if(!rect) {
            rect = _addBound(mapDiv, _defaults.rectStyleOptions.RESOURCE, swlat, swlng, nelat, nelng);
            _bindRectEvents(mapDiv, rect);
            $(mapDiv).data("resourceRect", rect);
        } else {
            var bounds = _bounds(swlat, swlng, nelat, nelng);
            rect.setBounds(bounds);
        }
        gmap.fitBounds(rect.getBounds());
    };
    
    return {
        _apiLoaded: _apiLoaded,
        initMapApi: _initGmapApi,
        setupMap: _setupMap,
        googleApiKey: false,
        defaults: _defaults,
        updateResourceRect: _updateResourceRect,
        setupEditMap: _setupEditMap
    };
}();

//todo: "enter / view coordinates"  should be a bootstrap toggle button, not a checkbox.


