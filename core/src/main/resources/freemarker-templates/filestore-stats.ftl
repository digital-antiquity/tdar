<b>This is an automated message from ${siteAcronym} reporting on files with issues.</b>
<br/><b>Run on:</b> ${baseUrl}
<br/><b>Date:</b> ${dateRun?date}
<br/><b>Total Files:</b> ${count}
<br/><b>Issues:</b>${totalIssues!0}

<#if missing?has_content><@printSection "MISSING FILES" missing?size >
<ul>
<#list missing as version>
<li> ${version.filename} with file id ${version.informationResourceFileId?c} not found [${version.informationResourceId?c}]</li>
</#list>
</ul>
</@printSection></#if>
<#if tainted?has_content><@printSection "TAINTED FILES" tainted?size >
<ul>
<#list tainted as  version>
 <li> ${version.filename} checksum does not match the one stored [${version.informationResourceId?c}]</li>
</#list>
</ul>
</@printSection></#if>
<#if other?has_content><@printSection "OTHER PROBLEMS" other?size >
<ul>
<#list other as  version>
 <li> ${version.filename} had a problem [${version.informationResourceId?c}] </li>
</#list>
</ul></@printSection></#if>
<#if !missing?has_content && !tainted?has_content && !other?has_content>
No issues found
</#if>

<#macro printSection header count>

<@bar /> 
	<p><b>${header} (${count})</b></p>
<@bar />
<#nested/>
</#macro>

<#macro bar><hr>
</#macro>