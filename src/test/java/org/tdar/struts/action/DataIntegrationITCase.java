package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.struts.action.resource.DatasetController;
import org.tdar.struts.data.IntegrationColumn;
import org.tdar.struts.data.IntegrationColumn.ColumnType;
import org.tdar.struts.data.IntegrationDataResult;
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

    /**
     * 
     * @throws Exception
     */
    @Test
    @Rollback
    public void testFilteredNodesSurviveHierarchy() throws Exception {
        Ontology taxonOntology = setupAndLoadResource("fauna-taxon---tag-uk-updated---default-ontology-draft.owl", Ontology.class);
        logger.trace("{}", taxonOntology.getOntologyNodes());
        Dataset spitalDb = setupAndLoadResource(SPITAL_DB_NAME, Dataset.class);
        DataTable spitalTable = spitalDb.getDataTableByGenericName("spital_abone_database_mdb_main_table");
        DataTableColumn spitalSpeciesColumn = spitalTable.getColumnByName(SPECIES_COMMON_NAME_COL);
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setDataset(spitalDb);
        controller.setDataTableId(spitalTable.getId());
        controller.setDataTableColumns(spitalTable.getDataTableColumns());
        spitalSpeciesColumn.setDefaultOntology(taxonOntology);
        controller.saveColumnMetadata();
        assertNotNull(spitalSpeciesColumn.getDefaultCodingSheet());
        assertNotNull(spitalSpeciesColumn.getDefaultOntology());

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
        integrationColumn.setOntologyNodesForSelect(new HashSet<OntologyNode>(Arrays.asList(pleuronectiformesOntologyNode, plaiceFlounderOntologyNode, gadidae,
                paracanthopt)));
        // tests that the mapped ontology node is not aggregated up to pleuronectiformes
        for (String term : plaiceFlounderTerms) {
            assertEquals(plaiceFlounderOntologyNode.getDisplayName(),
                    dataIntegrationService.getMappedOntologyNode(term, spitalSpeciesColumn, integrationColumn).getDisplayName());
        }

        // testing aggregation up from gaddidae to paracanthopt...
        for (String term : mappedGadidaeTerms) {
            logger.trace("{} -> {}", term, dataIntegrationService.getMappedOntologyNode(term, spitalSpeciesColumn, integrationColumn).getDisplayName());
            assertEquals(paracanthopt.getDisplayName(), dataIntegrationService.getMappedOntologyNode(term, spitalSpeciesColumn, integrationColumn)
                    .getDisplayName());
        }

    }

    @Test
    @Rollback
    public void testIntegrationProcess() throws Exception {
        // load datasets
        Dataset alexandriaDb = setupAndLoadResource(ALEXANDRIA_DB_NAME, Dataset.class);
        Dataset spitalDb = setupAndLoadResource(SPITAL_DB_NAME, Dataset.class);
        assertNotNull(spitalDb);
        assertNotNull(alexandriaDb);

        // load ontologies
        Ontology bElementOntology = setupAndLoadResource("fauna-element-updated---default-ontology-draft.owl", Ontology.class);
        Ontology taxonOntology = setupAndLoadResource("fauna-taxon---tag-uk-updated---default-ontology-draft.owl", Ontology.class);
        DataTable alexandriaTable = alexandriaDb.getDataTables().iterator().next();
        DataTable spitalMainTable = spitalDb.getDataTableByGenericName("spital_abone_database_mdb_main_table");

        // bookmark datasets
        bookmarkResource(alexandriaDb);
        bookmarkResource(spitalDb);

        // map ontologies to columns (setup proxies and then map)
        logger.info("mapping ontologies");
        DataTableColumn elementColumn = new DataTableColumn();
        elementColumn.setDefaultOntology(bElementOntology);
        DataTableColumn taxonColumn = new DataTableColumn();
        taxonColumn.setDefaultOntology(taxonOntology);
        elementColumn.setName(BELEMENT_COL);
        taxonColumn.setName(TAXON_COL);
        elementColumn.setId(alexandriaTable.getColumnByName(BELEMENT_COL).getId());
        taxonColumn.setId(alexandriaTable.getColumnByName(TAXON_COL).getId());
        elementColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        taxonColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        mapColumnsToDataset(alexandriaDb, alexandriaTable, elementColumn, taxonColumn);

        DataTableColumn elementColumn2 = new DataTableColumn();
        elementColumn2.setDefaultOntology(bElementOntology);
        DataTableColumn taxonColumn2 = new DataTableColumn();
        taxonColumn2.setDefaultOntology(taxonOntology);
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
        WorkspaceController controller = generateNewInitializedController(WorkspaceController.class);
        controller.execute();
        assertTrue(controller.getBookmarkedDatasets().size() == 2);

        // select tables
        List<Long> tableIds = new ArrayList<Long>();
        tableIds.add(alexandriaTable.getId());
        tableIds.add(spitalMainTable.getId());
        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTableIds(tableIds);

        // select columns
        controller.selectColumns();
        controller.execute();
        assertTrue("expected 2 selected tables", controller.getSelectedDataTables().size() == 2);
        List<IntegrationColumn> integrationColumns = new ArrayList<IntegrationColumn>();
        integrationColumns.add(new IntegrationColumn(ColumnType.INTEGRATION, spitalMainTable.getColumnByName(SPECIES_COMMON_NAME_COL), alexandriaTable
                .getColumnByName(TAXON_COL)));
        integrationColumns.add(new IntegrationColumn(ColumnType.INTEGRATION, spitalMainTable.getColumnByName(BONE_COMMON_NAME_COL), alexandriaTable
                .getColumnByName(BELEMENT_COL)));

        List<String> displayRulesColumns = new ArrayList<String>();
        displayRulesColumns.addAll(Arrays.asList(new String[] { "no", "fus_prox" }));

        for (String col : displayRulesColumns) {
            integrationColumns.add(new IntegrationColumn(ColumnType.DISPLAY, spitalMainTable.getColumnByName(col)));
        }
        integrationColumns.get(integrationColumns.size() - 1).getColumns().add(alexandriaTable.getColumnByName("feature"));

        // setup filters
        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTableIds(tableIds);
        controller.setIntegrationColumns(integrationColumns);
        controller.filterDataValues();
        for (IntegrationColumn column : controller.getIntegrationColumns()) {
            if (column.isDisplayColumn())
                continue;
            for (OntologyNode node : column.getFlattenedOntologyNodeList()) {
                logger.info("node: {} ", node);
                if (node.getIri().equals("Atlas") || node.getIri().equals("Axis")) {
                    logger.info("node: {} - {}", node, node.getColumnHasValueArray());
                    boolean oneTrue = false;
                    for (boolean val : node.getColumnHasValueArray()) {
                        if (val)
                            oneTrue = true;
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
            if (ic.isIntegrationColumn())
                integ++;
        }

        assertEquals(2, integ);
        assertEquals(2, integrationColumns.size() - integ);
        HashMap<Ontology, String[]> nodeSelectionMap = new HashMap<Ontology, String[]>();
        nodeSelectionMap.put(taxonOntology, new String[] { "Felis catus (Cat)", "Canis familiaris (Dog)", "Ovis aries (Sheep)" });
        nodeSelectionMap.put(bElementOntology, new String[] { "Atlas", "Axis", "Carpal", "Tooth", "Ulna" });

        List<IntegrationDataResult> results = performActualIntegration(tableIds, integrationColumns, nodeSelectionMap);
        // assuming we're dealing with alexandria here
        boolean seenElementNull = false;
        boolean seenSpeciesNull = false;
        for (IntegrationDataResult result : results) {
            logger.info("{} - {}", result.getDataTable().getName(), result.getRowData().size());
            assertTrue(CollectionUtils.isNotEmpty(result.getRowData()));
            // List<DataTableColumn> columnsToDisplay = result.getColumnsToDisplay();
            // for (DataTableColumn col : columnsToDisplay) { // capturing to test later
            // resultingDataTableColumns.add(col.getName());
            // resultingDataTableColumns.add(col.getDisplayName());
            // }
            logger.debug("\n{}\n\trowdata: {}", result, result.getRowData());
            assertFalse("Should have integration results from each dataset", CollectionUtils.isEmpty(result.getRowData()));
            for (String[] rowData : result.getRowData()) {
                if (rowData[1].equals(MessageHelper.getMessage("database.null_empty_integration_value")))
                    seenElementNull = true;
                if (rowData[3].equals(MessageHelper.getMessage("database.null_empty_integration_value")))
                    seenSpeciesNull = true;
            }
        }

        // assertTrue(resultingDataTableColumns.containsAll(displayRulesColumns));
        assertTrue(seenElementNull);
        assertTrue(seenSpeciesNull);
        logger.info("hi, we're done here");
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
        Dataset spitalDb = setupAndLoadResource(SPITAL_DB_NAME, Dataset.class);
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
        bookmarkResource(alexandriaDb);
        bookmarkResource(spitalDb);

        // map ontologies to columns (setup proxies and then map)
        logger.info("mapping ontologies");
        // testing actual integration mode
        WorkspaceController controller = generateNewInitializedController(WorkspaceController.class);
        DataTableColumn elementColumn = new DataTableColumn();
        elementColumn.setId(alexandriaTable.getColumnByName(BELEMENT_COL).getId());
        elementColumn.setName(BELEMENT_COL);
        elementColumn.setDefaultOntology(bElementOntology);
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
        List<Long> tableIds = new ArrayList<Long>();
        tableIds.add(alexandriaTable.getId());
        tableIds.add(spitalTable.getId());
        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTableIds(tableIds);

        // select columns
        controller.selectColumns();
        controller.execute();
        assertEquals("expected 2 selected tables", 2, controller.getSelectedDataTables().size());
        List<IntegrationColumn> integrationColumns = new ArrayList<IntegrationColumn>();
        integrationColumns.add(new IntegrationColumn(ColumnType.INTEGRATION, spitalTable.getColumnByName(BONE_COMMON_NAME_COL), alexandriaTable
                .getColumnByName(BELEMENT_COL)));

        // setup filters
        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTableIds(tableIds);
        controller.setIntegrationColumns(integrationColumns);
        controller.filterDataValues();
        controller.execute();
        integrationColumns = controller.getIntegrationColumns();
        assertFalse(integrationColumns.isEmpty());
        assertEquals(1, integrationColumns.size());
        HashMap<Ontology, String[]> nodeSelectionMap = new HashMap<Ontology, String[]>();
        nodeSelectionMap.put(bElementOntology, new String[] { "Tarsal", "Astragalus", "Ulna" });

        List<IntegrationDataResult> results = performActualIntegration(tableIds, integrationColumns, nodeSelectionMap);
        // assuming we're dealing with alexandria here
        boolean seenElementNull = false;
        logger.info("{}", results);
        int ulna = 0;
        int tarsal = 0;
        int astragalus = 0;
        int empty = 0;
        for (IntegrationDataResult result : results) {

            for (String[] rowData : result.getRowData()) {
                if (rowData[1].equals(MessageHelper.getMessage("database.null_empty_integration_value"))) {
                    seenElementNull = true;
                }
                if (rowData[2].equalsIgnoreCase("tarsal"))
                    tarsal++;
                if (rowData[2].equalsIgnoreCase("ulna"))
                    ulna++;
                if (rowData[2].equalsIgnoreCase("astragalus"))
                    astragalus++;
                if (rowData[2].equalsIgnoreCase(MessageHelper.getMessage("database.null_empty_mapped_value")))
                    empty++;
            }
        }
        logger.info("tarsal: {}", tarsal);
        logger.info("astragalus: {}", astragalus);
        logger.info("ulna: {}", ulna);
        logger.info("empty: {}", empty);
        assertEquals(41, astragalus);
        assertEquals(111, tarsal);
        assertEquals(332, ulna);
        assertEquals(276, empty);
        assertTrue(seenElementNull);
    }
}
