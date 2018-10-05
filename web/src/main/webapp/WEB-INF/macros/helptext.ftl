<#escape _untrusted as _untrusted?html>

    <#macro info title="title" contentDiv="" content="">
    	<#local text=contentDiv />
    	<#if content?has_content>
    	<#local text=content />
    	</#if>
     <a tabindex="0" class="moreinfo" role="button" data-toggle="popover" data-trigger="focus" title="${title}" data-popover-content="${text}" ><i class="fas fa-info-circle"></i></a> 
    </#macro>

    <#macro notes>
    <div id="notesHelpDiv" style="display:none">
    Use this section to append any notes that may help clarify certain aspects of the resource.  For example,
    a &quot;Redaction Note&quot; may be added to describe the rationale for certain redactions in a document.
    </div>
    </#macro>
    <#macro asyncUpload divId="" validFileExtensions=[] multipleFileUploadEnabled=true maxUploadFilesPerRecord=50 canReplace=false siteAcronym="tDAR">
    <div id="${divId}Help" style="display:none">
        <div>
            <h3>Adding Files</h3>
            <ul>
                <li>To attach files to this resource, click the button labeled "Add Files..."</li>
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

            <#if canReplace>
                <h3>Replacing Files</h3>
                <ol>
                    <li>Locate the row (or file tab) that corresponds to the file you would like to replace.</li>
                    <li>In that row, click on the button labeled "Replace". tDAR will prompt you for a new file.</li>
                    <li>Once the upload is complete, you must save the form to confirm your changes.Click on the "Save" button in the upper right hand portion
                        of the screen.
                    </li>
                    <li>To undo this action and restore the original file, simply click the button again (which will now be labeled "Restore Original").</li>
                </ol>

            </#if>

            <h3>Deleting Files</h3>
            You can remove files by clicking on the button labeled "Delete". You can restore the file by clicking the button a
            second time (the button will now be labeled "Undelete").


            <h3>File Information</h3>
            <dl>
                <dt>Description</dt>
                <dd>
                    Additional information specific to this file.
                </dd>
            </dl>
        </div>
    </div>
    </#macro>


    <#macro projectInheritance>
    <div tooltipfor="cbInheritingInvestigationInformationhint,cbInheritingSiteInformationhint,cbInheritingMaterialInformationhint,cbInheritingCulturalInformationhint,cbInheritingTemporalInformationhint,cbInheritingOtherInformationhint,cbInheritingSpatialInformationhint"
         class="hidden">

        <div>
            <dl>
                <dt>About</dt>
                <dd>
                    For certain sections, you can re-use information to simplify metadata entry for resources you want to associate with your project.
                </dd>

                <dt>What if I change values in my project?</dt>
                <dd>
                    If you change any metadata values at the project level, ${siteAcronym} will update those "inherited" values at the resource level.

                    For example, if you change "Investigation Types" for your project, any resource that inherited "Investigation Types" from that project will
                    be automatically updated.
                </dd>

            </dl>
        </div>
    </div>

    <div class="glide">

        <p>
            Projects in ${siteAcronym} contain and help organize a variety of different information resources such as documents,
            datasets, coding sheets, and images. The project also functions as a template to pass shared metadata
            (keywords) to child resources. Child resources may either inherit metadata from the parent project or
            the child resource may have unique metadata. For instance, if you enter the keywords &quot;Southwest&quot; and
            &quot;Pueblo&quot; for a project, resources associated with this project that you choose to inherit metadata
            will be discovered in searches including those keywords. Child resources that override those keywords
            would not be associated with keywords defined at the project level.
        </p>
    </div>


    </#macro>

    <#macro status>
    <div id="spanStatusToolTip" class="hidden">
        Indicates the stage of a resource's lifecycle and how ${siteAcronym} treats its content.
        <dl>
            <dt>Draft</dt>
            <dd>The resource is under construction and/or incomplete</dd>
            <dt>Active</dt>
            <dd>The resource is considered to be complete.</dd>
            <dt>Flagged</dt>
            <dd>This resource has been flagged for deletion or requires attention</dd>
            <dt>Deleted</dt>
            <dd>The item has been 'deleted' from ${siteAcronym} workspaces and search results, and is considered deprecated.</dd>
        </dl>
    </div>
    </#macro>

	<#macro columninfo>
    <div style="display:none" id="columnInfo">
    
    <span class="hidden" id="generalToolTip">
         Each "column" subform shown on the table metadata page represents a column in the dataset, and provides fields to describe the data in that column. This is important documentation for researchers that wish to use the dataset, and where relevant the form links to coding sheets and ${siteAcronym}
        ontologies to faciliate research.
    </span>
    <span class="hidden" id="columnTypeToolTip">
        Select the option that best describes the data in this column. The form will display fields relevant to your selection. <br/>
        <b>Note:</b> measurement and count cannot be selected for fields that have any non-numerical data.
    </span>
    <span class="hidden" id="displayNameToolTip">
        If needed, edit the name displayed for the column to help users understand the column's contents.
    </span>
    <span class="hidden" id="categoryVariableToolTip"> 
        Select the category and subcategory that best describes the data in this column.
    </span>
    <span class="hidden" id="descriptionToolTip">
        Add any notes that would help a researcher understand the data in the column. 
    </span>
    <span class="hidden" id="codingSheetToolTip">
        If the data in this column is coded and the right coding sheet has been added to ${siteAcronym}, please select a coding sheet that translates and explains the codes. 
    </span>
    <span class="hidden" id="ontologyToolTip">
        If you would like to link this column to a ${siteAcronym} ontology, make that selection here. This is important if you (or other researchers) intend to integrate this dataset with other datasets using the ${siteAcronym}
        data integration tool.
    </span>
            </div>

	</#macro>
    <#macro basicHelp>
    <div style="display:none" id="basicHelp">
            <h5>Status</h5>
        Indicates the stage of a resource's lifecycle and how ${siteAcronym} treats its content.
        <dl>
            <dt>Draft</dt>
            <dd>The resource is under construction and/or incomplete</dd>
            <dt>Active</dt>
            <dd>The resource is considered to be complete.</dd>
            <dt>Flagged</dt>
            <dd>This resource has been flagged for deletion or requires attention</dd>
            <dt>Deleted</dt>
            <dd>The item has been 'deleted' from ${siteAcronym} workspaces and search results, and is considered deprecated.</dd>
        </dl>
         <h5>Title</h5>
         If a formal title is given for the resource (as with a report) use this. If no title is supplied, the suggested formula is 'Content, Investigation Type or Site Name, Site Name or Specific Geographic Location'.
         <h5>Year</h5>
         Four digit year, e.g. 1966 or 2005. The publication year for a document, or the year a photograph was taken. Otherwise, the year the resource was created.
         </div>
    </#macro>

    <#macro inheritance>
    <div id="divSelectAllInheritanceTooltipContent" style="display:none">
        Projects in ${siteAcronym} can contain a variety of different information resources and used to organize a set of related information resources such as
        documents, datasets, coding sheets, and images. A project's child resources can either inherit or override the metadata entered at this project level.
        For instance, if you enter the keywords "southwest" and "pueblo" on a project, resources associated with this project that choose to inherit those
        keywords will also be discovered by searches for the keywords "southwest" and "pueblo". Child resources that override those keywords would not be
        associated with those keywords (only as long as the overriden keywords are different of course).
    </div>
    </#macro>


    <#macro resourceCollection>
    <div style="display:none" id="divResourceCollectionListTips">
        <p>
            Collections enable you to organize and share resources within ${siteAcronym}.
        </p>

        <p>
            To associate this resource with a collection, specify the names of the collections that ${siteAcronym} should add this resource to. Alternately you
            can start a new, <em>public</em> collection
            by typing the desired name and selecting the last option in the list of pop-up results. The newly-created collection will contain only this
            resource, but can be modified at any time.
        </p>
    </div>
    </#macro>

    <#macro geo>
    <div style="display:none" id="geoHelpDiv">
        Identify the approximate region of this resource by clicking on &quot;Select Region&quot; and drawing a bounding box on the map.
        <br/>Note: to protect site security, ${siteAcronym} obfuscates all bounding boxes, bounding boxes smaller than 1 mile, especially. This 'edit' view will
        always show the exact coordinates.
    </div>
    </#macro>

    <#macro manualGeo>
    <div id="divManualCoordinateEntryTip" class="hidden">

        <div>
            Click the Locate button after entering the longitude-latitude pairs in the respective input fields to draw a box on the map and zoom to it.
            <br/>Examples:
            <ul>
                <li>40&deg;44'55"N</li>
                <li>53 08 50N</li>
                <li>-73.9864</li>
            </ul>
            <aside><p><strong>Note:</strong> to protect site security, ${siteAcronym} obfuscates all bounding boxes, bounding boxes smaller than 1 mile. This
                'edit' view will
                always show the exact coordinates.</p></aside>
        </div>
    </div>
    </#macro>

    <#macro confidentialFile>
    <div id="divConfidentialAccessReminder" class="hidden">
        <em>Embargoed records will become public in ${embargoPeriodInYears} years. Confidential records will not be made public. Use the &quot;Access Rights&quot;
            section to assign access to this file for specific users. All metadata is public regardless of whether the file is marked as confidential or
            embargoed.</em>
    </div>
    </#macro>

    <#macro siteName>
    <div class="hidden" id="siteinfohelp">
        Keyword list: Enter site names and select (<a target="_blank" title="click to open view the complete list in a new window" href="${config.siteTypesHelpUrl}">feature
        types</a>)
        discussed in the document. Use the <em>Other</em> field if necessary.
    </div>

    </#macro>

    <#macro materialType>
    <div class="hidden" id="materialtypehelp">
        Keyword list: Select the artifact types discussed in the document.<a href="${config.materialTypesHelpUrl}">view all material types</a>
    </div>
    </#macro>

    <#macro cultureTerms>
    <div id="culturehelp" class="hidden">
        Keyword list: Select the archaeological &quot;${culturalTermsLabel!"cultures"}&quot; discussed in the document. Use the Other field if needed.
        <a href="${config.culturalTermsHelpUrl}">view all controlled terms</a>
    </div>

    </#macro>

    <#macro investigationType>
    <div class="hidden" id="investigationtypehelp">Keyword list: Select the investigation types relevant to the document.<a href="${config.investigationTypesHelpUrl}">
        view all investigation types</a></div>
    </#macro>

    <#macro accessRights>
    <div id="divAccessRightsTips" style="display:none">
        <p>Access rights determine the actions that a user may perform on a resource. Enter the first few letters of the person's name, email, or institution.
            The form will check for matches in the ${siteAcronym} database and populate the related fields.</p>
        <strong>Types of Permissions</strong>
        <dl>
            <dt>View and Download</dt>
            <dd>User can view/download all file attachments.</dd>
            <dt>Modify Metadata</dt>
            <dd>User can edit this resource metadata but <em>cannot</em> add, remove, or modify file attachments.</dd>
            <dt>Modify Files & Metadata</dt>
            <dd>User has full edit rights to the resource, including file attachments.</dd>
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
        Select the appropriate type of date (Gregorian calendar date or radiocarbon date). To enter a date range, enter the <em>earliest date</em> in the <em>Start
        Year field</em>
        and the latest date in the End Year Field. <em>Dates containing "AD" or "BC" are not valid</em>. Use positive numbers for AD dates (500, 1200), and use
        negative numbers for BC dates (-500, -1200). Examples:
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
                <dt>Name
                <dt>
                <dd>Description of the following agency or ${resource.resourceType.label} identifier (e.g. <code>Accession Number</code> or <code>TNF Project
                    Code</code>).
                </dd>
                <dt>Value
                <dt>
                <dd>Number, code, or other identifier (e.g. <code>2011.045.335</code> or <code>AZ-123-45-10</code>).</dd>
            </dl>
        </div>
    </div>
    </#macro>

    <#macro resourceCreator>
    <div id="divResourceCreatorsTip" class="hidden">
        Use these fields to properly credit individuals and institutions for their contribution to the resource.<br/>
        <dl>
            <dt>Type</dt>
            <dd>Use the toggle at the left to select whether you're adding a Person or Institution</dd>
            <dt>Roles</dt>
            <dd>View <a href="${config.resourceCreatorRoleDocumentationUrl}">All Roles</a></dd>
        </dl>
    </div>
    </#macro>

    <#macro copyrightHoldersTip>
    <div id="divCopyrightHolderTip" class="hidden">
        Use this field to nominate a primary copyright holder. Other information about copyright can be added in the 'notes' section by creating a new 'Rights &
        Attribution note.
    </div>
    </#macro>


    <#macro srid>
    <div id="sridTip" class="hidden">
        Please enter a Spatial Reference System ID, datum and projection. e.g.:
        <ul>
            <li>EPSG:3857</li>
            <li>WGS:84</li>
            <li> NAD83 StatePlane Iowa N</li>
            <li> NAD83(HARN)/UTM Zone 16 N</li>
        </ul>
    </div>
    </#macro>

    <#macro unpackArchiveTip>
    <div id="divUnpackArchiveTip" class="hidden">
        If checked, a process will run sometime after the saving of the archive, that will unpack the contents of the archive, and then import them as resources
        in
        the parent project.<br/>
        <strong>NB:</strong> This is a process that can only be run once!
    </div>
    </#macro>

    <#macro audioSoftwareTip>
    <div id="divAudioSoftwareTip" class="hidden">
        This is the name of the application or program that created the audio file that is being uploaded.<br/>
    </div>
    </#macro>

    <#macro showExactLocationTip>
    <div id="showExactLocationHelpDiv" class="hidden">
        <b>Beware:</b> if this box is checked, then everyone will be able to see the exact location that you enter on the map.<br/>
        <b>Leave unchecked</b> if you don't want the world to see the location. If in doubt, leave unchecked!
    </div>
    </#macro>

<#--
  emit the  joined string values of a collection, same as
    <#list mylist as item>${item}<#if item_has_next>,</#if></#list>
-->
    <#macro join sequence=[] delimiter=",">
        <#if sequence?has_content>
            <#list sequence as item>
            ${item}<#if item_has_next><#noescape>${delimiter}</#noescape></#if><#t>
            </#list>
        </#if>
    </#macro>

    <#macro userAgreementSummary>
    which, in summary,  states that you <em>(1)</em> will not use any of the information that you obtain from ${siteAcronym} in
    a way that would damage the archaeological resources; and, <em>(2)</em> will give credit to the individual(s) or organization 
    that created the information that you download.
    </#macro>
    <#macro helpBelow text="">
        <div class="row" style="margin-top:-1em;margin-bottom:1em;">
        <div class="col-10 offset-2"><span class="form-text text-muted">${text}</span>
        </div>
        </div>
    </#macro>
</#escape>
