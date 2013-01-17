<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist />
<#import "/WEB-INF/macros/search/search-macros.ftl" as search />
<head>
  <title>Search Results: <#if searchSubtitle??>${searchSubtitle?html}</#if></title>
<#if lookupSource == 'RESOURCE'>
  <@search.headerLinks includeRss=(actionName=="results") />
</#if>
</head>
<body>



    <@search.initResultPagination/>

    <div id="titlebar" parse="true">
        <#if searchPhrase?? && !explore>
            <h1>Search Results: <span>${searchPhrase}</span></h1>
            
        <#elseif explore && exploreKeyword?? && exploreKeyword.definition?has_content >
            <h1>${exploreKeyword.label?html}</h1>
            <div class="glide">
                <#if exploreKeyword.definition??>
		            <p>${exploreKeyword.definition?html}</p>
                </#if>
            </div>
        <#elseif query?has_content>
        <h1>${lookupSource.proper} Search Results: <span>${query?html}</span></h1>
        <#else>
        <h1>Search Results: ${(searchSubtitle!"all records")?html}</h1>
        </#if>
    </div>

<#if (totalRecords > 0)>

    <div id="sidebar-left" parse="true" class="options hidden-phone">
                
                <h2 class="totalRecords">Search Options</h2>

    <ul class="tools media-list">
        <li class="media"><a href="<@search.searchUrl "advanced"/>"><i class="pull-left search-magnify-icon-red" ></i>Refine your search &raquo;</a></li>
<#if lookupSource == 'RESOURCE'>
        <li class="media"><i class="pull-left search-download-icon-red" ></i><div class="media-body">Download these results &raquo;
        <#if sessionData?? && sessionData.authenticated && (totalRecords > 0) && (actionName=="results")>
            <@search.searchLink "download" "to Excel" />
            <#if totalRecords &gt; maxDownloadRecords>
                Limited to the first ${maxDownloadRecords} results.    
            </#if>
    
        <#else>
        Login
         </#if></div>
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
                </@s.url>"><i class="pull-left search-list-icon-red"></i>List</a></li>
                <li class="media"><a href="<@s.url includeParams="all">
                    <@s.param name="orientation">GRID</@s.param>
                </@s.url>"><i class="pull-left search-grid-icon-red"></i>Grid</a></li>
                <li class="media"><a href="<@s.url includeParams="all">
                    <@s.param name="orientation">MAP</@s.param>
                </@s.url>"><i class="pull-left search-map-icon-red"></i>Map</a></li>
        </ul>
</#if>
                <form>
                    <@facetBy facetlist=resourceTypeFacets currentValues=resourceTypes label="Resource Type(s)" facetParam="resourceTypes" />
                    <@facetBy facetlist=documentTypeFacets currentValues=documentType label="Document Type(s)" facetParam="documentType" />
                    <@facetBy facetlist=integratableOptionFacets currentValues=integratableOptions label="Integratable" facetParam="integratableOptions" />
                    <@facetBy facetlist=fileAccessFacets currentValues=fileAccess label="File Access" facetParam="fileAccess" />
                </form>
    </div>
    <div class="visible-phone">
    <@search.searchLink "advanced" "Refine your search &raquo;" />
    </div>

    
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
        <a href="http://dev.tdar.org/confluence/display/TDAR/Data+Integration">visit our documentation for more details</a>
        </div>
    </#if>



     <h2 class="totalRecords">${startRecord+1}-${lastRec} (${totalRecords} Results)</h2>
     <div class="sort">
         <p>Sort By:</p>
         <form action=''>
            <@search.sortFields true/>
         </form>
     </div>

    <div class="tdarresults">
    <br/>
    <hr class="dbl" />
<#if lookupSource == 'COLLECTION' || lookupSource='RESOURCE'>
    <#--fixme: replace explicit map sizes with css names -->
    <@rlist.listResources resourcelist=results sortfield=sortField expanded=true listTag="span" itemTag="span" titleTag="h3" orientation=orientation mapPosition="top" mapHeight="450"/>
<#else>
	<#list results as result>
	<#if result_index != 0> <hr/></#if>
<div class="listItemPart">
    <h3 class="search-result-title-${result.status}">
        <a class="resourceLink" href="/${result.urlNamespace}/${result.id?c}">${result.properName}</a>
    </h3>
    <#if result.institution?has_content><p>${result.institution.name}</p></#if>
    <blockquote class="luceneExplanation">${result.explanation!""}</blockquote>
    <blockquote class="luceneScore"<b>score:</b>${result.score!""}<br> </blockquote>
</div>
	</#list>
</#if>
</div>
    <hr class="dbl" />
    <@search.pagination ""/>
<#else>
    <h2>No records match the query.</h2>
</#if>



<script type="text/javascript">
        //pretty controls for sort options, sidebar options (pulled from main.js)
        $(function() {
            initializeView();
            
        });
</script>

</body>

<#macro facetBy facetlist=[] currentValues=[] label="Facet Label" facetParam="">
<#if (facetlist?? && !facetlist.empty)>
<h4>${label}:</h4>
<ul class="media-list tools">
    <#list facetlist as facet>
        <#assign facetLabel = facet />
        <#if facet.plural?has_content>
            <#assign facetLabel = facet.plural />
        <#elseif facet.label?has_content>
            <#assign facetLabel = facet.label />
        </#if>
        <li class="media">
            <#if (facetlist?size > 1)>
				<i class="pull-left search-list-check<#if currentValues?size == 1>ed</#if>box-grey"></i>
                <div class="media-body"><a href="<@s.url includeParams="all">
                    <@s.param name="${facetParam}">${facet}</@s.param>
                    <@s.param name="startRecord" value="0"/>
                    <#if facetParam != "documentType">
                        <@s.param name="documentType" value=""/>
                    </#if>
                    <#if facetParam != "integratableOptions">
                        <@s.param name="integratableOptions" value=""/>
                    </#if>
                    <#nested>
                </@s.url>">
                ${facetLabel}</a> <span>(${facet.count})</span></div>
            <#elseif currentValues?size == 1>
                <@removeFacet facetlist=currentValues facetParam=facetParam />
            <#else>
                <div class="media-body">${facetLabel} <span>(${facet.count})</span></div>
            </#if>
        </li>
    </#list>
</ul>
</#if>

</#macro>

<#macro removeFacet facetlist="" label="Facet Label" facetParam="">
    <#if facetlist?has_content>
    <#if (facetlist?is_collection)>
        <#if facetlist?size == 1>
            <#assign facet= facetlist.get(0) />
        </#if>
    <#elseif (facetlist?is_string) >
        <#assign facet= facetlist />
    </#if>
    <#if facet?has_content>
        <#assign facetText=facet/>
        <#if facet.plural?has_content><#assign facetText=facet.plural/>
        <#elseif facet.label?has_content><#assign facetText=facet.label/>
        </#if>
        <a href="<@s.url includeParams="all">
            <@s.param name="${facetParam}"value="" />
            <@s.param name="startRecord" value="0"/>
            <#if facetParam != "documentType">
                <@s.param name="documentType" value=""/>
            </#if>
            <#if facetParam != "integratableOptions">
                <@s.param name="integratableOptions" value=""/>
            </#if>
            <#nested>
        </@s.url>"><i class="pull-left search-list-checkedbox-grey"></i> 
                       <div class="media-body">${facetText}</div></a>
    </#if>
    </#if>
</#macro>
