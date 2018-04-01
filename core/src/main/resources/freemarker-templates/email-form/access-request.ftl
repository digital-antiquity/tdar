<#import "../email-macro.ftl" as mail /> 

<@mail.content>
Dear ${to.properName},<br />
<br />

${from.properName} has requested access to the following resource that you 
have administrative rights to: <a href="${baseUrl}${resource.detailUrl}"> ${resource.title}</a> (${resource.id?c}).  You can view
${from.properName}'s email address below if you need more information from them.<br />

<p>
${siteAcronym} allows you to extend view and download privileges for confidential 
records to registered ${siteAcronym} users.  If you decide to share this resource
with ${from.properName}, you may log in to ${siteAcronym}, and visit:
</p>

<a href="${baseUrl}/resource/request/grant?resourceId=${resource.id?c}&requestorId=${from.id?c}">Share this record</a>

<p>
Below is the detailed request from the User. To view the record in ${siteAcronym} visit:
<a href="${baseUrl}/${resource.detailUrl}">${baseUrl}/${resource.detailUrl}</a>
</p>

Kind regards,<br />
<br />
Staff at ${serviceProvider}<br />
<hr>
<pre>
From: ${from.email}

${message}





</pre>
<hr>
Note: please do not reply to this automated email
</@mail.content>