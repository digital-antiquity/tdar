
--if you want to manually make a new institution
INSERT INTO creator (date_created, last_updated, description, occurrence, status, url, browse_occurrence, hidden)
VALUES (now(), now(), 'SHESC @ ASU', 0, 'ACTIVE', 'http://shesc.asu.edu', 0, false)
;

--if you want to provide a parent institution (not sure this is a good idea, or if I'll even use this in the whitelabel layouut)
insert into institution (id, name, parentinstitution_id) values (
    (
        select id
        from creator
        where description = 'SHESC @ ASU'),
    'School of Human Evolution & Social Change',
    (
        select id
        from institution
        where name = 'Arizona State University'))
;


-- upgrade a collection into a whitelabel collection
insert into whitelabel_collection(id, institution_id) values (:id, :institution_id);

--upgrade a collection into a whitelabel collection (specify collection name and institution name)
insert into whitelabel_collection (id, institution_id) values (
    (
        select id
        from collection
        where name = 'Center for Archaeology and Society'),
    (
        select id
        from institution
        where name = 'School of Human Evolution & Social Change')
)
;


--turn features on/off
update whitelabel_collection
set
    custom_header_enabled   = true,
    search_enabled          = true,
    sub_collections_enabled = true,
    css                     = ''
where id = (
    select max(id)
    from whitelabel_collection)
;


--add a featured resource (checking to see if it's in the collection)
insert into whitelabel_featured_resource(collection_id, resource_id)
    select collection_id, resource_id from collection_resource where collection_id = ? and resource_id = ?

