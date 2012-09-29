<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<@view.htmlHeader resourceType="document">
<meta name="lastModifiedDate" content="$Date$"/>
<@view.googleScholar />
</@view.htmlHeader>
<@view.toolbar "${resource.urlNamespace}" "view" />

<@view.projectAssociation resourceType="document" />

<@view.infoResourceBasicInformation >
	<tr><td nowrap><b>Document type:</b></td><td>${document.documentType.label}</td></tr>
<!--
	<#if document.manuscriptDescription??>
 <tr><td nowrap><b>Manuscript type/description:</b></td><td>${document.manuscriptDescription}</td></tr>
	</#if>-->
	<#if document.seriesName?? && document.seriesName != ''>
	<tr><td nowrap><b>Series name:</b></td><td>${document.seriesName}</td></tr>
	</#if>
	<#if document.seriesNumber?? && document.seriesNumber != ''>
	<tr><td nowrap><b>Series number:</b></td><td>${document.seriesNumber}</td></tr>
	</#if>
  <#if document.journalName?? && document.journalName != ''>
  <tr><td nowrap><b>Journal name:</b></td><td>${document.journalName}</td></tr>
  </#if>
  <#if document.bookTitle?? && document.bookTitle!= ''>
  <tr><td nowrap><b>Book Title:</b></td><td>${document.bookTitle}</td></tr>
  </#if>
	<#if document.journalNumber?? && document.journalNumber != ''>
	<tr><td nowrap><b>Journal number:</b></td><td>${document.journalNumber}</td></tr>
	</#if>
	<#if document.volume?? && document.volume != ''>
	<tr><td nowrap><b>Volume:</b></td><td>${document.volume}</td></tr>
	</#if>
	<#if document.numberOfVolumes??>
	<tr><td nowrap><b>Number of volumes:</b></td><td>${document.numberOfVolumes}</td></tr>
	</#if>
    <#if document.numberOfPages?? >
	<tr><td nowrap><b>Number of pages:</b></td><td>${document.numberOfPages}</td></tr>
    </#if>
    <#if document.startPage??||document.endPage??>
    <tr><td nowrap><b>Start page:</b></td><td>${document.startPage!}</td></tr>
    <tr><td nowrap><b>End page:</b></td><td>${document.endPage!}</td></tr>    
    </#if>
	<#if document.edition?? && document.edition!= ''>
	<tr><td nowrap><b>Edition:</b></td><td>${document.edition}</td></tr>
	</#if>
	<#if document.publisher?? && document.publisher != ''>
	<tr><td nowrap><b>Publisher:</b></td><td>${document.publisher}</td></tr>
	</#if>
	<#if document.publisherLocation?? && document.publisherLocation != ''>
	<tr><td nowrap><b>Publisher location:</b></td><td>${document.publisherLocation}</td></tr>
	</#if>
    <#if document.isbn?? && document.isbn!=''>
    <tr><td nowrap><b>ISBN:</b></td><td>${document.isbn}</td></tr>
    </#if>
    <#if document.issn?? && document.issn != ''>
    <tr><td nowrap><b>ISSN:</b></td><td>${document.issn}</td></tr>
    </#if>
    <#if document.doi?? && document.doi != ''>
    <tr><td nowrap><b>DOI:</b></td><td>${document.doi}</td></tr>
    </#if>
	<#if document.url?? && document.url != ''>
  <tr><td nowrap><b>URL:</b></td><td>
	   <a href="${document.url}">${document.url}</a>
	   </td>
  </tr>
  </#if>
</@view.infoResourceBasicInformation>

<@view.uploadedFileInfo />
<@view.keywords />
<@view.spatialCoverage />
<@view.temporalCoverage />
<@view.resourceProvider />
<@view.indvidualInstitutionalCredit />
<@view.unapiLink document />

<@view.infoResourceAccessRights />
