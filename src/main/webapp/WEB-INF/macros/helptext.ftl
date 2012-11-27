<#escape _untrusted as _untrusted?html>

<#macro status>
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
</#macro>

<#macro inheritance>
    <div id="divSelectAllInheritanceTooltipContent" style="display:none"> 
    Projects in ${siteAcronym} can contain a variety of different information resources and used to organize a set of related information resources such as documents, datasets, coding sheets, and images. A project's child resources can either inherit or override the metadata entered at this project level. For instance, if you enter the keywords "southwest" and "pueblo" on a project, resources associated with this project that choose to inherit those keywords will also be discovered by searches for the keywords "southwest" and "pueblo". Child resources that override those keywords would not be associated with those keywords (only as long as the overriden keywords are different of course). 
    </div>
</#macro>


<#macro resourceCollection>
    <div style="display:none" id="divResourceCollectionListTips">
        <p>
            Collections enable you to organize and share resources within ${siteAcronym}.
        </p>
        <p>
            To associate this resource with a collection, specify the names of the collections that ${siteAcronym} should add this resource to.  Alternately you can start a new, <em>public</em>  collection 
            by typing the desired name and selecting the last option in the list of pop-up results.  The newly-created collection will contain only this 
            resource, but can be modified at any time. 
        </p>
    </div>
</#macro>

<#macro geo>
    <div style="display:none" id="geoHelpDiv">
	Identify the approximate region of this resource by clicking on &quot;Select Region&quot; and drawing a bounding box on the map.
	                <br/>Note: to protect site security, ${siteAcronym} obfuscates all bounding boxes, bounding boxes smaller than 1 mile, especially.  This 'edit' view 
	                will always show the exact coordinates.
	</div>
</#macro>

<#macro manualGeo>
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
</#macro>

<#macro confidentialFile>
  <div id="divConfidentialAccessReminder" class="hidden">
      <em>Embargoed records will become public in ${embargoPeriodInYears} years. Confidential records will not be made public. Use the &quot;Access Rights&quot; section to assign access to this file for specific users.</em>
  </div>
</#macro>

<#macro siteName>
        <div class="hidden" id="siteinfohelp">
            Keyword list: Enter site name(s) and select feature types (<a href="${siteTypesHelpUrl}">view complete list</a>) 
            discussed in the document. Use the Other field if needed.
        </div>

</#macro>

<#macro materialType>
    <div class="hidden" id="materialtypehelp">
        Keyword list: Select the artifact types discussed in the document.<a href="${materialTypesHelpUrl}">view all material types</a>
    </div>
</#macro>

<#macro cultureTerms>
    <div id="culturehelp" class="hidden">
        Keyword list: Select the archaeological &quot;cultures&quot; discussed in the document. Use the Other field if needed. 
        <a href="${culturalTermsHelpUrl}">view all controlled terms</a>
    </div>

</#macro>

<#macro investigationType>
<div class="hidden" id="investigationtypehelp">Keyword list: Select the investigation types relevant to the document.<a href="${investigationTypesHelpUrl}">
view all investigation types</a></div>
</#macro>

<#macro accessRights>
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
</#macro>

<#macro sourceRelatedCollection >
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

</#macro>

<#macro coverageDates>
<div class="hidden" id="coverageDatesTip">
    Select the approriate type of date (Gregorian calendar date or radiocarbon date). To enter a date range, enter the <em>earliest date</em> in the <em>Start Year field</em> 
    and the latest date in the End Year Field. <em>Dates containing "AD" or "BC" are not valid</em>. Use positive numbers for AD dates (500, 1200), and use negative numbers for BC dates (-500, -1200). Examples: 
    <ul>
        <li>Calendar dates: 300 start, 500 end (number only, smaller value first)</li>
        <li>Radiocarbon dates: 500 start, 300 end (number only, larger value first)</li>     
    </ul>
</div>

</#macro>

<#macro identifiers>
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
</#macro>


</#escape>