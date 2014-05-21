<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "common-invoice.ftl" as invoicecommon >

<head>
    <title>Review Billing Information</title>

</head>
<body>
<h1>Invoice <span class="small">{${invoice.transactionStatus}}</span></h1>

    <@invoicecommon.printInvoice />

<div class="container row">
    <div class="span3">
        <h3>Billing Address <#if invoice.modifiable><a href="<@s.url value="/cart/${invoice.id?c}/address" />" class="small">(modify)</a></#if></h3>
        <#if invoice.address?has_content>
            <@common.printAddress invoice.address/>
        </#if>
    </div>

</div>
    <#if invoice.transactionStatus == 'PENDING_TRANSACTION' || invoice.transactionStatus == 'PREPARED'>
    <a class="button btn btn-primary submitButton" href="<@s.url value="/cart/${id?c}/credit" />">Pay</a>
    </#if>

</body>
</#escape>