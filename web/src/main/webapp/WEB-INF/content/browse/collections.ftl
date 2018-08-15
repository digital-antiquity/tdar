<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/search-macros.ftl" as search>
<#-- @search.initResultPagination/ -->
    <#global searchResultsLayout=true>
<head>
    <title><#if collection??>${collection.name}<#else>All Collections</#if></title>
</head>
<body>

<div id="titlebar" parse="true">
    <h1>Browsing <span>All Collections</span></h1>
</div>


    <#if results?has_content>
    <div id="divResultsSortControl">
        <div class="row">
            <div class="col-4">
                <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Collection"/>
            </div>
            <div class="col-5">
                <#if !hideFacetsAndSort>
                    <div class="form-horizontal pull-right">
                        <@search.sortFields true/>
                    </div>
                </#if>
            </div>
        </div>
    </div>
    <div class="tdarresults">
        <@list.listResources resourcelist=results sortfield=sortField listTag="span" itemTag="span" titleTag="b" orientation='LIST_LONG' mapPosition="top" mapHeight="450"/>
    </div>



        <@search.basicPagination "Collections"/>
    <#else>
    No collections
    </#if>

</body>
</#escape>