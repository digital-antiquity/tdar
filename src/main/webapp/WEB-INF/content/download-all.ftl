<#-- fixme: refactor downloadcontroller so that id on querystring not assumed to be resourceFileId.  in this case, it's the resourceId -->
<#assign informationResourceId = informationResourceFileId />

<#assign download ="/filestore/downloadAllAsZip?informationResourceId=${informationResourceId?c}" />
<html>
<head>
    <title>Download: ${fileName?html}</title>
</head>
<body>
<div class="hero-unit">
    <h1>Download all Files</h1>
    <p>The download you requested will begin momentarily</p>
    <dl class="dl-horizontal">
        <dt>Requested File</dt>
        <dd><a href="${download}" class="manual-download" >${fileName?html}</a></dd>
    </dl>
    <p>Your files are being prepared. Note that this process may take longer for resources with large numbers of files.</p>
    <a class="btn btn-large btn-primary" href="<@s.url value="/dashboard" />">Take me to my dashboard</a>
</div>
<div class="row">
</div>

<script>
    var _register = function() {
        TDAR.common.registerDownload('<@s.url value="${download}"/>', '${informationResourceId?c}');
    };

    var _autoDownload = function() {
        _register();
        document.location="${download?js_string}";
    };

    $(function(){

        var DOWNLOAD_WAIT_SECONDS = 1;
        var id =  setTimeout(_autoDownload,  DOWNLOAD_WAIT_SECONDS * 1000);

        //cancel auto-download if user beats us to the clock
        $('.manual-download').click(function() {
            clearTimeout(id);
            _register();
            return true;
        });
    });



</script>
</body>