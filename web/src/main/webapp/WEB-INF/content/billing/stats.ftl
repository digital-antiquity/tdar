<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/common-stats.ftl" as statsCommon>
<title>Usage statistics for ${account.name}</title>

<h1>${account.name}</h1>

<@statsCommon.table />

</#escape>