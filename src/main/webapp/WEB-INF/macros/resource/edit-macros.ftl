<#-- 
$Id$ 
Edit freemarker macros.  Getting large, should consider splitting this file up.
-->
<#-- include navigation menu in edit and view macros -->
<#escape _untrusted as _untrusted?html>
<#include "common.ftl">
<#import "../helptext.ftl" as  helptext>
<#import "/${themeDir}/settings.ftl" as settings>
<#include "navigation-macros.ftl">

<#macro basicInformation itemTypeLabel="file" itemPrefix="resource" isBulk=false>
<div class="well-alt" id="basicInformationSection">
    <h2>Basic Information</h2>
  <#if resource.id?? &&  resource.id != -1>
      <@s.hidden name="id"  value="${resource.id?c}" />
  </#if>
  
  <@s.hidden name="startTime" value="${currentTime?c}" />

        <div id="spanStatus" tooltipcontent="#spanStatusToolTip">   
        <@s.select label="Status" value="resource.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}'/>
     	</div>
        <#if resource.resourceType.project><span class="help-block">Note: project status does not affect status of child resources.</span></#if>
    
        <#-- TODO: use bootstrap tooltips (need to decide how to toggle. click? hover?) -->
		<@helptext.status />
<#if isBulk>

    <@s.hidden labelposition='left' id='resourceTitle' label='Title' name='image.title' cssClass="" value="BULK_TEMPLATE_TITLE"/>
    <@s.hidden labelposition='left' id='dateCreated' placeholder='YYYY' label='Year Created' name='image.date' cssClass="" value="-100"/>
    <@s.hidden id='ImageDescription' name='image.description' value="placeholder description"/>

<#else>
    <span
    tiplabel="Title"
    tooltipcontent="Enter the entire title, including sub-title, if appropriate.">
   
    <@s.textfield label="Title" id="resourceRegistrationTitle"  
        title="A title is required for all ${itemTypeLabel}s" name='${itemPrefix}.title' cssClass="required descriptiveTitle input-xxlarge" required=true maxlength="512"/>
</span>
    <#if resource.resourceType != 'PROJECT'>
    <span tiplabel="Year" tooltipcontent="Four digit year, e.g. 1966 or 2005.">
    <#local dateVal = ""/>
    <#if resource.date?? && resource.date != -1>
    <#local dateVal = resource.date?c />
    </#if>
    <@s.textfield label="Year" id='dateCreated' name='${itemPrefix}.date' value="${dateVal}" cssClass="reasonableDate required input-mini" required=true
      title="Please enter the year this ${itemTypeLabel} was created" />
    </#if>
    </span>
</#if>
    <#nested>
</div>

</#macro>

<#macro abstractSection itemPrefix="resource">
<div class="well-alt">
    <h2>Abstract / Description</h2>
    <span id="t-abstract" class="clear"
        tiplabel="Abstract / Description"
        tooltipcontent="Short description of the ${resource.resourceType.label}.">
		    <@s.textarea id='resourceDescription'  name='${itemPrefix}.description' cssClass='required resizable span6' required=true title="A description is required" />
        </span>
    
</div>
</#macro>

<#macro organizeResourceSection>
<div class="" id="organizeSection">
        <h2>${siteAcronym} Collections &amp; Project</h2>
        <h4>Add to a Collection</h4>
         <@edit.resourceCollectionSection />
        

        <#if !resource.resourceType.project>
        <@chooseProjectSection />
        </#if>   
</div>
</#macro>

<#macro chooseProjectSection>
    <#local _projectId = 'project.id' />
    <#if resource.id == -1 >
    <#local _projectId = request.getParameter('projectId')!'' />
    </#if>
        <div id="projectTipText" style="display:none;">
        ${settings.helptext['projectTipText']!"Select a project with which your ${resource.resourceType.label} will be associated. This is an important choice because it  will allow metadata to be inherited from the project further down this form"}
        </div>
        <h4>Choose a Project</h4>
        <div id="t-project" tooltipcontent="#projectTipText" tiplabel="Project">
        </div>
        <div class="control-group">
            <label class="control-label">Project</label>
            <div class="controls controls-row">
                <@s.select theme="simple" title="Please select a project" emptyOption='true' id='projectId' name='projectId' listKey='id' listValue='title' list='%{potentialParents}'
                truncate="70" value='${_projectId}' required="true"  cssClass="required input-xxlarge" />
                
            </div>
        </div>
            
        <div class="modal hide fade" id="inheritOverwriteAlert" tabindex="-1" role="dialog" aria-labelledby="validationErrorModalLabel" aria-hidden="true">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 id="validationErrorModalLabel">Overwrite Existing Values?</h3>
            </div>
            <div class="modal-body">
                <p>Inheriting values from <span class="labeltext">the parent project</span> would overwrite existing information in the following sections</p>
                <p class="list-container"></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger" id="btnInheritOverwriteOkay">Overwrite Existing Values</button>
                <button type="button" class="btn"  id="btnInheritOverwriteCancel" data-dismiss="modal" aria-hidden="true">Cancel</button>
            </div>
        </div> 
        
		<@helptext.inheritance />
        <div class="control-group" tiplabel="Inherit Metadata from Selected Project" tooltipcontent="#divSelectAllInheritanceTooltipContent" id="divInheritFromProject">
            <div class="controls">
                <label class="checkbox" for="cbSelectAllInheritance">
                    <input type="checkbox" value="true" id="cbSelectAllInheritance" class="">
                    <span id="spanCurrentlySelectedProjectText">Inherit from project.</span>
                </label>
            </div>
        </div>
</#macro>

<#macro resourceCollectionSection>
    <#local _resourceCollections = [blankResourceCollection] />
    <#if (resourceCollections?? && !resourceCollections.empty)>
    <#local _resourceCollections = resourceCollections />
    </#if>
	<@helptext.resourceCollection />
    <div tiplabel="${siteAcronym} Collections" tooltipcontent="#divResourceCollectionListTips">
    <p class="help-block">Collections enable you to organize and share resources within ${siteAcronym}</p>
    <table id="resourceCollectionTable" class="table repeatLastRow" addAnother="add another collection">
        <thead>
            <th colspan=2>Collection Name</th>
        </thead>
        <tbody>
            <#list _resourceCollections as resourceCollection>
            <@resourceCollectionRow resourceCollection resourceCollection_index/>
            </#list>
        </tbody>
    </table>
	</div>
</#macro>

<#macro keywordRows label keywordList keywordField showDelete=true addAnother="add another keyword">
    <div class="control-group repeatLastRow" id="${keywordField}Repeatable" data-add-another="${addAnother}">
        <label class="control-label">${label}</label>
        <#if keywordList.empty >
          <@keywordRow keywordField />
        <#else>
        <#list keywordList as keyword>
          <@keywordRow keywordField keyword_index showDelete />
        </#list>
        </#if>
    </div>
</#macro>

<#macro keywordRow keywordField keyword_index=0 showDelete=true>
    <div class="controls controls-row" id='${keywordField}Row_${keyword_index}_'>
        <@s.textfield theme="tdar" name='${keywordField}[${keyword_index}]' cssClass='input-xlarge keywordAutocomplete' placeholder="enter keyword"/>
        <#if showDelete>
        <@clearDeleteButton id="${keywordField}Row" />
        </#if>
    </div>
</#macro>


<#macro spatialContext showInherited=true>
<div class="well-alt" id="spatialSection">
    <h2>Spatial Terms</h2>
    <@inheritsection checkboxId="cbInheritingSpatialInformation" name='resource.inheritingSpatialInformation' showInherited=showInherited />
    <div id="divSpatialInformation">
 
        <div tiplabel="Spatial Terms: Geographic" tooltipcontent="Keyword list: Geographic terms relevant to the document, e.g. &quot;Death Valley&quot; or &quot;Kauai&quot;." >
        <@keywordRows "Geographic Terms" geographicKeywords 'geographicKeywords' />
        </div>
        <@helptext.geo />
        <h4>Geographic Region</h4>
        <div id='editmapv3' class="tdar-map-large googlemap"
            tiplabel="Geographic Coordinates"
            tooltipcontent="#geoHelpDiv"
            ></div>
        <div id="divManualCoordinateEntry" tooltipcontent="#divManualCoordinateEntryTip">
        <br />
            
            <@s.checkbox id="viewCoordinatesCheckbox" name="viewCoordinatesCheckbox" onclick="$('#explicitCoordinatesDiv').toggle(this.checked);" label='Enter / View Coordinates' labelposition='right'  />
            
            <script type="text/javascript">
            /* FIXME: move to tdar.common and clean-up selector for .latlong to be more specific */
                $(document).ready(function(){
                    $('#explicitCoordinatesDiv').toggle($('#viewCoordinatesCheckbox')[0].checked);
                    
                    $(".latLong").each(function(index, value){
                        $(this).hide();
                        //copy value of hidden original to the visible text input
                        var id = $(this).attr('id'); 
                        $('#d_' + id).val($('#' + id).val());
                    });
                });
                
            </script>
            <div id='explicitCoordinatesDiv' style='text-align:center;'>
            
                <table cellpadding="0" cellspacing="0" style="margin-left:auto;margin-right:auto;text-align:left;" >
                <tr>                                    
                <td></td>
                <td>
                <@s.textfield  theme="simple" name='latitudeLongitudeBoxes[0].maximumLatitude' id='maxy' size="14" cssClass="float latLong ne-lat" title="Please enter a valid Maximum Latitude" />
                <input type="text"  id='d_maxy'  placeholder="Latitude (max)" onChange='processLatLong(this)' class="ne-lat-display span2" />
                </td>
                <td></td>
                </tr>
                <tr>
                <td style="width:33%;text-align:center">
                    <@s.textfield theme="simple"  name="latitudeLongitudeBoxes[0].minimumLongitude" id='minx' size="14" cssClass="float latLong sw-lng" title="Please enter a valid Minimum Longitude" />
                    <input type="text"  id='d_minx'  placeholder="Longitude (min)"  onChange='processLatLong(this)' class="sw-lng-display span2" />
                </td>
                <td style="width:33%;text-align:center">
                    <input type="button" id="locate" value="Locate" class="btn locateCoordsButton" />
                </td>
                <td style="width:33%;text-align:center">
                    <@s.textfield theme="simple"  name="latitudeLongitudeBoxes[0].maximumLongitude" id='maxx' size="14" cssClass="float latLong ne-lng" title="Please enter a valid Maximum Longitude" />
                    <input type="text"  id='d_maxx'   placeholder="Longitude (max)" onChange='processLatLong(this)' class="ne-lng-display span2" />
                </td>
                </tr>
                <tr>
                <td></td>
                <td>
                    <@s.textfield theme="simple"  name="latitudeLongitudeBoxes[0].minimumLatitude" id="miny" size="14" cssClass="float latLong sw-lat" title="Please enter a valid Minimum Latitude" /> 
                    <input type="text" id="d_miny"  placeholder="Latitude (min)" onChange='processLatLong(this)' class="sw-lat-display span2" /> 
                </td>
                <td></td>
                </tr>           
                </table>
            </div>
            <@helptext.manualGeo />
        </div>
    </div>
</div>
</#macro>


<#macro resourceProvider showInherited=true>
<div class="well-alt" id="divResourceProvider" tiplabel="Resource Provider" tooltipcontent="The institution authorizing ${siteAcronym} to ingest the resource for the purpose of preservation and access.">
    <h2>Institution Authorizing Upload of this ${resource.resourceType.label}</h2>
    <@s.textfield label='Institution' name='resourceProviderInstitutionName' id='txtResourceProviderInstitution' cssClass="institution input-xxlarge" size='40'/>
    <br/>
</div>
</#macro>


<#macro temporalContext showInherited=true>
<div class="well-alt" id="temporalSection">
    <h2>Temporal Coverage</h2>
    <@inheritsection checkboxId="cbInheritingTemporalInformation" name='resource.inheritingTemporalInformation' showInherited=showInherited  />
    <div  id="divTemporalInformation">
        <div tiplabel="Temporal Term" tooltipcontent="Keyword list: Temporal terms relevant to the document, e.g. &quot;Pueblo IV&quot; or &quot;Late Archaic&quot;."></div>
        <@keywordRows "Temporal Terms" temporalKeywords 'temporalKeywords' true "add another temporal keyword" />
        <@coverageDatesSection />
    </div>
</div>
</#macro>

<#macro combineValues list=[]>
	<#compress>
		<#list list as item>
			<#if item_index !=0>,</#if>"${item?html}"
		</#list>
	</#compress>
</#macro>
<#macro combineValues2 list=[]>
	<#compress>
		<#list list as item>
			<#if item_index !=0>,</#if>${item?html}
		</#list>
	</#compress>
</#macro>


<#macro generalKeywords showInherited=true>

<div class="well-alt" 
    tiplabel="General Keywords"
    tooltipcontent="Keyword list: Select the artifact types discussed in the document.">   
    <h2>General Keywords</h2>
    <@inheritsection checkboxId="cbInheritingOtherInformation" name='resource.inheritingOtherInformation'  showInherited=showInherited />
    <div id="divOtherInformation">
        <@keywordRows "Keyword" otherKeywords 'otherKeywords' />
    </div>
    
    <#--fixme:  moving 'tagstrip' experiment out of divOtherInformation so existing inheritance code doesn't break -->
    <div class="row">
                <p><span class="label label-warning">FIXME:</span> replace lame keyword lists with fancy taglists (like the one below!)</p><br>
        <div class="control-group">
            <label class="control-label">Other Keywords</label>
            <div class="controls">
                <input type=text" name="test" id="otherKeywords" style="width:500px" value="<@combineValues2 otherKeywords/>"/>
            </div>
        </div>
        <script>
        $(document).ready(function() {
            $("#otherKeywords").select2({
                tags:[<@combineValues otherKeywords />],
                tokenSeparators: [";"]});
        });
        </script>
    </div>
</div>
</#macro>


<#macro sharedUploadFile divTitle="Upload">
<div class="well-alt" id="uploadSection">
    <h2>${divTitle}</h2>
    <div class='fileupload-content'>
        <#nested />
        <#-- XXX: verify logic for rendering this -->
        <#if multipleFileUploadEnabled || resource.hasFiles()>
        <h4>Current ${multipleFileUploadEnabled?string("and Pending Files", "File")}</h4>
        <table id="uploadFiles" class="files table tableFormat">
        </table>
        <table id="files" class='files sortable tableFormat'>
        <thead>
            <tr class="reorder <#if (fileProxies?size < 2 )>hidden</#if>">
                <th colspan=2>Reorder: <span class="link alphasort">Alphabetic</span> | <span class="link" onclick="customSort(this)">Custom</span>  </th>
            </tr>
        </thead>
        <tbody>
        <#list fileProxies as fileProxy>
            <#if fileProxy??>
            <@fileProxyRow rowId=fileProxy_index filename=fileProxy.filename filesize=fileProxy.size fileid=fileProxy.fileId action=fileProxy.action versionId=fileProxy.originalFileVersionId/>
            </#if>
        </#list>
        <#if fileProxies.empty>
        <tr class="noFiles newRow">
        <td><em>no files uploaded</em></td>
        </tr>
        </#if>
        </tbody>
        </table>
        </#if>
    </div>
    <@helptext.confidentialFile />
</div>
</#macro>

<#macro siteKeywords showInherited=true divTitle="Site Information">
<div class="well-alt" id="siteSection">
    <h2>${divTitle}</h2>
    <@inheritsection checkboxId='cbInheritingSiteInformation' name='resource.inheritingSiteInformation'  showInherited=showInherited />
    <div id="divSiteInformation">
    <@helptext.siteName />
        <@keywordRows "Site Name" siteNameKeywords 'siteNameKeywords' />
        
        <div class="control-group">
            <label class="control-label">Site Type</label>
            <div class="controls">
                <@s.checkboxlist theme="hier" name="approvedSiteTypeKeywordIds" keywordList="approvedSiteTypeKeywords" />
            </div>
        </div>
        
        <@keywordRows "Other" uncontrolledSiteTypeKeywords 'uncontrolledSiteTypeKeywords' />
    </div>
</div>
</#macro>


<#macro materialTypes showInherited=true>
<div class="well-alt">
<@helptext.materialType />
    <h2>Material Types</h2>
    <@inheritsection checkboxId='cbInheritingMaterialInformation' name='resource.inheritingMaterialInformation'  showInherited=showInherited />
    <div id="divMaterialInformation">
        <@s.checkboxlist name='materialKeywordIds' list='allMaterialKeywords' listKey='id' listValue='label' listTitle="definition"  label="Select Type(s)"
            spanSize="2" numColumns="3" cssClass="smallIndent" />
    </div>      
</div>

</#macro>

<#macro culturalTerms showInherited=true inline=false>
<div  <#if !inline> class="well-alt" </#if>>
<@helptext.cultureTerms />
    <h2>Cultural Terms</h2>
    <@inheritsection checkboxId="cbInheritingCulturalInformation" name='resource.inheritingCulturalInformation'  showInherited=showInherited />
    <div id="divCulturalInformation">
        <div class="control-group">
            <label class="control-label">Culture</label>
            <div class="controls">
                <@s.checkboxlist theme="hier" name="approvedCultureKeywordIds" keywordList="approvedCultureKeywords" />
            </div>
        </div>
        
        <!--"add another cultural term" -->
        <@keywordRows "Other" uncontrolledCultureKeywords 'uncontrolledCultureKeywords' />
    </div>
</div>
</#macro>

<#macro uncontrolledCultureKeywordRow uncontrolledCultureKeyword_index=0>
            <tr id='uncontrolledCultureKeywordRow_${uncontrolledCultureKeyword_index}_'>
            <td>
                <@s.textfield name='uncontrolledCultureKeywords[${uncontrolledCultureKeyword_index}]' cssClass=' input-xxlarge cultureKeywordAutocomplete' autocomplete="off" />
                </td><td><@clearDeleteButton id="uncontrolledCultureKeywordRow" />
            </td>
            </tr>
</#macro>

<#macro investigationTypes showInherited=true >
<div class="well-alt" tiplabel="Investigation Types" tooltipcontent="#investigationtypehelp" id="investigationSection">
    <h2>Investigation Types</h2>
    <@inheritsection checkboxId='cbInheritingInvestigationInformation' name='resource.inheritingInvestigationInformation'  showInherited=showInherited />
    <div id="divInvestigationInformation">
        <@s.checkboxlist name='investigationTypeIds' list='allInvestigationTypes' listKey='id' listValue='label' numColumns="2" spanSize="3" 
            label="Select Type(s)" listTitle="definition" />
    </div>
</div>
<@helptext.investigationType />
</#macro>


<#-- provides a fieldset just for full user access -->
<#macro fullAccessRights tipsSelector="#divAccessRightsTips">
<#local _authorizedUsers=authorizedUsers />
<#if _authorizedUsers.empty><#local _authorizedUsers=[blankAuthorizedUser]></#if>
<@helptext.accessRights />

<div id="divAccessRights" class="well-alt" tooltipcontent="${tipsSelector}">
<h2><a name="accessRights"></a>Access Rights</h2>
<h4>Users who can view or modify this resource</h4>

<div id="accessRightsRecords" class="repeatLastRow" data-addAnother="add another user">
    <div class="control-group">
        <label class="control-label">Users</label>
        <#list _authorizedUsers as authorizedUser>
            <#if authorizedUser??>
            	<@userRow person=authorizedUser.user _indexNumber=authorizedUser_index includeRole=false _personPrefix="user" prefix="authorizedUsers" includeRights=true isUser=true includeRepeatRow=true includeDelete=true/>
            </#if>
        </#list>
    </div>
</div>

<#nested>

 <#if persistable.resourceType??>
  <@resourceCollectionsRights effectiveResourceCollections >
  Note: this does not reflect changes to resource collection you have made until you save.
  </@resourceCollectionsRights>
 </#if>

</div>
</#macro>


<#macro categoryVariable>
<div class="control-group row">
	<div id='categoryDivId' class="span4">
	<@s.select labelposition='left' label='Category' id='categoryId' name='categoryId' 
	    onchange='changeSubcategory("#categoryId","#subcategoryId")'
	                autocompleteName="sortCategoryId"
	    listKey='id' listValue='name' emptyOption='true' list='%{allDomainCategories}' />
	</div>
	<div id='subcategoryDivId' class="span3">
	<@s.select labelposition='left' label='Subcategory' id='subcategoryId' name='subcategoryId' 
	    autocompleteName="subCategoryId" headerKey="-1" listKey='id' headerValue="N/A" list='%{subcategories}'/>
	</div>
</div>
</#macro>


<#macro singleFileUpload typeLabel="${resource.resourceType.label}">
<div class="control-group"
        tiplabel="Upload your ${typeLabel}" 
        tooltipcontent="The metadata entered on this form will be associated with this file. We accept the following formats: 
                        <@join sequence=validFileExtensions delimiter=", "/>">
    <label for="fileUploadField" class="control-label">${typeLabel}</label>
    <div class="controls">
        <@s.file theme="simple" name='uploadedFiles'  cssClass="validateFileType input-xxlarge" id="fileUploadField" labelposition='left' size='40' />
        <span class="help-block">Valid file types include: <@join sequence=validFileExtensions delimiter=", "/></span>
    </div>
    <#nested>
</div>
</#macro>

<#macro manualTextInput typeLabel type uploadOptionText manualEntryText>
<#-- show manual option by default -->
<#local usetext=(resource.getLatestVersions().isEmpty() || (fileTextInput!"") != "")>
<div>
    <h3>${(resource.id == -1)?string("Submit", "Replace")} ${typeLabel}</h3>
    <div class="control-group">
        <label class='control-label' for='inputMethodId'>Submit as</label>
        <div class="controls">
            <select id='inputMethodId' name='fileInputMethod' onchange='refreshInputDisplay()' class="input-xxlarge">
                <option value='file' <#if !usetext>selected="selected"</#if>>${uploadOptionText}</option>
                <option value='text' <#if usetext>selected="selected"</#if>>${manualEntryText}</option>
            </select>
        </div>
    </div>

    <div id='uploadFileDiv' style='display:none;'>
        <div id="uploadFileExampleDiv" class="control-group">
            <div class="controls">
                <#nested 'upload'>
            </div>
        </div>
        <@singleFileUpload />
    </div>
    
    <div id='textInputDiv'>
        <div id="textInputExampleDiv" class="control-group">
            <div class="controls">
                <#nested 'manualEntry'>
            </div>
        </div>
        <@s.textarea label='${typeLabel}' labelposition='top' id='fileInputTextArea' name='fileTextInput' rows="5" cssClass='resizable input-xxlarge' />
    </div>
</div>

</#macro>

<#macro submit label="Save" fileReminder=true buttonid="submitButton">
<div class="errorsection"> 
    <#if fileReminder>
    <div id="reminder" class="row">
        <p><span class="label label-info">Reminder</span> No files are attached to this record. </p>
    </div>
    <div id="error" class="row"><ul></ul></div>
    </#if>     
    <div class="form-actions">
    <#nested>
    <@submitButton label=label id=buttonid />
       <img src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="visibility:hidden"/>
    </div> 

<div class="modal hide fade" id="validationErrorModal" tabindex="-1" role="dialog" aria-labelledby="validationErrorModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="validationErrorModalLabel">Validation Errors</h3>
    </div>
    <div class="modal-body">
        <h4>Please correct the following errors</h4>
        <p></p>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
    </div>
</div> 

</#macro>

<#macro submitButton label="submit" id="">
    <input type="submit" class='btn btn-primary submitButton' name="submitAction" value="${label}"  <#if id?has_content>id="${id}"</#if>>
</#macro>

<#macro resourceJavascript formSelector="#resourceMetadataForm" selPrefix="#resource" includeAsync=false includeInheritance=false>
<#noescape>
<script type='text/javascript'>

var formSelector = "${formSelector}";
var includeInheritance = ${includeInheritance?string("true", "false")};
var acceptFileTypes  = <@edit.acceptedFileTypesRegex />;
/*

 * FIXME: move to common.js
 */
$(function(){
    'use strict';
    var form = $(formSelector)[0];
    
    <#if includeAsync>
    //init fileupload
    var id = $('input[name=id]').val();
    TDAR.fileupload.registerUpload({informationResourceId: id, acceptFileTypes: acceptFileTypes, formSelector:"${formSelector}"});
    </#if>

    TDAR.common.initEditPage(form);
    
    //register maps, if any
    if($('#divSpatialInformation').length) {
        $(function() {
            //fixme: implicitly init when necessary
            TDAR.maps.initMapApi();
            var mapdiv = $('#editmapv3')[0];
            var inputCoordsContainer = $("#explicitCoordinatesDiv")[0];
            TDAR.maps.setupEditMap(mapdiv, inputCoordsContainer);
        });
    }
    
<#if includeInheritance>
var project = ${projectAsJson};
applyInheritance(project, formSelector);
</#if>    
    
    
});
<#nested>
</#noescape>
</script>
  
</#macro>



<#macro parentContextHelp element="div" resourceType="resource" valueType="values">
<${element} tiplabel="Inherited Values" tooltipcontent="The parent project for this ${resourceType} defines ${valueType} for this section.  You may also define your own, but note that they will not override the values defined by the parent.">
<#nested>
</${element}>
</#macro>

<#macro relatedCollections showInherited=true>
<#local _sourceCollections = sourceCollections />
<#local _relatedComparativeCollections = relatedComparativeCollections />
<#if _sourceCollections.empty><#local _sourceCollections = [blankSourceCollection] /></#if>
<#if _relatedComparativeCollections.empty><#local _relatedComparativeCollections = [blankRelatedComparativeCollection] /></#if>
<div class="well-alt" id="relatedCollectionsSectionGlide">
    <h2>Museum or Archive Collections</h2>
    <@inheritsection checkboxId="cbInheritingCollectionInformation" name='resource.inheritingCollectionInformation' showInherited=showInherited />
    <div id="relatedCollectionsSection" >
        <div id="divSourceCollectionControl" class="control-group">
            <label class="control-label">Source Collection</label>
            <#list _sourceCollections as sourceCollection>
	            <@sourceCollectionRow sourceCollection "sourceCollection" sourceCollection_index/>
            </#list>
        </div>
    
        <div id="divRelatedComparativeCitationControl" class="control-group">
            <label class="control-label">Related or Comparative Collection</label></label>
            <#list _relatedComparativeCollections as relatedComparativeCollection>
    	        <@sourceCollectionRow relatedComparativeCollection "relatedComparativeCollection" relatedComparativeCollection_index/>
            </#list>
        </div> 
		<@helptext.sourceRelatedCollection />    
    </div>
</div>
</#macro>

<#macro sourceCollectionRow sourceCollection prefix index=0>
<#local plural = "${prefix}s" />
    <div class="controls control-row">
    <@s.hidden name="${plural}[${index}].id" />
    <@s.textarea theme="tdar" name='${plural}[${index}].text' cssClass="input-xxlarge" /></td>
    <@edit.clearDeleteButton id="${prefix}Row" />
    </div>
</#macro>

<#macro inheritsection checkboxId name showInherited=true  label="Inherit this section" >
    <div class='divInheritSection'>
    <#if showInherited>
        <div class="control-group alwaysEnabled">
            <div class="controls">
                <label class="checkbox">
                    <@s.checkbox theme="simple" name="${name}" id="${checkboxId}" />
                    <span class="labeltext">${label}</span>
                </label>
            </div>
        </div>
            
    <#elseif resource??>
         <@inheritTips id="${checkboxId}" />
    </#if>
    </div>    
</#macro>

<#macro resourceCollectionRow resourceCollection collection_index = 0 type="internal">
      <tr id="resourceCollectionRow_${collection_index}_" class="repeat-row">
          <td> 
              <@s.hidden name="resourceCollections[${collection_index}].id"  id="resourceCollectionRow_${collection_index}_id" />
              <@s.textfield id="resourceCollectionRow_${collection_index}_id" name="resourceCollections[${collection_index}].name" cssClass="input-xlarge collectionAutoComplete "  autocomplete="off"
              autocompleteIdElement="#resourceCollectionRow_${collection_index}_id" label="${siteAcronym} Collection"
              autocompleteParentElement="#resourceCollectionRow_${collection_index}_" />
          </td>
          <td><@clearDeleteButton id="resourceCollectionRow" /> </td>
      </tr>
</#macro>



<#macro resourceNoteSection showInherited=true>
<div class="well-alt" id="resourceNoteSectionGlide">
    <#local _resourceNotes = resourceNotes />
    <#if _resourceNotes.empty >
    <#local _resourceNotes = [blankResourceNote] />
    </#if>
    <div class="hidden" tiplabel="Notes"  tooltipcontent="Use this section to append any notes that may help clarify certain aspects of the resource.  For example, 
    a &quot;Redaction Note&quot; may be added to describe the rationale for certain redactions in a document."></div>
    <h2>Notes</h2>
    <@inheritsection checkboxId="cbInheritingNoteInformation" name='resource.inheritingNoteInformation' showInherited=showInherited />
    <div id="resourceNoteSection" class="control-group repeatLastRow">
        <label class="control-label">Type / Contents</label>
        <#list _resourceNotes as resourceNote>
        <#if resourceNote??><@noteRow resourceNote resourceNote_index/></#if>
        </#list>
    </div>
</div>
</#macro>

<#macro noteRow proxy note_index=0>
      <div id="resourceNoteRow_${note_index}_" class="repeat-row">
          <div class="controls controls-row">
              <@s.hidden name="resourceNotes[${note_index}].id" />
              <@s.select theme="tdar" emptyOption='false' name='resourceNotes[${note_index}].type' list='%{noteTypes}' listValue="label" /> 
          <@clearDeleteButton id="resourceNoteRow" />
          </div>
          <div class="controls">
              <@s.textarea theme="tdar" name='resourceNotes[${note_index}].note' placeholder="enter note contents" cssClass='resizable input-xxlarge'  rows='3' maxlength='5000' />
          </div>
      </div>
</#macro>




<#macro coverageDatesSection>
<#local _coverageDates=coverageDates />
<#if _coverageDates.empty><#local _coverageDates = [blankCoverageDate] /></#if>
<@helptext.coverageDates />
<div class="control-group repeatLastRow" id="coverageDateRepeatable" data-add-another="add another coverage date" tooltipcontent="#coverageDatesTip">
    <label class="control-label">Coverage Dates</label>
    
    <#list _coverageDates as coverageDate>
    <#if coverageDate??>
    <@dateRow coverageDate coverageDate_index/>
    </#if>
    </#list>
</div>

</#macro>



<#macro dateRow proxy=proxy proxy_index=0>
<div class="controls controls-row" id="DateRow_${proxy_index}_">
        <@s.hidden name="coverageDates[${proxy_index}].id" />
        <@s.select theme="tdar"name="coverageDates[${proxy_index}].dateType" cssClass="coverageTypeSelect input-medium"
            listValue='label'  headerValue="Date Type" headerKey="NONE"
            list=allCoverageTypes />
        <@s.textfield theme="tdar" placeholder="Start Year" cssClass="coverageStartYear input-small" name="coverageDates[${proxy_index}].startDate" maxlength="10" /> 
        <@s.textfield theme="tdar" placeholder="End Year" cssClass="coverageEndYear input-small" name="coverageDates[${proxy_index}].endDate" maxlength="10" />
        <@s.textfield theme="tdar" placeholder="Description"  cssClass="coverageDescription input-xlarge" name="coverageDates[${proxy_index}].description" />
       <@edit.clearDeleteButton id="{proxy_index}DateRow"/>
</div>
</#macro>


<#macro allCreators sectionTitle proxies prefix inline=false showInherited=false>
    <@resourceCreators sectionTitle proxies prefix inline showInherited />
    <style>
    </style>
</#macro>

<#macro resourceCreators sectionTitle proxies prefix inline=false showInherited=false>
<#if !inline>
<div class="well-alt" tiplabel="${sectionTitle}" 
	id="${prefix}Section"
	tooltipcontent="Use these fields to properly credit individuals and institutions for their contribution to the resource. Use the '+' sign to add fields for either persons or institutions, and use the drop-down menu to select roles">
    <h2>${sectionTitle}</h2>
<#else>
<label class="toplabel">${sectionTitle}</label> <br />
</#if>

    <table 
        id="${prefix}Table"
        class="table repeatLastRow creatorProxyTable">
        <tbody>
            <#if proxies?has_content >
              <#list proxies as proxy>
                <@creatorProxyRow proxy  prefix proxy_index/>
              </#list>
            <#else>
              <@creatorProxyRow blankCreatorProxy prefix 0 />
            </#if>
        </tbody>
    </table>
<#if !inline>
</div>
</#if>

</#macro>


<#macro creatorProxyRow proxy=proxy prefix=prefix proxy_index=proxy_index type_override="NONE" required=false includeRole=true>
    <#assign relevantPersonRoles=personAuthorshipRoles />
    <#assign relevantInstitutionRoles=institutionAuthorshipRoles />
    <#if prefix=='credit'>
        <#assign relevantPersonRoles=personCreditRoles />
        <#assign relevantInstitutionRoles=institutionCreditRoles />
    </#if>

    <#if proxy??>

    <tr id="${prefix}Row_${proxy_index}_" class="repeat-row">
          <#assign creatorType = proxy.actualCreatorType!"PERSON" />
         <td>
         	<div class="btn-group creator-toggle-button" data-toggle="buttons-radio">
	           <button type="button" class="btn btn-small personButton <#if type_override == "PERSON" || (creatorType=='PERSON' && type_override=='NONE') >btn-active active</#if>" data-toggle="button">Person</button>
	           <button type="button" class="btn btn-small institutionButton <#if creatorType =='INSTITUTION' || type_override == "INSTITUTION">btn-active active</#if>" data-toggle="button">Institution</button>
			</div>
		</td>
        <td>
        	<@userRow person=proxy.person _indexNumber=proxy_index _personPrefix="person" prefix="${prefix}Proxies" 
        		includeRole=includeRole hidden=(creatorType =='INSTITUTION' || type_override == "INSTITUTION") required=(creatorType=='PERSON' && required) />

	        <@institutionRow institution=proxy.institution _indexNumber=proxy_index includeRole=includeRole _institutionPrefix="institution" prefix="${prefix}Proxies" 
	        	hidden=(type_override == "PERSON" || (creatorType=='PERSON' && type_override=='NONE')) required=(creatorType=='INSTITUTION' && required)/>
        </td>
        <td>
            <button class="btn  btn-mini repeat-row-delete " type="button" tabindex="-1" onclick="deleteParentRow(this)"><i class="icon-trash"></i></button>
        </td>
    </tr>
    </#if>
</#macro>


<#macro identifiers showInherited=true>
    <#local _resourceAnnotations = resourceAnnotations />
    <#if _resourceAnnotations.empty>
    <#local _resourceAnnotations = [blankResourceAnnotation] />
    </#if>
    <div class="well-alt" id="divIdentifiersGlide" tiplabel="${resource.resourceType.label} Specific or Agency Identifiers" tooltipcontent="#divIdentifiersTip">
		<@helptext.identifiers />
        <h2>${resource.resourceType.label} Specific or Agency Identifiers</h2>
        <@inheritsection checkboxId="cbInheritingIdentifierInformation" name='resource.inheritingIdentifierInformation' showInherited=showInherited />
        <div id="divIdentifiers">
        <table id="resourceAnnotationsTable" class="table repeatLastRow" addAnother="add another identifier" >
            <tbody>
                <#list _resourceAnnotations as annotation>
                    <@displayAnnotation annotation annotation_index/>
                </#list>
            </tbody>
        </table>
        </div>
    </div>

</#macro>

<#macro displayAnnotation annotation annotation_index=0>
    <tr id="resourceAnnotationRow_${annotation_index}_" class="repeat-row">
        <td >
            <div class="control-group">
            <label class="control-label">Name / Value</label>
                <div class="controls controls-row ">
                    <@s.textfield theme="tdar" placeholder="Name" cssClass="annotationAutoComplete span3" name='resourceAnnotations[${annotation_index}].resourceAnnotationKey.key' value='${annotation.resourceAnnotationKey.key!""}'  autocomplete="off" />
                    <@s.textfield theme="tdar" placeholder="Value" cssClass="span3" name='resourceAnnotations[${annotation_index}].value'  value='${annotation.value!""}' />
                </div>
            </div>            
        </td>
        <td><@clearDeleteButton id="resourceAnnotationRow" /></td>                        
    </tr>

</#macro>
<#macro join sequence=[] delimiter=",">
  <#if sequence?has_content>
    <#list sequence as item>
        ${item}<#if item_has_next><#noescape>${delimiter}</#noescape></#if><#t>
    </#list>
  </#if>
</#macro>

<#-- 
FIXME: this appears to only be used for Datasets.  Most of it has been extracted out
to singleFileUpload, continue lifting useful logic here into singleFileUpload (e.g.,
jquery validation hooks?)
-->
<#macro upload uploadLabel="File" showMultiple=false divTitle="Upload File" showAccess=true>
    <@sharedUploadFile>
      <@singleFileUpload>
          <div class="field indentFull">
          <@s.select name="fileProxies[0].restriction" id="cbConfidential" labelposition="right" label="This item has access restrictions" listValue="label" list=fileAccessRestrictions  />
          <div><b>NOTE:</b> by changing this from 'public', only the metadata will be visible to users, they will not be able to view this item.  
          You may explicity grant read access to users below.</div>
          <br />     
          </div>
      </@singleFileUpload>
    </@sharedUploadFile>
</#macro>


<#macro asyncFileUpload uploadLabel="Attach Files" showMultiple=false divTitle="Upload" divId="divFileUpload" >
<div id="${divId}" class="well-alt">
    <@s.hidden name="ticketId" id="ticketId" />
    <h2>${uploadLabel}</h2>
    <div class="row fileupload-buttonbar">
        <div class="span2">
            <!-- The fileinput-button span is used to style the file input field as button -->
            <span class="btn btn-success fileinput-button">
                <i class="icon-plus icon-white"></i>
                <span>Add files...</span>
            <input type="file" name="uploadFile" multiple="multiple" 
                data-form-data='{"ticketId":$("#ticketId").val()}'>
            </span>
            <#-- we don't want the 'bulk operations' for now,  might be handy later -->
            <#--
            <button type="submit" class="btn btn-primary start">
                <i class="icon-upload icon-white"></i>
                <span>Start upload</span>
            </button>
            <button type="reset" class="btn btn-warning cancel">
                <i class="icon-ban-circle icon-white"></i>
                <span>Cancel upload</span>
            </button>
            <button type="button" class="btn btn-danger delete">
                <i class="icon-trash icon-white"></i>
                <span>Delete</span>
            </button>
            <input type="checkbox" class="toggle">
           -->
        </div>
    <#if validFileExtensions??>
    <span class="help-block">
        Accepted file types: .<@join validFileExtensions ", ." />
    </span>
    </#if>
        <!-- The global progress information -->
        <div class="span5 fileupload-progress fade">
            <!-- The global progress bar -->
            <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
                <div class="bar" style="width:0%;"></div>
            </div>
            <!-- The extended global progress information -->
            <div class="progress-extended">&nbsp;</div>
        </div>
    </div>
    <!-- The loading indicator is shown during file processing -->
    <div class="fileupload-loading"></div>
    <br />
        <!-- The table listing the files available for upload/download -->

                <div class="reorder <#if (fileProxies?size < 2 )>hidden</#if>">
                    Reorder: <span class="link alphasort">Alphabetic</span> | <span class="link" onclick="customSort(this)">Custom</span>  
                </div>

        <table id="files" role="presentation" class="tableFormat table table-striped sortable">
            <thead>
               <th><!--preview-->&nbsp;</th>
               <th>Name</th>
               <th>Size</th>
               <th colspan="2">Access Restrictions</th>
               <th colspan="2">Action</th>
            </thead>
            <tbody id="fileProxyUploadBody" class="files"></tbody>
            <#list fileProxies as fileProxy>
                <#if fileProxy??>
                <@fileProxyRow rowId=fileProxy_index filename=fileProxy.filename filesize=fileProxy.size fileid=fileProxy.fileId action=fileProxy.action versionId=fileProxy.originalFileVersionId/>
                </#if>
            </#list>
        </table>
    
</div>
</#macro>

<#macro fileProxyRow rowId="{ID}" filename="{FILENAME}" filesize="{FILESIZE}" action="ADD" fileid=-1 versionId=-1>
<tr id="fileProxy_${rowId}" class="${(fileid == -1)?string('newrow', '')} sortable template-download fade existing-file in">
            <td class="preview"></td>
            <td class="name">
                        
                <a href="<@s.url value='/filestore/${versionId?c}/get'/>" title="${filename?html}" download="${filename?html}">${filename?html}</a>
                 
                <span class="replacement-text" style="display:none"></span>
            </td>
            <td class="size"><span>${filesize} bytes</span></td>
            <td colspan="2">
		<div class="control-group">
		
		    <div class="controls">
		        <@s.select id="proxy${rowId}_conf"  name="fileProxies[${rowId}].restriction" labelposition="right" 
		        style="padding-left: 20px;" list=fileAccessRestrictions listValue="label"  class="fileProxyConfidential" onchange="TDAR.fileupload.updateFileAction(this)" style="padding-left: 20px;" />
		    </div> 
		</div>
		</td>
        
        <td class="delete">
                <button class="btn btn-danger delete-button" data-type="DELETE" data-url="">
                    <i class="icon-trash icon-white"></i><span>Delete</span>
                </button>
        </td>
        <td>
            
          <div class="btn-group">
            <button class="btn btn-warning disabled dropdown-toggle replace-button" disabled="" data-toggle="dropdown">Replace <span class="caret"></span></button>
            <ul class="dropdown-menu" id="tempul">
              <li><a href="#">file 1</a></li>
              <li><a href="#">file 2</a></li>
              <li class="divider"></li>
              <li><a href="#">cancel replace operation</a></li>
            </ul>
          </div> 

        <input type="hidden" class="fileAction" name="fileProxies[${rowId}].action" value="${action}"/>
        <input type="hidden" class="fileId" name="fileProxies[${rowId}].fileId" value="${fileid?c}"/>
        <input type="hidden" class="fileReplaceName" name="fileProxies[${rowId}].filename" value="${filename}"/>
        <input type="hidden" class="fileSequenceNumber" name="fileProxies[${rowId}].sequenceNumber" value=${rowId} />
            
        </td>
    </tr>
</#macro>

<#macro citationInfo prefix="resource" includeAbstract=true >
     <#if !resource.resourceType.codingSheet && !resource.resourceType.ontology>
<div id="citationInformation" class="well-alt"> 
    <h2>Additional Citation Information</h2>

    
    <div tiplabel="Department / Publisher Location" tooltipcontent="Department name, or City,State (and Country, if relevant)">
        <span id="publisher-hints"  book="Publisher" book_section="Publisher" journal_article="Publisher"  conference="Conference" thesis="Institution" other="Publisher">
		    <@s.textfield id='publisher' label="Publisher" name='publisherName' cssClass="institution input-xxlarge"  />
        </span>

	    <span id="publisherLocation-hints" book="Publisher Loc." book_section="Publisher Loc." journal_article="Publisher Loc." conference="Location"  thesis="Department" other="Publisher Loc.">
		    <@s.textfield id='publisherLocation' label="Publisher Loc." name='${prefix}.publisherLocation' cssClass='input-xxlarge' />
	    </span>
	</div>
    <#nested />

    <div id="divUrl" tiplabel="URL" tooltipcontent="Website address for this resource, if applicable">
	    <@s.textfield name="${prefix}.url" id="txtUrl" label="URL" labelposition="left" cssClass="url input-xxlarge" placeholder="http://" />
    </div>
    
</div>
    </#if>
    <#if includeAbstract>
        <@abstractSection "${prefix}" />
    </#if>

    <#if resource.resourceType.label?lower_case != 'project'>
        <@copyrightHolders 'Primary Copyright Holder *' copyrightHolderProxy />
    </#if>
</#macro>

<#macro sharedFormComponents showInherited=true fileReminder=true prefix="${resource.resourceType.label?lower_case}">
    <@organizeResourceSection />
    <#if !resource.resourceType.project>
      <@resourceProvider showInherited />
      <#if licensesEnabled?? && licensesEnabled >
          <@edit.license />
      </#if>
      <#if copyrightEnabled??>
          <@edit.copyrightHolders />
      </#if>
    </#if>
    <@resourceCreators 'Individual and Institutional Roles' creditProxies 'credit' false showInherited />

    <@identifiers showInherited />

    <@spatialContext showInherited />
    <@temporalContext showInherited />

    <@investigationTypes showInherited />
    
    <@materialTypes showInherited />

    <@culturalTerms showInherited />

    <@siteKeywords showInherited />
    
    <@generalKeywords showInherited />

    <@resourceNoteSection showInherited />

    <#if !resource.resourceType.document>
      <@relatedCollections showInherited />
    </#if>
    
    <@edit.fullAccessRights />
    
    <#if !resource.resourceType.project>
      <@edit.submit fileReminder=((resource.id == -1) && fileReminder) />
    <#else>
      <@edit.submit fileReminder=false />
    </#if>
</#macro>

<#macro title>
<#if resource.id == -1>
<title>Create a new ${resource.resourceType.label}</title>
<#else>
<title>Editing ${resource.resourceType.label} Metadata for ${resource.title} (${siteAcronym} id: ${resource.id?c})</title>
</#if>
</#macro>

<#macro sidebar>
<div id="sidebar-right" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the page for editing metadata associated with ${resource.resourceType.plural}.
    </div>
</div>
</#macro>


<#macro inheritTips id>
    <div id="${id}hint" class="inherit-tips">
        <em>Note: This section supports <strong>inheritance</strong>: values can be re-used by resources associated with your project.</em>
    </div>
</#macro>


<#macro resourceDataTable showDescription=true selectable=false>
<div>
<@s.textfield name="query" id="query" label="Title" cssClass='input-xlarge' /><br/>
<div class="row">
<div class="span4">
	<div class="control-group">
		<label class="control-label" for="project-selector">Project</label>
		<div class="controls">
			<select id="project-selector">
			    <option value="" selected='selected'>All Editable Projects</option>
			  <#if allSubmittedProjects?? && !allSubmittedProjects.empty>
			  <optgroup label="Your Projects">
			    <@s.iterator value='allSubmittedProjects' status='projectRowStatus' var='submittedProject'>
			        <option value="${submittedProject.id?c}" title="${submittedProject.title!""?html}"><@truncate submittedProject.title 70 /> </option>
			    </@s.iterator>
			  </optgroup>
			  </#if>
			  
			  <optgroup label="Projects you have been given access to">
			    <@s.iterator value='fullUserProjects' var='editableProject'>
			        <option value="${editableProject.id?c}" title="${editableProject.title!""?html}"><@truncate editableProject.title 70 /></option>
			    </@s.iterator>
			  </optgroup>
			</select>
		</div>
	</div>
</div>
<div class="span4">
	<div class="control-group">
		<label class="control-label" for="collection-selector">Collection</label>
		<div class="controls">
			<select id="collection-selector">
			    <option value="" selected='selected'>All Collections</option>
			    <@s.iterator value='resourceCollections' var='rc'>
			        <option value="${rc.id?c}" title="${rc.name!""?html}"><@truncate rc.name!"(No Name)" 70 /></option>
			    </@s.iterator>
			</select>
		</div>
	</div>
</div>
</div>
<div class="row">
	<div class="span4">
	    <@s.select labelposition='left' id="statuses" headerKey="" headerValue="Any" label='Status' name='status'  emptyOption='false' listValue='label' list='%{statuses}'/></span>
	</div>
	<div class="span4">
	    
	    <@s.select labelposition='left' id="resourceTypes" label='Resource Type' name='resourceType'  headerKey="" headerValue="All" emptyOption='false' listValue='label' list='%{resourceTypes}'/></span>
	</div>
</div>

    <@s.select labelposition='left' label='Sort By' emptyOption='false' name='sortBy' 
     listValue='label' list='%{resourceDatatableSortOptions}' id="sortBy"
     value="ID_REVERSE" title="Sort resource by" />

<!-- <ul id="proj-toolbar" class="projectMenu"><li></li></ul> -->
</div>
<table cellpadding="0" cellspacing="0" border="0" class="display tableFormat table-striped table-bordered span8" id="resource_datatable" >
<thead>
     <tr>
         <#if selectable><th><input type="checkbox" onclick="checkAllToggle()" id="cbCheckAllToggle">id</th></#if>
         <th>Title</th>
         <th>Type</th>
     </tr>
</thead>
<tbody>
</tbody>
</table>
<br/>
<script>
function checkAllToggle() {
var unchecked = $('#resource_datatable td input[type=checkbox]:unchecked');
var checked = $('#resource_datatable td input[type=checkbox]:checked');
  if (unchecked.length > 0) {
    $(unchecked).click();
  } else {
    $(checked).click();
  }
}

</script>

</#macro>


<#macro resourceDataTableJavascript showDescription=true selectable=false >
<script type="text/javascript">

var $dataTable = null; //define at page-level, set after onload
 
var datatable_isAdministrator = ${administrator?string};
var datatable_isSelectable = ${selectable?string};
var datatable_showDescription = ${showDescription?string};
 
 
 /* FIXME: move to tdar.common.js */
 function projToolbarItem(link, image, text) {
    return '<li><a href="' + link + '"><img alt="toolbar item" src="' + image + '"/>' + text + '</a></li>';
 }
 
$(function() {
    // set the project selector to the last project viewed from this page
    // if not found, then select the first item 
    var prevSelected = $.cookie("tdar_datatable_selected_project");
    if (prevSelected != null) {
        var elem = $('#project-selector option[value=' + prevSelected + ']');
        if(elem.length) {
            elem.attr("selected", "selected");
        } else {
            $("#project-selector").find("option :first").attr("selected", "selected");
        }

    }
    drawToolbar($("#project-selector").val());
    var prevSelected = $.cookie("tdar_datatable_selected_collection");
    if (prevSelected != null) {
        var elem = $('#collection-selector option[value=' + prevSelected + ']');
        if(elem.length) {
            elem.attr("selected", "selected");
        } else {
            $("#collection-selector").find("option :first").attr("selected", "selected");
        }

    }

    jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages =3;
	$.extend( $.fn.dataTableExt.oStdClasses, {
	    "sWrapper": "dataTables_wrapper form-inline"
	} );
//        sDom:'<"datatabletop"ilrp>t<>', //omit the search box
	    
	  var aoColumns_ = [{ "mDataProp": "title",  sWidth: '65%', fnRender: fnRenderTitle, bUseRendered:false ,"bSortable":false},
          { "mDataProp": "resourceTypeLabel",  sWidth: '15%',"bSortable":false }];
          if (datatable_isSelectable) {
          aoColumns_[2] = { "mDataProp": "id", tdarSortOption: "ID", sWidth:'5em' ,"bSortable":false};
          };
    $dataTable = registerLookupDataTable({
        tableSelector: '#resource_datatable',
        sAjaxSource:'/lookup/resource',
        "bLengthChange": true,
        "bFilter": false,
        aoColumns: aoColumns_,
		"sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span4'i><'span5'p>>",
        sPaginationType:"bootstrap",
        sAjaxDataProp: 'resources',
        requestCallback: function(searchBoxContents){
                return {title: searchBoxContents,
                    'resourceTypes': $("#resourceTypes").val() == undefined ? "" : $("#resourceTypes").val(),
                    'includedStatuses': $("#statuses").val() == undefined ? "" : $("#statuses").val() ,
                    'sortField':$("#sortBy").val(),
                    'term':$("#query").val(),
                    'projectId':$("#project-selector").val(),
                    'collectionId':$("#collection-selector").val(),
                     useSubmitterContext: !datatable_isAdministrator
            }
        },
        selectableRows: datatable_isSelectable,
        rowSelectionCallback: function(id, obj, isAdded){
            if(isAdded) {
                rowSelected(obj);
            } else {
                rowUnselected(obj);
            }
        }
    });

    $("#project-selector").change(function() {
        var projId = $(this).val();
        $.cookie("tdar_datatable_selected_project", projId);
        drawToolbar(projId);
        $("#resource_datatable").dataTable().fnDraw();
    });

    $("#collection-selector").change(function() {
        var projId = $(this).val();
        $.cookie("tdar_datatable_selected_collection", projId);
        drawToolbar(projId);
        $("#resource_datatable").dataTable().fnDraw();
    });
    
    $("#resourceTypes").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });


    $("#statuses").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });
    
    $("#sortBy").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });
    
    $("#query").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });
    
    $("#query").bindWithDelay("keyup", function() {$("#resource_datatable").dataTable().fnDraw();} ,500);

});


function drawToolbar(projId) {
    var toolbar = $("#proj-toolbar");
    toolbar.empty();
    if (projId != undefined && projId != '') {
        toolbar.append(projToolbarItem('/project/' + projId + '/view', '/images/zoom.png', ' View selected project'));
        toolbar.append(projToolbarItem('/project/' + projId + '/edit', '/images/pencil.png', ' Edit project'));
        toolbar.append(projToolbarItem('/resource/add?projectId=' + projId, '/images/database_add.png', ' Add new resource to project'));
    }
}

function fnRenderTitle(oObj) {
    //in spite of name, aData is an object containing the resource record for this row
    var objResource = oObj.aData;
    var html = '<a href="'  + getURI(objResource.urlNamespace + '/' + objResource.id) + '" class=\'title\'>' + htmlEncode(objResource.title) + '</a>';
    html += ' (ID: ' + objResource.id 
    if (objResource.status != 'ACTIVE') {
    html += " " + objResource.status;
    }
    html += ')';
    if (datatable_showDescription) {
    	html += '<br /> <p>' + htmlEncode(objResource.description) + '</p>';
    }; 
    return html;
}
</script>

</#macro>


<#macro repeat count>
    <#list 1..count as idx> <#t>
        <#nested><#t>
    </#list><#t>
</#macro>


<#macro keywordNodeOptions node selectedKeyword>
    <option>not implemented</option>
</#macro>

<#macro keywordNodeSelect label node selectedKeywordId selectTagId='keywordSelect'>
    <label for="${selectTagId}">${label}</label>
    <select id="${selectTagId}">
        <@keywordNodeOptions node selectedKeywordId />
    </select>
</#macro>


<#macro copyrightHolders sectionTitle copyrightHolderProxy >
<#if copyrightMandatory>
    <div class="glide" tiplabel="Primary Copyright Holder" tooltipcontent="Use this field to nominate a primary copyright holder. Other information about copyright can be added in the 'notes' section by creating a new 'Rights & Attribution note.">
        <h3>${sectionTitle}</h3>

    <table>
	  <@creatorProxyRow proxy=copyrightHolderProxy proxy_index="" prefix="copyrightHolder" required=true includeRole=false required=true/>
	</table>
</#if>
</#macro>

<#macro license>
<#assign currentLicenseType = defaultLicenseType/>
<#if resource.licenseType?has_content>
    <#assign currentLicenseType = resource.licenseType/>
</#if>
<div class="glide" id="license_section">
        <h3>License</h3>
<@s.radio name='resource.licenseType' groupLabel='License Type' emptyOption='false' listValue="label" 
    list='%{licenseTypesList}' numColumns="1" cssClass="licenseRadio" value="%{'${currentLicenseType}'}" />

    <table id="license_details">
    <#list licenseTypesList as licenseCursor>
        <#if (licenseCursor != currentLicenseType)>
            <#assign visible="hidden"/>
        <#else>
            <#assign visible="">
        </#if>
        <tr id="license_details_${licenseCursor}" class="${visible}">
                <td>
                    <#if (licenseCursor.imageURI != "")>
                        <a href="${licenseCursor.URI}" target="_blank"><img src="${licenseCursor.imageURI}"/></a>
                    </#if>
                </td>
                <td>
                    <h4>${licenseCursor.licenseName}</h4>
                    <p>${licenseCursor.descriptionText}</p>
                    <#if (licenseCursor.URI != "")>
                        <p><a href="${licenseCursor.URI}" target="_blank">view details</a></p>
                    <#else>
                        <p><label style="position: static"  for="licenseText">License text:</label></p>
                        <p><@s.textarea id="licenseText" name='resource.licenseText' rows="3" cols="60" /></p>
                    </#if>
                </td>
            </tr>
    </#list>
    </table>
</div>
</#macro>

<#macro asyncUploadTemplates formId="resourceMetadataForm">

<!-- The template to display files available for upload (uses tmpl.min.js) -->
<script id="template-upload" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-upload fade">
        <td class="preview"><span class="fade"></span></td>
        <td class="name"><span>{%=file.name%}</span></td>
        <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
        {% if (file.error) { %}
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else if (o.files.valid && !i) { %}
            <td>
                <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="bar" style="width:0%;"></div></div>
            </td>
            <td class="start">{% if (!o.options.autoUpload) { %}
                <button class="btn btn-primary">
                    <i class="icon-upload icon-white"></i>
                    <span>{%=locale.fileupload.start%}</span>
                </button>
            {% } %}</td>
        {% } else { %}
            <td colspan="2"></td>
        {% } %}
        <td class="cancel">{% if (!i) { %}
            <button class="btn btn-warning">
                <i class="icon-ban-circle icon-white"></i>
                <span>{%=locale.fileupload.cancel%}</span>
            </button>
        {% } %}</td>
    </tr>
{% } %}
</script>

<!-- The template to display files available for download (uses tmpl.min.js) -->

<script id="template-download" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
{% var idx = '' + TDAR.fileupload.getRowId();%}
{% var rowclass = file.fileId ? "existing-file" : "new-file" ;%}
    <tr class="template-download fade {%=rowclass%}">
        {% if (file.error) { %}        
            <td></td>
            <td class="name"><span>{%=file.name%}</span></td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else { %}
            <td class="preview"></td>
            <td class="name">
                {% if (file.url) { %}        
                <a href="{%=file.url%}" title="{%=file.name%}" rel="{%=file.thumbnail_url&&'gallery'%}" download="{%=file.name%}">{%=file.name%}</a>
                {% } else { %}
                {%=file.name%}
                {% } %} 
                <span class="replacement-text" style="display:none"></span>
            </td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td colspan="2">
                <@s.select id="proxy{%=idx%}_conf"  name="fileProxies[{%=idx%}].restriction" 
                style="padding-left: 20px;" list=fileAccessRestrictions listValue="label"  
                onchange="TDAR.fileupload.updateFileAction(this)" 
                cssClass="fileProxyConfidential"/>
            </td>
        {% } %}
        <td class="delete">
                <button class="btn btn-danger delete-button" data-type="{%=file.delete_type%}" data-url="{%=file.delete_url%}">
                    <i class="icon-trash icon-white"></i>
                    <span>{%=locale.fileupload.destroy%}</span>
                </button>
        </td>
        <td>
            {%if (file.fileId) { %}
               <#--
                <button class="btn btn-warning disabled replace-button" disabled="disabled">
                    <i class="icon-retweet icon-white"></i>
                    <span>Replace</span>
                </button>
                -->
                  <div class="btn-group">
                    <button class="btn btn-warning disabled dropdown-toggle replace-button" disabled="disabled" data-toggle="dropdown">Replace <span class="caret"></span></button>
                    <ul class="dropdown-menu" id="tempul">
                      <li><a href="#">file 1</a></li>
                      <li><a href="#">file 2</a></li>
                      <li class="divider"></li>
                      <li><a href="#">cancel replace operation</a></li>
                    </ul>
                  </div> 
            {% } %}
            
            
            <#-- TODO: this widget has a "bulk actions" convention, but I don't want it.  is it safe to remove the html,  or do we also need to handle this in javascript --> 
            <#-- <input type="checkbox" name="delete"  value="1"> -->


            <input type="hidden" class="fileAction" name="fileProxies[{%=idx%}].action" value="{%=file.action||'ADD'%}"/>
            <input type="hidden" class="fileId" name="fileProxies[{%=idx%}].fileId" value="{%=''+(file.fileId || '-1')%}"/>
            <input type="hidden" class="fileReplaceName" name="fileProxies[{%=idx%}].filename" value="{%=file.name%}"/>
            <input type="hidden" class="fileSequenceNumber" name="fileProxies[{%=idx%}].sequenceNumber" value="{%=idx%}"/>
            
        </td>
    </tr>
{% } %}
</script>

<script id="template-replace-menu" type="text/x-tmpl">
{% for(var i = 0, row; row = o.jqnewfiles[i]; i++) { %}
   <li><a href="#" 
            class="replace-menu-item"
            data-action="rename"
            data-filename="{%=$('.fileReplaceName', row).val()%}" 
            data-target="" >{%=$('.fileReplaceName', row).val()%}</a></li>
{% } %}
   <li class="divider"></li> 
   <li><a href="#" class="cancel" >Cancel previous operation</a></li>
</script>
</#macro>

<#macro acceptedFileTypesRegex>
/\.(<@join sequence=validFileExtensions delimiter="|"/>)$/i<#t>
</#macro>


<#macro subNavMenu>
	<div id='subnavbar' class="affix-top resource-nav span12 row navbar-static"  data-offset-top="250" data-offset-bottom="250" data-spy="affix">
	  <div class="">
	    <div class="container" style="width: auto;">
		<ul class="nav">
			<li><a href="#basicInformationSection">Basic</a></li>
			<li><a href="#authorshipSection">Authors</a></li>
			<#if persistable.resourceType?has_content && persistable.resourceType != 'PROJECT' ><li><a href="#divFileUpload">Upload</a></li></#if>
			<#nested />
			<li><a href="#organizeSection">Project</a></li>
			<li><a href="#spatialSection">Where</a></li>
			<li><a href="#temporalSection">When</a></li>
			<li><a href="#investigationSection">What</a></li>
			<li><a href="#siteSection">Site</a></li>
			<li><a href="#resourceNoteSectionGlide">Notes</a></li>
			<li><a href="#divAccessRights">Permissions</a></li>
		</ul>
		<span class="brand">
			<span class="button btn btn-primary submitButton" id="fakeSubmitButton">Submit</span>
		</span>
		</div>
	  </div>
	</div>
	<div>
	<br/>
</#macro>


<#macro listMemberUsers >
<#local _authorizedUsers=account.authorizedMembers />
<#if !_authorizedUsers?has_content><#local _authorizedUsers=[blankPerson]></#if>

<div id="accessRightsRecords" class="repeatLastRow" data-addAnother="add another user">
    <div class="control-group">
        <label class="control-label">Users</label>
        <#list _authorizedUsers as user>
            <#if user??>
                <@userRow user user_index isUser=true />
            </#if>
        </#list>
    </div>
</div>

</#macro>


<#macro userRow person=person _indexNumber=0 disableSelfUser=false prefix="authorizedMembers" required=false _personPrefix="" includeRole=false
	includeRepeatRow=false includeRights=false includeDelete=false hidden=false isUser=false>
<#local bDisabled = (person.id == authenticatedUser.id && disableSelfUser) />
<#local disabled =  bDisabled?string />
<#local lookupType="nameAutoComplete"/>
<#if isUser><#local lookupType="userAutoComplete"/></#if>
<#local _index=""/>
<#if _indexNumber?string!=''><#local _index="[${_indexNumber?c}]" /></#if>
<#local personPrefix="" />
<#if _personPrefix!=""><#local personPrefix=".${_personPrefix}"></#if>
<#local strutsPrefix="${prefix}${_index}" />
<#local rowIdElement="${prefix}Row_${_indexNumber}_p" />
<#local idIdElement="${prefix}Id__id_${_indexNumber}_p" />
<#local requiredClass><#if required>required</#if></#local>
    <div id='${rowIdElement}' class="creatorPerson <#if hidden>hidden</#if> <#if includeRepeatRow>repeat-row</#if> indent-row">
        <@s.hidden name='${strutsPrefix}${personPrefix}.id' value='${(person.id!-1)?c}' id="${idIdElement}"  cssClass="validIdRequired" onchange="this.valid()"  autocompleteParentElement="#${rowIdElement}"   />
        <div class="control-group">
	        <div class="controls controls-row">
	            <@s.textfield theme="tdar" cssClass="span2 ${lookupType} ${requiredClass}" placeholder="Last Name"  readonly="${disabled}" autocompleteParentElement="#${rowIdElement}"
	                autocompleteIdElement="#${idIdElement}" autocompleteName="lastName" autocomplete="off"
	                name="${strutsPrefix}${personPrefix}.lastName" maxlength="255" /> 
	            <@s.textfield theme="tdar" cssClass="span2 ${lookupType} ${requiredClass}" placeholder="First Name"  readonly="${disabled}" autocomplete="off"
	                name="${strutsPrefix}${personPrefix}.firstName" maxlength="255" autocompleteName="firstName"
	                autocompleteIdElement="#${idIdElement}" 
	                autocompleteParentElement="#${rowIdElement}"  />
	
				<#if includeRole || includeRights>
					<#if includeRole>
				        <@s.select theme="tdar" name="${strutsPrefix}.role"  autocomplete="off" listValue='label' list=relevantPersonRoles  cssClass="creator-role-select span3" />
				    <#else>
				        <@s.select theme="tdar" cssClass="span3 creator-role-select" name="${strutsPrefix}.generalPermission" emptyOption='false' listValue='label' list='%{availablePermissions}' disabled=bDisabled />
				    </#if>
				<#else>
	
				</#if>
	        </div>
        <div class="controls controls-row">
        <@s.textfield theme="tdar" cssClass="span2 ${lookupType}" placeholder="Email (optional)" readonly="${disabled}" autocomplete="off"
            autocompleteIdElement="#${idIdElement}" autocompleteName="email" autocompleteParentElement="#${rowIdElement}"
            name="${strutsPrefix}${personPrefix}.email" maxlength="255"/>
        <@s.textfield theme="tdar" cssClass="span3 ${lookupType}" placeholder="Institution Name (Optional)" readonly="${disabled}" autocomplete="off"
            autocompleteIdElement="#${idIdElement}" 
            autocompleteName="institution" 
            autocompleteParentElement="#${rowIdElement}"
            name="${strutsPrefix}${personPrefix}.institution.name" maxlength="255" />
        </div>
	      <#if includeDelete>          
	          <@clearDeleteButton id="${prefix}${_index}" disabled="${disabled}" />
          </#if>
  </div>
  </div>
</#macro>

<#macro institutionRow institution _indexNumber=0 prefix="authorizedMembers" required=false includeRole=false _institutionPrefix="" includeDelete=false hidden=false includeRepeatRow=false>
<#local _index=""/>
<#if _indexNumber?string!=''><#local _index="[${_indexNumber}]" /></#if>
<#local institutionPrefix="" />
<#if _institutionPrefix!=""><#local institutionPrefix=".${_institutionPrefix}"></#if>
<#local strutsPrefix="${prefix}${_index}" />
<#local rowIdElement="${prefix}Row_${_indexNumber}_i" />
<#local idIdElement="${prefix}Id__id_${_indexNumber}_i" />

    <div id='${rowIdElement}' class="creatorInstitution <#if hidden >hidden</#if> <#if includeRepeatRow>repeat-row</#if> indent-row">

        <@s.hidden name='${strutsPrefix}${institutionPrefix}.id' value='${(institution.id!-1)?c}' id="${idIdElement}"  cssClass="validIdRequired" onchange="this.valid()"  autocompleteParentElement="#${rowIdElement}"  />
            <div class="control-group">
                <div class="controls controls-row">
			        <@s.textfield theme="tdar" cssClass="institutionAutoComplete institution span4" placeholder="Institution Name" autocomplete="off"
			            autocompleteIdElement="#${idIdElement}" autocompleteName="name" 
			            autocompleteParentElement="#${rowIdElement}"
			            name="${strutsPrefix}${institutionPrefix}.name" maxlength="255" />

				<#if includeRole>
                    <@s.select theme="tdar" name="${strutsPrefix}.role" listValue='label' list=relevantInstitutionRoles cssClass="creator-role-select span3" />
                 </#if>
			  </div>
		  </div>
	      <#if includeDelete>          
	          <@clearDeleteButton id="${prefix}${_index}" />
          </#if>
	</div>
</#macro>


</#escape>