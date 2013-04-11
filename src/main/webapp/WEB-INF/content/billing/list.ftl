<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>

<head>
	<title>All Accounts</title>
 </head>
<body>
<h1>All Accounts</h1>
<table class="table tableFormat">
<tr>
<th>Name</th><th>Owner</th><th>Status</th><th>Total</th><th>Files</th><th>Resources</th><th>Space</th>
</tr>
<tr>
  <#list accounts as account>
   <td><a href="<@s.url value="/billing/${account.id?c}"/>">${account.name}</a></td>
   <td>${account.owner.properName} </td>
   <td>${account.status}</td>
   <td>$${account.totalCost}</td>
   <td>${account.totalNumberOfFiles} (${account.filesUsed})</td>
   <td>${account.totalNumberOfResources} (${account.resourcesUsed})</td>
   <td>${account.totalSpaceInMb} (${account.spaceUsedInMb})</td>
</tr>
  </#list>
</table>
</body>
</#escape>