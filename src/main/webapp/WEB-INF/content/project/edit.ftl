<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#assign hideInherited=true > <#-- //TODO: remove hideInherited if safe-->

<head>
<@edit.title />

<style type="text/css">
#projectTitle {width:95% !important}
</style>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>
<@edit.subNavMenu />
<@edit.sidebar />
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
        If you change any metadata values at the project level, ${siteAcronym} will update those "inherited" values at the resource level. 

For example, if you change "Investigation Types" for your project, any resource that inherited "Investigation Types" from that project will be automatically updated.
        </dd>

        </dl>
    </div>
</div>


<div class="glide">
<h3>${siteAcronym} project metadata</h3>
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

<#if (totalRecords > 0) >
<div class='glide'>
<h3>There are ${totalRecords?c} Resources within this Project</h3>
<a class="field" href="<@s.url value="/project/${project.id?c}" />">view all items in this project</a>
</div>
</#if>


<@s.form name='projectMetadataForm' id='projectMetadataForm'  cssClass="form-horizontal" method='post' action='save'>

<@edit.basicInformation "project" "project" />
<@edit.citationInfo "project" />


<@edit.sharedFormComponents showInherited=false />


</@s.form>


<@edit.resourceJavascript formSelector="#projectMetadataForm" selPrefix="#project">
    $(function(){
        $("#collapse").click(toggleDiv);
    });
</@edit.resourceJavascript>


</body>
</#escape>