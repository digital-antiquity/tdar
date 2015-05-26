//FIXME: MODULARIZE

var statesData;
var hlayer;
var geodata = {};
var map = L.map('worldmap');
resetView();

var myStyle = {
	"strokeColor": "#ff7800",
	"weight": 1,
	"fillOpacity": 1
};

function initWorldMap() {
    for (var i =0 ;i < mapdata.length; i++) {
        if (mapdata[i].resourceType == undefined) {
            geodata[mapdata[i].code] = mapdata[i].count;
        }
    }
    
	$.getJSON("/js/maps/world.json", function(data) {
		hlayer = new L.GeoJSON(data, {
			style: myStyle,
			onEachFeature: function(feature, layer_) {
				layer_.on({
					click: clickWorldMapLayer,
					mouseover: highlightFeature,
					mouseout: resetHighlight
				});
			}
		}).addTo(map);

		
        hlayer.eachLayer(function(layer) {
            if (typeof layer._path != 'undefined') {
                layer._path.id = layer.feature.id;
                layer.options.color = getColor(geodata[layer.feature.id]);
                layer._path.style.fill = getColor(geodata[layer.feature.id]);
                layer._path.style.opacity = 1;
                layer.options.opacity = 1;

                layer.redraw();

            } else {
                layer.eachLayer(function(layer2) {
                    layer2._path.id = layer.feature.id;
                    layer2.options.fill = getColor(geodata[layer.feature.id]);
                    layer2.options.opacity = 1;
                    layer2.options.color = getColor(geodata[layer.feature.id]);
                    layer2._path.style.color = getColor(geodata[layer.feature.id]);
                    layer2._path.style.fill = getColor(geodata[layer.feature.id]);
                    layer2._path.style.opacity = 1;
                    layer2.redraw();
                });
            }

        });


	});
};
var stateLayer = undefined;
var overlay = false;

function clickWorldMapLayer(event) {
	var ly = event.target.feature.geometry.coordinates[0];
	if (stateLayer != undefined) {
		map.removeLayer(stateLayer);
	}

	if (event.target.feature.id == 'USA') {
		var usStyle = {
			"strokeColor": "#ff7800",
			"weight": .5,
			"fillOpacity": 0
		};
		stateLayer = new L.GeoJSON(statesData, {
			style: usStyle
		}).addTo(map);
		hlayer.setStyle({
			"fillOpacity": .1
		});
	}
	map.fitBounds(event.target.getBounds());
	overlay = true;
};



function resetView() {
	map.setView([44.505, -0.09], 1);
	overlay = false;
	if (hlayer != undefined) {
		hlayer.setStyle({
			"fillOpacity": 1
		});
	}
	if (stateLayer != undefined) {
		map.removeLayer(stateLayer);
	}
};

map.on('click', resetView);

function highlightFeature(e) {
	var layer = e.target;

	var cnt = 0;
	if (layer.feature.id != undefined) {
		cnt = geodata[layer.feature.id];
	}
	$("#data").html(layer.feature.properties.name + ": " + cnt);
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

function resetHighlight(e) {
	if (overlay === true) {
		return false;
	}

	var layer = e.target;
	hlayer.resetStyle(layer);
	$("#data").html("");
}

function getColor(d) {
	d = parseInt(d);
	return d > 1000 ? '#800026' :
		d > 100 ? '#BD0026' :
		d > 20 ? '#E31A1C' :
		d > 10 ? '#FC4E2A' :
		d > 5 ? '#FD8D3C' :
		d > 2 ? '#FEB24C' :
		d > 1 ? '#FED976' :
		'#FFF';
}
initWorldMap();
