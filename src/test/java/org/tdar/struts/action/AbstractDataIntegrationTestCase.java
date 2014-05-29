package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.tdar.TestConstants;
import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.db.conversion.DatasetConversionFactory;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;
import org.tdar.struts.action.resource.CodingSheetController;
import org.tdar.struts.action.resource.DatasetController;
import org.tdar.struts.data.IntegrationColumn;
import org.tdar.struts.data.IntegrationDataResult;

public abstract class AbstractDataIntegrationTestCase extends AbstractAdminControllerITCase {

    // public static final long SPITAL_IR_ID = 503l;
    public static final String SPITAL_DB_NAME = "Spital Abone database.mdb";
    protected static final String PATH = TestConstants.TEST_DATA_INTEGRATION_DIR;

    protected PostgresDatabase tdarDataImportDatabase = new PostgresDatabase();
    protected Filestore filestore = TdarConfiguration.getInstance().getFilestore();

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    @Override
    protected String getTestFilePath() {
        return PATH;
    }

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

    protected InformationResourceFileVersion makeFileVersion(File name, long id) throws IOException {
        long infoId = (long) (Math.random() * 10000);
        InformationResourceFileVersion version = new InformationResourceFileVersion(VersionType.UPLOADED, name.getName(), 1, infoId, 123L);
        version.setId(id);
        filestore.store(ObjectType.RESOURCE, name, version);
        version.setTransientFile(name);
        return version;
    }

    public DatasetConverter convertDatabase(File file, Long irFileId) throws IOException, FileNotFoundException {
        InformationResourceFileVersion accessDatasetFileVersion = makeFileVersion(file, irFileId);
        File storedFile = filestore.retrieveFile(ObjectType.RESOURCE, accessDatasetFileVersion);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(accessDatasetFileVersion, tdarDataImportDatabase);
        converter.execute();
        setDataImportTables((String[]) ArrayUtils.addAll(getDataImportTables(), converter.getTableNames().toArray(new String[0])));
        return converter;
    }

    static Long spitalIrId = (long) (Math.random() * 10000);

    public DatasetConverter setupSpitalfieldAccessDatabase() throws IOException {
        spitalIrId++;
        DatasetConverter converter = convertDatabase(new File(getTestFilePath(), SPITAL_DB_NAME), spitalIrId);
        return converter;
    }

    @Autowired
    @Qualifier("tdarDataImportDataSource")
    public void setIntegrationDataSource(DataSource dataSource) {
        tdarDataImportDatabase.setDataSource(dataSource);
    }

    String[] dataImportTables = new String[0];

    public String[] getDataImportTables() {
        return dataImportTables;
    }

    public void setDataImportTables(String[] dataImportTables) {
        this.dataImportTables = dataImportTables;
    }

    @Before
    public void dropDataImportDatabaseTables() throws Exception {
        for (String table : getDataImportTables()) {
            try {
                tdarDataImportDatabase.dropTable(table);
            } catch (Exception ignored) {
            }
        }

    }

    protected void mapDataOntologyValues(DataTable dataTable, String columnName, Map<String, String> valueMap, Ontology ontology) throws TdarActionException {
        CodingSheetController controller = generateNewInitializedController(CodingSheetController.class);
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

        Set<Long> idSet = Base.createIdMap(toSave).keySet();
        for (Long toCheck : idSet) {
            CodingRule find = genericService.find(CodingRule.class, toCheck);
            assertNotNull(find.getOntologyNode());
        }
        Assert.assertNotSame(0, toSave.size());
    }

    public void mapColumnsToDataset(Dataset dataset, DataTable dataTable, DataTableColumn... mappings) throws TdarActionException {
        logger.info("{}", dataTable);
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setDataTableId(dataTable.getId());
        controller.setId(dataset.getId());
        controller.prepare();
        controller.setDataTableColumns(Arrays.asList(mappings));
        controller.saveColumnMetadata();

        for (DataTableColumn mapping : mappings) {
            DataTableColumn col = dataTable.getColumnByName(mapping.getName());
            assertNotNull(col.getName() + " is null", col);
            assertEquals(col.getName() + " is missing ontology", mapping.getDefaultOntology(), col.getDefaultOntology());
            assertEquals(col.getName() + " is missing coding sheet", mapping.getDefaultCodingSheet(), col.getDefaultCodingSheet());
        }
    }

    public List<IntegrationDataResult> performActualIntegration(List<Long> tableIds, List<IntegrationColumn> integrationColumns,
            HashMap<Ontology, String[]> nodeSelectionMap) throws IOException {
        WorkspaceController controller = generateNewInitializedController(WorkspaceController.class);
        performIntegrationFiltering(integrationColumns, nodeSelectionMap);
        controller.setTableIds(tableIds);
        controller.setIntegrationColumns(integrationColumns);
        controller.displayFilteredResults();

        logger.info("Testing Integration Results");
        assertNotNull(controller.getIntegrationDataResults());
        for (IntegrationDataResult integrationDataResult : controller.getIntegrationDataResults()) {

            // expected colcount includes one table name, integration column count, and display column count
            int colCount = 1;

            colCount += integrationDataResult.getIntegrationColumns().size();

            for (IntegrationColumn col : integrationColumns) { // adding ontology mapping entry
                if (!col.isDisplayColumn()) {
                    colCount++;
                }
            }

            int size = 0;
            for (String[] data : integrationDataResult.getRowData()) {
                size++;
                assertEquals("row " + size + " didn't match expected # of columns " + colCount, colCount, data.length);
            }
        }
        logger.info("{}", controller.getIntegrationColumns());

        List<IntegrationDataResult> results = controller.getIntegrationDataResults();
        Long ticketId = controller.getTicketId();
        assertNotNull(ticketId);

        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTicketId(ticketId);
        controller.downloadIntegrationDataResults();
        InputStream integrationDataResultsInputStream = controller.getIntegrationDataResultsInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(integrationDataResultsInputStream));
        Assert.assertFalse(StringUtils.isEmpty(reader.readLine()));
        return results;
    }

    public List<String> performIntegrationFiltering(List<IntegrationColumn> integrationColumns, HashMap<Ontology, String[]> nodeSelectionMap) {
        List<String> checkedNodeList = new ArrayList<String>();
        for (IntegrationColumn integrationColumn : integrationColumns) {
            if (integrationColumn.isDisplayColumn()) {
                continue;
            }
            if (nodeSelectionMap.get(integrationColumn.getSharedOntology()) != null) {
                int foundNodeCount = 0;
                for (OntologyNode nodeData : integrationColumn.getFlattenedOntologyNodeList()) {
                    if (ArrayUtils.contains(nodeSelectionMap.get(integrationColumn.getSharedOntology()), nodeData.getDisplayName())) {
                        logger.trace("comparing " + nodeData.getDisplayName() + " <-> "
                                + StringUtils.join(nodeSelectionMap.get(integrationColumn.getSharedOntology()), "|"));
                        foundNodeCount++;
                        integrationColumn.getFilteredOntologyNodes().add(new OntologyNode(nodeData.getId()));

                    }
                }
                assertEquals(foundNodeCount, nodeSelectionMap.get(integrationColumn.getSharedOntology()).length);
            } else {
                assertTrue("found unexpected ontology", false);
            }
        }
        return checkedNodeList;
    }
}
