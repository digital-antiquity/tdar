package org.tdar.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.hibernate.ScrollableResults;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.FileProxies;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.RelationType;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.ExternalKeywordMapping;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ReflectionService;
import org.tdar.core.service.SerializationService;
import org.tdar.transform.ExtendedDcTransformer;
import org.tdar.utils.jaxb.JaxbParsingException;
import org.tdar.utils.json.JsonLookupFilter;
import org.tdar.utils.json.JsonProjectLookupFilter;
import org.xml.sax.SAXException;


public class JAXBITCase extends AbstractIntegrationTestCase {

    private static final String BEDOUIN = "bedouin";
    private static final String NABATAEAN = "Nabataean";

    @Autowired
    SerializationService serializationService;

    @Autowired
    ReflectionService reflectionService;

    @Autowired
    ImportService importService;

    @Autowired
    ObfuscationService obfuscationService;

    @Autowired
    GenericKeywordService genericKeywordService;

    @Test
    @Rollback
    public void testJAXBDocumentConversion() throws Exception {
        Document document = genericService.find(Document.class, 4232l);
        String xml = serializationService.convertToXML(document);
        logger.info(xml);
    }

    @Test
    @Rollback
    // no assertions, mostly setup for documentation output
    public void testJAXBDocumentConversionGIS() throws Exception {
        Geospatial geos = new Geospatial();
        geos.setStatus(Status.ACTIVE);
        geos.markUpdated(getAdminUser());
        geos.getResourceCreators().add(new ResourceCreator(getBasicUser(), ResourceCreatorRole.CREATOR));
        geos.setScale("1:1000");
        geos.setCurrentnessUpdateNotes("current as of 2012");
        geos.setSpatialReferenceSystem("WGS:84");
        geos.getGeographicKeywords().add(new GeographicKeyword("Washington, DC"));
        geos.getCultureKeywords().add(new CultureKeyword("Modern"));
        geos.getTemporalKeywords().add(new TemporalKeyword("21st Century"));
        geos.getOtherKeywords().add(new OtherKeyword("map"));
        geos.getInvestigationTypes().add(new InvestigationType("Architectural Survey"));
        geos.getMaterialKeywords().add(new MaterialKeyword("Ceramic"));
        geos.getResourceNotes().add(new ResourceNote(ResourceNoteType.GENERAL, "collected around the national monument"));
        geos.getLatitudeLongitudeBoxes().add(new LatitudeLongitudeBox(-77.05041825771332, 38.889028630817144, -77.04992473125458, 38.88953803591012));
        geos.setTitle("map of ceramics around national monument");
        geos.getResourceCollections()
                .add(new ResourceCollection("test collection", "test description", SortOption.RESOURCE_TYPE, CollectionType.SHARED, true, getAdminUser()));
        geos.setDescription("test map");
        geos.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 2010, 2015));
        geos.getFileProxies().add(new FileProxy("geotiff.tiff", null, VersionType.UPLOADED, FileAction.ADD));
        String xml = serializationService.convertToXML(geos);
        xml = StringUtils.replace(xml, " id=\"-1\"", "");
        logger.info(xml);
    }

    @Test
    @Rollback
    public void testFileProxyConversion() throws Exception {
        FileProxies fp = new FileProxies();
        FileProxy fpx = new FileProxy();
        fp.getFileProxies().add(fpx);
        fpx.setAction(FileAction.REPLACE);
        String xml = serializationService.convertToXML(fp);
        logger.info(xml);
        serializationService.parseXml(FileProxies.class, new StringReader(xml));

    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void exportResourceCollection() throws Exception {
        ResourceCollection collection = createAndSaveNewResourceCollection(NABATAEAN);
        for (Resource r : genericService.findRandom(Resource.class, 10)) {
            collection.getResources().add(r);
            r.getResourceCollections().add(collection);
        }
        genericService.saveOrUpdate(collection);
        genericService.synchronize();
        genericService.refresh(collection);
        String convertToXML = serializationService.convertToXML(collection);
        logger.debug(convertToXML);

    }

    @Test
    @Rollback
    public void testJsonExport() throws Exception {
        Document document = genericService.find(Document.class, 4232l);
        StringWriter sw = new StringWriter();
        document.getProject().getCultureKeywords().add(new CultureKeyword(NABATAEAN));
        document.setInheritingCulturalInformation(true);
        serializationService.convertToJson(document, sw, null, null);
        logger.info(sw.toString());
        assertTrue(sw.toString().contains(NABATAEAN));
        Project project = genericService.find(Project.class, 3805l);
        project.getCultureKeywords().add(new CultureKeyword(BEDOUIN));
        // logger.error("{}", project.getActiveInvestigationTypes());
        // logger.error("{}", project.getActiveMaterialKeywords().add(new MaterialKeyword()));
        // project.getActiveOtherKeywords().add(new OtherKeyword(BEDOUIN));
        sw = new StringWriter();
        serializationService.convertToJson(project, sw, JsonProjectLookupFilter.class, null);
        logger.info(sw.toString());
        assertFalse(sw.toString().contains("\"activeMaterialKeywords\":null"));
        assertTrue(sw.toString().contains(BEDOUIN));
    }

    @Test
    @Rollback
    public void testJsonExportRCorder() throws Exception {
        Project p = new Project();
        p.setTitle("test");
        p.setDescription("test");
        p.markUpdated(getAdminUser());
        for (int i = 1; i < 10; i++) {
            ResourceCreator rc = new ResourceCreator(new Institution(i + " I"), ResourceCreatorRole.CONTACT);
            genericService.saveOrUpdate(rc.getCreator());
            rc.setSequenceNumber(10 - i);
            p.getResourceCreators().add(rc);
        }

        genericService.saveOrUpdate(p);
        StringWriter sw = new StringWriter();
        Long pid = p.getId();
        p = null;
        evictCache();
        p = genericService.find(Project.class, pid);
        List<ResourceCreator> lst = new ArrayList<>(p.getResourceCreators());
        p.getResourceCreators().clear();
        Collections.sort(lst);
        p.getResourceCreators().addAll(lst);
        logger.debug("{}", p.getResourceCreators());
        serializationService.convertToJson(p, sw, null, null);
        for (String l : sw.toString().split("\n")) {
            if (l.contains("properName") || l.contains("sequenceNumber")) {
                logger.debug(l);
            }
        }
        // logger.info(sw.toString());
    }

    @Test()
    @Rollback(true)
    public void testRelatedKeyword() throws Exception {
        CultureKeyword kwd = new CultureKeyword("Nabatean");
        genericService.saveOrUpdate(kwd);
        kwd.getAssertions().add(new ExternalKeywordMapping("http://www.tdar.org", RelationType.DCTERMS_IS_VERSION_OF));
        genericService.saveOrUpdate(kwd.getAssertions());
        genericService.saveOrUpdate(kwd);
        String xml = serializationService.convertToXML(kwd);
        logger.info(xml);
        StringWriter json = new StringWriter();
        serializationService.convertToJson(kwd, json, JsonLookupFilter.class, null);
        logger.info(json.toString());
        assertTrue("string contains assertions", StringUtils.contains(xml, "assertions"));
        assertTrue("string contains assertions", StringUtils.contains(json.toString(), "assertions"));

    }

    @Test
    public void testJAXBProjectConversion() throws Exception {
        Project project = genericService.find(Project.class, 2420l);
        String xml = serializationService.convertToXML(project);
        logger.info(xml);
    }

    @Test
    @Rollback(false)
    public void testJaxbRoundtrip() throws Exception {
        Project project = genericService.find(Project.class, 3805l);
        ResourceCollection collection = createAndSaveNewResourceCollection(BEDOUIN);
        collection.getResources().add(project);
        project.getResourceCollections().add(collection);
        genericService.saveOrUpdate(project);
        genericService.saveOrUpdate(collection);
        final int totalShared = project.getSharedResourceCollections().size();
        final String xml = serializationService.convertToXML(project);
        logger.info(xml);
        genericService.detachFromSession(project);

        setVerifyTransactionCallback(new TransactionCallback<Project>() {

            @Override
            public Project doInTransaction(TransactionStatus arg0) {
                boolean exception = false;
                Integer size = -1;
                Project newProject = null;
                try {
                    newProject = (Project) serializationService.parseXml(new StringReader(xml));
                    newProject.markUpdated(getAdminUser());
                    newProject = importService.bringObjectOntoSession(newProject, getAdminUser(), true);
                    logger.debug("collections:{}", newProject.getResourceCollections());
                    size = newProject.getSharedResourceCollections().size();
                } catch (Exception e) {
                    exception = true;
                    logger.warn("exception: {}", e);
                } finally {
                    // genericService.delete(newProject.getResourceCollections());
                    // genericService.delete(newProject);
                }
                assertEquals(totalShared, size.intValue());
                assertFalse(exception);
                return null;
            }
        });
    }

    @Test
    @Rollback
    @Ignore("Fixture for testing")
    public void testLoad() throws FileNotFoundException, Exception {
        Project p = new Project();
        String convertToXML = serializationService.convertToXML(p);
        // logger.debug(convertToXML);
        try {
            serializationService.parseXml(new StringReader(convertToXML));
            serializationService.parseXml(new FileReader(new File("c:/Users/abrin/Documents/1979.020.xml"))); // confirm goodxml loads fine
        } catch (Exception e) {
            logger.error("{}", e);
        }

    }

    // make sure we're detecting enum errors.
    @Test
    @Rollback
    public void testLoadWithBadEnumValue() throws Exception {

        Document document = createAndSaveNewInformationResource(Document.class);
        final Language VALID_LANGUAGE = Language.DUTCH;
        final String BAD_LANGUAGE = "Dagnabit!"; // har har
        document.setResourceLanguage(VALID_LANGUAGE);
        String goodXml = serializationService.convertToXML(document);
        Exception parseException = null;
        String badXml = goodXml.replace(
                "<tdar:resourceLanguage>" + VALID_LANGUAGE.name() + "</tdar:resourceLanguage>",
                "<tdar:resourceLanguage>" + BAD_LANGUAGE + "</tdar:resourceLanguage>");

        Object obj = null;
        serializationService.parseXml(new StringReader(goodXml)); // confirm goodxml loads fine
        try {
            obj = serializationService.parseXml(new StringReader(badXml));
        } catch (JaxbParsingException e) {
            parseException = e;
            logger.debug("parsing exception", e);
            logger.debug("exception events:{}", e.getEvents());
            assertEquals(2, e.getEvents().size());
            for (String event : e.getEvents()) {
                Assert.assertTrue(event.contains(BAD_LANGUAGE));
            }
        }
        logger.debug("{}", obj);
        assertNotNull("this xml has intentional errors and should cause parse exceptions", parseException);
        assertNull(obj);
    }

    @Test
    public void testValidateOAIStatic() throws ConfigurationException, SAXException, IOException {
        testValidXMLResponse(new FileInputStream(new File(TestConstants.TEST_XML_DIR, "oaidc_get_records.xml")),
                "http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd");
    }

    @Test
    public void testValidateSchema() throws ConfigurationException, SAXException, IOException {
        File schemaFile = new File("target/out.xsd");
        try {
            File generateSchema = serializationService.generateSchema();
            FileUtils.copyFile(generateSchema, schemaFile);
            testValidXMLSchemaResponse(FileUtils.readFileToString(generateSchema));
        } catch (Exception e) {
            logger.warn("exception", e);
            assertFalse("I should not exist. fix me, please?", true);
        }
    }

    @Test
    /**
     * Because our our rdbms does not include timezone w/ it's timestamp values, the safest approach
     * is to serialize dates in the local timezone using a format which specifies the timezone offset information.
     */
    public void testFileProxyDateSerialization() throws IOException {

        // march 9th, local time zone
        Date dt = new DateTime(2015, 3, 9, 0, 0).toDate();
        String json = serializationService.convertToJson(dt);
        assertThat(json, containsString("2015-03-09T00:00:00"));
        logger.debug("json:{}", json);

        FileProxy fileProxy = new FileProxy();
        fileProxy.setFileCreatedDate(dt);
        json = serializationService.convertToJson(fileProxy);
        assertThat(json, containsString("2015-03-09T00:00:00"));
        logger.debug("json:{}", json);

        InformationResourceFile informationResourceFile = new InformationResourceFile();
        informationResourceFile.setFileCreatedDate(new DateTime(2015, 3, 9, 0, 0).toDate());
        fileProxy = new FileProxy(informationResourceFile);
        json = serializationService.convertToJson(fileProxy);
        assertThat(json, containsString("2015-03-09T00:00:00"));
        logger.debug("json:{}", json);
    }

    @Test
    public void testFileProxySqlDateSerialization() throws IOException {
        // slight tweak: make the underlying date a java.sql.Date object.
        Date dt = new java.sql.Date(new DateTime(2015, 3, 9, 0, 0).toDate().getTime());
        InformationResourceFile informationResourceFile = new InformationResourceFile();
        informationResourceFile.setFileCreatedDate(dt);
        FileProxy fileProxy = new FileProxy(informationResourceFile);

        String json = serializationService.convertToJson(fileProxy);
        assertThat(json, containsString("2015-03-09T00:00:00"));
        logger.debug("json:{}", json);
    }

    @Test
    public void testUnicodeReplace() {
        List<Resource> findAll = genericService.findAll(Resource.class, Arrays.asList(371798L, 366254L));
        findAll.forEach(r -> {
            ExtendedDcTransformer.transformAny(r);
        });
//        ScrollableResults allScrollable = genericService.findAllScrollable(Resource.class, 100);
//        while (allScrollable.next()) {
//            Resource r = (Resource) allScrollable.get()[0];
//            ExtendedDcTransformer.transformAny(r);
//        }
    }
}
