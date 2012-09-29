<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist />
<#import "/WEB-INF/macros/search/search-macros.ftl" as search />
<head>
  <title>Search Results: <#if searchSubtitle??>${searchSubtitle?html}</#if></title>
  <@search.headerLinks includeRss=(actionName=="results") />
</head>
<body>
<div>
<@search.initResultPagination/>
<#if searchPhrase?? && !explore>
    ${searchPhrase}
</#if>

<#if (totalRecords > 0)>

<#if explore && exploreKeyword?? && exploreKeyword.definition?has_content >
<div class="glide">
    <h3>${exploreKeyword.label?html}</h3>
	<#if exploreKeyword.definition??>
		${exploreKeyword.definition?html}
	</#if>
</div>
</#if>

<#if (referrer?? && referrer == 'TAG')>
<div class="glide">
<b>Welcome TAG Users</b><br/>
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
	<@search.basicPagination "Records"/>

	<style type='text/css'>
	ol { 
	    list-style-type:none !important;
	}

    h5 {
        display:block !important;
        border-bottom:1px solid #ccc;
    }
    
	</style>

<div class="limit">
<@removeFacet facetlist=resourceTypes label="Resource Type(s)" facetParam="resourceTypes" />
<@removeFacet facetlist=documentType label="Document Type(s)" facetParam="documentType" />
<@removeFacet facetlist=fileAccess label="File Access" facetParam="fileAccess" />

</div>
<div class="glide">
    <@rlist.listResources resourcelist=results sortfield=sortField expanded=true listTag="ol" titleTag="h5"/>
</div>
    <#if (numPages > 1)>
<div class="glide">
	<@search.pagination ""/>
</div>
</#if>
<#else>
	<h2>No records match the query.</h2>
</#if>
</div>


 <div id="sidebar" parse="true">
 <div style="height:110px"></div>
<h2>Search Options</h2>
<ul class="facets">
<li>        <B>Search Options:</b>
    <ul>
        <li><b>Refine:</b><@search.searchLink "advanced" "Modify Search" /> </li>
    <#if sessionData?? && sessionData.authenticated && (totalRecords > 0) && (actionName=="results")>
        <li><b>Download:</b>
        <@search.searchLink "download" "to Excel" />
        <#if totalRecords &gt; maxDownloadRecords>
            Limited to the first ${maxDownloadRecords} results.    
        </#if>
        </li>
     </#if>
      <li>  <form action=''>
        <b>Sort By:</b> <@search.sortFields true/>
        </form>
      </li>
      </ul>
      </li>
  </ul>
  <br/>
<#macro facetBy facetlist label="Facet Label" facetParam="">
<#if (facetlist?? && !facetlist.empty)>
<li><B>${label}:</B>
<ul>
	<#list facetlist as facet>
		<#assign facetLabel = facet />
	    <#if facet.plural?has_content>
			<#assign facetLabel = facet.plural />
	    <#elseif facet.label?has_content>
	    	<#assign facetLabel = facet.label />
	    </#if>
	    <li>
	    	<#if (facetlist?size > 1)>
		    	<a href="<@s.url includeParams="all">
			        <@s.param name="${facetParam}">${facet}</@s.param>
			        <@s.param name="startRecord" value="0"/>
			        <#if facetParam != "documentType">
				        <@s.param name="documentType" value=""/>
			        </#if>
			        <#nested>
			    </@s.url>">
					${facetLabel}
				</a>
		    <#else>
		    	${facetLabel}
		    </#if>
 	      (${facet.count})
		</li>
	</#list>
</ul><br/></li>
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
    	<a href="<@s.url includeParams="all">
	        <@s.param name="${facetParam}"value="" />
	        <@s.param name="startRecord" value="0"/>
	        <#if facetParam != "documentType">
		        <@s.param name="documentType" value=""/>
	        </#if>
	        <#nested>
	    </@s.url>"> [X]
	    <#if facet.plural?has_content>${facet.plural}
	    <#elseif facet.label?has_content>${facet.label}
	    <#else>${facet}
	    </#if></a>
    </#if>
    </#if>
</#macro>

<h2>Limit Your Search</h2>
<ul class="facets" id="facets">
<@facetBy facetlist=resourceTypeFacets label="Resource Type(s)" facetParam="resourceTypes" />

<@facetBy facetlist=documentTypeFacets label="Document Type(s)" facetParam="documentType" />

<@facetBy facetlist=fileAccessFacets label="File Access" facetParam="fileAccess" />

</ul>
</div>


</body>
