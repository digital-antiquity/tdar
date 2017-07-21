<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/common-auth.ftl" as auth>

<head>
    <title>Confirm Selection: Login Required</title>
</head>

<body>

<h1>Send a Message: <span class="red">Login Required</span></h1>
<h3>${resource.title} (${resource.id?c})</h3>
<br/>
<p>To send the owner of this record a message, you need to login or register. Creating an account in ${siteAcronym} is free and easy.
	<@auth.loginWarning />

    <div class="row">
        <div class="span9" id="divRegistrationSection">
                <@s.form name='registrationForm' id='registrationForm' method="post" cssClass="disableFormNavigate form-condensed tdarvalidate"
                        enctype='multipart/form-data' action="process-request-registration" dynamicAttributes={"data-validate-method":"initRegForm"}>
                    <@s.token name='struts.csrf.token' />
                        <legend>Register</legend>
                        <div class="authpane">
                            <div class="authfields">
                                <@auth.registrationFormFields detail="minimal" cols=9 showSubmit=false beanPrefix="requestUserRegistration"/>
                                <@_extraFields />

                            </div>
                            <div class="form-actions">
                                <input type="submit" class='submitButton tdar-button' name="submitAction" value="Register and Continue">
                            </div>
                        </div>

                </@s.form>
        </div>

        <div class="span3" id="divLoginSection">
            <@s.form name='loginForm' id='loginForm'  method="post" cssClass="disableFormNavigate form-condensed"
                    enctype='multipart/form-data' action="process-request-login">
                    <legend>
                        Log In
                    </legend>
                <div class="authpane">
                    <div class="authfields">
                        <@auth.login showLegend=false>
                        <@_extraFields />

                    </div>
                    <div class="form-actions">
                        <input type="submit" name="submitLogin" class="submitButton tdar-button" value="Login and Continue">
                    </div>
                </div>
                </@auth.login>
            </@s.form>
        </div>

    </div>

</body>

<#macro _extraFields>
    <@s.hidden name="id" />
    <@s.hidden name="type" />
    <@s.hidden name="messageBody" value="${(messageBody!'')?url}" />
</#macro>
</#escape>
