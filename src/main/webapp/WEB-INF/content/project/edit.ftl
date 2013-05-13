<#escape _untrusted as _untrusted?html>
<#global itemPrefix="project"/>
<#global inheritanceEnabled=false />
<#import "/${themeDir}/local-helptext.ftl" as  helptext>

	<#macro topSection>
		<@helptext.projectInheritance />
		
		<#if (totalRecords > 0) >
			<div class='glide'>
				<h3>There are ${totalRecords?c} Resources within this Project</h3>
				<a class="field" href="<@s.url value="/project/${project.id?c}" />">&raquo; view all items in this project</a>
			</div>
		</#if>
		
	</#macro>
	
	<#macro localSection>
		<h4>Organize the Resources Within your Project</h4>
		
		<@s.select labelposition='top' label='When Browsing Sort Resource By' name='project.sortBy' 
		     listValue='label' list='%{sortOptions}' title="Sort resource by" />
		
		<@s.select labelposition='top' label='Display Project contents as' name='project.orientation' 
		     list='%{ResultsOrientations}'  listValue='label'  title="Display as" />
	</#macro>
	
	<#macro localJavascript>
	    $(function(){
	        $("#collapse").click(toggleDiv);
	    });
	</#macro>

</#escape>