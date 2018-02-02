<#import "../email-macro.ftl" as mail /> 

<@mail.content>
Dear ${requestor.properName},<br />
Your request for access to ${resource.title} (${resource.id?c}) has been granted by ${authorizedUser.properName}<br />
<br />

<#if message?has_content>
${message}
</#if>

<#if expires?has_content>
<br />
Access has been granted until ${expires?string("yyyy-MM-dd")}
</#if>
</@mail.content>