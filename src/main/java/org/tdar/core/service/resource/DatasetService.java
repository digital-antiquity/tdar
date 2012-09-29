package org.tdar.core.service.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.DataValueOntologyNodeMapping;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableRelationship;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.XmlService;
import org.tdar.db.conversion.DatasetConversionFactory;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.struts.data.FileProxy;
import org.tdar.struts.data.ResultMetadataWrapper;

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
    private DataTableService dataTableService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private XmlService xmlService;

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
    public void createTranslatedFile(Dataset dataset) {
        // assumes that Datasets only have a single file
        InformationResourceFile file = dataset.getFirstInformationResourceFile();
        if (file == null) {
            getLogger().warn("Trying to translate {} with a null file payload.", dataset);
            return;
        }
        getInformationResourceFileService().deleteTranslatedFiles(file);

        // FIXME: remove synchronize once Hibernate learns more about unique
        // constraints
        // http://community.jboss.org/wiki/HibernateFAQ-AdvancedProblems#Hibernate_is_violating_a_unique_constraint
        genericDao.synchronize();
        HSSFWorkbook translatedDatasetWorkbook = toExcel(dataset);
        ByteArrayOutputStream translatedFileOutputStream = null;
        InputStream in = null;
        try {
            translatedFileOutputStream = new ByteArrayOutputStream();
            translatedDatasetWorkbook.write(translatedFileOutputStream);
            in = new ByteArrayInputStream(translatedFileOutputStream.toByteArray());
            String filename = FilenameUtils.getBaseName(file.getLatestUploadedVersion().getFilename()) + "_translated.xls";
            FileProxy fileProxy = new FileProxy(filename, in, VersionType.TRANSLATED, FileAction.ADD_DERIVATIVE);
            fileProxy.setFileId(file.getId());
            processFileProxy(dataset, fileProxy);
        } catch (IOException exception) {
            getLogger().error("Unable to create translated file for Dataset: " + dataset, exception);
        } finally {
            IOUtils.closeQuietly(translatedFileOutputStream);
            IOUtils.closeQuietly(in);
        }
    }
    
    /**
     * Re-uploads the latest version of the data file for the given dataset.
     * @param dataset
     */
    @Transactional(noRollbackFor=TdarRecoverableRuntimeException.class)
    public void reprocess(Dataset dataset) {
        logger.debug("Reprocessing {}", dataset);
    	convertDataFile(dataset.getFirstInformationResourceFile());
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

    private HSSFWorkbook toExcel(Dataset dataset) {
        Set<DataTable> dataTables = dataset.getDataTables();
        if (dataTables == null || dataTables.isEmpty()) {
            return null;
        }
        HSSFWorkbook workbook = new HSSFWorkbook();
        for (final DataTable dataTable : dataTables) {
            // each table becomes a sheet.
            String tableName = dataTable.getDisplayName();
            logger.debug(tableName);
            if (workbook.getSheet(tableName) != null) {
                throw new TdarRecoverableRuntimeException("two tables with same display name ");
            }
            final HSSFSheet sheet = workbook.createSheet(tableName);
            ResultSetExtractor<Object> excelExtractor = new ResultSetExtractor<Object>() {
                @Override
                public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                    // create and initialize the header row of the worksheet.
                    int rowIndex = 0;
                    List<String> columnNames = getColumnNames(resultSet, dataTable);
                    // List<String> columnNames =
                    // for (int columnIndex = 0; columnIndex < metadata.getColumnCount(); columnIndex++) {
                    // String columnName = metadata.getColumnName(columnIndex + 1);
                    // columnNames.add(columnName);
                    // }
                    logger.debug("column names: " + columnNames);
                    excelService.addHeaderRow(sheet, rowIndex, 0, columnNames);
                    while (resultSet.next()) {
                        rowIndex++;
                        excelService.addDataRow(sheet, rowIndex, 0, resultSet);
                    }
                    return null;
                }
            };
            tdarDataImportDatabase.selectAllFromTable(dataTable, excelExtractor, true);
        }
        return workbook;
    }

    @Transactional(readOnly = true)
    public boolean canLinkDataToOntology(Dataset dataset) {
        return getDao().canLinkDataToOntology(dataset);
    }

    /**
     * Converts the given dataset file into postgres, preserving mappings and column-level metadata as much as possible.
     * 
     */
    @Transactional(noRollbackFor=TdarRecoverableRuntimeException.class)
    public void convertDataFile(InformationResourceFile datasetFile) {
        if (datasetFile == null) {
            getLogger().warn("No datasetFile specified, returning");
            return;
        }
        if (!datasetFile.isColumnarDataFileType() || !(datasetFile.getInformationResource() instanceof Dataset)) {
            getLogger().error("datasetFile had wrong file type {} or inappropriate InformationResource {}", datasetFile, datasetFile.getInformationResource());
            return;
        }
        Dataset dataset = (Dataset) datasetFile.getInformationResource();
        // execute convert-to-db code.
        InformationResourceFileVersion versionToConvert = datasetFile.getLatestUploadedVersion();
        if (versionToConvert == null || versionToConvert.getFile() == null || ! versionToConvert.getFile().exists()) {
            throw new TdarRecoverableRuntimeException(String.format("Latest uploaded version %s for InformationResourceFile %s had no actual File payload", versionToConvert, datasetFile));
        }
        // drop this dataset's actual data tables from the tdardata database - we'll delete the actual hibernate metadata entities later after
        // performing reconciliation so we can preserve as much column-level metadata as possible
        dropDatasetTables(dataset);

        DatasetConverter databaseConverter = DatasetConversionFactory.getConverter(versionToConvert, tdarDataImportDatabase);
        // returns the set of transient POJOs from the incoming dataset.
        Set<DataTable> tablesToPersist = databaseConverter.execute();
        // helper Map to manage existing tables - all remaining entries in this existingTablesMap will be purged at the end of this process
        HashMap<String, DataTable> existingTablesMap = new HashMap<String, DataTable>();
        for (DataTable existingDataTable : dataset.getDataTables()) {
            existingTablesMap.put(databaseConverter.getInternalTableName(existingDataTable.getName()), existingDataTable);
            logger.debug("existingTableName: {}", databaseConverter.getInternalTableName(existingDataTable.getName()));
        }
        logger.debug("Existing name to table map: {}", existingTablesMap);
        for (DataTable tableToPersist : tablesToPersist) {
            // first check that the incoming data table has data table columns.
            if (CollectionUtils.isNotEmpty(tableToPersist.getDataTableColumns())) {
                String internalTableName = databaseConverter.getInternalTableName(tableToPersist.getName());
                DataTable existingTable = existingTablesMap.get(internalTableName);
                if (existingTable != null) {
                    // remove existingTable from the existingTablesMap since we're not going to delete this table.
                    // any DataTableColumns that match incomingDataTable's DataTableColumns will be preserved and reconciled
                    // and all others that don't match will be deleted.
                    existingTablesMap.remove(internalTableName);
                } else if (existingTablesMap.size() == 1 && tablesToPersist.size() == 1) {
                    // the table names did not match, but we have one incoming table and one existing table. Try to match them regardless.
                    existingTable = dataset.getDataTables().iterator().next();
                    existingTablesMap.clear();
                } else {
                    // continue with the for loop, tableToPersist does not require any metadata merging because
                    // we can't find an existing table to merge it with
                    logger.debug("No analogous existing table to merge with incoming data table {}, moving on", tableToPersist);
                    tableToPersist.setDataset(dataset);
                    dataset.getDataTables().add(tableToPersist);
                    getDao().saveOrUpdate(tableToPersist);
                    // unmergedTables.add(tableToPersist);
                    continue;
                }
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
                    incomingColumn = reconcileColumn(existingTable, existingColumnsMap, normalizedColumnName, incomingColumn, existingColumn);
                    columnsToPersist.set(i, incomingColumn);
                }
                logger.debug("deleting unmerged columns: {}", existingColumnsMap);
                logger.debug("result: {}", columnsToPersist);
                // get rid of all the old existing columns that don't have an analogous column (by name)
                Collection<DataTableColumn> columnsToRemove = existingColumnsMap.values();
                // first unmap these columns
                getDao().unmapAllColumnsInProject(dataset.getProject(), columnsToRemove);
                existingTable.getDataTableColumns().removeAll(columnsToRemove);
                getDao().delete(columnsToRemove);


                tableToPersist.setDataset(dataset);
                // merge folds all the data on incomingDataTable into the existingTable transparently
                // we don't need delete the existingTable or remove it from Dataset but we cannot refer
                // to it safely anymore
                tableToPersist = getDao().merge(tableToPersist, existingTable);
                logger.debug("merged data table is now {}", tableToPersist);
                logger.debug("actual data table columns {}, incoming data table columns {}", tableToPersist.getDataTableColumns(), columnsToPersist);
            }
            // any tables left in existingTables didn't have an analog in the incoming dataset, so clean them up
            Collection<DataTable> tablesToRemove = existingTablesMap.values();
            logger.info("deleting unmerged tables: {}", tablesToRemove);
            ArrayList<DataTableColumn> columnsToUnmap = new ArrayList<DataTableColumn>();
            for (DataTable table: tablesToRemove) {
                columnsToUnmap.addAll(table.getDataTableColumns());
            }
            // first unmap all columns from the removed tables
            getDao().unmapAllColumnsInProject(dataset.getProject(), columnsToUnmap);
            dataset.getDataTables().removeAll(tablesToRemove);
            getDao().delete(tablesToRemove);

            for (DataTableRelationship rel : dataset.getRelationships()) {
                String oldForeignName = rel.getForeignTable().getName();
                rel.setForeignTable(dataset.getDataTableByName(oldForeignName));
                String oldLocalName = rel.getLocalTable().getName();
                rel.setLocalTable(dataset.getDataTableByName(oldLocalName));
                Set<DataTableColumn> newLocalCols = new HashSet<DataTableColumn>();
                for (DataTableColumn col : rel.getLocalColumns()) {
                    newLocalCols.add(rel.getLocalTable().getColumnByName(col.getName()));
                }
                rel.setLocalColumns(newLocalCols);
                Set<DataTableColumn> newForeignCols = new HashSet<DataTableColumn>();
                for (DataTableColumn col : rel.getForeignColumns()) {
                    newForeignCols.add(rel.getForeignTable().getColumnByName(col.getName()));
                }
                rel.setForeignColumns(newForeignCols);
                getDao().saveOrUpdate(rel);
            }
        }

        logger.debug("dataset: {} id: {}", dataset.getTitle(), dataset.getId());
        for (DataTable dataTable : dataset.getDataTables()) {
            logger.debug("dataTable: {}", dataTable);
            List<DataTableColumn> columns = dataTable.getDataTableColumns();
            logger.debug("dataTableColumns: {}", columns);
            for (DataTableColumn column: columns) {
                translate(column);
            }
        }
        datasetFile.setStatus(FileStatus.PROCESSED);
        getDao().saveOrUpdate(datasetFile);
        getDao().saveOrUpdate(dataset);
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
    private DataTableColumn reconcileColumn(DataTable existingTable,
            HashMap<String, DataTableColumn> existingNameToColumnMap,
            String normalizedColumnName, DataTableColumn incomingColumn,
            DataTableColumn existingColumn) {
        // FIXME: check that types are compatible before merging

        if (existingColumn != null) {
            // if we've gotten this far, we know that the incoming column
            // should be saved onto the existing table instead of the transient table that it was
            // originally set on.
            incomingColumn.setDataTable(existingTable);
            // copy all values that should be retained
            incomingColumn.setDefaultCodingSheet(existingColumn.getDefaultCodingSheet());
            incomingColumn.setDefaultOntology(existingColumn.getDefaultOntology());

            incomingColumn.setCategoryVariable(existingColumn.getCategoryVariable());

            if (CollectionUtils.isNotEmpty(existingColumn.getValueToOntologyNodeMapping())) {
                for (DataValueOntologyNodeMapping mapping : existingColumn.getValueToOntologyNodeMapping()) {
                    mapping.setDataTableColumn(incomingColumn);
                }
                incomingColumn.getValueToOntologyNodeMapping().addAll(existingColumn.getValueToOntologyNodeMapping());
            }
            logger.debug("Merging incoming column with existing column");
            incomingColumn = getDao().merge(incomingColumn, existingColumn);
            incomingColumn.copyUserMetadataFrom(existingColumn);
            existingNameToColumnMap.remove(normalizedColumnName);
        }
        return incomingColumn;
    }

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
            logger.debug(message + writer.toString());
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
            logger.trace("order column did not exist" , e);
            wrapper.setResults(tdarDataImportDatabase.selectAllFromTable(dataTable, resultSetExtractor, includeGenerated));
        }
        return wrapper;
    }

    @Transactional
    public List<DataTableRelationship> listRelationshipsForColumns(DataTableColumn column) {
        List<DataTableRelationship> relationships = new ArrayList<DataTableRelationship>();
        Set<DataTableRelationship> allDatasetRelationships = column.getDataTable().getDataset().getRelationships();
        logger.trace("All relationships: {}", allDatasetRelationships);
        for (DataTableRelationship relationship : allDatasetRelationships) {
            if (relationship.getLocalColumns().contains(column)) {
                relationships.add(relationship);
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

    /*
     * Return a HashMap that maps data table columns to values
     */
    private Map<DataTableColumn, String> convertResultSetRowToDataTableColumnMap(final DataTable table, ResultSet rs) throws SQLException {
        Map<DataTableColumn, String> results = new HashMap<DataTableColumn, String>();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            DataTableColumn col = table.getColumnByName(rs.getMetaData().getColumnName(i));
            if (col != null && col.isVisible()) { // ignore if null (non translated version of translated)
                results.put(col, null);
            }
        }
        for (DataTableColumn key : results.keySet()) {
            results.put(key, rs.getString(key.getName()));
        }
        return results;
    }

    public List<Resource> findRecentlyUpdatedItemsInLastXDays(int days) {
        return getDao().findRecentlyUpdatedItemsInLastXDays(days);
    }

    @Transactional
    public void updateMappings(Project project, Collection<DataTableColumn> columns) {
        if (CollectionUtils.isEmpty(columns)) {
            return;
        }
        if (project == Project.NULL) {
            throw new TdarRecoverableRuntimeException("Unable to update mappings for an unspecified project.");
        }
        getDao().unmapAllColumnsInProject(project, columns);
        boolean hasMappedColumns = false;
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
                /*
                 * Take the distinct column values mapped and associate them with files in tDAR based on:
                 * - shared project
                 * - filename matches column value either (a) with extension or (b) with separator eg: file1.jpg;file2.jpg
                 * NOTE: a manual reindex happens at the end
                 */
                List<String> updatedValues = getDao().mapColumnToResource(column, dataTableService.findAllDistinctValues(column));
                hasMappedColumns = true;
                // FIXME: could add custom logic to add a backpointer (new column) to DB that has "mapped" set to true based on updatedValues
            }
        }
        if (hasMappedColumns) {
            // mapping columns to the resource runs a raw sql update, refresh the state of the Project. 
            getDao().refresh(project);
            // have to reindex...
            logger.debug("{}", project.getInformationResources());        	
        }
        searchIndexService.index((Resource[]) project.getInformationResources().toArray(new Resource[0]));
    }

}
