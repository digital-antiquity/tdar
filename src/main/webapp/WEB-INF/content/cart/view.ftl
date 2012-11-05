<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/common.ftl" as common>

<head>
<title>Your cart</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>

<table>
<tr>
	<th>Item</th><th>Quantity</th><th>Cost</th><th>Subtotal</th>
</tr>
<#list invoice.items as item>
	<tr>
	<td>${item.activity.name}</td>
	<td>${item.quantity}</td>
	<td>${item.activity.price}</td>
	<td>${item.subtotal}</td>
	</tr>
</#list>
<tr>
	<td colspan=3><em>Total:</em></td><td>${invoice.total!0}</td>
</tr>
</table>
<#if invoice.address?has_content>
<h3>Billing Address</h3>
<@common.printAddress invoice.address/>

<#if invoice.transactionType?has_content>
<h3>Transaction Info</h3>
<b>Transaction Type: </b>${invoice.transactionType}<br/>
<b>Transaction Status: </b>${invoice.transactionStatus}<br/>

<#else>
	<a class="button btn btn-primary submitButton" href="<@s.url value="/cart/${id?c}/credit" />">Pay</a>
</#if>

<#else>
	<a class="button btn btn-primary submitButton" href="<@s.url value="/cart/${id?c}/billing" />">Add Billing Address</a>

</#if>

</body>
</#escape>