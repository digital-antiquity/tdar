<#escape _untrusted as _untrusted?html>
<head>
<title>${ontology.title} &mdash; ${node.displayName}</title>
</head>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<h1>${ontology.title} &mdash; <span>${node.displayName}</span></h1>
<p><strong>Ontology:</strong> <a href="<@s.url value="${ontology.detailUrl}" />">${ontology.title}</a></p>

<p><strong>Parent:</strong>
    <#if parentNode?has_content>
        <a href="<@s.url value="${parentNode.iri}"/>">${parentNode.displayName}</a></p>
    <#else>
        <a href="<@s.url value="${ontology.detailUrl}"/>">${ontology.title} (ontology root)</a></p>
	</#if>


    <#if node.synonyms?has_content>
    <p><strong>Synonyms:</strong>
        <#list node.synonyms as synonym>
            <#if synonym_index != 0>,</#if>${synonym}
        </#list>
    </p>
    </#if>


    <#if children?has_content>
    <p><strong>Children:</strong>
        <#list children as child>
            <#if child_index !=0>,</#if>
            <a href="<@s.url value="${child.iri}"/>">${child.displayName}</a>
        </#list>
    </p>
    </#if>

<h2>Datasets that use ${node.displayName}</h2>
<ul>
    <#list datasetsWithMappingsToNode as dataset>
        <li><a href="<@s.url value="${dataset.detailUrl}"/>">${dataset.name}</a></li>
    </#list>
    <#if !datasetsWithMappingsToNode?has_content>
        <li>None</li>
    </#if>
</ul>
</#escape>
