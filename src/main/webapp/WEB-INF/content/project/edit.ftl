<#escape _untrusted as _untrusted?html>

<#import "/${themeDir}/local-helptext.ftl" as  helptext>
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
<@edit.sidebar />
<@edit.subNavMenu />
<@edit.resourceTitle />


<@helptext.projectInheritance />



<#if (totalRecords > 0) >
<div class='glide'>
	<h3>There are ${totalRecords?c} Resources within this Project</h3>
	<a class="field" href="<@s.url value="/project/${project.id?c}" />">&raquo; view all items in this project</a>
</div>
</#if>


<@s.form name='projectMetadataForm' id='projectMetadataForm'  cssClass="form-horizontal" method='post' action='save'>

<@edit.basicInformation "project" "project" />
<@edit.citationInfo "project" />

<@s.select labelposition='top' label='When Browsing Sort Resource By' name='project.sortBy' 
     listValue='label' list='%{sortOptions}' title="Sort resource by" />

<@s.select labelposition='top' label='Display Project contents as' name='project.orientation' 
     list='%{ResultsOrientations}'  listValue='label'  title="Display as" />


<@edit.sharedFormComponents showInherited=false />


</@s.form>


<@edit.resourceJavascript formSelector="#projectMetadataForm" selPrefix="#project">
    $(function(){
        $("#collapse").click(toggleDiv);
    });
</@edit.resourceJavascript>


</body>
</#escape>