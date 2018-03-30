<#import "email-macro.ftl" as mail /> 
<@mail.content>
Dear ${authenticatedUser.firstName},<br />

We have completed your resource export request.  You can download the requested files (listed below) here: ${url}.  This link will be valid for 24 hours.
<br />

<ul>
    <#list resources.resources![] as resource>
        <li><a href="${baseUrl}${resource.detailUrl}">${resource.title}</a> (${resource.id})</li>
    </#list>
    
    <#list (resources.collection.resources)![] as resource>
       <li><a href="${baseUrl}${resource.detailUrl}">${resource.title}</a> (${resource.id})</li>
    </#list>
    
    <#list (resources.account.resources)![] as resource>
       <li><a href="${baseUrl}${resource.detailUrl}">${resource.title}</a> (${resource.id})</li>
    </#list>
</ul>
</@mail.content>