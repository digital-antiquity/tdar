<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>

<head>
<title>Your address</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>

<h1>Your Address</h1>

<div>
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save-address'>
<@s.hidden name="id" value="${id?c}"/>
<@s.hidden name="addressId" value="${addressId!-1}"/>
<@s.hidden name="address.id" value="${address.id!-1}"/>

<div class="controls-row">
	<@s.radio labelposition='top' label='Address Type' name='address.type' 
	     list='%{allAddressTypes}'  listValue='label'  title="Address Type" />
		<@s.textfield name="address.street1" cssClass="input-xlarge" label="Street" />
		<@s.textfield name="address.street2" cssClass="input-xlarge"  label="Street 2"/>
		<@s.textfield name="address.city" cssClass="input-xlarge"  label="City" />
		<@s.textfield name="address.state" cssClass="input-xlarge" label="State" />
		<@s.textfield name="address.postal" cssClass="input-xlarge"  label="Postal"/>
		<@s.textfield name="address.country" cssClass="input-xlarge"  label="Country" />

		
</div>
    <@edit.submit fileReminder=false />
</@s.form>

</div>

</body>
</#escape>
