<!--
vim:sts=2:sw=2:filetype=jsp
-->
<#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
<#import "/WEB-INF/macros/common-auth.ftl" as auth>


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
<@s.form id='loginForm' method="post" action="/login/process" cssClass="form-horizontal">
	<@auth.loginWarning />
    <@auth.login>    
    ${url!"no url"}
    ${returnUrl!"no url"}
    <@s.hidden name="url" />
    <div class="form-actions">
        <button type="submit" class="button btn btn-primary input-small submitButton" name="_tdar.Login" id="btnLogin">Login</button>
        <div class="pull-right">
            <div class="btn-group">
                <a class="btn " href='<@s.url value="/account/new"/>' rel="nofollow">Register </a>
                <a class="btn " href='<@s.url value="/account/recover"/>' rel="nofollow">Reset Password</a>
            </div>
        </div>
    </div>
    </@auth.login>
</@s.form>
    <div id="error"></div>
</div>
<#include "/${config.themeDir}/notice.ftl">

</body>

