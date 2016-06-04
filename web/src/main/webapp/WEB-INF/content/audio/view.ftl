<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#macro afterFileInfo>
    <h4>Extended Information</h4>
    <p><strong>Format: </strong><#if resource.audioCodec??>${resource.audioCodec}</#if>
    <p><strong>Creating Application: </strong><#if resource.software??>${resource.software}</#if>
    </#macro>
</#escape>