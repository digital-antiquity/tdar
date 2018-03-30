<#import "../email-macro.ftl" as mail /> 
<@mail.content>
Dear ${submitter.properName},
<br><br>
The following files were marked as "embargoed" in ${siteAcronym}. That embargo has expired and
the embargo has been removed.  All ${siteAcronym} users can now download it.

<ul>
<#list files as file>
	<li> <a href="${baseUrl}${file.informationResource.detailUrl}">${file.filename}:  ${file.informationResource.title} (${file.informationResource.id?c})</a></li>
</#list>
</ul>
</@mail.content>