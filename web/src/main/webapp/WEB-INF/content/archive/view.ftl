<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#macro afterBasicInfo>
    <h2>Status</h2>
    <p>This archive <strong>${resource.importDone?string("is", "is not yet")}</strong> unpacked into the repository.
    </p>
    </#macro>
</#escape>