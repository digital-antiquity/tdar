<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "common-collection.ftl" as commonCollection>
	<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
	
<head>
</head>
<body>

<h1>${resourceCollection.name!"untitled collection"}</h1>
<h3>Occurence Counts</h3>
<table class="table tableFormat sortable" id="ocur">
<thead>
	<tr><th>type</th><th>subtype</th><th>label</th><th>count</th></tr>
		</thead>
		<tbody>
<#list facetWrapper.facetResults?keys as key>
	<#list facetWrapper.facetResults[key]>
	<#items  as val>
		<#compress>
		<tr><td>${val.simpleName}</td><td>${key}</td><td>
			<#if val.url?? >
				<a href="${val.url}">${val.label}</a>
			<#else>
			${val.label}			
			</#if>
		</td>
		<td>${val.count?c}</td></tr>
		</#compress>
	</#items>
	</#list>
</#list>
</tbody>
</table>
<script>
$(function() {
	$("#ocur").dataTable({"bPaginate": false});
});
</script>
</body>

</#escape>