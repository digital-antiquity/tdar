<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#macro afterBasicInfo>
        <@view.imageGallery />
    <br/>
    <hr/>
    </#macro>

</#escape>