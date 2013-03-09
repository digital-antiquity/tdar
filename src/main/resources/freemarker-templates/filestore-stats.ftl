This is an automated message from ${siteAcronym} reporting on files with issues.
Run on: ${baseUrl}
${dateRun}

<#if missing?has_content><@printSection "MISSING FILES">
<#list missing as file>
 - ${version.filename} not found [${version.informationResourceId}]
</#list>
</@printSection></#if>
<#if tainted?has_content><@printSection "TAINTED FILES">
<#list tainted as file>
 - ${version.filename} checksum does not match the one stored [${version.informationResourceId}]
</#list>
</@printSection></#if>
<#if other?has_content><@printSection "OTHER PROBLEMS">
<#list other as file>
 - ${version.filename} had a problem [${version.informationResourceId}]
</#list>
</@printSection></#if>
<#if !missing?has_content && !tainted?has_content && !other?has_content>
No issues found
</#if>

<#macro printSection header contents>
<#if contents?has_content>
<@bar /> ${header} <@bar />
${contents}
</#if>
</#macro>

<#macro bar>========================================================</#macro>