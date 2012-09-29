<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
<title>Register with the Digital Archaeological Record</title>
<meta name="lastModifiedDate" content="$Date$"/>
<style type="text/css">
label.error {display:block;}
label {width:14em;}
input[type=text],input[type=password] {margin-left:16em}
textarea {margin-left:150pt}
input[type=radio]+ label + input[type=radio] {margin-left:4em}
label.radio {width:4em !important}

.field {margin-left:16em !important}
textarea {width:32em}
</style>
<script type='text/javascript'>
$(function() {
  $('#contributor-id').click(function() {
    switchContributorReasonDisplay($(this).is(':checked'));
  });
  var contributor = $("#contributor-id").is(':checked');
  switchContributorReasonDisplay(contributor);
  $('#accountForm').validate({
    errorLabelContainer: 
        $("#error"),
    rules: {
      confirmEmail: {
        equalTo: "#emailAddress"
      },
      password: {
        minlength: 3
      },
      confirmPassword: {
        minlength: 3,
        equalTo: "#password"
      },
      'person.contributorReason': {
        maxlength: 512
      }
    },
    messages: {
      confirmEmail: {
        email: "Please enter a valid email address.",
        equalTo: "Your confirmation email doesn't match."
      },
      password: {
        required: "Please enter a password.",
        minlength: jQuery.format("Your password must be at least {0} characters.")
      },
      confirmPassword: {
        required: "Please confirm your password.",
        minlength: jQuery.format("Your password must be at least {0} characters."),
        equalTo: "Please make sure your passwords match."
      },
    },
  });
});
function switchContributorReasonDisplay(shouldDisplay) {
  $('#contributorReasonTextArea').toggle(shouldDisplay);
  $('#contributorReasonId').attr("disabled", ! shouldDisplay);
  if (shouldDisplay) {
    $('#contributorReasonId').focus();
  }
}
</script>
</head>
<body>
<#include "/${themeDir}/terms.ftl" />

<h2>Account Registration</h2>
<hr/>
<@s.form id="accountForm" method="post" action="register">

<@nav.showControllerErrors/>

<div class="glide">
    <@s.hidden name='personId' value='${person.id!-1}'/>
    
    <@s.textfield spellcheck="false" required='true' labelposition='left' label='First name' id='firstName' name='person.firstName' cssClass="required" size='25'/><br/>
    <@s.textfield spellcheck="false" required='true' labelposition='left' id='lastName' label='Last name' name='person.lastName' cssClass="required" size='25'/><br/>
    <@s.textfield spellcheck="false" required='true' labelposition='left' id='emailAddress' label="Email address" name="person.email" cssClass="required email" size='25'/><br/>
    <@s.textfield spellcheck="false" required='true' labelposition='left' id='confirmEmail' label="Confirm email address" name="confirmEmail" cssClass="required email" size='25'/><br/> 

	<#if privacyControlsEnabled>   
		<@s.checkbox labelposition='left' label='Make email public?' name="isEmailPublic" id="isEmailPublic" value="false" cssStyle='margin-left: 16em;'/><br/>
		<p class="field"><em><b>NOTE: </b> Making your email address public will display it to anyone who visits tDAR, this includes search engines, spammers, and visitors who are not logged in.</em></p>
	</#if>
	
    <@s.password required='true' label='Password' name='password' id='password' size='25' cssClass="required" autocomplete="off" /><br/>
    <@s.password required='true' label='Confirm password' name='confirmPassword' id='confirmPassword' size='25' cssClass="required" autocomplete="off" /><br/>
    <@s.textfield labelposition='left' label='Organization' name='institutionName' id='institutionName' size='25'/><br/>
    <@s.textfield label='Work phone' labelposition='left' name='person.phone' id='phone' size='25'/><br/>

 	<#if privacyControlsEnabled>
    	<@s.checkbox labelposition='left' label='Make phone public?' name="isPhonePublic" id="isPhonePublic" value="false" cssStyle='margin-left: 16em;'/><br/>
		<p class="field"><em><b>NOTE:</b> Making your phone # public will display it to anyone who visits tDAR, this includes search engines, and visitors who are not logged in.</em></p>
	</#if>    

 	<#if privacyControlsEnabled>
	 	<!-- hiding for DA-TDAR -->
	    <div>
	    <label for='description-id' style="line-height: 1.2em;">
	    Please provide a brief description of yourself:
	    </label>
	    <@s.textarea rows=6 cols='50' name='person.description' id='description-id' />
	    </div> 
    </#if>
    <@s.checkbox labelposition='left' label='Do you plan to contribute?' name="requestingContributorAccess" id="contributor-id" 
    value="true" cssStyle='margin-left: 16em;' />
    <div id='contributorReasonTextArea'>
    <br/>
    <label for='contributorReasonId' style="line-height: 1.2em;">
    Please briefly describe the geographical areas, time periods, or other
    subjects for which you would like to contribute information:
    </label>
    <@s.textarea rows=6 cols='50' name='person.contributorReason' id='contributorReasonId' />
    <br/>
    <p> 
    Are you a <a target='_blank' href='http://www.rpanet.org/'>Registered Professional Archaeologist?</a>
    </p>
    <@s.textfield labelposition='left' label='RPA Number' name='person.rpaNumber' id='rpaNumber' size='25'/>

    </div>
    <br/>
    
    <#if recaptcha_public_key??>
        <script type="text/javascript" src="http://api.recaptcha.net/challenge?k=${recaptcha_public_key}"><br /></script>
    </#if>

    <@s.hidden name="timeCheck"/>
    <textarea name="comment" class="tdarCommentDescription"></textarea>
    <div class='info'>By submitting the following form you <a href='#terms'>signify your
    consent to the above terms and conditions.</a></div>
    <div id="error"></div>
    <@s.submit cssStyle='margin-left: 16em;' value="Save" />
    </div>
</@s.form>
</body>
