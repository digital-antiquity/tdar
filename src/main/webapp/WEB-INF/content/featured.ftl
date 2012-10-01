<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#if featuredResource?? >
<h3>Featured ${featuredResource.resourceType.label}</h3>
    <@view.tdarCitation featuredResource false />
</#if>
