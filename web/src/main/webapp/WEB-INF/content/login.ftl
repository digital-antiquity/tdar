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
<div class="alert alert-warning pt-4">
    <button type="button" class="close" data-dismiss="alert">×</button>
    <strong>Note:</strong>You must be logged-in to download materials. Please log in below, or signup for a free user account.
</div>
</#if>
<#assign number = .now?long % 12 + 1/>

<div class="row login-image image${number}">
<div class="col-8"></div>
<div class="col-4 ml-0 pl-0 pb-5 pt-5">
<div class="card ">
<div class="card-body">
<h1>Log in to ${siteAcronym}</h1>
<@s.form id='loginForm' method="post" action="/login/process" cssClass="form-horizontal">
	<@auth.loginWarning />
    <@auth.login>
    <@s.hidden name="url" />
    <div class="form-actions">
                <button type="submit" class="button btn btn-primary input-small submitButton float-right" name="_tdar.Login" id="btnLogin">Login</button>
                <p class="mt-2 ">Need an account? <a class=" " href='<@s.url value="/account/new"/>' rel="nofollow">Register </a></p>
                
    </div>
    </@auth.login>
</@s.form>
    <div id="error"></div>
</div>
</div>
</div>

</div>
<#include "/WEB-INF/notice.ftl">
<script>
$(document).ready(function() {
setTimeout(200,function() {$("#loginUsername").focus();});
});
</script>
</body>

