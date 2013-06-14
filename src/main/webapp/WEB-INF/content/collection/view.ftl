<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>

<head>
<@search.headerLinks includeRss=false />
<title>${resourceCollection.name!"untitled collection"}</title>
<meta name="lastModifiedDate" content="$Date$"/>
<@view.canonical resourceCollection />
<#assign rssUrl>/search/rss?groups[0].fieldTypes[0]=COLLECTION&groups[0].collections[0].id=${resourceCollection.id?c}&groups[0].collections[0].name=${(resourceCollection.name!"untitled")?url}</#assign>
<@search.rssUrlTag url=rssUrl />

</head>
<body>
<@view.toolbar "collection" "view" />

<@view.pageStatusCallout />
<h1>${resourceCollection.name!"untitled collection"}</h1>
<#if resourceCollection.visible || viewable>
<!-- Don't show header if header doesn't exist -->
<#if resourceCollection.parent?? || resourceCollection.description?? || collections??>
    <div class="glide">
        <#if resourceCollection.parent??><p><b>Part of:</b> <a href="${resourceCollection.parent.id?c}">${resourceCollection.parent.name!"(n/a)"}</a></p></#if>
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

    <#if  results?has_content && results?size !=0 >

		<div id="divResultsSortControl">
		    <div class="row">
		        <div class="span4">
				    <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Record"/>
		        </div>
		        <div class="span5"></div>
		    </div>
		</div>


		<#assign mapSize="450" />
		<#if (totalRecords > 10)>
			<#assign mapSize="700" />
		</#if>
		<#if (totalRecords > 18)>
			<#assign mapSize="1000" />
		</#if>
         
		<div class="tdarresults">
		    <@list.listResources resourcelist=results sortfield=resourceCollection.sortBy  titleTag="h5" listTag="ul" itemTag="li" itemsPerRow=5
		        orientation=resourceCollection.orientation    mapPosition="left" mapHeight=mapSize />
		</div>

		<@search.basicPagination "Records" />
	<#else>
	This collection is either empty or you do not currently have permissions to view the contents.
	</#if>
		<#if editable>
		  <h3>Administrative Information</h3>
		  
		    <@common.resourceUsageInfo />
		  <dl>
		    <dt><p><strong>Collection Type:</strong></p><dt>
		    <dd><p> ${resourceCollection.type.label}</p></dd>
            <dt><p><strong>Created by</strong></p></dt>
            <dd><p><a href="<@s.url value="/browse/creators/${resourceCollection.owner.id?c}"/>">${resourceCollection.owner.properName}</a> on ${resourceCollection.dateCreated}</p></dd>
            <dt><p><strong>Updated By</strong></p></dt>
            <dd><p><a href="<@s.url value="/browse/creators/${resourceCollection.updater.id?c}"/>">${resourceCollection.updater.properName}</a> on ${resourceCollection.dateUpdated}</p></dd>
		    <dt><p><strong>Visible:</strong></p></dt>
		    <dd><p> ${resourceCollection.visible?string}</p></dd>
		    <#if resourceCollection.sortBy??><dt><p><strong>Sort by:</strong></dt><dd><p> ${resourceCollection.sortBy.label}</p></dd></#if>
		</dl>
		    <@view.authorizedUsers resourceCollection />
		</#if>
<#else>
This collection is not accessible
</#if>

<script type='text/javascript'>
$(document).ready(function(){
    $(initializeView);
});
</script>



</body>
</#escape>