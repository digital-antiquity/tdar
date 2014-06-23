<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<head>
    <title>Register with ${siteName}</title>
    <style type="text/css">
        label.error {
            display: block;
        }
    </style>
</head>
<body>

<h1>${siteAcronym} Registration</h1>
<#assign action_ = nav.getFormUrl("/account/register") >

<@s.form id="accountForm" method="post" action="${action_}" cssClass="">
<@s.token name='struts.csrf.token' />

<div class="alert alert-block alert-error" style="display:none" id="error">
    <h4>Please correct the following issues with this submission</h4>
    <ul id="errorList"></ul>
</div>
    <input type="hidden" name="url" value="${Parameters.url!''}"/>
    <div class="well">
    <div class="pull-right">
        <b>Already Registered?</b><br/><a href="<@s.url value="/login" />">Login</a>
    </div>
    <p><b>There is no charge to become a registered user of ${siteAcronym}. As a registered user, you can:</b>
    <ul>
        <li>Download Documents, Data sets, Images, and Other Resources</li>
        <li>Bookmark Resources for future use</li>
    </ul>


    <@s.hidden name='personId' value='${person.id!-1}'/>
    <h3>About You</h3>

    <div class="row">
        <div class="span5">
            <@s.textfield spellcheck="false" required=true id='firstName' label='First name'  name='registration.person.firstName' cssClass="required input-xlarge" />
        </div>
        <div class="span4">
            <@s.textfield spellcheck="false" required=true id='lastName' label='Last name' name='registration.person.lastName' cssClass="required input-xlarge" />
        </div>
    </div>
    <div class="row">
        <div class="span5">
            <@s.textfield spellcheck="false" required=true id='emailAddress' label="Email address" name="registration.person.email" cssClass="required email input-xlarge" />
        </div>
        <div class="span4">
            <@s.textfield spellcheck="false" required=true id='confirmEmail' label="Confirm email" name="registration.confirmEmail" cssClass="required email input-xlarge"/>
        </div>
    </div>

    <#if privacyControlsEnabled>
        <div class="control-group">
            <div class="controls">
                <label class="checkbox">
                    <@s.checkbox theme="simple"  name="registration.isEmailPublic" id="isEmailPublic" value="false" />
                    Make email public?
                </label>
                <span class="help-block">
                    <span class="label label-important"> Note </span>  
                    Making your email address public will display it to anyone who visits ${siteAcronym}, this includes search engines, spammers, and visitors 
                    who are not logged in.
                </span>
            </div>
        </div>
    </#if>

    <div class="row">
        <div class="span5">
            <@s.textfield labelposition='left' label='Organization' name='registration.institutionName' id='institutionName' cssClass="input-xlarge"/>
        </div>
        <div class="span4">
            <@s.textfield label='Work phone' labelposition='left' name='registration.person.phone' id='phone' cssClass=" input-xlarge"/>
        </div>
    </div>

    <#if privacyControlsEnabled>
        <!-- hiding for DA-TDAR -->
        <div class="control-group">
            <div class="controls">
                <label class="checkbox">
                    <@s.checkbox theme="simple"  name="registration.isPhonePublic" id="isPhonePublic" value="false" />
                    Make phone public?
                </label>
                <span class="help-block">
                    <span class="label label-important"> Note </span>  
                    Making your phone number public will display it to anyone who visits ${siteAcronym}, this includes search engines, and visitors who are not 
                    logged in.
                </span>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">Please provide a brief description of yourself</label>

            <div class="controls">
                <@s.textarea theme="simple" rows=6  cssClass="input-xxlarge" name='registration.person.description' id='description-id' />
            </div>
        </div>
    </#if>

    <div class="row">
        <div class="span5">
        <#-- listValueKey="localeKey"	       theme="tdar" -->
		<@s.select list="userAffiliations" name="registration.affilliation" label="Affiliation / Interest" listValue="label"
        headerKey="" headerValue="Select Affiliation"
        />
        </div>

    </div>


    <h3>Register for ${siteAcronym}</h3>
    <@s.textfield spellcheck="false" required=true id='username' label="Username" name="registration.person.username" cssClass="required username input-xlarge" />


    <div class="row">
        <div class="span5">
            <@s.password required=true label='Password' name='registration.password' id='password'  cssClass="required input-xlarge" autocomplete="off" />
        </div>
        <div class="span4">
            <@s.password required=true label='Confirm password' name='registration.confirmPassword' id='confirmPassword'  cssClass="required input-xlarge" autocomplete="off" />
        </div>
    </div>


    <div class="control-group">
        <label class="control-label">Terms of Use</label>

        <div class="controls">
            <span class="help-block">  </span>
            <label class="checkbox">
                <@s.checkbox theme="simple" name="registration.acceptTermsOfUse" id="tou-id"  />
                I have read and accept the ${siteAcronym}
                <@s.a href="${tosUrl}" target="_blank" title="click to open contributor agreement in another window">User Agreement</@s.a>.
            </label>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label">Contributor Agreement</label>

        <div class="controls">
            <span class="help-block">Check this box if you will be contributing resources and/or resource metadata to ${siteAcronym}. You may change this setting at any time.
            </span>
            <label class="checkbox">
                <@s.checkbox theme="simple" name="registration.requestingContributorAccess" id="contributor-id"  />
                I accept the ${siteAcronym}
                <@s.a href="${contributorAgreementUrl}" target="_blank" title="click to open contributor agreement in another window">Contributor
                    Agreement</@s.a>
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
                <@s.textarea theme="simple" rows=6 cssClass="input-xxlarge" name='registration.contributorReason' id='contributorReasonId' />
            </div>
        </div>

    </div>


    <@common.antiSpam />

    <div class="form-actions">
        <input type="submit" class='btn btn-primary  submitButton' name="submitAction" value="Register">
        <!--        <p class="help-block">
                    <small>By submitting the following form you <a href='#terms'>signify your
                    consent to the above terms and conditions.</a></small>
                </p> -->
    </div>


</div>

</@s.form>
<script type='text/javascript'>
    $(function () {
        TDAR.auth.initRegister(${registrationTimeout?c});
    });
</script>
</body>
