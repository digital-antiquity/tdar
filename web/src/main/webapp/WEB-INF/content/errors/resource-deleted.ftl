<head>
    <title>Access Denied</title>
</head>
<p>
    Sorry, the resource you have requested has either been marked as <em>deleted</em> or you do not have the appropriate rights to view or edit it.
<#if !authenticatedUser??>
    Note that deleted resources remain archived in ${siteAcronym}, and users with access rights to deleted documents
    may access these resources by logging into ${siteAcronym} prior to accessing this URL
<#else>
<#-- authenticated users that see this page do not have sufficient access, otherwise they are routed to the view page -->
    Note that deleted resources remain archived in ${siteAcronym}. If you feel this deletion was in error, or if you wish to request
    access to the deleted content, please <a href="${config.commentUrl}">contact us</a>.
</#if>
</p>
