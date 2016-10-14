<#escape _untrusted as _untrusted?html >
<h2>Resources and Shares User Has Access to</h2>
<#list findResourcesSharedWith>
<h5> Resources shared with:${user.properName}</h5>
    <ul>
    <#items as item>
        <li><a href="${item.detailUrl}">${item.title}</a> (${item.id?c})</li>
    </#items>
    </ul>
</#list>


<#list findCollectionsSharedWith>
<h5> Collections shared with:${user.properName}</h5>
    <ul>
    <#items as item>
        <li><a href="${item.detailUrl}">${item.title}</a> (${item.id?c})</li>
    </#items>
    </ul>
</#list>
</#escape>