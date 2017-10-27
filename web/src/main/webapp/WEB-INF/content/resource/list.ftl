<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/${config.themeDir}/settings.ftl" as settings>
<head>
    <#assign title>Browse all ${resourceType.plural} in ${siteAcronym}</#assign>
    <@search.simpleHeaderLinks />
    <title>${title}</title>
</head>
<h1>${title}</h1>


<hr class="smallmargin">
        <@search.basicPagination resourceType.plural />
<hr class="smallmargin">
    <@list.listResources resourcelist=results sortfield=sortField titleTag="h5" listTag="ul" itemTag="li" itemsPerRow=itemsPerRow orientation="LIST" />
<hr class="smallmargin">
        <@search.basicPagination resourceType.plural />

</#escape>