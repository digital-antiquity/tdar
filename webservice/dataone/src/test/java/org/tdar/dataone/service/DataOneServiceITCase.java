package org.tdar.dataone.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.dataone.service.types.v1.ObjectList;
import org.dspace.foresite.OREException;
import org.dspace.foresite.ORESerialiserException;
import org.jdom2.JDOMException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SerializationService;
import org.tdar.transform.ExtendedDcTransformer;

import edu.asu.lib.qdc.QualifiedDublinCoreDocument;

public class DataOneServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private DataOneService service;

    @Autowired
    private GenericService genericService;

    @Autowired
    SerializationService serializationService;
    @Test
    @Rollback
    public void testOaiORE() throws OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException {
        Document doc = genericService.find(Document.class, 4230L);
        String mapDocument = service.createResourceMap(doc);
        logger.debug(mapDocument.toString());
    }

    String checksum = null;
    
    @Test
    @Rollback(false)
    public void testDCTransformer() throws Exception {
        Image doc = createAndSaveNewInformationResource(Image.class);
        LatitudeLongitudeBox box = new LatitudeLongitudeBox(10.0001,10.0001,10.000101,10.000101);
//        box.setObfuscatedEast(box.getObfuscatedEast());
//        box.setObfuscatedSouth(box.getObfuscatedSouth());
//        box.setObfuscatedNorth(box.getObfuscatedNorth());
//        box.setObfuscatedWest(box.getObfuscatedWest());

//        box.setObfuscatedValues();
        doc.getLatitudeLongitudeBoxes().add(box);
        logger.debug("n:{}", doc.getFirstActiveLatitudeLongitudeBox().getObfuscatedNorth());
        genericService.saveOrUpdate(doc);
        genericService.saveOrUpdate(doc.getLatitudeLongitudeBoxes());
        Long id = doc.getId();
        doc = null;
        for (int i=0;i< 1000; i++) {
             test(id, true);
        }
        genericService.clearCurrentSession();
        doc = genericService.find(Image.class, id);
        doc.getFirstLatitudeLongitudeBox().setEast(0.0);
        genericService.saveOrUpdate(doc);
        doc = null;
        test(id, false);
        
    }

    private String test(Long id, boolean same) throws JAXBException, UnsupportedEncodingException, NoSuchAlgorithmException, IOException {
        Image doc;
        genericService.synchronize();
        genericService.clearCurrentSession();
        doc = genericService.find(Image.class, id);
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
    public void testObjectTotals() throws UnsupportedEncodingException, NoSuchAlgorithmException, OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException, JAXBException {
    	ObjectList listObjectsResponse = service.getListObjectsResponse(null, null, null, null, 0, 10);
    	assertEquals(2, listObjectsResponse.getTotal());
    	assertEquals(2, listObjectsResponse.getObjectInfoList().size());
    }
}
