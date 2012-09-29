<#-- 
$Id:Exp$
Common macros used in multiple contexts
-->
<#macro convertFileSize filesize=0>
<#assign mb = 1048576 />
<#assign kb = 1024 />
<#if (filesize > mb)>
${(filesize / mb)?string(",##0.00")}mb
<#elseif (filesize > kb)>
${(filesize / kb)?string(",##0.00")}kb
<#else>
${filesize?string(",##0.00")}b
</#if>
</#macro>

