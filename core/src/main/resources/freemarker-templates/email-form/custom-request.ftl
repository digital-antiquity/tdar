Dear ${to.properName},

${from.properName} has requested access to the following resource as part of the
${customName}: ${resource.title} (${resource.id?c}).  You can view
${from.properName}'s email address below if you need more information from them.

<#if descriptionRequest?has_content>${descriptionRequest}
</#if>
${baseUrl}/resource/request/grant?resourceId=${resource.id?c}&requestorId=${from.id?c}&type=CUSTOM

Below is the detailed request from the User. To view the record in ${siteAcronym} visit:
${baseUrl}/${resource.urlNamespace}/${resource.id?c}

Kind regards,

Staff at ${serviceProvider}

---------
From: ${from.email}

${message}





---------
Note: please do not reply to this automated email