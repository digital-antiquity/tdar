<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<!--
vim:sts=2:sw=2:filetype=jsp
-->
<head>
<title>Login/Register</title>
<meta name="lastModifiedDate" content="$Date$" />

<style type="text/css">
label.error {display:block;}
</style>
</head>
<body>
<#if (actionErrors?? && actionErrors.size() gt 0) >
   <div class="errors action-errors">
     <@s.iterator var='err' value='actionErrors'>
      ${err}<br/>
     </@s.iterator>
   </div>
</#if>
<@s.actionmessage />
<#if sessionData.returnUrl?? && sessionData.returnUrl.contains("/filestore/") >
<div class="infoNote">
<b>Note:</b> Currently users must be logged-in to download materials.  Please login below, or signup for a free user account.
</div>
</#if>
 <@nav.loginForm />
<#include "/includes/ftl/notice.ftl">
</body> 

