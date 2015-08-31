The following files will be un-embargoed tomorrow:
<#if toExpire?has_content><#list toExpire as file>
 - ${file}
</#list></#if>

The following files have been unembargoed:
<#if expired?has_content><#list expired as file>
 - ${file}
</#list></#if>
