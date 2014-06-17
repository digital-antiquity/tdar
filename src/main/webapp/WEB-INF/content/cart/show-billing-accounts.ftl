<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/content/billing/common-account.ftl" as accountcommon>
<#import "/WEB-INF/content/cart/common-invoice.ftl" as invoicecommon >


<head>
    <title>Select a Billing Account</title>
</head>
<body>

<h1>Select a Billing Account</h1>

<div>
    <@s.form name='metadataForm' id='metadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='process-billing-account-choice'>
        <@s.token name='struts.csrf.token' />
        <@s.select labelposition='top' label='Select Existing Account' name='id' emptyOption="true"
        list='%{accounts}'  listValue='name' listKey="id" title="Address Type" />

        <h3>Or... create a new one</h3>
        <#-- NOTE: these should not be the account. variants as we want to not overwrite the values-->
        <@s.textfield name="name" cssClass="input-xlarge" label="Account Name"/>
        <@s.textarea name="description" cssClass="input-xlarge" label="Account Description"/>

        <@accountcommon.accountInfoForm hideUsers=false />

    </@s.form>

</div>

    <@edit.personAutocompleteTemplate />

<script type="text/javascript">
$(function () {
    "use strict";
    TDAR.common.initEditPage($('#MetadataForm')[0]);
    TDAR.autocomplete.delegateCreator('#accessRightsRecords', true, false);
});
</script>
</body>
</#escape>
