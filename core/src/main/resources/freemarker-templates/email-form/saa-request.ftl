Dear ${to.properName},

${from.properName} has requested access to the following resource as part of the SAA
abstract project: ${resource.title} (${resource.id?c}).  You can view
${from.properName}'s email address below if you need more information from them.


${baseUrl}/resource/request/grant?resourceId=${resource.id?c}&requestorId=${from.id?c}&type=SAA

Below is the detailed request from the User. To view the record in ${siteAcronym} visit:
${baseUrl}/${resource.urlNamespace}/${resource.id?c}

Kind regards,

Staff at ${serviceProvider}

---------
From: ${from.email}

${message}





---------
Note: please do not reply to this automated email