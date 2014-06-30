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
<h1>Register or Log In to tDAR</h1>
<#if !sessionData.person?has_content>
    <div class="row">
        <div class="span9" id="divRegistrationSection">
                <@s.form name='registrationForm' id='registrationForm' method="post" cssClass="disableFormNavigate"
                        enctype='multipart/form-data' action="process-registration">
                    <@s.token name='struts.csrf.token' />
                    <fieldset>
                        <legend>Register</legend>
                        <div class="authpane">
                            <div class="authfields">
                                <@auth.registrationFormFields detail="minimal" cols=9 showSubmit=false />
                            </div>
                            <div class="form-actions">
                                <input type="submit" class='btn submitButton' name="submitAction" value="Register and Continue">
                            </div>
                        </div>

                    </fieldset>
                </@s.form>
        </div>

        <div class="span3" id="divLoginSection">
            <@s.form name='loginForm' id='loginForm'  method="post" cssClass="disableFormNavigate"
                    enctype='multipart/form-data' action="/login/process-cart-login">
                <fieldset>
                    <legend>
                        Log In
                    </legend>
                </fieldset>
                <div class="authpane">
                    <div class="authfields">
                        <@auth.login showLegend=flase>

                    </div>
                    <div class="form-actions">
                        <input type="submit" name="submit" class="btn" value="Login and Continue">
                    </div>
                </div>
                </@auth.login>
            </@s.form>
        </div>
    </div>
</#if>

<div class="row">
    <div class="span9"></div>
    <div class="span3">
        <#if authenticatedUser?has_content>
            <p>prepared for: ${authenticatedUser.properName}</p>
        </#if>
        <@invoicecommon.proxyNotice />
        <@invoicecommon.printSubtotal invoice />
        <p><b>Payment by:</b><@s.text name="${invoice.paymentMethod.localeKey}"/></p>
    </div>
</div>

</body>
</#escape>
