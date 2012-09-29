/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.AsyncUpdateReceiver.DefaultReceiver;

/**
 * @author Adam Brin
 * 
 */
public class BulkUploadServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    BulkUploadService bulkUploadService;

    @Test
    @Rollback
    public void testBulkUploadService() {
        LinkedHashSet<String> importFields = bulkUploadService.getImportFieldNamesForType(ResourceType.DOCUMENT);
        assertTrue("testing local field", importFields.contains("documentType"));
        assertTrue("testing parent class", importFields.contains("description"));
        logger.info(importFields);

        List<String> fieldnames = bulkUploadService.getAllValidFieldNames();
        for (String name : fieldnames) {
            logger.info(name);
        }
    }

    public Map<String, Resource> setup() throws FileNotFoundException {
        Map<String, Resource> filenameResourceMap = new HashMap<String, Resource>();
        filenameResourceMap.put("test1.pdf", new Document());
        filenameResourceMap.put("test2.pdf", new Document());
        filenameResourceMap.put("image.jpg", new Image());
        filenameResourceMap.put("test3.pdf", new Document());
        filenameResourceMap.get("test2.pdf").setTitle("bad title");
        for (Resource r : filenameResourceMap.values()) {
            r.markUpdated(getTestPerson());
        }
        return filenameResourceMap;
    }

    @Test
    @Rollback
    public <R extends Resource> void parseBasicFile() throws InvalidFormatException, IOException {
        FileInputStream inputStream = new FileInputStream(TestConstants.TEST_BULK_DIR + "/manifest.xlsx");
        Map<String, Resource> filenameResourceMap = setup();
        AsyncUpdateReceiver receiver = new DefaultReceiver();
        bulkUploadService.readExcelFile(inputStream, filenameResourceMap, receiver);

        for (Resource resource : filenameResourceMap.values()) {
            logger.info(resource);
            logger.info("title:" + resource.getTitle());
            logger.info("description:" + resource.getDescription());
            if (resource instanceof Document) {
                Document doc = (Document) resource;
                logger.info("documentType:" + doc.getDocumentType());
                logger.info("seriesNumber:" + doc.getSeriesNumber());
                logger.info("isbn:" + doc.getIsbn());
                logger.info("numberOfPages:" + doc.getNumberOfPages());
            }
        }

        Document doc = (Document) filenameResourceMap.get("test1.pdf");
        assertEquals("History of art", doc.getTitle());
        assertEquals("Janson's History of Art", doc.getDescription());
        assertEquals("Fifth Edition", doc.getSeriesNumber());
        assertEquals("1234-5312", doc.getIsbn());
        assertEquals(1234, doc.getNumberOfPages().intValue());
        assertEquals(DocumentType.OTHER, doc.getDocumentType());

        doc = (Document) filenameResourceMap.get("test2.pdf");
        assertEquals("The painted Sketch", doc.getTitle());
        assertEquals("whistler, etc", doc.getDescription());
        assertEquals(null, doc.getSeriesNumber());
        assertEquals(null, doc.getIsbn());
        assertEquals(null, doc.getNumberOfPages());
        assertEquals(DocumentType.BOOK, doc.getDocumentType());

        doc = (Document) filenameResourceMap.get("test3.pdf");
        assertEquals("Modernism", doc.getTitle());
        assertEquals("\"V&A\"", doc.getDescription());
        assertEquals(null, doc.getSeriesNumber());
        assertEquals(null, doc.getIsbn());
        assertEquals(null, doc.getNumberOfPages());
        assertEquals(DocumentType.THESIS, doc.getDocumentType());

    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithBadEnum() throws InvalidFormatException, IOException {
        FileInputStream inputStream = new FileInputStream(TestConstants.TEST_BULK_DIR + "/bad_enum_value.xlsx");
        Map<String, Resource> filenameResourceMap = setup();

        AsyncUpdateReceiver receiver = new DefaultReceiver();
        bulkUploadService.readExcelFile(inputStream, filenameResourceMap, receiver);
        assertTrue(receiver.getAsyncErrors().contains("is not a valid value for the"));
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithBadField() throws InvalidFormatException, IOException {
        FileInputStream inputStream = new FileInputStream(TestConstants.TEST_BULK_DIR + "/bad_field_name.xlsx");
        Map<String, Resource> filenameResourceMap = setup();

        AsyncUpdateReceiver receiver = new DefaultReceiver();
        try {
            bulkUploadService.readExcelFile(inputStream, filenameResourceMap, receiver);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("following column names are not"));
        }
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithBadNumericField() throws InvalidFormatException, IOException {
        FileInputStream inputStream = new FileInputStream(TestConstants.TEST_BULK_DIR + "/bad_int_value.xlsx");
        Map<String, Resource> filenameResourceMap = setup();

        AsyncUpdateReceiver receiver = new DefaultReceiver();
        bulkUploadService.readExcelFile(inputStream, filenameResourceMap, receiver);
        assertTrue(receiver.getAsyncErrors().contains("is expecting an integer value, but found"));
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithBadFilename() throws InvalidFormatException, IOException {
        FileInputStream inputStream = new FileInputStream(TestConstants.TEST_BULK_DIR + "/bad_filename.xlsx");
        Map<String, Resource> filenameResourceMap = setup();

        AsyncUpdateReceiver receiver = new DefaultReceiver();
        bulkUploadService.readExcelFile(inputStream, filenameResourceMap, receiver);
        assertTrue(receiver.getAsyncErrors().contains("a resource with the filename"));
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithBadFirstColumn() throws InvalidFormatException, IOException {
        FileInputStream inputStream = new FileInputStream(TestConstants.TEST_BULK_DIR + "/bad_first_column.xlsx");
        Map<String, Resource> filenameResourceMap = setup();

        AsyncUpdateReceiver receiver = new DefaultReceiver();
        try {
            bulkUploadService.readExcelFile(inputStream, filenameResourceMap, receiver);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("the first column must be the filename"));
        }
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithIncorrectField() throws InvalidFormatException, IOException {
        FileInputStream inputStream = new FileInputStream(TestConstants.TEST_BULK_DIR + "/invalid_fieldname_for_class.xlsx");
        Map<String, Resource> filenameResourceMap = setup();

        AsyncUpdateReceiver receiver = new DefaultReceiver();
        bulkUploadService.readExcelFile(inputStream, filenameResourceMap, receiver);
        assertTrue(receiver.getAsyncErrors().contains("is not valid for the resource type"));
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithEmptyIncorrectField() throws InvalidFormatException, IOException {
        FileInputStream inputStream = new FileInputStream(TestConstants.TEST_BULK_DIR + "/image_manifest empty column.xlsx");
        Map<String, Resource> filenameResourceMap = new HashMap<String, Resource>();
        filenameResourceMap.put("5127663428_42ef7f4463_b.jpg", new Image());
        filenameResourceMap.put("handbook_of_archaeology.jpg", new Image());
        for (Resource r : filenameResourceMap.values()) {
            r.markUpdated(getTestPerson());
        }
        boolean noException = true;
        try {
            AsyncUpdateReceiver receiver = new DefaultReceiver();
            bulkUploadService.readExcelFile(inputStream, filenameResourceMap, receiver);
        } catch (TdarRecoverableRuntimeException ex) {
            noException = false;
        }
        assertTrue(noException);
    }

}
