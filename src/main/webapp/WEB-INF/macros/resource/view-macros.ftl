<#escape _untrusted as _untrusted?html>
<#-- 
$Id$ 
View freemarker macros
-->
<#-- include navigation menu in edit and view macros -->
<#include "common.ftl">
<#include "navigation-macros.ftl">
<#setting url_escaping_charset='UTF-8'>

<#macro canonical object>
	<link rel="canonical" href="http://${hostName}/${object.urlNamespace}/${object.id?c}" /> 
</#macro>

<#macro displayNode ontologyNode>
    <span style="padding-left:${ontologyNode.numberOfParents * 2}em">${ontologyNode.displayName} 
    <#if ontologyNode.synonyms?has_content>
        (<#t>
      <#list ontologyNode.synonyms as synonym><#t>
        ${synonym}<#t>
        <#if synonym_has_next>, </#if><#t>
      </#list><#t>
      )<#t>
    </#if>
    </span><br>
</#macro>

<#macro ontology sectionTitle="Parsed Ontology Nodes" previewSize=10 triggerSize=15>
<#if resource.sortedOntologyNodesByImportOrder?has_content>
	<#local allNodes = resource.sortedOntologyNodesByImportOrder />
	<#local previewNodes = allNodes />
	<#local collapsedNodes = [] />
	<#local shouldCollapse = (allNodes?size > triggerSize) />
	<#if shouldCollapse >
	    <#local previewNodes = allNodes[0..(previewSize-1)] />
	    <#local collapsedNodes = allNodes[previewSize..] />
	</#if>
	
	<#if (allNodes?size>0)>
	    <h2>${sectionTitle}</h2>
	    <div class="ontology-nodes-container">
		    <div id="ontology-nodes-part1">
		    <#list previewNodes as ontologyNode>
		        <@displayNode ontologyNode />
		    </#list>
		    </div>
		    <#if shouldCollapse>
			    <div id="ontology-nodes-part2" style="display:none">
			    <#list collapsedNodes as ontologyNode>
			        <@displayNode ontologyNode />
			    </#list>
			    </div>
			    
			    <div id='divOntologyShowMore' class="alert">
			        <span>Showing first ${previewSize?c} ontology nodes.</span>
			        <button type="button" class="btn btn-small" id="btnOntologyShowMore">Show all ${resource.ontologyNodes?size?c} nodes...</button>
			    </div>
			    <script type="text/javascript">
				    $(function(){
				        $('#btnOntologyShowMore').click(function() {
				            $('#divOntologyShowMore').hide();
				            $('#ontology-nodes-part2').show();
				            return(false);
				        });
				    });
			    </script>
		    </#if>
		</#if>
		</div>
</#if>
</#macro>

<#macro createFileLink irfile newline=false >
    <#assign version=irfile />
         <#if version.latestUploadedVersion?? >
            <#assign version=version.latestUploadedVersion />        
         </#if>
        <#if (version.viewable)>
          <a href="<@s.url value='/filestore/${version.id?c}/get'/>" onClick="registerDownload('<@s.url value='/filestore/${version.id?c}/get'/>', '${id?c}')" 
    <#if resource.resourceType == 'IMAGE'>target='_blank'</#if>
          title="${version.filename?html}">
              <@truncate version.filename 65 />
          </a><#if newline><br /></#if>
         <#else>
             <@truncate version.filename 65 /> 
         </#if>
         <#if (!version.viewable || !version.informationResourceFile.public )>
            <span class="ui-icon ui-icon-locked" style="display: inline-block"></span>
         </#if>
        (<@convertFileSize version.fileLength />)
        <@downloadCount version />
</#macro>

<#macro adminFileActions>
  <#if (resource.totalNumberOfFiles?has_content && resource.totalNumberOfFiles > 0)>
        <#if ableToReprocessDerivatives>
        <h2> Admin File Actions</h2>
        <ul>
            <#if resource.resourceType=='DATASET'>
                <li><a href="<@s.url value='/${resource.urlNamespace}/reimport?id=${resource.id?c}' />">Reimport this dataset</a></li>
                <li><a href="<@s.url value='/${resource.urlNamespace}/retranslate?id=${resource.id?c}' />">Retranslate this dataset</a></li>
            </#if>
            <li><a href="<@s.url value='/${resource.urlNamespace}/reprocess'><@s.param name="id" value="${resource.id?c}"/></@s.url>">Reprocess all derivatives for this resource</a></li>
        </#if>
        
        <#assign processingErrors = "">
        <#list resource.informationResourceFiles as irfile>
            <#if irfile.errored>
                <#assign processingErrors>
                ${processingErrors}<li><strong>${irfile.latestUploadedVersion.filename}</strong> : ${irfile.errorMessage!""} </li>
                </#assign>    
            </#if>
        </#list>        
        <#if processingErrors?has_content>
        <B>The Following Files had Processing Errors</B>
        <ol>
            <#noescape>${processingErrors}</#noescape>
        </ol>
        </#if>
    </#if>

</#macro>

<#macro uploadedFileInfo>
  <#if (resource.totalNumberOfFiles?has_content)>
                <h3 class="downloads">
                    Downloads
                    <span class="downloadNumber hidden-tablet">${resource.totalNumberOfActiveFiles?c}</span>
                </h3>
    <#if resource.totalNumberOfFiles != 0>
      <@embargoCheck/>
    </#if>
        <ul class="downloads media-list">
        <#assign extensionMap = { 'pdf':'page-white-acrobat', 'doc':'page-white-word','docx':'page-white-word' ,'DOCUMENT','page-white-text',
                        'mdb':'page-white-key','mdbx':'page-white-key','accdb':'page-white-key',
                        'xls':'page-excel','xlsx':'page-excel','DATASET':'page-white-text','CODING_SHEET':'page-white-text',
                        'IMAGE':'page-white-picture','SENSORY_DATA':'page-white-picture','ONTOLOGY','page-white-text'
          } />

        <#local showAll = ""/>
        <#local visibleCount = 0>
        <#list resource.informationResourceFiles as irfile>
         <#if (visibleCount > 4)><#local showAll = "view-hidden-extra-files"/></#if>
              <#if irfile.latestUploadedVersion??>
                  <#if !irfile.deleted><#local visibleCount = 1 + visibleCount /></#if>
                      <#local ext = extensionMap[irfile.latestUploadedVersion.extension?lower_case ]!'' />
                      <#if !ext?has_content>
                      <#local ext = extensionMap[resource.resourceType ] />
                      </#if>
                    <li class="<#if irfile.deleted>view-deleted-file</#if> ${showAll} media">
                        <i class="iconf ${ext} pull-left"></i>
                        <div class="media-body"><@createFileLink irfile true /></div>
              </#if>
              <#if irfile.latestTranslatedVersion?? && resource.resourceType == 'DATASET' >
                <blockquote>
                  <b>Translated version</b> <@createFileLink irfile.latestTranslatedVersion /></br>
                   Data column(s) in this dataset have been associated with coding sheet(s) and translated: 
                  <#if sessionData?? && sessionData.authenticated>
        <br/><small>(<a href="<@s.url value='/dataset/retranslate'><@s.param name="id" value="${resource.id?c}"/></@s.url>">Retranslate this dataset</a> - <b>Note: this process may take some time</b>)</small>
                  </#if>
                </blockquote>
                    </li>
            </#if>
        </#list>
        <#if (resource.totalNumberOfFiles == 0)>
            <li class="citationNote">This Resource is a citation<#if resource.copyLocation?has_content> a physical copy is located at ${resource.copyLocation}</#if></li>
        </#if>

        </ul>
		<#if showAll != '' || hasDeletedFiles>
         <div id="downloadsMoreArea">
	        <#if showAll != ''>
	            <a href="#" id="showAllFiles" onClick="$('.view-hidden-extra-files, #showAllFiles').toggle();return false;">show all files</a>
			</#if>
	        <#if hasDeletedFiles && sessionData?? && sessionData.authenticated>
	            <a href="#" id="showHiddenFiles" onClick="$('.view-deleted-file, #showHiddenFiles').toggle();return false;">show deleted files</a>
	        </#if>
         </div>
        </#if>
    <#nested>
</#if>
</#macro>


<#macro codingRules>
<#if codingSheet.id != -1>
<#nested>
<h3 onClick="$(this).next().toggle('fast');return false;">Coding Rules</h3>
<#if codingSheet.codingRules.isEmpty() >
<div>
No coding rules have been entered for this coding sheet yet.  
</div>
<#else>
<div id='codingRulesDiv'>
<table width="60%" class="table table-striped tableFormat">
    <thead class='highlight'>
        <tr><th>Code</th><th>Term</th><th>Description</th><th>Mapped Ontology Node</th></tr>
    </thead>
    <tbody>
        <#list codingSheet.sortedCodingRules as codeRule>
            <tr>
            <td>${codeRule.code}</td>
            <td>${codeRule.term}</td>
            <td>${codeRule.description!""}</td>
            <td><#if codeRule.ontologyNode?has_content>${codeRule.ontologyNode.displayName}</#if></td>
            </tr>
        </#list>
    </tbody>
</table>
</div>
</#if>


</#if>

</#macro>


<#macro categoryVariables>
<div>
<#if resource.categoryVariable??>
  <#-- this might be a subcategory variable, check if parent exists -->
  <#if resource.categoryVariable.parent??>
      <@kvp key="Category" val=resource.categoryVariable.parent />
    <#if resource.categoryVariable.parent != resource.categoryVariable >
      <@kvp key="Subcategory" val=resource.categoryVariable />
    </#if>
  <#else>
    <#-- only the parent category exists -->
      <@kvp key="Category" val=resource.categoryVariable />
  </#if>
<#else>
<p class="sml">No categories or subcategories specified.</p>
</#if>
</div>
</#macro>


<#macro spatialCoverage>
  <#if (resource.activeLatitudeLongitudeBoxes?has_content )>
        <h2>Spatial Coverage</h2>
            <div class="title-data">
                <p>
                  min long: ${resource.firstActiveLatitudeLongitudeBox.minObfuscatedLongitude}; min lat: ${resource.firstActiveLatitudeLongitudeBox.minObfuscatedLatitude} ;
                  max long: ${resource.firstActiveLatitudeLongitudeBox.maxObfuscatedLongitude}; max lat: ${resource.firstActiveLatitudeLongitudeBox.maxObfuscatedLatitude} ;
                  <!-- ${resource.firstActiveLatitudeLongitudeBox.scale } -->
                  <!-- ${resource.managedGeographicKeywords } -->
                </p>
            </div>

        <div class="row">
          <div id='large-google-map' class="google-map span9"></div>
       </div>
       <div id="divCoordContainer" style="display:none">
          <input type="hidden"  class="ne-lat" value="${resource.firstActiveLatitudeLongitudeBox.maxObfuscatedLatitude}" id="maxy" />
          <input type="hidden"  class="sw-lng" value="${resource.firstActiveLatitudeLongitudeBox.minObfuscatedLongitude}" id="minx" />
          <input type="hidden"  class="ne-lng" value="${resource.firstActiveLatitudeLongitudeBox.maxObfuscatedLongitude}" id="maxx" />
          <input type="hidden"  class="sw-lat" value="${resource.firstActiveLatitudeLongitudeBox.minObfuscatedLatitude}"  id="miny" />
       </div>
  </#if>
</#macro>

<#macro keywordSection label keywordList searchParam>
    <#if keywordList?has_content>
        <p>
            <strong>${label}</strong><br>
             <@keywordSearch keywordList searchParam false />
        </p>
    </#if>
</#macro>

<#macro keywords showParentProjectKeywords=true>
  <#if resource.containsActiveKeywords >
        <h2>Keywords</h2>
        <#if resource.project?? && !resource.project.active && resource.inheritingSomeMetadata>
            <em>Note: Inherited values from this project are not available because the project is not active</em>
        </#if>
        <div class="row">
                <#if (resource.keywordProperties?size > 1)>        
                    <div class="span45">
                <#elseif resource.keywordProperties?size == 1>
                    <div class="span9">
                </#if>
                
                <#list resource.keywordProperties as prop>
                <#-- FIXME: somehow this should be folded into SearchFieldType to not have all of this if/else -->
                    <#if ((resource.keywordProperties?size /2)?ceiling == prop_index)>        
                        </div><div class="span45">
                    </#if>
                    <#if prop == "activeSiteNameKeywords">
                        <@keywordSection "Site Name" resource.activeSiteNameKeywords "siteNameKeywords" />
                    </#if>
                    <#if prop == "activeSiteTypeKeywords">
                        <@keywordSection "Site Type" resource.activeSiteTypeKeywords "uncontrolledSiteTypeKeywords" />
                    </#if>
                    <#if prop == "activeCultureKeywords">
                        <@keywordSection "Culture" resource.activeCultureKeywords "uncontrolledCultureKeywords" />
                    </#if>                    
                    <#if prop == "activeMaterialKeywords">
                        <@keywordSection "Material" resource.activeMaterialKeywords "query" />
                    </#if>
                    <#if prop == "activeInvestigationTypes">
                        <@keywordSection "Investigation Types" resource.activeInvestigationTypes "query" />
                    </#if>
                    <#if prop == "activeOtherKeywords">
                        <@keywordSection "General" resource.activeOtherKeywords "query" />
                    </#if>
                    <#if prop == "activeTemporalKeywords">
                        <@keywordSection "Temporal Keywords" resource.activeTemporalKeywords "query" />
                    </#if>
                    <#if prop == "activeGeographicKeywords">
                           <@keywordSection "Geographic Keywords" resource.activeGeographicKeywords "query" />
                    </#if>
                </#list>
                <#if (resource.keywordProperties?size > 0)>        
                    </div>
                </#if>                
        </div>
        <hr/>
  </#if>
</#macro>


<#macro temporalCoverage showParentCoverage=true>
    <#if resource.activeCoverageDates?has_content>
        <h2>Temporal Coverage</h2>
        <#list resource.activeCoverageDates as coverageDate>
				<#assign value>
                <#if coverageDate.startDate?has_content>${coverageDate.startDate?c}<#else>?</#if> to 
                        <#if coverageDate.endDate?has_content>${coverageDate.endDate?c}<#else>?</#if>
                         <#if (coverageDate.description?has_content)> (${coverageDate.description})</#if>
				</#assign>
				<@kvp key=coverageDate.dateType.label val=value />
        </#list>
        <hr/>
    </#if>
</#macro>

<#macro resourceProvider>
  <#if resource.resourceProviderInstitution?? && resource.resourceProviderInstitution.id != -1>
    <li>
        <strong>Resource Provider</strong><br>
        <@browse creator=resource.resourceProviderInstitution />
    </li>
  </#if>
</#macro>

<#macro browse creator><#compress>
<#if creator??> <a href="<@s.url value="/browse/creators/${creator.id?c}"/>">${creator.properName}</a></#if>
</#compress>
</#macro>

<#macro search fieldName="query" quoted=true>
<#assign q=''>
<#if quoted>
<#assign q='"'>
</#if>
 <#assign term><#nested></#assign> 
<#noescape><a href="<@s.url value="/search/search?${fieldName?url}=${q?url}${term?url}${q?url}"/>">${term}</a></#noescape>
</#macro>

<#macro keywordSearch _keywords fieldName="query" quoted=true>
<#list _keywords.toArray()?sort_by("label") as _keyword><#t>
 <@search fieldName quoted>${_keyword.label}</@search> <#if _keyword_has_next>&bull;</#if> 
</#list>
</#macro>

<#macro downloadCount irfile>
    <#assign downloads = 0 />
    <#if (irfile.transientDownloadCount?has_content && irfile.transientDownloadCount > 0 )>
        <#assign downloads = irfile.transientDownloadCount />    
    </#if>

    <#if (irfile.informationResourceFile?has_content && irfile.informationResourceFile.transientDownloadCount > 0 )>
        <#assign downloads = irfile.informationResourceFile.transientDownloadCount />    
    </#if>
    <#if (downloads > 0)>
    <#if (downloads != 1)>
        [downloaded ${downloads} times]
    <#else>
        [downloaded 1 time]
    </#if>
    </#if>
</#macro>



<#macro accessRights>
  <#if sessionData?? && sessionData.authenticated>
<h2>Administrative Information</h2>

    <@resourceUsageInfo />
    <div>
        <dl class="dl-horizontal">
            <dt><p><strong>Created by</strong></p></dt>
            <dd><p><a href="<@s.url value="/browse/creators/${resource.submitter.id?c}"/>">${resource.submitter.properName}</a> <#if resource.submitter.id == resource.uploader.id> on ${resource.dateCreated}</#if></p></dd>
        	<#if resource.submitter.id != resource.uploader.id>
	            <dt><p><strong>Uploaded by</strong></p></dt>
	            <dd><p><a href="<@s.url value="/browse/creators/${resource.uploader.id?c}"/>">${resource.uploader.properName}</a> on ${resource.dateCreated}</p></dd>
			</#if>
			<#if resource.account?has_content && (administrator || editable) >
            	<dt><p><strong>Account</strong></p></dt>
            	<dd><p><a href="<@s.url value="/billing/${resource.account.id?c}"/>">${resource.account.name}</a></p></dd>
			</#if>

            <#if administrator>
            <dt><p><strong>Status</strong></p></dt>
            <dd><p>${resource.status.label} <#if resource.previousStatus?has_content && resource.previousStatus != resource.status>(${resource.previousStatus.label})</#if></p></dd>
            </#if>
            <dt><p><strong>Last Updated by</strong></p></dt>
            <dd><p><a href="<@s.url value="/browse/creators/${resource.updatedBy.id?c}"/>">${resource.updatedBy.properName!""}</a> on ${resource.dateUpdated?date!""}</p></dd>
            <dt><p><strong>Viewed</strong></p></dt>
            <dd><p>${resource.transientAccessCount!"0"} time(s)</p></dd>
        </dl>
    </div>

    <#nested>
    <@resourceCollectionsRights collections=effectiveResourceCollections owner=resource.submitter />
    </#if>
</#macro>

<#macro authorizedUsers collection >
    <@resourceCollectionsRights collections=collection.hierarchicalResourceCollections />
</#macro>

<#macro infoResourceAccessRights>
    <@accessRights>
        <div>
        <#if resource.embargoedFiles?? && !resource.embargoedFiles>
           The file(s) attached to this resource are <b>not</b> publicly accessible.  
                    They will be released to the public domain in the future</b>.
        </#if>
        </div>
    </@accessRights>
</#macro>

<#macro indvidualInstitutionalCredit>
    <#if creditProxies?has_content >
        <h3>Individual &amp; Institutional Roles</h3>
        <@showCreatorProxy proxyList=creditProxies />
        <hr/>
    </#if>

</#macro>

<#macro kvp key="" val="" noescape=false>
	<#if val?has_content && val != 'NULL' >
       <p class="sml"><strong>${key}:</strong> <#if noescape><#noescape>${val}</#noescape><#else>${val}</#if></p>
    </#if>
</#macro>

<#macro resourceNotes>
    <#if resource.activeResourceNotes?has_content>
        <h2>Notes</h2>
        <#list resource.activeResourceNotes.toArray()?sort_by("sequenceNumber") as resourceNote>
            <@kvp key=resourceNote.type.label val=resourceNote.note />
        </#list>
        <hr />
    </#if>
</#macro>

<#macro resourceAnnotations>
    <#if ! resource.activeResourceAnnotations.isEmpty()>
    <h3>Record Identifiers</h3>
        <#list resource.activeResourceAnnotations as resourceAnnotation>
			<@kvp key=resourceAnnotation.resourceAnnotationKey.key val=resourceAnnotation.value />
        </#list>
        <hr/>
    </#if>

</#macro>

<#macro relatedSimpleItem listitems label>
  <#if ! listitems.isEmpty()>
        <h3>${label}</h3>
        <table>
        <#list listitems as citation>
            <tr><td>${citation}</td></tr>
        </#list>
        </table>
  </#if>
</#macro>


<#macro statusCallout onStatus cssClass>
<#if persistable.status.toString().equalsIgnoreCase(onStatus) >
<div class="alert-${cssClass} alert">
    <p><#nested></p>
</div>
</#if>
</#macro>

<#macro showCreatorProxy proxyList=authorshipProxies>
    <#if proxyList?has_content>
    <#list allResourceCreatorRoles as role>
        <#assign contents = "" />
        <#list proxyList as proxy>
          <#if proxy.valid && proxy.role == role >
            <#assign contents><#noescape>${contents}<#t/></#noescape><#if contents?has_content>,</#if> <@browse creator=proxy.resourceCreator.creator /><#t/></#assign>
          </#if>
        </#list>
        <#if contents?has_content>
        	<#assign key>${role.label}(s)</#assign>
            <@kvp key=key val=contents noescape=true />
        </#if>
    </#list>
    </#if>
</#macro>


<#macro pageStatusCallout>
<#local status="error">
<#if (persistable.status)?has_content && !persistable.active >
<#if persistable.status == 'DRAFT'>
  <#local status="info"/>
</#if>

<@statusCallout onStatus='${persistable.status?lower_case}' cssClass='${status}'>
    This record has been marked as <strong>${persistable.status.label}</strong> <#if authorityForDup?has_content> of 
    <a href="<@s.url value="/${authorityForDup.urlNamespace}/${authorityForDup.id?c}"/>">${authorityForDup.name}</a></#if>. 
    <#if !persistable.draft> While ${siteAcronym} will retain this record, it will not appear in search results.</#if>
</@statusCallout>

</#if> 
</#macro>

<#macro showcase>
    <#local numImagesToDisplay= resource.visibleFilesWithThumbnails?size />
  <#assign numImagesToDisplay=0/>
  <div id="showcase" class="showcase" >
 
    <#list resource.visibleFilesWithThumbnails as irfile>
          <div class="showcase-slide"> 
            <#if authenticatedUser??>
            <!-- Put the slide content in a div with the class .showcase-content. --> 
            <div class="showcase-content" style="height:100%">
              <span style="display: inline-block; height: 100%; vertical-align: middle;"></span>
              <#-- //FIXME: image hidden by overflow-hiden directive when width is 100%.  This shouldn't happen, but no time for analysis.  Quick fix time!-->
              <img style="max-width: 95%" alt="#${irfile_index}" src="<@s.url value="/filestore/${irfile.zoomableVersion.id?c}/get"/>"/>
            </div> 
            <!-- Put the thumbnail content in a div with the class .showcase-thumbnail --> 
            </#if>
            <div class="showcase-thumbnail"> 
              <img alt="${irfile.latestUploadedVersion.filename}" src="<@s.url value="/filestore/${irfile.latestThumbnail.id?c}/thumbnail"/>"  />
              <!-- The div below with the class .showcase-thumbnail-caption contains the thumbnail caption. --> 
              <!-- The div below with the class .showcase-thumbnail-cover is used for the thumbnails active state. --> 
              <div class="showcase-thumbnail-cover"></div> 
            </div> 
              <div class="showcase-caption">
              Download: <@createFileLink irfile />
              </div> 
            <!-- Put the caption content in a div with the class .showcase-caption --> 
          </div>   
   </#list>
  </div>
	<p><@embargoCheck /></p>

   <#if (authenticatedUser?? && numImagesToDisplay > 0 ) || ( numImagesToDisplay > 1) >
<script type="text/javascript">
    var numImagesToDisplay = ${numImagesToDisplay?c}; 
    var authenticatedUser =  ${(authenticatedUser??)?string("true", "false")};
    $(function() {
        TDAR.common.initImageGallery($('#showcase'), numImagesToDisplay, authenticatedUser);
    });
</script> 

</#if>
</#macro>



<#macro unapiLink resource>
    <abbr class="unapi-id" title="${resource.id?c}"></abbr>
</#macro>


<#macro embargoCheck showNotice=true> 
  <!-- FIXME: CHECK -->
    <#assign embargoDate='' />
    <#list resource.confidentialFiles as file>
        <#if file.embargoed>
            <#assign embargoDate = file.dateMadePublic />
        </#if>
    </#list>
  <#if !resource.publicallyAccessible && !ableToViewConfidentialFiles>
        <#if showNotice>
            <span class="label label-inverse">Restricted Access</span> 
                Some or all of this resource's attached file(s) are <b>not</b> publicly accessible.
                <#if embargoDate?has_content>  They will be released on ${embargoDate}</#if> 
       </#if>
   <#else>
        <#if showNotice && (!resource.publicallyAccessible) && !resource.citationRecord >
            <span class="label label-inverse">Restricted Access</span> 
            <em>This resource is restricted from general view; however, you have been granted access to it.</em>
            <#if embargoDate?has_content>  They will be released on ${embargoDate}</#if> 
       </#if>
   </#if>
      <#nested/>
</#macro>

<#macro shortDate _date includeTime=false>
<#if includeTime>
${_date?string.medium}<#t>
<#else>
${_date?string('MM/dd/yyyy')}<#t>
</#if>
</#macro>


<#macro relatedResourceSection label="">
    <#if relatedResources?? && !relatedResources.empty>
    <h3>This ${label} is Used by the Following Datasets:</h3>
    <ol style='list-style-position:inside'>
    <@s.iterator var='related' value='relatedResources' >
    <li><a href="<@s.url value="/${related.urlNamespace}/${related.id?c}"/>">${related.id?c} - ${related.title} </a></li>
    </@s.iterator>
    </ol>
    </#if>
</#macro>

<#macro linkToResource resource title target='resourcedetail'>
<a href="<@s.url value="/${resource.resourceType.urlNamespace}/${resource.id?c}"/>" target="${target}" >${title}</a>
</#macro>

<#macro resourceCollectionTable removeable=false>
    <table class="table table-condensed table-hover" id="tblCollectionResources">
        <colgroup>
            <col style="width:4em">
            <col>
            <col style="width:3em">
        </colgroup>
        <thead>
            <tr>
                <th style="width: 4em">${siteAcronym} ID</th>
                <th <#if removeable>colspan="2"</#if>>Name</th>
                
            </tr>
        </thead>
        <tbody>
            <#list resources as resource>
                <tr id='dataTableRow_${resource.id?c}'>
                    <td>${resource.id?c}</td>
                    <td>
                        <@linkToResource resource resource.title!'<em>(no title)</em>' />
                    </td>
                    <#if removeable>
                    <td>
                    <button class="btn btn-mini repeat-row-delete" 
                                type="button" tabindex="-1" 
                                onclick='_removeResourceClicked(${resource.id?c}, this);false;'><i class="icon-trash"></i></button></td>
                    </#if>
                </tr>
            </#list>
        </tbody>
    </table>
</#macro>


<#macro resourceCollections>
    <#if !viewableResourceCollections.empty>
        <h3>This Resource is Part of the Following Collections</h3>
        <p>
        <#list viewableResourceCollections as collection>
                <a href="<@s.url value="/collection/${collection.id?c}"/>">${collection.name}</a> <br/>
        </#list></p>
        <hr />
    </#if>
</#macro>



<#macro additionalInformation resource_>
    <#if resource_.resourceType != 'PROJECT'>
        <#assign map = resource_.relatedDatasetData />
        <#if map?? && !map.empty>
            <h3>Additional Metadata</h3>
            <#list map?keys as key>
                <#if key?? && map.get(key)?? && key.visible?? && key.visible>
				   <@kvp key=key.displayName val=map.get(key) />
                </#if>
            </#list>
        </#if>
    </#if>
</#macro>


<#macro boolean _label _val _show=true trueString="Yes" falseString="No">
<#if _show>
    <b>${_label}:</b>
    <#if _val>${trueString}<#else>${falseString}</#if>
</#if>
</#macro>

<#macro textfield _label _val="" _alwaysShow=true>
<#if _alwaysShow || _val?has_content >
    <b>${_label}:</b> ${_val}
</#if>
</#macro>

<#macro datefield _label _val="" _alwaysShow=true>
    <#if _alwaysShow || _val?is_date>
        <b>${_label}</b>
        <#if _val?is_date>
        <@shortDate _val true/>
        </#if>
    </#if>
</#macro>


<#macro datatableChildJavascript>
//$(function() {
    if(window.opener && window.opener.TDAR.common.adhocTarget)  {
        window.opener.populateTarget({
            id:${resource.id?c},
            title:"${resource.title?js_string}"
       });


//    $(function() {
        $( "#datatable-child" ).dialog({
            resizable: false,
            modal: true,
            buttons: {
                "Return to original page": function() {
                    window.opener.focus();
                    window.close();
                },
                "Stay on this page": function() {
                    window.opener.adhocTarget = null;
                    $( this ).dialog( "close" );
                }
            }
        });
//    });
    }
//});
</#macro>

<#macro firstThumbnail resource_ forceAddSchemeHostAndPort=true>
    <#-- if you don't test if the resource hasThumbnails -- then you start showing the Image Unavailable on Projects, Ontologies... -->
	<#local seenThumbnail = false/>
	<#if resource_.supportsThumbnails && resource_.primaryThumbnail?has_content>
		<#local seenThumbnail = true/>
	</#if>
    <#t><span class="primary-thumbnail <#if seenThumbnail>thumbnail-border</#if>"><#t>
    <#if seenThumbnail ><#t>
        <#t><span class="thumbnail-center-spacing"></span><#t>
			<#t><img src="<@s.url forceAddSchemeHostAndPort=forceAddSchemeHostAndPort value="/filestore/${resource_.primaryThumbnail.id?c}/thumbnail" />" title="${resource_.primaryThumbnail.filename}" onError="this.src = '<@s.url value="/images/image_unavailable_t.gif"/>';" /><#t>
<#t><#local seenThumbnail = true/><#t>
    <#else>
    <#t><i class="${resource_.resourceType?lower_case}-125"></i><#t>
    </#if>
<#t>                </span><#t>
</#macro>


<#macro tdarCitation resource=resource showLabel=true count=0 forceAddSchemeHostAndPort=false>
  <div class="item <#if count==0>active</#if>">
      <#local url><@s.url forceAddSchemeHostAndPort=forceAddSchemeHostAndPort value="/${resource.urlNamespace}/${resource.id?c}"/></#local>
<#if resource.firstActiveLatitudeLongitudeBox?has_content>
	<#assign bb=resource.firstActiveLatitudeLongitudeBox />
		<img class="pull-right" src="//maps.googleapis.com/maps/api/staticmap?size=410x235&maptype=terrain&path=color:0x000000|weight:1|fillcolor:0x888888|${bb.minObfuscatedLatitude?c},${bb.minObfuscatedLongitude?c}|${bb.minObfuscatedLatitude?c},${bb.maxObfuscatedLongitude?c}|${bb.maxObfuscatedLatitude?c},${bb.maxObfuscatedLongitude?c}|${bb.maxObfuscatedLatitude?c},${bb.minObfuscatedLongitude?c}|${bb.minObfuscatedLatitude?c},${bb.minObfuscatedLongitude?c}&sensor=false&key=${googleMapsApiKey}" />
<#else>
      <a href="${url}" target="_top"><@firstThumbnail resource true /></a> 
</#if>
        <p class="title">
            <a target="_top" href="${url}">${resource.title} </a><br>
            <#if resource.formattedAuthorList?has_content>${resource.formattedAuthorList}
            <br/></#if>
        </p>
    
        <p><@truncate resource.description 150 /></p>
    
        <p>
            <a target="_top"  href="${url}" class="button">View ${resource.resourceType.label}</a> or &nbsp; <a target="_top"  href="/search/results">Browse all Resources</a>
        </p>    

  </div>
</#macro>

<#macro toOpenURL resource>
<#noescape>
    <#assign openUrl>ctx_ver=Z39.88-2004&amp;rfr_id=info:sid/${hostName}&amp;rft.doi=${resource.externalId!""?url}</#assign>
    <#if resource.date?has_content && resource.date != -1>
        <#assign openUrl>${openUrl}&amp;rft.date=${resource.date?c?url}</#assign>
    </#if>
    <#if resource??>
        <#if resource.resourceType == 'DOCUMENT'>
            <#if resource.documentType == 'JOURNAL_ARTICLE'>
                <#assign openUrl>${openUrl}&amp;rft.title=${resource.journalTitle!""?url}&amp;rft.jtitle=${resource.journalTitle!""?url}&amp;rft.atitle=${resource.title!""?url}</#assign>
            <#elseif resource.documentType == 'BOOK_SECTION'>
                <#assign openUrl>${openUrl}&amp;rft.title=${resource.bookTitle!""?url}&amp;rft.btitle=${resource.bookTitle!""?url}&amp;rft.atitle=${resource.title!""?url}</#assign>
            <#else>
                <#assign openUrl>${openUrl}&amp;rft.title=${resource.title!""?url}</#assign>
            </#if>

            <#assign openUrl>${openUrl}&amp;rft_val_fmt=info:ofi/fmt:kev:mtx:${resource.documentType.openUrlGenre!""?url}&amp;rft.genre=${resource.documentType.openUrlGenre!""?url}&amp;rft.issn=${resource.issn!""?url}&rft.isbn=${resource.isbn!""?url}</#assign>
        <#else> 
            <#assign openUrl>${openUrl}&amp;rft_val_fmt=info:ofi/fmt:kev:mtx:${resource.resourceType.openUrlGenre!""?url}&amp;rft.genre=${resource.resourceType.openUrlGenre!""?url}&amp;rft.title=${resource.title!""?url}</#assign>
        </#if>
    </#if>
    ${openUrl}
    </#noescape>
</#macro>


<#macro coin resource>
    <#if resource??>
    <#noescape>
        <span class="Z3988" title="<@toOpenURL resource />"></span>
   </#noescape>
    </#if>
</#macro>


<#macro license>
    <#if (resource.licenseType??) >
        <h3>License</h3>
        <#if (resource.licenseType.imageURI != "")>
            <a href="${resource.licenseType.URI}"><img src="${resource.licenseType.imageURI}"/></a>
        </#if>
        <#if (resource.licenseType.URI != "")>
            <h4>${resource.licenseType.licenseName}</h4>
            <p><@s.property value="resource.licenseType.descriptionText"/></p>
            <p><a href="${resource.licenseType.URI}">view details</a></p>
        <#else>
            <h4>Custom License Type - See details below</h4>
            <p>${resource.licenseText}</p>
        </#if>
    </#if>
</#macro>

</#escape>
<#-- NOTHING SHOULD GO AFTER THIS --> 
