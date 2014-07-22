<#escape _untrusted as _untrusted?html>
<#assign download ="/filestore/${informationResourceFileVersion.id?c}" />
<html>
<head>
    <title>Download: ${informationResourceFileVersion.filename?html}</title>
</head>
<body>
<div class="hero-unit">
    <h1>Welcome Back</h1>

    <p>The download you requested will begin momentarily</p>
    <dl class="dl-horizontal">
        <dt>Requested File</dt>
        <dd><a href="${download}" class="manual-download">${informationResourceFileVersion.filename?html}</a></dd>
    </dl>
    <p>
        You've reached this page because you requested a file download when you were not logged into ${siteAcronym}. If your download does not begin
        automatically,
        or if you would like to download the file again, please click on the link above.
    </p>
    <a class="btn btn-large btn-primary" href="<@s.url value="/dashboard" />">Return to dashboard</a>
</div>
<div class="row">
</div>


<#if shouldAutoDownload>
<script>
    $(function () {
        TDAR.download.setup('<@s.url value="${download}"/>', '${informationResourceFileVersion.id?c}');
    });
</script>
</#if>

</body>
</#escape>