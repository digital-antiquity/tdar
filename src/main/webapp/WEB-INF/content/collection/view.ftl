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


<div class="row">
<div class="span3">
	<ul id="mapitems">
		<#list results as result>
			<li <#if result.firstLatitudeLongitudeBox?has_content> data-lat="${result.firstLatitudeLongitudeBox.minObfuscatedLatitude?c}"
			data-long="${result.firstLatitudeLongitudeBox.minObfuscatedLongitude?c}" </#if>>
				<a href="<@s.url value="/${result.resourceType.urlNamespace}/${result.id?c}"/>" class="title">${result.title}</a><br/>
				<p class="description">${result.description}</p>
			 </li>
		</#list>
	</ul>
</div>
<div class="span9" id="large-map">

</div>

</div>    
    
<div class="glide">
        <@list.listResources resourcelist=results sortfield=resourceCollection.sortBy  titleTag="h5" />
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

<script>
$(function() {
TDAR.maps.initMapApi();
var map = new google.maps.Map(document.getElementById("large-map"));

var bounds = new google.maps.LatLngBounds();

var markers = new Array();
var infowindows = new Array();

$("#mapitems li").each(function() {
	var infowindow = new google.maps.InfoWindow({
	    content: $(this).html()
	});
	
	var marker = new google.maps.Marker({
	    position: new google.maps.LatLng($(this).attr("data-lat"),$(this).attr("data-long")),
	    map: map,
	    title:$("a", $(this)).text()
	});

	google.maps.event.addListener(marker, 'click', function() {
	  infowindow.open(map,marker);
	});

	markers[markers.length] = marker;
	infowindows[infowindows.length] = infowindow;
	bounds.extend(marker.position);
});
map.fitBounds(bounds);

});
</script>

</body>
</#escape>