<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "common-collection.ftl" as commonCollection>
	<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
	
<head>
</head>
<body>

<h1>${resourceCollection.name!"untitled collection"}</h1>

${facetWrapper.facetResults}
<#-- 	        <@search.facetBy facetlist=resourceTypeFacets label="" facetParam="selectedResourceTypes" link=false liCssClass="" ulClass="inline" icon=false /> -->
<#--
<@search.facetBy facetlist="cultureKeyword" currentValues=[] label=""  /><!-- facetParam="selectedResourceTypes" -->
-->
</body>

</#escape>