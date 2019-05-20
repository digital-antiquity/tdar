<#import "common.ftl" as common>
<#macro scripts combine=false>

    <script type="text/javascript" src="/components/axios/dist/axios.min.js"></script>
    <#if combine>
    <script type="text/javascript" src="${staticHost}${wroTempDirName}/${wroProfile}.js"></script>
    <#else>
        <#list javascriptFiles as src>
        <script type="text/javascript" src="${staticHost}${src}?buildId=${config.changesetId}"></script>
        </#list>
    </#if>

</#macro>

<#macro outdatedBrowserWarning>
<#-- If browser is obsolete (ie8 or lower), show warning w/ upgrade link at top of page -->
<#-- This script also works for detecting obsolete versions of other browsers, but for now we limit to IE -->
<!--[if !IE 8]><!-->
<script>
    var $buoop = {vs:{i:8,f:50,o:12.1,s:7},c:2, reminder:0};
    //http://browser-update.org/customize.html
    function $buo_f(){
        var e = document.createElement("script");
        e.src = "//browser-update.org/update.min.js";
        document.body.appendChild(e);
    };
    try {document.addEventListener("DOMContentLoaded", $buo_f,false)}
    catch(e){window.attachEvent("onload", $buo_f)}
</script>
<!--<![endif]-->
</#macro>


<#macro css combine=true>
        <#if combine>
    <link rel="stylesheet" type="text/css" href="${staticHost}${wroTempDirName}/${wroProfile}.css" />
    <#else>
        <#list cssFiles as src>
        <link rel="stylesheet" type="text/css" href="${staticHost}${src}?buildId=${config.changesetId}" data-version="${config.changesetId}" />
        </#list>
    </#if>


</#macro>