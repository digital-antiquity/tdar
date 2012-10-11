	var map = null;
	var gzControl = null;
	var boundBox = false;

function loadTdarMap() {
	// googlemaps.js
	/* FIXME: modify this file to be a function that gets invoked */
	/*
	 * this writing is very important. note the "(" after addDomListener should
	 * match ")" after this whole function "}" I cannot directly write this
	 * function as load() and call this in the body load. The reason is that the
	 * popcalendar.js also init the window.
	 */
	// alert("action="+action);
	if (GBrowserIsCompatible()
			&& document.getElementById("large-google-map") != undefined) {
		map = new GMap2(document.getElementById("large-google-map"));
		// alert("load map"+map);
		// map.setCenter(new GLatLng(37.4419, -122.1419), 13);
		// add terrain map type as well.
		map.addMapType(G_PHYSICAL_MAP);
		// add the controls
		map.addControl(new GLargeMapControl());
		var ovmap = new GOverviewMapControl();
		ovmap.setMapType(G_PHYSICAL_MAP);
		GEvent.addListener(map, 'maptypechanged', function() {
			ovmap.setMapType(map.getCurrentMapType());
		});
		map.addControl(ovmap);
		map.addControl(new GMapTypeControl());
		map.addControl(new GScaleControl()); // hpcao added
		// 2008-04-30

		gzControl = new GZoomControl(
		/*
		 * first set of options is for the visual overlay.
		 */
		{
			nOpacity : .2,
			sBorder : "2px solid red"
		},
		/* second set of optionis is for everything else */
		{
			sButtonHTML : "<div id='selectARegion'>Select Region</div>",
			sButtonZoomingHTML : "<div id='selectARegion'>Select Region</div>"
		},

		/* third set of options specifies callbacks */
		{
			buttonClick : function() {
			},
			dragStart : function() {
			},
			dragging : function(x1, y1, x2, y2) {
			},
			dragEnd : function(nw, ne, se, sw, nwpx, nepx, sepx, swpx) {
				// hpcao changed this after Allen
				// changed the name of the four
				// spatial coordinates
				$("#minx").val(sw.lng());
				$("#miny").val(sw.lat());
				$("#maxx").val(ne.lng());
				$("#maxy").val(ne.lat());

				populateLatLongTextFields();
				boundBox = true;
			}
		});

		clearControl = new ClearControl();

		// TODO: decide this at controller level, and then have
		// ftl define a g_isEditing var
		var path = window.location.pathname;
		var action = path.substring(path.lastIndexOf("/") + 1);
		if (action.indexOf("edit") != -1 || action.indexOf("add") != -1
				|| action.indexOf("save") != -1 || path.indexOf("search") != -1) {
			map.addControl(gzControl, new GControlPosition(
					G_ANCHOR_BOTTOM_LEFT, new GSize(5, 85)));
			map.addControl(clearControl, new GControlPosition(
					G_ANCHOR_BOTTOM_LEFT, new GSize(5, 45)));
		}
		// set the starting location and zoom

		var MapLatLng = new GLatLng(GMapDefaultLat, GMapDefaultLng);
		var MapZoom = 4;

		if (google.loader.ClientLocation) {
			MapLatLng = new GLatLng(google.loader.ClientLocation.latitude,
					google.loader.ClientLocation.longitude);
		}

		map.setCenter(MapLatLng, MapZoom, G_PHYSICAL_MAP);

		// other

		map.enableDoubleClickZoom();
	}// end of if

	drawMBR(); // does this need to be here?
	drawMBR("p_", "#996633");
}

function ClearControl() {
}
	ClearControl.prototype = new GControl();
	ClearControl.prototype.initialize = function(map) {
		var container = document.createElement("div");
		var zoomInDiv = document.createElement("div");
		container.appendChild(zoomInDiv);
		zoomInDiv.id = "mapResetButton";
		zoomInDiv.appendChild(document.createTextNode("Reset"));
		GEvent.addDomListener(zoomInDiv, "click", function() {
			try {
				map.removeOverlay(GZoomControl.G.oZoomArea);
				document.getElementById("minx").value = "";
				document.getElementById("miny").value = "";
				document.getElementById("maxx").value = "";
				document.getElementById("maxy").value = "";

				document.getElementById("d_minx").value = "";
				document.getElementById("d_miny").value = "";
				document.getElementById("d_maxx").value = "";
				document.getElementById("d_maxy").value = "";
			} catch (e) {
			}
		});

		map.getContainer().appendChild(container);
		return container;
	};

// This function must be called after the map is displayed.
// If put this to the head script, the map cannot be zoomed to U.S. map.

// edit project and edit dataset cannot use this, so get this out
// to project registration and dataset registration
// zoomToUS();

// the following two functions work for "Locate" some spatial box
function locateCoords() {
	var minx = document.getElementById("minx").value;
	var miny = document.getElementById("miny").value;
	var maxx = document.getElementById("maxx").value;
	var maxy = document.getElementById("maxy").value;
	var minx1 = parseFloat(minx.replace(/^\s*|\s*$/g, ""));
	var miny1 = parseFloat(miny.replace(/^\s*|\s*$/g, ""));
	var maxx2 = parseFloat(maxx.replace(/^\s*|\s*$/g, ""));
	var maxy2 = parseFloat(maxy.replace(/^\s*|\s*$/g, ""));

	if (minx1 == null || minx1 == "" || miny1 == null || miny1 == ""
			|| maxx2 == "" || maxx2 == null || maxy2 == null || maxy2 == "") {
		alert("Please fill all four coordinate fields");
	} else {
		// alert("call make selection...");
		makeSelection(minx1, miny1, maxx2, maxy2);
	}
}
function makeSelection(x1, y1, x2, y2) {
	// alert ("makeSelection...");

	document.getElementById("minx").value = x1;
	document.getElementById("miny").value = y1;
	document.getElementById("maxx").value = x2;
	document.getElementById("maxy").value = y2;

	// draw a red box on top of the map
	var pts = [];
	pts[0] = new GLatLng(y1, x1);
	pts[1] = new GLatLng(y1, x2);
	pts[2] = new GLatLng(y2, x2);
	pts[3] = new GLatLng(y2, x1);
	pts[4] = new GLatLng(y1, x1);
	// alert ("makeSelection 2 ...");

	var G = GZoomControl.G;
	if (G.oZoomArea != null)
		G.oMap.removeOverlay(G.oZoomArea);

	// alert (G.style.sOutlineColor);
	G.oZoomArea = new GPolyline(pts, G.style.sOutlineColor,
			G.style.nOutlineWidth + 1, .4);

	var bounds = new GLatLngBounds();
	bounds.extend(pts[0]);
	bounds.extend(pts[1]);
	bounds.extend(pts[2]);
	bounds.extend(pts[3]);
	map.setZoom(map.getBoundsZoomLevel(bounds));

	map.panTo(new GLatLng((y1 + y2) / 2, (x1 + x2) / 2));
	map.addOverlay(G.oZoomArea);
}

// draw a red box on top of the map
function drawMBR(prefix, colour) {
	// x1 = min longitude
	// x2 = max longitude
	// y1 = min latitude
	// y2 = max latitude
	if (document.getElementById("large-google-map") == undefined)
		return;
	if (prefix == undefined)
		prefix = "";

	// make sure that the form name of the document is
	// "resourceRegistrationForm"
	// and it has minx, miny, maxx, and maxy
	try {
		var x1 = parseFloat(document.getElementById(prefix + "minx").value
				.replace(/^\s*|\s*$/g, ""));
		var y1 = parseFloat(document.getElementById(prefix + "miny").value
				.replace(/^\s*|\s*$/g, ""));
		var x2 = parseFloat(document.getElementById(prefix + "maxx").value
				.replace(/^\s*|\s*$/g, ""));
		var y2 = parseFloat(document.getElementById(prefix + "maxy").value
				.replace(/^\s*|\s*$/g, ""));

		if (isNaN(x1) || isNaN(y1) || isNaN(x2) || isNaN(y2))
			return;

		// alert("is NOT NaN");
		var pts = [];
		pts[0] = new GLatLng(y1, x1);
		pts[1] = new GLatLng(y1, x2);
		pts[2] = new GLatLng(y2, x2);
		pts[3] = new GLatLng(y2, x1);
		pts[4] = new GLatLng(y1, x1);

		// alert("hi1");
		var G = GZoomControl.G;
		if (colour == undefined)
			colour = G.style.sOutlineColor;
		var zoomarea = new GPolyline(pts, colour, G.style.nOutlineWidth + 1, .4);

		if (prefix == "") {
			try {
				if (G.oZoomArea != null)
					G.oMap.removeOverlay(G.oZoomArea);
			} catch (e) {
			}
			G.oZoomArea = zoomarea;
		}

		var bounds = new GLatLngBounds();
		bounds.extend(pts[0]);
		bounds.extend(pts[1]);
		bounds.extend(pts[2]);
		bounds.extend(pts[3]);
		map.setZoom(map.getBoundsZoomLevel(bounds));

		map.panTo(new GLatLng((y1 + y2) / 2, (x1 + x2) / 2));
		map.addOverlay(zoomarea);
	} catch (e) {
	}
}

function processLatLong(element) {
	var value = $(element).val();
	var id = $(element).attr('id');
	// value = value.replace(/([a-z]+)/ig,"");
	if (id.indexOf("d_") == 0)
		id = id.substring(2);
	$("#" + id).val(Geo.parseDMS(value));
}


TDAR.namespace("maps");
TDAR.maps = function() {
    "use strict";
    
    var _isApiLoaded = false;
    var _pendingOps = [];
    
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
    
    var _execute = function(callback) {
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
        script.src = "http://maps.googleapis.com/maps/api/js?key=" +
        		TDAR.maps.googleApiKey +
        		"&sensor=false&callback=TDAR.maps._apiLoaded";
        document.body.appendChild(script);        
    }
    
    
    //setup the specified map.
    var _setupMap = function(mapDiv) {
        _execute(function() {
            console.log("running  setupmap");
        });
    };
    
    
    return {
        _apiLoaded: _apiLoaded,
        initMapApi: _initGmapApi,
        setupMap: _setupMap,
        googleApiKey: false
    };
}();
