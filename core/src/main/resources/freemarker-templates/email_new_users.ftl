<#import "email-macro.ftl" as mail /> 
<@mail.content>
<#list users>
The following users registered with ${siteAcronym} since (${date?date?string.short}):<br />
     <ul>
    <#items as user>
        <li><a href="https://core.tdar.org/browse/creators/${user.id?c}">${user.properName}</a> ( ${user.email}) <br/>
        ${(user.institutionName)!"None"}<br/>
        affiliation: ${(user.affiliation.label)!"None"}<br/>
        contributor reason: <#if !user.contributorReason?? || (user.contributorReason?trim == "")>None<#else>${user.contributorReason?trim}</#if>
        </li>
    </#items>
    </ul>
</#list>
</@mail.content>