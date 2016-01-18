<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "common-collection.ftl" as commonCollection>
	<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
	
<head>
</head>
<body>

<h1>${resourceCollection.name!"untitled collection"}</h1>
<#list facetWrapper.facetResults?keys as key>
<h3>${key}</h3>
<ul>
	<#list facetWrapper.facetResults[key] as val>
	<#compress>
	<li>
		<#if val.url?? >
			<a href="${val.url}">${val.label}</a> (${val.count?c})
		<#else>
		${val}			
		</#if>
	</li>
	</#compress>
	</#list>
</ul>

</#list>
</body>

</#escape>