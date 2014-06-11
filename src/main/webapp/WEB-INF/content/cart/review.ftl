<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/content/cart/common-invoice.ftl" as invoicecommon >

<head>
    <title>Review Billing Information</title>

</head>
<body>
<h1>Invoice <span class="small">{${invoice.transactionStatus.label}}</span></h1>
    <@s.form name='MetadataForm' id='MetadataForm'  method="post" cssClass="form-horizontal disableFormNavigate" enctype='multipart/form-data' action="process-registration">
        <#--<@s.hidden name="id" value="${invoice.id?c!-1}" />-->
        <@s.token name='struts.csrf.token' />
        <@invoicecommon.proxyNotice />

        <@invoicecommon.printInvoice />
        <#if invoice.owner??>
        <#if invoice.owner.addresses?has_content>
        <h3>Choose an existing address</h3>
            <#assign addressId = ""/>
            <#if invoice.address?has_content><#assign addressId=invoice.address.id /></#if>
            <@s.select name="invoice.address.id" listValue="addressSingleLine" listKey="id" emptyOption="true" label="Address" list="invoice.owner.addresses"  value="${addressId}"
            headerKey="-1" headerValue="(optional)" />

        </#if>
        </#if>
        <#if authenticatedUser??>
        <h3>Choose Payment Method</h3>
            <@invoicecommon.paymentMethod includePhone=false />
        <#else>
            <#assign return="/cart/finalreview">
            <a href="<@s.url value="/account/new?url=${return?url}" />" class="button" rel="nofollow">Sign Up</a>
            <@common.loginButton class="button" returnUrl=return />
            
        </#if>
        <#-- person registration form, consider macroing it if we want to maintain uniformity between this and the
        "real" registration form -->
    <div class="row">
        <div class="span5">
            <@s.textfield spellcheck="false" required=true id='firstName' label='First name'  name='person.firstName' cssClass="required input-xlarge" />
        </div>
        <div class="span4">
            <@s.textfield spellcheck="false" required=true id='lastName' label='Last name' name='person.lastName' cssClass="required input-xlarge" />
        </div>
    </div>
    <div class="row">
        <div class="span5">
            <@s.textfield spellcheck="false" required=true id='emailAddress' label="Email address" name="person.email" cssClass="required email input-xlarge" />
        </div>
        <div class="span4">
            <@s.textfield spellcheck="false" required=true id='confirmEmail' label="Confirm email" name="confirmEmail" cssClass="required email input-xlarge"/>
        </div>
    </div>

    <div class="row">
        <div class="span5">
            <@s.textfield labelposition='left' label='Organization' name='institutionName' id='institutionName' cssClass="input-xlarge"/>
        </div>
    </div>

    <div class="row">
        <div class="span5">
        <#-- listValueKey="localeKey"	       theme="tdar" -->
		<@s.select list="userAffiliations" name="affilliation" label="Affiliation / Interest" listValue="label" headerKey="" headerValue="Select Affiliation"
        />
        </div>
    </div>
    <@s.textfield spellcheck="false" required=true id='username' label="Username" name="person.username" cssClass="required username input-xlarge" />
    <div class="row">
        <div class="span5">
            <@s.password required=true label='Password' name='password' id='password'  cssClass="required input-xlarge" autocomplete="off" />
        </div>
        <div class="span4">
            <@s.password required=true label='Confirm password' name='confirmPassword' id='confirmPassword'  cssClass="required input-xlarge" autocomplete="off" />
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">Terms of Use</label>
        <div class="controls">
            <span class="help-block">  </span>
            <label class="checkbox">
                <@s.checkbox theme="simple" name="acceptTermsOfUse" id="tou-id"  />
                I have read and accept the ${siteAcronym}
                <@s.a href="tosUrl" target="_blank" title="click to open contributor agreement in another window">User Agreement</@s.a>.
            </label>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">Contributor Agreement</label>
        <div class="controls">
            <span class="help-block">Check this box if you will be contributing resources and/or resource metadata to ${siteAcronym}. You may change this setting at any time.
            </span>
            <label class="checkbox">
                <@s.checkbox theme="simple" name="requestingContributorAccess" id="contributor-id"  />
                I accept the ${siteAcronym}
                <@s.a href="contributorAgreementUrl" target="_blank" title="click to open contributor agreement in another window">Contributor Agreement</@s.a>
                and wish to add ${siteAcronym} content.
            </label>
        </div>
    </div>
    <div id='contributorReasonTextArea'>
        <label class="control-label">Contributor information</label>
        <div class="control-group">
            <div class="controls">
                <span class="help-block">
                    Please briefly describe the geographical areas, time periods, or other subjects for which you
                    would like to contribute information
                </span>
                <@s.textarea theme="simple" rows=6 cssClass="input-xxlarge" name='contributorReason' id='contributorReasonId' />
            </div>
        </div>
    </div>

    <#--
    <#if h.recaptcha_public_key??>
        <script type="text/javascript" src="http://api.recaptcha.net/challenge?k=${h.recaptcha_public_key}"></script>
    </#if>

    <@s.hidden name="h.timeCheck"/>
    <textarea name="h.comment" class="tdarCommentDescription"></textarea>

    <#if h.reCaptchaText?has_content>
    ${h.reCaptchaText}
    </#if>
    -->
    <div class="form-actions">
        <input type="submit" class='btn btn-primary  submitButton' name="submitAction" value="Register">
    </div>
    </@s.form>
</body>
</#escape>
