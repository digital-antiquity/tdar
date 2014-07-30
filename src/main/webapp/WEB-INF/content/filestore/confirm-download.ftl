<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<#assign download ="/filestore/${informationResourceFileVersion.id?c}" />
<html>
<head>
    <title>Download: ${informationResourceFileVersion.filename?html}</title>
</head>
<body>
<div class="hero-unit">
    <h1>Welcome Back</h1>

    <p>
        You've reached this page because you requested a file download when you were not logged into ${siteAcronym}. If your download does not begin
        automatically, or if you would like to download the file again, please click on the link below.
    </p>
    <dl class="dl-horizontal">
        <dt>Requested File</dt>
        <dd><a href="${download}" class="manual-download " id="manual-download" <#if shouldAutoDownload>data-auto-download</#if>
               data-versionid="${informationResourceFileVersion.id?c}" >${informationResourceFileVersion.filename?html}</a>
        </dd>
    </dl>
    <a class="btn btn-large btn-primary" href="<@s.url value="/dashboard" />">Return to dashboard</a>
    <hr/>    
    <@view.resourceCitation informationResource />
    
</div>
<div class="row">
</div>

<script>
    $(function(){TDAR.download.setup()});
</script>

</body>
</#escape>