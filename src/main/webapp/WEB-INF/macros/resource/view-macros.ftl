<#escape _untrusted as _untrusted?html>

<#-- 
$Id$ 
View freemarker macros
-->
<#-- include navigation menu in edit and view macros -->
<#import "common.ftl" as common>
<#import "navigation-macros.ftl" as nav>
<#setting url_escaping_charset='UTF-8'>

<#--Emit rel=canonical element.  The "canonical" url points to the preferred version of a set of pages with similar content -->
<#macro canonical object>
	<link rel="canonical" href="http://${hostName}/${object.urlNamespace}/${object.id?c}" /> 
</#macro>

<#-- emit ontology as hierarchical list -->
<#macro ontology sectionTitle="Parsed Ontology Nodes" previewSize=10 triggerSize=15>
<#if resource.sortedOntologyNodesByImportOrder?has_content>
	<#local allNodes = resource.sortedOntologyNodesByImportOrder />
	<#local size = allNodes?size />
	<#if (size>0)>
	    <h2>${sectionTitle}</h2>
	    <div id="ontologyNodesContainer" class="ontology-nodes-container">
		    <div id="ontology-nodes">
		    <#list allNodes as node>
			        <@_displayNode node previewSize node_index size/>
		    </#list>
		    </div>
		    <#if (size >= previewSize)>
			    <div id='divOntologyShowMore' class="alert">
			        <span>Showing first ${previewSize?c} ontology nodes.</span>
			        <button type="button" class="btn btn-small" id="btnOntologyShowMore">Show all ${resource.ontologyNodes?size?c} nodes...</button>
			    </div>
		    </#if>
		</#if>
		</div>
</#if>
</#macro>
<#macro _displayNode ontologyNode previewSize count size>
    <#if count == 0><ul id="ontology-nodes-root"><li>${resource.name}<ul></#if>
    <#if !(numParents?has_content)>
        <#global numParents = 1/>
    </#if>
    <#local parentCount = ontologyNode.numberOfParents />
    <#if ontologyNode.index != ontologyNode.intervalStart?string && numParents == parentCount >
    </li>
    </#if>
    <#if (numParents < parentCount)>
        <#list 1.. (parentCount -numParents) as x ><ul></#list>
    </#if>
    <#if (parentCount < numParents )>
        <#list 1..(numParents -parentCount) as x >
            </li></ul>
        </#list>
    </#if>
    <#global numParents = parentCount />
<li class="<#if (previewSize <=count)>hidden-nodes</#if>">
    <a href="<@s.url value="/ontology/${ontologyNode.ontology.id?c}/${ontologyNode.iri}"/>">${ontologyNode.displayName!ontologyNode.iri}
        <#if ontologyNode.synonyms?has_content>
            (<#t>
            <#list ontologyNode.synonyms as synonym><#t>
            ${synonym}<#t>
                <#if synonym_has_next>, </#if><#t>
            </#list><#t>
            )<#t>
        </#if><!-- (${ontologyNode.index})-->
    </a>
    <#if count == (size -1)>			<#list 1.. (numParents) as x ></li></ul></#list>
    </li></ul>
    </#if>
</#macro>

<#-- Emit a download link for an information resource file -->
<#macro createFileLink irfile newline=false showDownloadCount=true showSize=true >
    <#assign version=irfile />
         <#if version.latestUploadedVersion?? >
            <#assign version=version.latestUploadedVersion />        
         </#if>
        <#if (version.viewable)>
          <a href="<@s.url value='/filestore/${version.id?c}/get'/>" onClick="TDAR.common.registerDownload('<@s.url value='/filestore/${version.id?c}/get'/>', '${id?c}')" 
    <#if resource.resourceType == 'IMAGE'>target='_blank'</#if>
          title="${version.filename?html}">
              <@common.truncate version.filename 65 />
          </a><#if newline><br /></#if>
         <#else>
             <@common.truncate version.filename 65 /> 
         </#if>
         <#if (!version.viewable || !version.informationResourceFile.public )>
            <span class="ui-icon ui-icon-locked" style="display: inline-block"></span>
         </#if>
        <#if showSize>(<@common.convertFileSize version.fileLength />)</#if>
        <#if showDownloadCount><@downloadCount version /></#if>
</#macro>

<#-- similar to createFileLink,  but creates a download link to a zip of containing all accessible files in a resource -->
<#macro createArchiveFileLink resource newline=false >
          <#--<a href="<@s.url value='/filestore/downloadAllAsZip?informationResourceId=${resource.id?c}'/>" onClick="TDAR.common.registerDownload('/filestore/informationResourceId=${resource.id?c}', '${id?c}')"-->
          <#-- fixme:should we change the google analytics event name, or will this be a pain? -->
          <a href="<@s.url value='/filestore/${resource.id?c}/show-download-landing'/>" target="_blank" onclick="TDAR.common.registerDownload('/filestore/informationResourceId=${resource.id?c}', '${id?c}')"
          title="download all as zip">Download All</a>
         <#if resource.hasConfidentialFiles() >
            <span class="ui-icon ui-icon-locked" style="display: inline-block"></span>
         </#if>
</#macro>

<#--emit links to admin-only dataset actions
    @requires resource.id
    @requires resource.urlNamespace
-->
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
    </#if>
</#macro>

<#-- emit a list of files for display in a "file information" section.  This macro calls out to #nested for rendering
    html for each individual file
    @requires resource.informationResourceFiles
    @nested irfile:InformationResourceFile the current irFile in the loop
    @nested showAll:String css class that the #nested section should use when rendering an individual file link
-->
<#macro fileInfoSection extended windowSize=4>
        <#local showAll = ""/>
        <#local visibleCount = 0>
        <#list resource.informationResourceFiles as irfile>
              <#if (visibleCount > windowSize)><#local showAll = "view-hidden-extra-files"/></#if>
              <#if !irfile.deleted><#local visibleCount = 1 + visibleCount /></#if>
              <#nested irfile, showAll>
        </#list>
</#macro>

<#-- emit download link for translated dataset file -->
<#macro translatedFileSection irfile>
    <#if irfile.hasTranslatedVersion >
    <blockquote>
        <b>Translated version</b> <@createFileLink irfile.latestTranslatedVersion /></br>
        Data column(s) in this dataset have been associated with coding sheet(s) and translated:
        <#if sessionData?? && sessionData.authenticated>
            <br><small>(<a href="<@s.url value='/dataset/retranslate'><@s.param name="id" value="${resource.id?c}"/></@s.url>">Retranslate this dataset</a> - <b>Note: this process may take some time</b>)</small>
        </#if>
    </blockquote>
    </#if>
</#macro>

<#-- emit the correct icon for a particular information resource file -->
<#macro fileIcon irfile=file extraClass="" >
        <#local extensionMap = {
                        'pdf':'page-white-acrobat',
                        'doc':'page-white-word',
                        'docx':'page-white-word' ,
                        'mdb':'page-white-key',
                        'mdbx':'page-white-key','accdb':'page-white-key',
                        'xls':'page-excel','xlsx':'page-excel',
                        'zip':'page-white-zip',
                        'tar':'page-white-zip',
                        'tgz':'page-white-zip',
        				'DOCUMENT':'page-white-text',
                        'DATASET':'page-white-text',
                        'CODING_SHEET':'page-white-text',
                        'IMAGE':'page-white-picture',
                        'SENSORY_DATA':'page-white-picture',
                        'ONTOLOGY':'page-white-text',
                        'GEOSPATIAL':'page-white-picture',
                        'ARCHIVE':'page-white-zip',
                        'AUDIO':'cd',
                        'VIDEO': 'film'
          } />
		      <#local ext = "" >
              <#local ext = extensionMap[irfile.latestUploadedOrArchivalVersion.extension?lower_case ]!'' />
              <#if !ext?has_content>
                <#local ext = extensionMap[resource.resourceType ] />
              </#if>
              <i class="iconf ${ext} ${extraClass!""}"></i>
</#macro>

<#--show summary information about the uploaded files for th current resource (appears in the right sidebar)
    file list is truncated if it takes up too much space-->
<#macro uploadedFileInfo >
    <#local showAll = "">
    <h3 class="downloads">
        Downloads
        <span class="downloadNumber hidden-tablet">${resource.totalNumberOfActiveFiles!0?c}</span>
    </h3>
    <div id="fileSummaryContainer">
        <ul class="downloads media-list">
        <#if ((resource.totalNumberOfFiles!0) > 0) >

            <#if resource.hasConfidentialFiles()><li><@embargoCheck/></li></#if>
            <@fileInfoSection extended=false; irfile, showAll>
                <#local showAll = showAll>
                <li class="<#if irfile.deleted>view-deleted-file</#if> ${showAll} media">
                    <@fileIcon irfile=irfile extraClass="pull-left" />
                    <div class="media-body"><@createFileLink irfile true /></div>
                    <@translatedFileSection irfile />
                </li>
            </@fileInfoSection>
                <#if (resource.totalNumberOfActiveFiles > 1)>
                    <li class="archiveLink media">
                        <i class="iconf page-white-zip pull-left"></i>
                        <div class="media-body"><@createArchiveFileLink resource=resource /></div>
                    </li>
                </#if>

        </#if>
        <#if (resource.totalNumberOfFiles == 0)>
            <li class="citationNote">This resource is a citation<#if resource.copyLocation?has_content> a physical copy is located at ${resource.copyLocation}</#if></li>
        </#if>
        </ul>
        <#if showAll != ''>
        <div id="downloadsMoreArea">
            <a href="#allfiles">show all files</a>
        </div>
        </#if>
    </div>
</#macro>

<#-- FIXME: FTLREFACTOR remove: rarely used -->
<#-- return true if the current resource has any restricted files -->
<#function hasRestrictedFiles>
 <#return !(resource.publicallyAccessible)>
<#--  <#return !(resource.publicallyAccessible) && !ableToViewConfidentialFiles> -->
</#function>

<#-- FIXME: FTLREFACTOR remove: rarely used -->
<#-- return true if current resource is has contact person/institution -->
<#function contactInformationAvailable>
    <#return !contactProxies.empty>
</#function>

<#--display more detailed information about the files associated with the current resource -->
<#macro extendedFileInfo>
    <#if (resource.informationResourceFiles?has_content)>
    <#local showDownloads = authenticatedUser?? />
    <div id="extendedFileInfoContainer">
        <h3 id="allfiles">File Information</h3>
        <table class="table tableFormat">
            <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th>Name</th>
                    <th>Size</th>
                    <th>Creation Date</th>
                    <th>Date Uploaded</th>
                    <th>Access</></th>
					<#if showDownloads>
    	                <th>Downloads</th>
                    </#if>
                </tr>
            </thead>
        <tbody>
        <@fileInfoSection extended=true; irfile, showAll, ext>
        <#if irfile.viewable>
        <#local twoRow = (irfile.hasTranslatedVersion || irfile.description?has_content ) />
            <tr class="${irfile.status!""}">
                <td <#if twoRow>rowspan=2</#if>><@fileIcon irfile=irfile /></td>
                <td><@createFileLink irfile false false false /></td>
                <td><@common.convertFileSize version.fileLength /></td>
                <td><@_printCreatedDate irfile /></td>
                <td>${irfile.latestUploadedVersion.dateCreated} </td>
                <td>${irfile.restriction.label}</td>
                
                <#if irfile.transientDownloadCount?? >
                	<td>${((irfile.transientDownloadCount)!0)}</td>
				</#if>
            </tr>
            <#if twoRow>
            <tr class="${irfile.status!''}">
                <td colspan=<#if showDownloads>6<#else>5</#if>>
                    ${irfile.description!""}
                    <@translatedFileSection irfile />
                </td>
                </tr>
			</#if>           
		</#if> 
        </@fileInfoSection>
        </tbody>
        </table>
        <#if (hasRestrictedFiles() && contactInformationAvailable())>
        <div class="well restricted-files-contacts">
            <h4>Accessing Restricted Files</h4>
            <p>At least one of the files for this resource is restricted from public view. For more information regarding
                access to these files, please reference the contact information below</p>
            <@showCreatorProxy proxyList=contactProxies />
        </div>
        </#if>

    </div>
    </#if>
</#macro>
<#-- FIXME: FTLREFACTOR remove: rarely used (if we want formatting consistency, consider constant stringformat instead)-->
<#macro _printCreatedDate irfile>
    <#if irfile.fileCreatedDate??>${(irfile.fileCreatedDate!"")?string("yyyy-MM-dd")}</#if>
</#macro>

<#-- FIXME: FTLREFACTOR move to common.ftl -->
<#-- emit the coding rules section for the current coding-sheet resource. Used on view page and edit page -->
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

<#-- emit div displaying the category/subcategory of the current resource -->
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

<#-- FIXME: FTLREFACTOR move to view-template.ftl -->
<#--emit the Spatial Coverage section of a view page -->
<#macro spatialCoverage>
  <#if (resource.activeLatitudeLongitudeBoxes?has_content )>
        <h2>Spatial Coverage</h2>
            <div class="title-data">
                <p>
                  min long: ${resource.firstActiveLatitudeLongitudeBox.minObfuscatedLongitude}; min lat: ${resource.firstActiveLatitudeLongitudeBox.minObfuscatedLatitude} ;
                  max long: ${resource.firstActiveLatitudeLongitudeBox.maxObfuscatedLongitude}; max lat: ${resource.firstActiveLatitudeLongitudeBox.maxObfuscatedLatitude} ;
                  <!-- ${resource.firstActiveLatitudeLongitudeBox.scale } -->
                  <!-- ${resource.managedGeographicKeywords } -->
                  <#if editor>
	                  <#if resource.firstActiveLatitudeLongitudeBox.actuallyObfuscated!false> [obfuscated]</#if>
                  </#if>
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

<#-- emit the keywords section of a view page -->
<#-- FIXME: FTLREFACTOR move to view-template.ftl -->
<#macro keywords showParentProjectKeywords=true>
  <#if resource.containsActiveKeywords >
        <h2>Keywords</h2>
        <#if resource.projectVisible?? && !resource.projectVisible && resource.inheritingSomeMetadata>
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
                        <@_keywordSection "Site Name" resource.activeSiteNameKeywords "siteNameKeywords" />
                    </#if>
                    <#if prop == "activeSiteTypeKeywords">
                        <@_keywordSection "Site Type" resource.activeSiteTypeKeywords "uncontrolledSiteTypeKeywords" />
                    </#if>
                    <#if prop == "activeCultureKeywords">
                        <@_keywordSection "Culture" resource.activeCultureKeywords "uncontrolledCultureKeywords" />
                    </#if>                    
                    <#if prop == "activeMaterialKeywords">
                        <@_keywordSection "Material" resource.activeMaterialKeywords "query" />
                    </#if>
                    <#if prop == "activeInvestigationTypes">
                        <@_keywordSection "Investigation Types" resource.activeInvestigationTypes "query" />
                    </#if>
                    <#if prop == "activeOtherKeywords">
                        <@_keywordSection "General" resource.activeOtherKeywords "query" />
                    </#if>
                    <#if prop == "activeTemporalKeywords">
                        <@_keywordSection "Temporal Keywords" resource.activeTemporalKeywords "query" />
                    </#if>
                    <#if prop == "activeGeographicKeywords">
                           <@_keywordSection "Geographic Keywords" resource.activeGeographicKeywords "query" />
                    </#if>
                </#list>
                <#if (resource.keywordProperties?size > 0)>        
                    </div>
                </#if>                
        </div>
        <hr/>
  </#if>
</#macro>
<#macro _keywordSection label keywordList searchParam>
    <#if keywordList?has_content>
    <p>
        <strong>${label}</strong><br>
        <@keywordSearch keywordList searchParam false />
    </p>
    </#if>
</#macro>

<#-- FIXME: FTLREFACTOR move to view-template.ftl -->
<#--emit the temporal coverage section of a view page -->
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


<#-- FIXME: FTLREFACTOR move to view-template.ftl -->
<#-- emit the resource provider section of a view page -->
<#macro resourceProvider>
  <#if resource.resourceProviderInstitution?? && resource.resourceProviderInstitution.id != -1>
    <li>
        <strong>Resource Provider</strong><br>
        <@browse creator=resource.resourceProviderInstitution />
    </li>
  </#if>
</#macro>

<#--emit a link to a resource creator information page -->
<#macro browse creator role=""><#compress>
<#local c=creator />
<#if creator.creator?has_content >
	<#local c = creator.creator />
</#if>
<#local schemaRole = role />
<#if creator.role?has_content && creator.role.partOfSchemaOrg >
	<#local schemaRole = creator.role.schemaOrgLabel />
</#if>

<#if c?? > <a <#if schemaRole?has_content >itemprop="${schemaRole }"</#if> href="<@s.url value="/browse/creators/${c.id?c}"/>">${c.properName}</a></#if>
</#compress>
</#macro>

<#--emit a link to a seach page with a predefined query.  The contents of the supplied #nested block become the text node
    of the link and also the value of the "q" querystring parameter for the link URL
    @nested search terms
-->
<#macro search fieldName="query" quoted=true>
<#assign q=''>
<#if quoted>
<#assign q='"'>
</#if>
 <#assign term><#nested></#assign> 
<#noescape><a href="<@s.url value="/search/search?${fieldName?url}=${q?url}${term?url}${q?url}"/>">${term}</a></#noescape>
</#macro>

<#-- emit a link to the search page for the label of the supplied keyword (see @search)-->
<#macro keywordSearch _keywords fieldName="query" quoted=true>
<#list _keywords.toArray()?sort_by("label") as _keyword><#t>
 <@search fieldName quoted>${_keyword.label}</@search> <#if _keyword_has_next>&bull;</#if> 
</#list>
</#macro>

<#--emit the download count for the supplied information resource file -->
<#macro downloadCount irfile>
    <#assign downloads = 0 />
    <#if (irfile.transientDownloadCount?has_content && irfile.transientDownloadCount > 0 )>
        <#assign downloads = irfile.transientDownloadCount />    
    </#if>

    <#if (irfile.informationResourceFile?has_content && (irfile.informationResourceFile.transientDownloadCount!0) > 0 )>
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


<#-- emit the access rights section of a view page -->
<#macro accessRights>
  <#if sessionData?? && sessionData.authenticated>
<h2>Administrative Information</h2>

    <@common.resourceUsageInfo />
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
    <@common.resourceCollectionsRights collections=effectiveResourceCollections owner=resource.submitter />
    </#if>
</#macro>

<#-- FIXME: FTLREFACTOR remove:rarely used -->
<#macro authorizedUsers collection >
    <@common.resourceCollectionsRights collections=collection.hierarchicalResourceCollections />
</#macro>

<#-- FIXME: FTLREFACTOR remove:rarely used -->
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

<#-- FIXME: FTLREFACTOR remove:rarely used -->
<#macro indvidualInstitutionalCredit>
    <#if creditProxies?has_content >
        <h3>Individual &amp; Institutional Roles</h3>
        <@showCreatorProxy proxyList=creditProxies />
        <hr/>
    </#if>
</#macro>

<#--emit a key/value pair -->
<#macro kvp key="" val="" noescape=false>
	<#if val?has_content && val != 'NULL' >
       <p class="sml"><strong>${key}:</strong> <#if noescape><#noescape>${val}</#noescape><#else>${val}</#if></p>
    </#if>
</#macro>

<#-- FIXME: FTLREFACTOR remove:rarely used -->
<#--emit resource notes section of view page -->
<#macro resourceNotes>
    <#if resource.activeResourceNotes?has_content>
        <h2>Notes</h2>
        <#list resource.activeResourceNotes.toArray()?sort_by("sequenceNumber") as resourceNote>
            <@kvp key=resourceNote.type.label val=resourceNote.note />
        </#list>
        <hr />
    </#if>
</#macro>

<#-- FIXME: FTLREFACTOR remove:rarely used -->
<#--emit resource annotations section of view page -->
<#macro resourceAnnotations>
    <#if ! resource.activeResourceAnnotations.isEmpty()>
    <h3>Record Identifiers</h3>
        <#list resource.activeResourceAnnotations as resourceAnnotation>
			<@kvp key=resourceAnnotation.resourceAnnotationKey.key val=resourceAnnotation.value />
        </#list>
        <hr/>
    </#if>

</#macro>

<#-- FIXME: FTLREFACTOR move to view-template.ftl -->
<#--emit a list of related items (e.g. list of source collections or list of comparative collections -->
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

<#--emit a list of resouce creator proxies -->
<#macro showCreatorProxy proxyList=authorshipProxies>
    <#if proxyList?has_content>
    <#list allResourceCreatorRoles as role>
        <#assign contents = "" />
        <#list proxyList as proxy>
          <#if proxy.valid && proxy.role == role >
            <#assign contents><#noescape>${contents}<#t/></#noescape><#if contents?has_content>,</#if> <@browse creator=proxy.resourceCreator /><#t/></#assign>
          </#if>
        </#list>
        <#if contents?has_content>
        	<#assign key>${role.label}(s)</#assign>
            <@kvp key=key val=contents noescape=true />
        </#if>
    </#list>
    </#if>
</#macro>

<#-- emit a warning callout if the current resource is DRAFT or DELETED -->
<#macro pageStatusCallout>
<#local status="error">
<#if (persistable.status)?has_content && !persistable.active >
<#if persistable.status == 'DRAFT'>
  <#local status="info"/>
</#if>

<@_statusCallout onStatus='${persistable.status?lower_case}' cssClass='${status}'>
    This record has been marked as <strong>${persistable.status.label}</strong> <#if authorityForDup?has_content> of 
    <a href="<@s.url value="/${authorityForDup.urlNamespace}/${authorityForDup.id?c}"/>">${authorityForDup.name}</a></#if>. 
    <#if !persistable.draft> While ${siteAcronym} will retain this record, it will not appear in search results.</#if>
</@_statusCallout>

</#if> 
</#macro>
<#macro _statusCallout onStatus cssClass>
    <#if persistable.status.toString().equalsIgnoreCase(onStatus) >
    <div class="alert-${cssClass} alert">
        <p><#nested></p>
    </div>
    </#if>
</#macro>

<#-- emit an image gallery for the accessible image/video files for the current resource -->
<#macro imageGallery>
<div class="slider">
 <#local numIndicatorsPerSection = 4 />
<#local numIndicators = ((resource.visibleFilesWithThumbnails?size!0) / numIndicatorsPerSection)?ceiling  />
<#--  from http://bootsnipp.com/snipps/thumbnail-carousel

<div class="hidden">
<p><strong># Indicators per section: </strong>${numIndicatorsPerSection}</p>
<p><strong># Visible Thumbnails: </strong>${resource.visibleFilesWithThumbnails?size!0}</p>
<p><strong># Indicators: </strong>${numIndicators}</p>
</div>


-->
 <#if (resource.visibleFilesWithThumbnails?size > 1 || !authenticatedUser??)>
	<div id="myCarousel" class="image-carousel carousel slide pagination-centered">
	    <#if (numIndicators > 1)>
			<div class="carousel-indicators ">
			    <span data-target="#myCarousel" data-slide-to="0" class="active">&nbsp;</span>
			    <#list 1..(numIndicators -1) as x>
	                <span data-target="#myCarousel" data-slide-to="${x}">&nbsp;</span>
			    </#list>
			</div>
	    </#if>
	 
		<!-- Carousel items -->
		<div class="carousel-inner">
		
		<#list resource.visibleFilesWithThumbnails as irfile>
			<#if (irfile_index % numIndicatorsPerSection) == 0>
			<div class="item pagination-centered <#if irfile_index == 0>active</#if>">
				<div class="row-fluid">
			</#if>
				  <div class="span3">
				  <span class="primary-thumbnail thumbnail-border <#if irfile_index == 0>thumbnail-border-selected</#if>">
				  	<span class="thumbnail-center-spacing "></span>
				  <img class="thumbnailLink img-polaroid" alt="<@_altText irfile />" src="<@s.url value="/filestore/${irfile.latestThumbnail.id?c}/thumbnail"/>" style="max-width:100%;"
				  	onError="this.src = '<@s.url value="/images/image_unavailable_t.gif"/>';" data-url="<@s.url value="/filestore/${irfile.zoomableVersion.id?c}/get"/>"  <#if !irfile.public>data-access-rights="${irfile.restriction.label}"</#if>/>
				  	                </span>
				  	</div>
			<#if ((irfile_index + 1) % numIndicatorsPerSection) == 0 || !irfile_has_next>
				</div><!--/row-fluid-->
			</div><!--/item-->
			</#if>
		</#list> 
		</div><!--/carousel-inner-->
		    <#if (numIndicators > 1)>
		<a class="left carousel-control" href="#myCarousel" data-slide="prev">‹</a>
		<a class="right carousel-control" href="#myCarousel" data-slide="next">›</a>
		</#if>
	</div><!--/myCarousel-->
</#if>
 <br/>
 
</div><!--/well-->
 <#if authenticatedUser?? >
	<div class="bigImage pagination-centered">
		<#list resource.visibleFilesWithThumbnails as irfile>
			<div>
			<span id="imageContainer">
			<img  id="bigImage" alt="#${irfile_index}" src="<@s.url value="/filestore/${irfile.zoomableVersion.id?c}/get"/>"/>
			<span id="confidentialLabel"><#if !irfile.public>This file is <em>${irfile.restriction.label}</em>, but you have rights to see it.</#if></span>
			</div>
			<div id="downloadText">
			<@_altText irfile/>
			</span>
			</div>
			<#break>
		</#list>
	</div>
</#if>
 
<script type="text/javascript">
$(document).ready(function() {
	$(".thumbnailLink").click(function() {
	var $this = $(this);
		$("#bigImage").attr('src',$this.data('url'));
		var rights = "";
		if ($this.data("access-rights")) {
			rights = "This file is <em>" + $this.data("access-rights") + "</em> but you have rights to it";
		} 
		$("#confidentialLabel").html(rights);
		$("#downloadText").html($this.attr('alt'));
		$(".thumbnail-border-selected").removeClass("thumbnail-border-selected");
		$this.parent().addClass("thumbnail-border-selected");
		});
});
</script>
</#macro>
<#macro _altText irfile>
${irfile.fileName} <#if ( irfile.description?has_content && (irfile.fileName)?has_content ) >- ${irfile.description}</#if>
    <#if irfile.fileCreatedDate??>(<@_printCreatedDate irfile/>)</#if>
</#macro>

<#--emit the unapi 'link' for the specified resource (see: http://unapi.info/specs/) -->
<#macro unapiLink resource>
    <abbr class="unapi-id" title="${resource.id?c}"></abbr>
</#macro>


<#--emit a warning message  if the current resource we are rendering is under embargo -->
<#macro embargoCheck showNotice=true>
    <#if resource.totalNumberOfFiles != 0>
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
    </#if>
</#macro>

<#-- emit a specified date in our notion of a "short" format -->
<#-- FIXME: FTLREFACTOR date format should be specified in theme or config file (TDAR-3550) -->
<#macro shortDate _date includeTime=false>
<#if includeTime>
${_date?string.medium}<#t>
<#else>
${_date?string('MM/dd/yyyy')}<#t>
</#if>
</#macro>


<#-- FIXME: FTLREFACTOR remove:rarely used -->
<#--emit list of related resources (or datasets) for the current resource -->
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

<#--emit a link tag for the specified resource and title-->
<#macro linkToResource resource title target='resourcedetail'>
<a href="<@s.url value="/${resource.resourceType.urlNamespace}/${resource.id?c}"/>" target="${target}" >${title}</a>
</#macro>

<#-- FIXME: FTLREFACTOR remove:rarely used -->
<#--emit table listing resources associated with the current resource collection  -->
<#macro resourceCollectionTable removeable=false tbid="tblCollectionResources">
    <table class="table table-condensed table-hover" id="${tbid}">
        <colgroup>
            <col style="width:4em">
            <col>
            <col style="width:3em">
        </colgroup>
        <thead>
            <tr>
                <th style="width: 4em">ID</th>
                <th <#if removeable>colspan="2"</#if>>Name</th>
                
            </tr>
        </thead>
        <tbody>
            <#list resources as resource>
                <tr id='dataTableRow_${resource.id?c}'>
                    <td>${resource.id?c}</td>
                    <td>
                        <@linkToResource resource resource.title!'<em>(no title)</em>' /> <#if !resource.active>[${resource.status.label}]</#if>
                    </td>
                    <#if removeable>
                    <td>
                    <button class="btn btn-mini remove-row"
                                type="button" tabindex="-1" 
                                data-resourceid="${resource.id?c}"><i class="icon-trash"></i></button></td>
                    </#if>
                </tr>
            </#list>
        </tbody>
    </table>
</#macro>


<#-- FIXME: FTLREFACTOR remove:rarely used -->
<#--emit list of resourceCollections associated with the current resource  -->
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



<#-- FIXME: FTLREFACTOR remove:rarely used -->
<#--emit additional dataset metadata as a list of key/value pairs  -->
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

<#-- FIXME: FTLREFACTOR remove:rarely used -->
<#-- FIXME: jim: this is the worst thing you've ever written.  -->
<#macro boolean _label _val _show=true trueString="Yes" falseString="No">
<#if _show>
    <b>${_label}:</b>
    <#if _val>${trueString}<#else>${falseString}</#if>
</#if>
</#macro>

<#-- FIXME: FTLREFACTOR remove:rarely used (actually, probably best replaced with @kvp) -->
<#macro textfield _label _val="" _alwaysShow=true>
<#if _alwaysShow || _val?has_content >
    <b>${_label}:</b> ${_val}
</#if>
</#macro>

<#-- FIXME: FTLREFACTOR remove:rarely used -->
<#macro datefield _label _val="" _alwaysShow=true>
    <#if _alwaysShow || _val?is_date>
        <b>${_label}</b>
        <#if _val?is_date>
        <@shortDate _val true/>
        </#if>
    </#if>
</#macro>

<#--FIXME: currently broken (TDAR-3531). also,  this should go into external javascript -->
<#macro datatableChildJavascript>
    if(window.opener && window.opener.TDAR.common.adhocTarget)  {
        window.opener.TDAR.common.populateTarget({
            id:${resource.id?c},
            title:"${resource.title?js_string}"
       });


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
    }
</#macro>

<#-- emit markup for a single thumbnail representing the specified resource (e.g. for use in search results or project/collection contents  -->
<#macro firstThumbnail resource_ forceAddSchemeHostAndPort=true>
    <#-- if you don't test if the resource hasThumbnails -- then you start showing the Image Unavailable on Projects, Ontologies... -->
	<#local seenThumbnail = false/>
	<#if resource_.supportsThumbnails && resource_.primaryThumbnail?has_content>
		<#local seenThumbnail = true/>
	</#if>
    <#t><span class="primary-thumbnail <#if seenThumbnail>thumbnail-border</#if>"><#t>
    <#if seenThumbnail ><#t>
        <#t><span class="thumbnail-center-spacing"></span><#t>
			<#t><img src="<@s.url forceAddSchemeHostAndPort=forceAddSchemeHostAndPort value="/filestore/${resource_.primaryThumbnail.id?c}/thumbnail" />" title="${resource_.primaryThumbnail.filename}" alt="${resource_.primaryThumbnail.filename}"  onError="this.src = '<@s.url value="/images/image_unavailable_t.gif"/>';" /><#t>
<#t><#local seenThumbnail = true/><#t>
    <#else>
    <#t><i class="${resource_.resourceType?lower_case}-125"></i><#t>
    </#if>
<#t>                </span><#t>
</#macro>


<#--emit the citation section of a view page (including map depicting bounding box, if bounding box defined) -->
<#macro tdarCitation resource=resource showLabel=true count=0 forceAddSchemeHostAndPort=false>
  <div class="item <#if count==0>active</#if>">
      <#local url><@s.url forceAddSchemeHostAndPort=forceAddSchemeHostAndPort value="/${resource.urlNamespace}/${resource.id?c}"/></#local>
<#if resource.firstActiveLatitudeLongitudeBox?has_content>
    <img alt="map" class="pull-right" src="${_staticGoogleMapUrl(resource.firstActiveLatitudeLongitudeBox, googleMapsApiKey)}" />
<#else>
      <a href="${url}" target="_top"><@firstThumbnail resource true /></a> 
</#if>
        <p class="title">
            <a target="_top" href="${url}">${resource.title} </a><br>
            <#if resource.formattedAuthorList?has_content>${resource.formattedAuthorList}
            <br/></#if>
        </p>
    
        <p><@common.truncate resource.description 150 /></p>
    
        <p>
            <a target="_top"  href="${url}" class="button">View ${resource.resourceType.label}</a> or &nbsp; <a target="_top"  href="/search/results">Browse all Resources</a>
        </p>    

  </div>
</#macro>
<#function _staticGoogleMapUrl boundingBox apikey>
    <#local bb=boundingBox>
    <#local bbvals="${bb.minObfuscatedLatitude?c},${bb.minObfuscatedLongitude?c}|${bb.minObfuscatedLatitude?c},${bb.maxObfuscatedLongitude?c}|${bb.maxObfuscatedLatitude?c},${bb.maxObfuscatedLongitude?c}|${bb.maxObfuscatedLatitude?c},${bb.minObfuscatedLongitude?c}|${bb.minObfuscatedLatitude?c},${bb.minObfuscatedLongitude?c}">
    <#local apikeyval="">
    <#if googleMapsApiKey?has_content>
        <#local apikeyval="&key=${googleMapsApiKey}">
    </#if>
    <#return "//maps.googleapis.com/maps/api/staticmap?size=410x235&maptype=terrain&path=color:0x000000|weight:1|fillcolor:0x888888|${bbvals}&sensor=false${apikeyval}">
</#function>

<#-- emit OpenURL format url for the specified resource (for use with COIN citation) -->
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

            <#assign openUrl>${openUrl}&amp;rft_val_fmt=info:ofi/fmt:kev:mtx:${resource.documentType.openUrlGenre!""?url}&amp;rft.genre=${resource.documentType.openUrlGenre!""?url}&amp;rft.issn=${resource.issn!""?url}&amp;rft.isbn=${resource.isbn!""?url}</#assign>
        <#else> 
            <#assign openUrl>${openUrl}&amp;rft_val_fmt=info:ofi/fmt:kev:mtx:${resource.resourceType.openUrlGenre!""?url}&amp;rft.genre=${resource.resourceType.openUrlGenre!""?url}&amp;rft.title=${resource.title!""?url}</#assign>
        </#if>
    </#if>
    ${openUrl}
    </#noescape>
</#macro>

<#-- emit COIN microformat link -->
<#macro coin resource>
    <#if resource??>
    <#noescape>
        <span class="Z3988" title="<@toOpenURL resource />"></span>
   </#noescape>
    </#if>
</#macro>

<#-- emit license information section -->
<#macro license>
    <#if (resource.licenseType??) >
        <h3>License</h3>
        <#if (resource.licenseType.imageURI != "")>
            <a href="${resource.licenseType.URI}"><img alt="license image" src="<#if secure>${resource.licenseType.secureImageURI}<#else>${resource.licenseType.imageURI}</#if>"/></a>
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
