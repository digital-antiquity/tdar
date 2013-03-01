<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<h1>Administrative info for <span>${resource.title}</span></h1>

<h2>Usage Stats</h2>
<table class="tableFormat table">
    <tr>
        <th>views</th>
        <th>day</th>
    </tr>
<#list usageStatsForResources as stats>
    <tr>
        <td>${stats.count}</td>
        <td>${stats.aggregateDate}</td>
    </tr>
</#list>
</table>

<#if downloadStats?has_content>
<h2>Download Stats</h2>
<#list downloadStats?keys as key>
<#if downloadStats.get(key)?has_content>
<h3>${key}</h3>
<table class="tableFormat table">
    <tr>
        <th>downloads</th>
        <th>day</th>
    </tr>
<#list (downloadStats.get(key)) as stats>
    <tr>
        <td>${stats.count}</td>
        <td>${stats.aggregateDate}</td>
    </tr>
</#list>
</table>
</#if>
</#list>
</#if>

<h2>Resource Revision History</h2>
<table class="table tableFormat">
    <tr>
        <th>When</th>
        <th>Who</th>
        <th>Event</th>
    </tr>
<#list resourceLogEntries as entry>
    <tr>
        <td>${entry.timestamp}</td>
        <td>${entry.person.properName}</td>
        <td>${entry.logMessage}</td>
    </tr>
</#list>
</table>



<@view.accessRights />

<@view.adminFileActions />
</#escape>