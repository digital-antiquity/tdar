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

<div class="container row">
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save-billing-address'>
<@s.hidden name="id" value="${invoice.id?c}" />
<#assign addressId = -1 />
<#if invoice.address?has_content && invoice.address.id?has_content>
 <#assign addressId =invoice.address.id />
</#if>
<div class="row">
	<#list invoice.person.addresses  as address>
	    <div class="span3">
	    <#assign label = ""/>
	    <#if address.type?has_content>
	    <#assign label = address.type.label>
	    </#if>
	    <@common.printAddress  address=address modifiable=true showLabel=false>
	        <label class="radio inline">
	        <input type="radio" name="invoice.address.id" label="${label}" value="${address.id}"  <#if address.id==addressId || (!addressId?has_content || addressId == -1) && address_index==0>checked=checked</#if>/>
	        <b><#if address.type?has_content>${address.type.label!""}</#if></b>
	        </label><br/>
	    </@common.printAddress>
	    </div>
	</#list>
    <div class="span3">
	    <a class="button btn btn-primary submitButton" href="<@s.url value="/entity/person/${invoice.person.id?c}/address?returnUrl=/cart/${id?c}" />">Add Address</a>
    </div>
</div>
    
    <@edit.submit fileReminder=false />

</@s.form>
</div>
</body>
</#escape>