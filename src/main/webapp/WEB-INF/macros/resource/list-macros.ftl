<#include "navigation-macros.ftl">
<#import "view-macros.ftl" as view>

<#-- 
Convert long string to shorter string with ellipses. If you pass it anything fancy (like 
html markup) you will probably not like the results
-->
<#-- FIXME: abbr isn't a perfect name, but sorta fits. feel free to replace w/ a more appropriate one -->
<#macro abbr maxlen=80>
    <#assign content><#nested></#assign>
    <#if (content?length > maxlen)>
        ${content?substring(0,maxlen-3)}...
    <#else>
        ${content}
    </#if>
</#macro>


<#macro listResources resources_ sortfield='PROJECT' editable=false bookmarkable=true  >
  <#assign prev =""/>
  <#assign first = true/>
    <#list resources_ as resource>
    <#assign key = "" />
    <#assign defaultKeyLabel="No Project"/>
    <#if sortfield?contains('RESOURCE_TYPE') || sortfield?contains('PROJECT')>
      <#if sortfield?contains('RESOURCE_TYPE')>
        <#assign key = resource.resourceType.plural />
        <#assign defaultKeyLabel="No Resource Type"/>  
      </#if>
      <#if sortfield?contains('PROJECT')>
        <#assign defaultKeyLabel="No Project"/>
        <#if resource.project??>
          <#assign key = resource.project.titleSort />
        <#else>
          <#if resource.resourceType == 'PROJECT'>
            <#assign key = resource.titleSort />
          </#if>
        </#if>
      </#if>
      <#if first || prev != key && key != ''>
       <#if prev != ''></ul></#if>
        <h3><#if key?? && key != ''>${key}<#else>${defaultKeyLabel}</#if></h3>
        <ul>
      </#if>
      <#assign prev=key />
    </#if>
      <#if administrator || resource.active || (editable && resource.draft)>
         <li><a href="<@s.url value="/${resource.urlNamespace}/${resource.id?c}"/>">${resource.title}</a>  <#if resource.draft><span class='cartouche'>DRAFT</span></#if>                               <@bookmark resource false/>
         </li>
     </#if>
     <#assign first=false/>
    </#list>
  </ul>
</#macro>


<#-- FIXME: displayable flag is a hack, should be able to have the macro figure out
the size of the iterable collection.. improve this when we get the chance 
-->
<#macro informationResources iterable="#submittedProject.sortedInformationResources" editable=false bookmarkable=true displayable=true title="Resources" yours=false showTitle=true showProject=true>
  <#if displayable>
  <#assign test><@informationResourcesInternal iterable=iterable editable=editable bookmarkable=bookmarkable displayable=displayable title=title yours=yours showTitle=showTitle showProject=showProject /></#assign>
  <#if test?? && test?contains("<li") >
    <#if showTitle><b>${title}</b></#if>
      <ol class='resource-list'>
        ${test}
      </ol>
    </#if>
  </#if>
</#macro>

<#macro informationResourcesInternal iterable="#submittedProject.sortedInformationResources" editable=false bookmarkable=true displayable=true title="Resources" yours=false showTitle=true showProject=true>
      <@s.iterator value=iterable var='informationResource'>
      
      <#if informationResource??>
      <#assign stat='ACTIVE'/>
      <#if informationResource.status??>
        <#assign stat>${informationResource.status.name()}</#assign>      
      </#if>
      <#assign titleCssClass="search-result-title-${stat?lower_case}" />

      <#if administrator || informationResource.active || (editable && informationResource.draft)>
      <li class='listItem'>
        <h5 class="${titleCssClass}"><a href="<@s.url value='/${informationResource.urlNamespace}/view'><@s.param name="id" value="${informationResource.id?c}"/></@s.url>">
        <#-- FIXME: is this check necessary?  All IRs should have a non-zero length
        title, yes? -->
        <span class="luceneScore">${informationResource.score}</span>
        <#if informationResource.title == "">
        No Title Provided for this Resource
                <#else>
                   ${informationResource.title!"No Title Provided for this Resource"}
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
              <#assign showSubmitter=true/>
              <span class="authors">
                <@s.iterator value="#informationResource.primaryCreators" var="creatr" status="status" >
                  <#assign showSubmitter=false/>
                  ${creatr.creator.properName}<@s.if test="!#status.last">,</@s.if><@s.else>.</@s.else>
                </@s.iterator>
              </span>
    
              <span class="editors">
                <@s.iterator value="#informationResource.editors" var="creatr" status="status" >
                  <#assign showSubmitter=false/>
                  <@s.if test="#status.first"><span class="editedBy">Edited by:</span></@s.if>
                  ${creatr.creator.properName}<@s.if test="!#status.last">,</@s.if><@s.else>.</@s.else>
                </@s.iterator>
              </span>
    
              <#if showSubmitter>
                <span class="creators"> 
                  <span class="createdBy">Uploaded by:</span> ${informationResource.submitter.properName}
                </span>
              </#if>
              <#if informationResource.dateCreated?? && informationResource.dateCreated &gt; 0 >
              (${informationResource.dateCreated?c!"" })
              </#if>
          </p>
          <#if showProject && informationResource.resourceType != 'PROJECT'>
          <p class="project">
             ${informationResource.project.title}
          </p>
          </#if>
                <p class="abstract">
                    <#local dots = "..." />
                    <#if (informationResource.description?? && (informationResource.description.length())?int > 500)>
                    <#local trunc = informationResource.description?index_of(".",480) >
                        <#if trunc == -1><#local trunc = informationResource.description?index_of(" ",480) />
                            <#if trunc == -1><#local trunc = 480 /></#if>   
                        <#else>
                            <#local dots="..">
                        </#if>
                        ${informationResource.description?substring(0,trunc+1) + dots}
                        <!-- <a href="<@s.url value='/${informationResource.urlNamespace}/view'><@s.param name="id" value="${informationResource.id?c}"/></@s.url>">read more</a>  --> 
                    <#elseif (informationResource.description?? && (informationResource.description.length())?int > 0)>
                        ${informationResource.description!"No description specified."}
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
</#macro>

<#macro bookmark _resource showLabel=true>
  <#if sessionData?? && sessionData.authenticated>
    <#if _resource.deleted?? && _resource.deleted>
        <img src='<@s.url value="/images/desaturated/bookmark.png"/>' alt='bookmark(unavailable)' title='Deleted items cannot be bookmarked.' />
        <span class="disabled" title='Deleted items cannot be bookmarked.'>bookmark</span>
    <#elseif bookmarkedResourceService.isAlreadyBookmarked(_resource, authenticatedUser)>
      <a href="<@s.url value='/resource/removeBookmark' resourceId='${_resource.id?c}'/>" onclick='removeBookmark(${_resource.id?c}, this); return false;'>
          <img src='<@s.url value="/images/bookmark.gif"/>'/>
            <#if showLabel>
        <span class="bookmark">un-bookmark</span>
      </#if></a>
    <#else>
      <a href="<@s.url value='/resource/bookmark' resourceId='${_resource.id?c}'/>" onclick='bookmarkResource(${_resource.id?c}, this); return false;'>
      <img src='<@s.url value="/images/unbookmark.gif"/>'/>  <#if showLabel>
        <span class="bookmark"> bookmark</span>
  </#if>
</a>
    </#if>
  </#if>
</#macro>

