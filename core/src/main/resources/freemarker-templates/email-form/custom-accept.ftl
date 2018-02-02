<#import "../email-macro.ftl" as mail /> 


<@mail.content>
Dear ${requestor.properName},<br />
<br />
Thanks for your interest in the ${customName}.  You have been granted access
to the requested tDAR record.<br />
<br />
 ${resource.title} (${resource.id?c}).<br />  
 <br />
${descriptionResponse!"To edit your abstract and upload your paper, poster, or presentation please visit the following URL"}:<br />
<br />
${baseUrl}/${resource.urlNamespace}/${resource.id?c}/edit<br />
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
---------
Note: please do not reply to this automated email
</@mail.content>