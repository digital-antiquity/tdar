<#escape _untrusted as _untrusted?html>
<#import "view-macros.ftl" as view>
<#import "common.ftl" as common>
<#assign DEFAULT_SORT = 'RELEVANCE' />
<#assign DEFAULT_ORIENTATION = 'LIST_FULL' />

	<#-- this macro prints a html tag with or without a closing -->
    <#macro printTag tagName className closing>
        <#if tagName?has_content>
            <<#if closing>/</#if>${tagName} class="${className}" <#nested><#rt/>>
        </#if>
    </#macro>

	<#-- special header for the grid layout -->
    <#macro printGridHeader orientation listTag_>
        <#if isGridLayout>
            <div class='resource-list row ${orientation}'>
        <#else>
            <@printTag listTag_ "resource-list ${orientation}" false />
        </#if>
    </#macro>

	<#-- divider between the sections of results -->
	<#macro printDividerBetweenResourceRows itemTag_ first rowCount itemsPerRow orientation>
        <#if itemTag_?lower_case != 'li'>
        	<#-- if not first result -->
            <#if !first>
                <#if (!isGridLayout)>
                    <hr/>
                <#elseif rowCount % itemsPerRow == 0>
                    </div>    </div><hr /><div class=" ${orientation} resource-list row"><div class="span2">
                </#if>
            </#if>
        </#if>
	</#macro>

	<#-- add the lat-long data attributes for the map -->
	<#macro addLatLongDataAttributes orientation resource>
				<#if orientation == 'MAP' && resource.latLongVisible >
	            data-lat="${resource.firstActiveLatitudeLongitudeBox.centerLatitude?c}"
	            data-long="${resource.firstActiveLatitudeLongitudeBox.centerLongitude?c}" </#if>
	</#macro>

<#--fixme:  with at least three presentation style (list/grid/map/custom), this macro has become *extremely* hard to modify, 
    let alone comprehend. Consider replacing w/ @listResources, @listResourcesMap, and @listResourcesGrid -->
<#macro listResources resourcelist sortfield=DEFAULT_SORT itemsPerRow=4
    listTag='ul' itemTag='li' headerTag="h3" titleTag="h3" orientation=DEFAULT_ORIENTATION mapPosition="" mapHeight="">
	
	<#-- parameters:
		sortField: how to sort the results
		itemsPerRow: how many items to show per row in "GRID" orientation
		listTag: by default wrap all resources in a (<ul/> for lists; <div/> for grid; <ol/> for map), alternately, you can change this to something else
		itemTag: by default wrap each item in a (<li/> for lists and map; <div/> for grid), alternately, specify something
		headerTag: if the search results is sorted in some manner wrap the header in a <h3/>, the tag to use for the header
		titleTag: the tag to wrap the resource title in, default <h3/>
		orientation: the default orientation for the results (Title List,  List, Grid, Map)
		mapPositon: where to show the map in relation to the result list
		mapHeight: how high the map should be
	-->
	
  <#local showProject = false />
  <#assign prev =""/>
  <#assign first = true/>
  <#local listTag_=listTag/>  
  <#local itemTag_=itemTag/> 
  <#local itemClass = ""/>
  
  <@common.reindexingNote />
  
  <#-- set default ; add map wrapper -->
  <#if orientation == "GRID">
    <#local listTag_="div"/>  
    <#local itemClass = "span2"/>
    <#local itemTag_="div"/> 
  <#elseif orientation == "MAP" >
    <#local listTag_="ol"/>  
    <#local itemTag_="li"/> 
    <div class="resource-list row">
      <#if mapPosition=="top" || mapPosition == "right">
        <div class="span9 google-map" <#if mapHeight?has_content>style="height:${mapHeight}px"</#if> > </div>
      </#if>    
    
      <div class="<#if mapPosition=='left' || mapPosition=="right">span3<#else>span9</#if>">
  </#if>
  <#global isGridLayout = (orientation=="GRID") />
  <#global isMapLayout = (orientation=="MAP") />
  <#local rowCount = -1 />

  <#if resourcelist??>  
  <#list resourcelist as resource>
    <#assign key = "" />
    <#assign defaultKeyLabel="No Project"/>

    <#-- if we're viewable -->
    <#if ((resource.viewable)!false) >
       <#local rowCount= rowCount+1 />

		<#-- list headers are displayed when sorting by specific fields ResourceType and Project -->
		<@printListHeaders sortfield first resource headerTag orientation listTag_ />	   

        <#-- printing item tag start / -->
        <@printTag itemTag_ "listItem ${itemClass!''}" false>
			<@addLatLongDataAttributes orientation resource />
        </@printTag>

		<#-- if we're at a new row; close the above tag and re-open it (bug) -->
		<@printDividerBetweenResourceRows itemTag_ first rowCount itemsPerRow orientation />

		<#-- add grid thumbnail -->
		<@addGridThumbnail resource />
		
		<#-- add the title -->
        <@searchResultTitleSection resource titleTag />

        <#-- if in debug add lucene description to explain relevancy -->
        <@printLuceneExplanation  resource />

        <#-- print resource's description -->
        <@printDescription resource=resource orientation=orientation length=500 showProject=showProject/>

		<#-- close item tag -->
        </${itemTag_}>
        <#local first=false/>
     </#if>
    </#list>
    
    <#-- if we didn't have any results, don't close the list tag, as there was none -->
    <#if rowCount != -1>
	  </${listTag_}>
    </#if>
  </#if>
  
  <@addMapFooter orientation mapPosition mapHeight />
</#macro>


<#macro printListHeaders sortfield first resource=null headerTag="" orientation='LIST' listTag_='li'>	   
    <#-- handle grouping/sorting with indentation -->
    <#-- special sorting for RESOURCE_TYPE and PROJECT to group lists by these; sort key stored in "key" -->
    <#if (sortfield?contains('RESOURCE_TYPE') || sortfield?contains('PROJECT')) && resource.resourceType?has_content>
        <#if sortfield?contains('RESOURCE_TYPE')>
            <#assign key = resource.resourceType.plural />
            <#assign defaultKeyLabel="No Resource Type"/>  
        </#if>
        <#if sortfield?contains('PROJECT')>
            <#if resource.project??>
                <#assign key = resource.project.title />
            <#elseif resource.resourceType.project >
                <#assign key = resource.title />
            </#if>
        </#if>
        <#-- print special header and group/list tag -->
        <#if first || (prev != key) && key?has_content>
            <#if prev != '' || sortField?has_content && !first && (sortField?contains("RESOURCE_TYPE") || sortField?contains("PROJECT"))></${listTag_}></#if>
            <${headerTag}><#if key?has_content>${key}<#else>${defaultKeyLabel}</#if></${headerTag}>

			<#-- if we're a grid, then reset rows -->
			<@printGridHeader orientation listTag_ />
        </#if>
        <#local prev=key />
    <#elseif first>
        <#-- default case for group tag -->
		<@printGridHeader orientation listTag_ />
    </#if>  
</#macro>

<#macro addMapFooter orientation mapPosition mapHeight  >
	  <#if orientation == "MAP">
	  </div>
	      <#if mapPosition=="left" || mapPosition == "bottom">
	    <div class="span9 google-map" <#if mapHeight?has_content>style="height:${mapHeight}px"</#if> >
	    
	    </div>
	    </#if>    
	    </div>    
	    <script>
	        $(document).ready(function() {
		        TDAR.maps.setupMapResult();
	        });
	    </script>      
	  </#if>
</#macro>



<#macro addGridThumbnail resource>
	<#if isGridLayout>
	    <a href="<@s.url value="/${resource.urlNamespace}/${resource.id?c}"/>" target="_top"><#t>
	            <@view.firstThumbnail resource /><#t>
	        <#t></a><br/>
	</#if>
</#macro>


<#macro printDescription resource=resource orientation=DEFAULT_ORIENTATION length=80 showProject=false>
	<#if resource?has_content>
		<#local _desc = "Description not available"/>
		<#if (resource.description)?has_content >
			<#if !resource.description?starts_with("The information in this record has been migrated into tDAR from the National Archaeological Database Reports Module")>
				<#local _desc = resource.description />
			</#if>
		</#if>
		<#local _rid = resource.id?c >
		<#--//FIXME: need non-hokey way to determine whether persistable is collection  -->
		<#if resource.class.simpleName == 'ResourceCollection'>
			<#local _rid = "C${resource.id?c}" >
		</#if>
	
        <#if orientation == 'LIST_FULL'>
            <div class="listItemPart">
	            <#if (resource.citationRecord?has_content && resource.citationRecord && !resource.resourceType.project)>
		            <span class='cartouche' title="Citation only; this record has no attached files.">Citation</span>
	            </#if>
	            <@common.cartouche resource true><@listCreators resource/></@common.cartouche>  
	            <@view.unapiLink resource  />
	            <#if showProject && !resource.resourceType.project >
		            <p class="project">${resource.project.title}</p>
	            </#if>
	            <p class="abstract">
	                <span class="pull-right small">[tDAR id: ${_rid}]</span>                        
	                <@common.truncate _desc length />
	            </p>
            </div>
        </#if>
	</#if>
</#macro>

    <#macro printLuceneExplanation resource>
            <blockquote class="luceneExplanation">
                <#if resource.explanation?has_content><b>explanation:</b>${resource.explanation}<br/></#if>
            </blockquote>
            <blockquote class="luceneScore">
                <#if resource.score?has_content><b>score:</b>${resource.score}<br/></#if>
            </blockquote>
    </#macro>


<#macro searchResultTitleSection result titleTag >
    <#local titleCssClass="search-result-title-${result.status!('ACTIVE')}" />
    <#if titleTag?has_content>
        <${titleTag} class="${titleCssClass}">
    </#if>
    <a class="resourceLink" href="<@s.url value="/${result.urlNamespace}/${result.id?c}"/>"><#rt>
    <#if result.title?has_content>
        ${result.title!"No Title"} <#if result.status?has_content && (editor || result.viewable) && !result.active ><small>[${result.status?upper_case}]</small></#if><#t>
    <#elseif result.properName?has_content>
        ${result.properName!"No Name"}<#t>
     <#else>
         No Title
    </#if>
    <#if isMapLayout && result.latLongVisible><i class="icon-map-marker"></i></#if>
        <#if (result.date?has_content && (result.date > 0 || result.date < -1) )>(${result.date?c})</#if>
    </a><#lt>
    <@bookmark result false/>
    <#if titleTag?has_content>
        </${titleTag}>
    </#if>
</#macro>


<#macro listCreators resource_>
     <#assign showSubmitter=true/>
     <#if resource_.primaryCreators?has_content>
      <span class="authors">
        <#list resource_.primaryCreators as creatr>
          <#assign showSubmitter=false/>
          ${creatr.creator.properName}<#if creatr__has_next??>,<#else>.</#if>
        </#list>
      </span>
    </#if>    

     <#if resource_.editors?has_content>
      <span class="editors">
        <#list resource_.editors as creatr>
          <#assign showSubmitter=false/>
          <#if creatr_index == 0><span class="editedBy">Edited by:</span></#if>
          ${creatr.creator.properName}<#if creatr__has_next??>,<#else>.</#if>
        </#list>
      </span>
    </#if>

    <#if showSubmitter && resource_.submitter?has_content>
    <#assign label = "Created" />
    <#if resource_.resourceType?has_content>
        <#assign label = "Uploaded" />
    </#if>
        <span class="creators"> 
          <span class="createdBy">${label} by:</span> ${resource_.submitter.properName}
        </span>
    </#if>
</#macro>

<#macro bookmark _resource showLabel=true useListItem=false>
  <#if sessionData?? && sessionData.authenticated>
      <#if _resource.resourceType?has_content>
      <#assign status = "disabled-bookmark" />

        <#if bookmarkedResourceService.isAlreadyBookmarked(_resource, authenticatedUser)>
           <#assign status = "un-bookmark" />
        <#else>
           <#assign status = "bookmark" />
        </#if>
        
        <#if useListItem>
            <li class="${status}">
        </#if>

        <#if _resource.deleted?? && _resource.deleted>
            <#if showLabel>
                <span class="disabled" title='Deleted items cannot be bookmarked.'>
            	<i title="disabled boomkark" class="bookmark-icon tdar-icon-bookmark-disabled"></i>
                bookmark</span><#t>
            </#if>
        <#elseif bookmarkedResourceService.isAlreadyBookmarked(_resource, authenticatedUser)>
            <a href="<@s.url value='/resource/removeBookmark' resourceId='${_resource.id?c}'/>" class="bookmark-link" resource-id="${_resource.id?c}" bookmark-state="bookmarked" >
                	<i title="bookmark or unbookmark" class="tdar-icon-bookmarked bookmark-icon"></i>
                <#if showLabel>
                    <span class="bookmark-label">un-bookmark</span><#t>
                </#if>
            </a><#t>
        <#else>
            <a href="<@s.url value='/resource/bookmark' resourceId='${_resource.id?c}'/>"  class="bookmark-link" resource-id="${_resource.id?c}" bookmark-state="bookmark">
                	<i title="bookmark or unbookmark"  class="bookmark-icon tdar-icon-bookmark"></i>
                <#if showLabel>
                    <span class="bookmark-label"> bookmark</span><#t>
                </#if>
            </a><#t>    
        </#if>

        <#if useListItem>
            </li>
        </#if>
        
      </#if>
  </#if>
</#macro>
</#escape>
