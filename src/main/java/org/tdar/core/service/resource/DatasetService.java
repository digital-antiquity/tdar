package org.tdar.core.service.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.ResultSetIterator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.DataTableColumnRelationship;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DataTableDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.InformationResourceFileDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.DataIntegrationService;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.excel.SheetProxy;
import org.tdar.core.service.resource.dataset.DatasetUtils;
import org.tdar.core.service.resource.dataset.TdarDataResultSetExtractor;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.struts.data.FileProxy;
import org.tdar.struts.data.ResultMetadataWrapper;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.Pair;

/**
 * $Id$
 * 
 * Supporting methods for dealing with Datasets, importing, converting, etc.
 * 
 * @author Allen Lee
 * @version $Revision$
 */
@Service
public class DatasetService extends AbstractInformationResourceService<Dataset, DatasetDao> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TargetDatabase tdarDataImportDatabase;

    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private DataIntegrationService dataIntegrationService;

    @Autowired
    private InformationResourceFileDao informationResourceFileDao;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private XmlService xmlService;

    @Autowired
    private DataTableDao dataTableDao;

    /*
     * Translates a @link DataTableColumn based on the default 
     */
    @Transactional
    public void translate(DataTableColumn column) {
        translate(column, column.getDefaultCodingSheet());
    }

    /*
     * Given a @link DataTableColumn and a @link CodingSheet, translate the column using the @link CodingSheet's rules. This creates a new column for the
     * original data, and replaces the original data in the tdar_data database with the translated version
     */
    @Transactional
    public boolean translate(final DataTableColumn column, final CodingSheet codingSheet) {
        if (codingSheet == null) {
            return false;
        }
        getLogger().debug("translating {} with {}", column.getName(), codingSheet);
        // FIXME: if we eventually offer on-the-fly coding sheet translation we cannot modify the actual dataset in place
        tdarDataImportDatabase.translateInPlace(column, codingSheet);
        return true;
    }

    /*
     * Convenience method for untranslate, then translate using column.getDefaultCodingSheet()
     */
    @Transactional
    public boolean retranslate(DataTableColumn column) {
        untranslate(column);
        return translate(column, column.getDefaultCodingSheet());
    }

    /*
     * Convenience method for untranslate, then translate using column.getDefaultCodingSheet() for a collection of DataTableColumns
     */
    @Transactional
    public void retranslate(Collection<DataTableColumn> columns) {
        for (DataTableColumn column : columns) {
            retranslate(column);
        }
    }

    /*
     * Untranslate a coding sheet (remove the mapped data column for the coding sheet and then rename the column from the original to the name specified in the
     * @link DataTableColumn
     */
    @Transactional
    public void untranslate(DataTableColumn column) {
        tdarDataImportDatabase.untranslate(column);
    }

    /*
     * Convenience method for a set of @link DataTableColumn
     */
    @Transactional
    public void translate(Set<DataTableColumn> columns, final CodingSheet codingSheet) {
        for (DataTableColumn column : columns) {
            translate(column, codingSheet);
        }
    }

    /*
     * For a given @link Dataset, create a translated file which includes all of the columns that are mapped to a @link CodingSheet and their mapped values as well
     * as the code.  The translated version is stored on the @link InformationResourceFileVersion as a derivative
     */
    @SuppressWarnings("deprecation")
    @Transactional
    public InformationResourceFile createTranslatedFile(Dataset dataset) {
        // assumes that Datasets only have a single file
        Set<InformationResourceFile> activeFiles = dataset.getActiveInformationResourceFiles();
        InformationResourceFile file = null;
        if(!activeFiles.isEmpty()) {
            file = dataset.getActiveInformationResourceFiles().iterator().next();
        }

        if (file == null) {
            getLogger().warn("Trying to translate {} with a null file payload.", dataset);
            return null;
        }
        informationResourceFileDao.deleteTranslatedFiles(dataset);
        // FIXME: remove synchronize once Hibernate learns more about unique constraints
        // http://community.jboss.org/wiki/HibernateFAQ-AdvancedProblems#Hibernate_is_violating_a_unique_constraint

        //        getDao().synchronize();

        InformationResourceFile irFile = null;
        FileOutputStream translatedFileOutputStream = null;
        try {
            File tempFile = File.createTempFile("translated", ".xls", TdarConfiguration.getInstance().getTempDirectory());
            translatedFileOutputStream = new FileOutputStream(tempFile);
            SheetProxy sheetProxy = toExcel(dataset, translatedFileOutputStream);
            String filename = FilenameUtils.getBaseName(file.getLatestUploadedVersion().getFilename()) + "_translated." + sheetProxy.getExtension();
            FileProxy fileProxy = new FileProxy(filename, tempFile, VersionType.TRANSLATED, FileAction.ADD_DERIVATIVE);
            fileProxy.setRestriction(file.getRestriction());
            fileProxy.setFileId(file.getId());
            processMetadataForFileProxies(dataset, fileProxy);
            irFile = fileProxy.getInformationResourceFile();
        } catch (IOException exception) {
            getLogger().error("Unable to create translated file for Dataset: " + dataset, exception);
        } finally {
            IOUtils.closeQuietly(translatedFileOutputStream);
        }
        return irFile;
    }

    /**
     * Re-uploads the latest version of the data file for the given dataset.
     * FIXME: once message queue + message queue processor is in place we shouldn't need the noRollbackFor anymore
     * 
     * @param dataset
     */
    @Transactional(noRollbackFor = TdarRecoverableRuntimeException.class)
    public void reprocess(Dataset dataset) {
        logger.debug("Reprocessing {}", dataset);
        if (CollectionUtils.isEmpty(dataset.getInformationResourceFiles()))
            return;
        try {
            for (InformationResourceFile file : dataset.getActiveInformationResourceFiles()) {
                InformationResourceFileVersion latestUploadedVersion = file.getLatestUploadedVersion();
                File transientFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(ObjectType.RESOURCE, latestUploadedVersion);
                latestUploadedVersion.setTransientFile(transientFile);
            }

            getAnalyzer().processFile(dataset.getActiveInformationResourceFiles().toArray(new InformationResourceFile[0]));
        } catch (Exception e) {
            throw new TdarRecoverableRuntimeException(e);
        }
    }

    /*
     * For a given @link ResultSet and a @link DataTable this returns a list of Column names based on the display name instead of the internal table names
     */
    private List<String> getColumnNames(ResultSet resultSet, DataTable dataTable) throws SQLException {
        List<String> columnNames = new ArrayList<String>();
        ResultSetMetaData metadata = resultSet.getMetaData();
        for (int columnIndex = 0; columnIndex < metadata.getColumnCount(); columnIndex++) {
            String columnName = metadata.getColumnName(columnIndex + 1);
            // if (columnName.equals(DataTableColumn.TDAR_ROW_ID.getName())) {
            // continue;
            // }
            DataTableColumn column = dataTable.getColumnByName(columnName);
            if (column != null) {
                columnName = column.getDisplayName();
            }

            columnNames.add(columnName);
        }
        return columnNames;
    }

    /*
     * Converts a @link Dataset to a Microsoft Excel File; this includes the Translated data values
     */
    private SheetProxy toExcel(Dataset dataset, OutputStream outputStream) throws IOException {
        Set<DataTable> dataTables = dataset.getDataTables();
        if (dataTables == null || dataTables.isEmpty()) {
            return null;
        }
        final SheetProxy proxy = new SheetProxy();

        for (final DataTable dataTable : dataTables) {
            // each table becomes a sheet.
            String tableName = dataTable.getDisplayName();
            logger.debug(tableName);
            proxy.setName(tableName);
            ResultSetExtractor<Boolean> excelExtractor = new ResultSetExtractor<Boolean>() {
                @Override
                public Boolean extractData(ResultSet resultSet) throws SQLException {
                    List<String> headerLabels = getColumnNames(resultSet, dataTable);
                    proxy.setHeaderLabels(headerLabels);
                    proxy.setData(new ResultSetIterator(resultSet));
                    logger.debug("column names: " + headerLabels);
                    excelService.addSheets(proxy);
                    return true;
                }
            };
            tdarDataImportDatabase.selectAllFromTableInImportOrder(dataTable, excelExtractor, true);
        }
        proxy.getWorkbook().write(outputStream);
        return proxy;
    }

    /*
     * Checks whether a @link Dataset can be mapped to an @link Ontology and thus, whether specific CodingValues can be mapped to that Ontology
     */
    @Transactional(readOnly = true)
    public boolean canLinkDataToOntology(Dataset dataset) {
        return getDao().canLinkDataToOntology(dataset);
    }


    /*
     * When we import a @link Dataset, if there's an existing set of @link DataTable entries mapped to a Dataset, we reconcile each @link DataTable and @link
     * DataTableColunn on import such that if the old DataTables and Columns match the incomming, then we'll re-use the mappings. If they're different, their
     * either added or dropped respectively.
     */
    @SuppressWarnings("deprecation")
    @Transactional(noRollbackFor = TdarRecoverableRuntimeException.class)
    public void reconcileDataset(InformationResourceFile datasetFile, Dataset dataset, Dataset transientDatasetToPersist) {
        // helper Map to manage existing tables - all remaining entries in this existingTablesMap will be purged at the end of this process
        // take the dataset off the session at the last moment, and then bring it back on

        Collection<DataTable> tablesToRemove = reconcileTables(dataset, transientDatasetToPersist);
        reconcileRelationships(dataset, transientDatasetToPersist);

        cleanupUnusedTablesAndColumns(dataset, tablesToRemove);

        logger.debug("dataset: {} id: {}", dataset.getTitle(), dataset.getId());
        for (DataTable dataTable : dataset.getDataTables()) {
            logger.debug("dataTable: {}", dataTable);
            List<DataTableColumn> columns = dataTable.getDataTableColumns();
            logger.debug("dataTableColumns: {}", columns);
            for (DataTableColumn column : columns) {
                translate(column);
            }
        }
        datasetFile.setStatus(FileStatus.PROCESSED);
        datasetFile.setInformationResource(dataset);

        getDao().merge(dataset);
//        getDao().synchronize();
    }

    /*
     * Reconciles two @link Dataset entities together based on the transient entries coming from the @link WorkflowContext and the existing ones. First, it
     * tries to match name-by-name. Second, if there is "just" a in both, eg. in a CSV, TAB, or other Format, then don't match on name, assume that they're the
     * same table, as table name was generated by us instead of the user.
     */
    private Collection<DataTable> reconcileTables(Dataset dataset, Dataset transientDatasetToPersist) {
        HashMap<String, DataTable> existingTablesMap = new HashMap<String, DataTable>();
        for (DataTable existingDataTable : dataset.getDataTables()) {
            existingTablesMap.put(existingDataTable.getInternalName(), existingDataTable);
            logger.debug("existingTableName: {}", existingDataTable.getInternalName());
        }
        dataset.getDataTables().clear();
        logger.debug("Existing name to table map: {}", existingTablesMap);
        for (DataTable tableToPersist : transientDatasetToPersist.getDataTables()) {
            // first check that the incoming data table has data table columns.
            String internalTableName = tableToPersist.getInternalName();
            DataTable existingTable = existingTablesMap.get(internalTableName);
            if (existingTable == null && existingTablesMap.size() == 1 && transientDatasetToPersist.getDataTables().size() == 1) {
                // the table names did not match, but we have one incoming table and one existing table. Try to match them regardless.
                existingTable = existingTablesMap.values().iterator().next();
            }

            if (existingTable != null) {
                existingTablesMap.remove(existingTable.getInternalName());
                tableToPersist = reconcileDataTable(dataset, existingTable, tableToPersist);
            } else {
                // continue with the for loop, tableToPersist does not require any metadata merging because
                // we can't find an existing table to merge it with
                logger.trace("No analogous existing table to merge with incoming data table {}, moving on", tableToPersist);
            }
            tableToPersist.setDataset(dataset);
            dataset.getDataTables().add(tableToPersist);
        }

        // any tables left in existingTables didn't have an analog in the incoming dataset, so clean them up
        Collection<DataTable> tablesToRemove = existingTablesMap.values();
        return tablesToRemove;
    }

    /*
     * Reconciles DataTableRelationships between two datasets, this is not well supported at the moment.
     */
    private void reconcileRelationships(Dataset dataset, Dataset transientDatasetToPersist) {
        // refresh the column relationships so that they refer to new versions of the columns which have the same names as the old columns
        dataset.getRelationships().clear();

        for (DataTableRelationship rel : transientDatasetToPersist.getRelationships()) {
            dataset.getRelationships().add(rel);
        }
    }

    /*
     * Iterate through each @link DataTableColumn on the @link DataTable and reconcile them by name.
     */
    private DataTable reconcileDataTable(Dataset dataset, DataTable existingTable, DataTable tableToPersist) {
        if (CollectionUtils.isNotEmpty(tableToPersist.getDataTableColumns())) {
            // if there is an analogous existing table, try to reconcile all the columns from the incoming data table
            // with the columns from the existing data table.
            HashMap<String, DataTableColumn> existingColumnsMap = new HashMap<String, DataTableColumn>();
            for (DataTableColumn existingColumn : existingTable.getDataTableColumns()) {
                existingColumnsMap.put(existingColumn.getName().toLowerCase().trim(), existingColumn);
            }
            logger.debug("existing columns: {}", existingColumnsMap);
            List<DataTableColumn> columnsToPersist = tableToPersist.getDataTableColumns();
            // for each incoming data table column, try to match it with an equivalent column
            // from existingTable using the existingNameToColumnMap
            for (int i = 0; i < columnsToPersist.size(); i++) {
                DataTableColumn incomingColumn = columnsToPersist.get(i);
                String normalizedColumnName = incomingColumn.getName().toLowerCase().trim();
                DataTableColumn existingColumn = existingColumnsMap.get(normalizedColumnName);
                logger.debug("Reconciling existing {} with incoming column {}", existingColumn, incomingColumn);
                reconcileColumn(tableToPersist, existingColumnsMap, normalizedColumnName, incomingColumn, existingColumn);
            }

            logger.debug("deleting unmerged columns: {}", existingColumnsMap);
            logger.debug("result: {}", columnsToPersist);
            tableToPersist.setId(existingTable.getId());

            // get rid of all the old existing columns that don't have an analogous column (by name)
            Collection<DataTableColumn> columnsToRemove = existingColumnsMap.values();

            getDao().unmapAllColumnsInProject(dataset.getProject(), columnsToRemove);

            logger.debug("merged data table is now {}", tableToPersist);
            logger.debug("actual data table columns {}, incoming data table columns {}", tableToPersist.getDataTableColumns(), columnsToPersist);
        }
        return tableToPersist;
    }

    /**
     * Using the existing column map, we try and find a matching @link DataTableColumn, if we do, we copy the values off of the 
     * existing column before returning.
     * 
     * @param incomingTable
     * @param existingNameToColumnMap
     * @param normalizedColumnName
     * @param incomingColumn
     * @param existingColumn
     * @return
     */
    @Transactional
    private void reconcileColumn(DataTable incomingTable, HashMap<String, DataTableColumn> existingNameToColumnMap,
            String normalizedColumnName, DataTableColumn incomingColumn, DataTableColumn existingColumn) {
        // FIXME: check that types are compatible before merging

        if (existingColumn == null) {
            return;
        }
        /*
         * if we've gotten this far, we know that the incoming column should be saved onto the existing table instead of the transient table that it was
         * originally set on. copy all values that should be retained
         */
        logger.trace("Merging incoming column with existing column");
        incomingColumn.setDataTable(incomingTable);
        incomingColumn.setId(existingColumn.getId());
        incomingColumn.setDefaultCodingSheet(existingColumn.getDefaultCodingSheet());
        incomingColumn.setDefaultOntology(existingColumn.getDefaultOntology());

        incomingColumn.setCategoryVariable(existingColumn.getCategoryVariable());

        incomingColumn.copyUserMetadataFrom(existingColumn);
        existingNameToColumnMap.remove(normalizedColumnName);
    }

    /*
     * Convenience method to drop all of the tdardata database tables associated with a dataset.
     */
    @SuppressWarnings("unused")
    private void dropDatasetTables(Dataset dataset) {
        for (DataTable dataTable : dataset.getDataTables()) {
            tdarDataImportDatabase.dropTable(dataTable.getName());
        }
    }

    /*
     * Log the DataTableColumn Information to XML to be stored in the ResourceRevisionLog
     */
    public void logDataTableColumns(DataTable dataTable, String message, Person authenticatedUser) {
        try {
            StringWriter writer = new StringWriter();
            xmlService.convertToXML(dataTable, writer);
            resourceService.logResourceModification(dataTable.getDataset(), authenticatedUser, message, writer.toString());
            logger.trace("{} - xml {}", message, writer);
        } catch (Exception e) {
            logger.error("could not serialize to XML:", e);
        }
    }

    /*
     * Takes a Coding Table within a larger data set and converts it to a tDAR CodingSheet
     */
    @Transactional
    public CodingSheet convertTableToCodingSheet(Person user, final DataTableColumn keyColumn, final DataTableColumn valueColumn,
            final DataTableColumn descriptionColumn) {
        final CodingSheet codingSheet = new CodingSheet();
        codingSheet.markUpdated(user);
        codingSheet.setTitle("Generated Coding Rule from " + keyColumn.getDataTable().getName());
        codingSheet.setDescription(codingSheet.getTitle());
        codingSheet.setDate(Calendar.getInstance().get(Calendar.YEAR));
        getDao().save(codingSheet);
        ResultSetExtractor<Set<CodingRule>> resultSetExtractor = new ResultSetExtractor<Set<CodingRule>>() {
            @Override
            public Set<CodingRule> extractData(ResultSet resultSet)
                    throws SQLException {
                Set<CodingRule> rules = new HashSet<CodingRule>();
                ResultSetMetaData metadata = resultSet.getMetaData();
                int columns = metadata.getColumnCount();
                while (resultSet.next()) {
                    CodingRule rule = new CodingRule();
                    for (int columnIndex = 0; columnIndex < columns; columnIndex++) {
                        String columnName = metadata.getColumnName(columnIndex + 1);
                        Object result = resultSet.getObject(columnIndex + 1);
                        String value = "";
                        if (result != null) {
                            value = result.toString();
                        }
                        if (columnName.equals(keyColumn.getName())) {
                            rule.setCodingSheet(codingSheet);
                            rule.setCode(value);
                        } else if (columnName.equals(valueColumn.getName())) {
                            rule.setTerm(value);
                        } else if (descriptionColumn != null && columnName.equals(descriptionColumn.getName())) {
                            rule.setDescription(value);
                        }
                    }
                    if (!StringUtils.isEmpty(rule.getCode()) && !StringUtils.isEmpty(rule.getTerm())) {
                        rules.add(rule);
                    }
                }
                return rules;
            }
        };
        @SuppressWarnings("deprecation")
        Set<CodingRule> codingRules = tdarDataImportDatabase.selectAllFromTable(keyColumn.getDataTable(), resultSetExtractor, false);
        codingSheet.getCodingRules().addAll(codingRules);
        getDao().save(codingRules);
        return codingSheet;
    }

    /*
     * Find all Rows within a @link DataTable with Pagination.  Used to browse a Data Table
     */
    @SuppressWarnings("deprecation")
    @Transactional
    public ResultMetadataWrapper selectAllFromDataTable(final DataTable dataTable, final int start, final int page, boolean includeGenerated,
            final boolean returnRowId) {
        final ResultMetadataWrapper wrapper = new ResultMetadataWrapper();
        wrapper.setRecordsPerPage(page);
        wrapper.setStartRecord(start);

        ResultSetExtractor<List<List<String>>> resultSetExtractor = new TdarDataResultSetExtractor(wrapper, start, page, dataTable, returnRowId);
        try {
            wrapper.setResults(tdarDataImportDatabase.selectAllFromTableInImportOrder(dataTable, resultSetExtractor, includeGenerated));
        } catch (BadSqlGrammarException e) {
            logger.trace("order column did not exist", e);
            wrapper.setResults(tdarDataImportDatabase.selectAllFromTable(dataTable, resultSetExtractor, includeGenerated));
        }
        return wrapper;
    }

    /*
     * Extracts a specific Row of data from a tdardata database and returns a map object with it's contents pre-mapped to @link DataTableColumn entries
     */
    @Transactional
    public Map<DataTableColumn, String> selectRowFromDataTable(final DataTable dataTable, final Long rowId, final boolean returnRowId) {
        ResultSetExtractor<Map<DataTableColumn, String>> resultSetExtractor = new ResultSetExtractor<Map<DataTableColumn, String>>() {

            @Override
            public Map<DataTableColumn, String> extractData(ResultSet rs) throws SQLException {
                Map<DataTableColumn, String> result = new HashMap<>();
                while (rs.next()) {
                    result = DatasetUtils.convertResultSetRowToDataTableColumnMap(dataTable, rs, returnRowId);
                }
                return result;
            }

        };
        return tdarDataImportDatabase.selectRowFromTable(dataTable, resultSetExtractor, rowId);

    }

    /*
     * Finds a set of Database rows from the TdarMetadata database that are associated with the String specified, and wraps them in a @link ResultsMetadataWrapper 
     */
    @Transactional
    public ResultMetadataWrapper findRowsFromDataTable(final DataTable dataTable, final int start, final int page, boolean includeGenerated, String query) {
        final ResultMetadataWrapper wrapper = new ResultMetadataWrapper();
        wrapper.setRecordsPerPage(page);
        wrapper.setStartRecord(start);

        ResultSetExtractor<List<List<String>>> resultSetExtractor = new TdarDataResultSetExtractor(wrapper, start, page, dataTable,false);
        try {
            wrapper.setResults(tdarDataImportDatabase.selectAllFromTable(dataTable, resultSetExtractor, includeGenerated, query));
        } catch (BadSqlGrammarException e) {
            logger.trace("order column did not exist", e);
        }
        return wrapper;
    }


    /*
     * Extracts out all @link DataTableRelationship entries for a @link DataTableColumn.
     */
    @Transactional
    public List<DataTableRelationship> listRelationshipsForColumns(DataTableColumn column) {
        List<DataTableRelationship> relationships = new ArrayList<>();
        Set<DataTableRelationship> allDatasetRelationships = column.getDataTable().getDataset().getRelationships();
        logger.trace("All relationships: {}", allDatasetRelationships);
        for (DataTableRelationship relationship : allDatasetRelationships) {
            for (DataTableColumnRelationship columnRelationship : relationship.getColumnRelationships()) {
                if (column.equals(columnRelationship.getLocalColumn())) {
                    relationships.add(relationship);
                }
            }
        }
        return relationships;
    }

    /*
     * Updates the transient Mapped Data for an @link InformationResource based on the linked @link DataTableColumn and data row in the tdar data database. The
     * row entry will be loaded into the Map<> entry on the InformationResource so it can be indexed and displayed on the View layer
     */
    @Transactional
    public void assignMappedDataForInformationResource(InformationResource resource) {
        String key = resource.getMappedDataKeyValue();
        DataTableColumn column = resource.getMappedDataKeyColumn();
        if (StringUtils.isBlank(key) || column == null) {
            return;
        }
        final DataTable table = column.getDataTable();
        ResultSetExtractor<Map<DataTableColumn, String>> resultSetExtractor = new ResultSetExtractor<Map<DataTableColumn, String>>() {
            @Override
            public Map<DataTableColumn, String> extractData(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    Map<DataTableColumn, String> results = DatasetUtils.convertResultSetRowToDataTableColumnMap(table, rs, false);
                    return results;
                }
                return null;
            }

        };

        Map<DataTableColumn, String> dataTableQueryResults = tdarDataImportDatabase.selectAllFromTable(column, key, resultSetExtractor);
        resource.setRelatedDatasetData(dataTableQueryResults);
    }

    /*
     * Based on a set of @link DataTableColumn entries, and a @link Project we can will clear out the existing mappings; and then identify mappings that need to
     * be made.
     */
    @Transactional
    public List<DataTableColumn> prepareAndFindMappings(Project project, Collection<DataTableColumn> columns) {
        List<DataTableColumn> columnsToMap = new ArrayList<DataTableColumn>();
        if (CollectionUtils.isEmpty(columns)) {
            return columnsToMap;
        }
        if (project == Project.NULL) {
            throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("datasetService.no_project_specified"));
        }
        getDao().unmapAllColumnsInProject(project, columns);
        for (DataTableColumn column : columns) {
            logger.info("mapping dataset to resources using column: {} ", column);
            Dataset dataset = column.getDataTable().getDataset();
            if (dataset == null) {
                throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("datasetService.dataset_null_column",column));
                }
            else if (ObjectUtils.notEqual(project, dataset.getProject())) {
                throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("datasetService.dataset_different_project", project, dataset.getProject()));
            }
            if (column.isMappingColumn()) {
                columnsToMap.add(column);
                // FIXME: could add custom logic to add a backpointer (new column) to DB that has "mapped" set to true based on updatedValues
            }
        }
        return columnsToMap;
    }

    /*
     * Finds all Dataset Ids
     */
    public List<Long> findAllIds() {
        return getDao().findAllIds();
    }

    /*
     * convenience method, used for Asynchronous as opposed to the Synchronous version by the Controller
     */
    @Async
    @Transactional
    public void remapColumnsAsync(final List<DataTableColumn> columns, final Project project) {
        remapColumns(columns, project);
    }
    
    /*
     * A special feature of a @link Dataset is if it's associated with a @link Project, we can use data from a @link DataTable to associate additional data with
     * other resources in the project, e.g. a database of images. The mapping here is created using a field in the column that contains the filename of the file
     * to be mapped, and is associated with the filename associated with @InformationResourceFileVersion of any @link Resource in that @link Project.
     */
    @Transactional
    public void remapColumns(List<DataTableColumn> columns, Project project) {
        logger.info("remapping columns: {} in {} ", columns, project);
        if (CollectionUtils.isNotEmpty(columns) && project != null) {
            // mapping columns to the resource runs a raw sql update, refresh the state of the Project.
            getDao().refresh(project);
            // have to reindex...
            /*
             * Take the distinct column values mapped and associate them with files in tDAR based on:
             * - shared project
             * - filename matches column value either (a) with extension or (b) with separator eg: file1.jpg;file2.jpg
             * NOTE: a manual reindex happens at the end
             */
            for (DataTableColumn column : columns) {
                getDao().mapColumnToResource(column, tdarDataImportDatabase.selectNonNullDistinctValues(column));
            }
        }
        if (project != null) {
            searchIndexService.indexProject(project);
        }
    }

    /*
     * Takes an existing @link Dataset and @link DataTable, and an incoming list of @link DataTableColumn entries, from the edit-column-metadata function in
     * tDAR, iterate through each incoming DataTableColumn and update the real entries in the database. Once updated, re-translate, map, and other changes as
     * necessary.
     */
    @Transactional
    public Pair<Boolean, List<DataTableColumn>> updateColumnMetadata(Dataset dataset, DataTable dataTable, List<DataTableColumn> dataTableColumns,
            Person authenticatedUser) {
        boolean hasOntologies = false;
        Pair<Boolean, List<DataTableColumn>> toReturn = new Pair<Boolean, List<DataTableColumn>>(hasOntologies, new ArrayList<DataTableColumn>());
        List<DataTableColumn> columnsToTranslate = new ArrayList<DataTableColumn>();
        List<DataTableColumn> columnsToMap = new ArrayList<DataTableColumn>();
        for (DataTableColumn incomingColumn : dataTableColumns) {
            boolean needToRemap = false;
            logger.debug("incoming data table column: {}", incomingColumn);
            DataTableColumn existingColumn = dataTable.getColumnById(incomingColumn.getId());
            if (existingColumn == null) {
                existingColumn = dataTable.getColumnByName(incomingColumn.getName());
                if (existingColumn == null) {
                    throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("datasetService.could_not_find_column", incomingColumn.getName(),
                            incomingColumn.getId()));
                }
            }
            CodingSheet incomingCodingSheet = incomingColumn.getDefaultCodingSheet();
            CodingSheet existingCodingSheet = existingColumn.getDefaultCodingSheet();
            Ontology defaultOntology = null;
            if (!Base.isNullOrTransient(incomingCodingSheet)) {
                // load the full hibernate entity and set it back on the incoming column
                incomingCodingSheet = getDao().find(CodingSheet.class, incomingCodingSheet.getId());
                incomingColumn.setDefaultCodingSheet(incomingCodingSheet);
                if (incomingCodingSheet.getDefaultOntology() != null) {
                    // ALWAYS defer to the CodingSheet's ontology if a coding sheet is set. Otherwise
                    // we run into conflicts when you specify both a coding sheet AND an ontology for a given DTC
                    defaultOntology = incomingCodingSheet.getDefaultOntology();
                }
            }
            if (defaultOntology == null) {
                // check if the incoming column had an ontology set
                defaultOntology = getDao().loadFromSparseEntity(incomingColumn.getDefaultOntology(), Ontology.class);
            }
            logger.debug("default ontology: {}", defaultOntology);
            logger.debug("incoming coding sheet: {}", incomingCodingSheet);
            incomingColumn.setDefaultOntology(defaultOntology);
            if (defaultOntology != null && Base.isNullOrTransient(incomingCodingSheet)) {
                incomingColumn.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
                CodingSheet generatedCodingSheet = dataIntegrationService.createGeneratedCodingSheet(existingColumn, authenticatedUser,
                        defaultOntology);
                incomingColumn.setDefaultCodingSheet(generatedCodingSheet);
                getLogger().debug("generated coding sheet {} for {}", generatedCodingSheet, incomingColumn);
            }
            // FIXME: can we simplify this logic? Perhaps push into DataTableColumn?
            // incoming ontology or coding sheet from the web was not null but the column encoding type was set to something that
            // doesn't support either, we set it to null
            // incoming ontology or coding sheet is explicitly set to null
            if (!Base.isNullOrTransient(defaultOntology)) {
                if (incomingColumn.getColumnEncodingType().isSupportsOntology()) {
                    hasOntologies = true;
                }
                else {
                    incomingColumn.setDefaultOntology(null);
                    logger.debug("column {} doesn't support ontologies - setting default ontology to null", incomingColumn);
                }
            }
            if (incomingColumn.getDefaultCodingSheet() != null && !incomingColumn.getColumnEncodingType().isSupportsCodingSheet()
                    && defaultOntology == null) {
                incomingColumn.setDefaultCodingSheet(null);
                logger.debug("column encoding type didn't support coding sheets - setting default coding sheet to null on column {} (encoding type: {})",
                        incomingColumn, incomingColumn.getColumnEncodingType());
            }

            existingColumn.setDefaultOntology(incomingColumn.getDefaultOntology());
            existingColumn.setDefaultCodingSheet(incomingColumn.getDefaultCodingSheet());

            existingColumn.setCategoryVariable(getDao().loadFromSparseEntity(incomingColumn.getCategoryVariable(), CategoryVariable.class));
            CategoryVariable subcategoryVariable = getDao().loadFromSparseEntity(incomingColumn.getTempSubCategoryVariable(),
                    CategoryVariable.class);

            if (subcategoryVariable != null) {
                existingColumn.setCategoryVariable(subcategoryVariable);
            }
            // check if values have changed
            needToRemap = existingColumn.hasDifferentMappingMetadata(incomingColumn);
            // copy off all of the values that can be directly copied from the bean
            existingColumn.copyUserMetadataFrom(incomingColumn);
            if (!existingColumn.isValid()) {
                throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("datasetService.invalid_column",existingColumn));
            }

            if (needToRemap) {
                logger.debug("remapping {}", existingColumn);
                columnsToMap.add(existingColumn);
            }
            // if there is a change in coding sheet a column may need to be retranslated or untranslated.
            if (isRetranslationNeeded(incomingCodingSheet, existingCodingSheet)) {
                logger.debug("retranslating {} for incoming coding sheet {}", existingColumn, incomingCodingSheet);
                columnsToTranslate.add(existingColumn);
            }
            logger.trace("{}", existingColumn);
            getDao().update(existingColumn);
        }
        dataset.markUpdated(authenticatedUser);
        toReturn.getSecond().addAll(prepareAndFindMappings(dataset.getProject(), columnsToMap));
        save(dataset);
        if (!columnsToTranslate.isEmpty()) {
            // create the translation file for this dataset.
            logger.debug("creating translated file");
            retranslate(columnsToTranslate);
            createTranslatedFile(dataset);
        }
        logDataTableColumns(dataTable, "data column metadata registration", authenticatedUser);
        logger.info("hasOntology: {} , mappingColumns: {} ", toReturn.getFirst(), toReturn.getSecond());
        return toReturn;
    }

    /*
     * Checks whether based on an incoming and an existing @link CodingSheet, whether retranslation is necessary
     */
    private boolean isRetranslationNeeded(CodingSheet incomingCodingSheet, CodingSheet existingCodingSheet) {
        logger.info("{} {} {}", incomingCodingSheet, existingCodingSheet, ObjectUtils.equals(incomingCodingSheet, existingCodingSheet));
        if (ObjectUtils.equals(incomingCodingSheet, existingCodingSheet)) {
            return false;
        }
        else if (incomingCodingSheet.isGenerated()) {
            return existingCodingSheet != null;
        }
        else {
            return true;
        }
    }


    /*
     * Each @link CodingSheet is mapped to one or many @link Dataset records. Because of this, when we re-map a @link CodingSheet to a @link Ontology, we need
     * to retranslate each of the @link Dataset records
     */
    @Transactional
    public void refreshAssociatedDataTables(CodingSheet codingSheet) {
        // retranslate associated datatables, and recreate translated files
        Set<DataTableColumn> associatedDataTableColumns = codingSheet.getAssociatedDataTableColumns();
        if (CollectionUtils.isEmpty(associatedDataTableColumns)) {
            return;
        }

        translate(associatedDataTableColumns, codingSheet);
        for (DataTable dataTable : dataTableDao.findDataTablesUsingResource(codingSheet)) {
            createTranslatedFile(dataTable.getDataset());
        }
    }

    /*
     * Exposes the @link DataTable as xml using the postgres xml format.
     * 
     * http://www.postgresql.org/docs/9.1/static/functions-xml.html
     */
    @Transactional
    public String selectTableAsXml(DataTable dataTable) {
        return tdarDataImportDatabase.selectTableAsXml(dataTable);
    }

    /*
     * Setter for the tdardata postgres database which is not managed by hibernate
     */
    public void setTdarDataImportDatabase(PostgresDatabase tdarDataImportDatabase) {
        this.tdarDataImportDatabase = tdarDataImportDatabase;
    }


}
