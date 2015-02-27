<#import "resource/common.ftl" as common>
<#macro scripts combine=false>
<!--[if lte IE 8]>
<script language="javascript" type="text/javascript" src="<@s.url value="/includes/jqplot-1.08/excanvas.js"/>"></script><![endif]-->

    <#if combine>
    <script type="text/javascript" src="${staticHost}${wroTempDirName}/${wroProfile}.js"></script>
    <#else>
        <#list javascriptFiles as src>
        <script type="text/javascript" src="${staticHost}${src}?buildId=${common.tdarBuildId}"></script>
        </#list>
    </#if>

</#macro>


<#macro css combine=true>
    <#if combine>
    <link rel="stylesheet" type="text/css" href="${staticHost}${wroTempDirName}/${wroProfile}.css"/>
    <#else>
        <#list cssFiles as src>
        <link rel="stylesheet" type="text/css" href="${staticHost}${src}?buildId=${common.tdarBuildId}" data-version="${common.tdarBuildId}">
        </#list>
    </#if>


</#macro>