-- $Id$
-- $Revision$
-- database upgrade script for schema changes between gridsphere tdar and
-- struts 2 version of tdar.

/* resource relationship table now contains all relationships between resources, for better or worse */
CREATE TABLE resource_relationship (
    id bigserial,
    /* FIXME: should be an enum for efficiency but enums are only supported in postgres 8.3+ */
    relationship_type character varying(255),
    first_id bigint references resource(id) on delete cascade on update cascade,
    second_id bigint references resource(id) on delete cascade on update cascade,
    primary key (id)
);

/* add associations between coding sheets and other resources */
insert into resource_relationship (relationship_type, first_id, second_id) 
    select 'CODING_SHEET_PROJECT', coding_sheet_id, resource_id from coding_sheet_resource_relationship where resource_id in (select id from project);

/* ontologies used to be used as a "concept" container */
insert into resource_relationship (relationship_type, first_id, second_id) 
    select 'CODING_SHEET_ONTOLOGY', coding_sheet_id, resource_id from coding_sheet_resource_relationship where resource_id in (select id from ontology);

/* FIXME: not sure if these should be destroyed or what */
insert into resource_relationship (relationship_type, first_id, second_id) 
    select 'CODING_SHEET_DATASET', coding_sheet_id, resource_id from coding_sheet_resource_relationship where resource_id in (select id from dataset);

/* add associations between coding sheets and data table columns to the resource_relationships table */
insert into resource_relationship (relationship_type, first_id, second_id) 
    select 'CODING_SHEET_DATA_TABLE_COLUMN', coding_sheet_id, resource_id from coding_sheet_resource_relationship where resource_id in (select id from data_table_column);

insert into resource_relationship (relationship_type, first_id, second_id)
    select 'CODING_SHEET_DATA_TABLE_COLUMN', coding_sheet_id, id from data_table_column where coding_sheet_id is not null;

alter table dataset add column translated_file_location varchar(255);
alter table data_table add column translated_table_name varchar(255);

create table contributor_request (
        id bigint not null, 
        approved bool not null,
        contributor_reason varchar(512), 
        dateApproved date, 
        timestamp timestamp not null, 
        applicant_id bigint references person(id) on update cascade, 
        approver_id bigint references person(id) on update cascade, 
        primary key (id)
);

create table institution (
        id bigserial,
        location varchar(255),
        name varchar(255) not null unique,
        url varchar(255),
        primary key (id)
);

create table user_session (
        id bigserial,
        session_start timestamp without time zone not null,
        session_end timestamp without time zone,
        person_id bigint references person(id) on update cascade,
        primary key(id)
);

create table citation (
        dtype varchar(31) not null,
        id bigserial,
        fedora_pid varchar(255),
        text varchar(1024),
        resource_id bigint references resource(id) on update cascade,
        primary key(id)
);

-- migrate citation data
insert into citation (dtype, text, resource_id) select 'SourceCitation', text, resource_id from source_citation;
insert into citation (dtype, text, resource_id) select 'SourceCollection', text, resource_id from source_collection;
insert into citation (dtype, text, resource_id) select 'RelatedCitation', text, resource_id from related_citation;
insert into citation (dtype, text, resource_id) select 'RelatedComparativeCollection', text, resource_id from related_comparative_collection;

drop table source_citation;
drop table source_collection;
drop table related_citation;
drop table related_comparative_collection;

-- alter resource_creator_institution to point at the institution table instead of storing the institution name directly.
alter table resource_creator_institution add column institution_id int8 references institution(id);

-- add log_message column to resource_revised_date
alter table resource_revised_date add column log_message varchar(512);
-- create sequence for resource_revised_date and then rename it to resource_revision_log
alter table resource_revised_date rename column date_revised to timestamp;
create sequence resource_revision_log_id_seq;
alter table resource_revised_date alter column id set default nextval('resource_revision_log_id_seq'::regclass);
select setval('resource_revision_log_id_seq', max(id)) from resource_revised_date;
alter table resource_revised_date rename to resource_revision_log;

-- make all person emails lowercase
update person set email=lower(email);
update person set email=null where email='';
-- FIXME: come up with an SQL query to replace this hard coded crap?
update resource_creator_person set person_id=100 where person_id=6074;
update resource_creator_person set person_id=5979 where person_id=3371;
-- special case delete lower id'ed user since higher id has more relations already
delete from person where id=3371;
delete from person where id in (select p1.id from person p1, person p2 where p1.email=p2.email and p1.id > p2.id);

/* replace institution names with ids from institution table */
insert into institution(name) select distinct name from resource_creator_institution;
update resource_creator_institution set institution_id = (select id from institution where institution.name=resource_creator_institution.name);
alter table resource_creator_institution drop column name;


-- add/modify new person fields
alter table person add column rpa bool not null default 'f';
alter table person add column rpa_number character varying(255);
alter table person add column phone character varying(255);
alter table person add column password character varying(255);
alter table person add column contributor_reason character varying(512);
alter table person add column institution_id int8 references institution(id);
alter table person rename column lastname to last_name;
alter table person rename column firstname to first_name;

/* set registered flag to true for all persons with a non-null gsoid*/
update person set registered='t' where gsoid is not null;
/* remove gsoid column */
alter table person drop column gsoid;
-- delete "Root User" from GEON
delete from person where id=5348;

/* add existing institutions to institutions table that don't already exist in the institution table */
insert into institution(name) select distinct institution from person where institution not in (select name from institution);
update person set institution_id = (select id from institution where institution.name=person.institution);
alter table person drop column institution;

/* add existing institutions (publisherName) from dataset to institutions table */
alter table dataset add column provider_institution_id int8 references institution(id);
insert into institution(name) select distinct publisher_name from dataset where publisher_name not in (select name from institution);
update dataset set provider_institution_id = (select id from institution where institution.name=dataset.publisher_name);
alter table dataset drop column publisher_name;
alter table dataset rename column type_id to dcmi_type_id;
alter table dataset alter column dataset_format_id drop not null;
alter table dataset alter column location drop not null;

alter table dcmi_type rename column term_name to name;
alter table dcmi_type add column description varchar(512);

alter table dataset_publisher rename column dataset_id to resource_id;
alter table dataset_publisher rename to publisher;

-- need to modify these names in order to conform with OWL naming conventions
-- replace spaces with underscores
alter table master_ontology add column label character varying(255);
UPDATE master_ontology SET label=regexp_replace(name, E'[\\s/]', '_', 'g');
-- change % Complete to Percent_Complete
UPDATE master_ontology SET label='Percent_Complete' WHERE id=20;
-- rename master_ontology and master_ontology_synonym to category_variable
alter table master_ontology rename to category_variable;
alter table master_ontology_synonyms rename to category_variable_synonyms;
alter table category_variable_synonyms rename column master_ontology_id to category_variable_id;
alter table category_variable alter column id type int8;
alter index master_ontology_pkey rename to category_variable_pkey;
alter table category_variable alter column parent_id type int8;

-- rename associated foreign key relationships
alter table coding_sheet rename column domain_context_variable_id to category_variable_id;
alter table data_table rename column domain_context_variable_id to category_variable_id;
alter table data_table_column rename column domain_context_variable_id to category_variable_id;
alter table measurement_unit rename column domain_context_variable_id to category_variable_id;
alter table ontology rename column domain_context_variable_id to category_variable_id;

-- add fedora PID to resource table
-- fedora pids are supposed to have a max size of 64 chars, according to 
-- http://www.fedora-commons.org/confluence/display/FCR30/Fedora+Identifiers
alter table resource add column fedora_pid character varying(255);
-- add active column to resource table
alter table resource add column active bool default true;

-- change primary keys in most tables to bigserial type (e.g., not null default nextval(table_seq))
-- XXX: should learn pgplsql to script this in the future instead of using vim
-- macros to generate them.
create sequence bookmarked_resource_id_seq;
alter table bookmarked_resource alter column id set default nextval('bookmarked_resource_id_seq'::regclass);
select setval('bookmarked_resource_id_seq', max(id)) from bookmarked_resource;

create sequence category_variable_id_seq;
alter table category_variable alter column id set default nextval('category_variable_id_seq'::regclass);
select setval('category_variable_id_seq', max(id)) from category_variable;

create sequence coding_rule_id_seq;
alter table coding_rule alter column id set default nextval('coding_rule_id_seq'::regclass);
select setval('coding_rule_id_seq', max(id)) from coding_rule;

create sequence column_encoding_type_id_seq;
alter table column_encoding_type alter column id set default nextval('column_encoding_type_id_seq'::regclass);
select setval('column_encoding_type_id_seq', max(id)) from column_encoding_type;

create sequence contributor_request_id_seq;
alter table contributor_request alter column id set default nextval('contributor_request_id_seq'::regclass);
select setval('contributor_request_id_seq', max(id)) from contributor_request;

create sequence coverage_calendar_date_id_seq;
alter table coverage_calendar_date alter column id set default nextval('coverage_calendar_date_id_seq'::regclass);
select setval('coverage_calendar_date_id_seq', max(id)) from coverage_calendar_date;

create sequence coverage_geographic_term_id_seq;
alter table coverage_geographic_term alter column id set default nextval('coverage_geographic_term_id_seq'::regclass);
select setval('coverage_geographic_term_id_seq', max(id)) from coverage_geographic_term;

create sequence coverage_longitude_latitude_id_seq;
alter table coverage_longitude_latitude alter column id set default nextval('coverage_longitude_latitude_id_seq'::regclass);
select setval('coverage_longitude_latitude_id_seq', max(id)) from coverage_longitude_latitude;

create sequence coverage_radiocarbon_date_id_seq;
alter table coverage_radiocarbon_date alter column id set default nextval('coverage_radiocarbon_date_id_seq'::regclass);
select setval('coverage_radiocarbon_date_id_seq', max(id)) from coverage_radiocarbon_date;

create sequence coverage_temporal_term_id_seq;
alter table coverage_temporal_term alter column id set default nextval('coverage_temporal_term_id_seq'::regclass);
select setval('coverage_temporal_term_id_seq', max(id)) from coverage_temporal_term;

create sequence creator_institution_role_id_seq;
alter table creator_institution_role alter column id set default nextval('creator_institution_role_id_seq'::regclass);
select setval('creator_institution_role_id_seq', max(id)) from creator_institution_role;

create sequence creator_person_role_id_seq;
alter table creator_person_role alter column id set default nextval('creator_person_role_id_seq'::regclass);
select setval('creator_person_role_id_seq', max(id)) from creator_person_role;

-- FIXME: sequence may not be needed
alter table dataset_format rename column format to name;
alter table dataset_format add column mime_type varchar(255);
create sequence dataset_format_id_seq;
alter table dataset_format alter column id set default nextval('dataset_format_id_seq'::regclass);
select setval('dataset_format_id_seq', max(id)) from dataset_format;

create sequence publisher_id_seq;
alter table publisher alter column id set default nextval('publisher_id_seq'::regclass);
select setval('publisher_id_seq', max(id)) from publisher;

/* don't need sequences for tiny fixed tables like dcmi_type */
-- create sequence dcmi_type_id_seq; 
-- alter table dcmi_type alter column id set default nextval('dcmi_type_id_seq'::regclass);
-- select setval('dcmi_type_id_seq', max(id)) from dcmi_type;

create sequence full_team_id_seq;
alter table full_team alter column id set default nextval('full_team_id_seq'::regclass);
select setval('full_team_id_seq', max(id)) from full_team;
-- remove keith's full_team entry in Heshotauthla, since he is the submitter
-- select f.id, r.id, r.title, f.person_id from full_team f inner join resource r on f.resource_id=r.id and f.person_id=r.submitter_id;
-- remove all submitters that are also in the full_team
delete from full_team where id in ( select f.id from full_team f inner join resource r on f.resource_id=r.id and f.person_id=r.submitter_id );

create sequence keyword_id_seq;
alter table keyword alter column id set default nextval('keyword_id_seq'::regclass);
select setval('keyword_id_seq', max(id)) from keyword;

create sequence language_id_seq;
alter table language alter column id set default nextval('language_id_seq'::regclass);
select setval('language_id_seq', max(id)) from language;

create sequence measurement_unit_id_seq;
alter table measurement_unit alter column id set default nextval('measurement_unit_id_seq'::regclass);
select setval('measurement_unit_id_seq', max(id)) from measurement_unit;

create sequence person_id_seq;
alter table person alter column id set default nextval('person_id_seq'::regclass);
select setval('person_id_seq', max(id)) from person;

create sequence read_team_id_seq;
alter table read_team alter column id set default nextval('read_team_id_seq'::regclass);
select setval('read_team_id_seq', max(id)) from read_team;
-- delete all read_users that are also submitters of the resource, since they already have r/w access.
delete from read_team where id in ( select f.id from read_team f inner join resource r on f.resource_id=r.id and f.person_id=r.submitter_id );

-- create sequence related_citation_id_seq;
-- alter table related_citation alter column id set default nextval('related_citation_id_seq'::regclass);
-- alter table related_citation add column fedora_pid varchar(255);
-- select setval('related_citation_id_seq', max(id)) from related_citation;

-- create sequence related_comparative_collection_id_seq;
-- alter table related_comparative_collection alter column id set default nextval('related_comparative_collection_id_seq'::regclass);
-- alter table related_comparative_collection add column fedora_pid varchar(255);
-- select setval('related_comparative_collection_id_seq', max(id)) from related_comparative_collection;

create sequence resource_creator_institution_id_seq;
alter table resource_creator_institution alter column id set default nextval('resource_creator_institution_id_seq'::regclass);
select setval('resource_creator_institution_id_seq', max(id)) from resource_creator_institution;

create sequence resource_creator_person_id_seq;
alter table resource_creator_person alter column id set default nextval('resource_creator_person_id_seq'::regclass);
select setval('resource_creator_person_id_seq', max(id)) from resource_creator_person;

-- create sequence source_citation_id_seq;
-- alter table source_citation alter column id set default nextval('source_citation_id_seq'::regclass);
-- alter table source_citation add column fedora_pid varchar(255);
-- select setval('source_citation_id_seq', max(id)) from source_citation;

-- create sequence source_collection_id_seq;
-- alter table source_collection alter column id set default nextval('source_collection_id_seq'::regclass);
-- alter table source_collection add column fedora_pid varchar(255);
-- select setval('source_collection_id_seq', max(id)) from source_collection;

-- add this column to denote which fields one term comes from, e.g., title, description, these are defined in search.bean.TermSourceType
alter table resource_term add column term_source varchar(255); 

-- added 4/26/09
alter table dcmi_type add column label varchar(255);
alter table dcmi_type add column active bool;
update dcmi_type set label=name;
update dcmi_type set label='Document' where name='Text';
-- according to https://issues.tdar.org/browse/TDAR-300 only enable the
-- following dcmi types
update dcmi_type set active=true where name in ('Dataset', 'Moving Image', 'Still
Image', 'Text');

-- update locations in dataset table to just hold the file name of the uploaded dataset file.
-- using a less restrictive regex to completely capture the file name (even if it's damaged)
update dataset set location=substring(location from E'data/\\w+/(.*)$')
where location is not null and location != '';
update dataset set location=regexp_replace(location, E'[^\\w.]', '_', 'g');
update dataset set location=null where location='';

-- using a less restrictive regex to completely capture the file name (even if it's damaged)
update coding_sheet set path=substring(path from E'coding_sheets/(.*)$')
where path is not null and path != '';

alter table dataset rename column location to filename;
alter table dataset rename column translated_file_location to translated_filename;
alter table coding_sheet rename column path to filename;
-- also need to delete resources, and resource_relationships?  don't set null constraint for now
-- delete from coding_sheet where filename is null;
-- alter table coding_sheet alter column filename set not null;
alter table resource_revision_log add column person_id bigint;
alter table resource_revision_log add foreign key (person_id) references person(id);

drop table coding_sheet_resource_relationship;

-- document/image/information resource refactoring, 7/21/2009, alllee
-- copy dataset to information_resource table
create table information_resource as select * from dataset;
-- add foreign keys to information_resource
alter table information_resource add foreign key(provider_institution_id) references institution(id);
alter table information_resource add foreign key(resource_language_id) references language(id);
alter table information_resource add foreign key(metadata_language_id) references language(id);
alter table information_resource add foreign key (dataset_format_id) references dataset_format(id);
alter table information_resource add foreign key (original_format_id) references dataset_format(id);
alter table information_resource add foreign key (dcmi_type_id) references dcmi_type(id);
alter table information_resource add foreign key (project_id) references project(id);
alter table information_resource add foreign key (id) references resource(id);
alter table information_resource add primary key (id);
-- modify column names
alter table information_resource rename column dataset_format_id to information_resource_format_id;
-- nothing in translated_filename yet, so no need to copy data
alter table information_resource drop column translated_filename;
-- drop newly redundant fields in dataset
alter table dataset drop column public;
alter table dataset drop column date_created;
alter table dataset drop column date_made_public;
alter table dataset drop column filename;
alter table dataset drop column project_id;
alter table dataset drop column dataset_format_id;
alter table dataset drop column dcmi_type_id;
alter table dataset drop column resource_language_id;
alter table dataset drop column original_format_id;
alter table dataset drop column metadata_language_id;
alter table dataset drop column provider_institution_id;
alter table dataset drop constraint fk5605b478e8e3bf97;
alter table dataset add foreign key (id) references information_resource(id);

-- rename dataset_format table to information_resource_format
alter table dataset_format rename column archival_dataset_format_id to archival_format_id;
alter table dataset_format rename to information_resource_format;
alter table dataset_format_id_seq rename to information_resource_format_id_seq;
alter index dataset_format_pkey rename to information_resource_format_pkey;

-- rename dataset_format_file_extension table (file extensions collection dependency from dataset_format)
alter table dataset_format_file_extension rename column dataset_format_id to information_resource_format_id;
alter table dataset_format_file_extension rename to information_resource_format_file_extension;

-- rename full_user and read_user tables
alter table full_team rename to full_user;
alter table read_team rename to read_user;
alter sequence full_team_id_seq rename to full_user_id_seq;
alter sequence read_team_id_seq rename to read_user_id_seq;

-- removing url from ontology since resource now has url
-- alter table ontology rename column path to url;
alter table ontology drop column path;

alter table coding_sheet add column project_id bigint references project(id);

create table document (
    copy_location varchar(255),
    document_type varchar(255) not null,
    edition varchar(255),
    isbn varchar(255),
    number_of_pages integer,
    number_of_volumes integer,
    publisher varchar(255),
    publisher_location varchar(255),
    series_name varchar(255),
    series_number varchar(255),
    volume varchar(255),
    journal_name varchar(255),
    journal_number varchar(255),
    issn varchar(255),
    book_title varchar(255),
    manuscript_description varchar(512),
    id bigserial primary key references information_resource(id) on delete cascade on update cascade
);

create index document_type_index on document(document_type);

--create table document_person_role (
--    id serial,
--    name varchar(255) not null,
--    primary key (id)
--);

--insert into document_person_role (id, name) values 
--    (1, 'Author'),
--    (2, 'Contributor'),
--    (3, 'Editor'),
--    (4, 'Series Editor'),
--    (5, 'Translator');

create table document_creator_person (
    id bigserial primary key,
    ordinal integer not null,
    role varchar(255) not null,
    document_id bigint references document(id),
    person_id bigint references person(id)
);
    
-- moving urls to resource.
alter table resource add column url varchar(255);

alter table information_resource add column last_uploaded timestamp;
alter table information_resource add column external_reference bool default false;

alter table information_resource rename column public to available_to_public;

-- insert all coding_sheets into into information_resource
insert into information_resource (id, available_to_public, filename, project_id) select id, true, filename, project_id from coding_sheet;

alter table coding_sheet drop column filename;
alter table coding_sheet drop column project_id;

alter table coding_sheet drop constraint fk59a30c4ae8e3bf97;
alter table coding_sheet add foreign key (id) references information_resource(id);

-- 9/8/09 Matt: add mime types and additional formats  

update information_resource_format set mime_type='application/x-msaccess' where id = 0;
update information_resource_format set mime_type='text/plain' where id = 1;
update information_resource_format set mime_type='application/vnd.ms-excel' where id = 2;
update information_resource_format set mime_type='text/csv' where id = 3;
update information_resource_format set mime_type='application/rdf+xml' where id = 5;
update information_resource_format set mime_type='application/pdf' where id = 6;
update information_resource_format set mime_type='text/plain' where id = 7;
update information_resource_format set mime_type='application/octet-stream' where id = 8;

insert into information_resource_format values(9, 'JPEG', 9, 'image/jpeg');
insert into information_resource_format values(10, 'PNG', 10, 'image/png');
insert into information_resource_format values(11, 'TIFF', 11, 'image/tiff');
insert into information_resource_format values(12, 'GIF', 12, 'image/gif');

insert into information_resource_format_file_extension values (9, 'jpeg');
insert into information_resource_format_file_extension values (9, 'jpg');
insert into information_resource_format_file_extension values (10, 'png');
insert into information_resource_format_file_extension values (11, 'tiff');
insert into information_resource_format_file_extension values (12, 'gif');

delete from information_resource_format_file_extension where information_resource_format_id = 4;
update information_resource set information_resource_format_id = 9 where information_resource_format_id = 4;
update information_resource set original_format_id = 9 where original_format_id = 4;
delete from information_resource_format where id = 4;

-- CODING_SHEET_PROJECT should only be used for additional relationship between
-- coding sheets and projects.  The project that "owns" a coding sheet is stored in
-- coding_sheet.project_id.
update information_resource 
set project_id=
(select rr.second_id 
from resource_relationship rr 
where relationship_type='CODING_SHEET_PROJECT' and rr.first_id=information_resource.id);

delete from resource_relationship where relationship_type='CODING_SHEET_PROJECT';

update resource set resource_type='CODING_SHEET' where id in (select id from coding_sheet);

-- FIXME: is this dirty to set the parent of a root category to be itself?  Otherwise we
-- get errors in view/edit coding sheets when we set it to a CATEGORY like Fauna.
update category_variable set parent_id=id where type='CATEGORY';

-- fix orphaned information resources with no project, set their project_id to the
-- independent resources project for now
update information_resource 
set project_id=project.id
from resource r, resource rr, person, project
where information_resource.project_id is null and r.id=information_resource.id 
and r.submitter_id=person.id and rr.resource_type='INDEPENDENT_RESOURCES_PROJECT' 
and rr.submitter_id=r.submitter_id and rr.id=project.id;
