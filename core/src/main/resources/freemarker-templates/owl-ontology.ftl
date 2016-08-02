<?xml version="1.0"?>
<rdf:RDF
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
        xmlns:owl="http://www.w3.org/2002/07/owl#"
        xmlns:dc="http://purl.org/dc/elements/1.1/"
        xmlns="${baseUrl}/ontologies/${id?c}"
        xmlns:tdar="${baseUrl}/ontologies#"
        xmlns:base="${baseUrl}/ontologies/${id?c}">
    <owl:Ontology rdf:about=""/>
    <#list ontlogyNodes as node>
        <owl:Class rdf:ID="${node.iri}">
            <rdfs:label><![CDATA[${node.displayName}]]></rdfs:label>
            <#if node.parentNode?has_content>
                <rdfs:subClassOf rdf:resource="#${node.parentNode.iri}"/>
            </#if>
            <#list node.synonymNodes as synonym>
                <owl:equivalentClass rdf:resource="#${synonym.iri}"/>
            </#list>
            <rdfs:comment>TDAROrder-${node.importOrder?c}</rdfs:comment>
            <rdfs:comment><![CDATA[TDARDescription-${node.description!""}]]></rdfs:comment>
            <rdfs:comment><![CDATA[TDARNode]]></rdfs:comment>
            <dc:description><![CDATA[${node.description!""}]]></dc:description>
        </owl:Class>
        <#list node.synonymNodes as synonym>
            <owl:Class rdf:ID="${synonym.iri}">
                <rdfs:label><![CDATA[${synonym.displayName}]]></rdfs:label>
                <owl:equivalentClass rdf:resource="#${node.iri}"/>
                <#-- always sort synonyms at the end -->
                <rdfs:comment>TDAROrder-${(node.importOrder * 10000 + synonym_index)?c}</rdfs:comment>
            </owl:Class>
        </#list>
    </#list>
</rdf:RDF>