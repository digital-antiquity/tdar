<#import "email-macro.ftl" as mail /> 
<@mail.content>
<#list users>
The following users registered with ${siteAcronym} since (${date?date?string.short}):<br />
    <#items as user>
     <ul>
        <li> ${user.properName}</li>
        <li><a href="https://core.tdar.org/browse/creators/${user.id?c}">${siteAcronym} page</a></li>
        <li>email: ${user.email}</li>
        <li>institution: ${(user.institutionName)!"None"}</li>
        <li>affiliation: ${(user.affiliation.label)!"None"}</li>
        <li>contributor reason: <#if !user.contributorReason?? || (user.contributorReason?trim == "")>None<#else>${user.contributorReason?trim}</#if></li>
    </ul>
    </#items>
</#list>
</@mail.content>