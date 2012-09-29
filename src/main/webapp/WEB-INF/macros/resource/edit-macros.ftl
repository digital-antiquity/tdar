<#-- 
$Id$ 
Edit freemarker macros.  Getting large, should consider splitting this file up.
-->
<#-- include navigation menu in edit and view macros -->
<#include "common.ftl">
<#include "navigation-macros.ftl">

<#macro tooltipdivs>
        <div tooltipfor="spanStatus" class="hidden">
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
</#macro>

<#macro basicInformation itemTypeLabel="file">
<@showControllerErrors/>
<div class="glide" >
    <h3>Basic Information</h3>
    <#if resource.id != -1>
    <@s.hidden name="resourceId" value="${resource.id?c}" />
    </#if>
    <div id="t-project" tooltipcontent="Choose which project a ${itemTypeLabel} is part of." tiplabel="Project"> 
        <#if resource.id != -1>
            <@s.select labelposition='left' label='Project' emptyOption='true' id='projectId' name='projectId' listKey='id' listValue='title' list='%{potentialParents}'
            truncate=80 value='project.id' required="true" title="Please select a project" cssClass="required" />
        <#else>
            <@s.select labelposition='left' label='Project' emptyOption='true' id='projectId' name='projectId' listKey='id' listValue='title' list='%{potentialParents}'
            truncate=80 value="${request.getParameter('projectId')!''}"required="true" title="Please select a project." cssClass="required" />
        </#if>   
        <br/>
        <#-- <#if administrator> -->
        <span id="spanStatus"><@s.select labelposition='left' label='Status' value="resource.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}'/></span>
        <br/>
        <#--</#if>-->
    </div>
    <@tooltipdivs />
    <#nested>
</div>
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
            <@s.iterator status='rowStatus' value='geographicKeywords'>
            <tr id='geographicKeywordRow_${rowStatus.index}_'>
            <td>
                <@s.textfield name='geographicKeywords[${rowStatus.index}]' cssClass='longfield geographicKeywordAutocomplete' />
                </td><td><@clearDeleteButton id="geographicKeywordRow" />
            </td>
            </tr>
            </@s.iterator>
            </tbody>
        </table>
        </div>
        <div id='large-google-map' style='height:450px;'
            tiplabel="Geographic Coordinates"
            tooltipcontent="Identify the approximate region of this resource by clicking on &quot;Select Region&quot; and drawing a bounding box on the map.<br/>Note: to protect site security, tDAR obfuscates all bounding boxes, bounding boxes smaller than 5 miles, especially.  This 'edit' view will always show the exact coordinates."
            ></div>
        <br />
        <div id="divManualCoordinateEntry">
            
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
                
                function processLatLong(element) {
                    var value = $(element).val();
                    var id = $(element).attr('id');
    //                value = value.replace(/([a-z]+)/ig,"");
                    if (id.indexOf("d_") == 0) id = id.substring(2);
                    $("#"+ id).val(Geo.parseDMS(value));
                }
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
            <div tooltipfor="divManualCoordinateEntry" class="hidden">
                <h2>Manually Enter Coordinates</h2>
                <div>
                    Click the Locate button after entering the longitude-latitude pairs in the respective input fields to draw a box on the map and zoom to it.
                    <br />Examples:
                    <ul>
                        <li>40&deg;44'55"N</li>
                        <li>53 08 50N</li>
                        <li>-73.9864</li>
                    </ul>
                    <p><aside><strong>Note:</strong> to protect site security, tDAR obfuscates all bounding boxes, bounding boxes smaller than 1 miles.  This 'edit' view will 
                    always show the exact coordinates.</aside></p>
                                   
                 </div>
            </div>
        </div>
    </div>
</div>
</#macro>


<#macro resourceProvider showInherited=true>
<div class="glide" id="divResourceProvider" tiplabel="Resource Provider" tooltipcontent="The institution authorizing tDAR to ingest the resource for the purpose of preservation and access.">
<h3>Resource Provider</h3>
<@s.textfield labelposition='left' label='Institution' name='resourceProviderInstitution' cssClass="institution longfield" size='40'/>
<br/>
</div>
</#macro>

<#macro authors>
<div 
    tiplabel="Author/Editor/Contributor" 
    tooltipcontent="Enter the first three letters of a term in any field in this section. The form will check for matches in the tDAR database and populate the related fields">
<label class="toplabel">Author / Editor / Contributor</label><br/>
<table id="authorTable" class="tableFormat width99percent repeatLastRow" addAnother="add another author, editor, or contributor">
<thead>
<tr>
<th>Last name</th>
<th>First name</th>
<th>Email (if known)</th>
<th>Institution</th>
<th colspan=2>Role</th>
</tr>
</thead>
<tbody>
<@s.iterator status='rowStatus' value='authorLastNames'>
<tr id='authorRow_${rowStatus.index}_'>
<td>
<@s.textfield name='authorLastNames[${rowStatus.index}]'  size='10' onchange="setCalcPersVal(this)" cssClass="nameAutoComplete"/>
<@s.hidden name='authorIds[${rowStatus.index}]' />
</td>
<td>
<@s.textfield name='authorFirstNames[${rowStatus.index}]'  size='10' onchange="setCalcPersVal(this)" cssClass="nameAutoComplete"/>
</td>
<td>
<@s.textfield name='authorEmails[${rowStatus.index}]'  cssClass="nameAutoComplete"/>
</td>
<td>
<@s.textfield name='authorInstitutions[${rowStatus.index}]'  cssClass="nameAutoComplete"/>
</td>
<td>
<@s.select name='authorRoles[${rowStatus.index}]'  listValue="label" list="%{documentPersonRoles}" 
    emptyOption="true" onchange="setCalcPersVal(this)" cssClass="" 
    title="Please enter a role for this author / editor / contributor."/>
<@s.hidden name='authorValidate[${rowStatus.index}]' value='' cssClass="multiPersRole hiddenCalc" /> 
</td><td><@clearDeleteButton id="authorRow" />
</td>
</tr>
</@s.iterator>
</tbody>
</table>
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
    <@s.iterator status='rowStatus' value='temporalKeywords'>
        <tr id='temporalKeywordRow_${rowStatus.index}_'>
        <td>
            <@s.textfield name='temporalKeywords[${rowStatus.index}]' cssClass='longfield temporalKeywordAutocomplete' />
            </td><td><@clearDeleteButton id="temporalKeywordRow" />
        </td>
        </tr>
    </@s.iterator>
    </tbody>
    </table>
    </div>
    <br/>
    <@coverageDatesSection />

    </div>

</div>

<div class="glide" 
    tiplabel="Other Keyword(s)"
    tooltipcontent="Keyword list: Select the artifact types discussed in the document.">   
    <h3>General Keyword(s)</h3>
    <@inheritsection checkboxId="cbInheritingOtherInformation" name='resource.inheritingOtherInformation'  showInherited=showInherited />
    <div id="divOtherInformation">
        <label>Keyword</label>
        <table id="otherKeywordTable" class="repeatLastRow field" addAnother="add another keyword">
        <tbody>
            <@s.iterator status='rowStatus' value='otherKeywords'>
            <tr id='otherKeywordRow_${rowStatus.index}_'>
            <td>
            <@s.textfield name='otherKeywords[${rowStatus.index}]' cssClass="longfield otherKeywordAutocomplete" />
            </td><td><@clearDeleteButton id="otherKeywordRow" />
            </td>
            </tr>
            </@s.iterator>
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
            <table id="files" class='files'>
            <tbody>
            <#list fileProxies as fileProxy>
                <#if fileProxy??>
                <@fileProxyRow rowId=fileProxy_index filename=fileProxy.filename filesize=fileProxy.size fileid=fileProxy.fileId action=fileProxy.action confidential_=fileProxy.confidential versionId=fileProxy.originalFileVersionId/>
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
      <label for="resourceAvailability" id="lblResourceAvailability">embargoed?</label><@s.select labelposition='left'  id='resourceAvailability' name='resourceAvailability' list=["Public", "Embargoed"] />
      <div id="divConfidentialAccessReminder" class="hidden">
          <em>Use the &quot;Access Rights&quot; section to assign access to this file for specific users.</em>
      </div>
</div>

</#macro>

<#macro keywords showInherited=true divTitle="About Your Site(s)">
<@investigationTypes showInherited />
<div class="glide" >
<h3>${divTitle}</h3>
<@inheritsection checkboxId='cbInheritingSiteInformation' name='resource.inheritingSiteInformation'  showInherited=showInherited />
<div id="divSiteInformation">
    <label for="siteNameKeywordTable">Site Name</label>
    <table id="siteNameKeywordTable" class="repeatLastRow field" addAnother="add another site name" 
        tiplabel="About Your Site(s)"
        tooltipcontent="Keyword list: Enter site name(s) and select feature types discussed in the document. Use the Other field if needed.">
    <tbody>
    <@s.iterator status='rowStatus' value='siteNameKeywords'>
    <tr id='siteNameKeywordRow_${rowStatus.index}_'>
    <td>
    <@s.textfield name='siteNameKeywords[${rowStatus.index}]' cssClass="longfield sitenameAutoComplete" />
    </td><td><@clearDeleteButton id="siteNameKeywordRow" />
    </td>
    </tr>
    </@s.iterator>
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
        <@s.iterator status='rowStatus' value='uncontrolledSiteTypeKeywords'>
            <tr id='uncontrolledSiteTypeKeywordRow_${rowStatus.index}_'>
                <td>
                    <@s.textfield name='uncontrolledSiteTypeKeywords[${rowStatus.index}]' cssClass="longfield siteTypeKeywordAutocomplete" />
                    </td><td><@clearDeleteButton id="uncontrolledSiteTypeKeywordRow" />
                </td>
            </tr>
        </@s.iterator>
        </tbody>
    </table>
</div>

</div>
<@materialTypes showInherited />
<@culturalTerms showInherited />
</#macro>

<#macro materialTypes showInherited=true>
<div class="glide" 
    tiplabel="Material Type(s)"
    tooltipcontent="Keyword list: Select the artifact types discussed in the document.">
    <h3>Material Type(s)</h3>
    <@inheritsection checkboxId='cbInheritingMaterialInformation' name='resource.inheritingMaterialInformation'  showInherited=showInherited />
    <div id="divMaterialInformation">
        <@s.checkboxlist name='materialKeywordIds' list='allMaterialKeywords' listKey='id' listValue='label' listTitle="definition"
            numColumns=3 cssClass="smallIndent" />
    </div>
</div>

</#macro>

<#macro culturalTerms showInherited=true inline=false>
<div  <#if !inline> class="glide" </#if> 
    tiplabel="Cultural Terms"
    tooltipcontent="Keyword list: Select the archaeological &quot;cultures&quot; discussed in the document. Use the Other field if needed.">
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
            <@s.iterator status='rowStatus' value='uncontrolledCultureKeywords'>
            <tr id='uncontrolledCultureKeywordRow_${rowStatus.index}_'>
            <td>
                <@s.textfield name='uncontrolledCultureKeywords[${rowStatus.index}]' cssClass='longfield cultureKeywordAutocomplete' />
                </td><td><@clearDeleteButton id="uncontrolledCultureKeywordRow" />
            </td>
            </tr>
            </@s.iterator>
            </tbody>
        </table>
    </div>
</div>
</#macro>

<#macro investigationTypes showInherited=true >
<div class="glide" tiplabel="Investigation Type(s)" tooltipcontent="Keyword list: Select the investigation types relevant to the document.">
    <h3>Investigation Type(s)</h3>
        <@inheritsection checkboxId='cbInheritingInvestigationInformation' name='resource.inheritingInvestigationInformation'  showInherited=showInherited />
        <div id="divInvestigationInformation">
            <@s.checkboxlist name='investigationTypeIds' list='allInvestigationTypes' listKey='id' listValue='label' numColumns=2 cssClass="smallIndent" 
                listTitle="definition" />
        </div>
</div>
</#macro>


<#-- includes both full user access and read user access -->
<#macro accessRights>
<script type="text/javascript">
$(document).ready(function() {
        $('#resourceAvailability').change(function(value) {
            toggleReadUsers();
        }).trigger('change');

        $('#cbConfidential').change(function(value) {
            toggleReadUsers();
        }).trigger('change');
        
        //bind toggleReadUsers to any current/future asyncfile checkboxes
        $('#files').delegate('.fileProxyConfidential', 'change', function() {
           toggleReadUsers();
        });

});

//FIXME: the name of this function no longer matches its behavior
function toggleReadUsers() {
     if($('#cbConfidential').is(':checked') || $(".fileProxyConfidential:checked").length > 0 || $('#resourceAvailability').val() == 'Embargoed') {
        $('#readUserDiv').show();
     } else {
        $('#readUserDiv').hide();
     }
}
</script>
<@fullAccessRights>
<br/>
<br/>
<div id='readUserDiv'>
<h4>Users who can view/download the attached file(s)</h4>
<table id="readOnlyRightsTable" class="tableFormat width99percent repeatLastRow" addAnother="add another read-only user">
<tbody>
<#assign readUsersList=readOnlyUsers />
<#if readUsersList.empty >
<#assign readUsersList=blankUser />
</#if>
<#list readUsersList as readUser>
<tr id='readUserRow_${readUser_index}_'>
<td>
	<div class="width30percent marginLeft10" >
	    <@s.hidden name='readUserIds[${readUser_index}]' value='${(readUser.id!-1)?c}'  cssClass="rowNotEmpty" onchange="this.valid()"  />
	    <@s.textfield cssClass="userAutoComplete" watermark="Last Name" 
	        name="readOnlyUsers[${readUser_index}].lastName" maxlength="255" onblur="setCalcUserVal(this)" /> 
        <@s.textfield cssClass="userAutoComplete" watermark="First Name" 
            name="readOnlyUsers[${readUser_index}].firstName" maxlength="255" onblur="setCalcUserVal(this)"  />
	    <@s.textfield cssClass="userAutoComplete" watermark="Email"
	        name="readOnlyUsers[${readUser_index}].email" maxlength="255"/>
	    <br />
	</div>
	<div class="width99percent marginLeft10">
	    <@s.textfield cssClass="userAutoComplete" watermark="Institution Name"
	        name="readOnlyUsers[${readUser_index}].institution.name" maxlength="255" onblur="setCalcUserVal(this)" />
	</div>
</td>
<td>
<@clearDeleteButton id="readUserRow" />
</td>
</tr>
</#list>
</tbody>
</table>
<br/>
</div>
</@fullAccessRights>
</#macro>

<#-- provides a fieldset just for full user access -->
<#macro fullAccessRights>
<div
    id="divAccessRights"
    class="glide"
    tiplabel="User Access Rights"
    tooltipcontent="Determines who can edit a document or related metadata. Enter the first few letters of the person's last name. The form will check for matches in the tDAR database and populate the related fields.">
<h3><a name="accessRights">Access Rights</a></h3>
<h4>Users who can modify this resource</h4>
<table id="accessRightsTable" class="tableFormat width99percent repeatLastRow" addAnother="add another full-rights user">
<tbody>
<#assign fullUsersList=fullUsers />
<#if fullUsersList.empty >
<#assign fullUsersList=blankUser />
</#if>
<#list fullUsersList as fullUser>
<tr id='fullUserRow_${fullUser_index}_'>
<td>
	<div class="width30percent marginLeft10" >
	    <@s.hidden name='fullUserIds[${fullUser_index}]' value='${(fullUser.id!-1)?c}'  cssClass="rowNotEmpty" onchange="this.valid()"  />
	    <@s.textfield cssClass="userAutoComplete" watermark="Last Name" 
	        name="fullUsers[${fullUser_index}].lastName" maxlength="255" onblur="setCalcUserVal(this)" /> 
        <@s.textfield cssClass="userAutoComplete" watermark="First Name" 
            name="fullUsers[${fullUser_index}].firstName" maxlength="255" onblur="setCalcUserVal(this)" />
	    <@s.textfield cssClass="userAutoComplete" watermark="Email"
	        name="fullUsers[${fullUser_index}].email" maxlength="255"/>
	    <br />
	</div>
	<div class="width99percent marginLeft10">
	    <@s.textfield cssClass="userAutoComplete" watermark="Institution Name"
	        name="fullUsers[${fullUser_index}].institution.name" maxlength="255" onblur="setCalcUserVal(this)" />
	</div>
</td>
<td><@clearDeleteButton id="fullUserRow" />
</td>

</tr>
</#list>
</tbody>
</table>
<#nested>

</div>
</#macro>


<#macro categoryVariable>
<script type="text/javascript">
function changeSubcategory() {
    $('#subcategoryDivId').load("<@s.url value='/resource/ajax/subcategories'/>", 
            { "categoryVariableId" : $('#categoryId').val() });
}
</script>
<div id='categoryDivId'>
<@s.select labelposition='left' label='Category' id='categoryId' name='categoryId' onchange='changeSubcategory()' listKey='id' listValue='name' emptyOption='true' list='%{allDomainCategories}' />
</div>
<div id='subcategoryDivId'>
<@s.select labelposition='left' label='Subcategory' id='subcategoryId' name='subcategoryId' headerKey="-1" listKey='id' headerValue="N/A" list='%{subcategories}'/>
</div>
</#macro>


<#macro singleFileUpload typeLabel="${resource.resourceType.label}">
    <div tiplabel="Upload your ${typeLabel}(s)" 
    tooltipcontent="The metadata entered on this form will be associated with this file. We accept ${typeLabel}s in the following formats: <@join sequence=validFileExtensions delimiter=", "/>"
    >
    <@s.file name='uploadedFiles' label='${typeLabel}' cssClass="validateFileType" labelposition='left' size='40' />
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
    <@categoryVariable />
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
        into a textarea.  
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

<#macro submit label="Save" fileReminder=true>
<div class="glide errorsection"> 
    <div id="error">
    </div>
    <#if fileReminder>
    <div id="reminder">
        <label class="error2">Did you mean to attach a file?</label>
    </div>
    </#if>     

    <@s.submit align='left' cssClass='submitButton' value="${label}"  id="submitButton" />
</div> 
<div id="submitDiv">
</div>

</#macro>

<#macro resourceJavascript formId="#resourceMetadataForm" selPrefix="#resource" includeAsync=false includeInheritance=false>

<link rel='stylesheet' type='text/css' href="<@s.url value='/includes/jquery-fileupload-4.4.1/jquery.fileupload-ui.css'/>" /> 
<script type='text/javascript' src="<@s.url value='/includes/jquery-fileupload-4.4.1/jquery.fileupload.js'/>"></script>
<script type='text/javascript' src="<@s.url value='/includes/jquery-fileupload-4.4.1/jquery.fileupload-ui.js'/>"></script>
<script type="text/javascript" src="<@s.url value='/includes/jquery.textarearesizer.js'/>"></script> 
<script type="text/javascript" src="<@s.url value='/includes/latLongUtil-1.0.js'/>"></script> 
<script type='text/javascript' src='<@s.url value="/includes/jquery.FormNavigate.js"/>'></script> 
<script type='text/javascript' src='<@s.url value="/includes/jquery.watermark-3.1.3.min.js"/>'></script> 
<script type='text/javascript' src='<@s.url value="/includes/jquery.tabby.min.js"/>'></script> 
<script type='text/javascript' src='<@s.url value="/includes/jquery.populate.js"/>'></script> 
<script type='text/javascript' src='<@s.url value="/includes/stacktrace-min-0.3.js"/>'></script> 
<script type='text/javascript' src='/struts/utils.js'></script>

<script type='text/javascript'>
 var formId = '${formId}';


var dialogOpen = false;
$(document).ready(function() {
    console.trace("edit-macros:ready:" +formId);

    $(formId).FormNavigate("Leaving the page will cause any unsaved data to be lost!"); 



    //FIXME: the jquery validate documentation for onfocusout/onkeyup/onclick doesn't jibe w/ what we see in practice.  supposedly these take a boolean 
    //argument specifying 'true' causes an error.   since true is the default for these three options I'm simply removing those lines from the validate call
    //below.
    //see http://docs.jquery.com/Plugins/Validation/validate#options  for options and defaults
    //see http://whilefalse.net/2011/01/17/jquery-validation-onkeyup/  for undocumented feature that lets you specify a function instead of a boolean.  
    
    //Watermark labels *must* be registered before validation rules are applied, otherwise you get nasty conflicts.
    applyWatermarks();
    $(formId).validate({
        errorLabelContainer: $("#error"),
        onkeyup: function() {return ;},
        onclick: function() {return;},
        onfocusout: function(element) {
        return ;
        // I WORK IN CHROME but FAIL in IE & FF
        // if (!dialogOpen) return;
        // if ( !this.checkable(element) && (element.name in this.submitted || !this.optional(element)) ) {
        //    this.element(element);
        //} 
        },
        invalidHandler: $.watermark.showAll,
        showErrors: function(errorMap, errorList) {
          this.defaultShowErrors();
          if (errorList != undefined && errorList.length > 0 && this.submitted) {
              dialogOpen = true;
            $("#error").clone().dialog({
              title: 'Please correct the following issues before saving',
              buttons: { "Ok": function() { dialogOpen=false;$(this).dialog("close"); } },
              dialogClass:'errorDialog',
              resizable:false,
              draggable:false
            });
          }
        },
        submitHandler: function(f) {
            //prevent multiple form submits (e.g. from double-clicking the submit button)
            $('input[type=submit]', f).attr('disabled', 'disabled');
            f.submit();
        }
    });
    
    //trim any type-converted fields prior to submit
    $(formId).submit(function() {
        try {
            $.each($('.reasonableDate, .coverageStartYear, .coverageEndYear, .date, .number'), function() {
                if($(this).val() == undefined || $(this).val() == "") return;  //this is essential, or IE will replace null values w/ empty-string values, and type-conversion dies.
                var elem = this;
                $(elem).val($.trim($(elem).val()));
            });
        } catch(err){
            console.error("unable to trim:" + err);
        }
        return true;
    });
    
    //this cant go in common because it needs to be called after .validate()
    $('.coverageTypeSelect').each(function(i, elem){
        prepareDateFields(elem);
    });
    
    
    
    
    
    if($(formId + '_uploadedFiles').length>0) {
        console.trace("wiring up uploaded file check");
        var validateUploadedFiles = function() {
            if ($(formId + "_uploadedFiles").val().length > 0) {
                $("#reminder").hide();
            }
        };
        $(formId +'_uploadedFiles').change(validateUploadedFiles);
        validateUploadedFiles();
    }
        
    <#nested>

    // gleaning lessons from http://forums.dropbox.com/topic.php?id=16926 [IE Script Issue]
    if ($.browser.msie && $.browser.version <= 8 ) {
    
       setTimeout(applyTreeviews,1000);
       setTimeout(initializeEdit,1500);
       <#if includeAsync> 
         setTimeout(function(){applyAsync(formId);},2000);
       </#if>
       <#if includeInheritance>
         setTimeout(function(){applyInheritance(project,resource)},2500);
       </#if>
    } else {
      applyTreeviews();
      initializeEdit();
       <#if includeAsync> 
        applyAsync(formId);
       </#if>
       <#if includeInheritance>
         applyInheritance(project,resource);
       </#if>
    }
    
    // FIXME: see if we can sniff this from browser feature instead of browser version
    if ($.browser.msie || $.browser.mozilla && $.browser.mozilla < 4 ) {
        $('textarea.resizable:not(.processed)').TextAreaResizer();
    }

    showAccessRightsLinkIfNeeded();    
    $('#cbConfidential').click(showAccessRightsLinkIfNeeded);
    $('#resourceAvailability').change(showAccessRightsLinkIfNeeded);

});

    
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
    <#if includeInheritance>
    var resource = ${resource.toJSON()!""};
    var project = getBlankProject();
    <#if projectAsJson??>
    var project = ${projectAsJson};
    </#if>
    var json;
    </#if>
</script>
  
</#macro>



<#macro parentContextHelp element="div" resourceType="resource" valueType="values">
<${element} tiplabel="Inherited Values" tooltipcontent="The parent project for this ${resourceType} defines ${valueType} for this section.  You may also define your own, but note that they will not override the values defined by the parent.">
<#nested>
</${element}>
</#macro>

<#macro relatedCollections showInherited=true>
<div class="glide">
    <h3>Source Collections and Related Comparative Collections</h3>
    <label>Source <br/>Collection</label>
    <table id="sourceCollectionTable" class="repeatLastRow field">
      <tbody>
      <@s.iterator status='rowStatus' value='sourceCollections'>
        <tr id='sourceCollectionRow_${rowStatus.index}_'>
          <td><@s.textarea name='sourceCollections[${rowStatus.index}]' rows="3" cols="60" /></td>
          <td>
            <@edit.clearDeleteButton id="sourceCollectionRow" />
          </td>
        </tr>
      </@s.iterator>
      </tbody>
    </table>
<br/>    
    <label>Related or<br/>Comparative <br/> Collection</label>
    <table id="relatedComparativeCitationTable" class="repeatLastRow field">
      <tbody>
      <@s.iterator status='rowStatus' value='relatedComparativeCitations'>
        <tr id='relatedComparativeCitationRow_${rowStatus.index}_'>
        <td><@s.textarea name='relatedComparativeCitations[${rowStatus.index}]' rows="3" cols="60" /></td>
        <td>
            <@edit.clearDeleteButton id="relatedComparativeCitationRow" />
        </td>
        </tr>
      </@s.iterator>
      </tbody>
    </table>
</div>
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


<#macro resourceNoteSection>
<div class="glide"
    tiplabel="Notes"  tooltipcontent="Use this section to append any notes that may help clarify certain aspects of the resource.  For example, 
    a &quot;Redaction Note&quot; may be added to describe the rationale for certain redactions in a document.">
    <h3>Notes</h3>
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
                  cols='60' rows='3' maxlength='255'
                  name='resourceNotes[${note_index}].note' 
                  />
          </td>
          <td><@clearDeleteButton id="resourceNoteRow" /> </td>
      </tr>
    </#if>
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
        class="tableFormat">
        <tbody>
            <#if (proxies.size() > 0)>
              <#list proxies as proxy>
                <@proxyRow proxy  prefix proxy_index/>
              </#list>
            <#else>
              <@proxyRow blankCreatorProxy prefix 0/>
            </#if>
        </tbody>
    </table>
    <button type="button" class="addAnother normalTop" onclick="repeatRow('${prefix}Table', personAdded)"><img src="/images/add.gif" />add person</button>
    <button type="button" class="addAnother normalTop" onclick="repeatRow('${prefix}Table', institutionAdded)"><img src="/images/add.gif "/>add institution</button>
<#if !inline>
</div>
</#if>

</#macro>



<#macro coverageDatesSection>
<label >Coverage Dates</label>
    <table 
        id="coverageTable" style="width:80%!important"
        class="field tableFormat repeatLastRow" callback="coverageRowAdded">
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



<#macro proxyRow proxy=proxy prefix=prefix proxy_index=proxy_index>
    <#assign relevantPersonRoles=personAuthorshipRoles />
    <#assign relevantInstitutionRoles=institutionAuthorshipRoles />
    <#if prefix=='credit'>
        <#assign relevantPersonRoles=personCreditRoles />
        <#assign relevantInstitutionRoles=institutionCreditRoles />
    </#if>

    <#if proxy??>
    <tr id="${prefix}Row_${proxy_index}_">
        <!-- <td> 
            <img class="creatorInstitution icon <#if proxy.actualCreatorType=='PERSON'>hidden</#if>" src="/images/house_silhouette.png"  />
            <img class="creatorPerson icon <#if proxy.actualCreatorType=='INSTITUTION'>hidden</#if>" src="/images/man_silhouette.png" /> 
        </td> -->
        <td>
            <!-- <input type="hidden" name="${prefix}Proxies[${proxy_index}].creatorId" value="-1" /> -->
            <!-- <input type="hidden" name="${prefix}Proxies[${proxy_index}].actualCreatorType" id="${prefix}ProxyActualCreatorType_${proxy_index}_ value="PERSON" />-->
            <span class="creatorPerson <#if proxy.actualCreatorType=='INSTITUTION'>hidden</#if>">
            <span class="smallLabel">Person</span>
            <div class="width30percent marginLeft10" >
                <@s.hidden name="${prefix}Proxies[${proxy_index}].person.id" />
                <@s.textfield cssClass="nameAutoComplete" watermark="Last Name"
                    name="${prefix}Proxies[${proxy_index}].person.lastName" maxlength="255" /> 
                <@s.textfield cssClass="nameAutoComplete" watermark="First Name" 
                    name="${prefix}Proxies[${proxy_index}].person.firstName" maxlength="255" />
                <@s.textfield cssClass="nameAutoComplete" watermark="Email"
                    name="${prefix}Proxies[${proxy_index}].person.email" maxlength="255"/>
                <br />
            </div>
            <div class="width60percent marginLeft10">
                <@s.textfield cssClass="nameAutoComplete" watermark="Institution Name"
                    name="${prefix}Proxies[${proxy_index}].person.institution.name" maxlength="255" />
                <@s.select name="${prefix}Proxies[${proxy_index}].personRole" 
                    listValue='label' label="Role "
                    list=relevantPersonRoles  />
            </div>
            </span>
            <span class="creatorInstitution <#if proxy.actualCreatorType=='PERSON'>hidden</#if>">
                <span class="smallLabel">Institution</span>
                <@s.hidden name="${prefix}Proxies[${proxy_index}].institution.id" />
            <div class="width60percent marginLeft10">
                <@s.textfield cssClass="institution" watermark="Institution Name"
                    name="${prefix}Proxies[${proxy_index}].institution.name" maxlength="255" />
                <@s.select name="${prefix}Proxies[${proxy_index}].institutionRole" 
                    listValue='label' label="Role "
                    list=relevantInstitutionRoles
                     />
            </div>
            </span>
        </td>
        <td><button class="addAnother minus" type="button" tabindex="-1" onclick="deleteParentRow(this)"><img src="/images/minus.gif" class="minus" alt="delete row" /></button></td>
    </tr>
    </#if>
</#macro>


<#macro identifiers showInherited=true>
    <div class="glide" id="divIdentifiers">
        <div tooltipfor="divIdentifiers" class="hidden">
            <div>
                <dl>
                    <dt>Name</<dt>
                    <dd>Description of the following agency or project identifier (e.g. <code>ASU Accession Number</code> or <code>TNF Project Code</code>).</dd>
                    <dt>Value</<dt>
                    <dd>Number, code, or other identifier (e.g. <code>2011.045.335</code> or <code>AZ-123-45-10</code>).</dd>
                </dl> 
            </div>
        </div>
        <h3>${resource.resourceType.label} Specific or Agency Identifiers</h3>
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

</#macro>

<#macro displayAnnotation annotation annotation_index=0>
    <tr id="resourceAnnotationRow_${annotation_index}_">
        <td style="width:50%">
            <@s.textfield cssClass="annotationAutoComplete" name='resourceAnnotations[${annotation_index}].resourceAnnotationKey.key' value='${annotation.resourceAnnotationKey.key!""}'  />
        </td>
       <td style="width:50%">
            <@s.textfield name='resourceAnnotations[${annotation_index}].value'  value='${annotation.value!""}' />
        </td>
        <td><@clearDeleteButton id="resourceAnnotationRow" /></td>                        
    </tr>

</#macro>
<#macro join sequence delimiter=",">
    <#list sequence as item>
        ${item}<#if item_has_next>${delimiter}</#if><#t>
    </#list>
</#macro>

<#-- 
FIXME: this appears to only be used for Datasets.  Most of it has been extracted out
to singleFileUpload, continue lifting useful logic here into singleFileUpload (e.g.,
jquery validation hooks?)
-->
<#macro upload uploadLabel="File" showMultiple=false divTitle="Upload File" showAccess=true>
    <@sharedUploadFile>
        <#if validFileExtensions??>
        <script type='text/javascript'>
            $(function() {
                var validate = $('.validateFileType');
                if ($(validate).length > 0) {
                    $(validate).rules("add", {
                        accept: "<@join sequence=validFileExtensions delimiter="|"/>",
                        messages: {
                            accept: "Please enter a valid file (<@join sequence=validFileExtensions delimiter=", "/>)"
                        }
                    });
                }
            });
        </script>
        </#if>
    <@singleFileUpload>
        <div class="field indentFull">
        <@s.checkbox name="confidential" id="cbConfidential" labelposition="right" label="This item contains confidential information" /> 
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
        <div class='file-upload ui-widget' id=${divId}>
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
        <div tooltipfor="${divId}" class="hidden">
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

<#macro fileProxyRow rowId="{ID}" filename="{FILENAME}" filesize="{FILESIZE}" action="ADD" fileid=-1 confidential_=false versionId=-1>
<tr id="fileProxy_${rowId}" class="${(fileid == -1)?string('newrow', '')}">
<td>
    <div class="width99percent">
            <#if fileid == -1>
                <b class="filename replacefilename">${filename}</b> 
            <#else>
                <b>Existing file:</b> <a class='filename' href="<@s.url value='/filestore/${versionId?c}/get' />">${filename}</a>
            </#if>
    
        <span style='font-size: 0.9em;'>(${filesize} bytes)</span>

        <input type="hidden" class="fileAction" name="fileProxies[${rowId}].action" value="${action}"/>
        <input type="hidden" class="fileId" name="fileProxies[${rowId}].fileId" value="${fileid?c}"/>
        <input type="hidden" class="fileReplaceName" name="fileProxies[${rowId}].filename" value="${filename}"/>
        <#-- FIXME: this may not be necessary, if we just use the ordering of the
        FileProxies list as the implicit ordering (i.e., fileProxies[0] is the first, [1] is the second, etc.) - this would require that we renumber 
        these things as we enter them.
        <input type="hidden" class="fileSequenceNumber" name="fileProxies[${rowId}].sequenceNumber" value="${rowId}"/>
        -->
    </div>
    <div class="width99percent field proxyConfidentialDiv">
        <@s.checkbox id="proxy${rowId}_conf" name="fileProxies[${rowId}].confidential" style="padding-left: 20px;" 
        onclick="updateFileAction('#fileProxy_${rowId}', 'MODIFY_METADATA');showAccessRightsLinkIfNeeded();" cssClass="fileProxyConfidential"/>
        <label for="proxy${rowId}_conf">Confidential</label>
    </div>
    <#nested />
</td>
<td>
    <button type="button" id='deleteFile_${rowId}' onclick="deleteAsyncFileRow('#fileProxy_${rowId}', ${(fileid == -1)?string}, this);return false;" 
    class="deleteButton file-button cancel ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary" role="button">
    <span class="ui-button-icon-primary ui-icon ui-icon-cancel"></span><span class="ui-button-text">delete</span></button><br/>
    <#if fileid != -1>
    <button type="button" onclick="replaceDialog('#fileProxy_${rowId}','${filename}');return false;" 
    class="replaceButton file-button cancel ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary" role="button">
    <span class="ui-button-icon-primary ui-icon "></span><span class="ui-button-text">replace</span></button>
    </#if>
</td>
</tr>
</#macro>

<#macro sharedFormComponents showInherited=true>

    <@edit.identifiers showInherited />
    
    <@edit.keywords showInherited />
    
    <@edit.temporalContext showInherited />
    
    <@edit.spatialContext showInherited />
    
    <@edit.resourceProvider showInherited />
    
    <@edit.resourceCreators 'Individual and Institutional Roles' creditProxies 'credit' false showInherited />
    
    <#if resource.resourceType.label?lower_case  != 'document'>
      <@edit.relatedCollections showInherited />
    </#if>
    <@edit.resourceNoteSection />
    
    <#if resource.resourceType.label?lower_case != 'project'>
      <@edit.accessRights />
      <@edit.submit fileReminder=(resource.id == -1) />
    <#else>
      <@edit.fullAccessRights />
      <@edit.submit fileReminder=false />
    
    </#if>

</#macro>

<#macro title>
<#if resource.id == -1>
<title>Register a New ${resource.resourceType.label} With tDAR</title>
<#else>
<title>Editing ${resource.resourceType.label} Metadata for ${resource.title} (tDAR id: ${resource.id?c})</title>
</#if>
</#macro>

<#macro sidebar>
<div id="sidebar" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the page editing form for a project.
    </div>
</div>
</#macro>


<#macro inheritTips id>
    <div id="${id}hint" class="inherit-tips">
        <em>Note: Ths section supports <strong>inheritance</strong>.  Values in this section can be re-used by resources associated with your project.</em>
    </div>
</#macro>
