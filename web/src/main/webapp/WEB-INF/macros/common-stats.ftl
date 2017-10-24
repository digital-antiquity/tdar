<#escape _untrusted as _untrusted?html>
	
<#macro table>

<#if json?has_content>
<h2>Usage Stats</h2>
    <div class="barChart" id="statusChart"  data-source="#graphJson" 
    	 style="height:200px" data-x="date" data-values="Views,Views (Bots),Downloads" data-legend="true" >
    </div>

<#noescape>
<script id="graphJson">
${json!'[]'}
</script>
</#noescape>
</#if>

    <div>
    <ul class="nav nav-tabs">
        <li class="<#if granularity == 'DAY'>active</#if>"><a href="?granularity=DAY">Last Week</a></li>
        <li class="<#if granularity == 'MONTH'>active</#if>"><a href="?granularity=MONTH">Last Year</a></li>
        <li class="<#if granularity == 'YEAR'>active</#if>"><a href="?granularity=YEAR">Overall</a></li>
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
					<th>${label}</th>
			</#list> 
		</tr>
		<@grandTotalRow />
		<#list statsForAccount.rowData as row> 
		<tr>
			<td>${row.resource.id?c}</td>
			<td><a href="/${row.resource.urlNamespace}/${row.resource.id?c}">${row.resource.title}</a></td>
			<td>${row.resource.resourceType}</td>
			<td>${row.resource.status}</td>
			<#list row.data as dataPoint>
					<td>${dataPoint!0?c}</td>
			</#list>
		</tr>
	</#list>
	<@grandTotalRow/>
	</table>
	<#else>
None Yet
    </#if>
</#macro>
<#macro grandTotalRow>
		<tr>
 			<th colspan="4">Grand Totals</th>
 			<#list 0.. (statsForAccount.totals?size -1) as i>
					<th>${statsForAccount.totalBots[i]!0?c}</th>
					<th>${statsForAccount.totals[i]!0?c}</th>
			</#list>
  			<#list 0.. (statsForAccount.totalDownloads?size -1) as i>
					<th>${statsForAccount.totalDownloads[i]!0?c}</th>
			</#list>
        </tr> 

</#macro>
</#escape>