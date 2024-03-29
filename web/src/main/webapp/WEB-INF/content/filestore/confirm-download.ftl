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


    <div class="card" style="background-color: #f4d35e">
        <div class="card-body">
            <h2 class="card-title">A Message From Our Director, Dr. Christopher Nicholson</h2>
            <p><strong>Please <a href="https://www.asufoundation.org/colleges-and-programs/schools-and-colleges/the-college-of-liberal-arts-and-sciences/center-for-digital-antiquity-fund-CA103777.html" onclick="TDAR.common.outboundAppeal('landing');" target="_blank">make a gift now</a> to ensure ${siteAcronym}'s future.</strong></p>
            <p>Your gift is invested back into ${siteAcronym}'s infrastructure to ensure this community-supported archive is sustainable!</p>

            <p>
                Thank you for your partnership! —
                Chris
            </p>
        </div>
    </div>

    <hr />
    <p>If your download does not begin
        automatically, or if you would like to download the file again, please click on the link below.
    <dl class="dl-horizontal">
        <dt>Requested File</dt>
        <dd><a href="${download}" class="manual-download " id="manual-download" <#if config.shouldAutoDownload>data-auto-download</#if>
               data-versionid="${versionid?c}" >${filename}</a>
        </dd>
    </dl>
    <a class="btn btn-large btn-primary" href="<@s.url value="/dashboard" />">Return to dashboard</a>
    <hr/>    
<#--    <@view.resourceCitationSection resource=informationResource />-->
    
</div>
<div class="row">
</div>

<script>
    $(function(){TDAR.download.setup()});
</script>

</body>
</#escape>
