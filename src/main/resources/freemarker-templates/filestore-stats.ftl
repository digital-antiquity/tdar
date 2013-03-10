This is an automated message from ${siteAcronym} reporting on files with issues.
Run on: ${baseUrl}
${dateRun?date}

<#if missing?has_content><@printSection "MISSING FILES">
<#list missing as version>
 - ${version.filename} not found [${version.informationResourceId?c}]
</#list>
</@printSection></#if>
<#if tainted?has_content><@printSection "TAINTED FILES">
<#list tainted as  version>
 - ${version.filename} checksum does not match the one stored [${version.informationResourceId?c}]
</#list>
</@printSection></#if>
<#if other?has_content><@printSection "OTHER PROBLEMS">
<#list other as  version>
 - ${version.filename} had a problem [${version.informationResourceId?c}]
</#list>
</@printSection></#if>
<#if !missing?has_content && !tainted?has_content && !other?has_content>
No issues found
</#if>

<#macro printSection header>
<@bar /> ${header} <@bar />
<#nested/>
</#macro>

<#macro bar>========================================================</#macro>