<#escape _untrusted as _untrusted?html >

<h1>Administrative info for ${resource.title}</h1>

<h2>Usage Stats</h2>
<table class="tableFormat table">
	<tr>
		<th>views</th>
		<th>day</th>
	</tr>
<#list usageStats as stats>
	<tr>
		<td>${stats.count}</td>
		<td>${stats.aggregateDate}</td>
	</tr>
</#list>
</table>

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
		<td>${person.properName}</td>
		<td>${logMessage}</td>
	</tr>
</#list>
</table>

</#escape>