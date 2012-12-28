<#escape _untrusted as _untrusted?html>
<#include "navigation-macros.ftl">
<#import "view-macros.ftl" as view>
<#import "common.ftl" as common>
<#assign DEFAULT_SORT = 'PROJECT' />
<#assign DEFAULT_ORIENTATION = 'LIST' />
    <#macro printTag tagName className closing>
        <#if tagName?has_content>
            <<#if closing>/</#if>${tagName} class="${className}" <#nested><#rt/>>
        </#if>
    </#macro>

<#macro listResources resourcelist=resources_ sortfield=DEFAULT_SORT editable=false bookmarkable=authenticated itemsPerRow=4
    expanded=false listTag='ul' itemTag='li' headerTag="h3" titleTag="h3" orientation=DEFAULT_ORIENTATION mapPosition="" mapHeight="">
  <#local showProject = false />
  <#local prev =""/>
  <#local first = true/>
  <#assign listTag_=listTag/>  
  <#assign itemTag_=itemTag/> 
  <#assign itemClass = ""/>
  
  <#-- setup default -->
  <#if orientation == "GRID">
    <#assign listTag_="div"/>  
    <#assign itemClass = "span2"/>
    <#assign itemTag_="div"/> 
  <#elseif orientation == "MAP" >
    <#assign listTag_="ol"/>  
    <#assign itemTag_="li"/> 
    <div class="resource-list controls-row">
      <#if mapPosition=="top" || mapPosition == "right">
        <div class="span9 google-map" <#if mapHeight?has_content>style="height:${mapHeight}px"</#if> > </div>
      </#if>    
    
      <div class="<#if mapPosition=='left' || mapPosition=="right">span3<#else>span9</#if>">
  </#if>
  
  
  <#if resourcelist??>
  <#list resourcelist as resource>
    <#local key = "" />
    <#local defaultKeyLabel="No Project"/>
    <#-- if we're a resource && are viewable -->
    <#if resource?? && (!resource.viewable?has_content || resource.viewable) >
        <#-- handle grouping/sorting with indentation -->
        <#if sortfield?contains('RESOURCE_TYPE') || sortfield?contains('PROJECT')>
            <#if sortfield?contains('RESOURCE_TYPE')>
                <#local key = resource.resourceType.plural />
                <#local defaultKeyLabel="No Resource Type"/>  
            </#if>
            <#if sortfield?contains('PROJECT')>
                <#if resource.project??>
                    <#local key = resource.project.titleSort />
                <#elseif resource.resourceType.project >
                    <#local key = resource.titleSort />
                </#if>
            </#if>
            <#-- print header and group/list tag -->
            <#if first || (prev != key) && key?has_content>
                <#if prev != '' || sortField?has_content && (sortField?contains("RESOURCE_TYPE") || sortField?contains("PROJECT"))></${listTag_}></#if>
                <${headerTag}><#if key?has_content>${key}<#else>${defaultKeyLabel}</#if></${headerTag}>
                <${listTag_} class='resource-list  controls-row ${orientation}'>
            </#if>
            <#local prev=key />
        <#elseif resource_index == 0>
            <#-- default case for group tag -->
            <@printTag listTag_ "resource-list  controls-row ${orientation}" false />
        </#if>  
	        <#-- printing item tag -->
            <@printTag itemTag_ "listItem ${itemClass!''}" false>
            <#if orientation == 'MAP' && resource.firstActiveLatitudeLongitudeBox?has_content> data-lat="${resource.firstActiveLatitudeLongitudeBox.centerLatitude?c}"
            data-long="${resource.firstActiveLatitudeLongitudeBox.centerLongitude?c}" </#if>
            </@printTag>

            <#if itemTag_?lower_case != 'li'>
                <#if resource_index != 0>
                <#if orientation != 'GRID'>
                    <hr/>
                <#elseif resource_index   % itemsPerRow = 0>
                    </div>    </div><hr /><div class=" ${orientation} resource-list controls-row"><div class="span2">
                </#if>
                </#if>
            </#if>
            <#if orientation == 'GRID'>
                <a href="<@s.url value="/${resource.urlNamespace}/${resource.id?c}"/>" target="_top">
                    <#if resource.supportsThumbnails>
                        <@view.firstThumbnail resource />
                    <#else>
                        <i class="${resource.resourceType?lower_case}-125"></i>
                    </#if>
                    </a><br/>
            </#if>
            <@searchResultTitleSection resource titleTag />
            <@printLuceneExplanation  resource />
            <@printDescription resource=resource expanded=expanded orientation=orientation length=500 showProject=showProject/>

            </${itemTag_}>
        <#local first=false/>
     </#if>
    </#list>
  </${listTag_}>
  </#if>
  <#if orientation == "MAP">
  </div>
      <#if mapPosition=="left" || mapPosition == "bottom">
      <#-- fixme: should be a span 9, but this seems to flow down instead of fit in 1 column -->
    <div class="span8 google-map" <#if mapHeight?has_content>style="height:${mapHeight}px"</#if> >
    
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


<#macro printDescription resource=resource expanded=false orientation=DEFAULT_ORIENTATION length=80 showProject=false>
            <#if expanded && orientation != 'GRID'>
                <div class="listItemPart">
            <#if (resource.citationRecord?has_content && resource.citationRecord && !resource.resourceType.project)>
               <span class='cartouche' title="Citation only; this record has no attached files.">Citation</span></#if>
            <@common.cartouche resource true><@listCreators resource/></@common.cartouche>  
                <@view.unapiLink resource  />
                <#if showProject && !resource.resourceType.project >
                <p class="project">${resource.project.title}</p>
                </#if>
                <#if resource.description?has_content && !resource.description?starts_with("The information in this record has been migrated into tDAR from the National Archaeological Database Reports Module") >
                    <p class="abstract">
                        <@common.truncate resource.description!"No description specified." length />
                    </p>
                </#if>
	 <p><b>tDAR id:</b> ${resource.id?c}</p>
                <br/>
                </div>
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


<#macro searchResultTitleSection result titleTag>
    <#local titleCssClass="search-result-title-${result.status!('ACTIVE')}" />
    <!-- <h3><a href="">Casa Grande Ruins National Monument, A Centennial History of the First Prehistoric Reserve, 1892-1992</a></h3> -->
    <#if titleTag?has_content>
        <${titleTag} class="${titleCssClass}">
    </#if>
    <a class="resourceLink" href="<@s.url value="/${result.urlNamespace}/${result.id?c}"/>"><#rt>
    <#if result.title?has_content>
        ${result.title!"No Title"}<#t>
    <#elseif result.properName?has_content>
        ${result.properName!"No Name"}<#t>
     <#else>
         No Title
    </#if>
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
                	<i title="disabled boomkark" class="bookmark-icon tdar-icon-bookmark-disabled"></i>
            <#if showLabel>
                <span class="disabled" title='Deleted items cannot be bookmarked.'>bookmark</span><#t>
            </#if>
        <#elseif bookmarkedResourceService.isAlreadyBookmarked(_resource, authenticatedUser)>
            <a href="<@s.url value='/resource/removeBookmark' resourceId='${_resource.id?c}'/>" class="bookmark-link" resource-id="${_resource.id?c}" bookmark-state="bookmarked" href="#">
                	<i title="bookmark or unbookmark" class="tdar-icon-bookmarked bookmark-icon"></i>
                <#if showLabel>
                    <span class="bookmark-label">un-bookmark</span><#t>
                </#if>
            </a><#t>
        <#else>
            <a href="<@s.url value='/resource/bookmark' resourceId='${_resource.id?c}'/>"  class="bookmark-link" resource-id="${_resource.id?c}" bookmark-state="bookmark" href="#">
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
