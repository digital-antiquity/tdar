<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<title>Your cart</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>

<h1>Your cart</h1>

<div>
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='process-payment-info'>
	<@s.hidden name="id" value="${invoice.id}"/>

	<@s.radio list="allTransactionTypes" name="invoice.transactionType" cssClass="transactionType" emptyOption='false' />
	<@s.textfield name="invoice.billing.phone" cssClass="input-xlarge phoneUS" label="Billing Phone #" />

	<div class="invoice typeToggle">
		<@s.textfield name="invoice.invoiceNumber" cssClass="input-xlarge" label="Invoice #" />
	</div>
	<div class="manual typeToggle">
		<@s.textarea name="invoice.otherReason" cssClass="input-xlarge" label="Other Reason" />
	</div>
	
	
	<div class="credit_card typeToggle">
		<@s.textfield name="creditCardNumber" cssClass="creditcard input-xlarge" label="Credit Cart #" />
		<@s.textfield name="verificationNumber" cssClass="input-xlarge" label="Verification #" />
		<@s.textfield name="expirationYear" cssClass="reasonableDate input-xlarge" label="Expiration Year" />
		<@s.textfield name="expirationMonth" cssClass="month input-xlarge" label="Expiration Month" />
	</div>	

    <@edit.submit fileReminder=false />
</@s.form>

</div>

<script>
$(document).ready(function() {
    'use strict';
    $(".transactionType[type=radio]").click(function() {switchType(this,'#MetadataForm');});
    switchType($(".transactionType[type=radio]:checked",'#MetadataForm'));
   if (!$(".transactionType[type=radio]:checked").length) {
	$($(".transactionType[type=radio]")[0]).click();
   }

    TDAR.common.initEditPage($('#MetadataForm')[0]);

});

</script>
</body>
</#escape>
