package org.tdar.core.dao;

public interface TdarNamedQueries {
    // public static final String QUERY_DOCUMENTAUTHOR_PERSON_DOCUMENTS = "documentAuthor.person.documents";
    public static final String QUERY_IS_ONTOLOGY_MAPPED = "ontology.isMapped";
    public static final String QUERY_IS_ONTOLOGY_MAPPED_TO_COLUMN = "ontology.isMappedToColumn";
    public static final String QUERY_IS_CODING_SHEET_MAPPED = "codingSheet.isMapped";
    public static final String QUERY_READ_ONLY_FULLUSER_PROJECTS = "fullUser.projects";
    public static final String QUERY_FULLUSER_RESOURCES = "fullUser.resources";
    public static final String QUERY_FULLUSER_DATASET = "fullUser.datasets";
    public static final String QUERY_READUSER_PROJECTS = "readUser.projects";
    public static final String QUERY_SPARSE_RECENT_EDITS = "resources.recent";
    public static final String QUERY_CONTRIBUTORREQUEST_PENDING = "contributorRequest.pending";
    public static final String QUERY_BOOKMARKEDRESOURCE_IS_ALREADY_BOOKMARKED = "bookmarkedResource.isAlreadyBookmarked";
    public static final String QUERY_BOOKMARKEDRESOURCE_REMOVE_BOOKMARK = "bookmarkedResource.removeBookmark";
    public static final String QUERY_BOOKMARKEDRESOURCE_FIND_RESOURCE_BY_PERSON = "bookmarkedResource.findResourcesByPerson";
    public static final String QUERY_CATEGORYVARIABLE_SUBCATEGORIES = "categoryVariable.subcategories";
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
    public static final String QUERY_READ_ONLY_SUBMITTER_RESOURCES = "submitter.resources";
    public static final String QUERY_PROJECT_COUNT_INTEGRATABLE_DATASETS = "project.countIntegratableDatasets";
    public static final String QUERY_PROJECTS_COUNT_INTEGRATABLE_DATASETS = "projects.countIntegratableDatasets";
    public static final String QUERY_READ_ONLY_EDITABLE_PROJECTS = "sparse.editableProject";
    public static final String QUERY_MANAGED_ISO_COUNTRIES = "resource.iso_code";
    public static final String QUERY_ACTIVE_RESOURCE_TYPE_COUNT = "resource.typeCounts";
    public static final String QUERY_USER_GET_ALL_RESOURCES_COUNT = "resource.active.count";
    public static final String QUERY_SPARSE_EMPTY_PROJECTS = "projects.empty";
    public static final String QUERY_RESOURCE_EDITABLE = "resource.editable";
    public static final String QUERY_SPARSE_PROJECTS = "project.all.sparse";
    // raw SQL queries
    public static final String QUERY_SQL_COUNT = "SELECT COUNT(*) FROM %1$s";
    public static final String QUERY_SQL_COUNT_ACTIVE_RESOURCE = "SELECT COUNT(*) FROM %1$s where status='ACTIVE'";
    public static final String QUERY_SQL_RESOURCE_INCREMENT_USAGE = "update Resource r set r.accessCounter=accessCounter+1 where r.id=:resourceId";
}
