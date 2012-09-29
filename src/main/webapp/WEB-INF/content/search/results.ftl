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
<#assign firstRec = (startRecord + 1) />
<#assign curPage = ((startRecord/recordsPerPage)?floor + 1) />
<#assign numPages = ((totalRecords/recordsPerPage)?ceiling) />
<#if (firstRec > totalRecords)>
 <#assign numPages = 0 />
 <#assign firstRec = totalRecords/>
</#if>

<#if (nextPageStartRecord > totalRecords) >
	<#assign lastRec = totalRecords>
<#else>
	<#assign lastRec = nextPageStartRecord>
</#if>

<#if (firstRec - recordsPerPage) < 1 >
	<#assign prevPageStartRec = 0>
<#else>
	<#assign prevPageStartRec = firstRec - recordsPerPage - 1>
</#if>

<#macro searchLink path linkText>
	<a href="
	<@s.url includeParams="all" value="${path}">
		<#nested>
	</@s.url> 
	">${linkText}</a>
</#macro>

<#macro paginationLink startRecord linkText>
	<span class="paginationLink">
	<@searchLink "results" linkText>
		<@s.param name="startRecord" value="${startRecord?c}" />
		<@s.param name="recordsPerPage" value="${recordsPerPage?c}" />
	</@searchLink>
	</span>
</#macro>

<#macro join sequence delimiter=",">
    <#list sequence as item>
        ${item}<#if item_has_next>${delimiter}</#if><#t>
    </#list>
</#macro>

<#assign pagination>
	<div class="pagination">
  <#assign start =0>
  <#assign end =numPages -1>
  <#if numPages &gt; 40 && curPage &gt; 19 >
    <#assign start = curPage - 20>
  </#if>
  <#if numPages &gt; 40 && curPage &lt; numPages -19 >
    <#assign end = curPage + 19>
  </#if> 

  <#if start != 0>
      <@paginationLink startRecord=(0 * recordsPerPage) linkText="first" />
  </#if>
		<#if (firstRec > 1)>
			<@paginationLink startRecord=prevPageStartRec linkText="previous" />
		</#if>
		<#if (numPages > 1)>
			<#list start..end as i>
				<#if (i + 1) = curPage>
                                        <#-- FIXME: there are 2 of these spans with
                                        the same id being generated.  Turn this into
                                        a CSS class instead or is this a bug?
                                        -->
					<span id="currentResultPage">${i + 1}</span>
				<#else>
					<@paginationLink startRecord=(i * recordsPerPage) linkText=(i + 1) />
				</#if>
			</#list>
			<#else>
			1<br/>
		</#if>
		<#if (nextPageStartRecord < totalRecords) >
			<@paginationLink startRecord=nextPageStartRecord linkText="next" />
		</#if>
  <#if (end != numPages && nextPageStartRecord < totalRecords)>
          <@paginationLink startRecord=(totalRecords - totalRecords % 20) linkText="last" />
  </#if>
	</div>
</#assign>
<#macro bcad _year>
  <#if (_year < 0)>BC<#else>AD</#if><#t/>
</#macro>

<p id="searchPhrase">
<em>
${searchPhrase}</em></p>

<#if (totalRecords > 0)>
<div class="glide">
	<div id="recordTotal">Records ${firstRec} - ${lastRec} of ${totalRecords}
	</div> 
	${pagination}

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
		<@rlist.informationResources iterable="results" editable=false bookmarkable=authenticated showTitle=false/>
</div>
    <#if (numPages > 1)>
<div class="glide">
	${pagination}
</div>
</#if>
<#else>
	<h2>No records match the query.</h2>
</#if>
</div>



<#macro cleanupEnum enumvalue>
    <#assign ret = enumvalue?replace("_"," ") />
    ${ret?capitalize}
</#macro>
 <div id="sidebar" parse="true">
 <div style="height:110px"></div>
<h2>Search Options</h2>
<ul class="facets">
<li>        <B><@searchLink "advanced" "Modify Search" /></b>
</li>
      <li>  <form action=''>
        <b>Sort By:</b> <@search.sortFields true/>
        </form>
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
    <a href="<@s.url includeParams="all">
        <@s.param name="startRecord" value="0"/>
        <@s.param name="dateCreated">${facet.facetQuery}</@s.param>
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
