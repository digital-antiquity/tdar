<#ftl ns_prefixes={
"oai_dc":"http://www.openarchives.org/OAI/2.0/oai_dc/",
"mods":"http://www.loc.gov/mods/v3",
"dc":"http://purl.org/dc/elements/1.1/",
"tdar":"http://www.tdar.org/namespace"}>
<#import "oai-macros.ftl" as oai>
<@oai.response "ListSets">
    <#list sets as set>
    <set>
        <setSpec>${set.id?c}</setSpec>
        <setName>${set.name?xml}</setName>
        <#if set.description?has_content>
         <setDescription>
      <oai_dc:dc 
          xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" 
          xmlns:dc="http://purl.org/dc/elements/1.1/" 
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
          xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ 
          http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
          <dc:description><![CDATA[${set.description}]]></dc:description>
       </oai_dc:dc>
    </setDescription>
    </#if>
    </set>
    </#list>
</@oai.response>