Dear ${authenticatedUser.firstName},
  We have completed your resource export request.  You can download the requested files (listed below) here: ${url}.  This link will be valid for 24 hours.

<#list resources.resources as resource>
 - ${resource.title} (${resource.id})
</#list>
<#if resources.collection?has_content >
    <#list resources.collection.resources as resource>
     - ${resource.title} (${resource.id})
    </#list>
</#if>
<#if resources.account?has_content >
    <#list resources.account.resources as resource>
     - ${resource.title} (${resource.id})
    </#list>
</#if>