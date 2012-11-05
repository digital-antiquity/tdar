<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<title>Your cart</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>

<h1>Billing Account</h1>

<div>
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save'>

	<@s.textfield name="account.name" cssClass="input-xlarge" label="Account Name"/>
	<@s.textarea name="account.description" cssClass="input-xlarge" label="Account Description"/>
	
	<h3>Who can charge to this account</h3>
	...
	<br/>
    <@edit.submit fileReminder=false />
</@s.form>

</div>

</body>
</#escape>
