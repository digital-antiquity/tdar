<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>

<head>
	<title>All Invoices</title>
 </head>
<body>
<h1>All Invoices</h1>
<table class="table tableFormat">
<tr>
<th>Date</th><th>Owner</th><th>Transaction Type</td><th>Status</th><th>Files</th><th>Resources</th><th>Space</th><th>Total</th>
</tr>
<tr>
  <#list invoices as invoice>
  <#if invoice.transactionStatus.complete>
	   <td><a href="<@s.url value="/cart/${invoice.id?c}"/>">${invoice.dateCreated}</a></td>
	   <td>${invoice.owner.properName} </td>
	   <td>${invoice.paymentMethod!""}</td>
	   <td>${invoice.transactionStatus}</td>
	   <td>${invoice.totalNumberOfFiles} </td>
	   <td>${invoice.totalResources}</td>
	   <td>${invoice.totalSpaceInMb}</td>
	   <td>$${invoice.total!0}</td>
   </#if>
</tr>
  </#list>
</table>
</body>
</#escape>