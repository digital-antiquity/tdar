<#assign download ="/filestore/${informationResourceFileId?c}" />
<html>
<head>
<title>Download: ${fileName?html}</title>
</head>
<body>
<div class="hero-unit">
    <h1>Welcome Back</h1>
    <p>The download you requested will begin momentarily</p>
    <dl class="dl-horizontal">
        <dt>Requested File</dt>
        <dd><a href="${download}" class="manual-download" >${fileName?html}</a></dd>
    </dl>
    <p>
    You've reached this page because you requested a file download when you were not logged into ${siteAcronym}.  If your download does not begin automatically,
    or if you would like to download the file again,  please click on the link above.
    </p>
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
        
        var DOWNLOAD_WAIT_SECONDS = 4;
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