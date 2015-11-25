TDAR.worldmap = (function(console, $, ctx) {
    "use strict";

    var statesData;
    var hlayer;
    var geodata = {};
    var map;
	var $mapDiv;
    var OUTLINE = "#777777";
    // note no # (leaflet doesn't use jquery selectors)
    var mapId = "worldmap";

    var myStyle = {
        "color": OUTLINE,
        "weight": 1,
        "fillOpacity": 1
    }

    /**
     * Look for embedded json in the specified container  and return the parsed result.  Embedded json should be
     * tagged with a boolean attribute named 'data-mapdata'.  If no embedded json found, method returns null.
     *
     * @param containerElem
     * @private
     */
    function _getMapdata(containerElem) {
        var json = $(containerElem).find('script[data-mapdata]').text() || 'null';
        return JSON.parse(json);
    }

    function _initWorldMap(mapId_) {
        if (mapId_ != undefined) {
            mapId = mapId_;
        }
        if (document.getElementById(mapId) == undefined) {
            return;
        }
		$mapDiv = $("#"+mapId);
        var mapdata = _getMapdata($mapDiv.parent());

        map = L.map(mapId,{
        // config for leaflet.sleep
        sleep: true,
        // time(ms) for the map to fall asleep upon mouseout
        sleepTime: 750,
        // time(ms) until map wakes on mouseover
        wakeTime: 750,
        // defines whether or not the user is prompted oh how to wake map
        sleepNote: false,
        // should hovering wake the map? (clicking always will)
        hoverToWake: true
        });
        _resetView();
        var max = 0;
        for (var i = 0; i < mapdata.length; i++) {
            if (mapdata[i].resourceType == undefined) {
                var val = mapdata[i].count;
                geodata[mapdata[i].code] = val;
                if (val > max) {
                    max = val;
                }
            }
        }

        // load map data
        //FIXME: consider embedding data for faster rendering
        var jqxhr = $.getJSON(TDAR.uri("/js/maps/world.json"));
        jqxhr.done(function(data) {
            hlayer = new L.GeoJSON(data, {
                style: myStyle,
                onEachFeature: function(feature, layer_) {
                    layer_.on({
                        click: _clickWorldMapLayer,
                        mouseover: _highlightFeature,
                        mouseout: _resetHighlight
                    });
                }
            }).addTo(map);
            _setupLayers(hlayer);
        });
        jqxhr.fail(function(xhr){
            console.error("Failed to load world.json file. XHR result follows this line.", xhr);
        });
        map.on('click', _resetView);
        
        var legend = L.control({position: 'bottomright'});

        legend.onAdd = function (map) {

            var div = L.DomUtil.create('div', 'info');
            $(div).append("<div id='data'></div>");
            var legnd = L.DomUtil.create("div"," legend");
            var grades = [0, 1, 2, 5, 10,  20, 100, 1000];

            // loop through our density intervals and generate a label with a colored square for each interval
            legnd.innerHTML += " <span>0</span> ";
            for (var i = 0; i < grades.length; i++) {
                legnd.innerHTML +=
                '<i style="width:10px;height:10px;display:inline-block;background:' + _getColor(grades[i] + 1) + '">&nbsp;</i> ';
            }
            console.log(max);
            legnd.innerHTML += " <span>"+TDAR.common.formatNumber(max)+"</span> ";
            $(div).append(legnd);
            return div;
        };

        legend.addTo(map);
        return map;
    }
    
    function _setupLayers(lyr) {
        lyr.eachLayer(function(layer) {
            var color = _getColor(geodata[layer.feature.id]);
            if (typeof layer._path != 'undefined') {
                layer._path.id = layer.feature.id;
                _redrawLayer(layer, color,1);
            } else {
                layer.eachLayer(function(layer2) {
                    layer2._path.id = layer.feature.id;
                    _redrawLayer(layer2, color,1);
                });
            }

        });
    }
    
    function _redrawLayer(layer, fillColor, opacity, id) {
        layer.options.fill = fillColor;
        layer.options.opacity = 1;
        layer.options.color = OUTLINE;
        layer._path.style.fill = fillColor;
        layer._path.style.opacity = 1;
        layer.redraw();
    }

    var stateLayer = undefined;
    var overlay = false;
    
    function _loadStateData() {
        var usStyle = {
            strokeColor: "#ff7800",
            weight: .5,
            fillColor:"#FEEFE",
            fillOpacity: 1
        };
        $.getJSON(TDAR.uri("/js/maps/USA.json"), function(data) {
            stateLayer = new L.GeoJSON(data, {
                style: usStyle,
                onEachFeature: function(feature, layer_) {
                    layer_.on({
                        click: _clickWorldMapLayer,
                        mouseover: _highlightFeature,
                        mouseout: _resetHighlight
                    });
                }
            }).addTo(map);
            _setupLayers(stateLayer);

        hlayer.setStyle({
            "fillOpacity": .1
        });
        });
        
    }

    function _clickWorldMapLayer(event) {
        var ly = event.target.feature.geometry.coordinates[0];
        if (event.target.feature.id && event.target.feature.id.indexOf('USA') == -1) {
            if (stateLayer != undefined) {
                map.removeLayer(stateLayer);
            }
        }
        
        
        if (event.target.feature.id == 'USA' && stateLayer == undefined) {
//            _loadStateData();
        }

        _drawDataGraph(event.target.feature.properties.name, event.target.feature.id);
		
        if (event.target.feature.id != 'RUS') {
            map.fitBounds(event.target.getBounds());
            overlay = true;
        }
    }
    
    var allData = new Array();
    
    function _drawDataGraph(name, id) {
		var $div = $("#mapgraphdata");
        //style='height:"+($mapDiv.height() - 50)+"px'
        if (name == undefined ) {
            $("#mapGraphHeader").html("Worldwide");
        } else {
            $("#mapGraphHeader").html(name);
        }
		
        var mapdata = _getMapdata($mapDiv.parent());
		var filter =[];
		var data = [];
		var typeLabelMap = {};
        if (id != undefined) {
            filter = mapdata.filter(function(d) {return d.code == id});  
      		filter.forEach(function(row){
      			if (parseInt(row.count) && row.count > 0 && row.resourceType != undefined) {
      			    console.log(row);
      				data.push([row.label, row.count]);				
      			}
      		});
        } else {
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

            for (var type in tmp) {
              if (tmp.hasOwnProperty(type)) {
                  allData.push([ type , tmp[type]]);
              }
            };
        }
        data = allData;
        }

		var obj = {
			bindto: '#mapgraphpie',
		    data: {
		        columns: data,
		        type : 'pie',
		        onclick: function (d, element) {
                    var uri = "/search/results?resourceTypes=" + typeLabelMap[d.id];
                    if (name != undefined) {
                        uri += "&geographicKeywords=" + name + " (Country)";
                    }
                    window.location.href = TDAR.c3graphsupport.getClickPath(uri);
		        }
		    },
            pie: {
                label: {
                    show : false
                }
            },
            donut: {
                label: {
                    show: false
                }
            },
            tooltip: {
              format: {
                value: function (value, ratio, id, index) { return  TDAR.common.formatNumber(value) + " ("+ (ratio * 100.00).toFixed(2) +"%)"; }
              }
            },
            size: {
              height: ($div.height() * 3/5)
            }
		};
		if ($div.data("mode") == 'vertical') {
            obj.legend =  {
                    position: 'bottom',
                inset: {
                    anchor: 'top-left',
                  x: 20,
                  y: 0,
                  step: 4
                }
            };
            obj.size.height= '300px';
		} else {
		    obj.legend =  {
                    position: 'right',
                inset: {
                    anchor: 'top-left',
                  x: 20,
                  y: 0,
                  step: 4
                }
            };
		}
		
		var c3colors = $("#c3colors");
		if (c3colors.length > 0) {
			var c3colorsobj = JSON.parse(c3colors.html());
			obj.color = {};
			obj.color.pattern = c3colorsobj;
		}
		setTimeout(100,  c3.generate(obj));
        
    }

    function _resetView() {
        map.setView([44.505, -0.09], 1);
        overlay = false;
        if (hlayer != undefined) {
            hlayer.setStyle({
            "fillOpacity": 1
        });
        }
        map.eachLayer(function(l) {
            if (typeof l.redraw === "function") {
                l.redraw();
            }
        });
        if (stateLayer != undefined) {
            map.removeLayer(stateLayer);
        }
        _drawDataGraph();
    }

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
            weight: 1,
            color: '#666',
            dashArray: '',
            fillOpacity: 0.7
        });
    }

    function _resetHighlight(e) {
        if (overlay === true) {
            return false;
        }

        var layer = e.target;
        hlayer.resetStyle(layer);
        $("#data").html("");
    }

    function _getColor(d) {
        d = parseInt(d);
        return d > 1000 ? '#800026' : d > 100 ? '#BD0026' : d > 20 ? '#E31A1C' : d > 10 ? '#FC4E2A' : d > 5 ? '#FD8D3C' : d > 2 ? '#FEB24C' : d > 1 ? '#FED976'
            : '#FFF';
    }

    return {
        initWorldMap: _initWorldMap
    }
})(console, jQuery, window);