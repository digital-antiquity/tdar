package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.codingSheet.CodingSheetMappingController;
import org.tdar.struts.action.dataset.ColumnMetadataController;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.ActionSupport;

public interface TestDatasetHelper {

    final Logger logger_ = LoggerFactory.getLogger(TestFileUploadHelper.class);

    default Map<String, String> getElementValueMap() {
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

    default Map<String, String> getHierarchyElementMap() {
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

    default Map<String, String> getTaxonValueMap() {
        HashMap<String, String> taxonValueMap = new HashMap<String, String>();
        taxonValueMap.put("cat", "Felis catus (Cat)");
        taxonValueMap.put("CAT", "Felis catus (Cat)");
        taxonValueMap.put("DOG", "Canis familiaris (Dog)");
        taxonValueMap.put("dog", "Canis familiaris (Dog)");
        taxonValueMap.put("sheep", "Ovis aries (Sheep)");
        taxonValueMap.put("SHEEP", "Ovis aries (Sheep)");
        return taxonValueMap;
    }

    default void mapDataOntologyValues(DataTable dataTable, String columnName, Map<String, String> valueMap, Ontology ontology) throws Exception {
        CodingSheetMappingController controller = generateNewInitializedController(CodingSheetMappingController.class);
        DataTableColumn column = dataTable.getColumnByName(columnName);
        controller.setId(column.getDefaultCodingSheet().getId());
        controller.prepare();
        controller.loadOntologyMappedColumns();
        Set<CodingRule> rules = column.getDefaultCodingSheet().getCodingRules();
        // List<OntologyNode> ontologyNodes = column.getDefaultOntology().getOntologyNodes();
        // List<String> dataColumnValues = dataTableService.findAllDistinctValues(column);
        logger_.info("mapping ontology values for: {} [{}]", dataTable.getName(), columnName);
        logger_.info("ontology nodes: {}", ontology.getOntologyNodes());
        List<CodingRule> toSave = new ArrayList<CodingRule>();
        for (CodingRule rule : rules) {
            String value = valueMap.get(rule.getTerm());
            if (value != null) {
                OntologyNode node = ontology.getNodeByNameIgnoreCase(value);
                if (node != null) {
                    logger_.info(String.format("setting %s -> %s (%s)", rule.getTerm(), value, node));
                    rule.setOntologyNode(node);
                    toSave.add(rule);
                }
            } else {
                logger_.info("ontology does not contain: " + rule.getTerm());
            }
        }
        controller.setCodingRules(toSave);
        controller.saveValueOntologyNodeMapping();

        Set<Long> idSet = PersistableUtils.createIdMap(toSave).keySet();
        for (Long toCheck : idSet) {
            CodingRule find = getGenericService().find(CodingRule.class, toCheck);
            if (find == null) {
                logger_.error("{} {}", toCheck, find);
                logger_.error("{}", toSave);
            } else {
                assertNotNull(find.getOntologyNode());
            }
        }
        Assert.assertNotSame(0, toSave.size());
    }

    default void mapColumnsToDataset(Dataset dataset, DataTable dataTable, DataTableColumn... mappings) throws Exception {
        logger_.info("{}", dataTable);
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
    
    <T extends ActionSupport> T generateNewInitializedController(Class<T> class1);

    GenericService getGenericService();

}
