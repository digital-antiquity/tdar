/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.resource.dataset.ResultMetadataWrapper;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts.action.codingSheet.CodingSheetController;
import org.tdar.struts.action.codingSheet.CodingSheetMappingController;
import org.tdar.struts.action.dataset.ColumnMetadataController;
import org.tdar.struts.action.download.DownloadController;
import org.tdar.utils.ExcelUnit;

/**
 * @author Adam Brin
 * 
 */
@RunWith(MultipleTdarConfigurationRunner.class)
public class CodingSheetMappingITCase extends AbstractDataIntegrationTestCase {

    private static final String TEST_DATASET_FILENAME = "total-number-of-bones-per-period.xlsx";

    private static final String EXCEL_FILE_NAME = "periods-modified-sm-01182011.xlsx";
    private static final String EXCEL_FILE_NAME2 = "periods-modified-sm-01182011-2.xlsx";
    private static final String EXCEL_FILE_PATH = TestConstants.TEST_DATA_INTEGRATION_DIR + EXCEL_FILE_NAME;
    private static final File PERIOD_1 = new File(TestConstants.TEST_CODING_SHEET_DIR + "period.csv");
    private static final File PERIOD_2 = new File(TestConstants.TEST_CODING_SHEET_DIR + "period2.csv");
    private static final String EXCEL_FILE_PATH2 = TestConstants.TEST_DATA_INTEGRATION_DIR + EXCEL_FILE_NAME2;
    private String codingSheetFileName = TestConstants.TEST_ROOT_DIR + "/coding sheet/csvCodingSheetText.csv";

    private InformationResourceFile tranlatedIRFile;

    private static final String DOUBLE_CODING = "double_coding_key.csv";
    private static final String DOUBLE_DATASET = "double_translation_test_dataset.xlsx";
    private static final String BASIC_CSV = "csvCodingSheetText.csv";

    private static final String PATH = TestConstants.TEST_CODING_SHEET_DIR;

    @Test
    @Rollback
    /**
     * @return
     * @throws TdarActionException
     */
    public void testInvalidCodingSheet() throws TdarActionException {
        setIgnoreActionErrors(true);
        CodingSheetController codingSheetController = generateNewInitializedController(CodingSheetController.class);
        codingSheetController.prepare();
        CodingSheet codingSheet = codingSheetController.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        codingSheetController.setFileInputMethod("text");
        codingSheetController.setFileTextInput("a,");
        codingSheetController.setServletRequest(getServletPostRequest());
        codingSheetController.save();
        Long codingId = codingSheet.getId();
        assertNotNull(codingId);
        assertTrue(codingSheet.getCodingRules().isEmpty());
        assertFalse(codingSheetController.getActionErrors().size() == 0);

    }

    @Test
    @Rollback
    public void testSimpleDelete() throws Exception {
        CodingSheet codingSheet = setupAndLoadResource(BASIC_CSV, CodingSheet.class);
        Set<CodingRule> oldCodingRules = new HashSet<CodingRule>(codingSheet.getCodingRules());

        CodingSheetController controller = generateNewInitializedController(CodingSheetController.class);
        controller.setId(codingSheet.getId());
        controller.prepare();
        String text = readToText(codingSheetFileName);
        text = text.substring(0, text.lastIndexOf("\n"));
        controller.setFileInputMethod("text");
        assertNotNull(text);
        assertFalse(text.contains("D, Don't"));
        controller.setFileTextInput(text);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Set<CodingRule> newCodingRules = controller.getCodingSheet().getCodingRules();
        logger.info("text was: " + text);
        logger.info("{}", oldCodingRules);
        logger.info("{}", newCodingRules);
        assertFalse(newCodingRules.equals(oldCodingRules));
        for (CodingRule rule : newCodingRules) {
            if (rule.getCode().equals("D")) {
                fail("Should not have found a 'D'");
            }
        }
    }

	private String readToText(String filename) throws IOException, FileNotFoundException {
		File file = new File(filename);
        String text = IOUtils.toString(new FileReader(file)).trim();
		return text;
	}

    @Test
    @Rollback
    public void testDegenerateCodingSheetWithTabs() throws IOException, TdarActionException {
        setIgnoreActionErrors(true);
        CodingSheet codingSheet = setupAndLoadResource("tab_as_csv.csv", CodingSheet.class);
        assertEquals(FileStatus.PROCESSING_ERROR, codingSheet.getFirstInformationResourceFile().getStatus());
        assertTrue(CollectionUtils.isNotEmpty(getActionErrors()));
    }

    @Test
    @Rollback
    public void testDoubleCoding() throws TdarActionException {
        CodingSheet codingSheet = setupAndLoadResource(DOUBLE_CODING, CodingSheet.class);
        Dataset dataset = setupAndLoadResource(DOUBLE_DATASET, Dataset.class);
        DataTable firstTable = dataset.getDataTables().iterator().next();
        assertNotNull(firstTable);
        DataTableColumn column = firstTable.getColumnByDisplayName("double");
        assertFalse(column.getName().equals(column.getDisplayName()));
        assertEquals(DataTableColumnEncodingType.UNCODED_VALUE, column.getColumnEncodingType());
        assertEquals(DataTableColumnType.DOUBLE, column.getColumnDataType());
        column.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
        column.setDefaultCodingSheet(codingSheet);
        genericService.save(column);
        datasetService.translate(column, codingSheet);

        ResultMetadataWrapper resultsWrapper = datasetService.selectAllFromDataTable(firstTable, 0, 100, true, false);
        List<List<String>> selectAllFromDataTable = resultsWrapper.getResults();
        assertEquals(6, selectAllFromDataTable.size());
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        int idRow = -1;
        int colRow = -1;
        for (int i = 0; i < resultsWrapper.getFields().size(); i++) {
            if (resultsWrapper.getFields().get(i).equals(DataTableColumn.TDAR_ROW_ID)) {
                idRow = i;
            }
            if (resultsWrapper.getFields().get(i).equals(column)) {
                colRow = i;
            }
        }
        assertFalse(colRow == -1);
        assertTrue("id row is hidden and should be -1", idRow == -1);

        idRow = 1;
        for (List<String> row : selectAllFromDataTable) {
            map.put(idRow, row.get(colRow));
            logger.info("{}: {}", idRow, row.get(colRow));
            idRow++;
        }
        assertEquals("elephant", map.get(1));
        assertEquals("elephant", map.get(2));
        assertEquals("elephant", map.get(3));
        assertEquals("marble", map.get(4));
        assertEquals("watercolor", map.get(5));
        assertEquals("watercolor", map.get(6));
    }

    @Test
    @Rollback
    public void testTabDelimitedCodingSheetUpload() throws Exception {
        CodingSheetController controller = generateNewInitializedController(CodingSheetController.class);
        controller.prepare();
        CodingSheet codingSheet = controller.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        controller.setFileInputMethod("text");
        String codingText = readToText(codingSheetFileName);
        assertNotNull(codingText);
        controller.setFileTextInput(codingText);
        controller.setServletRequest(getServletPostRequest());
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
        controller.setId(codingId);
        controller.prepare();
        controller.edit();
        controller.setFileInputMethod("text");
        assertNotNull(controller.getFileTextInput());
        assertEquals(codingText, controller.getFileTextInput());

        controller.setServletRequest(getServletPostRequest());
        controller.save();
        codingSheet = genericService.find(CodingSheet.class, codingId);
        // FIXME: brittle, use
        assertEquals(latestVersions, codingSheet.getLatestVersions());

        controller = generateNewInitializedController(CodingSheetController.class);
        controller.setId(codingId);
        controller.prepare();
        controller.edit();
        controller.setFileInputMethod("text");
        controller.setFileTextInput(codingText + "abd ");
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        codingSheet = genericService.find(CodingSheet.class, codingId);
        genericService.merge(codingSheet);
        assertFalse(latestVersions.equals(codingSheet.getLatestVersions()));
    }

    @Test
    @Rollback
    public void testFakeCodingSheetWithDataTable() throws Exception {
        CodingSheetController controller = generateNewInitializedController(CodingSheetController.class);
        controller.prepare();
        CodingSheet codingSheet = controller.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        controller.setFileInputMethod("text");
        String codingText = readToText(codingSheetFileName);
        assertNotNull(codingText);
        controller.setFileTextInput(codingText);
        codingSheet.setDate(1243);
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        Set<CodingRule> exitingCodingRules = new HashSet<CodingRule>(codingSheet.getCodingRules());
        controller = generateNewInitializedController(CodingSheetController.class);
        controller.setId(codingSheet.getId());
        controller.prepare();
        codingSheet = controller.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");
        controller.setFileInputMethod("text");
        codingText = readToText(codingSheetFileName);
        assertNotNull(codingText);
        controller.setFileTextInput(codingText);
        controller.setServletRequest(getServletPostRequest());
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
    @Rollback
    public void testXLSCodingSheetUpload() throws Exception {
        CodingSheet codingSheet = setupCodingSheet();

        Ontology ontology = setupAndLoadResource("fauna-element-ontology.txt", Ontology.class);
        List<OntologyNode> nodes = ontology.getOntologyNodes();
        HashMap<String, CodingRule> ruleMap = new HashMap<String, CodingRule>();
        int totalRules = codingSheet.getCodingRules().size();
        int count = 0;
        // valid because not mapped to ontology
        assertFalse(codingSheet.isMappedImproperly());
        codingSheet.setDefaultOntology(ontology);
        // invalid because ontology is mapped
        assertTrue(codingSheet.isMappedImproperly());
        for (CodingRule rule : codingSheet.getCodingRules()) {
            ruleMap.put(rule.getCode(), rule);
            rule.setOntologyNode(nodes.get(count));
            count++;
            if (count < totalRules / 4) {
                assertTrue(codingSheet.isMappedImproperly());
            } else {
                assertFalse(codingSheet.isMappedImproperly());
            }
        }
        logger.debug("total:{}", count);
        assertTrue(ruleMap.get("12") != null);
        assertTrue(ruleMap.get("6") != null);
        assertEquals("g", ruleMap.get("12").getTerm());
        assertEquals("m", ruleMap.get("6").getTerm());

    }

    @Test
    @Rollback
    public void testXLSCodingSheetUploadDoesNotGoThroughDatasetWorkflow() throws Exception {
        CodingSheet codingSheet = setupCodingSheet("semidegen.xlsx", TestConstants.TEST_CODING_SHEET_DIR + "semidegen.xlsx", null, null);
        for (InformationResourceFile file : codingSheet.getInformationResourceFiles()) {
            Dataset transientDataset = (Dataset) file.getWorkflowContext().getTransientResource();
            logger.info("file: {} ", file);
            logger.info("dataset: {} ", transientDataset);
            assertTrue((transientDataset == null) || CollectionUtils.isEmpty(transientDataset.getDataTables()));
            assertTrue(CollectionUtils.isEmpty(file.getWorkflowContext().getExceptions()));
        }
    }

    @Test
    @Rollback
    public void testCodingSheetMappingReplace() throws Exception {
        CodingSheet codingSheet = setupCodingSheet();
        CodingSheetController codingSheetController = generateNewInitializedController(CodingSheetController.class);
        codingSheetController.setId(codingSheet.getId());
        codingSheetController.prepare();
        List<File> uploadedFiles = new ArrayList<File>();
        List<String> uploadedFileNames = new ArrayList<String>();
        uploadedFiles.add(new File(EXCEL_FILE_PATH2));
        uploadedFileNames.add(EXCEL_FILE_NAME2);
        codingSheetController.setUploadedFilesFileName(uploadedFileNames);
        codingSheetController.setUploadedFiles(uploadedFiles);
        codingSheetController.setServletRequest(getServletPostRequest());
        codingSheetController.save();
        Long codingId = codingSheet.getId();
        assertNotNull(codingId);
        Set<CodingRule> rules = codingSheet.getCodingRules();
        assertFalse(rules.isEmpty());
        boolean found = false;
        for (CodingRule rule : rules) {
            if (rule.getCode().equals("0")) {
                assertEquals("aaaa", rule.getTerm());
                assertEquals("1234", rule.getDescription());
                found = true;
            }
        }
        // need to add ontology nodes to the mix
        assertTrue(found);
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testCodingSheetMapping() throws Exception {
        CodingSheet codingSheet = setupCodingSheet();
        Dataset dataset = setupDatasetWithCodingSheet(codingSheet);
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testCodingSheetMappingReplace2() throws Exception {
        CodingSheet codingSheet = setupCodingSheet(null, null, null, PERIOD_1);
        Long codingId = codingSheet.getId();
        Dataset dataset = setupDatasetWithCodingSheet(codingSheet);
        codingSheet = null;
        CodingSheetController codingSheetController = generateNewInitializedController(CodingSheetController.class);
        evictCache();
        codingSheetController.setId(codingId);
        codingSheetController.prepare();
        codingSheet = codingSheetController.getCodingSheet();

        // List<File> uploadedFiles = new ArrayList<File>();
        // List<String> uploadedFileNames = new ArrayList<String>();
        // uploadedFiles.add(PERIOD_2);
        // uploadedFileNames.add(PERIOD_2.getName());
        // codingSheetController.setUploadedFilesFileName(uploadedFileNames);
        // codingSheetController.setUploadedFiles(uploadedFiles);
        codingSheetController.setFileTextInput(FileUtils.readFileToString(PERIOD_2));
        codingSheetController.setFileInputMethod(AbstractInformationResourceController.FILE_INPUT_METHOD);
        codingSheetController.setServletRequest(getServletPostRequest());
        codingSheetController.save();
        assertNotNull(codingId);
        assertFalse(codingSheet.getCodingRules().isEmpty());

    }

    private Dataset setupDatasetWithCodingSheet(CodingSheet codingSheet) throws Exception {
        Dataset dataset = setupAndLoadResource(TestConstants.TEST_DATA_INTEGRATION_DIR + TEST_DATASET_FILENAME, Dataset.class);
        Long datasetId = dataset.getId();
        assertNotNull(datasetId);
        DataTableColumn period_ = cloneDataTableColumn(dataset.getDataTables().iterator().next().getColumnByDisplayName("Period"));
        ColumnMetadataController datasetController = generateNewInitializedController(ColumnMetadataController.class);
        datasetController.setId(datasetId);
        datasetController.prepare();
        datasetController.editColumnMetadata();
        period_.setDefaultCodingSheet(codingSheet);
        datasetController.saveColumnMetadata();
        dataset = null;
        dataset = genericService.find(Dataset.class, datasetId);
        assertTrue(CollectionUtils.isNotEmpty(dataset.getDataTables()));
        for (DataTable table : dataset.getDataTables()) {
            for (DataTableColumn dtc : table.getDataTableColumns()) {
                logger.debug(dtc.getName());
                if (dtc.getName().equals("Period")) {
                    assertEquals(dtc.getDefaultCodingSheet().getId(), codingSheet.getId());
                }
            }
        }

        tranlatedIRFile = datasetService.createTranslatedFile(dataset);
        return dataset;
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith=RunWithTdarConfiguration.SMALL_EXCEL)
    public void testBigDatasetSpansSheets() throws Exception {
        try {
            // setup coding sheet
            CodingSheet codingSheet = createAndSaveNewInformationResource(CodingSheet.class);
            Set<CodingRule> rules = codingSheet.getCodingRules();
            rules.add(createRule("1", "one", codingSheet));
            rules.add(createRule("2", "two", codingSheet));
            rules.add(createRule("3", "three", codingSheet));
            genericService.save(codingSheet);

            // File bigFile = new File(TestConstants.TEST_DATA_INTEGRATION_DIR + "bigsheet.xlsx");

            Dataset dataset = setupAndLoadResource(TestConstants.TEST_DATA_INTEGRATION_DIR + "bigsheet.xlsx", Dataset.class);
            Long datasetId = dataset.getId();
            assertNotNull(datasetId);
            DataTableColumn num = cloneDataTableColumn(dataset.getDataTableByGenericName("ds1").getColumnByDisplayName("num"));
            assertNotNull(num);
            ColumnMetadataController datasetController = generateNewInitializedController(ColumnMetadataController.class);
            datasetController.setId(datasetId);
            datasetController.prepare();
            datasetController.editColumnMetadata();
            num.setDefaultCodingSheet(codingSheet);
            datasetController.saveColumnMetadata();
            dataset = null;
            dataset = genericService.find(Dataset.class, datasetId);

            InformationResourceFile translatedFile = datasetService.createTranslatedFile(dataset);
            ExcelUnit excelUnit = new ExcelUnit();
            excelUnit.open(TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, translatedFile.getTranslatedFile()));
            assertTrue("there should be more than 2 sheets", 2 < excelUnit.getWorkbook().getNumberOfSheets());

            DownloadController dc = generateNewInitializedController(DownloadController.class);
            dc.setInformationResourceFileVersionId(translatedFile.getLatestTranslatedVersion().getId());
            dc.prepare();
            dc.execute();
            assertEquals("bigsheet_translated.xlsx", dc.getDownloadTransferObject().getFileName());

        } catch (OutOfMemoryError oem) {
            logger.debug("Well, guess I ran out of memory...", oem);
        }
    }

    @Test
    @Rollback
    public void testExcelOutput() throws Exception {
        testCodingSheetMapping();
        assertNotNull("file proxy was null", tranlatedIRFile);
        ExcelUnit excelUnit = new ExcelUnit();
        File retrieveFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, tranlatedIRFile.getTranslatedFile());
        excelUnit.open(retrieveFile);

        excelUnit.selectSheet("total_number_of_bones_per_perio");
        excelUnit.assertCellEquals(0, 0, "Row Id");
        excelUnit.assertCellEquals(0, 1, "Period");
        excelUnit.assertCellEquals(0, 2, "SumOfNo");
        Cell cell = excelUnit.getCell(0, 0);
        excelUnit.assertCellIsSizeInPoints(cell, (short) 11);

        // header should be bold
        excelUnit.assertCellIsBold(cell);

        excelUnit.assertRowNotEmpty(13);
        excelUnit.assertCellNotBold(excelUnit.getCell(13, 0));
        excelUnit.assertRowIsEmpty(15);
    }

    private CodingRule createRule(String code, String term, CodingSheet codingSheet) {
        CodingRule codingRule = new CodingRule(codingSheet, code, term, term);
        return codingRule;
    }


    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testgeneratedCodingSheet() throws Exception {
        Dataset dataset = setupAndLoadResource(TestConstants.TEST_DATA_INTEGRATION_DIR + TEST_DATASET_FILENAME, Dataset.class);
        ColumnMetadataController datasetController = generateNewInitializedController(ColumnMetadataController.class);
        Long datasetId = dataset.getId();
        DataTable table = dataset.getDataTables().iterator().next();
        datasetController.setId(datasetId);
        DataTableColumn period_ = table.getColumnByDisplayName("Period");
        assertFalse(period_.getColumnEncodingType().isSupportsCodingSheet());
        datasetController.prepare();
        datasetController.editColumnMetadata();
        Ontology ontology = setupAndLoadResource("fauna-element-ontology.txt", Ontology.class);
        //        period_.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
        period_.setTransientOntology(ontology);
        datasetController.setDataTableColumns(Arrays.asList(period_));
        datasetController.saveColumnMetadata();
        DataTableColumn periodColumn = null;
        DataTableColumn period = genericService.find(DataTableColumn.class, period_.getId());
        logger.info("{}", period.getDefaultCodingSheet());
        assertNotNull("coding sheet should exist", period.getDefaultCodingSheet());
        assertEquals(1, period.getDefaultCodingSheet().getInformationResourceFiles().size());
        InformationResourceFile file = period.getDefaultCodingSheet().getInformationResourceFiles().iterator().next();
        logger.debug("type:{}", file.getInformationResourceFileType());
        assertNotNull(file.getInformationResourceFileType());
    }

    @Test
    @Rollback
    public void testDatasetMappingPreservation() throws Exception {
        CodingSheet codingSheet = setupCodingSheet();
        Dataset dataset = setupAndLoadResource(TestConstants.TEST_DATA_INTEGRATION_DIR + TEST_DATASET_FILENAME, Dataset.class);
        ColumnMetadataController datasetController = generateNewInitializedController(ColumnMetadataController.class);
        Long datasetId = dataset.getId();
        DataTable table = dataset.getDataTables().iterator().next();
        datasetController.setId(datasetId);
        DataTableColumn period_ = cloneDataTableColumn(table.getColumnByDisplayName("Period"));
        assertFalse(period_.getColumnEncodingType().isSupportsCodingSheet());
        datasetController.prepare();
        datasetController.editColumnMetadata();
        period_.setDefaultCodingSheet(codingSheet);
        datasetController.setDataTableColumns(Arrays.asList(period_));
        datasetController.saveColumnMetadata();
        DataTableColumn periodColumn = null;
        DataTableColumn period = genericService.find(DataTableColumn.class, period_.getId());
        logger.info("{}", period.getDefaultCodingSheet());
        logger.info("{}", codingSheet.getId());
        assertNotNull(period.getDefaultCodingSheet());
        assertTrue(period.getColumnEncodingType().isSupportsCodingSheet());
        // FIXME: this assertion no longer holds, we set the column encoding type to CODED_VALUE automatically if we set a default coding sheet on the
        // column.
        assertNotNull("coding sheet should exist", period.getDefaultCodingSheet());

        datasetController = generateNewInitializedController(ColumnMetadataController.class);
        table = dataset.getDataTables().iterator().next();
        datasetController.setId(datasetId);
        period_ = cloneDataTableColumn(table.getColumnByDisplayName("Period"));
        datasetController.prepare();
        datasetController.editColumnMetadata();
        period_.setDefaultCodingSheet(codingSheet);
        period_.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
        datasetController.setDataTableColumns(Arrays.asList(period_));
        datasetController.saveColumnMetadata();
        periodColumn = null;
        period = genericService.find(DataTableColumn.class, period_.getId());
        assertEquals(period.getDefaultCodingSheet().getId(), codingSheet.getId());
        datasetService.createTranslatedFile(dataset);

        Dataset newDataset = setupAndLoadResource(TestConstants.TEST_DATA_INTEGRATION_DIR + TEST_DATASET_FILENAME, Dataset.class, datasetId);

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

    /**
     * note, this just mainly works for coding sheets... it's not intended to be perfect
     * @param dtc
     * @return
     */
    private DataTableColumn cloneDataTableColumn(DataTableColumn dtc) {
        DataTableColumn clone = new DataTableColumn();
        clone.setCategoryVariable(dtc.getCategoryVariable());
        clone.setColumnDataType(dtc.getColumnDataType());
        clone.setColumnEncodingType(dtc.getColumnEncodingType());
        clone.setDefaultCodingSheet(dtc.getDefaultCodingSheet());
        clone.setId(dtc.getId());
        clone.setName(dtc.getName());
        clone.setDisplayName(dtc.getDisplayName());
        return clone;
    }

    /**
     * @return
     * @throws TdarActionException
     */
    private CodingSheet setupCodingSheet() throws Exception {
        return setupCodingSheet(EXCEL_FILE_NAME, EXCEL_FILE_PATH, null, null);
    }

    private CodingSheet setupCodingSheet(String fileName, String filePath, Ontology ontology, File textFile) throws TdarActionException, IOException {
        CodingSheetController codingSheetController = generateNewInitializedController(CodingSheetController.class);
        codingSheetController.prepare();
        CodingSheet codingSheet = codingSheetController.getCodingSheet();
        codingSheet.setTitle("test coding sheet");
        codingSheet.setDescription("test description");

        codingSheet.setDefaultOntology(ontology);
        if (textFile != null) {
            codingSheetController.setFileTextInput(FileUtils.readFileToString(textFile));
            codingSheetController.setFileInputMethod(AbstractInformationResourceController.FILE_INPUT_METHOD);
        } else {
            List<File> uploadedFiles = new ArrayList<File>();
            List<String> uploadedFileNames = new ArrayList<String>();
            uploadedFiles.add(new File(filePath));
            uploadedFileNames.add(fileName);
            codingSheetController.setUploadedFilesFileName(uploadedFileNames);
            codingSheetController.setUploadedFiles(uploadedFiles);
        }
        codingSheetController.setServletRequest(getServletPostRequest());
        codingSheetController.save();
        Long codingId = codingSheet.getId();
        assertNotNull(codingId);
        assertFalse(codingSheet.getCodingRules().isEmpty());
        return codingSheet;
    }

    @Override
    protected String getTestFilePath() {
        return PATH;
    }

    @Test
    @Rollback
    public void testOntologyNodeDuplicationOnReplace() throws TdarActionException, Exception {
        // 1. Load things...
        Ontology ontology = setupAndLoadResource("fauna-element-ontology.txt", Ontology.class);
        Long ontology_id = ontology.getId();
        genericService.refresh(ontology);
        logger.info("nodes;{}", ontology.getOntologyNodes());
        assertNotEmpty(ontology.getOntologyNodes());
        genericService.detachFromSession(ontology);
        Dataset dataset = setupAndLoadResource(TestConstants.TEST_ROOT_DIR + "/data_integration_tests/periods-modified-sm-01182011.xlsx", Dataset.class);
        ColumnMetadataController controller = generateNewInitializedController(ColumnMetadataController.class);
        controller.setId(dataset.getId());
        dataset = null;
        controller.prepare();
        controller.editColumnMetadata();

        // 2. update mappings and set ontology on one column
        List<DataTableColumn> columns = new ArrayList<>();
        for (DataTableColumn dtc : controller.getDataTableColumns()) {
            logger.debug("dtc: {}", dtc);
            logger.debug("coding sheet? {}", dtc.getDefaultCodingSheet());
            DataTableColumn clone = (DataTableColumn) BeanUtils.cloneBean(dtc);
            columns.add(clone);
            if (clone.getName().equals("column__2")) {
                clone.setTransientOntology(ontology);
            }
        }
        controller.setDataTableColumns(columns);
        controller.saveColumnMetadata();
        controller.getDataTableColumns();
        DataTableColumn myColumn = null;
        for (DataTableColumn dtc : controller.getDataTableColumns()) {
            if (dtc.getName().equals("column__2")) {
                myColumn = dtc;
            }
        }
        assertNotNull(myColumn);
        // 3. update coding sheet mappings to point to ontology
        CodingSheetMappingController csc = generateNewInitializedController(CodingSheetMappingController.class);
        csc.setId(myColumn.getDefaultCodingSheet().getId());
        csc.prepare();
        csc.loadOntologyMappedColumns();
        List<CodingRule> rules = new ArrayList<>();
        Map<String, Long> iriMap = new HashMap<>();
        for (CodingRule rule_ : csc.getCodingRules()) {
            CodingRule rule = (CodingRule) BeanUtils.cloneBean(rule_);
            rules.add(rule);
            String iri = null;
            switch (rule.getCode()) {
                case "b":
                    iri = "I2";
                    break;
                case "c":
                    iri = "Upper_M3";
                    break;
                case "d":
                    iri = "Molar";
                    break;
                default:
                    break;
            }
            if (iri != null) {
                OntologyNode nodeByIri = ontology.getNodeByIri(iri);
                rule.setOntologyNode(nodeByIri);
                logger.info(iri);
                iriMap.put(iri, nodeByIri.getId());
            }
            logger.info("coding rule: {} ", rule);
        }
        csc.setCodingRules(rules);
        csc.saveValueOntologyNodeMapping();
        ontology = setupAndLoadResource("fauna-element-ontology-v2.txt", Ontology.class, ontology_id);
        evictCache();
        genericService.refresh(ontology);
        Set<OntologyNode> nodes = new HashSet<>(ontology.getOntologyNodes());
        logger.info("what's the difference: {}", CollectionUtils.disjunction(nodes, ontology.getOntologyNodes()));
        assertEquals(nodes.size(), ontology.getOntologyNodes().size());
        for (String iri : iriMap.keySet()) {
            assertEquals(iriMap.get(iri), ontology.getNodeByIri(iri).getId());
        }
        logger.trace("nodes: {}", StringUtils.join(ontology.getOntologyNodes(), ",\r\n\t"));
    }

}
