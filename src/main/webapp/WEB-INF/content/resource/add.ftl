<#macro link type title>
    <#if (projectId?? && projectId > 0)>
    <a href="<@s.url value="/${type}/add" />?projectId=${projectId?c}">${title}</a>
    <#else>
    <a href="<@s.url value="/${type}/add" />">${title}</a>
    </#if>
</#macro>
<head>
<title>Add a New Resource</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>
    <h1>Create Resources </h1>
    <div class="row">
    
        <div class="span45">
            <h3><@link "document" "Document" /></h3>
            A written, printed record of information, evidence, or analysis. Examples from archaeology include published articles, books, excavation reports, field notes, or doctoral dissertations. 
		</div>
        <div class="span45">
                <h3><@link "image" "Image" /></h3>
                A visual representation of an object or location. Examples from archaeology include photographs (born digital or scanned) of artifacts or sites, drawings or figures, and some maps.
		</div>
	</div>
	<div class="row">
        <div class="span45">
                <h3><@link "dataset" "Dataset" /></h3>
                A collection of data, usually in tabular form with columns representing variables and rows representing cases. A database usually refers to a set of linked or related datasets. Examples from archaeology include small spreadsheets documenting measurements and/or analysis of artifacts, as well as large databases cataloging all artifacts from a site. 
		</div>
        <div class="span45">
                <h3><@link "sensory-data" "Sensory Data or 3D Laser Scan" /></h3>
                Certain images and/or datasets fall under the heading of Sensory Data. 3-D scans, for example. 
		</div>
	</div>
     <#if administrator>
	<div class="row">
        <div class="span45">
            <h3><@link "video" "Video" /></h3>
            A video
		</div>
	</div>
    </#if>
	<div class="row">
        <div class="span45">
                <h3><@link "ontology" "Ontology"/></h3>
                In ${siteAcronym}, an ontology is a small file used with a dataset column (and/or coding sheet) to hierarchically organize values in the data in order to facilitate integrating datasets from different sources. (Please see the tutorials on data integration for a complete explanation).
		</div>
        <div class="span45">
                <h3><@link "coding-sheet" "Coding Sheet"/></h3>
                A list of codes and their meanings, usually associated with a single column in a dataset. An example from archaeology might be a list of ceramic type codes from a particular analysis project, linked to a specific dataset within ${siteAcronym}. A collection of coding sheets make up a coding packet, and are part of the proper documentation of a dataset.  
		</div>
	</div>
                            

<#if (projectId!-1) == -1>
    <h2>Organize Resources </h2>
	<div class="row">
        <div class="span45">
                <h3><@link "project" "Project" /></h3>
                
                In ${siteAcronym}, a project is an organizational tool for working with groups of related resources. Projects in ${siteAcronym} are flexible and can be used 
                in different ways, but it is useful as a starting point to think of a ${siteAcronym} project as a digital archive for materials generated by an 
                archaeological research project. It may contain related reports, photographs, maps, and databases, and those resources may inherit metadata 
                from the parent project. 
		</div>
        <div class="span45">
                <h3><@link "collection" "Collection"/></h3>
                In ${siteAcronym}, a collection is an organizational tool with two purposes. The first is to allow contributors and users to create groups and 
                hierarchies of resources in any way they find useful. A secondary use of collections allows users to easily administer view and edit 
                permissions for large numbers of persons and resources.
		</div>

</div>
</#if>



</body>