<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

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
        <td> ${(item.quantity!0) * (item.activity.numberOfFiles!0)} </td>
        <td> ${(item.quantity!0) * (item.activity.numberOfMb!0)} </td>
        <td> ${(item.quantity!0) * (item.activity.numberOfResources!0)}</td>
	    <td>$${item.subtotal}</td>
	    </tr>
	</#list>
	<tr>
	    <th colspan=6><em>Total:</em></th><th>$${invoice.calculatedCost!0}</th>
	</tr>
	</table>
</#macro>

<#macro paymentMethod includePhone=true>

    <@s.radio list="allPaymentMethods" name="invoice.paymentMethod" label="Payment Method" 
    listValue="label"    cssClass="transactionType" emptyOption='false' />

	<#if includePhone>
	    <div class="typeToggle credit_card invoice manual">
	        <@s.textfield name="billingPhone" cssClass="input-xlarge phoneUS  required-visible" label="Billing Phone #" />
	    </div>
    </#if>
    <div class="typeToggle invoice">
        <@s.textfield name="invoice.invoiceNumber" cssClass="input-xlarge" label="Invoice #" />
    </div>
    <div class="typeToggle manual">
        <@s.textarea name="invoice.otherReason" cssClass="input-xlarge" label="Other Reason" />
    </div>
    
    <@edit.submit fileReminder=false />

<script>
$(document).ready(function() {
    'use strict';
    TDAR.common.initEditPage($('#MetadataForm')[0]);
    $(".transactionType[type=radio]").click(function() {switchType(this,'#MetadataForm');});
   if (!$(".transactionType[type=radio]:checked").length) {
    $($(".transactionType[type=radio]")[0]).click();
   }
   switchType($(".transactionType[type=radio]:checked",$('#MetadataForm')),"#MetadataForm");
   
   $("#MetadataForm").submit(function() {
   	$("#MetadataForm_invoice_billingPhone").val($("#MetadataForm_invoice_billingPhone").val().replace(/([^\d]+)/ig,"") );
   });

});

</script>

</#macro>

</#escape>