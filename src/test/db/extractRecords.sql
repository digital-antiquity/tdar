------------------------------------------------------------------------------------------------
--- NOTE: THIS FILE IS COMPLETELY GENERATED FROM extractRecords.sql DO NOT EDIT MANUALLY
--- UNLESS ABSOLUTELY NECESSARY
------------------------------------------------------------------------------------------------
--- 
--- INSTRUCTIONS:
--- 1. edit the extractRecords.sql file to make sure that all tables are represented in the select statements
--- 2. download a production copy of tDAR's database
--- 3. run the maven profile sqlExtract < mvn -PsqlExtract clean initialize compile exec:java
--- 4. take the output and completely replace extractRecords.sql
--- 5. test tDAR
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

--DONT-PROCESS-- INSERT INTO creator (id, date_created, last_updated, location, url) VALUES (12088, NULL, NULL, NULL, NULL);
--DONT-PROCESS-- INSERT INTO creator (id, date_created, last_updated, location, url) VALUES (8092, NULL, NULL, NULL, NULL);
--DONT-PROCESS-- INSERT INTO creator (id, date_created, last_updated, location, url) VALUES (8093, NULL, NULL, NULL, NULL);

--DONT-PROCESS-- INSERT INTO institution(id,  "name") values (12088, 'University of TEST');
--DONT-PROCESS-- INSERT INTO person (id, contributor, email, first_name, last_name, privileged, registered, rpa, rpa_number, phone, password, contributor_reason, institution_id) VALUES (8092, true, 'test@tdar.org', 'test', 'user', false, true, false, NULL, '', 'b2932f560866bd7fe93bb640f967ec3c82f6d260', NULL, 12088);
--DONT-PROCESS-- INSERT INTO person (id, contributor, email, first_name, last_name, privileged, registered, rpa, rpa_number, phone, password, contributor_reason, institution_id) VALUES (8093, true, 'admin@tdar.org', 'admin', 'user', true, true, false, NULL, '', '44f8309043ad4ac22af60fc43dd4403116f28750', NULL, 12088);


--DONT-PROCESS-- INSERT INTO resource (status, id, access_counter, date_registered, description, resource_type, title, submitter_id, url) VALUES ('ACTIVE',1, 0,   '2008-04-15 13:33:21.962',  N'This project contains all of your independent data resources.  These are data resources that you have not explicitly associated with any project.',  N'PROJECT',  N'Admin''s Independent Resources', 8093, NULL);
--DONT-PROCESS-- INSERT INTO resource (status, id, access_counter, date_registered, description, resource_type, title, submitter_id, url) VALUES ('ACTIVE',3, 0,   '2008-04-15 13:33:21.962',  N'This project contains all of your independent data resources.  These are data resources that you have not explicitly associated with any project.',  N'PROJECT',  N'Test''s Independent Resources', 8092, NULL);
--DONT-PROCESS-- INSERT INTO project (id) VALUES (1);
--DONT-PROCESS-- INSERT INTO project (id) VALUES (3);


create temporary table test (id bigint);
--- ADD RESOURCE IDs TO EXTRACT OUT FROM tdarmetadata.zip
insert into test (id) VALUES(4),(3794),(191),(322),(140),(627),(626),(141),(142),(151),(165),(161),(155),(162),(148),(164),(144),(143),(163),(149),(153),(159),(160),(157),(146),(156),(154),(152),(147),(158),(150),(145),(636),(166),(167),(183),(184),(170),(4230),(4231),(4232),(1628),(262),(1656),(449),(2420),(139),(3738),(3805),(3479),(4287),(3088),(3074), (3029);

select * from category_variable where type = 'CATEGORY' order by id;
select * from category_variable where type = 'SUBCATEGORY' order by id asc;
select * from category_variable_synonyms;
select * from culture_keyword where approved is true; 
select * from site_type_keyword where approved is true; 
select * from investigation_type; 
select * from material_keyword; 


select * from creator where id in (select submitter_id from resource where id in (select id from test)
    UNION select updater_id from resource where id in (select id from test)
    UNION select user_id from authorized_user, collection_resource where authorized_user.resource_collection_id=collection_resource.collection_id and resource_id in (select id from test) 
    UNION select person_id from bookmarked_resource where resource_id in (select id from test)
    UNION select creator_id from resource_creator where resource_id in (select id from test)
    UNION select institution_id from person where id in (select submitter_id from resource where id in (select id from test)
      UNION select user_id from authorized_user, collection_resource where authorized_user.resource_collection_id=collection_resource.collection_id and resource_id in (select id from test) 
      UNION select creator_id from resource_creator where resource_id in (select id from test)
      UNION select person_id from bookmarked_resource where resource_id in (select id from test) )
    UNION select provider_institution_id from information_resource where id in (select id from test)) ;

select * from institution where id in (select submitter_id from resource where id in (select id from test)
    UNION select updater_id from resource where id in (select id from test)
    UNION select user_id from authorized_user, collection_resource where authorized_user.resource_collection_id=collection_resource.collection_id and resource_id in (select id from test) 
    UNION select person_id from bookmarked_resource where resource_id in (select id from test)
    UNION select creator_id from resource_creator where resource_id in (select id from test)
    UNION select institution_id from person where id in (select submitter_id from resource where id in (select id from test)
      UNION select user_id from authorized_user, collection_resource where authorized_user.resource_collection_id=collection_resource.collection_id and resource_id in (select id from test) 
      UNION select creator_id from resource_creator where resource_id in (select id from test)
      UNION select person_id from bookmarked_resource where resource_id in (select id from test) )
    UNION select provider_institution_id from information_resource where id in (select id from test));

select * from person where id in (select submitter_id from resource where id in (select id from test)
    UNION select updater_id from resource where id in (select id from test)
    UNION select user_id from authorized_user, collection_resource where authorized_user.resource_collection_id=collection_resource.collection_id and resource_id in (select id from test) 
    UNION select person_id from bookmarked_resource where resource_id in (select id from test)
    UNION select creator_id from resource_creator where resource_id in (select id from test)
    UNION select institution_id from person where id in (select submitter_id from resource where id in (select id from test)
      UNION select user_id from authorized_user, collection_resource where authorized_user.resource_collection_id=collection_resource.collection_id and resource_id in (select id from test) 
      UNION select creator_id from resource_creator where resource_id in (select id from test)
      UNION select person_id from bookmarked_resource where resource_id in (select id from test) )
    UNION select provider_institution_id from information_resource where id in (select id from test));



select * from site_type_keyword where id in (select site_type_keyword_id from resource_site_type_keyword where resource_id in (select id from test)) and approved=false; 
select * from culture_keyword where id in (select culture_keyword_id from resource_culture_keyword where resource_id in (select id from test)) and approved=false; 
select * from temporal_keyword where id in (select temporal_keyword_id from resource_temporal_keyword where resource_id in (select id from test));
select * from geographic_keyword where id in (select geographic_keyword_id from resource_geographic_keyword where resource_id in (select id from test)
    UNION select geographic_keyword_id from resource_managed_geographic_keyword where resource_id in (select id from test));
select * from other_keyword where id in (select other_keyword_id from resource_other_keyword where resource_id in (select id from test));
select * from site_name_keyword where id in (select site_name_keyword_id from resource_site_name_keyword where resource_id in (select id from test));
     
select * from resource where id in (select id from test) or 
        id in (select default_coding_sheet_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test))) or
        id in (select default_ontology_id from data_table_column where data_table_id in (select id from data_table where dataset_id in (select id from test))) or
        id in (select project_id from information_resource where id in (select id from test));

select * from collection where collection.id in (select distinct collection_id from collection_resource where collection.id=collection_resource.collection_id and resource_id in (select id from test));
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
select * from data_value_ontology_node_mapping where data_table_column_id in (select id from test) or ontology_node_id in (select id from test);

select * from resource_creator where resource_id in (select id from test);


select * from resource_annotation where resource_id in (select id from test);

select resource_annotation_key.* from resource_annotation_key,resource_annotation where resourceannotationkey_id=resource_annotation_key.id and resource_id in (select id from test);
select * from resource_note where resource_id in (select id from test);

drop table test;


--DONT-PROCESS-- -- ====================================================================================================================----
--DONT-PROCESS-- -- END extract from SQLExtract
--DONT-PROCESS-- -- ====================================================================================================================----

--DONT-PROCESS-- SELECT setval('category_variable_id_seq', (SELECT MAX(id) FROM category_variable)+1);
--DONT-PROCESS-- SELECT setval('contributor_request_id_seq', (SELECT MAX(id) FROM contributor_request)+1);
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
--DONT-PROCESS-- SELECT setval('data_value_ontology_node_mapping_id_seq', (SELECT MAX(id) FROM data_value_ontology_node_mapping)+1);
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
