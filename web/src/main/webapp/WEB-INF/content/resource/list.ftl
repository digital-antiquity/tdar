<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/settings.ftl" as settings>
<head>
    <#assign title>Browse all ${resourceType.plural} in ${siteAcronym}</#assign>
    <@search.simpleHeaderLinks />
    <title>${title}</title>
</head>
<h1><#noescape>${title}</#noescape></h1>


<#if paginationHelper.hasPrevious() || paginationHelper.hasNext()>
<hr class="smallmargin">
</#if>
        <@search.basicPagination resourceType.plural />
<hr class="smallmargin">
    <@list.listResources resourcelist=results sortfield=sortField titleTag="b" listTag="ul" itemTag="li" itemsPerRow=itemsPerRow orientation="LIST" />
<hr class="smallmargin">
        <@search.basicPagination resourceType.plural />

</#escape>