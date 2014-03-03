/* FIXME: still unsupported
@org.hibernate.annotations.NamedNativeQueries({
	@org.hibernate.annotations.NamedNativeQuery(
			name = TdarNamedQueries.QUERY_DASHBOARD,
			query = TdarNamedQueries.QUERY_SQL_DASHBOARD
			),
})
 */
@org.hibernate.annotations.NamedQueries({
        //    select distinct true from collection_resource, collection, authorized_user where user_id =2 and general_permissions='MODIFY_RECORD' and authorized_user.resource_collection_id=collection.id and collection_resource.collection_id=collection.id
        //    and (resource_id in(657) or resource_id in (2454));
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_IS_ALLOWED_TO,
            query = "SELECT distinct 1 from " +
                    " Resource res inner join res.resourceCollections as rescol inner join rescol.authorizedUsers " +
                    " as authUser where authUser.user.id=:userId and authUser.effectiveGeneralPermission > :effectivePermission and " +
                    " res.id in (:resourceIds)"),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_COLLECTION_RESOURCES_WITH_STATUS,
            query = "SELECT res from Resource res inner join res.resourceCollections as rescol " +
                    "  where res.status in (:statuses) and rescol.id in (:ids)"),
                        // NOTE QUERY below was modified, will need to confirm performance impact
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_IS_ALLOWED_TO_NEW, // NOTE: THIS MAY REQUIRE ADDITIONAL WORK
            query = "SELECT distinct 1 from ResourceCollection as rescol inner join rescol.authorizedUsers as authuser where authuser.user.id=:userId and authuser.effectiveGeneralPermission > :effectivePermission and "
                    + " rescol.id in (:resourceCollectionIds)"),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME,
            query = "SELECT distinct resCol from ResourceCollection resCol left join resCol.authorizedUsers as authUser where (authUser.user.id=:userId and authUser.effectiveGeneralPermission > :effectivePermission or resCol.owner=:userId) and"
                    + " resCol.type!='INTERNAL' and resCol.name like :name "),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO, // NOTE: THIS MAY REQUIRE ADDITIONAL WORK INNER JOIN WILL PRECLUDE OwnerId w/no authorized users
            query = "SELECT distinct resCol from ResourceCollection resCol left join resCol.authorizedUsers as authUser where (authUser.user.id=:userId or resCol.owner=:userId) and"
                    + " resCol.type!='INTERNAL'"),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_IS_ALLOWED_TO_MANAGE,
            query = "SELECT distinct 1 from " +
                    " ResourceCollection as rescol inner join rescol.authorizedUsers " +
                    " as authUser where authUser.user.id=:userId and authUser.effectiveGeneralPermission > :effectivePermission and " +
                    " rescol.id in (:resourceCollectionIds) and rescol.type!='PUBLIC'"),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_SPARSE_RESOURCE_LOOKUP,
            query = "SELECT new Resource(res.id, res.title, res.resourceType, res.description, res.status) FROM Resource as res where res.id in (:ids) "),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_SPARSE_CODING_SHEETS_USING_ONTOLOGY,
            query = "SELECT new Resource(res.id, res.title, res.resourceType, res.description, res.status) FROM CodingSheet as res where res.defaultOntology.id=:ontologyId and status in (:statuses) "),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_SPARSE_COLLECTION_LOOKUP,
            query = "SELECT new ResourceCollection(col.id, col.name, col.description, col.sortBy, col.type, col.visible) FROM ResourceCollection as col where col.id in (:ids) "),
        @org.hibernate.annotations.NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_EDITABLE_RESOURCES,
                query = "SELECT new Resource(res.id, res.title, res.resourceType) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX),
        @org.hibernate.annotations.NamedQuery(
                name = TdarNamedQueries.QUERY_EDITABLE_RESOURCES,
                query = TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX),
        @org.hibernate.annotations.NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_EDITABLE_SORTED_RESOURCES,
                query = "SELECT distinct new Resource(res.id, res.title, res.resourceType) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SORTED_SUFFIX),

    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_SPARSE_ACTIVE_RESOURCES,
            query = "SELECT distinct new Resource(res.id, res.title, res.resourceType) from Resource as res where res.status in ('ACTIVE') "),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_USER_GET_ALL_RESOURCES_COUNT,
            query = "SELECT count(res.id) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_SPARSE_PROJECTS,
            query = "select new Project(project.id,project.title) from Project project where " +
                    " project.status='ACTIVE' order by project.title"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_SPARSE_RESOURCES_SUBMITTER,
            query = "select new Resource(resource.id,resource.title,resource.resourceType) from Resource resource where (submitter.id=:submitter) "
                    + "and status like :status and resourceType=:resourceType"),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_RESOURCES_SUBMITTER,
            query = "select id from Resource where submitter.id = :submitterId "),
        @org.hibernate.annotations.NamedQuery(
                name = TdarNamedQueries.QUERY_RELATED_RESOURCES,
                query = "from ResourceRelationship where (sourceResource.id = :resourceId and sourceResource.status in (:statuses)) or (targetResource.id = :resourceId and targetResource.status in (:statuses))"),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_SPARSE_RESOURCES,
            query = "select new Resource(resource.id,resource.title,resource.resourceType) from Resource resource where status like :status and resourceType=:resourceType"),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_SPARSE_EMPTY_PROJECTS,
            query = "select new Project(project.id, project.title) from Project project where (submitter.id=:submitter) and project.status in ('ACTIVE', 'DRAFT') "
                    + " and not exists(select 1 from InformationResource ir where ir.status in ('ACTIVE','DRAFT') and ir.project.id = project.id)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_BOOKMARKEDRESOURCE_IS_ALREADY_BOOKMARKED,
            query = "from BookmarkedResource where resource.id=:resourceId and person.id=:personId"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_BOOKMARKEDRESOURCE_REMOVE_BOOKMARK,
            query = "delete BookmarkedResource where resource.id=:resourceId and person.id=:personId"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_BOOKMARKEDRESOURCE_FIND_RESOURCE_BY_PERSON,
            query = "select br.resource from BookmarkedResource br where br.person.id = :personId and br.resource.status in (:statuses) order by br.resource.resourceType "
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_DATASET_CAN_LINK_TO_ONTOLOGY,
            query = "select dtc.defaultOntology from DataTable dt inner join dt.dataTableColumns as dtc " +
                    "where dt.dataset.id=:datasetId"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_MAPPED_CODING_RULES,
            query = "FROM CodingRule WHERE codingSheet.id = :codingSheetId AND term IN (:valuesToMatch)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_DATATABLE_RELATED_ID,
            query = "SELECT DISTINCT dt FROM DataTable dt join dt.dataTableColumns as dtc WHERE (dtc.defaultOntology=:relatedId  or dtc.defaultCodingSheet=:relatedId) and dt.dataset.status!='DELETED'"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_DATATABLECOLUMN_WITH_DEFAULT_ONTOLOGY,
            query = "FROM DataTableColumn dtc WHERE dtc.dataTable.dataset.id=:datasetId AND dtc.defaultOntology IS NOT NULL ORDER BY dtc.id"),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_INFORMATIONRESOURCE_FIND_BY_FILENAME,
            query = "SELECT file from InformationResourceFile as file, InformationResourceFileVersion as version where file.informationResource = :resource and "
                    + "file=version.informationResourceFile and file.latestVersion=version.version and version.filename = :filename"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_ONTOLOGYNODE_ALL_CHILDREN_WITH_WILDCARD,
            query = "from OntologyNode o where o.ontology.id=:ontologyId and index like :indexWildcardString"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_ONTOLOGYNODE_PARENT,
            query = "from OntologyNode o where o.ontology.id=:ontologyId and index like :index"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_ONTOLOGYNODE_ALL_CHILDREN,
            query = "from OntologyNode o " +
                    "where o.ontology.id=:ontologyId and o.intervalStart > :intervalStart and o.intervalEnd < :intervalEnd"),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_SPARSE_RECENT_EDITS,
            query = "select new Resource(r.id, r.title,r.resourceType) from Resource r where (updater_id = :personId or submitter_id = :personId) and r.status!='DELETED' ORDER by date_updated desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_RESOURCE_RESOURCETYPE,
            query = "select resourceType from Resource where id=:id"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_RESOURCE_MODIFIED_SINCE,
            query = "select count(log) from ResourceRevisionLog log where log.resource = :id and log.timestamp > :date"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_PROJECT_COUNT_INTEGRATABLE_DATASETS,
            query = "select count(distinct ds.id) from Dataset as ds join ds.dataTables as dt " +
                    "join dt.dataTableColumns as dtc where dtc.defaultOntology <> null and ds.project.id = :projectId"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_PROJECTS_COUNT_INTEGRATABLE_DATASETS,
            query = "select count(distinct ds.id) from Dataset as ds join ds.dataTables as dt " +
                    "join dt.dataTableColumns as dtc where dtc.defaultOntology <> null and ds.project.id in (:projectIdList)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_ONTOLOGY,
            query = "select count(id) as mapped_data_value_count from CodingRule where ontologyNode.ontology.id=:ontologyId"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_COLUMN,
            query = "select count(id) as mapped_data_value_count from CodingRule where ontologyNode.ontology=:ontology and codingSheet=:codingSheet"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS,
            query = "select count(r.id) as resource_type_count from Resource as r where r.resourceType=:resourceType and r.status=:status"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_MANAGED_ISO_COUNTRIES,
            // NOTE: hibernate is not smart enough to handle the "group by kwd" it needs to be told to include ALL of the keyword attributes that
            // it's going to request in the setter.
            query = "select kwd.label , kwd.level, count(r.id), kwd.id from Resource r join " +
                    "r.managedGeographicKeywords as kwd where kwd.level='ISO_COUNTRY' and r.status='ACTIVE'  group by kwd.label , kwd.level, kwd.id"),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_ACTIVE_RESOURCE_TYPE_COUNT,
            query = "select count(res.id) as count , res.resourceType as resourceType from Resource as res where res.status='ACTIVE' group by res.resourceType "
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_COLLECTION_BY_PARENT,
            query = "from ResourceCollection as col where (col.parent.id=:parent or (col.parent.id is NULL AND :parent is NULL)) and col.type in (:collectionTypes) and (visible=:visible or :visible is NULL)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_COLLECTIONS_PUBLIC_ACTIVE,
            query = "SELECT id from ResourceCollection as col where col.type not in ('INTERNAL') and col.visible is true "
    ),
    
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_COLLECTION_BY_AUTH_OWNER,
            query = "select distinct col from ResourceCollection as col left join col.authorizedUsers as authorizedUser where "
                    + "col.type in (:collectionTypes) and (col.owner.id=:authOwnerId or (authorizedUser.user.id=:authOwnerId and authorizedUser.effectiveGeneralPermission >  :equivPerm)) order by col.name"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_COLLECTION_PUBLIC_WITH_HIDDEN_PARENT,
            query = "select distinct col from ResourceCollection as col left join col.parent as parent where parent.type!='INTERNAL' and parent.visible=false and col.visible=true and col.type!='INTERNAL'"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS_BY_USER,
            query = "select res.resourceType, res.status, count(res.id) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX
                    + " group by res.resourceType, res.status"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_EXTERNAL_ID_SYNC,
            query = "select distinct res.id from InformationResource res inner join res.informationResourceFiles where res.dateUpdated > :updatedDate or res.externalId is null"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_RECENT,
            query = "from Resource res where (res.dateUpdated > :updatedDate or res.dateCreated > :updatedDate )"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_CONTROLLED,
            query = "select keyword, (select count(*) from Resource res inner join res.cultureKeywords rk where rk.id = keyword.id) as keywordCount from CultureKeyword keyword where keyword.approved = true order by keyword.index, keywordCount desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_UNCONTROLLED,
            query = "select keyword, (select count(*) from Resource res inner join res.cultureKeywords rk where rk.id = keyword.id) as keywordCount from CultureKeyword keyword where keyword.approved = false order by keyword.index, keywordCount desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_KEYWORD_COUNT_GEOGRAPHIC_KEYWORD,
            query = "select keyword, (select count(*) from Resource res inner join res.geographicKeywords rk where rk.id = keyword.id) as keywordCount from GeographicKeyword keyword order by keywordCount desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_KEYWORD_COUNT_INVESTIGATION_TYPE,
            query = "select keyword, (select count(*) from Resource res inner join res.investigationTypes rk where rk.id = keyword.id) as keywordCount from InvestigationType keyword order by keywordCount desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_KEYWORD_COUNT_MATERIAL_KEYWORD,
            query = "select keyword, (select count(*) from Resource res inner join res.materialKeywords rk where rk.id = keyword.id) as keywordCount from MaterialKeyword keyword order by keywordCount desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_KEYWORD_COUNT_OTHER_KEYWORD,
            query = "select keyword, (select count(*) from Resource res inner join res.otherKeywords rk where rk.id = keyword.id) as keywordCount from OtherKeyword keyword order by keywordCount desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_NAME_KEYWORD,
            query = "select keyword, (select count(*) from Resource res inner join res.siteNameKeywords rk where rk.id = keyword.id) as keywordCount from SiteNameKeyword keyword order by keywordCount desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_CONTROLLED,
            query = "select keyword, (select count(*) from Resource res inner join res.siteTypeKeywords rk where rk.id = keyword.id) as keywordCount from SiteTypeKeyword keyword where keyword.approved = true order by keyword.index, keywordCount desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_UNCONTROLLED,
            query = "select keyword, (select count(*) from Resource res inner join res.siteTypeKeywords rk where rk.id = keyword.id) as keywordCount from SiteTypeKeyword keyword where keyword.approved = false order by keyword.index, keywordCount desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_KEYWORD_COUNT_TEMPORAL_KEYWORD,
            query = "select keyword, (select count(*) from Resource res inner join res.temporalKeywords rk where rk.id = keyword.id) as keywordCount from TemporalKeyword keyword order by keywordCount desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_KEYWORD_COUNT_FILE_EXTENSION,
            query = "select extension, count(*) from InformationResourceFileVersion where fileVersionType in (:internalTypes)  group by extension "
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_RECENT_USERS_ADDED,
            query = "select p from Person p where registered=TRUE and status='ACTIVE' order by id desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_USAGE_STATS,
            query = "from AggregateStatistic where recordedDate between :fromDate and :toDate and statisticType in (:statTypes) order by recordedDate desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_FILE_STATS,
            query = "select extension, avg(fileLength) , min(fileLength) , max(fileLength) from InformationResourceFileVersion where fileVersionType in (:types) group by extension order by extension desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_RESOURCES_IN_PROJECT,
            query = "from InformationResource res where res.project.id=:projectId"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_DELETE_INFORMATION_RESOURCE_FILE_DERIVATIVES,
            query = "delete from InformationResourceFileVersion version where version.informationResourceFile.id=:informationResourceFileId and version.fileVersionType in (:derivativeFileVersionTypes)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_RESOURCES_IN_PROJECT_WITH_STATUS,
            query = "from InformationResource res where res.project.id=:projectId and res.status in (:statuses)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_RESOURCES_BY_DECADE,
            query = "select dateNormalized, count(res.id) from InformationResource res where res.status in (:statuses) and dateNormalized is not -1 and dateNormalized is not null group by dateNormalized order by dateNormalized asc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_CLEAR_REFERENCED_ONTOLOGYNODE_RULES,
            query = "UPDATE CodingRule cr set cr.ontologyNode=NULL where cr.ontologyNode in (:ontologyNodes)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.UPDATE_DATATABLECOLUMN_ONTOLOGIES,
            query = "UPDATE DataTableColumn set defaultOntology = :ontology where defaultCodingSheet = :codingSheet"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.SPACE_BY_SUBMITTER,
            query = "select sum( res.spaceInBytesUsed) as len, sum(res.filesUsed), count(res) from InformationResource res"
                    + " where res.submitter.id in (:submitterIds) and res.status in (:statuses) "
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.SPACE_BY_RESOURCE,
            query = "select sum( res.spaceInBytesUsed) as len, sum(res.filesUsed),  count(res) from InformationResource res "
                    + " where res.id in (:resourceIds) and res.status in (:statuses) "
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.SPACE_BY_PROJECT,
            query = "select sum( res.spaceInBytesUsed) as len, sum(res.filesUsed),  count(res) from InformationResource res "
                    + " where res.project.id in (:projectIds) and res.status in (:statuses) "
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.SPACE_BY_COLLECTION,
            query = "select sum( res.spaceInBytesUsed) as len, sum(res.filesUsed), count(res) from ResourceCollection coll left join coll.resources as res "
                    + " where coll.id in (:collectionIds) and res.status in (:statuses) "
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.ACCESS_BY,
            query = "select ref.id, ref.title, ref.resourceType, date_trunc('day', ras.date), count(ref) FROM ResourceAccessStatistic ras inner join ras.reference as ref where ras.date between :start and :end group by date_trunc('day', ras.date), ref having count(ref) > :minCount order by count(ref) desc, date_trunc('day', ras.date) desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.ACCESS_BY_OVERALL,
            query = "select ref.id, ref.title, ref.resourceType, count(ras.id) FROM ResourceAccessStatistic ras inner join ras.reference as ref where ras.date between :start and :end group by ref having count(ras.id) > :minCount order by count(ras.id) desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.RESOURCE_ACCESS_HISTORY,
            query = "select new org.tdar.struts.data.AggregateViewStatistic(ref.id, date_trunc('day', ras.date), count(ref)) FROM ResourceAccessStatistic ras inner join ras.reference as ref where ref.id in (:resourceIds) and ras.date between :start and :end group by date_trunc('day', ras.date), ref having count(ref) >= :minCount order by date_trunc('day', ras.date) desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.FILE_DOWNLOAD_HISTORY,
            query = "select new org.tdar.struts.data.AggregateDownloadStatistic(ref.id, date_trunc('day', ras.date), count(ref)) FROM FileDownloadStatistic ras inner join ras.reference as ref where ref.id in (:fileIds) and ras.date between :start and :end group by date_trunc('day', ras.date), ref having count(ref) >= :minCount order by date_trunc('day', ras.date) desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.DOWNLOAD_BY,
            query = "select date_trunc('day', ras.date),  count(ref),  ref.id  FROM FileDownloadStatistic ras inner join ras.reference as ref where ras.date between :start and :end group by date_trunc('day', ras.date), ref having count(ref) > :minCount order by count(ref) desc, date_trunc('day', ras.date) desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.ACCOUNT_GROUP_FOR_ACCOUNT,
            query = "select grp from AccountGroup grp inner join grp.accounts as account where account.id =:accountId"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.ACCOUNTS_FOR_PERSON,
            query = "select act from Account act left join act.authorizedMembers as person where ( act.owner.id =:personId or person.id=:personId) and act.status in (:statuses)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.ACCOUNT_GROUPS_FOR_PERSON,
            query = "select act from AccountGroup act left join act.authorizedMembers as person where ( act.owner.id =:personId or person.id=:personId) and act.status in (:statuses)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.LOGS_FOR_RESOURCE,
            query = "select rlog from ResourceRevisionLog rlog where rlog.resource.id = :resourceId order by rlog.timestamp desc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.RESOURCES_WITH_NULL_ACCOUNT_ID,
            query = "select id from Resource res where res.id in (:ids) and account_id is null"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.RESOURCES_WITH_NON_MATCHING_ACCOUNT_ID,
            query = "select id from Resource res where res.id in (:ids) and account_id is not null and account_id !=:accountId"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.ACCOUNT_QUOTA_INIT,
            query = "select sum(res.filesUsed), sum(res.spaceInBytesUsed) from Resource res where account_id =:accountId and status in (:statuses)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_INFORMATIONRESOURCES_WITH_FILES,
            query = "SELECT file.informationResource.id from InformationResourceFile file"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.INVOICES_FOR_PERSON,
            query = "from Invoice where owner.id = :personId"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.UNASSIGNED_INVOICES_FOR_PERSON,
            query = "from Invoice where owner.id = :personId and account_id is null and transactionStatus in (:statuses)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.QUERY_LOGIN_STATS,
            query = "select totalLogins, count(id) from Person where totalLogins > 0 group by totalLogins order by totalLogins asc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.FIND_ACTIVE_COUPON,
            query = "from Coupon coupon where code=:code and (select count(*) from Invoice where coupon_id=coupon.id and owner.id !=:ownerId) = 0"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = TdarNamedQueries.FIND_INVOICE_FOR_COUPON,
            query = "from Invoice invoice where invoice.coupon.code=:code"
    ),
    @org.hibernate.annotations.NamedQuery(
            name=TdarNamedQueries.QUERY_FILE_STATUS,
            query= "from InformationResourceFile file where status in (:statuses)"),
    @org.hibernate.annotations.NamedQuery(
            name=TdarNamedQueries.QUERY_FILE_SIZE_TOTAL,
            query= "select vers.extension, sum(vers.fileLength) from InformationResourceFileVersion vers where fileVersionType in (:types) group by extension"),
    @org.hibernate.annotations.NamedQuery(
            name=TdarNamedQueries.QUERY_PROXY_RESOURCE_SHORT,
            query="select res from ResourceProxy res where res.id in (:ids)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name=TdarNamedQueries.QUERY_PROXY_RESOURCE_FULL,
            query="select res from ResourceProxy res fetch all properties left join fetch res.resourceCreators rc left join fetch res.latitudeLongitudeBoxes left join fetch rc.creator left join fetch res.informationResourceFileProxies left join fetch res.resourceCollections col left join fetch col.authorizedUsers user where res.id in (:ids)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name=TdarNamedQueries.QUERY_RESOURCE_FIND_OLD_LIST,
            query="select distinct res from Resource res where res.id in (:ids)"
    ),
    @org.hibernate.annotations.NamedQuery(
            name=TdarNamedQueries.FIND_ACCOUNT_FOR_INVOICE,
            query= "select account from Account account join account.invoices as invoice where invoice.id = :id"),
    @org.hibernate.annotations.NamedQuery(
            name=TdarNamedQueries.COLLECTION_LIST_WITH_AUTHUSER,
            query="select rescol from ResourceCollection rescol join rescol.authorizedUsers  as authUser where authUser.effectiveGeneralPermission > :effectivePermission  and authUser.user.id = :userId"),

    @org.hibernate.annotations.NamedQuery(
            name=TdarNamedQueries.QUERY_SPARSE_EDITABLE_SORTED_RESOURCES_INHERITED,
            query=  " SELECT distinct new Resource(res.id, res.title, res.resourceType) FROM Resource as res  where " +
                    " (TRUE=:allResourceTypes or res.resourceType in (:resourceTypes)) and (TRUE=:allStatuses or res.status in (:statuses) )  AND " +
                    " (res.submitter.id=:userId or exists (" +
                    " from ResourceCollection rescol  " +
                    " join rescol.resources as colres " +
                    " where " +
                    " colres.id = res.id and " +
                    " (TRUE=:admin or rescol.id in (:rescolIds) )))  "),
    @org.hibernate.annotations.NamedQuery(
            //select c.id, c.name from collection c left join collection_parents as p on c.id=p.collection_id left join authorized_user auth on c.id=auth.resource_collection_id where auth.general_permission_int>399 and auth.user_id=8092 or p.parent_id in (select resource_collection_id from authorized_user where authorized_user.general_permission_int>399 and authorized_user.user_id=8092);
            name=TdarNamedQueries.QUERY_SPARSE_EDITABLE_SORTED_RESOURCES_INHERITED_SORTED,
            query=  " SELECT distinct new Resource(res.id, res.title, res.resourceType) " + org.tdar.core.dao.TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX +
                    " order by res.title, res.id"),
    @org.hibernate.annotations.NamedQuery(
            name=TdarNamedQueries.DELETE_INFORMATION_RESOURCE_FILE_VERSION_IMMEDIATELY,
            query=  "DELETE InformationResourceFileVersion WHERE id = :id"
            ),
    @org.hibernate.annotations.NamedQuery(
	    name=TdarNamedQueries.QUERY_SPARSE_COLLECTION_RESOURCES,
	    query= "select new Resource(r.id, r.title, r.resourceType, r.status, r.submitter.id) from "
	            + " Resource r join r.resourceCollections rc where rc.id = :id"),
    @org.hibernate.annotations.NamedQuery(name=TdarNamedQueries.CREATOR_VIEW,
            query= "select count(*) from CreatorViewStatistic  where reference.id = :id"),
    @org.hibernate.annotations.NamedQuery(name=TdarNamedQueries.COLLECTION_VIEW,
            query= "select count(*) from ResourceCollectionViewStatistic where reference.id = :id"),
    @org.hibernate.annotations.NamedQuery(name=TdarNamedQueries.QUERY_COLLECTION_CHILDREN,
    query="from ResourceCollection rc inner join rc.parentIds parentId where parentId IN (:id) ")
})

package org.tdar.core.dao;

