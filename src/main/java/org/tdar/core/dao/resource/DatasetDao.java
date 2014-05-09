package org.tdar.core.dao.resource;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceProxy;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.statistics.ResourceAccessStatistic;
import org.tdar.core.dao.NamedNativeQueries;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.service.RssService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.resource.dataset.DatasetUtils;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.search.query.SearchResultHandler;

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

    public DatasetDao() {
        super(Dataset.class);
    }

    @Autowired
    private TargetDatabase tdarDataImportDatabase;

    public String normalizeTableName(String name) {
        return tdarDataImportDatabase.normalizeTableOrColumnNames(name);
    }

    public void assignMappedDataForInformationResource(InformationResource resource) {
        String key = resource.getMappedDataKeyValue();
        DataTableColumn column = resource.getMappedDataKeyColumn();
        if (StringUtils.isBlank(key) || (column == null)) {
            return;
        }
        final DataTable table = column.getDataTable();
        ResultSetExtractor<Map<DataTableColumn, String>> resultSetExtractor = new ResultSetExtractor<Map<DataTableColumn, String>>() {
            @Override
            public Map<DataTableColumn, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
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

    public boolean canLinkDataToOntology(Dataset dataset) {
        if (dataset == null) {
            return false;
        }
        Query query = getCurrentSession().getNamedQuery(QUERY_DATASET_CAN_LINK_TO_ONTOLOGY);
        query.setLong("datasetId", dataset.getId());
        return !query.list().isEmpty();
    }

    public long countResourcesForUserAccess(Person user) {
        if (user == null) {
            return 0;
        }
        Query query = getCurrentSession().getNamedQuery(QUERY_USER_GET_ALL_RESOURCES_COUNT);
        query.setLong("userId", user.getId());
        query.setParameterList("resourceTypes", Arrays.asList(ResourceType.values()));
        query.setParameterList("statuses", Status.values());
        query.setParameter("allStatuses", true);
        query.setParameter("effectivePermission", GeneralPermissions.MODIFY_METADATA.getEffectivePermissions() - 1);
        query.setParameter("allResourceTypes", true);
        query.setParameter("admin", false);
        return (Long) query.iterate().next();
    }

    /**
     * Finds all resource modified in the last X days
     * 
     * @param days
     * @return List<Resource>
     */
    @SuppressWarnings("unchecked")
    public List<Resource> findRecentlyUpdatedItemsInLastXDays(int days) {
        Query query = getCurrentSession().getNamedQuery(QUERY_RECENT);
        query.setDate("updatedDate", new Date(System.currentTimeMillis() - (86400000l * days)));
        return query.list();
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
        Query query = getCurrentSession().getNamedQuery(QUERY_EXTERNAL_ID_SYNC);
        query.setDate("updatedDate", new Date(System.currentTimeMillis() - (86400000l * days)));
        query.setCacheMode(CacheMode.IGNORE);
        return query.list();
    }

    /*
     * Take the distinct column values mapped and associate them with files in tDAR based on:
     * - shared project
     * - filename matches column value either (a) with extension or (b) with separator eg: file1.jpg;file2.jpg
     * 
     * Using a raw SQL update statement to try and simplify the execution here to use as few loops as possible...
     */
    public List<String> mapColumnToResource(DataTableColumn column, List<String> distinctValues) {
        Project project = column.getDataTable().getDataset().getProject();
        // for each distinct column value
        List<String> updatedValues = new ArrayList<String>();
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
                    valuesToMatch.set(i, FilenameUtils.getBaseName(valuesToMatch.get(i).toLowerCase()) + ".");
                }
            }
            String rawsql = NamedNativeQueries.updateDatasetMappings(project, column, columnValue, valuesToMatch,
                    Arrays.asList(VersionType.UPLOADED,
                            VersionType.ARCHIVAL, VersionType.UPLOADED_ARCHIVAL));
            logger.trace(rawsql);
            Query query = getCurrentSession().createSQLQuery(rawsql);
            int executeUpdate = query.executeUpdate();
            if (executeUpdate > 0) {
                updatedValues.add(columnValue);
            }
            logger.debug("values to match {}  -- {} ", valuesToMatch, executeUpdate);
        }
        return updatedValues;
    }

    public void unmapAllColumnsInProject(Project project, Collection<DataTableColumn> columns) {
        if (CollectionUtils.isEmpty(columns)) {
            return;
        }
        String rawsql = NamedNativeQueries.removeDatasetMappings(project, columns);
        logger.trace(rawsql);
        Query query = getCurrentSession().createSQLQuery(rawsql);
        int executeUpdate = query.executeUpdate();
        logger.debug("{} resources unmapped", executeUpdate);
    }

    public Number getAccessCount(Resource resource) {
        Criteria createCriteria = getCriteria(ResourceAccessStatistic.class).setProjection(Projections.rowCount())
                .add(Restrictions.eq("reference", resource));
        return (Number) createCriteria.list().get(0);
    }

    @SuppressWarnings("unchecked")
    public List<Long> findAllResourceIdsWithFiles() {
        Query query = getCurrentSession().getNamedQuery(QUERY_INFORMATIONRESOURCES_WITH_FILES);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Resource> findAllSparseActiveResources() {
        Query query = getCurrentSession().getNamedQuery(QUERY_SPARSE_ACTIVE_RESOURCES);
        return query.list();
    }

    public List<Resource> findSkeletonsForSearch(Long... ids) {
        Session session = getCurrentSession();
        // distinct prevents duplicates
        // left join res.informationResourceFiles
        long time = System.currentTimeMillis();
        Query query = session.getNamedQuery(QUERY_PROXY_RESOURCE_SHORT);
        // if we have more than one ID, then it's faster to do a deeper query (fewer follow-ups)
        if (ids.length > 1) {
            query = session.getNamedQuery(QUERY_PROXY_RESOURCE_FULL);
        }
        query.setParameterList("ids", Arrays.asList(ids));
        query.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        List<ResourceProxy> results = query.list();
        long queryTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
        List<Resource> toReturn = new ArrayList<>();
        Map<Long, Resource> resultMap = new HashMap<>();
        logger.debug("convert proxy to resource");
        for (ResourceProxy prox : results) {
            try {
                resultMap.put(prox.getId(), prox.generateResource());
            } catch (Exception e) {
                logger.error("{}", e);
            }
        }
        logger.debug("resorting results");
        for (Long id : ids) {
            toReturn.add(resultMap.get(id));
        }
        if (logger.isDebugEnabled()) {
            time = System.currentTimeMillis() - time;
            logger.info("Query: {} ; generation: {} {}->{}", queryTime, time, results.size(), toReturn.size());
        }
        return toReturn;
    }

    public List<Resource> findOld(Long[] ids) {
        Session session = getCurrentSession();
        long time = System.currentTimeMillis();
        Query query = session.getNamedQuery(QUERY_RESOURCE_FIND_OLD_LIST);
        query.setParameterList("ids", Arrays.asList(ids));
        List<Resource> results = query.list();
        logger.info("query took: {} ", System.currentTimeMillis() - time);
        return results;
    }

    public int findAllResourcesWithPublicImagesForSitemap(GoogleImageSitemapGenerator gisg) {
        Query query = getCurrentSession().createSQLQuery(SELECT_RAW_IMAGE_SITEMAP_FILES);
        int count = 0;
        logger.trace(SELECT_RAW_IMAGE_SITEMAP_FILES);
        for (Object[] row : (List<Object[]>) query.list()) {
            // select r.id, r.title, r.description, r.resource_type, irf.description, irfv.id
            Number id = (Number) row[0];
            String title = (String) row[1];
            String description = (String) row[2];
            ResourceType resourceType = ResourceType.valueOf((String) row[3]);
            String fileDescription = (String) row[4];
            Number imageId = (Number) row[5];

            String resourceUrl = UrlService.absoluteUrl(resourceType.getUrlNamespace(), id.longValue());
            String imageUrl = UrlService.thumbnailUrl(imageId.longValue());
            if (StringUtils.isNotBlank(fileDescription)) {
                description = fileDescription;
            }
            try {
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
        return StringEscapeUtils.escapeXml(RssService.stripInvalidXMLCharacters(text));
    }

    @SuppressWarnings("unchecked")
    public List<Resource> findByTdarYear(SearchResultHandler handler, int year) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_BY_TDAR_YEAR);
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
        Query query2 = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_BY_TDAR_YEAR_COUNT);
        query2.setParameter("year_start", dt.toDate());
        query2.setParameter("year_end", dt.plusYears(1).toDate());
        Number max = (Number) query2.uniqueResult();
        handler.setTotalRecords(max.intValue());
        
        return query.list();
    }

}
