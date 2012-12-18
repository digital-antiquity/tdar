<#escape _untrusted as _untrusted?html>

<#macro printInvoice>
	<h3>Items <#if invoice.modifiable><a href="<@s.url value="/cart/${invoice.id?c}/edit" />" class="small">(modify)</a></#if></h3>
	<table class="tableFormat">
	<tr>
	    <th>Item</th><th>Quantity</th><th>Cost</th>
	    <th>Files</th>
	    <th>Space</th>
	    <th>Resources</th>
	    <th>Subtotal</th>
	</tr>
	<#list invoice.items as item>
	    <tr>
	    <td>${item.activity.name}</td>
	    <td>${item.quantity!0}</td>
	    <td>$${item.activity.price}</td>
        <td> ${item.quanity!0 * item.activity.numberOfFiles}</td>
        <td> ${item.quanity!0 * item.activity.numberOfMb}</td>
        <td> ${item.quanity!0 * item.activity.numberOfResources}</td>
	    <td>$${item.subtotal}</td>
	    </tr>
	</#list>
	<tr>
	    <th colspan=6><em>Total:</em></th><th>$${invoice.calculatedCost!0}</th>
	</tr>
	</table>
</#macro>

</#escape>