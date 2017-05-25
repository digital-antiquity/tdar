<head>
    <title>Error Occurred</title>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as common><#t>
</head>


<ul class="inline-menu hidden-desktop"><@common.loginMenu false/></ul>
<p>
    Ack, an unhandled error occurred! Please enter the following error messages, if
    any, into our <a href='${bugReportUrl}'>issue tracker</a> along with a
    description of what you were doing at the time of the error. Thanks!
</p>
<@s.actionerror />
</p>

