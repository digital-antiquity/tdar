<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/content/cart/common-invoice.ftl" as invoicecommon >
    <#import "/WEB-INF/content/billing/common-account.ftl" as accountcommon >
    <#import "/WEB-INF/macros/common-auth.ftl" as auth>

<head>
    <title>Review</title>
</head>
<body>
<div>
    <@invoicecommon.proxyNotice />
</div>
<h1>Review your purchase</h1>
<div class="row">
    <div class="span5 " >
        <h3>Invoice Details</h3>
        <@invoicecommon.printInvoice />
        <h3>Invoice Summary</h3>
        <@invoicecommon.printSubtotal invoice/>
    </div>

    <div class="span7">
        <h3>Choose A Billing Account</h3>
        <div class="cartpane">
        <@s.form name='change-account' id='change-account'  method='post' cssClass="form-horizontal billing-account-choice" enctype='multipart/form-data' action='process-billing-account-choice'>
            <@s.token name='struts.csrf.token' />
            <@s.hidden name="invoiceId" value="${invoice.id?c}" />
            <#if accounts?has_content>
            <div class="alert alert-info">
                <@s.text name="cart.about_billing_accounts" />
            </div>
            <@s.select labelposition='top' label='Select Account' name='id' emptyOption="false" id="select-existing-account"
            list='%{accounts}'  listValue='name' listKey="id" title="Address Type" cssClass="input-xlarge" value="id" />
            </#if>
            <div class="add-new hidden">
                <h3>Create a new account</h3>
                <#-- NOTE: these should not be the account. variants as we want to not overwrite the values-->
                <@s.textfield name="account.name" cssClass="input-xlarge" label="Account Name"/>
                <@s.textarea name="account.description" cssClass="input-xlarge" label="Account Description"/>
                <p>Note: you can modify this account later to change the name, description, or specify who can charge it</p>
            </div>

            <@s.submit name="submit" value="Next Step: Payment" cssClass="btn btn-mini tdar-button"/>
        </@s.form>
        </div>

    </div>
</div>

<script>
    $(document).ready(function () {
        TDAR.pricing.initBillingChoice();
    });
</script>
</body>
</#escape>
