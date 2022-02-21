package org.tdar.dataone.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.dataone.service.types.v1.ObjectList;
import org.dspace.foresite.OREException;
import org.dspace.foresite.ORESerialiserException;
import org.jdom2.JDOMException;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.dao.base.DoiDao;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.dataone.DataOneAppConfiguration;
import org.tdar.utils.TestConfiguration;

@ContextConfiguration(classes = DataOneAppConfiguration.class)
@SuppressWarnings("rawtypes")
public class DataOneServiceITCase extends AbstractTransactionalJUnit4SpringContextTests {

    private static final String TEST_META = "doi:10.6067:XCV8SN0B29" + DataOneService.D1_SEP + DataOneService.META;
    private static final String BASE = "doi:10.6067:XCV8SN0B29" + DataOneService.D1_SEP + DataOneService.D1_FORMAT;
    private static final String TEST_DOI = BASE + "1281812043684";

    @Autowired
    private DataOneService service;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    protected DoiDao doiDao;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    // @Autowired
    // SerializationService serializationService;
    @Test
    @Rollback
    @Ignore("temporarily ignoring:TDAR-6445")
    public void testOaiORE() throws OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException {
        Document doc = genericDao.find(Document.class, 4230L);
        String mapDocument = service.createResourceMap(doc);
        logger.debug(mapDocument.toString());
    }

    String checksum = null;

    @Test
    public void testIdentifierParser() {
        IdentifierParser ip = new IdentifierParser(TEST_META, doiDao);
        logger.debug(ip.getDoi());
        logger.debug(ip.getType().name());
    }

    @Test
    public void testMetadataRequest() {
        service.metadataRequest(TEST_META);
    }

    @Test
    @Rollback(false)
    @Ignore("temporarily ignoring:TDAR-6445")
    public void testDCTransformer() throws Exception {
        Image doc = new Image();
        doc.setTitle("test");
        doc.setDescription("test");
        doc.markUpdated(genericDao.find(TdarUser.class, TestConfiguration.getInstance().getUserId()));
        doc.setDate(2012);
        LatitudeLongitudeBox box = new LatitudeLongitudeBox(10.0001, 10.0001, 10.000101, 10.000101);
        // box.setObfuscatedEast(box.getObfuscatedEast());
        // box.setObfuscatedSouth(box.getObfuscatedSouth());
        // box.setObfuscatedNorth(box.getObfuscatedNorth());
        // box.setObfuscatedWest(box.getObfuscatedWest());

        // box.setObfuscatedValues();
        doc.getLatitudeLongitudeBoxes().add(box);
        logger.debug("n:{}", doc.getFirstActiveLatitudeLongitudeBox().getObfuscatedNorth());
        genericDao.saveOrUpdate(doc);
        genericDao.saveOrUpdate(doc.getLatitudeLongitudeBoxes());
        Long id = doc.getId();
        doc = null;
        for (int i = 0; i < 1000; i++) {
            test(id, true);
        }
        genericDao.clearCurrentSession();
        doc = genericDao.find(Image.class, id);
        doc.getFirstLatitudeLongitudeBox().setEast(0.0);
        genericDao.saveOrUpdate(doc);
        doc = null;
        test(id, false);

    }

    private String test(Long id, boolean same) throws JAXBException, UnsupportedEncodingException, NoSuchAlgorithmException, IOException {
        Image doc;
        genericDao.synchronize();
        genericDao.clearCurrentSession();
        doc = genericDao.find(Image.class, id);
        logger.debug("n:{}", doc.getFirstActiveLatitudeLongitudeBox().getObfuscatedNorth());
        ObjectResponseContainer object = service.constructMetadataFormatObject(doc);
        if (checksum == null) {
            checksum = object.getChecksum();
        }
        if (same) {
            assertEquals(checksum, object.getChecksum());
        } else {
            assertNotEquals(checksum, object.getChecksum());
        }
        logger.debug(IOUtils.toString(object.getReader()));
        return checksum;
    }

    @Test
    @Rollback
    public void testObjectTotals() throws UnsupportedEncodingException, NoSuchAlgorithmException, OREException, URISyntaxException, ORESerialiserException,
            JDOMException, IOException, JAXBException {
        service.synchronizeTdarChangesWithDataOneObjects();
        ObjectList listObjectsResponse = service.getListObjectsResponse(null, null, null, null, 0, 10);
        assertEquals(4, listObjectsResponse.getTotal());
        assertEquals(4, listObjectsResponse.getObjectInfoList().size());
    }

    @Test
    @Rollback
    public void testObjectCounts() throws UnsupportedEncodingException, NoSuchAlgorithmException, OREException, URISyntaxException, ORESerialiserException,
            JDOMException, IOException, JAXBException {
        service.synchronizeTdarChangesWithDataOneObjects();
        ObjectList listObjectsResponse = service.getListObjectsResponse(null, null, null, null, 0, 2);
        assertEquals(2, listObjectsResponse.getCount());
        assertEquals(4, listObjectsResponse.getTotal());
        assertEquals(2, listObjectsResponse.getObjectInfoList().size());
    }

    @Test
    @Rollback
    public void testObjectStarts() throws UnsupportedEncodingException, NoSuchAlgorithmException, OREException, URISyntaxException, ORESerialiserException,
            JDOMException, IOException, JAXBException {
        service.synchronizeTdarChangesWithDataOneObjects();
        ObjectList listObjectsResponse = service.getListObjectsResponse(null, null, null, null, 3, 2);
        assertEquals(1, listObjectsResponse.getCount());
        assertEquals(4, listObjectsResponse.getTotal());
        assertEquals(1, listObjectsResponse.getObjectInfoList().size());
    }

    public static void assertNotEquals(Object obj1, Object obj2) {
        assertNotEquals("", obj1, obj2);
    }

    public static void assertNotEquals(String msg, Object obj1, Object obj2) {
        if (StringUtils.isNotBlank(msg)) {
            assertTrue(msg, ObjectUtils.notEqual(obj1, obj2));
        } else {
            assertTrue(String.format("'%s' == '%s'", obj1, obj2), ObjectUtils.notEqual(obj1, obj2));
        }
    }
}
