<#escape _untrusted as _untrusted?html >
<h2>Edit History / Activity for ${user.properName}</h2>
<#list logs>
	<table class="table">
		<thead>
			<tr>
				<th>Date</th>
				<th>Resource</th>
				<th>Type</th>
				<th>Time in Seconds</th>
				<th>Log</th>
			</tr>
		</thead>
		<tbody>
		<#items as log>
			<tr>
				<td>${log.timestamp}</td>
				<td><#if log.resource?has_content><a href="${log.resource.detailUrl}">${log.resource.title}</a></#if></td>
				<td>${log.type!''}</td>
				<td>${log.timeInSeconds!''}</td>
				<td>${log.logMessage}</td>
			</tr>
		</#items>
		</tbody>
	</table>
</#list>
</#escape>