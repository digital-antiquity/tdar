@javax.persistence.NamedQueries({
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_READ_ONLY_FULLUSER_PROJECTS,
                query = "select distinct new Project(project.id, project.title) from Project project inner join project.fullUsers as f " +
                        "where (f.person.id=:personId or project.submitter.id=:personId) and (project.status='ACTIVE' or project.status='DRAFT') order by project.title"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_READ_ONLY_EDITABLE_PROJECTS,
                query = "select new Project(project.id,project.title) from Project project where project.submitter.id=:submitterId OR project in " +
                        "(select project from Project project inner join project.fullUsers as fullUser " +
                        "where fullUser.person.id=:submitterId) " +
                        "and project.status='ACTIVE' order by project.title"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_PROJECTS,
                query = "select new Project(project.id,project.title) from Project project where " +
                        " project.status='ACTIVE' order by project.title"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_READ_ONLY_SUBMITTER_RESOURCES,
                query = "select new Resource(resource.id,resource.title,resource.resourceType) from Resource resource where (submitter.id=:submitter or confidential=false) "
                        +
                        "and status like :status and resourceType=:resourceType"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_EMPTY_PROJECTS,
                //FIXME: figure out how to this with an EXISTS statement, which is faster (in theory)
                //              query = "select new Project(project.id,project.title) from Project project where (submitter.id=:submitter) and status='ACTIVE' "
                //            	+ "and not exists (select * from InformationResource resource where resource.status='ACTIVE' and resource.project.id = project.id) "
                query = "select new Project(project.id,project.title) from Project project where (submitter.id=:submitter) and id not in (select resource.project.id from InformationResource resource where resource.status='ACTIVE' and resource.project.id is not null) "
                        + "and (status='ACTIVE' or status='DRAFT') "
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_USER_GET_ALL_RESOURCES_COUNT,
                query = "select count(resource) from Resource resource left join resource.fullUsers as fullUser left join resource.readUsers as readUser where ("
                        + " fullUser.person.id=:userId or readUser.person.id=:userId or resource.submitter.id=:userId or resource.updatedBy.id=:userId) and resource.status!='DELETED'"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_FULLUSER_RESOURCES,
                query = "select resource from Resource resource inner join resource.fullUsers as f " +
                        "where f.person.id=:personId and resource.status='ACTIVE' order by resource.title"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_FULLUSER_DATASET,
                query = "select dataset from Dataset dataset inner join dataset.fullUsers as f " +
                        "where f.person.id=:personId and dataset.status='ACTIVE' order by dataset.title"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_READUSER_PROJECTS,
                query = "select project from Project project inner join project.readUsers as r " +
                        "where r.person.id=:personId and project.status='ACTIVE' order by project.title"
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
                name = TdarNamedQueries.QUERY_CATEGORYVARIABLE_SUBCATEGORIES,
                query = "from CategoryVariable " +
                        "where encodedParentIds=:parentId " +
                        "or encodedParentIds like :firstParent " +
                        "or encodedParentIds like :middleParent " +
                        "or encodedParentIds like :lastParent " +
                        "order by name"
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
                name = TdarNamedQueries.QUERY_PROJECT_EDITABLE,
                query = "select project from Project project where project.submitter.id=:submitterId OR project in " +
                        "(select project from Project project inner join project.fullUsers as fullUser " +
                        "where fullUser.person.id=:personId) " +
                        "and project.status='ACTIVE' order by project.title"
        ),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_PROJECT_VIEWABLE,
                query = "select distinct project from Project project, ReadUser readUser, FullUser fullUser "
                        +
                        "where project.submitter.id=:submitterId OR (project.id=fullUser.resource.id and fullUser.person.id=:submitterId) OR (project.id=readUser.resource.id and readUser.person.id=:submitterId) "
                        +
                        "order by project.title"
        ),
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
                name = TdarNamedQueries.QUERY_MANAGED_ISO_COUNTRIES,
                // NOTE: hibernate is not smart enough to handle the "group by kwd" it needs to be told to include ALL of the keyword attributes that
                // it's going to request in the setter.
                query = "select distinct kwd, count(r.id) from Resource r join "
                        +
                        "r.managedGeographicKeywords as kwd where kwd.level='ISO_COUNTRY' and r.status='ACTIVE'  group by kwd.id , kwd.definition , kwd.label , kwd.level"),
        @javax.persistence.NamedQuery(
                name = TdarNamedQueries.QUERY_ACTIVE_RESOURCE_TYPE_COUNT,
                query = "select count(res.id) as count , res.resourceType as resourceType from Resource as res where res.status='ACTIVE' group by res.resourceType "
                ) })
package org.tdar.core.dao;

