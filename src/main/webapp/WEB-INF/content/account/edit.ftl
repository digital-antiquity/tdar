<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
<title>Register with ${siteName}</title>
<meta name="lastModifiedDate" content="$Date$"/>
<style type="text/css">
label.error {display:block;}
</style>
</head>
<body>
<#include "/${themeDir}/terms.ftl" />

<h2>Account Registration</h2>
<@s.form id="accountForm" method="post" action="register" cssClass="form-horizontal">

<div class="alert alert-block alert-error" style="display:none" id="error">
  <h4>Please correct the following issues with your submission</h4>
  <ul id="errorList"></ul>
</div>

<div class="well">
    <@s.hidden name='personId' value='${person.id!-1}'/>
    
    <@s.textfield spellcheck="false" required='true' id='firstName' label='First name'  name='person.firstName' cssClass="required" />
    <@s.textfield spellcheck="false" required='true' id='lastName' label='Last name' name='person.lastName' cssClass="required" />
    <@s.textfield spellcheck="false" required='true' id='username' label="Username" name="person.username" cssClass="required username input-xlarge" />
    <@s.textfield spellcheck="false" required='true' id='emailAddress' label="Email address" name="person.email" cssClass="required email input-xlarge" />
    <@s.textfield spellcheck="false" required='true' id='confirmEmail' label="Confirm email address" name="confirmEmail" cssClass="required email input-xlarge"/>

    <#if privacyControlsEnabled>   
        <div class="control-group">
            <div class="controls">
                <label class="checkbox">
                    <@s.checkbox theme="simple"  name="isEmailPublic" id="isEmailPublic" value="false" />
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
    
    <@s.password required='true' label='Password' name='password' id='password'  cssClass="required" autocomplete="off" />
    <@s.password required='true' label='Confirm password' name='confirmPassword' id='confirmPassword'  cssClass="required" autocomplete="off" />
    <@s.textfield labelposition='left' label='Organization' name='institutionName' id='institutionName' />
    <@s.textfield label='Work phone' labelposition='left' name='person.phone' id='phone' />

     <#if privacyControlsEnabled>
        <div class="control-group">
            <div class="controls">
                <label class="checkbox">
                    <@s.checkbox theme="simple"  name="isPhonePublic" id="isPhonePublic" value="false" />
                    Make phone public?
                </label>
                <span class="help-block">
                    <span class="label label-important"> Note </span>  
                    Making your phone number public will display it to anyone who visits ${siteAcronym}, this includes search engines, and visitors who are not 
                    logged in.
                </span>
            </div>
        </div>
    </#if>    

     <#if privacyControlsEnabled>
         <!-- hiding for DA-TDAR -->
         <div class="control-group">
             <label class="control-label">Please provide a brief description of yourself</label>

             <div class="controls">
                 <@s.textarea theme="simple" rows=6  cssClass="input-xxlarge" name='person.description' id='description-id' />
             </div> 
         </div>
    </#if>
    
    
    <div class="control-group">
        <div class="controls">
            <label class="checkbox">
                <@s.checkbox theme="simple" name="requestingContributorAccess" id="contributor-id" value="true" />
                Do you plan to contribute?
                <#-- Display resource creation options in menu? -->
            </label>
            <#--
            <span class="help-block">
                Check this option if you plan on contributing resources and/or resource metadata to ${siteAcronym}. You can change this option later.
            </span>
            -->
        </div>
    </div>
    <div id='contributorReasonTextArea'>
        <div class="control-group">
            <label for='contributorReasonId'  class="control-label initialism">
                <small>Please briefly describe the geographical areas, time periods, or other
                subjects for which you would like to contribute information</small>
            </label>
            <div class="controls">
                <@s.textarea theme="simple" rows=6 cssClass="input-xxlarge" name='person.contributorReason' id='contributorReasonId' />
            </div>
        </div>    

        <#if RPAEnabled>
        <div class="control-group">
            <label class="control-label" for="rpaNumber">RPA Number</label>
            <div class="controls">
                <@s.textfield theme="simple" name='person.rpaNumber' id='rpaNumber' />
                <span class="help-block"> 
                    Are you a <a target='_blank' href='http://www.rpanet.org/'>Registered Professional Archaeologist?</a>
                </span>
            </div>
        </div>
        </#if>
    </div>
    
    
    <#if recaptcha_public_key??>
        <script type="text/javascript" src="http://api.recaptcha.net/challenge?k=${recaptcha_public_key}"></script>
    </#if>

    <@s.hidden name="timeCheck"/>
    <textarea name="comment" class="tdarCommentDescription"></textarea>
    
    <div class="form-actions">
        <input type="submit" class='btn btn-primary  submitButton' name="submitAction" value="Save">
        <p class="help-block">
            <small>By submitting the following form you <a href='#terms'>signify your
            consent to the above terms and conditions.</a></small>
        </p>
    </div>
        

</div>

</@s.form>
<script type='text/javascript'>
$(function() {
  setTimeout(function() {
    alert("Your session has timed out, click ok to refresh the page.");
    location.reload(true);
  }, ${registrationTimeout?c});
  $('#contributor-id').click(function() {
    switchContributorReasonDisplay($(this).is(':checked'));
  });
  var contributor = $("#contributor-id").is(':checked');
  switchContributorReasonDisplay(contributor);
  
    
    TDAR.common.initRegformValidation("#accountForm");
 
  $('#firstName').focus();
  
});
function switchContributorReasonDisplay(shouldDisplay) {
  $('#contributorReasonTextArea').toggle(shouldDisplay);
  $('#contributorReasonId').attr("disabled", ! shouldDisplay);
//  if (shouldDisplay) {
//    $('#contributorReasonId').focus();
//  }
}
</script>
</body>
