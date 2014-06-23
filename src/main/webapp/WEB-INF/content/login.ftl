<!--
vim:sts=2:sw=2:filetype=jsp
-->
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/common.ftl" as common>

<head>
    <title>Log in / Register</title>
    <meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>
<#if sessionData.returnUrl?? && sessionData.returnUrl.contains("/filestore/") >
<div class="alert alert-warning">
    <button type="button" class="close" data-dismiss="alert">×</button>
    <strong>Note:</strong>You must be logged-in to download materials. Please log in below, or signup for a free user account.
</div>
</#if>
<h1>Log in to ${siteAcronym}</h1>

<div class="well">
<#assign formAction = nav.getFormUrl("/login/process") >
<@s.form id='loginForm' method="post" action="${formAction}" cssClass="form-horizontal}">
    <input type="hidden" name="url" value="${Parameters.url!''}"/>
    <@s.token name='struts.csrf.token' />
    <@common.antiSpam />
    <@s.textfield spellcheck="false" id='loginUsername' name="userLogin.loginUsername" label="Username" cssClass="required" autofocus="autofocus"/>
    <@s.password id='loginPassword' name="userLogin.loginPassword" label="Password" cssClass="required" />

    <@s.hidden name="returnUrl" />
    <div class="form-actions">
        <button type="submit" class="button btn btn-primary input-small submitButton" name="_tdar.Login" id="btnLogin">Login</button>
        <div class="pull-right">
            <div class="btn-group">
                <a class="btn " href='<@s.url value="/account/new"/>' rel="nofollow">Register </a>
                <a class="btn " href='<@s.url value="/account/recover"/>' rel="nofollow">Reset Password</a>
            </div>
        </div>
    </div>
</@s.form>
    <div id="error"></div>
</div>
<#include "/${themeDir}/notice.ftl">
    <script type="text/javascript">
        $(document).ready(function () {
            TDAR.auth.initLogin();
        });
    </script>

</body>

