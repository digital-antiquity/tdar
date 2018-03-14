<#import "../email-macro.ftl" as mail /> 


<@mail.content>
Dear ${requestor.properName},<br />
Your request for access to <a href="${baseUrl}${resource.detailUrl}">${resource.title} (${resource.id?c})</a> has been declined by ${authorizedUser.properName}<br />

<br />

<#if message?has_content>
${message}
</#if>

</@mail.content>