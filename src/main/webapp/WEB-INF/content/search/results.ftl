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
<#if !hideFacetsAndSort>
    <div id="sidebar-left" parse="true" class="options hidden-phone">
                
                <h2 class="totalRecords">Search Options</h2>
    <ul class="tools media-list">
    <li>Records Per Page
<@s.select id="recordsPerPage" name="recordsPerPage" list={"10":"10", "25":"25", "50":"50"} listKey="key" listValue="value" />
      <script type='text/javascript'>
      $("#recordsPerPage").change(function() {
        var url = window.location.search.replace(/([?&]+)recordsPerPage=([^&]+)/g,"");
        //are we adding a querystring or merely appending a name/value pair, i.e. do we need a '?' or '&'? 
        var prefix = "";
        if (url.indexOf("?") != 0) {
          prefix = "?";
        }
        url = prefix + url +  "&recordsPerPage="+$('#recordsPerPage').val();
        window.location = url;
        });
      </script>

    </li>
        <li class="media"><a href="<@search.refineUrl/>" rel="noindex"><i class="pull-left search-magnify-icon-red" ></i>Refine your search &raquo;</a></li>
	<#if lookupSource == 'RESOURCE'>
	        <li class="media"><i class="pull-left search-download-icon-red" ></i><div class="media-body">Download these results &raquo;
	        <#if sessionData?? && sessionData.authenticated && (totalRecords > 0) && (actionName=="results")>
	            <@search.searchLink "download" "to Excel" />
	            <#if (totalRecords > maxDownloadRecords)>
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
	                </@s.url>"><i class="pull-left search-list-icon-red"></i>List (Title)</a></li>
	                <li class="media"><a href="<@s.url includeParams="all">
	                    <@s.param name="orientation">LIST_FULL</@s.param>
	                </@s.url>"><i class="pull-left search-list-icon-red"></i>List (Expanded)</a></li>
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
        <a href="<@search.refineUrl />">Refind your search &raquo;</a>
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
        <a href="http://dev.tdar.org/confluence/display/TDAR/Data+Integration">visit our documentation for more details</a>
        </div>
    </#if>
    


    
    <div id="divResultsSortControl">
        <div class="row">
            <div class="span4">
	            <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Result" />
            </div>
            <div class="span5">
            	<#if !hideFacetsAndSort>
                <div class="form-horizontal pull-right">
                   <@search.sortFields true/>
                </div>
            	</#if>
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
	<#if indx != 0> <hr/></#if>
	<#assign indx = indx + 1/>
<div class="listItemPart">
    <h3 class="search-result-title-${result.status}">
        <a class="resourceLink" href="/${result.urlNamespace}/${result.id?c}">${result.properName}</a>
    </h3>
    <#if result.institution?has_content><p>${result.institution.name}</p></#if>
    <blockquote class="luceneExplanation">${result.explanation!""}</blockquote>
    <blockquote class="luceneScore"<b>score:</b>${result.score!""}<br> </blockquote>
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
        $(function() {
            TDAR.common.initializeView();
            <#assign map_ = "" />
            <#if map?has_content>
            	<#assign map_ = map />
            </#if>
            <#if !map_?has_content && (g[0].latitudeLongitudeBoxes[0])?has_content>
            	<#assign map_ = g[0].latitudeLongitudeBoxes[0] />
            </#if>
            <#if map_?has_content>
            TDAR.maps.mapPromise.done(function() {
	            TDAR.maps.updateResourceRect($(".google-map")[0], ${map_.minimumLatitude?c}, ${map_.minimumLongitude?c}, ${map_.maximumLatitude?c}, ${map_.maximumLongitude?c});
            });
            </#if>
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
				
                <div class="media-body">
                
                <a rel="noindex" href="<@s.url includeParams="all">
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
                <i class="search-list-check<#if currentValues?size == 1>ed</#if>box-grey"></i>
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
        <a rel="noindex" href="<@s.url includeParams="all">
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

