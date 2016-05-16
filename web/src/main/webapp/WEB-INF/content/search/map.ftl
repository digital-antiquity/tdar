<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>

<#--FIXME: this method for determining active tab won't work if (for example) controller returns INPUT for collection/institution/person search -->

<head>
    <title>Nap Search</title>
    <style type="text/css">
    </style>

</head>
<body>
<#escape _untrusted as _untrusted?html >
<h1>Search ${siteAcronym}</h1>

 <div id="map" style="width:100%;height:500px"></div>
    <script type="text/javascript">
    var map;
    var layer;
    var markers;
    $(document).ready(function() {
         map = L.map('map', {
            center: [45.57, -73.5648],
            zoom: 5,
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
        });
        L.tileLayer("http://{s}.tile.osm.org/{z}/{x}/{y}.png", {attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> Imagery © <a href="http://cloudmade.com">CloudMade</a>'}).addTo(map);
        layer = new L.geoJson();
        layer.addTo(map);
        
        updateMap();
        markers = new L.MarkerClusterGroup({maxClusterRadius:150, removeOutsideVisibleBounds:true, chunkedLoading: true});

        map.addLayer(markers);
        map.on("dragend zoomend resize", updateMap);
    });
    
    function updateMap() {
        var b = map.getBounds();
        var latLong = "&";  
        latLong += "groups[0].latitudeLongitudeBoxes[0].maximumLongitude=";
        latLong += b.getEast();
        latLong += "&groups[0].latitudeLongitudeBoxes[0].minimumLatitude=";
        latLong += b.getSouth();

        latLong += "&groups[0].latitudeLongitudeBoxes[0].minimumLongitude=";
        latLong += b.getWest();
        latLong += "&groups[0].latitudeLongitudeBoxes[0].maximumLatitude=";
        latLong += b.getNorth();
        latLong += "&";
        var baseUrl = "/search/json?orientation=MAP&recordsPerPage=100&projectionModel=LUCENE_EXPERIMENTAL&latScaleUsed=true&" + latLong;
        dynamicUpdateMap(baseUrl,0);    
    }

    function dynamicUpdateMap(baseUrl, startRecord_) {
        var startRecord = startRecord_;
        if (!startRecord) {
            startRecord = 0;
        }
        $.ajax({
        dataType: "json",
        url: baseUrl + "&startRecord="+startRecord,
        success: function(data) {
            _update(data,startRecord);
            var nextPage = data.properties.startRecord + data.properties.recordsPerPage;
            if (data.properties && (nextPage) < data.properties.totalRecords) {
                dynamicUpdateMap(baseUrl, nextPage);
            }    
        }
        }).error(function() {});
    
    }
    
    function _update(data,startRecord) {
        var layers = new Array();
        $(data.features).each(function(key, data_) {
            if (data_.geometry.type) {
                var title = data_.properties.title;
                var c = data_.geometry.coordinates;
                var marker = L.marker(new L.LatLng(c[1],c[0]), {title: title.trim()});
                marker.bindPopup(title + "<br><a href='" + data_.properties.detailUrl + "'>view</a>");
                layers.push(marker);
            }
        });
        if (startRecord == 0) {
            markers.clearLayers();
        }
        markers.addLayers(layers);

    }
    
    
    </script>
</#escape>
