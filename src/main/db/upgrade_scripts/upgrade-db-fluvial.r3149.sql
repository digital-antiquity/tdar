alter table coding_rule alter description type varchar(512);

alter table collection add column temp_resource_id bigint;
insert into collection (collection_type,owner_id,temp_resource_id) select distinct 'INTERNAL',submitter_id, id from resource where id in (select resource_id from full_user union select resource_id from read_user);
insert into collection_resource (collection_id,resource_id) select id,temp_resource_id from collection where temp_resource_id is not null;
insert into authorized_user (user_id, resource_collection_id, view_permission,modify_permission,admin_permission)  select person_id, collection.id, 'VIEW_ALL', 'MODIFY_RECORD','CAN_DELETE' from full_user, collection where temp_resource_id=resource_id;
insert into authorized_user (user_id, resource_collection_id, view_permission,modify_permission,admin_permission)  select person_id, collection.id, 'VIEW_ALL', 'NONE','NONE' from full_user, collection where temp_resource_id=resource_id;

alter table collection drop column temp_resource_id;

update information_resource set date_created=null where trim(date_created)='';
alter table information_resource alter column date_created TYPE integer using to_number(date_created,'9999');
alter table resource drop column confidential;
update information_resource_file_version set internal_type='UPLOADED_TEXT' where extension='txt' and internal_type!='INDEXABLE_TEXT' and (filename like '%coding-sheet%' or filename like '%ontology%');


alter table authorized_user add column general_permission varchar(50);
update authorized_user set general_permission=view_permission;
update authorized_user set general_permission=modify_permission where modify_permission!='NONE';

alter table authorized_user drop column view_permission;
alter table authorized_user drop column modify_permission;

alter table authorized_user add column general_permission_int int;
truncate table collection cascade;

-- trying this a second time

alter table collection add column temp_resource_id bigint;
insert into collection (collection_type,owner_id,temp_resource_id) select distinct 'INTERNAL',submitter_id, id from resource where id in (select resource_id from full_user union select resource_id from read_user);
insert into collection_resource (collection_id,resource_id) select id,temp_resource_id from collection;
insert into authorized_user (user_id, resource_collection_id, general_permission, general_permission_int, admin_permission)  select person_id, collection.id, 'MODIFY_RECORD',500, 'CAN_DELETE' from full_user, collection where temp_resource_id=resource_id;
insert into authorized_user (user_id, resource_collection_id, general_permission, general_permission_int ,admin_permission)  select person_id, collection.id, 'VIEW_ALL', 100 ,'NONE' from read_user, collection where temp_resource_id=resource_id and read_user.id not in (

select id from read_user where read_user.person_id || '_' || read_user.resource_id not in (select full_user.person_id || '_' || full_user.resource_id from full_user) );

alter table collection drop column temp_resource_id;

--increasing some lengths to handle ADS import.
ALTER TABLE sensory_data ALTER rgb_data_capture_info TYPE character varying(1024);
ALTER TABLE sensory_data ALTER point_deletion_summary TYPE character varying(1024);

-- 6/16/2011
ALTER TABLE data_table_column add column sequence_number integer;

-- 
-- 7/14/2011
--
ALTER TABLE collection add column shared boolean;
ALTER TABLE collection rename column shared to visible;
ALTER TABLE collection drop column visible;
ALTER TABLE collection add column visible boolean NOT NULL DEFAULT FALSE;

/* update category/subcategory list and references (old list will be placed in category_variable_old) */

--create temp new table to hold new categories
create table category_variable_new  (
    id bigserial primary key,
    description character varying(255),
    name character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    parent_id bigint references category_variable_new(id),
    encoded_parent_ids character varying(255),
    label character varying(255)
  );
  
--store mappings in the old table
alter table category_variable add column new_id bigint;

--add new subcategories
insert into category_variable_new(name, type, label) values ('Architecture', 'CATEGORY', 'Architecture');
insert into category_variable_new(name, type, label) values ('Basketry', 'CATEGORY', 'Basketry');
insert into category_variable_new(name, type, label) values ('Ceramics', 'CATEGORY', 'Ceramics');
insert into category_variable_new(name, type, label) values ('Chipped Stone', 'CATEGORY', 'Chipped Stone');
insert into category_variable_new(name, type, label) values ('Dating Sample', 'CATEGORY', 'Dating Sample');
insert into category_variable_new(name, type, label) values ('Fauna', 'CATEGORY', 'Fauna');
insert into category_variable_new(name, type, label) values ('Figurine', 'CATEGORY', 'Figurine');
insert into category_variable_new(name, type, label) values ('Glass', 'CATEGORY', 'Glass');
insert into category_variable_new(name, type, label) values ('Ground Stone', 'CATEGORY', 'Ground Stone');
insert into category_variable_new(name, type, label) values ('Historic Other', 'CATEGORY', 'Historic Other');
insert into category_variable_new(name, type, label) values ('Human Burial', 'CATEGORY', 'Human Burial');
insert into category_variable_new(name, type, label) values ('Human Dental', 'CATEGORY', 'Human Dental');
insert into category_variable_new(name, type, label) values ('Human Skeletal', 'CATEGORY', 'Human Skeletal');
insert into category_variable_new(name, type, label) values ('Lookup', 'CATEGORY', 'Lookup');
insert into category_variable_new(name, type, label) values ('Macrobotanical', 'CATEGORY', 'Macrobotanical');
insert into category_variable_new(name, type, label) values ('Metal', 'CATEGORY', 'Metal');
insert into category_variable_new(name, type, label) values ('Mineral', 'CATEGORY', 'Mineral');
insert into category_variable_new(name, type, label) values ('Photograph', 'CATEGORY', 'Photograph');
insert into category_variable_new(name, type, label) values ('Pollen', 'CATEGORY', 'Pollen');
insert into category_variable_new(name, type, label) values ('Provenience and Context', 'CATEGORY', 'Provenience and Context');
insert into category_variable_new(name, type, label) values ('Rock Art', 'CATEGORY', 'Rock Art');
insert into category_variable_new(name, type, label) values ('Shell', 'CATEGORY', 'Shell');
insert into category_variable_new(name, type, label) values ('Storage', 'CATEGORY', 'Storage');
insert into category_variable_new(name, type, label) values ('Textile', 'CATEGORY', 'Textile');
update category_variable_new set parent_id=id;


--add new subcategories
insert into category_variable_new(name, type, label, parent_id) values ('Material', 'SUBCATEGORY','Material',(select id from category_variable_new where name = 'Architecture' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Architecture' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Style/Type', 'SUBCATEGORY','Style Type',(select id from category_variable_new where name = 'Architecture' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Basketry' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Design', 'SUBCATEGORY','Design',(select id from category_variable_new where name = 'Basketry' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Form', 'SUBCATEGORY','Form',(select id from category_variable_new where name = 'Basketry' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Function', 'SUBCATEGORY','Function',(select id from category_variable_new where name = 'Basketry' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Material', 'SUBCATEGORY','Material',(select id from category_variable_new where name = 'Basketry' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Basketry' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Technique', 'SUBCATEGORY','Technique',(select id from category_variable_new where name = 'Basketry' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Basketry' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Composition', 'SUBCATEGORY','Composition',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Design/Decorative Element', 'SUBCATEGORY','Design Decorative Element',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Form', 'SUBCATEGORY','Form',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Paint', 'SUBCATEGORY','Paint',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Part', 'SUBCATEGORY','Part',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Paste', 'SUBCATEGORY','Paste',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Residue', 'SUBCATEGORY','Residue',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Surface Treatment', 'SUBCATEGORY','Surface Treatment',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Temper/Inclusions', 'SUBCATEGORY','Temper Inclusions',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Type', 'SUBCATEGORY','Type',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Variety/Subtype', 'SUBCATEGORY','Variety Subtype',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Ware', 'SUBCATEGORY','Ware',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Chipped Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Form', 'SUBCATEGORY','Form',(select id from category_variable_new where name = 'Chipped Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Material', 'SUBCATEGORY','Material',(select id from category_variable_new where name = 'Chipped Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Chipped Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Retouch', 'SUBCATEGORY','Retouch',(select id from category_variable_new where name = 'Chipped Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Type', 'SUBCATEGORY','Type',(select id from category_variable_new where name = 'Chipped Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Chipped Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Method', 'SUBCATEGORY','Method',(select id from category_variable_new where name = 'Dating Sample' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Date', 'SUBCATEGORY','Date',(select id from category_variable_new where name = 'Dating Sample' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Error', 'SUBCATEGORY','Error',(select id from category_variable_new where name = 'Dating Sample' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Age', 'SUBCATEGORY','Age',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Anterior/Posterior', 'SUBCATEGORY','Anterior Posterior',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Bone Artifact Form', 'SUBCATEGORY','Bone Artifact Form',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Breakage', 'SUBCATEGORY','Breakage',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Burning ', 'SUBCATEGORY','Burning ',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Butchering', 'SUBCATEGORY','Butchering',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Completeness', 'SUBCATEGORY','Completeness',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Condition', 'SUBCATEGORY','Condition',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Cultural Modification', 'SUBCATEGORY','Cultural Modification',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Digestion', 'SUBCATEGORY','Digestion',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Dorsal/Ventral', 'SUBCATEGORY','Dorsal Ventral',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Element', 'SUBCATEGORY','Element',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Erosion', 'SUBCATEGORY','Erosion',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Fusion', 'SUBCATEGORY','Fusion',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Gnawing/Animal Modification', 'SUBCATEGORY','Gnawing Animal Modification',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Modification', 'SUBCATEGORY','Modification',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Natural Modification', 'SUBCATEGORY','Natural Modification',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Pathologies', 'SUBCATEGORY','Pathologies',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Portion/Proximal/Distal', 'SUBCATEGORY','Portion Proximal Distal',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Sex', 'SUBCATEGORY','Sex',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Side', 'SUBCATEGORY','Side',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Spiral Fracture', 'SUBCATEGORY','Spiral Fracture',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Taxon', 'SUBCATEGORY','Taxon',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weathering', 'SUBCATEGORY','Weathering',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Zone', 'SUBCATEGORY','Zone',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Zone Scheme', 'SUBCATEGORY','Zone Scheme',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Figurine' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Form', 'SUBCATEGORY','Form',(select id from category_variable_new where name = 'Figurine' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Material', 'SUBCATEGORY','Material',(select id from category_variable_new where name = 'Figurine' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Figurine' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Style/Type', 'SUBCATEGORY','Style Type',(select id from category_variable_new where name = 'Figurine' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Glass' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Date', 'SUBCATEGORY','Date',(select id from category_variable_new where name = 'Glass' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Form', 'SUBCATEGORY','Form',(select id from category_variable_new where name = 'Glass' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Maker/Manufacturer', 'SUBCATEGORY','Maker Manufacturer',(select id from category_variable_new where name = 'Glass' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Material', 'SUBCATEGORY','Material',(select id from category_variable_new where name = 'Glass' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Glass' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Glass' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Completeness', 'SUBCATEGORY','Completeness',(select id from category_variable_new where name = 'Ground Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Ground Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Form', 'SUBCATEGORY','Form',(select id from category_variable_new where name = 'Ground Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Material', 'SUBCATEGORY','Material',(select id from category_variable_new where name = 'Ground Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Ground Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Ground Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Historic Other' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Date', 'SUBCATEGORY','Date',(select id from category_variable_new where name = 'Historic Other' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Form', 'SUBCATEGORY','Form',(select id from category_variable_new where name = 'Historic Other' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Maker/Manufacturer', 'SUBCATEGORY','Maker Manufacturer',(select id from category_variable_new where name = 'Historic Other' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Material', 'SUBCATEGORY','Material',(select id from category_variable_new where name = 'Historic Other' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Historic Other' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Historic Other' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Body Position/Flexure', 'SUBCATEGORY','Body Position Flexure',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Body Posture', 'SUBCATEGORY','Body Posture',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Body Preparation', 'SUBCATEGORY','Body Preparation',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Burial Accompaniment', 'SUBCATEGORY','Burial Accompaniment',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Burial Container ', 'SUBCATEGORY','Burial Container ',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Burial Facility', 'SUBCATEGORY','Burial Facility',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Disturbance', 'SUBCATEGORY','Disturbance',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Facing', 'SUBCATEGORY','Facing',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Orientation/Alignment ', 'SUBCATEGORY','Orientation Alignment ',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Preservation', 'SUBCATEGORY','Preservation',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Type of Interment', 'SUBCATEGORY','Type of Interment',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Buccal/Lingual/Occlusal', 'SUBCATEGORY','Buccal Lingual Occlusal',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Chemical Assay', 'SUBCATEGORY','Chemical Assay',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count   ', 'SUBCATEGORY','Count   ',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Cultural Modification', 'SUBCATEGORY','Cultural Modification',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Dental Pathologies', 'SUBCATEGORY','Dental Pathologies',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Dental Wear', 'SUBCATEGORY','Dental Wear',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Enamel Defects', 'SUBCATEGORY','Enamel Defects',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Maxillary/Mandibular', 'SUBCATEGORY','Maxillary Mandibular',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Permanent/Deciduous', 'SUBCATEGORY','Permanent Deciduous',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Tooth (element)', 'SUBCATEGORY','Tooth (element)',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Age', 'SUBCATEGORY','Age',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Age Criteria', 'SUBCATEGORY','Age Criteria',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Articulation', 'SUBCATEGORY','Articulation',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Bone Segment (proximal/distal)', 'SUBCATEGORY','Bone Segment (proximal distal)',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Chemical Assay', 'SUBCATEGORY','Chemical Assay',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Completeness', 'SUBCATEGORY','Completeness',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Condition', 'SUBCATEGORY','Condition',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Cranial Deformation', 'SUBCATEGORY','Cranial Deformation',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Crematory Burning', 'SUBCATEGORY','Crematory Burning',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Cultural Modification ', 'SUBCATEGORY','Cultural Modification ',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Diet', 'SUBCATEGORY','Diet',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Distrubance', 'SUBCATEGORY','Distrubance',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Disturbance sources', 'SUBCATEGORY','Disturbance sources',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Element', 'SUBCATEGORY','Element',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Epiphyseal Union', 'SUBCATEGORY','Epiphyseal Union',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Fracture/Breakage', 'SUBCATEGORY','Fracture Breakage',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Health ', 'SUBCATEGORY','Health ',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Nonmetric Trait', 'SUBCATEGORY','Nonmetric Trait',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Pathologies/Trauma', 'SUBCATEGORY','Pathologies Trauma',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Preservation', 'SUBCATEGORY','Preservation',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Sex', 'SUBCATEGORY','Sex',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Sex criteria', 'SUBCATEGORY','Sex criteria',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Side', 'SUBCATEGORY','Side',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Code', 'SUBCATEGORY','Code',(select id from category_variable_new where name = 'Lookup' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Description', 'SUBCATEGORY','Description',(select id from category_variable_new where name = 'Lookup' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Label', 'SUBCATEGORY','Label',(select id from category_variable_new where name = 'Lookup' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Notes', 'SUBCATEGORY','Notes',(select id from category_variable_new where name = 'Lookup' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Macrobotanical' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Taxon', 'SUBCATEGORY','Taxon',(select id from category_variable_new where name = 'Macrobotanical' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Metal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Date', 'SUBCATEGORY','Date',(select id from category_variable_new where name = 'Metal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Form', 'SUBCATEGORY','Form',(select id from category_variable_new where name = 'Metal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Maker/Manufacturer', 'SUBCATEGORY','Maker Manufacturer',(select id from category_variable_new where name = 'Metal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Material', 'SUBCATEGORY','Material',(select id from category_variable_new where name = 'Metal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Metal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Metal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Mineral' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Form', 'SUBCATEGORY','Form',(select id from category_variable_new where name = 'Mineral' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Mineral' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Mineral Type', 'SUBCATEGORY','Mineral Type',(select id from category_variable_new where name = 'Mineral' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Mineral' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Direction', 'SUBCATEGORY','Direction',(select id from category_variable_new where name = 'Photograph' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Film Type', 'SUBCATEGORY','Film Type',(select id from category_variable_new where name = 'Photograph' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Frame', 'SUBCATEGORY','Frame',(select id from category_variable_new where name = 'Photograph' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('ID', 'SUBCATEGORY','ID',(select id from category_variable_new where name = 'Photograph' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Roll', 'SUBCATEGORY','Roll',(select id from category_variable_new where name = 'Photograph' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Subject', 'SUBCATEGORY','Subject',(select id from category_variable_new where name = 'Photograph' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Pollen' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Taxon', 'SUBCATEGORY','Taxon',(select id from category_variable_new where name = 'Pollen' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Context', 'SUBCATEGORY','Context',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Date', 'SUBCATEGORY','Date',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Depth', 'SUBCATEGORY','Depth',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('East', 'SUBCATEGORY','East',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Excavation Method', 'SUBCATEGORY','Excavation Method',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Feature ID/Number', 'SUBCATEGORY','Feature ID Number',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Feature Type', 'SUBCATEGORY','Feature Type',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Horizontal Location', 'SUBCATEGORY','Horizontal Location',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Inclusions', 'SUBCATEGORY','Inclusions',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Item/Slash ', 'SUBCATEGORY','Item Slash ',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Level', 'SUBCATEGORY','Level',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Locus', 'SUBCATEGORY','Locus',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Lot', 'SUBCATEGORY','Lot',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('North', 'SUBCATEGORY','North',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Project', 'SUBCATEGORY','Project',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Recovery Method', 'SUBCATEGORY','Recovery Method',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Sampling', 'SUBCATEGORY','Sampling',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Screening', 'SUBCATEGORY','Screening',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Site', 'SUBCATEGORY','Site',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Soil Color', 'SUBCATEGORY','Soil Color',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Stratum', 'SUBCATEGORY','Stratum',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Unit', 'SUBCATEGORY','Unit',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Vertical Position', 'SUBCATEGORY','Vertical Position',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Volume', 'SUBCATEGORY','Volume',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Exposure', 'SUBCATEGORY','Exposure',(select id from category_variable_new where name = 'Rock Art' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Form', 'SUBCATEGORY','Form',(select id from category_variable_new where name = 'Rock Art' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Style', 'SUBCATEGORY','Style',(select id from category_variable_new where name = 'Rock Art' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Technology', 'SUBCATEGORY','Technology',(select id from category_variable_new where name = 'Rock Art' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Completeness', 'SUBCATEGORY','Completeness',(select id from category_variable_new where name = 'Shell' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Shell' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Shell' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Modification', 'SUBCATEGORY','Modification',(select id from category_variable_new where name = 'Shell' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Taxon', 'SUBCATEGORY','Taxon',(select id from category_variable_new where name = 'Shell' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Shell' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Box Number', 'SUBCATEGORY','Box Number',(select id from category_variable_new where name = 'Storage' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Location', 'SUBCATEGORY','Location',(select id from category_variable_new where name = 'Storage' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Count', 'SUBCATEGORY','Count',(select id from category_variable_new where name = 'Textile' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Design', 'SUBCATEGORY','Design',(select id from category_variable_new where name = 'Textile' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Form', 'SUBCATEGORY','Form',(select id from category_variable_new where name = 'Textile' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Function', 'SUBCATEGORY','Function',(select id from category_variable_new where name = 'Textile' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Material', 'SUBCATEGORY','Material',(select id from category_variable_new where name = 'Textile' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Measurement', 'SUBCATEGORY','Measurement',(select id from category_variable_new where name = 'Textile' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Technique', 'SUBCATEGORY','Technique',(select id from category_variable_new where name = 'Textile' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Weight', 'SUBCATEGORY','Weight',(select id from category_variable_new where name = 'Textile' and type = 'CATEGORY'));
--'other' subcats
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Architecture' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Basketry' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Ceramics' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Chipped Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Dating Sample' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Fauna' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Figurine' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Glass' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Ground Stone' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Historic Other' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Human Burial' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Human Dental' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Human Skeletal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Lookup' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Macrobotanical' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Metal' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Mineral' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Photograph' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Pollen' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Provenience and Context' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Rock Art' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Shell' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Storage' and type = 'CATEGORY'));
insert into category_variable_new(name, type, label, parent_id) values ('Other', 'SUBCATEGORY','Other',(select id from category_variable_new where name = 'Textile' and type = 'CATEGORY'));

--map old top-level category id's to new top-level category id's 
update category_variable set new_id = (select id from category_variable_new cvn WHERE cvn.name = category_variable.name and cvn.type = 'CATEGORY') WHERE type='CATEGORY';

--map old subcategory id's to new id
update category_variable
    set new_id = (
        select 
            id 
        from 
            category_variable_new cvn 
        where 
            cvn.name = category_variable.name 
            and cvn.parent_id = (select new_id from category_variable cv where cv.id = category_variable.parent_id)
    )
where
    type='SUBCATEGORY';

--create map 'other' subcategories to point to parent id instead (if not trivially matched in the previous statement)
update category_variable set new_id = (select new_id from category_variable cv where cv.id = category_variable.parent_id) where name='Other' and new_id is null;

--flatten 3rd level categories to 2nd level
create temporary table catlevel1 as 
select * from category_variable where encoded_parent_ids is null;
create temporary table catlevel2 as 
select * from category_variable where parent_id in (select id from catlevel1) and encoded_parent_ids is not null;
create temporary table catlevel3 as 
select * from category_variable where parent_id in (select id from catlevel2);

update category_variable
    set new_id = (select new_id from category_variable cv2 where cv2.id =category_variable.parent_id)
where id in (select id from catlevel3);

update category_variable set new_id = 79 where parent_id = 2; --misplaced natural modification children remapped to fauna > natural modification
update category_variable set new_id = 70 where parent_id = 3; --misplaced cultural modification children remapped to fauna > cultural modification
update category_variable set new_id = 79 where id = 2; --misplaced natural modification remapped to fauna > natural modification
update category_variable set new_id = 70 where id = 3; --misplaced cultural modification remapped to fauna > cultural modification
update category_variable set new_id = 78 where id = 1; --misplaced modification remapped to fauna > modification


--execute manual mappings from keith
update category_variable set new_id = 67 where id = 20; -- Fauna, % Complete= Completeness 
update category_variable set new_id = 62 where id = 183; -- Fauna, Anterior Posterior = Anterior/Posterior 
update category_variable set new_id = 215 where id = 172; -- Fauna, Excavated Volumes = (provenience Volume) 
update category_variable set new_id = 198 where id = 173; -- Fauna, Horizontal Location = (provenience Horizontal Location) 
update category_variable set new_id = 77 where id = 16; -- Fauna, Length = Measurement 
update category_variable set new_id = 69 where id = 19; -- Fauna, Maximum Count = Count 
update category_variable set new_id = 69 where id = 18; -- Fauna, Minimum Count = Count 
update category_variable set new_id = 81 where id = 11; -- Fauna, Portion = Portion/Proximal/Distal 
update category_variable set new_id = 81 where id = 12; -- Fauna, Proximal/Distal = Portion/Proximal/Distal 
update category_variable set new_id = 207 where id = 176; -- Fauna, Recovery Method = (provenience Recovery Method) 
update category_variable set new_id = 214 where id = 177; -- Fauna, Vertical Position = (provenience Vertical Position) 
update category_variable set new_id = 94 where id = 71; -- Figurine, Type = Style/Type 
update category_variable set new_id = 110 where id = 141; -- Historic Artifacts, Type = Form (?) 
update category_variable set new_id = 215 where id = 54; -- Provenience and Context, Excavated Volumes = Volume 
update category_variable set new_id = 196 where id = 44; -- Provenience and Context, Feature = Feature ID/Number 
update category_variable set new_id = 200 where id = 36; -- Provenience and Context, Slash = Item/Slash 
update category_variable set new_id = 76 where id = 23; -- animal modification = gnawing/ animal modification
update category_variable set new_id = 76 where id = 24;  -- gnawing = gnawing / animal modification
update category_variable set new_id = 63 where id = 27; -- Bone Artifact = Bone Artifact Form

--obvious mappings 
update category_variable set new_id = 17 where id = 50; -- minerals > mineral (did you know we have is mineral>mineral? why??)
update category_variable set new_id = 10  where id = 55; -- map historic artifacts -> historic other 
update category_variable set new_id = 108  where id = 142; -- map historic artifacts > count -> historic other > count
update category_variable set new_id = 114  where id = 143; -- map historic artifacts > weight -> historic other > weight
update category_variable set new_id = 10  where id = 144; -- map historic artifacts > other -> historic other

update category_variable set new_id = 11  where id = 53; -- burial -> human burial
update category_variable set new_id = 38  where id = 115; -- ceramics > design -->  ceramics > Design/Decorative Element

--disable fk's pointing to category_variable
alter table data_table_column drop constraint fke5d0f5c22109f6c;
alter table category_variable_synonyms drop constraint fkcb09742b369bbe7;
alter table coding_sheet drop constraint fk59a30c4a22109f6c;
alter table ontology drop constraint fk91d1094f22109f6c;
alter table data_table drop constraint fk608fa6f922109f6c;

--update references 
update data_table_column set category_variable_id = (select new_id from category_variable where category_variable.id  = data_table_column.category_variable_id);
update category_variable_synonyms set categoryvariable_id = (select new_id from category_variable where category_variable.id  = category_variable_synonyms.categoryvariable_id);
update coding_sheet set category_variable_id = (select new_id from category_variable where category_variable.id = coding_sheet.category_variable_id);
update ontology set category_variable_id = (select new_id from category_variable where category_variable.id = ontology.category_variable_id);
update data_table set category_variable_id = (select new_id from category_variable where category_variable.id = data_table.category_variable_id);


--add references to new table
alter table data_table_column add constraint fk_data_table_column__category_variable foreign key (category_variable_id) references category_variable_new(id);
alter table category_variable_synonyms add constraint fk_category_variable_synonyms__category_variable foreign key (categoryvariable_id) references category_variable_new(id);
alter table coding_sheet add constraint fk_coding_sheet__category_variable foreign key (category_variable_id) references category_variable_new(id);
alter table ontology add constraint fk_ontology__category_variable foreign key (category_variable_id) references category_variable_new(id);
alter table data_table add constraint fk_data_table__category_variable foreign key (category_variable_id) references category_variable_new(id);


--ditch encoded parent id's (no longer used needed we only have 2 levels of categories)
alter table category_variable_new drop column encoded_parent_ids;

--clean up 
alter table category_variable rename to category_variable_old;
alter table category_variable_new rename to category_variable;
--drop table category_variable_old;
/* cat/subcat update end */

--per keith ceramics should follow the singular convention of the other categories
update category_variable set name='Ceramic', label='Ceramic' where name = 'Ceramics' and type = 'CATEGORY';

alter table personal_filestore_ticket alter COLUMN description TYPE varchar(2000);

--7/24/2011
ALTER TABLE sensory_data ALTER estimated_data_resolution TYPE character varying(255);
ALTER TABLE sensory_data_scan ALTER resolution TYPE character varying(255);
update category_variable set parent_id = null where type = 'CATEGORY';

--8/4/2011
ALTER TABLE creator ADD description TEXT;

--8/16/2011
ALTER TABLE collection ALTER description TYPE TEXT;
ALTER TABLE collection ADD date_created TIMESTAMP;

--8/17/2011
ALTER TABLE collection add sort_order varchar(25);
UPDATE collection SET date_Created = now();
alter table culture_keyword add foreign key (parent_id) references culture_keyword;
alter table site_type_keyword add foreign key (parent_id) references site_type_keyword;
alter table institution add foreign key (parentinstitution_id) references institution;


--9/12/2011
alter table collection add status varchar(50);
update collection set status = 'ACTIVE';

--9/15/2011
alter table collection add "sort_index" character varying;
alter table collection drop column status;

--10/19/2011  adding indexes
create index authorized_user_cid on authorized_user (id, resource_collection_id);
create index authorized_user_perm on authorized_user (general_permission_int, user_id, resource_collection_id);
alter table coding_rule set description varchar(2000);