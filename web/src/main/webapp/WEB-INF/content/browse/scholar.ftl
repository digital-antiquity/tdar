<#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
<#import "/${themeDir}/settings.ftl" as settings>

<title>Explore ${siteAcronym}</title>

<#escape _untrusted as _untrusted?html >
<h1>Resources by ${siteAcronym} Year</h1>

<ul>
    <#list scholarData?sort_by("key") as key>

        <#assign tdarYear = key.key?substring(3) />
        <li><a href="<@s.url value="/scholar/scholar?year=${key.key?c}"/>">${key.key?c}</a></li>
    </#list>
</ul>

</#escape>

