<html>
<head>
    <title>Error Occurred</title>
<#import "/WEB-INF/macros/common-auth.ftl" as common><#t>
</head>
<body>


<ul class="inline-menu hidden-desktop"><@common.loginMenu false/></ul>
<p>
    Ack, an unhandled error occurred! Please enter the following error messages, if
    any, into our <a href='${config.bugReportUrl}'>issue tracker</a> along with a
    description of what you were doing at the time of the error. Thanks!
</p>
<@s.actionerror />
</p>

</body>
</html>