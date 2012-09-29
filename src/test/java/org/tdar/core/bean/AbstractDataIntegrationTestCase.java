package org.tdar.core.bean;

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
import org.tdar.core.bean.resource.dataTable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.dataTable.MeasurementUnit;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.db.conversion.DatasetConversionFactory;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.filestore.Filestore;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.DatasetController;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.WorkspaceController;
import org.tdar.struts.data.IntegrationDataResult;
import org.tdar.struts.data.IntegrationRowData;
import org.tdar.struts.data.OntologyDataFilter;
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
        dataset.markUpdated(getTestPerson());
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
        for (String database : getDatabaseList()) {
            try {
                tdarDataImportDatabase.dropTable(database);
            } catch (Exception ignored) {
            }
        }

    }

    protected void mapDataOntologyValues(DataTable dataTable, String columnName, Map<String, String> valueMap, Ontology ontology) {
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        DataTableColumn column = dataTable.getColumnByName(columnName);
        loadResourceFromId(controller, dataTable.getDataset().getId());
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
                logger.info("setting " + name + " ->" + valueMap.get(name) + " (" + node.getId() + ")");
                ontologyNodeIds.set(i, node.getId());
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

    public void mapColumnsToDataset(Dataset dataset, DataTable dataTable, DataTableColumn... mappings) {
        logger.info(dataTable);
        DatasetController controller = generateNewInitializedController(DatasetController.class);
        loadResourceFromId(controller, dataset.getId());
        List<DataTableColumnEncodingType> encodingTypes = new ArrayList<DataTableColumnEncodingType>();
        List<Long> codingSheetIds = new ArrayList<Long>();
        List<Long> ontologyIds = new ArrayList<Long>();
        List<MeasurementUnit> measurementUnits = new ArrayList<MeasurementUnit>();
        List<String> columnDescriptions = new ArrayList<String>();
        List<Long> categoryVariableIds = new ArrayList<Long>();
        List<Long> subcategoryIds = new ArrayList<Long>();
        controller.setDataTableId(dataTable.getId());
        for (DataTableColumn column : dataTable.getSortedDataTableColumns()) {
            boolean mappedOntology = false;
            boolean mappedCoding = false;
            boolean mappedEncoding = false;
            for (DataTableColumn mapping : mappings) {
                logger.info(mapping.getName() + " : " + column.getName());
                if (column.getName().equalsIgnoreCase(mapping.getName())) {
                    logger.info(column.getName());
                    if (mapping.getDefaultOntology() != null) {
                        ontologyIds.add(mapping.getDefaultOntology().getId());
                        mappedOntology = true;
                    }
                    if (mapping.getDefaultCodingSheet() != null) {
                        codingSheetIds.add(mapping.getDefaultCodingSheet().getId());
                        mappedCoding = true;
                    }
                    if (mapping.getColumnEncodingType() != null) {
                        encodingTypes.add(mapping.getColumnEncodingType());
                        mappedEncoding = true;
                    }
                }
            }
            if (!mappedOntology) {
                ontologyIds.add(null);
            }

            if (!mappedCoding) {
                codingSheetIds.add(null);
            }
            if (!mappedEncoding) {
                encodingTypes.add(column.getColumnEncodingType());
            }
            measurementUnits.add(null);
            columnDescriptions.add(null);
            categoryVariableIds.add(null);
            subcategoryIds.add(null);
        }
        controller.setCodingSheetIds(codingSheetIds);
        controller.setOntologyIds(ontologyIds);
        controller.setColumnEncodingTypes(encodingTypes);
        controller.setMeasurementUnits(measurementUnits);
        controller.setColumnDescriptions(columnDescriptions);
        controller.setCategoryVariableIds(categoryVariableIds);
        controller.setSubcategoryIds(subcategoryIds);
        controller.saveColumnMetadata();
        int i = 0;
        for (DataTableColumn mapping : mappings) {
            DataTableColumn col = dataTable.getColumnByName(mapping.getName());
            assertNotNull(col.getName() + " is null", col);
            assertEquals(col.getName() + " is missing ontology", mapping.getDefaultOntology(), col.getDefaultOntology());
            assertEquals(col.getName() + " is missing coding sheet", mapping.getDefaultCodingSheet(), col.getDefaultCodingSheet());
        }
    }

    public List<IntegrationDataResult> performActualIntegration(List<Long> tableIds, List<String> integrationRules, List<String> displayRules,
            List<OntologyDataFilter> ontologyDataFilters, HashMap<Ontology, String[]> nodeSelectionMap) throws IOException {
        WorkspaceController controller;
        List<String> integrationFilters = performIntegrationFiltering(ontologyDataFilters, nodeSelectionMap);
        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTableIds(tableIds);
        controller.setDisplayRules(displayRules);
        controller.setIntegrationRules(integrationRules);
        controller.setOntologyNodeFilterSelections(integrationFilters);
        controller.displayFilteredResults();

        logger.info("Testing Integration Results");
        assertNotNull(controller.getIntegrationDataResults());
        for (IntegrationDataResult integrationDataResult : controller.getIntegrationDataResults()) {
            int colCount = integrationDataResult.getColumnsToDisplay().size() + integrationDataResult.getIntegrationColumns().size();
            for (IntegrationRowData data : integrationDataResult.getRowData()) {
                assertEquals(data.getDataValues().size(), colCount);
            }
        }
        logger.info(controller.getDisplayRules());

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

    public List<String> performIntegrationFiltering(List<OntologyDataFilter> ontologyDataFilters, HashMap<Ontology, String[]> nodeSelectionMap) {
        List<String> checkedNodeList = new ArrayList<String>();
        for (OntologyDataFilter filter : ontologyDataFilters) {
            if (nodeSelectionMap.get(filter.getCommonOntology()) != null) {
                int foundNodeCount = 0;
                for (OntologyNodeData nodeData : filter.getFlattenedOntologyNodeList()) {
                    if (ArrayUtils.contains(nodeSelectionMap.get(filter.getCommonOntology()), nodeData.getDisplayName())) {
                        logger.trace("comparing " + nodeData.getDisplayName() + " <-> "
                                + StringUtils.join(nodeSelectionMap.get(filter.getCommonOntology()), "|"));
                        foundNodeCount++;
                        checkedNodeList.add(formatFilterCheckbox(filter, nodeData));

                    }
                }
                assertEquals(foundNodeCount, nodeSelectionMap.get(filter.getCommonOntology()).length);
            } else {
                assertTrue("found unexpected ontology", false);
            }
        }
        return checkedNodeList;
    }

    public String formatFilterCheckbox(OntologyDataFilter filter, OntologyNodeData nodeData) {
        return filter.getColumnIds() + "_" + nodeData.getId();
    }

    /**
     * @param column
     * @return
     */
    protected String generateDisplayRule(DataTableColumn column) {
        StringBuilder rule = new StringBuilder();
        rule.append(column.getId());
        return rule.toString();
    }

    /**
     * @param columnByName
     * @param columnByName2
     * @return
     */
    protected String generateIntegrationRule(DataTableColumn... columns) {
        StringBuilder rule = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            DataTableColumn column = columns[i];
            rule.append(column.getId());
            if (i + 1 < columns.length)
                rule.append("=");
        }
        return rule.toString();
    }
}
