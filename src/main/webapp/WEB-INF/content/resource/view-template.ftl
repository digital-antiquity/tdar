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
	<#if resource.resourceType.dataTableSupported && editable>
	    <#assign disabled = resource.dataTables?size==0 />
	    <@view.makeLink "dataset" "columns" "table metadata" "columns" current true disabled "hidden-tablet hidden-phone"/>
	    <@view.makeLink "dataset" "columns" "metadata" "columns" current true disabled "hidden-desktop"/>
    </#if>
	
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

<h1 itemprop="name" class="view-page-title">${resource.title!"No Title"}</h1>
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
        <@view.browse resource.copyrightHolder "copyrightHolder" />
        </p>
    </#if>
</p>

<p class="visible-phone"><a href="#sidebar-right">&raquo; Downloads &amp; Basic Metadata</a></p>
<hr class="dbl">

<h2>Summary</h2>
<p itemprop="description">
  <#assign description = resource.description!"No description specified."/>
  <#noescape>
    ${(description)?html?replace("[\r\n]++","</p><p>","r")}
  </#noescape>
</p>

<hr />

<#if resource.url! != ''>
    <p><strong>URL:</strong><a itemprop="url" href="${resource.url?html}" title="${resource.url?html}"><@view.truncate resource.url?html 80 /></a></p><br/>
</#if>


<#if local_.afterBasicInfo?? && local_.afterBasicInfo?is_macro>
	<@local_.afterBasicInfo />
</#if>

<#if ( resource.hasBrowsableImages && resource.visibleFilesWithThumbnails?size > 0)>
	<@view.showcase />
	<br/>
	<hr/>
</#if>

<#if resource.resourceType.dataTableSupported>
	<#if (dataset.dataTables?has_content)>
		<#if resource.viewable && authenticated >
			<h3>Browse the Data Set</h3>
			
			<#if (dataset.dataTables?size > 1)>
			<form>
			    <label for="table_select">Choose Table:</label>
			    <select id="table_select" name="dataTableId" onChange="window.location =  '?dataTableId=' + $(this).val()">
			    <#list dataset.dataTables as dataTable_>
			      <option value="${dataTable_.id?c}" <#if dataTable_.id == dataTable.id>selected </#if>
			      >${dataTable_.displayName}</option>
			    </#list>
			    </select>
			</form>
			</#if>
			
			<p><@view.embargoCheck /></p>
			
			<div class="row">
			    <div class="span9">
			        <table id="dataTable" class="table tableFormat table-striped table-bordered" ></table>
			    </div>
			</div>
		</#if>
		
		<h3>Data Set Structure</h3>
		<div class="row">
		    <div class="span3"><span class="columnSquare measurement"></span> Measurement Column</div>
		    <div class="span3"><span class="columnSquare count"></span>Count Column</div>
		    <div class="span3"><span class="columnSquare coded"></span>Coded Column</div>
		</div>
		<div class="row">
		    <div class="span3"><span class="columnSquare mapped"></span>Mapping Column</div>
		    <div class="span6"><span class="columnSquare integration"></span>Integration Column (has Ontology)</div>
		</div>
		
		<#list dataset.dataTables as dataTable>
		 <h4>Table Information: <span>${dataTable.displayName}</span></h4>
		     <table class="tableFormat table table-striped table-bordered">
		        <thead class='highlight'>
		         <tr>
		         <th class="guide">Column Name</th>
		         <th>Data Type</th>
		         <th>Type</th>
		         <th>Category</th>
		         <th>Coding Sheet</th>
		         <th>Ontology</th>
		         </tr>
		         </thead>
		            <#list dataTable.dataTableColumns?sort_by("sequenceNumber") as column>
		            <tr>
		                <#assign typeLabel = ""/>
		                <#if column.measurementUnit?has_content><#assign typeLabel = "measurement"/></#if>
		                <#if column.defaultCodingSheet?has_content><#assign typeLabel = "coded"/></#if>
		                <#if column.defaultOntology?has_content><#assign typeLabel = "integration"/></#if>
		                <#if column.columnEncodingType?has_content && column.columnEncodingType == 'COUNT'><#assign typeLabel = "count"/></#if>
		                <#if column.mappingColumn?has_content && column.mappingColumn ><#assign typeLabel = "mapped"/></#if>
		                <td class="guide" nowrap><span class="columnSquare ${typeLabel}"></span><b>
		                    ${column.displayName}
		                </b> </td>
		                 <td><#if column.columnDataType??>${column.columnDataType.label}&nbsp;</#if></td>
		                <td><#if column.columnEncodingType??>${column.columnEncodingType.label}</#if>
		                <#if column.measurementUnit??> (${column.measurementUnit.label})</#if> </td>
		                <td>
		                <#if column.categoryVariable??>
		                <#if column.categoryVariable.parent??>
		                ${column.categoryVariable.parent} :</#if> ${column.categoryVariable}
		                <#else>uncategorized</#if> </td>
		                <td>
		                    <#if column.defaultCodingSheet??>
		                    <a href="<@s.url value="/coding-sheet/${column.defaultCodingSheet.id?c}" />">
		                    ${column.defaultCodingSheet.title!"no title"}</a>
		                    <#else>none</#if>
		                </td><td>
		                <#if column.defaultOntology?? >
		                <a href="<@s.url value="/ontology/${column.defaultOntology.id?c}"/>">
		                    ${column.defaultOntology.title!"no title"}</a>
		                <#else>none</#if>
		                </td>
		            </tr>
		            </#list>
		         </table>
		 
		</#list>
	</#if>
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
                        <li><strong>Journal</strong><br>${resource.journalName}
                            <!-- issue -->
                            <#if resource.journalNumber?has_content> (${resource.journalNumber}) </#if>
                        </li>
                    </#if>
                    <#if resource.volume?has_content>
	                    <li><strong>Volume</strong><br>${resource.volume}</li>
                    </#if>
                  <#if resource.bookTitle?has_content>
                      <li><strong>Book Title</strong><br>${resource.bookTitle}</li>
                  </#if>
                    <#if resource.numberOfVolumes??>
                        <li><strong>Number of volumes</strong><br>${resource.numberOfVolumes}</li>
                    </#if>
                    <#if resource.edition?has_content>
                    <li><strong>Edition</strong><br><span itemprop="bookEdition">${resource.edition}</span></li>
                    </#if>
                    <#if ((resource.publisher.name)?has_content ||  resource.publisherLocation?has_content)>
                        <li><strong>
                        <#-- label -->
                        <#if resource.documentType?has_content>
                        	${resource.documentType.publisherName}
                        <#else>
                        Publisher
                        </#if></strong><br>
                        	<#if resource.publisher?has_content><span itemprop="publisher"><@view.browse creator=resource.publisher /></span></#if> 
                            <#if resource.degree?has_content>${resource.degree.label}</#if>
                            <#if resource.publisherLocation?has_content> (${resource.publisherLocation}) </#if>
                        </li>
                    </#if>
                    <#if resource.isbn?has_content>
                        <li><strong>ISBN</strong><br><span itemprop="isbn">${resource.isbn}</span></li>
                    </#if>
                    <#if resource.issn?has_content>
                        <li><strong>ISSN</strong><br><span itemprop="issn">${resource.issn}</span></li>
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
    <#if resource.resourceType.dataTableSupported>
	<#if (dataset.dataTables?has_content)>
    jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages =3;
        $.extend( $.fn.dataTableExt.oStdClasses, {
        "sWrapper": "dataTables_wrapper form-inline"
    } );
    
//        sDom:'<"datatabletop"ilrp>t<>', //omit the search box
    var options = { 
        "sAjaxDataProp":"results.results",
        "sDom": "<'row'<'span6'l><'span3'>r>t<'row'<'span4'i><'span5'p>>",
        "bProcessing": true,
        "bServerSide":true,
        "bScrollInfinite": false,
        "bScrollCollapse": true,
        tableSelector: '#dataTable',
        sPaginationType:"bootstrap",
        sScrollX: "100%",  
        //turn off vertical scrolling since we're paging (feels weird to advance through records using two mechanisms)
        "sScrollY": "",
        "aoColumns":[
                 <#assign offset=0>
                 <#if viewRowSupported>
                      { "bSortable" : false,
                        "sName" : "id_row_tdar", 
                        "sTitle" : '<i class="icon-eye-open  icon-white"></i>',
                        "fnRender": function(obj) {
                           return '<a href="/${resource.urlNamespace}/view-row?id=${resource.id?c}&dataTableId=${dataTable.id?c}&rowId=' + obj.aData[${offset}] + '" title="View row as page..."><i class="icon-list-alt"></i></a></li>';
                         } 
                      },
                      <#assign offset=1>
                 </#if>
                 <#list dataTable.dataTableColumns?sort_by("sequenceNumber") as column>
                    <#if column.visible?? && column.visible>
                    { "bSortable": false,
                       "sName" : "${column.jsSimpleName?js_string}", 
                       "sTitle" : "${column.displayName?js_string}",
                       "fnRender": function(obj) {
                           var val = obj.aData[${column_index?c} + ${offset}];
                           var str = htmlEncode(val);
                           return str;
                           }  
                     }<#if column_has_next >,</#if>
                     </#if>
                 </#list>
           ],
           "sAjaxSource": "<@s.url value="/datatable/browse?id=${dataTable.id?c}" />"
    };
    registerLookupDataTable(options);    
    </#if>
    </#if>
	<#if local_.localJavascript?? && local_.localJavascript?is_macro>
		<@local_.localJavascript />
	</#if>

});
</script>
</#escape>