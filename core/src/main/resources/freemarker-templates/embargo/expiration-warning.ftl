<#import "../email-macro.ftl" as mail /> 
<@mail.content>
Dear ${submitter.properName},
<br><br>
The following files are marked as "embargoed" in ${siteAcronym}. That embargo will expire soon.
At that time, the embargo will be automatically removed.
  
<ul>
<#list files as file>
	<li><a href="${baseUrl}${file.informationResource.detailUrl}"> ${file.filename}:  ${file.informationResource.title} (${file.informationResource.id?c})</a></li>
</#list>
</ul>
</@mail.content>