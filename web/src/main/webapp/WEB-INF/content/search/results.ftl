<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist />
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search />
<head>
    <title>Search Results: <#if searchSubtitle??>${searchSubtitle?html}</#if></title>
    <@search.headerLinks includeRss=(actionName=="results") />
</head>
<body>

<div id="titlebar" parse="true">
    <h1>
        <#if searchPhrase?? && !explore>
            Search Results: <span>${searchPhrase}</span>
        <#elseif explore && exploreKeyword?? && exploreKeyword.definition?has_content >
        ${exploreKeyword.label?html}
        <#elseif query?has_content>
        ${lookupSource.proper} Search Results: <span>${query?html}</span>
        <#else>
            Search Results: ${(searchSubtitle!"all records")?html}
        </#if>
        <#if sortField?? && sortField != defaultSort>
            <span class="smaller">; sorted by ${sortField.label}</span>
        </#if>
    </h1>
    <#if explore && exploreKeyword?? && exploreKeyword.definition?has_content >
        <div class="glide">
            <#if exploreKeyword.definition??>
                <p>${exploreKeyword.definition?html}</p>
            </#if>
        </div>
    </#if>
</div>

    <#if (totalRecords > 0)>
        <#if !hideFacetsAndSort>
        <div id="sidebar-left" parse="true" class="options hidden-phone">

            <h2 class="totalRecords">Search Options</h2>
            <ul class="tools media-list">
                <li class="media"><a href="<@search.refineUrl/>" rel="noindex">
                <svg class="svgicon red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_find"></use></svg>
                 Refine your search &raquo;</a></li>

                <#if (contextualSearch!false)>
                    <#if projectId??>
                        <li class="media"><@s.a href="/project/${projectId?c}">
                            <svg class="svgicon red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_project"></use></svg>
                            Return to project page &raquo;</@s.a></li>
                    <#else>
                        <li class="media"><@s.a href="/collection/${collectionId?c}">
                            <svg class="svgicon red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_project"></use></svg>
                            Return To collection page &raquo;</@s.a></li>
                    </#if>
                </#if>

                <li class="media">
                <svg class="svgicon red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_download"></use></svg>
                <span>Download these results &raquo;
                    <#if sessionData?? && sessionData.authenticated && (totalRecords > 0) && (actionName=="results")>
                        <@search.searchLink "download" "to Excel" />
                        <#if (totalRecords > maxDownloadRecords)>
                            Limited to the first ${maxDownloadRecords} results.
                        </#if>

                    <#else>
                        Login
                    </#if></span>
                    </li>
            </ul>
                <@rlist.displayWidget />
            <form>
<#--        <@search.facetBy facetlist=typeFacets currentValues=types label="Object Type(s)" facetParam="types" /> -->
        <@search.facetBy facetlist=collectionTypeFacets currentValues=collectionTypes label="Collection Type(s)" facetParam="collectionTypes" />
        <@search.facetBy facetlist=resourceTypeFacets currentValues=resourceTypes label="Resource Type(s)" facetParam="resourceTypes" />
        <@search.facetBy facetlist=documentTypeFacets currentValues=documentType label="Document Type(s)" facetParam="documentType" />
        <@search.facetBy facetlist=integratableOptionFacets currentValues=integratableOptions label="Integratable" facetParam="integratableOptions" />
        <@search.facetBy facetlist=fileAccessFacets currentValues=fileAccess label="File Access" facetParam="fileAccess" />

            </form>
    </div>
    <div class="visible-phone">
        <a href="<@search.refineUrl />">Refine your search &raquo;</a>
    </div>
    </#if>

        <#if (referrer?? && referrer == 'TAG')>
        <div class="notice">
            <b>Welcome TAG Users</b><br>
            If you'd like to perform an integration:
            <ol>
                <#if !sessionData?? || !sessionData.authenticated>
                    <#assign returnurl><@s.url value="/search/search?url=" includeParams="all" /></#assign>
                    <li><a href="<@s.url value="/login"/>?url=${returnurl?url}">Login or Register</a></li>
                </#if>
                <li>Bookmark datasets you'd like to integrate</li>
                <li>Visit your workspace to begin the integration process</li>
            </ol>
            <a href="${integrationDocumentationUrl}">visit ${siteAcronym} documentation for more details</a>
        </div>
        </#if>

    <div id="divResultsSortControl">
        <div class="row">
            <div class="span3">
                <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Result" />
            </div>
            <div class="span6 form-inline">
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
                <@rlist.listResources resourcelist=results sortfield=sortField listTag="span" itemTag="span" titleTag="h3" orientation=orientation mapPosition="top" mapHeight="450"/>
        

    </div>
        <@search.pagination ""/>

    <#else>
        <h2>No records match the query.</h2>
        <#if query?has_content><br/>

            <p><b>Try searching for:</b>
                <ul>
                    <li><a href="<@s.url value="/search/people?query=${query?url}"/>">People named ${query}</a></li>
                    <li><a href="<@s.url value="/search/institutions?query=${query?url}"/>">Institutions named ${query}</a></li>
                    <li><a href="<@s.url value="/search/collections?query=${query?url}"/>">Collections named ${query}</a></li>
                </ul>
    
        </#if>
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
