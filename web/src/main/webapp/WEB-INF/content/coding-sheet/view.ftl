<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>

    <#macro toolbarAdditions>
        <#if editable>
        <!-- disabled if coding sheet has errors, ontology is not mapped -->
            <#assign disabled = !okToMapOntology />
            <@nav.makeLink "coding-sheet" "mapping" "map ontology" "mapping"   current true disabled "mappingLink"/>
        </#if>
    </#macro>

    <#macro afterBasicInfo>
    <div class="section">
        <#if resource.defaultOntology?has_content>
        <p><b>Ontology:</b> <a href='<@s.url value="/${resource.defaultOntology.absoluteUrl}"/>'>${resource.defaultOntology.title}</a></p>
        </#if>
        <@view.categoryVariables />
        </div>
        <@common.codingRules />
    </#macro>

</#escape>