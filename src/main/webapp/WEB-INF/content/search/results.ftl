<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<head>
  <title>Search Results: <#if searchSubtitle??>${searchSubtitle?html}</#if></title>
  <meta name="totalResults" content="${totalRecords}" />
  <meta name="startIndex" content="${startRecord}" />
  <meta name="itemsPerPage" content="${recordsPerPage}" />
  <link rel="alternate" type="application/atom+xml" title="Atom 1.0" href="${rssUrl}" />
</head>
<body>
<div>
<@search.initResultPagination/>
<#if searchPhrase??>
	<p id="searchPhrase">
	<em>
		${searchPhrase!""}
	</em></p>
</#if>

<#if (totalRecords > 0)>
<div class="glide">
	<div id="recordTotal">Records ${firstRec} - ${lastRec} of ${totalRecords}
	</div> 
	<@search.pagination "results"/>

</div>
	<style type='text/css'>
	ol { 
	    list-style-type:none !important;
	}

    h5 {
        display:block !important;
        border-bottom:1px solid #ccc;
    }
    
	</style>

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

<div class="glide">
		<@rlist.informationResources iterable="results" editable=useSubmitterContext bookmarkable=authenticated showTitle=false/>
</div>
    <#if (numPages > 1)>
<div class="glide">
	<@search.pagination "results"/>
</div>
</#if>
<#else>
	<h2>No records match the query.</h2>
</#if>
</div>



<#macro cleanupEnum enumvalue>
    <#assign ret = enumvalue?replace("_"," ") />
    <#-- FIXME: this is not sustainable, but there's no access to the enum -->
    <#if enumvalue == 'BOOK'>
        <#assign ret = 'Book / Report'/>
    </#if>
    ${ret?capitalize}
</#macro>

 <div id="sidebar" parse="true">
 <div style="height:110px"></div>
<h2>Search Options</h2>
<ul class="facets">
<li>        <B>Search Options:</b>
    <ul>
        <li><b>Refine:</b><@search.searchLink "advanced" "Modify Search" /> </li>
    <#if sessionData?? && sessionData.authenticated && (totalRecords > 0)>
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
<h2>Limit Your Search</h2>
<ul class="facets" id="facets">

<#if (resourceTypeFacets?? && !resourceTypeFacets.empty)>
<li><B>Resource Type:</B>
<ul>
    <@s.iterator status='rowStatus' value='resourceTypeFacets' var='facet'>
    <li> 
    <a href="<@s.url includeParams="all">
        <@s.param name="resourceTypes" value="value"/>
        <@s.param name="startRecord" value="0"/>
        <@s.param name="documentType" value=""/>
    </@s.url>">
    <@cleanupEnum value /></a> (${count})</li>
    </@s.iterator>
</ul><br/></li>
</#if>
<#if (documentTypeFacets?? && !documentTypeFacets.empty)>
<li><B>Document Type:</B>
<ul>
    <@s.iterator status='rowStatus' value='documentTypeFacets' var='facet'>
    <li>
        <a href="<@s.url includeParams="all">
        <@s.param name="startRecord" value="0"/>
        <@s.param name="documentType" value="value"/>
    </@s.url>">
     <@cleanupEnum value/></a> (${count})</li>
    </@s.iterator>
</ul><br/></li>
</#if>

<#if (fileAccessFacets?? && !fileAccessFacets.empty)>
<li><B>File Access:</B>
<ul>
    <@s.iterator status='rowStatus' value='fileAccessFacets' var='facet'>
    <li>
        <a href="<@s.url includeParams="all">
        <@s.param name="startRecord" value="0"/>
        <@s.param name="fileAccess" value="value"/>
    </@s.url>">
     <@cleanupEnum value/></a> (${count})</li>
    </@s.iterator>
</ul><br/></li>
</#if>
<#-- 
<#if (dateCreatedFacets?? && !dateCreatedFacets.empty)>

<li><B>Date Created:</B>
<ul>
    <@s.iterator status='rowStatus' value='dateCreatedFacets' var='facet'>
<#if (count > 0) >
    <li> 
<#assign dateCreatedMin = minDateValue />
<#assign dateCreatedMax = maxDateValue />
<#if facet.getMin()??> 
  <#assign dateCreatedMin = facet.getMin() />
</#if>
<#if facet.getMax()??> 
  <#assign dateCreatedMax = facet.getMax() />
</#if>
    <a href="<@s.url includeParams="all">
        <@s.param name="startRecord" value="0"/>
        <@s.param name="dateCreatedMin">${dateCreatedMin?c}</@s.param>
        <@s.param name="dateCreatedMax">${dateCreatedMax?c}</@s.param>
    </@s.url>">
    <#assign val = facet.value?replace("]","") />
    <#assign val = val?replace("[","") />
    <#assign val = val?replace("(","") />
    <#assign val = val?replace(", ","-") />
    <#if (val?starts_with('-') )>Before </#if>
    <#if (val?ends_with('-') )>After 
      <#assign val = val?replace("-","") />
    </#if>
    ${val} 
    (${count})</li></#if>
    </@s.iterator>
</ul></li>
</#if>
-->
<#--
<li>Culture Keywords:
<ul>
    <@s.iterator status='rowStatus' value='cultureFacets' var='facet'>
    <li> ${value} (${count})</li>
    </@s.iterator>
</ul></li>
<li>Geographic Keywords:
<ul>
    <@s.iterator status='rowStatus' value='locationFacets' var='facet'>
    <li> ${value} (${count})</li>
    </@s.iterator>
</ul></li>
 -->
</ul>
</div>


</body>
