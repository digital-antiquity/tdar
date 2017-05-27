Dear ${requestor.properName},
  Your request for access to ${resource.title} (${resource.id?c}) has been granted by ${authorizedUser.properName}

<#if message?has_content>
${message}
</#if>
<#if expires?has_content>
Access has been granted until ${expires?string("yyyy-MM-dd")}
</#if>