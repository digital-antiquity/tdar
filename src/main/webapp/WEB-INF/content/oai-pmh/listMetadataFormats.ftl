<#import "oai-macros.ftl" as oai>
<@oai.response "ListMetadataFormats">
    <#list metadataFormats as metadataFormat>
    <metadataFormat>
        <metadataPrefix>${metadataFormat.prefix}</metadataPrefix>
        <schema>${metadataFormat.schemaLocation}</schema>
        <metadataNamespace>${metadataFormat.namespace}</metadataNamespace>
    </metadataFormat>
    </#list>
</@oai.response>