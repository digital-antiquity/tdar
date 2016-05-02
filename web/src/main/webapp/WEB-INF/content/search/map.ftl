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
    $(document).ready(function() {
         map = L.map('map', {
            center: [45.57, -73.5648],
            zoom: 5
        });
        L.tileLayer("http://{s}.tile.osm.org/{z}/{x}/{y}.png", {attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> Imagery © <a href="http://cloudmade.com">CloudMade</a>'}).addTo(map);
        layer = new L.geoJson();
        layer.addTo(map);
        
        updateMap();        
        map.on("dragend zoomend resize", updateMap);
    });
    
    function updateMap() {
        var b = map.getBounds();
        var latLong = "&";  
        latLong += "groups[0].latitudeLongitudeBoxes[0].maximumLongitude=";
        latLong += b.getWest();
        latLong += "&groups[0].latitudeLongitudeBoxes[0].minimumLatitude=";
        latLong += b.getNorth();
        latLong += "&groups[0].latitudeLongitudeBoxes[0].minimumLongitude=";
        latLong += b.getEast();
        latLong += "&groups[0].latitudeLongitudeBoxes[0].maximumLatitude=";
        latLong += b.getSouth();
        latLong += "&";
        $.ajax({
        dataType: "json",
        url: "/search/json?orientation=MAP&startRecord=0&recordsPerPage=5000&projectionModel=LUCENE_EXPERIMENTAL&latScaleUsed=true&" + latLong,
        success: function(data) {
           var markers = new L.MarkerClusterGroup();
            var hasBounds = false;
            $(data.features).each(function(key, data_) {
                if (data_.geometry.type) {
                    layer.addData(data_);
                    var title = data_.properties.title;
                    var c = data_.geometry.coordinates;
                    hasBounds = true;
                    var marker = L.marker([c[0],c[1]]);
                    marker.bindPopup(title + "<br><a href='" + data_.properties.detailUrl + "'>view</a>");
                    markers.addLayer(marker);
                }
            });
            map.addLayer(markers);
            if (hasBounds) {
//                map.fitBounds(markers.getBounds());
            }
        }
        }).error(function() {});
    
    }
    
    
    
    </script>
</#escape>
