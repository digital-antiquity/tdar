<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<@view.htmlHeader resourceType="document">
<meta name="lastModifiedDate" content="$Date$"/>
<@view.googleScholar />
</@view.htmlHeader>
<@view.toolbar "${resource.urlNamespace}" "view" />

<@view.projectAssociation resourceType="document" />

<@view.infoResourceBasicInformation >
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
    	<tr><td nowrap><b>Journal:</b></td><td>${document.journalName}<#if document.volume?? && document.volume != ''>, ${document.volume}
    </#if>
    <!-- issue -->
    <#if document.journalNumber?? && document.journalNumber != ''> (${document.journalNumber}) </#if>
    	
    	</td></tr>
	</#if>
  <#if document.bookTitle?? && document.bookTitle!= ''>
  <tr><td nowrap><b>Book Title:</b></td><td>${document.bookTitle}</td></tr>
  </#if>
	<#if document.numberOfVolumes??>
	<tr><td nowrap><b>Number of volumes:</b></td><td>${document.numberOfVolumes}</td></tr>
	</#if>
    <#if (document.startPage?? && document.startPage!='') || (document.endPage?? && document.endPage!='') || (document.numberOfPages?? && document.numberOfPages > 0)>
     <tr><td nowrap><b>Pages:</b></td><td>
        ${document.startPage!} <#if document.startPage?? && document.endPage?? && document.startPage!='' && document.endPage !=''>-</#if> ${document.endPage!}
    </#if>
      <#if document.numberOfPages?? > 
      <#if (document.startPage?? && document.startPage!='') || (document.endPage?? && document.endPage!='') >(</#if>
        ${document.numberOfPages}
      <#if (document.startPage?? && document.startPage!='') || (document.endPage?? && document.endPage!='') >)</#if></#if>
    </td></tr>    
	<#if document.edition?? && document.edition!= ''>
	<tr><td nowrap><b>Edition:</b></td><td>${document.edition}</td></tr>
	</#if>
	<#if (document.publisher?? && document.publisher != '' ||  document.publisherLocation?? && document.publisherLocation != '')>
	<tr><td nowrap><b>Publisher:</b></td><td>${document.publisher}
		<#if document.publisherLocation?? && document.publisherLocation != ''> (${document.publisherLocation}) </#if></td></tr>
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
	<tr><td nowrap><b>Document type:</b></td><td>${document.documentType.label}</td></tr>
</@view.infoResourceBasicInformation>

<@view.sharedViewComponents document />
</#escape>