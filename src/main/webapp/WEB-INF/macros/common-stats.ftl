<#escape _untrusted as _untrusted?html>
	
<#macro table>

    <div>
    <ul class="nav nav-tabs">
        <li><a href="<@s.url value="stats"/>?id=${id?c}&granularity=DAY">Last Week</a></li>
        <li><a href="<@s.url value="stats"/>?id=${id?c}&granularity=MONTH">Last Year</a></li>
        <li><a href="<@s.url value="stats"/>?id=${id?c}&granularity=YEAR">Overall</a></li>
    </ul>
    </div>
    <h3>Results: <@s.text name="${granularity.localeKey}"/></h3>
    <#if statsForAccount?has_content>
	<table class="table tableFormat" >
		<tr>
			<th>Id</th>
			<th>Title</th>
			<th>Resource Type</th>
			<th>Status</th>
			<#list statsForAccount.rowLabels as label>
				<th>${label}</th>
			</#list>
		</tr>
		<#list statsForAccount.rowData as row> 
		<tr>
			<td>${row.resource.id?c}</td>
			<td>${row.resource.title}</td>
			<td>${row.resource.resourceType}</td>
			<td>${row.resource.status}</td>
			<#list row.data as dataPoint>
				<td>${dataPoint!0?c}</td>
			</#list>
		</tr>
		</#list>
		<tr>
			<th colspan="4">Grand Totals</th>
			<#list statsForAccount.totals as dataPoint>
				<th>${dataPoint!0?c}</th>
			</#list>
	</tr>
</table>
<#else>
None Yet
    </#if>
</#macro>
</#escape>