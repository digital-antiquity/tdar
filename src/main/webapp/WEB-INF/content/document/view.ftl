<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<@view.htmlHeader resourceType="document">
<meta name="lastModifiedDate" content="$Date$"/>
<@view.googleScholar />
</@view.htmlHeader>
<@view.toolbar "${resource.urlNamespace}" "view" />

<@view.projectAssociation resourceType="document" />

<@view.infoResourceBasicInformation >
	<#if document.seriesName?has_content>
	<tr><td nowrap><b>Series name:</b></td><td>${document.seriesName}</td></tr>
	</#if>
	<#if document.seriesNumber?has_content>
	<tr><td nowrap><b>Series number:</b></td><td>${document.seriesNumber}</td></tr>
	</#if>
	<#if document.journalName?has_content>
    	<tr><td nowrap><b>Journal:</b></td><td>${document.journalName}<#if document.volume?has_content>, ${document.volume}
    </#if>
    <!-- issue -->
    <#if document.journalNumber?has_content> (${document.journalNumber}) </#if>
    	</td></tr>
	</#if>
  <#if document.bookTitle?has_content>
  <tr><td nowrap><b>Book Title:</b></td><td>${document.bookTitle}</td></tr>
  </#if>
	<#if document.numberOfVolumes??>
	<tr><td nowrap><b>Number of volumes:</b></td><td>${document.numberOfVolumes}</td></tr>
	</#if>
    <#if (document.startPage?has_content) || (document.endPage?has_content) || (document.totalNumberOfPages?has_content)>
     <tr><td nowrap><b>Pages:</b></td><td>
        ${document.startPage!} <#if document.startPage?has_content && document.endPage?has_content>-</#if> ${document.endPage!}
    </#if>
      <#if document.totalNumberOfPages?? > 
      <#if (document.startPage?has_content) || (document.endPage?has_content) >(</#if>
        ${document.totalNumberOfPages}
      <#if (document.startPage?has_content) || (document.endPage?has_content) >)</#if></#if>
    </td></tr>    
	<#if document.edition?has_content>
	<tr><td nowrap><b>Edition:</b></td><td>${document.edition}</td></tr>
	</#if>
	<#if (document.publisher?has_content ||  document.publisherLocation?has_content)>
	<tr><td nowrap><b>Publisher:</b></td><td>${document.publisher} 
	<#if document.degree?has_content>${document.degree.label}</#if>
		<#if document.publisherLocation?has_content> (${document.publisherLocation}) </#if></td></tr>
	</#if>
    <#if document.isbn?has_content>
    <tr><td nowrap><b>ISBN:</b></td><td>${document.isbn}</td></tr>
    </#if>
    <#if document.issn?has_content>
    <tr><td nowrap><b>ISSN:</b></td><td>${document.issn}</td></tr>
    </#if>
    <#if document.doi?has_content>
    <tr><td nowrap><b>DOI:</b></td><td>${document.doi}</td></tr>
    </#if>
	<tr><td nowrap><b>Document type:</b></td><td>${document.documentType.label}</td></tr>
</@view.infoResourceBasicInformation>

<@view.sharedViewComponents document />
</#escape>