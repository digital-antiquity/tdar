package org.tdar.core.dao.resource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.ResultSetIterator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.SpreadsheetVersion;
import org.hibernate.CacheMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.HasTables;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceProxy;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.datatable.ColumnVisibility;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.dao.NamedNativeQueries;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.FileProxyWrapper;
import org.tdar.core.service.resource.dataset.DatasetUtils;
import org.tdar.db.conversion.converters.ExcelWorkbookWriter;
import org.tdar.db.conversion.converters.SheetProxy;
import org.tdar.db.datatable.ImportTable;
import org.tdar.db.model.TargetDatabase;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.VersionType;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.utils.DatasetRef;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.XmlEscapeHelper;

import com.redfin.sitemapgenerator.GoogleImageSitemapGenerator;
import com.redfin.sitemapgenerator.GoogleImageSitemapUrl;
import com.redfin.sitemapgenerator.GoogleImageSitemapUrl.ImageTag;

/**
 * $Id$
 * 
 * <p>
 * Provides data integration and access functionality for persistent Datasets.
 * </p>
 * 
 * @author Adam Brin, Allen Lee
 * @version $Revision$
 */
@Component
public class DatasetDao extends ResourceDao<Dataset> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    Pattern originalColumnPattern = Pattern.compile("^(.+)_original_(\\d+)$");

    @Autowired
    private AuthorizationService authorizationService;
    
    public DatasetDao() {
        super(Dataset.class);
    }

    @Autowired
    @Qualifier("target")
    private TargetDatabase tdarDataImportDatabase;

    public String normalizeTableName(String name) {
        return tdarDataImportDatabase.normalizeTableOrColumnNames(name);
    }

    public Map<DataTableColumn, String> getMappedDataForInformationResource(InformationResource resource) {
        String key = resource.getMappedDataKeyValue();
        DataTableColumn column = resource.getMappedDataKeyColumn();
        if (StringUtils.isBlank(key) || (column == null)) {
            return null;
        }
        final DataTable table = column.getDataTable();
        ResultSetExtractor<Map<DataTableColumn, String>> resultSetExtractor = rs -> {
            while (rs.next()) {
                Map<DataTableColumn, String> results = DatasetUtils.convertResultSetRowToDataTableColumnMap(table, true, rs, false);
                return results;
            }
            return null;
        };

        return tdarDataImportDatabase.selectAllFromTableCaseInsensitive(column, key, resultSetExtractor);
    }

    public boolean canLinkDataToOntology(Dataset dataset) {
        if (dataset == null) {
            return false;
        }
        Query query = getCurrentSession().createNamedQuery(QUERY_DATASET_CAN_LINK_TO_ONTOLOGY);
        query.setParameter("datasetId", dataset.getId());
        return !query.getResultList().isEmpty();
    }

    public long countResourcesForUserAccess(Person user) {
        if (user == null) {
            return 0;
        }
        Query<Number> query = getCurrentSession().createNamedQuery(QUERY_USER_GET_ALL_RESOURCES_COUNT, Number.class);
        query.setParameter("userId", user.getId());
        query.setParameter("resourceTypes", Arrays.asList(ResourceType.values()));
        query.setParameter("statuses", Status.values());
        query.setParameter("allStatuses", true);
        query.setParameter("effectivePermission", Permissions.MODIFY_METADATA.getEffectivePermissions() - 1);
        query.setParameter("allResourceTypes", true);
        query.setParameter("admin", false);
        return query.getSingleResult().longValue();
    }

    /**
     * Finds all resource modified in the last X days
     * 
     * @param days
     * @return List<Resource>
     */
    @SuppressWarnings("unchecked")
    public List<Resource> findRecentlyUpdatedItemsInLastXDays(int days) {
        Query query = getCurrentSession().createNamedQuery(QUERY_RECENT);
        query.setParameter("updatedDate", new Date(System.currentTimeMillis() - (86400000l * days)));
        return query.getResultList();
    }

    /**
     * Finds all resource modified in the last X days or that have an empty externalId id field AND
     * that have at least one file
     * 
     * @param days
     * @return List<Resource>
     */
    @SuppressWarnings("unchecked")
    public List<Long> findRecentlyUpdatedItemsInLastXDaysForExternalIdLookup(int days) {
        Query query = getCurrentSession().createNamedQuery(QUERY_EXTERNAL_ID_SYNC);
        query.setParameter("updatedDate", new Date(System.currentTimeMillis() - (86400000l * days)));
        return query.getResultList();
    }

    public void resetColumnMappings(ResourceCollection project) {
        Query query = getCurrentSession().createNativeQuery(TdarNamedQueries.UPDATE_CLEAR_MAPPINGS);
        query.setParameter("collection_id", project.getId() );
        query.executeUpdate();
    }

    /*
     * Take the distinct column values mapped and associate them with files in tDAR based on:
     * - shared project
     * - filename matches column value either (a) with extension or (b) with separator eg: file1.jpg;file2.jpg
     * 
     * Using a raw SQL update statement to try and simplify the execution here to use as few loops as possible...
     */
    public void mapColumnToResource(Dataset dataset, DataTableColumn column, List<String> distinctValues, ResourceCollection collection) {
        // for each distinct column value
        long timestamp = System.currentTimeMillis();
        String sql = String.format("CREATE TEMPORARY TABLE MATCH%s (id bigserial, primary key(id), key varchar(255),actual varchar(255))", timestamp);
        NativeQuery create = getCurrentSession().createNativeQuery(sql);
        logger.debug("map column match table SQL: {}", sql);
        create.executeUpdate();
        if(logger.isTraceEnabled()) {
            for(String value: distinctValues) {
                logger.trace("distinct value:{}", value);
            }
        }
        int count = 0;
        for (String columnValue : distinctValues) {
            List<String> valuesToMatch = new ArrayList<String>();

            // split on delimiter if specified
            if (StringUtils.isNotBlank(column.getDelimiterValue())) {
                valuesToMatch.addAll(Arrays.asList(columnValue.split(column.getDelimiterValue())));
            } else {
                valuesToMatch.add(columnValue);
            }

            // clean up filename and join if delimeter specified
            if (column.isIgnoreFileExtension()) {
                for (int i = 0; i < valuesToMatch.size(); i++) {
//                    valuesToMatch.set(i, FilenameUtils.getBaseName(valuesToMatch.get(i).toLowerCase()) + ".");
                    valuesToMatch.set(i, valuesToMatch.get(i).toLowerCase() + ".");
                }
            }
            for (String match : valuesToMatch) {
                if (StringUtils.isBlank(match)) {
                    continue;
                }
                String format = String.format("insert into MATCH%s (key,actual) values('%s','%s')", timestamp,
                        org.apache.commons.lang.StringEscapeUtils.escapeSql(match.toLowerCase()),
                        org.apache.commons.lang.StringEscapeUtils.escapeSql(columnValue.toLowerCase()));
                NativeQuery insert = getCurrentSession().createNativeQuery(format);
                insert.executeUpdate();
                logger.trace(format);
                count++;
            }
        }
        List<VersionType> types = Arrays.asList(VersionType.UPLOADED, VersionType.ARCHIVAL, VersionType.UPLOADED_ARCHIVAL);
        String filenameCheck = "lower(irfv.filename)";
        if (column.isIgnoreFileExtension()) {
            filenameCheck = "substring(lower(irfv.filename), 0, length(irfv.filename) - length(irfv.extension) + 1)";
        }
        // FIXME: Replace with parameterizd query.
        String format = String.format(
                "update information_resource ir_ set mappeddatakeycolumn_id=%s, mappedDataKeyValue=actual from MATCH%s, information_resource ir inner join "
                        + "information_resource_file irf on ir.id=irf.information_resource_id " +
                        "inner join information_resource_file_version irfv on irf.id=irfv.information_resource_file_id " +
                        "WHERE ir.id in (select resource_id from collection_resource where collection_id =%s or collection_id in (select collection_id from collection_parents where parent_id=%s)) "
                        + " and lower(key)=%s and irfv.internal_type in ('%s') and ir.id=ir_.id and ir.mappedDataKeyValue is null",
                column.getId(), timestamp, collection.getId(), collection.getId(), filenameCheck, StringUtils.join(types, "','"));
        NativeQuery matching = getCurrentSession().createNativeQuery(format);
        int executeUpdate = matching.executeUpdate();
        logger.debug("map column update sql: {}", format);
        logger.debug("{} rows updated", executeUpdate);
    }

    public void unmapAllColumnsInProject(Long collectionId, List<Long> columns) {
        if (CollectionUtils.isEmpty(columns)) {
            return;
        }
        String rawsql = NamedNativeQueries.removeDatasetMappings(collectionId, columns);
        logger.trace(rawsql);
        Query query = getCurrentSession().createNativeQuery(rawsql);
        int executeUpdate = query.executeUpdate();
        logger.debug("{} resources unmapped", executeUpdate);
    }

    public Number getAccessCount(Resource resource) {
        String sql = String.format(TdarNamedQueries.RESOURCE_ACCESS_COUNT_SQL, resource.getId(), new Date());
        return (Number) getCurrentSession().createNativeQuery(sql).getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public List<Long> findAllResourceIdsWithFiles() {
        Query query = getCurrentSession().createNamedQuery(QUERY_INFORMATIONRESOURCES_WITH_FILES);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Resource> findAllSparseActiveResources() {
        Query query = getCurrentSession().createNamedQuery(QUERY_SPARSE_ACTIVE_RESOURCES);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public <I extends Indexable> List<I> findSkeletonsForSearch(boolean trustCache, List<Long> ids) {
        Session session = getCurrentSession();
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.EMPTY_LIST;
        }
        // distinct prevents duplicates
        // left join res.informationResourceFiles
        long time = System.currentTimeMillis();
        Query query = session.createNamedQuery(QUERY_PROXY_RESOURCE_SHORT);
        // if we have more than one ID, then it's faster to do a deeper query (fewer follow-ups)
        if (ids.size() > 1) {
            query = session.createNamedQuery(QUERY_PROXY_RESOURCE_FULL);
        }
        if (!trustCache) {
            query.setCacheable(false);
            query.setCacheMode(CacheMode.REFRESH);
        }
        query.setParameter("ids", ids);
        query.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        List<ResourceProxy> results = query.getResultList();
        long queryTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
        List<I> toReturn = new ArrayList<>();
        Map<Long, I> resultMap = new HashMap<>();
        logger.trace("convert proxy to resource");
        for (ResourceProxy prox : results) {
            try {
                resultMap.put(prox.getId(), (I) prox.generateResource());
            } catch (Exception e) {
                logger.error("{}", e);
            }
        }
        logger.trace("resorting results");
        for (Long id : ids) {
            toReturn.add(resultMap.get(id));
        }
        time = System.currentTimeMillis() - time;
        logger.trace("Query: {} ; generation: {} ", queryTime, time);

        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public List<Resource> findOld(Long[] ids) {
        Session session = getCurrentSession();
        long time = System.currentTimeMillis();
        Query query = session.createNamedQuery(QUERY_RESOURCE_FIND_OLD_LIST);
        query.setParameter("ids", Arrays.asList(ids));
        List<Resource> results = query.getResultList();
        logger.info("query took: {} ", System.currentTimeMillis() - time);
        return results;
    }

    public int findAllResourcesWithPublicImagesForSitemap(GoogleImageSitemapGenerator gisg) {
        Query query = getCurrentSession().createNativeQuery(SELECT_RAW_IMAGE_SITEMAP_FILES);
        query.setCacheMode(CacheMode.IGNORE);
        int count = 0;
        logger.trace(SELECT_RAW_IMAGE_SITEMAP_FILES);
        ScrollableResults scroll = query.scroll(ScrollMode.FORWARD_ONLY);
        while (scroll.next()) {
            // select r.id, r.title, r.description, r.resource_type, irf.description, irfv.id
            if (count % 500 == 0) {
                clearCurrentSession();
            }
            Number id = (Number) scroll.get(0);
            String title = (String) scroll.get(1);
            String description = (String) scroll.get(2);
            ResourceType resourceType = ResourceType.valueOf((String) scroll.get(3));
            String fileDescription = (String) scroll.get(4);
            Number imageId = (Number) scroll.get(5);
            try {
                Resource res = resourceType.getResourceClass().newInstance();
                res.setTitle(title);
                res.setId(id.longValue());
                markReadOnly(res);
                String resourceUrl = UrlService.absoluteUrl(res);
                String imageUrl = UrlService.thumbnailUrl(imageId.longValue());
                if (StringUtils.isNotBlank(fileDescription)) {
                    description = fileDescription;
                }
                ImageTag tag = new GoogleImageSitemapUrl.ImageTag(new URL(imageUrl)).title(cleanupXml(title)).caption(cleanupXml(description));
                GoogleImageSitemapUrl iurl = new GoogleImageSitemapUrl.Options(new URL(resourceUrl)).addImage(tag).build();
                gisg.addUrl(iurl);
                count++;
            } catch (Exception e) {
                logger.error("error in url generation for sitemap {}", e);
            }
        }
        return count;
    }

    private String cleanupXml(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        XmlEscapeHelper xse = new XmlEscapeHelper(-1L);
        return StringEscapeUtils.escapeXml11(xse.stripNonValidXMLCharacters(text));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Resource> findByTdarYear(SearchResultHandler handler, int year) {
        Query query = getCurrentSession().createNamedQuery(TdarNamedQueries.FIND_BY_TDAR_YEAR);
        if (handler.getRecordsPerPage() < 0) {
            handler.setRecordsPerPage(250);
        }
        query.setMaxResults(handler.getRecordsPerPage());
        if (handler.getStartRecord() < 0) {
            handler.setStartRecord(0);
        }
        query.setFirstResult(handler.getStartRecord());

        DateTime dt = new DateTime(year, 1, 1, 0, 0, 0, 0);
        query.setParameter("year_start", dt.toDate());
        query.setParameter("year_end", dt.plusYears(1).toDate());
        Query query2 = getCurrentSession().createNamedQuery(TdarNamedQueries.FIND_BY_TDAR_YEAR_COUNT);
        query2.setParameter("year_start", dt.toDate());
        query2.setParameter("year_end", dt.plusYears(1).toDate());
        Number max = (Number) query2.getSingleResult();
        handler.setTotalRecords(max.intValue());

        return query.getResultList();
    }

    public void deleteRelationships(Set<DataTableRelationship> relationshipsToRemove) {
        List<Long> ids = PersistableUtils.extractIds(relationshipsToRemove);
        ids.removeAll(Collections.singleton(null));
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        Query query = getCurrentSession().createNamedQuery(TdarNamedQueries.DELETE_DATA_TABLE_COLUMN_RELATIONSHIPS);
        query.setParameter("ids", ids);
        query.executeUpdate();
        Query query2 = getCurrentSession().createNamedQuery(TdarNamedQueries.DELETE_DATA_TABLE_RELATIONSHIPS);
        query2.setParameter("ids", ids);
        query2.executeUpdate();
    }

    public void cleanupUnusedTablesAndColumns(HasTables dataset, Collection<DataTable> tablesToRemove, Collection<DataTableColumn> columnsToRemove) {
        logger.info("deleting unmerged tables: {}", tablesToRemove);
        ArrayList<DataTableColumn> columnsToUnmap = new ArrayList<DataTableColumn>();
        if (CollectionUtils.isNotEmpty(columnsToRemove)) {
            for (DataTableColumn column : columnsToRemove) {
                columnsToUnmap.add(column);
            }
            // first unmap all columns from the removed tables
            if (dataset.isDataMappingSupported()) {
                ResourceCollection rc = findMappedCollectionForDataset((Dataset)dataset);
                if(PersistableUtils.isNotTransient(rc)) {
                    logger.info("Removing data mappings from resources in ResourceCollection: {}", rc);
                    unmapAllColumnsInProject(rc.getId(), PersistableUtils.extractIds(columnsToUnmap));
                } else {
                    logger.warn("expected a resource collection mapped to dataset {}, but none found", dataset );
                }
            }
            for (DataTableColumn column : columnsToRemove) {
                column.getDataTable().getDataTableColumns().remove(column);
            }

            delete(columnsToRemove);
        }
        if (CollectionUtils.isNotEmpty(tablesToRemove)) {
            logger.debug("deleting tables: {}", tablesToRemove);
            dataset.getDataTables().removeAll(tablesToRemove);
        }

    }

    //FIXME: This should be converted to find resources underneath a collection, not project
    public ScrollableResults findMappedResources(Project ignored) {
        Query query = getCurrentSession().createNamedQuery(MAPPED_RESOURCES);
        ScrollableResults scroll = query.scroll(ScrollMode.FORWARD_ONLY);
        return scroll;
    }

    public ScrollableResults findAllResourceWithProjectsScrollable() {
        Query query = getCurrentSession().createQuery("from InformationResource ir where ir.project is not null");
        query.scroll(ScrollMode.FORWARD_ONLY);
        ;
        return query.scroll(ScrollMode.FORWARD_ONLY);
    }

    public Number countMappedResources() {
        Query<Number> query = getCurrentSession().createNamedQuery(COUNT_MAPPED_RESOURCES, Number.class);
        return query.getSingleResult();
    }

    public void remapColumns(List<DataTableColumn> columns, Dataset dataset, ResourceCollection collection) {
        getLogger().info("remapping columns: {} in {} ", columns, collection);
        if (CollectionUtils.isNotEmpty(columns) && (collection != null)) {
            resetColumnMappings(collection);
            // mapping columns to the resource runs a raw sql update, refresh the state of the Project.
            refresh(collection);
            // have to reindex...
            /*
             * Take the distinct column values mapped and associate them with files in tDAR based on:
             * - shared project
             * - filename matches column value either (a) with extension or (b) with separator eg: file1.jpg;file2.jpg
             * NOTE: a manual reindex happens at the end
             */
            for (DataTableColumn column : columns) {
                mapColumnToResource(dataset, column, tdarDataImportDatabase.selectNonNullDistinctValues(column.getDataTable(), column, false), collection);
            }
        }

    }

    public boolean translate(DataTableColumn column, CodingSheet codingSheet) {
        if (codingSheet == null) {
            return false;
        }
        getLogger().debug("translating {} with {}", column.getName(), codingSheet);
        // FIXME: if we eventually offer on-the-fly coding sheet translation we cannot modify the actual dataset in place
        tdarDataImportDatabase.translateInPlace(column, codingSheet);
        return true;
    }

    public void retranslate(Dataset dataset) {
        for (DataTable table : dataset.getDataTables()) {
            retranslate(table.getDataTableColumns());
        }
    }

    public boolean retranslate(DataTableColumn column) {
        untranslate(column);
        return translate(column, column.getDefaultCodingSheet());
    }

    public void untranslate(DataTableColumn column) {
        tdarDataImportDatabase.untranslate(column);
    }

    public void translate(Set<DataTableColumn> columns, CodingSheet codingSheet) {
        for (DataTableColumn column : columns) {
            translate(column, codingSheet);
        }
    }

    public void retranslate(Collection<DataTableColumn> columns) {
        for (DataTableColumn column : columns) {
            retranslate(column);
        }
    }

    public InformationResourceFile createTranslatedFile(Dataset dataset, FileAnalyzer analyzer, InformationResourceFileDao informationResourceFileDao) {
        // assumes that Datasets only have a single file
        Set<InformationResourceFile> activeFiles = dataset.getActiveInformationResourceFiles();
        InformationResourceFile file = null;
        if (!activeFiles.isEmpty()) {
            file = dataset.getActiveInformationResourceFiles().iterator().next();
        }

        if (file == null) {
            getLogger().warn("Trying to translate {} with a null file payload.", dataset);
            return null;
        }
        informationResourceFileDao.deleteTranslatedFiles(dataset);

        InformationResourceFile irFile = null;
        FileOutputStream translatedFileOutputStream = null;
        try {
            File tempFile = File.createTempFile("translated", ".xlsx", TdarConfiguration.getInstance().getTempDirectory());
            translatedFileOutputStream = new FileOutputStream(tempFile);

            SheetProxy sheetProxy = toExcel(dataset, translatedFileOutputStream);
            String filename = FilenameUtils.getBaseName(file.getLatestUploadedVersion().getFilename()) + "_translated." + sheetProxy.getExtension();
            FileProxy fileProxy = new FileProxy(filename, tempFile, VersionType.TRANSLATED, FileAction.ADD_DERIVATIVE);
            fileProxy.setRestriction(file.getRestriction());
            fileProxy.setFileId(file.getId());
            FileProxyWrapper wrapper = new FileProxyWrapper(dataset, analyzer, this, Arrays.asList(fileProxy));
            wrapper.processMetadataForFileProxies();
            irFile = fileProxy.getInformationResourceFile();
            // InformationResourceFileVersion version = new InformationResourceFileVersion(VersionType.TRANSLATED, filename, irFile);
            // irFile.getInformationResourceFileVersions().add(version);
            // TdarConfiguration.getInstance().getFilestore().store(FilestoreObjectType.RESOURCE, tempFile, version);
            // informationResourceFileDao.saveOrUpdate(version);
        } catch (IOException ioe) {
            getLogger().error("Unable to create translated file for Dataset: " + dataset, ioe);
        } finally {
            IOUtils.closeQuietly(translatedFileOutputStream);
        }
        return irFile;
    }

    /*
     * Converts a @link Dataset to a Microsoft Excel File; this includes the Translated data values
     */
    private SheetProxy toExcel(Dataset dataset, OutputStream outputStream) throws IOException {
        Set<DataTable> dataTables = dataset.getDataTables();
        ExcelWorkbookWriter workbookWriter = new ExcelWorkbookWriter();

        if ((dataTables == null) || dataTables.isEmpty()) {
            return null;
        }
        Set<String> tableNames = new HashSet<>();
        final SheetProxy proxy = new SheetProxy(SpreadsheetVersion.EXCEL2007, true);
        for (final DataTable dataTable : dataTables) {
            // each table becomes a sheet.
            String tableName = dataTable.getDisplayName();
            getLogger().debug("{} ({})", dataTable.getName(), dataTable.getId());
            tableName = getUniqueTableName(tableNames, tableName);
            proxy.setName(tableName);
            tableNames.add(tableName);
            ResultSetExtractor<Boolean> excelExtractor = new ResultSetExtractor<Boolean>() {
                @Override
                public Boolean extractData(ResultSet resultSet) throws SQLException {
                    List<String> headerLabels = getColumnNames(resultSet, dataTable);
                    proxy.setHeaderLabels(headerLabels);
                    proxy.setData(new ResultSetIterator(resultSet));
                    getLogger().debug("column names: " + headerLabels);
                    workbookWriter.addSheets(proxy);

                    return true;
                }
            };
            dataTable.getDataTableColumns().forEach(dataTableColumn -> {
                getLogger().debug("\t{} (sheet: {})", dataTableColumn, dataTableColumn.getDefaultCodingSheet());
            });
            tdarDataImportDatabase.selectAllFromTableInImportOrder(dataTable, excelExtractor, true);
        }
        BufferedOutputStream stream = new BufferedOutputStream(outputStream);
        proxy.getWorkbook().write(stream);
        proxy.getWorkbook().close();
        IOUtils.closeQuietly(stream);

        return proxy;
    }

    public String getUniqueTableName(Set<String> tableNames, String originalTableName) {
        String tableName = originalTableName;
        if (tableNames.contains(tableName)) {
            String tablename_ = tableName;
            int count = 1;
            while (tableNames.contains(tablename_)) {
                tablename_ = String.format("%s (%s)", tableName, count);
                count++;
            }
            tableName = tablename_;
        }
        return tableName;
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
            String lookupName = columnName;
            Matcher match = originalColumnPattern.matcher(columnName);
            String suffix = "";
            if (match.matches()) {
                lookupName = match.group(1);
                suffix = " (original)";
            }
            DataTableColumn column = dataTable.getColumnByName(lookupName);
            logger.trace("name: {} - {}", columnName, column);
            if (column != null) {
                columnName = column.getDisplayName();
            }
            columnName += suffix;

            columnNames.add(columnName);
        }
        return columnNames;
    }

    public boolean checkExists(ImportTable dataTable) {
        return tdarDataImportDatabase.checkTableExists(dataTable);
    }

    public Collection<? extends AuthorizedUser> findAllAuthorizedUsersForResource(Long id) {
        Query query = getCurrentSession().createNamedQuery(AUTHORIZED_USERS_FOR_RESOURCE);
        query.setParameter("id", id);
        return query.list();
    }

    public List<DatasetRef> findAllMappedSearchableDatasets(TdarUser user) {
        Query<Dataset> query = getCurrentSession().createNamedQuery(TdarNamedQueries.MAPPED_DATASETS, Dataset.class);
        List<DatasetRef> refs = new ArrayList<>();
        for (Dataset dataset : query.list()) {
            DatasetRef ref = new DatasetRef();
            ref.setId(dataset.getId());
            ref.setTitle(dataset.getTitle());
            ref.setColumns(findAllSearchableColumns(dataset, user));
            List<Long> ids = new ArrayList<>();
            // build the tree of collections that we should show this for
            for (ResourceCollection p : dataset.getManagedResourceCollections()) {
                List<Long> treeIds = new ArrayList<>();
                for (ResourceCollection c : p.getHierarchicalResourceCollections()) {
                    treeIds.add(c.getId());
                    if (Objects.equals(c.getDataset(), dataset)) {
                        ids.addAll(treeIds);
                        treeIds.clear();
                    }
                }
            }
            
            refs.add(ref);
        }
        return refs;
    }

    public Set<DataTableColumn> findAllSearchableColumns(Dataset ds, TdarUser user) {
        Set<DataTableColumn> cols = new HashSet<>();
        boolean canViewConfidentialInformation = authorizationService.canViewConfidentialInformation(user, ds);
        ds.getDataTables().forEach(dt -> {
            dt.getDataTableColumns().forEach(dtc -> {
                if (dtc.isSearchField() && 
                        (dtc.getVisible() == null 
                        || dtc.getVisible() == ColumnVisibility.VISIBLE
                        || dtc.getVisible() == ColumnVisibility.CONFIDENTIAL && canViewConfidentialInformation)) {
                    cols.add(dtc);
                }

            });
        });
        return cols;
        
    }

    public ResourceCollection findMappedCollectionForDataset(Dataset dataset) {
        Query<ResourceCollection> query = getCurrentSession().createNamedQuery(TdarNamedQueries.MAPPED_COLLECTIONS, ResourceCollection.class);
        query.setParameter("datasetId", dataset.getId());
        return query.uniqueResult();
    }

}
