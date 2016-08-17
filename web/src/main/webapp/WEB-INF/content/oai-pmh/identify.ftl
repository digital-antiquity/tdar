<#import "oai-macros.ftl" as oai>
<@oai.response "Identify">
<repositoryName>${repositoryName}</repositoryName>
<baseURL>${request.requestURL}</baseURL>
<protocolVersion>2.0</protocolVersion>
<adminEmail>${adminEmail}</adminEmail>
<earliestDatestamp>2008-01-01</earliestDatestamp> <!-- hard coding -->
<deletedRecord>no</deletedRecord> <!-- technically could be "persistent" but this is easier -->
<granularity>YYYY-MM-DD</granularity>
<!--
    The "description" element is for extensions to OAI-PMH - the content of the description must be in another namespace,
    with a schema attached.
    TODO find an appropriate extension mechanism and implement it.
    <description>${description?html}</description>
    -->
<!-- lookup    <compression>deflate</compression> -->
<description>
    <oai-identifier
            xmlns="http://www.openarchives.org/OAI/2.0/oai-identifier"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation=
                    "http://www.openarchives.org/OAI/2.0/oai-identifier
            http://www.openarchives.org/OAI/2.0/oai-identifier.xsd">
        <scheme>oai</scheme>
        <repositoryIdentifier>${repositoryNamespaceIdentifier}</repositoryIdentifier>
        <delimiter>:</delimiter>
        <sampleIdentifier>oai:${repositoryNamespaceIdentifier}:Record:1</sampleIdentifier>
    </oai-identifier>
</description>
</@oai.response>