<#-- 
$Id$ 
View freemarker macros
-->
<#-- include navigation menu in edit and view macros -->
<#include "common.ftl">
<#include "navigation-macros.ftl">
<#setting url_escaping_charset='UTF-8'>

<#macro ontology sectionTitle="Parsed Ontology Nodes" previewSize=10 triggerSize=15>
<#if resource.getTotalNumberOfFiles() &gt; 0  && resource.ontologyNodes??>
    <h3>${sectionTitle}</h3>
    <table cellpadding='2' class="zebracolors">
    <thead class='highlight'>
        <tr>
            <th>Label</th>
        </tr>
    </thead>
    <tbody id="ontology-nodes-part1">
    <@s.iterator status='rowStatus' value='resource.sortedOntologyNodes' var='ontologyNode'>
    <tr>
        <td style="padding-left:${ontologyNode.numberOfParents * 2}em">${ontologyNode.displayName} 
        <#if (ontologyNode.synonyms?? && ontologyNode.synonyms.size() &gt; 0)>
          <@s.iterator value="ontologyNode.synonyms" var="synonym" status="stat">
            <#if synonym.first>(</#if>  
            <#if !synonym.last>,</#if>  
            <#if synonym.last>)</#if>  
          </@s.iterator>
        </#if>
        </td>
    </tr>
    <#if (rowStatus.index == previewSize && resource.ontologyNodes.size() > triggerSize ) >
    </tbody>
    <tbody id="ontology-nodes-part2" style="display:none">
    </#if>
    </@s.iterator>
    </tbody>
    </table>
    <#if (resource.ontologyNodes.size() > triggerSize )>
    <div id='divOntologyShowMore'>
        <p><em>Showing first ${previewSize?c} ontology nodes. </em><button id="btnOntologyShowMore">Show all ${resource.ontologyNodes.size()?c} nodes...</button></p>
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
</#macro>

<#macro createFileLink irfile>
        <#if (irfile.public && resource.availableToPublic) || allowedToViewConfidentialFiles>
          <a href="<@s.url value='/filestore/${irfile.latestUploadedVersion.id?c}/get'/>" <#if irfile.informationResourceFileType == "IMAGE">class="lightbox" </#if> >
              ${irfile.latestUploadedVersion.filename}
          </a>
         <#else>
             ${irfile.latestUploadedVersion.filename} 
         </#if>
         <#if irfile.confidential || !resource.availableToPublic>
            <span class="ui-icon ui-icon-locked" style="display: inline-block"></span>
         </#if>
        <@convertFileSize irfile.latestUploadedVersion.size />
        <@downloadCount irfile />
</#macro>

<#macro uploadedFileInfo>
  <#if (resource.getTotalNumberOfFiles() > 0)>
 <h3>Uploaded Files</h3>
      <@embargoCheck/>
        <#list resource.informationResourceFiles as irfile>
              <#if irfile.latestUploadedVersion??>
                <div class="<#if irfile.deleted>deleted-file</#if>">
                    <p><b>Original file:</b> 
                    <@createFileLink irfile />
                     </p>
                </div>
                <#elseif irfile.translatedFile?? >
                    <p>
                    Data column(s) in this dataset have been associated with coding sheet(s) and translated: 
                    <@createFileLink irfile />
                    <#if sessionData?? && sessionData.authenticated>
                      <br/><small>(<a href="<@s.url value='/dataset/retranslate' resourceId='${resource.id?c}'/>">Click here to retranslate</a> - <b>Note: this process may take some time</b>)</small>
                        <@downloadCount irFile />
                    </#if>
                    </p>
            </#if>
        </#list>

        <#assign jsonimages=""/>
        <#list resource.informationResourceFiles as irfile>
            <#if (irfile.public || allowedToViewConfidentialFiles) && irfile.zoomableVersion??>
                <#assign jsonimages>
                  ${jsonimages}
                 <#if jsonimages != "">,</#if>{url: '<@s.url value='/filestore/${irfile.zoomableVersion.id?c}/get'/>', title:''}
                </#assign>
            </#if>
        </#list>
        <#if authenticated>
		<script type='text/javascript'>
		$(document).ready(function(){
  		  $.fn.lightbox.defaults.fileLoadingImage = getBaseURI() + 'includes/jquery-lightbox-0.5/images/loading.gif';
          $.fn.lightbox.defaults.fileBottomNavCloseImage = getBaseURI() + 'includes/jquery-lightbox-0.5/images/closelabel.gif';
		
		  $(".lightbox").lightbox({
				  fitToScreen: true,
				  imageClickClose: true,
				  displayDownloadLink: true,
				  jsonData: new Array(${jsonimages})
			    });
	     });
		</script>
		</#if>
    <#nested>
</#if>
<#if (resource.getTotalNumberOfFiles() == 0 && editable)>
<h3>Uploaded File(s)</h3>
This resource does not have any uploaded files.
</#if>
</#macro>


<#macro codingRules>
<#if codingSheet.id != -1>
<#nested>
<@uploadedFileInfo />
<h3 class='collapsible'>Coding Rules</h3>
<#if codingSheet.codingRules.isEmpty() >
<div>
No coding rules have been entered for this coding sheet yet.  
</div>
<#else>
<div id='codingRulesDiv'>
<table width="60%" class="zebracolors">
<thead class='highlight'><tr><th>Code</th><th>Term</th><th>Description</th></tr></thead>
<tbody>
<tr>
<@s.iterator status='rowStatus' value='codingSheet.sortedCodingRules' var='codeRule'>
<tr>
<td>${codeRule.code}</td><td>${codeRule.term}</td><td>${codeRule.description!""}</td>
</tr>
</@s.iterator>
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
      <b>Category:</b> ${resource.categoryVariable.parent}
      <br/>
    <#if resource.categoryVariable.parent != resource.categoryVariable >
      <b>Subcategory:</b> ${resource.categoryVariable}
    </#if>
  <#else>
    <#-- only the parent category exists -->
    <b>Category:</b> ${resource.categoryVariable}
  </#if>
<#else>
No categories or subcategories specified.
</#if>
</div>
</#macro>

<#macro spatialCoverage>
    <#if (resource.inheritingSpatialInformation?? && resource.inheritingSpatialInformation)>
        <#assign inheriting=true>
        <#assign activeResource=project>
    <#else>
        <#assign inheriting=false>
        <#assign activeResource=resource>
    </#if>
    
  <#if (activeResource?? && (!activeResource.latitudeLongitudeBoxes.isEmpty() || !activeResource.joinedGeographicKeywords.isEmpty()))>
<h3>Spatial Coverage</h3>
  	<#if !activeResource.latitudeLongitudeBoxes.isEmpty()>
          <div id='large-google-map' style='height:450px;'></div>
          <input type="hidden"  readonly='true' value="${activeResource.firstLatitudeLongitudeBox.maxObfuscatedLatitude}" id="maxy" size=14 />
          <input type="hidden"  readonly='true' value="${activeResource.firstLatitudeLongitudeBox.minObfuscatedLongitude}" id="minx" size=14 />
          <input type="hidden"  readonly='true' value="${activeResource.firstLatitudeLongitudeBox.maxObfuscatedLongitude}" id="maxx" size=14 />
          <input type="hidden"  readonly='true' value="${activeResource.firstLatitudeLongitudeBox.minObfuscatedLatitude}"  id="miny" size=14 />
          <em>
              min long: ${activeResource.firstLatitudeLongitudeBox.minObfuscatedLongitude}; min lat: ${activeResource.firstLatitudeLongitudeBox.minObfuscatedLatitude} ;
              max long: ${activeResource.firstLatitudeLongitudeBox.maxObfuscatedLongitude}; max lat: ${activeResource.firstLatitudeLongitudeBox.maxObfuscatedLatitude} ;
          </em>
       <br/>
       <br/>
  	</#if>
  	<#if ! activeResource.joinedGeographicKeywords.isEmpty()>
  		<b>Geographic Terms:</b> ${activeResource.joinedGeographicKeywords}
  		<!-- 
  		${activeResource.joinedManagedGeographicKeywords}
  		-->
  	</#if>
  </#if>
</#macro>

<#macro keywords showParentProjectKeywords=true>
  <#assign targetSite=resource>
  <#assign targetCulture=resource>
  <#assign targetMaterial=resource>
  <#assign targetInvestigation=resource>
  <#assign targetOther=resource>
  <#if (resource.inheritingSiteInformation?? && resource.inheritingSiteInformation)> <#assign targetSite=resource.project> </#if>
  <#if (resource.inheritingCulturalInformation?? && resource.inheritingCulturalInformation)> <#assign targetCulture=resource.project> </#if>
  <#if (resource.inheritingMaterialInformation?? && resource.inheritingMaterialInformation)> <#assign targetMaterial=resource.project> </#if>
  <#if (resource.inheritingInvestigationInformation?? && resource.inheritingInvestigationInformation)> <#assign targetInvestigation=resource.project> </#if>
  <#if (resource.inheritingOtherInformation?? && resource.inheritingOtherInformation)> <#assign targetOther=resource.project> </#if>
  
  <#if (targetSite?? && !targetSite.joinedSiteNameKeywords.isEmpty()) || (targetCulture?? && !targetCulture.joinedCultureKeywords.isEmpty()) ||
  (targetSite?? && !targetSite.joinedSiteTypeKeywords.isEmpty()) || (targetMaterial?? && !targetMaterial.joinedMaterialKeywords.isEmpty()) ||
   (targetInvestigation?? && !targetInvestigation.joinedInvestigationTypes.isEmpty()) || (targetOther?? && !targetOther.joinedOtherKeywords.isEmpty())>
  <h3>Keywords</h3>

	<table>
  <#if (targetSite?? && !targetSite.joinedSiteNameKeywords.isEmpty())>
  	<tr><td><b>Site name keywords:</b></td><td> ${targetSite.joinedSiteNameKeywords}</td></tr>
	</#if>
  <#if (targetSite?? && !targetSite.joinedSiteTypeKeywords.isEmpty())>
  	<tr><td><b>Site type keywords:</b></td><td> ${targetSite.joinedSiteTypeKeywords}</td></tr>
	</#if>

  <#if (targetCulture?? && !targetCulture.joinedCultureKeywords.isEmpty())>
  	<tr><td><b>Culture keywords: </b></td><td> ${targetCulture.joinedCultureKeywords} </td></tr>
	</#if>
  <#if (targetMaterial?? && !targetMaterial.joinedMaterialKeywords.isEmpty())>
  	<tr><td><b>Material keywords:</b></td><td> ${targetMaterial.joinedMaterialKeywords}  </td></tr>
	</#if>
  <#if (targetInvestigation?? && !targetInvestigation.joinedInvestigationTypes.isEmpty())>
  	<tr><td><b>Investigation types:</b></td><td> ${targetInvestigation.joinedInvestigationTypes} </td></tr>
	</#if>
  <#if (targetOther?? && !targetOther.joinedOtherKeywords.isEmpty())>
  	<tr><td><b>Other keywords:</b></td><td>  ${targetOther.joinedOtherKeywords} </td></tr>
	</#if>
	</table>
  </#if>
</#macro>


<#macro temporalCoverage showParentCoverage=true>
<#assign target=resource>
<#if (resource.inheritingTemporalInformation?? && resource.inheritingTemporalInformation)>
<#assign target=resource.project>
</#if>
  
<#if !target.coverageDates.isEmpty() || !target.temporalKeywords.isEmpty()>
<h3>Temporal Coverage</h3>
    <#if !target.coverageDates.isEmpty()>
    <ul>
    <#list target.coverageDates as coverageDate>
    <li>${coverageDate} <#if (coverageDate.description?? && coverageDate.description.length() > 0)> (${coverageDate.description})</#if></li>
    </#list>
    </ul>
    </#if>
    
    <div>
        <#if !target.temporalKeywords.isEmpty()>
        <b>Temporal terms</b>:${target.joinedTemporalKeywords}</span>
        </#if>
    </div>
	</#if>
</#macro>


<#macro resourceProvider>
  <#if resource.resourceProviderInstitution??>
  <h3>Resource Provider</h3>
	<div>
    	<b>Institution:</b> <@search>${resource.resourceProviderInstitution}</@search>
	</div>
  </#if>
</#macro>

<#macro search>
 <#assign term><#nested></#assign> 
 <a href="<@s.url value="/search/search?query=\"${term?url}\""/>">${term}</a>
</#macro>



<#macro downloadCount irfile>
    <#if irfile.downloadCount??>
    <#if (irfile.downloadCount != 1)>
        (downloaded  ${irfile.downloadCount!"0"} times)
    <#else>
        (downloaded 1 time)
    </#if>
    </#if>
</#macro>



<#macro accessRights>
  <#if sessionData?? && sessionData.authenticated>
<h3>Administrative Information</h3>
  <table cellspacing="1" cellpadding="1" border="0">
    <tr><td nowrap><b>Created by:</b></td><td>${resource.submitter.properName} on ${resource.dateRegistered}</td></tr>
<#if administrator>
<tr>
<td nowrap><b>Status:</b></td>
<td>${resource.status.label}</td>
</tr>
</#if>


  <#if resource.updatedBy??>
    <tr><td nowrap><b>Last Updated by:</b></td><td>${resource.updatedBy.properName!""} on ${resource.dateUpdated?date!""} </td></tr>
</#if>
    <tr><td nowrap><b>Viewed:</b></td><td>${resource.accessCounter!"0"} time(s)</td></tr>
  </table>

	<div>
	<#if ! resource.fullUsers.isEmpty()>
	<b>Users with full access to this information resource</b>
	<ul>
	<@s.iterator value='resource.fullUsers' var='fullUser'>
	<li>${fullUser.person}</li>
	</@s.iterator>
	</ul>
	</#if>
	</div>

        <div>
	<#if ! resource.readUsers.isEmpty()>
	<b>Users with read access to this information resource</b>
        <ul>
	<@s.iterator value='resource.readUsers' var='readUser'>
        <li>${readUser.person}</li>
	</@s.iterator>
	</ul>
	</#if>
	</div>
        <br/>
	<div>
	<#-- if resource.confidential>
        The file(s) attached to this resource have been flagged as containing confidential information, they are not available for download, contact the creator or submitter for access.
	</#if -->
	</div>
	<#nested>
	</#if>
</#macro>

<#macro infoResourceAccessRights>
	<@accessRights>
		<div>
		<#if !resource.availableToPublic>
      	   The file(s) attached to this resource are <b>not</b> publicly accessible.  
                    They will be released to the public domain on <b>${resource.dateMadePublic!"N/A"}</b>.
		</#if>
		</div>
	</@accessRights>
</#macro>

<#macro indvidualInstitutionalCredit>
    <#if ! creditProxies.isEmpty()>
  	<h3>Individual &amp; Institutional Roles</h3>
  	<table>
  	<@s.iterator value='creditProxies' var='proxy'>
  	<tr><td>
  	<#if proxy.valid>
    <@view.search>${proxy.resourceCreator.creator.properName}</@view.search>
    (${proxy.resourceCreator.role.label})
    </#if>
    </td></tr>
  	</@s.iterator>
  	</table>
	</#if>

    <@resourceAnnotations />
	
	<@resourceNotes />

</#macro>

<#macro resourceNotes>
    <#if ! resource.resourceNotes.isEmpty()>
    <h3>Notes</h3>
        <table>
        <@s.iterator value='resource.resourceNotes' var='resourceNote'>
        <tr>
      <td><b>${resourceNote.type.label}</b></td>
      <td>${resourceNote.note}</td>
        </tr>
        </@s.iterator>
        </table>
    </#if>
</#macro>

<#macro resourceAnnotations>
    <#if ! resource.resourceAnnotations.isEmpty()>
    <h3>Record Identifiers</h3>
        <table>
        <@s.iterator value='resource.resourceAnnotations' var='resourceAnnotation'>
        <tr>
        <td><b>${resourceAnnotation.resourceAnnotationKey.key}:</b></td>
        <td>${resourceAnnotation.value}</td>
        </tr>
        </@s.iterator>
        </table>
    </#if>

</#macro>

<#macro sourceCitations>
  <#if ! resource.sourceCitations.isEmpty()>
  <h3>Source Citations</h3>
  	<table>
    	<@s.iterator value='resource.sourceCitations' var='citation'>
      	<tr><td>${citation}</td></tr>
    	</@s.iterator>
  	</table>
  </#if>
</#macro>

<#macro sourceCollections>
  <#if ! resource.sourceCollections.isEmpty()>
<h3>Source Collections</h3>
    	<table>
      	<@s.iterator value='resource.sourceCollections' var='citation'>
        	<tr><td>${citation}</td></tr>
      	</@s.iterator>
    	</table>
  </#if>
</#macro>

<#macro relatedCitations>
	<#if ! resource.relatedCitations.isEmpty()>
    	<h3>Related Citations</h3>
    	<table>
      	<@s.iterator value='resource.relatedCitations' var='citation'>
        	<tr><td>${citation}</td></tr>
      	</@s.iterator>
    	</table>
  </#if>
</#macro>

<#macro relatedComparativeCollections>
  <#if ! resource.relatedComparativeCollections.isEmpty()>
      <h3>Related comparative collections</h3>
    	<table>
      	<@s.iterator value='resource.relatedComparativeCollections' var='citation'>
        	<tr><td>${citation}</td></tr>
      	</@s.iterator>
    	</table>
  </#if>
</#macro>


<#macro statusCallout onStatus cssClass>
<#if resource.status.toString().equalsIgnoreCase(onStatus) >
<div class="${cssClass} ui-corner-all">
    <p><#nested></p>
</div>
</#if>
</#macro>


<#macro resourceDeletedCallout>
<@statusCallout onStatus='deleted' cssClass='resource-deleted'>
    This resource has been marked as <strong>Deleted</strong>.  While tDAR will retain this resource, it will not appear in search results.
</@statusCallout>
</#macro>

<#macro resourceFlaggedCallout>
<@statusCallout onStatus='flagged' cssClass='resource-flagged'>
    This resource been <strong>flagged for deletion</strong> by a tDAR adminstrator.
</@statusCallout>
</#macro>

<#macro basicInformation>
<head>
<script>
    $(document).ready(function() {
    initializeView();
    });
</script>
</head>
<@resourceDeletedCallout />
<@resourceFlaggedCallout />

<@showControllerErrors/>
<#if resource.project?? && resource.project.id?? && resource.project.id != -1>
<p style="padding-left:40px;">
  project: <a href="<@s.url value='/project/view' resourceId='${resource.project.id?c}' />">${resource.project.title}</a>
</p>
</#if>
<#if (authorshipProxies?? && !authorshipProxies.empty)>
  <p>
    <#list authorshipProxies as proxy>
      <#if proxy.valid>
      <@view.search>${proxy.resourceCreator.creator.properName?html}</@view.search>
      (${proxy.resourceCreator.role.label})
      </#if>
    </#list>
  </p>
</#if>
  <p>

<#if resource.informationResourceFiles??>
<#list resource.informationResourceFiles as irfile>
  <span style="float:right">
    <#if ((irfile.public && resource.availableToPublic) || allowedToViewConfidentialFiles) && irfile.latestThumbnail?? >
    <img src="<@s.url value="/filestore/${irfile.latestThumbnail.id?c}/thumbnail"/>"/>
    </#if>
  </span>
</#list>
</#if>

  ${resource.description!"No description specified."}
  </p>
 <h3>Basic Information</h3>
	<table cellspacing="1" cellpadding="1" border="0">
	<#nested>
  <#if (resource.copyLocation?? && resource.copyLocation != '')>
  <tr><td nowrap><b>Copy located at:</b></td><td>${resource.copyLocation!}</td></tr>
  </#if>
  <tr><td><b>tDAR ID:</b></td> <td>${resource.id?c}</td></tr>
	</table>

</#macro>


<#macro infoResourceBasicInformation>
<@basicInformation>
<#nested>
<#if resource.resourceLanguage?? && resource.resourceLanguage.label??>
<tr>
<td nowrap><b>Resource language:</b></td>
<td>
${resource.resourceLanguage.label}
</td></tr>
</#if>
<tr><td nowrap><b>Year:</b></td><td>${resource.dateCreated!"Creation year not set."}</td></tr>
</@basicInformation>
</#macro>

<#macro projectAssociation resourceType="resource">
</#macro>

<#macro htmlHeader resourceType="resource">
  <head>
    <title>${resource.title}</title>
    <#nested>
  </head>
</#macro>

<#macro unapiLink resource>
	<abbr class="unapi-id" title="${resource.id?c}"></abbr>
</#macro>


<#macro googleScholar>
<#if resource.title?? && resource.resourceCreators?? && resource.dateCreated??>
    <meta name="citation_title" content="${resource.title?html}">
    <@s.iterator status='rowStatus' value='resource.primaryCreators' var='resourceCreator'>
    <meta name="citation_author" content="${resourceCreator.creator.properName?html}">
    </@s.iterator>    
    <meta name="citation_date" content="${resource.dateCreated!''}">
    <#if resource.dateRegistered??><meta name="citation_online_date" content="${resource.dateRegistered?date?string('yyyy/MM/dd')}"></#if>
    <#list resource.informationResourceFiles as irfile>
        <#if (irfile.public || allowedToViewConfidentialFiles) && irfile.latestPDF??>
        <meta name="citation_pdf_url" content="<@s.url value='/filestore/${irfile.latestPDF.id?c}/get'/>">
        </#if>
    </#list>
      <#if resource.resourceType == 'DOCUMENT'>
      <#if document.documentType == 'CONFERENCE_PRESENTATION' && document.publisher??>
        <meta name="citation_conference_title" content="${document.publisher?html}">
      <#elseif document.documentType == 'JOURNAL_ARTICLE' && document.journalName??>
        <meta name="citation_journal_title" content="${document.journalName?html}">
      </#if>
        <#if document.volume??><meta name="citation_volume" content="${document.volume}"></#if>
        <#if document.journalNumber??><meta name="citation_issue" content="${document.journalNumber}"></#if>
        <#if document.issn??><meta name="citation_issn" content="${document.issn}"></#if>
        <#if document.isbn??><meta name="citation_isbn" content="${document.isbn}"></#if>
        <#if document.startPage??><meta name="citation_firstpage" content="${document.startPage}"></#if>
        <#if document.endPage??><meta name="citation_lastpage" content="${document.endPage}"></#if>
        <#if document.documentType == 'THESIS'>
        <meta name="citation_dissertation_institution" content="${document.publisher?html}" >
      <#elseif (document.documentType != 'CONFERENCE_PRESENTATION') && document.publisher?? && document.publisher != ''>
        <meta name="DC.publisher" content="${document.publisher?html}" >
      </#if>
    </#if>
<#else>
    <!--required google scholar fields not available - skipping meta tags -->
</#if>
</#macro>

<#macro embargoCheck showNotice=true> 
  <!-- FIXME: CHECK -->
  <#if !viewable>
        <#if showNotice>
                Some or all of this resource's attached file(s) may <b>not</b> be publicly accessible.  
            <#if !resource.availableToPublic>
                    They will be released to the public domain on <b>${resource.dateMadePublic!"N/A"}</b>.
            </#if>
       </#if>
   <#else>
        <#if showNotice && (resource.hasConfidentialFiles() || !resource.availableToPublic) >
            <i>Note: this resource is restricted from general view; however, you have been granted access to it. </i>
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
    <fieldset>
    <legend>This ${label} is used by the following datasets:</legend>
    <ol>
    <@s.iterator var='related' value='relatedResources' >
    <li><a href="<@s.url value="/${related.urlNamespace}/${related.id?c}"/>">${related.id?c} - ${related.title} </a></li>
    </@s.iterator>
    </ol>
    </fieldset>
    </#if>
</#macro>
