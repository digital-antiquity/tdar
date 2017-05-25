facetWrapper<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "../common-collection.ftl" as commonCollection>
	<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
	
<head>
<title>Metadata Report: ${resourceCollection.name!"untitled collection"}</title>
</head>
<body>

<h1>Metadata Report: ${resourceCollection.name!"untitled collection"}</h1>
<div class="row">
<div class="span6">
<h3>Status Breakdown</h3>
<#list facetWrapper.facetResults['status']>
    <#items  as val>
        <#compress>
        <p><b>${val.label}</b>:${val.count?c}</p>
        </#compress>
    </#items>

</#list>
</div>
<div class="span6">
<h3>ResourceType Breakdown</h3>
<#list facetWrapper.facetResults['resourceType']>
    <#items  as val>
        <#compress>
        <p><b>${val.label}</b>:${val.count?c}</p>
        </#compress>
    </#items>

</#list>
</div>
</div>

<h3>Occurence Counts</h3>
<table class="table tableFormat sortable" id="ocur">
<thead>
	<tr><th>type</th><th>subtype</th><th>label</th><th>count</th></tr>
		</thead>
		<tbody>
<#list facetWrapper.facetResults?keys as key>
    <#if key != 'status' && key != 'resourceType'>
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
    </#if>
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