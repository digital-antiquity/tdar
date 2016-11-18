<#escape _untrusted as _untrusted?html >
<title>Resources and Shares ${user.properName} Has Access to</title>
<h2>Resources and Shares ${user.properName} Has Access to</h2>
<#list findResourcesSharedWith>
<h5>Shared Resources:</h5>
    <ul>
    <#items as item>
        <li><a href="${item.detailUrl}">${item.title}</a> (${item.id?c})</li>
    </#items>
    </ul>
</#list>


<#list findCollectionsSharedWith>
<h5>Shared Collections:</h5>
    <ul>
    <#items as item>
        <li><a href="${item.detailUrl}">${item.title}</a> (${item.id?c})</li>
    </#items>
    </ul>
</#list>
</#escape>