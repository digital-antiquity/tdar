<#-- 
$Id$ 
Edit freemarker macros.  Getting large, should consider splitting this file up.
-->
<#-- include navigation menu in edit and view macros -->
<#escape _untrusted as _untrusted?html>
<#include "common.ftl">
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
        <div id="spanStatusToolTip" class="hidden">
            <h2>Status</h2>
            <div>
                Indicates the stage of a resource's lifecycle and how ${siteAcronym} treats its content.
                <dl>
                    <dt>Draft</dt><dd>The resource is under construction and/or incomplete</dd>
                    <dt>Active</dt><dd>The resource is considered to be complete.</dd>
                    <dt>Flagged</dt><dd>This resource has been flagged for deletion or requires attention</dd>
                    <dt>Deleted</dt><dd>The item has been 'deleted' from ${siteAcronym} workspaces and search results, and is considered deprecated.</dd>  
                </dl>
                
            </div>
        </div>
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
        
        <div id="divSelectAllInheritanceTooltipContent" style="display:none"> 
        Projects in ${siteAcronym} can contain a variety of different information resources and used to organize a set of related information resources such as documents, datasets, coding sheets, and images. A project's child resources can either inherit or override the metadata entered at this project level. For instance, if you enter the keywords "southwest" and "pueblo" on a project, resources associated with this project that choose to inherit those keywords will also be discovered by searches for the keywords "southwest" and "pueblo". Child resources that override those keywords would not be associated with those keywords (only as long as the overriden keywords are different of course). 
        </div>

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
    <div style="display:none" id="divResourceCollectionListTips">
        <p>
            Specify the names of the collections that ${siteAcronym} should add this resource to.  Alternately you can start a new, <em>public</em>  collection 
            by typing the desired name and selecting the last option in the list of pop-up results.  The newly-created collection will contain only this 
            resource, but can be modified at any time. 
        </p>
    </div>

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
        
        <h4>Geographic Region</h4>
        <div id='editmapv3' class="tdar-map-large googlemap"
            tiplabel="Geographic Coordinates"
            tooltipcontent="Identify the approximate region of this resource by clicking on &quot;Select Region&quot; and drawing a bounding box on the map.
                <br/>Note: to protect site security, ${siteAcronym} obfuscates all bounding boxes, bounding boxes smaller than 1 mile, especially.  This 'edit' view 
                will always show the exact coordinates."
            ></div>
        <div id="divManualCoordinateEntry" tooltipcontent="#divManualCoordinateEntryTip">
        <br />
            
            <@s.checkbox id="viewCoordinatesCheckbox" name="viewCoordinatesCheckbox" onclick="$('#explicitCoordinatesDiv').toggle(this.checked);" label='Enter / View Coordinates' labelposition='right'  />
            
            <script type="text/javascript">
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
            <div id="divManualCoordinateEntryTip" class="hidden">
                <h2>Manually Enter Coordinates</h2>
                <div>
                    Click the Locate button after entering the longitude-latitude pairs in the respective input fields to draw a box on the map and zoom to it.
                    <br />Examples:
                    <ul>
                        <li>40&deg;44'55"N</li>
                        <li>53 08 50N</li>
                        <li>-73.9864</li>
                    </ul>
                    <p><aside><strong>Note:</strong> to protect site security, ${siteAcronym} obfuscates all bounding boxes, bounding boxes smaller than 1 mile.  This 'edit' view will 
                    always show the exact coordinates.</aside></p>
                                   
                 </div>
            </div>
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
            <table id="uploadFiles" class="files tableFormat">
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
            <tr class="noFiles width99percent newRow">
            <td><em>no files uploaded</em></td>
            </tr>
            </#if>
            </tbody>
            </table>
            </#if>
        </div>
      <div id="divConfidentialAccessReminder" class="hidden">
          <em>Embargoed records will become public in ${embargoPeriodInYears} years. Confidential records will not be made public. Use the &quot;Access Rights&quot; section to assign access to this file for specific users.</em>
      </div>
</div>
</#macro>

<#macro siteKeywords showInherited=true divTitle="Site Information">
<div class="well-alt" id="siteSection">
    <h2>${divTitle}</h2>
    <@inheritsection checkboxId='cbInheritingSiteInformation' name='resource.inheritingSiteInformation'  showInherited=showInherited />
    <div id="divSiteInformation">
        <div class="hidden" id="siteinfohelp">
            Keyword list: Enter site name(s) and select feature types (<a href="${siteTypesHelpUrl}">view complete list</a>) 
            discussed in the document. Use the Other field if needed.
        </div>
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
    <div class="hidden" id="materialtypehelp">
        Keyword list: Select the artifact types discussed in the document.<a href="${materialTypesHelpUrl}">view all material types</a>
    </div>
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
    <div id="culturehelp" class="hidden">
        Keyword list: Select the archaeological &quot;cultures&quot; discussed in the document. Use the Other field if needed. 
        <a href="${culturalTermsHelpUrl}">view all controlled terms</a>
    </div>
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

<div class="hidden" id="investigationtypehelp">Keyword list: Select the investigation types relevant to the document.<a href="${investigationTypesHelpUrl}">
view all investigation types</a></div>
</#macro>


<#-- provides a fieldset just for full user access -->
<#macro fullAccessRights tipsSelector="#divAccessRightsTips">
<#local _authorizedUsers=authorizedUsers />
<#if _authorizedUsers.empty><#local _authorizedUsers=[blankAuthorizedUser]></#if>
<div id="divAccessRightsTips" style="display:none">
<p>Determines who can edit a document or related metadata. Enter the first few letters of the person's last name. 
The form will check for matches in the ${siteAcronym} database and populate the related fields.</p>
<em>Types of Permissions</em>
<dl>
    <dt>View All</dt>
    <dd>User can view/download all file attachments.</dd>
    <dt>Modify Record<dt>
    <dd>User can edit this resource.<dd>
</dl>
</div>

<div id="divAccessRights" class="well-alt" tooltipcontent="${tipsSelector}">
<h2><a name="accessRights"></a>Access Rights</h2>
<h4>Users who can view or modify this resource</h4>

<div id="accessRightsRecords" class="repeatLastRow" data-addAnother="add another user">
    <div class="control-group">
        <label class="control-label">Users</label>
        <#list _authorizedUsers as authorizedUser>
            <#if authorizedUser??>
                <@authorizedUserRow authorizedUser authorizedUser_index />
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

<#macro authorizedUserRow authorizedUser authorizedUser_index=0>
<#local bDisabled = (authorizedUser.user.id == authenticatedUser.id) />
<#local disabled =  bDisabled?string />

    <div id='authorizedUserRow_${authorizedUser_index}_' class="repeat-row indent-row">

        <@s.hidden name='authorizedUsers[${authorizedUser_index}].user.id' value='${(authorizedUser.user.id!-1)?c}' id="authorizedUserId__id_${authorizedUser_index}_"  cssClass="validIdRequired" onchange="this.valid()"  autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"  />
    <#if bDisabled>
        <@s.hidden name="authorizedUsers[${authorizedUser_index}].generalPermission" value="${authorizedUser.generalPermission!'VIEW_ALL'}"/>
	</#if>        
        <div class="controls controls-row">
            <@s.textfield theme="tdar" cssClass="span2 userAutoComplete" placeholder="Last Name"  readonly="${disabled}" autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"
                autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" autocompleteName="lastName" autocomplete="off"
                name="authorizedUsers[${authorizedUser_index}].user.lastName" maxlength="255" /> 
            <@s.textfield theme="tdar" cssClass="span2 userAutoComplete" placeholder="First Name"  readonly="${disabled}" autocomplete="off"
                name="authorizedUsers[${authorizedUser_index}].user.firstName" maxlength="255" autocompleteName="firstName"
                autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" 
                autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"  />
            <@s.textfield theme="tdar" cssClass="span2 userAutoComplete" placeholder="Email (optional)" readonly="${disabled}" autocomplete="off"
                autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" autocompleteName="email" autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"
                name="authorizedUsers[${authorizedUser_index}].user.email" maxlength="255"/>
          
          <@clearDeleteButton id="authorizedUserRow" disabled="${disabled}" />
        </div>
  
   
        <div class="controls controls-row">
        <@s.textfield theme="tdar" cssClass="span3 userAutoComplete" placeholder="Institution Name (Optional)" readonly="${disabled}" autocomplete="off"
            autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" 
            autocompleteName="institution" 
            autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"
            name="authorizedUsers[${authorizedUser_index}].user.institution.name" maxlength="255" />
        <#if bDisabled>
        <@s.select theme="tdar" cssClass="span3" name="authorizedUsers[${authorizedUser_index}].generalPermission" 
            emptyOption='false' listValue='label' list='%{availablePermissions}' disabled=true
        />
        <#else>
        <@s.select theme="tdar" cssClass="span3" name="authorizedUsers[${authorizedUser_index}].generalPermission" 
            emptyOption='false' listValue='label' list='%{availablePermissions}'
        />
        </#if>
        </div>
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
    <div tiplabel="Upload your ${typeLabel}<#if multipleFileUploadEnabled>(s)</#if>" 
    tooltipcontent="The metadata entered on this form will be associated with this file. We accept ${typeLabel}s in the following formats: <@join sequence=validFileExtensions delimiter=", "/>"
    >
    <@s.file name='uploadedFiles' label='${typeLabel}' cssClass="validateFileType" id="fileUploadField" labelposition='left' size='40' />
    <div class="field indentFull">
    <i>Valid file types include: <@join sequence=validFileExtensions delimiter=", "/></i>
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

$(function(){
    'use strict';
    var formSelector = "${formSelector}";
    var includeInheritance = ${includeInheritance?string("true", "false")};
    var form = $(formSelector)[0];
    
    <#if includeAsync>
    //init fileupload
    var id = $('input[name=id]').val();
    var acceptFileTypes  = <@edit.acceptedFileTypesRegex />;
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
    
        <div style="display:none" id="divSourceCollectionHelpText">
            <p>
              The museum or archival accession that contains the
              artifacts, original photographs, or original notes that are described
              in this ${siteAcronym} record.
            </p>
        </div>
        <div style="display:none" id="divComparativeCollectionHelpText">
            <p>
            Museum or archival collections (e.g.,
            artifacts, photographs, notes, etc.) which are associated with (or
            complement) a source collection. For example, a researcher may have
            used a comparative collection in an analysis of the materials
            documented in this ${siteAcronym} record.
            </p>
        </div>
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
<div class="hidden" id="coverageDatesTip">
    Select the approriate type of date (Gregorian calendar date or radiocarbon date). To enter a date range, enter the <em>earliest date</em> in the <em>Start Year field</em> 
    and the latest date in the End Year Field. <em>Dates containing "AD" or "BC" are not valid</em>. Use positive numbers for AD dates (500, 1200), and use negative numbers for BC dates (-500, -1200). Examples: 
    <ul>
        <li>Calendar dates: 300 start, 500 end (number only, smaller value first)</li>
        <li>Radiocarbon dates: 500 start, 300 end (number only, larger value first)</li>     
    </ul>
</div>
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


<#macro creatorProxyRow proxy=proxy prefix=prefix proxy_index=proxy_index type_override="NONE">
    <#assign relevantPersonRoles=personAuthorshipRoles />
    <#assign relevantInstitutionRoles=institutionAuthorshipRoles />
    <#if prefix=='credit'>
        <#assign relevantPersonRoles=personCreditRoles />
        <#assign relevantInstitutionRoles=institutionCreditRoles />
    </#if>

    <#if proxy??>
    <tr id="${prefix}Row_${proxy_index}_" class="repeat-row">
          <#assign creatorType = proxy.actualCreatorType!"PERSON" />
         <td><div class="btn-group creator-toggle-button" data-toggle="buttons-radio">
	         <button type="button" class="btn btn-small personButton <#if type_override == "PERSON" || (creatorType=='PERSON' && type_override=='NONE') >btn-active active</#if>" data-toggle="button">Person</button>
	         <button type="button" class="btn btn-small institutionButton <#if creatorType =='INSTITUTION' || type_override == "INSTITUTION">btn-active active</#if>" data-toggle="button">Institution</button>
		</div>
</td>
        <td>
        <span class="creatorPerson <#if creatorType =='INSTITUTION' || type_override == "INSTITUTION">hidden</#if>"  id="${prefix}Row_${proxy_index}_p">
            <@s.hidden name="${prefix}Proxies[${proxy_index}].person.id" id="${prefix}person_id${proxy_index}" onchange="this.valid()"  autocompleteParentElement="#${prefix}Row_${proxy_index}_p"  />
            <div class="control-group">
                <div class="controls controls-row">
                    <@s.textfield theme="tdar" cssClass="nameAutoComplete span2" placeholder="Last Name" placeholder="Last Name" autocomplete="off"
                        autocompleteName="lastName" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                        name="${prefix}Proxies[${proxy_index}].person.lastName" maxlength="255" /> 
                    <@s.textfield theme="tdar" cssClass="nameAutoComplete span2" placeholder="First Name" placeholder="First Name" autocomplete="off"
                        autocompleteName="firstName" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                        name="${prefix}Proxies[${proxy_index}].person.firstName" maxlength="255" />
	                <@s.select theme="tdar" name="${prefix}Proxies[${proxy_index}].role"  autocomplete="off"
	                    listValue='label'
	                    list=relevantPersonRoles  
	                    cssClass="creator-role-select span3"
	                    />
                </div>
                <div class="controls controls-row">
                <@s.textfield theme="tdar" cssClass="nameAutoComplete span4" placeholder="Institution Name (Optional)" placeholder="Institution Name (Optional)" autocomplete="off"
                     autocompleteName="institution" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                    name="${prefix}Proxies[${proxy_index}].person.institution.name" maxlength="255" />
                    <@s.textfield theme="tdar" cssClass="nameAutoComplete span3" placeholder="Email (Optional)" placeholder="Email (Optional)" autocomplete="off"
                         autocompleteName="email" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                        name="${prefix}Proxies[${proxy_index}].person.email" maxlength="255"/>
                </div>
            </div>
        </span>
        
            <span class="creatorInstitution <#if type_override == "PERSON" || (creatorType=='PERSON' && type_override=='NONE') >hidden</#if>" id="${prefix}Row_${proxy_index}_i">
            <@s.hidden name="${prefix}Proxies[${proxy_index}].institution.id" id="${prefix}institution_id${proxy_index}"/>
            <div class="control-group">
                <div class="controls controls-row">
                    <@s.textfield theme="tdar" cssClass="institutionAutoComplete institution span4" placeholder="Institution Name" placeholder="Institution Name" autocomplete="off"
                        autocompleteName="name" autocompleteIdElement="#${prefix}institution_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_i"
                        name="${prefix}Proxies[${proxy_index}].institution.name" maxlength="255" />
                    <@s.select theme="tdar" name="${prefix}Proxies[${proxy_index}].role" 
                        listValue='label'
                        list=relevantInstitutionRoles
                        cssClass="creator-role-select span3"
                         />
                </div>
            </div>
            </span>
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
        <div id="divIdentifiersTip" class="hidden">
            <div>
                <dl>
                    <dt>Name</<dt>
                    <dd>Description of the following agency or ${resource.resourceType.label} identifier (e.g. <code>ASU Accession Number</code> or <code>TNF Project Code</code>).</dd>
                    <dt>Value</<dt>
                    <dd>Number, code, or other identifier (e.g. <code>2011.045.335</code> or <code>AZ-123-45-10</code>).</dd>
                </dl> 
            </div>
        </div>
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
<#macro join sequence delimiter=",">
  <#if sequence??>
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

});
 
var $dataTable = null; //define at page-level, set after onload

$(function(){
    var isAdministrator = ${administrator?string};
    var isSelectable = ${selectable?string};
    jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages =3;
	$.extend( $.fn.dataTableExt.oStdClasses, {
	    "sWrapper": "dataTables_wrapper form-inline"
	} );
//        sDom:'<"datatabletop"ilrp>t<>', //omit the search box
	    
    $dataTable = registerLookupDataTable({
        tableSelector: '#resource_datatable',
        sAjaxSource:'/lookup/resource',
        "bLengthChange": true,
        "bFilter": false,
        aoColumns: [
          <#if selectable>{ "mDataProp": "id", tdarSortOption: "ID", sWidth:'5em' ,"bSortable":false},</#if>
          { "mDataProp": "title",  sWidth: '65%', fnRender: fnRenderTitle, bUseRendered:false ,"bSortable":false},
          { "mDataProp": "resourceTypeLabel",  sWidth: '15%',"bSortable":false }
        ],
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
                     useSubmitterContext: !isAdministrator
            }
        },
        selectableRows: isSelectable,
        rowSelectionCallback: function(id, obj, isAdded){
            if(isAdded) {
                rowSelected(obj);
            } else {
                rowUnselected(obj);
            }
        }
    });

});
 
$(document).ready(function() {
    
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

function fnRenderTitle(oObj) {
    //in spite of name, aData is an object containing the resource record for this row
    var objResource = oObj.aData;
    var html = '<a href="'  + getURI(objResource.urlNamespace + '/' + objResource.id) + '" class=\'title\'>' + htmlEncode(objResource.title) + '</a>';
    html += ' (ID: ' + objResource.id 
    if (objResource.status != 'ACTIVE') {
    html += " " + objResource.status;
    }
    html += ')';
    <#if showDescription>
    html += '<br /> <p>' + htmlEncode(objResource.description) + '</p>';
    </#if> 
    return html;
}

function drawToolbar(projId) {
    var toolbar = $("#proj-toolbar");
    toolbar.empty();
    if (projId != undefined && projId != '') {
        toolbar.append(projToolbarItem('/project/' + projId + '/view', '/images/zoom.png', ' View selected project'));
        toolbar.append(projToolbarItem('/project/' + projId + '/edit', '/images/pencil.png', ' Edit project'));
        toolbar.append(projToolbarItem('/resource/add?projectId=' + projId, '/images/database_add.png', ' Add new resource to project'));
    }
}

</script>
</#macro>


<#macro checkedif arg1 arg2><#t>
<@valif "checked='checked'" arg1 arg2 />
</#macro>

<#macro selectedif arg1 arg2>
<@valif "selected='selected'" arg1 arg2 />
</#macro>

<#macro valif val arg1 arg2><#t>
<#if arg1=arg2>${val}</#if><#t>
</#macro>

<#macro boolfield name label id  value labelPosition="left" type="checkbox" labelTrue="Yes" labelFalse="No" cssClass="">
    <@boolfieldCheckbox name label id  value labelPosition cssClass />
</#macro>

<#macro boolfieldCheckbox name label id value labelPosition cssClass>
<#if value?? && value?string == 'true'>
    <@s.checkbox name="${name}" label="${label}" labelPosition="${labelPosition}" id="${id}"  value=value cssClass="${cssClass}" 
        checked="checked"/>
<#else>
    <@s.checkbox name="${name}" label="${label}" labelPosition="${labelPosition}" id="${id}"  value=value cssClass="${cssClass}" />
</#if>
</#macro>

<#macro boolfieldRadio name label id value labelPosition labelTrue labelFalse>
    <label>${label}</label>
    <input type="radio" name="${name}" id="${id}-true" value="true"  <@checkedif true value />  />
    <label for="${id}-true" class="datatable-cell-unstyled"> ${labelTrue}</label>
    <#if (labelPosition=="top")><br />
    <input type="radio" name="${name}" id="${id}-false" value="false" <@checkedif false value />   />
    <label for="${id}-false" class="datatable-cell-unstyled"> ${labelFalse}</label>
    <#else>
    <input type="radio" name="${name}" id="${id}-false" value="false"   />
    <label for="${id}-false" class="datatable-cell-unstyled"> ${labelFalse}</label>
    </#if>
</#macro>

<#macro boolfieldSelect name label id value labelPosition labelTrue labelFalse>
    <label>${label}</label>
    <select id="${id}" name="${name}">
    <#if (labelPosition=="top")><br /></#if>
        <option id="${id}-true" value="true" <@selectedif true value/> />${labelTrue}</option>
        <option id="${id}-false" value="false" <@selectedif false value/> />${labelFalse}</option>
    </select>
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


<#macro copyrightHolders sectionTitle copyrightHolderProxy inline=false showInherited=false>
<#if copyrightMandatory>
    <#-- 
    FIXME replace with shared macros wherever possible:
    Martin & Daniel: This is a derivation of the creatorProxyRow macro, but the functionality is slightly different. The container here is not going to 
    allow for an arbitrary number of entries. We haven't been able to spend the time working out how to do this without significantly complicating the 
    creatorProxyRow and creatorProxy.
    We will hopefully do the FIXME when we have the time.
    -->
    <#if !inline>
        <div class="glide" tiplabel="Primary Copyright Holder" tooltipcontent="Use this field to nominate a primary copyright holder. Other information about copyright can be added in the 'notes' section by creating a new 'Rights & Attribution note.">
            <h3>${sectionTitle}</h3>
    <#else>
        <label class="toplabel">${sectionTitle}</label> <br />
    </#if>
        <div>
        <#assign creatorType  =copyrightHolderProxy.actualCreatorType!"PERSON" />
        <table id="copyrightHolderTable" class="tableFormat">
        <tr><td>
            <input type="radio" id="copyright_holder_type_person" name="copyrightHolderType" value="Person" <#if creatorType=='PERSON'>checked='checked'</#if> />
            <label for="copyright_holder_type_person">Person</label>
            <input type="radio" id="copyright_holder_type_institution" name="copyrightHolderType" value="Institution" <#if creatorType=='INSTITUTION'>checked='checked'</#if> />
            <label for="copyright_holder_type_institution">Institution</label>
        </td></tr>
        <tr><td>
            <div class="creatorInstitution <#if creatorType =='PERSON'>hidden</#if>" id="copyrightInstitution">
            <span class="creatorInstitution" id="copyrightInstitution">
                <span class="smallLabel">Institution</span>
                <@s.hidden name="copyrightHolderProxy.institution.id" id="copyright_institution_id" value="${(copyrightHolderProxy.institution.id)!}"/>
                <div class="width60percent marginLeft10">
                    <#if creatorType=='INSTITUTION'><#assign institution_name_required="required"/></#if>
                        <@s.textfield id="copyright_holder_institution_name" cssClass="institutionAutoComplete institution ${institution_name_required!}" placeholder="Institution Name"
                            autocompleteName="name" autocompleteIdElement="#copyright_institution_id" autocompleteParentElement="#copyrightInstitution"
                            name="copyrightHolderProxy.institution.name" value="${(copyrightHolderProxy.institution.name)!}" maxlength="255" 
                            title="Please enter a copyright holder institution" />
                </div>
            </span>
            </div>
            
            <div class="creatorPerson <#if creatorType=='INSTITUTION'>hidden</#if>" id="copyrightPerson">
            <span class="creatorPerson" id="copyrightPerson">
                <span class="smallLabel">Person</span>
                <div class="width30percent marginLeft10" >
                    <#if creatorType=='PERSON'><#assign person_name_required="required"/></#if>
                    <@s.hidden name="copyrightHolderProxy.person.id" id="copyright_person_id" onchange="this.valid()"  autocompleteParentElement="#copyrightPerson"  />
                    <@s.textfield id="copyright_holder_person_last_name" cssClass="nameAutoComplete ${person_name_required!}" placeholder="Last Name"
                        autocompleteName="lastName" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                        name="copyrightHolderProxy.person.lastName" maxlength="255" autocomplete="off"
                        title="Please enter the copyright holder's last name" />
                    <@s.textfield id="copyright_holder_person_first_name" cssClass="nameAutoComplete ${person_name_required!}" placeholder="First Name"
                        autocompleteName="firstName" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                        name="copyrightHolderProxy.person.firstName"  maxlength="255" autocomplete="off"
                        title="Please enter the copyright holder's first name" />
                    <@s.textfield cssClass="nameAutoComplete" placeholder="Email"
                        autocompleteName="email" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                        name="copyrightHolderProxy.person.email" maxlength="255" autocomplete="off"/>
                    <br />
                </div>
                <div class="width60percent marginLeft10">
                    <@s.textfield id="copyright_holder_institution_name" cssClass="nameAutoComplete" placeholder="Institution Name"
                        autocompleteName="institution" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                        name="copyrightHolderProxy.person.institution.name" maxlength="255" />
                </div>
            </span>
            </div>
        </td></tr>
        </table>
        </div>
    <#if !inline>
        </div>
    </#if>
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

<#macro combobox name target label autocompleteParentElement autocompleteIdElement placeholder value cssClass>
            <div class="control-group">
                <label class="control-label">${label}</label>
                <div class="controls">
                    <div class="input-append">
                        <@s.textfield theme="simple" name="${name}"  target="${target}"
                         label="${label}"
                         autocompleteParentElement="${autocompleteParentElement}"
                         autocompleteIdElement="${autocompleteIdElement}"
                         placeholder="${placeholder}"
                        value="${value}" cssClass="span5 ${cssClass}" />
                        <button type="button" class="btn show-all"><i class="icon-chevron-down"></i></button>                    
                    </div>
                </div>
            </div>
            
</#macro>

</#escape>