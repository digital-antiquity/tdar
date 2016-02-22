Dear ${submitter.properName},

The following files were marked as "embargoed" in ${siteAcronym}. That embargo has expired and
the embargo has been removed.  All ${siteAcronym} users can now download it.

<#list files as file>
	- ${file.filename}:  ${file.informationResource.title} (${file.informationResource.id?c}) - ${baseUrl}${file.informationResource.detailUrl}
</#list>