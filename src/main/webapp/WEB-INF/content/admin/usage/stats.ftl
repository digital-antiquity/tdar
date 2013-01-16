<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<title>Administrator Dashboard: Recent Activity</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<h1>stats for (${dateStart!"last week"} - ${dateEnd!.now} ) </h1>
<h2>Usage Stats</h2>
<table class="tableFormat">
    <tr>
        <th>resource</th>
        <th>views</th>
        <th>day</th>
    </tr>
<#list usageStats as stats>
    <tr>
        <td><a href="<@s.url value="/${stats.resource.resourceType.urlNamespace}/${stats.resource.id?c}" />">${stats.resource.title}</a> (${stats.resource.id?c})</td>
        <td>${stats.count}</td>
        <td>${stats.aggregateDate}</td>
    </tr>
</#list>
</table>

<h2>Download Stats</h2>
<table class="tableFormat">
    <tr>
        <th>File</th>
        <th>Downloads</th>
        <th>day</th>
    </tr>
<#list downloadStats as stats>
    <tr>
        <td><a href="<@s.url value="/filestore/${stats.informationResourceFileId?c}" />">${stats.filename}</a> (${stats.informationResourceId?c})</td>
        <td>${stats.count}</td>
        <td>${stats.aggregateDate}</td>
    </tr>
</#list>
</table>
</#escape>
