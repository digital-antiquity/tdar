Dear ${requestor.properName},

Thanks for your interest in the ${customName}.  You have been granted access
to the requested tDAR record.

 ${resource.title} (${resource.id?c}).  
 
${descriptionResponse!"To upload your abstract please visit the following URL"}:

${baseUrl}/${resource.urlNamespace}/${resource.id?c}/edit


Kind regards,

Staff at ${serviceProvider}


<#if message?has_content>
${message}
</#if>




---------
Note: please do not reply to this automated email