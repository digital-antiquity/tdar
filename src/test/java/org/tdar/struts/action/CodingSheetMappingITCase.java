/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractDataIntegrationTestCase;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.dataTable.DataTableColumnType;
import org.tdar.core.bean.resource.dataTable.MeasurementUnit;

import static org.junit.Assert.*;

/**
 * @author Adam Brin
 * 
 */
public class CodingSheetMappingITCase extends AbstractDataIntegrationTestCase {

    private static final String TEST_DATA_SET_FILE_PATH = TestConstants.TEST_DATA_INTEGRATION_DIR + "total-number-of-bones-per-period.xlsx";

    private static final File TEST_DATSET_FILE = new File(TEST_DATA_SET_FILE_PATH);
    private static final String EXCEL_FILE_NAME = "periods-modified-sm-01182011.xlsx";
    private static final String EXCEL_FILE_PATH = TestConstants.TEST_DATA_INTEGRATION_DIR + EXCEL_FILE_NAME;
    private String codingSheetFileName = "/coding sheet/csvCodingSheetText.csv";

    private static final String DOUBLE_CODING = "double_coding_key.csv";
    private static final String DOUBLE_DATASET = "double_translation_test_dataset.xlsx";
    private static final String BASIC_CSV = "csvCodingSheetText.csv";

    // TEST ME: http://dev.tdar.org/jira/browse/TDAR-587
    // TEST ME: http://dev.tdar.org/jira/browse/TDAR-581

    private static final String PATH = TestConstants.TEST_CODING_SHEET_DIR;

    @Test
    @Rollback
    public void testSimpleDelete() throws IOException {
        CodingSheet codingSheet = setupAndLoadResource(BASIC_CSV, CodingSheet.class);
        Set<CodingRule> oldCodingRules = new HashSet<CodingRule>(codingSheet.getCodingRules());

        CodingSheetController controller = generateNewInitializedController(CodingSheetController.class);
        controller.setResourceId(codingSheet.getId());
        controller.prepare();
        String text = (IOUtils.toString(getClass().getResourceAsStream(codingSheetFileName))).trim();
        text = text.substring(0, text.lastIndexOf("\n"));
        controller.setFileInputMethod("text");
        assertNotNull(text);
        assertFalse(text.contains("D, Don't"));
        controller.setFileTextInput(text);
        controller.save();
        Set<CodingRule> newCodingRules = controller.getCodingSheet().getCodingRules();
        logger.info("text was: " + text);
        logger.info(oldCodingRules);
        logger.info(newCodingRules);
        assertFalse(newCodingRules.equals(oldCodingRules));
        for (CodingRule rule : newCodingRules) {
            if (rule.getCode().equals("D")) {
                fail("Should not have found a 'D'");
            }
        }
    }

    @Test
    @Rollback
    public void testDoubleCoding() {

        CodingSheet codingSheet = setupAndLoadResource(DOUBLE_CODING, CodingSheet.class);
        Dataset dataset = setupAndLoadResource(DOUBLE_DATASET, Dataset.class);
        DataTable firstTable = dataset.getDataTables().iterator().next();
        assertNotNull(firstTable);
        DataTableColumn column = firstTable.getColumnByDisplayName("double");
        assertFalse(column.getName().equals(column.getDisplayName()));
        assertEquals(DataTableColumnEncodingType.NUMERIC, column.getColumnEncodingType());
        assertEquals(DataTableColumnType.DOUBLE, column.getColumnDataType());
        column.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
        column.setDefaultCodingSheet(codingSheet);
        datasetService.save(column);
        datasetService.translate(column, codingSheet);

        List<List<String>> selectAllFromDataTable = datasetService.selectAllFromDataTable(firstTable, 0, 100, true);
        assertEquals(7, selectAllFromDataTable.size());
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        for (List<String> row : selectAllFromDataTable) {
            logger.info(row);
            if (StringUtils.isNumeric(row.get(0))) {
                map.put(Integer.valueOf(row.get(0)), row.get(4));
            }
        }
        assertEquals("elephant", map.get(1));
        assertEquals("elephant", map.get(2));
        assertEquals("elephant", map.get(3));
        assertEquals("marble", map.get(4));
        assertEquals("watercolor", map.get(5));
        assertEquals("watercolor", map.get(6));
    }

    @Test
    @Rollback(true)
    public void testTabDelimitedCodingSheetUpload() throws IOException {
        CodingSheetController controller = generateNewInitializedController(CodingSheetController.class);
        controller.prepare();
        CodingSheet codingSheet = controller.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        controller.setFileInputMethod("text");
        String codingText = IOUtils.toString(getClass().getResourceAsStream(codingSheetFileName));
        assertNotNull(codingText);
        controller.setFileTextInput(codingText);
        controller.save();
        Long codingId = codingSheet.getId();
        assertNotNull(codingId);
        assertFalse(0 == codingSheet.getCodingRules().size());
        HashMap<String, CodingRule> ruleMap = new HashMap<String, CodingRule>();
        for (CodingRule rule : codingSheet.getCodingRules()) {
            ruleMap.put(rule.getCode(), rule);
        }
        assertTrue(ruleMap.get("a") != null);
        assertTrue(ruleMap.get("b") != null);
        assertTrue(ruleMap.get("1") != null);
        assertTrue(ruleMap.get("D") != null);
        assertEquals("test description", ruleMap.get("a").getDescription());
        assertEquals("Aardvark", ruleMap.get("a").getTerm());
        assertEquals("Bird", ruleMap.get("b").getTerm());
        assertEquals("numeric", ruleMap.get("1").getTerm());
        assertEquals("Don't", ruleMap.get("D").getTerm());

        Collection<InformationResourceFileVersion> latestVersions = codingSheet.getLatestVersions();

        controller = generateNewInitializedController(CodingSheetController.class);
        controller.setResourceId(codingId);
        controller.prepare();
        controller.loadBasicMetadata();
        controller.loadCustomMetadata();
        controller.setFileInputMethod("text");
        assertNotNull(controller.getFileTextInput());
        assertEquals(codingText, controller.getFileTextInput());

        controller.save();
        codingSheet = genericService.find(CodingSheet.class, codingId);
        // FIXME: brittle, use
        assertEquals(latestVersions, codingSheet.getLatestVersions());

        controller = generateNewInitializedController(CodingSheetController.class);
        controller.setResourceId(codingId);
        controller.prepare();
        controller.loadBasicMetadata();
        controller.loadCustomMetadata();
        controller.setFileInputMethod("text");
        controller.setFileTextInput(codingText + "abd ");
        controller.save();
        codingSheet = genericService.find(CodingSheet.class, codingId);
        genericService.merge(codingSheet);
        assertFalse(latestVersions.equals(codingSheet.getLatestVersions()));
    }

    @Test
    @Rollback(true)
    public void testFakeCodingSheetWithDataTable() throws IOException {
        CodingSheetController controller = generateNewInitializedController(CodingSheetController.class);
        controller.prepare();
        CodingSheet codingSheet = controller.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        controller.setFileInputMethod("text");
        String codingText = IOUtils.toString(getClass().getResourceAsStream(codingSheetFileName));
        assertNotNull(codingText);
        controller.setFileTextInput(codingText);
        controller.save();
        Dataset ds = new Dataset();
        ds.setTitle("test");
        ds.markUpdated(getTestPerson());
        genericService.save(ds);
        DataTable dt = new DataTable();
        dt.setName("test");
        dt.setDataset(ds);
        genericService.save(dt);
        DataTableColumn dtc = new DataTableColumn();
        dtc.setName("test");
        dtc.setDisplayName("test");
        dtc.setDataTable(dt);
        dtc.setColumnDataType(DataTableColumnType.VARCHAR);
        dtc.setDefaultCodingSheet(codingSheet);
        genericService.save(dtc);
        Set<CodingRule> exitingCodingRules = new HashSet<CodingRule>(codingSheet.getCodingRules());
        controller = generateNewInitializedController(CodingSheetController.class);
        controller.setResourceId(codingSheet.getId());
        controller.prepare();
        codingSheet = controller.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        controller.setFileInputMethod("text");
        codingText = IOUtils.toString(getClass().getResourceAsStream(codingSheetFileName));
        assertNotNull(codingText);
        controller.setFileTextInput(codingText);
        controller.save();

        Long codingId = codingSheet.getId();
        assertNotNull(codingId);
        assertFalse(0 == codingSheet.getCodingRules().size());
        HashMap<String, CodingRule> ruleMap = new HashMap<String, CodingRule>();
        for (CodingRule rule : codingSheet.getCodingRules()) {
            ruleMap.put(rule.getCode(), rule);
        }
        logger.debug("existing:" + exitingCodingRules);
        logger.debug("new:" + codingSheet.getCodingRules());
        for (CodingRule existing : exitingCodingRules) {
            assertEquals("expecting matching ids for key:" + existing.getCode(), existing.getId(), ruleMap.get(existing.getCode()).getId());
        }
        assertTrue(ruleMap.get("a") != null);
        assertTrue(ruleMap.get("b") != null);
        assertTrue(ruleMap.get("1") != null);
        assertTrue(ruleMap.get("D") != null);
        assertEquals("test description", ruleMap.get("a").getDescription());
        assertEquals("Aardvark", ruleMap.get("a").getTerm());
        assertEquals("Bird", ruleMap.get("b").getTerm());
        assertEquals("numeric", ruleMap.get("1").getTerm());
        assertEquals("Don't", ruleMap.get("D").getTerm());

    }

    @Test
    @Rollback(true)
    public void testXLSCodingSheetUpload() throws IOException {
        CodingSheet codingSheet = setupCodingSheet();

        HashMap<String, CodingRule> ruleMap = new HashMap<String, CodingRule>();
        for (CodingRule rule : codingSheet.getCodingRules()) {
            ruleMap.put(rule.getCode(), rule);
        }
        assertTrue(ruleMap.get("12") != null);
        assertTrue(ruleMap.get("6") != null);
        assertEquals("g", ruleMap.get("12").getTerm());
        assertEquals("m", ruleMap.get("6").getTerm());
    }

    @Test
    @Rollback(true)
    public void testCodingSheetMapping() {
        CodingSheet codingSheet = setupCodingSheet();

        DatasetController datasetController = generateNewInitializedController(DatasetController.class);
        datasetController.prepare();
        Dataset dataset = datasetController.getDataset();
        dataset.setTitle("test dataset");
        dataset.setDescription("test description");
        List<File> uploadedFiles = new ArrayList<File>();
        List<String> uploadedFileNames = new ArrayList<String>();
        uploadedFileNames.add(TEST_DATSET_FILE.getName());
        uploadedFiles.add(TEST_DATSET_FILE);
        datasetController.setUploadedFiles(uploadedFiles);
        datasetController.setUploadedFilesFileName(uploadedFileNames);
        datasetController.save();
        Long datasetId = dataset.getId();
        assertNotNull(datasetId);

        datasetController = generateNewInitializedController(DatasetController.class);
        datasetController.setResourceId(datasetId);
        datasetController.prepare();
        datasetController.editColumnMetadata();
        setupDatasetControllerForMapping(datasetController, codingSheet.getId());
        datasetController.saveColumnMetadata();
        for (DataTableColumn dtc : dataset.getDataTables().iterator().next().getDataTableColumns()) {
            logger.debug(dtc.getName());
            if (dtc.getName().equals("Period")) {
                assertEquals(dtc.getDefaultCodingSheet().getId(), codingSheet.getId());
            }
        }
        datasetService.createTranslatedFile(dataset);
    }

    @Test
    @Rollback(true)
    public void testDatasetMappingPreservation() {
        CodingSheet codingSheet = setupCodingSheet();
        Dataset dataset = setupIntegrationDataset(TEST_DATSET_FILE, "Test Dataset");
        DatasetController datasetController = generateNewInitializedController(DatasetController.class);
        Long datasetId = dataset.getId();
        datasetController.setResourceId(datasetId);
        datasetController.prepare();
        datasetController.editColumnMetadata();
        setupDatasetControllerForMapping(datasetController, codingSheet.getId());
        datasetController.saveColumnMetadata();
        DataTableColumn periodColumn = null;
        for (DataTableColumn dtc : dataset.getDataTables().iterator().next().getDataTableColumns()) {
            logger.debug(dtc.getName());
            if (dtc.getName().equals("Period")) {
                periodColumn = dtc;
                assertEquals(dtc.getDefaultCodingSheet().getId(), codingSheet.getId());
            }
        }
        datasetService.createTranslatedFile(dataset);

        Dataset newDataset = setupIntegrationDataset(TEST_DATSET_FILE, "Test Dataset", datasetId);

        for (DataTable incomingDataTable : newDataset.getDataTables()) {
            for (DataTableColumn incomingColumn : incomingDataTable.getDataTableColumns()) {
                if (incomingColumn.getName().equals("Period")) {
                    // default id equality
                    assertEquals(incomingColumn, periodColumn);
                    assertEquals(incomingColumn.getDefaultCodingSheet().getId(), codingSheet.getId());
                }

            }
        }

    }

    public Dataset setupIntegrationDataset(File file, String datasetTitle) {
        return setupIntegrationDataset(file, datasetTitle, null);
    }

    public Dataset setupIntegrationDataset(File file, String datasetTitle, Long datasetId) {
        DatasetController datasetController = generateNewInitializedController(DatasetController.class);
        logger.info("setting resource id: " + datasetId);
        if (datasetId != null) {
            datasetController.setResourceId(datasetId);
        }
        datasetController.prepare();

        Dataset dataset = datasetController.getDataset();
        dataset.setTitle(datasetTitle);
        dataset.setDescription("test description");
        List<File> uploadedFiles = new ArrayList<File>();
        List<String> uploadedFileNames = new ArrayList<String>();
        uploadedFileNames.add(file.getName());
        uploadedFiles.add(file);
        datasetController.setUploadedFiles(uploadedFiles);
        datasetController.setUploadedFilesFileName(uploadedFileNames);
        datasetController.save();
        assertNotNull(dataset.getId());
        return dataset;
    }

    /**
     * @return
     */
    private CodingSheet setupCodingSheet() {
        CodingSheetController codingSheetController = generateNewInitializedController(CodingSheetController.class);
        codingSheetController.prepare();
        CodingSheet codingSheet = codingSheetController.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        List<File> uploadedFiles = new ArrayList<File>();
        List<String> uploadedFileNames = new ArrayList<String>();
        uploadedFiles.add(new File(EXCEL_FILE_PATH));
        uploadedFileNames.add(EXCEL_FILE_NAME);
        codingSheetController.setUploadedFilesFileName(uploadedFileNames);
        codingSheetController.setUploadedFiles(uploadedFiles);
        codingSheetController.save();
        Long codingId = codingSheet.getId();
        assertNotNull(codingId);
        assertFalse(codingSheet.getCodingRules().isEmpty());
        return codingSheet;
    }

    private void setupDatasetControllerForMapping(DatasetController datasetController, Long codingId) {
        List<DataTableColumnEncodingType> columnEncodingTypes = new ArrayList<DataTableColumnEncodingType>();
        List<MeasurementUnit> measurements = new ArrayList<MeasurementUnit>();
        List<String> descriptions = new ArrayList<String>();
        List<Long> codingSheetIds = new ArrayList<Long>();
        List<Long> categoryIds = new ArrayList<Long>();
        List<Long> subcategoryIds = new ArrayList<Long>();
        List<Long> ontologyIds = new ArrayList<Long>();
        categoryIds.add(null);
        subcategoryIds.add(null);
        descriptions.add("");
        measurements.add(null);
        columnEncodingTypes.add(DataTableColumnEncodingType.CODED_VALUE);
        codingSheetIds.add(codingId);
        ontologyIds.add(null);
        ontologyIds.add(null);
        categoryIds.add(null);
        subcategoryIds.add(null);
        descriptions.add("");
        measurements.add(null);
        columnEncodingTypes.add(null);
        codingSheetIds.add(null);

        datasetController.setColumnEncodingTypes(columnEncodingTypes);
        datasetController.setCodingSheetIds(codingSheetIds);
        datasetController.setMeasurementUnits(measurements);
        datasetController.setColumnDescriptions(descriptions);
        datasetController.setCategoryVariableIds(categoryIds);
        datasetController.setSubcategoryIds(subcategoryIds);
        datasetController.setOntologyIds(ontologyIds);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractControllerITCase#getController()
     */
    @Override
    protected TdarActionSupport getController() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getDatabaseList() {
        // TODO Auto-generated method stub
        return new String[0];
    }

    @Override
    protected String getTestFilePath() {
        return PATH;
    }

}
