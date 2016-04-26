package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.tdar.core.dao.resource.OntologyNodeDao;
import org.tdar.core.service.integration.ColumnType;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.integration.ModernDataIntegrationWorkbook;
import org.tdar.core.service.integration.ModernIntegrationDataResult;
import org.tdar.struts.action.dataset.ColumnMetadataController;
import org.tdar.struts.action.workspace.LegacyWorkspaceController;
import org.tdar.utils.MessageHelper;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public class DataIntegrationITCase extends AbstractDataIntegrationTestCase {

    private static final String ALEXANDRIA_DB_NAME = "qrybonecatalogueeditedkk.xls";
    private static final String BELEMENT_COL = "belement";
    private static final String BONE_COMMON_NAME_COL = "bone_common_name";
    private static final String TAXON_COL = "taxon";
    private static final String SPECIES_COMMON_NAME_COL = "species_common_name";

    @Autowired
    private OntologyNodeDao ontologyNodeDao;

    @Test
    @Rollback
    public void testFilteredNodesSurviveHierarchy() throws Exception {
        Ontology taxonOntology = setupAndLoadResource("fauna-taxon---tag-uk-updated---default-ontology-draft.owl", Ontology.class);
        logger.trace("{}", taxonOntology.getOntologyNodes());
        Dataset spitalDb = setupAndLoadResource(TestConstants.SPITAL_DB_NAME, Dataset.class);
        DataTable spitalTable = spitalDb.getDataTableByGenericName("spital_abone_database_mdb_main_table");
        DataTableColumn spitalSpeciesColumn = spitalTable.getColumnByName(SPECIES_COMMON_NAME_COL);
        ColumnMetadataController controller = generateNewInitializedController(ColumnMetadataController.class);
        controller.setId(spitalDb.getId());
        controller.setDataTableId(spitalTable.getId());
        controller.prepare();
        controller.setDataTableColumns(spitalTable.getDataTableColumns());
        spitalSpeciesColumn.setTransientOntology(taxonOntology);
        controller.saveColumnMetadata();
        assertNotNull(spitalSpeciesColumn.getDefaultCodingSheet());
        assertNotNull(spitalSpeciesColumn.getDefaultCodingSheet().getDefaultOntology());

        /*
         * -- this is the part of the TAG faunal ontology that we care about
         * OSTEICHTHYES (bony fishes)
         * |-label "Neopterygii (neopterygians)"
         * | |-label "Acanthopterygii"
         * | | |-label "Pleuronectiformes (flatfishes flounders soles)"
         * | | | |-label "Pleuronectoidei"
         * | | | | |-label "Plaice Flounder"
         * | |-label "Paracanthopterygii"
         * | | |-label "Gadidae (true cods)"
         * | | | |-label "Gadidae Large (large true cods)"
         */
        CodingSheet codingSheet = spitalSpeciesColumn.getDefaultCodingSheet();
        OntologyNode plaiceFlounderOntologyNode = taxonOntology.getNodeByName("Plaice Flounder");
        // parent of plaice flounder
        OntologyNode pleuronectiformesOntologyNode = taxonOntology.getNodeByName("Pleuronectiformes (flatfishes flounders soles)");
        String[] plaiceFlounderTerms = { "PLAICE/FLOUNDER", "PLAICE" };
        for (String term : plaiceFlounderTerms) {
            for (CodingRule rule : codingSheet.getCodingRuleByTerm(term)) {
                rule.setOntologyNode(plaiceFlounderOntologyNode);
            }
        }
        OntologyNode gadidae = taxonOntology.getNodeByName("Gadidae Large (large true cods)");
        // parent of gadidae
        OntologyNode paracanthopt = taxonOntology.getNodeByName("Paracanthopterygii");
        List<String> mappedGadidaeTerms = new ArrayList<String>();
        for (CodingRule rule : codingSheet.getCodingRules()) {
            if (rule.getTerm().toLowerCase().contains("gadid")) {
                rule.setOntologyNode(gadidae);
                mappedGadidaeTerms.add(rule.getTerm());
            }
        }
        assertNotNull(plaiceFlounderOntologyNode);
        assertNotNull(gadidae);
        IntegrationColumn integrationColumn = new IntegrationColumn();
        integrationColumn.setFilteredOntologyNodes(Arrays.asList(pleuronectiformesOntologyNode, plaiceFlounderOntologyNode, paracanthopt));
        integrationColumn.buildNodeChildHierarchy(ontologyNodeDao);

        // integrationColumn.setOntologyNodesForSelect(new HashSet<OntologyNode>(Arrays.asList(pleuronectiformesOntologyNode, plaiceFlounderOntologyNode,
        // gadidae,
        // paracanthopt)));
        // tests that the mapped ontology node is not aggregated up to pleuronectiformes
        for (String term : plaiceFlounderTerms) {
            assertEquals(plaiceFlounderOntologyNode.getDisplayName(),
                    integrationColumn.getMappedOntologyNode(term, spitalSpeciesColumn).getDisplayName());
        }

        // testing aggregation up from gaddidae to paracanthopt...
        for (String term : mappedGadidaeTerms) {
            logger.trace("{} -> {}", term, integrationColumn.getMappedOntologyNode(term, spitalSpeciesColumn).getDisplayName());
            assertEquals(paracanthopt.getDisplayName(), integrationColumn.getMappedOntologyNode(term, spitalSpeciesColumn).getDisplayName());
        }

    }

    @Test
    @Rollback
    public void testIntegrationProcess() throws Exception {
        // load datasets
        Dataset alexandriaDb = setupAndLoadResource(ALEXANDRIA_DB_NAME, Dataset.class);
        Dataset spitalDb = setupAndLoadResource(TestConstants.SPITAL_DB_NAME, Dataset.class);
        assertNotNull(spitalDb);
        assertNotNull(alexandriaDb);

        // load ontologies
        Ontology bElementOntology = setupAndLoadResource("fauna-element-updated---default-ontology-draft.owl", Ontology.class);
        Ontology taxonOntology = setupAndLoadResource("fauna-taxon---tag-uk-updated---default-ontology-draft.owl", Ontology.class);
        DataTable alexandriaTable = alexandriaDb.getDataTables().iterator().next();
        DataTable spitalMainTable = spitalDb.getDataTableByGenericName("spital_abone_database_mdb_main_table");

        // bookmark datasets
        bookmarkResource(alexandriaDb, getUser());
        bookmarkResource(spitalDb, getUser());

        // map ontologies to columns (setup proxies and then map)
        logger.info("mapping ontologies");
        DataTableColumn elementColumn = new DataTableColumn();
        elementColumn.setTransientOntology(bElementOntology);
        DataTableColumn taxonColumn = new DataTableColumn();
        taxonColumn.setTransientOntology(taxonOntology);
        elementColumn.setName(BELEMENT_COL);
        taxonColumn.setName(TAXON_COL);
        elementColumn.setId(alexandriaTable.getColumnByName(BELEMENT_COL).getId());
        taxonColumn.setId(alexandriaTable.getColumnByName(TAXON_COL).getId());
        elementColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        taxonColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        mapColumnsToDataset(alexandriaDb, alexandriaTable, elementColumn, taxonColumn);

        DataTableColumn elementColumn2 = new DataTableColumn();
        elementColumn2.setTransientOntology(bElementOntology);
        DataTableColumn taxonColumn2 = new DataTableColumn();
        taxonColumn2.setTransientOntology(taxonOntology);
        elementColumn2.setName(BONE_COMMON_NAME_COL);
        taxonColumn2.setName(SPECIES_COMMON_NAME_COL);
        elementColumn2.setId(spitalMainTable.getColumnByName(BONE_COMMON_NAME_COL).getId());
        taxonColumn2.setId(spitalMainTable.getColumnByName(SPECIES_COMMON_NAME_COL).getId());

        mapColumnsToDataset(spitalDb, spitalMainTable, elementColumn2, taxonColumn2);

        mapDataOntologyValues(alexandriaTable, BELEMENT_COL, getElementValueMap(), bElementOntology);
        mapDataOntologyValues(spitalMainTable, BONE_COMMON_NAME_COL, getElementValueMap(), bElementOntology);

        mapDataOntologyValues(alexandriaTable, TAXON_COL, getTaxonValueMap(), taxonOntology);
        mapDataOntologyValues(spitalMainTable, SPECIES_COMMON_NAME_COL, getTaxonValueMap(), taxonOntology);

        // testing actual integration mode
        LegacyWorkspaceController controller = generateNewInitializedController(LegacyWorkspaceController.class);
        controller.execute();
        assertTrue(controller.getBookmarkedDatasets().size() == 2);

        // select tables
        List<Long> tableIds = new ArrayList<>();
        tableIds.add(alexandriaTable.getId());
        tableIds.add(spitalMainTable.getId());
        controller = generateNewInitializedController(LegacyWorkspaceController.class);
        controller.setTableIds(tableIds);

        // select columns
        controller.selectColumns();
        controller.execute();
        assertTrue("expected 2 selected tables", controller.getSelectedDataTables().size() == 2);
        List<IntegrationColumn> integrationColumns = new ArrayList<>();
        integrationColumns.add(new IntegrationColumn(ColumnType.INTEGRATION, spitalMainTable.getColumnByName(SPECIES_COMMON_NAME_COL), alexandriaTable
                .getColumnByName(TAXON_COL)));
        integrationColumns.add(new IntegrationColumn(ColumnType.INTEGRATION, spitalMainTable.getColumnByName(BONE_COMMON_NAME_COL), alexandriaTable
                .getColumnByName(BELEMENT_COL)));

        List<String> displayRulesColumns = new ArrayList<>();
        displayRulesColumns.addAll(Arrays.asList("col_no", "fus_prox"));

        for (String col : displayRulesColumns) {
            integrationColumns.add(new IntegrationColumn(ColumnType.DISPLAY, spitalMainTable.getColumnByName(col)));
        }
        integrationColumns.get(integrationColumns.size() - 1).getColumns().add(alexandriaTable.getColumnByName("feature"));

        // setup filters
        controller = generateNewInitializedController(LegacyWorkspaceController.class);
        controller.setTableIds(tableIds);
        controller.setIntegrationColumns(integrationColumns);
        controller.filterDataValues();
        for (IntegrationColumn column : controller.getIntegrationColumns()) {
            if (!column.isIntegrationColumn()) {
                continue;
            }

            for (OntologyNode node : column.getFlattenedOntologyNodeList()) {
                logger.trace("node: {} ", node);
                if (node.getIri().equals("Atlas") || node.getIri().equals("Axis")) {
                    logger.trace("node: {} - {}", node, node.getColumnHasValueMap());
                    boolean oneTrue = false;
                    for (boolean val : node.getColumnHasValueMap().values()) {
                        if (val) {
                            oneTrue = true;
                            break;
                        }
                    }
                    assertTrue(String.format("Mapped value for :%s should be true", node.getIri()), oneTrue);
                }
            }
        }
        controller.execute();
        integrationColumns = controller.getIntegrationColumns();
        assertFalse(integrationColumns.isEmpty());
        assertEquals(4, integrationColumns.size());
        int integ = 0;
        for (IntegrationColumn ic : integrationColumns) {
            if (ic.isIntegrationColumn()) {
                integ++;
            }
        }

        assertEquals(2, integ);
        assertEquals(2, integrationColumns.size() - integ);
        HashMap<Ontology, String[]> nodeSelectionMap = new HashMap<>();
        nodeSelectionMap.put(taxonOntology, new String[] { "Felis catus (Cat)", "Canis familiaris (Dog)", "Ovis aries (Sheep)" });
        nodeSelectionMap.put(bElementOntology, new String[] { "Atlas", "Axis", "Carpal", "Tooth", "Ulna" });

        Object results_ = performActualIntegration(tableIds, integrationColumns, nodeSelectionMap);
        // assuming we're dealing with alexandria here
        boolean seenElementNull = false;
        boolean seenSpeciesNull = false;
        boolean seenUnmapped = false;
        
        ModernIntegrationDataResult result = (ModernIntegrationDataResult) results_;
        logger.trace("result: {}", result);
        Workbook workbook = result.getWorkbook().getWorkbook();
        Sheet sheet = workbook.getSheet(MessageHelper.getMessage("dataIntegrationWorkbook.data_worksheet"));
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (logger.isTraceEnabled()) {
                logger.trace(" {} | {} | {} | {} | {}", getVal(row, 0), getVal(row, 1), getVal(row, 2), getVal(row, 3), getVal(row, 4));
            }
            
            if (row.getCell(1) != null) {
                // avoid note column
                String stringCellValue = row.getCell(1).getStringCellValue();
                
                if (StringUtils.equals(stringCellValue,"rockfish")) {
                    seenUnmapped = true;
                }
                
                if (stringCellValue.equals(MessageHelper.getMessage("database.null_empty_integration_value"))) {
                    seenElementNull = true;
                }
                if (row.getCell(5).getStringCellValue().equals(MessageHelper.getMessage("database.null_empty_integration_value"))) {
                    seenSpeciesNull = true;
                }
            }
        }
        // assertTrue(resultingDataTableColumns.containsAll(displayRulesColumns));
        assertTrue("should have seen null element values",seenElementNull);
        assertTrue("should have seen 'unmapped values'",seenUnmapped);
        assertTrue("should have seen null species values",seenSpeciesNull);

        // confirm that the pivot sheet is created properly with names and at least one known value
        Sheet summarySheet = workbook.getSheet(MessageHelper.getMessage("dataIntegrationWorkbook.summary_worksheet"));
        List<String> names = new ArrayList<>();
        for (IntegrationColumn col : integrationColumns) {
            if (col.isIntegrationColumn()) {
                names.add(col.getName());
            }
        }
        names.add(ModernDataIntegrationWorkbook.formatTableName(alexandriaTable));
        names.add(ModernDataIntegrationWorkbook.formatTableName(spitalMainTable));
        Row row = summarySheet.getRow(3);
        List<String> seen = new ArrayList<String>();
        for (int i = 0; i < names.size(); i++) {
            seen.add(row.getCell(i).getStringCellValue());
        }

        names.removeAll(seen);
        logger.debug("colNames:{}", names);
        logger.debug("seen    :{}", seen);
        assertTrue(names.isEmpty());

        String[] row3 = new String[] { "Felis catus (Cat)", "Ulna", "23", "15" };
        row = summarySheet.getRow(4);
        for (int i = 0; i < names.size(); i++) {
            Cell cell = row.getCell(i);
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                assertEquals((int) Double.parseDouble(row3[i]), (int) cell.getNumericCellValue());
            } else {
                assertEquals(row3[i], cell.getStringCellValue());

            }
        }

        // assertions on descriptions too?

        logger.info("hi, we're done here");
    }

    private RichTextString getVal(Row row, int val) {
        if (row.getCell(val) != null) {
            return row.getCell(val).getRichStringCellValue();
        }
        return null;
    }

    /**
     * Tests that selecting a parent node in an ontology will include and aggregate its child nodes properly when generating an integration result
     * ie, if values are mapped to child nodes, they will get aggregated to the parent node when the parent node is selected.
     * 
     * Child nodes should stay separate if they are explicitly checked.
     * 
     * @throws Exception
     */
    @Test
    @Rollback
    public void testHierarchicalIntegrationProcess() throws Exception {
        // load datasets
        Dataset alexandriaDb = setupAndLoadResource(ALEXANDRIA_DB_NAME, Dataset.class);
        Dataset spitalDb = setupAndLoadResource(TestConstants.SPITAL_DB_NAME, Dataset.class);
        assertNotNull(spitalDb);
        assertNotNull(alexandriaDb);

        // load ontologies
        Ontology bElementOntology = setupAndLoadResource("fauna-element-updated---default-ontology-draft.owl", Ontology.class);
        DataTable alexandriaTable = alexandriaDb.getDataTables().iterator().next();
        DataTable spitalTable = spitalDb.getDataTableByGenericName("spital_abone_database_mdb_main_table");
        assertNotNull(spitalTable);
        assertNotNull(alexandriaTable);
        logger.debug(spitalTable.getName());
        // bookmark datasets
        bookmarkResource(alexandriaDb, getUser());
        bookmarkResource(spitalDb, getUser());

        // map ontologies to columns (setup proxies and then map)
        logger.info("mapping ontologies");
        // testing actual integration mode
        LegacyWorkspaceController controller = generateNewInitializedController(LegacyWorkspaceController.class);
        DataTableColumn elementColumn = new DataTableColumn();
        elementColumn.setId(alexandriaTable.getColumnByName(BELEMENT_COL).getId());
        elementColumn.setName(BELEMENT_COL);
        elementColumn.setTransientOntology(bElementOntology);
        // persists this pojo using the dataset controller
        mapColumnsToDataset(alexandriaDb, alexandriaTable, elementColumn);
        elementColumn.setDefaultCodingSheet(null);
        elementColumn.setId(spitalTable.getColumnByName(BONE_COMMON_NAME_COL).getId());
        elementColumn.setName(BONE_COMMON_NAME_COL);
        mapColumnsToDataset(spitalDb, spitalTable, elementColumn);

        mapDataOntologyValues(alexandriaTable, BELEMENT_COL, getHierarchyElementMap(), bElementOntology);
        mapDataOntologyValues(spitalTable, BONE_COMMON_NAME_COL, getHierarchyElementMap(), bElementOntology);

        controller.execute();
        assertEquals(2, controller.getBookmarkedDatasets().size());

        // select tables
        List<Long> tableIds = new ArrayList<>();
        tableIds.add(alexandriaTable.getId());
        tableIds.add(spitalTable.getId());
        controller = generateNewInitializedController(LegacyWorkspaceController.class);
        controller.setTableIds(tableIds);

        // select columns
        controller.selectColumns();
        controller.execute();
        assertEquals("expected 2 selected tables", 2, controller.getSelectedDataTables().size());
        List<IntegrationColumn> integrationColumns = new ArrayList<>();
        integrationColumns.add(new IntegrationColumn(ColumnType.INTEGRATION, spitalTable.getColumnByName(BONE_COMMON_NAME_COL), alexandriaTable
                .getColumnByName(BELEMENT_COL)));

        // setup filters
        controller = generateNewInitializedController(LegacyWorkspaceController.class);
        controller.setTableIds(tableIds);
        controller.setIntegrationColumns(integrationColumns);
        controller.filterDataValues();
        controller.execute();
        integrationColumns = controller.getIntegrationColumns();
        assertFalse(integrationColumns.isEmpty());
        assertEquals(1, integrationColumns.size());
        HashMap<Ontology, String[]> nodeSelectionMap = new HashMap<>();
        nodeSelectionMap.put(bElementOntology, new String[] { "Tarsal", "Astragalus", "Ulna" });

        Object results_ = performActualIntegration(tableIds, integrationColumns, nodeSelectionMap);
        logger.info("{}", results_);
        // assuming we're dealing with alexandria here
        boolean seenElementNull = false;
        int ulna = 0;
        int tarsal = 0;
        int astragalus = 0;
        int empty = 0;

        ModernIntegrationDataResult result = (ModernIntegrationDataResult) results_;

        int unmapped = 0;
        int nulls = 0;
        Sheet sheet = result.getWorkbook().getWorkbook().getSheet(MessageHelper.getMessage("dataIntegrationWorkbook.data_worksheet"));
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getCell(2) != null) {
                String value2 = row.getCell(2).getStringCellValue();
                String value1 = row.getCell(1).getStringCellValue();
                if (value1.equals(MessageHelper.getMessage("database.null_empty_integration_value"))) {
                    seenElementNull = true;
                }

                if (value2.equalsIgnoreCase("tarsal")) {
                    tarsal++;
                    continue;
                }
                if (value2.equalsIgnoreCase("ulna")) {
                    ulna++;
                    continue;
                }
                if (value2.equalsIgnoreCase("astragalus")) {
                    astragalus++;
                    continue;
                }
                if (StringUtils.equals(CodingRule.UNMAPPED.getTerm(), value2)) {
                    unmapped++;
                    continue;
                }
                if (StringUtils.equals(CodingRule.NULL.getTerm(), value2)) {
                    nulls++;
                    continue;
                }
                if (value2.equalsIgnoreCase(MessageHelper.getMessage("database.null_empty_mapped_value"))) {
                    empty++;
                }
            }
        }
        logger.info("tarsal: {}", tarsal);
        logger.info("astragalus: {}", astragalus);
        logger.info("ulna: {}", ulna);
        logger.info("empty: {}", empty);
        logger.info("unmapped: {}", unmapped);
        logger.info("nulls: {}", nulls);
        assertEquals(41, astragalus);
        assertEquals(111, tarsal);
        assertEquals(332, ulna);
        assertTrue(seenElementNull);
        if (getTdarConfiguration().includeSpecialCodingRules()) {
            assertEquals("expect to see NULL value",276,nulls);
            assertEquals("expect to see Unmapped", 9187, unmapped);
        } else {
            assertEquals(276, empty);
        }
    }

    @Override
    protected String getTestFilePath() {
        return TestConstants.TEST_DATA_INTEGRATION_DIR;
    }
}
