<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "common-invoice.ftl" as invoicecommon >

<head>
<title>Your cart -- choose payment method</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>

<h1>Choose Payment Method</h1>

<div>
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='/cart/process-payment-request?id=${invoice.id?c}'>
	    <@s.hidden name="id" value="${invoice.id?c!-1}" />
    <@s.hidden name="invoice.id" />

<@invoicecommon.paymentMethod />
</@s.form>

</div>

</body>
</#escape>
