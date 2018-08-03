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
    <title>Confirm Selection: Login Required</title>
</head>

<body>
    <@invoicecommon.proxyNotice />

<h1>Confirm Selection: Login Required</h1>
<div class="row">
    <div class="col-5 " >
        <h3>Invoice Details</h3>
        <@invoicecommon.printInvoice />
        <h3>Invoice Summary</h3>
        <@invoicecommon.printSubtotal invoice/>
        <p></p>
    </div>
</div>
<#if sessionData.person?has_content>
	<@s.form action='process-payment-request' method='post'>
    <div class="form-actions">
    	<@s.token name='struts.csrf.token' />
        <button type='submit' class='btn btn-mini tdar-button'>Pay now</button>
    </div>
    </@s.form>        
<#else>
	<@auth.loginWarning />

    <div class="row">
        <div class="col-9" id="divRegistrationSection">
                <@s.form name='registrationForm' id='registrationForm' method="post" cssClass="disableFormNavigate form-condensed tdarvalidate"
                        enctype='multipart/form-data' action="process-registration" dynamicAttributes={"data-validate-method":"initRegForm"}>
                    <@s.token name='struts.csrf.token' />
                        <legend>Register</legend>
                        <div class="authpane">
                            <div class="authfields">
                                <@auth.registrationFormFields detail="minimal" cols=9 showSubmit=false />
                            </div>
                            <div class="form-actions">
                                <input type="submit" class='submitButton tdar-button' name="submitAction" id="cartRegister" value="Register and Continue">
                            </div>
                        </div>

                </@s.form>
        </div>

        <div class="col-3" id="divLoginSection">
            <@s.form name='loginForm' id='loginForm'  method="post" cssClass="disableFormNavigate form-condensed"
                    enctype='multipart/form-data' action="/cart/process-cart-login">
                    <legend>
                        Log In
                    </legend>
                <div class="authpane">
                    <div class="authfields">
                        <@auth.login showLegend=false>

                    </div>
                    <div class="form-actions">
                        <input type="submit" name="submitLogin" id="cartLogin" class="submitButton tdar-button" value="Login and Continue">
                    </div>
                </div>
                </@auth.login>
            </@s.form>
        </div>

    </div>
</#if>

</body>
</#escape>
