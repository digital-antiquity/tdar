<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "admin-common.ftl" as admin>
<title>Admin Pages</title>

    <@admin.header/>

    <@admin.statsTable historicalResourceStatsWithFiles "Resource Statistics (With Files)" "resourceStatsWithFiles" />

    <@admin.statsTable historicalResourceStats "Resource Statistics (All)" "resourceStats" />


    <@admin.statsTable historicalCollectionStats "Collection Statistics" "collectionStats" />

</#escape>
