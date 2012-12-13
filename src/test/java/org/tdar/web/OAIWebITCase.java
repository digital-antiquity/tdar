package org.tdar.web;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.apache.tools.ant.filters.StringInputStream;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.SearchIndexService;
import org.tdar.struts.data.oai.OAIMetadataFormat;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class OAIWebITCase extends AbstractWebTestCase {

    @Autowired
    SearchIndexService indexService;

    private XpathEngine xpathEngine;
    private String repositoryNamespaceIdentifier;
    private String firstPersonIdentifier;
    private String firstInstitutionIdentifier;
    private String firstResourceIdentifier;

    @Before
    public void prepareOai() throws SAXException, IOException, ParserConfigurationException, XpathException {
        // reindex
        indexService.indexAll();

        // establish namespace bindings for the XPath tests
        HashMap<String, String> namespaceBindings = new HashMap();
        namespaceBindings.put("oai", "http://www.openarchives.org/OAI/2.0/");
        namespaceBindings.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        namespaceBindings.put("dc", "http://purl.org/dc/elements/1.1/");
        namespaceBindings.put("tdar", "http://www.tdar.org/namespace");
        namespaceBindings.put("mods", "http://www.loc.gov/mods/v3");
        namespaceBindings.put("oai-id", "http://www.openarchives.org/OAI/2.0/oai-identifier");
        NamespaceContext context = new SimpleNamespaceContext(namespaceBindings);
        XMLUnit.setXpathNamespaceContext(context);

        // ask the repository for its namespace identifier
        xpathEngine = XMLUnit.newXpathEngine();
        gotoPage(getBase() + "Identify");
        Document response = getPageDOM();
        repositoryNamespaceIdentifier = xpathEngine.evaluate("oai:OAI-PMH/oai:Identify/oai:description/oai-id:oai-identifier/oai-id:repositoryIdentifier",
                response);

        // list identifiers and find the identifiers of the first person, institution, and resource,
        // all of which should appear in the first page of results (because the provider performs
        // all three searches and returns a page containing the next 10 people, the next 10
        // institutions, and the next 10 resources).
        gotoPage(getBase() + "ListIdentifiers&metadataPrefix=tdar");
        response = getPageDOM();
        firstPersonIdentifier = xpathEngine.evaluate("oai:OAI-PMH/oai:ListIdentifiers/oai:header/oai:identifier[contains(., 'Person')][1]", response);
        Assert.assertTrue("First page of ListIdentifier results includes a Person", (firstPersonIdentifier.contains("Person")));
        firstInstitutionIdentifier = xpathEngine.evaluate("oai:OAI-PMH/oai:ListIdentifiers/oai:header/oai:identifier[contains(., 'Institution')][1]", response);
        Assert.assertTrue("First page of ListIdentifier results includes an Institution", (firstInstitutionIdentifier.contains("Institution")));
        firstResourceIdentifier = xpathEngine.evaluate("oai:OAI-PMH/oai:ListIdentifiers/oai:header/oai:identifier[contains(., 'Resource')][1]", response);
        Assert.assertTrue("First page of ListIdentifier results includes a Resource", (firstResourceIdentifier.contains("Resource")));
    }

    /**
     * Exhaustively invoke ListIdentifiers or ListRecords verb
     * 
     * @param verb
     *            The verb to use; either "ListIdentifiers" or "ListRecords"
     * @return number of records listed
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws XpathException
     * @throws NumberFormatException
     */
    private int listIdentifiersOrRecords(String verb, String metadataPrefix) throws SAXException, IOException, ParserConfigurationException,
            NumberFormatException, XpathException {
        int totalRecordCount = 0;
        int pageCount = 0;
        String resumptionToken;
        String requestURI = getBase() + verb + "&metadataPrefix=" + metadataPrefix;
        do {
            gotoPage(requestURI);
            // commented out the schema validation - it's too slow! Need to debug the speed issue
            // testValidOAIResponse();
            Document response = getPageDOM();
            // count the number of records returned in this page
            int recordCount = Integer.valueOf(xpathEngine.evaluate("count(oai:OAI-PMH/oai:" + verb + "/*)", response));
            // must be > 0 (otherwise, repository is empty, or else the repository issued us with an unnecessary resumptionToken
            Assert.assertTrue(requestURI + " response returned > 0 records", (recordCount > 0));
            totalRecordCount += recordCount;
            pageCount++;
            resumptionToken = xpathEngine.evaluate("oai:OAI-PMH/oai:" + verb + "/oai:resumptionToken", response);
            requestURI = getBase() + verb + "&resumptionToken=" + resumptionToken;
        } while (!resumptionToken.equals(""));
        Assert.assertTrue("Harvesting " + metadataPrefix + " records with " + verb + " returned multiple pages", pageCount > 1);
        return totalRecordCount;
    }

    @Test
    public void testHarvest() throws NumberFormatException, XpathException, SAXException, IOException, ParserConfigurationException {
        // harvest all records using ListRecords and ListIdentifiers, in all 3 formats
        int tdarIdentifiers = listIdentifiersOrRecords("ListIdentifiers", "tdar");
        int tdarRecords = listIdentifiersOrRecords("ListRecords", "tdar");
        int modsIdentifiers = listIdentifiersOrRecords("ListIdentifiers", "mods");
        int modsRecords = listIdentifiersOrRecords("ListRecords", "mods");
        int dcIdentifiers = listIdentifiersOrRecords("ListIdentifiers", "oai_dc");
        int dcRecords = listIdentifiersOrRecords("ListRecords", "oai_dc");
        // check that the numbers make sense
        Assert.assertEquals("Number of identifiers matches number of records for tDAR format", tdarIdentifiers, tdarRecords);
        Assert.assertEquals("Number of identifiers matches number of records for MODS format", modsIdentifiers, modsRecords);
        Assert.assertEquals("Number of identifiers matches number of records for DC format", dcIdentifiers, dcRecords);
        Assert.assertEquals("Number of identifiers for tDAR format matches number of identifiers for DC format", tdarIdentifiers, dcIdentifiers);
        // NB people and institutions are not disseminated in MODS, so the number should be smaller
        Assert.assertTrue("Number of identifiers for tDAR format greater than number of identifier for MODS format", tdarIdentifiers > modsIdentifiers);
        Assert.assertTrue("Number of harvested records > 0", modsIdentifiers > 0);
    }

    public String getBase() {
        return getBaseUrl() + "/oai-pmh/oai?verb=";
    }

    // http://xmlunit.sourceforge.net/userguide/html/

    private void testValidOAIResponse() throws ConfigurationException, SAXException {
        testValidXMLResponse(new StringInputStream(getPageCode(), "utf8"), "http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd");
    }

    @Test
    public void testIdentify() throws ConfigurationException, SAXException {
        gotoPage(getBase() + "Identify");
        testValidOAIResponse();

        assertTextPresentInCode(TdarConfiguration.getInstance().getSystemAdminEmail());
        // set these in the src/test/resources/tdar.properties
        assertTextPresentInCode(TdarConfiguration.getInstance().getSystemDescription());
        assertTextPresentInCode(TdarConfiguration.getInstance().getRepositoryName());
    }

    @Test
    public void testListMetadataFormats() throws ConfigurationException, SAXException {
        gotoPage(getBase() + "ListMetadataFormats");
        testValidOAIResponse();

        for (OAIMetadataFormat format : OAIMetadataFormat.values()) {
            assertTextPresentInCode(format.getNamespace());
            assertTextPresentInCode(format.getSchemaLocation());
            assertTextPresentInCode(format.getPrefix());
        }
    }

    @Test
    public void testListSets() throws ConfigurationException, SAXException, XpathException, IOException {
        gotoPage(getBase() + "ListSets");
        testValidOAIResponse();
        assertXpathExists("oai:OAI-PMH/oai:error[@code='noSetHierarchy']");
    }

    @Test
    public void testGetRecord() throws ConfigurationException, SAXException, XpathException, IOException, ParserConfigurationException {
        // test for well-formed but invalid identifier
        getRecord("tdar", repositoryNamespaceIdentifier + ":Resource:0");
        assertXpathExists("oai:OAI-PMH/oai:error[@code='idDoesNotExist']");

        // test for bogus identifier
        getRecord("tdar", repositoryNamespaceIdentifier + ":Bogus:1");
        assertXpathExists("oai:OAI-PMH/oai:error[@code='idDoesNotExist']");

        // get a person in DC format
        getRecord("oai_dc", firstPersonIdentifier);
        assertXpathExists("oai:OAI-PMH/oai:GetRecord/oai:record/oai:metadata/oai_dc:dc/dc:identifier");

        // try to get a person in MODS format (and fail, because Person records aren't disseminated in MODS)
        getRecord("mods", firstPersonIdentifier);
        assertXpathExists("oai:OAI-PMH/oai:error[@code='cannotDisseminateFormat']");

        if (TdarConfiguration.getInstance().getEnableEntityOai()) {
            // get a person in tDAR format
            getRecord("tdar", firstPersonIdentifier);
            logger.info(getPageCode());
            assertXpathExists("oai:OAI-PMH/oai:GetRecord/oai:record/oai:metadata/tdar:person/@id");

            // get an institution in tDAR format
            getRecord("tdar", firstInstitutionIdentifier);
            assertXpathExists("oai:OAI-PMH/oai:GetRecord/oai:record/oai:metadata/tdar:institution/@id");

            // get an institution in DC format
            getRecord("oai_dc", firstInstitutionIdentifier);
            assertXpathExists("oai:OAI-PMH/oai:GetRecord/oai:record/oai:metadata/oai_dc:dc/dc:identifier");
        }

        // try to get an institution in MODS format (and fail, because Institution records aren't disseminated in MODS)
        getRecord("mods", firstInstitutionIdentifier);
        assertXpathExists("oai:OAI-PMH/oai:error[@code='cannotDisseminateFormat']");

        // Get Resource records

        // get a Resource in tDAR format
        getRecord("tdar", firstResourceIdentifier);
        assertXpathExists("oai:OAI-PMH/oai:GetRecord/oai:record/oai:metadata/tdar:*/@id");

        // get a resource in DC format
        getRecord("oai_dc", firstResourceIdentifier);
        // FIXME: Resources don't get a dc:identifier - shouldn't they?
        assertXpathExists("oai:OAI-PMH/oai:GetRecord/oai:record/oai:metadata/oai_dc:dc/dc:title");

        // try to get a resource in MODS format
        getRecord("mods", firstResourceIdentifier);
        assertXpathExists("oai:OAI-PMH/oai:GetRecord/oai:record/oai:metadata/mods:mods");

    }

    private void getRecord(String metadataPrefix, String identifier) throws ConfigurationException, SAXException {
        gotoPage(getBase() + "GetRecord&metadataPrefix=" + metadataPrefix + "&identifier=" + identifier);
        testValidOAIResponse();
    }

    private void assertXpathExists(String xpath) throws XpathException, IOException, SAXException {
        XMLAssert.assertXpathExists(xpath, getPageCode());
    }

    private Document getPageDOM() throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder builder = XMLUnit.getTestDocumentBuilderFactory().newDocumentBuilder();
        String page = getPageCode();
        Reader reader = new StringReader(page);
        InputSource inputSource = new InputSource(reader);
        Document doc = builder.parse(inputSource);
        return doc;
    }

}
