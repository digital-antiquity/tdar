<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>

<head>
<title>Your cart</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>

<h1>Your Address</h1>

<div>
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save-billing-address'>
<@s.hidden name="id" value="${invoice.id}"/>

<@listAddress invoice.person.addresses />
<#if invoice.person.institution?has_content>
	<@listAddress invoice.person.institution.addresses />
</#if>

<#macro listAddress addresses>
<#assign addressId = -1 />
<#if invoice.address?has_content && invoice.address.id?has_content>
 <#assign addressId =invoice.address.id />
</#if>
<#list addresses  as address>
	<div class="controls-row">
		<input type="radio" name="invoice.address.id" label="${address.type}" value="${address.id}"  <#if address.id==addressId>checked=checked</#if>/>
	<@common.printAddress  address />
	</div>
</#list>
</#macro>
<div class="controls-row">
		<input type="radio" name="invoice.address.id" label="Other" value="-1"   <#if !invoice.address?has_content>checked=checked</#if> />
		<@s.textfield name="invoice.address.street1" cssClass="input-xlarge" label="Street" />
		<@s.textfield name="invoice.address.street2" cssClass="input-xlarge"  label="Street 2"/>
		<@s.textfield name="invoice.address.city" cssClass="input-xlarge"  label="City" />
		<@s.textfield name="invoice.address.state" cssClass="input-xlarge" label="State" />
		<@s.textfield name="invoice.address.postal" cssClass="input-xlarge"  label="Postal"/>
		<@s.textfield name="invoice.address.country" cssClass="input-xlarge"  label="Country" />
</div>
    <@edit.submit fileReminder=false />
</@s.form>

</div>

</body>
</#escape>
