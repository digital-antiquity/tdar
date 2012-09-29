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
SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;
SET search_path = public, pg_catalog;
SET SESSION AUTHORIZATION DEFAULT;
set constraints all deferred;
INSERT INTO creator (id, date_created, last_updated, location, url) VALUES (12088, NULL, NULL, NULL, NULL);
INSERT INTO creator (id, date_created, last_updated, location, url) VALUES (8092, NULL, NULL, NULL, NULL);
INSERT INTO creator (id, date_created, last_updated, location, url) VALUES (8093, NULL, NULL, NULL, NULL);
INSERT INTO institution(id,  "name") values (12088, 'University of TEST');
INSERT INTO person (id, contributor, email, first_name, last_name, registered, rpa_number, phone, contributor_reason, institution_id) VALUES (8092, true, 'test@tdar.org', 'test', 'user', true, NULL, '', NULL, 12088);
INSERT INTO person (id, contributor, email, first_name, last_name, registered, rpa_number, phone, contributor_reason, institution_id) VALUES (8093, true, 'admin@tdar.org', 'admin', 'user', true, NULL, '', NULL, 12088);
INSERT INTO resource (status, id, date_registered, description, resource_type, title, submitter_id, uploader_id, url) VALUES ('ACTIVE',1,   '2008-04-15 13:33:21.962',  N'This project contains all of your independent data resources.  These are data resources that you have not explicitly associated with any project.',  N'PROJECT',  N'Admin''s Independent Resources', 8093, 8093, NULL);
INSERT INTO resource (status, id, date_registered, description, resource_type, title, submitter_id, uploader_id, url) VALUES ('ACTIVE',3,   '2008-04-15 13:33:21.962',  N'This project contains all of your independent data resources.  These are data resources that you have not explicitly associated with any project.',  N'PROJECT',  N'Test''s Independent Resources', 8092, 8092, NULL);
INSERT INTO project (id) VALUES (1);
INSERT INTO project (id) VALUES (3);
--- ADD RESOURCE IDs TO EXTRACT OUT FROM tdarmetadata.zip
-- ====================================================================================================================----
-- END extract from SQLExtract
-- ====================================================================================================================----
SELECT setval('category_variable_id_seq', (SELECT MAX(id) FROM category_variable)+1);
SELECT setval('contributor_request_id_seq', (SELECT MAX(id) FROM contributor_request)+1);
SELECT setval('creator_id_seq', (SELECT MAX(id) FROM creator)+1);
SELECT setval('culture_keyword_id_seq', (SELECT MAX(id) FROM culture_keyword)+1);
SELECT setval('geographic_keyword_id_seq', (SELECT MAX(id) FROM geographic_keyword)+1);
SELECT setval('information_resource_file_id_seq', (SELECT MAX(id) FROM information_resource_file)+1);
SELECT setval('information_resource_file_version_id_seq', (SELECT MAX(id) FROM information_resource_file_version)+1);
SELECT setval('investigation_type_id_seq', (SELECT MAX(id) FROM investigation_type)+1);
SELECT setval('material_keyword_id_seq', (SELECT MAX(id) FROM material_keyword)+1);
SELECT setval('ontology_node_id_seq', (SELECT MAX(id) FROM ontology_node)+1);
SELECT setval('resource_revision_log_id_seq', (SELECT MAX(id) FROM resource_revision_log)+1);
SELECT setval('resource_sequence', (SELECT MAX(id) FROM resource)+1);
SELECT setval('site_name_keyword_id_seq', (SELECT MAX(id) FROM site_name_keyword)+1);
SELECT setval('site_type_keyword_id_seq', (SELECT MAX(id) FROM site_type_keyword)+1);
SELECT setval('source_collection_id_seq', (SELECT MAX(id) FROM source_collection)+1);
SELECT setval('temporal_keyword_id_seq', (SELECT MAX(id) FROM temporal_keyword)+1);
SELECT setval('bookmarked_resource_id_seq', (SELECT MAX(id) FROM bookmarked_resource)+1);
SELECT setval('coverage_date_id_seq', (SELECT MAX(id) FROM coverage_date)+1);
SELECT setval('coding_rule_id_seq', (SELECT MAX(id) FROM coding_rule)+1);
SELECT setval('data_table_id_seq', (SELECT MAX(id) FROM data_table)+1);
SELECT setval('data_table_column_id_seq', (SELECT MAX(id) FROM data_table_column)+1);
SELECT setval('data_table_relationship_id_seq', (SELECT MAX(id) FROM data_table_relationship)+1);
SELECT setval('latitude_longitude_id_seq', (SELECT MAX(id) FROM latitude_longitude)+1);
SELECT setval('other_keyword_id_seq', (SELECT MAX(id) FROM other_keyword)+1);
SELECT setval('personal_filestore_ticket_id_seq', (SELECT MAX(id) FROM personal_filestore_ticket)+1);
SELECT setval('related_comparative_collection_id_seq', (SELECT MAX(id) FROM related_comparative_collection)+1);
SELECT setval('resource_annotation_id_seq', (SELECT MAX(id) FROM resource_annotation)+1);
SELECT setval('resource_annotation_key_id_seq', (SELECT MAX(id) FROM resource_annotation_key)+1);
SELECT setval('resource_creator_id_seq', (SELECT MAX(id) FROM resource_creator)+1);
SELECT setval('resource_note_id_seq', (SELECT MAX(id) FROM resource_note)+1);
SELECT setval('stats_id_seq', (SELECT MAX(id) FROM stats)+1);
SELECT setval('upgrade_task_id_seq', (SELECT MAX(id) FROM upgrade_task)+1);
SELECT setval('user_session_id_seq', (SELECT MAX(id) FROM user_session)+1);
SELECT setval('collection_id_seq', (SELECT MAX(id) FROM collection)+1);
SELECT setval('authorized_user_id_seq', (SELECT MAX(id) FROM authorized_user)+1);
UPDATE information_resource set inheriting_cultural_information=true where id=4230;
set constraints all immediate;
update data_table_column set column_encoding_type=NULL where column_encoding_type='NUMERIC' or column_encoding_type='TEXT' or column_encoding_type='';
insert into authorized_user (general_permission_int,general_permission,resource_collection_id, user_id) values (500,'MODIFY_RECORD',1391,60);
update information_resource set date_created=-1 where date_created is null;
update information_resource set date_created_normalized= round(date_created / 10) * 10 ;
update resource set description='this should not be null' where description is null or description ='';
update resource set date_updated=date_registered where date_updated is null;
update resource set updater_id=submitter_id where updater_id is null;
update data_table_column set mappingcolumn=false where mappingcolumn is null;
update data_table_column set visible=true where visible is null;
update data_table_column set ignorefileextension=true where ignorefileextension is null;
update person set username=email where registered=true;
update creator set status='ACTIVE';
update culture_keyword set status='ACTIVE';
update geographic_keyword set status='ACTIVE';
update investigation_type set status='ACTIVE';
update material_keyword set status='ACTIVE';
update other_keyword set status='ACTIVE';
update site_name_keyword set status='ACTIVE';
update site_type_keyword set status='ACTIVE';
update temporal_keyword set status='ACTIVE';
