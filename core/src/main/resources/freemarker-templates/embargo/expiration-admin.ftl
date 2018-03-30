<#import "../email-macro.ftl" as mail /> 
<@mail.content>
Dear tDAR Admin:<br />
<#list toExpire![]>

<p><b>The following files will be un-embargoed tomorrow:</b></p>
<ul>
<#items as file>
 <li><a href="${baseUrl}${file.informationResource.detailUrl}">${file.filename} (${file.id?c}):  ${file.informationResource.title} (${file.informationResource.id?c})</a>
    (${file.informationResource.submitter.properName})</li>
</#items>
</ul>

</#list>

<#list expired![]>
<p><b>The following files have been unembargoed:</b></p>
<ul>
<#items as file>
 <li><a href="${baseUrl}${file.informationResource.detailUrl}"> ${file.filename} (${file.id?c}):  ${file.informationResource.title} (${file.informationResource.id?c})
    (${file.informationResource.submitter.properName})</a></li>
</#items>
</ul>
</#list>
</@mail.content>