alter table sensory_data add column additional_project_notes varchar(255);
alter table sensory_data add column company_name varchar(255);
alter table sensory_data add column decimated_mesh_dataset varchar(255);
alter table sensory_data add column decimated_mesh_original_triangle_count int8;
alter table sensory_data add column decimated_mesh_triangle_count int8;
alter table sensory_data add column final_dataset_description varchar(255);
alter table sensory_data add column final_registration_points int8;
alter table sensory_data add column control_data_present boolean default FALSE not null;
alter table sensory_data add column planimetric_map boolean default FALSE not null;
alter table sensory_data add column turntable_used boolean default FALSE not null;
alter table sensory_data add column mesh_adjustment_matrix character varying(255);
alter table sensory_data add column mesh_coordinate_system_adjustment boolean default FALSE not null;
alter table sensory_data add column mesh_dataset_name varchar(255);
alter table sensory_data add column mesh_holes_filled boolean default FALSE not null;
alter table sensory_data add column mesh_processing_notes varchar(255);
alter table sensory_data add column mesh_rgb_included boolean default FALSE not null;
alter table sensory_data add column mesh_smoothing boolean default FALSE not null;
alter table sensory_data add column mesh_triangle_count int8;
alter table sensory_data add column mesh_data_reduction boolean default FALSE not null;
alter table sensory_data add column monument_number varchar(255);
alter table sensory_data add column premesh_dataset_name varchar(255);
alter table sensory_data add column premesh_points int8;
alter table sensory_data add column premesh_color_editions boolean default FALSE not null;
alter table sensory_data add column premesh_overlap_reduction boolean default FALSE not null;
alter table sensory_data add column premesh_smoothing boolean default FALSE not null;
alter table sensory_data add column premesh_subsampling boolean default FALSE not null;
alter table sensory_data add column registered_dataset_name varchar(255);
alter table sensory_data add column registration_error_units float8;
alter table sensory_data add column rgb_data_capture_info varchar(255);
alter table sensory_data add column rgb_preserved_from_original boolean default FALSE not null;
alter table sensory_data add column scanner_details varchar(255);
alter table sensory_data add column scans_total_acquired int4;
alter table sensory_data add column scans_used int4;
alter table sensory_data add column survey_conditions varchar(255);
alter table sensory_data add column survey_date timestamp;
alter table sensory_data add column survey_location varchar(255);

alter table sensory_data add column estimated_data_resolution float8;
alter table sensory_data add column total_scans_in_project int8;
alter table sensory_data add column point_deletion_summary varchar(255);
alter table sensory_data add column mesh_color_editions boolean default FALSE not null;
alter table sensory_data add column mesh_healing_despiking boolean default FALSE not null;

update data_table_column set column_encoding_type='CODED_VALUE' where default_coding_sheet_id is not null;

create table sensory_data_image (id  bigserial not null, description varchar(255), filename varchar(255) not null, sensory_data_id int8, sequence_number int8 not null default 1, primary key (id));
create table sensory_data_scan (id  bigserial not null, filename varchar(255) not null, monument_name varchar(255), points_in_scan int8, resolution int8, scan_notes varchar(255), scanner_technology varchar(255), transformation_matrix varchar(255), triangulation_details varchar(255), sensory_data_id int8, sequence_number int8 not null default 1, primary key (id));

alter table information_resource_file rename sequence to sequence_number;
alter table information_resource_file add column confidential bool;
update information_resource_file irf set confidential = (select confidential from resource where resource.id=irf.information_resource_id);
update information_resource_file set download_count=0 where download_count is null;


create table coverage_date (id  bigserial not null, date_type varchar(255), end_date int4, start_date int4, resource_id int8, primary key (id));
alter table coverage_date add constraint FKF246A3A532793D68 foreign key (resource_id) references resource;

insert INTO coverage_date (date_type, resource_id, start_date,end_date) select 'CALENDAR_DATE',resource_id, start_date,end_date from calendar_date;
insert INTO coverage_date (date_type, resource_id, start_date,end_date) select 'RADIOCARBON_DATE',resource_id, start_date,end_date from radiocarbon_date;

/* use postgres enum type or a varchar? */
/* create type irfstatus as enum ('QUEUED', 'PROCESSED', 'DELETED'); */
alter table information_resource_file add column status varchar(32);
update information_resource_file set status='QUEUED' where queued='t';
update information_resource_file set status='PROCESSED' where processed='t';
drop table resource_relationship;
drop table calendar_date;
drop sequence calendar_date_id_seq;
drop table radiocarbon_date;
drop sequence radiocarbon_date_id_seq;

-- cleanup orphan sequence tables
drop sequence column_encoding_type_id_seq;
drop sequence creator_institution_role_id_seq;
drop sequence creator_person_role_id_seq;
drop sequence language_id_seq;
drop sequence measurement_unit_id_seq;
drop sequence resource_creator_institution_id_seq;
drop sequence resource_creator_person_id_seq;
drop sequence resource_provider_contact_id_seq;
-- FIXME: JIM: is it safe to remove tmp_person?
-- drop table tmp_person;
-- drop sequence tmp_person_id_seq;

CREATE INDEX resId_sesory_data_img
  ON sensory_data_image
  USING btree
  (sensory_data_id, id);
  
CREATE INDEX resId_sesory_data_scan
  ON sensory_data_scan
  USING btree
  (sensory_data_id, id);

CREATE INDEX creator_sequence
  ON resource_creator
  USING btree
  (resource_id, sequence_number, creator_id);

CREATE INDEX coverage_resid
  ON coverage_date
  USING btree
  (resource_id, id);

  
alter table coverage_date add column start_aprox boolean default FALSE not null;
alter table coverage_date add column end_aprox boolean default FALSE not null;
alter table coverage_date add column description character varying(255);
update coverage_date set start_aprox=false, end_aprox=false;


create table collection (id  bigserial not null, description varchar(255), name varchar(255), owner_id bigint, parent_id bigint, collection_type varchar(255), primary key (id));
create table collection_resource (collection_id bigint not null, resource_id bigint not null, primary key (collection_id, resource_id));
alter table collection_resource add constraint FK74B2D88F53679086 foreign key (collection_id) references collection;
alter table collection_resource add constraint FK74B2D88FD20877F1 foreign key (resource_id) references resource;

create table authorized_user (id  bigserial not null, admin_permission varchar(255), modify_permission varchar(255), view_permission varchar(255), user_id int8, resource_collection_id int8, primary key (id));
alter table authorized_user add constraint FKB234F7EF1E1B5338 foreign key (resource_collection_id) references collection;


alter table sensory_data drop column control_data_present;
alter table sensory_data drop column planimetric_map;
alter table sensory_data drop column mesh_coordinate_system_adjustment;
alter table sensory_data rename survey_date to survey_date_begin;
alter table sensory_data add column survey_date_end timestamp;

alter table sensory_data_scan add column tof_return varchar(255);
alter table sensory_data_scan add column phase_frequency_settings varchar(255);
alter table sensory_data_scan add column phase_noise_settings varchar(255);
alter table sensory_data_scan add column camera_exposure_settings varchar(255);
alter table sensory_data_scan add column planimetric_map_filename varchar(255);
alter table sensory_data_scan add column control_data_filename varchar(255);

alter table sensory_data_scan add column scan_date timestamp;

alter table sensory_data_scan drop column planimetric_map_filename;
alter table sensory_data_scan drop column control_data_filename;

alter table sensory_data add column planimetric_map_filename varchar(255);
alter table sensory_data add column control_data_filename varchar(255);
alter table sensory_data_scan add column matrix_applied boolean default FALSE not null;

alter table information_resource_file drop column queued;
alter table information_resource_file drop column processed;

update information_resource set date_created=NULL where date_created='';

alter table sensory_data add column registration_method varchar(255);

