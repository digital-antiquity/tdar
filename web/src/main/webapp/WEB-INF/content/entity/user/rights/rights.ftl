<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>

<title>Resources and Shares ${user.properName} Has Access to</title>

<body>
<@nav.creatorToolbar "rights" />

		<h2 id="resources">Resources and Collections ${user.properName} Has Access to</h2>
<div class="row">
<div class="col-2">
       <ul class="nav nav-list nav-pills nav-stacked dashboard-nav">
        <li class="nav-header">Items</li>
        <li class="nav-item"><a  class="nav-link" href="#resources">Resources (${findResourcesSharedWith?size})</a></li>
        <li class="nav-item"><a  class="nav-link" href="#collections">Collections (${findCollectionsSharedWith?size})</a></li>
        <li class="nav-item"><a  class="nav-link" href="#integrations">Integrations (${integrations?size})</a></li>
        <li class="nav-item"><a  class="nav-link" href="#accounts">Billing Accounts (${(accounts![])?size})</a></li>
</ul>
</div>
<div class="col-10">
<#list findResourcesSharedWith>
<h5>${findResourcesSharedWith?size } Directly Shared Resources: (not through a collection)</h5>
    <ul>
    <#items as item>
        <li><a href="${item.detailUrl}">${item.title}</a> (${item.id?c})</li>
    </#items>
    </ul>
</#list>


<#list findCollectionsSharedWith>
<h5 id="collections">${findCollectionsSharedWith?size} Shared Collections:</h5>
    <ul>
    <#items as item>
        <li><a href="${item.detailUrl}">${item.title}</a> (${item.id?c})</li>
    </#items>
    </ul>
</#list>


<#list accounts![]>
<h5 id="accounts">${accounts?size} Billing Accounts:</h5>
    <ul>
    <#items as item>
        <li><a href="${item.detailUrl}">${item.name}</a> (${item.id?c})</li>
    </#items>
    </ul>
</#list>

<#list integrations>
<h5 id="integrations">${integrations?size} Data Integrations:</h5>
    <ul>
    <#items as item>
        <li><a href="${item.detailUrl}">${item.title}</a> (${item.id?c})</li>
    </#items>
    </ul>
</#list>

</div>
</div>
</body>
</#escape>