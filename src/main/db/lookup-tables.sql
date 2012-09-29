/* project creator person roles */
insert into creator_person_role values (1, 'Principal Investigator');
insert into creator_person_role values (2, 'Project Director');
insert into creator_person_role values (3, 'Field Director');
insert into creator_person_role values (4, 'Lab Director');
insert into creator_person_role values (5, 'Primary Author');
insert into creator_person_role values (6, 'Secondary Author');
insert into creator_person_role values (7, 'Analyst');
insert into creator_person_role values (8, 'Primary Editor');
insert into creator_person_role values (9, 'Secondary Editor');
insert into creator_person_role values (10, 'Other');

/* project creator institution roles */
insert into creator_institution_role values (1, 'Sponsor');
insert into creator_institution_role values (2, 'Lead');
insert into creator_institution_role values (3, 'Collaborator');
insert into creator_institution_role values (4, 'Repository');
insert into creator_institution_role values (5, 'Other');

/* controlled choices for dcmi type (from dublin core) */
insert into dcmi_type (id, name, active) values (1, 'Dataset', 't');
insert into dcmi_type (id, name, active) values (2, 'Moving Image', 'f');
insert into dcmi_type (id, name, active) values (3, 'Software', 'f');
insert into dcmi_type (id, name, active) values (4, 'Still Image', 't');
insert into dcmi_type (id, name, active) values (5, 'Text', 't');
insert into dcmi_type (id, name, active) values (6, 'Service', 'f');
insert into dcmi_type (id, name, active) values (7, 'Sound', 'f');
insert into dcmi_type (id, name, active) values (8, 'Interactive Resource', 'f');


/* controlled choices for data format */
insert into information_resource_format (id, name, archival_format_id, mime_type) values(7, 'PostgreSQL', 7, 'text/plain');
insert into information_resource_format (id, name, archival_format_id, mime_type) values(0, 'Access', 7, 'application/x-msaccess');
insert into information_resource_format (id, name, archival_format_id, mime_type) values(1, 'ASCII', 1, 'text/plain');
insert into information_resource_format (id, name, archival_format_id, mime_type) values(2, 'Excel', 7, 'application/vnd.ms-excel');
insert into information_resource_format (id, name, archival_format_id, mime_type) values(3, 'CSV', 7, 'text/csv');
insert into information_resource_format (id, name, archival_format_id, mime_type) values(5, 'OWL', 5, 'application/rdf+xml');
insert into information_resource_format (id, name, archival_format_id, mime_type) values(6, 'PDF', 6, 'application/pdf');
insert into information_resource_format (id, name, archival_format_id, mime_type) values(8, 'Shapefiles', 8, 'application/octet-stream');
insert into information_resource_format (id, name, archival_format_id, mime_type) values(9, 'JPEG', 9, 'image/jpeg');
insert into information_resource_format (id, name, archival_format_id, mime_type) values(10, 'PNG', 10, 'image/png');
insert into information_resource_format (id, name, archival_format_id, mime_type) values(11, 'TIFF', 11, 'image/tiff');
insert into information_resource_format (id, name, archival_format_id, mime_type) values(12, 'GIF', 12, 'image/gif');

insert into information_resource_format_file_extension values (7, 'sql');
insert into information_resource_format_file_extension values (0, 'mdb');
insert into information_resource_format_file_extension values (1, 'txt');
insert into information_resource_format_file_extension values (2, 'xls');
insert into information_resource_format_file_extension values (3, 'csv');
insert into information_resource_format_file_extension values (5, 'owl');
insert into information_resource_format_file_extension values (6, 'pdf');
insert into information_resource_format_file_extension values (9, 'jpeg');
insert into information_resource_format_file_extension values (9, 'jpg');
insert into information_resource_format_file_extension values (10, 'png');
insert into information_resource_format_file_extension values (11, 'tiff');
insert into information_resource_format_file_extension values (12, 'gif');

/* choices for metadata languages */
insert into language values(0, 'English');
insert into language values(1, 'Spanish');
insert into language values(2, 'French');
insert into language values(3, 'German');
insert into language values(4, 'Dutch');
insert into language values(5, 'Chinese');
insert into language values(6, 'Turkish');

/* column encoding type */
insert into column_encoding_type values (0, 'Arbitrary integer');
insert into column_encoding_type values (1, 'Arbitrary real');
insert into column_encoding_type values (2, 'Arbitrary string');
insert into column_encoding_type values (3, 'Coded integer');
insert into column_encoding_type values (4, 'Coded real');
insert into column_encoding_type values (5, 'Coded string');
insert into column_encoding_type values (6, 'Measurement');
insert into column_encoding_type values (7, 'Other');
insert into column_encoding_type values (8, 'Count');


/* domain category variables to provide additional context*/
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (0, 'Faunal', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (1, 'Modification', 0, 'CATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (2, 'Natural_Modification', 1, 'CATEGORY', '0.1');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (3, 'Cultural_Modification', 1, 'CATEGORY', '0.1');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (4, 'Provenience_And_Context', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (5, 'Horizontal_Location', 4, 'CATEGORY', '4');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (6, 'Vertical_Position', 4, 'CATEGORY', '4');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (7, 'Recovery_Method', 4, 'CATEGORY', '4');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (8, 'Element', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (9, 'Taxon', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (10, 'Side', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (11, 'Portion', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (12, 'Proximal_Distal', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (13, 'Dorsal_Ventral', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (14, 'Fusion', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (15, 'Weight', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (16, 'Length', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (17, 'Count', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (18, 'Minimum_Count', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (19, 'Maximum_Count', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (20, 'Percent_Complete', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (21, 'Completeness', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (22, 'Breakage', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (23, 'Animal_Modification', 2, 'SUBCATEGORY', '0.1.2');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (24, 'Gnawing', 2, 'SUBCATEGORY', '0.1.2');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (25, 'Digestion', 2, 'SUBCATEGORY', '0.1.2');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (26, 'Weathering', 2, 'SUBCATEGORY', '0.1.2');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (27, 'Bone_Artifact', 3, 'SUBCATEGORY', '0.1.3');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (28, 'Butchering', 3, 'SUBCATEGORY', '0.1.3');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (29, 'Spiral_Fracture', 3, 'SUBCATEGORY', '0.1.3');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (30, 'Burning', 3, 'SUBCATEGORY', '0.1.3');

insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (31, 'Site', 5, 'SUBCATEGORY', '4.5');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (32, 'North',5, 'SUBCATEGORY', '4.5');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (33, 'East', 5, 'SUBCATEGORY', '4.5');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (34, 'Unit', 5, 'SUBCATEGORY', '4.5');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (35, 'Locus', 5, 'SUBCATEGORY', '4.5');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (36, 'Slash', 5, 'SUBCATEGORY', '4.5');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (37, 'Feature', 5, 'SUBCATEGORY', '4.5');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (38, 'Level', 6, 'SUBCATEGORY', '4.6');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (39, 'Stratum', 6, 'SUBCATEGORY', '4.6');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (40, 'Depth', 6, 'SUBCATEGORY', '4.6');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (41, 'Lot', 4, 'SUBCATEGORY', '4');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (42, 'Date', 4, 'SUBCATEGORY', '4');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (43, 'Context', 4, 'SUBCATEGORY', '4');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (44, 'Feature_Type', 4, 'SUBCATEGORY', '4');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (45, 'Screening', 7, 'SUBCATEGORY', '4.7');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (46, 'Excavation_Method', 7, 'SUBCATEGORY', '4.7');

insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (47, 'Ceramics', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (48, 'Chipped_Stone', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (49, 'Ground_Stone', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (50, 'Minerals', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (51, 'Architecture', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (52, 'Human_Skeletal', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (53, 'Burial', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (54, 'Excavated_Volumes', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (55, 'Historic_Artifacts', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (56, 'Macrobotanical', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (57, 'Pollen', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (58, 'Figurine', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (59, 'Shell', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (60, 'Lookup', NULL, 'CATEGORY', NULL);
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (61, 'Dating_Sample', NULL, 'CATEGORY', NULL);

/* subcategories under Figurine (58)*/
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (71, 'Type', 58, 'SUBCATEGORY', '58');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (72, 'Form', 58, 'SUBCATEGORY', '58');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (73, 'Material', 58, 'SUBCATEGORY', '58');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (74, 'Count', 58, 'SUBCATEGORY', '58');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (75, 'Other', 58, 'SUBCATEGORY', '58');
 
/* subcategories under Shell (59) */
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (81, 'Taxon', 59, 'SUBCATEGORY', '59');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (82, 'Modification', 59, 'SUBCATEGORY', '59');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (83, 'Measurement', 59, 'SUBCATEGORY', '59');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (84, 'Count', 59, 'SUBCATEGORY', '59');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (85, 'Weight', 59, 'SUBCATEGORY', '59');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (86, 'Other', 59, 'SUBCATEGORY', '59');

/* subcategories under Lookup (60) */
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (91, 'Code', 60, 'SUBCATEGORY', '60');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (92, 'Label', 60, 'SUBCATEGORY', '60');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (93, 'Description', 60, 'SUBCATEGORY', '60');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (94, 'Notes', 60, 'SUBCATEGORY', '60');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (95, 'Other', 60, 'SUBCATEGORY', '60');
   
/* subcategories under Dating Sample (61) */
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (101, 'Date', 61, 'SUBCATEGORY', '61');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (102, 'Error', 61, 'SUBCATEGORY', '61');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (103, 'Other', 61, 'SUBCATEGORY', '61');

/* add subcategories under Ceramics (47)*/
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (111, 'Ware', 47, 'SUBCATEGORY', '47');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (112, 'Type', 47, 'SUBCATEGORY', '47');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (113, 'Form', 47, 'SUBCATEGORY', '47');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (114, 'Part', 47, 'SUBCATEGORY', '47');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (115, 'Design', 47, 'SUBCATEGORY', '47');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (116, 'Count', 47, 'SUBCATEGORY', '47');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (117, 'Weight', 47, 'SUBCATEGORY', '47');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (118, 'Other', 47, 'SUBCATEGORY', '47');

/* add subcategories under Chipped Stone (48) */
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (121, 'Material', 48, 'SUBCATEGORY', '48');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (122, 'Form', 48, 'SUBCATEGORY', '48');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (123, 'Count', 48, 'SUBCATEGORY', '48');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (124, 'Weight', 48, 'SUBCATEGORY', '48');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (125, 'Measurement', 48, 'SUBCATEGORY', '48');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (126, 'Other', 48, 'SUBCATEGORY', '48');

/* add subcategories under Ground Stone (49) */
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (131, 'Material', 49, 'SUBCATEGORY', '49');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (132, 'Form', 49, 'SUBCATEGORY', '49');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (133, 'Count', 49, 'SUBCATEGORY', '49');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (134, 'Weight', 49, 'SUBCATEGORY', '49');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (135, 'Measurement', 49, 'SUBCATEGORY', '49');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (136, 'Other', 49, 'SUBCATEGORY', '49');

/* add subcategories under Historic Artifact (55) */
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (141, 'Type', 55, 'SUBCATEGORY', '55');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (142, 'Count', 55, 'SUBCATEGORY', '55');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (143, 'Weight', 55, 'SUBCATEGORY', '55');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (144, 'Other', 55, 'SUBCATEGORY', '55');

/* add subcategories under Macrobotanical (56) */
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (151, 'Taxon', 56, 'SUBCATEGORY', '56');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (152, 'Count', 56, 'SUBCATEGORY', '56');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (153, 'Other', 56, 'SUBCATEGORY', '56');

/* add subcategories under Pollen (57) */
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (161, 'Taxon', 57, 'SUBCATEGORY', '57');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (162, 'Count', 57, 'SUBCATEGORY', '57');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (163, 'Other', 57, 'SUBCATEGORY', '57');

/* add subcategories under faunal (0)*/
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (171, 'Cultural_Modification', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (172, 'Excavated_Volumes', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (173, 'Horizontal_Location', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (174, 'Modification', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (175, 'Natural_Modification', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (176, 'Recovery_Method', 0, 'SUBCATEGORY', '0');
insert into category_variable (id, name, parent_id, type, encoded_parent_ids) values (177, 'Vertical_Position', 0, 'SUBCATEGORY', '0');


/* master ontology domain context variable synonyms */
insert into category_variable_synonyms values (8, 'Elements');
insert into category_variable_synonyms values (9, 'Species');
insert into category_variable_synonyms values (10, 'Symmetry');

insert into category_variable_synonyms values (17, 'Quantity');
insert into category_variable_synonyms values (17, 'Number');

insert into category_variable_synonyms values (21, 'Amount Present');
insert into category_variable_synonyms values (22, 'Fragmentation');
insert into category_variable_synonyms values (34, 'Room');
insert into category_variable_synonyms values (34, 'Operation');

insert into category_variable_synonyms values (36, 'Slash No');
insert into category_variable_synonyms values (37, 'Feature No');
insert into category_variable_synonyms values (37, 'FN');
insert into category_variable_synonyms values (37, 'Cluster');
insert into category_variable_synonyms values (41, 'Lot No');
insert into category_variable_synonyms values (41, 'Spec');
insert into category_variable_synonyms values (41, 'Spec No');
insert into category_variable_synonyms values (41, 'Log');
insert into category_variable_synonyms values (41, 'Log No');
insert into category_variable_synonyms values (41, 'Bag');
insert into category_variable_synonyms values (41, 'Bag No');
insert into category_variable_synonyms values (41, 'Case');
insert into category_variable_synonyms values (41, 'Case No');
insert into category_variable_synonyms values (42, 'Time');
insert into category_variable_synonyms values (42, 'Age');
insert into category_variable_synonyms values (43, 'Courtyard');
insert into category_variable_synonyms values (43, 'Feat# Cat#');
insert into category_variable_synonyms values (46, 'Recovery Method');


/* measurement units */
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (0, 'kg', 'kilogram', 1000, 15);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (1, 'g', 'gram', 1, 15);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (2, 'mg', 'milligram', 0.001, 15);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (3, 'mcg', 'microgram', 0.000001, 15);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (4, 'km', 'kilometer', 1000, 16);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (5, 'm', 'meter', 1, 16);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (6, 'cm', 'centimeter', .01, 16);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (7, 'mm', 'millimeter', 0.001, 16);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (8, 'm2', 'square meter', 1, 4);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (9, 'ha', 'hectare', 1, 4);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (10, 'km2', 'square kilometer', 1, 4);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (11, 'ml', 'milliliter', 0.001, 4);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (12, 'cc', 'cubic centimeter', 1, 4);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (13, 'l', 'liter', 1, 4);
insert into measurement_unit (id, name, full_name, modifier, category_variable_id) values (14, 'ppm', 'parts per million', 1, 4);

/* insert into measurement_modifier (id, prefix, modifier) values (0, 'milli', 0.001) */

