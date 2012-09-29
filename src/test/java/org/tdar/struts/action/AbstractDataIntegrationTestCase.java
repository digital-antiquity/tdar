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

import javax.sql.DataSource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.db.conversion.DatasetConversionFactory;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.filestore.Filestore;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;
import org.tdar.struts.action.resource.DatasetController;
import org.tdar.struts.data.IntegrationColumn;
import org.tdar.struts.data.IntegrationDataResult;
import org.tdar.struts.data.OntologyNodeData;

public abstract class AbstractDataIntegrationTestCase extends AbstractAdminControllerITCase {

    public static final long SPITAL_IR_ID = 503l;
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

    public Map<String, String> getElementValueMap() {
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

    public Map<String, String> getHierarchyElementMap() {
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

    public Map<String, String> getTaxonValueMap() {
        HashMap<String, String> taxonValueMap = new HashMap<String, String>();
        taxonValueMap.put("cat", "Felis catus (Cat)");
        taxonValueMap.put("CAT", "Felis catus (Cat)");
        taxonValueMap.put("DOG", "Canis familiaris (Dog)");
        taxonValueMap.put("dog", "Canis familiaris (Dog)");
        taxonValueMap.put("sheep", "Ovis aries (Sheep)");
        taxonValueMap.put("SHEEP", "Ovis aries (Sheep)");
        return taxonValueMap;
    }

    protected InformationResourceFileVersion makeFileVersion(String name, long id) throws IOException {
        InformationResourceFileVersion version = new InformationResourceFileVersion(VersionType.UPLOADED, name, 1, 1234L, 123L);
        version.setId(id);
        File file = new File(getTestFilePath() + "/" + name);
        filestore.store(file, version);
        return version;
    }

    public Dataset setupAndConvertDataset(String filename, Long irFileId) throws IOException {
        DatasetConverter converter = convertDatabase(filename, irFileId);
        Dataset dataset = new Dataset();
        dataset.setDataTables(converter.getDataTables());
        dataset.setTitle(filename);
        dataset.setDescription(filename);
        dataset.markUpdated(getUser());
        datasetService.saveOrUpdate(dataset);
        datasetService.saveOrUpdateAll(dataset.getDataTables());
        return dataset;
    }

    public DatasetConverter convertDatabase(String filename, Long irFileId) throws IOException, FileNotFoundException {
        InformationResourceFileVersion accessDatasetFileVersion = makeFileVersion(filename, irFileId);
        File storedFile = filestore.retrieveFile(accessDatasetFileVersion);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(accessDatasetFileVersion, tdarDataImportDatabase);
        converter.execute();
        return converter;
    }

    public DatasetConverter setupSpitalfieldAccessDatabase() throws IOException {
        DatasetConverter converter = convertDatabase(SPITAL_DB_NAME, SPITAL_IR_ID);
        return converter;
    }

    @Autowired
    @Qualifier("tdarDataImportDataSource")
    public void setIntegrationDataSource(DataSource dataSource) {
        tdarDataImportDatabase.setDataSource(dataSource);
    }

    public abstract String[] getDatabaseList();

    @Before
    public void prepareFiles() throws Exception {
        // have the filestore put our sample files... wherever it puts files
        String[] list = new String[0];
        if (getDatabaseList() != null) {
            list = getDatabaseList();
        };
        for (String database : list) {
            try {
                tdarDataImportDatabase.dropTable(database);
            } catch (Exception ignored) {
            }
        }

    }

    protected void mapDataOntologyValues(DataTable dataTable, String columnName, Map<String, String> valueMap, Ontology ontology) throws TdarActionException {
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        DataTableColumn column = dataTable.getColumnByName(columnName);
        AbstractResourceControllerITCase.loadResourceFromId(controller, dataTable.getDataset().getId());
        controller.setDataTableId(dataTable.getId());
        controller.setColumnId(column.getId());
        controller.loadOntologyMappedColumns();
        List<String> dataColumnValues = controller.getDistinctColumnValues();
        List<String> ontologyNodeNames = controller.getOntologyNodeNames();
        List<Long> ontologyNodeIds = controller.getOntologyNodeIds();
        logger.info("mapping ontology values for: " + dataTable.getName() + " [" + columnName + "]");
        for (int i = 0; i < dataColumnValues.size(); i++) {
            String name = dataColumnValues.get(i);
            if (valueMap.get(name) != null) {
                OntologyNode node = ontology.getNodeByNameIgnoreCase(valueMap.get(name));
                if (node != null) {
                    logger.info("setting " + name + " ->" + valueMap.get(name) + " (" + node.getId() + ")");
                    ontologyNodeIds.set(i, node.getId());
                } else {
                    logger.info("ontology does not contain: " + name);
                }
            }
        }
        controller.setDataColumnValues(dataColumnValues);
        controller.setOntologyNodeNames(ontologyNodeNames);
        controller.setOntologyNodeIds(ontologyNodeIds);
        controller.saveDataValueOntologyNodeMapping();
        assertTrue(column.getValueToOntologyNodeMap().size() > 0);
        Long columnId = dataTable.getColumnByName(columnName).getId();
        column = genericService.find(DataTableColumn.class, columnId);
        assertNotNull(column.getValueToOntologyNodeMap());
    }

    public void mapColumnsToDataset(Dataset dataset, DataTable dataTable, DataTableColumn... mappings) throws TdarActionException {
        logger.info("{}", dataTable);
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        controller.setDataTableId(dataTable.getId());
        AbstractResourceControllerITCase.loadResourceFromId(controller, dataset.getId());
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
        WorkspaceController controller;
        performIntegrationFiltering(integrationColumns, nodeSelectionMap);
        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTableIds(tableIds);
        controller.setIntegrationColumns(integrationColumns);
        controller.displayFilteredResults();

        logger.info("Testing Integration Results");
        assertNotNull(controller.getIntegrationDataResults());
        for (IntegrationDataResult integrationDataResult : controller.getIntegrationDataResults()) {
            int colCount = integrationDataResult.getIntegrationColumns().size();

            for (IntegrationColumn col : integrationColumns) { // adding ontology mapping entry
                if (!col.isDisplayColumn())
                    colCount++;
            }
            int size = 0;
            for (List<String> data : integrationDataResult.getRowData()) {
                size++;
                assertEquals("row " + size + " didn't match expected # of columns " + colCount, data.size(), colCount);
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
            if (integrationColumn.isDisplayColumn())
                continue;
            if (nodeSelectionMap.get(integrationColumn.getSharedOntology()) != null) {
                int foundNodeCount = 0;
                for (OntologyNodeData nodeData : integrationColumn.getFlattenedOntologyNodeList()) {
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
