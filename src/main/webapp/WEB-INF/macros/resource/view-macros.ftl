<#escape _untrusted as _untrusted?html>
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

<#macro createFileLink irfile >
    <#assign version=irfile />
         <#if version.latestUploadedVersion?? >
            <#assign version=version.latestUploadedVersion />        
         </#if>
        <#if (version.informationResourceFile.public && resource.availableToPublic) || allowedToViewConfidentialFiles>
          <a href="<@s.url value='/filestore/${version.id?c}/get'/>" onClick="registerDownload('<@s.url value='/filestore/${version.id?c}/get'/>', '${id?c}')" 
	<#if resource.resourceType == 'IMAGE'>target='_blank'</#if>
          title="${version.filename?html}">
              <@truncate version.filename 65 />
          </a>
         <#else>
             <@truncate version.filename 65 /> 
         </#if>
         <#if version.informationResourceFile.confidential || !resource.availableToPublic>
            <span class="ui-icon ui-icon-locked" style="display: inline-block"></span>
         </#if>
        (<@convertFileSize version.size />)
        <@downloadCount version />
</#macro>

<#macro uploadedFileInfo>
  <#if (resource.getTotalNumberOfFiles?? && resource.getTotalNumberOfFiles() > 0)>
 <h3>Uploaded Files</h3>
    <#assign seenDeleted = false />
      <@embargoCheck/>
        <#list resource.informationResourceFiles as irfile>
              <#if irfile.latestUploadedVersion??>
                <div class="<#if irfile.deleted>view-deleted-file</#if>">
                    <#if irfile.deleted><#assign seenDeleted = true /></#if>
                    <p><b>Original file #${irfile_index + 1}</b> 
                    <@createFileLink irfile />
                    <#-- FIXME: create a File Management section and put this + reprocess derivatives + retranslate there? -->
                    <#if resource.resourceType=='DATASET' && ableToReprocessDerivatives>
                    <small>(<a href="<@s.url value='/${resource.urlNamespace}/reimport?id=${resource.id?c}' />">Reimport this dataset</a>)</small>
                    </#if>
                     </p>
                </div>
              </#if>

              <#if irfile.latestTranslatedVersion?? && resource.resourceType == 'DATASET' >
                <blockquote>
                  <b>Translated version</b> <@createFileLink irfile.latestTranslatedVersion /></br>
                   Data column(s) in this dataset have been associated with coding sheet(s) and translated: 
                  <#if sessionData?? && sessionData.authenticated>
                    <br/><small>(<a href="<@s.url value='/dataset/retranslate'><@s.param name="id" value="${resource.id?c}"/></@s.url>">Retranslate this dataset</a> - <b>Note: this process may take some time</b>)</small>
                      <@downloadCount irfile />
                  </#if>
                </blockquote>
            </#if>
        </#list>
        <#if seenDeleted><div><a href="#" id="showHiddenFiles" onClick="$('.view-deleted-file').toggle();$('#showHiddenFiles').toggle();return false;">show deleted files</a></div></#if>

        <#if ableToReprocessDerivatives>
        <br/><small>(<a href="<@s.url value='/${resource.urlNamespace}/reprocess'><@s.param name="id" value="${resource.id?c}"/></@s.url>">Reprocess all derivatives for this resource</a> - <b>Note: this process may take some time</b>)</small>
        </#if>
    <#nested>
</#if>
<#if (resource.getTotalNumberOfFiles?? && resource.getTotalNumberOfFiles() == 0 && editable)>
<h3>Uploaded File(s)</h3>
This resource does not have any uploaded files.
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
    
  <#if (activeResource?? && (!activeResource.latitudeLongitudeBoxes.isEmpty() || !resource.activeGeographicKeywords.isEmpty()))>
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
    <#if !resource.activeGeographicKeywords.isEmpty()>
        <b>Geographic Terms:</b> <@keywordSearch resource.activeGeographicKeywords "query" true />
    </#if>
<!--    <#if !resource.managedGeographicKeywords.isEmpty()>
        <b>Geographic Terms:</b> ${resource.managedGeographicKeywords}
    </#if>
    -->
  </#if>
</#macro>

<#macro keywords showParentProjectKeywords=true>
  <#if ( !resource.activeSiteNameKeywords.isEmpty()) || ( !resource.activeCultureKeywords.isEmpty()) ||
  ( !resource.activeSiteTypeKeywords.isEmpty()) || ( !resource.activeMaterialKeywords.isEmpty()) ||
   ( !resource.activeInvestigationTypes.isEmpty()) || ( !resource.activeOtherKeywords.isEmpty())>
  <h3>Keywords</h3>
	<#if resource.project?? && !resource.project.active && resource.inheritingSomeMetadata>
	<em>Note: Inherited values from this project are not available because the project is not active</em>
	</#if>
    <table>
  <#if ( !resource.activeSiteNameKeywords.isEmpty())>
    <tr><td><b>Site Name Keywords:</b></td><td> <@keywordSearch resource.activeSiteNameKeywords "siteNameKeywords" false /></td></tr>
    </#if>
  <#if ( !resource.activeSiteTypeKeywords.isEmpty())>
    <tr><td><b>Site Type Keywords:</b></td><td> <@keywordSearch resource.activeSiteTypeKeywords "uncontrolledSiteTypeKeywords" false /></td></tr>
    </#if>

  <#if ( !resource.activeCultureKeywords.isEmpty())>
    <tr><td><b>Culture Keywords: </b></td><td> <@keywordSearch resource.activeCultureKeywords "uncontrolledCultureKeywords" false /> </td></tr>
    </#if>
  <#if ( !resource.activeMaterialKeywords.isEmpty())>
    <tr><td><b>Material Keywords:</b></td><td> <@keywordSearch resource.activeMaterialKeywords "query" true />  </td></tr>
    </#if>
  <#if ( !resource.activeInvestigationTypes.isEmpty())>
    <tr><td><b>Investigation Types:</b></td><td>  <@keywordSearch resource.activeInvestigationTypes "query" true /> </td></tr>
    </#if>
  <#if ( !resource.activeOtherKeywords.isEmpty())>
    <tr><td><b>General Keyword:</b></td><td>  <@keywordSearch resource.activeOtherKeywords "query" true /></td></tr>
    </#if>
    </table>
  </#if>
</#macro>


<#macro temporalCoverage showParentCoverage=true>
<#if !resource.coverageDates.isEmpty() || !resource.temporalKeywords.isEmpty()>
<h3>Temporal Coverage</h3>
    <#if !resource.coverageDates.isEmpty()>
    <#list resource.coverageDates as coverageDate>
    <b>${coverageDate.dateType.label}</b>: <@safenum coverageDate.startDate /> to <@safenum coverageDate.endDate /> <#if (coverageDate.description?? && coverageDate.description.length() > 0)> (${coverageDate.description})</#if><br/>
    </#list>
    </#if>
    
    <div>
        <#if !resource.temporalKeywords.isEmpty()>
        <b>Temporal terms</b>:<@keywordSearch resource.activeTemporalKeywords "query" true /></span>
        </#if>
    </div>
    </#if>
</#macro>

<#macro resourceProvider>
  <#if resource.resourceProviderInstitution?? && resource.resourceProviderInstitution.id != -1>
  <h3>Resource Provider</h3> 
	<div>
    	<b>Institution:</b> <@browse creator=resource.resourceProviderInstitution />
	</div>
  </#if>
</#macro>

<#macro browse creator>
<#if creator??>
 <a href="<@s.url value="/browse/creators/${creator.id?c}"/>">${creator.properName}</a>
</#if>

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
    <tr><td nowrap><b>Created by:</b></td><td>${resource.submitter.properName} on ${resource.dateCreated}</td></tr>
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

<#--
  <div>
  <#if resource.internalResourceCollection?? && !resource.internalResourceCollection.authorizedUsers.isEmpty()>
  <b>Users with access to this information resource</b>
  <@authorizedUsers resource.internalResourceCollection.authorizedUsers />
  </#if>
  </div>
-->
   <br/>
	<#nested>
	<@resourceCollectionsRights effectiveResourceCollections />
	</#if>
</#macro>

<#macro authorizedUsers users>
  <ul>
  <#list users as authorizedUser>
    <li>${authorizedUser.user} (<#if authorizedUser?? && authorizedUser.generalPermission??>${authorizedUser.generalPermission.label!""}</#if>)</li>
  </#list>
  </ul>
</#macro>

<#macro infoResourceAccessRights>
	<@accessRights>
		<div>
		<#if resource.availableToPublic?? && !resource.availableToPublic>
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
  	<#list creditProxies?sort as proxy>
  	<tr><td>
  	<#if proxy.valid>
    <@browse creator=proxy.resourceCreator.creator />
    (${proxy.resourceCreator.role.label})
    </#if>
    </td></tr>
  	</#list>
  	</table>
	</#if>

    <@resourceAnnotations />
	
	<@resourceNotes />

</#macro>

<#macro resourceNotes>
    <#if ! resource.resourceNotes.isEmpty()>
    <h3>Notes</h3>
        <table>
        <#list resource.resourceNotes.toArray()?sort_by("sequenceNumber") as resourceNote>
        <tr>
	      <td><b>${resourceNote.type.label}</b>:</td>
	      <td>${resourceNote.note}</td>
        </tr>
        </#list>
        </table>
    </#if>
</#macro>

<#macro resourceAnnotations>
    <#if ! resource.resourceAnnotations.isEmpty()>
    <h3>Record Identifiers</h3>
        <table>
        <#list resource.resourceAnnotations as resourceAnnotation>
	        <tr>
		        <td><b>${resourceAnnotation.resourceAnnotationKey.key}:</b></td>
		        <td>${resourceAnnotation.value}</td>
	        </tr>
        </#list>
        </table>
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

<div id="subtitle" parse="true"> 
    Part of the  
  <#if resource.project.active || editable>
  	<a href="<@s.url value='/project/view'><@s.param name="id" value="${resource.project.id?c}"/></@s.url>">
  </#if>
  	${resource.project.coreTitle}
  <#if resource.project.active || editable ></a></#if>
	    <#if resource.project.draft>(DRAFT)</#if> project
</div>
</#if>
<#if (authorshipProxies?? && !authorshipProxies.empty)>
  <p>
    <#list authorshipProxies?sort as proxy>
      <#if proxy.valid>
      <@browse creator=proxy.resourceCreator.creator />
      (${proxy.resourceCreator.role.label})
      </#if>
    </#list>
  </p>
</#if>
  <p>

<#if resource.informationResourceFiles??>
<#assign img =""/>
<!-- only show the 1st image -->
<#list resource.informationResourceFiles as irfile>
    <#if ((irfile.public && resource.availableToPublic) || allowedToViewConfidentialFiles) && irfile.latestThumbnail?? && img == ''>
    <#assign img>
        <span style="float:right">
            <img src="<@s.url value="/filestore/${irfile.latestThumbnail.id?c}/thumbnail"/>"/>
        </span>
      </#assign>
    </#if>
</#list>
<#noescape>${img}</#noescape>
</#if>

  ${resource.description!"No description specified."}
  </p>
 <h3>Basic Information</h3>
	<table cellspacing="1" cellpadding="1" border="0">
	<#nested>
    <#if resource.url! != ''>
    <tr>
        <td nowrap><b>URL:</b></td><td>
        <a href="${resource.url?html}">${resource.url?html}</a></td>
    </tr>
    </#if>
  <#if (resource.copyLocation?? && resource.copyLocation != '')>
  <tr><td nowrap><b>Copy located at:</b></td><td>${resource.copyLocation!}</td></tr>
  </#if>
  <tr><td><b>tDAR ID:</b></td> <td>${resource.id?c}</td></tr>
  <#if resource.externalId??>
    <tr><td><b>External Id:</b></td> <td>${resource.externalId}</td></tr>
  </#if>
	</table>

</#macro>

<#macro showcase>
  <#assign numImagesToDisplay=0/>
  <div id="showcase" class="showcase" style="display:none;<#if !authenticatedUser??>margin:0px !important</#if>"> 
    <#list resource.informationResourceFiles as irfile>
      <#if !irfile.deleted && ((irfile.public && resource.availableToPublic) || allowedToViewConfidentialFiles) && irfile.latestThumbnail??>
          <#assign numImagesToDisplay= 1 + numImagesToDisplay />
          <div class="showcase-slide"> 
            <#if authenticatedUser??>
            <!-- Put the slide content in a div with the class .showcase-content. --> 
            <div class="showcase-content" style="position:relative; top:50%;margin-top:-${irfile.zoomableVersion.height /2}px;"> 
              <img alt="#${irfile_index}" src="<@s.url value="/filestore/${irfile.zoomableVersion.id?c}/get"/>" />
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
          </a>
              
              </div> 
            <!-- Put the caption content in a div with the class .showcase-caption --> 
          </div>   
      </#if>
   </#list>
   <#if (authenticatedUser?? && numImagesToDisplay > 0 ) || ( numImagesToDisplay > 1) >
    <script>
     $("#showcase").toggle();
    </script>
   </#if>
  </div>

   <#if (authenticatedUser?? && numImagesToDisplay > 0 ) || ( numImagesToDisplay > 1) >
<script type="text/javascript"> 
  <#assign width = 700 />
  <#if ((numImagesToDisplay * 100 + 200)< 700)>
    <#assign width = numImagesToDisplay * 100 + 200/>
  </#if>
  $("#showcase").awShowcase(
  {
   <#if authenticatedUser??>
      content_width:      600,
      content_height:     600,
      arrows:           true,
   <#else>
      content_width:      ${width},
      content_height:     0,
      arrows:         false,
   </#if>
    fit_to_parent:      false,
    auto:         false,
    interval:       3000,
    continuous:       false,
    loading:        true,
    tooltip_width:      100,
    tooltip_icon_width:   32,
    tooltip_icon_height:  32,
    tooltip_offsetx:    18,
    tooltip_offsety:    0,
    buttons:        true,
    btn_numbers:      true,
    keybord_keys:     true,
    mousetrace:       false, /* Trace x and y coordinates for the mouse */
    pauseonover:      true,
    stoponclick:      true,
    transition:       'hslide', /* hslide/vslide/fade */
    transition_delay:   300,
    transition_speed:   500,
    show_caption:     'onhover', /* onload/onhover/show */
    thumbnails:       true,
    thumbnails_position:  'outside-last', /* outside-last/outside-first/inside-last/inside-first */
    thumbnails_direction: 'horizontal', /* vertical/horizontal */
    thumbnails_slidex:    1, /* 0 = auto / 1 = slide one thumbnail / 2 = slide two thumbnails / etc. */
    dynamic_height:     false, /* For dynamic height to work in webkit you need to set the width and height of images in the source. Usually works to only set the dimension of the first slide in the showcase. */
    speed_change:     false, /* Set to true to prevent users from swithing more then one slide at once. */
    viewline:       false /* If set to true content_width, thumbnails, transition and dynamic_height will be disabled. As for dynamic height you need to set the width and height of images in the source. */
  });
 
</script> 

<style>
.showcase-thumbnail-container, .showcase-thumbnail-restriction {
  height:110px !important; overflow:hidden !important;
}

.showcase-thumbnail { margin-bottom:20px}

</style>
</#if>
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
<#if resource.date?? && resource.date != -1><tr><td nowrap><b>Year:</b></td><td>${resource.date?c}</td></tr></#if>
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
<#if resource.title?? && resource.resourceCreators?? && resource.date??>
    <meta name="citation_title" content="${resource.title?html}">
    <#list resource.primaryCreators?sort_by("sequenceNumber") as resourceCreator>
	    <meta name="citation_author" content="${resourceCreator.creator.properName?html}">
    </#list>    
    <meta name="citation_date" content="${resource.date?c!''}">
    <#if resource.dateCreated??><meta name="citation_online_date" content="${resource.dateCreated?date?string('yyyy/MM/dd')}"></#if>
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
  <#if !resource.publicallyAccessible && !ableToViewConfidentialFiles>
        <#if showNotice>
                Some or all of this resource's attached file(s) may <b>not</b> be publicly accessible.  
            <#if !resource.availableToPublic>
                    They will be released to the public domain on <b>${resource.dateMadePublic!"N/A"}</b>.
            </#if>
       </#if>
   <#else>
        <#if showNotice && (!resource.publicallyAccessible) >
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
    <h3>This ${label} is Used by the Following Datasets:</h3>
    <ol style='list-style-position:inside'>
    <@s.iterator var='related' value='relatedResources' >
    <li><a href="<@s.url value="/${related.urlNamespace}/${related.id?c}"/>">${related.id?c} - ${related.title} </a></li>
    </@s.iterator>
    </ol>
    </#if>
</#macro>

<#macro linkToResource resource title target='resourcedetail'>
<a href="<@s.url value="/${resource.resourceType.toString().toLowerCase()}/${resource.id?c}"/>" target="${target}" >
    ${title}
</a>
</#macro>

<#macro resourceCollectionTable removeable=false>
    <table class="zebracolors tableFormat" id="tblCollectionResources">
        <thead>
            <tr>
                <th style="width: 4em">tDAR ID</th>
                <th>Name</th>
                <#if removeable><th></th></#if>
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
                    <td><button class="addAnother minus" 
                                type="button" tabindex="-1" 
                                onclick='removeResourceClicked(${resource.id?c}, this);false;'><img src="/images/minus.gif" class="minus"></button></td>
                    </#if>
                </tr>
            </#list>
        </tbody>
    </table>
</#macro>


<#macro resourceCollections>
    <#if !persistable.sharedVisibleResourceCollections.empty>
    <h3>This Resource is Part of the Following Collections</h3>
    <#list persistable.sharedVisibleResourceCollections as collection>
            <a href="<@s.url value="/collection/${collection.id?c}"/>">
                ${collection.name}
            </a> <br/>
    </#list>
    </#if>
</#macro>



<#macro sharedViewComponents resource_ >
    <#if resource_.resourceType != 'PROJECT'>
    	<#if licensesEnabled??>
    		<@license />
        </#if>
        
        <#if copyrightEnabled??>
    		<@copyrightHolders />
        </#if>
    </#if>
	
    <@uploadedFileInfo >
        <#if resource_.resourceType == 'CODING_SHEET' ||  resource_.resourceType == 'ONTOLOGY'>
            <@categoryVariables />
        </#if>
    </@uploadedFileInfo>
    
    <#if resource_.resourceType == 'PROJECT'>
        <@keywords showParentProjectKeywords=false />
    <#else>
        <@keywords />
    </#if>
    <@spatialCoverage />
    <@temporalCoverage />
    <@resourceProvider />
    <@indvidualInstitutionalCredit />

    <#-- <@relatedSimpleItem resource.sourceCitations "Source Citations"/> -->
    <#-- <@relatedSimpleItem resource.relatedCitations "Related Citations"/> -->
    <@relatedSimpleItem resource.sourceCollections "Source Collections"/>
    <@relatedSimpleItem resource.relatedComparativeCollections "Related Comparative Collections" />
    <#-- display linked data <-> ontology nodes -->
    <@relatedResourceSection label=resource_.resourceType.label />
    
    
    <@unapiLink resource_ />
    <@resourceCollections />
	<@additionalInformation resource_ />
	
	<#nested>
	
    <@infoResourceAccessRights />
    
</#macro>

<#macro additionalInformation resource_>
    <#if resource_.resourceType != 'PROJECT'>
		<#assign map = resource_.relatedDatasetData />
		<#if map?? && !map.empty>
		    <h3>Additional Data</h3>
			<#list map?keys as key>
				<#if key?? && map.get(key)?? && key.visible?? && key.visible>
				   <b>${key.displayName}</b> : ${map.get(key) }<br/>
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
<#if _alwaysShow || _val!="" >
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

<#macro copyrightHolders>
<#if copyrightMandatory>
	<#if resource.copyrightHolder??>
		<h3>Primary Copyright Holder</h3>
		<@browse resource.copyrightHolder />
	</#if>
</#if>
</#macro>

<#macro license>
	<#if licensesEnabled>
		<#if resource.licenseType??>
			<h3>License</h3>
			<#if (resource.licenseType.imageURI != "")>
				<a href="${resource.licenseType.URI}"><img src="${resource.licenseType.imageURI}"/></a>
			</#if>
			<#if (resource.licenseType.URI != "")>
				<h4>${resource.licenseType.licenseName}</h4>
				<p><@s.property value="resource.licenseType.descriptionText"/></p>
				<p><a href="${resource.licenseType.URI}">view details</a></p>
			<#else>
				<p>${resource.licenseText}</p>
			</#if>		
		</#if>
	</#if>
</#macro>
</#escape>

