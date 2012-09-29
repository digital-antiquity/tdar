<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#assign hideInherited=true > <#-- //TODO: remove hideInherited if safe-->

<head>
<#if project.id == -1>
<title>Register a new project with tDAR</title>
<#else>
<title>Editing metadata for ${project.title} (tDAR id: ${project.id?c})</title>
</#if>
<style type="text/css">
#projectTitle {width:95% !important}
</style>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>
<@edit.toolbar "${resource.urlNamespace}" "edit" />

<div tooltipfor="cbInheritingInvestigationInformationhint,cbInheritingSiteInformationhint,cbInheritingMaterialInformationhint,cbInheritingCulturalInformationhint,cbInheritingTemporalInformationhint,cbInheritingOtherInformationhint,cbInheritingSpatialInformationhint" class="hidden">
    <h2>&quot;Inheriting&quot; Project Metadata</h2>
    <div>
        <dl>
        <dt>About</dt>
        <dd>
            For certain sections, you can re-use information to simplify metadata entry for resources you want to associate with your project. 
        </dd>
        
        <dt>What if I change values in my project?</dt>
        <dd>
        If you change any metadata values at the project level, tDAR will update those "inherited" values at the resource level. 

For example, if you change "Investigation Types" for your project, any resource that inherited "Investigation Types" from that project will be automatically updated.
        </dd>

        </dl>
    </div>
</div>


<div class="glide">
<h3>tDAR project metadata</h3>
<p>
Projects in tDAR can contain a variety of different information resources and are used
to organize a set of related information resources such as documents, datasets,
coding sheets, and images.  A project's child resources can either <b>inherit or
override</b> the metadata entered at this project level. For instance, if you enter
the keywords "southwest" and "pueblo" on a project, resources associated with this
project that choose to <b>inherit</b> those keywords will also be discovered by
searches for the keywords "southwest" and "pueblo." Child resources that
<b>override</b> those keywords would not be associated with keywords defined at the project level.
</p>
</div>

<#if ! project.informationResources.isEmpty() >
<div class='glide'>
<h3 id="collapse"><span class="arrow ui-icon ui-icon-triangle-1-e"></span>Resources within this Project</h3>
<div style="display:none">
      <@rlist.informationResources iterable="project.documents" title="Documents" displayable=!project.documents.empty editable=editable showProject=false/>
      <@rlist.informationResources iterable="project.images" title="Images" displayable=!project.images.empty editable=editable showProject=false/>
      <@rlist.informationResources iterable="project.datasets" title="Datasets" displayable=!project.datasets.empty editable=editable showProject=false/>
      <@rlist.informationResources iterable="project.codingSheets" title="Coding sheets" displayable=!project.codingSheets.empty editable=editable showProject=false/>
      <@rlist.informationResources iterable="project.ontologies" title="Ontologies" displayable=!project.ontologies.empty editable=editable showProject=false/>
</div>
</div>
</#if>


<@s.form name='projectMetadataForm' id='projectMetadataForm' method='post' action='save'>
<@edit.showControllerErrors/>

<@edit.basicInformation>
<div tiplabel="Title" tooltipcontent="Name for this project.">
<@s.textfield labelposition='left' id='resourceTitle' label='Title' required='true' name='project.title' size="75" 
    cssClass="required descriptiveTitle longfield"  title="A title is required for all documents." maxlength="512" />
</div>
<div tiplabel="Description" tooltipcontent="Short description of the resource. Often comes from the resource itself, but sometimes will include additional information from the contributor.">
    <p id='t-description' class='new-group'>
        <@s.textarea id='projectDescription' labelposition='top' label='Description' name='project.description' rows=5 cssClass='resizable tdartext required' required="true"
        title="A basic description is required for all projects" />
    </p>
</div>
</@edit.basicInformation>


<@edit.sharedFormComponents showInherited=false />


</@s.form>

<@edit.sidebar />

<@edit.resourceJavascript formId="#projectMetadataForm" selPrefix="#project">
 $("#collapse").click(toggleDiv);
</@edit.resourceJavascript>


</body>
