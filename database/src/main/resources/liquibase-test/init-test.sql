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
INSERT INTO creator (id, date_created, last_updated, url) VALUES (12088, NULL, NULL, NULL);
INSERT INTO creator (id, date_created, last_updated, url) VALUES (8092, NULL, NULL, NULL);
INSERT INTO creator (id, date_created, last_updated, url) VALUES (8093, NULL, NULL, NULL);
INSERT INTO creator (id, date_created, last_updated, url) VALUES (8094, NULL, NULL, NULL);
INSERT INTO creator (id, date_created, last_updated, url) VALUES (8095, NULL, NULL, NULL);
INSERT INTO institution(id,  "name") values (12088, 'University of TEST');
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id) VALUES (8092, 'test@tdar.org', 'test', 'user', NULL, '', 12088);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id) VALUES (8093, 'admin@tdar.org', 'admin', 'user', NULL, '', 12088);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id) VALUES (8094, 'editor@tdar.org', 'editor', 'user', NULL, '', 12088);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id) VALUES (8095, 'billing@tdar.org', 'billing', 'user', NULL, '', 12088);
INSERT INTO tdar_user(id, username, contributor) VALUES (8092, 'test@tdar.org', true);
INSERT INTO tdar_user(id, username, contributor) VALUES (8093, 'admin@tdar.org', true);
INSERT INTO tdar_user(id, username, contributor) VALUES (8094, 'editor@tdar.org', true);
INSERT INTO tdar_user(id, username, contributor) VALUES (8095, 'billing@tdar.org', true);
INSERT INTO resource (status, id, date_registered, description, resource_type, title, submitter_id, uploader_id, url) VALUES ('ACTIVE',1,   '2008-04-15 13:33:21.962',  N'This project contains all of your independent data resources.  These are data resources that you have not explicitly associated with any project.',  N'PROJECT',  N'Admin''s Independent Resources', 8093, 8093, NULL);
INSERT INTO resource (status, id, date_registered, description, resource_type, title, submitter_id, uploader_id, url) VALUES ('ACTIVE',3,   '2008-04-15 13:33:21.962',  N'This project contains all of your independent data resources.  These are data resources that you have not explicitly associated with any project.',  N'PROJECT',  N'Test''s Independent Resources', 8092, 8092, NULL);
INSERT INTO project (id) VALUES (1);
INSERT INTO project (id) VALUES (3);
--- ADD RESOURCE IDs TO EXTRACT OUT FROM tdarmetadata.zip
--------------------------- category_variable ---------------------------
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (1, NULL,  N'Architecture',  N'CATEGORY', NULL,  N'Architecture');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (2, NULL,  N'Basketry',  N'CATEGORY', NULL,  N'Basketry');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (3, NULL,  N'Ceramic',  N'CATEGORY', NULL,  N'Ceramic');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (4, NULL,  N'Chipped Stone',  N'CATEGORY', NULL,  N'Chipped Stone');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (5, NULL,  N'Dating Sample',  N'CATEGORY', NULL,  N'Dating Sample');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (6, NULL,  N'Fauna',  N'CATEGORY', NULL,  N'Fauna');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (7, NULL,  N'Figurine',  N'CATEGORY', NULL,  N'Figurine');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (8, NULL,  N'Glass',  N'CATEGORY', NULL,  N'Glass');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (9, NULL,  N'Ground Stone',  N'CATEGORY', NULL,  N'Ground Stone');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (10, NULL,  N'Historic Other',  N'CATEGORY', NULL,  N'Historic Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (11, NULL,  N'Human Burial',  N'CATEGORY', NULL,  N'Human Burial');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (12, NULL,  N'Human Dental',  N'CATEGORY', NULL,  N'Human Dental');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (13, NULL,  N'Human Skeletal',  N'CATEGORY', NULL,  N'Human Skeletal');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (14, NULL,  N'Lookup',  N'CATEGORY', NULL,  N'Lookup');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (15, NULL,  N'Macrobotanical',  N'CATEGORY', NULL,  N'Macrobotanical');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (16, NULL,  N'Metal',  N'CATEGORY', NULL,  N'Metal');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (17, NULL,  N'Mineral',  N'CATEGORY', NULL,  N'Mineral');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (18, NULL,  N'Photograph',  N'CATEGORY', NULL,  N'Photograph');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (19, NULL,  N'Pollen',  N'CATEGORY', NULL,  N'Pollen');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (20, NULL,  N'Provenience and Context',  N'CATEGORY', NULL,  N'Provenience and Context');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (21, NULL,  N'Rock Art',  N'CATEGORY', NULL,  N'Rock Art');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (22, NULL,  N'Shell',  N'CATEGORY', NULL,  N'Shell');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (23, NULL,  N'Storage',  N'CATEGORY', NULL,  N'Storage');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (24, NULL,  N'Textile',  N'CATEGORY', NULL,  N'Textile');
--------------------------- category_variable ---------------------------
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (25, NULL,  N'Material',  N'SUBCATEGORY', 1,  N'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (26, NULL,  N'Measurement',  N'SUBCATEGORY', 1,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (27, NULL,  N'Style/Type',  N'SUBCATEGORY', 1,  N'Style Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (28, NULL,  N'Count',  N'SUBCATEGORY', 2,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (29, NULL,  N'Design',  N'SUBCATEGORY', 2,  N'Design');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (30, NULL,  N'Form',  N'SUBCATEGORY', 2,  N'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (31, NULL,  N'Function',  N'SUBCATEGORY', 2,  N'Function');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (32, NULL,  N'Material',  N'SUBCATEGORY', 2,  N'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (33, NULL,  N'Measurement',  N'SUBCATEGORY', 2,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (34, NULL,  N'Technique',  N'SUBCATEGORY', 2,  N'Technique');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (35, NULL,  N'Weight',  N'SUBCATEGORY', 2,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (36, NULL,  N'Composition',  N'SUBCATEGORY', 3,  N'Composition');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (37, NULL,  N'Count',  N'SUBCATEGORY', 3,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (38, NULL,  N'Design/Decorative Element',  N'SUBCATEGORY', 3,  N'Design Decorative Element');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (39, NULL,  N'Form',  N'SUBCATEGORY', 3,  N'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (40, NULL,  N'Measurement',  N'SUBCATEGORY', 3,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (41, NULL,  N'Paint',  N'SUBCATEGORY', 3,  N'Paint');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (42, NULL,  N'Part',  N'SUBCATEGORY', 3,  N'Part');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (43, NULL,  N'Paste',  N'SUBCATEGORY', 3,  N'Paste');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (44, NULL,  N'Residue',  N'SUBCATEGORY', 3,  N'Residue');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (45, NULL,  N'Surface Treatment',  N'SUBCATEGORY', 3,  N'Surface Treatment');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (46, NULL,  N'Temper/Inclusions',  N'SUBCATEGORY', 3,  N'Temper Inclusions');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (47, NULL,  N'Type',  N'SUBCATEGORY', 3,  N'Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (48, NULL,  N'Variety/Subtype',  N'SUBCATEGORY', 3,  N'Variety Subtype');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (49, NULL,  N'Ware',  N'SUBCATEGORY', 3,  N'Ware');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (50, NULL,  N'Weight',  N'SUBCATEGORY', 3,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (51, NULL,  N'Count',  N'SUBCATEGORY', 4,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (52, NULL,  N'Form',  N'SUBCATEGORY', 4,  N'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (53, NULL,  N'Material',  N'SUBCATEGORY', 4,  N'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (54, NULL,  N'Measurement',  N'SUBCATEGORY', 4,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (55, NULL,  N'Retouch',  N'SUBCATEGORY', 4,  N'Retouch');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (56, NULL,  N'Type',  N'SUBCATEGORY', 4,  N'Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (57, NULL,  N'Weight',  N'SUBCATEGORY', 4,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (58, NULL,  N'Method',  N'SUBCATEGORY', 5,  N'Method');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (59, NULL,  N'Date',  N'SUBCATEGORY', 5,  N'Date');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (60, NULL,  N'Error',  N'SUBCATEGORY', 5,  N'Error');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (61, NULL,  N'Age',  N'SUBCATEGORY', 6,  N'Age');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (62, NULL,  N'Anterior/Posterior',  N'SUBCATEGORY', 6,  N'Anterior Posterior');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (63, NULL,  N'Bone Artifact Form',  N'SUBCATEGORY', 6,  N'Bone Artifact Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (64, NULL,  N'Breakage',  N'SUBCATEGORY', 6,  N'Breakage');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (65, NULL,  N'Burning ',  N'SUBCATEGORY', 6,  N'Burning ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (66, NULL,  N'Butchering',  N'SUBCATEGORY', 6,  N'Butchering');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (67, NULL,  N'Completeness',  N'SUBCATEGORY', 6,  N'Completeness');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (68, NULL,  N'Condition',  N'SUBCATEGORY', 6,  N'Condition');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (69, NULL,  N'Count',  N'SUBCATEGORY', 6,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (70, NULL,  N'Cultural Modification',  N'SUBCATEGORY', 6,  N'Cultural Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (71, NULL,  N'Digestion',  N'SUBCATEGORY', 6,  N'Digestion');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (72, NULL,  N'Dorsal/Ventral',  N'SUBCATEGORY', 6,  N'Dorsal Ventral');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (73, NULL,  N'Element',  N'SUBCATEGORY', 6,  N'Element');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (74, NULL,  N'Erosion',  N'SUBCATEGORY', 6,  N'Erosion');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (75, NULL,  N'Fusion',  N'SUBCATEGORY', 6,  N'Fusion');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (76, NULL,  N'Gnawing/Animal Modification',  N'SUBCATEGORY', 6,  N'Gnawing Animal Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (77, NULL,  N'Measurement',  N'SUBCATEGORY', 6,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (78, NULL,  N'Modification',  N'SUBCATEGORY', 6,  N'Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (79, NULL,  N'Natural Modification',  N'SUBCATEGORY', 6,  N'Natural Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (80, NULL,  N'Pathologies',  N'SUBCATEGORY', 6,  N'Pathologies');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (81, NULL,  N'Portion/Proximal/Distal',  N'SUBCATEGORY', 6,  N'Portion Proximal Distal');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (82, NULL,  N'Sex',  N'SUBCATEGORY', 6,  N'Sex');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (83, NULL,  N'Side',  N'SUBCATEGORY', 6,  N'Side');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (84, NULL,  N'Spiral Fracture',  N'SUBCATEGORY', 6,  N'Spiral Fracture');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (85, NULL,  N'Taxon',  N'SUBCATEGORY', 6,  N'Taxon');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (86, NULL,  N'Weathering',  N'SUBCATEGORY', 6,  N'Weathering');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (87, NULL,  N'Weight',  N'SUBCATEGORY', 6,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (88, NULL,  N'Zone',  N'SUBCATEGORY', 6,  N'Zone');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (89, NULL,  N'Zone Scheme',  N'SUBCATEGORY', 6,  N'Zone Scheme');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (90, NULL,  N'Count',  N'SUBCATEGORY', 7,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (91, NULL,  N'Form',  N'SUBCATEGORY', 7,  N'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (92, NULL,  N'Material',  N'SUBCATEGORY', 7,  N'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (93, NULL,  N'Measurement',  N'SUBCATEGORY', 7,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (94, NULL,  N'Style/Type',  N'SUBCATEGORY', 7,  N'Style Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (95, NULL,  N'Count',  N'SUBCATEGORY', 8,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (96, NULL,  N'Date',  N'SUBCATEGORY', 8,  N'Date');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (97, NULL,  N'Form',  N'SUBCATEGORY', 8,  N'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (98, NULL,  N'Maker/Manufacturer',  N'SUBCATEGORY', 8,  N'Maker Manufacturer');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (99, NULL,  N'Material',  N'SUBCATEGORY', 8,  N'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (100, NULL,  N'Measurement',  N'SUBCATEGORY', 8,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (101, NULL,  N'Weight',  N'SUBCATEGORY', 8,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (102, NULL,  N'Completeness',  N'SUBCATEGORY', 9,  N'Completeness');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (103, NULL,  N'Count',  N'SUBCATEGORY', 9,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (104, NULL,  N'Form',  N'SUBCATEGORY', 9,  N'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (105, NULL,  N'Material',  N'SUBCATEGORY', 9,  N'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (106, NULL,  N'Measurement',  N'SUBCATEGORY', 9,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (107, NULL,  N'Weight',  N'SUBCATEGORY', 9,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (108, NULL,  N'Count',  N'SUBCATEGORY', 10,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (109, NULL,  N'Date',  N'SUBCATEGORY', 10,  N'Date');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (110, NULL,  N'Form',  N'SUBCATEGORY', 10,  N'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (111, NULL,  N'Maker/Manufacturer',  N'SUBCATEGORY', 10,  N'Maker Manufacturer');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (112, NULL,  N'Material',  N'SUBCATEGORY', 10,  N'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (113, NULL,  N'Measurement',  N'SUBCATEGORY', 10,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (114, NULL,  N'Weight',  N'SUBCATEGORY', 10,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (115, NULL,  N'Body Position/Flexure',  N'SUBCATEGORY', 11,  N'Body Position Flexure');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (116, NULL,  N'Body Posture',  N'SUBCATEGORY', 11,  N'Body Posture');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (117, NULL,  N'Body Preparation',  N'SUBCATEGORY', 11,  N'Body Preparation');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (118, NULL,  N'Burial Accompaniment',  N'SUBCATEGORY', 11,  N'Burial Accompaniment');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (119, NULL,  N'Burial Container ',  N'SUBCATEGORY', 11,  N'Burial Container ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (120, NULL,  N'Burial Facility',  N'SUBCATEGORY', 11,  N'Burial Facility');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (121, NULL,  N'Count',  N'SUBCATEGORY', 11,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (122, NULL,  N'Disturbance',  N'SUBCATEGORY', 11,  N'Disturbance');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (123, NULL,  N'Facing',  N'SUBCATEGORY', 11,  N'Facing');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (124, NULL,  N'Measurement',  N'SUBCATEGORY', 11,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (125, NULL,  N'Orientation/Alignment ',  N'SUBCATEGORY', 11,  N'Orientation Alignment ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (126, NULL,  N'Preservation',  N'SUBCATEGORY', 11,  N'Preservation');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (127, NULL,  N'Type of Interment',  N'SUBCATEGORY', 11,  N'Type of Interment');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (128, NULL,  N'Buccal/Lingual/Occlusal',  N'SUBCATEGORY', 12,  N'Buccal Lingual Occlusal');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (129, NULL,  N'Chemical Assay',  N'SUBCATEGORY', 12,  N'Chemical Assay');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (130, NULL,  N'Count   ',  N'SUBCATEGORY', 12,  N'Count   ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (131, NULL,  N'Cultural Modification',  N'SUBCATEGORY', 12,  N'Cultural Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (132, NULL,  N'Dental Pathologies',  N'SUBCATEGORY', 12,  N'Dental Pathologies');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (133, NULL,  N'Dental Wear',  N'SUBCATEGORY', 12,  N'Dental Wear');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (134, NULL,  N'Enamel Defects',  N'SUBCATEGORY', 12,  N'Enamel Defects');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (135, NULL,  N'Maxillary/Mandibular',  N'SUBCATEGORY', 12,  N'Maxillary Mandibular');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (136, NULL,  N'Measurement',  N'SUBCATEGORY', 12,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (137, NULL,  N'Permanent/Deciduous',  N'SUBCATEGORY', 12,  N'Permanent Deciduous');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (138, NULL,  N'Tooth (element)',  N'SUBCATEGORY', 12,  N'Tooth (element)');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (139, NULL,  N'Age',  N'SUBCATEGORY', 13,  N'Age');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (140, NULL,  N'Age Criteria',  N'SUBCATEGORY', 13,  N'Age Criteria');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (141, NULL,  N'Articulation',  N'SUBCATEGORY', 13,  N'Articulation');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (142, NULL,  N'Bone Segment (proximal/distal)',  N'SUBCATEGORY', 13,  N'Bone Segment (proximal distal)');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (143, NULL,  N'Chemical Assay',  N'SUBCATEGORY', 13,  N'Chemical Assay');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (144, NULL,  N'Completeness',  N'SUBCATEGORY', 13,  N'Completeness');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (145, NULL,  N'Condition',  N'SUBCATEGORY', 13,  N'Condition');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (146, NULL,  N'Count',  N'SUBCATEGORY', 13,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (147, NULL,  N'Cranial Deformation',  N'SUBCATEGORY', 13,  N'Cranial Deformation');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (148, NULL,  N'Crematory Burning',  N'SUBCATEGORY', 13,  N'Crematory Burning');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (149, NULL,  N'Cultural Modification ',  N'SUBCATEGORY', 13,  N'Cultural Modification ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (150, NULL,  N'Diet',  N'SUBCATEGORY', 13,  N'Diet');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (151, NULL,  N'Distrubance',  N'SUBCATEGORY', 13,  N'Distrubance');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (152, NULL,  N'Disturbance sources',  N'SUBCATEGORY', 13,  N'Disturbance sources');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (153, NULL,  N'Element',  N'SUBCATEGORY', 13,  N'Element');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (154, NULL,  N'Epiphyseal Union',  N'SUBCATEGORY', 13,  N'Epiphyseal Union');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (155, NULL,  N'Fracture/Breakage',  N'SUBCATEGORY', 13,  N'Fracture Breakage');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (156, NULL,  N'Health ',  N'SUBCATEGORY', 13,  N'Health ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (157, NULL,  N'Measurement',  N'SUBCATEGORY', 13,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (158, NULL,  N'Nonmetric Trait',  N'SUBCATEGORY', 13,  N'Nonmetric Trait');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (159, NULL,  N'Pathologies/Trauma',  N'SUBCATEGORY', 13,  N'Pathologies Trauma');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (160, NULL,  N'Preservation',  N'SUBCATEGORY', 13,  N'Preservation');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (161, NULL,  N'Sex',  N'SUBCATEGORY', 13,  N'Sex');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (162, NULL,  N'Sex criteria',  N'SUBCATEGORY', 13,  N'Sex criteria');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (163, NULL,  N'Side',  N'SUBCATEGORY', 13,  N'Side');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (164, NULL,  N'Weight',  N'SUBCATEGORY', 13,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (165, NULL,  N'Code',  N'SUBCATEGORY', 14,  N'Code');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (166, NULL,  N'Description',  N'SUBCATEGORY', 14,  N'Description');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (167, NULL,  N'Label',  N'SUBCATEGORY', 14,  N'Label');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (168, NULL,  N'Notes',  N'SUBCATEGORY', 14,  N'Notes');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (169, NULL,  N'Count',  N'SUBCATEGORY', 15,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (170, NULL,  N'Taxon',  N'SUBCATEGORY', 15,  N'Taxon');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (171, NULL,  N'Count',  N'SUBCATEGORY', 16,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (172, NULL,  N'Date',  N'SUBCATEGORY', 16,  N'Date');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (173, NULL,  N'Form',  N'SUBCATEGORY', 16,  N'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (174, NULL,  N'Maker/Manufacturer',  N'SUBCATEGORY', 16,  N'Maker Manufacturer');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (175, NULL,  N'Material',  N'SUBCATEGORY', 16,  N'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (176, NULL,  N'Measurement',  N'SUBCATEGORY', 16,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (177, NULL,  N'Weight',  N'SUBCATEGORY', 16,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (178, NULL,  N'Count',  N'SUBCATEGORY', 17,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (179, NULL,  N'Form',  N'SUBCATEGORY', 17,  N'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (180, NULL,  N'Measurement',  N'SUBCATEGORY', 17,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (181, NULL,  N'Mineral Type',  N'SUBCATEGORY', 17,  N'Mineral Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (182, NULL,  N'Weight',  N'SUBCATEGORY', 17,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (183, NULL,  N'Direction',  N'SUBCATEGORY', 18,  N'Direction');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (184, NULL,  N'Film Type',  N'SUBCATEGORY', 18,  N'Film Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (185, NULL,  N'Frame',  N'SUBCATEGORY', 18,  N'Frame');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (186, NULL,  N'ID',  N'SUBCATEGORY', 18,  N'ID');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (187, NULL,  N'Roll',  N'SUBCATEGORY', 18,  N'Roll');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (188, NULL,  N'Subject',  N'SUBCATEGORY', 18,  N'Subject');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (189, NULL,  N'Count',  N'SUBCATEGORY', 19,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (190, NULL,  N'Taxon',  N'SUBCATEGORY', 19,  N'Taxon');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (191, NULL,  N'Context',  N'SUBCATEGORY', 20,  N'Context');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (192, NULL,  N'Date',  N'SUBCATEGORY', 20,  N'Date');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (193, NULL,  N'Depth',  N'SUBCATEGORY', 20,  N'Depth');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (194, NULL,  N'East',  N'SUBCATEGORY', 20,  N'East');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (195, NULL,  N'Excavation Method',  N'SUBCATEGORY', 20,  N'Excavation Method');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (196, NULL,  N'Feature ID/Number',  N'SUBCATEGORY', 20,  N'Feature ID Number');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (197, NULL,  N'Feature Type',  N'SUBCATEGORY', 20,  N'Feature Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (198, NULL,  N'Horizontal Location',  N'SUBCATEGORY', 20,  N'Horizontal Location');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (199, NULL,  N'Inclusions',  N'SUBCATEGORY', 20,  N'Inclusions');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (200, NULL,  N'Item/Slash ',  N'SUBCATEGORY', 20,  N'Item Slash ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (201, NULL,  N'Level',  N'SUBCATEGORY', 20,  N'Level');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (202, NULL,  N'Locus',  N'SUBCATEGORY', 20,  N'Locus');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (203, NULL,  N'Lot',  N'SUBCATEGORY', 20,  N'Lot');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (204, NULL,  N'Measurement',  N'SUBCATEGORY', 20,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (205, NULL,  N'North',  N'SUBCATEGORY', 20,  N'North');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (206, NULL,  N'Project',  N'SUBCATEGORY', 20,  N'Project');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (207, NULL,  N'Recovery Method',  N'SUBCATEGORY', 20,  N'Recovery Method');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (208, NULL,  N'Sampling',  N'SUBCATEGORY', 20,  N'Sampling');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (209, NULL,  N'Screening',  N'SUBCATEGORY', 20,  N'Screening');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (210, NULL,  N'Site',  N'SUBCATEGORY', 20,  N'Site');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (211, NULL,  N'Soil Color',  N'SUBCATEGORY', 20,  N'Soil Color');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (212, NULL,  N'Stratum',  N'SUBCATEGORY', 20,  N'Stratum');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (213, NULL,  N'Unit',  N'SUBCATEGORY', 20,  N'Unit');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (214, NULL,  N'Vertical Position',  N'SUBCATEGORY', 20,  N'Vertical Position');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (215, NULL,  N'Volume',  N'SUBCATEGORY', 20,  N'Volume');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (216, NULL,  N'Exposure',  N'SUBCATEGORY', 21,  N'Exposure');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (217, NULL,  N'Form',  N'SUBCATEGORY', 21,  N'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (218, NULL,  N'Style',  N'SUBCATEGORY', 21,  N'Style');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (219, NULL,  N'Technology',  N'SUBCATEGORY', 21,  N'Technology');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (220, NULL,  N'Completeness',  N'SUBCATEGORY', 22,  N'Completeness');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (221, NULL,  N'Count',  N'SUBCATEGORY', 22,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (222, NULL,  N'Measurement',  N'SUBCATEGORY', 22,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (223, NULL,  N'Modification',  N'SUBCATEGORY', 22,  N'Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (224, NULL,  N'Taxon',  N'SUBCATEGORY', 22,  N'Taxon');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (225, NULL,  N'Weight',  N'SUBCATEGORY', 22,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (226, NULL,  N'Box Number',  N'SUBCATEGORY', 23,  N'Box Number');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (227, NULL,  N'Location',  N'SUBCATEGORY', 23,  N'Location');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (228, NULL,  N'Count',  N'SUBCATEGORY', 24,  N'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (229, NULL,  N'Design',  N'SUBCATEGORY', 24,  N'Design');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (230, NULL,  N'Form',  N'SUBCATEGORY', 24,  N'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (231, NULL,  N'Function',  N'SUBCATEGORY', 24,  N'Function');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (232, NULL,  N'Material',  N'SUBCATEGORY', 24,  N'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (233, NULL,  N'Measurement',  N'SUBCATEGORY', 24,  N'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (234, NULL,  N'Technique',  N'SUBCATEGORY', 24,  N'Technique');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (235, NULL,  N'Weight',  N'SUBCATEGORY', 24,  N'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (236, NULL,  N'Other',  N'SUBCATEGORY', 1,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (237, NULL,  N'Other',  N'SUBCATEGORY', 2,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (238, NULL,  N'Other',  N'SUBCATEGORY', 3,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (239, NULL,  N'Other',  N'SUBCATEGORY', 4,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (240, NULL,  N'Other',  N'SUBCATEGORY', 5,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (241, NULL,  N'Other',  N'SUBCATEGORY', 6,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (242, NULL,  N'Other',  N'SUBCATEGORY', 7,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (243, NULL,  N'Other',  N'SUBCATEGORY', 8,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (244, NULL,  N'Other',  N'SUBCATEGORY', 9,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (245, NULL,  N'Other',  N'SUBCATEGORY', 10,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (246, NULL,  N'Other',  N'SUBCATEGORY', 11,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (247, NULL,  N'Other',  N'SUBCATEGORY', 12,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (248, NULL,  N'Other',  N'SUBCATEGORY', 13,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (249, NULL,  N'Other',  N'SUBCATEGORY', 14,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (250, NULL,  N'Other',  N'SUBCATEGORY', 15,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (251, NULL,  N'Other',  N'SUBCATEGORY', 16,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (252, NULL,  N'Other',  N'SUBCATEGORY', 17,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (253, NULL,  N'Other',  N'SUBCATEGORY', 18,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (254, NULL,  N'Other',  N'SUBCATEGORY', 19,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (255, NULL,  N'Other',  N'SUBCATEGORY', 20,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (256, NULL,  N'Other',  N'SUBCATEGORY', 21,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (257, NULL,  N'Other',  N'SUBCATEGORY', 22,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (258, NULL,  N'Other',  N'SUBCATEGORY', 23,  N'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (259, NULL,  N'Other',  N'SUBCATEGORY', 24,  N'Other');
--------------------------- category_variable_synonyms ---------------------------
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (73,  N'Elements');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (85,  N'Species');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (83,  N'Symmetry');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (69,  N'Quantity');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (69,  N'Number');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (67,  N'Amount Present');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (64,  N'Fragmentation');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (198,  N'Room');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (198,  N'Operation');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (200,  N'Slash No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (198,  N'Feature No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (198,  N'FN');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (198,  N'Cluster');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203,  N'Lot No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203,  N'Spec');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203,  N'Spec No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203,  N'Log');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203,  N'Log No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203,  N'Bag');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203,  N'Bag No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203,  N'Case');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203,  N'Case No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (192,  N'Time');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (192,  N'Age');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (191,  N'Courtyard');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (191,  N'Feat# Cat#');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (207,  N'Recovery Method');
--------------------------- culture_keyword ---------------------------
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (3, NULL,  N'Pre-Clovis',  true ,  N'1',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (4, NULL,  N'PaleoIndian',  true ,  N'2',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (8, NULL,  N'Archaic',  true ,  N'3',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (12, NULL,  N'Hopewell',  true ,  N'4',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (13, NULL,  N'Woodland',  true ,  N'5',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (17, NULL,  N'Plains Village',  true ,  N'6',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (18, NULL,  N'Mississippian',  true ,  N'7',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (19, NULL,  N'Ancestral Puebloan',  true ,  N'8',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (21, NULL,  N'Hohokam',  true ,  N'9',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (23, NULL,  N'Patayan',  true ,  N'11',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (24, NULL,  N'Fremont',  true ,  N'12',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (25, NULL,  N'Historic',  true ,  N'13',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (34, NULL,  N'Mogollon',  true ,  N'10',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (26, NULL,  N'African American',  true ,  N'13.1',  true , 25,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (27, NULL,  N'Chinese American',  true ,  N'13.2',  true , 25,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (28, NULL,  N'Euroamerican',  true ,  N'13.3',  true , 25,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (29, NULL,  N'Japanese American',  true ,  N'13.4',  true , 25,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (30, NULL,  N'Native American',  true ,  N'13.5',  true , 25,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (31, NULL,  N'Spanish',  true ,  N'13.6',  true , 25,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (14, NULL,  N'Early Woodland',  true ,  N'5.1',  true , 13,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (15, NULL,  N'Middle Woodland',  true ,  N'5.2',  true , 13,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (16, NULL,  N'Late Woodland',  true ,  N'5.3',  true , 13,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (9, NULL,  N'Early Archaic',  true ,  N'3.1',  true , 8,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (10, NULL,  N'Middle Archaic',  true ,  N'3.2',  true , 8,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (11, NULL,  N'Late Archaic',  true ,  N'3.3',  true , 8,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (5, NULL,  N'Clovis',  true ,  N'2.1',  true , 4,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (6, NULL,  N'Folsom',  true ,  N'2.2',  true , 4,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (7, NULL,  N'Dalton',  true ,  N'2.3',  true , 4,  N'ACTIVE', NULL, NULL);
--------------------------- site_type_keyword ---------------------------
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (251,  N'The locations and/or archaeological remains of a building or buildings used for human habitation. Use more specific term(s) if possible.',  N'Domestic Structure or Architectural Complex',  true ,  N'1',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (252,  N'Locations, or the remains of multiple structures or features, that were inhabited by humans in the past. Use more specific term(s) if possible.',  N'Settlements',  true ,  N'1.1',  true , 251,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (253,  N'A relatively small, short-term human habitation occupied by a relatively small group.',  N'Encampment',  true ,  N'1.1.1',  true , 252,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (254,  N'Relatively small, self-contained groups of dwellings and associated structures providing shelter and a home base for its human inhabitants.  Typically occupied for a number of years or decades, and in some cased for centuries.',  N'Hamlet / village',  true ,  N'1.1.2',  true , 252,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (255,  N'Larger settlements with more dwellings and a wide variety of other kinds of structures.  These settlements typically have internally organized infrastructure of streets or walkways and water and waste-disposal systems. Typically occupied for decades or centuries.',  N'Town / city',  true ,  N'1.1.3',  true , 252,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (256,  N'Locations, or the remains of buildings that were inhabited by humans in the past. Use more specific term(s) if possible.',  N'Domestic Structures',  true ,  N'1.2',  true , 251,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (257,  N'A temporary structure, made out of brush, with a roof and walls, built to provide shelter for occupants or contents (e.g., wikieup, ki).',  N'Brush structure',  true ,  N'1.2.1',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (266,  N'All temporary shelters (e.g. lean-tos, windbreaks, brush enclosures, sun shades etc.).',  N'Shade structure / ramada',  true ,  N'1.2.10',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (267,  N'Circular pattern (sometimes outlined with rocks) left when a tipi or tent is dismantled.',  N'Tent ring / tipi ring',  true ,  N'1.2.11',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (268,  N'A relatively high (over 1 meter), flat-topped mound, frequently constructed in several stages on which one or more structures were placed. Platform mounds are constructed using soil, shell, or refuse. They may incorporate earlier, filled-in structures in their substructure.',  N'Platform mound',  true ,  N'1.2.12',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (269,  N'A low mounded area of shell built to provide a platform for one or more domestic structures.',  N'Shell mound',  true ,  N'1.2.13',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (270,  N'Relatively small dwellings, typically circular or rectangular and about 3 meters tall, made of wooden frames with bases dug into the soil and covered with woven mats or sheets of birchbark. The frames could be shaped like a dome, a cone, or a rectangle with an arched roof.',  N'Wigwam / wetu',  true ,  N'1.2.14',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (271,  N'Relatively large, rectangular dwellings made of long, flat planks of cedar wood lashed to a substantial wooden frame. Typical of permanent villages of Indian tribes living in the American Northwest during the historic contact period and earlier.',  N'Plank house',  true ,  N'1.2.15',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (258,  N'Natural hollow or opening beneath earth''s surface, with an aperture to the surface, showing evidence of human use. Caves may or may not have been modified for or by human use. A cave differs from a rockshelter in depth, penetration, and the constriction of the opening.',  N'Cave',  true ,  N'1.2.2',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (259,  N'A relatively small dwelling occupied by a single nuclear or extended family. May appear archaeologically as a stone foundation or pattern of post molds.',  N'House',  true ,  N'1.2.3',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (260,  N'A slightly raised, mounded area of earth or rock built to provide a platform for a single domestic structure.',  N'House mound',  true ,  N'1.2.4',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (261,  N'The remains of a small surface structure constructed of brush (wattle) and mud (daub).',  N'Wattle & daub (jacal) structure',  true ,  N'1.2.5',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (262,  N'A long, relatively narrow multi-family dwelling, best known as a typical village dwelling used by Iroquois Confederacy tribes.',  N'Long house',  true ,  N'1.2.6',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (263,  N'Semi-subterranean habitation that may have an oval, round or rectangular shape. Typically with a dome-like covering constructed using a wood frame covered by branches, reeds, other vegetation and earth.',  N'Pit house / earth lodge',  true ,  N'1.2.7',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (264,  N'Remains of a contiguous, multi-room habitation structure. Typically constructed of stone, mud brick or adobe. Usually manifests archaeologically as a surface mound of construction debris, sometimes with visible wall alignments.',  N'Room block / compound / pueblo',  true ,  N'1.2.8',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (265,  N'Overhang, indentation, or alcove formed naturally by rock fall or in a rock face; generally not of great depth. Rockshelters may or may not be modified with structural elements for human use.',  N'Rock shelter',  true ,  N'1.2.9',  true , 256,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (272,  N'The locations and/or archaeological remains of features or sites related to resource extraction, commerce, industry, or transportation. Use more specific term(s) if possible.',  N'Resource Extraction/Production/Transportation Structure or Features',  true ,  N'2',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (273,  N'Locations, or the remains of features or facilities, that were used for  horticulture, agriculture, or animal husbandry. Use more specific term(s) if possible.',  N'Agricultural or Herding',  true ,  N'2.1',  true , 272,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (274,  N'An area of land, often enclosed, used for cultivation. Fields are not necessarily formally bounded, and may be identifiable based on diagnostic features such as boundary markers or raised beds.',  N'Agricultural field or field feature',  true ,  N'2.1.1',  true , 273,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (275,  N'Ditch or interrelated group of ditches, acequias, head gates, and drains that constitute an irrigation system for individual watering and irrigation features.',  N'Canal or canal feature',  true ,  N'2.1.2',  true , 273,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (276,  N'An enclosure for confining livestock. May be constructed of any material and incorporate natural features or vegetation as part of the enclosure.',  N'Corral',  true ,  N'2.1.3',  true , 273,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (277,  N'Natural or artificial lake in which water can be stored for future use.',  N'Reservoir',  true ,  N'2.1.4',  true , 273,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (278,  N'An artificially created, more or less level area cut into the side of a hill.?The edge may be bordered by stone or other material to prevent erosion.',  N'Terrace',  true ,  N'2.1.5',  true , 273,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (279,  N'A device which controls the flow of water, particularly run-off. Includes check dams, flumes, gabions, head gates, drop structures, and riprap.',  N'Water control feature',  true ,  N'2.1.6',  true , 273,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (280,  N'Locations, or the remains of features or facilities, that were used for commercial or industrial purposes. Use more specific term(s) if possible.',  N'Commercial or Industrial Structures',  true ,  N'2.2',  true , 272,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (281,  N'A relative large structure in which goods were manufactured or prepared for commercial distribution.',  N'Factory / workshop',  true ,  N'2.2.1',  true , 280,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (282,  N'A relatively small facility (one or a few rooms) for processing grain, wood, or other materials.',  N'Mill',  true ,  N'2.2.2',  true , 280,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (283,  N'A mill for processing grain, typically powered by water or wind.',  N'Grist mill',  true ,  N'2.2.2.1',  true , 282,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (284,  N'A mill for processing timber or wood.',  N'Saw mill',  true ,  N'2.2.2.2',  true , 282,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (285,  N'A structure designed for catching fish. Sometimes constructed as a fence or enclosure of wooden stakes or stones, placed in a river, lake, wetland or tidal estuary.',  N'Fish trap / weir',  true ,  N'2.3',  true , 272,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (286,  N'Locations, or the remains of features or facilities, that were used for hunting or trapping animals. Use more specific term(s) if possible.',  N'Hunting / Trapping',  true ,  N'2.4',  true , 272,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (287,  N'Concentration of faunal remains resulting from human hunting activity.',  N'Butchering / kill site',  true ,  N'2.4.1',  true , 286,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (288,  N'Small, unroofed structure expediently constructed out of natural rock and/or wood as camouflage.',  N'Hunting blind',  true ,  N'2.4.2',  true , 286,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (289,  N'A cliff or other natural drop off where large game can be stampeded over the edge.',  N'Large game jump',  true ,  N'2.4.3',  true , 286,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (290,  N'Locations used for the extraction of metals, ores, minerals, or other materials.',  N'Mine',  true ,  N'2.5',  true , 272,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (291,  N'An excavation made in the earth for the purpose of digging out metallic ores, coal, salt, precious stones or other resources. Includes portals, adits, vent shafts, prospects, and haulage tunnels.',  N'Mine tunnels',  true ,  N'2.5.1',  true , 290,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (292,  N'The remains of facilities or equipment, usually above ground, used for processing or storing mined materials.',  N'Mine-related structures',  true ,  N'2.5.2',  true , 290,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (293,  N'Outcrops of lithic material that have been mined or otherwise utilized to obtain lithic raw materials.',  N'Quarry',  true ,  N'2.6',  true , 272,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (294,  N'The archaeological remains of identifiable paths or routes between two or more locations. Use more specific term(s) if possible.',  N'Road, Trail, and Related Structures or Features',  true ,  N'2.7',  true , 272,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (295,  N'A structure with one or more intervals under it to span a river or other space.',  N'Bridge',  true ,  N'2.7.1',  true , 294,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (296,  N'A road or pathway constructed from packed earth, stone, or shell, usually across a wetland or small water body.',  N'Causeway',  true ,  N'2.7.2',  true , 294,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (297,  N'Identifiable, linear archaeological remains of unknown function.',  N'Linear feature',  true ,  N'2.7.3',  true , 294,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (298,  N'Segment(s) of railroad tracks or railroad bed.',  N'Railroad',  true ,  N'2.7.4',  true , 294,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (299,  N'A prepared, formal way used for the passage of humans, animals, and/or vehicles. These include examples such as Chacoan roads.',  N'Road',  true ,  N'2.7.5',  true , 294,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (300,  N'An informal foot path used for the passage of humans, animals, and/or vehicles, defined and worn by use without formal construction or maintenance.',  N'Trail',  true ,  N'2.7.6',  true , 294,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (301,  N'The archaeological features or locations used for human burial or funerary activities. Use more specific term(s) if possible.',  N'Funerary and Burial Structures or Features',  true ,  N'3',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (302,  N'A formal location for burying the dead.',  N'Cemetery',  true ,  N'3.1',  true , 301,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (303,  N'An artificial mound constructed using earth, shell, or stone for the purpose of holding one or more burials. Frequently containing several episodes of construction and burials from different periods of time.',  N'Burial mound',  true ,  N'3.2',  true , 301,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (304,  N'A structure in which recently deceased human bodies were placed so that the flesh and other soft tissue would decompose prior to final interment of the remains.',  N'Charnel house',  true ,  N'3.3',  true , 301,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (305,  N'A location containing a human burial, spatially removed from other archaeological evidence.',  N'Isolated burial',  true ,  N'3.4',  true , 301,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (306,  N'A secondary burial of multiple individuals.',  N'Ossuary',  true ,  N'3.5',  true , 301,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (307,  N'An unmarked human interment in a subterranean pit.',  N'Burial pit',  true ,  N'3.6',  true , 301,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (308,  N'A prepared, architecturally distinctive structure, normally sub-surface, often containing multiple interments.  Use for features such as shaft tombs.',  N'Tomb',  true ,  N'3.7',  true , 301,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (309,  N'The locations and/or archaeological remains of a building or buildings used for purposes other than human habitation. Use more specific term(s) if possible.',  N'Non-Domestic Structures',  true ,  N'4',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (310,  N'An unroofed structure associated with the playing of the Mesoamerican ball game, found in the American southwest and parts of Mesoamerica.',  N'Ball Court',  true ,  N'4.1',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (330,  N'An enclosure, constructed of timbers or posts driven into ground, or otherwise walled.',  N'Palisade',  true ,  N'4.10',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (331,  N'An area which may be partially or completely enclosed by structural remains (standing or collapsed), used for community activities.  May contain temporary structures (e.g. sun shades or ramadas) as well as special activity areas (e.g. milling bins, hearths).',  N'Plaza',  true ,  N'4.11',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (332,  N'A massive structure, typically with triangular outer surfaces that converge at the top. Often flat-topped to accommodate public gatherings and/or buildings.',  N'Pyramid',  true ,  N'4.12',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (333,  N'A series of steps allowing access to a different level. Use for toe/hand holds, stairs, ladders, etc.',  N'Stairway',  true ,  N'4.13',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (334,  N'Architectural remnant of a building of unknown form or function.',  N'Structure',  true ,  N'4.14',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (335,  N'Architectural remnant of a prehistoric building of unknown form or function. Use more specific term if possible.',  N'Ancient structure',  true ,  N'4.14.1',  true , 310,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (336,  N'Architectural remnant of a historic building of unknown form or function. Use more specific term if possible.',  N'Historic structure',  true ,  N'4.14.2',  true , 310,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (337,  N'Small enclosure or hut used for steam baths, usually ephemeral in construction. Often with fire-cracked rock and/or hearths in association.',  N'Sweat house / sweat lodge',  true ,  N'4.15',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (338,  N'Monumental architecture constructed from stone or other materials, and used for religious and/or political purposes.',  N'Temple',  true ,  N'4.16',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (311,  N'Buildings, or the archaeological remains of features or facilities, that were used for religious purposes. Use more specific term(s) if possible.',  N'Church / religious structure',  true ,  N'4.2',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (312,  N'Remains of a prehistoric building or location designed for public religious services.',  N'Ancient church / religious structure',  true ,  N'4.2.1',  true , 311,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (313,  N'Remains of a historic building or location designed for public religious services.',  N'Historic church / religious structure',  true ,  N'4.2.2',  true , 311,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (314,  N'Locations, or the remains of buildings that were associated with communal or public activities.',  N'Communal / public structure',  true ,  N'4.3',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (315,  N'Specified area containing evidence that is associated with prehistoric communal or public activity.',  N'Ancient communal / public structure',  true ,  N'4.3.1',  true , 314,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (316,  N'Specified area containing evidence that is associated with historic communal or public activity.',  N'Historic communal / public structure',  true ,  N'4.3.2',  true , 314,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (317,  N'A multi-story building with massive masonry or adobe walls, found in the Chacoan and Hohokam regions of the American southwest.',  N'Great House / Big House',  true ,  N'4.4',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (318,  N'Locations, or the remains of buildings that were associated with governmental activities.',  N'Governmental structure',  true ,  N'4.5',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (319,  N'Specified area containing evidence that is associated with prehistoric governmental activity.',  N'Ancient governmental structure',  true ,  N'4.5.1',  true , 318,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (320,  N'Specified area containing evidence that is associated with historic governmental activity.',  N'Historic governmental structure',  true ,  N'4.5.2',  true , 318,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (321,  N'Circular or rectangular ceremonial structure. May be subterranean or part of a surface room block.',  N'Kiva / Great Kiva',  true ,  N'4.6',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (322,  N'Military-related structure constructed for various purposes (personnel barracks, testing, aircraft storage or landing, etc.).',  N'Military structure',  true ,  N'4.7',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (323,  N'An above ground construction of earth, shell or other material, undifferentiated as to function.',  N'Mound / Earthwork',  true ,  N'4.8',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (324,  N'An above ground prepared surface on which a non-residential structure is built.',  N'Building substructure',  true ,  N'4.8.1',  true , 323,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (325,  N'A non-residential cultural construction made from earth, shell or other materials, often formed to enclose or demarcate an area, or, in the case of causeways, to link areas. Examples include shell rings.',  N'Ancient earthwork',  true ,  N'4.8.2',  true , 323,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (326,  N'A defensive construction made from earth, shell or other materials.',  N'Military earthwork',  true ,  N'4.8.3',  true , 323,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (327,  N'An above ground construction of earth, shell or other material, built in the shape of geometric, animal or other symbolic forms. Prominent examples include Effigy Mounds National Monument and Serpent Mound.',  N'Geometric / effigy / zoomorphic mound',  true ,  N'4.8.4',  true , 323,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (328,  N'A large, circular area defined by a perimeter of mounded shell (often several meters in height), for example, Archaic Period shell ring sites in the American southeast.',  N'Shell ring',  true ,  N'4.8.5',  true , 323,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (329,  N'A large and/or ornate building, normally associated with a high ranking family or individual.',  N'Palace',  true ,  N'4.9',  true , 309,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (339,  N'A localized area containing evidence of human activity. Use more specific term(s) if possible.',  N'Archaeological Feature',  true ,  N'5',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (340,  N'Prehistoric lithic and/or ceramic scatters with no features.',  N'Artifact Scatter',  true ,  N'5.1',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (355,  N'A discrete excavation directly attributable to human activity. Use more specific term(s) if possible.',  N'Pit',  true ,  N'5.10',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (356,  N'A discrete excavation directly attributable to human activity that was used for the disposal of discarded artifacts, ecofacts and other cultural materials.',  N'Refuse pit',  true ,  N'5.10.1',  true , 340,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (357,  N'An enclosed space used to heat objects placed within its bounds. Includes earth ovens, oven pits, mud ovens, and bread ovens.',  N'Roasting pit / oven / horno',  true ,  N'5.10.2',  true , 340,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (358,  N'A discrete excavation directly attributable to human activity used for storing artifacts, ecofacts and other cultural materials.',  N'Storage pit',  true ,  N'5.10.3',  true , 340,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (359,  N'One or more upright posts, remains of posts, or sockets usually associated with a larger feature or structure such as a building, fence, corral, stockade, pen, etc.',  N'Post hole / post mold',  true ,  N'5.11',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (360,  N'Group of rocks which appear to have some cultural association. Use for possible walls, wall-like phenomena, human produced architectural oddities, rock piles, etc.',  N'Rock alignment',  true ,  N'5.12',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (341,  N'Mound or stack of rocks used to mark significant locations (e.g., boundaries or claims).',  N'Cairn',  true ,  N'5.2',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (342,  N'A structure that creates a boundary, barrier, or enclosure. Construction materials can vary widely and may include unmodified natural materials (such as brush).',  N'Fence',  true ,  N'5.3',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (343,  N'Discolored area of soil, often including charcoal, ash deposits or fire cracked rock, exhibiting evidence of use in association with fire.  May be bounded (e.g., rock ring) or ill-defined.',  N'Hearth',  true ,  N'5.4',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (344,  N'A find spot containing a single artifact.',  N'Isolated artifact',  true ,  N'5.5',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (345,  N'A find spot containing a single cultural feature.',  N'Isolated feature',  true ,  N'5.6',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (346,  N'Oven used to bake food, fire pottery, or thermally alter other materials (e.g., bricks, lithic materials).',  N'Kiln',  true ,  N'5.7',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (347,  N'An archaeological refuse deposit containing the broken or discarded remains of human activities. Use more specific term(s) if possible.',  N'Midden',  true ,  N'5.8',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (348,  N'A large, dense concentrations, often mounded, of fire cracked rock (FCR), usually associated with large scale plant processing. Although other cultural materials may be present in the midden, FCR is usually predominant.',  N'Burned rock midden',  true ,  N'5.8.1',  true , 347,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (349,  N'A surficial archaeological deposit containing discarded artifacts and other cultural materials. Midden deposits normally contain ashy or charcoal-stained sediments, and domestic-related items such as sherds, lithic debitage, and bone.',  N'Sheet midden',  true ,  N'5.8.2',  true , 347,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (350,  N'An archaeological deposit composed primarily of discarded mollusk shells.',  N'Shell midden',  true ,  N'5.8.3',  true , 347,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (351,  N'A substantial concentration of refuse, built up as a result of multiple episodes of deposition.',  N'Trash midden',  true ,  N'5.8.4',  true , 347,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (352,  N'A facility made or used for grinding or processing plant materials. Use more specific term(s) if possible.',  N'Milling feature',  true ,  N'5.9',  true , 339,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (353,  N'A pecked or ground concavity in a large boulder or outcrop, including both bedrock mortar and bedrock metate.',  N'Bedrock grinding feature',  true ,  N'5.9.1',  true , 352,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (354,  N'An enclosed container used for milling plant material. May be above ground or partially or completely underground.',  N'Milling Bin',  true ,  N'5.9.2',  true , 352,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (361,  N'Designs, whether carved, scraped, pecked or painted, applied to free-standing stones, cave walls, or the earth?s surface. Use more specific term(s) if possible.',  N'Rock Art',  true ,  N'6',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (362,  N'Designs created on the ground surface by arranging rocks or other materials, or by scraping or altering the earth surface. Usually on a large scale.',  N'Intaglio / geoglyph',  true ,  N'6.1',  true , 361,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (363,  N'Design scratched, pecked, or scraped into a rock surface.',  N'Petroglyph',  true ,  N'6.2',  true , 361,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (364,  N'Design drawn in pigment upon an unprepared or ground rock surface.',  N'Pictograph',  true ,  N'6.3',  true , 361,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (365,  N'The locations and/or archaeological remains of ships, boats, or other vessels, or the facilities related to shipping or sailing.',  N'Water-related',  true ,  N'7',  true , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (366,  N'The remains of facilities or equipment related to boats, ships, or shipping. Use for dock, wharf etc.',  N'Shipping-related structure',  true ,  N'7.1',  true , 365,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (367,  N'The remains of a ship, boat or other vessel.',  N'Shipwreck',  true ,  N'7.2',  true , 365,  N'ACTIVE', NULL, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (368,  N'The underwater remains of an aircraft.',  N'Submerged aircraft',  true ,  N'7.3',  true , 365,  N'ACTIVE', NULL, NULL);
--------------------------- investigation_type ---------------------------
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (1,  N'A study that involves the synthesis of the archaeological history of investigation and culture history of a location or area.',  N'Archaeological Overview',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (3,  N'A study that involves research and analysis of archaeological resources held in a museum, historical society or other repository.',  N'Collections Research',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (4,  N'An activity that involves discussions and meetings with communities, stakeholders, and other interested parties for the purpose of describing proposed archaeological activities and eliciting their comments, perspectives and feedback.',  N'Consultation',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (7,  N'An activity employed to gather a general impression of the nature and distribution of archaeological or cultural resources in an area. Relatively little field work is conducted in relation to the size of the research area.',  N'Reconnaissance / Survey',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (9,  N'Activities undertaken to plan and schedule future archaeological research.',  N'Research Design / Data Recovery Plan',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (11,  N'An activity that involves the rehabilitation of eroding, slumping, subsiding or otherwise deteriorating archaeological resources, including structures and building materials.',  N'Site Stabilization',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (8,  N'An activity involving the review of records, files, and other information about the sites recorded in a particular area.  Typically, such studies involve checking the site files and other archives in an agency''s database, or SHPO''s office, or State Archaeologist''s office.',  N'Records Search / Inventory Checking',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (6,  N'A non-field study of archaeological theory, method or technique. These investigations may also include broadly synthetic regional studies.',  N'Methodology, Theory, or Synthesis',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (13,  N'These are studies that examine aspects of the present or past natural environment to provide a context, often off-site,  for interpreting archaeological resources.  Sometimes reported in stand-alone volumes representing significant research, such investigations may include geomorphological, paleontological, or palynological work.',  N'Environment Research',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (14,  N'This is research that focuses on the systematic description and analysis of cultural systems or lifeways.  These studies of contemporary people and cultures rely heavily on participant observation as well as interviews, oral histories, and review of relevant documents.  Ethnoarchaeological studies are a subset of this kind of research that investigate correlations between traditional contemporary cultures and patterns in the archaeological record.    ',  N'Ethnographic Research',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (15,  N'These are studies of biological aspects of human individuals, groups, or cultural systems, including a broad range of topics describing and analyzing human physiology, osteology, diet, disease, and origins research. ',  N'Bioarchaeological Research',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (16,  N'Descriptive and analytical research that documents, describes, and/or interprets detailed data on historic structures in an area, including images and floor plans. ',  N'Architectural Documentation',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (17,  N'This kind of research includes systematic description and analysis of changes in cultural systems through time, using historical documents and oral and traditional histories.  Ethnohistoric studies typically deal with time periods of initial or early contact between different cultural systems, for example, European explorers or early colonists and indigenous cultures. ',  N'Ethnohistoric Research',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (18,  N'These are investigations of the past using written records and other documents.  Evidence from the records are compared, judged for veracity, placed in chronological or topical sequence, and interpreted in light of preceding, contemporary, and subsequent events.',  N'Historic Background Research',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (19,  N'Visits to a site to record archaeological resources and their conditions and recover finds that may have come to light since a previous visit.  Also refers to regular or systematic monitoring and recording of the condition of a site, checking for signs of vandalism, other human intervention, and natural processes that may have damaged the resource.',  N'Site Stewardship Monitoring',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (20,  N'Observations and investigations conducted during any ground disturbing operation carried out for non-archaeological reasons (e.g. construction, land-leveling, wild land fire-fighting, etc.) that may reveal and/or damage archaeological deposits.',  N'Ground Disturbance Monitoring',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (12,  N'This is an investigation employed to systematically gather data on the general presence or absence of archaeological resources, to define resource types, or to estimate the distribution of resources in an area. These studies may provide a general understanding of the resources in an area. This includes the description, analysis, and specialized studies of artifacts and samples recovered during survey. ',  N'Systematic Survey',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (10,  N'These investigations include fieldwork undertaken to identify the archaeological resources in a given area and to collect information sufficient to evaluate the resource(s) and develop treatment recommendations. Such investigations typically determine the number, location, distribution and condition of archaeological resources. This includes the description, analysis, and specialized studies of artifacts and samples recovered during site testing. ',  N'Site Evaluation / Testing',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (5,  N'These investigations include substantial field investigation of an archaeological site (or sites) involving the removal, and systematic recording of, archaeological matrix. These activities often mitigate the adverse effects of a public undertaking. This includes the description, analysis, and specialized studies of artifacts and samples recovered during excavations.',  N'Data Recovery / Excavation',  N'ACTIVE', NULL, NULL);
INSERT INTO investigation_type (id, definition, label, status, merge_keyword_id, occurrence) VALUES (2,  N'These investigations include field and/or document and records reviews to gather data on the presence and type of historic structures and provide a general understanding of architectural and cultural resources in an area. ',  N'Architectural Survey',  N'ACTIVE', NULL, NULL);
--------------------------- material_keyword ---------------------------
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (1,  N'Historic or prehistoric artifacts made from pottery or fired clay',  N'Ceramic',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (2,  N'Lithic artifacts such as tools, flakes, shatter, or debitage',  N'Chipped Stone',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (3,  N'Material collected for use with dating techniques such as radiocarbon, dendrochronology or archaeomagnetism',  N'Dating Sample',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (4,  N'Animal bone remains',  N'Fauna',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (5,  N'Rocks showing evidence of intense heating or burning, carbon staining, or other indications of changes due to heat.',  N'Fire Cracked Rock',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (6,  N'Historic or prehistoric artifacts made from glass',  N'Glass',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (7,  N'Lithic artifact formed or finished by polishing the body or edges with an abrasive',  N'Ground Stone',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (8,  N'Materials used for construction (e.g., brick, wood, adobe)',  N'Building Materials',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (9,  N'The remains of any part of a human',  N'Human Remains',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (10,  N'Plant remains such as fruit, seeds, buds, or other plant parts.',  N'Macrobotanical',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (11,  N'Any prehistoric or historic artifact made of metal (e.g., iron, copper, gold, silver, etc.)',  N'Metal',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (12,  N'Natural inorganic substance possessing a definite chemical composition in a crystalline form.',  N'Mineral',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (13,  N'Use for any microscopic plant remains',  N'Pollen',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (14,  N'Modified or unmodified objects made from mollusc shell.',  N'Shell',  N'ACTIVE', NULL, NULL);
INSERT INTO material_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (15,  N'Modified or unmodified objects made from the roots, trunk, or branches of trees or shrubs.',  N'Wood',  N'ACTIVE', NULL, NULL);
--------------------------- creator ---------------------------
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (1, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (6, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (38, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (60, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (100, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (121, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (5349, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (5979, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8007, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8008, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8009, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8014, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8018, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8019, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8044, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8067, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8161, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8344,  '2010-06-08 00:00:00',  '2010-06-08 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8389,  '2010-07-19 00:00:00',  '2010-07-19 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8394,  '2010-07-22 00:00:00',  '2010-07-22 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8422,  '2010-07-29 00:00:00',  '2010-07-29 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8424,  '2010-07-29 00:00:00',  '2010-07-29 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8425,  '2010-07-29 00:00:00',  '2010-07-29 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8426,  '2010-07-29 00:00:00',  '2010-07-29 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8489,  '2010-08-05 00:00:00',  '2010-08-05 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8608,  '2010-08-31 00:00:00',  '2010-08-31 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (8621,  '2010-09-01 00:00:00',  '2010-09-01 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11001, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11003, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11006, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11008, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11009, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11010, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11017, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11018, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11019, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11037, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11044, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11045, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11055, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11110, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11140, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11149, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11150, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11153, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11154, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11156, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11157, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11158, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11159, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11165, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11169, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11262, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11269, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11270, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11285, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11304, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11309, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11313, NULL,  '2012-04-27 14:13:06.306093', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11785,  '2011-02-10 00:00:00',  '2011-02-10 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (11914,  '2011-03-11 00:00:00',  '2011-03-11 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12082,  '2011-04-28 00:00:00',  '2011-04-28 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12129,  '2011-05-07 00:00:00',  '2011-05-07 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12214,  '2011-05-12 00:00:00',  '2011-05-12 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12395,  '2011-06-25 00:00:00',  '2011-06-25 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12396,  '2011-06-25 00:00:00',  '2011-06-25 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12397,  '2011-06-25 00:00:00',  '2011-06-25 00:00:00', NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12474,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12475,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12476,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12477,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12478,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12479,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12480,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12481,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12482,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12483,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12484,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12485,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12486,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12487,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12488,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12489,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12490,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12491,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12492,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12493,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12494,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12495,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12496,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12497,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12498,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12499,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12500,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12501,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12502,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12503,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12504,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12505,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12506,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12507,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12508,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12509,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12510,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12511,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12512,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12513,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12514,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12515,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12516,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12517,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12518,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12519,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12520,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12521,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12522,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12523,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12524,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12525,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12526,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12527,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12528,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12529,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12530,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12531,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12532,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12533,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12534,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12535,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12536,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12537,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12538,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12539,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12540,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12541,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12542,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
INSERT INTO creator (id, date_created, last_updated, url, description, status, occurrence) VALUES (12543,  '2012-08-03 00:00:00', NULL, NULL, NULL,  N'ACTIVE', 0);
--------------------------- institution ---------------------------
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11001,  N'Arizona State Parks', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11003,  N'Arizona State University', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11006,  N'ASU', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11008,  N'National Science Foundation', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11009,  N'Pueblo of Zuni', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11010,  N'Soil Systems, Inc.', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11017,  N'asu', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11018,  N'ASU SHESC', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11019,  N'ASU - SHESC', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11037,  N'Washington University', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11044,  N'ADS', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11045,  N'National Park Service', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11055,  N'Digital Antiquity', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11110,  N'Illinois State Museum', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11140,  N'Museum of Northern Arizona', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11149,  N'Mabel Hinkson', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11150,  N'TFQA', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11153,  N'City of Alexandria', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11154,  N'City of Alexandria, Virginia', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11156,  N'U. Illinois', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11157,  N'University of Illinois', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11158,  N'University of Maryland', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11159,  N'University of North Carolina', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11165,  N'Western Archeological Center', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11169,  N'Western Archeological and Conservation Center ', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11262,  N'Crow Canyon Archaeological Center', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11269,  N'Howard University', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11270,  N'Boston University', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11285,  N'n/a', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11304,  N'University of Redlands', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11309,  N'Mesa Verde Museum Association', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11313,  N'Statistical Research, Inc.', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11785,  N'Denver Service Center, National Park Service', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (11914,  N'Simon Fraser University', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12082,  N'New York State Archaeological Association ', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12129,  N'Association for the Preservation of Virginia Antiquities', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12214,  N'Great Lakes Archaeological Research Center', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12395,  N'Virginia Commonwealth University Archaeological Research Center', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12396,  N'Virginia Department of Historic Resources', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12397,  N'Robert E. Lee Memorial Association, Inc. ', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12474,  N'The Gentleman''s Magazine and Historical Review', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12475,  N'Robert E. Lee Memorial Association', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12476,  N'the American Ethnological Society', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12477,  N'Ph.D. dissertation, Department of Archaeology, Simon Fraser University, Burnaby', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12478,  N'First International Congress for Palynology, Tucson Arizona', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12479,  N'Office of Cultural Resource Management, Department of Anthropology, Arizona State University', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12480,  N'American Anthopological Association', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12481,  N'Westerm Archeological and Conservation Center', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12482,  N'Syracuse Univ. Press', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12483,  N'IX International Pollen Conference, Houston Texas', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12484,  N'Masters Thesis', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12485,  N'Quaternary Science', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12486,  N'Memoirs of the Connecticut Academy of Arts and Sciences', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12487,  N'Waveland Press', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12488,  N'Marshall H. Saville Collection, Archives of the Museum of the American Indian, Heye Foundation.', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12489,  N'New York Time', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12490,  N'Ethos', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12491,  N'76th Annual Meeting of the Society for American Archaeology', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12492,  N'American Society for Conservation Archaeology', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12493,  N'G.P. Putnam''s Sons', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12494,  N'Transactions of the Ethnological Society of London', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12495,  N'Goteborg', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12496,  N'"Why Does the Past Matter?"', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12497,  N'Center for the Study of the First Americans', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12498,  N'American Ethnological Society: Public Domain', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12499,  N'National Park Service, Region Five', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12500,  N'Society for American Archaeology  annual meeting', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12501,  N'Baywood Publishing Co., Inc.', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12502,  N'Heye Foundation', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12503,  N'Soil Systems, Incorporated', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12504,  N'New York Archaeological Association', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12505,  N'Harpers New Monthly Magazine', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12506,  N'Cambridge University Press', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12507,  N'Western Acheological and Conservation Center', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12508,  N'Royal Anthropological Institute of Great Britian and Ireland', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12509,  N'U.S.D.I. Bureau of Reclamation, Engineering and Research Center', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12510,  N'Journal of Archaeological Science', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12511,  N'Division of Cultural Resources, North Atlantic Regional Office, National Park Service', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12512,  N'The Colonial Williamsburg Foundation', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12513,  N'Historic Preservation Office, Commonwealth of the Northern Marianas', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12514,  N'The New York Times', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12515,  N'Harpers', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12516,  N'University Microfilms', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12517,  N'Unknown', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12518,  N'Society for American Archaeology', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12519,  N'Scientific American', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12520,  N'Pueblo Grande Museum, City of Phoenix Parks, Recreation and Library Department', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12521,  N'American Anthropological Association', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12522,  N'University of Texas Press', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12523,  N'Department of Archaeological Research, Colonial Williamsburg Foundation', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12524,  N'Statistical Research, Inc.Press', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12525,  N'Ph.D. dissertation, Arizona State University, Tempe.  University Microfilms, Ann Arbor', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12526,  N'Unpublished Masters paper', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12527,  N'The State Education Department, Albany 12230', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12528,  N'Smithsoniam Institute: Government Printing Office', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12529,  N'The Numismatic and Antiquarian Society', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12530,  N'North Atlantic Regional Office, National Park Service', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12531,  N'Statistical Research, Inc. Press', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12532,  N'Cultural Resource Management Division, Arizona State Museum, University of Arizona', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12533,  N'Olmsted Center for Landscape Preservation, National Park Service', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12534,  N'Transaction', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12535,  N'American Ethnological Society annual meeting', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12536,  N'The Arizona Archeological Center', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12537,  N'Peabody Museum Press', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12538,  N'Cahokia Ceramic Conference', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12539,  N'ESRI International User Conference', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12540,  N'Department of Anthropology, SUNY Albany', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12541,  N'Illinois State Museum Society', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12542,  N'Society for American Archaeology Annual Meeting', NULL, NULL);
INSERT INTO institution (id, name, parentinstitution_id, merge_creator_id) VALUES (12543,  N'Division of Cultural Resources, North Atlantic Region, National Park Service, US DOI', NULL, NULL);
--------------------------- person ---------------------------
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (1,  N'allen.lee@asu.edu',  N'Allen',  N'Lee', NULL, NULL, 11006,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (6,  N'kintigh@asu.edu',  N'Keith',  N'Kintigh', NULL, NULL, 11003,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (38,  N'mallorie.hatch@asu.edu',  N'Mallorie',  N'Hatch', NULL, NULL, 11006,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (60,  N'matthew.peeples@asu.edu',  N'Matthew',  N'Peeples', NULL, NULL, 11003,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (100,  N'tiffany.clark@asu.edu',  N'Tiffany',  N'Clark', NULL, NULL, NULL,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (121,  N'michelle.elliott@asu.edu',  N'Michelle',  N'Elliott', NULL, NULL, 11003,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (5349,  N'joshua.watts@asu.edu',  N'Joshua',  N'Watts', NULL, NULL, 11019,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (5979,  N'karen.schollmeyer@asu.edu',  N'Karen',  N'Schollmeyer', NULL, NULL, 11018,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8007,  N'pjwatson@artsci.wustl.edu',  N'Patty Jo',  N'Watson', NULL, NULL, NULL,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8008,  N'charles.redman@asu.edu',  N'Charles',  N'Redman', NULL, NULL, NULL,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8009, NULL,  N'Steven',  N'LeBlanc', NULL, NULL, NULL,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8014,  N'jdr1@york.ac.uk',  N'Julian',  N'Richards', NULL, NULL, 11044,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8018,  N'smanney@asu.edu',  N'Shelby',  N'Manney', NULL,  N'915-373-0847', 11006,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8019,  N'shelby.manney@asu.edu',  N'Shelby',  N'Manney', NULL,  N'915-373-0847', 11006,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8044,  N'fpmcmanamon@asu.edu',  N'Francis',  N'McManamon', NULL,  N'480-965-6510', 11055,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8067,  N'candan@asu.edu',  N'K. Selcuk',  N'Candan', NULL, NULL, 11017,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8161,  N'kate.spielmann@asu.edu',  N'Katherine',  N'Spielmann', NULL,  N'480-965-7212', 11003,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8344,  N'adam.brin@asu.edu',  N'adam',  N'brin', NULL, NULL, 11055,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8389,  N'kintigh@tfqa.com',  N'Keith',  N'Kintigh', NULL,  N'480-965-6909', 11150,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8394, NULL,  N'Pamela',  N'Cressey', NULL, NULL, NULL,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8422,  N'cfennell@illinois.edu',  N'Christopher',  N'Fennell', NULL,  N'217-333-3616', 11156,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8424,  N'pshackel@anth.umd.edu',  N'Paul',  N'Shackel', NULL, NULL, NULL,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8425,  N'martin@museum.state.il.us',  N'Terrance',  N'Martin', NULL, NULL, NULL,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8426,  N'agbe-davies@unc.edu',  N'Anna',  N'Agbe-Davies', NULL, NULL, NULL,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8489, NULL,  N'Susan J.',  N'Wells', NULL, NULL, NULL,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8608,  N'james.t.devos@asu.edu',  N'jim',  N'DeVos', NULL,  N'480-965-6533', 11003,  false ,  false , NULL, NULL);
INSERT INTO person (id, email, first_name, last_name, rpa_number, phone, institution_id, phone_public, email_public, merge_creator_id, orcid_id) VALUES (8621, NULL,  N'Keith M.',  N'Anderson', NULL, NULL, NULL,  false ,  false , NULL, NULL);
--------------------------- tdar_user ---------------------------
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL, NULL, NULL, NULL, 0, 0,  N'allen.lee@asu.edu', 1, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL, NULL, NULL, NULL, 0, 0,  N'kintigh@asu.edu', 6, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL, NULL, NULL, NULL, 0, 0,  N'mallorie.hatch@asu.edu', 38, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL, NULL, NULL, NULL, 0, 0,  N'matthew.peeples@asu.edu', 60, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL, NULL, NULL, NULL, 0, 0,  N'michelle.elliott@asu.edu', 121, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL, NULL, NULL, NULL, 0, 0,  N'joshua.watts@asu.edu', 5349, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL, NULL, NULL, NULL, 0, 0,  N'karen.schollmeyer@asu.edu', 5979, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL, NULL, NULL, NULL, 0, 0,  N'jdr1@york.ac.uk', 8014, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL, NULL, NULL, NULL, 0, 0,  N'smanney@asu.edu', 8018, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL, NULL, NULL, NULL, 0, 0,  N'shelby.manney@asu.edu', 8019, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0,  N'Northeastern North America
Historic period
Prehistoric period
Assemblage analysis
Lithic analysis
CRM', NULL, NULL, NULL, 0, 0,  N'fpmcmanamon@asu.edu', 8044, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL, NULL, NULL, NULL, 0, 0,  N'candan@asu.edu', 8067, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0,  N'North American southwest and eastern united states; prehistoric to early colonial', NULL, NULL, NULL, 0, 0,  N'kate.spielmann@asu.edu', 8161, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0, NULL,  '2014-04-15 19:27:02.662',  '2014-04-15 19:23:30.983', NULL, 0, 7,  N'adam.brin@asu.edu', 8344, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0,  N'SW US
AD 600-1540', NULL, NULL, NULL, 0, 0,  N'kintigh@tfqa.com', 8389, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0,  N'New Philadelphia Archaeology Project, 19th century, Illinois, historical archaeology', NULL, NULL, NULL, 0, 0,  N'cfennell@illinois.edu', 8422, NULL);
INSERT INTO tdar_user (affiliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, username, id, proxyinstitution_id) VALUES (NULL,  true , 0,  N'n/a', NULL, NULL, NULL, 0, 0,  N'james.t.devos@asu.edu', 8608, NULL);
--------------------------- site_type_keyword ---------------------------
--------------------------- culture_keyword ---------------------------
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (39, NULL,  N'Cibola',  false , NULL,  false , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (40, NULL,  N'Zuni',  false , NULL,  false , NULL,  N'ACTIVE', NULL, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id, status, merge_keyword_id, occurrence) VALUES (52, NULL,  N'Sinagua',  false , NULL,  false , NULL,  N'ACTIVE', NULL, NULL);
--------------------------- temporal_keyword ---------------------------
INSERT INTO temporal_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (54, NULL,  N'Pueblo I',  N'ACTIVE', NULL, 0);
INSERT INTO temporal_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (55, NULL,  N'Pueblo II',  N'ACTIVE', NULL, 0);
INSERT INTO temporal_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (56, NULL,  N'Pueblo III',  N'ACTIVE', NULL, 0);
INSERT INTO temporal_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (57, NULL,  N'Pueblo IV',  N'ACTIVE', NULL, 0);
INSERT INTO temporal_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (78, NULL,  N'Basketmaker III',  N'ACTIVE', NULL, 0);
INSERT INTO temporal_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (81, NULL,  N'Eighteenth and nineteenth century',  N'ACTIVE', NULL, 0);
INSERT INTO temporal_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (82, NULL,  N'archaic period, 19th and 20th centuries',  N'ACTIVE', NULL, 0);
--------------------------- geographic_keyword ---------------------------
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2, NULL,  N'Arizona', NULL,  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (8, NULL,  N'Cibola', NULL,  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (11, NULL,  N'El Morro Valley', NULL,  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (46, NULL,  N'Zuni Indian Reservation', NULL,  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (53, NULL,  N'Zuni', NULL,  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (57, NULL,  N'Alexandria, Virginia', NULL,  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (58, NULL,  N'New Philadelphia, Pike County, Illinois', NULL,  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (98, NULL,  N'Verde Valley', NULL,  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (247, NULL,  N'Cibola County (County)',  N'COUNTY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (255, NULL,  N'McKinley County (County)',  N'COUNTY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (276, NULL,  N'US (ISO Country Code)',  N'ISO_COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (299, NULL,  N'New Mexico (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (327, NULL,  N'Yavapai County (County)',  N'COUNTY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (335, NULL,  N'Arizona (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (384, NULL,  N'United States of America (Country)',  N'COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (408, NULL,  N'Apache County (County)',  N'COUNTY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (927, NULL,  N'Illinois (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (1474, NULL,  N'Virginia (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (1539, NULL,  N'Pike County (County)',  N'COUNTY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (1683, NULL,  N'District of Columbia (County)',  N'COUNTY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (1906, NULL,  N'Prince George''s County (County)',  N'COUNTY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2040, NULL,  N'Fairfax County (County)',  N'COUNTY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2092, NULL,  N'Alexandria city (County)',  N'COUNTY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2545, NULL,  N'French Republic (Country)',  N'COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2546, NULL,  N'Basse-Normandie (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2547, NULL,  N'Picardie (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2548, NULL,  N'GB (ISO Country Code)',  N'ISO_COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2549, NULL,  N'Isle of Man (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2550, NULL,  N'England (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2551, NULL,  N'Nord-Pas-de-Calais (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2552, NULL,  N'FR (ISO Country Code)',  N'ISO_COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2553, NULL,  N'Wales (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2554, NULL,  N'United Kingdom of Great Britain and Nort (Country)',  N'COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2555, NULL,  N'Scotland (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2556, NULL,  N'Haute-Normandie (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2557, NULL,  N'Northern Ireland (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2558, NULL,  N'Ireland (Country)',  N'COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2559, NULL,  N'Ulster (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2560, NULL,  N'IE (ISO Country Code)',  N'ISO_COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2561, NULL,  N'Leinster (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2562, NULL,  N'Munster (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2563, NULL,  N'Isle of Man (Country)',  N'COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2564, NULL,  N'Connacht (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2565, NULL,  N'Hainaut (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2566, NULL,  N'Guernsey (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2567, NULL,  N'Kingdom of Belgium (Country)',  N'COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2568, NULL,  N'Bailiwick of Guernsey (Country)',  N'COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2569, NULL,  N'BE (ISO Country Code)',  N'ISO_COUNTRY',  N'ACTIVE', NULL, 0);
INSERT INTO geographic_keyword (id, definition, label, level, status, merge_keyword_id, occurrence) VALUES (2570, NULL,  N'West-Vlaanderen (State / Territory)',  N'STATE',  N'ACTIVE', NULL, 0);
--------------------------- other_keyword ---------------------------
INSERT INTO other_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (7, NULL,  N'CARP',  N'ACTIVE', NULL, 0);
INSERT INTO other_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (49, NULL,  N'HARP',  N'ACTIVE', NULL, 0);
INSERT INTO other_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (50, NULL,  N'Cushing',  N'ACTIVE', NULL, 0);
INSERT INTO other_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (51, NULL,  N'OBAP',  N'ACTIVE', NULL, 0);
--------------------------- site_name_keyword ---------------------------
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (4, NULL,  N'Atsinna',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (12, NULL,  N'Cienega Site',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (13, NULL,  N'Durrington Walls',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (24, NULL,  N'Heshotauthla',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (27, NULL,  N'Hinkson Site',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (29, NULL,  N'H-Spear',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (34, NULL,  N'Jaralosa Pueblo',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (38, NULL,  N'Knowth',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (50, NULL,  N'Mirabal Ruin',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (58, NULL,  N'North Atsinna',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (64, NULL,  N'Ojo Bonito',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (71, NULL,  N'Pueblo de los Muertos',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (76, NULL,  N'Rudd Creek',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (79, NULL,  N'Scribe S Site',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (91, NULL,  N'Spier 81',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (95, NULL,  N'Tinaja',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (113, NULL,  N'New Philadelphia, Illinois',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (200, NULL,  N'Montezuma Castle',  N'ACTIVE', NULL, 0);
INSERT INTO site_name_keyword (id, definition, label, status, merge_keyword_id, occurrence) VALUES (201, NULL,  N'Montezuma Well',  N'ACTIVE', NULL, 0);
--------------------------- resource ---------------------------
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3462,  '2010-07-09 12:57:10.251',  N'DELETED; Ooutdated ---Working ontologies used by the Archaeology Data Service/tDAR Transatlantic Archaeology Gateway initiative funded by NEH and JISC. These ontologies have to do with the project''s Work Package II that is directed to detailed cross search of faunal databases.',  N'PROJECT',  N'Fauna Ontologies - Transatlantic Archaeology Gateway - OUTDATED', 6, NULL, 8019,  '2011-03-05 17:35:21.101',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (1656,  '2010-04-07 00:52:30.62',  N'this should not be null',  N'CODING_SHEET',  N'HARP Fauna Species Coding Sheet', 6, NULL, 6,  '2010-04-07 00:52:30.62',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (262,  '2009-05-26 21:38:53.091',  N'A survey and excavation project directed by Keith Kintigh and executed from 1983 through 1994.  Approximate 58km2 were surveyed and 560 sites were recorded.  Substantial excavations were undertaken at the Hinkson Site great house complex and Jaralosa Pueblo.  Test excavations were completed at H-Spear, a Chacoan Great House located by the project and Ojo Bonito Pueblo.  The project took place on the ranch of Mrs. Everett (Mabel) Hinkson (deceased).  Most of the project work was done as a part of an Arizona State University summer archaeological field school.  ',  N'PROJECT',  N'Ojo Bonito Archaeological Project (OBAP)', 6, NULL, 6,  '2010-10-18 12:25:01.026',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (1628,  '2009-11-11 09:35:05.465',  N'The Heshotauthla Archaeological Research Project (HARP) did limited excavation at the Pueblo IV site of Heshotauthla and intensive systematic survey in the area of the site on the Zuni Indian Reservation.  Excavation was limited to areas threatened by erosion and areas thought t have been previously excavated by the Hemenway Southwestern Archaeological Expedition in the late 1880s.  HARP survey recorded 305 prehistoric and historic sites in 10.4 square kilometers, including a post-Chaocan great house with a great kiva.',  N'PROJECT',  N'Heshotauthla Archaeological Research Project (HARP)', 6, NULL, 6,  '2009-11-11 09:35:05.465',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3029,  '2010-02-17 11:14:59.623',  N'this should not be null',  N'ONTOLOGY',  N'Fauna Pathologies - Default Ontology Draft', 6, NULL, 6,  '2010-02-17 11:14:59.623',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3460,  '2010-07-09 12:15:56.561',  N'These ontologies are working drafts used by the ASU Faunal Working Group funded by NSF and by the NEH/JISC-funded TAG faunal experts.  As of 2010-08-11 the older versions of both the NSF & TAG ontologies are replaced by these due to a bug in the ontology parser.',  N'PROJECT',  N'Fauna Ontology Drafts - NSF & NEH/JISC TAG Faunal Working Groups', 6, NULL, 6,  '2010-07-09 12:15:56.561',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (4231,  '2010-08-14 11:55:20.911',  N'2008 New Philadelphia Archaeology Report, Chapter 3, Block 3, Lot 4',  N'DOCUMENT',  N'2008 New Philadelphia Archaeology Report, Chapter 3, Block 3, Lot 4', 8422, NULL, 8422,  '2010-08-14 11:55:20.911',  N'ACTIVE', NULL, 8422, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3794,  '2010-07-22 14:03:57.722',  N'this should not be null',  N'DOCUMENT',  N'Faunal Coding Key', 8161, NULL, 8161,  '2010-07-22 14:03:57.722',  N'ACTIVE', NULL, 8161, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3088,  '2010-03-05 16:05:35.9',  N'Knowth Stage 8 Fauna Dataset',  N'DATASET',  N'Knowth Stage 8 Fauna Dataset', 6, NULL, 8608,  '2010-11-19 08:50:30.811',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (2420,  '2009-09-16 07:41:11.726',  N'This NSF-funded research project was directed by Patty Jo Watson, Steven, LeBlanc, and Charles Redman.  In the summers of 1972 and 1973 it accomplished survey and excavation in the El Morro Valley of New Mexico.  ',  N'PROJECT',  N'Cibola Archaeological Research Project (CARP)', 6, NULL, 6,  '2009-09-16 07:41:11.726',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3073,  '2010-03-05 12:52:53.862',  N'This is a project set up to include files for the April 2010 ADS/tDAR workshop at the University of York.',  N'PROJECT',  N'TAG Faunal Workshop', 6, NULL, 6,  '2010-03-05 12:52:53.862',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3738,  '2010-07-22 13:40:50.11',  N'These databases are from on-going archaeological research in the city of Alexandria, Virginia on eighteenth and nineteenth century occupations, directed by Dr. Pamela Cressey, City Archaeologist.',  N'PROJECT',  N'Alexandria, Virginia Historic Period Fauna', 8161, NULL, 8019,  '2011-04-13 17:22:13.514',  N'ACTIVE', NULL, 8161, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3805,  '2010-07-29 13:23:45.594',  N'Archaeological and historical analysis of New Philadelphia, Illinois, founded in 1836.',  N'PROJECT',  N'New Philadelphia Archaeology Project', 8422, NULL, 8422,  '2010-07-29 13:23:45.594',  N'ACTIVE', NULL, 8422, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3479,  '2010-07-09 15:35:06.14',  N'this should not be null',  N'ONTOLOGY',  N'(Outdated) TAG Fauna Ontology - Taxon', 6, NULL, 8019,  '2011-04-22 13:03:13.405',  N'DELETED', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (4232,  '2010-08-14 11:57:13.085',  N'2008 New Philadelphia Archaeology Report, Chapter 4, Block 7, Lot 1',  N'DOCUMENT',  N'2008 New Philadelphia Archaeology Report, Chapter 4, Block 7, Lot 1', 8422, NULL, 8422,  '2010-08-14 11:57:13.085',  N'ACTIVE', NULL, 8422, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (4230,  '2010-08-14 11:54:03.684',  N'2008 New Philadelphia Archaeology Report, Chapter 2, An Investigation of New Philadelphia Using Thermal Infrared Remote Sensing',  N'DOCUMENT',  N'2008 New Philadelphia Archaeology Report, Chapter 2, An Investigation of New Philadelphia Using Thermal Infrared Remote Sensing', 8422, NULL, 8422,  '2010-08-14 11:54:03.684',  N'ACTIVE', NULL, 8422, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (4279,  '2010-09-02 10:04:31.402',  N'WACC reports of archaeological excavation and survey projects within and near Tuzigoot National Monument, Yavapai County, Arizona.',  N'PROJECT',  N'The Archaeology of Tuzigoot National Monument and Montezuma Castle National Monument', 5349, NULL, 5349,  '2010-09-02 10:04:31.402',  N'ACTIVE', NULL, 5349, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3087,  '2010-03-05 16:04:45.919',  N'this should not be null',  N'CODING_SHEET',  N'Durrington Walls - Coding Sheet - Fauna -  Fusion  ', 6, NULL, 6,  '2010-03-05 16:04:45.919',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (4287,  '2010-09-02 13:05:13.595',  N'Inventory survey of Montezuma Castle National Monument was conducted from April 11 to May 7, 1988. Of the 70 sites recorded, 30 were new additions to the site inventory. Cliff dwellings, rockshelters and pueblos were recorded along with one and two-room masonry structures, roasting pits and artifact scatters. Agricultural feature sites include the canals at Montezuma Well. A site with prehistoric cobble concentrations, a burial ground, a bedrock mortar site, a lithic scatter, a historical site and three sites with enigmatic rock features complete the inventory. The development of architecture at the Monument is seen in the early pithouses (Squaw Peak and Camp Verde phases) excavated by Breternitz (1960), followed in time by surface structures, rockshelters with masonry rooms and finally the Honanki-Tuzigoot phase cliff dwellings.

In addition to the survey, the project included a detailed and systematic re-recording of Montezuma Castle''s 20 rooms. The results of this work, Part II of the report, are a roorn-by-roorn description, chronological summary of previous research and stabilization, a record of historic graffiti, and interpretation of construction sequence and room function. No structural deterioration was recorded, but continued periodic inspection by a trained specialist is recommended',  N'DOCUMENT',  N'Archeological Survey and Architectural Study of Montezuma Castle National Monument', 5349, NULL, 8344,  '2011-06-04 21:41:17.936',  N'ACTIVE', NULL, 5349, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (139,  '2008-04-24 00:37:53.078',  N'this should not be null',  N'PROJECT',  N'Rudd Creek Archaeological Project', 38, NULL, 38,  '2008-04-24 00:37:53.078',  N'ACTIVE', NULL, 38, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3074,  '2010-03-05 12:59:58.505',  N'this should not be null',  N'DATASET',  N'Durrington Walls Humerus Dataset', 6, NULL, 6,  '2010-03-05 12:59:58.505',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (3167,  '2010-03-05 16:06:13.44',  N'this should not be null',  N'CODING_SHEET',  N'Knowth - Coding Sheet - Fauna - Fusion', 6, NULL, 6,  '2010-03-05 16:06:13.44',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
INSERT INTO resource (id, date_registered, description, resource_type, title, submitter_id, url, updater_id, date_updated, status, external_id, uploader_id, account_id, previous_status, total_files, total_space_in_bytes) VALUES (449,  '2009-11-10 13:50:48.203',  N'this should not be null',  N'CODING_SHEET',  N'CARP Fauna Proximal-Distal', 6, NULL, 6,  '2009-11-10 13:50:48.203',  N'ACTIVE', NULL, 6, NULL, NULL, NULL, NULL);
--------------------------- collection ---------------------------
--------------------------- collection ---------------------------
--------------------------- collection ---------------------------
INSERT INTO collection (id, description, name, updater_id, parent_id, orientation, collection_type, visible, date_created, date_updated, sort_order, owner_id, secondary_sort_order, description_admin) VALUES (867, NULL, NULL, 6, NULL,  N'LIST',  N'INTERNAL',  false ,  '2011-10-18 14:07:48.434823',  '2013-02-11 16:54:24.923212', NULL, 6, NULL, NULL);
INSERT INTO collection (id, description, name, updater_id, parent_id, orientation, collection_type, visible, date_created, date_updated, sort_order, owner_id, secondary_sort_order, description_admin) VALUES (1095, NULL, NULL, 5349, NULL,  N'LIST',  N'INTERNAL',  false ,  '2011-10-18 14:07:48.434823',  '2013-02-11 16:54:24.923212', NULL, 5349, NULL, NULL);
INSERT INTO collection (id, description, name, updater_id, parent_id, orientation, collection_type, visible, date_created, date_updated, sort_order, owner_id, secondary_sort_order, description_admin) VALUES (1107, NULL, NULL, 6, NULL,  N'LIST',  N'INTERNAL',  false ,  '2011-10-18 14:07:48.434823',  '2013-02-11 16:54:24.923212', NULL, 6, NULL, NULL);
INSERT INTO collection (id, description, name, updater_id, parent_id, orientation, collection_type, visible, date_created, date_updated, sort_order, owner_id, secondary_sort_order, description_admin) VALUES (1205, NULL, NULL, 8161, NULL,  N'LIST',  N'INTERNAL',  false ,  '2011-10-18 14:07:48.434823',  '2013-02-11 16:54:24.923212', NULL, 8161, NULL, NULL);
INSERT INTO collection (id, description, name, updater_id, parent_id, orientation, collection_type, visible, date_created, date_updated, sort_order, owner_id, secondary_sort_order, description_admin) VALUES (1391, NULL, NULL, 8422, NULL,  N'LIST',  N'INTERNAL',  false ,  '2011-10-18 14:07:48.434823',  '2013-02-11 16:54:24.923212', NULL, 8422, NULL, NULL);
INSERT INTO collection (id, description, name, updater_id, parent_id, orientation, collection_type, visible, date_created, date_updated, sort_order, owner_id, secondary_sort_order, description_admin) VALUES (1397, NULL, NULL, 6, NULL,  N'LIST',  N'INTERNAL',  false ,  '2011-10-18 14:07:48.434823',  '2013-02-11 16:54:24.923212', NULL, 6, NULL, NULL);
INSERT INTO collection (id, description, name, updater_id, parent_id, orientation, collection_type, visible, date_created, date_updated, sort_order, owner_id, secondary_sort_order, description_admin) VALUES (1543, NULL, NULL, 6, NULL,  N'LIST',  N'INTERNAL',  false ,  '2011-10-18 14:07:48.434823',  '2013-02-11 16:54:24.923212', NULL, 6, NULL, NULL);
INSERT INTO collection (id, description, name, updater_id, parent_id, orientation, collection_type, visible, date_created, date_updated, sort_order, owner_id, secondary_sort_order, description_admin) VALUES (1552, NULL, NULL, 6, NULL,  N'LIST',  N'INTERNAL',  false ,  '2011-10-18 14:07:48.434823',  '2013-02-11 16:54:24.923212', NULL, 6, NULL, NULL);
INSERT INTO collection (id, description, name, updater_id, parent_id, orientation, collection_type, visible, date_created, date_updated, sort_order, owner_id, secondary_sort_order, description_admin) VALUES (1570, NULL, NULL, 6, NULL,  N'LIST',  N'INTERNAL',  false ,  '2011-10-18 14:07:48.434823',  '2013-02-11 16:54:24.923212', NULL, 6, NULL, NULL);
--------------------------- collection_resource ---------------------------
INSERT INTO collection_resource (collection_id, resource_id) VALUES (867, 262);
INSERT INTO collection_resource (collection_id, resource_id) VALUES (1095, 4287);
INSERT INTO collection_resource (collection_id, resource_id) VALUES (1107, 1628);
INSERT INTO collection_resource (collection_id, resource_id) VALUES (1205, 3738);
INSERT INTO collection_resource (collection_id, resource_id) VALUES (1391, 3805);
INSERT INTO collection_resource (collection_id, resource_id) VALUES (1397, 449);
INSERT INTO collection_resource (collection_id, resource_id) VALUES (1543, 1656);
INSERT INTO collection_resource (collection_id, resource_id) VALUES (1552, 3088);
INSERT INTO collection_resource (collection_id, resource_id) VALUES (1570, 2420);
--------------------------- authorized_user ---------------------------
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5056,  N'CAN_DELETE', 5979, 867,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5116,  N'CAN_DELETE', 5979, 1107,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5118,  N'CAN_DELETE', 5979, 1570,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5119,  N'CAN_DELETE', 60, 1570,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5347,  N'CAN_DELETE', 8018, 1543,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5348,  N'CAN_DELETE', 8019, 1543,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5392,  N'CAN_DELETE', 8018, 1397,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5393,  N'CAN_DELETE', 8019, 1397,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5407,  N'CAN_DELETE', 1, 1552,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5408,  N'CAN_DELETE', 8019, 1552,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5420,  N'CAN_DELETE', 8018, 1552,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5538,  N'CAN_DELETE', 60, 1107,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5622,  N'CAN_DELETE', 5979, 1397,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5623,  N'CAN_DELETE', 8161, 1397,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5649,  N'CAN_DELETE', 8161, 1570,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5650,  N'CAN_DELETE', 8018, 1570,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5651,  N'CAN_DELETE', 8019, 1570,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5652,  N'CAN_DELETE', 8161, 1107,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5653,  N'CAN_DELETE', 8019, 1107,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5654,  N'CAN_DELETE', 8018, 1107,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5655,  N'CAN_DELETE', 8018, 867,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5656,  N'CAN_DELETE', 8161, 867,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5658,  N'CAN_DELETE', 60, 867,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5659,  N'CAN_DELETE', 8019, 867,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5874,  N'CAN_DELETE', 8389, 1107,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5887,  N'CAN_DELETE', 8044, 1391,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (5888,  N'CAN_DELETE', 8344, 1391,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (6012,  N'CAN_DELETE', 8344, 1095,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (6013,  N'CAN_DELETE', 8018, 1095,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (6014,  N'CAN_DELETE', 8044, 1095,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (7177,  N'CAN_DELETE', 8019, 1205,  N'MODIFY_RECORD', 500);
INSERT INTO authorized_user (id, admin_permission, user_id, resource_collection_id, general_permission, general_permission_int) VALUES (7550,  N'NONE', 8019, 1205,  N'VIEW_ALL', 100);
--------------------------- project ---------------------------
INSERT INTO project (id, sort_order, orientation, secondary_sort_order) VALUES (139,  N'RESOURCE_TYPE',  N'LIST', NULL);
INSERT INTO project (id, sort_order, orientation, secondary_sort_order) VALUES (262,  N'RESOURCE_TYPE',  N'LIST', NULL);
INSERT INTO project (id, sort_order, orientation, secondary_sort_order) VALUES (1628,  N'RESOURCE_TYPE',  N'LIST', NULL);
INSERT INTO project (id, sort_order, orientation, secondary_sort_order) VALUES (2420,  N'RESOURCE_TYPE',  N'LIST', NULL);
INSERT INTO project (id, sort_order, orientation, secondary_sort_order) VALUES (3073,  N'RESOURCE_TYPE',  N'LIST', NULL);
INSERT INTO project (id, sort_order, orientation, secondary_sort_order) VALUES (3460,  N'RESOURCE_TYPE',  N'LIST', NULL);
INSERT INTO project (id, sort_order, orientation, secondary_sort_order) VALUES (3462,  N'RESOURCE_TYPE',  N'LIST', NULL);
INSERT INTO project (id, sort_order, orientation, secondary_sort_order) VALUES (3738,  N'RESOURCE_TYPE',  N'LIST', NULL);
INSERT INTO project (id, sort_order, orientation, secondary_sort_order) VALUES (3805,  N'RESOURCE_TYPE',  N'LIST', NULL);
INSERT INTO project (id, sort_order, orientation, secondary_sort_order) VALUES (4279,  N'RESOURCE_TYPE',  N'LIST', NULL);
--------------------------- authorized_user ---------------------------
--------------------------- information_resource ---------------------------
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (3794, 2010, 3738, 11154, NULL,  false ,  N'ENGLISH', NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 2010,  false ,  false ,  false , NULL, NULL,  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (3029, -1, 3460, NULL, NULL,  false , NULL, NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 0,  false ,  false ,  false , NULL, NULL,  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (3087, 2010, 3073, NULL, NULL,  false , NULL, NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 2010,  false ,  false ,  false , NULL, NULL,  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (449, 2009, 2420, NULL, NULL,  false , NULL, NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 2000,  false ,  false ,  false , NULL, NULL,  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (3479, -1, 3462, NULL, NULL,  false , NULL, NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 0,  false ,  false ,  false , NULL, NULL,  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (3167, -1, 3073, NULL, NULL,  false , NULL, NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 0,  false ,  false ,  false , NULL, NULL,  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (1656, -1, 1628, NULL, NULL,  false , NULL, NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 0,  false ,  false ,  false , NULL, NULL,  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (4287, 1988, 4279, 11055, NULL,  false ,  N'ENGLISH', NULL,  N'Montezuma Castle National Monument, Western Archeological and Conservation Center',  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 1980,  false ,  false ,  false , 11169,  N'Tucson, Arizona',  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (3074, 2010, 3073, NULL, NULL,  false ,  N'ENGLISH', NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 2010,  false ,  false ,  false , NULL, NULL,  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (3088, 2010, 3073, NULL, NULL,  false ,  N'ENGLISH', NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 2010,  false ,  false ,  false , NULL, NULL,  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (4230, 2008, 3805, NULL, NULL,  false ,  N'ENGLISH', NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 2000,  false ,  false ,  false , NULL, NULL,  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (4231, 2008, 3805, NULL, NULL,  false ,  N'ENGLISH', NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 2000,  false ,  false ,  false , NULL, NULL,  false );
INSERT INTO information_resource (id, date_created, project_id, provider_institution_id, last_uploaded, external_reference, resource_language, metadata_language, copy_location, inheriting_investigation_information, inheriting_site_information, inheriting_material_information, inheriting_other_information, inheriting_cultural_information, inheriting_spatial_information, inheriting_temporal_information, mappeddatakeycolumn_id, mappeddatakeyvalue, license_type, license_text, copyright_holder_id, date_created_normalized, inheriting_identifier_information, inheriting_note_information, inheriting_collection_information, publisher_id, publisher_location, inheriting_individual_institutional_credit) VALUES (4232, 2008, 3805, NULL, NULL,  false ,  N'ENGLISH', NULL, NULL,  false ,  false ,  false ,  false ,  false ,  false ,  false , NULL, NULL, NULL, NULL, NULL, 2000,  false ,  false ,  false , NULL, NULL,  false );
--------------------------- information_resource_file ---------------------------
INSERT INTO information_resource_file (id, general_type, latest_version, sequence_number, information_resource_id, status, number_of_parts, restriction, date_made_public, error_message, part_of_composite, description, file_created_date, filename, deleted) VALUES (730,  N'DOCUMENT', 0, 0, 4287, NULL, NULL,  N'CONFIDENTIAL', NULL, NULL,  false , NULL, NULL,  N'pia-50-moca.pdf',  false );
INSERT INTO information_resource_file (id, general_type, latest_version, sequence_number, information_resource_id, status, number_of_parts, restriction, date_made_public, error_message, part_of_composite, description, file_created_date, filename, deleted) VALUES (689,  N'DOCUMENT', 0, 0, 4230, NULL, NULL,  N'PUBLIC', NULL, NULL,  false , NULL, NULL,  N'a2008reportchap2.pdf',  false );
INSERT INTO information_resource_file (id, general_type, latest_version, sequence_number, information_resource_id, status, number_of_parts, restriction, date_made_public, error_message, part_of_composite, description, file_created_date, filename, deleted) VALUES (691,  N'DOCUMENT', 0, 0, 4232, NULL, NULL,  N'PUBLIC', NULL, NULL,  false , NULL, NULL,  N'a2008reportchap4.pdf',  false );
INSERT INTO information_resource_file (id, general_type, latest_version, sequence_number, information_resource_id, status, number_of_parts, restriction, date_made_public, error_message, part_of_composite, description, file_created_date, filename, deleted) VALUES (690,  N'DOCUMENT', 0, 0, 4231, NULL, NULL,  N'PUBLIC', NULL, NULL,  false , NULL, NULL,  N'a2008reportchap3.pdf',  false );
INSERT INTO information_resource_file (id, general_type, latest_version, sequence_number, information_resource_id, status, number_of_parts, restriction, date_made_public, error_message, part_of_composite, description, file_created_date, filename, deleted) VALUES (124,  N'COLUMNAR_DATA', 0, 0, 1656,  N'PROCESSED', NULL,  N'PUBLIC', NULL, NULL,  false , NULL, NULL,  N'coding_sheet_1656_harpspecies2.csv',  false );
INSERT INTO information_resource_file (id, general_type, latest_version, sequence_number, information_resource_id, status, number_of_parts, restriction, date_made_public, error_message, part_of_composite, description, file_created_date, filename, deleted) VALUES (285,  N'COLUMNAR_DATA', 0, 0, 449, NULL, NULL,  N'PUBLIC', NULL, NULL,  false , NULL, NULL,  N'carp-proximal-distal.csv',  false );
INSERT INTO information_resource_file (id, general_type, latest_version, sequence_number, information_resource_id, status, number_of_parts, restriction, date_made_public, error_message, part_of_composite, description, file_created_date, filename, deleted) VALUES (45,  N'COLUMNAR_DATA', 0, 0, 3088,  N'PROCESSED', NULL,  N'PUBLIC', NULL, NULL,  false , NULL, NULL,  N'dataset_3088_knowthstage8.xls',  false );
INSERT INTO information_resource_file (id, general_type, latest_version, sequence_number, information_resource_id, status, number_of_parts, restriction, date_made_public, error_message, part_of_composite, description, file_created_date, filename, deleted) VALUES (354,  N'OTHER', 0, 0, 3479, NULL, NULL,  N'PUBLIC', NULL, NULL,  false , NULL, NULL,  N'tag-fauna-ontology---taxon.owl',  false );
INSERT INTO information_resource_file (id, general_type, latest_version, sequence_number, information_resource_id, status, number_of_parts, restriction, date_made_public, error_message, part_of_composite, description, file_created_date, filename, deleted) VALUES (490,  N'OTHER', 0, 0, 3029, NULL, NULL,  N'PUBLIC', NULL, NULL,  false , NULL, NULL,  N'fauna-pathologies---default-ontology-draft.owl',  false );
INSERT INTO information_resource_file (id, general_type, latest_version, sequence_number, information_resource_id, status, number_of_parts, restriction, date_made_public, error_message, part_of_composite, description, file_created_date, filename, deleted) VALUES (41,  N'COLUMNAR_DATA', 0, 0, 3074,  N'PROCESSED', NULL,  N'PUBLIC', NULL, NULL,  false , NULL, NULL,  N'dataset_3074_dwhum.xls',  false );
INSERT INTO information_resource_file (id, general_type, latest_version, sequence_number, information_resource_id, status, number_of_parts, restriction, date_made_public, error_message, part_of_composite, description, file_created_date, filename, deleted) VALUES (416,  N'DOCUMENT', 0, 0, 3794, NULL, NULL,  N'EMBARGOED',  '2015-07-22 14:03:57.731', NULL,  false , NULL, NULL,  N'alexandriafaunalcodes.pdf',  false );
--------------------------- information_resource_file_version ---------------------------
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (68,  N'481a6c17491082682952bd243620d842',  N'MD5',  '2010-10-16 10:43:14.640083',  N'owl', NULL,  N'tag-fauna-ontology---taxon.owl',  N'b67146eb39734bdfad05bb2e7ad5ef2e', NULL, NULL,  N'UPLOADED',  N'application/rdf+xml',  N'34/79/rec/354/v0', NULL, 21751, 0, NULL, 354, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (100,  N'fd2569d135443239e53082398ddbbbbc',  N'MD5',  '2010-10-16 10:43:14.640083',  N'xls', NULL,  N'dataset_3088_knowthstage8.xls',  N'99883f25aa34403db16a2dc56a57f78a', NULL, NULL,  N'UPLOADED',  N'application/vnd.ms-excel',  N'30/88/rec/45/v0', NULL, 700928, 0, NULL, 45, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (133,  N'122ca46deab33face57313deee8c604d',  N'MD5',  '2010-10-16 10:43:14.640083',  N'xls', NULL,  N'dataset_3074_dwhum.xls',  N'594b614b45014ea480f1ea4b192c3672', NULL, NULL,  N'UPLOADED',  N'application/vnd.ms-excel',  N'30/74/rec/41/v0', NULL, 73216, 0, NULL, 41, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (197,  N'1002fb7da74588a09166b51fc78a3585',  N'MD5',  '2010-10-16 10:43:14.640083',  N'csv', NULL,  N'carp-proximal-distal.csv',  N'3a7dcee257f347e58c231ed0ec8ab727', NULL, NULL,  N'UPLOADED',  N'text/csv',  N'44/9/rec/285/v0', NULL, 917, 0, NULL, 285, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (248,  N'909f44160dba147987624e26bb5914e4',  N'MD5',  '2010-10-16 10:43:14.640083',  N'pdf', NULL,  N'alexandriafaunalcodes.pdf',  N'6f2e9433c3384cbf87699655428e2851', NULL, NULL,  N'UPLOADED',  N'application/pdf',  N'37/94/rec/416/v0', NULL, 20999, 0, NULL, 416, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (274,  N'2035b2fb63e66bec52049666343a7028',  N'MD5',  '2010-10-16 10:43:14.640083',  N'owl', NULL,  N'fauna-pathologies---default-ontology-draft.owl',  N'06bbb604934d467f9ef5007c6d9257ff', NULL, NULL,  N'UPLOADED',  N'application/rdf+xml',  N'30/29/rec/490/v0', NULL, 580, 0, NULL, 490, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (477,  N'ca3e5b3082d02d22eab26d27bd05bda9',  N'MD5',  '2010-10-16 10:43:14.640083',  N'pdf', NULL,  N'a2008reportchap4.pdf',  N'3bee995a33eb4e40a92808d9d1a945ef', NULL, NULL,  N'UPLOADED',  N'application/pdf',  N'42/32/rec/691/v0', NULL, 923934, 0, NULL, 691, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (476,  N'34c5009fab97c0145d90efd22525f2d9',  N'MD5',  '2010-10-16 10:43:14.640083',  N'pdf', NULL,  N'a2008reportchap2.pdf',  N'01f2009734364f288186c9ecdd090699', NULL, NULL,  N'UPLOADED',  N'application/pdf',  N'42/30/rec/689/v0', NULL, 1031374, 0, NULL, 689, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (514,  N'808bc26e46014a4279b9d23d9c52b89a',  N'MD5',  '2010-10-16 10:43:14.640083',  N'pdf', NULL,  N'pia-50-moca.pdf',  N'bc0c8d7b02cf406e814b322cc562805d', NULL, NULL,  N'UPLOADED',  N'application/pdf',  N'42/87/rec/730/v0', NULL, 8711273, 0, NULL, 730, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (614,  N'23a43b0621f4e3250ed9a694ed5c99d4',  N'MD5',  '2010-10-16 10:43:14.640083',  N'pdf', NULL,  N'a2008reportchap3.pdf',  N'3afc44b1750c46bdb1db8221ecf708ff', NULL, NULL,  N'UPLOADED',  N'application/pdf',  N'42/31/rec/690/v0', NULL, 1294831, 0, NULL, 690, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (36,  N'3421436d6672f2ceef8d86ca7c4f3cb7',  N'MD5',  '2010-10-16 10:43:14.640083',  N'xls', NULL,  N'dataset_3074_dwhum_translated.xls',  N'4dcfb6a29d1b44fd96a494d2e82ee183', NULL, NULL,  N'TRANSLATED',  N'application/vnd.ms-excel',  N'30/74/rec/41/v0/deriv', NULL, 82432, 0, NULL, 41, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (1912,  N'5056b9bb5848cfe92d415f444676e0e8',  N'MD5',  '2010-12-06 10:57:20.771',  N'txt', NULL,  N'pia-50-moca.pdf.txt', NULL, NULL, NULL,  N'INDEXABLE_TEXT',  N'text/plain',  N'42/87/rec/730/v0/deriv', NULL, 307390, 0, NULL, 730, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (1913,  N'd9dc8d0f755b6008877444192f5f659f',  N'MD5',  '2010-12-06 10:57:20.902',  N'jpg', NULL,  N'pia-50-moca1_md.jpg', NULL, NULL, 300,  N'WEB_MEDIUM',  N'image/jpeg',  N'42/87/rec/730/v0/deriv', NULL, 3432, 0, 233, 730, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (1914,  N'f9c46a8f88caf99e66d9a292b659e987',  N'MD5',  '2010-12-06 10:57:21.007',  N'jpg', NULL,  N'pia-50-moca1_lg.jpg', NULL, NULL, 600,  N'WEB_LARGE',  N'image/jpeg',  N'42/87/rec/730/v0/deriv', NULL, 8126, 0, 466, 730, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (1915,  N'3b1d2647801a4c3a293e385d4c33099b',  N'MD5',  '2010-12-06 10:57:21.026',  N'jpg', NULL,  N'pia-50-moca1_sm.jpg', NULL, NULL, 96,  N'WEB_SMALL',  N'image/jpeg',  N'42/87/rec/730/v0/deriv', NULL, 1255, 0, 75, 730, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2060,  N'2964e7041c3140a67351da2a0d8137e2',  N'MD5',  '2010-12-06 10:59:17.957',  N'txt', NULL,  N'a2008reportchap4.pdf.txt', NULL, NULL, NULL,  N'INDEXABLE_TEXT',  N'text/plain',  N'42/32/rec/691/v0/deriv', NULL, 29497, 0, NULL, 691, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2061,  N'0a6415f1749e935639aee5c78e53f50d',  N'MD5',  '2010-12-06 10:59:18.129',  N'jpg', NULL,  N'a2008reportchap41_md.jpg', NULL, NULL, 300,  N'WEB_MEDIUM',  N'image/jpeg',  N'42/32/rec/691/v0/deriv', NULL, 14227, 0, 232, 691, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2062,  N'a529ee5307dfa8307008f2e856a55f13',  N'MD5',  '2010-12-06 10:59:18.228',  N'jpg', NULL,  N'a2008reportchap41_lg.jpg', NULL, NULL, 600,  N'WEB_LARGE',  N'image/jpeg',  N'42/32/rec/691/v0/deriv', NULL, 34335, 0, 464, 691, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2063,  N'4ddbaf5b813f5bafed9ddeb6c76ac693',  N'MD5',  '2010-12-06 10:59:18.277',  N'jpg', NULL,  N'a2008reportchap41_sm.jpg', NULL, NULL, 96,  N'WEB_SMALL',  N'image/jpeg',  N'42/32/rec/691/v0/deriv', NULL, 1804, 0, 74, 691, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2064,  N'dbd4faf63595d78a00bb1a8223a13813',  N'MD5',  '2010-12-06 10:59:19.318',  N'txt', NULL,  N'a2008reportchap3.pdf.txt', NULL, NULL, NULL,  N'INDEXABLE_TEXT',  N'text/plain',  N'42/31/rec/690/v0/deriv', NULL, 46028, 0, NULL, 690, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2065,  N'ffe59f0880f7c197efc5adc85f278969',  N'MD5',  '2010-12-06 10:59:19.509',  N'jpg', NULL,  N'a2008reportchap31_md.jpg', NULL, NULL, 300,  N'WEB_MEDIUM',  N'image/jpeg',  N'42/31/rec/690/v0/deriv', NULL, 20123, 0, 232, 690, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2066,  N'309d7f6da54928ef730f827ee90d682d',  N'MD5',  '2010-12-06 10:59:19.563',  N'jpg', NULL,  N'a2008reportchap31_lg.jpg', NULL, NULL, 600,  N'WEB_LARGE',  N'image/jpeg',  N'42/31/rec/690/v0/deriv', NULL, 57831, 0, 464, 690, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2067,  N'dbe446eb9e7a33c13ab308474ce8ec1a',  N'MD5',  '2010-12-06 10:59:19.582',  N'jpg', NULL,  N'a2008reportchap31_sm.jpg', NULL, NULL, 96,  N'WEB_SMALL',  N'image/jpeg',  N'42/31/rec/690/v0/deriv', NULL, 2214, 0, 74, 690, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2068,  N'b941e009efb55c732e2d9dd7e087c1a0',  N'MD5',  '2010-12-06 10:59:20.329',  N'txt', NULL,  N'a2008reportchap2.pdf.txt', NULL, NULL, NULL,  N'INDEXABLE_TEXT',  N'text/plain',  N'42/30/rec/689/v0/deriv', NULL, 13317, 0, NULL, 689, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2069,  N'b2956dc307e512527d038df76af2b8ca',  N'MD5',  '2010-12-06 10:59:20.48',  N'jpg', NULL,  N'a2008reportchap21_md.jpg', NULL, NULL, 300,  N'WEB_MEDIUM',  N'image/jpeg',  N'42/30/rec/689/v0/deriv', NULL, 19515, 0, 232, 689, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2070,  N'776f15c86b992f13023159da589bebe3',  N'MD5',  '2010-12-06 10:59:20.534',  N'jpg', NULL,  N'a2008reportchap21_lg.jpg', NULL, NULL, 600,  N'WEB_LARGE',  N'image/jpeg',  N'42/30/rec/689/v0/deriv', NULL, 50861, 0, 464, 689, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2071,  N'930759ac347d74b4379d1ed0cffd686c',  N'MD5',  '2010-12-06 10:59:20.553',  N'jpg', NULL,  N'a2008reportchap21_sm.jpg', NULL, NULL, 96,  N'WEB_SMALL',  N'image/jpeg',  N'42/30/rec/689/v0/deriv', NULL, 2231, 0, 74, 689, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2892,  N'3843398b8f010ca9615c6275f8bdfbf1',  N'MD5',  '2010-12-06 11:03:13.892',  N'txt', NULL,  N'alexandriafaunalcodes.pdf.txt', NULL, NULL, NULL,  N'INDEXABLE_TEXT',  N'text/plain',  N'37/94/rec/416/v0/deriv', NULL, 10491, 0, NULL, 416, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2893,  N'ffed39624223564f6c3a693fbc53d7b9',  N'MD5',  '2010-12-06 11:03:14.061',  N'jpg', NULL,  N'alexandriafaunalcodes1_md.jpg', NULL, NULL, 300,  N'WEB_MEDIUM',  N'image/jpeg',  N'37/94/rec/416/v0/deriv', NULL, 11760, 0, 232, 416, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2894,  N'481cd3f59e6858b7e89a22fabf950907',  N'MD5',  '2010-12-06 11:03:14.158',  N'jpg', NULL,  N'alexandriafaunalcodes1_lg.jpg', NULL, NULL, 600,  N'WEB_LARGE',  N'image/jpeg',  N'37/94/rec/416/v0/deriv', NULL, 29484, 0, 464, 416, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (2895,  N'801e4017f05854c88c76f198df7b22c3',  N'MD5',  '2010-12-06 11:03:14.209',  N'jpg', NULL,  N'alexandriafaunalcodes1_sm.jpg', NULL, NULL, 96,  N'WEB_SMALL',  N'image/jpeg',  N'37/94/rec/416/v0/deriv', NULL, 1671, 0, 74, 416, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (1,  N'cdabeeeb114ef109181ea1075b9e3725',  N'MD5',  '2010-10-16 10:43:14.640083',  N'csv', NULL,  N'coding_sheet_1656_harpspecies2.csv',  N'1c5c6d5f8e7a408c9be09233f5818262', NULL, NULL,  N'UPLOADED',  N'text/csv',  N'16/56/rec/124/v0', NULL, 810, 0, NULL, 124, NULL, NULL,  false );
INSERT INTO information_resource_file_version (id, checksum, checksum_type, date_created, extension, file_type, filename, filestore_id, format, height, internal_type, mime_type, path, premisid, size, file_version, width, information_resource_file_id, total_time, effective_size, primary_file) VALUES (3565,  N'f9a58d48cf1f3733c4a4c72bdf70ab4b',  N'MD5',  '2011-01-24 16:53:45.898',  N'xls', NULL,  N'dataset_3088_knowthstage8_translated.xls', NULL, NULL, NULL,  N'TRANSLATED',  N'application/vnd.ms-excel',  N'30/88/rec/45/v0/deriv', NULL, 1363968, 0, NULL, 45, NULL, NULL,  false );
--------------------------- image ---------------------------
--------------------------- coding_sheet ---------------------------
INSERT INTO coding_sheet (id, category_variable_id, default_ontology_id, generated) VALUES (3087, 75, NULL,  false );
INSERT INTO coding_sheet (id, category_variable_id, default_ontology_id, generated) VALUES (3167, 75, NULL,  false );
INSERT INTO coding_sheet (id, category_variable_id, default_ontology_id, generated) VALUES (449, 81, NULL,  false );
INSERT INTO coding_sheet (id, category_variable_id, default_ontology_id, generated) VALUES (1656, 85, NULL,  false );
--------------------------- ontology ---------------------------
INSERT INTO ontology (id, category_variable_id) VALUES (3479, 85);
INSERT INTO ontology (id, category_variable_id) VALUES (3029, 80);
--------------------------- dataset ---------------------------
INSERT INTO dataset (id) VALUES (3074);
INSERT INTO dataset (id) VALUES (3088);
--------------------------- data_table ---------------------------
INSERT INTO data_table (id, name, dataset_id, description, display_name) VALUES (3089,  N'dataset_3088_knowthstage8_tbl_st', 3088, NULL,  N'dataset_3088_knowthstage8_tbl_st');
INSERT INTO data_table (id, name, dataset_id, description, display_name) VALUES (3075,  N'dataset_3074_dwhum_dwhum', 3074, NULL,  N'dataset_3074_dwhum_dwhum');
--------------------------- data_table_column ---------------------------
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3085,  N'VARCHAR',  N'FUSD', 75, NULL, 3075, 3087, NULL,  N'FUSD', NULL,  N'CODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3101,  N'VARCHAR',  N'FUSD', 75, NULL, 3089, 3167, NULL,  N'FUSD', NULL,  N'CODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3100,  N'VARCHAR',  N'FUSP', 75, NULL, 3089, 3167, NULL,  N'FUSP', NULL,  N'CODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3129,  N'VARCHAR',  N'Wmin', NULL, NULL, 3089, NULL, NULL,  N'Wmin', NULL,  N'MEASUREMENT', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3138,  N'VARCHAR',  N'I1', NULL, NULL, 3089, NULL, NULL,  N'I1', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3130,  N'VARCHAR',  N'Wmax', NULL, NULL, 3089, NULL, NULL,  N'Wmax', NULL,  N'MEASUREMENT', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3084,  N'VARCHAR',  N'FUSP', 75, NULL, 3075, 3087, NULL,  N'FUSP', NULL,  N'CODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3081,  N'VARCHAR',  N'BT', NULL, NULL, 3075, NULL, NULL,  N'BT', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3147,  N'VARCHAR',  N'P', NULL, NULL, 3089, NULL, NULL,  N'P', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3157,  N'VARCHAR',  N'M2', NULL, NULL, 3089, NULL, NULL,  N'M2', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3091,  N'VARCHAR',  N'REC ID', 200, NULL, 3089, NULL, NULL,  N'REC ID', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3097,  N'VARCHAR',  N'PROX', 81, NULL, 3089, NULL, NULL,  N'PROX', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3094,  N'VARCHAR',  N'Species', 85, NULL, 3089, NULL, NULL,  N'Species', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3102,  N'VARCHAR',  N'Gnawed', 76, NULL, 3089, NULL, NULL,  N'Gnawed', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3158,  N'VARCHAR',  N'M2WA', NULL, NULL, 3089, NULL, NULL,  N'M2WA', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3159,  N'VARCHAR',  N'M2WP', NULL, NULL, 3089, NULL, NULL,  N'M2WP', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3150,  N'VARCHAR',  N'P3', NULL, NULL, 3089, NULL, NULL,  N'P3', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3163,  N'VARCHAR',  N'M3L', NULL, NULL, 3089, NULL, NULL,  N'M3L', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3106,  N'VARCHAR',  N'GL', NULL, NULL, 3089, NULL, NULL,  N'GL', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3160,  N'VARCHAR',  N'M2L', NULL, NULL, 3089, NULL, NULL,  N'M2L', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3076,  N'VARCHAR',  N'AREA', 198, NULL, 3075, NULL, NULL,  N'AREA', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3083,  N'VARCHAR',  N'SD', 6, NULL, 3075, NULL, NULL,  N'SD', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3117,  N'VARCHAR',  N'BFdl', NULL, NULL, 3089, NULL, NULL,  N'BFdl', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3164,  N'VARCHAR',  N'M3Wa', NULL, NULL, 3089, NULL, NULL,  N'M3Wa', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3080,  N'VARCHAR',  N'BD', NULL, NULL, 3075, NULL, NULL,  N'BD', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3077,  N'VARCHAR',  N'BOX', 203, NULL, 3075, NULL, NULL,  N'BOX', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3086,  N'VARCHAR',  N'COMMENTS', NULL, NULL, 3075, NULL, NULL,  N'COMMENTS', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3082,  N'VARCHAR',  N'HTC', 6, NULL, 3075, NULL, NULL,  N'HTC', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3078,  N'VARCHAR',  N'N', 69, NULL, 3075, NULL, NULL,  N'N', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3079,  N'VARCHAR',  N'SIDE', 83, NULL, 3075, NULL, NULL,  N'SIDE', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3124,  N'VARCHAR',  N'B@F', NULL, NULL, 3089, NULL, NULL,  N'B@F', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3116,  N'VARCHAR',  N'BFdm', NULL, NULL, 3089, NULL, NULL,  N'BFdm', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3110,  N'VARCHAR',  N'Bp', NULL, NULL, 3089, NULL, NULL,  N'Bp', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3125,  N'VARCHAR',  N'CAc', NULL, NULL, 3089, NULL, NULL,  N'CAc', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3119,  N'VARCHAR',  N'Ddl', NULL, NULL, 3089, NULL, NULL,  N'Ddl', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3120,  N'VARCHAR',  N'DtL', NULL, NULL, 3089, NULL, NULL,  N'DtL', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3112,  N'VARCHAR',  N'Bd', NULL, NULL, 3089, NULL, NULL,  N'Bd', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3131,  N'VARCHAR',  N'C', NULL, NULL, 3089, NULL, NULL,  N'C', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3098,  N'VARCHAR',  N'DIST', NULL, NULL, 3089, NULL, NULL,  N'DIST', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3122,  N'VARCHAR',  N'Dm', NULL, NULL, 3089, NULL, NULL,  N'Dm', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3113,  N'VARCHAR',  N'GB', NULL, NULL, 3089, NULL, NULL,  N'GB', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3108,  N'VARCHAR',  N'GLC', NULL, NULL, 3089, NULL, NULL,  N'GLC', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3137,  N'VARCHAR',  N'I', NULL, NULL, 3089, NULL, NULL,  N'I', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3140,  N'VARCHAR',  N'I3', NULL, NULL, 3089, NULL, NULL,  N'I3', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3104,  N'VARCHAR',  N'Measurable', NULL, NULL, 3089, NULL, NULL,  N'Measurable', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3148,  N'VARCHAR',  N'P1', NULL, NULL, 3089, NULL, NULL,  N'P1', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3111,  N'VARCHAR',  N'SD', NULL, NULL, 3089, NULL, NULL,  N'SD', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3090,  N'VARCHAR',  N'STAGE', 214, NULL, 3089, NULL, NULL,  N'STAGE', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3134,  N'VARCHAR',  N'di1', NULL, NULL, 3089, NULL, NULL,  N'di1', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3141,  N'VARCHAR',  N'dp', NULL, NULL, 3089, NULL, NULL,  N'dp', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3144,  N'VARCHAR',  N'dp4', NULL, NULL, 3089, NULL, NULL,  N'dp4', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3165,  N'VARCHAR',  N'M3Wc', NULL, NULL, 3089, NULL, NULL,  N'M3Wc', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3107,  N'VARCHAR',  N'GLl', NULL, NULL, 3089, NULL, NULL,  N'GLl', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3123,  N'VARCHAR',  N'BFd', NULL, NULL, 3089, NULL, NULL,  N'BFd', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3114,  N'VARCHAR',  N'BT', NULL, NULL, 3089, NULL, NULL,  N'BT', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3103,  N'VARCHAR',  N'Butchery', NULL, NULL, 3089, NULL, NULL,  N'Butchery', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3126,  N'VARCHAR',  N'CAc+d', NULL, NULL, 3089, NULL, NULL,  N'CAc+d', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3118,  N'VARCHAR',  N'Ddm', NULL, NULL, 3089, NULL, NULL,  N'Ddm', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3121,  N'VARCHAR',  N'DtM', NULL, NULL, 3089, NULL, NULL,  N'DtM', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3115,  N'VARCHAR',  N'HTC', NULL, NULL, 3089, NULL, NULL,  N'HTC', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3139,  N'VARCHAR',  N'I2', NULL, NULL, 3089, NULL, NULL,  N'I2', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3152,  N'VARCHAR',  N'M', NULL, NULL, 3089, NULL, NULL,  N'M', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3153,  N'VARCHAR',  N'M1', NULL, NULL, 3089, NULL, NULL,  N'M1', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3161,  N'VARCHAR',  N'M12', NULL, NULL, 3089, NULL, NULL,  N'M12', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3156,  N'VARCHAR',  N'M1L', NULL, NULL, 3089, NULL, NULL,  N'M1L', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3154,  N'VARCHAR',  N'M1WA', NULL, NULL, 3089, NULL, NULL,  N'M1WA', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3155,  N'VARCHAR',  N'M1WP', NULL, NULL, 3089, NULL, NULL,  N'M1WP', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3162,  N'VARCHAR',  N'M3', NULL, NULL, 3089, NULL, NULL,  N'M3', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3151,  N'VARCHAR',  N'P4', NULL, NULL, 3089, NULL, NULL,  N'P4', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3166,  N'VARCHAR',  N'PM', NULL, NULL, 3089, NULL, NULL,  N'PM', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3128,  N'VARCHAR',  N'SLC', NULL, NULL, 3089, NULL, NULL,  N'SLC', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3093,  N'VARCHAR',  N'FEATURE INFO', 20, NULL, 3089, NULL, NULL,  N'FEATURE INFO', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3109,  N'VARCHAR',  N'GH', NULL, NULL, 3089, NULL, NULL,  N'GH', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3127,  N'VARCHAR',  N'GLP', NULL, NULL, 3089, NULL, NULL,  N'GLP', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3095,  N'VARCHAR',  N'Element', 73, NULL, 3089, NULL, NULL,  N'Element', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3092,  N'VARCHAR',  N'FEATURE', 198, NULL, 3089, NULL, NULL,  N'FEATURE', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3099,  N'VARCHAR',  N'Other', NULL, NULL, 3089, NULL, NULL,  N'Other', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3149,  N'VARCHAR',  N'P2', NULL, NULL, 3089, NULL, NULL,  N'P2', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3096,  N'VARCHAR',  N'SIDE', NULL, NULL, 3089, NULL, NULL,  N'SIDE', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3105,  N'VARCHAR',  N'Toothwear', NULL, NULL, 3089, NULL, NULL,  N'Toothwear', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3132,  N'VARCHAR',  N'dc', NULL, NULL, 3089, NULL, NULL,  N'dc', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3135,  N'VARCHAR',  N'di2', NULL, NULL, 3089, NULL, NULL,  N'di2', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3142,  N'VARCHAR',  N'dp2', NULL, NULL, 3089, NULL, NULL,  N'dp2', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3145,  N'VARCHAR',  N'dp4L', NULL, NULL, 3089, NULL, NULL,  N'dp4L', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3133,  N'VARCHAR',  N'di', NULL, NULL, 3089, NULL, NULL,  N'di', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3136,  N'VARCHAR',  N'di3', NULL, NULL, 3089, NULL, NULL,  N'di3', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3143,  N'VARCHAR',  N'dp3', NULL, NULL, 3089, NULL, NULL,  N'dp3', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
INSERT INTO data_table_column (id, column_data_type, name, category_variable_id, default_ontology_id, data_table_id, default_coding_sheet_id, description, display_name, measurement_unit, column_encoding_type, sequence_number, delimitervalue, ignorefileextension, visible, mappingcolumn) VALUES (3146,  N'VARCHAR',  N'dp4W', NULL, NULL, 3089, NULL, NULL,  N'dp4W', NULL,  N'UNCODED_VALUE', NULL, NULL,  true ,  true ,  false );
--------------------------- document ---------------------------
INSERT INTO document (document_type, edition, isbn, number_of_pages, number_of_volumes, series_name, series_number, volume, journal_name, journal_number, issn, book_title, id, start_page, end_page, degree) VALUES ( N'BOOK', NULL, NULL, 257, NULL, NULL,  N'50', NULL, NULL, NULL, NULL, NULL, 4287, NULL, NULL, NULL);
INSERT INTO document (document_type, edition, isbn, number_of_pages, number_of_volumes, series_name, series_number, volume, journal_name, journal_number, issn, book_title, id, start_page, end_page, degree) VALUES ( N'OTHER', NULL, NULL, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3794, NULL, NULL, NULL);
INSERT INTO document (document_type, edition, isbn, number_of_pages, number_of_volumes, series_name, series_number, volume, journal_name, journal_number, issn, book_title, id, start_page, end_page, degree) VALUES ( N'OTHER', NULL, NULL, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 4230, NULL, NULL, NULL);
INSERT INTO document (document_type, edition, isbn, number_of_pages, number_of_volumes, series_name, series_number, volume, journal_name, journal_number, issn, book_title, id, start_page, end_page, degree) VALUES ( N'OTHER', NULL, NULL, 22, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 4231, NULL, NULL, NULL);
INSERT INTO document (document_type, edition, isbn, number_of_pages, number_of_volumes, series_name, series_number, volume, journal_name, journal_number, issn, book_title, id, start_page, end_page, degree) VALUES ( N'OTHER', NULL, NULL, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 4232, NULL, NULL, NULL);
--------------------------- information_resource_related_citation ---------------------------
--------------------------- information_resource_source_citation ---------------------------
--------------------------- resource_culture_keyword ---------------------------
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (2420, 19);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (2420, 39);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (2420, 40);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (1628, 40);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (1628, 39);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (1628, 19);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (3805, 8);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (3805, 25);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (262, 39);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (262, 19);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (262, 40);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (3738, 25);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (4287, 19);
INSERT INTO resource_culture_keyword (resource_id, culture_keyword_id) VALUES (4287, 52);
--------------------------- resource_geographic_keyword ---------------------------
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (1628, 8);
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (1628, 53);
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (1628, 46);
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (2420, 8);
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (2420, 11);
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (2420, 53);
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3805, 58);
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (262, 8);
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (262, 53);
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3738, 57);
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (4287, 2);
INSERT INTO resource_geographic_keyword (resource_id, geographic_keyword_id) VALUES (4287, 98);
--------------------------- resource_managed_geographic_keyword ---------------------------
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (4287, 276);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (4287, 327);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (4287, 335);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (4287, 384);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3805, 276);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3805, 384);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3805, 927);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3805, 1539);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (262, 247);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (262, 276);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (262, 299);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (262, 335);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (262, 384);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (262, 408);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (1628, 255);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (1628, 276);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (1628, 299);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (1628, 384);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2545);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2546);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2547);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2548);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2549);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2550);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2551);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2552);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2553);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2554);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2555);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2556);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2557);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2558);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2559);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2560);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2561);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2562);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2563);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2564);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2565);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2566);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2567);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2568);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2569);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3074, 2570);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3088, 2548);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3088, 2553);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3088, 2554);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3088, 2555);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3088, 2557);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3088, 2558);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3088, 2559);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3088, 2560);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3088, 2561);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3088, 2562);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3088, 2564);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (2420, 247);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (2420, 255);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (2420, 276);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (2420, 299);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (2420, 384);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3738, 276);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3738, 384);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3738, 1474);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3738, 1683);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3738, 1906);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3738, 2040);
INSERT INTO resource_managed_geographic_keyword (resource_id, geographic_keyword_id) VALUES (3738, 2092);
--------------------------- resource_investigation_type ---------------------------
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (2420, 12);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (2420, 5);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (1628, 12);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (1628, 5);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (3805, 1);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (3805, 9);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (3805, 6);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (3805, 7);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (3805, 5);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (3805, 12);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (3805, 10);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (262, 12);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (262, 7);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (262, 5);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (3738, 5);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (4287, 2);
INSERT INTO resource_investigation_type (resource_id, investigation_type_id) VALUES (4287, 12);
--------------------------- resource_material_keyword ---------------------------
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (139, 4);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3074, 4);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (2420, 7);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (2420, 2);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (2420, 4);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (2420, 13);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (2420, 1);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (2420, 10);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 2);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 15);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 11);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 3);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 1);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 6);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 8);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 4);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 14);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 10);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 13);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3805, 12);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (262, 13);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (262, 1);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (262, 7);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (262, 3);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (262, 12);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (262, 15);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (262, 4);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (262, 2);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (3738, 4);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (4287, 2);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (4287, 1);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (4287, 6);
INSERT INTO resource_material_keyword (resource_id, material_keyword_id) VALUES (4287, 11);
--------------------------- resource_other_keyword ---------------------------
INSERT INTO resource_other_keyword (resource_id, other_keyword_id) VALUES (1628, 49);
INSERT INTO resource_other_keyword (resource_id, other_keyword_id) VALUES (1628, 50);
INSERT INTO resource_other_keyword (resource_id, other_keyword_id) VALUES (2420, 7);
INSERT INTO resource_other_keyword (resource_id, other_keyword_id) VALUES (262, 51);
--------------------------- resource_revision_log ---------------------------
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (318,  '2010-02-17 11:14:59.821', 3029,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (13,  '2009-05-16 22:39:59.235', 1628,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (27,  '2009-05-26 15:49:51.64', 1628,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (32,  '2009-05-26 21:38:53.989', 262,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (33,  '2009-05-26 21:39:25.661', 1628,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (35,  '2009-05-26 21:58:49.605', 2420,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (57,  '2009-07-23 09:07:59.137', 1628,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (76,  '2009-09-16 07:41:15.457', 2420,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (136,  '2009-11-10 12:53:57.314', 1656,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (160,  '2009-11-10 13:48:37.488', 449,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (161,  '2009-11-10 13:49:54.814', 449,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (162,  '2009-11-10 13:50:48.607', 449,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (361,  '2010-03-05 12:59:59.488', 3074,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (365,  '2010-03-05 13:18:37.057', 3088,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (368,  '2010-03-05 16:05:36.181', 3088,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (198,  '2009-11-10 18:21:34.629', 1656,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (204,  '2009-11-10 18:26:40.522', 1656,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (262,  '2009-11-11 09:35:06.61', 1628,  N'Resource edited and saved by Kintigh, Keith (Arizona State University)', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (460,  '2010-04-07 00:52:32.942', 1656,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (569,  '2010-05-04 21:24:57.48', 1628,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (577,  '2010-05-04 21:51:11.966', 1656,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (578,  '2010-05-04 21:51:31.645', 1656,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (656,  '2010-07-09 12:27:25.229', 3029,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (696,  '2010-07-09 15:35:06.287', 3479,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (717,  '2010-07-14 16:13:18.378', 449,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (730,  '2010-07-14 21:00:51.492', 2420,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (732,  '2010-07-14 21:02:39.728', 2420,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (734,  '2010-07-14 21:05:02.711', 1628,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (735,  '2010-07-14 21:06:40.364', 2420,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (737,  '2010-07-14 21:08:52.574', 1628,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (738,  '2010-07-14 21:10:00.569', 1628,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (739,  '2010-07-14 21:10:45.55', 1628,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (741,  '2010-07-14 21:18:56.658', 262,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (742,  '2010-07-14 21:21:03.557', 262,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (817,  '2010-07-18 22:32:06.803', 3479,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (818,  '2010-07-18 22:32:33.554', 3479,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (914,  '2010-07-29 13:23:45.695', 3805,  N'Resource edited and saved by Fennell, Christopher [U. Illinois]', 8422, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (892,  '2010-07-22 13:40:50.138', 3738,  N'Resource edited and saved by Spielmann, Katherine [Arizona State University]', 8161, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (894,  '2010-07-22 13:46:12.314', 3738,  N'Resource edited and saved by Spielmann, Katherine [Arizona State University]', 8161, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (897,  '2010-07-22 13:54:06.955', 3738,  N'Resource edited and saved by Spielmann, Katherine [Arizona State University]', 8161, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (898,  '2010-07-22 14:03:57.746', 3794,  N'Resource edited and saved by Spielmann, Katherine [Arizona State University]', 8161, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (991,  '2010-08-05 19:19:41.863', 3805,  N'Resource edited and saved by Fennell, Christopher [U. Illinois]', 8422, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (1042,  '2010-08-12 11:07:31.293', 1628,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (1246,  '2010-08-14 11:54:03.755', 4230,  N'Resource edited and saved by Fennell, Christopher [U. Illinois]', 8422, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (1247,  '2010-08-14 11:55:21.016', 4231,  N'Resource edited and saved by Fennell, Christopher [U. Illinois]', 8422, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (1248,  '2010-08-14 11:57:13.163', 4232,  N'Resource edited and saved by Fennell, Christopher [U. Illinois]', 8422, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (1260,  '2010-08-17 11:03:21.922', 3805,  N'Resource edited and saved by Fennell, Christopher [U. Illinois]', 8422, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (1262,  '2010-08-18 11:05:27.894', 3805,  N'Resource edited and saved by Fennell, Christopher [U. Illinois]', 8422, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (1325,  '2010-09-02 13:05:13.877', 4287,  N'Resource edited and saved by Watts, Joshua [ASU - SHESC]', 5349, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (1406,  '2010-09-14 10:32:51.429', 3738,  N'Resource edited and saved by Spielmann, Katherine [Arizona State University]', 8161, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (1479,  '2010-09-24 09:40:30.748', 4287,  N'Resource edited and saved by Watts, Joshua [ASU - SHESC]', 5349, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (1704,  '2010-10-18 12:25:01.099', 262,  N'Resource edited and saved by Kintigh, Keith [Arizona State University]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (1917,  '2010-11-19 08:50:30.827', 3088,  N'Resource edited and saved by DeVos, jim [Arizona State University]', 8608, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (2501,  '2011-01-24 15:19:27.444', 3479,  N'Ontology edited and saved by Kintigh, Keith [Arizona State University]:	tdar id:3479	title:[(Outdated) TAG Fauna Ontology - Taxon]', 6, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (2526,  '2011-01-24 16:49:34.552', 3074,  N'data column metadata registration - no translation', 8019,  N'<list>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>AREA</name>
    <columnDataType>text</columnDataType>
    <categoryVariable>
      <label>Locus</label>
      <type>SUBCATEGORY</type>
      <parent>
        <label>Horizontal_Location</label>
        <type>SUBCATEGORY</type>
        <parent>
          <label>Provenience_and_Context</label>
          <type>CATEGORY</type>
          <parent reference=".."/>
        </parent>
      </parent>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>BD</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>BOX</name>
    <columnDataType>text</columnDataType>
    <categoryVariable>
      <label>Lot</label>
      <type>SUBCATEGORY</type>
      <parent reference="../../../org.tdar.core.bean.resource.DataTableColumn/categoryVariable/parent/parent"/>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>BT</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>COMMENTS</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>FUSD</name>
    <columnDataType>text</columnDataType>
    <categoryVariable>
      <label>Fusion</label>
      <type>SUBCATEGORY</type>
      <parent>
        <label>Fauna</label>
        <type>CATEGORY</type>
        <parent reference=".."/>
      </parent>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>FUSP</name>
    <columnDataType>text</columnDataType>
    <categoryVariable reference="../../org.tdar.core.bean.resource.DataTableColumn[6]/categoryVariable"/>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>HTC</name>
    <columnDataType>text</columnDataType>
    <categoryVariable reference="../../org.tdar.core.bean.resource.DataTableColumn[6]/categoryVariable/parent"/>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>N</name>
    <columnDataType>text</columnDataType>
    <categoryVariable>
      <label>Count</label>
      <type>SUBCATEGORY</type>
      <parent reference="../../../org.tdar.core.bean.resource.DataTableColumn[6]/categoryVariable/parent"/>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>SD</name>
    <columnDataType>text</columnDataType>
    <categoryVariable reference="../../org.tdar.core.bean.resource.DataTableColumn[6]/categoryVariable/parent"/>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>SIDE</name>
    <columnDataType>text</columnDataType>
    <categoryVariable>
      <label>Side</label>
      <type>SUBCATEGORY</type>
      <parent reference="../../../org.tdar.core.bean.resource.DataTableColumn[6]/categoryVariable/parent"/>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
</list>');
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (2527,  '2011-01-24 16:53:45.93', 3088,  N'data column metadata registration - translated', 8019,  N'<list>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>B@F</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>BFd</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>BFdl</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>BFdm</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>BT</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Bd</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Bp</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Butchery</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>C</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>CAc</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>CAc+d</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>DIST</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Ddl</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Ddm</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Dm</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>DtL</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>DtM</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Element</name>
    <columnDataType>text</columnDataType>
    <columnEncodingType>
      <name>Arbitrary integer</name>
    </columnEncodingType>
    <categoryVariable>
      <label>Element</label>
      <type>SUBCATEGORY</type>
      <parent>
        <label>Fauna</label>
        <type>CATEGORY</type>
        <parent reference=".."/>
      </parent>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>FEATURE</name>
    <columnDataType>text</columnDataType>
    <columnEncodingType>
      <name>Arbitrary string</name>
    </columnEncodingType>
    <categoryVariable>
      <label>Feature</label>
      <type>SUBCATEGORY</type>
      <parent>
        <label>Horizontal_Location</label>
        <type>SUBCATEGORY</type>
        <parent>
          <label>Provenience_and_Context</label>
          <type>CATEGORY</type>
          <parent reference=".."/>
        </parent>
      </parent>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>FEATURE INFO</name>
    <columnDataType>text</columnDataType>
    <categoryVariable reference="../../org.tdar.core.bean.resource.DataTableColumn[19]/categoryVariable/parent/parent"/>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>FUSD</name>
    <columnDataType>text</columnDataType>
    <columnEncodingType>
      <name>Coded string</name>
    </columnEncodingType>
    <categoryVariable>
      <label>Fusion</label>
      <type>SUBCATEGORY</type>
      <parent reference="../../../org.tdar.core.bean.resource.DataTableColumn[18]/categoryVariable/parent"/>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>FUSP</name>
    <columnDataType>text</columnDataType>
    <columnEncodingType reference="../../org.tdar.core.bean.resource.DataTableColumn[21]/columnEncodingType"/>
    <categoryVariable reference="../../org.tdar.core.bean.resource.DataTableColumn[21]/categoryVariable"/>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>GB</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>GH</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>GL</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>GLC</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>GLP</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>GLl</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Gnawed</name>
    <columnDataType>text</columnDataType>
    <columnEncodingType>
      <name>Coded integer</name>
    </columnEncodingType>
    <categoryVariable>
      <label>Gnawing</label>
      <type>SUBCATEGORY</type>
      <parent>
        <label>Natural_Modification</label>
        <type>SUBCATEGORY</type>
        <parent reference="../../../../org.tdar.core.bean.resource.DataTableColumn[19]/categoryVariable/parent/parent"/>
      </parent>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>HTC</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>I</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>I1</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>I2</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>I3</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M1</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M12</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M1L</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M1WA</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M1WP</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M2</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M2L</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M2WA</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M2WP</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M3</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M3L</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M3Wa</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>M3Wc</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Measurable</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Other</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>P</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>P1</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>P2</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>P3</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>P4</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>PM</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>PROX</name>
    <columnDataType>text</columnDataType>
    <columnEncodingType reference="../../org.tdar.core.bean.resource.DataTableColumn[19]/columnEncodingType"/>
    <categoryVariable>
      <label>Proximal_Distal</label>
      <type>SUBCATEGORY</type>
      <parent reference="../../../org.tdar.core.bean.resource.DataTableColumn[18]/categoryVariable/parent"/>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>REC ID</name>
    <columnDataType>text</columnDataType>
    <columnEncodingType reference="../../org.tdar.core.bean.resource.DataTableColumn[18]/columnEncodingType"/>
    <categoryVariable>
      <label>Slash</label>
      <type>SUBCATEGORY</type>
      <parent reference="../../../org.tdar.core.bean.resource.DataTableColumn[19]/categoryVariable/parent"/>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>SD</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>SIDE</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>SLC</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>STAGE</name>
    <columnDataType>text</columnDataType>
    <categoryVariable>
      <label>Stratum</label>
      <type>SUBCATEGORY</type>
      <parent>
        <label>Vertical_Position</label>
        <type>SUBCATEGORY</type>
        <parent reference="../../../../org.tdar.core.bean.resource.DataTableColumn[19]/categoryVariable/parent/parent"/>
      </parent>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Species</name>
    <columnDataType>text</columnDataType>
    <columnEncodingType reference="../../org.tdar.core.bean.resource.DataTableColumn[19]/columnEncodingType"/>
    <categoryVariable>
      <label>Taxon</label>
      <type>SUBCATEGORY</type>
      <parent reference="../../../org.tdar.core.bean.resource.DataTableColumn[18]/categoryVariable/parent"/>
    </categoryVariable>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Toothwear</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Wmax</name>
    <columnDataType>text</columnDataType>
    <columnEncodingType>
      <name>Measurement</name>
    </columnEncodingType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>Wmin</name>
    <columnDataType>text</columnDataType>
    <columnEncodingType reference="../../org.tdar.core.bean.resource.DataTableColumn[65]/columnEncodingType"/>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>dc</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>di</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>di1</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>di2</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>di3</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>dp</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>dp2</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>dp3</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>dp4</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>dp4L</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
  <org.tdar.core.bean.resource.DataTableColumn>
    <name>dp4W</name>
    <columnDataType>text</columnDataType>
  </org.tdar.core.bean.resource.DataTableColumn>
</list>');
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (3315,  '2011-04-07 16:46:53.957', 4287,  N'Document edited and saved by brin, adam [Digital Antiquity]:	tdar id:4287	title:[Archeological Survey and Architectural Study of Montezuma Castle National Monument]', 8344, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (3352,  '2011-04-13 17:22:14.906', 3738,  N'Project edited and saved by Manney, Shelby [ASU]:	tdar id:3738	title:[Alexandria, Virginia Historic Period Fauna]', 8019, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (3608,  '2011-04-22 13:03:15.205', 3479,  N'Ontology edited and saved by Manney, Shelby [ASU]:	tdar id:3479	title:[(Outdated) TAG Fauna Ontology - Taxon]', 8019, NULL);
INSERT INTO resource_revision_log (id, timestamp, resource_id, log_message, person_id, payload) VALUES (4478,  '2011-06-04 21:41:22.241', 4287,  N'Document edited and saved by brin, adam [adam.brin@asu.edu | Digital Antiquity]:	tdar id:4287	title:[Archeological Survey and Architectural Study of Montezuma Castle National Monument]', 8344, NULL);
--------------------------- resource_site_name_keyword ---------------------------
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (3074, 13);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (139, 76);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (2420, 50);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (2420, 71);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (2420, 95);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (2420, 79);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (2420, 4);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (2420, 12);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (2420, 58);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (1628, 91);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (1628, 24);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (3805, 113);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (4287, 200);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (4287, 201);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (262, 29);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (262, 34);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (262, 27);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (262, 64);
INSERT INTO resource_site_name_keyword (resource_id, site_name_keyword_id) VALUES (3088, 38);
--------------------------- resource_site_type_keyword ---------------------------
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (2420, 252);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (2420, 256);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (2420, 251);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (2420, 255);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (2420, 264);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (1628, 256);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (1628, 254);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (1628, 264);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (1628, 252);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (1628, 251);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (1628, 255);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (1628, 321);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (3805, 251);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (3805, 309);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (3805, 339);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (262, 264);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (262, 317);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (262, 256);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (262, 263);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (262, 252);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (262, 253);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (262, 254);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (262, 265);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (262, 321);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (262, 251);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (262, 255);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (3738, 251);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (4287, 301);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (4287, 251);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (4287, 252);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (4287, 339);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (4287, 305);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (4287, 344);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (4287, 272);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (4287, 256);
INSERT INTO resource_site_type_keyword (resource_id, site_type_keyword_id) VALUES (4287, 345);
--------------------------- resource_temporal_keyword ---------------------------
INSERT INTO resource_temporal_keyword (resource_id, temporal_keyword_id) VALUES (2420, 56);
INSERT INTO resource_temporal_keyword (resource_id, temporal_keyword_id) VALUES (1628, 57);
INSERT INTO resource_temporal_keyword (resource_id, temporal_keyword_id) VALUES (3805, 82);
INSERT INTO resource_temporal_keyword (resource_id, temporal_keyword_id) VALUES (262, 55);
INSERT INTO resource_temporal_keyword (resource_id, temporal_keyword_id) VALUES (262, 54);
INSERT INTO resource_temporal_keyword (resource_id, temporal_keyword_id) VALUES (262, 78);
INSERT INTO resource_temporal_keyword (resource_id, temporal_keyword_id) VALUES (262, 56);
INSERT INTO resource_temporal_keyword (resource_id, temporal_keyword_id) VALUES (262, 57);
INSERT INTO resource_temporal_keyword (resource_id, temporal_keyword_id) VALUES (4287, 57);
INSERT INTO resource_temporal_keyword (resource_id, temporal_keyword_id) VALUES (3738, 81);
--------------------------- source_collection ---------------------------
--------------------------- related_comparative_collection ---------------------------
--------------------------- ontology_node ---------------------------
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4141, 3479, 97, 97,  N'Asio_otus_(Long-eared_owl)',  N'http://www.tdar.org/ontologies/3479#Asio_otus_(Long-eared_owl)',  N'1.2.96.97', NULL,  N'Asio otus (Long-eared owl)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4053, 3479, 83, 88,  N'Rallidae',  N'http://www.tdar.org/ontologies/3479#Rallidae',  N'1.2.83', NULL,  N'Rallidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4128, 3479, 21, 28,  N'Anserinae',  N'http://www.tdar.org/ontologies/3479#Anserinae',  N'1.2.13.21', NULL,  N'Anserinae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4124, 3479, 89, 95,  N'Scolopacidae_(Scolopacid)',  N'http://www.tdar.org/ontologies/3479#Scolopacidae_(Scolopacid)',  N'1.2.89', NULL,  N'Scolopacidae (Scolopacid)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4125, 3479, 193, 193,  N'Ursus_arctos_(Brown_bear)',  N'http://www.tdar.org/ontologies/3479#Ursus_arctos_(Brown_bear)',  N'146.166.193', NULL,  N'Ursus arctos (Brown bear)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4126, 3479, 91, 91,  N'Limosa_sp._(Godwit)',  N'http://www.tdar.org/ontologies/3479#Limosa_sp._(Godwit)',  N'1.2.89.91', NULL,  N'Limosa sp. (Godwit)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4127, 3479, 185, 185,  N'Martes_martes_(Pine_martin)',  N'http://www.tdar.org/ontologies/3479#Martes_martes_(Pine_martin)',  N'146.166.177.184.185', NULL,  N'Martes martes (Pine martin)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4129, 3479, 3, 8,  N'Accipitridae',  N'http://www.tdar.org/ontologies/3479#Accipitridae',  N'1.2.3', NULL,  N'Accipitridae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3993, 3479, 90, 90,  N'Gallinago_gallinago_(Snipe)',  N'http://www.tdar.org/ontologies/3479#Gallinago_gallinago_(Snipe)',  N'1.2.89.90', NULL,  N'Gallinago gallinago (Snipe)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4130, 3479, 153, 153,  N'Cervus_elaphus_or_Dama_dama_(Red_deer_or_Fallow_deer)',  N'http://www.tdar.org/ontologies/3479#Cervus_elaphus_or_Dama_dama_(Red_deer_or_Fallow_deer)',  N'146.147.153', NULL,  N'Cervus elaphus or Dama dama (Red deer or Fallow deer)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4131, 3479, 120, 120,  N'small_corvid',  N'http://www.tdar.org/ontologies/3479#small_corvid',  N'1.109.113.120', NULL,  N'small corvid', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4132, 3479, 166, 194,  N'Carnivora',  N'http://www.tdar.org/ontologies/3479#Carnivora',  N'146.166', NULL,  N'Carnivora', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4133, 3479, 119, 119,  N'large_corvid',  N'http://www.tdar.org/ontologies/3479#large_corvid',  N'1.109.113.119', NULL,  N'large corvid', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4134, 3479, 181, 181,  N'Mustela_erminea_(Stoat)',  N'http://www.tdar.org/ontologies/3479#Mustela_erminea_(Stoat)',  N'146.166.177.180.181', NULL,  N'Mustela erminea (Stoat)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4135, 3479, 184, 187,  N'Mustela_putorius_or_Martes_martes_(Polecat_or_Pine_martin)',  N'http://www.tdar.org/ontologies/3479#Mustela_putorius_or_Martes_martes_(Polecat_or_Pine_martin)',  N'146.166.177.184', NULL,  N'Mustela putorius or Martes martes (Polecat or Pine martin)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4136, 3479, 54, 56,  N'Gaviidae',  N'http://www.tdar.org/ontologies/3479#Gaviidae',  N'1.2.54', NULL,  N'Gaviidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4137, 3479, 92, 92,  N'Lymnocryptes_minimus_(Jack_snipe)',  N'http://www.tdar.org/ontologies/3479#Lymnocryptes_minimus_(Jack_snipe)',  N'1.2.89.92', NULL,  N'Lymnocryptes minimus (Jack snipe)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4138, 3479, 211, 211,  N'Talpa_europaea_(Common_mole)',  N'http://www.tdar.org/ontologies/3479#Talpa_europaea_(Common_mole)',  N'146.204.210.211', NULL,  N'Talpa europaea (Common mole)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4139, 3479, 189, 192,  N'Phoca_vitulina_or_Halichoerus_grypus_(Common_or_Grey_seal)',  N'http://www.tdar.org/ontologies/3479#Phoca_vitulina_or_Halichoerus_grypus_(Common_or_Grey_seal)',  N'146.166.189', NULL,  N'Phoca vitulina or Halichoerus grypus (Common or Grey seal)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4140, 3479, 2, 108,  N'Non-passerine_families',  N'http://www.tdar.org/ontologies/3479#Non-passerine_families',  N'1.2', NULL,  N'Non-passerine families', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3968, 3479, 106, 106,  N'Tyto_alba_(Barn_owl)',  N'http://www.tdar.org/ontologies/3479#Tyto_alba_(Barn_owl)',  N'1.2.105.106', NULL,  N'Tyto alba (Barn owl)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3969, 3479, 123, 123,  N'Hirundo_rustico_(Swallow)',  N'http://www.tdar.org/ontologies/3479#Hirundo_rustico_(Swallow)',  N'1.109.122.123', NULL,  N'Hirundo rustico (Swallow)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3970, 3479, 18, 18,  N'Anas_platyrhynchos_(Mallard_or_Domestic_duck)',  N'http://www.tdar.org/ontologies/3479#Anas_platyrhynchos_(Mallard_or_Domestic_duck)',  N'1.2.13.14.18', NULL,  N'Anas platyrhynchos (Mallard or Domestic duck)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3971, 3479, 168, 172,  N'Canis_familiaris_or_lupus_(Dog_or_Wolf)',  N'http://www.tdar.org/ontologies/3479#Canis_familiaris_or_lupus_(Dog_or_Wolf)',  N'146.166.168', NULL,  N'Canis familiaris or lupus (Dog or Wolf)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3972, 3479, 148, 148,  N'Bos_primigenius_(Auroch)',  N'http://www.tdar.org/ontologies/3479#Bos_primigenius_(Auroch)',  N'146.147.148', NULL,  N'Bos primigenius (Auroch)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3973, 3479, 70, 70,  N'Coturnix_coturnix_(Quail)',  N'http://www.tdar.org/ontologies/3479#Coturnix_coturnix_(Quail)',  N'1.2.69.70', NULL,  N'Coturnix coturnix (Quail)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3974, 3479, 62, 62,  N'large_gull',  N'http://www.tdar.org/ontologies/3479#large_gull',  N'1.2.60.62', NULL,  N'large gull', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3975, 3479, 240, 240,  N'Clethrionomys_glareolus_(Bank_vole)',  N'http://www.tdar.org/ontologies/3479#Clethrionomys_glareolus_(Bank_vole)',  N'146.226.238.240', NULL,  N'Clethrionomys glareolus (Bank vole)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3976, 3479, 75, 75,  N'Lagopus_lagopus_(Willow_or_Red_grouse)',  N'http://www.tdar.org/ontologies/3479#Lagopus_lagopus_(Willow_or_Red_grouse)',  N'1.2.69.75', NULL,  N'Lagopus lagopus (Willow or Red grouse)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3977, 3479, 100, 100,  N'Morus_bassanus_(Northern_Gannet)',  N'http://www.tdar.org/ontologies/3479#Morus_bassanus_(Northern_Gannet)',  N'1.2.99.100', NULL,  N'Morus bassanus (Northern Gannet)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3978, 3479, 29, 29,  N'Aythyini_(Aythyin)',  N'http://www.tdar.org/ontologies/3479#Aythyini_(Aythyin)',  N'1.2.13.29', NULL,  N'Aythyini (Aythyin)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3979, 3479, 135, 135,  N'Prunella_modularis_(Dunnock)',  N'http://www.tdar.org/ontologies/3479#Prunella_modularis_(Dunnock)',  N'1.109.134.135', NULL,  N'Prunella modularis (Dunnock)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3980, 3479, 134, 136,  N'Prunellidae',  N'http://www.tdar.org/ontologies/3479#Prunellidae',  N'1.109.134', NULL,  N'Prunellidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3981, 3479, 215, 217,  N'Lepus_sp._(Hare)',  N'http://www.tdar.org/ontologies/3479#Lepus_sp._(Hare)',  N'146.214.215', NULL,  N'Lepus sp. (Hare)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3982, 3479, 137, 139,  N'Turdidae',  N'http://www.tdar.org/ontologies/3479#Turdidae',  N'1.109.137', NULL,  N'Turdidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3983, 3479, 73, 73,  N'Gallus_gallus_or_Numida_meleagris_or_Phasianus_colchicus_(Chicken_or_Guinea_fowl_or_Pheasant)',  N'http://www.tdar.org/ontologies/3479#Gallus_gallus_or_Numida_meleagris_or_Phasianus_colchicus_(Chicken_or_Guinea_fowl_or_Pheasant)',  N'1.2.69.73', NULL,  N'Gallus gallus or Numida meleagris or Phasianus colchicus (Chicken or Guinea fowl or Pheasant)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3984, 3479, 227, 227,  N'Castor_fiber_(Beaver)',  N'http://www.tdar.org/ontologies/3479#Castor_fiber_(Beaver)',  N'146.226.227', NULL,  N'Castor fiber (Beaver)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3985, 3479, 235, 237,  N'Rattus_sp._(Rat)',  N'http://www.tdar.org/ontologies/3479#Rattus_sp._(Rat)',  N'146.226.235', NULL,  N'Rattus sp. (Rat)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3986, 3479, 163, 163,  N'Sus_scrofa_(Wild_boar)',  N'http://www.tdar.org/ontologies/3479#Sus_scrofa_(Wild_boar)',  N'146.147.161.163', NULL,  N'Sus scrofa (Wild boar)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3987, 3479, 201, 201,  N'Equus_asinus_(Donkey)',  N'http://www.tdar.org/ontologies/3479#Equus_asinus_(Donkey)',  N'146.200.201', NULL,  N'Equus asinus (Donkey)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3988, 3479, 48, 50,  N'Cuculidae',  N'http://www.tdar.org/ontologies/3479#Cuculidae',  N'1.2.48', NULL,  N'Cuculidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3989, 3479, 229, 233,  N'Murinae_(small)',  N'http://www.tdar.org/ontologies/3479#Murinae_(small)',  N'146.226.229', NULL,  N'Murinae (small)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3990, 3479, 171, 171,  N'Vulpes_vulpes_(Red_fox)',  N'http://www.tdar.org/ontologies/3479#Vulpes_vulpes_(Red_fox)',  N'146.166.168.171', NULL,  N'Vulpes vulpes (Red fox)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3991, 3479, 9, 12,  N'Alcidae',  N'http://www.tdar.org/ontologies/3479#Alcidae',  N'1.2.9', NULL,  N'Alcidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3992, 3479, 109, 141,  N'Passeriformes_(Passeriforme)',  N'http://www.tdar.org/ontologies/3479#Passeriformes_(Passeriforme)',  N'1.109', NULL,  N'Passeriformes (Passeriforme)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3994, 3479, 232, 232,  N'Mus_musculus_(House_mouse)',  N'http://www.tdar.org/ontologies/3479#Mus_musculus_(House_mouse)',  N'146.226.229.232', NULL,  N'Mus musculus (House mouse)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3995, 3479, 170, 170,  N'Canis_lupus_(Wolf)',  N'http://www.tdar.org/ontologies/3479#Canis_lupus_(Wolf)',  N'146.166.168.170', NULL,  N'Canis lupus (Wolf)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3996, 3479, 7, 7,  N'Milvus_milvus_(Red_kite)',  N'http://www.tdar.org/ontologies/3479#Milvus_milvus_(Red_kite)',  N'1.2.3.7', NULL,  N'Milvus milvus (Red kite)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3997, 3479, 169, 169,  N'Canis_familiaris_(Dog)',  N'http://www.tdar.org/ontologies/3479#Canis_familiaris_(Dog)',  N'146.166.168.169', NULL,  N'Canis familiaris (Dog)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3998, 3479, 151, 151,  N'Capreolus_capreolus_(Roe_deer)',  N'http://www.tdar.org/ontologies/3479#Capreolus_capreolus_(Roe_deer)',  N'146.147.151', NULL,  N'Capreolus capreolus (Roe deer)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (3999, 3479, 13, 32,  N'Anatidae_(Ducks)',  N'http://www.tdar.org/ontologies/3479#Anatidae_(Ducks)',  N'1.2.13', NULL,  N'Anatidae (Ducks)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4000, 3479, 178, 178,  N'Lutra_lutra_(Otter)',  N'http://www.tdar.org/ontologies/3479#Lutra_lutra_(Otter)',  N'146.166.177.178', NULL,  N'Lutra lutra (Otter)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4001, 3479, 41, 41,  N'Pluvialis_squatarola_(Grey_plover)',  N'http://www.tdar.org/ontologies/3479#Pluvialis_squatarola_(Grey_plover)',  N'1.2.37.41', NULL,  N'Pluvialis squatarola (Grey plover)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4002, 3479, 27, 27,  N'Goose_(Goose)',  N'http://www.tdar.org/ontologies/3479#Goose_(Goose)',  N'1.2.13.21.27', NULL,  N'Goose (Goose)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4003, 3479, 226, 244,  N'Rodentia',  N'http://www.tdar.org/ontologies/3479#Rodentia',  N'146.226', NULL,  N'Rodentia', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4004, 3479, 144, 144,  N'Bufo_bufo_(Toad)',  N'http://www.tdar.org/ontologies/3479#Bufo_bufo_(Toad)',  N'143.144', NULL,  N'Bufo bufo (Toad)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4005, 3479, 80, 82,  N'Procellariidae',  N'http://www.tdar.org/ontologies/3479#Procellariidae',  N'1.2.80', NULL,  N'Procellariidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4006, 3479, 114, 114,  N'Corvus_corax_(Raven)',  N'http://www.tdar.org/ontologies/3479#Corvus_corax_(Raven)',  N'1.109.113.114', NULL,  N'Corvus corax (Raven)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4007, 3479, 204, 213,  N'Insectivora',  N'http://www.tdar.org/ontologies/3479#Insectivora',  N'146.204', NULL,  N'Insectivora', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4008, 3479, 58, 58,  N'Grus_grus_(Crane)',  N'http://www.tdar.org/ontologies/3479#Grus_grus_(Crane)',  N'1.2.57.58', NULL,  N'Grus grus (Crane)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4009, 3479, 51, 53,  N'Falconidae_(Falconid)',  N'http://www.tdar.org/ontologies/3479#Falconidae_(Falconid)',  N'1.2.51', NULL,  N'Falconidae (Falconid)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4010, 3479, 221, 221,  N'Medium_mammal_(vertebra_and_ribs)_(pig_and_fallow_deer-sized)',  N'http://www.tdar.org/ontologies/3479#Medium_mammal_(vertebra_and_ribs)_(pig_and_fallow_deer-sized)',  N'146.221', NULL,  N'Medium mammal (vertebra and ribs) (pig and fallow deer-sized)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4011, 3479, 71, 71,  N'Gallus_gallus_(Chicken)',  N'http://www.tdar.org/ontologies/3479#Gallus_gallus_(Chicken)',  N'1.2.69.71', NULL,  N'Gallus gallus (Chicken)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4012, 3479, 140, 140,  N'Turdidae_or_Sturnidae_(Turdid_or_Sturnid)',  N'http://www.tdar.org/ontologies/3479#Turdidae_or_Sturnidae_(Turdid_or_Sturnid)',  N'1.109.140', NULL,  N'Turdidae or Sturnidae (Turdid or Sturnid)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4013, 3479, 31, 31,  N'Tadorna_tadorna_(Shelduck)',  N'http://www.tdar.org/ontologies/3479#Tadorna_tadorna_(Shelduck)',  N'1.2.13.31', NULL,  N'Tadorna tadorna (Shelduck)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4014, 3479, 197, 197,  N'Small_cetacean',  N'http://www.tdar.org/ontologies/3479#Small_cetacean',  N'146.195.197', NULL,  N'Small cetacean', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4015, 3479, 182, 182,  N'Mustela_nivalis_(Weasel)',  N'http://www.tdar.org/ontologies/3479#Mustela_nivalis_(Weasel)',  N'146.166.177.180.182', NULL,  N'Mustela nivalis (Weasel)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4016, 3479, 25, 25,  N'Cygnus_or_Anser_spp._(Swan_or_Goose)',  N'http://www.tdar.org/ontologies/3479#Cygnus_or_Anser_spp._(Swan_or_Goose)',  N'1.2.13.21.25', NULL,  N'Cygnus or Anser spp. (Swan or Goose)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4017, 3479, 30, 30,  N'Branta_leucopsis_(Barnacle_goose)',  N'http://www.tdar.org/ontologies/3479#Branta_leucopsis_(Barnacle_goose)',  N'1.2.13.30', NULL,  N'Branta leucopsis (Barnacle goose)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4018, 3479, 149, 149,  N'Bos_taurus_(Cattle)',  N'http://www.tdar.org/ontologies/3479#Bos_taurus_(Cattle)',  N'146.147.149', NULL,  N'Bos taurus (Cattle)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4019, 3479, 214, 219,  N'Lagomorpha',  N'http://www.tdar.org/ontologies/3479#Lagomorpha',  N'146.214', NULL,  N'Lagomorpha', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4020, 3479, 1, 142,  N'AVES',  N'http://www.tdar.org/ontologies/3479#AVES',  N'1', NULL,  N'AVES', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4021, 3479, 15, 15,  N'Anas_crecca_(Teal)',  N'http://www.tdar.org/ontologies/3479#Anas_crecca_(Teal)',  N'1.2.13.14.15', NULL,  N'Anas crecca (Teal)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4022, 3479, 10, 10,  N'Alle_alle_(Little_auk)',  N'http://www.tdar.org/ontologies/3479#Alle_alle_(Little_auk)',  N'1.2.9.10', NULL,  N'Alle alle (Little auk)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4023, 3479, 143, 145,  N'AMPHIBIA_(Amphibian)',  N'http://www.tdar.org/ontologies/3479#AMPHIBIA_(Amphibian)',  N'143', NULL,  N'AMPHIBIA (Amphibian)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4024, 3479, 33, 36,  N'Ardeidae',  N'http://www.tdar.org/ontologies/3479#Ardeidae',  N'1.2.33', NULL,  N'Ardeidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4025, 3479, 44, 47,  N'Columbidae_(Columbid)',  N'http://www.tdar.org/ontologies/3479#Columbidae_(Columbid)',  N'1.2.44', NULL,  N'Columbidae (Columbid)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4026, 3479, 6, 6,  N'Buteo_Buteo_(Buzzard)',  N'http://www.tdar.org/ontologies/3479#Buteo_Buteo_(Buzzard)',  N'1.2.3.6', NULL,  N'Buteo Buteo (Buzzard)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4027, 3479, 78, 78,  N'Phasianus_colchicus_(Pheasant)',  N'http://www.tdar.org/ontologies/3479#Phasianus_colchicus_(Pheasant)',  N'1.2.69.78', NULL,  N'Phasianus colchicus (Pheasant)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4028, 3479, 46, 46,  N'Columba_palumbus_(Wood_pigeon)',  N'http://www.tdar.org/ontologies/3479#Columba_palumbus_(Wood_pigeon)',  N'1.2.44.46', NULL,  N'Columba palumbus (Wood pigeon)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4029, 3479, 16, 16,  N'Anas_crecca_or_querquedula_(Teal_or_Garganey)',  N'http://www.tdar.org/ontologies/3479#Anas_crecca_or_querquedula_(Teal_or_Garganey)',  N'1.2.13.14.16', NULL,  N'Anas crecca or querquedula (Teal or Garganey)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4030, 3479, 65, 68,  N'Phallacrocoracidae',  N'http://www.tdar.org/ontologies/3479#Phallacrocoracidae',  N'1.2.65', NULL,  N'Phallacrocoracidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4031, 3479, 231, 231,  N'Apomedus_sp._(Murid)',  N'http://www.tdar.org/ontologies/3479#Apomedus_sp._(Murid)',  N'146.226.229.231', NULL,  N'Apomedus sp. (Murid)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4032, 3479, 22, 22,  N'Anser_anser_(Domestic_or_Greylag_goose)',  N'http://www.tdar.org/ontologies/3479#Anser_anser_(Domestic_or_Greylag_goose)',  N'1.2.13.21.22', NULL,  N'Anser anser (Domestic or Greylag goose)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4033, 3479, 111, 111,  N'Alauda_arvensis_(Skylark)',  N'http://www.tdar.org/ontologies/3479#Alauda_arvensis_(Skylark)',  N'1.109.110.111', NULL,  N'Alauda arvensis (Skylark)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4034, 3479, 126, 126,  N'Anthus_pratensis_(Meadow_pipit)',  N'http://www.tdar.org/ontologies/3479#Anthus_pratensis_(Meadow_pipit)',  N'1.109.125.126', NULL,  N'Anthus pratensis (Meadow pipit)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4035, 3479, 147, 165,  N'Artiodactyla',  N'http://www.tdar.org/ontologies/3479#Artiodactyla',  N'146.147', NULL,  N'Artiodactyla', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4036, 3479, 19, 19,  N'Anas_querquedula_(Garganey)',  N'http://www.tdar.org/ontologies/3479#Anas_querquedula_(Garganey)',  N'1.2.13.14.19', NULL,  N'Anas querquedula (Garganey)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4097, 3479, 57, 59,  N'Gruidae',  N'http://www.tdar.org/ontologies/3479#Gruidae',  N'1.2.57', NULL,  N'Gruidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4037, 3479, 177, 188,  N'Mustela_(Mustelid)',  N'http://www.tdar.org/ontologies/3479#Mustela_(Mustelid)',  N'146.166.177', NULL,  N'Mustela (Mustelid)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4038, 3479, 105, 107,  N'Tytonidae',  N'http://www.tdar.org/ontologies/3479#Tytonidae',  N'1.2.105', NULL,  N'Tytonidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4039, 3479, 116, 116,  N'Corvus_monedula_(Jackdaw)',  N'http://www.tdar.org/ontologies/3479#Corvus_monedula_(Jackdaw)',  N'1.109.113.116', NULL,  N'Corvus monedula (Jackdaw)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4040, 3479, 118, 118,  N'Pica_pica_(Magpie)',  N'http://www.tdar.org/ontologies/3479#Pica_pica_(Magpie)',  N'1.109.113.118', NULL,  N'Pica pica (Magpie)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4041, 3479, 200, 203,  N'Equidae_(Equid)',  N'http://www.tdar.org/ontologies/3479#Equidae_(Equid)',  N'146.200', NULL,  N'Equidae (Equid)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4042, 3479, 87, 87,  N'Rallus_aquaticus_(Water_rail)',  N'http://www.tdar.org/ontologies/3479#Rallus_aquaticus_(Water_rail)',  N'1.2.83.87', NULL,  N'Rallus aquaticus (Water rail)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4043, 3479, 115, 115,  N'Corvus_frugilegus_or_corone_(Rook_or_Carrion_crow)',  N'http://www.tdar.org/ontologies/3479#Corvus_frugilegus_or_corone_(Rook_or_Carrion_crow)',  N'1.109.113.115', NULL,  N'Corvus frugilegus or corone (Rook or Carrion crow)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4044, 3479, 61, 61,  N'Sterna_hirundo_(Common_tern)',  N'http://www.tdar.org/ontologies/3479#Sterna_hirundo_(Common_tern)',  N'1.2.60.61', NULL,  N'Sterna hirundo (Common tern)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4045, 3479, 69, 79,  N'Phasianidae',  N'http://www.tdar.org/ontologies/3479#Phasianidae',  N'1.2.69', NULL,  N'Phasianidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4046, 3479, 158, 158,  N'Ovis_aries_(Sheep)',  N'http://www.tdar.org/ontologies/3479#Ovis_aries_(Sheep)',  N'146.147.156.158', NULL,  N'Ovis aries (Sheep)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4047, 3479, 186, 186,  N'Mustela_putorius_(Polecat)',  N'http://www.tdar.org/ontologies/3479#Mustela_putorius_(Polecat)',  N'146.166.177.184.186', NULL,  N'Mustela putorius (Polecat)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4048, 3479, 39, 39,  N'Pluvialis_apricaria_(Golden_plover)',  N'http://www.tdar.org/ontologies/3479#Pluvialis_apricaria_(Golden_plover)',  N'1.2.37.39', NULL,  N'Pluvialis apricaria (Golden plover)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4049, 3479, 129, 129,  N'Erithacus_rubecula_(Robin)',  N'http://www.tdar.org/ontologies/3479#Erithacus_rubecula_(Robin)',  N'1.109.128.129', NULL,  N'Erithacus rubecula (Robin)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4050, 3479, 60, 64,  N'Laridae_(Gull)',  N'http://www.tdar.org/ontologies/3479#Laridae_(Gull)',  N'1.2.60', NULL,  N'Laridae (Gull)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4051, 3479, 14, 20,  N'Anatini_(Anatin)',  N'http://www.tdar.org/ontologies/3479#Anatini_(Anatin)',  N'1.2.13.14', NULL,  N'Anatini (Anatin)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4052, 3479, 160, 160,  N'Ovis_aries_or_Capra_hircus_or_Capreolus_capreolus_(Sheep_or_Goat_or_Roe_deer)',  N'http://www.tdar.org/ontologies/3479#Ovis_aries_or_Capra_hircus_or_Capreolus_capreolus_(Sheep_or_Goat_or_Roe_deer)',  N'146.147.160', NULL,  N'Ovis aries or Capra hircus or Capreolus capreolus (Sheep or Goat or Roe deer)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4054, 3479, 152, 152,  N'Cervus_elaphus_(Red_deer)',  N'http://www.tdar.org/ontologies/3479#Cervus_elaphus_(Red_deer)',  N'146.147.152', NULL,  N'Cervus elaphus (Red deer)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4055, 3479, 102, 104,  N'Tetraoninae',  N'http://www.tdar.org/ontologies/3479#Tetraoninae',  N'1.2.102', NULL,  N'Tetraoninae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4056, 3479, 146, 246,  N'MAMMALIA',  N'http://www.tdar.org/ontologies/3479#MAMMALIA',  N'146', NULL,  N'MAMMALIA', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4057, 3479, 138, 138,  N'Turdus_viscivorus_(Mistle_thrush)',  N'http://www.tdar.org/ontologies/3479#Turdus_viscivorus_(Mistle_thrush)',  N'1.109.137.138', NULL,  N'Turdus viscivorus (Mistle thrush)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4058, 3479, 205, 209,  N'Sorex_or_Neomys_spp._(Shrew)',  N'http://www.tdar.org/ontologies/3479#Sorex_or_Neomys_spp._(Shrew)',  N'146.204.205', NULL,  N'Sorex or Neomys spp. (Shrew)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4059, 3479, 34, 34,  N'Ardea_cinerea_(Grey_heron)',  N'http://www.tdar.org/ontologies/3479#Ardea_cinerea_(Grey_heron)',  N'1.2.33.34', NULL,  N'Ardea cinerea (Grey heron)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4060, 3479, 122, 124,  N'Hirundinidae',  N'http://www.tdar.org/ontologies/3479#Hirundinidae',  N'1.109.122', NULL,  N'Hirundinidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4061, 3479, 238, 242,  N'Small_microtinae_(Small_arvicolinae)',  N'http://www.tdar.org/ontologies/3479#Small_microtinae_(Small_arvicolinae)',  N'146.226.238', NULL,  N'Small microtinae (Small arvicolinae)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4062, 3479, 162, 162,  N'Sus_domesticus_(Pig)',  N'http://www.tdar.org/ontologies/3479#Sus_domesticus_(Pig)',  N'146.147.161.162', NULL,  N'Sus domesticus (Pig)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4063, 3479, 11, 11,  N'Cepphus_grylle_(Black_guillemot)',  N'http://www.tdar.org/ontologies/3479#Cepphus_grylle_(Black_guillemot)',  N'1.2.9.11', NULL,  N'Cepphus grylle (Black guillemot)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4064, 3479, 190, 190,  N'Halichoerus_grypus_(Grey_seal)',  N'http://www.tdar.org/ontologies/3479#Halichoerus_grypus_(Grey_seal)',  N'146.166.189.190', NULL,  N'Halichoerus grypus (Grey seal)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4065, 3479, 99, 101,  N'Sulidae',  N'http://www.tdar.org/ontologies/3479#Sulidae',  N'1.2.99', NULL,  N'Sulidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4066, 3479, 247, 247,  N'Other_Animal',  N'http://www.tdar.org/ontologies/3479#Other_Animal',  N'247', NULL,  N'Other Animal', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4067, 3479, 202, 202,  N'Equus_caballus_(Horse)',  N'http://www.tdar.org/ontologies/3479#Equus_caballus_(Horse)',  N'146.200.202', NULL,  N'Equus caballus (Horse)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4068, 3479, 67, 67,  N'Phalacrocorax_carbo_(Cormorant)',  N'http://www.tdar.org/ontologies/3479#Phalacrocorax_carbo_(Cormorant)',  N'1.2.65.67', NULL,  N'Phalacrocorax carbo (Cormorant)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4069, 3479, 26, 26,  N'Cygnus_sp._(Swan)',  N'http://www.tdar.org/ontologies/3479#Cygnus_sp._(Swan)',  N'1.2.13.21.26', NULL,  N'Cygnus sp. (Swan)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4070, 3479, 55, 55,  N'Gavia_immer_(Great_northern_diver)',  N'http://www.tdar.org/ontologies/3479#Gavia_immer_(Great_northern_diver)',  N'1.2.54.55', NULL,  N'Gavia immer (Great northern diver)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4071, 3479, 24, 24,  N'Cygnus_olar_(Mute_swan)',  N'http://www.tdar.org/ontologies/3479#Cygnus_olar_(Mute_swan)',  N'1.2.13.21.24', NULL,  N'Cygnus olar (Mute swan)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4072, 3479, 207, 207,  N'Sorex_araneus_(Common_shrew)',  N'http://www.tdar.org/ontologies/3479#Sorex_araneus_(Common_shrew)',  N'146.204.205.207', NULL,  N'Sorex araneus (Common shrew)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4073, 3479, 52, 52,  N'Falco_peregrinus_(Peregrine_falcon)',  N'http://www.tdar.org/ontologies/3479#Falco_peregrinus_(Peregrine_falcon)',  N'1.2.51.52', NULL,  N'Falco peregrinus (Peregrine falcon)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4074, 3479, 230, 230,  N'Apomedus_or_Mus_spp._(Murid)',  N'http://www.tdar.org/ontologies/3479#Apomedus_or_Mus_spp._(Murid)',  N'146.226.229.230', NULL,  N'Apomedus or Mus spp. (Murid)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4075, 3479, 37, 43,  N'Charadriidae',  N'http://www.tdar.org/ontologies/3479#Charadriidae',  N'1.2.37', NULL,  N'Charadriidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4076, 3479, 218, 218,  N'Oryctolagus_cuniculus_(Rabbit)',  N'http://www.tdar.org/ontologies/3479#Oryctolagus_cuniculus_(Rabbit)',  N'146.214.218', NULL,  N'Oryctolagus cuniculus (Rabbit)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4077, 3479, 131, 133,  N'Passeridae',  N'http://www.tdar.org/ontologies/3479#Passeridae',  N'1.109.131', NULL,  N'Passeridae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4078, 3479, 77, 77,  N'Perdix_perdix_(Grey_partridge)',  N'http://www.tdar.org/ontologies/3479#Perdix_perdix_(Grey_partridge)',  N'1.2.69.77', NULL,  N'Perdix perdix (Grey partridge)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4079, 3479, 239, 239,  N'Arvicola_terrestris_(Water_vole)',  N'http://www.tdar.org/ontologies/3479#Arvicola_terrestris_(Water_vole)',  N'146.226.238.239', NULL,  N'Arvicola terrestris (Water vole)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4080, 3479, 49, 49,  N'Cuculus_canorus_(Cuckoo)',  N'http://www.tdar.org/ontologies/3479#Cuculus_canorus_(Cuckoo)',  N'1.2.48.49', NULL,  N'Cuculus canorus (Cuckoo)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4081, 3479, 173, 176,  N'Felis_sp.',  N'http://www.tdar.org/ontologies/3479#Felis_sp.',  N'146.166.173', NULL,  N'Felis sp.', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4082, 3479, 96, 98,  N'Strigidae_(Owl)',  N'http://www.tdar.org/ontologies/3479#Strigidae_(Owl)',  N'1.2.96', NULL,  N'Strigidae (Owl)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4083, 3479, 210, 212,  N'Tapla_sp._(Mole)',  N'http://www.tdar.org/ontologies/3479#Tapla_sp._(Mole)',  N'146.204.210', NULL,  N'Tapla sp. (Mole)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4084, 3479, 191, 191,  N'Phoca_vitulina_(Common_seal)',  N'http://www.tdar.org/ontologies/3479#Phoca_vitulina_(Common_seal)',  N'146.166.189.191', NULL,  N'Phoca vitulina (Common seal)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4085, 3479, 175, 175,  N'Felis_sylvestris_(Wild_cat)',  N'http://www.tdar.org/ontologies/3479#Felis_sylvestris_(Wild_cat)',  N'146.166.173.175', NULL,  N'Felis sylvestris (Wild cat)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4086, 3479, 72, 72,  N'Gallus_gallus_or_Numida_meleagris_(Chicken_or_Guinea_fowl)',  N'http://www.tdar.org/ontologies/3479#Gallus_gallus_or_Numida_meleagris_(Chicken_or_Guinea_fowl)',  N'1.2.69.72', NULL,  N'Gallus gallus or Numida meleagris (Chicken or Guinea fowl)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4087, 3479, 245, 245,  N'Small_mammal_(sheep_goat_dog_roe_deer-sized)',  N'http://www.tdar.org/ontologies/3479#Small_mammal_(sheep_goat_dog_roe_deer-sized)',  N'146.245', NULL,  N'Small mammal (sheep goat dog roe deer-sized)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4088, 3479, 45, 45,  N'Columba_livia_(Rock_dove_or_Feral_pigeon)',  N'http://www.tdar.org/ontologies/3479#Columba_livia_(Rock_dove_or_Feral_pigeon)',  N'1.2.44.45', NULL,  N'Columba livia (Rock dove or Feral pigeon)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4089, 3479, 86, 86,  N'Gallinula_chloropus_(Moorhen)',  N'http://www.tdar.org/ontologies/3479#Gallinula_chloropus_(Moorhen)',  N'1.2.83.86', NULL,  N'Gallinula chloropus (Moorhen)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4090, 3479, 236, 236,  N'Rattus_rattus_(Black_rat)',  N'http://www.tdar.org/ontologies/3479#Rattus_rattus_(Black_rat)',  N'146.226.235.236', NULL,  N'Rattus rattus (Black rat)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4091, 3479, 93, 93,  N'Numenius_arquata_(Curlew)',  N'http://www.tdar.org/ontologies/3479#Numenius_arquata_(Curlew)',  N'1.2.89.93', NULL,  N'Numenius arquata (Curlew)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4092, 3479, 196, 196,  N'Large_cetacean',  N'http://www.tdar.org/ontologies/3479#Large_cetacean',  N'146.195.196', NULL,  N'Large cetacean', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4093, 3479, 206, 206,  N'Erinacaeus_europaeus_(Hedgehog)',  N'http://www.tdar.org/ontologies/3479#Erinacaeus_europaeus_(Hedgehog)',  N'146.204.205.206', NULL,  N'Erinacaeus europaeus (Hedgehog)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4094, 3479, 128, 130,  N'Muscicapidae',  N'http://www.tdar.org/ontologies/3479#Muscicapidae',  N'1.109.128', NULL,  N'Muscicapidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4095, 3479, 84, 84,  N'Crex_crex_(Corncrake)',  N'http://www.tdar.org/ontologies/3479#Crex_crex_(Corncrake)',  N'1.2.83.84', NULL,  N'Crex crex (Corncrake)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4096, 3479, 234, 234,  N'Rattus_norvegicus_(Brown_rat)Rattus_norvegicus_or_Arvicola_terrestris_(Brown_rat_or_Water_vole)',  N'http://www.tdar.org/ontologies/3479#Rattus_norvegicus_(Brown_rat)Rattus_norvegicus_or_Arvicola_terrestris_(Brown_rat_or_Water_vole)',  N'146.226.234', NULL,  N'Rattus norvegicus (Brown rat)Rattus norvegicus or Arvicola terrestris (Brown rat or Water vole)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4098, 3479, 161, 164,  N'Sus_domesticus_or_scrofa_(Pig_or_Wild_boar)',  N'http://www.tdar.org/ontologies/3479#Sus_domesticus_or_scrofa_(Pig_or_Wild_boar)',  N'146.147.161', NULL,  N'Sus domesticus or scrofa (Pig or Wild boar)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4099, 3479, 113, 121,  N'Corvidae_(Corvid)',  N'http://www.tdar.org/ontologies/3479#Corvidae_(Corvid)',  N'1.109.113', NULL,  N'Corvidae (Corvid)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4100, 3479, 81, 81,  N'Puffinus_puffinus_(Manx_shearwater)',  N'http://www.tdar.org/ontologies/3479#Puffinus_puffinus_(Manx_shearwater)',  N'1.2.80.81', NULL,  N'Puffinus puffinus (Manx shearwater)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4101, 3479, 35, 35,  N'Botaurus_stellaris_(Bittern)',  N'http://www.tdar.org/ontologies/3479#Botaurus_stellaris_(Bittern)',  N'1.2.33.35', NULL,  N'Botaurus stellaris (Bittern)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4102, 3479, 224, 224,  N'Macaca_sp._(Macaque)',  N'http://www.tdar.org/ontologies/3479#Macaca_sp._(Macaque)',  N'146.223.224', NULL,  N'Macaca sp. (Macaque)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4103, 3479, 74, 74,  N'Gallus_gallus_or_Phasianus_colchicus_(Chicken_or_Pheasant)',  N'http://www.tdar.org/ontologies/3479#Gallus_gallus_or_Phasianus_colchicus_(Chicken_or_Pheasant)',  N'1.2.69.74', NULL,  N'Gallus gallus or Phasianus colchicus (Chicken or Pheasant)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4104, 3479, 125, 127,  N'Motacillidae',  N'http://www.tdar.org/ontologies/3479#Motacillidae',  N'1.109.125', NULL,  N'Motacillidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4105, 3479, 216, 216,  N'Lepus_europaeus_(Brown_hare)',  N'http://www.tdar.org/ontologies/3479#Lepus_europaeus_(Brown_hare)',  N'146.214.215.216', NULL,  N'Lepus europaeus (Brown hare)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4106, 3479, 223, 225,  N'Primates',  N'http://www.tdar.org/ontologies/3479#Primates',  N'146.223', NULL,  N'Primates', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4107, 3479, 222, 222,  N'Microfauna',  N'http://www.tdar.org/ontologies/3479#Microfauna',  N'146.222', NULL,  N'Microfauna', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4108, 3479, 132, 132,  N'Passer_domesticus_(House_sparrow)',  N'http://www.tdar.org/ontologies/3479#Passer_domesticus_(House_sparrow)',  N'1.109.131.132', NULL,  N'Passer domesticus (House sparrow)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4109, 3479, 220, 220,  N'Large_mammal_(vertebra_and_ribs)_(cattle_horse_red_deer-sized)',  N'http://www.tdar.org/ontologies/3479#Large_mammal_(vertebra_and_ribs)_(cattle_horse_red_deer-sized)',  N'146.220', NULL,  N'Large mammal (vertebra and ribs) (cattle horse red deer-sized)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4110, 3479, 103, 103,  N'Lyrurus_tetrix_(Black_grouse)',  N'http://www.tdar.org/ontologies/3479#Lyrurus_tetrix_(Black_grouse)',  N'1.2.102.103', NULL,  N'Lyrurus tetrix (Black grouse)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4111, 3479, 94, 94,  N'Scolopax_rusticola_(Woodcock)',  N'http://www.tdar.org/ontologies/3479#Scolopax_rusticola_(Woodcock)',  N'1.2.89.94', NULL,  N'Scolopax rusticola (Woodcock)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4112, 3479, 167, 167,  N'Canis_familiaris_or_Vulpes_vulpes_(Dog_or_Fox)',  N'http://www.tdar.org/ontologies/3479#Canis_familiaris_or_Vulpes_vulpes_(Dog_or_Fox)',  N'146.166.167', NULL,  N'Canis familiaris or Vulpes vulpes (Dog or Fox)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4113, 3479, 155, 155,  N'Dama_dama_or_Capreolus_capreolus_(Fallow_deer_or_Roe_deer)',  N'http://www.tdar.org/ontologies/3479#Dama_dama_or_Capreolus_capreolus_(Fallow_deer_or_Roe_deer)',  N'146.147.155', NULL,  N'Dama dama or Capreolus capreolus (Fallow deer or Roe deer)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4114, 3479, 117, 117,  N'Garrulus_glandarius_(Jay)',  N'http://www.tdar.org/ontologies/3479#Garrulus_glandarius_(Jay)',  N'1.109.113.117', NULL,  N'Garrulus glandarius (Jay)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4115, 3479, 241, 241,  N'Microtus_agrestis_(Field_vole)',  N'http://www.tdar.org/ontologies/3479#Microtus_agrestis_(Field_vole)',  N'146.226.238.241', NULL,  N'Microtus agrestis (Field vole)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4116, 3479, 228, 228,  N'Large_rodent_(rat_water_vole_squirrel_doormouse-sized)',  N'http://www.tdar.org/ontologies/3479#Large_rodent_(rat_water_vole_squirrel_doormouse-sized)',  N'146.226.228', NULL,  N'Large rodent (rat water vole squirrel doormouse-sized)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4117, 3479, 42, 42,  N'Vanellus_vanellus_(Lapwing)',  N'http://www.tdar.org/ontologies/3479#Vanellus_vanellus_(Lapwing)',  N'1.2.37.42', NULL,  N'Vanellus vanellus (Lapwing)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4118, 3479, 156, 159,  N'Ovis_aries_or_Capra_hircus_(Sheep_or_Goat)',  N'http://www.tdar.org/ontologies/3479#Ovis_aries_or_Capra_hircus_(Sheep_or_Goat)',  N'146.147.156', NULL,  N'Ovis aries or Capra hircus (Sheep or Goat)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4119, 3479, 17, 17,  N'Anas_penelope_(Wigeon)',  N'http://www.tdar.org/ontologies/3479#Anas_penelope_(Wigeon)',  N'1.2.13.14.17', NULL,  N'Anas penelope (Wigeon)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4120, 3479, 150, 150,  N'Bos_taurus_or_Cervus_elaphus_(Cattle_or_Red_deer)',  N'http://www.tdar.org/ontologies/3479#Bos_taurus_or_Cervus_elaphus_(Cattle_or_Red_deer)',  N'146.147.150', NULL,  N'Bos taurus or Cervus elaphus (Cattle or Red deer)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4121, 3479, 199, 199,  N'Chiroptera_(Bat)',  N'http://www.tdar.org/ontologies/3479#Chiroptera_(Bat)',  N'146.199', NULL,  N'Chiroptera (Bat)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4122, 3479, 38, 38,  N'Haematopus_ostralegus_(Oystercatcher)',  N'http://www.tdar.org/ontologies/3479#Haematopus_ostralegus_(Oystercatcher)',  N'1.2.37.38', NULL,  N'Haematopus ostralegus (Oystercatcher)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4123, 3479, 63, 63,  N'small_gull',  N'http://www.tdar.org/ontologies/3479#small_gull',  N'1.2.60.63', NULL,  N'small gull', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4142, 3479, 179, 179,  N'Meles_meles_(Badger)',  N'http://www.tdar.org/ontologies/3479#Meles_meles_(Badger)',  N'146.166.177.179', NULL,  N'Meles meles (Badger)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4143, 3479, 40, 40,  N'Pluvialis_sp._(Plover)',  N'http://www.tdar.org/ontologies/3479#Pluvialis_sp._(Plover)',  N'1.2.37.40', NULL,  N'Pluvialis sp. (Plover)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4144, 3479, 23, 23,  N'Cygnus_cygnus_(Whooper_swan)',  N'http://www.tdar.org/ontologies/3479#Cygnus_cygnus_(Whooper_swan)',  N'1.2.13.21.23', NULL,  N'Cygnus cygnus (Whooper swan)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4145, 3479, 76, 76,  N'Pavo_cristatus_(Peacock)',  N'http://www.tdar.org/ontologies/3479#Pavo_cristatus_(Peacock)',  N'1.2.69.76', NULL,  N'Pavo cristatus (Peacock)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4146, 3479, 243, 243,  N'Small_rodent_(field_vole_common_vole_mouse-sized)',  N'http://www.tdar.org/ontologies/3479#Small_rodent_(field_vole_common_vole_mouse-sized)',  N'146.226.243', NULL,  N'Small rodent (field vole common vole mouse-sized)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4147, 3479, 110, 112,  N'Alaudidae',  N'http://www.tdar.org/ontologies/3479#Alaudidae',  N'1.109.110', NULL,  N'Alaudidae', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4148, 3479, 208, 208,  N'Sorex_minutus_(Pygmy_shrew)',  N'http://www.tdar.org/ontologies/3479#Sorex_minutus_(Pygmy_shrew)',  N'146.204.205.208', NULL,  N'Sorex minutus (Pygmy shrew)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4149, 3479, 157, 157,  N'Capra_hircus_(Goat)',  N'http://www.tdar.org/ontologies/3479#Capra_hircus_(Goat)',  N'146.147.156.157', NULL,  N'Capra hircus (Goat)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4150, 3479, 154, 154,  N'Dama_dama_(Fallow_deer)',  N'http://www.tdar.org/ontologies/3479#Dama_dama_(Fallow_deer)',  N'146.147.154', NULL,  N'Dama dama (Fallow deer)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4151, 3479, 85, 85,  N'Fulica_atra_(Coot)',  N'http://www.tdar.org/ontologies/3479#Fulica_atra_(Coot)',  N'1.2.83.85', NULL,  N'Fulica atra (Coot)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4152, 3479, 4, 4,  N'Accipiter_gentilis_(Goshawk)',  N'http://www.tdar.org/ontologies/3479#Accipiter_gentilis_(Goshawk)',  N'1.2.3.4', NULL,  N'Accipiter gentilis (Goshawk)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4153, 3479, 174, 174,  N'Felis_catus_(Cat)',  N'http://www.tdar.org/ontologies/3479#Felis_catus_(Cat)',  N'146.166.173.174', NULL,  N'Felis catus (Cat)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4154, 3479, 5, 5,  N'Accipiter_nisus_(Sparrowhawk)',  N'http://www.tdar.org/ontologies/3479#Accipiter_nisus_(Sparrowhawk)',  N'1.2.3.5', NULL,  N'Accipiter nisus (Sparrowhawk)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4155, 3479, 180, 183,  N'Mustela_erminea_or_nivalis_(Stoat_or_Weasel)',  N'http://www.tdar.org/ontologies/3479#Mustela_erminea_or_nivalis_(Stoat_or_Weasel)',  N'146.166.177.180', NULL,  N'Mustela erminea or nivalis (Stoat or Weasel)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4156, 3479, 66, 66,  N'Phalacrocorax_aristotelis_(Shag)',  N'http://www.tdar.org/ontologies/3479#Phalacrocorax_aristotelis_(Shag)',  N'1.2.65.66', NULL,  N'Phalacrocorax aristotelis (Shag)', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (4157, 3479, 195, 198,  N'Cetacea',  N'http://www.tdar.org/ontologies/3479#Cetacea',  N'146.195', NULL,  N'Cetacea', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (5623, 3029, 1, 1,  N'Present',  N'http://www.tdar.org/ontologies/3029#Present',  N'1', NULL,  N'Present', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (5624, 3029, 2, 2,  N'Probable',  N'http://www.tdar.org/ontologies/3029#Probable',  N'2', NULL,  N'Probable', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (5625, 3029, 3, 3,  N'Indeterminate',  N'http://www.tdar.org/ontologies/3029#Indeterminate',  N'3', NULL,  N'Indeterminate', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (5626, 3029, 4, 4,  N'Absent',  N'http://www.tdar.org/ontologies/3029#Absent',  N'4', NULL,  N'Absent', 0);
INSERT INTO ontology_node (id, ontology_id, interval_start, interval_end, iri, uri, index, description, display_name, import_order) VALUES (5627, 3029, 5, 5,  N'Not_Recorded',  N'http://www.tdar.org/ontologies/3029#Not_Recorded',  N'5', NULL,  N'Not Recorded', 0);
--------------------------- ontology_node_synonym ---------------------------
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4047,  N'Polecat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4141,  N'Asio otus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4141,  N'Long-eared owl');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4053,  N'Rallidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4128,  N'Anserinae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4124,  N'Scolopacidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4124,  N'Scolopacid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4125,  N'Ursus arctos');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4125,  N'Brown bear');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4126,  N'Limosa sp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4126,  N'Godwit');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4127,  N'Martes martes');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4127,  N'Pine martin');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4129,  N'Accipitridae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4130,  N'Cervus elaphus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4130,  N'Dama dama');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4130,  N'Red deer');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4130,  N'Fallow deer');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4131,  N'small corvid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4132,  N'Carnivora');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4133,  N'large corvid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4134,  N'Mustela erminea');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4134,  N'Stoat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4135,  N'Mustela putorius');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4135,  N'Martes martes');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4135,  N'Polecat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4135,  N'Pine martin');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4136,  N'Gaviidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4137,  N'Lymnocryptes minimus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4137,  N'Jack snipe');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4138,  N'Talpa europaea');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4138,  N'Common mole');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4139,  N'Phoca vitulina');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4139,  N'Halichoerus grypus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4139,  N'Common');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4139,  N'Grey seal');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4140,  N'Non-passerine families');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3968,  N'Tyto alba');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3968,  N'Barn owl');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3969,  N'Hirundo rustico');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3969,  N'Swallow');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3970,  N'Anas platyrhynchos');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3970,  N'Mallard');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3970,  N'Domestic duck');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3971,  N'Canis familiaris');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3971,  N'lupus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3971,  N'Dog');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3971,  N'Wolf');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3972,  N'Bos primigenius');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3972,  N'Auroch');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3973,  N'Coturnix coturnix');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3973,  N'Quail');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3974,  N'large gull');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3975,  N'Clethrionomys glareolus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3975,  N'Bank vole');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3976,  N'Lagopus lagopus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3976,  N'Willow');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3976,  N'Red grouse');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3977,  N'Morus bassanus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3977,  N'Northern Gannet');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3978,  N'Aythyini');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3978,  N'Aythyin');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3979,  N'Prunella modularis');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3979,  N'Dunnock');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3980,  N'Prunellidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3981,  N'Lepus sp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3981,  N'Hare');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3982,  N'Turdidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3983,  N'Gallus gallus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3983,  N'Numida meleagris');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3983,  N'Phasianus colchicus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3983,  N'Chicken');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3983,  N'Guinea fowl');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3983,  N'Pheasant');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3984,  N'Castor fiber');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3984,  N'Beaver');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3985,  N'Rattus sp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3985,  N'Rat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3986,  N'Sus scrofa');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3986,  N'Wild boar');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3987,  N'Equus asinus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3987,  N'Donkey');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3988,  N'Cuculidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3989,  N'Murinae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3989,  N'small');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3990,  N'Vulpes vulpes');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3990,  N'Red fox');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3991,  N'Alcidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3992,  N'Passeriformes');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3992,  N'Passeriforme');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3993,  N'Gallinago gallinago');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3993,  N'Snipe');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3994,  N'Mus musculus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3994,  N'House mouse');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3995,  N'Canis lupus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3995,  N'Wolf');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3996,  N'Milvus milvus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3996,  N'Red kite');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3997,  N'Canis familiaris');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3997,  N'Dog');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3998,  N'Capreolus capreolus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3998,  N'Roe deer');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3999,  N'Anatidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (3999,  N'Ducks');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4000,  N'Lutra lutra');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4000,  N'Otter');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4001,  N'Pluvialis squatarola');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4001,  N'Grey plover');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4002,  N'Goose');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4002,  N'Goose');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4003,  N'Rodentia');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4004,  N'Bufo bufo');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4004,  N'Toad');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4005,  N'Procellariidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4006,  N'Corvus corax');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4006,  N'Raven');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4007,  N'Insectivora');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4008,  N'Grus grus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4008,  N'Crane');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4009,  N'Falconidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4009,  N'Falconid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4010,  N'Medium mammal');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4010,  N'vertebra and ribs');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4010,  N'pig and fallow deer-sized');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4011,  N'Gallus gallus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4011,  N'Chicken');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4012,  N'Turdidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4012,  N'Sturnidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4012,  N'Turdid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4012,  N'Sturnid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4013,  N'Tadorna tadorna');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4013,  N'Shelduck');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4014,  N'Small cetacean');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4015,  N'Mustela nivalis');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4015,  N'Weasel');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4016,  N'Cygnus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4016,  N'Anser spp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4016,  N'Swan');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4016,  N'Goose');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4017,  N'Branta leucopsis');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4017,  N'Barnacle goose');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4018,  N'Bos taurus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4018,  N'Cattle');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4019,  N'Lagomorpha');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4020,  N'AVES');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4021,  N'Anas crecca');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4021,  N'Teal');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4022,  N'Alle alle');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4022,  N'Little auk');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4023,  N'AMPHIBIA');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4023,  N'Amphibian');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4024,  N'Ardeidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4025,  N'Columbidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4025,  N'Columbid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4026,  N'Buteo Buteo');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4026,  N'Buzzard');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4027,  N'Phasianus colchicus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4027,  N'Pheasant');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4028,  N'Columba palumbus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4028,  N'Wood pigeon');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4029,  N'Anas crecca');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4029,  N'querquedula');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4029,  N'Teal');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4029,  N'Garganey');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4030,  N'Phallacrocoracidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4031,  N'Apomedus sp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4031,  N'Murid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4032,  N'Anser anser');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4032,  N'Domestic');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4032,  N'Greylag goose');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4033,  N'Alauda arvensis');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4033,  N'Skylark');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4034,  N'Anthus pratensis');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4034,  N'Meadow pipit');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4035,  N'Artiodactyla');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4036,  N'Anas querquedula');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4036,  N'Garganey');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4037,  N'Mustela');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4037,  N'Mustelid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4038,  N'Tytonidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4039,  N'Corvus monedula');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4039,  N'Jackdaw');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4040,  N'Pica pica');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4040,  N'Magpie');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4041,  N'Equidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4041,  N'Equid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4042,  N'Rallus aquaticus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4042,  N'Water rail');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4043,  N'Corvus frugilegus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4043,  N'corone');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4043,  N'Rook');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4043,  N'Carrion crow');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4044,  N'Sterna hirundo');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4044,  N'Common tern');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4045,  N'Phasianidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4046,  N'Ovis aries');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4046,  N'Sheep');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4047,  N'Mustela putorius');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4048,  N'Pluvialis apricaria');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4048,  N'Golden plover');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4049,  N'Erithacus rubecula');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4049,  N'Robin');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4050,  N'Laridae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4050,  N'Gull');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4051,  N'Anatini');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4051,  N'Anatin');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4052,  N'Ovis aries');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4052,  N'Capra hircus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4052,  N'Capreolus capreolus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4052,  N'Sheep');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4052,  N'Goat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4052,  N'Roe deer');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4054,  N'Cervus elaphus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4054,  N'Red deer');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4055,  N'Tetraoninae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4056,  N'MAMMALIA');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4057,  N'Turdus viscivorus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4057,  N'Mistle thrush');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4058,  N'Sorex');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4058,  N'Neomys spp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4058,  N'Shrew');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4059,  N'Ardea cinerea');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4059,  N'Grey heron');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4060,  N'Hirundinidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4061,  N'Small microtinae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4061,  N'Small arvicolinae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4062,  N'Sus domesticus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4062,  N'Pig');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4063,  N'Cepphus grylle');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4063,  N'Black guillemot');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4064,  N'Halichoerus grypus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4064,  N'Grey seal');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4065,  N'Sulidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4066,  N'Other Animal');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4067,  N'Equus caballus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4067,  N'Horse');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4068,  N'Phalacrocorax carbo');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4068,  N'Cormorant');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4069,  N'Cygnus sp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4069,  N'Swan');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4070,  N'Gavia immer');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4070,  N'Great northern diver');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4071,  N'Cygnus olar');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4071,  N'Mute swan');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4072,  N'Sorex araneus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4072,  N'Common shrew');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4073,  N'Falco peregrinus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4073,  N'Peregrine falcon');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4074,  N'Apomedus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4074,  N'Mus spp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4074,  N'Murid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4075,  N'Charadriidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4076,  N'Oryctolagus cuniculus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4076,  N'Rabbit');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4077,  N'Passeridae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4078,  N'Perdix perdix');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4078,  N'Grey partridge');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4079,  N'Arvicola terrestris');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4079,  N'Water vole');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4080,  N'Cuculus canorus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4080,  N'Cuckoo');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4081,  N'Felis sp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4082,  N'Strigidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4082,  N'Owl');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4083,  N'Tapla sp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4083,  N'Mole');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4084,  N'Phoca vitulina');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4084,  N'Common seal');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4085,  N'Felis sylvestris');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4085,  N'Wild cat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4086,  N'Gallus gallus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4086,  N'Numida meleagris');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4086,  N'Chicken');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4086,  N'Guinea fowl');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4087,  N'Small mammal');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4087,  N'sheep goat dog roe deer-sized');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4088,  N'Columba livia');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4088,  N'Rock dove');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4088,  N'Feral pigeon');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4089,  N'Gallinula chloropus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4089,  N'Moorhen');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4090,  N'Rattus rattus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4090,  N'Black rat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4091,  N'Numenius arquata');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4091,  N'Curlew');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4092,  N'Large cetacean');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4093,  N'Erinacaeus europaeus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4093,  N'Hedgehog');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4094,  N'Muscicapidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4095,  N'Crex crex');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4095,  N'Corncrake');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4096,  N'Rattus norvegicus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4096,  N'Brown ratRattus norvegicus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4096,  N'Arvicola terrestris');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4096,  N'Brown rat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4096,  N'Water vole');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4097,  N'Gruidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4098,  N'Sus domesticus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4098,  N'scrofa');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4098,  N'Pig');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4098,  N'Wild boar');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4099,  N'Corvidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4099,  N'Corvid');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4100,  N'Puffinus puffinus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4100,  N'Manx shearwater');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4101,  N'Botaurus stellaris');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4101,  N'Bittern');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4102,  N'Macaca sp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4102,  N'Macaque');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4103,  N'Gallus gallus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4103,  N'Phasianus colchicus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4103,  N'Chicken');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4103,  N'Pheasant');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4104,  N'Motacillidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4105,  N'Lepus europaeus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4105,  N'Brown hare');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4106,  N'Primates');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4107,  N'Microfauna');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4108,  N'Passer domesticus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4108,  N'House sparrow');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4109,  N'Large mammal');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4109,  N'vertebra and ribs');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4109,  N'cattle horse red deer-sized');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4110,  N'Lyrurus tetrix');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4110,  N'Black grouse');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4111,  N'Scolopax rusticola');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4111,  N'Woodcock');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4112,  N'Canis familiaris');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4112,  N'Vulpes vulpes');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4112,  N'Dog');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4112,  N'Fox');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4113,  N'Dama dama');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4113,  N'Capreolus capreolus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4113,  N'Fallow deer');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4113,  N'Roe deer');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4114,  N'Garrulus glandarius');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4114,  N'Jay');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4115,  N'Microtus agrestis');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4115,  N'Field vole');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4116,  N'Large rodent');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4116,  N'rat water vole squirrel doormouse-sized');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4117,  N'Vanellus vanellus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4117,  N'Lapwing');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4118,  N'Ovis aries');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4118,  N'Capra hircus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4118,  N'Sheep');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4118,  N'Goat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4119,  N'Anas penelope');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4119,  N'Wigeon');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4120,  N'Bos taurus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4120,  N'Cervus elaphus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4120,  N'Cattle');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4120,  N'Red deer');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4121,  N'Chiroptera');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4121,  N'Bat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4122,  N'Haematopus ostralegus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4122,  N'Oystercatcher');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4123,  N'small gull');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4142,  N'Meles meles');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4142,  N'Badger');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4143,  N'Pluvialis sp.');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4143,  N'Plover');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4144,  N'Cygnus cygnus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4144,  N'Whooper swan');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4145,  N'Pavo cristatus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4145,  N'Peacock');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4146,  N'Small rodent');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4146,  N'field vole common vole mouse-sized');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4147,  N'Alaudidae');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4148,  N'Sorex minutus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4148,  N'Pygmy shrew');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4149,  N'Capra hircus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4149,  N'Goat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4150,  N'Dama dama');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4150,  N'Fallow deer');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4151,  N'Fulica atra');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4151,  N'Coot');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4152,  N'Accipiter gentilis');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4152,  N'Goshawk');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4153,  N'Felis catus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4153,  N'Cat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4154,  N'Accipiter nisus');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4154,  N'Sparrowhawk');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4155,  N'Mustela erminea');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4155,  N'nivalis');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4155,  N'Stoat');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4155,  N'Weasel');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4156,  N'Phalacrocorax aristotelis');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4156,  N'Shag');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (4157,  N'Cetacea');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (5623,  N'Present');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (5624,  N'Probable');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (5625,  N'Indeterminate');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (5626,  N'Absent');
INSERT INTO ontology_node_synonym (ontologynode_id, synonyms) VALUES (5627,  N'Not Recorded');
--------------------------- coding_rule ---------------------------
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14056,  N'46', NULL,  N'Lepus (jackrabbit)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14057,  N'59', NULL,  N'Cynomys', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14058,  N'70', NULL,  N'Geomydiae', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14059,  N'135', NULL,  N'Corvus brachyrhynchus (raven)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14060,  N'80', NULL,  N'Unidentfied rodent', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14061,  N'101', NULL,  N'Unidentified bird', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14062,  N'103', NULL,  N'Large bird (turkey-sized)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14063,  N'50', NULL,  N'Neotoma', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14064,  N'3', NULL,  N'Medium mammal (coyote/wolf)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14065,  N'65', NULL,  N'Peromyscus', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14066,  N'105', NULL,  N'Small bird (perching bird-sized)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14067,  N'110', NULL,  N'Turkey', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14068,  N'58', NULL,  N'Spermophilus or Cynonmys', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14069,  N'62', NULL,  N'Canis latrans (coyote)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14070,  N'120', NULL,  N'Buteo jamaicensis (red-tailed hawk)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14071,  N'20', NULL,  N'deer sized (cf deer)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14072,  N'104', NULL,  N'Medium bird (falcon-sized)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14073,  N'44', NULL,  N'lagomorph', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14074,  N'500', NULL,  N'Unknown', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14075,  N'10', NULL,  N'deer', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14076,  N'130', NULL,  N'Passeriformes', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14077,  N'49', NULL,  N'Sylvilagus (cottontail)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14078,  N'57', NULL,  N'Spermophilus variegatus', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14079,  N'1', NULL,  N'Very large mammal (bison/elk/domestic cow)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14080,  N'125', NULL,  N'Buteo sp. (small hawk)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14081,  N'18', NULL,  N'deer/antelope', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14082,  N'60', NULL,  N'Canis sp . (dog/coyote)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14083,  N'102', NULL,  N'Very large bird', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14084,  N'4', NULL,  N'Small mammal (rabbit/squirrel)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14085,  N'63', NULL,  N'cf. Canis', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14086,  N'5', NULL,  N'Very small mammal (mouse)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14087,  N'2', NULL,  N'Large mammal (deer/antelope)', 1656, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14442,  N'2', NULL,  N'Proximal epiphysis only', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14443,  N'99', NULL,  N'Not Applicable', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14444,  N'23', NULL,  N'Ilium without acetabulum', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14445,  N'28', NULL,  N'Ilium and Ischium present in portions with acetabulum', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14446,  N'9', NULL,  N'Distal end', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14447,  N'15', NULL,  N'Ilium - Ischium - Pubis present in portions - no acetabulum', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14448,  N'12', NULL,  N'Distal end and shaft fragment (broken)', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14449,  N'88', NULL,  N'Indeterminate', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14450,  N'14', NULL,  N'Complete Innominate', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14451,  N'1', NULL,  N'Complete', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14452,  N'11', NULL,  N'Distal shaft fragment', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14453,  N'26', NULL,  N'Ischium and pubis present in portions with acetabulum', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14454,  N'21', NULL,  N'Ischium without acetabulum', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14455,  N'4', NULL,  N'Proximal shaft minus proximal epiphysis (unfused)', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14456,  N'22', NULL,  N'Ilium with acetabulum', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14457,  N'17', NULL,  N'Pubis', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14458,  N'5', NULL,  N'Proximal shaft fragment', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14459,  N'16', NULL,  N'Ilium - Ischium - Pubis - Acetabulum at present in portions', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14460,  N'8', NULL,  N'Distal epiphysis only', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14461,  N'10', NULL,  N'Distal shaft minus distal epiphysis', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14462,  N'6', NULL,  N'Proximal end and shaft (broken)', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14463,  N'20', NULL,  N'Ischium with acetabulum', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14464,  N'25', NULL,  N'Acetabulum area only', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14465,  N'3', NULL,  N'Proximal end', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14466,  N'18', NULL,  N'Ilium', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14467,  N'24', NULL,  N'Pubis without acetabulum', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14468,  N'19', NULL,  N'Ischium', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14469,  N'7', NULL,  N'Shaft (both ends broken)', 449, NULL);
INSERT INTO coding_rule (id, code, description, term, coding_sheet_id, ontology_node_id) VALUES (14470,  N'27', NULL,  N'Ilium and pubis present in portions with acetabulum', 449, NULL);
--------------------------- bookmarked_resource ---------------------------
INSERT INTO bookmarked_resource (id, name, timestamp, person_id, resource_id) VALUES (123,  N'Bookmarked 139',  '2008-04-24 08:47:40.14', 121, 139);
INSERT INTO bookmarked_resource (id, name, timestamp, person_id, resource_id) VALUES (409,  N'Bookmarked 3074',  '2010-03-10 14:32:12.361', 1, 3074);
INSERT INTO bookmarked_resource (id, name, timestamp, person_id, resource_id) VALUES (411,  N'Bookmarked 3088',  '2010-03-10 14:49:46.561', 1, 3088);
INSERT INTO bookmarked_resource (id, name, timestamp, person_id, resource_id) VALUES (424,  N'Bookmarked 3074',  '2010-04-14 08:20:11.949', 8161, 3074);
INSERT INTO bookmarked_resource (id, name, timestamp, person_id, resource_id) VALUES (426,  N'Bookmarked 3088',  '2010-04-14 08:20:28.905', 8161, 3088);
INSERT INTO bookmarked_resource (id, name, timestamp, person_id, resource_id) VALUES (461,  N'Bookmarked 3074',  '2010-06-13 15:06:59.9', 8019, 3074);
INSERT INTO bookmarked_resource (id, name, timestamp, person_id, resource_id) VALUES (462,  N'Bookmarked 3088',  '2010-06-13 15:07:05.344', 8019, 3088);
INSERT INTO bookmarked_resource (id, name, timestamp, person_id, resource_id) VALUES (520,  N'Bookmarked 3088',  '2010-07-19 07:51:51.886', 8014, 3088);
INSERT INTO bookmarked_resource (id, name, timestamp, person_id, resource_id) VALUES (579,  N'Bookmarked 3088',  '2010-10-18 15:44:25.711', 8067, 3088);
INSERT INTO bookmarked_resource (id, name, timestamp, person_id, resource_id) VALUES (629,  N'Bookmark for tDAR resource:3738',  '2011-04-13 17:07:36.044', 8019, 3738);
--------------------------- coverage_date ---------------------------
INSERT INTO coverage_date (id, date_type, end_date, start_date, resource_id, start_aprox, end_aprox, description) VALUES (102,  N'CALENDAR_DATE', 1700, 1, 4287,  false ,  false , NULL);
INSERT INTO coverage_date (id, date_type, end_date, start_date, resource_id, start_aprox, end_aprox, description) VALUES (7,  N'CALENDAR_DATE', 1900, -12500, 262,  false ,  false , NULL);
INSERT INTO coverage_date (id, date_type, end_date, start_date, resource_id, start_aprox, end_aprox, description) VALUES (11,  N'CALENDAR_DATE', 1400, 850, 1628,  false ,  false , NULL);
INSERT INTO coverage_date (id, date_type, end_date, start_date, resource_id, start_aprox, end_aprox, description) VALUES (30,  N'CALENDAR_DATE', 2010, -8000, 3805,  false ,  false , NULL);
--------------------------- latitude_longitude ---------------------------
INSERT INTO latitude_longitude (id, maximum_latitude, maximum_longitude, minimum_latitude, minimum_longitude, resource_id, is_ok_to_show_exact_location, min_obfuscated_lat, min_obfuscated_long, max_obfuscated_lat, max_obfuscated_long) VALUES (181, 34.8994478300573, -108.932189941406, 34.7009774147201, -109.228820800781, 262,  false , NULL, NULL, NULL, NULL);
INSERT INTO latitude_longitude (id, maximum_latitude, maximum_longitude, minimum_latitude, minimum_longitude, resource_id, is_ok_to_show_exact_location, min_obfuscated_lat, min_obfuscated_long, max_obfuscated_lat, max_obfuscated_long) VALUES (6030, 35.17380831799959, -108.23593139648438, 35.030558627895005, -108.47557067871094, 2420,  false , NULL, NULL, NULL, NULL);
INSERT INTO latitude_longitude (id, maximum_latitude, maximum_longitude, minimum_latitude, minimum_longitude, resource_id, is_ok_to_show_exact_location, min_obfuscated_lat, min_obfuscated_long, max_obfuscated_lat, max_obfuscated_long) VALUES (3867, 35.149950854565155, -108.61358642578125, 35.06372505419625, -108.66439819335938, 1628,  false , NULL, NULL, NULL, NULL);
INSERT INTO latitude_longitude (id, maximum_latitude, maximum_longitude, minimum_latitude, minimum_longitude, resource_id, is_ok_to_show_exact_location, min_obfuscated_lat, min_obfuscated_long, max_obfuscated_lat, max_obfuscated_long) VALUES (6064, 59.44507509904714, 3.076171875, 49.38237278700955, -12.392578125, 3074,  false , NULL, NULL, NULL, NULL);
INSERT INTO latitude_longitude (id, maximum_latitude, maximum_longitude, minimum_latitude, minimum_longitude, resource_id, is_ok_to_show_exact_location, min_obfuscated_lat, min_obfuscated_long, max_obfuscated_lat, max_obfuscated_long) VALUES (6065, 55.825973254619015, -5.009765625, 51.45400691005981, -10.634765625, 3088,  false , NULL, NULL, NULL, NULL);
INSERT INTO latitude_longitude (id, maximum_latitude, maximum_longitude, minimum_latitude, minimum_longitude, resource_id, is_ok_to_show_exact_location, min_obfuscated_lat, min_obfuscated_long, max_obfuscated_lat, max_obfuscated_long) VALUES (6119, 38.81536866036827, -77.03510284423828, 38.79423249261199, -77.08076477050781, 3738,  false , NULL, NULL, NULL, NULL);
INSERT INTO latitude_longitude (id, maximum_latitude, maximum_longitude, minimum_latitude, minimum_longitude, resource_id, is_ok_to_show_exact_location, min_obfuscated_lat, min_obfuscated_long, max_obfuscated_lat, max_obfuscated_long) VALUES (6617, 34.65693354309436, -111.7477798461914, 34.60325873668768, -111.8521499633789, 4287,  false , NULL, NULL, NULL, NULL);
INSERT INTO latitude_longitude (id, maximum_latitude, maximum_longitude, minimum_latitude, minimum_longitude, resource_id, is_ok_to_show_exact_location, min_obfuscated_lat, min_obfuscated_long, max_obfuscated_lat, max_obfuscated_long) VALUES (6701, 21.3062947592286, -52.3209409849371, 21.2964093904814, -52.3296925634281, 3805,  false , NULL, NULL, NULL, NULL);
--------------------------- resource_creator ---------------------------
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1459,  N'LANDOWNER', 4, 11009, 1628);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1460,  N'SPONSOR', 14, 11003, 1628);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1461,  N'PERMITTER', 16, 11009, 1628);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1462,  N'REPOSITORY', 3, 11003, 1628);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1485,  N'SPONSOR', 14, 11157, 3805);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1486,  N'COLLABORATOR', 5, 11159, 3805);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1487,  N'COLLABORATOR', 5, 11110, 3805);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1488,  N'COLLABORATOR', 5, 11158, 3805);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1518,  N'SPONSOR', 14, 11153, 3738);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1542,  N'REPOSITORY', 3, 11003, 2420);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1544,  N'SPONSOR', 14, 11008, 2420);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1327,  N'SPONSOR', 14, 11001, 262);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1328,  N'LANDOWNER', 4, 11149, 262);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1329,  N'SPONSOR', 14, 11003, 262);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1345,  N'SPONSOR', 14, 11006, 139);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1565,  N'PROJECT_DIRECTOR', 13, 6, 262);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1566,  N'PRINCIPAL_INVESTIGATOR', 12, 6, 262);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1711,  N'PRINCIPAL_INVESTIGATOR', 12, 8009, 2420);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1712,  N'PRINCIPAL_INVESTIGATOR', 12, 8007, 2420);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1713,  N'PRINCIPAL_INVESTIGATOR', 12, 8008, 2420);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1732,  N'PRINCIPAL_INVESTIGATOR', 12, 6, 1628);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1743,  N'PROJECT_DIRECTOR', 13, 8426, 3805);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1744,  N'PROJECT_DIRECTOR', 13, 8425, 3805);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1745,  N'PRINCIPAL_INVESTIGATOR', 12, 8422, 3805);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1746,  N'PROJECT_DIRECTOR', 13, 8424, 3805);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1658,  N'CONTRIBUTOR', 6, 100, 139);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1543,  N'PREPARER', 10, 11037, 2420);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (1917,  N'CONTACT', 9, 8394, 3794);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (3265,  N'AUTHOR', 0, 8489, 4287);
INSERT INTO resource_creator (id, role, sequence_number, creator_id, resource_id) VALUES (3266,  N'AUTHOR', 1, 8621, 4287);
--------------------------- resource_access_statistics ---------------------------
--------------------------- information_resource_file_download_statistics ---------------------------
--------------------------- resource_annotation ---------------------------
--------------------------- resource_annotation_key ---------------------------
--------------------------- resource_note ---------------------------
-- ====================================================================================================================----
-- END extract from SQLExtract
-- ====================================================================================================================----
INSERT INTO collection (id, date_created, date_updated, description, name, orientation, sort_order, collection_type, visible, owner_id, parent_id, updater_id) VALUES (1575, '2013-07-01 16:49:15.583', '2013-07-01 16:49:15.583', 'this is a test', 'sample collection', 'LIST', 'TITLE', 'SHARED', true, 8092, NULL, 8092);
INSERT INTO resource (id, date_registered, date_updated, title, account_id , total_space_in_bytes, status, resource_type, total_files, previous_status, description, url, uploader_id, submitter_id, updater_id) VALUES (4289, '2013-07-01 16:42:32.355', '2013-07-01 16:42:32.355', 'this is a test ', NULL, 0, 'ACTIVE', 'SENSORY_DATA', 0, 'ACTIVE', 'sample sensorydata', '', 8092, 8092, 8092);
INSERT INTO resource (id, date_registered, date_updated, title, account_id , total_space_in_bytes, status, resource_type, total_files, previous_status, description, url, uploader_id, submitter_id, updater_id) VALUES (4290, '2013-07-01 16:44:49.712', '2013-07-01 16:44:49.712', 'this is a test', NULL, 0, 'ACTIVE', 'VIDEO', 0, 'ACTIVE', 'sample video', '', 8092, 8092, 8092);
INSERT INTO resource (id, date_registered, date_updated, title, account_id , total_space_in_bytes, status, resource_type, total_files, previous_status, description, url, uploader_id, submitter_id, updater_id) VALUES (4291, '2013-07-01 16:46:42.454', '2013-07-01 16:46:42.454', 'test', NULL, 0, 'ACTIVE', 'GEOSPATIAL', 0, 'ACTIVE', 'sample geospatial', '', 8092, 8092, 8092);
INSERT INTO resource (id, date_registered, date_updated, title, account_id , total_space_in_bytes, status, resource_type, total_files, previous_status, description, url, uploader_id, submitter_id, updater_id) VALUES (4292, '2013-07-01 17:21:35.881', '2013-07-01 17:21:35.881', 'this is a test', NULL, 0, 'ACTIVE', 'IMAGE', 0, 'ACTIVE', 'sample image', '', 8092, 8092, 8092);
INSERT INTO resource_access_statistics (date_accessed, resource_id) VALUES ('2013-07-01 16:42:34.693', 4289);
INSERT INTO resource_access_statistics (date_accessed, resource_id) VALUES ('2013-07-01 16:44:51.07', 4290);
INSERT INTO resource_access_statistics (date_accessed, resource_id) VALUES ('2013-07-01 16:46:43.537', 4291);
INSERT INTO resource_access_statistics ( date_accessed, resource_id) VALUES ('2013-07-01 16:46:43.537', 4292);
INSERT INTO information_resource (copy_location, date_created, date_created_normalized ,id, external_reference) VALUES (NULL, 2013, 2010, 4289, false);
INSERT INTO information_resource (copy_location, date_created, date_created_normalized ,id, external_reference) VALUES ('', 2012, 2010,   4290, false);
INSERT INTO information_resource (copy_location, date_created, date_created_normalized ,id, external_reference) VALUES (NULL, 2012, 2010, 4291, false);
INSERT INTO information_resource (copy_location, date_created, date_created_normalized ,id, external_reference) VALUES (NULL, 2012, 2010, 4292, false);
INSERT INTO dataset (id) values(4291);
INSERT INTO dataset (id) values(4289);
INSERT INTO sensory_data (id, turntable_used , mesh_holes_filled , mesh_rgb_included , mesh_smoothing , mesh_data_reduction , premesh_color_editions , premesh_overlap_reduction , premesh_smoothing , premesh_subsampling , rgb_preserved_from_original, mesh_color_editions , mesh_healing_despiking ) VALUES (4289,false,false,false,false,false,false,false,false,false,false,false, false);
INSERT INTO video (id) VALUES (4290);
INSERT INTO geospatial (currentnessUpdateNotes,map_source,id) VALUES ('recent', 'bogus information',4291);
INSERT INTO image VALUES (4292);
INSERT INTO collection_resource(resource_id, collection_id) VALUES(4289,1575),(4290,1575),(4291,1575),(4292,1575);
SELECT setval('category_variable_id_seq', (SELECT MAX(id) FROM category_variable)+1);
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
update creator set status='ACTIVE';
update culture_keyword set status='ACTIVE';
update geographic_keyword set status='ACTIVE';
update investigation_type set status='ACTIVE';
update material_keyword set status='ACTIVE';
update other_keyword set status='ACTIVE';
update site_name_keyword set status='ACTIVE';
update site_type_keyword set status='ACTIVE';
update temporal_keyword set status='ACTIVE';
insert into pos_billing_model (id, date_created, active, counting_files, counting_space, counting_resources) VALUES (1, now(), true, true, true, false);
insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price, min_allowed_files, model_id) values (true, 'error', 5,1,50,5, 55.21,400,1);
insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price, min_allowed_files, model_id) values (true, 'decline', 5,1,50,5, 55.11,400,1);
insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price, min_allowed_files, model_id) values (true, 'unknown', 5,1,50,5, 55.31,400,1);
insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price, min_allowed_files, model_id) values (false, 'inactive', 5,1,50,5, 550,400,1);
insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price, model_id) values (true, ' 1-  4', 1, 1, 10, 55,1);
insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price, model_id) values (true, ' 100 mb', 0, 0, 100, 50,1);
insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price, model_id) values (true, ' 5- 19', 1, 5, 10, 40,1);
insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price, model_id) values (true, '20- 49', 1, 20, 10, 35,1);
insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price, model_id) values (true, '50-500', 1, 50, 10, 31,1);
SELECT setval('pos_billing_model_id_seq', (SELECT MAX(id) FROM pos_billing_model)+1);
update pos_billing_activity set activity_type='PRODUCTION';
update pos_billing_activity set activity_type = 'TEST' where name in ('good','error', 'decline', 'unknown');
--update user_info set tos_version = 99, contributor_agreement_version = 99;
insert into user_notification(date_created, message_key,message_type,display_type) VALUES(now(), 'lithic.announce','SYSTEM_BROADCAST','NORMAL');
SELECT setval('user_notification_id_seq', (SELECT MAX(id) FROM user_notification)+1);
alter table resource_access_day_agg add month int;
alter table file_download_day_agg add month int;
update creator set browse_occurrence=10 where id=8161 or id=8092;
