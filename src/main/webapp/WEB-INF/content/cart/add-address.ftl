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

<@invoicecommon.printInvoice />

<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save-billing-address'>
<@s.hidden name="id" value="${invoice.id?c}" />
<#assign addressId = -1 />
<#if invoice.address?has_content && invoice.address.id?has_content>
 <#assign addressId =invoice.address.id />
</#if>
<h3>Select a Billing Address</h3>
<@common.listAddresses person=invoice.owner  choiceField="invoice.address.id" addressId=addressId />
    
    <@edit.submit fileReminder=false />

</@s.form>
</div>
</body>
</#escape>