--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Name: category_variable_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdar
--

SELECT pg_catalog.setval('category_variable_id_seq', 259, true);


--
-- Name: culture_keyword_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdar
--

SELECT pg_catalog.setval('culture_keyword_id_seq', 154, true);


--
-- Name: investigation_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdar
--

SELECT pg_catalog.setval('investigation_type_id_seq', 12, true);


--
-- Name: material_keyword_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdar
--

SELECT pg_catalog.setval('material_keyword_id_seq', 15, true);


--
-- Name: site_type_keyword_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdar
--

SELECT pg_catalog.setval('site_type_keyword_id_seq', 394, true);


--
-- Data for Name: category_variable; Type: TABLE DATA; Schema: public; Owner: tdar
--

SET SESSION AUTHORIZATION DEFAULT;

ALTER TABLE category_variable DISABLE TRIGGER ALL;

INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (1, NULL, 'Architecture', 'CATEGORY', NULL, 'Architecture');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (25, NULL, 'Material', 'SUBCATEGORY', 1, 'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (26, NULL, 'Measurement', 'SUBCATEGORY', 1, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (27, NULL, 'Style/Type', 'SUBCATEGORY', 1, 'Style Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (28, NULL, 'Count', 'SUBCATEGORY', 2, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (29, NULL, 'Design', 'SUBCATEGORY', 2, 'Design');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (30, NULL, 'Form', 'SUBCATEGORY', 2, 'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (31, NULL, 'Function', 'SUBCATEGORY', 2, 'Function');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (32, NULL, 'Material', 'SUBCATEGORY', 2, 'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (33, NULL, 'Measurement', 'SUBCATEGORY', 2, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (34, NULL, 'Technique', 'SUBCATEGORY', 2, 'Technique');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (35, NULL, 'Weight', 'SUBCATEGORY', 2, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (36, NULL, 'Composition', 'SUBCATEGORY', 3, 'Composition');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (37, NULL, 'Count', 'SUBCATEGORY', 3, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (38, NULL, 'Design/Decorative Element', 'SUBCATEGORY', 3, 'Design Decorative Element');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (39, NULL, 'Form', 'SUBCATEGORY', 3, 'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (40, NULL, 'Measurement', 'SUBCATEGORY', 3, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (41, NULL, 'Paint', 'SUBCATEGORY', 3, 'Paint');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (42, NULL, 'Part', 'SUBCATEGORY', 3, 'Part');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (43, NULL, 'Paste', 'SUBCATEGORY', 3, 'Paste');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (44, NULL, 'Residue', 'SUBCATEGORY', 3, 'Residue');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (45, NULL, 'Surface Treatment', 'SUBCATEGORY', 3, 'Surface Treatment');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (46, NULL, 'Temper/Inclusions', 'SUBCATEGORY', 3, 'Temper Inclusions');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (47, NULL, 'Type', 'SUBCATEGORY', 3, 'Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (48, NULL, 'Variety/Subtype', 'SUBCATEGORY', 3, 'Variety Subtype');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (49, NULL, 'Ware', 'SUBCATEGORY', 3, 'Ware');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (50, NULL, 'Weight', 'SUBCATEGORY', 3, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (51, NULL, 'Count', 'SUBCATEGORY', 4, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (52, NULL, 'Form', 'SUBCATEGORY', 4, 'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (53, NULL, 'Material', 'SUBCATEGORY', 4, 'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (54, NULL, 'Measurement', 'SUBCATEGORY', 4, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (55, NULL, 'Retouch', 'SUBCATEGORY', 4, 'Retouch');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (56, NULL, 'Type', 'SUBCATEGORY', 4, 'Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (57, NULL, 'Weight', 'SUBCATEGORY', 4, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (58, NULL, 'Method', 'SUBCATEGORY', 5, 'Method');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (59, NULL, 'Date', 'SUBCATEGORY', 5, 'Date');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (60, NULL, 'Error', 'SUBCATEGORY', 5, 'Error');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (61, NULL, 'Age', 'SUBCATEGORY', 6, 'Age');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (62, NULL, 'Anterior/Posterior', 'SUBCATEGORY', 6, 'Anterior Posterior');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (63, NULL, 'Bone Artifact Form', 'SUBCATEGORY', 6, 'Bone Artifact Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (64, NULL, 'Breakage', 'SUBCATEGORY', 6, 'Breakage');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (65, NULL, 'Burning ', 'SUBCATEGORY', 6, 'Burning ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (66, NULL, 'Butchering', 'SUBCATEGORY', 6, 'Butchering');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (67, NULL, 'Completeness', 'SUBCATEGORY', 6, 'Completeness');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (68, NULL, 'Condition', 'SUBCATEGORY', 6, 'Condition');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (69, NULL, 'Count', 'SUBCATEGORY', 6, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (70, NULL, 'Cultural Modification', 'SUBCATEGORY', 6, 'Cultural Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (71, NULL, 'Digestion', 'SUBCATEGORY', 6, 'Digestion');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (72, NULL, 'Dorsal/Ventral', 'SUBCATEGORY', 6, 'Dorsal Ventral');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (73, NULL, 'Element', 'SUBCATEGORY', 6, 'Element');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (74, NULL, 'Erosion', 'SUBCATEGORY', 6, 'Erosion');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (75, NULL, 'Fusion', 'SUBCATEGORY', 6, 'Fusion');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (76, NULL, 'Gnawing/Animal Modification', 'SUBCATEGORY', 6, 'Gnawing Animal Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (77, NULL, 'Measurement', 'SUBCATEGORY', 6, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (78, NULL, 'Modification', 'SUBCATEGORY', 6, 'Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (79, NULL, 'Natural Modification', 'SUBCATEGORY', 6, 'Natural Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (80, NULL, 'Pathologies', 'SUBCATEGORY', 6, 'Pathologies');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (81, NULL, 'Portion/Proximal/Distal', 'SUBCATEGORY', 6, 'Portion Proximal Distal');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (82, NULL, 'Sex', 'SUBCATEGORY', 6, 'Sex');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (83, NULL, 'Side', 'SUBCATEGORY', 6, 'Side');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (84, NULL, 'Spiral Fracture', 'SUBCATEGORY', 6, 'Spiral Fracture');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (85, NULL, 'Taxon', 'SUBCATEGORY', 6, 'Taxon');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (86, NULL, 'Weathering', 'SUBCATEGORY', 6, 'Weathering');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (87, NULL, 'Weight', 'SUBCATEGORY', 6, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (88, NULL, 'Zone', 'SUBCATEGORY', 6, 'Zone');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (89, NULL, 'Zone Scheme', 'SUBCATEGORY', 6, 'Zone Scheme');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (90, NULL, 'Count', 'SUBCATEGORY', 7, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (91, NULL, 'Form', 'SUBCATEGORY', 7, 'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (92, NULL, 'Material', 'SUBCATEGORY', 7, 'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (93, NULL, 'Measurement', 'SUBCATEGORY', 7, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (94, NULL, 'Style/Type', 'SUBCATEGORY', 7, 'Style Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (95, NULL, 'Count', 'SUBCATEGORY', 8, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (96, NULL, 'Date', 'SUBCATEGORY', 8, 'Date');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (97, NULL, 'Form', 'SUBCATEGORY', 8, 'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (98, NULL, 'Maker/Manufacturer', 'SUBCATEGORY', 8, 'Maker Manufacturer');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (99, NULL, 'Material', 'SUBCATEGORY', 8, 'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (100, NULL, 'Measurement', 'SUBCATEGORY', 8, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (101, NULL, 'Weight', 'SUBCATEGORY', 8, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (102, NULL, 'Completeness', 'SUBCATEGORY', 9, 'Completeness');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (103, NULL, 'Count', 'SUBCATEGORY', 9, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (104, NULL, 'Form', 'SUBCATEGORY', 9, 'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (105, NULL, 'Material', 'SUBCATEGORY', 9, 'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (106, NULL, 'Measurement', 'SUBCATEGORY', 9, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (107, NULL, 'Weight', 'SUBCATEGORY', 9, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (108, NULL, 'Count', 'SUBCATEGORY', 10, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (109, NULL, 'Date', 'SUBCATEGORY', 10, 'Date');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (110, NULL, 'Form', 'SUBCATEGORY', 10, 'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (111, NULL, 'Maker/Manufacturer', 'SUBCATEGORY', 10, 'Maker Manufacturer');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (112, NULL, 'Material', 'SUBCATEGORY', 10, 'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (113, NULL, 'Measurement', 'SUBCATEGORY', 10, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (114, NULL, 'Weight', 'SUBCATEGORY', 10, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (115, NULL, 'Body Position/Flexure', 'SUBCATEGORY', 11, 'Body Position Flexure');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (116, NULL, 'Body Posture', 'SUBCATEGORY', 11, 'Body Posture');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (117, NULL, 'Body Preparation', 'SUBCATEGORY', 11, 'Body Preparation');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (118, NULL, 'Burial Accompaniment', 'SUBCATEGORY', 11, 'Burial Accompaniment');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (119, NULL, 'Burial Container ', 'SUBCATEGORY', 11, 'Burial Container ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (120, NULL, 'Burial Facility', 'SUBCATEGORY', 11, 'Burial Facility');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (121, NULL, 'Count', 'SUBCATEGORY', 11, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (122, NULL, 'Disturbance', 'SUBCATEGORY', 11, 'Disturbance');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (123, NULL, 'Facing', 'SUBCATEGORY', 11, 'Facing');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (124, NULL, 'Measurement', 'SUBCATEGORY', 11, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (125, NULL, 'Orientation/Alignment ', 'SUBCATEGORY', 11, 'Orientation Alignment ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (126, NULL, 'Preservation', 'SUBCATEGORY', 11, 'Preservation');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (127, NULL, 'Type of Interment', 'SUBCATEGORY', 11, 'Type of Interment');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (128, NULL, 'Buccal/Lingual/Occlusal', 'SUBCATEGORY', 12, 'Buccal Lingual Occlusal');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (129, NULL, 'Chemical Assay', 'SUBCATEGORY', 12, 'Chemical Assay');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (130, NULL, 'Count   ', 'SUBCATEGORY', 12, 'Count   ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (131, NULL, 'Cultural Modification', 'SUBCATEGORY', 12, 'Cultural Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (132, NULL, 'Dental Pathologies', 'SUBCATEGORY', 12, 'Dental Pathologies');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (133, NULL, 'Dental Wear', 'SUBCATEGORY', 12, 'Dental Wear');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (134, NULL, 'Enamel Defects', 'SUBCATEGORY', 12, 'Enamel Defects');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (135, NULL, 'Maxillary/Mandibular', 'SUBCATEGORY', 12, 'Maxillary Mandibular');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (136, NULL, 'Measurement', 'SUBCATEGORY', 12, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (137, NULL, 'Permanent/Deciduous', 'SUBCATEGORY', 12, 'Permanent Deciduous');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (138, NULL, 'Tooth (element)', 'SUBCATEGORY', 12, 'Tooth (element)');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (139, NULL, 'Age', 'SUBCATEGORY', 13, 'Age');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (140, NULL, 'Age Criteria', 'SUBCATEGORY', 13, 'Age Criteria');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (141, NULL, 'Articulation', 'SUBCATEGORY', 13, 'Articulation');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (142, NULL, 'Bone Segment (proximal/distal)', 'SUBCATEGORY', 13, 'Bone Segment (proximal distal)');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (143, NULL, 'Chemical Assay', 'SUBCATEGORY', 13, 'Chemical Assay');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (144, NULL, 'Completeness', 'SUBCATEGORY', 13, 'Completeness');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (145, NULL, 'Condition', 'SUBCATEGORY', 13, 'Condition');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (146, NULL, 'Count', 'SUBCATEGORY', 13, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (147, NULL, 'Cranial Deformation', 'SUBCATEGORY', 13, 'Cranial Deformation');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (148, NULL, 'Crematory Burning', 'SUBCATEGORY', 13, 'Crematory Burning');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (149, NULL, 'Cultural Modification ', 'SUBCATEGORY', 13, 'Cultural Modification ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (150, NULL, 'Diet', 'SUBCATEGORY', 13, 'Diet');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (151, NULL, 'Distrubance', 'SUBCATEGORY', 13, 'Distrubance');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (152, NULL, 'Disturbance sources', 'SUBCATEGORY', 13, 'Disturbance sources');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (153, NULL, 'Element', 'SUBCATEGORY', 13, 'Element');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (154, NULL, 'Epiphyseal Union', 'SUBCATEGORY', 13, 'Epiphyseal Union');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (155, NULL, 'Fracture/Breakage', 'SUBCATEGORY', 13, 'Fracture Breakage');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (156, NULL, 'Health ', 'SUBCATEGORY', 13, 'Health ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (157, NULL, 'Measurement', 'SUBCATEGORY', 13, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (158, NULL, 'Nonmetric Trait', 'SUBCATEGORY', 13, 'Nonmetric Trait');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (159, NULL, 'Pathologies/Trauma', 'SUBCATEGORY', 13, 'Pathologies Trauma');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (160, NULL, 'Preservation', 'SUBCATEGORY', 13, 'Preservation');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (161, NULL, 'Sex', 'SUBCATEGORY', 13, 'Sex');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (162, NULL, 'Sex criteria', 'SUBCATEGORY', 13, 'Sex criteria');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (163, NULL, 'Side', 'SUBCATEGORY', 13, 'Side');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (164, NULL, 'Weight', 'SUBCATEGORY', 13, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (165, NULL, 'Code', 'SUBCATEGORY', 14, 'Code');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (166, NULL, 'Description', 'SUBCATEGORY', 14, 'Description');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (167, NULL, 'Label', 'SUBCATEGORY', 14, 'Label');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (168, NULL, 'Notes', 'SUBCATEGORY', 14, 'Notes');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (169, NULL, 'Count', 'SUBCATEGORY', 15, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (170, NULL, 'Taxon', 'SUBCATEGORY', 15, 'Taxon');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (171, NULL, 'Count', 'SUBCATEGORY', 16, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (172, NULL, 'Date', 'SUBCATEGORY', 16, 'Date');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (173, NULL, 'Form', 'SUBCATEGORY', 16, 'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (174, NULL, 'Maker/Manufacturer', 'SUBCATEGORY', 16, 'Maker Manufacturer');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (175, NULL, 'Material', 'SUBCATEGORY', 16, 'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (176, NULL, 'Measurement', 'SUBCATEGORY', 16, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (177, NULL, 'Weight', 'SUBCATEGORY', 16, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (178, NULL, 'Count', 'SUBCATEGORY', 17, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (179, NULL, 'Form', 'SUBCATEGORY', 17, 'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (180, NULL, 'Measurement', 'SUBCATEGORY', 17, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (181, NULL, 'Mineral Type', 'SUBCATEGORY', 17, 'Mineral Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (182, NULL, 'Weight', 'SUBCATEGORY', 17, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (183, NULL, 'Direction', 'SUBCATEGORY', 18, 'Direction');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (184, NULL, 'Film Type', 'SUBCATEGORY', 18, 'Film Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (185, NULL, 'Frame', 'SUBCATEGORY', 18, 'Frame');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (186, NULL, 'ID', 'SUBCATEGORY', 18, 'ID');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (187, NULL, 'Roll', 'SUBCATEGORY', 18, 'Roll');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (188, NULL, 'Subject', 'SUBCATEGORY', 18, 'Subject');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (189, NULL, 'Count', 'SUBCATEGORY', 19, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (190, NULL, 'Taxon', 'SUBCATEGORY', 19, 'Taxon');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (191, NULL, 'Context', 'SUBCATEGORY', 20, 'Context');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (192, NULL, 'Date', 'SUBCATEGORY', 20, 'Date');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (193, NULL, 'Depth', 'SUBCATEGORY', 20, 'Depth');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (194, NULL, 'East', 'SUBCATEGORY', 20, 'East');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (195, NULL, 'Excavation Method', 'SUBCATEGORY', 20, 'Excavation Method');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (196, NULL, 'Feature ID/Number', 'SUBCATEGORY', 20, 'Feature ID Number');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (197, NULL, 'Feature Type', 'SUBCATEGORY', 20, 'Feature Type');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (198, NULL, 'Horizontal Location', 'SUBCATEGORY', 20, 'Horizontal Location');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (199, NULL, 'Inclusions', 'SUBCATEGORY', 20, 'Inclusions');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (200, NULL, 'Item/Slash ', 'SUBCATEGORY', 20, 'Item Slash ');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (201, NULL, 'Level', 'SUBCATEGORY', 20, 'Level');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (202, NULL, 'Locus', 'SUBCATEGORY', 20, 'Locus');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (203, NULL, 'Lot', 'SUBCATEGORY', 20, 'Lot');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (204, NULL, 'Measurement', 'SUBCATEGORY', 20, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (205, NULL, 'North', 'SUBCATEGORY', 20, 'North');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (206, NULL, 'Project', 'SUBCATEGORY', 20, 'Project');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (207, NULL, 'Recovery Method', 'SUBCATEGORY', 20, 'Recovery Method');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (208, NULL, 'Sampling', 'SUBCATEGORY', 20, 'Sampling');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (209, NULL, 'Screening', 'SUBCATEGORY', 20, 'Screening');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (210, NULL, 'Site', 'SUBCATEGORY', 20, 'Site');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (211, NULL, 'Soil Color', 'SUBCATEGORY', 20, 'Soil Color');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (212, NULL, 'Stratum', 'SUBCATEGORY', 20, 'Stratum');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (213, NULL, 'Unit', 'SUBCATEGORY', 20, 'Unit');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (214, NULL, 'Vertical Position', 'SUBCATEGORY', 20, 'Vertical Position');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (215, NULL, 'Volume', 'SUBCATEGORY', 20, 'Volume');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (216, NULL, 'Exposure', 'SUBCATEGORY', 21, 'Exposure');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (217, NULL, 'Form', 'SUBCATEGORY', 21, 'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (218, NULL, 'Style', 'SUBCATEGORY', 21, 'Style');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (219, NULL, 'Technology', 'SUBCATEGORY', 21, 'Technology');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (220, NULL, 'Completeness', 'SUBCATEGORY', 22, 'Completeness');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (221, NULL, 'Count', 'SUBCATEGORY', 22, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (222, NULL, 'Measurement', 'SUBCATEGORY', 22, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (223, NULL, 'Modification', 'SUBCATEGORY', 22, 'Modification');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (224, NULL, 'Taxon', 'SUBCATEGORY', 22, 'Taxon');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (225, NULL, 'Weight', 'SUBCATEGORY', 22, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (226, NULL, 'Box Number', 'SUBCATEGORY', 23, 'Box Number');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (227, NULL, 'Location', 'SUBCATEGORY', 23, 'Location');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (228, NULL, 'Count', 'SUBCATEGORY', 24, 'Count');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (229, NULL, 'Design', 'SUBCATEGORY', 24, 'Design');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (230, NULL, 'Form', 'SUBCATEGORY', 24, 'Form');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (231, NULL, 'Function', 'SUBCATEGORY', 24, 'Function');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (232, NULL, 'Material', 'SUBCATEGORY', 24, 'Material');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (233, NULL, 'Measurement', 'SUBCATEGORY', 24, 'Measurement');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (234, NULL, 'Technique', 'SUBCATEGORY', 24, 'Technique');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (235, NULL, 'Weight', 'SUBCATEGORY', 24, 'Weight');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (236, NULL, 'Other', 'SUBCATEGORY', 1, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (237, NULL, 'Other', 'SUBCATEGORY', 2, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (238, NULL, 'Other', 'SUBCATEGORY', 3, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (239, NULL, 'Other', 'SUBCATEGORY', 4, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (240, NULL, 'Other', 'SUBCATEGORY', 5, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (241, NULL, 'Other', 'SUBCATEGORY', 6, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (242, NULL, 'Other', 'SUBCATEGORY', 7, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (243, NULL, 'Other', 'SUBCATEGORY', 8, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (244, NULL, 'Other', 'SUBCATEGORY', 9, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (245, NULL, 'Other', 'SUBCATEGORY', 10, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (246, NULL, 'Other', 'SUBCATEGORY', 11, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (247, NULL, 'Other', 'SUBCATEGORY', 12, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (248, NULL, 'Other', 'SUBCATEGORY', 13, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (249, NULL, 'Other', 'SUBCATEGORY', 14, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (250, NULL, 'Other', 'SUBCATEGORY', 15, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (251, NULL, 'Other', 'SUBCATEGORY', 16, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (252, NULL, 'Other', 'SUBCATEGORY', 17, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (253, NULL, 'Other', 'SUBCATEGORY', 18, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (254, NULL, 'Other', 'SUBCATEGORY', 19, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (255, NULL, 'Other', 'SUBCATEGORY', 20, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (256, NULL, 'Other', 'SUBCATEGORY', 21, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (257, NULL, 'Other', 'SUBCATEGORY', 22, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (258, NULL, 'Other', 'SUBCATEGORY', 23, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (259, NULL, 'Other', 'SUBCATEGORY', 24, 'Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (2, NULL, 'Basketry', 'CATEGORY', NULL, 'Basketry');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (4, NULL, 'Chipped Stone', 'CATEGORY', NULL, 'Chipped Stone');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (5, NULL, 'Dating Sample', 'CATEGORY', NULL, 'Dating Sample');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (6, NULL, 'Fauna', 'CATEGORY', NULL, 'Fauna');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (7, NULL, 'Figurine', 'CATEGORY', NULL, 'Figurine');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (8, NULL, 'Glass', 'CATEGORY', NULL, 'Glass');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (9, NULL, 'Ground Stone', 'CATEGORY', NULL, 'Ground Stone');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (10, NULL, 'Historic Other', 'CATEGORY', NULL, 'Historic Other');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (11, NULL, 'Human Burial', 'CATEGORY', NULL, 'Human Burial');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (12, NULL, 'Human Dental', 'CATEGORY', NULL, 'Human Dental');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (13, NULL, 'Human Skeletal', 'CATEGORY', NULL, 'Human Skeletal');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (14, NULL, 'Lookup', 'CATEGORY', NULL, 'Lookup');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (15, NULL, 'Macrobotanical', 'CATEGORY', NULL, 'Macrobotanical');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (16, NULL, 'Metal', 'CATEGORY', NULL, 'Metal');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (17, NULL, 'Mineral', 'CATEGORY', NULL, 'Mineral');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (18, NULL, 'Photograph', 'CATEGORY', NULL, 'Photograph');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (19, NULL, 'Pollen', 'CATEGORY', NULL, 'Pollen');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (20, NULL, 'Provenience and Context', 'CATEGORY', NULL, 'Provenience and Context');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (21, NULL, 'Rock Art', 'CATEGORY', NULL, 'Rock Art');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (22, NULL, 'Shell', 'CATEGORY', NULL, 'Shell');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (23, NULL, 'Storage', 'CATEGORY', NULL, 'Storage');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (24, NULL, 'Textile', 'CATEGORY', NULL, 'Textile');
INSERT INTO category_variable (id, description, name, type, parent_id, label) VALUES (3, NULL, 'Ceramic', 'CATEGORY', NULL, 'Ceramic');


ALTER TABLE category_variable ENABLE TRIGGER ALL;

--
-- Data for Name: category_variable_synonyms; Type: TABLE DATA; Schema: public; Owner: tdar
--

ALTER TABLE category_variable_synonyms DISABLE TRIGGER ALL;

INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (73, 'Elements');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (85, 'Species');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (83, 'Symmetry');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (69, 'Quantity');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (69, 'Number');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (67, 'Amount Present');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (64, 'Fragmentation');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (198, 'Room');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (198, 'Operation');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (200, 'Slash No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (198, 'Feature No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (198, 'FN');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (198, 'Cluster');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203, 'Lot No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203, 'Spec');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203, 'Spec No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203, 'Log');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203, 'Log No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203, 'Bag');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203, 'Bag No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203, 'Case');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (203, 'Case No');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (192, 'Time');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (192, 'Age');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (191, 'Courtyard');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (191, 'Feat# Cat#');
INSERT INTO category_variable_synonyms (categoryvariable_id, synonyms) VALUES (207, 'Recovery Method');


ALTER TABLE category_variable_synonyms ENABLE TRIGGER ALL;

--
-- Data for Name: culture_keyword; Type: TABLE DATA; Schema: public; Owner: tdar
--

ALTER TABLE culture_keyword DISABLE TRIGGER ALL;

INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (1, NULL, '', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (2, NULL, 'Viking', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (3, NULL, 'Pre-Clovis', true, '1', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (4, NULL, 'PaleoIndian', true, '2', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (8, NULL, 'Archaic', true, '3', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (12, NULL, 'Hopewell', true, '4', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (13, NULL, 'Woodland', true, '5', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (17, NULL, 'Plains Village', true, '6', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (18, NULL, 'Mississippian', true, '7', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (19, NULL, 'Ancestral Puebloan', true, '8', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (35, NULL, 'Contact Period', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (21, NULL, 'Hohokam', true, '9', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (36, NULL, 'French', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (23, NULL, 'Patayan', true, '11', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (24, NULL, 'Fremont', true, '12', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (25, NULL, 'Historic', true, '13', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (33, NULL, 'Halaf', false, '', false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (34, NULL, 'Mogollon', true, '10', true, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (37, NULL, 'Montagnais', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (38, NULL, 'Historic Period', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (39, NULL, 'Cibola', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (40, NULL, 'Zuni', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (41, NULL, 'Early medieval', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (42, NULL, 'Middle Saxon', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (43, NULL, 'English', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (44, NULL, 'Virgin Anasazi', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (45, NULL, 'Paiute', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (46, NULL, 'Mohave', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (47, NULL, 'Hualapai', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (48, NULL, 'Native Pacific Islander', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (49, NULL, 'Papago', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (50, NULL, 'Yurok', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (51, NULL, 'Chilula', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (52, NULL, 'Sinagua', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (53, NULL, 'Miwok', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (54, NULL, 'Bribrí', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (55, NULL, 'pre-columbian', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (56, NULL, 'columbian', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (57, NULL, 'Chiriquí ', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (58, NULL, 'Tropical Forest Archaic', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (59, NULL, 'Concepción Phase', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (60, NULL, 'Chibchan', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (62, NULL, 'Shoshonean', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (63, NULL, 'Basketmaker', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (64, NULL, 'Niwok', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (65, NULL, 'Maya', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (66, NULL, 'Shoshone', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (67, NULL, 'Leprosy', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (68, NULL, 'Leper Colony', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (69, NULL, 'Hawaiian', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (70, NULL, 'Salado', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (71, NULL, 'Yavapai', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (72, NULL, 'Apache', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (73, NULL, 'Southern Paiute', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (74, NULL, 'Micronesian', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (75, NULL, 'Chamorro', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (76, NULL, 'Colonial Japanese', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (77, NULL, 'Protohistoric', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (78, NULL, 'Trincheras', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (79, NULL, 'Ute', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (80, NULL, 'Cheyenne', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (81, NULL, 'Arapahoe', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (82, NULL, 'Yuman', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (83, NULL, 'Mesoamerican', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (84, NULL, 'Anasazi', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (85, NULL, 'Aztec', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (86, NULL, 'Effigy Mound', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (87, NULL, 'Olmec', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (88, NULL, 'Chacoan', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (89, NULL, 'Classic Maya', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (90, NULL, 'Mohawk Iroquois', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (91, NULL, 'Mesoamerica', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (92, NULL, 'Icelandic', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (93, NULL, 'formative', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (94, NULL, 'chiripa', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (95, NULL, 'tiwanaku', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (96, NULL, 'inka', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (97, NULL, 'pacajes', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (98, NULL, 'Nahua', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (99, NULL, 'Classic Period', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (100, NULL, 'Crow', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (102, NULL, 'chiipa', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (103, NULL, 'Roman', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (104, NULL, 'British', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (105, NULL, 'Western Apache', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (106, NULL, 'Hakataya', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (107, NULL, 'Southern Sinagua', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (108, NULL, 'Mimbres', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (109, NULL, 'Thule', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (110, NULL, 'Historic Inughuit', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (111, NULL, 'Palaeoeskimo', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (112, NULL, 'Perry Mesa Tradition', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (5, NULL, 'Clovis', true, '2.1', true, 4);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (6, NULL, 'Folsom', true, '2.2', true, 4);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (7, NULL, 'Dalton', true, '2.3', true, 4);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (9, NULL, 'Early Archaic', true, '3.1', true, 8);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (10, NULL, 'Middle Archaic', true, '3.2', true, 8);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (11, NULL, 'Late Archaic', true, '3.3', true, 8);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (14, NULL, 'Early Woodland', true, '5.1', true, 13);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (15, NULL, 'Middle Woodland', true, '5.2', true, 13);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (16, NULL, 'Late Woodland', true, '5.3', true, 13);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (26, NULL, 'African American', true, '13.1', true, 25);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (27, NULL, 'Chinese American', true, '13.2', true, 25);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (28, NULL, 'Euroamerican', true, '13.3', true, 25);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (29, NULL, 'Japanese American', true, '13.4', true, 25);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (30, NULL, 'Native American', true, '13.5', true, 25);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (31, NULL, 'Spanish', true, '13.6', true, 25);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (113, NULL, 'archaeologists', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (114, NULL, 'Pueblo of Zuni', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (115, NULL, 'Early Agricultural ', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (116, NULL, 'Upper Republican', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (117, NULL, 'Reeve Phase', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (118, NULL, 'American Bottoms', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (119, NULL, 'Numic and Late Pueblo', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (120, NULL, 'Prehistoric', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (121, NULL, 'Middle Preclassic', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (122, NULL, 'Late Preclassic', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (123, NULL, 'Early Classic', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (124, NULL, 'Late Classic', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (125, NULL, 'Terminal Classic', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (126, NULL, 'Postclassic', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (127, NULL, 'Colonial', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (128, NULL, 'Republican/Modern', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (129, NULL, 'Eastman Phase', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (130, NULL, 'Weaver', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (131, NULL, 'Havana', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (132, NULL, 'Mycenean III C1', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (133, NULL, 'Philistine', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (134, NULL, 'Persian', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (135, NULL, 'Iron Age II', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (136, NULL, 'Middle Bronze Age II', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (137, NULL, 'Iron Age I', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (138, NULL, 'Late Bronze Age II', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (139, NULL, 'Helton Phase', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (140, NULL, 'Mesolithic', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (141, NULL, 'Hominid', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (142, NULL, 'Hominin', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (143, NULL, 'Casas Grandes', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (144, NULL, 'Susquehanna', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (145, NULL, 'Incan', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (146, NULL, 'Nodena phase', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (147, NULL, 'Kansyore', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (148, NULL, 'Elmenteitan', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (149, NULL, 'Urewe', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (150, NULL, 'Middle Stone Age', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (151, NULL, 'Later Stone Age', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (152, NULL, 'Savanna Pastoral Neolithic', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (153, NULL, 'Iron Age', false, NULL, false, NULL);
INSERT INTO culture_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (154, NULL, 'Narosura', false, NULL, false, NULL);


ALTER TABLE culture_keyword ENABLE TRIGGER ALL;

--
-- Data for Name: culture_keyword_synonym; Type: TABLE DATA; Schema: public; Owner: tdar
--

ALTER TABLE culture_keyword_synonym DISABLE TRIGGER ALL;



ALTER TABLE culture_keyword_synonym ENABLE TRIGGER ALL;

--
-- Data for Name: investigation_type; Type: TABLE DATA; Schema: public; Owner: tdar
--

ALTER TABLE investigation_type DISABLE TRIGGER ALL;

INSERT INTO investigation_type (id, definition, label) VALUES (1, 'A study that involves the synthesis of the archaeological history of investigation and culture history of a location or area.', 'Archaeological Overview');
INSERT INTO investigation_type (id, definition, label) VALUES (3, 'A study that involves research and analysis of archaeological resources held in a museum, historical society or other repository.', 'Collections Research');
INSERT INTO investigation_type (id, definition, label) VALUES (4, 'An activity that involves discussions and meetings with communities, stakeholders, and other interested parties for the purpose of describing proposed archaeological activities and eliciting their comments, perspectives and feedback.', 'Consultation');
INSERT INTO investigation_type (id, definition, label) VALUES (7, 'An activity employed to gather a general impression of the nature and distribution of archaeological or cultural resources in an area. Relatively little field work is conducted in relation to the size of the research area.', 'Reconnaissance / Survey');
INSERT INTO investigation_type (id, definition, label) VALUES (9, 'Activities undertaken to plan and schedule future archaeological research.', 'Research Design / Data Recovery Plan');
INSERT INTO investigation_type (id, definition, label) VALUES (11, 'An activity that involves the rehabilitation of eroding, slumping, subsiding or otherwise deteriorating archaeological resources, including structures and building materials.', 'Site Stabilization');
INSERT INTO investigation_type (id, definition, label) VALUES (8, 'An activity involving the review of records, files, and other information about the sites recorded in a particular area.  Typically, such studies involve checking the site files and other archives in an agency''s database, or SHPO''s office, or State Archaeologist''s office.', 'Records Search / Inventory Checking');
INSERT INTO investigation_type (id, definition, label) VALUES (6, 'A non-field study of archaeological theory, method or technique. These investigations may also include broadly synthetic regional studies.', 'Methodology, Theory, or Synthesis');
INSERT INTO investigation_type (id, definition, label) VALUES (13, 'These are studies that examine aspects of the present or past natural environment to provide a context, often off-site,  for interpreting archaeological resources.  Sometimes reported in stand-alone volumes representing significant research, such investigations may include geomorphological, paleontological, or palynological work.', 'Environment Research');
INSERT INTO investigation_type (id, definition, label) VALUES (14, 'This is research that focuses on the systematic description and analysis of cultural systems or lifeways.  These studies of contemporary people and cultures rely heavily on participant observation as well as interviews, oral histories, and review of relevant documents.  Ethnoarchaeological studies are a subset of this kind of research that investigate correlations between traditional contemporary cultures and patterns in the archaeological record.    ', 'Ethnographic Research');
INSERT INTO investigation_type (id, definition, label) VALUES (15, 'These are studies of biological aspects of human individuals, groups, or cultural systems, including a broad range of topics describing and analyzing human physiology, osteology, diet, disease, and origins research. ', 'Bioarchaeological Research');
INSERT INTO investigation_type (id, definition, label) VALUES (16, 'Descriptive and analytical research that documents, describes, and/or interprets detailed data on historic structures in an area, including images and floor plans. ', 'Architectural Documentation');
INSERT INTO investigation_type (id, definition, label) VALUES (17, 'This kind of research includes systematic description and analysis of changes in cultural systems through time, using historical documents and oral and traditional histories.  Ethnohistoric studies typically deal with time periods of initial or early contact between different cultural systems, for example, European explorers or early colonists and indigenous cultures. ', 'Ethnohistoric Research');
INSERT INTO investigation_type (id, definition, label) VALUES (18, 'These are investigations of the past using written records and other documents.  Evidence from the records are compared, judged for veracity, placed in chronological or topical sequence, and interpreted in light of preceding, contemporary, and subsequent events.', 'Historic Background Research');
INSERT INTO investigation_type (id, definition, label) VALUES (19, 'Visits to a site to record archaeological resources and their conditions and recover finds that may have come to light since a previous visit.  Also refers to regular or systematic monitoring and recording of the condition of a site, checking for signs of vandalism, other human intervention, and natural processes that may have damaged the resource.', 'Site Stewardship Monitoring');
INSERT INTO investigation_type (id, definition, label) VALUES (20, 'Observations and investigations conducted during any ground disturbing operation carried out for non-archaeological reasons (e.g. construction, land-leveling, wild land fire-fighting, etc.) that may reveal and/or damage archaeological deposits.', 'Ground Disturbance Monitoring');
INSERT INTO investigation_type (id, definition, label) VALUES (12, 'This is an investigation employed to systematically gather data on the general presence or absence of archaeological resources, to define resource types, or to estimate the distribution of resources in an area. These studies may provide a general understanding of the resources in an area. This includes the description, analysis, and specialized studies of artifacts and samples recovered during survey. ', 'Systematic Survey');
INSERT INTO investigation_type (id, definition, label) VALUES (10, 'These investigations include fieldwork undertaken to identify the archaeological resources in a given area and to collect information sufficient to evaluate the resource(s) and develop treatment recommendations. Such investigations typically determine the number, location, distribution and condition of archaeological resources. This includes the description, analysis, and specialized studies of artifacts and samples recovered during site testing. ', 'Site Evaluation / Testing');
INSERT INTO investigation_type (id, definition, label) VALUES (5, 'These investigations include substantial field investigation of an archaeological site (or sites) involving the removal, and systematic recording of, archaeological matrix. These activities often mitigate the adverse effects of a public undertaking. This includes the description, analysis, and specialized studies of artifacts and samples recovered during excavations.', 'Data Recovery / Excavation');
INSERT INTO investigation_type (id, definition, label) VALUES (2, 'These investigations include field and/or document and records reviews to gather data on the presence and type of historic structures and provide a general understanding of architectural and cultural resources in an area. ', 'Architectural Survey');


ALTER TABLE investigation_type ENABLE TRIGGER ALL;

--
-- Data for Name: investigation_type_synonym; Type: TABLE DATA; Schema: public; Owner: tdar
--

ALTER TABLE investigation_type_synonym DISABLE TRIGGER ALL;



ALTER TABLE investigation_type_synonym ENABLE TRIGGER ALL;

--
-- Data for Name: material_keyword; Type: TABLE DATA; Schema: public; Owner: tdar
--

ALTER TABLE material_keyword DISABLE TRIGGER ALL;

INSERT INTO material_keyword (id, definition, label) VALUES (1, 'Historic or prehistoric artifacts made from pottery or fired clay', 'Ceramic');
INSERT INTO material_keyword (id, definition, label) VALUES (2, 'Lithic artifacts such as tools, flakes, shatter, or debitage', 'Chipped Stone');
INSERT INTO material_keyword (id, definition, label) VALUES (3, 'Material collected for use with dating techniques such as radiocarbon, dendrochronology or archaeomagnetism', 'Dating Sample');
INSERT INTO material_keyword (id, definition, label) VALUES (4, 'Animal bone remains', 'Fauna');
INSERT INTO material_keyword (id, definition, label) VALUES (5, 'Rocks showing evidence of intense heating or burning, carbon staining, or other indications of changes due to heat.', 'Fire Cracked Rock');
INSERT INTO material_keyword (id, definition, label) VALUES (6, 'Historic or prehistoric artifacts made from glass', 'Glass');
INSERT INTO material_keyword (id, definition, label) VALUES (7, 'Lithic artifact formed or finished by polishing the body or edges with an abrasive', 'Ground Stone');
INSERT INTO material_keyword (id, definition, label) VALUES (8, 'Materials used for construction (e.g., brick, wood, adobe)', 'Building Materials');
INSERT INTO material_keyword (id, definition, label) VALUES (9, 'The remains of any part of a human', 'Human Remains');
INSERT INTO material_keyword (id, definition, label) VALUES (10, 'Plant remains such as fruit, seeds, buds, or other plant parts.', 'Macrobotanical');
INSERT INTO material_keyword (id, definition, label) VALUES (11, 'Any prehistoric or historic artifact made of metal (e.g., iron, copper, gold, silver, etc.)', 'Metal');
INSERT INTO material_keyword (id, definition, label) VALUES (12, 'Natural inorganic substance possessing a definite chemical composition in a crystalline form.', 'Mineral');
INSERT INTO material_keyword (id, definition, label) VALUES (13, 'Use for any microscopic plant remains', 'Pollen');
INSERT INTO material_keyword (id, definition, label) VALUES (14, 'Modified or unmodified objects made from mollusc shell.', 'Shell');
INSERT INTO material_keyword (id, definition, label) VALUES (15, 'Modified or unmodified objects made from the roots, trunk, or branches of trees or shrubs.', 'Wood');


ALTER TABLE material_keyword ENABLE TRIGGER ALL;

--
-- Data for Name: material_keyword_synonym; Type: TABLE DATA; Schema: public; Owner: tdar
--

ALTER TABLE material_keyword_synonym DISABLE TRIGGER ALL;



ALTER TABLE material_keyword_synonym ENABLE TRIGGER ALL;

--
-- Data for Name: site_type_keyword; Type: TABLE DATA; Schema: public; Owner: tdar
--

ALTER TABLE site_type_keyword DISABLE TRIGGER ALL;

INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (251, 'The locations and/or archaeological remains of a building or buildings used for human habitation. Use more specific term(s) if possible.', 'Domestic Structure or Architectural Complex', true, '1', true, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (381, NULL, 'Santa Cruz Island', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (383, NULL, 'hillfort', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (384, NULL, 'Marble Canyon', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (385, NULL, 'Prado Basin', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (386, NULL, 'Spring', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (387, NULL, 'Urban Structure', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (388, NULL, 'School', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (389, NULL, 'Schoolhouse', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (390, NULL, 'Gaol', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (391, NULL, 'Jail', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (393, NULL, 'Ring midden', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (394, NULL, 'Caches', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (272, 'The locations and/or archaeological remains of features or sites related to resource extraction, commerce, industry, or transportation. Use more specific term(s) if possible.', 'Resource Extraction/Production/Transportation Structure or Features', true, '2', true, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (369, NULL, 'Castle', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (382, NULL, 'Cache River Valley sites', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (392, NULL, 'museum', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (301, 'The archaeological features or locations used for human burial or funerary activities. Use more specific term(s) if possible.', 'Funerary and Burial Structures or Features', true, '3', true, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (309, 'The locations and/or archaeological remains of a building or buildings used for purposes other than human habitation. Use more specific term(s) if possible.', 'Non-Domestic Structures', true, '4', true, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (339, 'A localized area containing evidence of human activity. Use more specific term(s) if possible.', 'Archaeological Feature', true, '5', true, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (361, 'Designs, whether carved, scraped, pecked or painted, applied to free-standing stones, cave walls, or the earth’s surface. Use more specific term(s) if possible.', 'Rock Art', true, '6', true, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (365, 'The locations and/or archaeological remains of ships, boats, or other vessels, or the facilities related to shipping or sailing.', 'Water-related', true, '7', true, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (370, NULL, 'canal system', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (371, NULL, 'Open-Air Domestic Encampment', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (372, NULL, 'Axotlan', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (375, NULL, 'Tavern', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (376, NULL, 'homesteading', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (377, NULL, 'dam', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (378, NULL, 'irrigation canals', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (379, NULL, 'cattle ranching', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (380, NULL, 'sheep herding', false, NULL, false, NULL);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (252, 'Locations, or the remains of multiple structures or features, that were inhabited by humans in the past. Use more specific term(s) if possible.', 'Settlements', true, '1.1', true, 251);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (253, 'A relatively small, short-term human habitation occupied by a relatively small group.', 'Encampment', true, '1.1.1', true, 252);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (254, 'Relatively small, self-contained groups of dwellings and associated structures providing shelter and a home base for its human inhabitants.  Typically occupied for a number of years or decades, and in some cased for centuries.', 'Hamlet / village', true, '1.1.2', true, 252);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (255, 'Larger settlements with more dwellings and a wide variety of other kinds of structures.  These settlements typically have internally organized infrastructure of streets or walkways and water and waste-disposal systems. Typically occupied for decades or centuries.', 'Town / city', true, '1.1.3', true, 252);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (256, 'Locations, or the remains of buildings that were inhabited by humans in the past. Use more specific term(s) if possible.', 'Domestic Structures', true, '1.2', true, 251);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (257, 'A temporary structure, made out of brush, with a roof and walls, built to provide shelter for occupants or contents (e.g., wikieup, ki).', 'Brush structure', true, '1.2.1', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (258, 'Natural hollow or opening beneath earth''s surface, with an aperture to the surface, showing evidence of human use. Caves may or may not have been modified for or by human use. A cave differs from a rockshelter in depth, penetration, and the constriction of the opening.', 'Cave', true, '1.2.2', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (259, 'A relatively small dwelling occupied by a single nuclear or extended family. May appear archaeologically as a stone foundation or pattern of post molds.', 'House', true, '1.2.3', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (260, 'A slightly raised, mounded area of earth or rock built to provide a platform for a single domestic structure.', 'House mound', true, '1.2.4', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (261, 'The remains of a small surface structure constructed of brush (wattle) and mud (daub).', 'Wattle & daub (jacal) structure', true, '1.2.5', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (262, 'A long, relatively narrow multi-family dwelling, best known as a typical village dwelling used by Iroquois Confederacy tribes.', 'Long house', true, '1.2.6', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (263, 'Semi-subterranean habitation that may have an oval, round or rectangular shape. Typically with a dome-like covering constructed using a wood frame covered by branches, reeds, other vegetation and earth.', 'Pit house / earth lodge', true, '1.2.7', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (264, 'Remains of a contiguous, multi-room habitation structure. Typically constructed of stone, mud brick or adobe. Usually manifests archaeologically as a surface mound of construction debris, sometimes with visible wall alignments.', 'Room block / compound / pueblo', true, '1.2.8', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (265, 'Overhang, indentation, or alcove formed naturally by rock fall or in a rock face; generally not of great depth. Rockshelters may or may not be modified with structural elements for human use.', 'Rock shelter', true, '1.2.9', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (266, 'All temporary shelters (e.g. lean-tos, windbreaks, brush enclosures, sun shades etc.).', 'Shade structure / ramada', true, '1.2.10', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (267, 'Circular pattern (sometimes outlined with rocks) left when a tipi or tent is dismantled.', 'Tent ring / tipi ring', true, '1.2.11', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (268, 'A relatively high (over 1 meter), flat-topped mound, frequently constructed in several stages on which one or more structures were placed. Platform mounds are constructed using soil, shell, or refuse. They may incorporate earlier, filled-in structures in their substructure.', 'Platform mound', true, '1.2.12', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (269, 'A low mounded area of shell built to provide a platform for one or more domestic structures.', 'Shell mound', true, '1.2.13', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (270, 'Relatively small dwellings, typically circular or rectangular and about 3 meters tall, made of wooden frames with bases dug into the soil and covered with woven mats or sheets of birchbark. The frames could be shaped like a dome, a cone, or a rectangle with an arched roof.', 'Wigwam / wetu', true, '1.2.14', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (271, 'Relatively large, rectangular dwellings made of long, flat planks of cedar wood lashed to a substantial wooden frame. Typical of permanent villages of Indian tribes living in the American Northwest during the historic contact period and earlier.', 'Plank house', true, '1.2.15', true, 256);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (273, 'Locations, or the remains of features or facilities, that were used for  horticulture, agriculture, or animal husbandry. Use more specific term(s) if possible.', 'Agricultural or Herding', true, '2.1', true, 272);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (320, 'Specified area containing evidence that is associated with historic governmental activity.', 'Historic governmental structure', true, '4.5.2', true, 318);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (274, 'An area of land, often enclosed, used for cultivation. Fields are not necessarily formally bounded, and may be identifiable based on diagnostic features such as boundary markers or raised beds.', 'Agricultural field or field feature', true, '2.1.1', true, 273);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (275, 'Ditch or interrelated group of ditches, acequias, head gates, and drains that constitute an irrigation system for individual watering and irrigation features.', 'Canal or canal feature', true, '2.1.2', true, 273);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (276, 'An enclosure for confining livestock. May be constructed of any material and incorporate natural features or vegetation as part of the enclosure.', 'Corral', true, '2.1.3', true, 273);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (277, 'Natural or artificial lake in which water can be stored for future use.', 'Reservoir', true, '2.1.4', true, 273);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (278, 'An artificially created, more or less level area cut into the side of a hill. The edge may be bordered by stone or other material to prevent erosion.', 'Terrace', true, '2.1.5', true, 273);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (279, 'A device which controls the flow of water, particularly run-off. Includes check dams, flumes, gabions, head gates, drop structures, and riprap.', 'Water control feature', true, '2.1.6', true, 273);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (280, 'Locations, or the remains of features or facilities, that were used for commercial or industrial purposes. Use more specific term(s) if possible.', 'Commercial or Industrial Structures', true, '2.2', true, 272);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (281, 'A relative large structure in which goods were manufactured or prepared for commercial distribution.', 'Factory / workshop', true, '2.2.1', true, 280);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (282, 'A relatively small facility (one or a few rooms) for processing grain, wood, or other materials.', 'Mill', true, '2.2.2', true, 280);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (283, 'A mill for processing grain, typically powered by water or wind.', 'Grist mill', true, '2.2.2.1', true, 282);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (284, 'A mill for processing timber or wood.', 'Saw mill', true, '2.2.2.2', true, 282);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (285, 'A structure designed for catching fish. Sometimes constructed as a fence or enclosure of wooden stakes or stones, placed in a river, lake, wetland or tidal estuary.', 'Fish trap / weir', true, '2.3', true, 272);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (286, 'Locations, or the remains of features or facilities, that were used for hunting or trapping animals. Use more specific term(s) if possible.', 'Hunting / Trapping', true, '2.4', true, 272);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (287, 'Concentration of faunal remains resulting from human hunting activity.', 'Butchering / kill site', true, '2.4.1', true, 286);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (288, 'Small, unroofed structure expediently constructed out of natural rock and/or wood as camouflage.', 'Hunting blind', true, '2.4.2', true, 286);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (289, 'A cliff or other natural drop off where large game can be stampeded over the edge.', 'Large game jump', true, '2.4.3', true, 286);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (290, 'Locations used for the extraction of metals, ores, minerals, or other materials.', 'Mine', true, '2.5', true, 272);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (291, 'An excavation made in the earth for the purpose of digging out metallic ores, coal, salt, precious stones or other resources. Includes portals, adits, vent shafts, prospects, and haulage tunnels.', 'Mine tunnels', true, '2.5.1', true, 290);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (292, 'The remains of facilities or equipment, usually above ground, used for processing or storing mined materials.', 'Mine-related structures', true, '2.5.2', true, 290);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (293, 'Outcrops of lithic material that have been mined or otherwise utilized to obtain lithic raw materials.', 'Quarry', true, '2.6', true, 272);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (294, 'The archaeological remains of identifiable paths or routes between two or more locations. Use more specific term(s) if possible.', 'Road, Trail, and Related Structures or Features', true, '2.7', true, 272);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (295, 'A structure with one or more intervals under it to span a river or other space.', 'Bridge', true, '2.7.1', true, 294);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (296, 'A road or pathway constructed from packed earth, stone, or shell, usually across a wetland or small water body.', 'Causeway', true, '2.7.2', true, 294);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (297, 'Identifiable, linear archaeological remains of unknown function.', 'Linear feature', true, '2.7.3', true, 294);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (298, 'Segment(s) of railroad tracks or railroad bed.', 'Railroad', true, '2.7.4', true, 294);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (299, 'A prepared, formal way used for the passage of humans, animals, and/or vehicles. These include examples such as Chacoan roads.', 'Road', true, '2.7.5', true, 294);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (300, 'An informal foot path used for the passage of humans, animals, and/or vehicles, defined and worn by use without formal construction or maintenance.', 'Trail', true, '2.7.6', true, 294);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (302, 'A formal location for burying the dead.', 'Cemetery', true, '3.1', true, 301);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (303, 'An artificial mound constructed using earth, shell, or stone for the purpose of holding one or more burials. Frequently containing several episodes of construction and burials from different periods of time.', 'Burial mound', true, '3.2', true, 301);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (304, 'A structure in which recently deceased human bodies were placed so that the flesh and other soft tissue would decompose prior to final interment of the remains.', 'Charnel house', true, '3.3', true, 301);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (305, 'A location containing a human burial, spatially removed from other archaeological evidence.', 'Isolated burial', true, '3.4', true, 301);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (306, 'A secondary burial of multiple individuals.', 'Ossuary', true, '3.5', true, 301);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (307, 'An unmarked human interment in a subterranean pit.', 'Burial pit', true, '3.6', true, 301);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (308, 'A prepared, architecturally distinctive structure, normally sub-surface, often containing multiple interments.  Use for features such as shaft tombs.', 'Tomb', true, '3.7', true, 301);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (310, 'An unroofed structure associated with the playing of the Mesoamerican ball game, found in the American southwest and parts of Mesoamerica.', 'Ball Court', true, '4.1', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (311, 'Buildings, or the archaeological remains of features or facilities, that were used for religious purposes. Use more specific term(s) if possible.', 'Church / religious structure', true, '4.2', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (312, 'Remains of a prehistoric building or location designed for public religious services.', 'Ancient church / religious structure', true, '4.2.1', true, 311);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (313, 'Remains of a historic building or location designed for public religious services.', 'Historic church / religious structure', true, '4.2.2', true, 311);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (314, 'Locations, or the remains of buildings that were associated with communal or public activities.', 'Communal / public structure', true, '4.3', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (315, 'Specified area containing evidence that is associated with prehistoric communal or public activity.', 'Ancient communal / public structure', true, '4.3.1', true, 314);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (316, 'Specified area containing evidence that is associated with historic communal or public activity.', 'Historic communal / public structure', true, '4.3.2', true, 314);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (317, 'A multi-story building with massive masonry or adobe walls, found in the Chacoan and Hohokam regions of the American southwest.', 'Great House / Big House', true, '4.4', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (318, 'Locations, or the remains of buildings that were associated with governmental activities.', 'Governmental structure', true, '4.5', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (319, 'Specified area containing evidence that is associated with prehistoric governmental activity.', 'Ancient governmental structure', true, '4.5.1', true, 318);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (321, 'Circular or rectangular ceremonial structure. May be subterranean or part of a surface room block.', 'Kiva / Great Kiva', true, '4.6', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (322, 'Military-related structure constructed for various purposes (personnel barracks, testing, aircraft storage or landing, etc.).', 'Military structure', true, '4.7', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (323, 'An above ground construction of earth, shell or other material, undifferentiated as to function.', 'Mound / Earthwork', true, '4.8', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (324, 'An above ground prepared surface on which a non-residential structure is built.', 'Building substructure', true, '4.8.1', true, 323);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (325, 'A non-residential cultural construction made from earth, shell or other materials, often formed to enclose or demarcate an area, or, in the case of causeways, to link areas. Examples include shell rings.', 'Ancient earthwork', true, '4.8.2', true, 323);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (326, 'A defensive construction made from earth, shell or other materials.', 'Military earthwork', true, '4.8.3', true, 323);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (327, 'An above ground construction of earth, shell or other material, built in the shape of geometric, animal or other symbolic forms. Prominent examples include Effigy Mounds National Monument and Serpent Mound.', 'Geometric / effigy / zoomorphic mound', true, '4.8.4', true, 323);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (328, 'A large, circular area defined by a perimeter of mounded shell (often several meters in height), for example, Archaic Period shell ring sites in the American southeast.', 'Shell ring', true, '4.8.5', true, 323);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (329, 'A large and/or ornate building, normally associated with a high ranking family or individual.', 'Palace', true, '4.9', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (330, 'An enclosure, constructed of timbers or posts driven into ground, or otherwise walled.', 'Palisade', true, '4.10', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (331, 'An area which may be partially or completely enclosed by structural remains (standing or collapsed), used for community activities.  May contain temporary structures (e.g. sun shades or ramadas) as well as special activity areas (e.g. milling bins, hearths).', 'Plaza', true, '4.11', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (332, 'A massive structure, typically with triangular outer surfaces that converge at the top. Often flat-topped to accommodate public gatherings and/or buildings.', 'Pyramid', true, '4.12', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (333, 'A series of steps allowing access to a different level. Use for toe/hand holds, stairs, ladders, etc.', 'Stairway', true, '4.13', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (334, 'Architectural remnant of a building of unknown form or function.', 'Structure', true, '4.14', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (335, 'Architectural remnant of a prehistoric building of unknown form or function. Use more specific term if possible.', 'Ancient structure', true, '4.14.1', true, 310);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (336, 'Architectural remnant of a historic building of unknown form or function. Use more specific term if possible.', 'Historic structure', true, '4.14.2', true, 310);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (337, 'Small enclosure or hut used for steam baths, usually ephemeral in construction. Often with fire-cracked rock and/or hearths in association.', 'Sweat house / sweat lodge', true, '4.15', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (338, 'Monumental architecture constructed from stone or other materials, and used for religious and/or political purposes.', 'Temple', true, '4.16', true, 309);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (340, 'Prehistoric lithic and/or ceramic scatters with no features.', 'Artifact Scatter', true, '5.1', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (341, 'Mound or stack of rocks used to mark significant locations (e.g., boundaries or claims).', 'Cairn', true, '5.2', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (342, 'A structure that creates a boundary, barrier, or enclosure. Construction materials can vary widely and may include unmodified natural materials (such as brush).', 'Fence', true, '5.3', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (343, 'Discolored area of soil, often including charcoal, ash deposits or fire cracked rock, exhibiting evidence of use in association with fire.  May be bounded (e.g., rock ring) or ill-defined.', 'Hearth', true, '5.4', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (344, 'A find spot containing a single artifact.', 'Isolated artifact', true, '5.5', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (345, 'A find spot containing a single cultural feature.', 'Isolated feature', true, '5.6', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (346, 'Oven used to bake food, fire pottery, or thermally alter other materials (e.g., bricks, lithic materials).', 'Kiln', true, '5.7', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (347, 'An archaeological refuse deposit containing the broken or discarded remains of human activities. Use more specific term(s) if possible.', 'Midden', true, '5.8', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (348, 'A large, dense concentrations, often mounded, of fire cracked rock (FCR), usually associated with large scale plant processing. Although other cultural materials may be present in the midden, FCR is usually predominant.', 'Burned rock midden', true, '5.8.1', true, 347);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (349, 'A surficial archaeological deposit containing discarded artifacts and other cultural materials. Midden deposits normally contain ashy or charcoal-stained sediments, and domestic-related items such as sherds, lithic debitage, and bone.', 'Sheet midden', true, '5.8.2', true, 347);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (350, 'An archaeological deposit composed primarily of discarded mollusk shells.', 'Shell midden', true, '5.8.3', true, 347);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (351, 'A substantial concentration of refuse, built up as a result of multiple episodes of deposition.', 'Trash midden', true, '5.8.4', true, 347);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (352, 'A facility made or used for grinding or processing plant materials. Use more specific term(s) if possible.', 'Milling feature', true, '5.9', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (353, 'A pecked or ground concavity in a large boulder or outcrop, including both bedrock mortar and bedrock metate.', 'Bedrock grinding feature', true, '5.9.1', true, 352);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (354, 'An enclosed container used for milling plant material. May be above ground or partially or completely underground.', 'Milling Bin', true, '5.9.2', true, 352);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (355, 'A discrete excavation directly attributable to human activity. Use more specific term(s) if possible.', 'Pit', true, '5.10', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (356, 'A discrete excavation directly attributable to human activity that was used for the disposal of discarded artifacts, ecofacts and other cultural materials.', 'Refuse pit', true, '5.10.1', true, 340);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (357, 'An enclosed space used to heat objects placed within its bounds. Includes earth ovens, oven pits, mud ovens, and bread ovens.', 'Roasting pit / oven / horno', true, '5.10.2', true, 340);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (358, 'A discrete excavation directly attributable to human activity used for storing artifacts, ecofacts and other cultural materials.', 'Storage pit', true, '5.10.3', true, 340);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (359, 'One or more upright posts, remains of posts, or sockets usually associated with a larger feature or structure such as a building, fence, corral, stockade, pen, etc.', 'Post hole / post mold', true, '5.11', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (360, 'Group of rocks which appear to have some cultural association. Use for possible walls, wall-like phenomena, human produced architectural oddities, rock piles, etc.', 'Rock alignment', true, '5.12', true, 339);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (362, 'Designs created on the ground surface by arranging rocks or other materials, or by scraping or altering the earth surface. Usually on a large scale.', 'Intaglio / geoglyph', true, '6.1', true, 361);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (363, 'Design scratched, pecked, or scraped into a rock surface.', 'Petroglyph', true, '6.2', true, 361);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (364, 'Design drawn in pigment upon an unprepared or ground rock surface.', 'Pictograph', true, '6.3', true, 361);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (366, 'The remains of facilities or equipment related to boats, ships, or shipping. Use for dock, wharf etc.', 'Shipping-related structure', true, '7.1', true, 365);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (367, 'The remains of a ship, boat or other vessel.', 'Shipwreck', true, '7.2', true, 365);
INSERT INTO site_type_keyword (id, definition, label, approved, index, selectable, parent_id) VALUES (368, 'The underwater remains of an aircraft.', 'Submerged aircraft', true, '7.3', true, 365);


ALTER TABLE site_type_keyword ENABLE TRIGGER ALL;

--
-- Data for Name: site_type_keyword_synonym; Type: TABLE DATA; Schema: public; Owner: tdar
--

ALTER TABLE site_type_keyword_synonym DISABLE TRIGGER ALL;



ALTER TABLE site_type_keyword_synonym ENABLE TRIGGER ALL;

--
-- PostgreSQL database dump complete
--

