<#import "../email-macro.ftl" as mail /> 
<@mail.content>
Dear tDAR Admin:<br />
<#list toExpire![]>
The following files will be un-embargoed tomorrow:
<#items as file>
 - ${file.filename} (${file.id?c}):  ${file.informationResource.title} (${file.informationResource.id?c})
   ${baseUrl}${file.informationResource.detailUrl} (${file.informationResource.submitter.properName})
</#items></#list>

<#list expired![]>
The following files have been unembargoed:
<#items as file>
 - ${file.filename} (${file.id?c}):  ${file.informationResource.title} (${file.informationResource.id?c})
   ${baseUrl}${file.informationResource.detailUrl} (${file.informationResource.submitter.properName})
</#items></#list>
</@mail.content>