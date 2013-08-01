<#-- fixme: refactor downloadcontroller so that id on querystring not assumed to be resourceFileId.  in this case, it's the resourceId -->
<#assign informationResourceId = informationResourceFileId />

<#assign download ="/filestore/downloadAllAsZip?informationResourceId=${informationResourceId?c}" />
<html>
<head>
    <title>Download All Files</title>
</head>
<body>
<div class="hero-unit">
    <h1>Download all Files</h1>
    <p>The download you requested will begin momentarily</p>
    <p>Your files are being prepared. Note that this process may take up to five minutes or longer for resources with large numbers of files.</p>
    <button type="button" class="btn btn-primary" onclick="console.log('closing');window.close()">Click here to close this window</button>
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