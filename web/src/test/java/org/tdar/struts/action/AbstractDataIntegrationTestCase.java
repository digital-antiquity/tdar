package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.integration.ModernIntegrationDataResult;
import org.tdar.struts.action.codingSheet.CodingSheetMappingController;
import org.tdar.struts.action.dataset.ColumnMetadataController;
import org.tdar.struts.action.workspace.IntegrationDownloadAction;
import org.tdar.struts.action.workspace.LegacyWorkspaceController;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractDataIntegrationTestCase extends AbstractAdminControllerITCase {

    // public static final long SPITAL_IR_ID = 503l;

    public static Map<String, String> getElementValueMap() {
        HashMap<String, String> elementValueMap = new HashMap<String, String>();
        elementValueMap.put("Atlas", "Atlas");
        elementValueMap.put("Axis", "Axis");
        elementValueMap.put("Carpal", "Carpal");
        elementValueMap.put("Tooth Unknown", "Tooth");
        elementValueMap.put("Tooth", "Tooth");
        elementValueMap.put("Ulna", "Ulna");
        elementValueMap.put("ATLAS", "Atlas");
        elementValueMap.put("AXIS", "Axis");
        elementValueMap.put("CARPAL", "Carpal");
        elementValueMap.put("TOOTH UNKNOWN", "Tooth");
        elementValueMap.put("TOOTH", "Tooth");
        elementValueMap.put("ULNA", "Ulna");
        return elementValueMap;
    }

    public static Map<String, String> getHierarchyElementMap() {
        Map<String, String> elementValueMap = getElementValueMap();
        elementValueMap.put("TARSAL", "Tarsal");
        elementValueMap.put("ASTRAGALUS", "Astragalus");
        elementValueMap.put("CALCANEUM", "Calcaneus");
        elementValueMap.put("CUBOID", "Cuboid (4th tarsal)");
        elementValueMap.put("LATERAL MALLEOLUS", "Lateral malleolus");
        elementValueMap.put("NAVICULAR", "Navicular (Central)");

        elementValueMap.put("Navicular", "Navicular (Central)");
        elementValueMap.put("Navicular/Cuboid", "Navicular (Central) and cuboid (4th tarsal)");
        elementValueMap.put("Cuboid", "Cuboid (4th tarsal)");
        elementValueMap.put("Calcaneum", "Calcaneus");
        elementValueMap.put("Calcaneus", "Calcaneus");
        elementValueMap.put("Astragalus", "Astragalus");
        elementValueMap.put("Cuneiform", "1st cuneiform (1st tarsal)");
        elementValueMap.put("Cuneiform Pes", "Tarsal");
        elementValueMap.put("Tarsal", "Tarsal");
        elementValueMap.put("Unknown Tarsal", "Tarsal");

        return elementValueMap;
    }

    public static Map<String, String> getTaxonValueMap() {
        HashMap<String, String> taxonValueMap = new HashMap<String, String>();
        taxonValueMap.put("cat", "Felis catus (Cat)");
        taxonValueMap.put("CAT", "Felis catus (Cat)");
        taxonValueMap.put("DOG", "Canis familiaris (Dog)");
        taxonValueMap.put("dog", "Canis familiaris (Dog)");
        taxonValueMap.put("sheep", "Ovis aries (Sheep)");
        taxonValueMap.put("SHEEP", "Ovis aries (Sheep)");
        return taxonValueMap;
    }


    protected void mapDataOntologyValues(DataTable dataTable, String columnName, Map<String, String> valueMap, Ontology ontology) throws Exception {
        CodingSheetMappingController controller = generateNewInitializedController(CodingSheetMappingController.class);
        DataTableColumn column = dataTable.getColumnByName(columnName);
        controller.setId(column.getDefaultCodingSheet().getId());
        controller.prepare();
        controller.loadOntologyMappedColumns();
        Set<CodingRule> rules = column.getDefaultCodingSheet().getCodingRules();
        // List<OntologyNode> ontologyNodes = column.getDefaultOntology().getOntologyNodes();
        // List<String> dataColumnValues = dataTableService.findAllDistinctValues(column);
        logger.info("mapping ontology values for: {} [{}]", dataTable.getName(), columnName);
        logger.info("ontology nodes: {}", ontology.getOntologyNodes());
        List<CodingRule> toSave = new ArrayList<CodingRule>();
        for (CodingRule rule : rules) {
            String value = valueMap.get(rule.getTerm());
            if (value != null) {
                OntologyNode node = ontology.getNodeByNameIgnoreCase(value);
                if (node != null) {
                    logger.info(String.format("setting %s -> %s (%s)", rule.getTerm(), value, node));
                    rule.setOntologyNode(node);
                    toSave.add(rule);
                }
            } else {
                logger.info("ontology does not contain: " + rule.getTerm());
            }
        }
        controller.setCodingRules(toSave);
        controller.saveValueOntologyNodeMapping();

        Set<Long> idSet = PersistableUtils.createIdMap(toSave).keySet();
        for (Long toCheck : idSet) {
            CodingRule find = genericService.find(CodingRule.class, toCheck);
            if (find == null) {
                logger.error("{} {}", toCheck, find);
                logger.error("{}", toSave);
            } else {
                assertNotNull(find.getOntologyNode());
            }
        }
        Assert.assertNotSame(0, toSave.size());
    }

    public void mapColumnsToDataset(Dataset dataset, DataTable dataTable, DataTableColumn... mappings) throws Exception {
        logger.info("{}", dataTable);
        ColumnMetadataController controller = generateNewInitializedController(ColumnMetadataController.class);
        controller.setDataTableId(dataTable.getId());
        controller.setId(dataset.getId());
        controller.prepare();
        controller.setDataTableColumns(Arrays.asList(mappings));
        controller.saveColumnMetadata();

        for (DataTableColumn mapping : mappings) {
            DataTableColumn col = dataTable.getColumnByName(mapping.getName());
            assertNotNull(col.getName() + " is null", col);
            // assertEquals(col.getName() + " is missing ontology", mapping.getDefaultOntology(), col.getDefaultOntology());
            assertEquals(col.getName() + " is missing coding sheet", mapping.getDefaultCodingSheet(), col.getDefaultCodingSheet());
        }
    }

    public Object performActualIntegration(List<Long> tableIds, List<IntegrationColumn> integrationColumns,
            HashMap<Ontology, String[]> nodeSelectionMap) throws Exception {
        LegacyWorkspaceController controller = generateNewInitializedController(LegacyWorkspaceController.class);
        performIntegrationFiltering(integrationColumns, nodeSelectionMap);
        controller.setTableIds(tableIds);
        controller.setIntegrationColumns(integrationColumns);
        controller.displayFilteredResults();

        logger.info("Testing Integration Results");
        assertNotNull(controller.getResult());
        logger.info("{}", controller.getIntegrationColumns());

        Long ticketId = controller.getTicketId();
        assertNotNull(ticketId);
        ModernIntegrationDataResult result = controller.getResult();
        IntegrationDownloadAction dc = generateNewInitializedController(IntegrationDownloadAction.class);
        dc.setTicketId(ticketId);
        dc.prepare();
        dc.downloadIntegrationDataResults();
        InputStream integrationDataResultsInputStream = dc.getIntegrationDataResultsInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(integrationDataResultsInputStream));
        Assert.assertFalse(StringUtils.isEmpty(reader.readLine()));
        return result;
    }

    public List<String> performIntegrationFiltering(List<IntegrationColumn> integrationColumns, HashMap<Ontology, String[]> nodeSelectionMap) {
        List<String> checkedNodeList = new ArrayList<String>();
        for (IntegrationColumn integrationColumn : integrationColumns) {
            if (!integrationColumn.isIntegrationColumn()) {
                continue;
            }
            String[] nodeSelections = nodeSelectionMap.get(integrationColumn.getSharedOntology());
            if (nodeSelections != null) {
                int foundNodeCount = 0;
                for (OntologyNode nodeData : dataIntegrationService.getFilteredOntologyNodes(integrationColumn)) {
                    String name = nodeData.getDisplayName();
                    logger.debug("comparing {} <-> {}", name, StringUtils.join(nodeSelections, "|"));
                    if (ArrayUtils.contains(nodeSelections, name)) {
                        foundNodeCount++;
                        integrationColumn.getFilteredOntologyNodes().add(new OntologyNode(nodeData.getId()));

                    }
                }
                logger.debug("nodeSelections: {}", Arrays.asList(nodeSelections));
                assertEquals(foundNodeCount, nodeSelections.length);
            } else {
                assertTrue("found unexpected ontology", false);
            }
        }
        return checkedNodeList;
    }

}
