<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<#macro toolbarAdditions>
	<#if editable>
		<#assign disabled = !resource.defaultOntology?? />
		<@view.makeLink "coding-sheet" "mapping" "map ontology" "mapping"   current true disabled />
	</#if>
</#macro>

<#macro afterBasicInfo>
	<@view.codingRules>
		<#if codingSheet.defaultOntology?has_content>
			<p><b>Ontology:</b> <a href='<@s.url value="/${codingSheet.defaultOntology.absoluteUrl}"/>'>${codingSheet.defaultOntology.title}</a></p>
		</#if>
		<@view.categoryVariables />
	</@view.codingRules>
</#macro>

<#macro localJavascript>
    <@view.datatableChildJavascript />
</#macro>
</#escape>