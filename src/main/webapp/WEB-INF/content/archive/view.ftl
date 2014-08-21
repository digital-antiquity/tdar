<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#macro afterBasicInfo>
    <h2>Status</h2>
    <p>This archive<strong>
        <#if resource.isImportDone()>is
        <#else>is not yet
        </#if>
    </strong> unpacked into the repository.
    </p>
    </#macro>
</#escape>