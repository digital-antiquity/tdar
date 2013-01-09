<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/header.ftl" as header>

<#if request.requestURI?contains("map")>
<head>
        <#include "/${themeDir}/head.dec" />
	
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.1/jquery.min.js"></script>
	<script src="/includes/jquery.xcolor-1.5.js"></script>
	<script src="/includes/jquery.metadata.2.1/jquery.metadata.js"></script>
	<script src="/includes/jquery.maphighlight.local.js"></script>
	<link rel="stylesheet" href="/css/tdar-bootstrap.css"/>
</head>
<body id="home">
<h3>tDAR Worldwide</h3>
</#if>
<@common.worldMap />
