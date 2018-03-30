<#import "../email-macro.ftl" as mail /> 
<@mail.content>
Dear ${to.properName},
<p>
${from.properName} has requested access to the following resource as part of the
${customName}: <a href="${baseUrl}${resource.detailUrl}">${resource.title}</a> (${resource.id?c}). You can view
${from.properName}'s email address below if you need more information from them.
</p>
<p>
<#if descriptionRequest?has_content>
    ${descriptionRequest}
</#if>
</p>
<p>
<a href="${baseUrl}/resource/request/grant?resourceId=${resource.id?c}&requestorId=${from.id?c}&type=CUSTOM">
    Click here to grant access to this resource
</a>
</p>
<p>
    Below is the detailed request from the User. To view the record in ${siteAcronym} visit:
    <a href="${baseUrl}${resource.detailUrl}">${resource.title}</a> (${resource.id?c})
</p>

Kind regards,<br />
<br />
Staff at ${serviceProvider}<br />
<br />
<p>
<hr/>
From: ${from.email}<br />
<br />
${message}

<hr/>
</p>
Note: please do not reply to this automated email
</@mail.content>