<#escape _untrusted as _untrusted?html>
	
<#macro table>

    <div>
    <ul class="nav nav-tabs">
        <li class="<#if granularity == 'DAY'>active</#if>"><a href="<@s.url value="stats"/>?id=${id?c}&granularity=DAY">Last Week</a></li>
        <li class="<#if granularity == 'MONTH'>active</#if>"><a href="<@s.url value="stats"/>?id=${id?c}&granularity=MONTH">Last Year</a></li>
        <li class="<#if granularity == 'YEAR'>active</#if>"><a href="<@s.url value="stats"/>?id=${id?c}&granularity=YEAR">Overall</a></li>
    </ul>
    </div>
    <h3>Results: <@s.text name="${granularity.localeKey}"/></h3>
    <#if statsForAccount?has_content>
	<table class="table tableFormat" >
		<tr>
			<th>Id</th>
			<th width="80%">Title</th>
			<th>Resource Type</th>
			<th>Status</th>
			<#list statsForAccount.rowLabels as label>
				<#if statsForAccount.totals[label_index] != 0>
					<th>${label}</th>
				</#if>
			</#list>
		</tr>
		<#list statsForAccount.rowData as row> 
		<tr>
			<td>${row.resource.id?c}</td>
			<td><a href="/${row.resource.urlNamespace}/${row.resource.id?c}">${row.resource.title}</a></td>
			<td>${row.resource.resourceType}</td>
			<td>${row.resource.status}</td>
			<#list row.data as dataPoint>
				<#if statsForAccount.totals[dataPoint_index] != 0>
					<td>${dataPoint!0?c}</td>
				</#if>
			</#list>
		</tr>
	</#list>
		<tr>
			<th colspan="4">Grand Totals</th>
			<#list statsForAccount.totals as dataPoint>
				<#if statsForAccount.totals[dataPoint_index] != 0>
					<th>${dataPoint!0?c}</th>
				</#if>
			</#list>
	</tr>
	</table>
	<#else>
None Yet
    </#if>
</#macro>
</#escape>