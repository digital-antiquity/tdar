<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
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
<hr/>
<@s.actionerror />

<h3>Active Users</h3>
<ul>
	<#list activePeople as user>
	<li>
	<#if user?has_content>
	<a href="<@s.url value="/browse/creators/${user.id?c}"/>">${user.properName}</a></li>
	<#else>
	Unknown User
	</#if>
	</li>
	</#list>
</ul>


<h3>User Agents</h3>
<table class="tableFormat table">
    <thead>
        <tr>
            <th>browser</th><th>count</th>
        </tr>
    </thead>
    <tbody>
        <#list counters?keys as count>
        <#if count?has_content>
         <tr>
             <td>${count}</td>
             <td>${counters.get(count)}</td>
          </tr>
        </#if>
        </#list>
    </tbody>
</table>
<br/>
<h3>Recent Activity</h3>
<table class="tableFormat table">
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
        <td width=550>${(activity.name!"")?html?replace("&", "<wbr>&")}</td>
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

<h3>Hibernate Statistics</h3>
<pre>
${sessionStatistics}
</pre>
<script>
$(function(){
    $(".tableFormat").dataTable({"bFilter": false, "bInfo": false, "bPaginate":false});
});
</script>
</body>


</#escape>
