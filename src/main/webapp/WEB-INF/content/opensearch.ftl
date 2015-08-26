<?xml version="1.0" encoding="UTF-8"?>
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/" xmlns:geo="http://a9.com/-/opensearch/extensions/geo/1.0/"
                       xmlns:custom="http://example.com/opensearchextensions/1.0/">
    <ShortName>${siteAcronym}</ShortName>
    <Description>Search ${siteName}</Description>
    <Tags>archaeology</Tags>
    <SyndicationRight>open</SyndicationRight>
    <Contact>${contactEmail}</Contact>
    <Url type="text/html" template="http://${hostName}/search/search?query={searchTerms}&amp;resourceType={custom:resourceTypes?}&amp;pw={startPage?}&amp;latLongBox={geo:box?}"/>
    <Url type="application/atom+xml" template="http://${hostName}/search/rss?query={searchTerms}&amp;resourceType={custom:resourceTypes?}&amp;pw={startPage?}&amp;latLongBox={geo:box?}"/>

    <Parameter xmlns:custom="http://example.com/opensearchextensions/1.0/" name="resourceTypes" value="{custom:resourceTypes}" minimum="0" maximum="*"/>
    <OutputEncoding>UTF-8</OutputEncoding>
    <InputEncoding>UTF-8</InputEncoding>
    <Attribution></Attribution>
</OpenSearchDescription>