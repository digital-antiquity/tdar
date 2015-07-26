<#import "oai-macros.ftl" as oai>
<@oai.response "ListIdentifiers">
    <#list records as record>
    <header>
        <identifier>${record.identifier?html}</identifier>
        <datestamp>${record.datestamp?iso_utc}</datestamp>
        <#list record.sets as set> 
            <setSpec>${set?c}</setSpec>
        </#list>
    </header>
    </#list>
</@oai.response>