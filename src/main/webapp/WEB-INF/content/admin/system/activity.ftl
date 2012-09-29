<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<title>Administrator Dashboard: Recent Activity</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>

<h2>Recent System Activity</h2>
<hr/>
<@s.actionerror />
<h3>Scheduled Processes Currently in the Queue</h3>
<#if scheduledProcessesEnabled??>
<ol>
<#list scheduledProcessQueue as process>
 <li>${process} - current id: ${process.lastId?c}
</#list>
</ol>
<#else>
 Scheduled Processes are not enabled on this machine
</#if>

<h3>Configured Scheduled Processes</h3>
<#if scheduledProcessesEnabled??>
<ol>
<#list allScheduledProcesses as process>
 <li>${process}
</#list>
</ol>
<#else>
 Scheduled Processes are not enabled on this machine
</#if>

<h3>Hibernate Statistics</h3>
${sessionStatistics}


</#escape>
