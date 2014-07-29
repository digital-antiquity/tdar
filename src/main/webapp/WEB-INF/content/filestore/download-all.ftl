<#escape _untrusted as _untrusted?html>

<#assign download ="/filestore/downloadAllAsZip?informationResourceId=${informationResourceId?c}" />
<html>
<head>
    <title>Download All Files</title>
</head>
<body>
<div class="hero-unit" >
    <h1>Download all Files</h1>

    <p>The download you requested will begin momentarily</p>
    <dl class="dl-horizontal">
        <dt>Requested File</dt>
        <dd>
            <a href="${download}" class="manual-download " id="manual-download" data-auto-download
               data-versionid="${informationResourceId?c}" >All files</a>
        </dd>
    </dl>

    <p>Your files are being prepared. Note that this process may take up to five minutes or longer for resources with large numbers of files.</p>
    <button type="button" class="btn btn-primary" onclick="console.log('closing');window.close()">Click here to close this window</button>
</div>
<div class="row">
</div>

<script>
    $(function () {
        //TDAR.download.setup('<@s.url value="${download}"/>', '${informationResourceId?c}');
        TDAR.download.setup();
    });
</script>
</body>
</#escape>