package org.tdar.struts.action;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.integration.ModernIntegrationDataResult;
import org.tdar.db.conversion.DatasetConversionFactory;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.struts.action.codingSheet.CodingSheetController;
import org.tdar.struts.action.dataset.ColumnMetadataController;
import org.tdar.struts.action.workspace.WorkspaceController;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractDataIntegrationTestCase extends AbstractAdminControllerITCase {

    // public static final long SPITAL_IR_ID = 503l;
    public static final String SPITAL_DB_NAME = "Spital Abone database.mdb";
    protected static final String PATH = TestConstants.TEST_DATA_INTEGRATION_DIR;

    protected PostgresDatabase tdarDataImportDatabase = new PostgresDatabase();
    protected Filestore filestore = TdarConfiguration.getInstance().getFilestore();

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

        Set<Long> idSet = PersistableUtils.createIdMap(toSave).keySet();
        for (Long toCheck : idSet) {
            CodingRule find = genericService.find(CodingRule.class, toCheck);
            assertNotNull(find.getOntologyNode());
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
            assertEquals(col.getName() + " is missing ontology", mapping.getDefaultOntology(), col.getDefaultOntology());
            assertEquals(col.getName() + " is missing coding sheet", mapping.getDefaultCodingSheet(), col.getDefaultCodingSheet());
        }
    }

    public Object performActualIntegration(List<Long> tableIds, List<IntegrationColumn> integrationColumns,
            HashMap<Ontology, String[]> nodeSelectionMap) throws IOException {
        WorkspaceController controller = generateNewInitializedController(WorkspaceController.class);
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
        controller = generateNewInitializedController(WorkspaceController.class);
        controller.setTicketId(ticketId);
        controller.downloadIntegrationDataResults();
        InputStream integrationDataResultsInputStream = controller.getIntegrationDataResultsInputStream();
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

    public void assertArchiveContents(Collection<File> expectedFiles, File archive) throws IOException {
        assertArchiveContents(expectedFiles, archive, true);
    }

    public void assertArchiveContents(Collection<File> expectedFiles, File archive, boolean strict) throws IOException {

        Map<String, Long> nameSize = unzipArchive(archive);
        List<String> errs = new ArrayList<>();
        for (File expected : expectedFiles) {
            Long size = nameSize.get(expected.getName());
            if (size == null) {
                errs.add("expected file not in archive:" + expected.getName());
                continue;
            }
            // if doing a strict test, assert that file is exactly the same
            if (strict) {
                if (size.longValue() != expected.length()) {
                    errs.add(String.format("%s: item in archive %s does not have same content", size.longValue(), expected));
                }
                // otherwise, just make sure that the actual file is not empty
            } else {
                if (expected.length() > 0) {
                    assertThat(size, greaterThan(0L));
                }
            }
        }
        if (errs.size() > 0) {
            for (String err : errs) {
                logger.error(err);
            }
            fail("problems found in archive:" + archive);
        }
    }

    public Map<String, Long> unzipArchive(File archive) {
        Map<String, Long> files = new HashMap<>();
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(archive);
            for (Enumeration<?> e = zipfile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                files.put(entry.getName(), entry.getSize());
                logger.info("{} {}", entry.getName(), entry.getSize());
            }
        } catch (Exception e) {
            logger.error("Error while extracting file " + archive, e);
        } finally {
            if (zipfile != null) {
                IOUtils.closeQuietly(zipfile);
            }
        }
        return files;
    }

}
