<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<title>Your cart</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>

<h1>Select Account</h1>

<div>
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save'>
    <@s.select labelposition='top' label='Select Existing Account' name='id' 
         list='%{accounts}'  listValue='name' listKey="id" title="Address Type" />

    <h3>Or... create a new one</h3>
    <@s.textfield name="account.name" cssClass="input-xlarge" label="Account Name"/>
    <@s.textarea name="account.description" cssClass="input-xlarge" label="Account Description"/>

    <@s.hidden name="invoiceId" />    
    <h3>Who can charge to this account</h3>
    ...
    <br/>
    <@edit.submit fileReminder=false />
</@s.form>

</div>

</body>
</#escape>
