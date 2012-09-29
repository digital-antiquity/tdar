package org.tdar.core.dao;

public interface TdarNamedQueries {
    public static final String QUERY_IS_ONTOLOGY_MAPPED = "ontology.isMapped";
    public static final String QUERY_IS_ONTOLOGY_MAPPED_TO_COLUMN = "ontology.isMappedToColumn";
    public static final String QUERY_READ_ONLY_FULLUSER_PROJECTS = "fullUser.projects";
    public static final String QUERY_FULLUSER_RESOURCES = "fullUser.resources";
    public static final String QUERY_FULLUSER_DATASET = "fullUser.datasets";
    public static final String QUERY_READUSER_PROJECTS = "readUser.projects";
    public static final String QUERY_SPARSE_RECENT_EDITS = "resources.recent";
    public static final String QUERY_CONTRIBUTORREQUEST_PENDING = "contributorRequest.pending";
    public static final String QUERY_BOOKMARKEDRESOURCE_IS_ALREADY_BOOKMARKED = "bookmarkedResource.isAlreadyBookmarked";
    public static final String QUERY_BOOKMARKEDRESOURCE_REMOVE_BOOKMARK = "bookmarkedResource.removeBookmark";
    public static final String QUERY_BOOKMARKEDRESOURCE_FIND_RESOURCE_BY_PERSON = "bookmarkedResource.findResourcesByPerson";
    public static final String QUERY_DATASET_CAN_LINK_TO_ONTOLOGY = "dataset.canLinkToOntology";
    public static final String QUERY_DATATABLE_IDLIST = "dataTable.idlist";
    public static final String QUERY_DATATABLE_RELATED_ID = "dataTable.relatedId";
    public static final String QUERY_DATATABLECOLUMN_WITH_DEFAULT_ONTOLOGY = "dataTableColumn.withDefaultOntology";
    public static final String QUERY_INFORMATIONRESOURCE_FIND_BY_FILENAME = "informationResource.findByFileName";
    public static final String QUERY_ONTOLOGYNODE_ALL_CHILDREN_WITH_WILDCARD = "ontologyNode.allChildrenWithIndexWildcard";
    public static final String QUERY_ONTOLOGYNODE_ALL_CHILDREN = "ontologyNode.allChildren";
    public static final String QUERY_PROJECT_CODINGSHEETS = "project.codingSheets";
    public static final String QUERY_PROJECT_DATASETS = "project.datasets";
    public static final String QUERY_PROJECT_DOCUMENTS = "project.documents";
    public static final String QUERY_PROJECT_INDEPENDENTRESOURCES_PROJECTS = "project.independentResourcesProject";
    public static final String QUERY_PROJECT_EDITABLE = "project.editable";
    public static final String QUERY_PROJECT_VIEWABLE = "project.viewable";
    public static final String QUERY_PROJECT_ALL_OTHER = "project.all.other";
    public static final String QUERY_RESOURCE_RESOURCETYPE = "resource.resourceType";
    public static final String QUERY_RESOURCE_MODIFIED_SINCE = "resource.modifiedSince";
    public static final String QUERY_SPARSE_RESOURCES = "all.resources";
    public static final String QUERY_SPARSE_RESOURCES_SUBMITTER = "submitter.resources";
    public static final String QUERY_PROJECT_COUNT_INTEGRATABLE_DATASETS = "project.countIntegratableDatasets";
    public static final String QUERY_PROJECTS_COUNT_INTEGRATABLE_DATASETS = "projects.countIntegratableDatasets";
    public static final String QUERY_READ_ONLY_EDITABLE_PROJECTS = "sparse.editableProject";
    public static final String QUERY_MANAGED_ISO_COUNTRIES = "resource.iso_code";
    public static final String QUERY_ACTIVE_RESOURCE_TYPE_COUNT = "resource.typeCounts";
    public static final String QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS = "resource.countByTypeAndStatus";
    public static final String QUERY_USER_GET_ALL_RESOURCES_COUNT = "resource.active.count";
    public static final String QUERY_SPARSE_EMPTY_PROJECTS = "projects.empty";
    public static final String QUERY_RESOURCE_EDITABLE = "resource.editable";
    public static final String QUERY_SPARSE_PROJECTS = "project.all.sparse";
    public static final String QUERY_IS_ALLOWED_TO = "resource.isEditable";
    public static final String QUERY_IS_ALLOWED_TO_NEW = "resource.isEditable2";
    public static final String QUERY_IS_ALLOWED_TO_MANAGE = "resourcecollection.isEditable";
    public static final String QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS_BY_USER = "dashboard.resourceByPerson";
    public static final String QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO = "rescol.accessible";
    public static final String QUERY_SPARSE_EDITABLE_RESOURCES = "resource.editable.sparse";
    public static final String QUERY_COLLECTION_BY_PARENT = "collection.parent";
    public static final String QUERY_COLLECTION_BY_AUTH_OWNER = "collection.authOwnerId_name";
    public static final String QUERY_COLLECTION_PUBLIC_WITH_HIDDEN_PARENT = "collection.hiddenParent";
    public static final String QUERY_EXTERNAL_ID_SYNC = "resource.externalId";
    public static final String QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_CONTROLLED = "adminStats.cultureKeywordControlled";
    public static final String QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_UNCONTROLLED = "adminStats.cultureKeywordUnControlled";
    public static final String QUERY_KEYWORD_COUNT_GEOGRAPHIC_KEYWORD = "adminStats.geographicKeyword";
    public static final String QUERY_KEYWORD_COUNT_INVESTIGATION_TYPE = "adminStats.investigationType";
    public static final String QUERY_KEYWORD_COUNT_MATERIAL_KEYWORD = "adminStats.materialKeyword";
    public static final String QUERY_KEYWORD_COUNT_OTHER_KEYWORD = "adminStats.otherKeyword";
    public static final String QUERY_KEYWORD_COUNT_SITE_NAME_KEYWORD = "adminStats.siteNameKeyword";
    public static final String QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_CONTROLLED = "adminStats.siteTypeKeywordControlled";
    public static final String QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_UNCONTROLLED = "adminStats.siteTypeKeywordUnControlled";
    public static final String QUERY_KEYWORD_COUNT_TEMPORAL_KEYWORD = "adminStats.temporalKeyword";
    public static final String QUERY_KEYWORD_COUNT_FILE_EXTENSION = "adminStats.fileExtensions";
    public static final String QUERY_RECENT_USERS_ADDED = "adminStats.recentUsers";
    public static final String QUERY_RECENT = "adminStats.recentFiles";
    public static final String QUERY_MATCHING_FILES = "datasetRelated.Files";
    public static final String QUERY_USAGE_STATS = "adminStats.usage";
    public static final String QUERY_FILE_STATS = "adminStats.fileDetails";
    public static final String QUERY_DASHBOARD = "dashboard.sql";
 

    // raw SQL/HQL queries
    public static final String QUERY_SQL_DASHBOARD = 
            "select id, status, resource_type from resource " +
            "where id in " +
            "(select resource_id from collection_resource,collection, authorized_user " +
            "where collection.id=collection_resource.collection_id and collection.id=authorized_user.resource_collection_id " +
            "and user_id=:submitterId and general_permission_int > :effectivePermissions " +
            "union select id from resource where updater_id=:submitterId or submitter_id=:submitterId)";
    public static final String QUERY_SQL_COUNT = "SELECT COUNT(*) FROM %1$s";
    public static final String QUERY_FIND_ALL_WITH_IDS = "FROM %s WHERE id in (:ids)";
    public static final String QUERY_SQL_COUNT_ACTIVE_RESOURCE = "SELECT COUNT(*) FROM %1$s where status='ACTIVE'";
    public static final String QUERY_SQL_RESOURCE_INCREMENT_USAGE = "update Resource r set r.accessCounter=accessCounter+1 where r.id=:resourceId";
    public static final String QUERY_SQL_RAW_RESOURCE_STAT_LOOKUP = "select (select count(*) from resource where resource_type = rt.resource_type and status = 'ACTIVE') as all,"
            + "(select count(distinct information_resource_id) from information_resource_file irf join resource rr on (rr.id = irf.information_resource_id) where rr.resource_type = rt.resource_type and rr.status = 'ACTIVE') as with_files,"
            + "(select count(distinct information_resource_id) from information_resource_file irf join resource rr on (rr.id = irf.information_resource_id) where rr.resource_type = rt.resource_type and rr.status = 'ACTIVE' and irf.confidential) as with_conf,"
            + "rt.* from (select distinct resource_type from resource) as rt";

    // generated HQL formats

    // e.g."from Resource r1 where exists (from Resource r2 inner join r2.cultureKeywords ck where r2.id = r1.id and ck.id in (:idlist))"
    public static final String QUERY_HQL_MANY_TO_MANY_REFERENCES =
            "from %1$s r1 where exists (from %1$s r2 inner join r2.%2$s ck where r2.id = r1.id and ck.id in (:idlist))";
    // e.g. "from Resource r1 where submitter_id in (:idlist)"
    public static final String QUERY_HQL_MANY_TO_ONE_REFERENCES =
            "from %1$s r1 where %2$s.id in (:idlist)";

    public static final String QUERY_HQL_COUNT_MANY_TO_MANY_REFERENCES =
            "select count(*) as referenceCount from %1$s r1 where exists (from %1$s r2 inner join r2.%2$s ck where r2.id = r1.id and ck.id in (:idlist))";
    public static final String QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES =
            "select count(*) as referenceCount from %1$s r1 where %2$s.id in (:idlist)";

    public static final String QUERY_HQL_COUNT_MANY_TO_MANY_REFERENCES_MAP =
            "select new map(ck.id as id, count(*) as referenceCount) from %1$s r2 inner join r2.%2$s ck where ck.id in (:idlist) group by ck.id";
    public static final String QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES_MAP =
            "select new map(%2$s.id as id, count(*) as referenceCount) from %1$s r1 where %2$s.id in (:idlist) group by %2$s.id";

    public static final String QUERY_HQL_UPDATE_MANY_TO_MANY_REFERENCES = ""; // TODO: //Not possible, I think.
    public static final String QUERY_HQL_UPDATE_MANY_TO_ONE_REFERENCES = ""; // TODO: use many_to_one_count in exists clause.

    public static final String HQL_EDITABLE_RESOURCE_SUFFIX = "FROM Resource as res  where " +
            " (TRUE=:allResourceTypes or res.resourceType in (:resourceTypes)) and (TRUE=:allStatuses or res.status in (:statuses) )  AND " +
               "(res.submitter.id=:userId or res in (" +
               " select elements(rescol.resources) from ResourceCollection rescol join rescol.authorizedUsers  as authUser " +
               " where (TRUE=:admin or authUser.user.id=:userId and authUser.effectiveGeneralPermission > :effectivePermission))) ";

}
