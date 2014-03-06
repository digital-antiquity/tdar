<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "common-account.ftl" as accountcommon>
<#import "/WEB-INF/macros/resource/common.ftl" as common>

<head>
<title>${account.name!"Your account"}</title>
</head>
<body>

<h1>Billing Account</h1>

<div>
<div class="well">
Note: you may have multiple accounts to simplify billing and allow different people to charge to different accounts within an organization.
</div>
<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save'>
    <@common.jsErrorLog />
    <@s.textfield name="account.name" cssClass="input-xlarge" label="Account Name"/>
    <@s.textarea name="account.description" cssClass="input-xlarge" label="Account Description"/>

    <@s.hidden name="id" value="${account.id?c!-1}" />    
	<@accountcommon.accountInfoForm />
</@s.form>

</div>
<script>
$(document).ready(function(){
    'use strict';
    TDAR.common.initEditPage($('#MetadataForm')[0]);
    TDAR.common.delegateCreator('#accessRightsRecords', true, false);
});
</script>

<@edit.personAutocompleteTemplate />

</body>
</#escape>
