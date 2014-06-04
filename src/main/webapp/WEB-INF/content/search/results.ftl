<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist />
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search />
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
                <li class="media"><a href="<@search.refineUrl/>" rel="noindex"><i class="search-magnify-icon-red"></i> Refine your search &raquo;</a></li>

                <#if (contextualSearch!false)>
                    <#if projectId??>
                        <li class="media"><@s.a href="/project/${projectId?c}"><i class="icon-project icon-red"></i> Return to project page &raquo;</@s.a></li>
                    <#else>
                        <li class="media"><@s.a href="/collection/${collectionId?c}"><i class="icon-collection icon-red"></i> Return To collection
                            page &raquo;</@s.a></li>
                    </#if>
                </#if>

                <#if lookupSource == 'RESOURCE'>
                    <li class="media"><i class="search-download-icon-red"></i> <span>Download these results &raquo;
                        <#if sessionData?? && sessionData.authenticated && (totalRecords > 0) && (actionName=="results")>
                            <@search.searchLink "download" "to Excel" />
                            <#if (totalRecords > maxDownloadRecords)>
                                Limited to the first ${maxDownloadRecords} results.
                            </#if>

                        <#else>
                            Login
                        </#if></span>
                    </li>
                </#if>
                <!--        <li>Subscribe via &raquo;
	            <a class="subscribe"  href="${rssUrl}">RSS</a>
	        </li> -->
            </ul>

            <#if lookupSource == 'RESOURCE'>
                <h3>View Options</h3>
                <ul class="tools media-list">
                    <li class="media"><a href="<@s.url includeParams="all">
	                    <@s.param name="orientation">LIST</@s.param>
	                </@s.url>"><i class="search-list-icon-red"></i> <@s.text name="DisplayOrientation.LIST"/></a></li>
                    <li class="media"><a href="<@s.url includeParams="all">
	                    <@s.param name="orientation">LIST_FULL</@s.param>
	                </@s.url>"><i class="search-list-icon-red"></i> <@s.text name="DisplayOrientation.LIST_FULL"/></a></li>
                    <li class="media"><a href="<@s.url includeParams="all">
	                    <@s.param name="orientation">GRID</@s.param>
	                </@s.url>"><i class="search-grid-icon-red"></i> <@s.text name="DisplayOrientation.GRID"/></a></li>
                    <li class="media"><a href="<@s.url includeParams="all">
	                    <@s.param name="orientation">MAP</@s.param>
	                </@s.url>"><i class="search-map-icon-red"></i> <@s.text name="DisplayOrientation.MAP"/></a></li>
                </ul>
            </#if>
            <form>
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
            <a href="http://dev.tdar.org/confluence/display/TDAR/Data+Integration">visit ${siteAcronym} documentation for more details</a>
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
                    <script type='text/javascript'>
                        $("#recordsPerPage").change(function () {
                            var url = window.location.search.replace(/([?&]+)recordsPerPage=([^&]+)/g, "");
                            //are we adding a querystring or merely appending a name/value pair, i.e. do we need a '?' or '&'?
                            var prefix = "";
                            if (url.indexOf("?") != 0) {
                                prefix = "?";
                            }
                            url = prefix + url + "&recordsPerPage=" + $('#recordsPerPage').val();
                            window.location = url;
                        });
                    </script>
                    <#if !hideFacetsAndSort>
                        <@search.sortFields true/>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <div class="tdarresults">
        <#if lookupSource == 'COLLECTION' || lookupSource='RESOURCE'>
        <#--fixme: replace explicit map sizes with css names -->
            <@rlist.listResources resourcelist=results sortfield=sortField listTag="span" itemTag="span" titleTag="h3" orientation=orientation mapPosition="top" mapHeight="450"/>
        <#else>
            <#assign indx = 0/>
            <#list results as result>
                <#if result?has_content>
                    <#if indx != 0>
                        <hr/></#if>
                    <#assign indx = indx + 1/>
                    <div class="listItemPart">
                        <h3 class="search-result-title-${result.status}">
                            <a class="resourceLink" href="/${result.urlNamespace}/${result.id?c}">${result.properName}</a>
                        </h3>
                        <#if result.institution?has_content><p>${result.institution.name}</p></#if>
                        <blockquote class="luceneExplanation">${result.explanation!""}</blockquote>
                        <blockquote class="luceneScore"
                        <b>score:</b>${result.score!""}<br> </blockquote>
                    </div>
                </#if>
            </#list>
        </#if>
    </div>
        <@search.pagination ""/>

    <#else>
    <h2>No records match the query.</h2>
    </#if>

<script type="text/javascript">
    //pretty controls for sort options, sidebar options (pulled from main.js)
    $(function () {
        TDAR.common.initializeView();
        <#assign map_ = "" />
        <#if map?has_content>
            <#assign map_ = map />
        </#if>
        <#if !map_?has_content && (g[0].latitudeLongitudeBoxes[0])?has_content>
            <#assign map_ = g[0].latitudeLongitudeBoxes[0] />
        </#if>
        <#if map_?has_content && map_.valid && map_.minimumLatitude?has_content >
//            ${map_.minimumLatitude}
            TDAR.maps.mapPromise.done(function () {
                TDAR.maps.updateResourceRect($(".google-map")[0], ${map_.minimumLatitude?c}, ${map_.minimumLongitude?c}, ${map_.maximumLatitude?c}, ${map_.maximumLongitude?c});
            });
        </#if>
    });
</script>

</body>



</#escape>
