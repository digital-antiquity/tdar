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
<h1>Recent System Activity</h1>
<@s.actionerror />

<h3>Active Users</h3>
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

<h3>Recent Activity</h3>
<table class="tableFormat table" id="tblRecentActivity">
    <thead>
        <tr>
            <th>date</th><th>user</th><th>total time (ms)</th><th>request</th>
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
        <#noescape>
        <td width=550>${(activity.name!"")?html?replace("&", "<br>&")}</td>
        </#noescape>
      </tr>
    </#list>
    </tbody>
</table>
<h3>Scheduled Processes Currently in the Queue</h3>
<#if scheduledProcessesEnabled??>
<ol>
<#list scheduledProcessQueue as process>
 <li>${process} - current id: ${(process.lastId!-1)?c}</li>
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

<h3>System Statistics</h3>
<table class="tableFormat table" id="tblQueryStats">
<#list moreInfo?keys as key>
<tr><th>${key}</th><td>${(moreInfo[key]!"")?string}</td></tr>
</#list>
</table>

<h3>Hibernate Statistics</h3>
<pre>
${sessionStatistics}
</pre>
<#assign threshold =99/>
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
		        <tr><td><b>${query}</b></td>
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
<script>
$(function(){
    TDAR.datatable.extendSorting();
    $("#tblRecentActivity, #tblQueryStats").dataTable({"bFilter": false, "bInfo": false, "bPaginate":false});
});
</script>
</body>


</#escape>
