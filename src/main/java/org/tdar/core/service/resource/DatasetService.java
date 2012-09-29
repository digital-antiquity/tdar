package org.tdar.core.service.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.DataValueOntologyNodeMapping;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableRelationship;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.db.conversion.DatasetConversionFactory;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.SimpleSerializer;

/**
 * $Id$
 * 
 * @author Allen Lee
 * @version $Revision$
 */
@Service
public class DatasetService extends AbstractInformationResourceService<Dataset, DatasetDao> {

    @Autowired
    private PostgresDatabase tdarDataImportDatabase;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    public void setDao(DatasetDao dao) {
        super.setDao(dao);
    }

    @Transactional
    public boolean translate(final DataTableColumn column, final CodingSheet codingSheet) {
        if (codingSheet == null) {
            return false;
        }
        getLogger().debug("translating {} with {}", column.getName(), codingSheet);
        tdarDataImportDatabase.translateInPlace(column, codingSheet);
        return true;
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

    private HSSFWorkbook toExcel(Dataset dataset) {
        Set<DataTable> dataTables = dataset.getDataTables();
        if (dataTables == null || dataTables.isEmpty()) {
            return null;
        }
        HSSFWorkbook workbook = new HSSFWorkbook();
        for (DataTable dataTable : dataTables) {
            // each table becomes a sheet.
            String tableName = dataTable.getName();
            final HSSFSheet sheet = workbook.createSheet(tableName);
            ResultSetExtractor<Object> excelExtractor = new ResultSetExtractor<Object>() {
                @Override
                public Object extractData(ResultSet resultSet)
                        throws SQLException, DataAccessException {
                    ResultSetMetaData metadata = resultSet.getMetaData();
                    int columns = metadata.getColumnCount();
                    // create and initialize the header row of the worksheet.
                    int rowIndex = 0;
                    HSSFRow headerRow = sheet.createRow(rowIndex++);
                    int id_col = -1;
                    for (int columnIndex = 0; columnIndex < columns; columnIndex++) {
                        String columnName = metadata
                                .getColumnName(columnIndex + 1);
                        if (columnName.equals(TargetDatabase.TDAR_ID_COLUMN)) {
                            id_col = columnIndex;
                        } else {
                            HSSFCell headerCell = headerRow.createCell(columnIndex);
                            headerCell.setCellValue(columnName);
                        }
                    }
                    while (resultSet.next()) {
                        HSSFRow row = sheet.createRow(rowIndex++);
                        for (int columnIndex = 0; columnIndex < columns; columnIndex++) {
                            // int columnType = metadata.getColumnType(columnIndex+1);
                            if (columnIndex == id_col)
                                continue;

                            HSSFCell cell = row.createCell(columnIndex);
                            Object result = resultSet.getObject(columnIndex + 1);
                            if (result != null) {
                                cell.setCellValue(result.toString());
                            }
                        }
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
     * Converts the local data file to postgres if possible and generates the
     * appropriate DataTable and DataTableColumn resources collected by the
     * DatabaseConverter after conversion.
     * 
     * FIXME: still need to delete the uploaded data file. FIXME: may eventually
     * replace usage of DatabaseConverter with DatabaseConversionService. That
     * would probably replace this method entirely though.
     * 
     * 
     * @param localDataFile
     * @param dataset
     */
    @Transactional
    public void convertDataFile(InformationResourceFile datasetFile) {
        if (datasetFile == null) {
            getLogger().warn("No datasetFile specified, returning");
            return;
        }
        if (! datasetFile.isColumnarDataFileType() || ! (datasetFile.getInformationResource() instanceof Dataset) ) {
            getLogger().error("datasetFile had wrong file type {} or inappropriate InformationResource {}", datasetFile, datasetFile.getInformationResource());
            return;
        }
        Dataset dataset = (Dataset) datasetFile.getInformationResource();
        // delete existing data tables and drop them from the tdardata database.
        dropDatasetTables(dataset);
        // execute convert-to-db code.
        InformationResourceFileVersion versionToConvert = datasetFile.getLatestUploadedVersion();
        DatasetConverter databaseConverter = DatasetConversionFactory.getConverter(versionToConvert, tdarDataImportDatabase);
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
                    // if we just have one, then attempt to use this as our existing table regardless of the table / sheet name
                    existingTable = dataset.getDataTables().iterator().next();
                    existingTablesMap.clear();
                } else {
                    logger.warn("Couldn't find an analog to incoming data table {}, moving on", tableToPersist);
                    tableToPersist.setDataset(dataset);
                    dataset.getDataTables().add(tableToPersist);
                    getDao().saveOrUpdate(tableToPersist);
//                    unmergedTables.add(tableToPersist);
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
                logger.debug("result: {}",columnsToPersist);
                // get rid of all the old existing columns that don't have an analogous column (by name)
                existingTable.getDataTableColumns().removeAll(existingColumnsMap.values());
                getDao().delete(existingColumnsMap.values());

                tableToPersist.setDataset(dataset);
                // merge folds all the data on incomingDataTable into the existingTable transparently
                // we don't need delete the existingTable or remove it from Dataset but we cannot refer 
                // to it safely anymore
                tableToPersist = getDao().merge(tableToPersist, existingTable);

                logger.debug("merged data table is now {}", tableToPersist);
                logger.debug("actual data table columns {}, incoming data table columns {}", tableToPersist.getDataTableColumns(), columnsToPersist);
                
            }
            // clean up existing data tables
            logger.info("deleting unmerged tables: {}", existingTablesMap.values());
            dataset.getDataTables().removeAll(existingTablesMap.values());
            getDao().delete(existingTablesMap.values());


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
            logger.debug("dataTableColumns: {}", dataTable.getDataTableColumns());
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
            
            // FIXME: it appears that local bean properties are not being transferred or managed in the merge
            // check if setting MERGE cascades on DataTableColumn.dataTable will fix this.
//            existingColumn.setDescription(incomingColumn.getDescription());
//            existingColumn.setColumnDataType(incomingColumn.getColumnDataType());
//            existingColumn.setColumnEncodingType(incomingColumn.getColumnEncodingType());

            incomingColumn.setCategoryVariable(existingColumn.getCategoryVariable());
            incomingColumn.setMeasurementUnit(existingColumn.getMeasurementUnit());
            if (CollectionUtils.isNotEmpty(existingColumn.getValueToOntologyNodeMapping())) {
                for (DataValueOntologyNodeMapping mapping : existingColumn.getValueToOntologyNodeMapping()) {
                    mapping.setDataTableColumn(incomingColumn);
                }
                incomingColumn.getValueToOntologyNodeMapping().addAll(existingColumn.getValueToOntologyNodeMapping());
            }
            logger.debug("Merging incoming column with existing column");
            incomingColumn = getDao().merge(incomingColumn, existingColumn);
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
        SimpleSerializer serializer = new SimpleSerializer();
        serializer.addToWhitelist(DataTableColumn.class, "categoryVariable", "columnDataType", "columnEncodingType", "columnEncodingType", "measurementUnit",
                                    "name", "valueToOntologyNodeMapping");
        serializer.addToWhitelist(CategoryVariable.class, "description", "label", "parent", "type");
        // FIXME: ensure that we can rehydrate the list of mappings if we only persist the "bag" field (we assume mappings are implemented with a hibernate
        // PersistentBag)
        serializer.addToWhitelist(org.hibernate.collection.PersistentBag.class, "bag");
        serializer.addToWhitelist(DataValueOntologyNodeMapping.class, "dataValue", "ontologyNode");
        serializer.addToWhitelist(OntologyNode.class, "description", "displayName");
        serializer.addToWhitelist(Persistable.Base.class, "id");

        String xml = serializer.toXml(dataTableColumn);
        resourceService.logResourceModification(dataTableColumn.getDataTable().getDataset(), authenticatedUser, "saveDataValueOntologyNodeMapping", xml);

        getLogger().debug("--saveDataValueOntologyNodeMapping--\n{}", xml);
    }

    public void logDataTableColumns(DataTable dataTable, String message, Person authenticatedUser) {
        // blacklist everything in DataTableColumn except for a few fields.
        SimpleSerializer serializer = new SimpleSerializer();
        serializer.addToWhitelist(DataTableColumn.class, "categoryVariable", "columnDataType", "columnEncodingType", "columnEncodingType", "measurementUnit",
                "name");
        serializer.addToWhitelist(CategoryVariable.class, "description", "label", "parent", "type");
        String xml = serializer.toXml(dataTable.getSortedDataTableColumns());
        resourceService.logResourceModification(dataTable.getDataset(), authenticatedUser, message, xml);
        logger.debug(message + xml);
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
    public List<List<String>> selectAllFromDataTable(DataTable dataTable, final int start, final int page, boolean includeGenerated) {
        ResultSetExtractor<List<List<String>>> resultSetExtractor = new ResultSetExtractor<List<List<String>>>() {
            @Override
            public List<List<String>> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<List<String>> results = new ArrayList<List<String>>();
                List<String> colNames = new ArrayList<String>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    colNames.add(rs.getMetaData().getColumnName(i));
                }
                int rowNum = 0;
                results.add(colNames);
                while (rs.next()) {
                    rowNum++;
                    if (rowNum <= start || rowNum > start + page)
                        continue;
                    List<String> row = new ArrayList<String>();
                    for (int i = 1; i <= colNames.size(); i++) {
                        row.add(rs.getString(i));
                    }
                    results.add(row);
                }
                return results;
            }
        };
        return tdarDataImportDatabase.selectAllFromTable(dataTable, resultSetExtractor, includeGenerated);
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
}
