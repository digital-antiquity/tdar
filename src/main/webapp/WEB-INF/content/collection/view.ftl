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
	
	<style>
		i.search-list-checkbox-grey {background-image:none!important;}
		li.media { display:inline-block}
	</style>

	
</head>
<body>
<#if editable>
    <@nav.toolbar "collection" "view">
        <@nav.makeLink
            namespace="collection"
            action="add?parentId=${id?c}"
            label="create child collection"
            name="columns"
            current=current
            includeResourceId=false
            disabled=disabled
            extraClass="hidden-tablet hidden-phone"/>
    </@nav.toolbar>
<#else>
    <@nav.toolbar "collection" "view" />
</#if>

<@view.pageStatusCallout />
<h1>${resourceCollection.name!"untitled collection"}</h1>
<#if (resourceCollection.visible || viewable)>
<#if !collections.empty>
<!-- Don't show header if header doesn't exist -->
    <div id="sidebar-right" parse="true">
        <br/>
        <br/>
        <br/>
        <br/>
        <br/> <#-- Nooooooo!!!!! -->
        <br/>
        <br/>
        <br/>
        <br/>
			<h3>Child Collections</h3>
			<@common.listCollections collections=collections showOnlyVisible=true />
	</div>
</#if>

<#if resourceCollection.parent?? || resourceCollection.description??  || resourceCollection.adminDescription?? || collections??>
    <div class="glide">
        <#if resourceCollection.parent??><p><b>Part of:</b> <a href="/collection/${resourceCollection.parent.id?c}">${resourceCollection.parent.name!"(n/a)"}</a></p></#if>
        <@common.description resourceCollection.description />

		<#if resourceCollection.adminDescription??>
		<p itemprop="description">
		  <#noescape>
		    ${resourceCollection.adminDescription}
		  </#noescape>
		</p>
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
        <#if selectedResourceTypes.empty>
        <@search.facetBy facetlist=resourceTypeFacets currentValues=selectedResourceTypes label="" facetParam="selectedResourceTypes" />
        <#else>
            <h4>
There are ${paginationHelper.totalNumberOfItems?c}
 <#if selectedResourceTypes?has_content>
${resourceTypeFacets[0].plural}

 <#else>Resources</#if> within this Project <#if selectedResourceTypes?has_content>                <sup><a style="text-decoration: " href="<@s.url includeParams="all">
            <@s.param name="selectedResourceTypes"value="" />
            <@s.param name="startRecord" value=""/>
</@s.url>">[remove this filter]</a></sup>
 </#if>
            </h4>
        </#if>
		<div class="tdarresults">
		<#assign itemsPerRow = 4/>
		<#assign mapPosition="top"/>
		<#if collections.empty>
			<#assign itemsPerRow = 5 />
			<#assign mapPosition="left"/>
		</#if>
		    <@list.listResources resourcelist=results sortfield=sortField titleTag="h5" listTag="ul" itemTag="li" itemsPerRow=itemsPerRow
		        orientation=resourceCollection.orientation    mapPosition=mapPosition mapHeight=mapSize />
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
    TDAR.common.initializeView();
    TDAR.common.collectionTreeview();
});
</script>



</body>
</#escape>