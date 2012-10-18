<!--
vim:sts=2:sw=2:filetype=jsp
-->
<@s.set name="theme" value="'bootstrap'" scope="request" />
<head>
    <title>Login/Register</title>
    <meta name="lastModifiedDate" content="$Date$" />
</head>
<body>
<@s.actionmessage />
 
<#if (actionErrors?size > 0)>
<div class="alert alert-error">
    <p>There were the following problems with your submission</p>
    <ul>
    <#list actionErrors as err>
        <li>${(err!"unspecified error")}</li>
    </#list>
    </ul>
</div>
</#if>

<#if sessionData.returnUrl?? && sessionData.returnUrl.contains("/filestore/") >
<div class="alert alert-warning">
    <button type="button" class="close" data-dismiss="alert">×</button>
    <strong>Note:</strong> Currently users must be logged-in to download materials.  Please login below, or signup for a free user account.
</div>
</#if>

<script type="text/javascript">
$(document).ready(function() {
  $('#loginForm').validate({
    messages: {
      loginUsername: {
        required: "Please enter your username."
      },
      loginPassword: {
        required: "Please enter your password."
      }
    },
    errorClass:'help-inline',
  highlight: function(label) {
    $(label).closest('.control-group').addClass('error');
  },
  success: function($label) {
    $label.closest('.control-group').removeClass('error').addClass('success');
  }
  
    });
  $('#loginUsername').focus();
  $('#loginUsername').bind("focusout",function() {
    var fld = $('#loginUsername');
    fld.val($.trim(fld.val()))});
});
</script>

<h1>Login to ${siteAcronym}</h1>
<@s.form id='loginForm' method="post" action="%{#request.contextPath}/login/process" cssClass="well form-horizontal">
    <input type="hidden" name="url" value="${Parameters.url!''}"/>
    <@s.textfield spellcheck="false" id='loginUsername' name="loginUsername" label="Username" cssClass="required" />
    <@s.password id='loginPassword' name="loginPassword" label="Password" cssClass="required" />
    <@s.checkbox  name="userCookieSet" label="Stay logged-in the next time I visit this page" />
    
    <div class="form-actions">
        <button type="submit" class="btn" name="Login">Login</button>
        <p class="pull-right">
            <a href='<@s.url value="/account/new"/>'>Register </a> |
            <a href='<@s.url value="/account/recover"/>'>Reset Password</a>
        </p>
    </div>
</@s.form>
<div id="error"></div>


<footer class="footer"> 
<#include "/${themeDir}/notice.ftl">
</footer>
</body>

