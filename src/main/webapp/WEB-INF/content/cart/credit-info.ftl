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
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save'>

<@s.radio list="allTransactionTypes" name="invoice.transactionType" cssClass="transactionType" />
	<@s.textfield name="invoice.billing.phone" cssClass="input-xlarge" label="Billing Phone #" />
<div class="INVOICE typeToggle">
	<@s.textfield name="invoice.invoiceNumber" cssClass="input-xlarge" label="Invoice #" />
</div>
<div class="OTHER typeToggle">
	<@s.textarea name="invoice.otherReason" cssClass="input-xlarge" label="Other Reason" />
</div>


<div class="CREDIT_CARD typeToggle">
	<@s.textfield name="creditCardNumber" cssClass="input-xlarge" label="Credit Cart #" />
	<@s.textfield name="verificationNumber" cssClass="input-xlarge" label="Verification #" />
	<@s.textfield name="expirationYear" cssClass="input-xlarge" label="Expiration Year" />
	<@s.textfield name="expirationMonth" cssClass="input-xlarge" label="Expiration Month" />
</div>	
    <@edit.submit fileReminder=false />
</@s.form>

</div>

<script>
$(document).ready(function() {
    $(".transactionType input[type=radio]").click(function() {switchType(this,'#MetadataForm');});
    switchType($(".transactionType input[type=radio]:checked",'#MetadataForm'));
});

</script>
</body>
</#escape>
