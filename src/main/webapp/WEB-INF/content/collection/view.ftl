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
<h1>${resourceCollection.name!"untitled collection"}</h1>
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

<hr/>
<div class="row">
<div class="span3">
        <@list.listResources resourcelist=results sortfield=resourceCollection.sortBy  titleTag="h5" orientation="MAP"/>
</div>
<div class="span9 google-map">

</div>

</div>    
    
<div class="glide">
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

    <@view.authorizedUsers resourceCollection />
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

<script>
$(function() {

  $("body").bind("mapready", function() {
	var bounds = new google.maps.LatLngBounds();
	var myMap = $(".google-map").data('gmap');
	var markers = new Array();
	var infowindows = new Array();
	var i=0;
	$("ol.MAP li").each(function() {
		i++;
		var $this = $(this);
		if ($this.attr("data-lat") && $this.attr('data-long')) {
			var infowindow = new google.maps.InfoWindow({
			    content: $this.html()
			});
			var marker = new google.maps.Marker({
			    position: new google.maps.LatLng($this.attr("data-lat"),$this.attr("data-long")),
			    map: myMap,
			    icon: 'http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld='+i+'|7a1501|FFFFFF',
			    title:$("a.resourceLink", $this).text()
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
		};
	}); 
});
});
</script>

</body>
</#escape>