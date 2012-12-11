<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "common-invoice.ftl" as invoicecommon >

<head>
<title>Your cart</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<h1>Invoice <span class="small">{${invoice.transactionStatus}}</span></h1>

<h3>Items <#if invoice.modifiable><a href="<@s.url value="/cart/${invoice.id?c}/edit" />" class="small">(modify)</a></#if></h3>

<@invoicecommon.printInvoice />

<div class="container row">
    <div class="span3">
        <h3>Billing Address</h3>
        <@common.printAddress invoice.address/>
    </div>

    <div class="span3">
        <h3>Transaction Info</h3>
        <b>Transaction Type: </b>${invoice.transactionType}<br/>
        <b>Transaction Status: </b>${invoice.transactionStatus}<br/>
    </div>    
</div>
</body>
</#escape>