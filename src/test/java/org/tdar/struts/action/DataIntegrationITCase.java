/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractDataIntegrationTestCase;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.struts.data.IntegrationDataResult;
import org.tdar.struts.data.IntegrationRowData;
import org.tdar.struts.data.OntologyDataFilter;

/**
 * @author Adam Brin
 * 
 */
public class DataIntegrationITCase extends AbstractDataIntegrationTestCase {

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
    public void testIntegrationProcess() throws IOException {
        // load datasets
        Dataset alexandriaDb = setupAndLoadResource("qrybonecatalogueeditedkk.xls", Dataset.class);
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
        taxonColumn.setName(TAXON_COL);
        taxonColumn.setDefaultOntology(taxonOntology);

        mapColumnsToDataset(alexandriaDb, alexandriaTable, elementColumn, taxonColumn);
        elementColumn.setName(BONE_COMMON_NAME_COL);
        taxonColumn.setName(SPECIES_COMMON_NAME_COL);

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
        List<String> integrationRules = new ArrayList<String>();
        List<String> displayRules = new ArrayList<String>();
        integrationRules.add(generateIntegrationRule(spitalMain.getColumnByName(SPECIES_COMMON_NAME_COL), alexandriaTable.getColumnByName(TAXON_COL)));
        integrationRules.add(generateIntegrationRule(spitalMain.getColumnByName(BONE_COMMON_NAME_COL), alexandriaTable.getColumnByName(BELEMENT_COL)));

        List<String> displayRulesColumns = new ArrayList<String>();
        displayRulesColumns.addAll(Arrays.asList(new String[] { "no", "fus_prox" }));

        for (String col : displayRulesColumns) {
            displayRules.add(generateDisplayRule(spitalMain.getColumnByName(col)));

        }
        displayRules.add(generateDisplayRule(alexandriaTable.getColumnByName("feature")));
        displayRulesColumns.add("feature");

        // setup filters
        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTableIds(tableIds);
        controller.setDisplayRules(displayRules);
        controller.setIntegrationRules(integrationRules);
        controller.filterDataValues();
        controller.execute();
        assertEquals(2, controller.getIntegrationColumnIds().size());
        List<OntologyDataFilter> ontologyDataFilters = controller.getOntologyDataFilters();
        assertFalse(ontologyDataFilters.isEmpty());
        assertEquals(2, ontologyDataFilters.size());
        HashMap<Ontology, String[]> nodeSelectionMap = new HashMap<Ontology, String[]>();
        nodeSelectionMap.put(taxonOntology, new String[] { "Felis catus (Cat)", "Canis familiaris (Dog)", "Ovis aries (Sheep)" });
        nodeSelectionMap.put(bElementOntology, new String[] { "Atlas", "Axis", "Carpal", "Tooth", "Ulna" });

        List<IntegrationDataResult> results = performActualIntegration(tableIds, integrationRules, displayRules, ontologyDataFilters, nodeSelectionMap);
        // assuming we're dealing with alexandria here
        boolean seenElementNull = false;
        boolean seenSpeciesNull = false;
        logger.info(results);
        List<String> resultingDataTableColumns = new ArrayList<String>();
        for (IntegrationDataResult result : results) {

            List<DataTableColumn> columnsToDisplay = result.getColumnsToDisplay();
            for (DataTableColumn col : columnsToDisplay) { // capturing to test later
                resultingDataTableColumns.add(col.getName());
                resultingDataTableColumns.add(col.getDisplayName());
            }
            for (IntegrationRowData rowData : result.getRowData()) {
                // logger.info(rowData.getDataValues());
                if (rowData.getDataValues().get(0).contains(org.tdar.db.model.abstracts.Database.NULL_EMPTY_INTEGRATION_VALUE))
                    seenElementNull = true;
                if (rowData.getDataValues().get(1).contains(org.tdar.db.model.abstracts.Database.NULL_EMPTY_INTEGRATION_VALUE))
                    seenSpeciesNull = true;
            }
        }

        assertTrue(resultingDataTableColumns.containsAll(displayRulesColumns));
        assertTrue(seenElementNull);
        assertTrue(seenSpeciesNull);
        logger.info("hi, we're done here");
    }
}
