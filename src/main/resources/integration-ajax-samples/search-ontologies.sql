--search ontologies
select
    r.id
    , r.title
    , cv.parent_id category_parent_id
    , cv.id category_id
    , cvp.name category_parent_name
    , cv.name category_name
    , r.submitter_id
    , (p.first_name || ' ' || p.last_name) submitter_display_name
    , r.date_registered
from
    resource r
    join information_resource ir on (ir.id = r.id)
    join ontology o on (o.id = r.id)
    join category_variable cv on (cv.id = o.category_variable_id)
    join category_variable cvp on (cvp.id = cv.parent_id)
    join person p on (p.id = r.submitter_id)
where
    (${category_id} is null or cv.id = ${category_id})
    --optionally filter by project (null to remove filter
    and (${project_id} is null or ir.project_id = ${project_id})
    --optionally filter by collection (note this clause only considers direct collection membership) (null to remove filter)
    and (${collection_id} is null or exists (
            select * from collection_resource cr where cr.resource_id = r.id and cr.collection_id = ${collection_id}
    ))
    --optionally filter bookmarked items ( arg:= null to remove filter)
    and (${bookmark_user_id} is null or exists (
            select * from bookmarked_resource br where br.resource_id = r.id and br.person_id = ${bookmark_user_id}
    ))
    --optionally filter by partial title ('' for no filter)
    and ( ${partial_title_text} = '' or r.title like '%' || ${partial_title_text} || '%')

    --optional filter by integrate-ability (note: I don't think this subquery is accurate)
    and (
        ${dataTableListSize} = 0  or exists (
                select * from data_table_column dtc where dtc.id in ${dataTableList} and dtc.default_ontology_id = o.id
        )
    )

limit 50 offset 0
;

