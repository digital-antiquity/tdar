<#macro link type title>
    <#if ((projectId!0) > 0)>
    <a href="<@s.url value="/${type}/add" />?projectId=${projectId?c}">${title}</a>
    <#else>
    <a href="<@s.url value="/${type}/add" />">${title}</a>
    </#if>
</#macro>
<head>
    <title>Add a New Resource</title>
</head>
<body>

<div class="row">
    <h1>Create &amp; <span>Organize</span> Resources</h1>

    <div class="col-8">
        <h2>Create</h2>
    </div>
    <div class="col-4">
        <h2>Add</h2>
    </div>
    </div>
        <div class="row">
            <div class="col-4">
                <h3>
                <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_document"></use></svg>
                <@link "document" "Document" /></h3>
                A written, printed record of information, evidence, or analysis. Examples from archaeology include published articles, books, excavation
                reports, field notes, or doctoral dissertations.
            </div>
            <div class="col-4">
                <h3>
                <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_image"></use></svg>
                <@link "image" "Image" /></h3>
                A visual representation of an object or location. Examples from archaeology include photographs (born digital or scanned) of artifacts or sites,
                drawings or figures, and some maps.
            </div>


        <div class="col-4">
            <h3>
                <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_cart"></use></svg>
            <a href="<@s.url value="/cart/add"/>">Add Files or Space</a></h3>
            Create a new account or add files or space to your account.  
        </div>
    </div>
    <div class="row">
        <div class="col-4">
            <h3>
            <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_dataset"></use></svg>            
            <@link "dataset" "Dataset" /></h3>
            A collection of data, usually in tabular form with columns representing variables and rows representing cases. A database usually refers to a
            set of linked or related datasets. Examples from archaeology include small spreadsheets documenting measurements and/or analysis of artifacts,
            as well as large databases cataloging all artifacts from a site.
        </div>
        <div class="col-4">
            <h3><a href="<@s.url value="/bulk/add?projectId=${(projectId!-1)?c}" />">
            <svg class="svgicon svg-small"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_bulk"></use></svg>Bulk Upload</a></h3>
            Useful for uploading groups of resources with similar metadata at once.

        </div>
            <div class="col-4">
            <h2>Organize</h2>
                <h3>
                <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_project"></use></svg>                
                <@link "project" "Project" /></h3>

                In ${siteAcronym}, a project is an organizational tool for working with groups of related resources. Projects in ${siteAcronym}  may contain related reports, photographs, maps,
                 and databases, and those resources may inherit metadata from the parent project.
            </div>
    </div>
        <div class="row">
            <div class="col-4">
                <h3>
                <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_geospatial"></use></svg>
                <@link "geospatial" "Geospatial" /></h3>
                GIS files, shape files, personal geodatabases, and geo-rectified images.
            </div>
            <div class="col-4">
                <h3>
                <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_sensory_data"></use></svg>
                <@link "sensory-data" "Sensory Data / 3D Scan" /></h3>
                Certain images and/or datasets fall under the heading of Sensory Data. 3-D scans, for example.
            </div>
            <div class="col-4">
                <h3>
                <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_collection"></use></svg>
                <@link "collection" "Collection"/></h3>
                In ${siteAcronym}, a collection is an organizational tool with two purposes. The first is to allow contributors and users to create groups and
                hierarchies of resources in any way they find useful. A secondary use of collections allows users to easily administer view and edit
                permissions for large numbers of persons and resources.
            </div>

        </div>

        <#if config.videoEnabled>
        <div class="row">
            <div class="col-4">
                <h3 class="video-mid-red"><@link "video" "Video" /></h3>
                A video
            </div>
            <div class="col-4">
                <h3 class="audio-mid-red"><@link "audio" "Audio" /></h3>
                An audio file
            </div>
        </div>
        </#if>

        <div class="row">
            <div class="col-4">
            <h3>
            <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_ontology"></use></svg>            
            <@link "ontology" "Ontology" /></h3>
                In ${siteAcronym}, an ontology is a small file used with a dataset column (and/or coding sheet) to hierarchically organize values in the data in
                order to facilitate integrating datasets from different sources. (Please see the tutorials on data integration for a complete explanation).
            </div>
            <div class="col-4">
            <h3>
            <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_coding_sheet"></use></svg>            
            <@link "coding-sheet" "Coding Sheet"/></h3>
                A list of codes and their meanings, usually associated with a single column in a dataset. An example from archaeology might be a list of ceramic
                type codes from a particular analysis project, linked to a specific dataset within ${siteAcronym}. A collection of coding sheets make up a
                coding packet, and are part of the proper documentation of a dataset.
            </div>
        </div>

        <#if config.archiveFileEnabled>
        <div class="row">
            <div class="col-4">
            </div>
            <div class="col-4">
            <h3 class="archive-mid-red"><@link "archive" "Site Archive"/></h3>
                The upload of archived data from a ${siteAcronym} compatible field server that has been used in conjunction with mobile devices on a site. <br/>
            </div>
        </div>
        </#if>
    </div>


    </div>
</div>


</body>