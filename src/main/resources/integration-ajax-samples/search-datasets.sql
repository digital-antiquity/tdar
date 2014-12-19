-- sql used for creating sample json data for new workspace actions

-- search datasets
select
    dt.dataset_id
    , dt.id data_table_id
    , r.title dataset_title
    , dt.name
    , dt.display_name
    , dt.description
    , (p.first_name || ' ' || p.last_name) submitter_display_name
    , r.date_registered
    , (select count(*) from data_table where dataset_id = r.id) sibling_count  -- helper to determine if we need to be more-specific than dataset title
from
    resource r
    join information_resource ir on (ir.id = r.id)
    join data_table dt on (dt.dataset_id = r.id)
    join person p on (p.id = r.submitter_id)
where
--optionally filter by project (null to remove filter
    (:project_id is null or ir.project_id = :project_id)
    --optionally filter by collection (note this clause only considers direct collection membership) (null to remove filter)
    and (:collection_id is null or exists (
            select * from collection_resource cr where cr.resource_id = r.id and cr.collection_id = :collection_id
    ))
    --optionally filter bookmarked items ( arg:= null to remove filter)
    and (:bookmark_user_id is null or exists (
            select * from bookmarked_resource br where br.resource_id = r.id and br.person_id = :bookmark_user_id
    ))
    --optionally filter compatible ontologies (arg1:= 0, arg2:=(null) to remove filter)
    and (( :sharedOntologyIdListSize = 0) or exists (
            select * from  data_table_column dtc where dtc.data_table_id = dt.id and dtc.default_ontology_id in :shared_ontology_id_list
    ))
    --optionally filter by partial title ('' for no filter)
    and ( :partial_title_text = '' or r.title like '%' || :partial_title_text || '%')
limit 50 offset 0
;


