<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<@search.initResultPagination/>

<head>
<title><#if collection??>${collection.name}<#else>All Collections</#if></title>
</head>
<body>
<#if collection??>
<p>${collection.description!""}</p>
</#if>


<#if (totalRecords > 0)>
<div class="glide">
	<div id="recordTotal">Collections ${firstRec} - ${lastRec} of ${totalRecords}
	</div> 
	<@search.pagination ""/>

</div>
</#if>  
<div class="glide">
<ul>
  <#list results as collection_>
   <li><a href="<@s.url value="/collection/${collection_.id?c}"/>">${collection_.name}</a></li>
  </#list>
</ul>
</div>
</body>
</#escape>