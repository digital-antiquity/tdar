<#import "resource/common.ftl" as common>
<#macro scripts combine=false>
<#-- TODO: expose css/js profile name to view layer for debugging -->
<#local jsProfile="todo">
<!-- js profile:${jsProfile} filecount:${javascriptFiles?size} -->

<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="<@s.url value="/includes/jqplot-1.08/excanvas.js"/>"></script><![endif]-->

<#if combine>
    <script type="text/javascript" src="/wro/default.js"></script>
<#else>
    <#list javascriptFiles as src>
	 <script type="text/javascript" src="${staticHost}${src}"></script>
    </#list>
</#if>

</#macro>


<#macro css combine=true>
<#-- TODO: expose css/js profile name to view layer for debugging -->
<#local cssProfile="todo">
<!-- css profile:${cssProfile} filecount:${cssFiles?size} -->
<#if combine>
<!-- call to http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet#URLs_in_CSS -->
    <link rel="stylesheet" type="text/css" href="/wro/default.css"/>
<#else>
    <#list cssFiles as src>
    <link rel="stylesheet" type="text/css" href="${staticHost}${src}" data-version="${common.tdarBuildId}">
    </#list>
</#if>


</#macro>