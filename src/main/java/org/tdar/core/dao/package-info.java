@javax.persistence.NamedQueries({
        //    select distinct true from collection_resource, collection, authorized_user where user_id =2 and general_permissions='MODIFY_RECORD' and authorized_user.resource_collection_id=collection.id and collection_resource.collection_id=collection.id
        //    and (resource_id in(657) or resource_id in (2454));
        @javax.persistence.NamedQuery(
            name = TdarNamedQueries.QUERY_IS_ALLOWED_TO,
            query = "SELECT distinct 1 from " +
                    " Resource res inner join res.resourceCollections as rescol inner join rescol.authorizedUsers " +
                    " as authUser where authUser.user.id=:userId and authUser.effectiveGeneralPermission > :effectivePermission and " +
                    " res.id in (:resourceIds)"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_IS_ALLOWED_TO_NEW, // NOTE: THIS MAY REQUIRE ADDITIONAL WORK
                query = "SELECT distinct 1 from AuthorizedUser authUser inner join authUser.resourceCollection as rescol where authUser.user.id=:userId and authUser.effectiveGeneralPermission > :effectivePermission and " +
                        " rescol.id in (:resourceCollectionIds)"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO, // NOTE: THIS MAY REQUIRE ADDITIONAL WORK INNER JOIN WILL PRECLUDE OwnerId w/no authorized users
                query = "SELECT distinct resCol from ResourceCollection resCol inner join resCol.authorizedUsers as authUser where authUser.user.id=:userId and" +
                        " resCol.type='SHARED'"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_IS_ALLOWED_TO_MANAGE,
                query = "SELECT distinct 1 from " +
                        " ResourceCollection as rescol inner join rescol.authorizedUsers " +
                        " as authUser where authUser.user.id=:userId and authUser.effectiveGeneralPermission > :effectivePermission and " +
                        " rescol.id in (:resourceCollectionIds)"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_EDITABLE_RESOURCES,
                query = "SELECT distinct new Resource(res.id, res.title, res.resourceType) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_USER_GET_ALL_RESOURCES_COUNT,
                query = "SELECT count(distinct res.id) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_PROJECTS,
                query = "select new Project(project.id,project.title) from Project project where " +
                        " project.status='ACTIVE' order by project.title"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_RESOURCES_SUBMITTER,
                query = "select new Resource(resource.id,resource.title,resource.resourceType) from Resource resource where (submitter.id=:submitter) "
                        +
                        "and status like :status and resourceType=:resourceType"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_RESOURCES,
                query = "select new Resource(resource.id,resource.title,resource.resourceType) from Resource resource where status like :status and resourceType=:resourceType"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_EMPTY_PROJECTS,
                query = "select new Project(project.id, project.title) from Project project where (submitter.id=:submitter) and project.status in ('ACTIVE', 'DRAFT') "
                        +
                        " and not exists(select 1 from InformationResource ir where ir.status='ACTIVE' and ir.project.id = project.id)"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_CONTRIBUTORREQUEST_PENDING,
                query = "from ContributorRequest where approver is null and approved='f' order by timestamp desc"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_BOOKMARKEDRESOURCE_IS_ALREADY_BOOKMARKED,
                query = "from BookmarkedResource where resource.id=:resourceId and person.id=:personId"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_BOOKMARKEDRESOURCE_REMOVE_BOOKMARK,
                query = "delete BookmarkedResource where resource.id=:resourceId and person.id=:personId"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_BOOKMARKEDRESOURCE_FIND_RESOURCE_BY_PERSON,
                query = "select br.resource from BookmarkedResource br where br.person.id = :personId and br.resource.status='ACTIVE' "
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_DATASET_CAN_LINK_TO_ONTOLOGY,
                query = "select dtc.defaultOntology from DataTable dt inner join dt.dataTableColumns as dtc " +
                        "where dt.dataset.id=:datasetId"
        ),
        @javax.persistence.NamedQuery(
                name = "dataTable.idlist",
                query = "from DataTable where id in (:dataTableIds)"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_DATATABLE_RELATED_ID,
                query = "SELECT DISTINCT dt FROM DataTable dt join dt.dataTableColumns as dtc WHERE (dtc.defaultOntology=:relatedId  or dtc.defaultCodingSheet=:relatedId) and dt.dataset.status!='DELETED'"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_DATATABLECOLUMN_WITH_DEFAULT_ONTOLOGY,
                query = "FROM DataTableColumn dtc WHERE dtc.dataTable.dataset.id=:datasetId AND dtc.defaultOntology IS NOT NULL ORDER BY dtc.id"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_INFORMATIONRESOURCE_FIND_BY_FILENAME,
                query = "SELECT file from InformationResourceFile as file, InformationResourceFileVersion as version where file.informationResource = :resource and "
                        +
                        "file=version.informationResourceFile and file.latestVersion=version.version and version.filename = :filename"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_ONTOLOGYNODE_ALL_CHILDREN_WITH_WILDCARD,
                query = "from OntologyNode o " +
                        "where o.ontology.id=:ontologyId and index like :indexWildcardString"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_ONTOLOGYNODE_ALL_CHILDREN,
                query = "from OntologyNode o " +
                        "where o.ontology.id=:ontologyId and o.intervalStart > :intervalStart and o.intervalEnd < :intervalEnd"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_RECENT_EDITS,
                query = "select new Resource(r.id, r.title,r.resourceType) from InformationResource r where (updater_id = :personId or submitter_id = :personId) and r.status!='DELETED' ORDER by date_updated desc"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCE_RESOURCETYPE,
                query = "select resourceType from Resource where id=:id"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCE_MODIFIED_SINCE,
                query = "select count(log) from ResourceRevisionLog log where log.resource = :id and log.timestamp > :date"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_PROJECT_COUNT_INTEGRATABLE_DATASETS,
                query = "select count(distinct ds.id) from Dataset as ds join ds.dataTables as dt " +
                        "join dt.dataTableColumns as dtc where dtc.defaultOntology <> null and ds.project.id = :projectId"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_PROJECTS_COUNT_INTEGRATABLE_DATASETS,
                query = "select count(distinct ds.id) from Dataset as ds join ds.dataTables as dt " +
                        "join dt.dataTableColumns as dtc where dtc.defaultOntology <> null and ds.project.id in (:projectIdList)"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_IS_ONTOLOGY_MAPPED,
                query = "select count(dtv.id) as mapped_data_value_count from DataValueOntologyNodeMapping as dtv where dtv.ontologyNode.ontology.id=:ontologyId"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_IS_CODING_SHEET_MAPPED,
                query = "select count(dtc.id) as mapped_data_value_count from DataTableColumn as dtc where dtc.defaultCodingSheet.id=:codingId"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_IS_ONTOLOGY_MAPPED_TO_COLUMN,
                query = "select count(dtv.id) as mapped_data_value_count from DataValueOntologyNodeMapping as dtv where dtv.ontologyNode.ontology.id=:ontologyId and dtv.dataTableColumn.id=:dataTableColumnId"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS,
                query = "select count(r.id) as resource_type_count from Resource as r where r.resourceType=:resourceType and r.status=:status"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_MANAGED_ISO_COUNTRIES,
                // NOTE: hibernate is not smart enough to handle the "group by kwd" it needs to be told to include ALL of the keyword attributes that
                // it's going to request in the setter.
                query = "select distinct kwd, count(r.id) from Resource r join "
                        +
                        "r.managedGeographicKeywords as kwd where kwd.level='ISO_COUNTRY' and r.status='ACTIVE'  group by kwd.id , kwd.definition , kwd.label , kwd.level"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_ACTIVE_RESOURCE_TYPE_COUNT,
                query = "select count(res.id) as count , res.resourceType as resourceType from Resource as res where res.status='ACTIVE' group by res.resourceType "
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_COLLECTION_BY_PARENT,
                query = "from ResourceCollection as col where (col.parent.id=:parent or (col.parent.id is NULL AND :parent is NULL)) and col.type in (:collectionTypes) and (visible=:visible or :visible is NULL)"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_COLLECTION_BY_AUTH_OWNER,
                query = "select distinct col from ResourceCollection as col left join col.authorizedUsers as authorizedUser where col.type in (:collectionTypes) and (col.owner.id=:authOwnerId or (authorizedUser.id=:authOwnerId and authorizedUser.effectiveGeneralPermission >  :equivPerm)) order by col.name"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_COLLECTION_PUBLIC_WITH_HIDDEN_PARENT,
                query = "select distinct col from ResourceCollection as col left join col.parent as parent where parent.type='SHARED' and parent.visible=false and col.visible=true and col.type='SHARED'"
        ),
        @javax.persistence.NamedQuery(
            name = TdarNamedQueries.QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS_BY_USER,
            query = "select res.resourceType, res.status, count(res.id) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX + " group by res.resourceType, res.status"
    )}
)
package org.tdar.core.dao;

