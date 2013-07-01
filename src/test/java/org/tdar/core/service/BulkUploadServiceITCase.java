/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.AsyncUpdateReceiver.DefaultReceiver;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.utils.ExcelUnit;
import org.tdar.utils.bulkUpload.BulkManifestProxy;
import org.tdar.utils.bulkUpload.BulkUploadTemplate;
import org.tdar.utils.bulkUpload.CellMetadata;

/**
 * @author Adam Brin
 * 
 */

@RunWith(MultipleTdarConfigurationRunner.class)
public class BulkUploadServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    BulkUploadService bulkUploadService;

    @Test
    @Rollback
    public void testLookupMaps() {
        LinkedHashSet<CellMetadata> importFields = bulkUploadService.getAllValidFieldNames(ResourceType.DOCUMENT);
        Map<String, CellMetadata> cellLookupMap = bulkUploadService.getCellLookupMap(importFields);
        assertTrue("testing local field", cellLookupMap.containsKey("documentType"));
        assertTrue("testing parent class", cellLookupMap.containsKey("description"));
        logger.info("{}", importFields);

        for (CellMetadata name : importFields) {
            logger.info("{}", name);
        }
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR, RunWithTdarConfiguration.FAIMS })
    public void testTemplate() throws FileNotFoundException, IOException {
        HSSFWorkbook workbook = bulkUploadService.createExcelTemplate();
        File file = File.createTempFile("templateTest", ".xls", TdarConfiguration.getInstance().getTempDirectory());
        workbook.write(new FileOutputStream(file));
        logger.info(file.getAbsolutePath());
        ExcelUnit excelUnit = new ExcelUnit();
        excelUnit.open(file);
        assertEquals("there should be 2 sheets", 2, excelUnit.getWorkbook().getNumberOfSheets());
        Sheet sheet = excelUnit.getWorkbook().getSheetAt(0);
        excelUnit.assertCellEquals(0, 0, BulkUploadTemplate.FILENAME + "*");
        excelUnit.assertCellEquals(1, 0, BulkUploadTemplate.EXAMPLE_PDF);
        excelUnit.assertCellEquals(2, 0, BulkUploadTemplate.EXAMPLE_TIFF);
        excelUnit.assertCellCommentEquals(0, 0, BulkImportField.FILENAME_DESCRIPTION);

        excelUnit.assertCellEquals(0, 1, BulkImportField.TITLE_LABEL + "*");
        excelUnit.assertCellCommentEquals(0, 1, BulkImportField.TITLE_DESCRIPTION);

        excelUnit.assertCellEquals(0, 2, BulkImportField.DESCRIPTION_LABEL + "*");
        excelUnit.assertCellCommentEquals(0, 2, BulkImportField.DESCRIPTION_DESCRIPTION);

        if (!TdarConfiguration.getInstance().getLicenseEnabled()) {
            excelUnit.assertRowDoesNotContain(0, BulkImportField.LICENSE_TYPE);
            excelUnit.assertCellEquals(0, 13, BulkImportField.YEAR_LABEL + "*");
            excelUnit.assertCellCommentEquals(0, 13, BulkImportField.YEAR_DESCRIPTION);
        } else {
            excelUnit.assertRowContains(0, BulkImportField.LICENSE_TYPE);
            excelUnit.assertRowContains(0, BulkImportField.YEAR_LABEL + "*");
        }

        if (!TdarConfiguration.getInstance().getCopyrightMandatory()) {
            excelUnit.assertRowDoesNotContain(0, BulkImportField.COPYRIGHT_HOLDER);
        } else {
            excelUnit.assertRowContains(0, BulkImportField.COPYRIGHT_HOLDER);
        }

        sheet.getRow(1).getCell(3).isPartOfArrayFormulaGroup();
    }

    public Map<String, Resource> setup() {
        Map<String, Resource> filenameResourceMap = new HashMap<String, Resource>();
        filenameResourceMap.put("test1.pdf", new Document());
        filenameResourceMap.put("test2.pdf", new Document());
        filenameResourceMap.put("image.jpg", new Image());
        filenameResourceMap.put("test3.pdf", new Document());
        filenameResourceMap.get("test2.pdf").setTitle("bad title");
        for (Resource r : filenameResourceMap.values()) {
            r.markUpdated(getUser());
        }
        return filenameResourceMap;
    }

    @Test
    @Rollback
    public <R extends Resource> void parseBasicFile() throws InvalidFormatException, IOException {
        BulkManifestProxy manifestProxy = generateManifest("manifest.xlsx");
        Map<String, Resource> filenameResourceMap = setup();
        AsyncUpdateReceiver receiver = new DefaultReceiver();
        bulkUploadService.readExcelFile(manifestProxy, filenameResourceMap, receiver);

        for (Resource resource : filenameResourceMap.values()) {
            logger.info("{}", resource);
            logger.info("title:" + resource.getTitle());
            logger.info("description:" + resource.getDescription());
            if (resource instanceof Document) {
                Document doc = (Document) resource;
                logger.info("documentType:" + doc.getDocumentType());
                logger.info("seriesNumber:" + doc.getSeriesNumber());
                logger.info("isbn:" + doc.getIsbn());
                // logger.info("numberOfPages:" + doc.getNumberOfPages());
            }
        }

        Document doc = (Document) filenameResourceMap.get("test1.pdf");
        assertEquals("History of art", doc.getTitle());
        assertEquals("Janson's History of Art", doc.getDescription());
        assertEquals("Fifth Edition", doc.getSeriesNumber());
        assertEquals("1234-5312", doc.getIsbn());
        // assertEquals(1234, doc.getNumberOfPages().intValue());
        assertEquals(DocumentType.OTHER, doc.getDocumentType());

        doc = (Document) filenameResourceMap.get("test2.pdf");
        assertEquals("The painted Sketch", doc.getTitle());
        assertEquals("whistler, etc", doc.getDescription());
        assertEquals(null, doc.getSeriesNumber());
        assertEquals(null, doc.getIsbn());
        // assertEquals(null, doc.getNumberOfPages());
        assertEquals(DocumentType.BOOK, doc.getDocumentType());

        doc = (Document) filenameResourceMap.get("test3.pdf");
        assertEquals("Modernism", doc.getTitle());
        assertEquals("\"V&A\"", doc.getDescription());
        assertEquals(null, doc.getSeriesNumber());
        assertEquals(null, doc.getIsbn());
        // assertEquals(null, doc.getNumberOfPages());
        assertEquals(DocumentType.THESIS, doc.getDocumentType());

    }

    private BulkManifestProxy generateManifest(String filename) throws IOException, InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(new FileInputStream(TestConstants.TEST_BULK_DIR + filename));
        BulkManifestProxy manifestProxy = bulkUploadService.validateManifestFile(workbook.getSheetAt(0));
        return manifestProxy;
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithBadEnum() throws InvalidFormatException, IOException {
        BulkManifestProxy manifestProxy = generateManifest("bad_enum_value.xlsx");
        Map<String, Resource> filenameResourceMap = setup();

        AsyncUpdateReceiver receiver = new DefaultReceiver();
        bulkUploadService.readExcelFile(manifestProxy, filenameResourceMap, receiver);
        assertTrue(receiver.getAsyncErrors().contains("is not a valid value for the"));
    }

    @Test
    @Rollback
    /* Note: this test tests that a bad field does not break the import... it's ignored */
    public <R extends Resource> void parseExcelFileWithBadField() {
        try {
            BulkManifestProxy manifestProxy = generateManifest("bad_field_name.xlsx");
            Map<String, Resource> filenameResourceMap = setup();

            AsyncUpdateReceiver receiver = new DefaultReceiver();
            bulkUploadService.readExcelFile(manifestProxy, filenameResourceMap, receiver);
        } catch (Exception e) {
            logger.info(e.getMessage());
            assertTrue("exception:" + ExceptionUtils.getFullStackTrace(e),e.getMessage().contains("the following columns are required: Date Created (Year)"));
        }
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithBadNumericField() throws InvalidFormatException, IOException {
        BulkManifestProxy manifestProxy = generateManifest("bad_int_value.xlsx");
        Map<String, Resource> filenameResourceMap = setup();

        AsyncUpdateReceiver receiver = new DefaultReceiver();
        bulkUploadService.readExcelFile(manifestProxy, filenameResourceMap, receiver);
        logger.info(receiver.getAsyncErrors());
        assertTrue(receiver.getAsyncErrors().contains("is expecting an integer value, but found"));
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithBadFilename() throws InvalidFormatException, IOException {
        BulkManifestProxy manifestProxy = generateManifest("bad_filename.xlsx");
        Map<String, Resource> filenameResourceMap = setup();

        AsyncUpdateReceiver receiver = new DefaultReceiver();
        bulkUploadService.readExcelFile(manifestProxy, filenameResourceMap, receiver);
        assertTrue(receiver.getAsyncErrors().contains("skipping line in excel file as resource with the filename"));
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithBadFirstColumn() {
        try {
            BulkManifestProxy manifestProxy = generateManifest("bad_first_column.xlsx");
            Map<String, Resource> filenameResourceMap = setup();

            AsyncUpdateReceiver receiver = new DefaultReceiver();
            bulkUploadService.readExcelFile(manifestProxy, filenameResourceMap, receiver);
        } catch (Exception e) {
            logger.info(e.getMessage());
            assertTrue(e.getMessage().contains("the first column must be the filename"));
        }
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithIncorrectField() throws InvalidFormatException, IOException {
        BulkManifestProxy manifestProxy = generateManifest("invalid_fieldname_for_class.xlsx");
        Map<String, Resource> filenameResourceMap = setup();

        AsyncUpdateReceiver receiver = new DefaultReceiver();
        bulkUploadService.readExcelFile(manifestProxy, filenameResourceMap, receiver);
        assertTrue(receiver.getAsyncErrors().contains("is not valid for the resource type"));
    }

    @Test
    @Rollback
    public <R extends Resource> void parseExcelFileWithEmptyIncorrectField() throws InvalidFormatException, IOException {
        BulkManifestProxy manifestProxy = generateManifest("image_manifest_empty_column.xlsx");
        Map<String, Resource> filenameResourceMap = new HashMap<String, Resource>();
        filenameResourceMap.put("5127663428_42ef7f4463_b.jpg", new Image());
        filenameResourceMap.put("handbook_of_archaeology.jpg", new Image());
        for (Resource r : filenameResourceMap.values()) {
            r.markUpdated(getUser());
        }
        boolean noException = true;
        try {
            AsyncUpdateReceiver receiver = new DefaultReceiver();
            bulkUploadService.readExcelFile(manifestProxy, filenameResourceMap, receiver);
        } catch (TdarRecoverableRuntimeException ex) {
            noException = false;
        }
        assertTrue(noException);
    }

}
