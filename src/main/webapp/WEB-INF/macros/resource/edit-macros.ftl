<#-- 
$Id$ 
Edit freemarker macros.  Getting large, should consider splitting this file up.
-->
<#-- include navigation menu in edit and view macros -->
<#escape _untrusted as _untrusted?html>
<#include "common.ftl">
<#include "navigation-macros.ftl">

<#macro basicInformation itemTypeLabel="file" itemPrefix="resource" isBulk=false>
<div class="glide" >
    <h3>Basic Information</h3>
  <#if resource.id?? &&  resource.id != -1>
      <@s.hidden name="id"  value="${resource.id?c}" />
  </#if>
  
  <@s.hidden name="startTime" value="${currentTime?c}" />

        <div id="spanStatus" tooltipcontent="#spanStatusToolTip"><@s.select labelposition='left' label='Status' value="resource.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}'/>
        <#if resource.resourceType.project><em>Note: project status does not affect status of child resources.</em></#if>
        <br/>
    </div>    
    

    <div id="spanStatusToolTip" class="hidden">
        <h2>Status</h2>
        <div>
            <#-- TODO: verbiage -->
            Indicates the stage of a resource's lifecycle and how tDAR treats its content.
            <dl>
                <dt>Draft</dt><dd>The resource is under construction and/or incomplete</dd>
                <dt>Active</dt><dd>The resource is considered to be complete.</dd>
                <dt>Flagged</dt><dd>This resource has been flagged for deletion or requires attention</dd>
                <dt>Deleted</dt><dd>The item has been 'deleted' from tDAR workspaces and search results, and is considered deprecated.</dd>  
            </dl>
            
        </div>
    </div>
<#if isBulk>

	<@s.hidden labelposition='left' id='resourceTitle' label='Title' name='image.title' cssClass="" value="BULK_TEMPLATE_TITLE"/>
	<@s.hidden labelposition='left' id='dateCreated' label='Year Created' name='image.date' cssClass="" value="-100"/>
	<@s.hidden id='ImageDescription' name='image.description' value="placeholder description"/>

<#else>
    <span
    tiplabel="Title"
    tooltipcontent="Enter the entire title, including sub-title, if appropriate."
>
   
<@s.textfield labelposition='left' id="resourceRegistrationTitle" label='Title' 
	title="A title is required for all ${itemTypeLabel}s" name='${itemPrefix}.title' cssClass="required descriptiveTitle longfield" required=true maxlength="512"/>
</span>
<br/>

	<#if resource.resourceType != 'PROJECT'>
	<span tiplabel="Year" tooltipcontent="Four digit year, e.g. 1966 or 2005.">
	       <#assign dateVal = ""/>
	       <#if resource.date?? && resource.date != -1>
	         <#assign dateVal = resource.date?c />
	      </#if>
	        <@s.textfield labelposition='left' id='dateCreated' label='Year' name='${itemPrefix}.date' value="${dateVal}" cssClass="shortfield reasonableDate required" required=true
	          title="Please enter the year this ${itemTypeLabel} was created" />
	</span>
	</#if>
</#if>
    
    <#nested>

</div>

</#macro>

<#macro abstractSection itemPrefix="resource">
<div class="glide">
<h3>Abstract / Description *</h3>
<span id="t-abstract" class="clear"
    tiplabel="Abstract / Description"
    tooltipcontent="Short description of the ${resource.resourceType.label}.">
    <@s.textarea labelposition='top' id='resourceDescription'  name='${itemPrefix}.description' rows="5" cssClass='required resizable tdartext' required=true title="A description is required" />
</span>

</div>
</#macro>

<#macro organizeResourceSection>
    <div class="glide" id="organize">
<h3>${siteAcronym} Collection(s) &amp; Project</h3>
<h4>Add it to a Collection</h4>
 	<@edit.resourceCollectionSection />
    
    <div id="projectTipText" style="display:none;">
      Select a project with which your ${resource.resourceType.label} will be associated. This is an important choice because it 
      will allow metadata to be inherited from the project further down this 
      form
    </div>

        <#if !resource.resourceType.project>
<h4>Choose a Project</h4>
          <div id="t-project" tooltipcontent="#projectTipText" tiplabel="Project">
                  <#if resource.id != -1>
                      <@s.select labelposition='left' label='Project' emptyOption='true' id='projectId' name='projectId' listKey='id' listValue='title' list='%{potentialParents}'
                      truncate=70 value='project.id' required="true" title="Please select a project" cssClass="required" />
                  <#else>
                      <@s.select labelposition='left' label='Project' title="Please select a project" emptyOption='true' id='projectId' name='projectId' listKey='id' listValue='title' list='%{potentialParents}'
                      truncate=70 value="${request.getParameter('projectId')!''}"required="true" cssClass="required" />
                  </#if>
              <br/>
          </div>
<div id="divSelectAllInheritanceTooltipContent" style="display:none"> 
Projects in tDAR can contain a variety of different information resources and used to organize a set of related information resources such as documents, datasets, coding sheets, and images. A project's child resources can either inherit or override the metadata entered at this project level. For instance, if you enter the keywords "southwest" and "pueblo" on a project, resources associated with this project that choose to inherit those keywords will also be discovered by searches for the keywords "southwest" and "pueblo". Child resources that override those keywords would not be associated with those keywords (only as long as the overriden keywords are different of course). 
</div>

<div class="indentFull" tiplabel="Inherit Metadata from Selected Project" tooltipcontent="#divSelectAllInheritanceTooltipContent" id="divInheritFromProject">
    <input type="checkbox" value="true" id="cbSelectAllInheritance" class="">
    <label class="datatable-cell-unstyled" for="cbSelectAllInheritance" id="lblCurrentlySelectedProject">inherit from project</label>
</div>
        </#if>   
    
</div>
</#macro>

<#macro resourceCollectionSection>
   <div style="display:none" id="divResourceCollectionListTips">
        <p>
            Specify the names of the collections that tDAR should add this resource to.  Alternately you can start a new, <em>public</em>  collection 
            by typing the desired name and selecting the last option in the list of pop-up results.  The newly-created collection will contain only this 
            resource, but can be modified at any time. 
        </p>
    </div>


    <p tiplabel="tDAR Collections" tooltipcontent="#divResourceCollectionListTips">
        <em>Collections enable you to organize and share resources within tDAR</em>
        <table id="resourceCollectionTable" class="tableFormat width99percent repeatLastRow" addAnother="add another collection">
            <thead>
                <th colspan=2>Collection Name</th>
            </thead>
            <tbody>
                <#if (resourceCollections?? && !resourceCollections.empty)>
                  <#list resourceCollections as resourceCollection>
                    <@resourceCollectionRow resourceCollection resourceCollection_index/>
                  </#list>
                <#else>
                    <@resourceCollectionRow blankResourceCollection />
                </#if>
            </tbody>
        </table>
    </p>

</#macro>

<#macro keywordRows keywordList keywordField showDelete=true>
    <#if keywordList.empty >
      <@keywordRow keywordField />
    <#else>
    <#list keywordList as keyword>
      <@keywordRow keywordField keyword_index />
    </#list>
    </#if>
</#macro>

<#macro keywordRow keywordField keyword_index=0 showDelete=true>
    <tr id='${keywordField}Row_${keyword_index}_'>
    <td>
        <@s.textfield name='${keywordField}[${keyword_index}]' cssClass='longfield keywordAutocomplete' autocomplete="off" />
    </td>
    <#if showDelete>
    <td><@clearDeleteButton id="${keywordField}Row" /></td>
    </#if>
    </tr>
</#macro>


<#macro spatialContext showInherited=true>
<div class="glide">
    <h3>Spatial Terms</h3>
    <@inheritsection checkboxId="cbInheritingSpatialInformation" name='resource.inheritingSpatialInformation' showInherited=showInherited />
    <div id="divSpatialInformation">
        <div tiplabel="Spatial Terms: Geographic"
            tooltipcontent="Keyword list: Geographic terms relevant to the document, e.g. &quot;Death Valley&quot; or &quot;Kauai&quot;." >
        <label>Geographic Term</label>
        <table id="geographicKeywordTable" class="repeatLastRow field" addAnother="add another geographic term">
            <tbody>
            <@keywordRows geographicKeywords 'geographicKeywords' />
            </tbody>
        </table>
        </div>
        <div id='large-google-map' style='height:450px;'
            tiplabel="Geographic Coordinates"
            tooltipcontent="Identify the approximate region of this resource by clicking on &quot;Select Region&quot; and drawing a bounding box on the map.
                <br/>Note: to protect site security, tDAR obfuscates all bounding boxes, bounding boxes smaller than 1 mile, especially.  This 'edit' view 
                will always show the exact coordinates."
            ></div>
        <br />
        <div id="divManualCoordinateEntry" tooltipcontent="#divManualCoordinateEntryTip">
            
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
                <@s.textfield  name='latitudeLongitudeBoxes[0].maximumLatitude' id='maxy' size="14" cssClass="float latLong" title="Please enter a valid Maximum Latitude" />
                <input type="text"  id='d_maxy'  watermark="Latitude (max)" onChange='processLatLong(this)' onBlur='processLatLong(this)' />
                </td>
                <td></td>
                </tr>
                <tr>
                <td style="width:33%;text-align:center">
                    <@s.textfield  name="latitudeLongitudeBoxes[0].minimumLongitude" id='minx' size="14" cssClass="float latLong" title="Please enter a valid Minimum Longitude" />
                    <input type="text"  id='d_minx'  watermark="Longitude (min)"  onChange='processLatLong(this)' onBlur='processLatLong(this)' />
                </td>
                <td style="width:33%;text-align:center">
                    <input type="button" id="locate" value="Locate" onclick="locateCoords();" style="padding:5px; margin:0;width:10em" />
                </td>
                <td style="width:33%;text-align:center">
                    <@s.textfield  name="latitudeLongitudeBoxes[0].maximumLongitude" id='maxx' size="14" cssClass="float latLong" title="Please enter a valid Maximum Longitude" />
                    <input type="text"  id='d_maxx'   watermark="Longitude (max)" onChange='processLatLong(this)' onBlur='processLatLong(this)' />
                </td>
                </tr>
                <tr>
                <td></td>
                <td>
                    <@s.textfield  name="latitudeLongitudeBoxes[0].minimumLatitude" id="miny" size="14" cssClass="float latLong " title="Please enter a valid Minimum Latitude" /> 
                    <input type="text" id="d_miny"  watermark="Latitude (min)" onChange='processLatLong(this)' onBlur='processLatLong(this)' /> 
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
                    <p><aside><strong>Note:</strong> to protect site security, tDAR obfuscates all bounding boxes, bounding boxes smaller than 1 mile.  This 'edit' view will 
                    always show the exact coordinates.</aside></p>
                                   
                 </div>
            </div>
        </div>
    </div>
</div>
</#macro>


<#macro resourceProvider showInherited=true>
<div class="glide" id="divResourceProvider" tiplabel="Resource Provider" tooltipcontent="The institution authorizing tDAR to ingest the resource for the purpose of preservation and access.">
	<h3>Institution Authorizing Upload of this ${resource.resourceType.label}</h3>
	<@s.textfield labelposition='left' label='Institution' name='resourceProviderInstitutionName' id='txtResourceProviderInstitution' cssClass="institution longfield" size='40'/>
	<br/>
</div>
</#macro>


<#macro temporalContext showInherited=true>
<div class="glide">
<h3>Temporal Coverage</h3>
<@inheritsection checkboxId="cbInheritingTemporalInformation" name='resource.inheritingTemporalInformation' showInherited=showInherited  />
<div  id="divTemporalInformation">
    <div
        tiplabel="Temporal Term"
        tooltipcontent="Keyword list: Temporal terms relevant to the document, e.g. &quot;Pueblo IV&quot; or &quot;Late Archaic&quot;.">
        <label>Temporal Terms</label>
    <table id="temporalKeywordTable" class="repeatLastRow field" addAnother="add another temporal keyword">
    <tbody>
    <@keywordRows temporalKeywords 'temporalKeywords' />
    </tbody>
    </table>
    </div>
    <br/>
    <@coverageDatesSection />
    </div>

</div>
</#macro>

<#macro generalKeywords showInherited=true>

<div class="glide" 
    tiplabel="General Keyword(s)"
    tooltipcontent="Keyword list: Select the artifact types discussed in the document.">   
    <h3>General Keyword(s)</h3>
    <@inheritsection checkboxId="cbInheritingOtherInformation" name='resource.inheritingOtherInformation'  showInherited=showInherited />
    <div id="divOtherInformation">
        <label>Keyword</label>
        <table id="otherKeywordTable" class="repeatLastRow field" addAnother="add another keyword">
        <tbody>
            <@keywordRows otherKeywords 'otherKeywords' />
        </tbody>
        </table>
    </div>
</div>
</#macro>


<#macro sharedUploadFile divTitle="Upload">
<div class="glide">
    <h3>${divTitle}</h3>
        <div class='fileupload-content'>
            <#nested />
            <#-- XXX: verify logic for rendering this -->
            <#if multipleFileUploadEnabled || resource.hasFiles()>
            <h4>Current ${multipleFileUploadEnabled?string("and Pending Files", "File")}</h4>
            <table id="uploadFiles" class="files">
            </table>
            <table id="files" class='files sortable'>
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
      <label for="resourceAvailability" id="lblResourceAvailability">Embargoed?</label><@s.select labelposition='left'  id='resourceAvailability' name='resourceAvailability' list=["Public", "Embargoed"] />
      <div id="divConfidentialAccessReminder" class="hidden">
          <em>Embargoed records will become public in ${embargoPeriodInYears} years. Confidential records will not be made public. Use the &quot;Access Rights&quot; section to assign access to this file for specific users.</em>
      </div>
</div>

</#macro>

<#macro siteKeywords showInherited=true divTitle="About Your Site(s)">
<div class="glide" >
<h3>${divTitle}</h3>
<@inheritsection checkboxId='cbInheritingSiteInformation' name='resource.inheritingSiteInformation'  showInherited=showInherited />
<div id="divSiteInformation">
        <div class="hidden" id="siteinfohelp">
        Keyword list: Enter site name(s) and select feature types (<a href="${siteTypesHelpUrl}">view complete list</a>) discussed in the document. Use the Other field if needed.</div>
    <label for="siteNameKeywordTable">Site Name</label>
    <table id="siteNameKeywordTable" class="repeatLastRow field" addAnother="add another site name" 
        tiplabel="About Your Site(s)"
        tooltipcontent="#siteinfohelp">
    <tbody>
    <@keywordRows siteNameKeywords 'siteNameKeywords' />
    </tbody>
    </table>
    
    <br/>
    <label>Site Type</label>
    
    
    <table id="siteTypeKeywordTable" class="field">
    <tbody>
    <tr><td><@s.checkboxlist theme="hier" name="approvedSiteTypeKeywordIds" keywordList="approvedSiteTypeKeywords" /></td></tr>
    </tbody>
    </table>
    
    <label>Other</label>
    <table id="uncontrolledSiteTypeKeywordTable" class="repeatLastRow field" addAnother="add another uncontrolled site type keyword" >
        <tbody>
        <@keywordRows uncontrolledSiteTypeKeywords 'uncontrolledSiteTypeKeywords' />
        </tbody>
    </table>
</div>

</div>
</#macro>


<#macro materialTypes showInherited=true>
<div class="glide" 
    tiplabel="Material Type(s)"
    tooltipcontent="#materialtypehelp">
    <div class="hidden" id="materialtypehelp">
    	Keyword list: Select the artifact types discussed in the document.<a href="${materialTypesHelpUrl}">view all material types</a>
    </div>
    <h3>Material Type(s)</h3>
    <@inheritsection checkboxId='cbInheritingMaterialInformation' name='resource.inheritingMaterialInformation'  showInherited=showInherited />
    <div id="divMaterialInformation">
        <@s.checkboxlist name='materialKeywordIds' list='allMaterialKeywords' listKey='id' listValue='label' listTitle="definition"
            theme="tdar" numColumns=3 cssClass="smallIndent" />
    </div>
</div>

</#macro>

<#macro culturalTerms showInherited=true inline=false>
<div  <#if !inline> class="glide" </#if> 
    tiplabel="Cultural Terms"
    tooltipcontent="#culturehelp">
    <div id="culturehelp" class="hidden">
    Keyword list: Select the archaeological &quot;cultures&quot; discussed in the document. Use the Other field if needed. <a href="${culturalTermsHelpUrl}">view all controlled terms</a>
    </div>
    <h3>Cultural Term(s)</h3>
    <@inheritsection checkboxId="cbInheritingCulturalInformation" name='resource.inheritingCulturalInformation'  showInherited=showInherited />
    <div id="divCulturalInformation">
        <label>Culture</label>
        <table id="cultureKeywordTable" class="field">
            <tbody>
            <tr><td><@s.checkboxlist theme="hier" name="approvedCultureKeywordIds" keywordList="approvedCultureKeywords" /></td></tr>
          </tbody>
        </table>
        
        <br />
        <label>Other</label>
        <table id="uncontrolledCultureKeywordTable" class="repeatLastRow field" addAnother="add another cultural term">
            <tbody>
            <@keywordRows uncontrolledCultureKeywords 'uncontrolledCultureKeywords' />
            </tbody>
        </table>
    </div>
</div>
</#macro>

<#macro uncontrolledCultureKeywordRow uncontrolledCultureKeyword_index=0>
            <tr id='uncontrolledCultureKeywordRow_${uncontrolledCultureKeyword_index}_'>
            <td>
                <@s.textfield name='uncontrolledCultureKeywords[${uncontrolledCultureKeyword_index}]' cssClass='longfield cultureKeywordAutocomplete' autocomplete="off" />
                </td><td><@clearDeleteButton id="uncontrolledCultureKeywordRow" />
            </td>
            </tr>
</#macro>

<#macro investigationTypes showInherited=true >
<div class="glide" tiplabel="Investigation Type(s)" tooltipcontent="#investigationtypehelp">
<div class="hidden" id="investigationtypehelp">
Keyword list: Select the investigation types relevant to the document.<a href="${investigationTypesHelpUrl}">view all investigation types</a></div>
    <h3>Investigation Type(s)</h3>
        <@inheritsection checkboxId='cbInheritingInvestigationInformation' name='resource.inheritingInvestigationInformation'  showInherited=showInherited />
        <div id="divInvestigationInformation">
            <@s.checkboxlist name='investigationTypeIds' list='allInvestigationTypes' listKey='id' listValue='label' numColumns=2 cssClass="smallIndent" 
                listTitle="definition" />
        </div>
</div>
</#macro>


<#-- provides a fieldset just for full user access -->
<#macro fullAccessRights tipsSelector="#divAccessRightsTips">
<div id="divAccessRightsTips" style="display:none">
<p>Determines who can edit a document or related metadata. Enter the first few letters of the person's last name. 
The form will check for matches in the tDAR database and populate the related fields.</p>
<em>Types of Permissions</em>
<dl>
    <dt>View All</dt>
    <dd>User can view/download all file attachments.</dd>
    <dt>Modify Record<dt>
    <dd>User can edit this resource.<dd>
</dl>
</div>

<div
    id="divAccessRights"
    class="glide"
    tiplabel="User Access Rights"
    tooltipcontent="${tipsSelector}">
<h3><a name="accessRights"></a>Access Rights</h3>
<h4>Users who can view or modify this resource</h4>
<table id="accessRightsTable" class="tableFormat width99percent repeatLastRow" addAnother="add another user">
<tbody>
<#if authorizedUsers.empty >
  <@authorizedUserRow blankAuthorizedUser />
<#else>
  <#list authorizedUsers as authorizedUser>
    <#if authorizedUser??>
      <@authorizedUserRow authorizedUser authorizedUser_index />
   </#if>
  </#list>
</#if>
</tbody>
</table>

<#nested>

 <#if persistable.resourceType??>
  <@resourceCollectionsRights effectiveResourceCollections >
  Note: this does not reflect changes to resource collection you have made until you save.
  </@resourceCollectionsRights>
 </#if>

</div>
</#macro>

<#macro authorizedUserRow authorizedUser authorizedUser_index=0>
 <#assign disabled = "false" />
 <#if authorizedUser.user.id == authenticatedUser.id>
   <#assign disabled = "true" />
 </#if>
  <tr id='authorizedUserRow_${authorizedUser_index}_'>
  <td>
    <div class="width30percent marginLeft10" >
        <@s.hidden name='authorizedUsers[${authorizedUser_index}].user.id' value='${(authorizedUser.user.id!-1)?c}' id="authorizedUserId__id_${authorizedUser_index}_"  cssClass="validIdRequired" onchange="this.valid()"  autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"  />
        <@s.textfield cssClass="userAutoComplete" watermark="Last Name"  readonly="${disabled}" autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"
        autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" autocompleteName="lastName" autocomplete="off"
            name="authorizedUsers[${authorizedUser_index}].user.lastName" maxlength="255" /> 
          <@s.textfield cssClass="userAutoComplete" watermark="First Name"  readonly="${disabled}" autocomplete="off"
              name="authorizedUsers[${authorizedUser_index}].user.firstName" maxlength="255" autocompleteName="firstName"
              autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" 
              autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"  />
        <@s.textfield cssClass="userAutoComplete" watermark="Email" readonly="${disabled}" autocomplete="off"
        autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" autocompleteName="email" autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"
            name="authorizedUsers[${authorizedUser_index}].user.email" maxlength="255"/>
        <br />
    </div>
    <div class="width60percent marginLeft10">
        <@s.textfield cssClass="userAutoComplete" watermark="Institution Name" readonly="${disabled}" autocomplete="off"
            autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" 
            autocompleteName="institution" 
            autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"
            name="authorizedUsers[${authorizedUser_index}].user.institution.name" maxlength="255" />
           <#if disabled?index_of("t") != -1>
            <@s.select name="authorizedUsers[${authorizedUser_index}].generalPermission" 
               emptyOption='false' listValue='label' list='%{availablePermissions}' disabled=true
            />
            <!-- FIXME -- is this needed -->
            <@s.hidden name="authorizedUsers[${authorizedUser_index}].generalPermission" 
                value="${authorizedUser.generalPermission!'VIEW_ALL'}"/>
        <#else>
            <@s.select name="authorizedUsers[${authorizedUser_index}].generalPermission" 
               emptyOption='false' listValue='label' list='%{availablePermissions}'
            />
        </#if>
    </div>
  </td>
  <td><@clearDeleteButton id="authorizedUserRow" disabled="${disabled}" />
  </td>
  
  </tr>

</#macro>

<#macro categoryVariable>
<div id='categoryDivId'>
<@s.select labelposition='left' label='Category' id='categoryId' name='categoryId' 
    onchange='changeSubcategory("#categoryId","#subcategoryId")'
	            autocompleteName="sortCategoryId"
	listKey='id' listValue='name' emptyOption='true' list='%{allDomainCategories}' />
</div>
<div id='subcategoryDivId'>
<@s.select labelposition='left' label='Subcategory' id='subcategoryId' name='subcategoryId' 
    autocompleteName="subCategoryId" headerKey="-1" listKey='id' headerValue="N/A" list='%{subcategories}'/>
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

<#macro manualTextInput typeLabel="" type="">
<div class="glide">
    <h3>${(resource.id == -1)?string("Submit", "Replace")} ${typeLabel}</h3>
    <div>
    <label class='label' for='inputMethodId'>Submit as:</label>
    <select id='inputMethodId' name='fileInputMethod' onchange='refreshInputDisplay()' cssClass="field">
        <#-- show manual option by default -->
        <#assign usetext=(resource.getLatestVersions().isEmpty() || (fileTextInput!"") != "")>
        <#if type=="coding">
            <option value='file' <#if !usetext>selected="selected"</#if>>Upload an Excel or CSV coding sheet file</option>
            <option value='text' <#if usetext>selected="selected"</#if>>Manually enter coding rules into a textarea</option>
        <#else>
            <option value='file' <#if !usetext>selected="selected"</#if>>Upload an OWL file</option>
            <option value='text' <#if usetext>selected="selected"</#if>>Manually enter your ontology into a textarea</option>
        </#if>
    </select>
    </div>
    <br/>

    <div id='uploadFileDiv' style='display:none;'>
    <div id='uploadFileExampleDiv' class='info'  >
    <#if type=="coding">
        <p>
        To be parsed properly your coding sheet should have <b>Code, Term, Description (optional)</b> columns, in order.  For example,
        </p>
        <table class="zebracolors">
        <thead>
        <tr><th>Code</th><th>Term</th><th>Description (optional)</th></tr>
        </thead>
        <tbody>
        <tr>
        <td>18</td><td>Eutamias spp.</td><td>Tamias spp. is modern term</td>
        </tr>
        <tr>
        <td>19</td><td>Ammospermophilus spp.</td><td></td>
        </tr>
        <tr>
        <td>20</td><td>Spermophilus spp.</td><td></td>
        </tr>
        </tbody>
        </table>
        <br/>
    <#else>
        <p>
        We currently support uploads of <a class='external' href='http://www.w3.org/TR/owl2-overview/'>OWL XML/RDF files</a>.  
        You can create OWL files by hand (difficult) or with a tool like <a
        class='external' href='http://protege.stanford.edu/'>the
        Prot&eacute;g&eacute; ontology editor</a>.  Alternatively, choose the <b>Submit
        as: Manually enter your ontology</b> option above and enter your ontology
        into a text area.  
        </p>
    </#if>
    </div>
    <@singleFileUpload />
    </div>
    
    <div id='textInputDiv'>
    <div id='textInputExampleDiv' class='info'>
    <#if type="coding">
        <p>Enter your coding rules in the text area below.  Each line can have a maximum of three elements, separated by commas, 
        and should be in the form <code>code, term, optional description</code>.  Codes can be numbers or arbitrary strings.  
        For example, 
        </p>
        <p>
        <code>1, Small Mammal, Small mammals are smaller than a breadbox</code><br/>
        <code>2, Medium Mammal, Medium Mammals are coyote or dog-sized</code>
        </p>
        
        <div class='note'>If a code, a term, or a description has an embedded comma, 
            the whole value must be enclosed in double quotes, e.g. <br/>
            <code>3, Large Mammal, &quot;Large mammals include deer, antelope, and bears&quot;</code>
        </div>
        <br/>
    <#else>
        <p>
        You can enter your ontology in the text area below.  Separate each concept in
        your ontology with newlines (hit enter), and indicate parent-child relationships
        with tabs (make sure you use the tab key on your keyboard - spaces do not work).
        To specify synonyms for a given term use comma-separated parentheses, e.g.,
        <br/>
        <code>Flake (Debris, Debitage)</code>. 
        <br/> 
        For lithic form, the following would be a simple ontology:
        </p>
        <pre>
            Tool
                Projectile Point
                Scraper (Grattoir)
                    End Scraper
                    Side Scraper
                Other Tool
            Flake (Debris, Debitage)
                Utilized
                Unutilized
            Core
        </pre>
    </#if>
    </div>
    <@s.textarea label='${typeLabel}' labelposition='top' id='fileInputTextArea' name='fileTextInput' rows="5" cssClass='resizable' />
    </div>
</div>

</#macro>

<#macro submit label="Save" fileReminder=true showWrapper=true buttonid="submitButton">
<#if showWrapper>
<div class="glide errorsection"> 
    <div id="error">
    </div>
    <#if fileReminder>
    <div id="reminder">
        <label class="error2">Did you mean to attach a file?</label>
    </div>
    </#if>     
</#if>
    <#nested>
    <@s.submit align='left' cssClass='submitButton' name="submitAction" value="${label}"  id="${buttonid}" />
   	<img src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="visibility:hidden"/>
<#if showWrapper>
	</div> 
	<div id="submitDiv">
	</div>
</#if>
</#macro>

<#macro resourceJavascript formId="#resourceMetadataForm" selPrefix="#resource" includeAsync=false includeInheritance=false>

<script type='text/javascript'>
 var formId = '${formId}';


var dialogOpen = false;
$(document).ready(function() {
    //console.log("edit-macros:ready:" +formId);

    <#if validFileExtensions??>
      setupEditForm(formId,"<@join sequence=validFileExtensions delimiter="|"/>");
    <#else>
      setupEditForm(formId);
    </#if>
    <#nested>
    
	setupSortableTables();

    // gleaning lessons from http://forums.dropbox.com/topic.php?id=16926 [IE Script Issue]
    //if ($.browser.msie && $.browser.version <= 8 ) {
    //FIXME: i think this section is sporatically breaking inheritance UI, but I can't reliably reproduce it. 
    if (false) {
       setTimeout(loadTdarMap,500);
       setTimeout(applyTreeviews,1000);
       setTimeout(initializeEdit,1500);
       <#if includeAsync> 
         setTimeout(function(){applyAsync(formId);},2000);
       </#if>
       <#if includeInheritance>
         setTimeout(function(){applyInheritance(project,resource);updateSelectAllCheckboxState();},2500);
       </#if>
    } else {
      loadTdarMap();
      applyTreeviews();
      initializeEdit();
       <#if includeAsync> 
        applyAsync(formId);
       </#if>
       <#if includeInheritance>
         applyInheritance(project,resource);
         updateSelectAllCheckboxState(); 
       </#if>
    }
    
});

var json;
var project = {};
var resource = {};
    <#noescape>
    <#if includeInheritance>
    	resource = ${resource.toJSON()!""};
    <#if projectAsJson??>
    	project = ${projectAsJson};
    </#if>
    </#if>
    </#noescape>

    <#if validFileExtensions??>
        //FIXME:  use element data instead of global if it's not too slow
        var g_asyncUploadCount = 0;
        function fileAccepted(filename) {
            var regexp = /\.(<@join sequence=validFileExtensions delimiter="|"/>)$/i;
            var accept="<@join sequence=validFileExtensions delimiter="|"/>";
            //console.log("regex:" + regexp + "  test:" + filename);
            return regexp.test(filename);
        }
    </#if>
    
</script>
  
</#macro>



<#macro parentContextHelp element="div" resourceType="resource" valueType="values">
<${element} tiplabel="Inherited Values" tooltipcontent="The parent project for this ${resourceType} defines ${valueType} for this section.  You may also define your own, but note that they will not override the values defined by the parent.">
<#nested>
</${element}>
</#macro>

<#macro relatedCollections showInherited=true>
<div class="glide" id="relatedCollectionsSectionGlide">
    <h3>Museum or Archive Collections</h3>
    <@inheritsection checkboxId="cbInheritingCollectionInformation" name='resource.inheritingCollectionInformation' showInherited=showInherited />
    <div id="relatedCollectionsSection">
	    <label>Source <br/>Collection</label>
	    <table id="sourceCollectionTable" class="repeatLastRow field" tiplabel="Source Collection" tooltipcontent="#divSourceCollectionHelpText">
	      <tbody>
	        <#if sourceCollections.empty>
	            <@sourceCollectionRow blankSourceCollection "sourceCollection"/>
	        <#else>
	          <#list sourceCollections as sourceCollection>
	            <@sourceCollectionRow sourceCollection "sourceCollection" sourceCollection_index/>
	          </#list>
	        </#if>
	      </tbody>
	    </table>
	<br/>    
	    <label>Related or<br/>Comparative <br/> Collection</label>
	    <table id="relatedComparativeCitationTable" class="repeatLastRow field" tiplabel="Related or Comparative Collection" tooltipcontent="#divComparativeCollectionHelpText" >
	      <tbody>
	        <#if relatedComparativeCollections.empty>
	            <@sourceCollectionRow blankRelatedComparativeCollection "relatedComparativeCollection" />
	        <#else>
	          <#list relatedComparativeCollections as relatedComparativeCollection>
	            <@sourceCollectionRow relatedComparativeCollection "relatedComparativeCollection" relatedComparativeCollection_index/>
	          </#list>
	        </#if>
	
	      </tbody>
	    </table>
	
	    <div style="display:none" id="divSourceCollectionHelpText">
	        <p>
	          The museum or archival accession that contains the
	          artifacts, original photographs, or original notes that are described
	          in this tDAR record.
	        </p>
	    </div>
	    <div style="display:none" id="divComparativeCollectionHelpText">
	        <p>
	        Museum or archival collections (e.g.,
	        artifacts, photographs, notes, etc.) which are associated with (or
	        complement) a source collection. For example, a researcher may have
	        used a comparative collection in an analysis of the materials
	        documented in this tDAR record.
	        </p>
	    </div>
	</div>
</div>
</#macro>

<#macro sourceCollectionRow sourceCollection prefix index=0>
<#assign plural>${prefix}s</#assign>
          <tr id='${prefix}_${index}_'>
          <td>
              <@s.hidden name="${plural}[${index}].id" />
              <@s.textarea name='${plural}[${index}].text' rows="3" cols="60" /></td>
          <td>
            <@edit.clearDeleteButton id="${prefix}Row" />
          </td>
        </tr>
</#macro>

<#macro inheritsection checkboxId name showInherited=true  label="Inherit this section" >
	<div class='inheritlabel'>
	<#if showInherited>
		<@s.checkbox labelposition='right' id='${checkboxId}' name='${name}'cssClass="alwaysEnabled" />
		<label class="alwaysEnabled" for="${checkboxId}">${label}</label> </br>
	<#elseif resource??>
		 <@inheritTips id="${checkboxId}" />
	</#if>
	</div>    
</#macro>

<#macro resourceCollectionRow resourceCollection collection_index = 0 type="internal">
      <tr id="resourceCollectionRow_${collection_index}_">
          <td style="vertical-align:top"> 
              <@s.hidden name="resourceCollections[${collection_index}].id"  id="resourceCollectionRow_${collection_index}_id" />
              <@s.textfield id="resourceCollectionRow_${collection_index}_id" name="resourceCollections[${collection_index}].name" cssClass="collectionAutoComplete"  autocomplete="off"
              autocompleteIdElement="#resourceCollectionRow_${collection_index}_id"
              autocompleteParentElement="#resourceCollectionRow_${collection_index}_" />
          </td>
          <td><@clearDeleteButton id="resourceCollectionRow" /> </td>
      </tr>
</#macro>



<#macro resourceNoteSection showInherited=true>
<div class="glide" id="resourceNoteSectionGlide"
    tiplabel="Notes"  tooltipcontent="Use this section to append any notes that may help clarify certain aspects of the resource.  For example, 
    a &quot;Redaction Note&quot; may be added to describe the rationale for certain redactions in a document.">
    <h3>Notes</h3>
    <@inheritsection checkboxId="cbInheritingNoteInformation" name='resource.inheritingNoteInformation' showInherited=showInherited />
    <div id="resourceNoteSection">
    <table id="resourceNoteTable" class="tableFormat width99percent repeatLastRow" addAnother="add another note">
        <thead>
            <th>Type</th>
            <th colspan="2">Contents</th>
        </thead>
        <tbody>
            <#if (!resourceNotes.empty)>
              <#list resourceNotes as resourceNote>
              <!-- ${resourceNote} -->
                <@noteRow resourceNote resourceNote_index/>
              </#list>
            <#else>
                <@noteRow blankResourceNote />
            </#if>
        </tbody>
    </table>
    </div>
</div>
</#macro>

<#macro noteRow proxy note_index=0>
    <#if proxy??>
      <tr id="resourceNoteRow_${note_index}_">
          <td style="vertical-align:top"> 
              <@s.hidden name="resourceNotes[${note_index}].id" />
              <@s.select emptyOption='false' name='resourceNotes[${note_index}].type' list='%{noteTypes}' listValue="label" /> 
          </td>
          <td>
              <@s.textarea labelposition='left' 
                  cssClass='resizable tdartext'
                  cols='60' rows='3' maxlength='5000'
                  name='resourceNotes[${note_index}].note' 
                  />
          </td>
          <td><@clearDeleteButton id="resourceNoteRow" /> </td>
      </tr>
    </#if>
</#macro>




<#macro coverageDatesSection multiRow=true tooltip=true>
<#local repeatRowClass = "">
<#if multiRow>
<#local repeatRowClass = "repeatLastRow">
</#if>
<#if tooltip>
<div class="hidden" id="coverageDatesTip">
<div>
    Select the approriate type of date (Gregorian calendar date or radiocarbon date). To enter a date range, enter the <em>earliest date</em> in the <em>Start Year field<em> 
    and the latest date in the End Year Field. <em>Dates containing "AD" or "BC" are not valid</em>. Use positive numbers for AD dates (500, 1200), and use negative numbers for BC dates (-500, -1200). Examples: 
    <ul>
        <li>Calendar dates: 300 start, 500 end (number only, smaller value first)</li>
        <li>Radiocarbon dates: 500 start, 300 end (number only, larger value first)</li>     
    </ul>
</div>
</div>
</#if>
<div tiplabel="Coverage Dates" tooltipcontent="#coverageDatesTip">
    <label>Coverage Dates</label>
    <table 
        id="coverageTable" style="width:80%!important"
        class="field tableFormat ${repeatRowClass}" addAnother="add another coverage date">
        <tbody>
            <#if (!coverageDates.empty)>
              <#list coverageDates as coverageDate>
                <@dateRow coverageDate coverageDate_index/>
              </#list>
            <#else>
              <@dateRow blankCoverageDate/>
            </#if>
        </tbody>
    </table>
</div>
</#macro>



<#macro dateRow proxy=proxy proxy_index=0>

    <#if proxy??>
    <tr id="DateRow_${proxy_index}_">
        <td>
            <div class="marginLeft10" >
                <@s.hidden name="coverageDates[${proxy_index}].id" />
                <@s.select name="coverageDates[${proxy_index}].dateType" cssClass="coverageTypeSelect"
                    listValue='label'  headerValue="Date Type" headerKey="NONE"
                    list=allCoverageTypes />
                <@s.textfield  watermark="Start Year" cssClass="coverageStartYear"
                    name="coverageDates[${proxy_index}].startDate" maxlength="10" /> 
                <@s.textfield  watermark="End Year" cssClass="coverageEndYear"
                    name="coverageDates[${proxy_index}].endDate" maxlength="10" />
                <@s.textfield  watermark="Description"  cssClass="coverageDescription"
                    name="coverageDates[${proxy_index}].description" />
                    <#--                    
                     <a href="#" onClick="$(this).parent().find('.circa').toggle();return false;">more</a>
                    <div class="circa" style="display:none">
                    <@s.checkbox name="coverageDates[${proxy_index}].startDateApproximate" label="Start Approx?"/>
                    <@s.checkbox name="coverageDates[${proxy_index}].endDateApproximate" label="End Approx?" />circa</div> -->
            </div>
            </span>
        </td>
        <td>
           <@edit.clearDeleteButton id="DateRow"/>
        </td>
    </tr>
    </#if>
</#macro>


<#macro allCreators sectionTitle proxies prefix inline=false showInherited=false>
    <@resourceCreators sectionTitle proxies prefix inline showInherited />
    <style>
    </style>
</#macro>

<#macro resourceCreators sectionTitle proxies prefix inline=false showInherited=false>
<#if !inline>
<div class="glide" tiplabel="Individual or Institutional Credit" tooltipcontent="Use these fields to properly credit individuals and institutions for their contribution to the resource. Use the '+' sign to add fields for either persons or institutions, and use the drop-down menu to select roles">
    <h3>${sectionTitle}</h3>
<#else>
<label class="toplabel">${sectionTitle}</label> <br />
</#if>

    <table 
        id="${prefix}Table"
        class="tableFormat countingtable">
        <tbody>
            <#if proxies?has_content >
              <#list proxies as proxy>
                <@creatorProxyRow proxy  prefix proxy_index/>
              </#list>
            <#else>
              <@creatorProxyRow blankCreatorProxy prefix 0 />
              <@creatorProxyRow blankCreatorProxy2 prefix 1 "INSTITUTION"/> 
            </#if>
        </tbody>
    </table>
    <button type="button" class="addAnother normalTop" onclick="repeatRow('${prefix}Table', personAdded)"><img src="/images/add.gif" />add another person</button>
    <button type="button" class="addAnother normalTop" onclick="repeatRow('${prefix}Table', institutionAdded)"><img src="/images/add.gif "/>add another institution</button>
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
    <tr id="${prefix}Row_${proxy_index}_">
      
        <td>
            <span class="creatorPerson <#if proxy.actualCreatorType=='INSTITUTION' || type_override == "INSTITUTION">hidden</#if>"  id="${prefix}Row_${proxy_index}_p">
            <span class="smallLabel">Person</span>
            <div class="width30percent marginLeft10" >
                <@s.hidden name="${prefix}Proxies[${proxy_index}].person.id" id="${prefix}person_id${proxy_index}" onchange="this.valid()"  autocompleteParentElement="#${prefix}Row_${proxy_index}_p"  />
                <@s.textfield cssClass="nameAutoComplete" watermark="Last Name" autocomplete="off"
                	 autocompleteName="lastName" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                    name="${prefix}Proxies[${proxy_index}].person.lastName" maxlength="255" /> 
                <@s.textfield cssClass="nameAutoComplete" watermark="First Name" autocomplete="off"
                	 autocompleteName="firstName" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                    name="${prefix}Proxies[${proxy_index}].person.firstName" maxlength="255" />
                <@s.textfield cssClass="nameAutoComplete" watermark="Email" autocomplete="off"
                	 autocompleteName="email" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                    name="${prefix}Proxies[${proxy_index}].person.email" maxlength="255"/>
                <br />
            </div>
            <div class="width60percent marginLeft10">
                <@s.textfield cssClass="nameAutoComplete" watermark="Institution Name" autocomplete="off"
                	 autocompleteName="institution" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                    name="${prefix}Proxies[${proxy_index}].person.institution.name" maxlength="255" />
                <@s.select name="${prefix}Proxies[${proxy_index}].personRole"  autocomplete="off"
                    listValue='label' label="Role "
                    list=relevantPersonRoles  
                    cssClass="creator-role-select"
                    />
            </div>
            </span>
            <span class="creatorInstitution <#if type_override == "PERSON" || (proxy.actualCreatorType=='PERSON' && type_override=='NONE') >hidden</#if>" id="${prefix}Row_${proxy_index}_i">
                <span class="smallLabel">Institution</span>
                <@s.hidden name="${prefix}Proxies[${proxy_index}].institution.id" id="${prefix}institution_id${proxy_index}"/>
            <div class="width60percent marginLeft10">
                <@s.textfield cssClass="institutionAutoComplete institution" watermark="Institution Name" autocomplete="off"
                	 autocompleteName="name" autocompleteIdElement="#${prefix}institution_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_i"
                    name="${prefix}Proxies[${proxy_index}].institution.name" maxlength="255" />
                <@s.select name="${prefix}Proxies[${proxy_index}].institutionRole" 
                    listValue='label' label="Role "
                    list=relevantInstitutionRoles
                    cssClass="creator-role-select"
                     />
            </div>
            </span>
        </td>
        <td><button class="addAnother minus" type="button" tabindex="-1" onclick="deleteParentRow(this)"><img src="/images/minus.gif" class="minus" alt="delete row" /></button></td>
    </tr>
    </#if>
</#macro>


<#macro identifiers showInherited=true>
    <div class="glide" id="divIdentifiersGlide" tiplabel="${resource.resourceType.label} Specific or Agency Identifiers" tooltipcontent="#divIdentifiersTip">
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
        <h3>${resource.resourceType.label} Specific or Agency Identifiers</h3>
	    <@inheritsection checkboxId="cbInheritingIdentifierInformation" name='resource.inheritingIdentifierInformation' showInherited=showInherited />
	    <div id="divIdentifiers">
        <table id="resourceAnnotationsTable" class="tableFormat width99percent repeatLastRow" addAnother="add another identifier" >
            <thead>
                <tr>
                    <th>Name</th>
                    <th colspan=2>Value</th>
                </tr>
            </thead>
            <tbody>
              <#if resourceAnnotations.empty>
                <@displayAnnotation blankResourceAnnotation />              
              </#if>
                <#list resourceAnnotations as annotation>
                  <@displayAnnotation annotation annotation_index/>
                </#list>
            </tbody>
        </table>
        </div>
    </div>

</#macro>

<#macro displayAnnotation annotation annotation_index=0>
    <tr id="resourceAnnotationRow_${annotation_index}_">
        <td style="width:50%">
            <@s.textfield cssClass="annotationAutoComplete" name='resourceAnnotations[${annotation_index}].resourceAnnotationKey.key' value='${annotation.resourceAnnotationKey.key!""}'  autocomplete="off" />
        </td>
       <td style="width:50%">
            <@s.textfield name='resourceAnnotations[${annotation_index}].value'  value='${annotation.value!""}' />
        </td>
        <td><@clearDeleteButton id="resourceAnnotationRow" /></td>                        
    </tr>

</#macro>
<#macro join sequence delimiter=",">
  <#if sequence??>
    <#list sequence as item>
        ${item}<#if item_has_next>${delimiter}</#if><#t>
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
          <@s.checkbox name="fileProxies[0].confidential" id="cbConfidential" labelposition="right" label="This item contains confidential information" /> 
          <div><b>NOTE:</b> by checking this box, only the metadata will be visible to users, they will not be able to view this item.  
          You may explicity grant read access to users below.</div>
          <br />     
          </div>
      </@singleFileUpload>
    </@sharedUploadFile>
</#macro>


<#macro asyncFileUpload uploadLabel="File" showMultiple=false divTitle="Upload" divId="divFileUpload" >
	<#assign confidentialLabelText>
        <#if showMultiple>These items contain<#else>This item contains</#if> confidential information
        </#assign>

        <@sharedUploadFile>
        <div class="action-errors hidden" id='divAsyncFileUploadErrors'></div>
        <#nested />

        <input type="hidden" name="ticketId" id="ticketId" value="${ticketId!""}" />
        <div class='file-upload ui-widget' id=${divId} tooltipcontent="#${divId}ToolTip">
            <label class="fileinput-button ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary" role="button">
                <span class='ui-button-icon-primary ui-icon ui-icon-plusthick'></span>
                <span class="ui-button-text">
                <span>
                <#if showMultiple>
                Select Files
                <#else>
                Select File
                </#if>
                </span>
                </span>
                <input type="file" name="uploadFile" <#if showMultiple>multiple</#if> id="fileAsyncUpload" />
            </label>
        </div>
        <div id="${divId}ToolTip" class="hidden">
            <h2>Upload file(s)</h2>
            <div>
                Add files to this resource by clicking on the button labeled "Select Files".
                <ul>
                    <li>Files must be of the following types: <@join sequence=validFileExtensions delimiter=", "/></li>
                    <li>You may repeat this process to upload several files.</li>
                    <li class="supports-multifile">Some browsers support selecting multiple files in the "File Upload" dialog using the SHIFT, CTRL, or Command key.</li>
                    <li class="supports-draganddrop">Some browsers support drag-and-drop uploads. Drag files from your desktop to the "Select Files" button.</li>                    
                </ul>
            </div>
        </div>
        <div class="field">
         <i>Valid file types include: <@join sequence=validFileExtensions delimiter=", "/></i>
        </div>
        </@sharedUploadFile>
    
</#macro>

<#macro fileProxyRow rowId="{ID}" filename="{FILENAME}" filesize="{FILESIZE}" action="ADD" fileid=-1 versionId=-1>
<tr id="fileProxy_${rowId}" class="${(fileid == -1)?string('newrow', '')} sortable">
<td class="fileinfo">
    <div class="width99percent">
            <#if fileid == -1>
                <b class="filename replacefilename" title="{FILENAME}">{FILENAME}</b> 
            <#else>
                <b>Existing file:</b> <a class='filename' href="<@s.url value='/filestore/${versionId?c}/get'/>" title="${filename?html}"><@truncate filename 45 /></a>
            </#if>
    
        <span style='font-size: 0.9em;'>(${filesize} bytes)</span>

        <input type="hidden" class="fileAction" name="fileProxies[${rowId}].action" value="${action}"/>
        <input type="hidden" class="fileId" name="fileProxies[${rowId}].fileId" value="${fileid?c}"/>
        <input type="hidden" class="fileReplaceName" name="fileProxies[${rowId}].filename" value="${filename}"/>
        <input type="hidden" class="fileSequenceNumber" name="fileProxies[${rowId}].sequenceNumber" value=${rowId} />

    </div>
    <#if multipleFileUploadEnabled>
    <div class="width99percent field proxyConfidentialDiv">
        <@s.checkbox id="proxy${rowId}_conf" name="fileProxies[${rowId}].confidential" style="padding-left: 20px;" 
        onclick="updateFileAction('#fileProxy_${rowId}', 'MODIFY_METADATA');showAccessRightsLinkIfNeeded();" cssClass="fileProxyConfidential"/>
        <label for="proxy${rowId}_conf">Confidential</label>
    </div>
    </#if>
    <#nested />
</td>
<td>
    <button id='deleteFile_${rowId}' onclick="deleteFile('#fileProxy_${rowId}', ${(fileid == -1)?string}, this);return false;"  type="button"
    class="deleteButton file-button cancel ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary" role="button">
    <span class="ui-button-icon-primary ui-icon ui-icon-cancel"></span><span class="ui-button-text">delete</span></button><br/>
    <#if fileid != -1>
    <button onclick="replaceDialog('#fileProxy_${rowId}','${filename}');return false;"  type="button"
    class="replaceButton file-button cancel ui-button ui-widget ui-state-disabled ui-corner-all ui-button-text-icon-primary" role="button" disabled=disabled>
    <#-- replace with ui-icon-transferthick-e-w ? -->
    <span class="ui-button-icon-primary ui-icon"></span><span class="ui-button-text">replace</span></button>
    </#if>
</td>
</tr>
</#macro>

<#macro citationInfo prefix="resource" includeAbstract=true >
     <#if !resource.resourceType.codingSheet && !resource.resourceType.ontology>
<div id="citationInformation" class="glide"> 
	<h3>Additional Citation Information</h3>

	<#nested />

    <div id="divUrl" tiplabel="URL" tooltipcontent="Website address for this resource, if applicable">
        <@s.textfield name="${prefix}.url" id="txtUrl" label="URL" labelposition="left" cssClass="longfield url" />
    </div>
</div>
    </#if>
    <#if includeAbstract>
		<@abstractSection "${prefix}" />
    </#if>

    <#if resource.resourceType.label?lower_case != 'project'>
		<@copyrightHolders 'Primary Copyright Holder' copyrightHolderProxy />
    </#if>
</#macro>

<#macro sharedFormComponents showInherited=true fileReminder=true prefix="${resource.resourceType.label?lower_case}">

    <@organizeResourceSection />

    <#if resource.resourceType.label?lower_case != 'project'>
      <@resourceProvider showInherited />
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

    
    
    <#if resource.resourceType.label?lower_case  != 'document'>
      <@relatedCollections showInherited />
    </#if>
    
    <@edit.fullAccessRights />
    <#if resource.resourceType.label?lower_case != 'project'>
      <@edit.submit fileReminder=((resource.id == -1) && fileReminder) />
    <#else>
      <@edit.submit fileReminder=false />
    </#if>

</#macro>

<#macro title>
<#if resource.id == -1>
<title>Create a new ${resource.resourceType.label}</title>
<#else>
<title>Editing ${resource.resourceType.label} Metadata for ${resource.title} (tDAR id: ${resource.id?c})</title>
</#if>
</#macro>

<#macro sidebar>
<div id="sidebar" parse="true">
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
<@s.textfield name="query" id="query" label="Title" cssClass='longfield' /><br/>
<label for="project-selector">Project:</label>
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
</select><br/>
<label for="collection-selector">Collection:</label>
<select id="collection-selector">
    <option value="" selected='selected'>All Collections</option>
    <@s.iterator value='resourceCollections' var='rc'>
        <option value="${rc.id?c}" title="${rc.name!""?html}"><@truncate rc.name!"(No Name)" 70 /></option>
    </@s.iterator>
</select>
<br/>
<div>
    <@s.select labelposition='left' id="statuses" headerKey="" headerValue="Any" label='Status' name='status'  emptyOption='false' listValue='label' list='%{statuses}'/></span>
    
    <@s.select labelposition='left' id="resourceTypes" label='Resource Type' name='resourceType'  headerKey="" headerValue="All" emptyOption='false' listValue='label' list='%{resourceTypes}'/></span>

    <br/>
    <@s.select labelposition='left' label='Sort By' emptyOption='false' name='sortBy' 
     listValue='label' list='%{resourceDatatableSortOptions}' id="sortBy"
     value="ID_REVERSE" title="Sort resource by" />
</div>
<!-- <ul id="proj-toolbar" class="projectMenu"><li></li></ul> -->
</div>
<table cellpadding="0" cellspacing="0" border="0" class="display" id="resource_datatable" width="650px">
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
        sDom:'<"datatabletop"ilrp>t<>', //omit the search box
        sPaginationType:"full_numbers",
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
    var html = '<a href="'  + getURI(objResource.urlNamespace + '/' + objResource.id) + '">' + htmlEncode(objResource.title) + '</a>';
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
<#assign copyrightHolder = copyrightHolderProxy[0]>
<#!-- fixme replace with shared macros wherever possible -->
<#if !inline>
<div class="glide" tiplabel="Primary Copyright Holder" tooltipcontent="Use this field to nominate a primary copyright holder. Other information about copyright can be added in the 'notes' section by creating a new 'Rights & Attribution note.">
    <h3>${sectionTitle}</h3>
<#else>
<label class="toplabel">${sectionTitle}</label> <br />
</#if>
    <div>
        <table id="copyrightHolderTable" class="tableFormat">
        <tr><td>
            <@s.radio name="copyrightHolderType" list=["Person", "Institution"] onchange="quickToggle('copyright' + value, 'copyrightPerson', 'copyrightInstitution');" />
            <div class="creatorInstitution hidden" id="copyrightInstitution">
                <span class="smallLabel">Institution</span>
                        <@s.hidden name="copyrightHolder.institution.id" id="copyright_institution_id"/>
                        <div class="width60percent marginLeft10">
                            <@s.textfield cssClass="institutionAutoComplete institution" watermark="Institution Name" autocomplete="off"
                                autocompleteName="name" autocompleteIdElement="#copyright_institution_id" autocompleteParentElement="#copyrightInstitution"
                                name="copyrightHolder.institution.id" maxlength="255" />
                        </div>
            </div>

            <div class="creatorPerson hidden" id="copyrightPerson">
                <span class="smallLabel">Person</span>
                <div class="width30percent marginLeft10" >
                    <@s.hidden name="copyrightHolder.person.id" id="copyright_person_id" onchange="this.valid()"  autocompleteParentElement="#copyrightPerson"  />
                    <@s.textfield cssClass="nameAutoComplete" watermark="Last Name"autocomplete="off"
                         autocompleteName="lastName" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                        name="copyrightHolder.person.lastName" maxlength="255" />
                    <@s.textfield cssClass="nameAutoComplete" watermark="First Name" autocomplete="off"
                         autocompleteName="firstName" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                        name="copyrightHolder.person.firstName" maxlength="255" />
                    <@s.textfield cssClass="nameAutoComplete" watermark="Email" autocomplete="off"
                         autocompleteName="email" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                        name="copyrightHolder.person.email" maxlength="255"/>
                    <br />
                </div>
                <div class="width60percent marginLeft10">
                    <@s.textfield cssClass="nameAutoComplete" watermark="Institution Name" autocomplete="off"
                     autocompleteName="institution" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                    name="copyrightHolder.person.institution.name" maxlength="255" />
                </div>
        </div>
    </td></tr>
    </table>
    </div>
<#if !inline>
</div>
</#if>
</#if>
</#macro>

</#escape>

