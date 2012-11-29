<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/common.ftl" as common>

<head>
<title>Your cart</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<h1>Invoice <span class="small">{${invoice.transactionStatus}}</span></h1>

<h3>Items <#if invoice.modifiable><a href="<@s.url value="/cart/${invoice.id?c}/edit" />" class="small">(modify)</a></#if></h3>
<table class="tableFormat">
<tr>
    <th>Item</th><th>Quantity</th><th>Cost</th><th>Subtotal</th>
</tr>
<#list invoice.items as item>
    <tr>
    <td>${item.activity.name}</td>
    <td>${item.quantity!0}</td>
    <td>${item.activity.price}</td>
    <td>${item.subtotal}</td>
    </tr>
</#list>
<tr>
    <td colspan=3><em>Total:</em></td><td>${invoice.calculatedCost!0}</td>
</tr>
</table>
<div class="container row">
<#if invoice.address?has_content>
    <div class="span3">
        <h3>Billing Address <#if invoice.modifiable><a href="<@s.url value="/cart/${invoice.id?c}/address" />" class="small">(modify)</a></#if></h3>
        <@common.printAddress invoice.address/>
    </div>

    <#if invoice.transactionType?has_content>
    <div class="span3">
        <h3>Transaction Info</h3>
        <b>Transaction Type: </b>${invoice.transactionType}<br/>
        <b>Transaction Status: </b>${invoice.transactionStatus}<br/>
    </div>    
<#else>
</div>
    <a class="button btn btn-primary submitButton" href="<@s.url value="/cart/${id?c}/credit" />">Pay</a>
</#if>

<#else>
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
        <input type="radio" name="invoice.address.id" label="${label}" value="${address.id}"  <#if address.id==addressId || !address.id?has_content && address_index==0>checked=checked</#if>/>
        <b><#if address.type?has_content>${address.type.label!""}</#if></b>
        </label><br/>
    </@common.printAddress>
    </div>
</#list>
    </div>
    <a class="button btn btn-primary submitButton" href="<@s.url value="/entity/person/${invoice.person.id?c}/address" />">Add a new address</a>

    <@edit.submit fileReminder=false />

</@s.form>
</#if>
</div>
</body>
</#escape>