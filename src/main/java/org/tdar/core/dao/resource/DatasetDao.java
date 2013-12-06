package org.tdar.core.dao.resource;

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
import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
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
import org.tdar.core.service.resource.dataset.DatasetUtils;
import org.tdar.db.model.abstracts.TargetDatabase;

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
        if (StringUtils.isBlank(key) || column == null) {
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
        if (dataset == null)
            return false;
        Query query = getCurrentSession().getNamedQuery(QUERY_DATASET_CAN_LINK_TO_ONTOLOGY);
        query.setLong("datasetId", dataset.getId());
        return !query.list().isEmpty();
    }

    public long countResourcesForUserAccess(Person user) {
        if (user == null)
            return 0;
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
        query.setDate("updatedDate", new Date(System.currentTimeMillis() - 86400000 * days));
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
        query.setDate("updatedDate", new Date(System.currentTimeMillis() - 86400000 * days));
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
        if (CollectionUtils.isEmpty(columns))
            return;
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

    public List<Resource> findSkeletonsForSearch(Long ... ids) {
        Session session = getCurrentSession();
        //distinct prevents duplicates
        //left join res.informationResourceFiles
        long time = System.currentTimeMillis();
        String queryString = "select distinct res from ResourceProxy res ";

        //if we have more than one ID, then it's faster to do a deeper query (fewer follow-ups)
        if (ids.length > 1) {
            queryString += "fetch all properties left join fetch res.resourceCreators rc left join fetch res.latitudeLongitudeBoxes left join fetch rc.creator left join fetch res.informationResourceFileProxies ";
        }
        queryString += "where res.id in (:ids)";

        Query query = session.createQuery(queryString);
        query.setParameterList("ids", Arrays.asList(ids));
        List<ResourceProxy> results = (List<ResourceProxy>)query.list();
        logger.info("query took: {} ", System.currentTimeMillis() - time);
        time = System.currentTimeMillis();
        List<Resource> toReturn = new ArrayList<>();
        Map<Long, Resource> resultMap = new HashMap<>();
        for (ResourceProxy prox : results) {
            try {
                resultMap.put(prox.getId(), prox.generateResource());
            } catch (Exception e) {
                logger.error("{}", e);
            }
        }
        for (Long id : ids) {
            toReturn.add(resultMap.get(id));
        }
        
        logger.info("generation took: {} {}", System.currentTimeMillis() - time,toReturn.size());
        return toReturn;
    }

    public List<Resource> findOld(Long[] ids) {
        Session session = getCurrentSession();
        long time = System.currentTimeMillis();
        Query query = session.createQuery("select distinct res from Resource res where res.id in (:ids)");
        query.setParameterList("ids", Arrays.asList(ids));
        List<Resource> results = (List<Resource>)query.list();
        logger.info("query took: {} ", System.currentTimeMillis() - time);
        return results;
    }

}
