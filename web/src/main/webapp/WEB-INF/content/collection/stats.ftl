<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/common-stats.ftl" as statsCommon>

<h1>${collection.name}</h1>

<@statsCommon.table />

</#escape>