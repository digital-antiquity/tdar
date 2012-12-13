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
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='/cart/process-payment-request?id=${invoice.id?c}'>
    <@s.hidden name="id" value="${invoice.id?c}"/>

    <@s.radio list="allPaymentMethods" name="invoice.paymentMethod" cssClass="transactionType" emptyOption='false' />

    <div class="typeToggle credit_card invoice manual">
        <@s.textfield name="invoice.billingPhone" cssClass="input-xlarge phoneUS  required-visible" label="Billing Phone #" />
    </div>
    <div class="typeToggle invoice">
        <@s.textfield name="invoice.invoiceNumber" cssClass="input-xlarge" label="Invoice #" />
    </div>
    <div class="typeToggle manual">
        <@s.textarea name="invoice.otherReason" cssClass="input-xlarge" label="Other Reason" />
    </div>
    
    <@edit.submit fileReminder=false />
</@s.form>

</div>

<script>
$(document).ready(function() {
    'use strict';
    TDAR.common.initEditPage($('#MetadataForm')[0]);
    $(".transactionType[type=radio]").click(function() {switchType(this,'#MetadataForm');});
   if (!$(".transactionType[type=radio]:checked").length) {
    $($(".transactionType[type=radio]")[0]).click();
   }
   switchType($(".transactionType[type=radio]:checked",$('#MetadataForm')),"#MetadataForm");

});

</script>
</body>
</#escape>
