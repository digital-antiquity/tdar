Dear ${to.properName},

${from.properName} has requested access to the following resource that you 
have administrative rights to: ${resource.title} (${resource.id?c}).  You can view
${from.properName}'s email address below if you need more information from them.

${siteAcronym} allows you to extend view and download privileges for confidential 
records to registered ${siteAcronym} users.  If you decide to share this resource
with ${from.properName}, you may log in to ${siteAcronym}, and visit:

${baseUrl}/resource/request/grant?resourceId=${resource.id?c}&requestorId=${from.id?c}

Below is the detailed request from the User. To view the record in ${siteAcronym} visit:
${baseUrl}/${resource.urlNamespace}/${resource.id?c}

Kind regards,

Staff at ${serviceProvider}

---------
From: ${from.email}

${message}





---------
Note: please do not reply to this automated email