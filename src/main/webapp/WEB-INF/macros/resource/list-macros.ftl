<#escape _untrusted as _untrusted?html>
<#include "navigation-macros.ftl">
<#import "view-macros.ftl" as view>
<#import "common.ftl" as common>
<#-- 
Convert long string to shorter string with ellipses. If you pass it anything fancy (like 
html markup) you will probably not like the results
-->
    <#macro printTag tagName className closing>
    	<#if tagName?has_content>
    		<<#if closing>/</#if>${tagName} class="${className}">
    	</#if>
    </#macro>

<#macro listResources resourcelist=resources_ sortfield='PROJECT' editable=false bookmarkable=authenticated expanded=false listTag='ul' itemTag='li' headerTag="h3" titleTag="h3">
  <#local showProject = false />
  <#local prev =""/>
  <#local first = true/>
  <#if resourcelist??>
  <#list resourcelist as resource>
    <#local key = "" />
    <#local defaultKeyLabel="No Project"/>
    <#if resource?? && (!resource.viewable?has_content || resource.viewable) >
        <#if sortfield?contains('RESOURCE_TYPE') || sortfield?contains('PROJECT')>
            <#if sortfield?contains('RESOURCE_TYPE')>
                <#local key = resource.resourceType.plural />
                <#local defaultKeyLabel="No Resource Type"/>  
            </#if>
            <#if sortfield?contains('PROJECT')>
                <#local defaultKeyLabel="No Project"/>
                <#if resource.project??>
                    <#local key = resource.project.titleSort />
                <#elseif resource.resourceType == 'PROJECT'>
                    <#local key = resource.titleSort />
                </#if>
            </#if>
            <#if first || (prev != key) && key?has_content>
                <#if prev?has_content></${listTag}></#if>
                <${headerTag}><#if key?has_content>${key}<#else>${defaultKeyLabel}</#if></${headerTag}>
                <${listTag} class='resource-list'>
            </#if>
            <#local prev=key />
        <#elseif resource_index == 0>
            <@printTag listTag "resource-list" false />
        </#if>  
            <@printTag itemTag "listItem" false />
            <#if itemTag?lower_case != 'li'>
				<#if resource_index != 0>
            		<hr />
				</#if>
            </#if>
            <@searchResultTitleSection resource titleTag />

            <blockquote class="luceneExplanation">
    	        <#if resource.explanation?has_content><b>explanation:</b>${resource.explanation}<br/></#if>
			</blockquote>
            <blockquote class="luceneScore">
	            <#if resource.score?has_content><b>score:</b>${resource.score}<br/></#if>
			</blockquote>
            
            <#if expanded>
                <div class="listItem">
    <#if (resource.citationRecord && resource.resourceType != 'PROJECT')>
   			<span class='cartouche' title="Citation only; this record has no attached files.">Citation</span></#if>
		    <@common.cartouch resource true><@listCreators resource/></@common.cartouch>  
                <@view.unapiLink resource  />
                <#if showProject && resource.resourceType != 'PROJECT'>
                <p class="project">${resource.project.title}</p>
                </#if>
                <#if resource.description?has_content && !resource.description?starts_with("The information in this record has been migrated into tDAR from the National Archaeological Database Reports Module") >
                    <p class="abstract">
                        <@common.truncate resource.description!"No description specified." 500 />
                    </p>
                </#if>
                <br/>
                </div>
            </#if>
            </${itemTag}>
        <#local first=false/>
     </#if>
    </#list>
  </${listTag}>
  </#if>
</#macro>

<#macro searchResultTitleSection result titleTag>
    <#local titleCssClass="search-result-title-${result.status!('ACTIVE')}" />
	<!-- <h3><a href="">Casa Grande Ruins National Monument, A Centennial History of the First Prehistoric Reserve, 1892-1992</a></h3> -->
    <#if titleTag?has_content>
        <${titleTag} class="${titleCssClass}">
    </#if>
    <a class="resourceLink" href="<@s.url value="/${result.urlNamespace}/${result.id?c}"/>"><#rt>
        ${result.title!"No Title"}<#t>
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
            <#if !useListItem><img src='<@s.url value="/images/desaturated/bookmark.png"/>' alt='bookmark(unavailable)' title='Deleted items cannot be bookmarked.' /><#t></#if>
            <#if showLabel>
                <span class="disabled" title='Deleted items cannot be bookmarked.'>bookmark</span><#t>
            </#if>
        <#elseif bookmarkedResourceService.isAlreadyBookmarked(_resource, authenticatedUser)>
            <a href="<@s.url value='/resource/removeBookmark' resourceId='${_resource.id?c}'/>" class="bookmark" onclick='removeBookmark(${_resource.id?c}, this); return false;'>
                <#if !useListItem><img src='<@s.url value="/images/bookmark.gif"/>'/><#t></#if>
                <#if showLabel>
                    <span class="bookmark">un-bookmark</span><#t>
                </#if>
            </a><#t>
        <#else>
            <a href="<@s.url value='/resource/bookmark' resourceId='${_resource.id?c}'/>" onclick='bookmarkResource(${_resource.id?c}, this); return false;'>
                <#if !useListItem><img src='<@s.url value="/images/unbookmark.gif"/>'/><#t></#if>
                <#if showLabel>
                    <span class="bookmark"> bookmark</span><#t>
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
