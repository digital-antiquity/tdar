<head>
    <title>Error Occurred</title>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as common><#t>
</head>

<ul class="inline-menu hidden-desktop"><@common.loginMenu false/></ul>
<p>
</p>
<h2>An error within our system has occurred.</h2>
<p>
    An unhandled error occurred and has been logged. Please check the <a
        href='${bugReportUrl}'>issue tracker</a> to see if it has already been
    reported - if not, please enter the error messages listed below into the <a
        href='${bugReportUrl}'>issue tracker</a> along with any relevant details of
    what you were doing at the time of the error. Thanks, and apologies for the
    inconvenience. We'll try to fix this as soon as possible.
</p>
<p>
    If you have other concerns or questions or would like additional information, there are
    tutorials and documentation available on the <a
        href='${documentationUrl}'>${siteAcronym!'site'} wiki</a>. We also maintain a frequently
    monitored <a href='http://lists.asu.edu/cgi-bin/wa?A0=TDAR-DEV'>mailing list</a>
    where you can ask questions pertaining to the development or usage of the tDAR
    software platform.
</p>


