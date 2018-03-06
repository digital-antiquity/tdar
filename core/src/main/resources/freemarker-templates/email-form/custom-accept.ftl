<#import "../email-macro.ftl" as mail /> 


<@mail.content>
Dear ${requestor.properName},<br />
<br />
Thanks for your interest in the ${customName}.  You have been granted access
to the requested tDAR record.<br />
<br />
 <a href="${baseUrl}${resource.detailUrl}">${resource.title} (${resource.id?c})</a>.<br />  
 <br />
${descriptionResponse!"To edit your abstract and upload your paper, poster, or presentation please visit the following URL"}:<br />
<br />
<a href="${baseUrl}/${resource.urlNamespace}/${resource.id?c}/edit">Edit resource</a><br />
<br />
<br />
Kind regards,<br />
<br />
Staff at ${serviceProvider}<br />
<br />
<#if message?has_content>
${message}
</#if>
<br />
<hr/>
Note: please do not reply to this automated email
</@mail.content>