<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "../admin-common.ftl" as admin>


<head>
    <title>Administrator Dashboard: Recent Activity</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <style>
        pre, td {
            white-space: pre-line;
        }

    </style>
</head>
<body>
    <@admin.header/>


<a href="#users">users</a> 
<a href="#activity">activity</a>
<a href="#process">process</a>
<a href="#stats">stats</a>

<h1>Recent System Activity</h1>
    <@s.actionerror />

<h3 id="users">Active Users</h3>
<ul>
    <#list activePeople as user>
        <li>
            <#if user?has_content>
                <a href="<@s.url value="/browse/creators/${user.id?c}"/>">${user.properName}</a>
            <#else>Unknown User
            </#if>
        </li>
    </#list>
</ul>


<h3>User Agents</h3>
    <@list.hashtable data=counters keyLabel="Browser" valueLabel="Count" />

<h3 id="activity">Recent Activity</h3>
<table class="tableFormat table" id="tblRecentActivity">
    <thead>
    <tr>
        <th>date</th>
        <th>user</th>
        <th>total time (ms)</th>
        <th>action (ms)</th>
        <th>result (ms)</th>
        <th>request</th>
    </tr>
    </thead>
    <tbody>
        <#list activityList as activity>
            <#assign highlight = false/>
            <#assign highlightPost = false/>
            <#if activity.user?has_content>
                <#assign highlight=true />
            </#if>
            <#if activity.name?contains("POST") >
                <#assign highlightPost=true />
            </#if>
        <tr class="${highlight?string('highlightrow-yellow','')} ${highlightPost?string('highlightrow-green','')}">
            <td>${activity.startDate?datetime}</td>
            <td><#if activity.user?has_content>${activity.user.properName}</#if></td>
            <td>${(activity.totalTime?c)!default("-")}</td>
            <td>${(activity.actionTime!0?c)!default("-")}</td>
            <td>${(activity.resultTime!0?c)!default("-")}</td>
            <#noescape>
                <td width=550>${(activity.name!"")?html?replace("&", "<br>&")}</td>
            </#noescape>
        </tr>
        </#list>
    </tbody>
</table>
<h3 id="process">Scheduled Processes Currently in the Queue</h3>
    <#if scheduledProcessesEnabled??>
    <ol>
        <#list scheduledProcessQueue as process>
            <li>${process}</li>
        </#list>
    </ol>
    <#else>
    Scheduled Processes are not enabled on this machine
    </#if>

<h3>Configured Scheduled Processes</h3>
    <#if scheduledProcessesEnabled??>
    <ol>
        <#list allScheduledProcesses as process>
            <li>${process}</li>
        </#list>
    </ol>
    <#else>
    Scheduled Processes are not enabled on this machine
    </#if>

<h3 id="stats">System Statistics</h3>
<table class="tableFormat table" id="tblQueryStats">
    <#list moreInfo?keys as key>
        <tr>
            <th>${key}</th>
            <td>${(moreInfo[key]!"")?string}</td>
        </tr>
    </#list>
</table>

<h3>Hibernate Statistics</h3>
<table class="tableFormat table" id="tblQueryStats">
    <#assign txt = sessionStatistics?string>
    <#assign txt = txt?substring(1+txt?index_of("["), txt?last_index_of("]")) >
    <#list txt?split(",") as row_>
        <#assign row = row_?split("=") />
        <tr>
            <th>${row[0]}</th>
            <td>${(row[1]!"")?string}</td>
        </tr>
    </#list>
</table>

    <#assign threshold = 200 />
<p>Threshold for slow queries is: <strong>${threshold} ms</strong></p>
<table class="tableFormat table" id="tblQueryStats">
    <thead>
    <tr>
        <th><b>query</b></th>
        <th>executionCount</th>
        <th>executionAvgTime</th>
        <th>executionMaxTime</th>
        <th>executionMinTime</th>
        <th>cacheHitCount</th>
        <th>cacheMissCount</th>
        <th>cachePutCount</th>
        <th>executionRowCount</th>
    </tr>
    </thead>
    <tbody>
        <#list sessionStatistics.queries as query>
            <#assign stat = sessionStatistics.getQueryStatistics(query) />
            <#if (stat.executionAvgTime > threshold || stat.executionMaxTime > threshold) >
            <tr>
                <td><b>${query}</b></td>
                <td>${stat.executionCount}</td>
                <td>${stat.executionAvgTime}</td>
                <td>${stat.executionMaxTime}</td>
                <td>${stat.executionMinTime}</td>
                <td>${stat.cacheHitCount}</td>
                <td>${stat.cacheMissCount}</td>
                <td>${stat.cachePutCount}</td>
                <td>${stat.executionRowCount}</td>
            </tr>
            </#if>
        </#list>
    </tbody>
</table>
</body>


</#escape>
