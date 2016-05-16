TDAR.worldmap = (function(console, $, ctx) {
    "use strict";

    var hlayer;
    var geodata = {};
    var map;
    var $mapDiv;
    var OUTLINE = "#777777";
    var stateLayer = undefined;
    var overlay = false;
    var allData = new Array();
    var clickId;
    // note no # (leaflet doesn't use jquery selectors)
    var mapId = "worldmap";

    var myStyle = {
        "color" : OUTLINE,
        "weight" : 1,
        "fillOpacity" : 1
    }

    var c3colors = [];
    var max = 0;

    // Intialize the Geodata based on the JSON
    function _initializeGeoData(mapdata) {
        for (var i = 0; i < mapdata.length; i++) {
            if (mapdata[i].resourceType == undefined) {
                var val = mapdata[i].count;
                geodata[mapdata[i].code] = val;
                if (val > max) {
                    max = val;
                }
            }
        }
    }

    /**
     * Look for embedded json in the specified container and return the parsed result. Embedded json should be tagged with a boolean attribute named
     * 'data-mapdata'. If no embedded json found, method returns null.
     * 
     * @param containerElem
     * @private
     */
    function _getMapdata(containerElem) {
        var json = $(containerElem).find('script[data-mapdata]').text() || 'null';
        return JSON.parse(json);
    }

    /**
     * Default color range for the world map based on custom weighting function
     */
    var _worldRangeColor = {
        colorProperty : _getColor,
    };

    /**
     * defaults for choropleth plugin
     */
    var _defaultChoropleth = {
        valueProperty : function(feature) {
            if (geodata[feature.id]) {
                return geodata[feature.id];
            } else {
                return 0;
            }
        },
        colors : [ "#fff", "#FED976", "#FEB24C", "#FD8D3C", "#FC4E2A", "#E31A1C", "#BD0026", "#800026" ],
        steps : 8,
        mode : 'q',
        style : {
            color : '#ccc',
            weight : 1,
            fillOpacity : 0.8
        },
        onEachFeature : function(feature, layer) {
            layer.on({
                click : _clickWorldMapLayer,
                mouseover : _highlightFeature,
                mouseout : _resetHighlight
            });
        }
    }

    /**
     * Init the world map passing in the DIV id
     */
    function _initWorldMap(mapId_) {
        if (mapId_ != undefined) {
            mapId = mapId_;
        }
        if (document.getElementById(mapId) == undefined) {
            return;
        }
        $mapDiv = $("#" + mapId);
        var mapdata = _getMapdata($mapDiv.parent());
        var c3_ = $("#c3colors");
        if (c3_.length > 0) {
            c3colors = JSON.parse(c3_.html());
        }
        

        map = L.map(mapId, {
            // config for leaflet.sleep
            sleep : true,
            // time(ms) for the map to fall asleep upon mouseout
            sleepTime : 750,
            // time(ms) until map wakes on mouseover
            wakeTime : 750,
            // defines whether or not the user is prompted oh how to wake map
            sleepNote : false,
            // should hovering wake the map? (clicking always will)
            hoverToWake : true,
            sleepOpacity : 1
        });
        var grades = [ 0, 1, 2, 5, 10, 20, 100, 1000 ];
        _initializeGeoData(mapdata);
        _setupLegend(map, grades, _getColor, max);
        _resetView();

        // load map data
        // FIXME: consider embedding data for faster rendering
        var jqxhr = $.getJSON(TDAR.uri("/js/maps/world.json"));
        jqxhr.done(function(data) {
            _setupMapLayer(data, map);
        });
        jqxhr.fail(function(xhr) {
            console.error("Failed to load world.json file. XHR result follows this line.", xhr);
        });
        map.on('click', _resetView);
        return map;
    }

    /**
     * build out the legend based on the color function, grades, and the max size
     */
    function _setupLegend(map, grades, colorFunction, _max) {
        var legend = L.control({
            position : 'bottomright'
        });

        var zoom = L.control({
            position : 'topright'
        });
        zoom.onAdd = function(map) {
            var div = L.DomUtil.create('div', 'mapGraphZoomOut');
            div.id="mapGraphZoomOut";
            $(div).append("<i class='icon-zoom-out'></i> Zoom Out");
            return div;
        };
        zoom.addTo(map);
        
        legend.onAdd = function(map) {

            var div = L.DomUtil.create('div', 'info');
            $(div).append("<div id='data'></div>");
            var legnd = L.DomUtil.create("div", " legend");

            // loop through our density intervals and generate a label with a colored square for each interval
            legnd.innerHTML += " <span>0</span> ";
            for (var i = 0; i < grades.length; i++) {
                legnd.innerHTML += '<i style="width:10px;height:10px;display:inline-block;background:' + colorFunction(grades[i] + 1) + '">&nbsp;</i> ';
            }
            legnd.innerHTML += " <span>" + TDAR.common.formatNumber(_max) + "</span> ";
            $(div).append(legnd);
            return div;
        };

        legend.addTo(map);
    }

    /**
     * setup Leaflet.Choropleth
     */
    function _setupMapLayer(data, map, isStateLayer) {
        var props = {};
        if (!isStateLayer) {
            $.extend(props, _defaultChoropleth, _worldRangeColor);
        } else {
            props = _defaultChoropleth;
        }
        var layer = L.choropleth(data, props);
        layer.addTo(map);
        return layer;
    }

    /**
     * Load the state JSON file and choropleth
     */
    function _loadStateData() {
        var usStyle = {
            strokeColor : "#ff7800",
            weight : .5,
            fillColor : "#FEEFE",
            fillOpacity : 1
        };
        $.getJSON(TDAR.uri("/js/maps/USA.json"), function(data) {
            stateLayer = _setupMapLayer(data, map, true);
        });

    }

    /**
     * handle the Click event and load the local map if needed, alternately, and load the graph data
     */
    function _clickWorldMapLayer(event) {
        var ly = event.target.feature.geometry.coordinates[0];
        var id = event.target.feature.id;
        console.log(event.target.feature);
        var $zoomout = $("#mapGraphZoomOut");
        if (id && id != 'RUS') {
            $zoomout.show();
        } else {
            $zoomout.hide();
        }
        clickId = id;
        if (id && id.indexOf('USA') == -1) {
            if (stateLayer != undefined) {
                map.removeLayer(stateLayer);
                stateLayer = undefined;
            }
        }

        if (id == 'USA' && stateLayer == undefined) {
            _loadStateData();
        }

        _drawDataGraph(event.target.feature.properties.name, id);

        if (id != 'RUS') {
            map.fitBounds(event.target.getBounds());
            overlay = true;
        }
    }

    /**
     * Draw the pie chart for the state or country
     */
    function _drawDataGraph(name, id) {
        var $div = $("#mapgraphdata");
        var $header = $("#mapGraphHeader");
        // style='height:"+($mapDiv.height() - 50)+"px'
        if (name == undefined) {
            $header.html("Worldwide");
        } else {
            $header.html(name);
        }

        var mapdata = _getMapdata($mapDiv.parent());
        var filter = [];
        var data = [];
        var typeLabelMap = {};
        // ID == Country/state id (USA / USAAZ)
        if (id != undefined) {
            // find the set of data associated with this id
            filter = mapdata.filter(function(d) {
                return d.code == id
            });

            filter.forEach(function(row) {
                if (parseInt(row.count) && row.count > 0 && row.resourceType != undefined) {
                    data.push([ row.label, row.count ]);
                    typeLabelMap[row.label] = row.resourceType;
                }
            });
        } else {
            // initialization for "worldwide"
            if (allData.length == 0) {
                var tmp = {};
                mapdata.forEach(function(row) {
                    if (parseInt(row.count) && row.count > 0 && row.resourceType != undefined) {
                        typeLabelMap[row.label] = row.resourceType;
                        var t = 0;
                        if (parseInt(tmp[row.label])) {
                            t = parseInt(tmp[row.label]);
                        }
                        tmp[row.label] = t + row.count;
                    }
                });

                for ( var type in tmp) {
                    if (tmp.hasOwnProperty(type)) {
                        allData.push([ type, tmp[type] ]);
                    }
                }
                ;
            }
            data = allData;
        }

        var obj = {
            bindto : '#mapgraphpie',
            data : {
                columns : data,
                type : 'pie',
                onclick : function(d, element) {
                    var uri = "/search/results?resourceTypes=" + typeLabelMap[d.id];
                    if (clickId != undefined && name != undefined) {
                        uri += "&geographicKeywords=" + name;
                        if (clickId.length == 3) {
                            uri += " (Country)";
                        } else {
                            uri += " (State / Territory)";
                        }
                    }
                    window.location.href = TDAR.c3graphsupport.getClickPath(uri);
                }
            },
            pie : {
                label : {
                    show : false
                }
            },
            donut : {
                label : {
                    show : false
                }
            },
            tooltip : {
                format : {
                    value : function(value, ratio, id, index) {
                        return TDAR.common.formatNumber(value) + " (" + (ratio * 100.00).toFixed(2) + "%)";
                    }
                }
            },
            size : {
                height : ($div.height() * 3 / 5)
            }
        };

        // setup positioning based on mode
        if ($div.data("mode") == 'vertical') {
            obj.legend = {
                position : 'bottom',
                inset : {
                    anchor : 'top-left',
                    x : 20,
                    y : 0,
                    step : 4
                }
            };
            obj.size.height = '300px';
        } else {
            obj.legend = {
                position : 'right',
                inset : {
                    anchor : 'top-left',
                    x : 20,
                    y : 0,
                    step : 4
                }
            };
        }

        if (c3colors.length > 0) {
            obj.color = {};
            obj.color.pattern = c3colors;
        }
        setTimeout(100, c3.generate(obj));

    }

    /**
     * Zoom out
     */
    function _resetView() {
        map.setView([ 44.505, -0.09 ], 1);
        overlay = false;
        var $zoomout = $("#mapGraphZoomOut");
        $zoomout.hide();
        clickId = undefined;
        if (hlayer != undefined) {
            hlayer.setStyle({
                "fillOpacity" : 1
            });
        }
        map.eachLayer(function(l) {
            if (typeof l.redraw === "function") {
                l.redraw();
            }
        });
        if (stateLayer != undefined) {
            map.removeLayer(stateLayer);
            stateLayer = undefined;
        }
        _drawDataGraph();
    }

    /**
     * handle mouse-over
     */
    function _highlightFeature(e) {
        var layer = e.target;

        var cnt = 0;
        if (layer.feature.id != undefined) {
            if (geodata[layer.feature.id] != undefined) {
                cnt = geodata[layer.feature.id];
            }
        }

        $("#data").html(layer.feature.properties.name + ": " + TDAR.common.formatNumber(cnt));
        if (overlay === true) {
            return false;
        }
        layer.setStyle({
            fillOpacity : 0.7
        });
    }

    function _resetHighlight(e) {
        if (overlay === true) {
            return false;
        }

        var layer = e.target;
        layer.setStyle({
            fillOpacity : 1
        });
        // hlayer.resetStyle(layer);
        $("#data").html("");
    }

    /**
     * Custom color assignent;
     */
    function _getColor(d) {
        d = parseInt(d);
        if (d > 1000) {
            return '#800026';
        }
        if (d > 100) {
            return '#BD0026';
        }
        if (d > 20) {
            return '#E31A1C';
        }
        if (d > 10) {
            return '#FC4E2A';
        }
        if (d > 5) {
            return '#FD8D3C';
        }
        if (d > 2) {
            return '#FEB24C';
        }
        if (d > 1) {
            return '#FED976';
        }
        return '#FFF';
    }

    return {
        initWorldMap : _initWorldMap
    }
})(console, jQuery, window);