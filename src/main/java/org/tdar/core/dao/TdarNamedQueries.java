package org.tdar.core.dao;

public interface TdarNamedQueries {
    /**
     * constants to map between the Annotation Keys for HQL queries and the queries in the DAOs
     */
    static final String QUERY_DELETE_INFORMATION_RESOURCE_FILE_DERIVATIVES = "informationResourceFileVersion.deleteDerivatives";
    static final String QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_ONTOLOGY = "ontology.isMapped";
    static final String QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_COLUMN = "ontology.isMappedToColumn";
    static final String QUERY_READ_ONLY_FULLUSER_PROJECTS = "fullUser.projects";
    static final String QUERY_FULLUSER_RESOURCES = "fullUser.resources";
    static final String QUERY_FULLUSER_DATASET = "fullUser.datasets";
    static final String QUERY_READUSER_PROJECTS = "readUser.projects";
    static final String QUERY_SPARSE_RECENT_EDITS = "resources.recent";
    static final String QUERY_CONTRIBUTORREQUEST_PENDING = "contributorRequest.pending";
    static final String QUERY_BOOKMARKEDRESOURCE_IS_ALREADY_BOOKMARKED = "bookmarkedResource.isAlreadyBookmarked";
    static final String QUERY_BOOKMARKEDRESOURCE_REMOVE_BOOKMARK = "bookmarkedResource.removeBookmark";
    static final String QUERY_BOOKMARKEDRESOURCE_FIND_RESOURCE_BY_PERSON = "bookmarkedResource.findResourcesByPerson";
    static final String QUERY_DATASET_CAN_LINK_TO_ONTOLOGY = "dataset.canLinkToOntology";
    static final String QUERY_DATATABLE_RELATED_ID = "dataTable.relatedId";
    static final String QUERY_DATATABLECOLUMN_WITH_DEFAULT_ONTOLOGY = "dataTableColumn.withDefaultOntology";
    static final String QUERY_INFORMATIONRESOURCE_FIND_BY_FILENAME = "informationResource.findByFileName";
    static final String QUERY_ONTOLOGYNODE_ALL_CHILDREN_WITH_WILDCARD = "ontologyNode.allChildrenWithIndexWildcard";
    static final String QUERY_ONTOLOGYNODE_PARENT = "ontologyNode.parent";
    static final String QUERY_ONTOLOGYNODE_ALL_CHILDREN = "ontologyNode.allChildren";
    static final String QUERY_PROJECT_CODINGSHEETS = "project.codingSheets";
    static final String QUERY_PROJECT_DATASETS = "project.datasets";
    static final String QUERY_PROJECT_DOCUMENTS = "project.documents";
    static final String QUERY_PROJECT_INDEPENDENTRESOURCES_PROJECTS = "project.independentResourcesProject";
    static final String QUERY_PROJECT_EDITABLE = "project.editable";
    static final String QUERY_PROJECT_VIEWABLE = "project.viewable";
    static final String QUERY_FILE_STATUS = "file.with.statuses";
    static final String QUERY_LOGIN_STATS = "admin.userLogin";
    static final String QUERY_PROJECT_ALL_OTHER = "project.all.other";
    static final String QUERY_RESOURCE_RESOURCETYPE = "resource.resourceType";
    static final String QUERY_RESOURCE_MODIFIED_SINCE = "resource.modifiedSince";
    static final String QUERY_SPARSE_RESOURCES = "all.resources";
    static final String QUERY_SPARSE_RESOURCES_SUBMITTER = "submitter.resources.sparse";
    static final String QUERY_RESOURCES_SUBMITTER = "submitter.resources";
    static final String QUERY_PROJECT_COUNT_INTEGRATABLE_DATASETS = "project.countIntegratableDatasets";
    static final String QUERY_PROJECTS_COUNT_INTEGRATABLE_DATASETS = "projects.countIntegratableDatasets";
    static final String QUERY_READ_ONLY_EDITABLE_PROJECTS = "sparse.editableProject";
    static final String QUERY_MANAGED_ISO_COUNTRIES = "resource.iso_code";
    static final String QUERY_ACTIVE_RESOURCE_TYPE_COUNT = "resource.typeCounts";
    static final String QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS = "resource.countByTypeAndStatus";
    static final String QUERY_USER_GET_ALL_RESOURCES_COUNT = "resource.active.count";
    static final String QUERY_SPARSE_EMPTY_PROJECTS = "projects.empty";
    static final String QUERY_RESOURCE_EDITABLE = "resource.editable";
    static final String QUERY_SPARSE_PROJECTS = "project.all.sparse";
    static final String QUERY_IS_ALLOWED_TO = "resource.isEditable";
    static final String QUERY_IS_ALLOWED_TO_NEW = "resource.isEditable2";
    static final String QUERY_IS_ALLOWED_TO_MANAGE = "resourcecollection.isEditable";
    static final String QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS_BY_USER = "dashboard.resourceByPerson";
    static final String QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO = "rescol.accessible";
    static final String QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME = "rescol.accessibleName";
    static final String QUERY_SPARSE_EDITABLE_RESOURCES = "resource.editable.sparse";
    static final String QUERY_EDITABLE_RESOURCES = "resource.editable";
    static final String QUERY_SPARSE_EDITABLE_SORTED_RESOURCES = "resource.editable.sorted.sparse" ;
    static final String QUERY_COLLECTION_BY_PARENT = "collection.parent";
    static final String QUERY_COLLECTIONS_PUBLIC_ACTIVE = "collection.activeId";
    static final String QUERY_COLLECTION_RESOURCES_WITH_STATUS = "collection.resourcesWithStatus";
    static final String QUERY_COLLECTION_BY_AUTH_OWNER = "collection.authOwnerId_name";
    static final String QUERY_COLLECTION_PUBLIC_WITH_HIDDEN_PARENT = "collection.hiddenParent";
    static final String QUERY_EXTERNAL_ID_SYNC = "resource.externalId";
    static final String QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_CONTROLLED = "adminStats.cultureKeywordControlled";
    static final String QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_UNCONTROLLED = "adminStats.cultureKeywordUnControlled";
    static final String QUERY_KEYWORD_COUNT_GEOGRAPHIC_KEYWORD = "adminStats.geographicKeyword";
    static final String QUERY_KEYWORD_COUNT_INVESTIGATION_TYPE = "adminStats.investigationType";
    static final String QUERY_KEYWORD_COUNT_MATERIAL_KEYWORD = "adminStats.materialKeyword";
    static final String QUERY_KEYWORD_COUNT_OTHER_KEYWORD = "adminStats.otherKeyword";
    static final String QUERY_KEYWORD_COUNT_SITE_NAME_KEYWORD = "adminStats.siteNameKeyword";
    static final String QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_CONTROLLED = "adminStats.siteTypeKeywordControlled";
    static final String QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_UNCONTROLLED = "adminStats.siteTypeKeywordUnControlled";
    static final String QUERY_KEYWORD_COUNT_TEMPORAL_KEYWORD = "adminStats.temporalKeyword";
    static final String QUERY_KEYWORD_COUNT_FILE_EXTENSION = "adminStats.fileExtensions";
    static final String QUERY_RECENT_USERS_ADDED = "adminStats.recentUsers";
    static final String QUERY_RECENT = "adminStats.recentFiles";
//    static final String QUERY_MATCHING_FILES = "datasetRelated.Files";
    static final String QUERY_USAGE_STATS = "adminStats.usage";
    static final String QUERY_FILE_STATS = "adminStats.fileDetails";
    static final String QUERY_MAPPED_CODING_RULES = "dataTableColumn.mappedCodingRules";
    static final String QUERY_RESOURCES_IN_PROJECT = "resources.inProject";
    static final String QUERY_RESOURCES_IN_PROJECT_WITH_STATUS = "project.informationResourcesWithStatus";
    static final String QUERY_DASHBOARD = "dashboard.sql";
    static final String QUERY_RESOURCES_BY_DECADE = "resources.byDecade";
    static final String QUERY_SPARSE_RESOURCE_LOOKUP = "resource.sparseLookup";
    static final String QUERY_SPARSE_COLLECTION_LOOKUP = "resourceCollection.sparseLookup";
    static final String SPACE_BY_PROJECT = "admin.size.project";
    static final String SPACE_BY_RESOURCE = "admin.size.resource";
    static final String SPACE_BY_COLLECTION = "admin.size.collection";
    static final String SPACE_BY_SUBMITTER = "admin.size.submitter";
    static final String ACCESS_BY = "admin.access";
    static final String DOWNLOAD_BY = "admin.download";
    static final String LOGS_FOR_RESOURCE = "admin.logsforResource";
    static final String FIND_ACTIVE_COUPON = "coupon.active";
    static final String RESOURCE_ACCESS_HISTORY = "admin.accessHistory";
    static final String FILE_DOWNLOAD_HISTORY = "admin.fileFileHistory";
    static final String RESOURCES_WITH_NULL_ACCOUNT_ID = "account.resourceNull";
    static final String ACCOUNT_QUOTA_INIT = "account.quota.init";
    static final String RESOURCES_WITH_NON_MATCHING_ACCOUNT_ID = "account.resourceDifferent";
    static final String ACCOUNT_GROUP_FOR_ACCOUNT = "account.group";
    static final String ACCOUNTS_FOR_PERSON = "accounts.forPerson";
    static final String ACCOUNT_GROUPS_FOR_PERSON = "accountgroups.forPerson";
    static final String QUERY_INFORMATIONRESOURCES_WITH_FILES = "informationResources.withFiles";
    static final String QUERY_SPARSE_ACTIVE_RESOURCES = "resources.active";
    static final String INVOICES_FOR_PERSON = "invoices.forPerson";
    static final String UNASSIGNED_INVOICES_FOR_PERSON = "invoices.unassignedForPerson";
    static final String FIND_INVOICE_FOR_COUPON = "invoices.coupons";
    static final String QUERY_SPARSE_CODING_SHEETS_USING_ONTOLOGY = "sparseCodingSheets.ontology";
    static final String QUERY_FILE_SIZE_TOTAL ="file.total_size";
    static final String QUERY_RELATED_RESOURCES = "resource.related";
    static final String QUERY_PROXY_RESOURCE_FULL = "resourceProxy.full";
    static final String QUERY_PROXY_RESOURCE_SHORT = "resourceProxy.short";
    static final String QUERY_RESOURCE_FIND_OLD_LIST = "resource.old";
    static final String FIND_ACCOUNT_FOR_INVOICE = "account.forInvoice";

    static final String COLLECTION_LIST_WITH_AUTHUSER = "collection.idlest.with.authuser";
    static final String QUERY_SPARSE_EDITABLE_SORTED_RESOURCES_INHERITED = "query.sparse.editable.sorted.resources.inherited" ;
    static final String QUERY_SPARSE_EDITABLE_SORTED_RESOURCES_INHERITED_SORTED = "query.sparse.editable.sorted.resources.inherited.sorted" ;
    // raw SQL/HQL queries

    /**
     * Static HQL and SQL queries that cannot be represented as annotations because they are either pure SQL or use String replacement.
     */
    static final String QUERY_SQL_DASHBOARD =
            "select id, status, resource_type from resource " +
                    "where id in " +
                    "(select resource_id from collection_resource,collection, authorized_user " +
                    "where collection.id=collection_resource.collection_id and collection.id=authorized_user.resource_collection_id " +
                    "and user_id=:submitterId and general_permission_int > :effectivePermissions " +
                    "union select id from resource where updater_id=:submitterId or submitter_id=:submitterId)";
    static final String QUERY_SQL_COUNT = "SELECT COUNT(*) FROM %1$s";
    static final String QUERY_FIND_ALL_WITH_IDS = "FROM %s WHERE id in (:ids)";
    static final String QUERY_FIND_ALL_WITH_STATUS = "FROM %s WHERE status in (:statuses)";
    static final String QUERY_SQL_COUNT_ACTIVE_RESOURCE = "SELECT COUNT(*) FROM %1$s where status='ACTIVE' and resourceType='%2$s' ";
    static final String QUERY_SQL_COUNT_ACTIVE_RESOURCE_WITH_FILES = "select count(distinct  resource.id) from  resource, information_resource_file where  resource.status='ACTIVE' and resource.resource_type='%1$s' and resource.id=information_resource_id";
    static final String QUERY_SQL_RESOURCE_INCREMENT_USAGE = "update Resource r set r.accessCounter=accessCounter+1 where r.id=:resourceId";
    static final String QUERY_SQL_RAW_RESOURCE_STAT_LOOKUP = "select (select count(*) from resource where resource_type = rt.resource_type and status = 'ACTIVE') as all,"
            + "(select count(distinct information_resource_id) from information_resource_file irf join resource rr on (rr.id = irf.information_resource_id) where rr.resource_type = rt.resource_type and rr.status = 'ACTIVE') as with_files,"
            + "(select count(distinct information_resource_id) from information_resource_file irf join resource rr on (rr.id = irf.information_resource_id) where rr.resource_type = rt.resource_type and rr.status = 'ACTIVE' and irf.restriction = 'CONFIDENTIAL') as with_conf,"
            + "rt.* from (select distinct resource_type from resource) as rt";
    // generated HQL formats
    static final String QUERY_CREATOR_MERGE_ID = "select merge_creator_id from %1$s where id=%2$s";

    static final String QUERY_KEYWORD_MERGE_ID = "select merge_keyword_id from %1$s where id=%2$s";

    // e.g."from Resource r1 where exists (from Resource r2 inner join r2.cultureKeywords ck where r2.id = r1.id and ck.id in (:idlist))"
    static final String QUERY_HQL_MANY_TO_MANY_REFERENCES =
            "from %1$s r1 where exists (from %1$s r2 inner join r2.%2$s ck where r2.id = r1.id and ck.id in (:idlist))";
    // e.g. "from Resource r1 where submitter_id in (:idlist)"
    static final String QUERY_HQL_MANY_TO_ONE_REFERENCES =
            "from %1$s r1 where %2$s.id in (:idlist)";

    static final String QUERY_HQL_COUNT_MANY_TO_MANY_REFERENCES =
            "select count(*) as referenceCount from %1$s r1 where exists (from %1$s r2 inner join r2.%2$s ck where r2.id = r1.id and ck.id in (:idlist))";
    static final String QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES =
            "select count(*) as referenceCount from %1$s r1 where %2$s.id in (:idlist)";

    static final String QUERY_HQL_COUNT_MANY_TO_MANY_REFERENCES_MAP =
            "select new map(ck.id as id, count(*) as referenceCount) from %1$s r2 inner join r2.%2$s ck where ck.id in (:idlist) group by ck.id";
    static final String QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES_MAP =
            "select new map(%2$s.id as id, count(*) as referenceCount) from %1$s r1 where %2$s.id in (:idlist) group by %2$s.id";

    static final String QUERY_HQL_UPDATE_MANY_TO_MANY_REFERENCES = ""; // TODO: //Not possible, I think.
    static final String QUERY_HQL_UPDATE_MANY_TO_ONE_REFERENCES = ""; // TODO: use many_to_one_count in exists clause.

    static final String HQL_EDITABLE_RESOURCE_SUFFIX = "FROM Resource as res  where " +
            " (TRUE=:allResourceTypes or res.resourceType in (:resourceTypes)) and (TRUE=:allStatuses or res.status in (:statuses) )  AND " +
            "(res.submitter.id=:userId or exists (" +
            " from ResourceCollection rescol join rescol.authorizedUsers  as authUser " +
            " join rescol.resources as colres " +
            " where colres.id = res.id and " +
            "(TRUE=:admin or authUser.user.id=:userId and authUser.effectiveGeneralPermission > :effectivePermission))) ";
    static final String HQL_EDITABLE_RESOURCE_SORTED_SUFFIX = HQL_EDITABLE_RESOURCE_SUFFIX + " order by res.title, res.id";
    static final String QUERY_CLEAR_REFERENCED_ONTOLOGYNODE_RULES = "update.clearOntologyNodeReferences";
    static final String UPDATE_DATATABLECOLUMN_ONTOLOGIES = "update.dataTableColumnOntologies";
    static final String QUERY_ACCOUNTS_FOR_RESOURCES = "select id, account_id from resource res where res.id in (%s) ";
    static final String QUERY_SQL_RESOURCES_BY_YEAR = "select date_part('year', date_registered), count(id) from resource where status='ACTIVE' and date_registered is not null group by date_part('year', date_registered)  order by date_part('year', date_registered)  asc";
    static final String DISTINCT_SUBMITTERS = "SELECT DISTINCT submitter_id from resource";

    static final String UPDATE_KEYWORD_OCCURRENCE_CLEAR_COUNT = "update %1$s set occurrence=0";
    static final String UPDATE_KEYWORD_OCCURRENCE_COUNT_INHERITANCE = "update %1$s set occurrence = occurrence + coalesce((select count(resource_id) from resource_%1$s where %1$s_id =%1$s.id and resource_id in (select project_id from information_resource where %2$s is true) group by %1$s_id),0)";
    static final String UPDATE_KEYWORD_OCCURRENCE_COUNT = "update %1$s set occurrence =  occurrence + coalesce((select count(resource_id) from resource_%1$s where %1$s_id =%1$s.id group by %1$s_id),0)";
    static final String UPDATE_CREATOR_OCCURRENCE_CLEAR_COUNT = "update creator set occurrence=0";
    static final String UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_PUBLISHER = "update creator set occurrence=occurrence + coalesce((select count(information_resource.id) from information_resource where publisher_id=creator.id group by publisher_id),0)";
    static final String UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_PROVIDER = "update creator set occurrence=occurrence + coalesce((select count(information_resource.id) from information_resource where provider_institution_id=creator.id group by provider_institution_id),0)";
    static final String UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_COPYRIGHT = "update creator set occurrence=occurrence + coalesce((select count(information_resource.id) from information_resource where copyright_holder_id=creator.id group by copyright_holder_id),0) ";
    static final String UPDATE_CREATOR_OCCURRENCE_RESOURCE_SUBMITTER = "update creator set occurrence=occurrence + coalesce((select count(resource.id) from resource where submitter_id=creator.id group by submitter_id),0)";
    static final String UPDATE_CREATOR_OCCURRENCE_RESOURCE = "update creator set occurrence = occurrence+ coalesce((select count(resource_id) from resource_creator where creator_id=creator.id group by creator_id),0) ";
    static final String DATASETS_USING_NODES = "select id from resource where id in (select dataset_id from data_table where data_table.id in (select data_table_id from data_table_column, coding_rule, coding_sheet where data_table_column.default_coding_sheet_id=coding_sheet_id and coding_rule.coding_sheet_id=coding_sheet.id and  ontology_node_id=%s)) and status = 'ACTIVE'";
    
}