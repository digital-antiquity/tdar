<#-- common authentication/authorization macros and functions -->
<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/common.ftl" as common>

<#--
 registrationForm:  render a user registration form
     verbosity:string  relative amount of detail to capture (minimal|extended|verbose)
     columns: maximum width consumed by this section, assuming 12-column grid layout
-->
<#macro registrationFormFields detail="minimal" cols=12 beanPrefix="reg" showSubmit=true source="cart">
    <@common.chromeAutofillWorkaround />
    <@antiSpam  />

    <#local level = 1/>
    <#local showMinimal = true />
    <#if detail == 'verbose'>
        <#local level=3 />
    </#if>
<#local showMinimal = (level == 1) />
<#local showVerbose = (3 <= level) />
<#local spanfull = "col-${cols}" />
<#local spanhalf = "col-${cols/2}"/>

<div class="row">
    <div class="col">
        <@s.textfield spellcheck="false" required=true id='username' label="Username"
            name="${beanPrefix}.person.username" cssClass="required username input-xlarge"
            dynamicAttributes={"data-rule-minlength":"5",  "data-msg-required":"Username is required.",
                "data-msg-minlength":"Username length must be at least 5 characters."} />
    </div>
</div>

<div class="row">
    <div class="col">
        <@s.textfield spellcheck="false" required=true id='firstName' label='First name'
            name='${beanPrefix}.person.firstName' cssClass="required input-xlarge"
            dynamicAttributes={
                "data-msg-required":"First name is required."
            }/>
    </div>
    <div class="col">
        <@s.textfield spellcheck="false" required=true id='lastName' label='Last name'
            name='${beanPrefix}.person.lastName' cssClass="required input-xlarge"
            dynamicAttributes={
                "data-msg-required":"Last name is required."
            }/>
    </div>
</div>

<div class="row">
    <div class="col">
        <@s.textfield spellcheck="false" required=true id='emailAddress' label="Email address"
        name="${beanPrefix}.person.email" cssClass="required email input-xlarge"
        dynamicAttributes={
            "data-msg-email":"Please enter a valid email address.",
            "data-msg-required":"Email address is required."
        }/>
    </div>
    <div class="col">
        <@s.textfield spellcheck="false" required=true id='confirmEmail' label="Confirm email"
            name="${beanPrefix}.confirmEmail" cssClass="required email input-xlarge"
            dynamicAttributes={
                "data-rule-equaltoignorecase":"#emailAddress",
                "data-msg-equaltoignorecase":"Your confirmation email doesn't match.",
                "data-msg-email":"Please enter a valid email address.",
                "data-msg-required":"Please confirm your email address."
            }/>
    </div>
</div>

<div class="row">
    <div class="col">
        <@s.textfield labelposition='top' label='Organization' name='${beanPrefix}.institutionName' id='institutionName' cssClass="input-xlarge"/>
    </div>
    <div class="col">
    <#-- listValueKey="localeKey"	       theme="tdar" -->
            <@s.select list="affiliations" name="${beanPrefix}.affiliation" label="Affiliation / Interest" listValue="label" headerKey=""
    headerValue="Select Affiliation"   />
</div>

</div>
<div class="row">
    <div class="col">
        <@s.password required=true label='Password' name='${beanPrefix}.password' id='password'
            cssClass="required input-xlarge" autocomplete="off"
            dynamicAttributes={
                "data-rule-minlength":"8",
                "data-msg-required":"Please enter a password.",
                "data-msg-minlength":"Your password must be at least 8 characters."
            }/>
    </div>
    <div class="col">
        <@s.password required=true label='Confirm password' name='${beanPrefix}.confirmPassword' id='confirmPassword'
        cssClass="required input-xlarge" autocomplete="off"
        dynamicAttributes={
            "data-rule-minlength":"8",
            "data-rule-equalto":"#password",
            "data-msg-required":"Please confirm your password.",
            "data-msg-minlength":"Your password must be at least 8 characters.",
            "data-msg-equalto":"Please make sure your passwords match."
        }/>
    </div>
</div>

<#if (level > 1)>
<div class="row">
    <div class="col-6">
        <@s.textfield label='Work phone' labelposition='top' name='${beanPrefix}.person.phone' id='phone' cssClass=" input-xlarge"/>
    </div>
</div>
</#if>

<div class="row">
    <div class="${spanfull}">
        <#if showMinimal>
            <label class="checkbox">
                <@s.checkbox theme="simple" name="${beanPrefix}.acceptTermsOfUseAndContributorAgreement" id="tou-id"  />
                I have read and accept the ${siteAcronym}
                <@s.a href="${config.tosUrl}" target="_blank" title="click to open contributor agreement in another window">User Agreement</@s.a> and
                <@s.a href="${config.contributorAgreementUrl}" target="_blank" title="click to open contributor agreement in another window">Contributor Agreement</@s.a>
            </label>
        <#else>
            <@tos beanPrefix=beanPrefix />
            <div class="control-group">
                <label class="control-label">Contributor Agreement</label>
                <div class="controls">
                <span class="help-block">Check this box if you will be contributing resources and/or resource metadata to ${siteAcronym}. You may change this setting at any time.
                </span>
                    <label class="checkbox">
                        <@s.checkbox theme="simple" name="${beanPrefix}.requestingContributorAccess" id="contributor-id"  />
                        I accept the ${siteAcronym}
                        <@s.a href="${config.contributorAgreementUrl}" target="_blank" title="click to open contributor agreement in another window">Contributor Agreement</@s.a>
                        and wish to add ${siteAcronym} content.
                    </label>
	<br/>
                <span class="help-block">Review our <a href="${config.privacyPolicyUrl}">privacy policy</a>.</span>
                    
                </div>
            </div>
        </#if>
        <div id='contributorReasonTextArea' class="hidden" >
            <label class="control-label">Contributor information</label>
            <div class="control-group">
                <div class="controls">
                <span class="help-block">
                    Please briefly describe the geographical areas, time periods, or other subjects for which you
                    would like to contribute information
                </span>
                    <@s.textarea theme="simple" rows=6 cssClass="input-xxlarge"
                        name='${beanPrefix}.contributorReason' id='contributorReasonId'  cols="80"
                        dynamicAttributes={
                            "data-rule-maxlength":"512",
                            "data-msg-maxlength": "Please limit your summary to less than 512 characters"
                        }
                    />
                </div>
            </div>
        </div>

        <#if showSubmit>
        <div class="form-actions">
            <input type="submit" class='btn btn-primary  submitButton' name="submitAction" value="Register">
        </div>
        </#if>
    </div>
</div>
</#macro>

<#-- print the login form 
	- showLegend (show the login header)
	- beanPrefix (the prefix for the login info bean)
-->
<#macro login showLegend=false beanPrefix="userLogin">

    <@antiSpam  />
    <#if showLegend>
        <legend>Login</legend>
    </#if>
    <@s.token name='struts.csrf.token' />
    <@s.textfield spellcheck="false" id='loginUsername' name="${beanPrefix}.loginUsername" label="Username" cssClass="required" autofocus="autofocus"  cssClass="col"/>
    <a class="label-right" href='<@s.url value="/account/recover"/>' rel="nofollow"><i>Forgot your password?</i></a>
    <@s.password id='loginPassword' name="${beanPrefix}.loginPassword" label="Password" cssClass="required" cssClass="col"/>

    <#nested />
    <script type="text/javascript">
        $(document).ready(function () {
            TDAR.auth.initLogin();
        });
    </script>
</#macro>

<#-- print the Terms of service with the specified bean prefix -->
<#macro tos beanPrefix>
    <span class="help-block">  </span>
    <label class="checkbox">
        <@s.checkbox theme="simple" name="${beanPrefix}.acceptTermsOfUse" id="tou-id"  />
        I have read and accept the ${siteAcronym}
        <@s.a href="${config.tosUrl}" target="_blank" title="click to open contributor agreement in another window">User Agreement</@s.a>.
    </label>
</#macro>

<#-- show a warning if authentication is disabled -->
<#macro loginWarning>
<#if !config.authenticationAllowed>
 <div class="alert alert-warning">
 <b>Login to ${siteAcronym} is temporarily unavailable due to system maintence. Please try again later.</b>
</div>
</#if>

</#macro>


<#--FIXME:  there has to be a better way here -->
    <#macro antiSpam>
        <#if h.recaptcha_public_key??>
        <script type="text/javascript" src="http://api.recaptcha.net/challenge?k=${h.recaptcha_public_key}"></script>
        </#if>
    
        <@s.hidden name="h.timeCheck"/>
        <textarea name="h.comment" class="tdarCommentDescription" style="display:none"></textarea>

        <#if h.reCaptchaText?has_content>
            ${h.reCaptchaText}
        </#if>
    </#macro>

    <#macro embeddedAntiSpam  bean="downloadRegistration">
        <#local actual = bean?eval />
        <#if actual.srecaptcha_public_key??>
        <script type="text/javascript" src="http://api.recaptcha.net/challenge?k=${actual.h.recaptcha_public_key}"></script>
        </#if>
    
        <@s.hidden name="${bean}.h.timeCheck"/>
        <textarea name="${bean}.h.comment" class="tdarCommentDescription" style="display:none"></textarea>

        <#if actual.h.reCaptchaText?has_content>
            ${actual.h.reCaptchaText}
        </#if>
    </#macro>


<#-- emit login menu list items -->
<#-- @param showMenu:boolean if true,  wrap list items in UL tag, otherwise just emit LI's -->
    <#macro loginMenu showMenu=false>
        <#if showMenu>
        <ul class="subnav-rht hidden-phone hidden-tablet">
        </#if>
        <#if !(authenticatedUser??) >
            <li><a href="<@s.url value="/account/new" />" class="button" rel="nofollow">Sign Up</a></li>
            <li><@loginButton class="button" /></li>
        <#else>
            <#--<li><a href="<@s.url value="/logout" />" class="button">Logout</a></li>-->
            <li>
            <form class="form-unstyled seleniumIgnoreForm logoutForm" id="frmLogout" name="logoutForm" method="post" action="/logout">
                    <input type="submit" class="tdar-button" name="logout" value="Logout" id="logout-button">
            </form>
            </li>
        </#if>
        <#if showMenu>
        </ul>
        </#if>
    </#macro>



<#--
    Emit login button link.
    If current page is home page, link has no querystring arguments.  Otherwise,  include the current url in the
    querystring (in parameter named 'url).
-->
    <#macro loginButton class="" returnUrl="">
        <#noescape>
        <#local _current = (currentUrl!'/') >
        <#if returnUrl != ''><#local _current = returnUrl /></#if>
        <#if _current == '/' || currentUrl?starts_with('/login')>
        <a class="${class}" href="<@s.url value='/login'/>" rel="nofollow" id="loginButton">Log In</a>
        <#else>
        <a class="${class}" rel="nofollow" href="<@s.url value='/login'><@s.param name="url">${_current}</@s.param></@s.url>" id="loginButton">Log In</a>
        </#if>
        </#noescape>
    </#macro>


    <#function loginLink returnUrl="">
        <#noescape>
        <#local _current = (currentUrl!'/') />
        <#if returnUrl != ''><#local _current = returnUrl /></#if>
        <#if _current == '/' || currentUrl?starts_with('/login')>
                <#return '/login' />
                <#else>
                <#local ret><@s.url value='/login'><@s.param name="url">${_current}</@s.param></@s.url></#local>
                    <#return ret />
        </#if>
        </#noescape>
    </#function>

</#escape>