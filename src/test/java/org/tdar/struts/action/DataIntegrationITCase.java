/**
x * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
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
import org.tdar.core.bean.resource.DataValueOntologyNodeMapping;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnEncodingType;
import org.tdar.struts.data.IntegrationColumn;
import org.tdar.struts.data.IntegrationColumn.ColumnType;
import org.tdar.struts.data.IntegrationDataResult;

/**
 * @author Adam Brin
 * 
 */
public class DataIntegrationITCase extends AbstractDataIntegrationTestCase {

    /**
     * 
     */
    private static final String ALEXANDRIA_DB_NAME = "qrybonecatalogueeditedkk.xls";
    private static final String BELEMENT_COL = "belement";
    private static final String BONE_COMMON_NAME_COL = "bone_common_name";
    private static final String TAXON_COL = "taxon";
    private static final String SPECIES_COMMON_NAME_COL = "species_common_name";

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.bean.AbstractDataIntegrationTestCase#getDatabaseList()
     */
    @Override
    public String[] getDatabaseList() {
        String[] databases = {};
        return databases;
    }

    @Test
    @Rollback
    public void testFilteredNodesSurviveHierarchy() {
        Ontology taxonOntology = setupAndLoadResource("fauna-taxon---tag-uk-updated---default-ontology-draft.owl", Ontology.class);
        logger.info("{}", taxonOntology.getOntologyNodes());
        DataTableColumn column = new DataTableColumn();
        column.setName("test");
        column.setDefaultOntology(taxonOntology);

        OntologyNode pleuronectiformes = taxonOntology.getNodeByName("Pleuronectiformes (flatfishes flounders soles)");
        pleuronectiformes.setDataValueOntologyNodeMappings(new HashSet<DataValueOntologyNodeMapping>(Arrays.asList(new DataValueOntologyNodeMapping(column,
                pleuronectiformes, "flounder"))));
        OntologyNode plaiceFlounder = taxonOntology.getNodeByName("Plaice Flounder");
        plaiceFlounder.setDataValueOntologyNodeMappings(new HashSet<DataValueOntologyNodeMapping>(Arrays.asList(new DataValueOntologyNodeMapping(column,
                plaiceFlounder, "placie flounder"))));
        OntologyNode gadidae = taxonOntology.getNodeByName("Gadidae Large (large true cods)");
        gadidae.setDataValueOntologyNodeMappings(new HashSet<DataValueOntologyNodeMapping>(Arrays.asList(new DataValueOntologyNodeMapping(column, gadidae,
                "gadidae"))));
        assertNotNull(pleuronectiformes);
        assertNotNull(plaiceFlounder);
        assertNotNull(gadidae);
        IntegrationColumn integrationColumn = new IntegrationColumn();
        integrationColumn.setFilteredOntologyNodes(Arrays.asList(pleuronectiformes, plaiceFlounder, gadidae));
        integrationColumn.setOntologyNodesForSelect(new HashSet<OntologyNode>(Arrays.asList(pleuronectiformes, plaiceFlounder, gadidae)));

        assertEquals(pleuronectiformes.getDisplayName(), integrationColumn.getMappedOntologyNode("flounder", column).getDisplayName());
        assertEquals(gadidae.getDisplayName(), integrationColumn.getMappedOntologyNode("gadidae", column).getDisplayName());
        assertEquals(plaiceFlounder.getDisplayName(), integrationColumn.getMappedOntologyNode("placie flounder", column).getDisplayName());
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
        DataTable spitalMain = spitalDb.getDataTableByGenericName("spital_abone_database_mdb_main_table");

        // bookmark datasets
        bookmarkResource(alexandriaDb);
        bookmarkResource(spitalDb);

        // map ontologies to columns (setup proxies and then map)
        logger.info("mapping ontologies");
        DataTableColumn elementColumn = new DataTableColumn();
        elementColumn.setDefaultOntology(bElementOntology);
        DataTableColumn taxonColumn = new DataTableColumn();
        elementColumn.setName(BELEMENT_COL);
        elementColumn.setId(alexandriaTable.getColumnByName(BELEMENT_COL).getId());
        taxonColumn.setName(TAXON_COL);
        taxonColumn.setDefaultOntology(taxonOntology);
        taxonColumn.setId(alexandriaTable.getColumnByName(TAXON_COL).getId());
        elementColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        taxonColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        mapColumnsToDataset(alexandriaDb, alexandriaTable, elementColumn, taxonColumn);
        elementColumn.setName(BONE_COMMON_NAME_COL);
        elementColumn.setId(spitalMain.getColumnByName(BONE_COMMON_NAME_COL).getId());
        taxonColumn.setName(SPECIES_COMMON_NAME_COL);
        taxonColumn.setId(spitalMain.getColumnByName(SPECIES_COMMON_NAME_COL).getId());

        mapColumnsToDataset(spitalDb, spitalMain, elementColumn, taxonColumn);

        mapDataOntologyValues(alexandriaTable, BELEMENT_COL, getElementValueMap(), bElementOntology);
        mapDataOntologyValues(spitalMain, BONE_COMMON_NAME_COL, getElementValueMap(), bElementOntology);

        mapDataOntologyValues(alexandriaTable, TAXON_COL, getTaxonValueMap(), taxonOntology);
        mapDataOntologyValues(spitalMain, SPECIES_COMMON_NAME_COL, getTaxonValueMap(), taxonOntology);

        // testing actual integration mode
        WorkspaceController controller = generateNewInitializedController(WorkspaceController.class);
        controller.execute();
        assertTrue(controller.getBookmarkedDatasets().size() == 2);

        // select tables
        List<Long> tableIds = new ArrayList<Long>();
        tableIds.add(alexandriaTable.getId());
        tableIds.add(spitalMain.getId());
        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTableIds(tableIds);

        // select columns
        controller.selectColumns();
        controller.execute();
        assertTrue("expected 2 selected tables", controller.getSelectedDataTables().size() == 2);
        List<IntegrationColumn> integrationColumns = new ArrayList<IntegrationColumn>();
        integrationColumns.add(new IntegrationColumn(ColumnType.INTEGRATION, spitalMain.getColumnByName(SPECIES_COMMON_NAME_COL), alexandriaTable
                .getColumnByName(TAXON_COL)));
        integrationColumns.add(new IntegrationColumn(ColumnType.INTEGRATION, spitalMain.getColumnByName(BONE_COMMON_NAME_COL), alexandriaTable
                .getColumnByName(BELEMENT_COL)));

        List<String> displayRulesColumns = new ArrayList<String>();
        displayRulesColumns.addAll(Arrays.asList(new String[] { "no", "fus_prox" }));

        for (String col : displayRulesColumns) {
            integrationColumns.add(new IntegrationColumn(ColumnType.DISPLAY, spitalMain.getColumnByName(col)));
        }
        integrationColumns.get(integrationColumns.size() - 1).getColumns().add(alexandriaTable.getColumnByName("feature"));

        // setup filters
        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTableIds(tableIds);
        controller.setIntegrationColumns(integrationColumns);
        controller.filterDataValues();
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
        logger.info("{}", results);
        assertEquals(2,results.size());
        for (IntegrationDataResult result : results) {
            logger.info("{} - {}",result.getDataTable().getName(), result.getRowData().size());
            assertTrue(CollectionUtils.isNotEmpty(result.getRowData()));
            // List<DataTableColumn> columnsToDisplay = result.getColumnsToDisplay();
            // for (DataTableColumn col : columnsToDisplay) { // capturing to test later
            // resultingDataTableColumns.add(col.getName());
            // resultingDataTableColumns.add(col.getDisplayName());
            // }
            for (List<String> rowData : result.getRowData()) {
                if (rowData.get(0).equals(org.tdar.db.model.abstracts.Database.NULL_EMPTY_INTEGRATION_VALUE))
                    seenElementNull = true;
                if (rowData.get(2).equals(org.tdar.db.model.abstracts.Database.NULL_EMPTY_INTEGRATION_VALUE))
                    seenSpeciesNull = true;
            }
        }

        // assertTrue(resultingDataTableColumns.containsAll(displayRulesColumns));
        assertTrue(seenElementNull);
        assertTrue(seenSpeciesNull);
        logger.info("hi, we're done here");
    }

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
        DataTable spitalMain = spitalDb.getDataTableByGenericName("spital_abone_database_mdb_main_table");

        // bookmark datasets
        bookmarkResource(alexandriaDb);
        bookmarkResource(spitalDb);

        // map ontologies to columns (setup proxies and then map)
        logger.info("mapping ontologies");
        DataTableColumn elementColumn = new DataTableColumn();
        elementColumn.setDefaultOntology(bElementOntology);
        elementColumn.setName(BELEMENT_COL);
        elementColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        mapColumnsToDataset(alexandriaDb, alexandriaTable, elementColumn);
        elementColumn.setName(BONE_COMMON_NAME_COL);
        mapColumnsToDataset(spitalDb, spitalMain, elementColumn);

        mapDataOntologyValues(alexandriaTable, BELEMENT_COL, getHierarchyElementMap(), bElementOntology);
        mapDataOntologyValues(spitalMain, BONE_COMMON_NAME_COL, getHierarchyElementMap(), bElementOntology);

        // testing actual integration mode
        WorkspaceController controller = generateNewInitializedController(WorkspaceController.class);
        controller.execute();
        assertTrue(controller.getBookmarkedDatasets().size() == 2);

        // select tables
        List<Long> tableIds = new ArrayList<Long>();
        tableIds.add(alexandriaTable.getId());
        tableIds.add(spitalMain.getId());
        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTableIds(tableIds);

        // select columns
        controller.selectColumns();
        controller.execute();
        assertTrue("expected 2 selected tables", controller.getSelectedDataTables().size() == 2);
        List<IntegrationColumn> integrationColumns = new ArrayList<IntegrationColumn>();
        integrationColumns.add(new IntegrationColumn(ColumnType.INTEGRATION, spitalMain.getColumnByName(BONE_COMMON_NAME_COL), alexandriaTable
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

            for (List<String> rowData : result.getRowData()) {
                if (rowData.get(0).equals(org.tdar.db.model.abstracts.Database.NULL_EMPTY_INTEGRATION_VALUE)) {
                    seenElementNull = true;
                }
                if (rowData.get(1).equalsIgnoreCase("tarsal"))
                    tarsal++;
                if (rowData.get(1).equalsIgnoreCase("ulna"))
                    ulna++;
                if (rowData.get(1).equalsIgnoreCase("astragalus"))
                    astragalus++;
                if (rowData.get(1).equalsIgnoreCase(org.tdar.db.model.abstracts.Database.NULL_EMPTY_MAPPED_VALUE))
                    empty++;
            }
        }
        logger.info("tarsal:" + tarsal);
        logger.info("astragalus:" + astragalus);
        logger.info("ulna:" + ulna);
        assertEquals(41, astragalus);
        assertEquals(111, tarsal);
        assertEquals(332, ulna);
        assertEquals(276, empty);

        assertTrue(seenElementNull);
        logger.info("hi, we're done here");
    }
}
