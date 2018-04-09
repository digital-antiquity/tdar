<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<#if (informationResourceFileVersion??)>
    <#assign download ="/filestore/get/${informationResourceId?c}/${informationResourceFileVersion.id?c}">
    <#assign filename = informationResourceFileVersion.filename?html>
    <#assign versionid =  informationResourceFileVersion.id>
<#elseif (informationResourceId??)>
    <#assign download = "/filestore/download/${informationResourceId?c}">
    <#assign filename = "files-${informationResourceId?c}.zip">
    <#assign versionid = informationResourceId>
	</#if>
<html>
<head>
    <title>Download: ${filename?html}</title>
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
        <dd><a href="${download}" class="manual-download " id="manual-download" <#if config.shouldAutoDownload>data-auto-download</#if>
               data-versionid="${versionid?c}" >${filename}</a>
        </dd>
    </dl>
    <a class="btn btn-large btn-primary" href="<@s.url value="/dashboard" />">Return to dashboard</a>
    <hr/>    
    <@view.resourceCitationSection resource=informationResource />
    
</div>
<div class="row">
</div>

<script>
    $(function(){TDAR.download.setup()});
</script>

</body>
</#escape>