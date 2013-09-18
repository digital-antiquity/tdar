<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#macro afterFileInfo>
        <#if resource.audioCodec??>
            <p><strong>Format: </strong>${resource.audioCodec}
        </#if>
        <#if resource.software??>
            <p><strong>Creating Application: </strong>${resource.software}
        </#if>
    </#macro>
</#escape>