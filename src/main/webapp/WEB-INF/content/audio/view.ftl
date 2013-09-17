<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#macro afterFileInfo>
        <p><strong>Format: </strong>${resource.audioCodec}
    </#macro>
</#escape>