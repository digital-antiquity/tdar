alter table data_table_column rename coding_sheet_id to default_coding_sheet_id;
alter table resource_relationship add submitter_id bigint references person(id);

alter table document add doi varchar(255);
alter table document add start_page integer;
alter table document add end_page integer;

drop table concept;
alter table ontology drop resource_id;
alter table ontology drop constraint "fk91d1094fe8e3bf97";

alter table ontology add foreign key (id) references information_resource(id);

create table ontology_node (
    id bigserial primary key,
    ontology_id bigint references ontology(id),
    interval_start integer,
    interval_end integer,
    label character varying,
    uri character varying,
    index character varying 
    );

create index ontology_node_index on ontology_node(index);
 
create table data_value_ontology_node_mapping (
    id bigserial primary key,
    data_table_column_id bigint references data_table_column(id),
    data_value character varying,
    ontology_node_id bigint references ontology_node(id)
    );
    
update information_resource set available_to_public='t' where available_to_public is null;

insert into information_resource_format_file_extension VALUES (2, 'xlsx');
update dcmi_type set active = false where active is null;

alter table contributor_request rename dateapproved to date_approved;
alter table contributor_request add column comments character varying;

update resource_revision_log set person_id=6 where id in (2,3,5,6,8,9,10,11,12);
update resource_revision_log set person_id=5979 where id in (1,7);

create index category_variable_parent_index on category_variable(encoded_parent_ids);

alter table document drop manuscript_description;

create table information_resource_file (
    id bigserial primary key,
    information_resource_id bigint references information_resource(id),
    filestore_id varchar(32),
    filename character varying,
    checksum character varying,
    checksum_type character varying
    );

alter table information_resource_file add column download_count int;
alter table information_resource_file add column translated_file_id bigint references information_resource_file(id);

alter table information_resource_file add column information_resource_format_id bigint references information_resource_format(id);
alter table information_resource_file add column mime_type character varying;

create table resource_material_keyword (
    id bigserial primary key,
    resource_id bigint references resource(id),
    material_keyword character varying
    );
    
insert into resource_material_keyword (resource_id, material_keyword) 
select resource_id, regexp_replace(upper(keyword), ' ', '_', 'g') 
from keyword where dtype = 'MaterialSubjectKeyword';

delete from keyword where dtype = 'MaterialSubjectKeyword';
delete from resource_material_keyword where material_keyword = 'OTHER';

create table resource_investigation_type (
    id bigserial primary key,
    resource_id bigint references resource(id),
    investigation_type character varying
    );
    
alter table resource drop download_counter;
