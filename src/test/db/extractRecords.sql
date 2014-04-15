------------------------------------------------------------------------------------------------
--- NOTE: THIS FILE IS COMPLETELY GENERATED FROM extractRecords.sql DO NOT EDIT MANUALLY
--- UNLESS ABSOLUTELY NECESSARY
------------------------------------------------------------------------------------------------
--- 
--- INSTRUCTIONS:
--- 1. edit the extractRecords.sql file to make sure that all tables are represented in the select statements
--- 2. unzip src/main/db/tdarmetadata.zip  and load it into tdarmetadata tdatabase
--- 3. update with upgrade-db.sql 
--- 4. run the maven profile sqlExtract < mvn -PsqlExtract clean initialize compile exec:java
--- 5. test tDAR
--- 6. run pg_dump -U tdar tdarmetadata > tdarmetadata.sql
--- 7. zip back up database
---
---
--- PostgreSQL database dump
---
--- pg_dump --disable-triggers --column-inserts -a -U tdar -t culture_keyword -t site_type_keyword -t material_keyword -t investigation_type -t creator_person_role -t creator_institution_role -t dcmi_type -t information_resource_format -t information_resource_format_file_extension -t language -t category_variable -t category_variable_synonyms tdarmetadata > init-test.sql

--DONT-PROCESS-- SET statement_timeout = 0;
--DONT-PROCESS-- SET client_encoding = 'UTF8';
--DONT-PROCESS-- SET standard_conforming_strings = off;
--DONT-PROCESS-- SET check_function_bodies = false;
--DONT-PROCESS-- SET client_min_messages = warning;
--DONT-PROCESS-- SET escape_string_warning = off;

--DONT-PROCESS-- SET search_path = public, pg_catalog;

--DONT-PROCESS-- SET SESSION AUTHORIZATION DEFAULT;
--DONT-PROCESS-- set constraints all deferred;

--DONT-PROCESS-- INSERT INTO creator (id, date_created, last_updated, url) VALUES (12088, NULL, NULL, NULL);
--DONT-PROCESS-- INSERT INTO creator (id, date_created, last_updated, url) VALUES (8092, NULL, NULL, NULL);
--DONT-PROCESS-- INSERT INTO creator (id, date_created, last_updated, url) VALUES (8093, NULL, NULL, NULL);
--DONT-PROCESS-- INSERT INTO creator (id, date_created, last_updated, url) VALUES (8094, NULL, NULL, NULL);
--DONT-PROCESS-- INSERT INTO creator (id, date_created, last_updated, url) VALUES (8095, NULL, NULL, NULL);

--DONT-PROCESS-- INSERT INTO institution(id,  "name") values (12088, 'University of TEST');
--DONT-PROCESS-- INSERT INTO person (id, email, first_name, last_name, registered, rpa_number, phone, institution_id) VALUES (8092, 'test@tdar.org', 'test', 'user', true, NULL, '', 12088);
--DONT-PROCESS-- INSERT INTO person (id, email, first_name, last_name, registered, rpa_number, phone, institution_id) VALUES (8093, 'admin@tdar.org', 'admin', 'user', true, NULL, '', 12088);
--DONT-PROCESS-- INSERT INTO person (id, email, first_name, last_name, registered, rpa_number, phone, institution_id) VALUES (8094, 'editor@tdar.org', 'editor', 'user', true, NULL, '', 12088);
--DONT-PROCESS-- INSERT INTO person (id, email, first_name, last_name, registered, rpa_number, phone, institution_id) VALUES (8095, 'billing@tdar.org', 'billing', 'user', true, NULL, '', 12088);

--DONT-PROCESS--INSERT INTO user_info(user_id, contributor) VALUES (8092, true); 
--DONT-PROCESS--INSERT INTO user_info(user_id, contributor) VALUES (8093, true); 
--DONT-PROCESS--INSERT INTO user_info(user_id, contributor) VALUES (8094, true); 
--DONT-PROCESS--INSERT INTO user_info(user_id, contributor) VALUES (8095, true); 


--DONT-PROCESS-- INSERT INTO resource (status, id, date_registered, description, resource_type, title, submitter_id, uploader_id, url) VALUES ('ACTIVE',1,   '2008-04-15 13:33:21.962',  N'This project contains all of your independent data resources.  These are data resources that you have not explicitly associated with any project.',  N'PROJECT',  N'Admin''s Independent Resources', 8093, 8093, NULL);
--DONT-PROCESS-- INSERT INTO resource (status, id, date_registered, description, resource_type, title, submitter_id, uploader_id, url) VALUES ('ACTIVE',3,   '2008-04-15 13:33:21.962',  N'This project contains all of your independent data resources.  These are data resources that you have not explicitly associated with any project.',  N'PROJECT',  N'Test''s Independent Resources', 8092, 8092, NULL);
--DONT-PROCESS-- INSERT INTO project (id) VALUES (1);
--DONT-PROCESS-- INSERT INTO project (id) VALUES (3);


create temporary table test (id bigint);
--- ADD RESOURCE IDs TO EXTRACT OUT FROM tdarmetadata.zip
insert into test (id) VALUES(4),(3794),(191),(322),(140),(627),(626),(141),(142),(151),(165),(161),(155),(162),(148),(164),(144),(143),(163),(149),(153),(159),(160),(157),(146),(156),(154),(152),(147),(158),(150),(145),(636),(166),(167),(183),(184),(170),(4230),(4231),(4232),(1628),(262),(1656),(449),(2420),(139),(3738),(3805),(3479),(4287),(3088),(3074), (3029);
create temporary table creatorids (id bigint);
insert into creatorids select submitter_id from resource where id in (select id from test)
    UNION select updater_id from resource where id in (select id from test)
    UNION select user_id from authorized_user, collection_resource where authorized_user.resource_collection_id=collection_resource.collection_id and resource_id in (select id from test) 
    UNION select person_id from bookmarked_resource where resource_id in (select id from test)
    UNION select publisher_id from information_resource
    UNION select creator_id from resource_creator where resource_id in (select id from test)
    UNION select institution_id from person where id in (select submitter_id from resource where id in (select id from test)
      UNION select user_id from authorized_user, collection_resource where authorized_user.resource_collection_id=collection_resource.collection_id and resource_id in (select id from test) 
      UNION select creator_id from resource_creator where resource_id in (select id from test)
      UNION select person_id from bookmarked_resource where resource_id in (select id from test) )
    UNION select provider_institution_id from information_resource where id in (select id from test);
    
select * from category_variable where type = 'CATEGORY' order by id;
select * from category_variable where type = 'SUBCATEGORY' order by id asc;
select * from category_variable_synonyms;
select * from culture_keyword where approved is true order by parent_id desc, id asc;   
select * from site_type_keyword where approved is true order by index asc; 
select * from investigation_type; 
select * from material_keyword; 


select * from creator where id in (select id from creatorIds) ;
select * from user_info where user_id in (select id from creatorIds);
select * from institution where id in (select id from creatorIds);
select * from person where id in (select id from creatorIds);



select * from site_type_keyword where id in (select site_type_keyword_id from resource_site_type_keyword where resource_id in (select id from test)) and approved=false order by id asc; 
select * from culture_keyword where id in (select culture_keyword_id from resource_culture_keyword where resource_id in (select id from test)) and approved=false order by id asc; 
select * from temporal_keyword where id in (select temporal_keyword_id from resource_temporal_keyword where resource_id in (select id from test)) order by id asc;
select * from geographic_keyword where id in (select geographic_keyword_id from resource_geographic_keyword where resource_id in (select id from test)
    UNION select geographic_keyword_id from resource_managed_geographic_keyword where resource_id in (select id from test)) order by id asc;
select * from other_keyword where id in (select other_keyword_id from resource_other_keyword where resource_id in (select id from test)) order by id asc;
select * from site_name_keyword where id in (select site_name_keyword_id from resource_site_name_keyword where resource_id in (select id from test)) order by id asc;
     
select * from resource where id in (select id from test) or 
        id in (select default_coding_sheet_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test))) or
        id in (select default_ontology_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test))) or
        id in (select project_id from information_resource where id in (select id from test))
;

--todo: end this madness and find a solution that handles collections of arbitrary depth
--grandparent collections
select
    *
from
    collection
where
    exists(
        select
            *
        from
            test t 
                join collection_resource cr on (cr.resource_id = t.id)
                    join collection c2 on (c2.id = cr.collection_id)
                        join collection c3 on (c3.id = c2.parent_id)
        where
            collection.id = c3.parent_id
    )
;        

--parent collections
select
    *
from
    collection
where
    exists(
        select
            *
        from
            test t 
                join collection_resource cr on (cr.resource_id = t.id)
                    join collection c2 on (c2.id = cr.collection_id)
        where
            collection.id = c2.parent_id
    )
;        
        
select * from collection where collection.id in                               (select distinct collection_id from collection_resource where resource_id in (select id from test));
select * from collection_resource where resource_id in (select id from test);
select * from authorized_user where authorized_user.resource_collection_id in (select distinct collection_id from collection_resource where resource_id in (select id from test));
select * from project where id in (select id from test) or
    id in (select project_id from information_resource where id in (select id from test));

--select * from collection where id in (select collection_id from collection_resource where resource_id in (select id from test) or 
--        resource_id in (select default_coding_sheet_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test))) or
--        resource_id in (select default_ontology_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test))) or
--        resource_id in (select project_id from information_resource where id in (select id from test)));

--select * from collection_resource where resource_id in (select id from test) or 
--        resource_id in (select default_coding_sheet_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test))) or
--        resource_id in (select default_ontology_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test))) or
--        resource_id in (select project_id from information_resource where id in (select id from test));


select * from authorized_user where resource_collection_id in (select id from collection_resource where resource_id in (select id from test) or 
        resource_id in (select default_coding_sheet_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test))) or
        resource_id in (select default_ontology_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test))) or
        resource_id in (select project_id from information_resource where id in (select id from test)));

select * from information_resource where id in (select id from test) or
        id in (select default_ontology_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test))) or
        id in (select default_coding_sheet_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test)));;
select * from information_resource_file where information_resource_id in (select id from test);
select * from information_resource_file_version where information_resource_file_id in (select id from information_resource_file where information_resource_id in (select id from test));

select * from image where id in (select id from test);    
select * from coding_sheet where id in (select id from test) or
    id in (select default_coding_sheet_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test)));
select * from ontology where id in (select id from test) or
    id in (select default_ontology_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test)));
select * from dataset where id in (select id from test);
select * from data_table where dataset_id in (select id from test);
select * from data_table_column  where data_table_id in (select id from data_table where dataset_id in (select id from test));
select * from document where id in (select id from test);

select * from information_resource_related_citation where information_resource_id in (select id from test);
select * from information_resource_source_citation where information_resource_id in (select id from test);
select * from resource_culture_keyword where resource_id in (select id from test);
select * from resource_geographic_keyword where resource_id in (select id from test);
select * from resource_managed_geographic_keyword where resource_id in (select id from test);
select * from resource_investigation_type where resource_id in (select id from test);
select * from resource_material_keyword where resource_id in (select id from test);
select * from resource_other_keyword where resource_id in (select id from test);
select * from resource_revision_log where resource_id in (select id from test);
select * from resource_site_name_keyword where resource_id in (select id from test);
select * from resource_site_type_keyword where resource_id in (select id from test);
select * from resource_temporal_keyword where resource_id in (select id from test);
select * from source_collection where resource_id in (select id from test);
select * from related_comparative_collection where resource_id in (select id from test);
select * from ontology_node where ontology_id in (select id from test);
select * from ontology_node_synonym where ontologynode_id in (select id from ontology_node where ontology_id in (select id from test));
select * from coding_rule where coding_sheet_id in (select id from test);
select * from bookmarked_resource where resource_id in (select id from test);
select * from coverage_date where resource_id in (select id from test);
select * from latitude_longitude where resource_id in (select id from test);

select * from resource_creator where resource_id in (select id from test);

select * from resource_access_statistics where resource_id in (select id from test);
select * from information_resource_file_download_statistics where information_resource_file_id in (select id from information_resource_file where information_resource_id in (select id from test));
select * from resource_annotation where resource_id in (select id from test);

select resource_annotation_key.* from resource_annotation_key,resource_annotation where resourceannotationkey_id=resource_annotation_key.id and resource_id in (select id from test);
select * from resource_note where resource_id in (select id from test);

drop table test;


--DONT-PROCESS-- -- ====================================================================================================================----
--DONT-PROCESS-- -- END extract from SQLExtract
--DONT-PROCESS-- -- ====================================================================================================================----

-- added by jim 6/1/2013
--DONT-PROCESS-- INSERT INTO collection (id, date_created, date_updated, description, name, orientation, sort_order, collection_type, visible, owner_id, parent_id, updater_id) VALUES (1575, '2013-07-01 16:49:15.583', '2013-07-01 16:49:15.583', 'this is a test', 'sample collection', 'LIST', 'TITLE', 'SHARED', true, 8092, NULL, 8092);
--DONT-PROCESS-- INSERT INTO resource (id, date_registered, date_updated, title, account_id , total_space_in_bytes, status, resource_type, total_files, previous_status, description, url, uploader_id, submitter_id, updater_id) VALUES (4289, '2013-07-01 16:42:32.355', '2013-07-01 16:42:32.355', 'this is a test ', NULL, 0, 'ACTIVE', 'SENSORY_DATA', 0, 'ACTIVE', 'sample sensorydata', '', 8092, 8092, 8092);
--DONT-PROCESS-- INSERT INTO resource (id, date_registered, date_updated, title, account_id , total_space_in_bytes, status, resource_type, total_files, previous_status, description, url, uploader_id, submitter_id, updater_id) VALUES (4290, '2013-07-01 16:44:49.712', '2013-07-01 16:44:49.712', 'this is a test', NULL, 0, 'ACTIVE', 'VIDEO', 0, 'ACTIVE', 'sample video', '', 8092, 8092, 8092);
--DONT-PROCESS-- INSERT INTO resource (id, date_registered, date_updated, title, account_id , total_space_in_bytes, status, resource_type, total_files, previous_status, description, url, uploader_id, submitter_id, updater_id) VALUES (4291, '2013-07-01 16:46:42.454', '2013-07-01 16:46:42.454', 'test', NULL, 0, 'ACTIVE', 'GEOSPATIAL', 0, 'ACTIVE', 'sample geospatial', '', 8092, 8092, 8092);
--DONT-PROCESS-- INSERT INTO resource (id, date_registered, date_updated, title, account_id , total_space_in_bytes, status, resource_type, total_files, previous_status, description, url, uploader_id, submitter_id, updater_id) VALUES (4292, '2013-07-01 17:21:35.881', '2013-07-01 17:21:35.881', 'this is a test', NULL, 0, 'ACTIVE', 'IMAGE', 0, 'ACTIVE', 'sample image', '', 8092, 8092, 8092);

--DONT-PROCESS-- INSERT INTO resource_access_statistics (date_accessed, resource_id) VALUES ('2013-07-01 16:42:34.693', 4289);
--DONT-PROCESS-- INSERT INTO resource_access_statistics (date_accessed, resource_id) VALUES ('2013-07-01 16:44:51.07', 4290);
--DONT-PROCESS-- INSERT INTO resource_access_statistics (date_accessed, resource_id) VALUES ('2013-07-01 16:46:43.537', 4291);
--DONT-PROCESS-- INSERT INTO resource_access_statistics ( date_accessed, resource_id) VALUES ('2013-07-01 16:46:43.537', 4292);
--DONT-PROCESS-- INSERT INTO information_resource (copy_location, date_created, date_created_normalized ,id, external_reference) VALUES (NULL, 2013, 2010, 4289, false);
--DONT-PROCESS-- INSERT INTO information_resource (copy_location, date_created, date_created_normalized ,id, external_reference) VALUES ('', 2012, 2010,   4290, false);
--DONT-PROCESS-- INSERT INTO information_resource (copy_location, date_created, date_created_normalized ,id, external_reference) VALUES (NULL, 2012, 2010, 4291, false);
--DONT-PROCESS-- INSERT INTO information_resource (copy_location, date_created, date_created_normalized ,id, external_reference) VALUES (NULL, 2012, 2010, 4292, false);
--DONT-PROCESS-- INSERT INTO dataset (id) values(4291);
--DONT-PROCESS-- INSERT INTO dataset (id) values(4289);
--DONT-PROCESS-- INSERT INTO sensory_data (id, turntable_used , mesh_holes_filled , mesh_rgb_included , mesh_smoothing , mesh_data_reduction , premesh_color_editions , premesh_overlap_reduction , premesh_smoothing , premesh_subsampling , rgb_preserved_from_original, mesh_color_editions , mesh_healing_despiking ) VALUES (4289,false,false,false,false,false,false,false,false,false,false,false, false);
--DONT-PROCESS-- INSERT INTO video (id) VALUES (4290);
--DONT-PROCESS-- INSERT INTO geospatial (currentnessUpdateNotes,map_source,id) VALUES ('recent', 'bogus information',4291);
--DONT-PROCESS-- INSERT INTO image VALUES (4292);

-- end added by jim 6/1/2013
-- ab add 6/2/2103
--DONT-PROCESS-- INSERT INTO collection_resource(resource_id, collection_id) VALUES(4289,1575),(4290,1575),(4291,1575),(4292,1575);


--DONT-PROCESS-- SELECT setval('category_variable_id_seq', (SELECT MAX(id) FROM category_variable)+1);
--DONT-PROCESS-- SELECT setval('creator_id_seq', (SELECT MAX(id) FROM creator)+1);
--DONT-PROCESS-- SELECT setval('culture_keyword_id_seq', (SELECT MAX(id) FROM culture_keyword)+1);
--DONT-PROCESS-- SELECT setval('geographic_keyword_id_seq', (SELECT MAX(id) FROM geographic_keyword)+1);
--DONT-PROCESS-- SELECT setval('information_resource_file_id_seq', (SELECT MAX(id) FROM information_resource_file)+1);
--DONT-PROCESS-- SELECT setval('information_resource_file_version_id_seq', (SELECT MAX(id) FROM information_resource_file_version)+1);
--DONT-PROCESS-- SELECT setval('investigation_type_id_seq', (SELECT MAX(id) FROM investigation_type)+1);
--DONT-PROCESS-- SELECT setval('material_keyword_id_seq', (SELECT MAX(id) FROM material_keyword)+1);
--DONT-PROCESS-- SELECT setval('ontology_node_id_seq', (SELECT MAX(id) FROM ontology_node)+1);
--DONT-PROCESS-- SELECT setval('resource_revision_log_id_seq', (SELECT MAX(id) FROM resource_revision_log)+1);
--DONT-PROCESS-- SELECT setval('resource_sequence', (SELECT MAX(id) FROM resource)+1);
--DONT-PROCESS-- SELECT setval('site_name_keyword_id_seq', (SELECT MAX(id) FROM site_name_keyword)+1);
--DONT-PROCESS-- SELECT setval('site_type_keyword_id_seq', (SELECT MAX(id) FROM site_type_keyword)+1);
--DONT-PROCESS-- SELECT setval('source_collection_id_seq', (SELECT MAX(id) FROM source_collection)+1);
--DONT-PROCESS-- SELECT setval('temporal_keyword_id_seq', (SELECT MAX(id) FROM temporal_keyword)+1);

--DONT-PROCESS-- SELECT setval('bookmarked_resource_id_seq', (SELECT MAX(id) FROM bookmarked_resource)+1);
--DONT-PROCESS-- SELECT setval('coverage_date_id_seq', (SELECT MAX(id) FROM coverage_date)+1);
--DONT-PROCESS-- SELECT setval('coding_rule_id_seq', (SELECT MAX(id) FROM coding_rule)+1);
--DONT-PROCESS-- SELECT setval('data_table_id_seq', (SELECT MAX(id) FROM data_table)+1);
--DONT-PROCESS-- SELECT setval('data_table_column_id_seq', (SELECT MAX(id) FROM data_table_column)+1);
--DONT-PROCESS-- SELECT setval('data_table_relationship_id_seq', (SELECT MAX(id) FROM data_table_relationship)+1);
--DONT-PROCESS-- SELECT setval('latitude_longitude_id_seq', (SELECT MAX(id) FROM latitude_longitude)+1);
--DONT-PROCESS-- SELECT setval('other_keyword_id_seq', (SELECT MAX(id) FROM other_keyword)+1);
--DONT-PROCESS-- SELECT setval('personal_filestore_ticket_id_seq', (SELECT MAX(id) FROM personal_filestore_ticket)+1);
--DONT-PROCESS-- SELECT setval('related_comparative_collection_id_seq', (SELECT MAX(id) FROM related_comparative_collection)+1);
--DONT-PROCESS-- SELECT setval('resource_annotation_id_seq', (SELECT MAX(id) FROM resource_annotation)+1);
--DONT-PROCESS-- SELECT setval('resource_annotation_key_id_seq', (SELECT MAX(id) FROM resource_annotation_key)+1);
--DONT-PROCESS-- SELECT setval('resource_creator_id_seq', (SELECT MAX(id) FROM resource_creator)+1);
--DONT-PROCESS-- SELECT setval('resource_note_id_seq', (SELECT MAX(id) FROM resource_note)+1);
--DONT-PROCESS-- SELECT setval('stats_id_seq', (SELECT MAX(id) FROM stats)+1);
--DONT-PROCESS-- SELECT setval('upgrade_task_id_seq', (SELECT MAX(id) FROM upgrade_task)+1);
--DONT-PROCESS-- SELECT setval('user_session_id_seq', (SELECT MAX(id) FROM user_session)+1);

--DONT-PROCESS-- SELECT setval('collection_id_seq', (SELECT MAX(id) FROM collection)+1);
--DONT-PROCESS-- SELECT setval('authorized_user_id_seq', (SELECT MAX(id) FROM authorized_user)+1);


--DONT-PROCESS-- UPDATE information_resource set inheriting_cultural_information=true where id=4230;
--DONT-PROCESS-- set constraints all immediate;
--DONT-PROCESS-- update data_table_column set column_encoding_type=NULL where column_encoding_type='NUMERIC' or column_encoding_type='TEXT' or column_encoding_type='';
--DONT-PROCESS-- insert into authorized_user (general_permission_int,general_permission,resource_collection_id, user_id) values (500,'MODIFY_RECORD',1391,60);
--DONT-PROCESS-- update information_resource set date_created=-1 where date_created is null;
--DONT-PROCESS-- update information_resource set date_created_normalized= round(date_created / 10) * 10 ; 
--DONT-PROCESS-- update resource set description='this should not be null' where description is null or description ='';
--DONT-PROCESS-- update resource set date_updated=date_registered where date_updated is null;
--DONT-PROCESS-- update resource set updater_id=submitter_id where updater_id is null;
--DONT-PROCESS-- update data_table_column set mappingcolumn=false where mappingcolumn is null;
--DONT-PROCESS-- update data_table_column set visible=true where visible is null;
--DONT-PROCESS-- update data_table_column set ignorefileextension=true where ignorefileextension is null;
--DONT-PROCESS-- update person set username=email where registered=true;
--DONT-PROCESS-- update creator set status='ACTIVE';
--DONT-PROCESS-- update culture_keyword set status='ACTIVE';
--DONT-PROCESS-- update geographic_keyword set status='ACTIVE';
--DONT-PROCESS-- update investigation_type set status='ACTIVE';
--DONT-PROCESS-- update material_keyword set status='ACTIVE';
--DONT-PROCESS-- update other_keyword set status='ACTIVE';
--DONT-PROCESS-- update site_name_keyword set status='ACTIVE';
--DONT-PROCESS-- update site_type_keyword set status='ACTIVE';
--DONT-PROCESS-- update temporal_keyword set status='ACTIVE';
-- 12-06-12 -- adding some invalid billing values
--DONT-PROCESS-- insert into pos_billing_model (id, date_created, active, counting_files, counting_space, counting_resources) VALUES (1, now(), true, true, true, false);
--DONT-PROCESS-- insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price, min_allowed_files, model_id) values (true, 'error', 5,1,50,5, 55.21,400,1);
--DONT-PROCESS-- insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price, min_allowed_files, model_id) values (true, 'decline', 5,1,50,5, 55.11,400,1);
--DONT-PROCESS-- insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price, min_allowed_files, model_id) values (true, 'unknown', 5,1,50,5, 55.31,400,1);
--DONT-PROCESS-- insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price, min_allowed_files, model_id) values (false, 'inactive', 5,1,50,5, 550,400,1);
--DONT-PROCESS-- insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price, model_id) values (true, ' 1-  4', 1, 1, 10, 55,1);
--DONT-PROCESS-- insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price, model_id) values (true, ' 100 mb', 0, 0, 100, 50,1);
--DONT-PROCESS-- insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price, model_id) values (true, ' 5- 19', 1, 5, 10, 40,1);
--DONT-PROCESS-- insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price, model_id) values (true, '20- 49', 1, 20, 10, 35,1);
--DONT-PROCESS-- insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price, model_id) values (true, '50-500', 1, 50, 10, 31,1);
--DONT-PROCESS-- SELECT setval('pos_billing_model_id_seq', (SELECT MAX(id) FROM pos_billing_model)+1);
--DONT-PROCESS-- update pos_billing_activity set activity_type='PRODUCTION';
--DONT-PROCESS-- update pos_billing_activity set activity_type = 'TEST' where name in ('good','error', 'decline', 'unknown');
--DONT-PROCESS-- update user_info set tos_version = 99, contributor_agreement_version = 99;

