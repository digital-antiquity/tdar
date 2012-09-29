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


<#-- FIXME: displayable flag is a hack, should be able to have the macro figure out
the size of the iterable collection.. improve this when we get the chance 
-->
<#macro informationResources iterable="#submittedProject.sortedInformationResources" editable=false bookmarkable=true displayable=true title="Resources" yours=false showTitle=true showProject=true>
      <#if displayable>
      <#if showTitle><b>${title}</b></#if>
      <ol class='resource-list'>
      <@s.iterator value=iterable var='informationResource'>
      <#assign titleCssClass="search-result-title-${informationResource.status.name().toLowerCase()}" />

      <#if administrator || informationResource.status.name() == 'ACTIVE' || (editable && informationResource.status.name() == 'DRAFT')>
      <li class='listItem'>
        <h5 class="${titleCssClass}"><a href="<@s.url value='/${informationResource.urlNamespace}/view' resourceId='${informationResource.id?c}'/>">
        <#-- FIXME: is this check necessary?  All IRs should have a non-zero length
        title, yes? -->
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
              <#if informationResource.dateCreated?? && informationResource.dateCreated.length() &gt; 0 >
              (${informationResource.dateCreated!"" })
              </#if>
          </p>
          <#if showProject && informationResource.resourceType != 'PROJECT'>
          <p class="project">
             ${informationResource.project.title}
          </p>
          </#if>
                <p class="abstract">
                    <#if (informationResource.description?? && (informationResource.description.length())?int > 500)>
                    <#assign trunc = informationResource.description?index_of(".",480) >
                        ${informationResource.description?substring(0,trunc+1) + ".. "}
                        <!-- <a href="<@s.url value='/${informationResource.urlNamespace}/view' resourceId='${informationResource.id?c}'/>">read more</a>  --> 
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
      </@s.iterator>
      </ol>
      </#if>
      <#nested>
</#macro>

<#macro bookmark _resource showLabel=true>
  <#if sessionData?? && sessionData.authenticated && (_resource.status.name().equalsIgnoreCase('active') )>
    <#if bookmarkedResourceService.isAlreadyBookmarked(_resource, authenticatedUser)>
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

