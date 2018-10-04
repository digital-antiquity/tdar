<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "common-invoice.ftl" as invoicecommon >

<head>
    <title>Your Cart</title>

</head>
<body>
<h1>Invoice <span class="small">{${invoice.transactionStatus}}</span></h1>
    <#if account?has_content>
    <p><b>Account:</b><a class="accountLink" href="<@s.url value="/billing/${account.id?c}" />">${account.name}</a></p>
    </#if>
    <#if invoice.otherReason?has_content>
    <p>${invoice.otherReason}</p>
    </#if>
    <@invoicecommon.printInvoice />

<div class="container row">
    <div class="col-3">
        <h3>Billing Information</h3>
        <#if invoice.owner??>
            <b>Account Owner</b>
            <br>${invoice.owner.properName}
        </#if>



        <#if invoice.address?has_content>
            <@common.printAddress invoice.address/>
        </#if>
    </div>

    <div class="col-3">
        <h3>Transaction Info</h3>
        <b>Transaction Type: </b>${invoice.paymentMethod!"Unknown"}<br/>
        <b>Transaction Status: </b>${invoice.transactionStatus}<br/>
    </div>
</div>
</body>
</#escape>