<#ftl ns_prefixes={
"oai_dc":"http://www.openarchives.org/OAI/2.0/oai_dc/",
"mods":"http://www.loc.gov/mods/v3",
"dc":"http://purl.org/dc/elements/1.1/",
"tdar":"http://www.tdar.org/namespace"}>
<#import "oai-macros.ftl" as oai>
<@oai.response "GetRecord">
<record>
    <header>
        <identifier>${record.identifier?html}</identifier>
        <datestamp>${record.datestamp?iso_utc}</datestamp>
        <!-- TODO list setSpecs here -->
        <!--<setSpec></setSpec>-->
    </header>
    <metadata>
    ${record.metadata.@@markup}
    </metadata>
</record>
</@oai.response>