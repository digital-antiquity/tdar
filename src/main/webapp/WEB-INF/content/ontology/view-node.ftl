<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<h1>${ontology.title} &mdash; <span>${node.displayName}</span></h1>
<#if node.synonyms?has_content>
<p><strong>Synonyms:</strong> 
<#list node.synonyms as synonym>
<#if synonym_index != 0>,</#if>${synonym}
</#list>
</p>
</#if>

<p><strong>Parent:</strong>
<#if parentNode?has_content>
 <a href="<@s.url value="${parentNode.iri}"/>">${parentNode.displayName}</a></p>
<#else>
<a href="<@s.url value="/ontology/${ontology.id?c}"/>">${ontology.title} (ontology root)</a></p>

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
 <li><a href="<@s.url value="/${dataset.urlNamespace}/${dataset.id?c}"/>">${dataset.name}</a></li>
</#list>
<#if !datasetsWithMappingsToNode?has_content>
<li>None</li>
</#if>
</ul>
</#escape>
