package org.tdar.core.dao.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.statistics.ResourceAccessStatistic;
import org.tdar.core.dao.NamedNativeQueries;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ReflectionService;

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

    @SuppressWarnings("unchecked")
    public List<Resource> findResourceLinkedValues(Class<?> cls) {
        String name = ReflectionService.cleanupMethodName(cls.getSimpleName() + "s");
        if (Keyword.class.isAssignableFrom(cls)) {
            String prop = "label";
            Criteria createCriteria = getCriteria(Resource.class).setProjection(Projections.distinct(Projections.property("kwd." + prop)))
                    .add(Restrictions.eq("status", Status.ACTIVE)).createAlias(name, "kwd", Criteria.INNER_JOIN).addOrder(Order.asc("kwd." + prop));
            return createCriteria.list();
        }
        // if (Creator.class.isAssignableFrom(cls)) {
        // String prop = "properName";
        // Criteria createCriteria = getCriteria(ResourceCreator.class).setProjection(Projections.distinct(Projections.property("rc." + prop)))
        // .createAlias("creator", "rc")
        // .createCriteria("resource").add(Restrictions.eq("status", Status.ACTIVE)).addOrder(Order.asc("rc." + prop));
        // return createCriteria.list();
        //
        // }
        throw new TdarRecoverableRuntimeException("passed a class we didn't know what to do with");

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

//    @SuppressWarnings("unchecked")
//    public List<InformationResource> findByFilename(List<String> valuesToMatch, DataTableColumn column) {
//        List<String> filenames = new ArrayList<String>();
//        for (String value : valuesToMatch) {
//            if (column.isIgnoreFileExtension()) {
//                filenames.add(FilenameUtils.getBaseName(value.toLowerCase()) + ".");
//            } else {
//                filenames.add(value.toLowerCase());
//            }
//        }
//        Query query = getCurrentSession().getNamedQuery(QUERY_MATCHING_FILES);
//        query.setParameterList("filenamesToMatch", filenames);
//        query.setParameter("projectId", column.getDataTable().getDataset().getProject().getId());
//        query.setParameterList("versionTypes", Arrays.asList(VersionType.UPLOADED,
//                VersionType.ARCHIVAL, VersionType.UPLOADED_ARCHIVAL));
//        List<InformationResource> toReturn = new ArrayList<InformationResource>();
//        for (InformationResourceFileVersion version : (List<InformationResourceFileVersion>) query.list()) {
//            // add checks for (a) latest version (b) matching is correct
//            toReturn.add(version.getInformationResourceFile().getInformationResource());
//        }
//        logger.debug("find by filename: " + toReturn);
//        return toReturn;
//    }

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

    public List<Long> findAllResourceIdsWithFiles() {
        Query query = getCurrentSession().getNamedQuery(QUERY_INFORMATIONRESOURCES_WITH_FILES);
        return query.list();
    }

    public List<Resource> findAllSparseActiveResources() {
        Query query = getCurrentSession().getNamedQuery(QUERY_SPARSE_ACTIVE_RESOURCES);
        return query.list();
    }

}
