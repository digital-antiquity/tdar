Dear tDAR Admin:
The following files will be un-embargoed tomorrow:
<#if toExpire?has_content><#list toExpire as file>
 - ${file.filename} (${file.id}):  ${file.informationResource.title} (${file.informationResource.id?c})
   ${baseUrl}${file.informationResource.detailUrl} (${file.informationResource.submitter.properName})

</#list></#if>

The following files have been unembargoed:
<#if expired?has_content><#list expired as file>
 - ${file.filename} (${file.id}):  ${file.informationResource.title} (${file.informationResource.id?c})
   ${baseUrl}${file.informationResource.detailUrl} (${file.informationResource.submitter.properName})
   
</#list></#if>
