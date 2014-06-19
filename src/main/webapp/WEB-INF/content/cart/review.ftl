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
    <title>Review Billing Information</title>

</head>
<body>
<h1>Invoice <span class="small">{${invoice.transactionStatus.label}}</span></h1>
        <div class="row">
            <div class="span6">
                <@invoicecommon.proxyNotice />
                <@invoicecommon.printInvoice />
                <p><b>Payment by:</b><@s.text name="${invoice.paymentMethod.localeKey}"/></p>
            </div>
            <div class="span6">
    <#if accounts?has_content>
        <@s.form name='metadataForm' id='metadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='process-billing-account-choice'>
        <@s.token name='struts.csrf.token' />
        <@s.select labelposition='top' label='Select Existing Account' name='id' emptyOption="true"
        list='%{accounts}'  listValue='name' listKey="id" title="Address Type" />

FIXME:: allow user to select value that shows this section
        <h3>Or... create a new one</h3>
        <#-- NOTE: these should not be the account. variants as we want to not overwrite the values-->
        <@s.textfield name="name" cssClass="input-xlarge" label="Account Name"/>
        <@s.textarea name="description" cssClass="input-xlarge" label="Account Description"/>
        <p>Note: you can modify this account later to change the name, description, or specify who can charge it</p>
        
        <@s.submit name="submit" value="submit" />
        </@s.form>
    </#if>
            
            </div>
        </div>


<#if !sessionData.person?has_content>
    <div class="row">
        <div class="span9" id="divRegistrationSection">
            <@s.form name='registrationForm' id='registrationForm' method="post" cssClass=" disableFormNavigate"
                    enctype='multipart/form-data' action="process-registration">
                <@s.token name='struts.csrf.token' />
                <fieldset>
                    <legend>Register</legend>
                    <@auth.registrationFormFields detail="minimal" cols=9 />
                </fieldset>
            </@s.form>

        </div>

        <div class="span3" id="divLoginSection">
            <@s.form name='registrationForm' id='registrationForm'  method="post" cssClass="disableFormNavigate"
                    enctype='multipart/form-data' action="/login/process-cart-login">
                <@s.token name='struts.csrf.token' />
                <fieldset>
                    <legend>Login:</legend>
                    <label>Username</label>
                    <input type="text" name="loginUsername" placeholder="Enter username">
                    <label>Password</label>
                    <input type="password" name="loginPassword">
                    <div class="form-actions">
                        <input type="submit" name="submit" class="btn btn-large" value="Login and Continue">
                    </div>

                </fieldset>
            </@s.form>

        </div>
    </div>
    <#else>
    <#if !accounts?has_content>
        <a href="<@s.url value="process-payment-request"/>" class="button btn btn-primary">Next</a>
    </#if>
</#if>
</body>
</#escape>
