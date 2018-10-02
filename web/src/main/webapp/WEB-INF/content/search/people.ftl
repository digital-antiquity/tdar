<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist />
    <#import "/WEB-INF/macros/search-macros.ftl" as search />
<head>
    <title>Search Results: <#if searchSubtitle??>${searchSubtitle?html}<#else>${query!''?html}</#if></title>
</head>
<body>

<div id="titlebar" parse="true">
    <h1>
        <#if searchPhrase?? && !explore>
            Search Results: <span>${searchPhrase}</span>
        <#elseif query?has_content>
        ${lookupSource.proper} Search Results: <span>${query!''?html}</span>
        <#else>
            Search Results: ${(searchSubtitle!"all people")?html}
        </#if>
        <#if sortField?? && sortField != defaultSort>
            <span class="smaller">; sorted by ${sortField.label}</span>
        </#if>
    </h1>
</div>

    <#if (totalRecords > 0)>
        <#if !hideFacetsAndSort>
        <div id="sidebar-left" parse="true" class="options hidden-phone">

            <h2 class="totalRecords">Search Options</h2>
            <ul class="tools media-list">
                <li class="media"><a href="<@search.refineUrl/>" rel="noindex"><i class="search-magnify-icon-red"></i> Refine your search &raquo;</a></li>

                <#if (contextualSearch!false)>
                    <#if projectId??>
                        <li class="media"><@s.a href="/project/${projectId?c}"><i class="icon-project icon-red"></i> Return to project page &raquo;</@s.a></li>
                    <#else>
                        <li class="media"><@s.a href="/collection/${collectionId?c}"><i class="icon-collection icon-red"></i> Return To collection
                            page &raquo;</@s.a></li>
                    </#if>
                </#if>

                <!--        <li>Subscribe via &raquo;
	            <a class="subscribe"  href="${rssUrl}">RSS</a>
	        </li> -->
            </ul>

        </div>
        </#if>

    <div id="divResultsSortControl">
        <div class="row">
            <div class="col-6">
                <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Result" />
            </div>
            <div class="col-6">
                <div class="float-right">
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
        <#assign indx = 0/>
        <#list results as result>
            <#if result?has_content>
                <#if indx != 0>
                    <hr/></#if>
                <#assign indx = indx + 1/>
                <div class="listItemPart">
                    <b class="search-result-title-${result.status}">
                        <a class="resourceLink" href="${result.detailUrl}">${result.properName}</a>
                    </b>
                    <#if result.institution?has_content><p>${result.institution.name}</p></#if>
                </div>
            </#if>
        </#list>
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
