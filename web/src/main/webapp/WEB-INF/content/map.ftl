<title>Map</title>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/header.ftl" as header>

<h3>${siteAcronym} Worldwide</h3>
<@common.renderWorldMap forceAddSchemeHostAndPort=true />