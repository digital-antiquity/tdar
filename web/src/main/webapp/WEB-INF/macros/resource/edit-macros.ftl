<#--
$Id$ 
Edit freemarker macros.  Getting large, should consider splitting this file up.
-->
<#-- include navigation menu in edit and view macros -->
<#escape _untrusted as _untrusted?html>
    <#import "common-resource.ftl" as commonr>
    <#import "../common.ftl" as common>
    <#import "/WEB-INF/macros/helptext.ftl" as  helptext>
    <#import "/WEB-INF/settings.ftl" as settings>
    <#import "../navigation-macros.ftl" as nav>
    <#import "../common-rights.ftl" as rights>

	<#assign useSelect2=select2Enabled!true  />

    <#macro basicInformation itemTypeLabel="file" itemPrefix="resource">

    </#macro>



<#-- Emit the choose-a-collection section -->
<#macro resourceCollectionSection prefix="resourceCollections" label="Collection" list=[] >
    <#local _resourceCollections = [blankResourceCollection] />
    <#local collectionType="LIST"/>
    <#if prefix=='shares'>
        <#local collectionType="SHARED"/>
        <#local _resourceCollections = [blankShare] />
    </#if> 
    
    <h3>Collection Membership <@helptext.info title="Collection Membership" contentDiv="#divResourceCollectionListTips" /></h3>               

    <#if (list?has_content && list?is_collection && (list?size!0) > 0 )>
        <#local _resourceCollections = list />
    </#if>
    
    <@helptext.resourceCollection />
    
    <div>
        <#if (ableToAdjustPermissions?? && ableToAdjustPermissions) || resource.resourceType.project || rightsPage!false >
            <div id="${prefix}Table" class="form-group row repeatLastRow" addAnother="add another ${label}">
                <label class="col-form-label col-2">${label}</label>

                <div class="col-10">
                    <#list _resourceCollections as resourceCollection>
                    <#-- emit a single row of the choose-a-collection section -->
                    <div id="${prefix}Row_${resourceCollection_index}_" class="row repeat-row">
                        <@s.hidden name="${prefix}[${resourceCollection_index}].id"  id="${prefix}Row_${resourceCollection_index}_id" />
                        <@s.textfield  id="txt${prefix}Row_${resourceCollection_index}_id" name="${prefix}[${resourceCollection_index}].name" cssClass="col-11 collectionAutoComplete "  autocomplete="off"
                        autocompleteIdElement="#${prefix}Row_${resourceCollection_index}_id" maxlength=255
                        collectionType="${collectionType}"
                        autocompleteParentElement="#${prefix}Row_${resourceCollection_index}_" />
                    
                        <@nav.clearDeleteButton id="${prefix}Row" />
                    </div>
                    </#list>
                   
                    <#if resource.resourceType.project>
                        <span class="help-inline"><em>Note</em>: adding this project to a collection will not include the resources within this project.</span>
                    </#if>
                </div>
            </div>
        
        <#local _hidden = true>
        <#if (effectiveShares?size > 0)>
            <#list effectiveShares as share>
                <#if share.viewable == false>
                    <#local _hidden = false>
                </#if>
            </#list>
        </#if> 

        <#if !_hidden >
            <p id="effectiveCollectionsVisible"><i><b>The following collections cannot be modified because you do not have sufficient permissions:</b>
            <#assign comma =false>
            <#list effectiveShares as share>
                <#if share.viewable == false>
                    <#if comma>,</#if>${share.name} <#assign comma=true />
                </#if>
            </#list>
            </i></p>
        </#if>

        <#else>
        <p>Collection selection is disabled because you don't have full rights on this resource.</p>    
        </#if>
    </div>
</#macro>


<#-- emit a div containing "repeatable"  keyword fields
    @param label:string label associated with the section  (e.g. "Geographic Keywords")
    @param keywordList:list<Keyword> collection of Keyword objects.  this macro will populate the section with
            input fields for each element in the list, with the value of the element pre-populated with the keyword.label value
    @param keywordField:string prefix to use when generating the name-attribute for the input fields in this section.
-->
    <#macro keywordRows label keywordList keywordField className showDelete=true addAnother="add another keyword">
    
        <div class="form-group row repeatLastRow" id="${keywordField}Repeatable" data-add-another="${addAnother}">
            <label class="col-form-label col-2">${label}</label>
            <div class="form-group col-10">
            <#list keywordList as keyword>
                <@_keywordRow keywordField keyword_index showDelete />
            <#else>
                <@_keywordRow keywordField />
            </#list>
	        </div>
        </div>

    
    </#macro>

    <#macro _keywordRow keywordField keyword_index=0 showDelete=true>
    <div class="controls repeat-row row" id='${keywordField}Row_${keyword_index}_'>
        <@s.textfield name='${keywordField}[${keyword_index}]'  maxlength=255 cssClass='col-11 keywordAutocomplete' placeholder="enter keyword" includeGroup=false />
        <#if showDelete>
        <@nav.clearDeleteButton id="${keywordField}Row" />
        </#if>
    </div>
    </#macro>



<#-- render the "spatial information" section:geographic keywords, map, coordinates, etc. -->
    <#macro spatialContext showInherited=true>
    <div class="col-12" id="spatialSection">
        <h2 id="spatialInfoSectionLabel">Spatial Terms</h2>
        <@_inheritsection checkboxId="cbInheritingSpatialInformation" name='resource.inheritingSpatialInformation' showInherited=showInherited sectionId='#divSpatialInformation' />
        <div id="divSpatialInformation">

            <div>
                <@keywordRows "Geographic Terms" geographicKeywords 'geographicKeywords' "GeographicKeyword" /> 
            </div>
            <@helptext.geo />
            <h4>Geographic Region <@helptext.info title="Enter Coordinates" contentDiv="#divManualCoordinateEntryTip" /></h4>

        <div id='large-map' style="height:300px" class="leaflet-map-editable col-12" data-search="true">
            <div id="divManualCoordinateEntry" class="latlong-fields">
                <@s.checkbox id="viewCoordinatesCheckbox" name="_tdar.viewCoordinatesCheckbox" onclick="TDAR.common.coordinatesCheckboxClicked(this);" label='Enter / View Coordinates' labelposition='right'  />
                <div id='explicitCoordinatesDiv' style='text-align:center;'>
                    <table cellpadding="0" cellspacing="0" style="margin-left:auto;margin-right:auto;text-align:left;">
                        <tr>
                            <td></td>
                            <td>
                                <@s.textfield  theme="simple" name='latitudeLongitudeBoxes[0].north' id='maxy' size="14" cssClass="maxy float latLong ne-lat" title="Please enter a valid Maximum Latitude" />
                                <input type="text" id='d_maxy' placeholder="Latitude (max)" class="ne-lat-display form-control d_maxy"/>
                            </td>
                            <td></td>
                        </tr>
                        <tr>
                            <td style="width:33%;text-align:center">
                                <@s.textfield theme="simple"  name="latitudeLongitudeBoxes[0].west" id='minx' size="14" cssClass="minx float latLong sw-lng" title="Please enter a valid Minimum Longitude" />
                                <input type="text" id='d_minx' placeholder="Longitude (min)" class="sw-lng-display form-control d_minx"/>
                            </td>
                            <td style="width:33%;text-align:center">
                                <input type="button" id="locate" value="Locate" class="btn locateCoordsButton"/>
                            </td>
                            <td style="width:33%;text-align:center">
                                <@s.textfield theme="simple"  name="latitudeLongitudeBoxes[0].east" id='maxx' size="14" cssClass="maxx float latLong ne-lng" title="Please enter a valid Maximum Longitude" />
                                <input type="text" id='d_maxx' placeholder="Longitude (max)" class="d_maxx ne-lng-display form-control"/>
                            </td>
                        </tr>
                        <tr>
                            <td></td>
                            <td>
                                <@s.textfield theme="simple"  name="latitudeLongitudeBoxes[0].south" id="miny" size="14" cssClass="miny float latLong sw-lat" title="Please enter a valid Minimum Latitude" />
                                <input type="text" id="d_miny" placeholder="Latitude (min)" class="d_miny sw-lat-display  form-control"/>
                            </td>
                            <td></td>
                        </tr>
                    </table>
                </div>
            </div>
            <div class="mapdiv"></div>
                <@helptext.manualGeo />
        </div>
            <#if config.switchableMapObfuscation>
                <@helptext.showExactLocationTip />
                    <div class="" id="showExactLocation" data-tiplabel="Reveal location to public users?" data-tooltipcontent="#showExactLocationHelpDiv" >
                        <@s.checkbox id="is_okay_to_show_exact_location" name="latitudeLongitudeBoxes[0].okayToShowExactLocation" label='Reveal location to public users?' labelposition='right'  />
                </div>
            </#if>
        </div>
    </div>
    </#macro>

<#-- emit resource.resourceType.lable
    @requires resource:Resource
-->
    <#macro resourceTypeLabel>
        <#if bulkUpload>
        Resource
        <#else>
            <#noescape>${resource.resourceType.label}</#noescape>
        </#if>
    </#macro>

<#-- emit the "temporal context" section (temporal coverage, temporal keywords) -->
    <#macro temporalContext showInherited=true>
    <div class="col-12" id="temporalSection">
        <h2 id="temporalInfoSectionLabel">Temporal Coverage</h2>
        <@_inheritsection checkboxId="cbInheritingTemporalInformation" name='resource.inheritingTemporalInformation' showInherited=showInherited sectionId='#divTemporalInformation' />
        <div id="divTemporalInformation">
            <div data-tooltip="Temporal terms relevant to the document, e.g. &quot;Pueblo IV&quot; or &quot;Late Archaic&quot;.">
                <@keywordRows "Temporal Terms" temporalKeywords 'temporalKeywords' "TemporalKeyword" true "add another temporal keyword" />
            </div>
            <@_coverageDatesSection />
        </div>
    </div>
    </#macro>

<#-- emit the coverage dates section (temporal coverage, temporal keywords) -->
    <#macro _coverageDatesSection>
        <@helptext.coverageDates />
    <div class="form-group repeatLastRow" id="coverageDateRepeatable" data-add-another="add another coverage date">
        <label class="col-form-label">Coverage Dates <@helptext.info title="Coverage Dates" contentDiv="#coverageDatesTip" /></label>

        <#list coverageDates as coverageDate>
            <#if coverageDate??>
                <@_dateRow coverageDate coverageDate_index/>
            </#if>
			<#else>
                <@_dateRow blankCoverageDate coverageDate_index/>
        </#list>
    </div>
    </#macro>
    <#macro _dateRow proxy=proxy proxy_index=0>
    <div class="controls form-row" id="DateRow_${proxy_index}_">
    <#--<@s.hidden name="coverageDates[${proxy_index}].id" cssClass="dont-inherit" /> -->
        <@s.select name="coverageDates[${proxy_index}].dateType" cssClass="coverageTypeSelect col-2"
    listValue='label'  headerValue="Date Type" headerKey="NONE"
    list=allCoverageTypes />
        <div class="col-2">
        <@s.textfield placeholder="Start Year" cssClass="coverageStartYear mr-1 trim" name="coverageDates[${proxy_index}].startDate" maxlength="10" />
        </div>
        <div class="col-2">
        <@s.textfield placeholder="End Year" cssClass="coverageEndYear mr-1 trim" name="coverageDates[${proxy_index}].endDate" maxlength="10" />
        </div>
        <div class="col-5">
        <@s.textfield placeholder="Description"  cssClass="coverageDescription  trim" name="coverageDates[${proxy_index}].description"  maxlength=255 />
        </div>
       <@nav.clearDeleteButton id="{proxy_index}DateRow"/>
    </div>
    </#macro>


<#-- emit the "general keywords" repeatable fields -->
    <#macro generalKeywords showInherited=true>
    <div
             class="col-12" data-tooltip="Add additional keywords for this resource">
        <h2 id="generalInfoSectionLabel">General Keywords</h2>
        <@_inheritsection checkboxId="cbInheritingOtherInformation" name='resource.inheritingOtherInformation'  showInherited=showInherited sectionId='#divOtherInformation'/>
        <div id="divOtherInformation">
            <@keywordRows "Keyword" otherKeywords 'otherKeywords' "OtherKeyword" />
        </div>
    </div>
    </#macro>

<#-- emit the "Site Information" section of an edit page; controlled/uncontrolled site types, site names.
     @see hier.checkboxlist for information on how hierarchical checkboxlists work
 -->
    <#macro siteKeywords showInherited=true divTitle="Site Information">
        <@helptext.siteName />
    <div id="siteSection" class="col-12">
        <h2 id="siteInfoSectionLabel">${divTitle} <@helptext.info title="Site Information" contentDiv="#siteinfohelp"/></h2>
        <@_inheritsection checkboxId='cbInheritingSiteInformation' name='resource.inheritingSiteInformation'  showInherited=showInherited sectionId='#divSiteInformation'/>
        <div id="divSiteInformation">
            <@keywordRows "Site Name / Number" siteNameKeywords 'siteNameKeywords' "SiteNameKeyword" />

            <div class="form-group row">
                <label class="col-form-label col-2">Site Type</label>

                <div class="controls col-10">
                    <@s.checkboxlist theme="hier" name="approvedSiteTypeKeywordIds" keywordList="approvedSiteTypeKeywords" />
                </div>
            </div>

            <@keywordRows "Other" uncontrolledSiteTypeKeywords 'uncontrolledSiteTypeKeywords' "SiteTypeKeyword" />
        </div>
    </div>
    </#macro>

<#-- render material types section as a list of keywords
    @param showInherited:boolean  if true, show the "inherit from project" checkbox
    @requires  allMaterialKeywords:list<Keyword>  list of keywords to populate the checkboxlist key=keyword.id,
                    value=keyword.label
    @requires materialKeywordIds:list<long>  list of ids corresponding to checkboxes that chould be "checked"
 -->
    <#macro materialTypes showInherited=true>
    <div class="col-12">
        <@helptext.materialType />
        <h2 id="materialInfoSectionLabel">Material Types</h2>
        <@_inheritsection checkboxId='cbInheritingMaterialInformation' name='resource.inheritingMaterialInformation'  showInherited=showInherited sectionId='#allMaterialInformation' />
		<div id="allMaterialInformation">
	        <div id="divMaterialInformation">
	            <@s.checkboxlist name='approvedMaterialKeywordIds' list='allMaterialKeywords' listKey='id' listValue='label' listTitle="definition"  label="Select Type(s)" labelposition="left"
	            spanClass="col" numColumns="3" />
    	    </div>

            <@keywordRows "Other" uncontrolledMaterialKeywords 'uncontrolledMaterialKeywords' "MaterialKeyword" />
		</div>
    </div>
    </#macro>

<#-- render material types section: repeatable keyword text fields and checkboxlist of approved keywords
    @param showInherited:boolean  if true, show the "inherit from project" checkbox
    @param inline:boolean not used
    @requires  approvedkeywordIds:list<long>
    @requires  approvedCultureKeywords:list<Keyword>
 -->
    <#macro culturalTerms showInherited=true inline=false>
    <div class="col-12">
        <@helptext.cultureTerms />
        <h2 id="culturalInfoSectionLabel">${culturalTermsLabel!"Cultural Terms"}</h2>
        <@_inheritsection checkboxId="cbInheritingCulturalInformation" name='resource.inheritingCulturalInformation'  showInherited=showInherited sectionId='#divCulturalInformation'/>
        <div id="divCulturalInformation">
            <div class="form-group row">
                <label class="col-form-label col-2">${culturalTermsLabel!"Culture"}</label>

                <div class="controls col-10">
                    <@s.checkboxlist theme="hier" name="approvedCultureKeywordIds" keywordList="approvedCultureKeywords" />
                </div>
            </div>
            <!--"add another cultural term" -->
            <@keywordRows "Other" uncontrolledCultureKeywords 'uncontrolledCultureKeywords'  "CultureKeyword" />
        </div>
    </div>
    </#macro>

<#-- emit investigation types
    @param showInherited:boolean
    @requires  allInvestigationTypes:list<Keyword>
    @requires  investigationTypeIds:list<long>
 -->
    <#macro investigationTypes showInherited=true >
    <div id="investigationSection" class="col-12">
        <h2 id="investigationInfoSectionLabel">Investigation Types</h2>
        <@_inheritsection checkboxId='cbInheritingInvestigationInformation' name='resource.inheritingInvestigationInformation'  showInherited=showInherited sectionId='#divInvestigationInformation' />
        <div id="divInvestigationInformation">

            <@s.checkboxlist name='investigationTypeIds' list='allInvestigationTypes' listKey='id' listValue='label' numColumns="2" spanClass="col" labelposition="left"
                 label="Select Type(s)" listTitle="definition" />
        </div>
    </div>
        <@helptext.investigationType />
    </#macro>


<#-- provides a fieldset just for full user access -->
    <#macro fullAccessRights tipsSelector="#divAccessRightsTips" label="Users who can view or modify this resource" type="resource" header=true>
        <#local _authorizedUsers=authorizedUsers />
        <#local _isSubmitter = authenticatedUser.id == ((persistable.submitter.id)!-1)>
        <#if _authorizedUsers.empty><#local _authorizedUsers=[blankAuthorizedUser]></#if>
        <@helptext.accessRights />


    <div id="divAccessRights">
        <#if header>
                <h2><a name="accessRights"></a>Access Rights <@helptext.info title="Access Rights" contentDiv="${tipsSelector}" /></h2>
        </#if>
    <#--<#if type == 'resource'>-->
        <#--<@resourceCollectionSection prefix="shares" label="Shares" list=shares />-->
    <#--</#if>-->

        <h3>${label}<#if !header><@helptext.info title="Access Rights" contentDiv="${tipsSelector}" /></#if></h3>

        <div id="accessRightsRecords" class="<#if (ableToUploadFiles?? && ableToUploadFiles) || (!ableToUploadFiles?has_content)>repeatLastRow</#if>"
             data-addAnother="add another user">
            <div class="form-group">
                <label class="col-form-label">Users</label>

                <div class="controls">
                    <#list _authorizedUsers as authorizedUser>
                        <#if authorizedUser??>
                            <div class="form-row repeat-row" id="authorizedUsersRow_${authorizedUser_index}_">
                                <div class="col-6">
                                    <@registeredUserRow person=authorizedUser.user isDisabled=!authorizedUser.enabled _indexNumber=authorizedUser_index  _personPrefix="user"
                                    prefix="authorizedUsers" includeRights=true includeRepeatRow=false />
                                </div>
                                <div class="col-1">
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
            <#--Note: this does not reflect changes to resource collection you have made until you save.-->
            <@rights.resourceCollectionsRights collections=effectiveResourceCollections owner=submitter  />
        </#if>

    </div>
    </#macro>

<#-- emit the category & subcategory SELECT fields
    @requires allDomainCategories:List<CategoryVariable>
    @requires subcategories:List<CategoryVariable>
-->
    <#macro categoryVariable>
    <div class="form-group row">
        <label class="col-form-label col-2">Category / Subcategory</label>
        <div class="col-10">
        <div class="controls form-row">
            <div id='categoryDivId' class="col-6">
                <@s.select id='categoryId' name='categoryId'
                onchange='TDAR.common.changeSubcategory("#categoryId","#subcategoryId")' autocompleteName="sortCategoryId"
                listKey='id' listValue='name' emptyOption='true' list='%{allDomainCategories}' cssClass="form-control" />
            </div>
            <div id='subcategoryDivId' class="col-6">
                <@s.select id='subcategoryId' name='subcategoryId'
                autocompleteName="subCategoryId" headerKey="-1" listKey='id' headerValue="N/A" list='%{subcategories}'  cssClass="input-block-level" />
            </div>
        </div>
        </div>
    </div>
    </#macro>

<#-- emit the manual-text-entry section (i.e. for ontology or coding-sheet values) -->
    <#macro manualTextInput typeLabel type uploadOptionText manualEntryText>
    <#-- show manual option by default -->
        <#local usetext=(resource.latestVersions.empty || (fileTextInput!"") != "")>
    <div id="enter-data">
        <h2>${(resource.id == -1)?string("Submit", "Replace")} ${typeLabel}</h2>

        <div class="form-group row">
            <label class='col-form-label col-2' for='inputMethodId'>Submit as</label>

                <select id='inputMethodId' name='fileInputMethod' onchange='TDAR.common.refreshInputDisplay()' class="col-10 form-control">
                    <option value='file' <#if !usetext>selected="selected"</#if>>${uploadOptionText}</option>
                    <option value='text' <#if usetext>selected="selected"</#if>>${manualEntryText}</option>
                </select>
        </div>

        <div id='uploadFileDiv' style='display:none;'>
            <div id="uploadFileExampleDiv" class="form-group">
                <div class="controls">
                    <#nested 'upload'>
                </div>
            <@_singleFileUpload />
            </div>
        </div>

        <div id='textInputDiv'>
            <div id="textInputExampleDiv" class="form-group">
                <div class="controls">
                    <#nested 'manualEntry'>
                </div>
            </div>
            <@s.textarea label='${typeLabel}' labelposition='top' id='fileInputTextArea' name='fileTextInput' rows="5" cssClass='resizable resize-vertical col-12' cols="80" />
    </div>

    </#macro>

<#-- emit the section containing an edit form's "save" button,  including stubs for error messages and reminders
    @param label:string label of the actual save button
    @param fileReminder:boolean if true, render the 'did you upload a file yet?' reminder
    @param buttonid:string  element id value for the actual save button
    @param span:string  css class name specifying the column-sizing of the DIV for this section, using Bootstrap v2
        syntax
    @nested any additional html/freemarker content - will be injected in div#editFormActions prior to the save button
        element
-->
    <#macro submit label="Save" fileReminder=true buttonid="submitButton" span="col-9" class="btn-primary submitButton">
    <div class="errorsection ${span}">
            <#if fileReminder>
                <div id="reminder" class="">
                    <p><span class="label label-info">Reminder</span> No files are attached to this record. </p>
                </div>
            </#if>
        <#-- if you put an error class on this, then you get a pink box at the bottom of every page visible on submit, ugly -->
            <div id="error" class="" style="display:none">
                <ul></ul>
            </div>
            <div class="form-actions col-12" id="editFormActions">
                <input type="submit" class='btn ${class} submittableButtons' name="submitAction" value="${label}" id="${buttonid}">
                <img alt="progress indicator" title="progress indicator"  src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="display:none"/>
                <#nested>
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


<#-- emit related collections section-->
    <#macro relatedCollections showInherited=true>
    <div class="col-12" id="relatedCollectionsSectionGlide">
        <h2 id="relatedCollectionInfoSectionLabel">Museum or Archive Collections</h2>
        <@_inheritsection checkboxId="cbInheritingCollectionInformation" name='resource.inheritingCollectionInformation' showInherited=showInherited sectionId='#relatedCollectionsSection'/>
        <div id="relatedCollectionsSection">
            <div id="divSourceCollectionControl" class="form-group repeatLastRow row">
                <label class="col-form-label col-2">Source Collections</label>
                <div class="col-10">
                <#list sourceCollections as sourceCollection>
                    <@_sourceCollectionRow sourceCollection "sourceCollection" sourceCollection_index/>
					<#else>
	                    <@_sourceCollectionRow blankSourceCollection "sourceCollection" sourceCollection_index/>
                </#list>
	            </div>
            </div>

            <div id="divRelatedComparativeCitationControl" class="form-group repeatLastRow row">
                <label class="col-form-label col-2">Related or Comparative Collections</label>
                <div class="col-10">
                <#list relatedComparativeCollections as relatedComparativeCollection>
                    <@_sourceCollectionRow relatedComparativeCollection "relatedComparativeCollection" relatedComparativeCollection_index/>
    				<#else>
                    <@_sourceCollectionRow blankRelatedComparativeCollection "relatedComparativeCollection" relatedComparativeCollection_index/>
                </#list>
                </div>
            </div>
            <@helptext.sourceRelatedCollection />
        </div>
    </div>
    </#macro>

<#-- emit source collections section-->
    <#macro _sourceCollectionRow sourceCollection prefix index=0>
        <#local plural = "${prefix}s" />
    <div class="controls repeat-row row" id="${prefix}Row_${index}_">
                <@s.textarea rows="4" cols="80" name='${plural}[${index}].text' cssClass="col-11 resizable resize-vertical" />
            <@nav.clearDeleteButton id="${prefix}Row${index}" />
    </div>
    </#macro>

<#-- emit an 'inherit from parent project' checkbox for a subsection of an edit form-->
    <#macro _inheritsection checkboxId name sectionId showInherited=true  label="Inherit this section" >
    <div class='divInheritSection'>
        <#if showInherited>
            <div class="form-group alwaysEnabled">
                <div class="controls offset-2">
                    <#if editor!false>
                        <button type="button" class="btn btn-xs btn-danger clear-section float-right"
                                data-clear-target="${sectionId}"
                                title="Admin only: reset checkboxes and remove multi-value fields in this inheritance section."
                                >Reset Section</button>
                    </#if>
                    <label class="checkbox">
                        <@s.checkbox theme="simple" name="${name}" id="${checkboxId}" />
                        <span class="labeltext">${label}</span>
                    </label>
                </div>
            </div>


        <#elseif resource??>
            <div id="${checkboxId}hint" class="inherit-tips">
                <em>Note: This section supports <strong>inheritance</strong>: values can be re-used by resources associated with a project.</em>
            </div>
        </#if>
    </div>
    </#macro>

<#-- emit 'resource notes' section
    @requires resourceNotes:list<ResourceNote>
-->
    <#macro resourceNoteSection showInherited=true>
    <div id="resourceNoteSectionGlide"  class="col-12">
        <@helptext.notes />
        <h2 id="notesInfoSectionLabel">Notes <@helptext.info title="Notes" contentDiv="#notesHelpDiv" /></h2>
        <@_inheritsection checkboxId="cbInheritingNoteInformation" name='resource.inheritingNoteInformation' showInherited=showInherited sectionId='#resourceNoteSection'/>
        <div id="resourceNoteSection" class="repeatLastRow row">
            <label class="col-form-label col-2">Type / Contents</label>
            <div class='col-10'>
            <#list resourceNotes as resourceNote>
                <#if resourceNote??><@_noteRow resourceNote resourceNote_index/></#if>
				<#else>
				<@_noteRow blankResourceNote resourceNote_index/>
            </#list>
	        </div>
        </div>
    </div>
    </#macro>
    <#macro _noteRow proxy note_index=0>
    <div id="resourceNoteRow_${note_index}_" class="repeat-row">
        <div class="controls form-row">
                <div class="col-11">
               	<div class="row">
                <@s.select  emptyOption='false' name='resourceNotes[${note_index}].type' list='%{noteTypes}' listValue="label" cssClass="col-4" />
                <@s.textarea rows="4"  name='resourceNotes[${note_index}].note' placeholder="enter note contents" cssClass=' col-12 resizable resize-vertical'
                maxlength='5000' cols="80" />
                </div>
                </div>
                <@nav.clearDeleteButton id="resourceNoteRow" />
        </div>
    </div>
    </#macro>


    <#-- emit a section for entering a list of resource creators associated with a particular reslurce.  This macro renders this section as a 'repeat-row'
     table of 'creaty proxy' controls (see @creatorProxyRow for more info)
      @param sectionTitle:string name for this section (displayed in header text)
      @param proxies:list<ResourceCreatorProxy> list of creator proxies
      @pram  prefix:string prefix to append to form field 'name' attribute.  This gets passed to @creatorProxyRow
    -->
    <#macro resourceCreators sectionTitle proxies prefix>
    <div class="col-12" 
         id="${prefix}Section">

        <h2 id="${prefix}InfoSectionLabel">${sectionTitle} <@helptext.info title="${sectionTitle}" contentDiv="#divResourceCreatorsTip" /></h2>
        <#nested>
        <div id="${prefix}Table" class="repeatLastRow creatorProxyTable">
            <#list proxies as proxy>
        		<@creatorProxyRow proxy  prefix proxy_index/>
				<#else>
        		<@creatorProxyRow blankCreatorProxy  prefix 0/>
	        </#list>
            </div>
    </div> <!-- section -->
    </#macro>

    <#-- Emit a form "control"  representing a resource creator. Each control is form fields that allow the user to enter information about a single person
     or a single institution (the user can toggle between one or the other). -->
    <#macro creatorProxyRow proxy=proxy prefix=prefix proxy_index=proxy_index type_override="NONE"
    required=false includeRole=true leadTitle="" showDeleteButton=true>
        <#assign relevantPersonRoles=personAuthorshipRoles />
        <#assign relevantInstitutionRoles=institutionAuthorshipRoles />
        <#if prefix=='credit'>
            <#assign relevantPersonRoles=personCreditRoles />
            <#assign relevantInstitutionRoles=institutionCreditRoles />
        </#if>

        <#if proxy??>
        <div id="${prefix}Row_${proxy_index}_" class="repeat-row row">
            <#assign creatorType = proxy.actualCreatorType!"PERSON" />
            <div class="col-3 col-lg-2">
                <div class="btn-group creator-toggle-button btn-group-toggle" data-toggle="buttons">
                    <#if type_override == 'PERSON' || (creatorType=='PERSON' && type_override=='NONE') >
                        <#local selectedType="PERSON"/>
                    <#else>
                        <#local selectedType="INSTITUTION"/>
                    </#if>
                    <button type="button"
                            class="btn btn-xs personButton <#if type_override == "PERSON" || (creatorType=='PERSON' && type_override=='NONE') >btn-dark active<#else> btn-secondary</#if>"
                            data-toggle="button">Person
                    </button>
                    <@s.hidden name="${prefix}Proxies[${proxy_index}].type" value="${selectedType}" cssClass="toggleValue" />
                    <#if !resource.resourceType.project && resource.inheritingIndividualAndInstitutionalCredit && prefix=='credit'>
                        <@s.hidden name="${prefix}Proxies[${proxy_index}].id" value="" cssClass="toggleValue resourceCreatorId" />
                    <#else>
                        <@s.hidden name="${prefix}Proxies[${proxy_index}].id" cssClass="toggleValue resourceCreatorId" />
                    </#if>
                    <button type="button"
                            class="btn btn-xs institutionButton <#if creatorType =='INSTITUTION' || type_override == "INSTITUTION">btn-active active btn-dark<#else>btn-secondary</#if>"
                            data-toggle="button">Institution
                    </button>
                </div>
            </div>
            <div class="col-8 col-lg-9">
                <@_userRow person=proxy.person _indexNumber=proxy_index _personPrefix="person" prefix="${prefix}Proxies"
                includeRole=includeRole hidden=(creatorType =='INSTITUTION' || type_override == "INSTITUTION")
                required=(required) leadTitle="${leadTitle}"/>

                <@institutionRow institution=proxy.institution _indexNumber=proxy_index includeRole=includeRole _institutionPrefix="institution"
                prefix="${prefix}Proxies" hidden=(type_override == "PERSON" || (creatorType=='PERSON' && type_override=='NONE'))
                required=(required) leadTitle="${leadTitle}"/>
            
            </div>
            
            <#if showDeleteButton>
                <button class="btn btn-sm form-control col-1 repeat-row-delete" type="button" tabindex="-1"><i class="fas fa-trash-alt float-right"></i></button>
            </#if>
            </div>
        </#if>
    </#macro>
    <#macro _userRow person=person _indexNumber=0 isDisabled=false prefix="authorizedMembers" required=false _personPrefix="" includeRole=false
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
    <div id='${rowIdElement}' class="creatorPerson mb-2 <#if hidden>hidden</#if> <#if includeRepeatRow>repeat-row</#if>">
        <@s.hidden name='${strutsPrefix}${personPrefix}.id' value='${(person.id!-1)?c}' id="${idIdElement}"  cssClass="" onchange="this.valid()" autocompleteParentElement="#${rowIdElement}"   />
        <div class="form-row">
            <div class="col-5">
            <@s.textfield cssClass="mr-2 ${lookupType} ${requiredClass} trim" placeholder="Last Name"  readonly=isDisabled autocompleteParentElement="#${rowIdElement}"
            autocompleteIdElement="#${idIdElement}" autocompleteName="lastName" autocomplete="off" formGroupClass="mb-p5"
            name="${strutsPrefix}${personPrefix}.lastName" maxlength="255"
            title="${surnameTitle}"
            />
            </div>
            <div class="col-5">
            <@s.textfield cssClass=" mr-2 ${lookupType} ${requiredClass} trim" placeholder="First Name"  readonly=isDisabled autocomplete="off" formGroupClass="mb-p5"
            name="${strutsPrefix}${personPrefix}.firstName" maxlength="255" autocompleteName="firstName"
            autocompleteIdElement="#${idIdElement}"
            autocompleteParentElement="#${rowIdElement}"
            title="${firstnameTitle}"
            />
            </div>
            <#if includeRole || includeRights>
                <#if includeRole>
                    <@s.select  name="${strutsPrefix}.role" id="metadataForm_authorshipProxies_${_indexNumber?c}__userrole"  autocomplete="off" listValue='label' list=relevantPersonRoles
                    cssClass="creator-role-select col-2" />
                <#else>
                    <@s.select  cssClass="creator-rights-select col-2" name="${strutsPrefix}.generalPermission" emptyOption='false'
                    listValue='label' list='%{availablePermissions}' disabled=isDisabled />
                <#--HACK: disabled fields do not get sent in request, so we copy generalPermission via hidden field and prevent it from being cloned -->
                    <@s.hidden name="${strutsPrefix}.generalPermission" id="hdn${strutsPrefix}_generalPermission" cssClass="repeat-row-remove" />
                </#if>
            <#else>
                <span class="col-2">&nbsp;</span>
            </#if>
        </div>
        <div class="form-row">
            <div class="col-5">
            <@s.textfield  cssClass=" mr-2 ${lookupType} trim" placeholder="Email (optional)" readonly=isDisabled autocomplete="off"
            autocompleteIdElement="#${idIdElement}" autocompleteName="email" autocompleteParentElement="#${rowIdElement}"
            name="${strutsPrefix}${personPrefix}.email" maxlength="255"/>
            </div>
            <div class="col-7">
                <@s.textfield cssClass=" ${lookupType} trim" placeholder="Institution Name (Optional)" readonly=isDisabled autocomplete="off"
        autocompleteIdElement="#${idIdElement}"
        autocompleteName="institution"
        autocompleteParentElement="#${rowIdElement}"
        name="${strutsPrefix}${personPrefix}.institution.name" maxlength="255" />
            </div>

        </div>
    </div>
    </#macro>

<#-- emit the custom identifiers section of a resource edit page-->
    <#macro identifiers showInherited=true>
    <div id="divIdentifiersGlide" class="col-12">
        <@helptext.identifiers />
        <h2 id="identifierInfoSectionLabel"><@resourceTypeLabel /> Specific or Agency Identifiers<@helptext.info title="Resource Specific or Agency Identifiers" contentDiv="#divIdentifiersTip" /></h2>
        <@_inheritsection checkboxId="cbInheritingIdentifierInformation" name='resource.inheritingIdentifierInformation' showInherited=showInherited sectionId='#divIdentifiers' />
        <div id="divIdentifiers" class="repeatLastRow">
            <div class="form-group row">
                <label class="col-form-label col-2">Name / Value</label>

                <div class="col-10">
                    <div id="resourceAnnotationsTable" addAnother="add another identifier">
                        <#list resourceAnnotations as annotation>
                        	<@_displayAnnotation annotation annotation_index/>
							<#else>
                        	<@_displayAnnotation blankResourceAnnotation annotation_index/>
                        </#list>
                    </div>
                </div>
            </div>
        </div>
    </div>

    </#macro>
    <#macro _displayAnnotation annotation annotation_index=0>
    <div id="resourceAnnotationRow_${annotation_index}_" class="form-row repeat-row">
        <div class="col-4">
        <@s.textfield placeholder="Name"  maxlength=128 cssClass="annotationAutoComplete mr-2" name='resourceAnnotations[${annotation_index}].resourceAnnotationKey.key' value='${annotation.resourceAnnotationKey.key!""}'  autocomplete="off" />
        </div>
        <div class="col-7">
        <@s.textfield placeholder="Value" cssClass="" name='resourceAnnotations[${annotation_index}].value'  value='${annotation.value!""}' />
        </div>
        <@nav.clearDeleteButton id="resourceAnnotationRow" />
    </div>
    </#macro>

<#--
  emit the  joined string values of a collection, same as
    <#list mylist as item>${item}<#if item_has_next>,</#if></#list>
-->
    <#macro join sequence=[] delimiter=",">
        <#if sequence?has_content>
            <#list sequence as item>
            ${item}<#sep><#noescape>${delimiter}</#noescape></#sep><#t>
            </#list>
        </#if>
    </#macro>

<#--
FIXME: this appears to only be used for Datasets.  Most of it has been extracted out
to singleFileUpload, continue lifting useful logic here into singleFileUpload (e.g.,
jquery validation hooks?)
MARTIN: it's also used by the FAIMS Archive type on edit.
-->
<#-- emit file upload section for non-async uploads --> 
    <#macro upload uploadLabel="File" showMultiple=false divTitle="Upload File" showAccess=true>
        <@_sharedUploadFile>
            <@_singleFileUpload>
            <div class="field indentFull singleFileUpload">
                <@s.select name="fileProxies[0].restriction" id="cbConfidential" labelposition="right" label="This item has access restrictions" listValue="label" list=fileAccessRestrictions />
                <div><b>NOTE:</b> by changing this from 'public', all of the metadata will be visible to users, they will not be able to view or download this
                    file.
                    You may explicity grant read access to users below.
                </div>
                <br/>
                <#local val = ""/>
                <#if (fileProxies[0].fileCreatedDate)?has_content>
                    <#local val = fileProxies[0].fileCreatedDate?string["MM/dd/yyyy"] />
                </#if>
                Date             
                        <div class="input-append">
   						  <@s.textfield name="fileProxies[0].fileCreatedDate" cssClass="datepicker input-small" placeholder="mm/dd/yyyy" value="${val}" dynamicAttributes={"data-date-format":"mm/dd/yyyy"} />
                          <span class="input-group-text"><i class="far fa-calendar-alt"></i></span>
                        </div>
                Description      <@s.textarea cssClass="input-block-level resizable resize-vertical" name="fileProxies[0].description" rows="3" placeholder="Enter a description here" cols="80" />

            </div>
            </@_singleFileUpload>
        </@_sharedUploadFile>
    </#macro>
    <#macro _sharedUploadFile divTitle="Upload">
    <div class="col-12" id="uploadSection">
        <h2>${divTitle}</h2>

        <div class='fileupload-content'>
            <#nested />
            <#if multipleFileUploadEnabled || resource.hasFiles()>
                <h4>Current ${multipleFileUploadEnabled?string("and Pending Files", "File")}</h4>

                <div class="">
                    <p><span class="label">Note:</span> You can only have <strong><#if !multipleFileUploadEnabled>1 file<#else>${maxUploadFilesPerRecord}
                        files</#if> </strong> per record</p>
                </div>
            </#if>
        </div>
        <@helptext.confidentialFile />
    </div>
    </#macro> 
    <#macro _singleFileUpload typeLabel="${resource.resourceType.label}">
        <#if !ableToUploadFiles>
            <#if ableToAdjustPermissions?? && !ableToAdjustPermissions>
                <b>note:</b> you have not been granted permission to upload or modify files<br/>
            <#else>
                <b>note:</b> your billing acccount is overdrawn. You must add funds to your account before you can add or replace a file.<br/>
            </#if>
        <#else>
        <div class="form-group"
             data-tooltip="The metadata entered on this form will be associated with this file. We accept the following formats:
                        <@join sequence=validFileExtensions delimiter=", "/>">
            <label for="fileUploadField" class="col-form-label">${typeLabel}</label>

            <div class="controls">
                <@s.file theme="simple" name='uploadedFiles' cssClass="validateFileType col-12" id="fileUploadField" labelposition='left' size='40' />
                <span class="help-block">Valid file types include: <@join sequence=validFileExtensions delimiter=", "/></span>
            </div>
            <#nested>
        </div>
        </#if>
    </#macro>

<#-- emit the file upload section for async uploads
    @param showMultiple:boolean (not used?)
    @param divTitle:string (not used?)
    @param divId:string prefix for element ids used in this section
    @param inputFileCss:string  string to inject in the actual file input[class] attribute
-->
    <#macro asyncFileUpload uploadLabel="Attach Files" showMultiple=false divTitle="Upload" divId="divFileUpload" inputFileCss="" >
        <@helptext.asyncUpload divId=divId validFileExtensions=validFileExtensions multipleFileUploadEnabled=multipleFileUploadEnabled
        maxUploadFilesPerRecord=maxUploadFilesPerRecord canReplace=(fileProxies?size > 0) siteAcronym=siteAcronym />


    <div id="${divId}" class="col-12" >
        <@s.hidden name="ticketId" id="ticketId" />
        <h2>${uploadLabel} <@helptext.info title="${uploadLabel}" contentDiv="#${divId}Help" /></h2>

        <div id="fileuploadErrors" class="fileupload-error-container" style="display:none">
            <div class="alert alert-block">
                <h4>We found the folllowing problems with these uploads</h4>
                <ul class="error-list"></ul>
            </div>
        </div>

        <#if !ableToUploadFiles &&  ableToAdjustPermissions?? && !ableToAdjustPermissions>
                <b>note:</b> you have not been granted permission to upload or modify files<br/>
        <#else>
            <#if !ableToUploadFiles>
                <b>note:</b> your billing acccount is overdrawn. You must add funds to your account before you can add or replace a file.<br/>
            </#if>

                <#assign uploadConfigId="uploadConfig"/>
                <script id="uploadConfig" type="application/json">
                <#noescape>
                ${fileUploadSettings!''}
                </#noescape>
                </script>

			<#include "../../content/resource/vue-file-upload-template.html"/>
        </#if>
    </div>
    </#macro>

<#-- emit the right-sidebar section.  Note this gets parsed by sitemesh, so more content will go inside.
    @requires resource:Resource
-->
    <#macro sidebar>
    <div id="sidebar-right" parse="true">
    <div class="row">
    <div class="col-10 offset-2">
        <#nested />
    </div>
    </div>
    </div>
    </#macro>

<#-- emit a 'datatable' with a list of resources
    @requires allSumbittedProjects:list<Project> list of projects created by user (goes in separate select optgroup)
    @requires fullUserProjects:list<Project> list of other editable projects (goes in separate select optgroup)
    @param showDescription:boolean (unused)
    @param selectable:boolean render resources in the list with "selectable" rows,  which will render a checkbox
        as the first column in each row of the data table
-->
<#macro resourceDataTable showDescription=true selectable=false clickable=false limitToCollection=false idAddition="" span="col-8" useUnmanagedCollections=false>
	<div class="card tdar-widget div-search-filter bg-light mb-2" id="divSearchFilters${idAddition}"> 
		<div class="card-body">
                <div class="row " >
                    <div class="col-12" >
                    	<div class="input-group">
                        <@s.textfield  name="_tdar.query" id="query${idAddition}" cssClass='col-8'
                            placeholder="Enter a full or partial title to filter results" />
                            <div class="input-append">
                                <button type="button" class="btn btn-outline-secondary" id="btnToggleFilters${idAddition}" data-toggle="collapse" data-target="#divAdvancedFilters${idAddition}">
                                    More options
                                </button>
                            </div>
                        </div>
                        <div class="float-right">
                            <#if limitToCollection>
                                <label class="checkbox hidden" style="font-weight:normal; ">
                                    <input type="checkbox" name='_tdar.parentCollectionsIncluded' id="parentCollectionsIncluded${idAddition}">
                                    Show only selected resources
                                </label>
                            </#if>
                        </div>
                    </div>
                </div>

                <div id="divAdvancedFilters${idAddition}" class="collapse mt-2">
                    <div class="row">
                        <div class="col">
                            <label class="col-form-label" for="project-selector${idAddition}">Project</label>
                            
                            <select id="project-selector${idAddition}" name="_tdar.project" class="form-control">
                                <option value="" selected='selected'>All Editable Projects</option>
                                <#if allSubmittedProjects?? && !allSubmittedProjects.empty>
                                    <optgroup label="Projects">
                                        <#list allSubmittedProjects?sort_by("title") as submittedProject>
                                            <option value="${submittedProject.id?c}"
                                                    title="${submittedProject.title!""?html}"><@common.truncate submittedProject.title 70 /> </option>
                                        </#list>
                                    </optgroup>
                                </#if>
        
                                <#if fullUserProjects??>
                                    <optgroup label="Projects you have been given access to">
                                        <#list fullUserProjects?sort_by("title") as editableProject>
                                            <option value="${editableProject.id?c}"
                                                title="${editableProject.title!""?html}"><@common.truncate editableProject.title 70 /></option>
                                        </#list>
                                    </optgroup>
                                </#if>
                            </select>
                        </div>
        
                        <div class="col">
                            <label class="col-form-label" for="collection-selector${idAddition}">Collection</label>
                            <#local selectedId=-1/>
                   
                            
                            <div class="">
                                <select name="_tdar.collection" id="collection-selector${idAddition}" class="form-control">
                                    <option value="" <#if (selectedId!-1) == -1>selected='selected'</#if>>All Collections</option>
                                    <@s.iterator value='allResourceCollections' var='rc'>
                                        <option value="${rc.id?c}" title="${rc.name!""?html}"
                                        <#if (selectedId!-1) != -1 && rc.id == selectedId>selected="selected"</#if>
                                        ><@common.truncate rc.name!"(No Name)" 70 /></option>
                                    </@s.iterator>
                                </select>
                            </div>
                        </div>
                    </div>
        
                    <div class="row">
                        <div class="col">
                            <label class="col-form-label">Status</label>
                            <@s.select  id="statuses${idAddition}" headerKey="" headerValue="Any" name='_tdar.status'  emptyOption='false' listValue='label'
                            list='%{statuses}' cssClass="form-control"/>
                        </div>
        
                        <div class="col">
                            <label class="col-form-label">Resource Type</label>
                            <@s.select  id="resourceTypes${idAddition}" name='_tdar.resourceType'  headerKey="" headerValue="All" emptyOption='false'
                            listValue='label' list='%{resourceTypes}' cssClass="form-control"/>
                        </div>
                    </div> 
        
                    <div class="row">
                        <div class="col">
                            <label class="col-form-label">Sort by</label>
        
                            <div class="">
                                <@s.select emptyOption='false' id="sortBy${idAddition}" name='_tdar.sortBy' listValue='label' list='%{resourceDatatableSortOptions}' cssClass="selSortBy"
                                value="ID_REVERSE" cssClass="form-control"/>
                            </div>
                        </div>
                        <div class="col">
                        </div>
                    </div>
                </div>
		</div>
    </div>

        <#--The HTML table for resources. -->
        <div class="row">
            <div class="${span}">
            <table class="table table-striped " id="resource_datatable${idAddition}" style="width:100%">
                    <colgroup>
                        <#if selectable || clickable>
                            <col style="" />
                        </#if>
                        
                        <col style="width: 60%" />
                        <col style="" />
                        <#if clickable>

                            <#--Renders an additional column for the Manage/Unmanged status if necessary  -->
                            <#if useUnmanagedCollections>
                                <col style="" />
                            </#if>
                            
                            <#--Renders a column for the buttons to be displayed -->
                            <col style="" />
                        </#if>
                        
                    </colgroup>
                      <thead class="thead-dark">

                        <tr>
                            <#if selectable || clickable>
                                <th>id</th>
                            </#if>
                                <th>Title</th>
                                <th>Type</th>
                                
                            <#if clickable>
                                <#if useUnmanagedCollections>
                                    <th>Status</th>
                                </#if>
                                    <th>Add</th>
                            </#if>
                        </tr>
                    </thead>
        
                    <tbody>
                        <tr>
                            <#if selectable || clickable>
                                <td>&nbsp;</td>
                            </#if>
                            <td>&nbsp;</td>
                            <td>&nbsp;</td>
                            <#if clickable>
                                <#if useUnmanagedCollections>
                                    <td>&nbsp;</td>
                                </#if>
                                    <td>&nbsp;</td>
                            </#if>
                        </tr>
                    </tbody>
                </table>
                            
            </div>
        </div>
    <#nested />
    <br/>
</#macro> 
<#--End Datatable macro-->


<#-- emit $.ready javascript snippet that registers is responsible for wiring up a table element as a datatable widget -->
    <#macro resourceDataTableJavascript showDescription=true selectable=false >
    <script type="text/javascript">
        $(function () {
            TDAR.datatable.setupDashboardDataTable({
                isAdministrator: ${(editor!false)?string},
                isSelectable: ${selectable?string},
                showDescription: ${showDescription?string}
            });
        });
            <#nested>
    </script>
    </#macro>


<#-- emit the copyright holders section -->
    <#macro copyrightHolders sectionTitle copyrightHolderProxies >
        <#if config.copyrightMandatory || resource.copyrightHolder?has_content>
            <@helptext.copyrightHoldersTip />
        <div class="" id="copyrightHoldersSection" >
            <h2>${sectionTitle} <@helptext.info title="Primary Copyright Holder" contentDiv="#divCopyrightHoldersTip" /></h2>

            <div id="copyrightHolderTable" class="form-group table creatorProxyTable">
                <@creatorProxyRow proxy=copyrightHolderProxies proxy_index="" prefix="copyrightHolder" required=true
                includeRole=false required=true leadTitle="copyright holder " showDeleteButton=false/>
            </div>
        </div>
        </#if>
    </#macro>

<#-- emit the license section -->
    <#macro license>
        <#assign currentLicenseType = defaultLicenseType />
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
                            <a href="${licenseCursor.URI}" target="_blank">
                                <img alt="${licenseCursor.licenseName}" title="${licenseCursor.licenseName}"
                                  src="<#if secure>${licenseCursor.secureImageURI}<#else>${licenseCursor.imageURI}</#if>"/></a>
                        </#if>
                    </td>
                    <td>
                        <h4>${licenseCursor.licenseName}</h4>

                        <p>${licenseCursor.descriptionText}</p>
                        <#if (licenseCursor.URI != "")>
                            <p><a href="${licenseCursor.URI}" target="_blank">view details</a></p>
                        <#else>
                            <p><label style="position: static" for="licenseText">License text:</label></p>

                            <p><@s.textarea id="licenseText" name='resource.licenseText' rows="4" cols="60" /></p>
                        </#if>
                    </td>
                </tr>
            </#list>
        </table>
    </div>
    </#macro>

 <#macro _fileuploadButton id="fileuploadButton" name="" label="" cssClass="" buttonCssClass="">
    <span class="btn fileinput-button ${buttonCssClass}" id="${id}Wrapper" style="width:6em;text-align:left">
    <i class="icon-refresh icon-white"> </i>
    <span>${label}</span>
    <input type="file" name="uploadFile" id="${id}" class="${cssClass}">
</span>
    </#macro>

<#--emit the sub-navmenu of a resource edit page -->
    <#macro subNavMenu>
        <#local supporting = resource.resourceType.supporting >
    <nav id='subnavbar'  class="bg-light d-sm-none d-md-block" >
    <div class=" col-12">
        <p>Jump to Section:</p>
    <ul class="list-unstyled">
    <li><a class="nav-link" href="#basicInformationSection">Basic Information</a></li>
                    <#if persistable.resourceType?has_content && persistable.resourceType != 'PROJECT' >
                        <li><a class="nav-link" href="#authorshipSection">Authors</a></li>
                    </#if>
                    <#if persistable.resourceType?has_content && persistable.resourceType != 'PROJECT'  && (!supporting)>
                        <li><a class="nav-link"href="#divFileUpload">Files</a></#if></li>
                    <#nested />
                    <#if persistable.resourceType?has_content && persistable.resourceType != 'PROJECT' >
                        <li><a class="nav-link"href="#organizeSection">Project</a></li>
                    </#if>
                    <#if !supporting>
                        <li><a class="nav-link"href="#spatialSection">Spatial Coverage</a></li>
                        <li><a class="nav-link"href="#temporalSection">Temporal Coverage</a></li>
                        <li><a class="nav-link"href="#investigationSection">What</a></li>
                        <li><a class="nav-link"href="#siteSection">Site Information</a></li>
                    </#if>
                    <li><a class="nav-link"href="#resourceNoteSectionGlide">Notes</a></li>
                    <div class="button btn btn-primary submitButton" id="fakeSubmitButton">Save</div>
                    <img alt="progress indicator" title="progress indicator"  src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="display:none"/>
		</div>
    </nav>
    
    </#macro>

    <#-- emit a repeatrow table of @registeredUserRow controls -->
    <#macro listMemberUsers includerights=true>
        <#local _authorizedUsers=authorizedMembers />
        <#if !_authorizedUsers?has_content><#local _authorizedUsers=[blankPerson]></#if>

    <div id="accessRightsRecords" class="repeatLastRow" data-addAnother="add another user">
        <div class="form-group">
            <label class="col-form-label">Users</label>

            <div class="controls">
                <#list _authorizedUsers as user>
                    <#if user??>
                        <div class="form-row repeat-row" id="userrow_${user_index}_">
                            <div class="col-6">
                                <@registeredUserRow person=user _indexNumber=user_index includeRepeatRow=false includeRights=includerights />
                            </div>
                            <@nav.clearDeleteButton id="user${user_index}"  />
                        <div class="col-5"></div>
                        </div>
                    </#if>
                </#list>
            </div>
        </div>
    </div>

    </#macro>

<#--Search valuestack for the value of the specified field name.  Starting with the controller fields, then request parameters -->
    <#function requestValue name default="">
    <#-- look in the request parameters -->
        <#local parameterVal = (stack.context.parameters[name][0])!default>

    <#-- look in list of values set by struts on the action -->
        <#local val = (stack.findValue(name))!>

    <#-- prefer the model value over the request parameter -->
        <#if val?has_content>
            <#return val>
        <#else>
            <#return parameterVal>
        </#if>

    </#function>

<#-- emit one "row" of a registered user table.  Each row contains an text input field that will be initialized
    with the jquery-ui autocomplete plugin
-->
    <#macro registeredUserRow person=person _indexNumber=0 isDisabled=false prefix="authorizedMembers" required=false _personPrefix=""
    includeRepeatRow=false includeRights=false  hidden=false leadTitle="" textfieldCssClass="">
    <#-- //fixme: this macro composes too many disparate concerns (Person vs. AuthorizedUser, two very different things) and should be refactored into two (or more) smaller macros -->
        <#local disabled =  isDisabled?string("disabled", "") />
        <#local readonly = isDisabled?string("readonly", "") />
        <#local lookupType="userAutoComplete notValidIfIdEmpty"/>
        <#local _index=""/>
        <#if _indexNumber?string!=''><#local _index="[${_indexNumber?c}]" /></#if>
        <#local personPrefix="" />
        <#if _personPrefix!=""><#local personPrefix=".${_personPrefix}"></#if>
        <#local strutsPrefix="${prefix}${_index}" />
        <#local rowIdElement="${prefix?replace('.','_')}Row_${_indexNumber}_p" />
        <#local idIdElement="${prefix?replace('.','_')}Id__id_${_indexNumber}_p" />
        <#local idIdElement=idIdElement?replace(".","_") /> <#-- strip dots to make css selectors easier to write  -->
        <#local requiredClass><#if required>required</#if></#local>
        <#local nameTitle>A ${leadTitle} name<#if required> is required</#if></#local>
        <#local _val = requestValue("${strutsPrefix}${personPrefix}.name")>

        <#local properNameField>${prefix}.properName</#local>
        <#if _index != ''>
            <#local properNameField>authorizedUsersFullNames${_index}</#local>
        <#elseif prefix == 'submitter'>
            <#local properNameField>submitterProperName</#local>
        </#if>
<#if includeRights>
    <div id='${rowIdElement}' class="creatorPerson <#if hidden>hidden</#if> <#if includeRepeatRow>repeat-row</#if>">
</#if>
                <@s.hidden name='${strutsPrefix}${personPrefix}.id' value='${(person.id!-1)?c}' id="${idIdElement}"  cssClass="" onchange="this.valid()"  autocompleteParentElement="#${rowIdElement}"   />
            
            <@s.textfield  cssClass="${lookupType} ${requiredClass} ${textfieldCssClass!'col-10'}" placeholder="Name"  readonly=isDisabled autocomplete="off"
            name="${properNameField}" maxlength="255" autocompleteName="properName"
            autocompleteIdElement="#${idIdElement}"
            autocompleteParentElement="#${rowIdElement}"

            dynamicAttributes={"data-msg-notValidIfIdEmpty":"Invalid user name.  Please type a name (or partial name) and choose one of the options from the menu that appears below."}

            />

            <#if test>
                <!-- ${(person.id!-1)?c}::${(person.properName)!}::${"(${strutsPrefix}.generalPermission)!'n/a'"?eval} -->
            </#if>

            <#if includeRights>
                <@s.select cssClass="creator-rights-select col-2" name="${strutsPrefix}.generalPermission" emptyOption='false'
                listValue='label' list='%{availablePermissions}' disabled=isDisabled />
            <#--HACK: disabled fields do not get sent in request, so we copy generalPermission via hidden field and prevent it from being cloned -->
                <@s.hidden id="${strutsPrefix}hdnGeneralPermission" name="${strutsPrefix}.generalPermission" cssClass="repeat-row-remove" />
            </#if>
<#if includeRights>
    </div>
</#if>
    </#macro>


<#-- emit a single row of a table that contains institution autocomplete fields -->.
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

    <div id='${rowIdElement}' class="creatorInstitution mb-2 <#if hidden >hidden</#if>">

        <@s.hidden name='${strutsPrefix}${institutionPrefix}.id' value='${(institution.id!-1)?c}' id="${idIdElement}"  cssClass="" onchange="this.valid()"  autocompleteParentElement="#${rowIdElement}"  />
        <div class="form-row">
            <div class="col-10">

            <@s.textfield cssClass="institutionAutoComplete institution mr-3 ${requiredClass} trim" placeholder="Institution Name" autocomplete="off"
            autocompleteIdElement="#${idIdElement}" autocompleteName="name"
            autocompleteParentElement="#${rowIdElement}"
            name="${strutsPrefix}${institutionPrefix}.name" maxlength="255"
            title="${institutionTitle}"
            />
                        </div>

            <#if includeRole>
                <@s.select  name="${strutsPrefix}.role" id="metadataForm_authorshipProxies_${_indexNumber?c}__institutionrole" listValue='label' list=relevantInstitutionRoles cssClass="creator-role-select col-2" />
            <#else>
            <#-- is includeRole ever false?  if not we should ditch the parm entirely, perhaps the entire macro. -->
                <div class="col-2">&nbsp;</div>
            </#if>
        </div>
    </div>
    </#macro>

<#-- emit a text input field intended for use as a date-entry control-->
    <#macro datefield date name="" id=name cssClass="" label="" format="MM/dd/yyyy" placeholder="mm/dd/yyyy" >
        <#local val = "">
        <#if date?has_content>
            <#local val = date?string[format]>
        </#if>
        <@s.textfield name="${name}" id="${id}" cssClass="${cssClass}" label="${label}" placeholder="${placeholder}" value="${val}" dynamicAttributes={"data-date-format":"${placeholder}"}/>
    </#macro>

    <#macro hiddenStartTime value=.now?long>
        <@s.hidden name="startTime" value="${value?c}" />
    </#macro>

    <#macro shareSection formAction>
    <form class="form-horizontal" method="POST" action="${formAction}">
        <div class="well">
            <div class="row">
                <div class="col-4">
                    <@s.textfield name="adhocShare.email" id="txtShareEmail" label="Email" labelPosistion="left" />
                </div>

                <div class="col-4">
                    <@s.select name="adhocShare.generalPermissions" label="Permission" labelposition="left" listValue='label' list="%{availablePermissions}" />
                </div>
            </div>
            <div class="row">
                <div class="col-5">
                    <div class="form-group">
                        <label class="col-form-label" for="inputPassword">Until:</label>
                        <div class="controls">
                            <div class="input-append">
                                <input class="col-2 datepicker" size="16" type="text" value="12-02-2016" id="dp3" data-date-format="mm/dd/yyyy" >
                                <span class="add-on"><i class="far fa-calendar-alt"></i></span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-3">
                    <input type="submit" class="btn tdar-button btn-primary" value="Submit">
                </div>

            </div>
        </div>
    </form>

    </#macro>


</#escape>