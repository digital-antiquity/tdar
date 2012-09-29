<#escape _untrusted as _untrusted?html>
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
Projects in tDAR contain and help organize a variety of different information resources such as documents,
 datasets, coding sheets, and images. The project also functions as a template to pass shared metadata
  (keywords) to child resources. Child resources may either inherit metadata from the parent project or 
  the child resource may have unique metadata. For instance, if you enter the keywords &quot;Southwest&quot; and 
  &quot;Pueblo&quot; for a project, resources associated with this project that you choose to inherit metadata
   will be discovered in searches including those keywords. Child resources that override those keywords
    would not be associated with keywords defined at the project level.
</p>
</div>

<#if ! project.informationResources.isEmpty() >
<div class='glide'>
<h3>There are ${project.informationResources.size()} Resources within this Project</h3>
<a class="field" href="<@s.url value="/project/${project.id?c}" />">view all items in this project</a>
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
</#escape>