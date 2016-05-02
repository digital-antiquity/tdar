<head>
<title>Resource Export Request</title>
</head>
<body>
<h1>Export Resources from ${siteAcronym}</h1>

<p>We are currently exporting the requested specified resources.  We will email you when your request has been completed</p>

<ul>
<#if exportProxy.collection?has_content><li>${exportProxy.collection.name}</li></#if>
<#if exportProxy.account?has_content><li>${exportProxy.account.name}</li></#if>
<#if exportProxy.resources?has_content>
    <#list exportProxy.resources as res>
        <li>${res.title}</li>
    </#list>
</#if>
</ul>

</body>