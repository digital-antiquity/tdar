package org.tdar.core.service.resource;

import java.io.File;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.datatable.ColumnVisibiltiy;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.dao.resource.DataTableColumnDao;
import org.tdar.core.dao.resource.DataTableDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.InformationResourceFileDao;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.integration.DataIntegrationService;
import org.tdar.core.service.resource.dataset.DatasetUtils;
import org.tdar.core.service.resource.dataset.ResultMetadataWrapper;
import org.tdar.core.service.resource.dataset.TdarDataResultSetExtractor;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.db.model.TargetDatabase;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.TitleSortComparator;

import com.opensymphony.xwork2.TextProvider;

/**
 * $Id$
 * 
 * Supporting methods for dealing with Datasets, importing, converting, etc.
 * 
 * @author Allen Lee
 * @version $Revision$
 */
@Service
public class DatasetServiceImpl extends ServiceInterface.TypedDaoBase<Dataset, DatasetDao> implements DatasetService {

    @Autowired
    @Qualifier("target")
    private TargetDatabase tdarDataImportDatabase;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private DataIntegrationService dataIntegrationService;

    @Autowired
    private InformationResourceFileDao informationResourceFileDao;

    @Autowired
    private DataTableColumnDao dataTableColumnDao;
    @Autowired
    private DataTableDao dataTableDao;

    @Autowired
    private SerializationService serializationService;

    @Autowired
    private FileAnalyzer analyzer;

    /*
     * @see org.tdar.core.service.resource.DatasetService#retranslate(org.tdar.core.bean.resource.Dataset)
     */
    @Override
    @Transactional(readOnly = false)
    public void retranslate(Dataset dataset) {
        getDao().retranslate(dataset);
    }

    /*
     * For a given @link Dataset, create a translated file which includes all of the columns that are mapped to a @link CodingSheet and their mapped values as
     * well
     * as the code. The translated version is stored on the @link InformationResourceFileVersion as a derivative
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#createTranslatedFile(org.tdar.core.bean.resource.Dataset)
     */
    @Override
    @Transactional
    public InformationResourceFile createTranslatedFile(Dataset dataset) {
        return getDao().createTranslatedFile(dataset, analyzer, informationResourceFileDao);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#reprocess(org.tdar.core.bean.resource.Dataset)
     */
    @Override
    @Transactional(noRollbackFor = TdarRecoverableRuntimeException.class)
    public void reprocess(Dataset dataset) {
        getLogger().debug("Reprocessing {}", dataset);
        if (CollectionUtils.isEmpty(dataset.getInformationResourceFiles())) {
            return;
        }
        List<InformationResourceFileVersion> latestVersions = new ArrayList<>();
        try {
            for (InformationResourceFile file : dataset.getActiveInformationResourceFiles()) {
                InformationResourceFileVersion latestUploadedVersion = file.getLatestUploadedVersion();
                File transientFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, latestUploadedVersion);
                latestUploadedVersion.setTransientFile(transientFile);
                latestVersions.add(latestUploadedVersion);
            }

            analyzer.processFiles(dataset.getResourceType(), latestVersions, true);
            if (dataset.hasCodingColumns()) {
                createTranslatedFile(dataset);
            }
        } catch (Exception e) {
            throw new TdarRecoverableRuntimeException(e);
        }
    }

    /*
     * Checks whether a @link Dataset can be mapped to an @link Ontology and thus, whether specific CodingValues can be mapped to that Ontology
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#canLinkDataToOntology(org.tdar.core.bean.resource.Dataset)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canLinkDataToOntology(Dataset dataset) {
        return getDao().canLinkDataToOntology(dataset);
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
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#logDataTableColumns(org.tdar.core.bean.resource.datatable.DataTable, java.lang.String,
     * org.tdar.core.bean.entity.TdarUser, java.lang.Long)
     */
    @Override
    public void logDataTableColumns(DataTable dataTable, String message, TdarUser authenticatedUser, Long start) {
        try {
            StringWriter writer = new StringWriter();
            serializationService.convertToXML(dataTable, writer);
            Dataset dataset = dataTableDao.findDatasetForTable(dataTable);
            resourceService.logResourceModification(dataset, authenticatedUser, message, writer.toString(), RevisionLogType.EDIT, start);
            getLogger().trace("{} - xml {}", message, writer);
        } catch (Exception e) {
            getLogger().error("could not serialize to XML:", e);
        }
    }

    /*
     * Takes a Coding Table within a larger data set and converts it to a tDAR CodingSheet
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#convertTableToCodingSheet(org.tdar.core.bean.entity.TdarUser, com.opensymphony.xwork2.TextProvider,
     * org.tdar.core.bean.resource.datatable.DataTableColumn, org.tdar.core.bean.resource.datatable.DataTableColumn,
     * org.tdar.core.bean.resource.datatable.DataTableColumn)
     */
    @Override
    @Transactional
    public CodingSheet convertTableToCodingSheet(TdarUser user, final TextProvider provider, final DataTableColumn keyColumn,
            final DataTableColumn valueColumn,
            final DataTableColumn descriptionColumn) {
        // codingSheet.setAccount(keyColumn.getDataTable().getDataset().getAccount());
        Dataset dataset = dataTableDao.findDatasetForTable(keyColumn.getDataTable());
        final CodingSheet codingSheet = dataTableColumnDao.setupGeneratedCodingSheet(keyColumn, dataset, user, provider, null);
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
                        } else if ((descriptionColumn != null) && columnName.equals(descriptionColumn.getName())) {
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
     * Find all Rows within a @link DataTable with Pagination. Used to browse a Data Table
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#selectAllFromDataTable(org.tdar.core.bean.resource.datatable.DataTable, int, int, boolean, boolean)
     */
    @Override
    @SuppressWarnings("deprecation")
    @Transactional
    public ResultMetadataWrapper selectAllFromDataTable(final Dataset dataset, final DataTable dataTable, final int start, final int page, boolean includeGenerated,
            final boolean returnRowId, final TdarUser authenticatedUser) {
        final ResultMetadataWrapper wrapper = new ResultMetadataWrapper();
        wrapper.setRecordsPerPage(page);
        wrapper.setStartRecord(start);

        ResultSetExtractor<List<List<String>>> resultSetExtractor = new TdarDataResultSetExtractor(wrapper, start, page, dataTable, returnRowId,
                authorizationService.canViewConfidentialInformation(authenticatedUser, dataset));
        try {
            wrapper.setResults(tdarDataImportDatabase.selectAllFromTableInImportOrder(dataTable, resultSetExtractor, includeGenerated));
        } catch (BadSqlGrammarException e) {
            getLogger().trace("order column did not exist", e);
            wrapper.setResults(tdarDataImportDatabase.selectAllFromTable(dataTable, resultSetExtractor, includeGenerated));
        }
        return wrapper;
    }

    /*
     * Extracts a specific Row of data from a tdardata database and returns a map object with it's contents pre-mapped to @link DataTableColumn entries
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#selectRowFromDataTable(org.tdar.core.bean.resource.datatable.DataTable, java.lang.Long, boolean)
     */
    @Override
    @Transactional
    public Map<DataTableColumn, String> selectRowFromDataTable(final Dataset dataset, final DataTable dataTable, final Long rowId, final boolean returnRowId,TdarUser authenticatedUser) {
        ResultSetExtractor<Map<DataTableColumn, String>> resultSetExtractor = new ResultSetExtractor<Map<DataTableColumn, String>>() {

            @Override
            public Map<DataTableColumn, String> extractData(ResultSet rs) throws SQLException {
                Map<DataTableColumn, String> result = new HashMap<>();
                while (rs.next()) {
                    result = DatasetUtils.convertResultSetRowToDataTableColumnMap(dataTable, authorizationService.canViewConfidentialInformation(authenticatedUser, dataset), rs, returnRowId);
                }
                return result;
            }

        };
        return tdarDataImportDatabase.selectRowFromTable(dataTable, resultSetExtractor, rowId);

    }

    /*
     * Finds a set of Database rows from the TdarMetadata database that are associated with the String specified, and wraps them in a @link
     * ResultsMetadataWrapper
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#findRowsFromDataTable(org.tdar.core.bean.resource.datatable.DataTable, int, int, boolean,
     * java.lang.String)
     */
    @Override
    @Transactional
    public ResultMetadataWrapper findRowsFromDataTable(final Dataset dataset, final DataTable dataTable, final int start, final int page, boolean includeGenerated, String query,TdarUser authenticatedUser) {
        final ResultMetadataWrapper wrapper = new ResultMetadataWrapper();
        wrapper.setRecordsPerPage(page);
        wrapper.setStartRecord(start);

        ResultSetExtractor<List<List<String>>> resultSetExtractor = new TdarDataResultSetExtractor(wrapper, start, page, dataTable, false, authorizationService.canViewConfidentialInformation(authenticatedUser, dataset));
        try {
            wrapper.setResults(tdarDataImportDatabase.selectAllFromTable(dataTable, resultSetExtractor, includeGenerated, query));
        } catch (BadSqlGrammarException e) {
            getLogger().trace("order column did not exist", e);
        }
        return wrapper;
    }


//    /*
//     * Extracts out all @link DataTableRelationship entries for a @link DataTableColumn.
//     */
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.tdar.core.service.resource.DatasetService#listRelationshipsForColumns(org.tdar.core.bean.resource.datatable.DataTableColumn)
//     */
//    @Override
//    @Transactional
//    public List<DataTableRelationship> listRelationshipsForColumns(DataTableColumn column) {
//        List<DataTableRelationship> relationships = new ArrayList<>();
//        Set<DataTableRelationship> allDatasetRelationships = column.getDataTable().getDataset().getRelationships();
//        getLogger().trace("All relationships: {}", allDatasetRelationships);
//        for (DataTableRelationship relationship : allDatasetRelationships) {
//            for (DataTableColumnRelationship columnRelationship : relationship.getColumnRelationships()) {
//                if (column.equals(columnRelationship.getLocalColumn())) {
//                    relationships.add(relationship);
//                }
//            }
//        }
//        return relationships;
//    }

    /*
     * Based on a set of @link DataTableColumn entries, and a @link Project we can will clear out the existing mappings; and then identify mappings that need to
     * be made.
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#prepareAndFindMappings(org.tdar.core.bean.resource.Project, java.util.Collection)
     */
    @Override
    @Transactional
    public List<DataTableColumn> prepareAndFindMappings(Dataset dataset, Collection<DataTableColumn> columns) {
        List<DataTableColumn> columnsToMap = new ArrayList<DataTableColumn>();
        
        if (dataset == null) {
            throw new TdarRecoverableRuntimeException("datasetService.dataset_null_column", Arrays.asList(dataset));
        }

        if (CollectionUtils.isEmpty(columns)) {
            return columnsToMap;
        }

        ResourceCollection collection = getDao().findMappedCollectionForDataset(dataset);
        if (collection == null) {
            return null;
        }

        getDao().unmapAllColumnsInProject(collection.getId(), PersistableUtils.extractIds(columns));
        for (DataTableColumn column : columns) {
            getLogger().info("mapping dataset to resources using column: {} ", column);

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
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#findAllIds()
     */
    @Override
    public List<Long> findAllIds() {
        return getDao().findAllIds();
    }

    /*
     * A special feature of a @link Dataset is if it's associated with a @link Project, we can use data from a @link DataTable to associate additional data with
     * other resources in the project, e.g. a database of images. The mapping here is created using a field in the column that contains the filename of the file
     * to be mapped, and is associated with the filename associated with @InformationResourceFileVersion of any @link Resource in that @link Project.
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#remapColumns(java.util.List, org.tdar.core.bean.resource.Project)
     */
    @Override
    @Transactional
    public void remapColumns(List<DataTableColumn> columns, ResourceCollection collection) {
        remapColumnsWithoutIndexing(columns, collection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#remapColumnsWithoutIndexing(java.util.List, org.tdar.core.bean.resource.Project)
     */
    @Override
    @Transactional
    public void remapColumnsWithoutIndexing(List<DataTableColumn> columns, ResourceCollection collection) {
        getLogger().info("remapping columns: {} in {} ", columns, collection);
        if (CollectionUtils.isNotEmpty(columns) && (collection != null)) {
            getDao().resetColumnMappings(collection);
            // mapping columns to the resource runs a raw sql update, refresh the state of the Project.
            getDao().refresh(collection);
            // have to reindex...
            /*
             * Take the distinct column values mapped and associate them with files in tDAR based on:
             * - shared project
             * - filename matches column value either (a) with extension or (b) with separator eg: file1.jpg;file2.jpg
             * NOTE: a manual reindex happens at the end
             */
            for (DataTableColumn column : columns) {
                Dataset dataset = dataTableDao.findDatasetForTable(column.getDataTable());
                getDao().mapColumnToResource(dataset, column, tdarDataImportDatabase.selectNonNullDistinctValues(column.getDataTable(), column, false), collection);
            }
        }
    }

    /*
     * Takes an existing @link Dataset and @link DataTable, and an incoming list of @link DataTableColumn entries, from the edit-column-metadata function in
     * tDAR, iterate through each incoming DataTableColumn and update the real entries in the database. Once updated, re-translate, map, and other changes as
     * necessary.
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#updateColumnMetadata(com.opensymphony.xwork2.TextProvider, org.tdar.core.bean.resource.Dataset,
     * org.tdar.core.bean.resource.datatable.DataTable, java.util.List, org.tdar.core.bean.entity.TdarUser, java.lang.Long)
     */
    @Override
    @Transactional
    public Boolean updateColumnMetadata(TextProvider provider, Dataset dataset, DataTable dataTable,
            List<DataTableColumn> dataTableColumns, TdarUser authenticatedUser, Long startTime) {
        Boolean hasOntologies = false;
        List<DataTableColumn> columnsToTranslate = new ArrayList<DataTableColumn>();
        for (DataTableColumn incomingColumn : dataTableColumns) {
            getLogger().debug("incoming data table column: {}", incomingColumn);
            DataTableColumn existingColumn = dataTable.getColumnById(incomingColumn.getId());
            existingColumn = checkForMissingColumn(dataTable, incomingColumn, existingColumn);
            CodingSheet incomingCodingSheet = incomingColumn.getDefaultCodingSheet();
            CodingSheet existingCodingSheet = existingColumn.getDefaultCodingSheet();
            Ontology defaultOntology = null;
            if (!PersistableUtils.isNullOrTransient(incomingCodingSheet)) {
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
                defaultOntology = getDao().loadFromSparseEntity(incomingColumn.getTransientOntology(), Ontology.class);
            }
            getLogger().debug("incoming coding sheet: {} | default ontology: {}", incomingCodingSheet, defaultOntology);
            incomingColumn.setTransientOntology(defaultOntology);
            if ((defaultOntology != null) && PersistableUtils.isNullOrTransient(incomingCodingSheet)) {
                incomingColumn.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);

                CodingSheet generatedCodingSheet = dataIntegrationService.createGeneratedCodingSheet(provider, dataset, existingColumn, authenticatedUser,
                        defaultOntology);
                incomingColumn.setDefaultCodingSheet(generatedCodingSheet);
                getLogger().debug("generated coding sheet {} for {}", generatedCodingSheet, incomingColumn);
            }

            // FIXME: can we simplify this logic? Perhaps push into DataTableColumn?
            // incoming ontology or coding sheet from the web was not null but the column encoding type was set to something that
            // doesn't support either, we set it to null
            // incoming ontology or coding sheet is explicitly set to null
            if (!PersistableUtils.isNullOrTransient(defaultOntology)) {
                if (incomingColumn.getColumnEncodingType().isSupportsOntology()) {
                    hasOntologies = true;
                } else {
                    incomingColumn.setTransientOntology(null);
                    getLogger().debug("column {} doesn't support ontologies - setting default ontology to null", incomingColumn);
                }
            }
            if ((incomingColumn.getDefaultCodingSheet() != null) && !incomingColumn.getColumnEncodingType().isSupportsCodingSheet()
                    && (defaultOntology == null)) {
                incomingColumn.setDefaultCodingSheet(null);
                getLogger().debug("column encoding type didn't support coding sheets - setting default coding sheet to null on column {} (encoding type: {})",
                        incomingColumn, incomingColumn.getColumnEncodingType());
            }

            existingColumn.setTransientOntology(incomingColumn.getTransientOntology());
            existingColumn.setDefaultCodingSheet(incomingColumn.getDefaultCodingSheet());

            copyCategoryVariableInfo(incomingColumn, existingColumn);
            // check if values have changed
            // copy off all of the values that can be directly copied from the bean
            existingColumn.copyUserMetadataFrom(incomingColumn);
            if (!existingColumn.isValid()) {
                throw new TdarRecoverableRuntimeException("datasetService.invalid_column", Arrays.asList(existingColumn));
            }

            // if there is a change in coding sheet a column may need to be retranslated or untranslated.
            if (isRetranslationNeeded(incomingCodingSheet, existingCodingSheet)) {
                getLogger().debug("retranslating {} for incoming coding sheet {}", existingColumn, incomingCodingSheet);
                columnsToTranslate.add(existingColumn);
            }
            getLogger().trace("{}", existingColumn);
            getDao().update(existingColumn);
        }
        dataset.markUpdated(authenticatedUser);
        save(dataset);
        if (!columnsToTranslate.isEmpty()) {
            // create the translation file for this dataset.
            getLogger().debug("creating translated file");
            getDao().retranslate(columnsToTranslate);
            createTranslatedFile(dataset);
        }
        logDataTableColumns(dataTable, "data column metadata registration", authenticatedUser, startTime);
        getLogger().info("hasOntology: {} ", hasOntologies);
        return hasOntologies;
    }

    private void copyCategoryVariableInfo(DataTableColumn incomingColumn, DataTableColumn existingColumn) {
        existingColumn.setCategoryVariable(getDao().loadFromSparseEntity(incomingColumn.getCategoryVariable(), CategoryVariable.class));
        CategoryVariable subcategoryVariable = getDao().loadFromSparseEntity(incomingColumn.getTempSubCategoryVariable(),
                CategoryVariable.class);

        if (subcategoryVariable != null) {
            existingColumn.setCategoryVariable(subcategoryVariable);
        }
    }

    /*
     * Takes an existing @link Dataset and @link DataTable, and an incoming list of @link DataTableColumn entries, from the edit-column-metadata function in
     * tDAR, iterate through each incoming DataTableColumn and update the real entries in the database. Just handle resource-column-row mappings
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#updateColumnResourceMappingMetadata(com.opensymphony.xwork2.TextProvider,
     * org.tdar.core.bean.resource.Dataset, org.tdar.core.bean.resource.datatable.DataTable, java.util.List, org.tdar.core.bean.entity.TdarUser, java.lang.Long)
     */
    @Override
    @Transactional
    public List<DataTableColumn> updateColumnResourceMappingMetadata(TextProvider provider, Dataset dataset, DataTable dataTable,
            List<DataTableColumn> dataTableColumns, TdarUser authenticatedUser, Long start) {
        List<DataTableColumn> columnsToMap = new ArrayList<DataTableColumn>();
        for (DataTableColumn incomingColumn : dataTableColumns) {
            getLogger().debug("incoming data table column: {}", incomingColumn);
            DataTableColumn existingColumn = dataTable.getColumnById(incomingColumn.getId());
            existingColumn = checkForMissingColumn(dataTable, incomingColumn, existingColumn);

            if (existingColumn.hasDifferentMappingMetadata(incomingColumn)) {
                getLogger().debug("remapping {}", existingColumn);
                columnsToMap.add(existingColumn);
            }
            // copy off all of the values that can be directly copied from the bean
            existingColumn.copyMappingMetadataFrom(incomingColumn);
            getLogger().trace("{}", existingColumn);
            getDao().update(existingColumn);
        }
        dataset.markUpdated(authenticatedUser);
        List<DataTableColumn> toReturn = prepareAndFindMappings(dataset, columnsToMap);
        save(dataset);
        logDataTableColumns(dataTable, "column metadata mapping", authenticatedUser, start);
        getLogger().info("mappingColumns: {} ", toReturn);
        return toReturn;
    }

    /**
     * Throws exception if the column is null or doesn't exist
     * 
     * @param dataTable
     * @param incomingColumn
     * @param existingColumn
     * @return
     */
    private DataTableColumn checkForMissingColumn(DataTable dataTable, DataTableColumn incomingColumn, DataTableColumn existingColumn_) {
        DataTableColumn existingColumn = existingColumn_;
        if (existingColumn == null) {
            existingColumn = dataTable.getColumnByName(incomingColumn.getName());
            if (existingColumn == null) {
                throw new TdarRecoverableRuntimeException("datasetService.could_not_find_column", Arrays.asList(incomingColumn.getName(),
                        incomingColumn.getId()));
            }
        }
        return existingColumn;
    }

    /*
     * Checks whether based on an incoming and an existing @link CodingSheet, whether retranslation is necessary
     */
    private boolean isRetranslationNeeded(CodingSheet incomingCodingSheet, CodingSheet existingCodingSheet) {
        getLogger().info("coding(incoming):{} coding(existing):{} equals?:{}", incomingCodingSheet, existingCodingSheet,
                Objects.equals(incomingCodingSheet, existingCodingSheet));
        if (Objects.equals(incomingCodingSheet, existingCodingSheet)) {
            return false;
        } else if (incomingCodingSheet.isGenerated()) {
            return existingCodingSheet != null;
        } else {
            return true;
        }
    }

    /*
     * Exposes the @link DataTable as xml using the postgres xml format.
     * 
     * http://www.postgresql.org/docs/9.1/static/functions-xml.html
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#selectTableAsXml(org.tdar.core.bean.resource.datatable.DataTable)
     */
    @Override
    @Transactional
    public String selectTableAsXml(DataTable dataTable) {
        return tdarDataImportDatabase.selectTableAsXml(dataTable);
    }

    /*
     * Setter for the tdardata postgres database which is not managed by hibernate
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#setTdarDataImportDatabase(org.tdar.db.model.PostgresDatabase)
     */
    @Override
    public void setTdarDataImportDatabase(PostgresDatabase tdarDataImportDatabase) {
        this.tdarDataImportDatabase = tdarDataImportDatabase;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#remapAllColumnsAsync(java.lang.Long, java.lang.Long)
     */
    @Override
    @Transactional(readOnly = false)
    @Async
    public void remapAllColumnsAsync(final Long datasetId) {
        remapAllColumns(find(datasetId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#remapAllColumns(java.lang.Long, java.lang.Long)
     */
    @Override
    @Transactional(readOnly = false)
    public void remapAllColumns(final Long datasetId) {
        remapAllColumns(find(datasetId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMappedCollection(Dataset dataset) {
        return getDao().findMappedCollectionForDataset(dataset) != null;
    }
    
    private void remapAllColumns(Dataset dataset) {
        ResourceCollection collection = getDao().findMappedCollectionForDataset(dataset);
        List<DataTableColumn> columns = new ArrayList<>();
        if (dataset != null && collection != null && CollectionUtils.isNotEmpty(dataset.getDataTables())) {
            for (DataTable datatable : dataset.getDataTables()) {
                if (CollectionUtils.isNotEmpty(datatable.getDataTableColumns())) {
                    for (DataTableColumn col : datatable.getDataTableColumns()) {
                        if (col.isMappingColumn()) {
                            columns.add(col);
                        }
                    }
                }
            }
        }
        remapColumns(columns, collection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#setAnalyzer(org.tdar.filestore.FileAnalyzer)
     */
    @Override
    @Autowired
    public void setAnalyzer(FileAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetService#getAnalyzer()
     */
    @Override
    public FileAnalyzer getAnalyzer() {
        return analyzer;
    }

    @Override
    public Long count() {
        return super.count().longValue();
    }

    @Override
    public List<Dataset> findAll() {
        return getDao().findAllSorted("title asc");
    }

    @Override
    public List<Dataset> findAll(String string) {
        return getDao().findAllSorted(string);
    }

    @Override
    @Transactional(readOnly=true)
    public Set<DataTableColumn> findSearchableColumns(Dataset ds, TdarUser user) {
        return getDao().findAllSearchableColumns(ds, user);
    }
    
    
    @Transactional(readOnly=true)
    @Override
    public Set<String> findAutocompleteValues(Dataset dataset, DataTableColumn column, String value, TdarUser authenticatedUser) {
        TreeSet<String> toReturn = new TreeSet<String>();
        for (String val : column.getValues()) {
            if (StringUtils.containsIgnoreCase(val, value)) {
                toReturn.add(val);
            }
        }
        return toReturn;

        
    }
}
