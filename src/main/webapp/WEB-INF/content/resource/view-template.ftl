<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/content/${namespace}/view.ftl" as local_ />
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

  <head>
    <title>${resource.title}</title>
	<meta name="lastModifiedDate" content="$Date$"/>
	
	<#if includeRssAndSearchLinks??>
		<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
		<#assign rssUrl>/search/rss?groups[0].fieldTypes[0]=PROJECT&groups[0].projects[0].id=${project.id?c}&groups[0].projects[0].name=${(project.name!"untitled")?url}</#assign>
		<@search.rssUrlTag url=rssUrl />
		<@search.headerLinks includeRss=false />
	</#if>
	
	
	<#if resource.title?? && resource.resourceCreators?? && resource.date??>
	    <meta name="citation_title" content="${resource.title?html}">
	    <#list resource.primaryCreators?sort_by("sequenceNumber") as resourceCreator>
	        <meta name="citation_author" content="${resourceCreator.creator.properName?html}">
	    </#list>    
	    <meta name="citation_date" content="${resource.date?c!''}">
	    <#if resource.dateCreated??><meta name="citation_online_date" content="${resource.dateCreated?date?string('yyyy/MM/dd')}"></#if>
	    <#list resource.informationResourceFiles as irfile>
	        <#if (irfile.viewable) && irfile.latestPDF?has_content>
	        <meta name="citation_pdf_url" content="<@s.url value='/filestore/${irfile.latestPDF.id?c}/get'/>">
	        </#if>
	    </#list>
	    <#assign publisherFieldName = "DC.publisher" />
	    <#if resource.resourceType == 'DOCUMENT'>
	         <#if document.documentType == 'CONFERENCE_PRESENTATION'>
	           <#assign publisherFieldName="citation_conference_title" />
	         <#elseif document.documentType == 'JOURNAL_ARTICLE' && document.journalName??>
	            <meta name="citation_journal_title" content="${document.journalName?html}">
	        </#if>
	        <#if document.volume?has_content><meta name="citation_volume" content="${document.volume}"></#if>
	        <#if document.journalNumber?has_content><meta name="citation_issue" content="${document.journalNumber}"></#if>
	        <#if document.issn?has_content><meta name="citation_issn" content="${document.issn}"></#if>
	        <#if document.isbn?has_content><meta name="citation_isbn" content="${document.isbn}"></#if>
	        <#if document.startPage?has_content><meta name="citation_firstpage" content="${document.startPage}"></#if>
	        <#if document.endPage?has_content><meta name="citation_lastpage" content="${document.endPage}"></#if>
	        <#if document.documentType == 'THESIS'>
	              <#assign publisherFieldName="citation_dissertation_institution" />
	       </#if>
	    </#if>
	   <#if resource.publisher?has_content>
	     <meta name="${publisherFieldName}" content="${resource.publisher.name?html}" >
	   </#if>
	
	<#else>
	    <!--required google scholar fields not available - skipping meta tags -->
	</#if>

	<@view.canonical resource />
  </head>


<@view.toolbar "${resource.urlNamespace}" "view">
	<#if local_.toolbarAdditions?? && local_.toolbarAdditions?is_macro>
		<@local_.toolbarAdditions />
	</#if>
</@view.toolbar>


<div id="datatable-child" style="display:none">
    <p class="">
        You have successfully updated the page that opened this window.  What would you like to do now?
    </p>
</div>

<#if resource.informationResourceFiles?has_content>
	<#assign files = resource.filesWithFatalProcessingErrors />
	<#if editor>
		<#assign files = resource.filesWithProcessingErrors />
	</#if>
	<#if (files?size > 0 ) && authenticatedUser??  && (administrator || editable) >
	<div class="alert alert-error">
	<h3>The following Files have Processing Errors</h3>
	<ul>	<#list files as file>
		<li>${file.fileName} - ${file.errorMessage!""}</li>
		</#list>
	</ul>
	<br/>
	</div>
</#if>
</#if>

<@view.pageStatusCallout />

<h1 class="view-page-title">${resource.title!"No Title"}</h1>
<#if resource.project?? && resource.project.id?? && resource.project.id != -1>

<div id="subtitle"> 
    <p>Part of the  
  <#if resource.project.active || editable>
    <a href="<@s.url value='/project/view'><@s.param name="id" value="${resource.project.id?c}"/></@s.url>">${resource.project.coreTitle}</a>
  <#else>
  ${resource.project.coreTitle}
  </#if>
        <#if resource.project.draft>(DRAFT)</#if> project
</p></div>
</#if>

<#if editor>
<div data-spy="affix" class="affix no-print adminbox rotate-90"><a href="<@s.url value="/${resource.urlNamespace}/${resource.id?c}/admin"/>">ADMIN</a></div>
</#if>

<p class="meta">
    <@view.showCreatorProxy proxyList=authorshipProxies />
    <#if resource.date?has_content && resource.date != -1 >
	    <@view.kvp key="Year" val=resource.date?c />
    </#if>

    <#if copyrightMandatory && resource.copyrightHolder?? >
        <strong>Primary Copyright Holder:</strong>
        <@view.browse resource.copyrightHolder />
        </p>
    </#if>
</p>

<p class="visible-phone"><a href="#sidebar-right">&raquo; Downloads &amp; Basic Metadata</a></p>
<hr class="dbl">

<h2>Summary</h2>
<p>
  <#assign description = resource.description!"No description specified."/>
  <#noescape>
    ${(description)?html?replace("[\r\n]++","</p><p>","r")}
  </#noescape>
</p>

<hr />

<#if resource.url! != ''>
    <p><strong>URL:</strong><a href="${resource.url?html}" title="${resource.url?html}"><@view.truncate resource.url?html 80 /></a></p><br/>
</#if>


<#if local_.afterBasicInfo?? && local_.afterBasicInfo?is_macro>
	<@local_.afterBasicInfo />
</#if>


<h2>Cite this Record</h2>
    <div class="citeMe">
        <p class="sml">
        ${resource.title}. <#if resource.formattedAuthorList?has_content>${resource.formattedAuthorList}.</#if> 
         <#if resource.formattedSourceInformation?has_content>${resource.formattedSourceInformation}</#if> (${siteAcronym} ID: ${resource.id?c})<br/>
        <#if resource.externalId?has_content>${resource.externalId}
        <#elseif resource.lessThanDayOld && !resource.citationRecord>
        <br/>
            <em>Note:</em>A DOI will be generated in the next day for this resource.
        </#if>
        </p>
    </div>
    <hr />        

        <#if resource.resourceType == 'CODING_SHEET' ||  resource.resourceType == 'ONTOLOGY'>
            <@view.categoryVariables />
        </#if>
        <#if resource.resourceType != 'PROJECT'>
            <#if licensesEnabled?? &&  licensesEnabled>
                <@view.license />
            </#if>
        </#if>

    <@view.coin resource/>
    <#if resource.resourceType == 'PROJECT'>
        <@view.keywords showParentProjectKeywords=false />
    <#else>
        <@view.keywords />
    </#if>
    <@view.temporalCoverage />

    <@view.spatialCoverage />

    <@view.indvidualInstitutionalCredit />

    <@view.resourceAnnotations />
    
    <@view.resourceNotes />

    <#-- <@relatedSimpleItem resource.sourceCitations "Source Citations"/> -->
    <#-- <@relatedSimpleItem resource.relatedCitations "Related Citations"/> -->
    <@view.relatedSimpleItem resource.activeSourceCollections "Source Collections"/>
    <@view.relatedSimpleItem resource.activeRelatedComparativeCollections "Related Comparative Collections" />
    <#if resource.activeSourceCollections?has_content || resource.activeRelatedComparativeCollections?has_content>
         <hr />
     </#if>
    <#-- display linked data <-> ontology nodes -->
    <@view.relatedResourceSection label=resource.resourceType.label />
    

    <@view.unapiLink resource />
    <@view.resourceCollections />
    <@view.additionalInformation resource />
    
    <@view.infoResourceAccessRights />
    


        <div id="sidebar-right" parse="true">
                <i class="${resource.resourceType?lower_case}-bg-large"></i>    
                
                <@view.uploadedFileInfo />

                <h3>Basic Information</h3>

                <p>

                <ul class="unstyled-list">
                    <@view.resourceProvider />
                    <#if resource.seriesName?has_content>
                    <li><strong>Series name</strong><br>${resource.seriesName}</li>
                    </#if>
                    <#if resource.seriesNumber?has_content>
                    <li><strong>Series number</strong><br>${resource.seriesNumber}</li>
                    </#if>
                    <#if resource.journalName?has_content>
                        <li><strong>Journal</strong><br>${resource.journalName}<#if resource.volume?has_content>, ${resource.volume}</#if>
                            <!-- issue -->
                            <#if resource.journalNumber?has_content> (${resource.journalNumber}) </#if>
                        </li>
                    </#if>
                      <#if resource.bookTitle?has_content>
                          <li><strong>Book Title</strong><br>${resource.bookTitle}</li>
                      </#if>
                    <#if resource.numberOfVolumes??>
                        <li><strong>Number of volumes</strong><br>${resource.numberOfVolumes}</li>
                    </#if>
                    </li>
                    <#if resource.edition?has_content>
                    <li><strong>Edition</strong><br>${resource.edition}</li>
                    </#if>
                    <#if ((resource.publisher.name)?has_content ||  resource.publisherLocation?has_content)>
                        <li><strong>
                        <#-- label -->
                        <#if resource.documentType?has_content>
                        	${resource.documentType.publisherName}
                        <#else>
                        Publisher
                        </#if></strong><br>
                        	<#if resource.publisher?has_content>${resource.publisher.name!"Not Specified"}</#if> 
                            <#if resource.degree?has_content>${resource.degree.label}</#if>
                            <#if resource.publisherLocation?has_content> (${resource.publisherLocation}) </#if>
                        </li>
                    </#if>
                    <#if resource.isbn?has_content>
                        <li><strong>ISBN</strong><br>${resource.isbn}</li>
                    </#if>
                    <#if resource.issn?has_content>
                        <li><strong>ISSN</strong><br>${resource.issn}</li>
                    </#if>
                    <#if resource.doi?has_content>
                        <li><strong>DOI</strong><br>${resource.doi}</li>
                    <#elseif resource.externalId?has_content>
                        <li><strong>DOI</strong><br>${resource.externalId}</li>
                    </#if>


                    <#if resource.documentType?has_content>
                    <#if (resource.startPage?has_content) || (resource.endPage?has_content) || (resource.totalNumberOfPages?has_content)>
                    <li>
                        <strong>Pages</strong><br>
						<#if resource.documentType.partOfLargerDocument>
                            ${resource.startPage!} <#if resource.startPage?has_content && resource.endPage?has_content>-</#if> ${resource.endPage!}
						</#if>
                        </#if>
                          <#if resource.totalNumberOfPages?? >
                          <#assign showParen = false/>
                          <#if ((resource.startPage?has_content || resource.endPage?has_content) && !resource.documentType.partOfLargerDocument) >
                          <#assign showParen = true/>
                           </#if>
                          <#if showParen >(</#if>
                            ${resource.totalNumberOfPages}
                          <#if showParen >)</#if>
                       </li>
                    </#if>
                    <li>
                        <strong>Document Type</strong><br>
                        ${resource.documentType.label}
                    </li>
                    </#if>
                    <#if resource.resourceLanguage?has_content>
                    <li>
                        <strong>Language</strong><br>
                        ${resource.resourceLanguage.label}
                    </li>
                    </#if>
                    <#if resource.copyLocation?has_content>
                    <li>
                        <strong>Location</strong><br>
                        ${resource.copyLocation}
                    </li>
                    </#if>
                    <li>
                        <strong>${siteAcronym} ID</strong><br>
                        ${resource.id?c}
                    </li>
                </ul>

            </div>



<#if local_.footer?? && local_.footer?is_macro>
	<@local_.footer />
</#if>


<script type="text/javascript">
$(function() {
    'use strict';
    TDAR.common.initializeView();
	<#if local_.localJavascript?? && local_.localJavascript?is_macro>
		<@local_.localJavascript />
	</#if>

});
</script>

</#escape>