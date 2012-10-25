<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<@search.initResultPagination/>
<@search.headerLinks includeRss=false />

<head>
<title>${resourceCollection.name!"untitled collection"}</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@view.toolbar "collection" "view" />
<#if resourceCollection.visible || viewable>
<!-- Don't show header if header doesn't exist -->
<#if resourceCollection.parent?? || resourceCollection.description?? || collections??>
	<div class="glide">
	    <#if resourceCollection.parent??><p><b>Part of:</b> <a href="${resourceCollection.parent.id?c}"/>${resourceCollection.parent.name!"(n/a)"}</a></p></#if>
	    <p>${resourceCollection.description!"(n/a)"}</p>
	
	<#if (collections?has_content) >
	<B>Collections Contained in this Collection</B>
	<ul>
	  <#list collections as collection_>
	   <li><a href="<@s.url value="/collection/${collection_.id?c}"/>">${collection_.name}</a></li>
	  </#list>
	</ul>
	</#if>
  </div>
</#if>

<#if ( results?has_content )>
<@search.basicPagination "Records" />
<div class="glide">
<#if resourceCollection.orientation=='MAP'>
<div class="row" id="map_container">
<div id="map_canvas" class="google-map" style="width:65%;float:right;">

</div>
<div style="width:33%">

	<ol id="mapitems">
		<#list results as result>
			<li id="result${result_index}" <#if result.firstActiveLatitudeLongitudeBox?has_content> data-lat="${result.firstActiveLatitudeLongitudeBox.minObfuscatedLatitude?c}"
			data-long="${result.firstActiveLatitudeLongitudeBox.minObfuscatedLongitude?c}" </#if>>
				<a href="<@s.url value="/${result.resourceType.urlNamespace}/${result.id?c}"/>" class="title">${result.title}</a><br/>
				<p class="description"><@common.truncate result.description /></p>
			 </li>
		</#list>
	</ol>
</div>
<style>
	#mapitems .description {visibility:hidden;display:none;}
</style>
</div>    
    
<#else>    
		<@list.listResources resourcelist=results sortfield=resourceCollection.sortBy />
    </#if>
</div>
    </#if>

<#if editable>
<div class="glide">
  <h3>Administrative Information</h3>
  
	<@common.resourceUsageInfo />
  
    <p><b>Collection Type:</b> ${resourceCollection.type.label}</p>
    <p><b>Visible:</b> ${resourceCollection.visible?string}</p>
    <p><b>Owner:</b> <a href="<@s.url value="/browse/creators/${resourceCollection.owner.id?c}"/>">${resourceCollection.owner}</a></p>
    <#if resourceCollection.sortBy??><p><b>Sort by:</b> ${resourceCollection.sortBy.label}</p></#if>

    <p><b>Authorized Users:</b></p>
    <@view.authorizedUsers resourceCollection.authorizedUsers />
</div>
</#if>
<#else>
This collection is not accessible
</#if>
<script type='text/javascript'>
$(document).ready(function(){
    $(initializeView);
});
</script>
<script type="text/javascript">


	var markers = new Array();
$(document).ready(function() { 
	var latlngbounds = new GLatLngBounds();

	$("#mapitems li").each(function(){
		var $this = $(this);
	 	if ($this.attr("data-lat") != undefined) {
	 	console.log($this);
			var item = {};
			item['info'] = {};
			item['latitude'] = $this.attr("data-lat");
			item['longitude'] = $this.attr("data-long");
			item['info']['layer'] = "#" + $(this).attr('id');	
			markers[markers.length] = item;	
		    latlngbounds.extend(new GLatLng(item['latitude'],item['longitude']));
		}
	});

	$("#map_canvas").height($("#map_container").height());
    $('#map_canvas').googleMaps({
	 	latitude: 38.8, 
	    longitude: -99.2,
 	    depth:3,
        markers: markers
    }); 
	$.googleMaps.gMap.setCenter(latlngbounds.getCenter(), $.googleMaps.gMap.getBoundsZoomLevel(latlngbounds));
}); 

</script>

<script>
$(function() {

  $("body").bind("mapready", function() {
	var bounds = new google.maps.LatLngBounds();
	var myMap = $(".google-map").data('gmap');
	var markers = new Array();
	var infowindows = new Array();
	var i=0;
	$("#mapitems li").each(function() {
	i++;
	var $this = $(this);
		var infowindow = new google.maps.InfoWindow({
		    content: $this.html()
		});
		var marker = new google.maps.Marker({
		    position: new google.maps.LatLng($this.attr("data-lat"),$this.attr("data-long")),
		    map: myMap,
		    icon: 'http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld='+i+'|7a1501|FFFFFF',
		    title:$("a", $this).text()
		});
	
		$(this).click(function() {
			myMap.panTo(marker.getPosition());
		  $(infowindows).each(function() {this.close(myMap);});
		  infowindow.open(myMap,marker);
		  return false;
		});

		google.maps.event.addListener(marker, 'click', function() {
		  $(infowindows).each(function() {this.close(myMap);});
		  infowindow.open(myMap,marker);
		});
	
		markers[markers.length] = marker;
		infowindows[infowindows.length] = infowindow;
		bounds.extend(marker.position);
		myMap.fitBounds(bounds);
	}); 
});
});
</script>

</body>
</#escape>