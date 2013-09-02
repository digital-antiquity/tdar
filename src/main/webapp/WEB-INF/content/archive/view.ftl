<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#macro afterBasicInfo>
        <h2>Status</h2>
        <p>This archive<strong>
           <#if resource.isImportPeformed()>is
           <#else>is not
           </#if>
            </strong> unpacked into the repository.
        </p>
    </#macro>
</#escape>