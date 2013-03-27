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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.DataTableColumnRelationship;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.DataIntegrationService;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.excel.SheetProxy;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.data.FileProxy;
import org.tdar.struts.data.ResultMetadataWrapper;
import org.tdar.utils.Pair;

/**
 * $Id$
 * 
 * @author Allen Lee
 * @version $Revision$
 */
@Service
public class DatasetService extends AbstractInformationResourceService<Dataset, DatasetDao> {

    @Autowired
    private TargetDatabase tdarDataImportDatabase;

    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private DataIntegrationService dataIntegrationService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private XmlService xmlService;

    @Autowired
    private FileAnalyzer fileAnalyzer;

    @Transactional
    public void translate(DataTableColumn column) {
        translate(column, column.getDefaultCodingSheet());
    }

    @Transactional
    public boolean translate(final DataTableColumn column, final CodingSheet codingSheet) {
        if (codingSheet == null) {
            return false;
        }
        getLogger().debug("translating {} with {}", column.getName(), codingSheet);
        // FIXME: if we eventually offer on-the-fly coding sheet translation we cannot
        // modify the actual dataset in place
        tdarDataImportDatabase.translateInPlace(column, codingSheet);
        return true;
    }

    @Transactional
    public boolean retranslate(DataTableColumn column) {
        untranslate(column);
        return translate(column, column.getDefaultCodingSheet());
    }

    @Transactional
    public void retranslate(Collection<DataTableColumn> columns) {
        for (DataTableColumn column : columns) {
            retranslate(column);
        }
    }

    @Transactional
    public void untranslate(DataTableColumn column) {
        tdarDataImportDatabase.untranslate(column);
    }

    @Transactional
    public void translate(Set<DataTableColumn> columns, final CodingSheet codingSheet) {
        for (DataTableColumn column : columns) {
            translate(column, codingSheet);
        }
    }

    @SuppressWarnings("deprecation")
    @Transactional
    public InformationResourceFile createTranslatedFile(Dataset dataset) {
        // assumes that Datasets only have a single file
        InformationResourceFile file = dataset.getFirstInformationResourceFile();
        if (file == null) {
            getLogger().warn("Trying to translate {} with a null file payload.", dataset);
            return null;
        }
        getInformationResourceFileService().deleteTranslatedFiles(file);

        // FIXME: remove synchronize once Hibernate learns more about unique constraints
        // http://community.jboss.org/wiki/HibernateFAQ-AdvancedProblems#Hibernate_is_violating_a_unique_constraint
        getDao().synchronize();

        InformationResourceFile processedFileProxy = null;
        FileOutputStream translatedFileOutputStream = null;
        try {
            File tempFile = File.createTempFile("translated", ".xls");
            translatedFileOutputStream = new FileOutputStream(tempFile);
            SheetProxy sheetProxy = toExcel(dataset, translatedFileOutputStream);
            String filename = FilenameUtils.getBaseName(file.getLatestUploadedVersion().getFilename()) + "_translated." + sheetProxy.getExtension();
            FileProxy fileProxy = new FileProxy(filename, tempFile, VersionType.TRANSLATED, FileAction.ADD_DERIVATIVE);
            fileProxy.setRestriction(file.getRestriction());
            fileProxy.setFileId(file.getId());
            processedFileProxy = processFileProxy(dataset, fileProxy);
        } catch (IOException exception) {
            getLogger().error("Unable to create translated file for Dataset: " + dataset, exception);
        } finally {
            IOUtils.closeQuietly(translatedFileOutputStream);
        }
        return processedFileProxy;
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
            fileAnalyzer.processFile(dataset.getFirstInformationResourceFile());
        } catch (Exception e) {
            throw new TdarRecoverableRuntimeException(e);
        }
    }

    public List<String> getColumnNames(ResultSet resultSet, DataTable dataTable) throws SQLException {
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
                public Boolean extractData(ResultSet resultSet) throws SQLException, DataAccessException {
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

    @Transactional(readOnly = true)
    public boolean canLinkDataToOntology(Dataset dataset) {
        return getDao().canLinkDataToOntology(dataset);
    }

    @Transactional(noRollbackFor = TdarRecoverableRuntimeException.class)
    public void reconcileDataset(InformationResourceFile datasetFile, Dataset dataset, Dataset transientDatasetToPersist) {
        // helper Map to manage existing tables - all remaining entries in this existingTablesMap will be purged at the end of this process
        // take the dataset off the session at the last moment, and then bring it back on

        Collection<DataTable> tablesToRemove = reconcileTables(dataset, transientDatasetToPersist);
        reconcileRelationships(dataset, transientDatasetToPersist);

        cleanupUnusedTablesAndColumns(dataset, tablesToRemove);
        // getDao().detachFromSession(dataset);

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
        // getDao().saveOrUpdate(datasetFile);

        // FIXME:: the merge doesn't work here because of access to the authorized user on the session
        // Person p = getDao().find(Person.class, dataset.getUpdatedBy().getId());
        // dataset.markUpdated(p);

        getDao().merge(dataset);
        getDao().synchronize();
    }

    private void cleanupUnusedTablesAndColumns(Dataset dataset, Collection<DataTable> tablesToRemove) {
        logger.info("deleting unmerged tables: {}", tablesToRemove);
        ArrayList<DataTableColumn> columnsToUnmap = new ArrayList<DataTableColumn>();
        for (DataTable table : tablesToRemove) {
            columnsToUnmap.addAll(table.getDataTableColumns());
        }
        // first unmap all columns from the removed tables
        getDao().unmapAllColumnsInProject(dataset.getProject(), columnsToUnmap);
        dataset.getDataTables().removeAll(tablesToRemove);
    }

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

    private void reconcileRelationships(Dataset dataset, Dataset transientDatasetToPersist) {
        // refresh the column relationships so that they refer to new versions of the columns which have the same names as the old columns
        dataset.getRelationships().clear();

        for (DataTableRelationship rel : transientDatasetToPersist.getRelationships()) {
            dataset.getRelationships().add(rel);
        }
    }

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
     * Returns either the incoming column without any changes or the result of merging the incoming column
     * with the existing column.
     * 
     * @param existingTable
     * @param existingNameToColumnMap
     * @param normalizedColumnName
     * @param incomingColumn
     * @param existingColumn
     * @return
     */
    @Transactional
    private void reconcileColumn(DataTable incomingTable,
            HashMap<String, DataTableColumn> existingNameToColumnMap,
            String normalizedColumnName, DataTableColumn incomingColumn,
            DataTableColumn existingColumn) {
        // FIXME: check that types are compatible before merging

        if (existingColumn != null) {
            // if we've gotten this far, we know that the incoming column
            // should be saved onto the existing table instead of the transient table that it was
            // originally set on.
            // copy all values that should be retained
            logger.trace("Merging incoming column with existing column");
            incomingColumn.setDataTable(incomingTable);
            incomingColumn.setId(existingColumn.getId());
            incomingColumn.setDefaultCodingSheet(existingColumn.getDefaultCodingSheet());
            incomingColumn.setDefaultOntology(existingColumn.getDefaultOntology());

            incomingColumn.setCategoryVariable(existingColumn.getCategoryVariable());

            incomingColumn.copyUserMetadataFrom(existingColumn);
            existingNameToColumnMap.remove(normalizedColumnName);
        }
    }

    @SuppressWarnings("unused")
    private void dropDatasetTables(Dataset dataset) {
        for (DataTable dataTable : dataset.getDataTables()) {
            tdarDataImportDatabase.dropTable(dataTable.getName());
        }
    }

    public void setTdarDataImportDatabase(PostgresDatabase tdarDataImportDatabase) {
        this.tdarDataImportDatabase = tdarDataImportDatabase;
    }

    public void serializeDataValueOntologyNodeMapping(DataTableColumn dataTableColumn, Person authenticatedUser) {
        try {
            StringWriter writer = new StringWriter();
            xmlService.convertToXML(dataTableColumn, writer);
            resourceService.logResourceModification(dataTableColumn.getDataTable().getDataset(), authenticatedUser, "saveDataValueOntologyNodeMapping",
                    writer.toString());
            getLogger().debug("--saveDataValueOntologyNodeMapping--\n{}", writer.toString());
        } catch (Exception e) {
            logger.error("could not serialize to XML:", e);
        }
    }

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
                    throws SQLException, DataAccessException {
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
        Set<CodingRule> codingRules = tdarDataImportDatabase.selectAllFromTable(keyColumn.getDataTable(), resultSetExtractor, false);
        codingSheet.getCodingRules().addAll(codingRules);
        getDao().save(codingRules);
        return codingSheet;
    }

    @Transactional
    public ResultMetadataWrapper selectAllFromDataTable(final DataTable dataTable, final int start, final int page, boolean includeGenerated) {
        final ResultMetadataWrapper wrapper = new ResultMetadataWrapper();
        wrapper.setRecordsPerPage(page);
        wrapper.setStartRecord(start);

        ResultSetExtractor<List<List<String>>> resultSetExtractor = new ResultSetExtractor<List<List<String>>>() {
            @Override
            public List<List<String>> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<List<String>> results = new ArrayList<List<String>>();
                int rowNum = 1;
                while (rs.next()) {
                    Map<DataTableColumn, String> result = convertResultSetRowToDataTableColumnMap(dataTable, rs);
                    if (rs.isFirst()) {
                        wrapper.setFields(new ArrayList<DataTableColumn>(result.keySet()));
                    }

                    if (rowNum > start && rowNum <= start + page) {
                        ArrayList<String> values = new ArrayList<String>();
                        for (DataTableColumn col : wrapper.getFields()) {
                            if (col.isVisible()) {
                                values.add(result.get(col));
                            }
                        }
                        results.add(values);
                    }
                    rowNum++;

                    if (rs.isLast()) {
                        wrapper.setTotalRecords(rs.getRow());
                    }
                }
                return results;
            }
        };
        try {
            wrapper.setResults(tdarDataImportDatabase.selectAllFromTableInImportOrder(dataTable, resultSetExtractor, includeGenerated));
        } catch (BadSqlGrammarException e) {
            logger.trace("order column did not exist", e);
            wrapper.setResults(tdarDataImportDatabase.selectAllFromTable(dataTable, resultSetExtractor, includeGenerated));
        }
        return wrapper;
    }


    @Transactional
    public ResultMetadataWrapper selectFromDataTable(final DataTable dataTable, final int start, final int page, boolean includeGenerated, String query) {
        final ResultMetadataWrapper wrapper = new ResultMetadataWrapper();
        wrapper.setRecordsPerPage(page);
        wrapper.setStartRecord(start);

        ResultSetExtractor<List<List<String>>> resultSetExtractor = new TdarDataResultSetExtractor(wrapper, start, page, dataTable);
        try {
            wrapper.setResults(tdarDataImportDatabase.selectAllFromTable(dataTable, resultSetExtractor, includeGenerated, query));
        } catch (BadSqlGrammarException e) {
            logger.trace("order column did not exist", e);
        }
        return wrapper;
    }
    private final class TdarDataResultSetExtractor implements ResultSetExtractor<List<List<String>>> {
        private final ResultMetadataWrapper wrapper;
        private final int start;
        private final int page;
        private final DataTable dataTable;

        private TdarDataResultSetExtractor(ResultMetadataWrapper wrapper, int start, int page, DataTable dataTable) {
            this.wrapper = wrapper;
            this.start = start;
            this.page = page;
            this.dataTable = dataTable;
        }

        @Override
        public List<List<String>> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<List<String>> results = new ArrayList<List<String>>();
            int rowNum = 1;
            while (rs.next()) {
                Map<DataTableColumn, String> result = DatasetService.convertResultSetRowToDataTableColumnMap(dataTable, rs);
                if (rs.isFirst()) {
                    wrapper.setFields(new ArrayList<DataTableColumn>(result.keySet()));
                }

                if (rowNum > start && rowNum <= start + page) {
                    ArrayList<String> values = new ArrayList<String>();
                    for (DataTableColumn col : wrapper.getFields()) {
                        if (col.isVisible()) {
                            values.add(result.get(col));
                        }
                    }
                    results.add(values);
                }
                rowNum++;

                if (rs.isLast()) {
                    wrapper.setTotalRecords(rs.getRow());
                }
            }
            return results;
        }
    }
    
    @Transactional
    public List<DataTableRelationship> listRelationshipsForColumns(DataTableColumn column) {
        List<DataTableRelationship> relationships = new ArrayList<DataTableRelationship>();
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
            public Map<DataTableColumn, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                while (rs.next()) {
                    Map<DataTableColumn, String> results = convertResultSetRowToDataTableColumnMap(table, rs);
                    return results;
                }
                return null;
            }

        };

        Map<DataTableColumn, String> dataTableQueryResults = tdarDataImportDatabase.selectAllFromTable(column, key, resultSetExtractor);
        resource.setRelatedDatasetData(dataTableQueryResults);
    }

    public List<Resource> findRecentlyUpdatedItemsInLastXDays(int days) {
        return getDao().findRecentlyUpdatedItemsInLastXDays(days);
    }

    @Transactional
    public List<DataTableColumn> updateMappings(Project project, Collection<DataTableColumn> columns) {
        List<DataTableColumn> columnsToMap = new ArrayList<DataTableColumn>();
        if (CollectionUtils.isEmpty(columns)) {
            return columnsToMap;
        }
        if (project == Project.NULL) {
            throw new TdarRecoverableRuntimeException("Unable to update mappings for an unspecified project.");
        }
        getDao().unmapAllColumnsInProject(project, columns);
        for (DataTableColumn column : columns) {
            logger.info("mapping dataset to resources using column: {} ", column);
            Dataset dataset = column.getDataTable().getDataset();
            if (dataset == null) {
                throw new TdarRecoverableRuntimeException("dataset for " + column + " was null");
            }
            else if (ObjectUtils.notEqual(project, dataset.getProject())) {
                throw new TdarRecoverableRuntimeException("dataset project " + project + " somehow wasn't the same as " + project);
            }
            if (column.isMappingColumn()) {
                columnsToMap.add(column);
                // FIXME: could add custom logic to add a backpointer (new column) to DB that has "mapped" set to true based on updatedValues
            }
        }
        return columnsToMap;
    }

    public List<Long> findAllIds() {
        return getDao().findAllIds();
    }

    @Async
    @Transactional
    public void remapColumnsAsync(final List<DataTableColumn> columns, final Project project) {
        remapColumns(columns, project);
    }

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
        searchIndexService.indexProject(project);
    }

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
                    throw new TdarRecoverableRuntimeException(String.format("could not find column named %s with id %s", incomingColumn.getName(),
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
                        incomingColumn,
                        incomingColumn.getColumnEncodingType());
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
                throw new TdarRecoverableRuntimeException("invalid column: " + existingColumn);
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
        toReturn.getSecond().addAll(updateMappings(dataset.getProject(), columnsToMap));
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
     * Return a HashMap that maps data table columns to values
     * FIXME: where should this really live
     */
    public static Map<DataTableColumn, String> convertResultSetRowToDataTableColumnMap(final DataTable table, ResultSet rs) throws SQLException {
        Map<DataTableColumn, String> results = new HashMap<DataTableColumn, String>();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            DataTableColumn col = table.getColumnByName(rs.getMetaData().getColumnName(i));
            if (col != null && col.isVisible()) { // ignore if null (non translated version of translated)
                results.put(col, null);
            }
        }
        for (DataTableColumn key : results.keySet()) {
            String val = "NULL";
            Object obj = rs.getObject(key.getName());
            if (obj != null) {
                val = obj.toString();
            }
            results.put(key, val);
        }
        return results;
    }

}
