/* FIXME: still unsupported
@org.hibernate.annotations.NamedNativeQueries({
    @org.hibernate.annotations.NamedNativeQuery(
            name = TdarNamedQueries.QUERY_DASHBOARD,
            query = TdarNamedQueries.QUERY_SQL_DASHBOARD
            ),
})
 */

@NamedQueries({
        // select distinct true from collection_resource, collection, authorized_user where user_id =2 and general_permissions='MODIFY_RECORD' and
        // authorized_user.resource_collection_id=collection.id and collection_resource.collection_id=collection.id
        // and (resource_id in(657) or resource_id in (2454));
        @NamedQuery(
                name = TdarNamedQueries.QUERY_IS_ALLOWED_TO,
                query = "SELECT distinct 1 from " +
                        " Resource res inner join res.managedResourceCollections as rescol inner join rescol.authorizedUsers " +
                        " as authUser where authUser.user.id=:userId and authUser.effectiveGeneralPermission > :effectivePermission and rescol.status='ACTIVE' AND "
                        + " (authUser.dateExpires is null or authUser.dateExpires > current_date() ) AND " +
                        " res.id in (:resourceIds)"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_COLLECTION_RESOURCES_WITH_STATUS,
                query = "SELECT res from Resource res inner join res.managedResourceCollections as rescol " +
                        "  where res.status in (:statuses) and rescol.id in (:ids) and rescol.status='ACTIVE' "),
        // NOTE QUERY below was modified, will need to confirm performance impact
        @NamedQuery(
                name = TdarNamedQueries.QUERY_IS_ALLOWED_TO_NEW, // NOTE: THIS MAY REQUIRE ADDITIONAL WORK
                query = "SELECT distinct 1 from ResourceCollection as rescol inner join rescol.authorizedUsers as authuser where authuser.user.id=:userId and authuser.effectiveGeneralPermission > :effectivePermission and "
                + " (authuser.dateExpires is null or authuser.dateExpires > current_date() ) AND "
                        + " rescol.id in (:resourceCollectionIds) and rescol.status='ACTIVE' "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME,
                query = "SELECT distinct resCol from ResourceCollection resCol where lower(trim(resCol.name)) like lower(trim(:name)) and resCol.status='ACTIVE' "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO,
                query = "SELECT distinct resCol from ResourceCollection resCol left join resCol.authorizedUsers as authUser where (authUser.user.id=:userId) and resCol.status='ACTIVE' and (:perm is null or authUser.effectiveGeneralPermission > :perm ) and (authUser.dateExpires is null or authUser.dateExpires > current_date() )"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RIGHTS_EXPIRY_RESOURCE,
                query = "SELECT au from AuthorizedUser au where (exists (select c.id from ResourceCollection c where c.id=au.collectionId and c.id in (select p.id from Resource r left join r.managedResourceCollections as p where r.id=:id ) "
                        + " or c.id in (select parentId from Resource r left join r.managedResourceCollections as p left join p.parentIds parentId where r.id=:id )) or au.resourceId=:id) "
                        + " and au.user.id=:userId and (:perm is null or au.effectiveGeneralPermission > :perm)"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RIGHTS_EXPIRY_COLLECTION,
                query = "SELECT au from AuthorizedUser au LEFT JOIN ResourceCollection c on au.collectionId=c.id "
                        + "left join c.parentIds parentId "
                        + "where au.user.id=:userId and (:perm is null or au.effectiveGeneralPermission > :perm) and ( au.collectionId =:id or parentId=:id)"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RIGHTS_EXPIRY_ACCOUNT,
                query = "SELECT au from AuthorizedUser au JOIN BillingAccount c on au.accountId=c.id "
                        + "where au.user.id=:userId and (:perm is null or au.effectiveGeneralPermission > :perm) and c.id=:id"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RIGHTS_EXPIRY_WORKFLOW,
                query = "SELECT au from AuthorizedUser au JOIN DataIntegrationWorkflow c on au.integrationId=c.id "
                        + "where au.user.id=:userId and (:perm is null or au.effectiveGeneralPermission > :perm) and c.id=:id"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_IS_ALLOWED_TO_MANAGE,
                query = "SELECT distinct 1 from " +
                        " ResourceCollection as rescol inner join rescol.authorizedUsers " +
                        " as authUser where authUser.user.id=:userId and authUser.effectiveGeneralPermission > :effectivePermission and " +
                        " rescol.id in (:resourceCollectionIds) and rescol.status='ACTIVE' and (authUser.dateExpires is null or authUser.dateExpires > current_date() )"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_RESOURCE_LOOKUP,
                query = "SELECT new Resource(res.id, res.title, res.resourceType, res.description, res.status) FROM Resource as res where res.id in (:ids) "),
        @NamedQuery(
                name = TdarNamedQueries.SCROLLABLE_SITEMAP,
                query = "SELECT new Resource(res.id, res.title, res.resourceType) from Resource as res where res.status='ACTIVE'"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_BY_TDAR_YEAR,
                query = "SELECT new Resource(res.id, res.title, res.resourceType, res.description, res.status) FROM Resource as res where res.dateCreated between :year_start and :year_end and status='ACTIVE' order by res.dateCreated "),
        @NamedQuery(
                name = TdarNamedQueries.FIND_BY_TDAR_YEAR_COUNT,
                query = "SELECT count(*) FROM Resource as res where res.dateCreated between :year_start and :year_end and status='ACTIVE'"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_CODING_SHEETS_USING_ONTOLOGY,
                query = "SELECT new CodingSheet(res.id, res.title, res.description, res.status) FROM CodingSheet as res where res.defaultOntology.id=:ontologyId and status in (:statuses) "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_SHARED_COLLECTION_LOOKUP,
                query = "SELECT new ResourceCollection(col.id, col.name, col.description, col.sortBy, col.hidden) FROM ResourceCollection col where col.id in (:ids) "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_EDITABLE_RESOURCES,
                query = "SELECT new Resource(res.id, res.title, res.resourceType) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_EDITABLE_RESOURCES,
                query = TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_EDITABLE_SORTED_RESOURCES,
                query = "SELECT distinct new Resource(res.id, res.title, res.resourceType) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SORTED_SUFFIX),

        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_ACTIVE_RESOURCES,
                query = "SELECT distinct new Resource(res.id, res.title, res.resourceType) from Resource as res where res.status in ('ACTIVE') "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_USER_GET_ALL_RESOURCES_COUNT,
                query = "SELECT count(res.id) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_PROJECTS,
                query = "select new Project(project.id,project.title) from Project project where " +
                        " project.status='ACTIVE' order by project.title"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_RESOURCES_SUBMITTER,
                query = "select new Resource(resource.id,resource.title,resource.resourceType) from Resource resource where (submitter.id=:submitter) "
                        + "and status like :status and resourceType=:resourceType"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCES_SUBMITTER,
                query = "select id from Resource where submitter.id = :submitterId "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RELATED_RESOURCES,
                query = "from ResourceRelationship where (sourceResource.id = :resourceId and sourceResource.status in (:statuses)) or (targetResource.id = :resourceId and targetResource.status in (:statuses))"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_RESOURCES,
                query = "select new Resource(resource.id,resource.title,resource.resourceType) from Resource resource where status like :status and resourceType=:resourceType"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_EMPTY_PROJECTS,
                query = "select new Project(project.id, project.title) from Project project where (submitter.id=:submitter) and project.status in ('ACTIVE', 'DRAFT') "
                        + " and not exists(select 1 from InformationResource ir  join ir.project p2 with (project = p2) where ir.status in ('ACTIVE','DRAFT'))"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_BOOKMARKEDRESOURCE_IS_ALREADY_BOOKMARKED,
                query = "from BookmarkedResource where resource.id=:resourceId and person.id=:personId"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_BOOKMARKEDRESOURCE_REMOVE_BOOKMARK,
                query = "delete BookmarkedResource where resource.id=:resourceId and person.id=:personId"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_BOOKMARKEDRESOURCE_FIND_RESOURCE_BY_PERSON,
                query = "select br.resource from BookmarkedResource br where br.person.id = :personId and br.resource.status in (:statuses) order by br.resource.resourceType "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_BOOKMARKEDRESOURCES_FOR_USER,
                query = "select br from BookmarkedResource br where br.person.id = :personId order by br.resource.resourceType "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_DATASET_CAN_LINK_TO_ONTOLOGY,
                query = "select code.defaultOntology from DataTable dt inner join dt.dataTableColumns as dtc inner join dtc.defaultCodingSheet as code " +
                        "where dt in (select datatable.id from Dataset dataset inner join dataset.dataTables as datatable where dataset.id=:datasetId ) "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_MAPPED_CODING_RULES,
                query = "FROM CodingRule WHERE codingSheet.id = :codingSheetId AND term IN (:valuesToMatch)"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_DATATABLE_RELATED_ID,
                query = "SELECT DISTINCT dt FROM DataTable dt join dt.dataTableColumns as dtc join dtc.defaultCodingSheet as code WHERE (code.defaultOntology.id=:relatedId  or code=:relatedId) and dt.id in (select datatable.id from Dataset dataset inner join dataset.dataTables as datatable where dataset.status!='DELETED') "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_DATATABLE_DATASET,
                query = "select d FROM Dataset d join d.dataTables as dt where dt.id=:dataTableId"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_DATATABLECOLUMN_WITH_DEFAULT_ONTOLOGY,
                query = "FROM DataTableColumn dtc inner join dtc.defaultCodingSheet as code WHERE dtc.dataTable.dataset.id=:datasetId AND code.defaultOntology IS NOT NULL ORDER BY dtc.id"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_INFORMATIONRESOURCE_FIND_BY_FILENAME,
                query = "SELECT file from InformationResourceFile as file, InformationResourceFileVersion as version where file.informationResource = :resource and "
                        + "file=version.informationResourceFile and file.latestVersion=version.version and version.filename = :filename"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_ONTOLOGYNODE_ALL_CHILDREN_WITH_WILDCARD,
                query = "from OntologyNode o where o.ontology.id=:ontologyId and index like :indexWildcardString"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_ONTOLOGYNODE_PARENT,
                query = "from OntologyNode o where o.ontology.id=:ontologyId and index like :index"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_ONTOLOGYNODE_ALL_CHILDREN,
                query = "from OntologyNode o " +
                        "where o.ontology.id=:ontologyId and o.intervalStart > :intervalStart and o.intervalEnd < :intervalEnd"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_RECENT_EDITS,
                query = "select new Resource(r.id, r.title,r.resourceType) from Resource r where (updater_id = :personId or submitter_id = :personId) and r.status!='DELETED' ORDER by date_updated desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCE_RESOURCETYPE,
                query = "select resourceType from Resource where id=:id"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCE_MODIFIED_SINCE,
                query = "select count(log) from ResourceRevisionLog log where log.resource = :id and log.timestamp > :date"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_PROJECT_COUNT_INTEGRATABLE_DATASETS,
                query = "select count(distinct ds.id) from Dataset as ds join ds.dataTables as dt " +
                        "join dt.dataTableColumns as dtc join dtc.defaultCodingSheet as code where code.defaultOntology <> null and ds.project.id = :projectId"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_PROJECTS_COUNT_INTEGRATABLE_DATASETS,
                query = "select count(distinct ds.id) from Dataset as ds join ds.dataTables as dt "
                        +
                        "join dt.dataTableColumns as dtc join dtc.defaultCodingSheet as code where code.defaultOntology <> null and ds.project.id in (:projectIdList)"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_ONTOLOGY,
                query = "select count(id) as mapped_data_value_count from CodingRule where ontologyNode.ontology.id=:ontologyId"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_COLUMN,
                query = "select count(id) as mapped_data_value_count from CodingRule where ontologyNode.ontology=:ontology and codingSheet=:codingSheet"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS,
                query = "select count(r.id) as resource_type_count from Resource as r where r.resourceType=:resourceType and r.status=:status"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_MANAGED_ISO_COUNTRIES,
                // NOTE: hibernate is not smart enough to handle the "group by kwd" it needs to be told to include ALL of the keyword attributes that
                // it's going to request in the setter.
                query = "select kwd.label , kwd.level, count(r.id), kwd.id from Resource r join " +
                        "r.managedGeographicKeywords as kwd where kwd.level='COUNTRY' and r.status='ACTIVE'  group by kwd.label , kwd.level, kwd.id"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_ACTIVE_RESOURCE_TYPE_COUNT,
                query = "select count(res.id) as count , res.resourceType as resourceType from Resource as res where res.status='ACTIVE' group by res.resourceType "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SHARED_COLLECTION_BY_PARENT,
                query = "from ResourceCollection as col where (col.parent.id=:parent or (col.parent.id is NULL AND :parent is NULL))  and (hidden=:visible or :visible is NULL) and col.status='ACTIVE' "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_COLLECTIONS_PUBLIC_ACTIVE,
                query = "SELECT id from ResourceCollection as col where col.hidden is false and col.status='ACTIVE'"),

        @NamedQuery(
                name = TdarNamedQueries.QUERY_SHARED_COLLECTION_BY_AUTH_OWNER,
                query = "select col from ResourceCollection as col left join col.authorizedUsers as authorizedUser where "
                        + "(authorizedUser.user.id=:authOwnerId and authorizedUser.effectiveGeneralPermission >  :equivPerm) and col.status='ACTIVE' and (authorizedUser.dateExpires is null or authorizedUser.dateExpires > current_date() ) order by col.name"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_COLLECTION_PUBLIC_WITH_HIDDEN_PARENT,
                query = "select distinct col from ResourceCollection as col left join col.parent as parent where parent.hidden=true and col.hidden=false and col.status='ACTIVE' "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS_BY_USER,
                query = "select res.resourceType, res.status, count(res.id) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX
                        + " group by res.resourceType, res.status"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_EXTERNAL_ID_SYNC,
                query = "select distinct res.id from Resource res left join res.informationResourceFiles irf where (irf.id > 0 or res.resourceType='PROJECT')  and (res.dateUpdated > :updatedDate or res.externalId is null)"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RECENT,
                query = "from Resource res where (res.dateUpdated > :updatedDate or res.dateCreated > :updatedDate )"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RECENT_INFORMATION_RESOURCE_WITH_DOI,
                query = "from InformationResource res where (res.dateUpdated between :startDate and :endDate or res.dateCreated between :startDate  and :endDate) and externalId != '' and externalId is not NULL"),

        @NamedQuery(
                name = TdarNamedQueries.QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_CONTROLLED,
                query = "select keyword, (select count(*) from Resource res inner join res.cultureKeywords rk where rk.id = keyword.id) as keywordCount from CultureKeyword keyword where keyword.approved = true order by keyword.index, keywordCount desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_UNCONTROLLED,
                query = "select keyword, (select count(*) from Resource res inner join res.cultureKeywords rk where rk.id = keyword.id) as keywordCount from CultureKeyword keyword where keyword.approved = false order by keyword.index, keywordCount desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_KEYWORD_COUNT_GEOGRAPHIC_KEYWORD,
                query = "select keyword, (select count(*) from Resource res inner join res.geographicKeywords rk where rk.id = keyword.id) as keywordCount from GeographicKeyword keyword order by keywordCount desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_KEYWORD_COUNT_INVESTIGATION_TYPE,
                query = "select keyword, (select count(*) from Resource res inner join res.investigationTypes rk where rk.id = keyword.id) as keywordCount from InvestigationType keyword order by keywordCount desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_KEYWORD_COUNT_MATERIAL_KEYWORD,
                query = "select keyword, (select count(*) from Resource res inner join res.materialKeywords rk where rk.id = keyword.id) as keywordCount from MaterialKeyword keyword order by keywordCount desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_KEYWORD_COUNT_OTHER_KEYWORD,
                query = "select keyword, (select count(*) from Resource res inner join res.otherKeywords rk where rk.id = keyword.id) as keywordCount from OtherKeyword keyword order by keywordCount desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_NAME_KEYWORD,
                query = "select keyword, (select count(*) from Resource res inner join res.siteNameKeywords rk where rk.id = keyword.id) as keywordCount from SiteNameKeyword keyword order by keywordCount desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_CONTROLLED,
                query = "select keyword, (select count(*) from Resource res inner join res.siteTypeKeywords rk where rk.id = keyword.id) as keywordCount from SiteTypeKeyword keyword where keyword.approved = true order by keyword.index, keywordCount desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_UNCONTROLLED,
                query = "select keyword, (select count(*) from Resource res inner join res.siteTypeKeywords rk where rk.id = keyword.id) as keywordCount from SiteTypeKeyword keyword where keyword.approved = false order by keyword.index, keywordCount desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_KEYWORD_COUNT_TEMPORAL_KEYWORD,
                query = "select keyword, (select count(*) from Resource res inner join res.temporalKeywords rk where rk.id = keyword.id) as keywordCount from TemporalKeyword keyword order by keywordCount desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_KEYWORD_COUNT_FILE_EXTENSION,
                query = "select lower(extension), count(*) from InformationResourceFileVersion where fileVersionType in (:internalTypes)  group by lower(extension) "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RECENT_USERS_ADDED,
                query = "select p from TdarUser p where status='ACTIVE' order by p.id desc"),
        // add boolean for contributor, (left join on resource exists
        @NamedQuery(
                name = TdarNamedQueries.QUERY_USAGE_STATS,
                query = "from AggregateStatistic where recordedDate between :fromDate and :toDate and statisticType in (:statTypes) order by recordedDate desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_FILE_STATS,
                query = "select lower(extension), avg(fileLength) , min(fileLength) , max(fileLength) from InformationResourceFileVersion where fileVersionType in (:types) group by lower(extension) order by lower(extension) desc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCES_IN_PROJECT,
                query = "from InformationResource res where res.project.id=:projectId"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_DELETE_INFORMATION_RESOURCE_FILE_DERIVATIVES,
                query = "delete from InformationResourceFileVersion version where version.informationResourceFile.id=:informationResourceFileId and version.fileVersionType in (:derivativeFileVersionTypes)"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCES_IN_PROJECT_WITH_STATUS,
                query = "from InformationResource res where res.project.id=:projectId and res.status in (:statuses)"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCES_BY_DECADE,
                query = "select new org.tdar.core.cache.BrowseDecadeCountCache(dateNormalized, count(res.id)) from InformationResource res where res.status in (:statuses) and dateNormalized is not -1 and dateNormalized is not null group by dateNormalized order by dateNormalized asc"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_CLEAR_REFERENCED_ONTOLOGYNODE_RULES,
                query = "UPDATE CodingRule cr set cr.ontologyNode=NULL where cr.ontologyNode in (:ontologyNodes)"),
        @NamedQuery(
                name = TdarNamedQueries.SPACE_BY_SUBMITTER,
                query = "select sum( res.spaceInBytesUsed) as len, sum(res.filesUsed), count(res) from InformationResource res"
                        + " where res.submitter.id in (:submitterIds) and res.status in (:statuses) "),
        @NamedQuery(
                name = TdarNamedQueries.SPACE_BY_RESOURCE,
                query = "select sum( res.spaceInBytesUsed) as len, sum(res.filesUsed),  count(res) from InformationResource res "
                        + " where res.id in (:resourceIds) and res.status in (:statuses) "),
        @NamedQuery(
                name = TdarNamedQueries.SPACE_BY_PROJECT,
                query = "select sum( res.spaceInBytesUsed) as len, sum(res.filesUsed),  count(res) from InformationResource res "
                        + " where res.project.id in (:projectIds) and res.status in (:statuses) "),
        @NamedQuery(
                name = TdarNamedQueries.SPACE_BY_COLLECTION,
                query = "select sum( res.spaceInBytesUsed) as len, sum(res.filesUsed), count(res) from Resource res where res.id in (select distinct res.id from ResourceCollection coll left join coll.parentIds parentId left join coll.managedResources as res "
                        + " where coll.id in (:collectionIds) or parentId in (:collectionIds))  and res.status in (:statuses) "),
        @org.hibernate.annotations.NamedQuery(
                name = TdarNamedQueries.FILE_DOWNLOAD_HISTORY,
                query = "select ras FROM AggregateDownloadStatistic ras inner join ras.file as ref where ref.id in (:fileIds) and ras.aggregateDate between :start and :end and ras.count >= :minCount order by ras.aggregateDate desc"),
        @NamedQuery(
                name = TdarNamedQueries.DOWNLOAD_BY,
                query = "select ras FROM AggregateDownloadStatistic ras inner join ras.file as ref where ras.aggregateDate between :start and :end and ras.count >= :minCount order by ras.aggregateDate desc"),
        @NamedQuery(
                name = TdarNamedQueries.ACCOUNT_GROUP_FOR_ACCOUNT,
                query = "select grp from BillingAccountGroup grp inner join grp.accounts as account where account.id =:accountId"),
        @NamedQuery(
                name = TdarNamedQueries.ACCOUNTS_FOR_PERSON,
                query = "from BillingAccount act where act.status in (:statuses) and (act.owner.id = :personid or exists (select authmem.user.id from act.authorizedUsers as authmem where authmem.user.id = :personid))"),
        @NamedQuery(
                name = TdarNamedQueries.ACCOUNT_GROUPS_FOR_PERSON,
                query = "from BillingAccountGroup act where act.status in (:statuses) and (act.owner.id = :personid or exists (select authmem.id from act.authorizedMembers as authmem where authmem.id = :personid))"),
        @NamedQuery(
                name = TdarNamedQueries.LOGS_FOR_RESOURCE,
                query = "select rlog from ResourceRevisionLog rlog where rlog.resource.id = :resourceId order by rlog.timestamp desc"),
        @NamedQuery(
                name = TdarNamedQueries.RESOURCES_WITH_NULL_ACCOUNT_ID,
                query = "select id from Resource res where res.id in (:ids) and account_id is null"),
        @NamedQuery(
                name = TdarNamedQueries.RESOURCES_WITH_NON_MATCHING_ACCOUNT_ID,
                query = "select id from Resource res where res.id in (:ids) and account_id is not null and account_id !=:accountId"),
        @NamedQuery(
                name = TdarNamedQueries.ACCOUNT_QUOTA_INIT,
                query = "select sum(res.filesUsed), sum(res.spaceInBytesUsed) from Resource res where account_id =:accountId and status in (:statuses)"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_INFORMATIONRESOURCES_WITH_FILES,
                query = "SELECT file.informationResource.id from InformationResourceFile file"),
        @NamedQuery(
                name = TdarNamedQueries.INVOICES_FOR_PERSON,
                query = "from Invoice where owner.id = :personId"),
        @NamedQuery(
                name = TdarNamedQueries.UNASSIGNED_INVOICES_FOR_PERSON,
                query = "from Invoice where owner.id = :personId and account_id is null and transactionStatus in (:statuses)"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_LOGIN_STATS,
                query = "select totalLogins, count(person.id) from TdarUser person where totalLogins > 0 group by totalLogins order by totalLogins asc"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_ACTIVE_COUPON,
                query = "from Coupon coupon where code=:code and (select count(*) from Invoice where coupon_id=coupon.id and owner.id !=:ownerId and owner.id is NOT null) = 0"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_INVOICE_FOR_COUPON,
                query = "from Invoice invoice where invoice.coupon.code=:code"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_FILE_STATUS,
                query = "from InformationResourceFile where status in (:statuses) and deleted=false"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCE_FILE_STATUS,
                query = "from ResourceProxy res fetch all properties left join fetch res.informationResourceFileProxies file  where res.status in (:statuses) and res.submitter.id=:submitterId and file.status in (:fileStatuses) and file.deleted is false"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_FILE_SIZE_TOTAL,
                query = "select vers.extension as ext , sum(vers.fileLength) as sum from InformationResourceFileVersion vers where vers.fileVersionType in (:types) group by vers.extension"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_PROXY_RESOURCE_SHORT,
                query = "select res from ResourceProxy res where res.id in (:ids)"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_PROXY_RESOURCE_FULL,
                query = "select res from ResourceProxy res fetch all properties left join fetch res.resourceCreators rc left join fetch res.latitudeLongitudeBoxes left join fetch rc.creator left join fetch res.informationResourceFileProxies left join fetch res.managedResourceCollections col left join fetch col.authorizedUsers user where res.id in (:ids) and col.status = 'ACTIVE' "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCE_FIND_OLD_LIST,
                query = "select distinct res from Resource res where res.id in (:ids)"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_ACCOUNT_FOR_INVOICE,
                query = "select account from BillingAccount account join account.invoices as invoice where invoice.id = :id"),
        @NamedQuery(
                name = TdarNamedQueries.SHARED_COLLECTION_LIST_WITH_AUTHUSER,
                query = "select rescol from ResourceCollection rescol join rescol.authorizedUsers  as authUser where authUser.effectiveGeneralPermission > :effectivePermission  and authUser.user.id = :userId and rescol.status='ACTIVE' and (authUser.dateExpires is null or authUser.dateExpires > current_date() )"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_EDITABLE_SORTED_RESOURCES_INHERITED,
                query = " SELECT distinct new Resource(res.id, res.title, res.resourceType) FROM Resource as res  where " +
                        " (TRUE=:allResourceTypes or res.resourceType in (:resourceTypes)) and (TRUE=:allStatuses or res.status in (:statuses) )  AND " +
                        " (res.submitter.id=:userId or exists (" +
                        " from ResourceCollection rescol  " +
                        " join rescol.managedResources as colres " +
                        " where " +
                        " colres.id = res.id and colres.status='ACTIVE'  and " +
                        " (TRUE=:admin or rescol.id in (:rescolIds) )))  "),
        @NamedQuery(
                // select c.id, c.name from collection c left join collection_parents as p on c.id=p.collection_id left join authorized_user auth on
                // c.id=auth.resource_collection_id where auth.general_permission_int>399 and auth.user_id=8092 or p.parent_id in (select resource_collection_id
                // from authorized_user where authorized_user.general_permission_int>399 and authorized_user.user_id=8092);
                name = TdarNamedQueries.QUERY_SPARSE_EDITABLE_SORTED_RESOURCES_INHERITED_SORTED,
                query = " SELECT distinct new Resource(res.id, res.title, res.resourceType) " + TdarNamedQueries.HQL_EDITABLE_RESOURCE_SUFFIX
                        + " order by res.title, res.id"),
        @NamedQuery(
                name = TdarNamedQueries.DELETE_INFORMATION_RESOURCE_FILE_VERSION_IMMEDIATELY,
                query = "DELETE InformationResourceFileVersion WHERE id = :id"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_CURRENT_USER_NOTIFICATIONS,
                query = "from UserNotification "
                        + "WHERE (messageType = 'SYSTEM_BROADCAST' OR tdarUser.id = :userId) and (expirationDate is null or expirationDate > now()) "
                        + "ORDER BY dateCreated DESC"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_USER_NOTIFICATIONS_BY_TYPE,
                query = "from UserNotification WHERE messageType = :messageType ORDER BY dateCreated DESC"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SPARSE_COLLECTION_RESOURCES,
                query = "select new Resource(r.id, r.title, r.resourceType, r.status, r.submitter.id) from "
                        + " Resource r join r.managedResourceCollections rc where rc.id = :id and rc.status='ACTIVE' "),
        @NamedQuery(name = TdarNamedQueries.CREATOR_VIEW,
                query = "select count(*) from CreatorViewStatistic  where reference.id = :id"),
        @NamedQuery(name = TdarNamedQueries.COLLECTION_VIEW,
                query = "select count(*) from ResourceCollectionViewStatistic where reference.id = :id"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SHARED_COLLECTION_CHILDREN_RESOURCES,
                query = "select r from Resource r "
                        + "left join r.managedResourceCollections as rc "
                        + "left join r.unmanagedResourceCollections as urc "
                        + "left join rc.parentIds parentId  "
                        + "left join urc.parentIds uparentId where "
                        + "((parentId IN (:id) or rc.id=:id) and rc.status='ACTIVE') or "
                        + "((uparentId IN (:id) or urc.id=:id) and urc.status='ACTIVE')"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_COLLECTION_CHILDREN_RESOURCES_COUNT,
                query = "select count(distinct res.id) from ResourceCollection rc left join rc.parentIds parentId join rc.managedResources res where (parentId IN (:id) or rc.id=:id) and rc.status='ACTIVE' "),
        @NamedQuery(name = TdarNamedQueries.QUERY_COLLECTION_CHILDREN,
                query = "from ResourceCollection rc inner join rc.parentIds parentId where parentId IN (:id) and rc.status='ACTIVE' "),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_INFORMATION_RESOURCE_FILE_VERSION_VERIFICATION,
                query = "select ir.id, irf.id, irf.latestVersion, irfv from ResourceProxy ir join ir.informationResourceFileProxies as irf join irf.informationResourceFileVersionProxies as irfv"),
        @NamedQuery(
                name = TdarNamedQueries.WEEKLY_EMAIL_STATS,
                query = "select count(*) from Email where status='SENT' and dateSent >= :date and userGenerated=TRUE"),

        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCE_FILE_EMBARGO_EXIPRED,
                query = "from InformationResourceFile irf where irf.id in (select irf_.id from InformationResource r join r.informationResourceFiles as irf_ where r.status in ('ACTIVE','DRAFT') and irf_.dateMadePublic < :dateStart and irf_.restriction like 'EMBARGO%' )"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_RESOURCE_FILE_EMBARGOING_TOMORROW,
                query = "from InformationResourceFile irf where irf.id in (select irf_.id from InformationResource r join r.informationResourceFiles as irf_ where r.status in ('ACTIVE','DRAFT') and irf_.dateMadePublic < :dateStart and irf_.dateMadePublic >:dateEnd  and irf_.restriction like 'EMBARGO%' )"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_INTEGRATION_DATA_TABLE,
                query = "select distinct dt, ds, ds.title " + TdarNamedQueries.INTEGRATION_DATA_TABLE_SUFFIX
                        + " order by ds.title, dt.displayName"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_INTEGRATION_DATA_TABLE_COUNT,
                query = "select count(distinct dt.id) " + TdarNamedQueries.INTEGRATION_DATA_TABLE_SUFFIX),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_INTEGRATION_ONTOLOGY,
                query = "select distinct ont from Ontology ont left join ont.managedResourceCollections as rc"
                        + " left join rc.parentIds parentId  "
                        + "where ont.status='ACTIVE' and (:projectId=-1L or ont.project.id=:projectId) and "
                        + " ont.title like :titleLookup and "
                        + "(:collectionId=-1L or (rc.id=:collectionId  or parentId=:collectionId) and rc.status='ACTIVE') and "
                        + "(:categoryVariableId=-1L or ont.categoryVariable.id=:categoryVariableId) and "
                        + "(:hasDatasets=false or ont.id in "
                        + "(select dtcont.id from DataTableColumn dtc inner join dtc.defaultCodingSheet.defaultOntology as dtcont where dtc.dataTable.dataset.status='ACTIVE' and dtc.dataTable.id in (:paddedDataTableIds))) and "
                        + "(:bookmarked=false or ont.id in (select b.resource.id from BookmarkedResource b where b.person.id=:submitterId) )"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_HOSTED_DOWNLOAD_AUTHORIZATION,
                query = "select da from DownloadAuthorization da inner join da.refererHostnames rh join da.sharedCollection as rc left join rc.parentIds as parentId where da.apiKey=:apiKey and lower(rh)=lower(:hostname) and (rc.status= 'ACTIVE' and (rc.id in (:collectionids) or parentId in (:collectionids)))"),
        @NamedQuery(
                name = TdarNamedQueries.CAN_EDIT_INSTITUTION,
                query = "select authorized from InstitutionManagementAuthorization ima where ima.user.id=:userId and ima.institution.id=:institutionId and authorized=true"),
        @NamedQuery(
                name = TdarNamedQueries.WORKFLOWS_BY_USER,
                query = "from DataIntegrationWorkflow w where submitter.id=:userId or w.hidden is false or exists (select authmem.user.id from w.authorizedUsers as authmem where authmem.user.id = :userId)"),
        @NamedQuery(
                name = TdarNamedQueries.WORKFLOWS_BY_USER_ADMIN,
                query = "from DataIntegrationWorkflow"),
        @NamedQuery(
                name = TdarNamedQueries.AGREEMENT_COUNTS,
                query = "select count(*), (select count(*) from TdarUser where contributor is true) from TdarUser user where status='ACTIVE'"),
        @NamedQuery(
                name = TdarNamedQueries.AFFILIATION_COUNTS,
                query = "select affiliation, count(id) from TdarUser user where status='ACTIVE' group by affiliation"),
        @NamedQuery(
                name = TdarNamedQueries.AFFILIATION_COUNTS_CONTRIBUTOR,
                query = "select affiliation, count(id) from TdarUser user where status='ACTIVE' and id in (select distinct uploader.id from Resource) group by affiliation"),
        @NamedQuery(
                name = TdarNamedQueries.DELETE_DATA_TABLE_COLUMN_RELATIONSHIPS,
                query = "DELETE FROM DataTableColumnRelationship rel where rel.id in (select distinct cr.id from DataTableRelationship dtr join dtr.columnRelationships as cr where dtr.id in (:ids))"),
        @NamedQuery(
                name = TdarNamedQueries.DELETE_DATA_TABLE_RELATIONSHIPS,
                query = "DELETE FROM DataTableRelationship dtr where dtr.id in :ids"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_BY_DOI,
                query = "from InformationResource where lower(externalId)=trim(lower(:doi))"),
        @NamedQuery(
                name = TdarNamedQueries.UPDATE_RESOURCE_IN_COLLECTION_TO_ACTIVE,
                query = "select res from Resource res inner join res.managedResourceCollections as rescol where rescol.id in (select coll.id from ResourceCollection coll left join coll.parentIds p where p=:id or coll.id=:id) and rescol.status='ACTIVE' and res.status='DRAFT'"),
        @NamedQuery(
                name = TdarNamedQueries.ALL_RESOURCES_IN_COLLECTION,
                query = "select res from Resource res inner join res.managedResourceCollections as rescol where rescol.id in (select coll.id from ResourceCollection coll left join coll.parentIds p where p=:id or coll.id=:id) and rescol.status='ACTIVE' and res.status!='DELETED'"),
        @NamedQuery(
                name = TdarNamedQueries.USERS_IN_COLLECTION,
                query = "select au from ResourceCollection rc inner join rc.authorizedUsers as au where rc.id=:id"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_SIMILAR_PEOPLE,
                query = "select p from Person p where lower(lastName) like lower(:lastName) and (lower(firstName) like lower(:firstName) or lower(firstName) like lower(:initial) or lower(firstName) like lower(:initial2) ) and status='ACTIVE' and p.id not in (select id from TdarUser)"),
        @NamedQuery(
                name = TdarNamedQueries.MAPPED_RESOURCES,
                query = "from InformationResource ir inner join ir.project as project inner join ir.mappedDataKeyColumn as col where :projectId = -1 or project.id=:projectId"),
        @NamedQuery(
                name = TdarNamedQueries.COUNT_MAPPED_RESOURCES,
                query = "select count(ir.id) from InformationResource ir inner join ir.project as project inner join ir.mappedDataKeyColumn as col"),
        @NamedQuery(
                name = TdarNamedQueries.CHECK_INVITES,
                query = "select ui from UserInvite ui inner join ui.user as user where lower(user.email) like lower(:email)"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_DOWNLOAD_AUTHORIZATION,
                query = "from DownloadAuthorization da where da.sharedCollection.id=:collectionId"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_RESOURCES_SHARED_WITH,
                query = "select new Resource(r.id, r.title, r.resourceType, r.description, r.status) from Resource r left join r.authorizedUsers ru "
                        + " where  ru.user=:user and r.status in ('ACTIVE','DRAFT') and (:admin is true or r.id in "
                        + "     (select r_.id from Resource r_ left join r_.managedResourceCollections as rc_ left join rc_.parentIds parentId left join r_.authorizedUsers au "
                        + "            where rc_.id in (:collectionIds) or parentId in (:collectionIds) or (au.user=:owner and (au.dateExpires is null or au.dateExpires > current_date() ))"
                        + "))"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_COLLECTIONS_SHARED_WITH,
                query = "select distinct s from ResourceCollection s inner join s.authorizedUsers au where au.user=:user and (au.dateExpires is null or au.dateExpires > current_date() ) and (:admin is true or s.id in ("
                        + "select s_.id from ResourceCollection s_  left join s_.parentIds parentId where s_.id in (:collectionIds) or parentId in (:collectionIds) "
                        + "))"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_RESOURCES_SHARED_WITH_USERS,
                query = "select distinct au.user from Resource r inner join r.authorizedUsers au where (r.id in ("
                        + "select r_.id from Resource r_ left join r_.managedResourceCollections as rc_ left join rc_.parentIds parentId where  rc_.id in (:collectionIds) or parentId in (:collectionIds) "
                        + ")) and (au.dateExpires is null or au.dateExpires > current_date() )"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_COLLECTIONS_SHARED_WITH_USERS,
                query = "select distinct au.user from ResourceCollection s inner join s.authorizedUsers au where  (s.id in ("
                        + "select s_.id from ResourceCollection s_  left join s_.parentIds parentId where s_.id in (:collectionIds) or parentId in (:collectionIds) "
                        + ")) and (au.dateExpires is null or au.dateExpires > current_date() )"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_USERINVITES_BY_COLLECTION,
                query = "from UserInvite u where u.resourceCollection = :collection"),

        @NamedQuery(
                name = TdarNamedQueries.FIND_USERINVITES_BY_RESOURCE,
                query = "select ui from UserInvite ui where ui.resource = :resource "),
        @NamedQuery(
                name = TdarNamedQueries.FIND_USERINVITES_BY_USER,
                query = "from UserInvite u where u.user = :user"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_ALTERNATE_CHILDRENS,
                query = "from ResourceCollection rc where rc.alternateParent.id in :collectionIds"),
        @NamedQuery(
                name = TdarNamedQueries.QUERY_INSTITUTION_PEOPLE,
                query = "from Person p where p.status in ('ACTIVE','DUPLICATE') and p.institution.id in :id"),
        @NamedQuery(
                name = TdarNamedQueries.AUTHORIZED_USERS_FOR_RESOURCE,
                query = "from AuthorizedUser au where au.resourceId=:id and (au.dateExpires is null or au.dateExpires > current_date() )"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_EXPIRING_AUTH_USERS_FOR_COLLECTION,
                query = "select r from ResourceCollection r join r.authorizedUsers au where au.dateExpires < :date and status='ACTIVE'"),
        @NamedQuery(
                name = TdarNamedQueries.FIND_EXPIRING_AUTH_USERS_FOR_RESOURCE,
                query = "select r from Resource r join r.authorizedUsers au where au.dateExpires < :date and status in ('ACTIVE', 'DRAFT')"),
        @NamedQuery(
                name = org.tdar.core.dao.TdarNamedQueries.FIND_ALTERNATE_CHILDRENS_TREE,
                query = "from ResourceCollection rc where rc.alternateParent.id in :collectionIds or rc.parent.id in ( select id from ResourceCollection rc1 where rc1.alternateParent.id in :collectionIds )"),
        @NamedQuery(
                name = org.tdar.core.dao.TdarNamedQueries.FIND_ALTERNATE_LIST_CHILDRENS_TREE,
                query = "from ResourceCollection rc where rc.alternateParent.id in :collectionIds or rc.parent.id in ( select id from ResourceCollection rc1 where rc1.alternateParent.id in :collectionIds )"),
        @NamedQuery(
                name = org.tdar.core.dao.TdarNamedQueries.MONTHLY_USAGE_FOR_RESOURCE,
                query = "from AggregateDayViewStatistic vs where vs.resource.id=:resourceId"),
        @NamedQuery(
                name = org.tdar.core.dao.TdarNamedQueries.FIND_EMAIL_BY_GUID,
                query = "from Email email where email.messageUuid=:guid"),
        @NamedQuery(
                name = org.tdar.core.dao.TdarNamedQueries.FIND_FILES_BY_STATUS,
                query = "from TdarFile file where file.status in (:status)"),
        @NamedQuery(
                name = org.tdar.core.dao.TdarNamedQueries.LIST_FILES_FOR_DIR,
                query = "from AbstractFile file where (:topLevel is true and file.parent is null or file.parent.id=:parentId or :term is not null) "
                        + "and (:account is null or file.account=:account) "
                        + "and (:term is null or lower(file.filename) like :term  )"
                        + "and (:uploader is null or file.uploader=:uploader) and part_of_id is null"),
        @NamedQuery(
                name = org.tdar.core.dao.TdarNamedQueries.LIST_DIR,
                query = "from TdarDir file where (:topLevel is true or :parentId is not null and file.parent.id=:parentId) "
                        + "and (file.account=:account) "),
        @NamedQuery(
                name = org.tdar.core.dao.TdarNamedQueries.FIND_DIR_BY_NAME,
                query = "from TdarDir file where file.filename =:name and (:account is null or file.account=:account) and (:uploader is null or file.uploader=:uploader)"),
        @NamedQuery( 
                name = org.tdar.core.dao.TdarNamedQueries.SUMMARIZE_BY_ACCOUNT,
                query = "select f.parentId, count(*), count(f.resourceId) as resource_id, count(f.dateCurated) as curated, count(f.dateInitialReviewed) as initial_reviewed , "
                        + "count(f.dateReviewed) as reviewed , count(f.dateExternalReviewed) as external_revieewd from TdarFile f where f.partOfId is null and f.account.id=:accountId "
                        + "and (cast(:date as date) is null "
                        + " or (:date < f.dateCreated or :date < f.dateCurated or :date < f.dateInitialReviewed or :date < f.dateReviewed or :date < f.dateExternalReviewed)"
                        + ") group by f.parentId"),
        @NamedQuery(
                name = org.tdar.core.dao.TdarNamedQueries.BY_ACCOUNT_RECENT,
                query = "from TdarFile f where part_of_id is null and account_id=:accountId and (cast(:dateStart as date) is null or " + 
                        "(:dateStart < f.dateCreated or :dateStart < f.dateCurated or :dateStart < f.dateInitialReviewed or :dateStart < f.dateReviewed or :dateStart < f.dateExternalReviewed)"
                        + ")  AND  (cast(:dateEnd as date) is null or "
                        + "(:dateEnd < f.dateCreated or :dateEnd < f.dateCurated or :dateEnd < f.dateInitialReviewed or :dateEnd < f.dateReviewed or :dateEnd < f.dateExternalReviewed)"
                        + ") and (:dir is null or :dir = f.parent) "),
        @NamedQuery(
                name = org.tdar.core.dao.TdarNamedQueries.QUERY_FILE_UPLOAD_SWEEP,
                query = "from TdarFile f where f.parent.id in (select f_.parent.id from TdarFile f_ where f_.dateCreated > :date) and (f.status='UPLOADED' or f.status='') ")
})
package org.tdar.core.dao;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
