<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/content/cart/common-invoice.ftl" as invoicecommon >
    <#import "/WEB-INF/macros/common-auth.ftl" as auth>

<head>
    <title>Review Billing Information</title>

</head>
<body>
<h1>Invoice <span class="small">{${invoice.transactionStatus.label}}</span></h1>
        <div class="row">
            <div class="span12">
                <@invoicecommon.proxyNotice />
                <@invoicecommon.printInvoice />
            </div>
        </div>


        <#-- person registration form, consider macroing it if we want to maintain uniformity between this and the
        "real" registration form -->
    <div class="row">
        <div class="span9" id="divRegistrationSection">
            <@s.form name='registrationForm' id='registrationForm'  method="post" cssClass=" disableFormNavigate"
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
</body>
</#escape>
