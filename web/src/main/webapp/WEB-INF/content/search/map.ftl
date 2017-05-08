<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>

<#--FIXME: this method for determining active tab won't work if (for example) controller returns INPUT for collection/institution/person search -->

<head>
    <title>Map Search</title>
    <style type="text/css">
    </style>

</head>
<body>
<#escape _untrusted as _untrusted?html >
<h1>Search ${siteAcronym}</h1>

 <div id="map" style="width:100%;height:500px" class="leaflet-map-results" data-infinite-url="/search/json?"></div>
    <script type="text/javascript">
    var map;
    var layer;
    var markers;
    $(document).ready(function() {
		setTimeout(initLocal,500);
	});
    function initLocal() {
		var map = $("#map").data("map");           
        map.on("dragend zoomend resize", updateMap);
	    function updateMap() {
	        var b = map.getBounds();
	        var latLong = "&";  
	        latLong += "groups[0].latitudeLongitudeBoxes[0].east=";
	        latLong += b.getEast();
	        latLong += "&groups[0].latitudeLongitudeBoxes[0].south=";
	        latLong += b.getSouth();
	
	        latLong += "&groups[0].latitudeLongitudeBoxes[0].west=";
	        latLong += b.getWest();
	        latLong += "&groups[0].latitudeLongitudeBoxes[0].north=";
	        latLong += b.getNorth();
	        latLong += "&";
	        var baseUrl = "/search/json?orientation=MAP&recordsPerPage=100&projectionModel=LUCENE_EXPERIMENTAL&latScaleUsed=true&" + latLong;
	        TDAR.leaflet.dynamicUpdateMap($("#map"),baseUrl,0);    
	    }
    }
    </script>
</#escape>
