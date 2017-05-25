<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
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
<h1>Review Your Purchase</h1>
<@s.form name='change-account' id='change-account'  method='post' cssClass="billing-account-choice form-condensed" enctype='multipart/form-data' action='process-billing-account-choice'>
<@edit.hiddenStartTime />
<div class="row">
    <#if accounts?has_content>
    <div class="span7" >
        <h3>Invoice Information</h3>
        <div class="cartpane" style="min-height:370px">
            <h3>Invoice Details</h3>
            <@invoicecommon.printInvoice />
            <h3>Invoice Summary</h3>
            <@invoicecommon.printSubtotal invoice/>
        </div>
    </div>
    <div class="span5">
        <h3>Choose A Billing Account</h3>
        <div class="cartpane" style="min-height: 370px">
            <div class="" >
                <@s.token name='struts.csrf.token' />
                <@s.hidden name="invoiceId" value="${invoice.id?c}" />

                <#if accounts?has_content>

                <div class="alert">
                    <@s.text name="cart.about_billing_accounts" />
                </div>
                <@s.select labelposition='top' label='Select Account' name='id' emptyOption="false" id="select-existing-account"
                list='%{accounts}'  listValue='name' listKey="id" title="Address Type" cssClass="input-xlarge" value="id" />
                </#if>
                <div class="add-new hidden">
                    <#-- NOTE: these should not be the account. variants as we want to not overwrite the values-->
                    <@s.textfield name="account.name" cssClass="input-xlarge" label="Account Name"/>
                    <@s.textarea name="account.description" cssClass="input-xlarge" label="Account Description"  cols="80"  rows="4" />
                    <p>Note: you can modify this account later to change the name, description, or specify who can charge it.</p>
                </div>
            </div>

        </div>
    </div>
    
        <#else>
            <div class="span12" >
                <h3>Invoice Information</h3>
                <div class="cartpane" style="min-height: 350px">
                    <div class="" >
                        <h3>Invoice Details</h3>
                        <@invoicecommon.printInvoice />
                        <h3>Invoice Summary</h3>
                        <@invoicecommon.printSubtotal invoice/>
                    </div>
                </div>
            </div>

    </#if>
</div>
<div class="row">
    <div class="span12">
        <h3>About Invoices and Accounts</h3>
        <p>
            In tDAR, billing accounts are used to manage resources. Each resource must be associated with an account. tDAR is run by Digital Antiquity, a
            not-for-profit organization dedicated to the preservation of archaeological information. The fees related to upload are used to ensure the proper
            preservation of materials uploaded to tDAR.
        </p>

        <#--<h3>Account Management</h3>-->
        <p>
            Accounts can be shared between users, and users can grant access to modify or manage resources to any tDAR user
            they choose.
        </p>

        <#if showContributorAgreement>
        <div class="alert alert-info">
            <strong>Contributer Features Required</strong>
            <p>Contributor-specific features are currently disabled in your user profile.  Please review and accept the
                <@s.a href="${contributorAgreementUrl}" target="_blank" title="click to open contributor agreement in another window">Contributor Agreement</@s.a>
                to enable these features and continue.
            </p>
            <label class="checkbox">
                <@s.checkbox theme="simple" name="acceptContributorAgreement" id="tou-id"  />
                I have read and accept the ${siteAcronym}
                <@s.a href="${contributorAgreementUrl}" target="_blank" title="click to open contributor agreement in another window">Contributor Agreement</@s.a>
            </label>

        </div>
        </#if>

        <div class="form-actions">
            <#if invoice.modifiable>
                <@s.a href="/cart/modify" cssClass="button muted">Modify This Invoice</@s.a>
            </#if>
            <@s.submit name="submit" value="Next Step: Payment" cssClass="tdar-button"/>

        </div>

    </div>
</div>
</@s.form>


<script>
    $(document).ready(function () {
        TDAR.pricing.initBillingChoice();
    });
</script>
</body>
</#escape>
