k<#--
$Id$ 
Edit freemarker macros.  Getting large, should consider splitting this file up.
-->
<#-- include navigation menu in edit and view macros -->
<#escape _untrusted as _untrusted?html>
<#import "common.ftl" as common>
<#import "/${themeDir}/local-helptext.ftl" as  helptext>
<#import "/${themeDir}/settings.ftl" as settings>
<#import "navigation-macros.ftl" as nav>

<#macro basicInformation itemTypeLabel="file" itemPrefix="resource">

</#macro>


<#macro chooseProjectSection>
    <#local _projectId = 'project.id' />
    <#if resource.id == -1 >
    <#local _projectId = request.getParameter('projectId')!'' />
    </#if>
        <div id="projectTipText" style="display:none;">
        Select a project with which your <@resourceTypeLabel /> will be associated. This is an important choice because it  will allow metadata to be inherited from the project further down this form
        </div>
        <h4>Choose a Project</h4>
        <div id="t-project" data-tooltipcontent="#projectTipText" data-tiplabel="Project">
            <@s.select title="Please select a project" emptyOption='true' id='projectId' label="Project"  labelposition="left" name='projectId' listKey='id' listValue='title' list='%{potentialParents}'
            truncate="70" value='${_projectId}' required=true  cssClass="required input-xxlarge" />
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
        <div class="control-group" data-tiplabel="Inherit Metadata from Selected Project" data-tooltipcontent="#divSelectAllInheritanceTooltipContent" id="divInheritFromProject">
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
    <div data-tiplabel="${siteAcronym} Collections" data-tooltipcontent="#divResourceCollectionListTips">
        <div id="resourceCollectionTable" class="control-group repeatLastRow" addAnother="add another collection">
            <label class="control-label">Collection Name(s)</label>
            <div class="controls">
                <#list _resourceCollections as resourceCollection>
                <@resourceCollectionRow resourceCollection resourceCollection_index/>
                </#list>
                <#if resource.resourceType.project>
                <span class="help-inline"><em>Note</em>: adding this project to a collection will not include the resources within this project.</span>
                </#if>
            </div>
        </div>
    </div>
</#macro>

<#macro resourceCollectionRow resourceCollection collection_index = 0 type="internal">
    <div id="resourceCollectionRow_${collection_index}_" class="controls-row repeat-row">
            <@s.hidden name="resourceCollections[${collection_index}].id"  id="resourceCollectionRow_${collection_index}_id" />
            <@s.textfield theme="simple" id="resourceCollectionRow_${collection_index}_id" name="resourceCollections[${collection_index}].name" cssClass="input-xxlarge collectionAutoComplete "  autocomplete="off"
            autocompleteIdElement="#resourceCollectionRow_${collection_index}_id" maxlength=255
            autocompleteParentElement="#resourceCollectionRow_${collection_index}_" />
        <@nav.clearDeleteButton id="resourceCollectionRow" />
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
        <@s.textfield theme="tdar" name='${keywordField}[${keyword_index}]'  maxlength=255 cssClass='input-xlarge keywordAutocomplete' placeholder="enter keyword"/>
        <#if showDelete>
        <@nav.clearDeleteButton id="${keywordField}Row" />
        </#if>
    </div>
</#macro>


<#macro spatialContext showInherited=true>
<div class="well-alt" id="spatialSection">
    <h2>Spatial Terms</h2>
    <@inheritsection checkboxId="cbInheritingSpatialInformation" name='resource.inheritingSpatialInformation' showInherited=showInherited />
    <div id="divSpatialInformation">
 
        <div data-tiplabel="Spatial Terms: Geographic" data-tooltipcontent="Keyword list: Geographic terms relevant to the document, e.g. &quot;Death Valley&quot; or &quot;Kauai&quot;." >
        <@keywordRows "Geographic Terms" geographicKeywords 'geographicKeywords' />
        </div>
        <@helptext.geo />
        <h4>Geographic Region</h4>
        <div id='editmapv3' class="tdar-map-large google-map"
            data-tiplabel="Geographic Coordinates"
            data-tooltipcontent="#geoHelpDiv"
            ></div>
        <div id="divManualCoordinateEntry" data-tooltipcontent="#divManualCoordinateEntryTip">
            <br />
            <@s.checkbox id="viewCoordinatesCheckbox" name="_tdar.viewCoordinatesCheckbox" onclick="TDAR.common.coordinatesCheckboxClicked(this);" label='Enter / View Coordinates' labelposition='right'  />
            <div id='explicitCoordinatesDiv' style='text-align:center;'>
                <table cellpadding="0" cellspacing="0" style="margin-left:auto;margin-right:auto;text-align:left;" >
                <tr>                                    
                <td></td>
                <td>
                <@s.textfield  theme="simple" name='latitudeLongitudeBoxes[0].maximumLatitude' id='maxy' size="14" cssClass="float latLong ne-lat" title="Please enter a valid Maximum Latitude" />
                <input type="text"  id='d_maxy'  placeholder="Latitude (max)"  class="ne-lat-display span2" />
                </td>
                <td></td>
                </tr>
                <tr>
                <td style="width:33%;text-align:center">
                    <@s.textfield theme="simple"  name="latitudeLongitudeBoxes[0].minimumLongitude" id='minx' size="14" cssClass="float latLong sw-lng" title="Please enter a valid Minimum Longitude" />
                    <input type="text"  id='d_minx'  placeholder="Longitude (min)"   class="sw-lng-display span2" />
                </td>
                <td style="width:33%;text-align:center">
                    <input type="button" id="locate" value="Locate" class="btn locateCoordsButton" />
                </td>
                <td style="width:33%;text-align:center">
                    <@s.textfield theme="simple"  name="latitudeLongitudeBoxes[0].maximumLongitude" id='maxx' size="14" cssClass="float latLong ne-lng" title="Please enter a valid Maximum Longitude" />
                    <input type="text"  id='d_maxx'   placeholder="Longitude (max)"  class="ne-lng-display span2" />
                </td>
                </tr>
                <tr>
                <td></td>
                <td>
                    <@s.textfield theme="simple"  name="latitudeLongitudeBoxes[0].minimumLatitude" id="miny" size="14" cssClass="float latLong sw-lat" title="Please enter a valid Minimum Latitude" /> 
                    <input type="text" id="d_miny"  placeholder="Latitude (min)"  class="sw-lat-display span2" /> 
                </td>
                <td></td>
                </tr>           
                </table>
            </div>
            <@helptext.manualGeo />
        </div>
        <#if switchableMapObfuscation>
            <@helptext.showExactLocationTip />
            <div class="" id="showExactLocation" data-tiplabel="Is it OK to show the exact location?" data-tooltipcontent="#showExactLocationHelpDiv" >
                <@s.checkbox id="is_okay_to_show_exact_location" name="latitudeLongitudeBoxes[0].okayToShowExactLocation" label='Is it OK to show the exact location?' labelposition='right'  />
            </div>
        </#if>
    </div>
</div>
</#macro>

<#macro resourceTypeLabel>
<#if bulkUpload>
	Resource
<#else>
<#noescape>${resource.resourceType.label}</#noescape>
</#if>
</#macro>

<#macro resourceProvider showInherited=true>
<div class="well-alt" id="divResourceProvider" data-tiplabel="Resource Provider" data-tooltipcontent="The institution authorizing ${siteAcronym} to ingest the resource for the purpose of preservation and access.">
    <h2>Institution Authorizing Upload of this <@resourceTypeLabel /></h2>
    <@s.textfield label='Institution' name='resourceProviderInstitutionName' id='txtResourceProviderInstitution' cssClass="institution input-xxlarge"  maxlength='255'/>
    <br/>
</div>
</#macro>


<#macro temporalContext showInherited=true>
<div class="well-alt" id="temporalSection">
    <h2>Temporal Coverage</h2>
    <@inheritsection checkboxId="cbInheritingTemporalInformation" name='resource.inheritingTemporalInformation' showInherited=showInherited  />
    <div  id="divTemporalInformation">
        <div data-tiplabel="Temporal Terms" data-tooltipcontent="Keyword list: Temporal terms relevant to the document, e.g. &quot;Pueblo IV&quot; or &quot;Late Archaic&quot;.">
            <@keywordRows "Temporal Terms" temporalKeywords 'temporalKeywords' true "add another temporal keyword" />
        </div>
        <@coverageDatesSection />
    </div>
</div>
</#macro>

<#macro generalKeywords showInherited=true>

<div  
    data-tiplabel="General Keywords"
    data-tooltipcontent="Keyword list: Select the artifact types discussed in the document.">   
    <h2>General Keywords</h2>
    <@inheritsection checkboxId="cbInheritingOtherInformation" name='resource.inheritingOtherInformation'  showInherited=showInherited />
    <div id="divOtherInformation">
        <@keywordRows "Keyword" otherKeywords 'otherKeywords' />
    </div>
    
    <#--fixme:  moving 'tagstrip' experiment out of divOtherInformation so existing inheritance code doesn't break
    <div class="row">
                <p><span class="label label-warning">FIXME:</span> replace lame keyword lists with fancy taglists (like the one below!)</p><br>
        <div class="control-group">
            <label class="control-label">Other Keywords</label>
            <div class="controls">
                <input type=text" name="test" id="otherKeywords" style="width:500px" value="${otherKeywords?join(","}"/>
            </div>
        </div>
        <script>
        $(document).ready(function() {
            $("#otherKeywords").select2({
                tags:[${otherKeywords?join(","}],
                tokenSeparators: [";"]});
        });
        </script>
    </div>
     -->
</div>
</#macro>


<#macro sharedUploadFile divTitle="Upload">
<div class="well-alt" id="uploadSection">
    <h2>${divTitle}</h2>
    <div class='fileupload-content'>
        <#nested />
        <#-- XXX: verify logic for rendering this -->
        <#if multipleFileUploadEnabled || resource.hasFiles()>
            <!-- not sure this is ever used -->
            <h4>Current ${multipleFileUploadEnabled?string("and Pending Files", "File")}</h4>
        
            <div class="">
            <p><span class="label">Note:</span> You can only have <strong><#if !multipleFileUploadEnabled>1 file<#else>${maxUploadFilesPerRecord} files</#if> </strong> per record</p> 
            </div>
            <table id="uploadFiles" class="files table tableFormat">
            </table>
            <table id="files" class="files sortable">
                <thead>
                    <tr class="reorder <#if (fileProxies?size < 2 )>hidden</#if>">
                    <th colspan=2>Reorder: <span class="link alphasort">Alphabetic</span> | <span class="link" onclick="customSort(this)">Custom</span>  </th>
                    </tr>
                </thead>
                <tbody>
                    <#list fileProxies as fileProxy>
                        <#if fileProxy??>
                            <@fileProxyRow rowId=fileProxy_index filename=fileProxy.filename filesize=fileProxy.size fileid=fileProxy.fileId action=fileProxy.action versionId=fileProxy.originalFileVersionId proxy=fileProxy />
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
<@helptext.siteName />
<div id="siteSection" data-tooltipcontent="#siteinfohelp">
    <h2>${divTitle}</h2>
    <@inheritsection checkboxId='cbInheritingSiteInformation' name='resource.inheritingSiteInformation'  showInherited=showInherited />
    <div id="divSiteInformation" >
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
<div data-tooltipcontent="#materialtypehelp">
<@helptext.materialType />
    <h2>Material Types</h2>
    <@inheritsection checkboxId='cbInheritingMaterialInformation' name='resource.inheritingMaterialInformation'  showInherited=showInherited />
    <div id="divMaterialInformation">
        <@s.checkboxlist theme="bootstrap" name='materialKeywordIds' list='allMaterialKeywords' listKey='id' listValue='label' listTitle="definition"  label="Select Type(s)"
            spanClass="span2" numColumns="3" />
    </div>      
</div>

</#macro>

<#macro culturalTerms showInherited=true inline=false>
<div data-tooltipcontent="#culturehelp">
<@helptext.cultureTerms />
    <h2>${culturalTermsLabel!"Cultural Terms"}</h2>
    <@inheritsection checkboxId="cbInheritingCulturalInformation" name='resource.inheritingCulturalInformation'  showInherited=showInherited />
    <div id="divCulturalInformation" >
        <div class="control-group">
            <label class="control-label">${culturalTermsLabel!"Culture"}</label>
            <div class="controls">
                <@s.checkboxlist theme="hier" name="approvedCultureKeywordIds" keywordList="approvedCultureKeywords" />
            </div>
        </div>
        
        <!--"add another cultural term" -->
        <@keywordRows "Other" uncontrolledCultureKeywords 'uncontrolledCultureKeywords' />
    </div>
</div>
</#macro>

<#--
<#macro uncontrolledCultureKeywordRow uncontrolledCultureKeyword_index=0>
            <tr id='uncontrolledCultureKeywordRow_${uncontrolledCultureKeyword_index}_'>
            <td>
                <@s.textfield name='uncontrolledCultureKeywords[${uncontrolledCultureKeyword_index}]' cssClass=' input-xxlarge cultureKeywordAutocomplete' autocomplete="off" />
                </td><td><@nav.clearDeleteButton id="uncontrolledCultureKeywordRow" />
            </td>
            </tr>
</#macro>
-->
<#macro investigationTypes showInherited=true >
<div data-tiplabel="Investigation Types" data-tooltipcontent="#investigationtypehelp" id="investigationSection">
    <h2>Investigation Types</h2>
    <@inheritsection checkboxId='cbInheritingInvestigationInformation' name='resource.inheritingInvestigationInformation'  showInherited=showInherited />
    <div id="divInvestigationInformation">
    
        <@s.checkboxlist name='investigationTypeIds' list='allInvestigationTypes' listKey='id' listValue='label' numColumns="2" spanClass="span3" 
    theme="bootstrap"        label="Select Type(s)" listTitle="definition" />
    </div>
</div>
<@helptext.investigationType />
</#macro>


<#-- provides a fieldset just for full user access -->
<#macro fullAccessRights tipsSelector="#divAccessRightsTips">
<#local _authorizedUsers=authorizedUsers />
<#local _isSubmitter = authenticatedUser.id == ((persistable.submitter.id)!-1)>
<#if _authorizedUsers.empty><#local _authorizedUsers=[blankAuthorizedUser]></#if>
<@helptext.accessRights />

<div id="divAccessRights" data-tiplabel="Access Rights" data-tooltipcontent="${tipsSelector}">
<h2><a name="accessRights"></a>Access Rights</h2>
<h3>Users who can view or modify this resource</h3>
<div id="accessRightsRecords" class="<#if !ableToUploadFiles?has_content || ableToUploadFiles>repeatLastRow</#if>" data-addAnother="add another user">
    <div class="control-group">
        <label class="control-label">Users</label>
        <div class="controls">
        <#list _authorizedUsers as authorizedUser>
            <#if authorizedUser??>
                <div class="controls-row repeat-row"  id="authorizedUsersRow_${authorizedUser_index}_">
                    <div class="span6">
                        <@registeredUserRow person=authorizedUser.user isDisabled=!authorizedUser.enabled _indexNumber=authorizedUser_index  _personPrefix="user" 
                           prefix="authorizedUsers" includeRights=true includeRepeatRow=false />
                    </div>
                    <div class="span1">
                        <@nav.clearDeleteButton id="accessRightsRecordsDelete${authorizedUser_index}" disabled=!authorizedUser.enabled />
                    </div>
                </div>
            </#if>
        </#list>
       </div>
    </div>
</div>

<#nested>

 <#if persistable.resourceType??>
  <@common.resourceCollectionsRights collections=effectiveResourceCollections owner=submitter >
  <#--Note: this does not reflect changes to resource collection you have made until you save.-->
  </@common.resourceCollectionsRights>
 </#if>

</div>
</#macro>


<#macro categoryVariable>
<div class="control-group">
    <label class="control-label"><small>Category / Subcategory</small></label>
    <div class="controls controls-row">
        <div id='categoryDivId' class="span3">
        <@s.select theme="tdar"  id='categoryId' name='categoryId' 
            onchange='TDAR.common.changeSubcategory("#categoryId","#subcategoryId")' autocompleteName="sortCategoryId"
            listKey='id' listValue='name' emptyOption='true' list='%{allDomainCategories}' cssClass="input-block-level" />
        </div>
        <div id='subcategoryDivId' class="span3">
            <@s.select theme="tdar" id='subcategoryId' name='subcategoryId' 
                autocompleteName="subCategoryId" headerKey="-1" listKey='id' headerValue="N/A" list='%{subcategories}'  cssClass="input-block-level" />
        </div>
    </div>
</div>
</#macro>


<#macro singleFileUpload typeLabel="${resource.resourceType.label}">
<#if !ableToUploadFiles>
	<b>note:</b> you have not been granted permission to upload or modify files<br/>
<#else>
	<div class="control-group"
	        data-tiplabel="Upload your ${typeLabel}" 
	        data-tooltipcontent="The metadata entered on this form will be associated with this file. We accept the following formats: 
	                        <@join sequence=validFileExtensions delimiter=", "/>">
	    <label for="fileUploadField" class="control-label">${typeLabel}</label>
	    <div class="controls">
	        <@s.file theme="simple" name='uploadedFiles' cssClass="validateFileType input-xxlarge" id="fileUploadField" labelposition='left' size='40' />
	        <span class="help-block">Valid file types include: <@join sequence=validFileExtensions delimiter=", "/></span>
	    </div>
	    <#nested>
	</div>
</#if>
</#macro>

<#macro manualTextInput typeLabel type uploadOptionText manualEntryText>
<#-- show manual option by default -->
<#local usetext=(resource.getLatestVersions().isEmpty() || (fileTextInput!"") != "")>
<div id="enter-data">
    <h2>${(resource.id == -1)?string("Submit", "Replace")} ${typeLabel}</h2>
    <div class="control-group">
        <label class='control-label' for='inputMethodId'>Submit as</label>
        <div class="controls">
            <select id='inputMethodId' name='fileInputMethod' onchange='TDAR.common.refreshInputDisplay()' class="input-xxlarge">
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
        <@s.textarea label='${typeLabel}' labelposition='top' id='fileInputTextArea' name='fileTextInput' rows="5" cssClass='resizable resize-vertical input-xxlarge' />
    </div>
</div>

</#macro>

<#macro submit label="Save" fileReminder=true buttonid="submitButton" span="span9">
<div class="errorsection row">
    <div class="${span}">
        <#if fileReminder>
        <div id="reminder" class="">
            <p><span class="label label-info">Reminder</span> No files are attached to this record. </p>
        </div>
        </#if>     
        <#-- if you put an error class on this, then you get a pink box at the bottom of every page visible on submit, ugly -->
        <div id="error" class="" style="display:none">
            <ul></ul>
        </div>
        <div class="form-actions" id="editFormActions">
            <#nested>
            <@submitButton label=label id=buttonid />
            <img alt="progress indicator" src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="display:none" />
        </div> 
    </div>
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

<#macro parentContextHelp element="div" resourceType="resource" valueType="values">
<${element} data-tiplabel="Inherited Values" data-tooltipcontent="The parent project for this ${resourceType} defines ${valueType} for this section.  You may also define your own, but note that they will not override the values defined by the parent.">
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
    <div id="relatedCollectionsSection">
        <div id="divSourceCollectionControl" class="control-group repeatLastRow">
            <label class="control-label">Source Collections</label>
            <#list _sourceCollections as sourceCollection>
                <@sourceCollectionRow sourceCollection "sourceCollection" sourceCollection_index/>
            </#list>
        </div>
    
        <div id="divRelatedComparativeCitationControl" class="control-group repeatLastRow">
            <label class="control-label">Related or Comparative Collections</label>
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
    <div class="controls controls-row repeat-row" id="${prefix}Row_${index}_">
        <#-- <@s.hidden name="${plural}[${index}].id" cssClass="dont-inherit" /> -->
        <@s.textarea rows="4" theme="tdar" name='${plural}[${index}].text' cssClass="span6 resizable resize-vertical" />
        <div class="span1">
            <@nav.clearDeleteButton id="${prefix}Row${index}" />
        </div>
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




<#macro resourceNoteSection showInherited=true>
<div id="resourceNoteSectionGlide" data-tiplabel="Notes" data-tooltipcontent="Use this section to append any notes that may help clarify certain aspects of the resource.  For example, 
    a &quot;Redaction Note&quot; may be added to describe the rationale for certain redactions in a document.">
    <#local _resourceNotes = resourceNotes />
    <#if _resourceNotes.empty >
    <#local _resourceNotes = [blankResourceNote] />
    </#if>
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

<#macro accountSection>
<#if payPerIngestEnabled>
    <#if activeAccounts?size == 1>
    <div class="well-alt" id="accountsection">
        <h2>Billing Account Information</h2>
        <div class="control-group">
            <label class="control-label">Account Name</label>
            <div class="controls">
                <#list activeAccounts as activeAccount>
                <span class="uneditable-input">${activeAccount.name}</span>
                </#list>            
            </div>
        </div>
    </div>
    <#else>
    <div class="well-alt" id="accountsection">
        <h2>Choose an account to bill from</h2>
        <@s.select name="accountId" list="%{activeAccounts}" label="Account" title="Choose an account to bill from" listValue="name" listKey="id" emptyOption="true" required=true cssClass="required"/>
    </div>
    </#if>
</#if>
</#macro>

<#macro noteRow proxy note_index=0>
<div id="resourceNoteRow_${note_index}_" class="repeat-row">
    <div class="controls controls-row">
        <div class="span6">
            <div class="controls-row">
                <@s.select theme="tdar" emptyOption='false' name='resourceNotes[${note_index}].type' list='%{noteTypes}' listValue="label" />
            </div>
            <div class="controls-row">
                <@s.textarea rows="4" theme="tdar" name='resourceNotes[${note_index}].note' placeholder="enter note contents" cssClass='span6 resizable resize-vertical' 
                    maxlength='5000' />
            </div>
        </div>
        <div class="span1">
            <@nav.clearDeleteButton id="resourceNoteRow" />
        </div>
    </div>
</div>
</#macro>




<#macro coverageDatesSection>
<#local _coverageDates=coverageDates />
<#if _coverageDates.empty><#local _coverageDates = [blankCoverageDate] /></#if>
<@helptext.coverageDates />
<div class="control-group repeatLastRow" id="coverageDateRepeatable" data-add-another="add another coverage date" data-tiplabel="Coverage Dates" data-tooltipcontent="#coverageDatesTip">
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
        <#--<@s.hidden name="coverageDates[${proxy_index}].id" cssClass="dont-inherit" /> -->
        <@s.select theme="tdar"name="coverageDates[${proxy_index}].dateType" cssClass="coverageTypeSelect input-medium"
            listValue='label'  headerValue="Date Type" headerKey="NONE"
            list=allCoverageTypes />
        <@s.textfield theme="tdar" placeholder="Start Year" cssClass="coverageStartYear input-small" name="coverageDates[${proxy_index}].startDate" maxlength="10" /> 
        <@s.textfield theme="tdar" placeholder="End Year" cssClass="coverageEndYear input-small" name="coverageDates[${proxy_index}].endDate" maxlength="10" />
        <@s.textfield theme="tdar" placeholder="Description"  cssClass="coverageDescription input-xlarge" name="coverageDates[${proxy_index}].description"  maxlength=255 />
       <@nav.clearDeleteButton id="{proxy_index}DateRow"/>
</div>
</#macro>



<#macro resourceCreators sectionTitle proxies prefix>
<#local _proxies = proxies >
<#if proxies?size == 0><#local _proxies = [blankCreatorProxy]></#if>
<div class="" data-tiplabel="${sectionTitle}" 
    id="${prefix}Section"
    data-tooltipcontent="#divResourceCreatorsTip">
    <h2>${sectionTitle}</h2>
       
    <div id="${prefix}Table" class="table repeatLastRow creatorProxyTable">
        <#list _proxies as proxy>
        <@creatorProxyRow proxy  prefix proxy_index/>
        </#list>
    </div>
</div> <!-- section -->
</#macro>

<#macro creatorProxyRow proxy=proxy prefix=prefix proxy_index=proxy_index type_override="NONE" 
    required=false includeRole=true leadTitle="" showDeleteButton=true>
    <#assign relevantPersonRoles=personAuthorshipRoles />
    <#assign relevantInstitutionRoles=institutionAuthorshipRoles />
    <#if prefix=='credit'>
        <#assign relevantPersonRoles=personCreditRoles />
        <#assign relevantInstitutionRoles=institutionCreditRoles />
    </#if>

    <#if proxy??>
    <div id="${prefix}Row_${proxy_index}_" class="repeat-row control-group">
          <#assign creatorType = proxy.actualCreatorType!"PERSON" />
          <!-- fixme: careful with this styling -->
        <div class="control-label">
             <div class="btn-group creator-toggle-button" data-toggle="buttons-radio">
               <#if type_override == 'PERSON' || (creatorType=='PERSON' && type_override=='NONE') >
               	<#local selectedType="PERSON"/>
               	<#else>
               		<#local selectedType="INSTITUTION"/>
				</#if>
               <button type="button" class="btn btn-small personButton <#if type_override == "PERSON" || (creatorType=='PERSON' && type_override=='NONE') >btn-active active</#if>" data-toggle="button">Person</button>
               <button type="button" class="btn btn-small institutionButton <#if creatorType =='INSTITUTION' || type_override == "INSTITUTION">btn-active active</#if>" data-toggle="button">Institution</button>
               <@s.hidden name="${prefix}Proxies[${proxy_index}].type" 
	               	value="${selectedType}" cssClass="toggleValue" />
            </div>
        </div>
        <div class="controls controls-row">
            <#--assuming we are in a span9 and that a controls-div is 2 cells narrower, our width should be span 7 -->
            <div class="span6">
                <@userRow person=proxy.person _indexNumber=proxy_index _personPrefix="person" prefix="${prefix}Proxies" 
                    includeRole=includeRole hidden=(creatorType =='INSTITUTION' || type_override == "INSTITUTION") 
                    required=(required) leadTitle="${leadTitle}"/>

                <@institutionRow institution=proxy.institution _indexNumber=proxy_index includeRole=includeRole _institutionPrefix="institution" 
                    prefix="${prefix}Proxies" hidden=(type_override == "PERSON" || (creatorType=='PERSON' && type_override=='NONE')) 
                    required=(required) leadTitle="${leadTitle}"/>
            </div>
            <div class="span1">
                <#if showDeleteButton>
                <button class="btn  btn-mini repeat-row-delete " type="button" tabindex="-1" ><i class="icon-trash"></i></button>
                </#if>
            </div>
        </div>
    </div>
    </#if>
</#macro>

<#macro identifiers showInherited=true>
    <#local _resourceAnnotations = resourceAnnotations />
    <#if _resourceAnnotations.empty>
    <#local _resourceAnnotations = [blankResourceAnnotation] />
    </#if>
    <div id="divIdentifiersGlide" data-tiplabel="<@resourceTypeLabel /> Specific or Agency Identifiers" data-tooltipcontent="#divIdentifiersTip">
        <@helptext.identifiers />
        <h2><@resourceTypeLabel /> Specific or Agency Identifiers</h2>
        <@inheritsection checkboxId="cbInheritingIdentifierInformation" name='resource.inheritingIdentifierInformation' showInherited=showInherited />
        <div id="divIdentifiers" class="repeatLastRow">
            <div class="control-group">
                <label class="control-label">Name / Value</label>
                <div class="controls">
                    <div id="resourceAnnotationsTable"  addAnother="add another identifier" >
                        <#list _resourceAnnotations as annotation>
                        <@displayAnnotation annotation annotation_index/>
                        </#list>
                    </div>
                </div>
            </div>        
        </div>
    </div>

</#macro>

<#macro displayAnnotation annotation annotation_index=0>
    <div id="resourceAnnotationRow_${annotation_index}_" class="controls-row repeat-row">
        <@s.textfield theme="tdar" placeholder="Name"  maxlength=128 cssClass="annotationAutoComplete span3" name='resourceAnnotations[${annotation_index}].resourceAnnotationKey.key' value='${annotation.resourceAnnotationKey.key!""}'  autocomplete="off" />
        <@s.textfield theme="tdar" placeholder="Value" cssClass="span3" name='resourceAnnotations[${annotation_index}].value'  value='${annotation.value!""}' />
        <div class="span1"><@nav.clearDeleteButton id="resourceAnnotationRow" /></div>                        
    </div>
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
MARTIN: it's also used by the FAIMS Archive type on edit.
-->
<#macro upload uploadLabel="File" showMultiple=false divTitle="Upload File" showAccess=true>
    <@sharedUploadFile>
      <@singleFileUpload>
          <div class="field indentFull singleFileUpload">
          <@s.select name="fileProxies[0].restriction" id="cbConfidential" labelposition="right" label="This item has access restrictions" listValue="label" list=fileAccessRestrictions />
          <div><b>NOTE:</b> by changing this from 'public', all of the metadata will be visible to users, they will not be able to view or download this file.  
          You may explicity grant read access to users below.</div>
          <br />     
          <#local val = ""/>
          <#if (fileProxies[0].fileCreatedDate)?has_content>
          	<#local val = fileProxies[0].fileCreatedDate?string("MM/dd/yyyy") />
          </#if>
              Date             <@s.textfield name="fileProxies[0].fileCreatedDate" cssClass="date input-small" placeholder="mm/dd/yyyy" value="${val}" />
	          Description      <@s.textarea class="input-block-level" name="fileProxies[0].description" rows="3" placeholder="Enter a description here" />
          
          </div>
      </@singleFileUpload>
    </@sharedUploadFile>
</#macro>


<#macro asyncFileUpload uploadLabel="Attach Files" showMultiple=false divTitle="Upload" divId="divFileUpload" inputFileCss="" >

<div id="${divId}Help" style="display:none">
    <div class="">
        <h3>Adding Files</h3>
        <ul>
            <li>To attach files to this resource,  click the button labeled "Add Files..." </li>
            <#if multipleFileUploadEnabled>
                <li>You may upload up to <#if !multipleFileUploadEnabled>1 file<#else>${maxUploadFilesPerRecord} files</#if> for this resource type</li>
            <#else>
                <#--FIXME:  i'm pretty sure async upload for single files is untested, and wont work as described here -->
                <li> To replace a file, simply upload the updated version</li>
            </#if>
            <#if validFileExtensions??>
                <li>Accepted file types: .<@join validFileExtensions ", ." /></li>
            </#if>
        </ul>

        <#if fileProxies?size &gt; 0>
        <h3>Replacing Files</h3>
        <ol>
            <li>In the list of files, locate the row (or file tab) that corresponds to the file you would like to replace.</li>
            <li>In that row, click on the button labeled "Replace". tDAR will prompt you for a new file.</li>
            <li>Once the upload is complete, you must save the form to confirm your changes.Click on the "Save" button in the upper right hand portion of the screen.</li>
            <li>To undo this action and restore the original file, simply click the button again (which will now be labeled "Restore Original").</li>
        </ol>

        </#if>

        <h3>Deleting Files</h3>
        You can remove files by clicking on the button labeled "Delete". If you change your mind or if you
        mistakenly clicked on the delete button, do not worry. You can restore the file by clicking the button a
        second time (the button will now be labeled "Undelete").


        <h3>File Information</h3>
        <dl>
            <dt>Restriction</dt>
            <dd><em>Public Files</em> are accessible to all registered ${siteAcronym} users.  <em>Confidential</em> and <em>Embargoed</em> files can only be downloaded by registered ${siteAcronym} users that you specify in the Access Rights section</dd>
            <dt>Date Created</dt>
            <dd>The date this file was created. For image files, it is the calendar date when the image was taken, rendered, etc.</dd>
            <dt>Description</dt>
            <dd>
                Additional information specific to this file.
            </dd>
        </dl>

    </span>
    </div>
</div>
<div id="${divId}" class="well-alt" data-tiplabel="${uploadLabel}" data-tooltipcontent="#${divId}Help">
    <@s.hidden name="ticketId" id="ticketId" />
    <h2>${uploadLabel}</h2>
  
    <div id="fileuploadErrors" class="fileupload-error-container" style="display:none">
        <div class="alert alert-block">
            <h4>We found the folllowing problems with your uploads</h4>
            <ul class="error-list"></ul>
            </div>
    </div>
    
    <#if !ableToUploadFiles>
    <b>note:</b> you have not been granted permission to upload or modify files<br/>
    <#else>
    <div class="row fileupload-buttonbar">
        <div class="span2">
            <!-- The fileinput-button span is used to style the file input field as button -->
            <span class="btn btn-success fileinput-button btn-block">
                <i class="icon-plus icon-white"></i>
                <span class="btn-lbl-singleclick">Add files...</span>
                <span class="btn-lbl-doubleclick">Double-click to add files ...</span>
            <input type="file" name="uploadFile" id="fileAsyncUpload" multiple="multiple" class="${inputFileCss}">
            </span>
        </div>
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
        <!-- The table listing the files available for upload/download -->
	</#if>
        <table id="files" role="presentation" class="table table-striped table-bordered">
            <tbody id="fileProxyUploadBody" class="files">
            <#list fileProxies as fileProxy>
                <#if fileProxy??>
                <@fileProxyRow rowId=fileProxy_index filename=fileProxy.filename filesize=fileProxy.size fileid=fileProxy.fileId action=fileProxy.action versionId=fileProxy.originalFileVersionId proxy=fileProxy />
                </#if>
            </#list>
            </tbody>
        </table>
    <div id="cancelledProxies" style="display:none">
    
    </div>
</div>
</#macro>

<#macro fileProxyRow rowId="{ID}" filename="{FILENAME}" filesize="{FILESIZE}" action="ADD" fileid=-1 versionId=-1 proxy=blankFileProxy >
<tr id="fileProxy_${rowId}" class="${(fileid == -1)?string('newrow', '')} sortable fade existing-file in">

            <td class="preview"></td>
            <td class="name">
                <a href="<@s.url value='/filestore/${versionId?c}/get'/>" title="${filename?html}" download="${filename?html}">${filename?html}</a>
                 
                <span class="replacement-text"></span>
            </td>
            <td class="size"><span>${filesize} bytes</span></td>
        <#if ableToUploadFiles>
            <td colspan="2">
            
        <div class="control-group">
        
            <div class="controls">
                <@s.select id="proxy${rowId}_conf"  name="fileProxies[${rowId}].restriction" labelposition="right" 
                style="padding-left: 20px;" list=fileAccessRestrictions listValue="label"  class="fileProxyConfidential confidential-contact-required" style="padding-left: 20px;" />
                <#local val = ""/>
                <#if (proxy.fileCreatedDate)?has_content>
					<#local val = proxy.fileCreatedDate?string("MM/dd/yyyy")>
				</#if>
                <@s.textfield name="fileProxies[${rowId}].fileCreatedDate" cssClass="date input-small" placeholder="mm/dd/yyyy" value="${val}" />
                <@s.textarea class="input-block-level" name="fileProxies[${rowId}].description" rows="1" placeholder="Enter a description here" />

            </div> 
        </div>
        </td>
        
        <td class="delete">
                <button class="btn btn-danger delete-button" data-type="DELETE" data-url="">
                    <i class="icon-trash icon-white"></i><span>Delete</span>
                </button>
        </td>
        <td>
            
        <input type="hidden" class="fileAction" name="fileProxies[${rowId}].action" value="${action}"/>
        <input type="hidden" class="fileId" name="fileProxies[${rowId}].fileId" value="${fileid?c}"/>
        <input type="hidden" class="fileReplaceName" name="fileProxies[${rowId}].filename" value="${filename}"/>
        <input type="hidden" class="fileSequenceNumber" name="fileProxies[${rowId}].sequenceNumber" value=${rowId} />
            
        </td>
	</#if>
    </tr>
</#macro>

<#macro title>
<#-- expose pageTitle so edit pages can use it elsewhere -->
<#assign pageTitle>Create a new <@resourceTypeLabel /></#assign>
<#if resource.id != -1>
<#assign pageTitle>Editing <@resourceTypeLabel /> Metadata for ${resource.title} (${siteAcronym} id: ${resource.id?c})</#assign>
</#if>
<title>${pageTitle}</title>
</#macro>

<#macro sidebar>
<div id="sidebar-right" parse="true">
    <div id="notice">
        <h2>Introduction</h2>
        <div id="noticecontent">
        This is the page for editing metadata associated with ${resource.resourceType.plural}.
	    </div>
    </div>
</div>
</#macro>


<#macro inheritTips id>
    <div id="${id}hint" class="inherit-tips">
        <em>Note: This section supports <strong>inheritance</strong>: values can be re-used by resources associated with your project.</em>
    </div>
</#macro>


<#macro resourceDataTable showDescription=true selectable=false>
<div class="well tdar-widget"> <#--you are in a span9, but assume span8 so we fit inside well -->
    <div class="row">
        <div class="span8">
            <label for="query">Title</label>
            <@s.textfield theme="tdar" name="_tdar.query" id="query" cssClass='span8' 
                    placeholder="Enter a full or partial title to filter results" />
        </div>
    </div>

    <div class="row">
        <div class="span4">
            <label class="" for="project-selector">Project</label>
            <select id="project-selector" name="_tdar.project" class="input-block-level">
              <option value="" selected='selected'>All Editable Projects</option>
              <#if allSubmittedProjects?? && !allSubmittedProjects.empty>
              <optgroup label="Your Projects">
                  <#list allSubmittedProjects?sort_by("titleSort") as submittedProject>
                  <option value="${submittedProject.id?c}" title="${submittedProject.title!""?html}"><@common.truncate submittedProject.title 70 /> </option>
                  </#list>
              </optgroup>
              </#if>

              <optgroup label="Projects you have been given access to">
                  <#list fullUserProjects?sort_by("titleSort") as editableProject>
                      <option value="${editableProject.id?c}" title="${editableProject.title!""?html}"><@common.truncate editableProject.title 70 /></option>
                  </#list>
              </optgroup>
            </select>
        </div>
        
        <div class="span4">
            <label class="" for="collection-selector">Collection</label>
            <div class="">
                <select name="_tdar.collection" id="collection-selector" class="input-block-level">
                    <option value="" selected='selected'>All Collections</option>
                    <@s.iterator value='resourceCollections' var='rc'>
                        <option value="${rc.id?c}" title="${rc.name!""?html}"><@common.truncate rc.name!"(No Name)" 70 /></option>
                    </@s.iterator>
                </select>
            </div>
        </div>
    </div>


    
    <div class="row">

        <div class="span4">
            <label class="">Status</label>
            <@s.select theme="tdar" id="statuses" headerKey="" headerValue="Any" name='_tdar.status'  emptyOption='false' listValue='label' 
                        list='%{statuses}' cssClass="input-block-level"/>
        </div>
        
        <div class="span4"> 
        <label class="">Resource Type</label>
        <@s.select theme="tdar" id="resourceTypes" name='_tdar.resourceType'  headerKey="" headerValue="All" emptyOption='false' 
                    listValue='label' list='%{resourceTypes}' cssClass="input-block-level"/>
        </div>
        
    </div>

    <div class="row">
        <div class="span4">
            <label class="">Sort  by</label>
            <div class="">
                <@s.select theme="tdar" emptyOption='false' name='_tdar.sortBy' listValue='label' list='%{resourceDatatableSortOptions}' id="sortBy"
                            value="ID_REVERSE" cssClass="input-block-level"/>
             </div>
        </div>
        <p class="span4">&nbsp;</p>
    </div>

<!-- <ul id="proj-toolbar" class="projectMenu"><li></li></ul> -->
</div>
<div class="row">
    <div class="span9">

            <table class="display table table-striped table-bordered tableFormat" id="resource_datatable">
                <colgroup>
                    <#if selectable><col style="width:10%"></#if>
                    <col style="width: 70%">
                    <col style="">
                </colgroup>
            <thead>
                 <tr>
                     <#if selectable><th><input type="checkbox" onclick="checkAllToggle()" id="cbCheckAllToggle">id</th></#if>
                     <th>Title</th>
                     <th>Type</th>
                 </tr>
            </thead>
            <tbody>
                <tr>
                    <#if selectable><td>&nbsp;</td></#if>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                </tr>
            </tbody>
            </table>
    </div>
</div>
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
$(function() {
    TDAR.datatable.setupDashboardDataTable({
        isAdministrator: ${(administrator!false)?string},
        isSelectable: ${selectable?string},
        showDescription: ${showDescription?string}
    });
});
<#nested>
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


<#macro copyrightHolders sectionTitle copyrightHolderProxies >
    <#if copyrightMandatory>
        <@helptext.copyrightHoldersTip />
        <div class="" id="copyrightHoldersSection" data-tiplabel="Primary Copyright Holder" data-tooltipcontent="#divCopyrightHoldersTip" >
            <h2>${sectionTitle}</h2>
            <div id="copyrightHolderTable" class="control-group table creatorProxyTable">
                <@creatorProxyRow proxy=copyrightHolderProxies proxy_index="" prefix="copyrightHolder" required=true 
                    includeRole=false required=true leadTitle="copyright holder " showDeleteButton=false/>
            </div>
        </div>
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
                        <a href="${licenseCursor.URI}" target="_blank"><img alt="license image" src="<#if secure>${licenseCursor.secureImageURI}<#else>${licenseCursor.imageURI}</#if>"/></a>
                    </#if>
                </td>
                <td>
                    <h4>${licenseCursor.licenseName}</h4>
                    <p>${licenseCursor.descriptionText}</p>
                    <#if (licenseCursor.URI != "")>
                        <p><a href="${licenseCursor.URI}" target="_blank">view details</a></p>
                    <#else>
                        <p><label style="position: static"  for="licenseText">License text:</label></p>
                        <p><@s.textarea id="licenseText" name='resource.licenseText' rows="4" cols="60" /></p>
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
            <td class="error" colspan="2"><span class="label label-important">Error</span> {%=file.error%}</td>
        {% } else if (o.files.valid && !i) { %}
            <td>
                <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="bar" style="width:0%;"></div></div>
            </td>
            <td class="start">{% if (!o.options.autoUpload) { %}
                <button class="btn btn-primary">
                    <i class="icon-upload icon-white"></i>
                    <span>Start</span>
                </button>
            {% } %}</td>
        {% } else { %}
            <td colspan="2"></td>
        {% } %}
        <td class="cancel">{% if (!i) { %}
            <button class="btn btn-warning">
                <i class="icon-ban-circle icon-white"></i>
                <span>Cancel</span>
            </button>
        {% } %}</td>
    </tr>
{% } %}
</script>

<#-- The template to display files available for download (uses tmpl.min.js) -->
<#-- lets assume we are working with about span5 amount of space width -->
<script id="template-download" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
{% var idx = '' + TDAR.fileupload.getRowId();%}
{% var rowclass = file.fileId ? "existing-file" : "new-file" ;%}
{% rowclass += TDAR.fileupload.getRowVisibility() ? "" : " hidden"; %}
    <tr class="template-download fade {%=rowclass%}" id="files-row-{%=idx%}">
            <td colspan="4">
                {% if (file.error) { %}
                <div class="error"><span class="label label-important">Error</span> {%=file.error%}</div>
                {% } %}

                <label class="control-label">Filename</label>
                <div class="controls controls-row">
                    <div class="span5">
                        <div><em class="replacement-text "></em></div>
                        <span class="name uneditable-input subtle inpux-xlarge">{%=file.name%}</span>
                        <span class="help-inline">{%=o.formatFileSize(file.size)%}</span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label">Restriction</label>
                    <div class="controls">
                        <#-- FIXME:supposedly struts 2.1+ allows custom data attributes but I get a syntax error.  What gives? -->
                        <@s.select id="proxy{%=idx%}_conf" datarestriction="{%=file.restriction%}" theme="simple" name="fileProxies[{%=idx%}].restriction"
                            style="padding-left: 20px;" list=fileAccessRestrictions listValue="label"
                            onchange="TDAR.fileupload.updateFileAction(this)"
                            cssClass="fileProxyConfidential confidential-contact-required"/>
                    </div>

                    <label class="control-label" for="">Date Created</label>
                    <div class="controls controls-row">
                         <div class="span5">
                            <input type="text" name="fileProxies[{%=idx%}].fileCreatedDate" class="date" placeholder="mm/dd/yyyy" value="{%=file.fileCreatedDate%}">
                         </div>
                    </div>

                    <label class="control-label">Description</label>
                    <div class="controls controls-row">
                        <div class="span5">
                            <textarea class="input-block-level" name="fileProxies[{%=idx%}].description" rows="1" placeholder="Enter a description here">{%=file.description%}</textarea>
                        </div>
                    </div>
                </div>

            </td>
        <td style="width:10%">

            {%if (file.fileId) { %}
            <@fileuploadButton label="Replace" id="fileupload{%=idx%}" cssClass="replace-file" buttonCssClass="replace-file-button btn btn-small btn-warning btn-block"/>
            <button type="button" style="display:none; text-align:left" class="btn btn-small btn-warning undo-replace-button btn-block" title="Restore Original File">Restore</button>
            {% } %}


                <div class="delete">
                    <button class="btn btn-danger delete-button btn-small btn-block" data-type="{%=file.delete_type%}" data-url="{%=file.delete_url%}" style="text-align:left ">
                        <i class="icon-trash icon-white"></i>
                        <span>Delete</span>
                    </button>
                </div>
            

            <div class="fileProxyFields">
                <input type="hidden" class="fileAction" name="fileProxies[{%=idx%}].action" value="{%=file.action||'ADD'%}"/>
                <input type="hidden" class="fileId" name="fileProxies[{%=idx%}].fileId" value="{%=''+(file.fileId || '-1')%}"/>
                <input type="hidden" class="fileReplaceName" name="fileProxies[{%=idx%}].filename" value="{%=file.name%}"/>
                <input type="hidden" class="fileSequenceNumber" name="fileProxies[{%=idx%}].sequenceNumber" value="{%=idx%}"/>
            </div>
        </td>
    </tr>
{% } %}
</script>

</#macro>

<#macro acceptedFileTypesRegex>
/\.(<@join sequence=validFileExtensions delimiter="|"/>)$/i<#t>
</#macro>


<#macro subNavMenu>
    <#local supporting = resource.resourceType.supporting >
    <div id='subnavbar' class="subnavbar-scrollspy affix-top subnavbar resource-nav navbar-static  screen"  data-offset-top="250" data-spy="affix" >
      <div class="">
        <div class="container" >
        <ul class="nav">
        	<li class="alwaysHidden"><a href="#top">top</a></li>
            <li class="active hidden-tablet hidden-phone"><a href="#basicInformationSection">Basic</a></li>
             <#if persistable.resourceType?has_content && persistable.resourceType != 'PROJECT' >
            <li><a href="#authorshipSection">Authors</a></li>
            </#if>
            <#if persistable.resourceType?has_content && persistable.resourceType != 'PROJECT'  && (!supporting)><li><a href="#divFileUpload">Files</a></li></#if>
            <#nested />
             <#if persistable.resourceType?has_content && persistable.resourceType != 'PROJECT' >
            <li><a href="#organizeSection"><span class="visible-phone visible-tablet" title="Project">Proj.</span><span class="hidden-phone hidden-tablet">Project</span></a></li>
            </#if>
            <#if !supporting>
            <li><a href="#spatialSection">Where</a></li>
            <li class="hidden-phone"><a href="#temporalSection">When</a></li>
            <li><a href="#investigationSection">What</a></li>
            <li class="hidden-phone"><a href="#siteSection">Site</a></li>
            </#if>
            <li class="hidden-tablet hidden-phone"><a href="#resourceNoteSectionGlide">Notes</a></li>
            <li><a href="#divAccessRights"><span class="visible-phone visible-tablet" title="Permissions">Permis.</span><span class="hidden-phone hidden-tablet">Permissions</span></a></li>
        </ul>
            <div id="fakeSubmitDiv" class="pull-right">
                <button type=button class="button btn btn-primary submitButton" id="fakeSubmitButton">Save</button>
                <img alt="progress indicator" src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner"  style="display:none"/>
            </div>
        </div>
      </div>
    </div>
</#macro>


<#macro listMemberUsers >
<#local _authorizedUsers=account.authorizedMembers />
<#if !_authorizedUsers?has_content><#local _authorizedUsers=[blankPerson]></#if>

<div id="accessRightsRecords" class="repeatLastRow" data-addAnother="add another user">
    <div class="control-group">
        <label class="control-label">Users</label>
        <div class="controls">
        <#list _authorizedUsers as user>
            <#if user??>
                <div class="controls-row repeat-row" id="userrow_${user_index}_">
                    <div class="span6">
                        <@registeredUserRow person=user _indexNumber=user_index includeRepeatRow=false/>
                    </div>
                    <div class="span1">
                        <@nav.clearDeleteButton id="user${user_index}"  />
                    </div>
                </div>
            </#if>
        </#list>
        </div>
    </div>
</div>

</#macro>

<#--render person control assumed to be directly inside of a grid-sized element and inside of a control group -->
<#macro personControl person person_index isDisabled namePrefix >
</#macro>


<#macro userRow person=person _indexNumber=0 isDisabled=false prefix="authorizedMembers" required=false _personPrefix="" includeRole=false
    includeRepeatRow=false includeRights=false  hidden=false isUser=false leadTitle="">
<#local disabled =  isDisabled?string("disabled", "") />
<#local readonly = isDisabled?string("readonly", "") />
<#local lookupType="nameAutoComplete"/>
<#if isUser><#local lookupType="userAutoComplete notValidIfIdEmpty"/></#if>
<#local _index=""/>
<#if _indexNumber?string!=''><#local _index="[${_indexNumber?c}]" /></#if>
<#local personPrefix="" />
<#if _personPrefix!=""><#local personPrefix=".${_personPrefix}"></#if>
<#local strutsPrefix="${prefix}${_index}" />
<#local rowIdElement="${prefix}Row_${_indexNumber}_p" />
<#local idIdElement="${prefix}Id__id_${_indexNumber}_p" />
<#local requiredClass><#if required>required</#if></#local>
<#local firstnameTitle>A ${leadTitle}first name<#if required> is required</#if></#local>
<#local surnameTitle>A ${leadTitle}last name<#if required> is required</#if></#local>
    <div id='${rowIdElement}' class="creatorPerson <#if hidden>hidden</#if> <#if includeRepeatRow>repeat-row</#if>">
        <@s.hidden name='${strutsPrefix}${personPrefix}.id' value='${(person.id!-1)?c}' id="${idIdElement}"  cssClass="" onchange="this.valid()" autocompleteParentElement="#${rowIdElement}"   />
        <div class="controls-row">
                <@s.textfield theme="tdar" cssClass="span2 ${lookupType} ${requiredClass}" placeholder="Last Name"  readonly=isDisabled autocompleteParentElement="#${rowIdElement}"
                    autocompleteIdElement="#${idIdElement}" autocompleteName="lastName" autocomplete="off"
                    name="${strutsPrefix}${personPrefix}.lastName" maxlength="255"
                    title="${surnameTitle}"
                     />
                <@s.textfield theme="tdar" cssClass="span2 ${lookupType} ${requiredClass}" placeholder="First Name"  readonly=isDisabled autocomplete="off"
                    name="${strutsPrefix}${personPrefix}.firstName" maxlength="255" autocompleteName="firstName"
                    autocompleteIdElement="#${idIdElement}"
                    autocompleteParentElement="#${rowIdElement}"
                     title="${firstnameTitle}"
                    />

            <#if includeRole || includeRights>
                    <#if includeRole>
                        <@s.select theme="tdar" name="${strutsPrefix}.role"  autocomplete="off" listValue='label' list=relevantPersonRoles
                            cssClass="creator-role-select span2" />
                    <#else>
                        <@s.select theme="tdar" cssClass="creator-rights-select span2" name="${strutsPrefix}.generalPermission" emptyOption='false'
                            listValue='label' list='%{availablePermissions}' disabled=isDisabled />
                        <#--HACK: disabled fields do not get sent in request, so we copy generalPermission via hidden field and prevent it from being cloned -->
                        <@s.hidden name="${strutsPrefix}.generalPermission" cssClass="repeat-row-remove" />
                    </#if>
            <#else>
                <span class="span2">&nbsp;</span> 
            </#if>
        </div>
        <div class="controls-row">
                <@s.textfield theme="tdar" cssClass="span3 ${lookupType} skip_validation" placeholder="Email (optional)" readonly=isDisabled autocomplete="off"
                    autocompleteIdElement="#${idIdElement}" autocompleteName="email" autocompleteParentElement="#${rowIdElement}"
                    name="${strutsPrefix}${personPrefix}.email" maxlength="255"/>
                <@s.textfield theme="tdar" cssClass="span3 ${lookupType} skip_validation" placeholder="Institution Name (Optional)" readonly=isDisabled autocomplete="off"
                    autocompleteIdElement="#${idIdElement}"
                    autocompleteName="institution"
                    autocompleteParentElement="#${rowIdElement}"
                    name="${strutsPrefix}${personPrefix}.institution.name" maxlength="255" />

        </div>
    </div>
</#macro>



<#macro registeredUserRow person=person _indexNumber=0 isDisabled=false prefix="authorizedMembers" required=false _personPrefix="" 
    includeRepeatRow=false includeRights=false  hidden=false leadTitle="">
<#local disabled =  isDisabled?string("disabled", "") />
<#local readonly = isDisabled?string("readonly", "") />
<#local lookupType="userAutoComplete notValidIfIdEmpty"/>
<#local _index=""/>
<#if _indexNumber?string!=''><#local _index="[${_indexNumber?c}]" /></#if>
<#local personPrefix="" />
<#if _personPrefix!=""><#local personPrefix=".${_personPrefix}"></#if>
<#local strutsPrefix="${prefix}${_index}" />
<#local rowIdElement="${prefix}Row_${_indexNumber}_p" />
<#local idIdElement="${prefix}Id__id_${_indexNumber}_p" />
<#local requiredClass><#if required>required</#if></#local>
<#local nameTitle>A ${leadTitle} name<#if required> is required</#if></#local>
    <div id='${rowIdElement}' class="creatorPerson <#if hidden>hidden</#if> <#if includeRepeatRow>repeat-row</#if>">
        <@s.hidden name='${strutsPrefix}${personPrefix}.id' value='${(person.id!-1)?c}' id="${idIdElement}"  cssClass="" onchange="this.valid()"  autocompleteParentElement="#${rowIdElement}"   />
        <div class="controls-row">
            <@s.textfield theme="simple" cssClass="span3 ${lookupType} ${requiredClass}" placeholder="Name"  readonly=isDisabled autocomplete="off"
                name="${strutsPrefix}${personPrefix}.tempDisplayName" maxlength="255" autocompleteName="tempDisplayName"
                autocompleteIdElement="#${idIdElement}" 
                autocompleteParentElement="#${rowIdElement}" 
                 title="${nameTitle}"
                dynamicAttributes={"data-msg-notValidIfIdEmpty":"Invalid user name.  Please type a name (or partial name) and choose one of the options from the menu that appears below."}
                />

            <#if includeRights>
                    <@s.select theme="tdar" cssClass="creator-rights-select span3" name="${strutsPrefix}.generalPermission" emptyOption='false'
                        listValue='label' list='%{availablePermissions}' disabled=isDisabled />
                    <#--HACK: disabled fields do not get sent in request, so we copy generalPermission via hidden field and prevent it from being cloned -->
                    <@s.hidden name="${strutsPrefix}.generalPermission" cssClass="repeat-row-remove" />
            <#else>
                <span class="span2">&nbsp;</span> 
            </#if>
        </div>
    </div>
</#macro>

<#macro institutionRow institution _indexNumber=0 prefix="authorizedMembers" required=false includeRole=false _institutionPrefix=""  
    hidden=false leadTitle="">
<#local _index=""/>
<#if _indexNumber?string!=''><#local _index="[${_indexNumber}]" /></#if>
<#local institutionPrefix="" />
<#if _institutionPrefix!=""><#local institutionPrefix=".${_institutionPrefix}"></#if>
<#local strutsPrefix="${prefix}${_index}" />
<#local rowIdElement="${prefix}Row_${_indexNumber}_i" />
<#local idIdElement="${prefix}Id__id_${_indexNumber}_i" />
<#local requiredClass><#if required>required</#if></#local>
<#local institutionTitle>The ${leadTitle}institution name<#if required> is required</#if></#local>

    <div id='${rowIdElement}' class="creatorInstitution <#if hidden >hidden</#if>">

        <@s.hidden name='${strutsPrefix}${institutionPrefix}.id' value='${(institution.id!-1)?c}' id="${idIdElement}"  cssClass="" onchange="this.valid()"  autocompleteParentElement="#${rowIdElement}"  />
                <div class="controls-row">
                    <@s.textfield theme="tdar" cssClass="institutionAutoComplete institution span4 ${requiredClass}" placeholder="Institution Name" autocomplete="off"
                        autocompleteIdElement="#${idIdElement}" autocompleteName="name" 
                        autocompleteParentElement="#${rowIdElement}"
                        name="${strutsPrefix}${institutionPrefix}.name" maxlength="255"
                        title="${institutionTitle}" 
                        />

                    <#if includeRole>
                    <@s.select theme="tdar" name="${strutsPrefix}.role" listValue='label' list=relevantInstitutionRoles cssClass="creator-role-select span2" />
                    <#else>
                    <#-- is includeRole ever false?  if not we should ditch the parm entirely, perhaps the entire macro. -->
                    <div class="span2">&nbsp;</div>
                    </#if>
                </div>
    </div>
</#macro>

<#macro fileuploadButton id="fileuploadButton" name="" label="" cssClass="" buttonCssClass="">
<span class="btn fileinput-button ${buttonCssClass}" id="${id}Wrapper" style="width:6em;text-align:left">
    <i class="icon-refresh icon-white"> </i>
    <span>${label}</span>
    <input type="file" name="uploadFile" id="${id}" class="${cssClass}">
</span>
</#macro>

<#macro datefield date name="" id=name cssClass="" label="" format="MM/dd/yyyy" placeholder="mm/dd/yyyy" >
    <#local val = "">
    <#if date?has_content>
    <#local val = date?string(format)>
    </#if>
    <@s.textfield name="${name}" id="${id}" cssClass="${cssClass}" label="${label}" placeholder="${placeholder}" value="${val}" />
</#macro>

<#macro personAutocompleteTemplate>
<script id="template-person-autocomplete-li" type="text/x-tmpl">
    <li class="{%=o.addnew?'addnew':''%}">
        <a><#-- person-{id} class used below to allow the test autocomplete to work without having access to email addresses -->
            <div class="person-{%=_e(o.id)%}">
                <span class="name">{%=_e(o.properName)%}</span>
                {% if(o.email)  %}<span class="email">({%=_e(o.email)%})</span>{%
                %}{% if(o.institution && o.institution.name) { %}, <span class="institution">{%=_e(o.institution.name)%}</span> {% } %}
                {% if(o.addnew) { %}<em>Create a new person record</em> {% } %}
            </div>
        </a>
    </li>
</script>
</#macro>

</#escape>