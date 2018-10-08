<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist />
    <#import "/WEB-INF/macros/search-macros.ftl" as search />
<head>
    <title>Search Results: <#if searchSubtitle??>${searchSubtitle?html}</#if></title>
    <@search.headerLinks includeRss=(actionName=="results") />
</head>
<body>


<div id="titlebar" parse="true">
    <h1>
        <#if exploreKeyword?? && exploreKeyword.definition?has_content >
        ${exploreKeyword.label?html}
        <#elseif searchPhrase??>
            Search Results: <span>${searchPhrase}</span>
        <#elseif query?has_content>
        ${lookupSource.proper} Search Results: <span>${query?html}</span>
        <#else>
            Search Results: ${(searchSubtitle!"all records")}
        </#if>
        <#if sortField?? && sortField != defaultSort>
            <span class="smaller">; sorted by ${sortField.label}</span>
        </#if>
    </h1>
    <#if exploreKeyword?? && exploreKeyword.definition?has_content >
        <div class="glide">
            <#if exploreKeyword.definition??>
                <p>${exploreKeyword.definition?html}</p>
            </#if>
        </div>
    </#if>
</div>


<div class="modal hide fade" id="modal">
    <#include '/components/tdar-autocomplete/template/autocomplete.html' />
    <#include 'vue-collection-selection.html' />
</div>

    <#if (totalRecords > 0)>
        <#if !hideFacetsAndSort>
        <div id="sidebar-left" parse="true" class="options d-sm-none d-md-block">

            <h2 class="totalRecords">Search Options</h2>
            <ul class="tools media-list ml-0 pl-0">
                <li class="media">
                    <i class="mr-3 fas fa-search red"></i> 
                    <div class="media-body">

                <a href="<@search.refineUrl/>" rel="noindex">Refine your search &raquo;</a>
                </div>
                </li>

                <#if (contextualSearch!false)>
                    <#if projectId??>
                        <li class="media">
                        <i class="mr-3 icon-project icon-red"></i>
                        <div class="media-body">
                        <@s.a href="/project/${projectId?c}">Return to project page &raquo;</@s.a>
                        </div>
                        </li>
                    <#else>
                        <li class="media">
                        <i class="mr-3 icon-collection icon-red"></i>
                        <div class="media-body">

                        <@s.a href="/collection/${collectionId?c}"> Return To collection
                            page &raquo;</@s.a>
                        </div>
                        </li>
                    </#if>
                </#if>
                        <li class="media">
                <svg class="mr-3 svgicon red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_download"></use></svg>
                    <div class="media-body">
                <span>Download these results &raquo;
                    <#if sessionData?? && sessionData.authenticated && (totalRecords > 0) && (actionName=="results")>
                        <@search.searchLink "download" "to Excel" />
                        <#if (totalRecords > maxDownloadRecords)>
                            Limited to the first ${maxDownloadRecords} results.
                        </#if>

                    <#else>
                        Login
                    </#if></span>
                    </div>
                    </li>
            
                <#if editor>        
                    <li class="media">
                    <svg class="mr-3 svgicon red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_collection"></use></svg>
                    <div class="media-body">
                    <span>Save these results &raquo;
                        <#if sessionData?? && sessionData.authenticated && (totalRecords > 0) && (actionName=="results")>
                            
                            <a id="saveSearchLink" href="#modal" data-toggle="modal">to Collection</a>
                            
                            <#if (totalRecords > maxDownloadRecords)>
                                Limited to the first ${maxDownloadRecords} results.
                            </#if>
                        <#else>
                            Login
                        </#if></span>
                        </div>
                    </li>
                </#if>
                <li class="media">
                    <i class="mr-3 fas fa-sort-alpha-down"></i>
                    <div class="media-body">
                        <#if !hideFacetsAndSort>
                            <@search.sortFields />
                        </#if>
                    </div>
                    </li>
            </ul>
                <@rlist.displayWidget />
            <form>
            <#--
            <@search.facetBy facetlist=collectionTypeFacets currentValues=collectionTypes label="Collection Type(s)" facetParam="collectionTypes" />-->
            <@search.facetBy facetlist=resourceTypeFacets currentValues=resourceTypes label="Resource Type(s)" facetParam="resourceTypes" />
            <@search.facetBy facetlist=objectTypeFacets currentValues=objectTypes label="Object Type(s)" facetParam="objectTypes" />
            <@search.facetBy facetlist=documentTypeFacets currentValues=documentType label="Document Type(s)" facetParam="documentType" />
            <@search.facetBy facetlist=integratableOptionFacets currentValues=integratableOptions label="Integratable" facetParam="integratableOptions" />
            <@search.facetBy facetlist=fileAccessFacets currentValues=fileAccess label="File Access" facetParam="fileAccess" />
            </form>
    </div>
    </#if>

    <div id="divResultsSortControl">
        <div class="row">
            <div class="col-7">
                <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Result" />
            </div>
            <div class="col-5">
            	<p class="align-text-bottom float-right">
                    <label>Records Per Page
                        <@s.select  theme="simple" id="recordsPerPage" cssClass="input-small" name="recordsPerPage"
                        list={"10":"10", "25":"25", "50":"50"} listKey="key" listValue="value" />
                    </label>
				</p>
            </div>
        </div>
    </div>

    <div class="tdarresults">
                <@rlist.listResources resourcelist=results sortfield=sortField listTag="span" itemTag="span" titleTag="b" orientation=orientation mapPosition="top" mapHeight="450"/>
        

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
        <#if authenticated>
        TDAR.vuejs.collectionwidget.init("#collection-selection-form");
        </#if> 
    });
</script>

</body>

</#escape>
