<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/search-macros.ftl" as search>

<head>
    <@search.headerLinks includeRss=false />
    <title>Browse By Year: ${year?c}</title>

</head>
<body>
<h1>Browse By Year: ${year?c}</h1>
    <@list.listResources resourcelist=results sortfield=sortField titleTag="h5" listTag="ul" itemTag="li" itemsPerRow=itemsPerRow orientation="LIST" />
</div>

    <@search.basicPagination "Records" />

</body>
</#escape>