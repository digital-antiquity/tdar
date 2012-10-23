<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist />
<#import "/WEB-INF/macros/search/search-macros.ftl" as search />
<head>
  <title>Search Results: <#if searchSubtitle??>${searchSubtitle?html}</#if></title>
  <@search.headerLinks includeRss=(actionName=="results") />
</head>
<body>



	<@search.initResultPagination/>

	<div id="titlebar" parse="true">
		<#if searchPhrase?? && !explore>
			<h1>Search Results: <span>"${searchPhrase}"</span></h1>
			
		<#elseif explore && exploreKeyword?? && exploreKeyword.definition?has_content >
		    <h1>${exploreKeyword.label?html}</h1>
			<div class="glide">
			    <#if exploreKeyword.definition??>
			        ${exploreKeyword.definition?html}
			    </#if>
			</div>
	    </#if>
	</div>

<#if (totalRecords > 0)>

	<div id="sidebar-left" parse="true" class="options">
				
				<h2>Search Options</h2>

				<ul class="tools">
					<li><@search.searchLink "advanced" "Refine your search &raquo;" />
        <li>Download these results &raquo;
    <#if sessionData?? && sessionData.authenticated && (totalRecords > 0) && (actionName=="results")>
        <@search.searchLink "download" "to Excel" />
        <#if totalRecords &gt; maxDownloadRecords>
            Limited to the first ${maxDownloadRecords} results.    
        </#if>
        </li>
	<#else>
	Login
     </#if>
				</ul>

				<form>
					<@facetBy facetlist=resourceTypeFacets currentValues=resourceTypes label="Resource Type(s)" facetParam="resourceTypes" />
					<@facetBy facetlist=documentTypeFacets currentValues=documentType label="Document Type(s)" facetParam="documentType" />
					<@facetBy facetlist=integratableOptionFacets currentValues=integratableOptions label="Integratable" facetParam="integratableOptions" />
					<@facetBy facetlist=fileAccessFacets currentValues=fileAccess label="File Access" facetParam="fileAccess" />
				</form>
	</div>

	
	<#if (referrer?? && referrer == 'TAG')>
		<div class="notice">
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



	 <h2>${totalRecords} Results</h2>
	 <div class="sort">
		 <p>Sort By:</p>
		 <form action=''>
		    <@search.sortFields true/>
		 </form>
	 </div>

	<hr class="dbl" />
    <@rlist.listResources resourcelist=results sortfield=sortField expanded=true listTag="" itemTag="" titleTag="h3"/>
	<hr class="dbl" />
    <@search.pagination ""/>
</div>
<#else>
    <h2>No records match the query.</h2>
</#if>



<#macro facetBy facetlist=[] currentValues=[] label="Facet Label" facetParam="">
<#if (facetlist?? && !facetlist.empty)>
<h4>${label}:</h4>
<ul>
    <#list facetlist as facet>
        <#assign facetLabel = facet />
        <#if facet.plural?has_content>
            <#assign facetLabel = facet.plural />
        <#elseif facet.label?has_content>
            <#assign facetLabel = facet.label />
        </#if>
        <li>
			<input type="checkbox" <#if currentValues?size == 1>checked=checked</#if>>
			<label class="<#if currentValues?size == 1>checked</#if>">
            <#if (facetlist?size > 1)>
                <a href="<@s.url includeParams="all">
                    <@s.param name="${facetParam}">${facet}</@s.param>
                    <@s.param name="startRecord" value="0"/>
                    <#if facetParam != "documentType">
                        <@s.param name="documentType" value=""/>
                    </#if>
                    <#if facetParam != "integratableOptions">
                        <@s.param name="integratableOptions" value=""/>
                    </#if>
                    <#nested>
                </@s.url>">${facetLabel}</a>
            <#elseif currentValues?size == 1>
				<@removeFacet facetlist=currentValues facetParam=facetParam />
            <#else>
	                ${facetLabel}
            </#if>
           <span>(${facet.count})</span>
			</label>
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
        <#elseif facet.label?has_content><#assign facetText=label/>
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
        </@s.url>">${facetText}</a>
    </#if>
    </#if>
</#macro>

</div>


</body>
