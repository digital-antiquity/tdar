<#escape _untrusted as _untrusted?html>
<#include "navigation-macros.ftl">
<#import "view-macros.ftl" as view>
<#import "common.ftl" as common>
<#-- 
Convert long string to shorter string with ellipses. If you pass it anything fancy (like 
html markup) you will probably not like the results
-->

<#macro listResources resources_ sortfield='PROJECT' editable=false bookmarkable=true  expanded=false listTag='ul' headerTag="h3">
  <#local showProject = false />
  <#local prev =""/>
  <#local first = true/>
  
  <#list resources_ as resource_>
    <#local stat=resource_.status.name()?lower_case />      
    <#local key = "" />
    <#local defaultKeyLabel="No Project"/>
    
    <#if sortfield?contains('RESOURCE_TYPE') || sortfield?contains('PROJECT')>
      <#if sortfield?contains('RESOURCE_TYPE')>
        <#local key = resource_.resourceType.plural />
        <#local defaultKeyLabel="No Resource Type"/>  
      </#if>
      <#if sortfield?contains('PROJECT')>
        <#local defaultKeyLabel="No Project"/>
        <#if resource_.project??>
          <#local key = resource_.project.titleSort />
        <#else>
          <#if resource_.resourceType == 'PROJECT'>
            <#local key = resource_.titleSort />
          </#if>
        </#if>
      </#if>
      <#if first || prev != key && key != ''>
       <#if prev != ''></${listTag}></#if>
        <${headerTag}><#if key?? && key != ''>${key}<#else>${defaultKeyLabel}</#if></${headerTag}>
        <${listTag}>
      </#if>
      <#local prev=key />
    </#if>
  
      <#if resource_.active || (ableToFindDraftResources && resource_.draft) ||
       (editable && resource_.draft) ||  
       (ableToFindFlaggedResources && resource_.flagged) || 
       (ableToFindDeletedResources && resource_.deleted)>
      <!-- NOTE THIS MAY EXPOSE ISSUES IF (A) project is EDITABLE and resource IS NOT -->
         <li>
            <span  class="resource-list-title-${stat}"><#t>
                <a href="<@s.url value="/${resource_.urlNamespace}/${resource_.id?c}"/>">${resource_.title}</a><#t>
                <#if resource_.draft><span class='cartouche'>DRAFT</span></#if><#t>
            </span>
            <@bookmark resource_ false/>
			<#if expanded>
            <div class="listItem">
            <@view.unapiLink resource_  />
              <p>
			<@listCreators resource_/>

              <#if resource_.date?? && resource_.date &gt; 0 >
              (${resource_.date?c!"" })
              </#if>
          </p>
          <#if showProject && resource_.resourceType != 'PROJECT'>
          <p class="project">
             ${resource_.project.title}
          </p>
          </#if>
                <p class="abstract">
                <#if resource_.description??>
                     <@common.truncate resource_.description!"No description specified." 500 />
                <#else>
                    No description specified.
                 </#if>
                </p>
			  <br/>
            </div>
            </#if>
         </li>
     </#if>
     <#local first=false/>
    </#list>
  </${listTag}>
</#macro>


<#macro listCreators resource_>
	  <#assign showSubmitter=true/>
	  <span class="authors">
	    <#list resource_.primaryCreators as creatr>
	      <#assign showSubmitter=false/>
	      ${creatr.creator.properName}<#if creatr__has_next??>,<#else>.</#if>
	    </#list>
	  </span>
	
	  <span class="editors">
	    <#list resource_.editors as creatr>
	      <#assign showSubmitter=false/>
	      <#if creatr_index == 0><span class="editedBy">Edited by:</span></#if>
	      ${creatr.creator.properName}<#if creatr__has_next??>,<#else>.</#if>
	    </#list>
	  </span>
	
	  <#if showSubmitter>
	    <span class="creators"> 
	      <span class="createdBy">Uploaded by:</span> ${resource_.submitter.properName}
	    </span>
	  </#if>
</#macro>

<#-- FIXME: displayable flag is a hack, should be able to have the macro figure out
the size of the iterable collection.. improve this when we get the chance 
-->
<#macro informationResources iterable="#submittedProject.sortedInformationResources" editable=false bookmarkable=true displayable=true title="Resources" yours=false showTitle=true showProject=true>
    <#noescape>
  <#if displayable>
  <#assign test><@informationResourcesInternal iterable=iterable editable=editable bookmarkable=bookmarkable displayable=displayable title=title yours=yours showTitle=showTitle showProject=showProject /></#assign>
  <#if test?? && test?contains("<li") >
    <#if showTitle><b>${title}</b></#if>
      <ol class='resource-list'>
        ${test}
      </ol>
    </#if>
  </#if>
    </#noescape>
</#macro>

<#macro informationResourcesInternal iterable="#submittedProject.sortedInformationResources" editable=false bookmarkable=true displayable=true title="Resources" yours=false showTitle=true showProject=true>
    <#noescape>
      <@s.iterator value=iterable var='informationResource'>
      
      <#if informationResource??>
      <#assign stat='ACTIVE'/>
      <#if informationResource.status??>
        <#assign stat>${informationResource.status.name()}</#assign>      
      </#if>
      <#assign titleCssClass="resource-list-title-${stat?lower_case}" />

      <#if informationResource.active || (editable && informationResource.draft) || 
      		(ableToFindDraftResources && informationResource.draft) || (ableToFindFlaggedResources && informationResource.flagged) || (ableToFindDeletedResources && informationResource.deleted)>
      <li class='listItem'>
        <h5 class="${titleCssClass}"><a href="<@s.url value='/${informationResource.urlNamespace}/view'><@s.param name="id" value="${informationResource.id?c}"/></@s.url>">
        <#-- FIXME: is this check necessary?  All IRs should have a non-zero length
        title, yes? -->
        <span class="luceneScore">${informationResource.score}</span>
        <#if informationResource.title == "">
	        No Title Provided for this Resource
        <#else>
           ${informationResource.title?html}
        </#if>
                </a>&nbsp;
                <span class="typeDesc small"><#if !showTitle>
                <#-- FIXME: push the first part of this conditional into ResourceType -->
                <#if ! ["CODING_SHEET", "ONTOLOGY", "PROJECT"]?seq_contains(informationResource.resourceType) && informationResource.informationResourceFiles.isEmpty()>
                <#assign docType = informationResource.resourceType />
                <#if informationResource.resourceType == "DOCUMENT">
                  <#assign docType = informationResource.documentType />
                </#if>
                  [${docType.label} - Citation]
                <#else>                
                  [${informationResource.resourceType.label}]
                </#if>
                </#if></span>
                                <@bookmark informationResource false/>
            </h5>
            <div class="listItem">
            <@view.unapiLink informationResource />
              <p>
				<@listCreators informationResource />

              <#if (informationResource.date?? && informationResource.date > 0) >
              (${informationResource.date?c!"" })
              </#if>
          </p>
          <#if showProject && informationResource.resourceType != 'PROJECT'>
          <p class="project">
             ${informationResource.project.title?html}
          </p>
          </#if>
                <p class="abstract">
                <#if informationResource.description??>
                     <@common.truncate informationResource.description!"No description specified." 500 />
                <#else>
                    No description specified.
                 </#if>
                </p>
            </div>
            <br/>
      </li>
      </#if>
      </#if>
      </@s.iterator>
      <#nested>
      </#noescape>
</#macro>

<#macro bookmark _resource showLabel=true>
  <#if sessionData?? && sessionData.authenticated>
    <#if _resource.deleted?? && _resource.deleted>
        <img src='<@s.url value="/images/desaturated/bookmark.png"/>' alt='bookmark(unavailable)' title='Deleted items cannot be bookmarked.' /><#t>
        <#if showLabel>
            <span class="disabled" title='Deleted items cannot be bookmarked.'>bookmark</span><#t>
        </#if>
    <#elseif bookmarkedResourceService.isAlreadyBookmarked(_resource, authenticatedUser)>
        <a href="<@s.url value='/resource/removeBookmark' resourceId='${_resource.id?c}'/>" onclick='removeBookmark(${_resource.id?c}, this); return false;'>
            <img src='<@s.url value="/images/bookmark.gif"/>'/><#t>
            <#if showLabel>
                <span class="bookmark">un-bookmark</span><#t>
            </#if>
        </a><#t>
    <#else>
        <a href="<@s.url value='/resource/bookmark' resourceId='${_resource.id?c}'/>" onclick='bookmarkResource(${_resource.id?c}, this); return false;'>
            <img src='<@s.url value="/images/unbookmark.gif"/>'/><#t>
            <#if showLabel>
                <span class="bookmark"> bookmark</span><#t>
            </#if>
        </a><#t>    
    </#if>
  </#if>
</#macro>
</#escape>