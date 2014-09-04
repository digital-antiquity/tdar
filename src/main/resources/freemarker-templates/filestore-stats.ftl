This is an automated message from ${siteAcronym} reporting on files with issues.
Run on: ${baseUrl}
Date: ${dateRun?date}
Total Files: ${count}
Issues: ${totalIssues!0}

<#if missing?has_content><@printSection "MISSING FILES" missing?size >
<#list missing as version>
 - ${version.filename} with file id ${version.informationResourceFileId?c} not found [${version.informationResourceId?c}]
</#list>
</@printSection></#if>
<#if tainted?has_content><@printSection "TAINTED FILES" tainted?size >
<#list tainted as  version>
 - ${version.filename} checksum does not match the one stored [${version.informationResourceId?c}]
</#list>
</@printSection></#if>
<#if other?has_content><@printSection "OTHER PROBLEMS" other?size >
<#list other as  version>
 - ${version.filename} had a problem [${version.informationResourceId?c}]
</#list>
</@printSection></#if>
<#if !missing?has_content && !tainted?has_content && !other?has_content>
No issues found
</#if>

<#macro printSection header count>

<@bar /> 
	${header} (${count})
<@bar />
<#nested/>
</#macro>

<#macro bar>========================================================
</#macro>