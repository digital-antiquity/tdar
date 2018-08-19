<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist />
    <#import "/WEB-INF/macros/search-macros.ftl" as search />
<head>
    <title>Search Results: <#if searchSubtitle??>${searchSubtitle?html}</#if></title>
    <#if lookupSource == 'RESOURCE'>
        <@search.headerLinks includeRss=(actionName=="results") />
    </#if>
</head>
<body>

<div id="titlebar" parse="true">
    <h1>
        <#if searchPhrase?? && !explore>
            Search Results: <span>${searchPhrase}</span>
        <#elseif query?has_content>
        ${lookupSource.proper} Search Results: <span>${query!''?html}</span>
        <#else>
            Search Results: ${(searchSubtitle!"all records")?html}
        </#if>
        <#if sortField?? && sortField != defaultSort>
            <span class="smaller">; sorted by ${sortField.label}</span>
        </#if>
    </h1>
</div>

    <#if (totalRecords > 0)>
        <div id="sidebar-left" parse="true" class="options hidden-phone">

            <h2 class="totalRecords">Search Options</h2>
            <ul class="tools media-list">
                <li class="media"><a href="<@search.refineUrl/>" rel="noindex"><i class="search-magnify-icon-red"></i> Refine your search &raquo;</a></li>
            </ul>
        </div>
        <div class="visible-phone">
            <a href="<@search.refineUrl />">Refine your search &raquo;</a>
        </div>

    <div id="divResultsSortControl">
        <div class="row">
            <div class="col-3">
                <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Result" />
            </div>
            <div class="col-6 form-inline">
                <div class="pull-right">
                    <div class="control-group"></div>
                    <label>Records Per Page
                        <@s.select  theme="simple" id="recordsPerPage" cssClass="input-small" name="recordsPerPage"
                        list={"10":"10", "25":"25", "50":"50"} listKey="key" listValue="value" />
                    </label>
                    <#if !hideFacetsAndSort>
                        <@search.sortFields />
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div class="tdarresults">
            <@rlist.listResources resourcelist=results sortfield=sortField listTag="span" itemTag="span" titleTag="b" orientation=orientation mapPosition="top" mapHeight="450"/>
    </div>
        <@search.pagination ""/>

    <#else>
    <h2>No records match the query.</h2>
    </#if>

<script type="text/javascript">
    //pretty controls for sort options, sidebar options (pulled from main.js)
    $(function () {
        TDAR.common.initializeView();
        TDAR.advancedSearch.initializeResultsPage();
    });
</script>

</body>

</#escape>
