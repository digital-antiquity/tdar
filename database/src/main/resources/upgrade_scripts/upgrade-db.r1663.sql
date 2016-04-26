
-- the following populates resource_creator but is not represented in the code -- uncomment for testing only
 insert into resource_creator (sequence_number ,role, creator_id, resource_id) select ordinal,role, person_id,document_id from document_author ;
 insert into resource_creator (role, creator_id, resource_id) select role, institution_id,resource_id from resource_creator_institution;
 insert into resource_creator (role, creator_id, resource_id) select role, person_id,resource_id from resource_creator_person;
 insert into resource_creator (role, creator_id, resource_id) select 'CONTACT', person_id,resource_id from resource_provider_contact;



-- insert into resource_annotation_key (annotation_data_type, resource_annotation_type,"key") VALUES ('STRING','IDENTIFIER','ISSN'), ('STRING','IDENTIFIER','ISBN'),('STRING','IDENTIFIER','DOI');

-- the following populates resource_annotation but is not represented in the code -- uncomment for testing only
-- insert into resource_annotation (value,resourceannotationkey_id,resource_id) select isbn,(select id from resource_annotation_key where "key"='ISBN' ) ,id from document where isbn is not null and isbn != '';
-- insert into resource_annotation (value,resourceannotationkey_id,resource_id) select issn,(select id from resource_annotation_key where "key"='ISSN' ) ,id from document where issn is not null and issn != '';
-- insert into resource_annotation (value,resourceannotationkey_id,resource_id) select doi,(select id from resource_annotation_key where "key"='DOI' ) ,id from document where doi is not null and doi != '';

ALTER TABLE geographic_keyword ADD COLUMN "level" character varying(50);

CREATE INDEX calendar_date_res_id_id
  ON calendar_date
  USING btree
  (resource_id, id);

CREATE INDEX coding_catvar_id
  ON coding_sheet
  USING btree
  (category_variable_id);
  
CREATE INDEX ontology_catvar_id
  ON ontology
  USING btree
  (category_variable_id);

CREATE INDEX resource_id_keyid
  ON resource_annotation
  USING btree
  (resource_id, id, resourceannotationkey_id);

CREATE INDEX rescreator_resid
  ON resource_creator
  USING btree
  (resource_id);

  
CREATE INDEX resId_noteId
  ON resource_note
  USING btree
  (resource_id, id);



CREATE INDEX resId_temporalKwdId
  ON resource_temporal_keyword
  USING btree
  (resource_id, temporal_keyword_id);


CREATE INDEX resId_siteTypeKwdId
  ON resource_site_type_keyword
  USING btree
  (resource_id, site_type_keyword_id);


CREATE INDEX resId_siteNameKwdId
  ON resource_site_name_keyword
  USING btree
  (resource_id, site_name_keyword_id);


CREATE INDEX resId_otherKwdId
  ON resource_other_keyword
  USING btree
  (resource_id, other_keyword_id);


CREATE INDEX resId_matKwdId
  ON resource_material_keyword
  USING btree
  (resource_id, material_keyword_id);


CREATE INDEX resId_invTypeId
  ON resource_investigation_type
  USING btree
  (resource_id, investigation_type_id);


CREATE INDEX resId_geogKwdId
  ON resource_geographic_keyword
  USING btree
  (resource_id, geographic_keyword_id);


CREATE INDEX resId_cultKwdId
  ON resource_culture_keyword
  USING btree
  (resource_id, culture_keyword_id);


CREATE INDEX infoRes_provId
  ON information_resource
  USING btree
  (provider_institution_id);

  CREATE INDEX infoRes_ProjId
  ON information_resource
  USING btree
  (project_id, id);

CREATE INDEX person_InstId
  ON person
  USING btree
  (id, institution_id);

  CREATE INDEX res_submitterId
  ON resource
  USING btree
  (submitter_id);

  CREATE INDEX res_updaterId
  ON resource
  USING btree
  (updater_id);

  
CREATE INDEX cltKwd_appr
  ON culture_keyword
  USING btree
  (approved, id);

CREATE INDEX siteType_appr
  ON site_type_keyword
  USING btree
  (approved, id);

alter table document alter column start_page type varchar(10);
alter table document alter column end_page type varchar(10);


update investigation_type set label='Methodology, Theory, or Synthesis' where label='Methodological, Synthetic or Theoretical Research';
INSERT INTO investigation_type (id, definition, label) VALUES (13, 'These are studies that examine aspects of the present or past natural environment to provide a context, often off-site,  for interpreting archaeological resources.  Sometimes reported in stand-alone volumes representing significant research, such investigations may include geomorphological, paleontological, or palynological work.','Environment Research');
INSERT INTO investigation_type (id, definition, label) VALUES (14, 'This is research that focuses on the systematic description and analysis of cultural systems or lifeways.  These studies of contemporary people and cultures rely heavily on participant observation as well as interviews, oral histories, and review of relevant documents.  Ethnoarchaeological studies are a subset of this kind of research that investigate correlations between traditional contemporary cultures and patterns in the archaeological record.    ','Ethnographic Research');
INSERT INTO investigation_type (id, definition, label) VALUES (15, 'These are studies of biological aspects of human individuals, groups, or cultural systems, including a broad range of topics describing and analyzing human physiology, osteology, diet, disease, and origins research. ','Bioarchaeological Research');
INSERT INTO investigation_type (id, definition, label) VALUES (16, 'Descriptive and analytical research that documents, describes, and/or interprets detailed data on historic structures in an area, including images and floor plans. ','Architectural Documentation');
INSERT INTO investigation_type (id, definition, label) VALUES (17, 'This kind of research includes systematic description and analysis of changes in cultural systems through time, using historical documents and oral and traditional histories.  Ethnohistoric studies typically deal with time periods of initial or early contact between different cultural systems, for example, European explorers or early colonists and indigenous cultures. ','Ethnohistoric Research');
INSERT INTO investigation_type (id, definition, label) VALUES (18, 'These are investigations of the past using written records and other documents.  Evidence from the records are compared, judged for veracity, placed in chronological or topical sequence, and interpreted in light of preceding, contemporary, and subsequent events.','Historic Background Research');
INSERT INTO investigation_type (id, definition, label) VALUES (19, 'Visits to a site to record archaeological resources and their conditions and recover finds that may have come to light since a previous visit.  Also refers to regular or systematic monitoring and recording of the condition of a site, checking for signs of vandalism, other human intervention, and natural processes that may have damaged the resource.','Site Stewardship Monitoring');
INSERT INTO investigation_type (id, definition, label) VALUES (20, 'Observations and investigations conducted during any ground disturbing operation carried out for non-archaeological reasons (e.g. construction, land-leveling, wild land fire-fighting, etc.) that may reveal and/or damage archaeological deposits.','Ground Disturbance Monitoring');

update investigation_type set definition='This is an investigation employed to systematically gather data on the general presence or absence of archaeological resources, to define resource types, or to estimate the distribution of resources in an area. These studies may provide a general understanding of the resources in an area. This includes the description, analysis, and specialized studies of artifacts and samples recovered during survey. ' where label= 'Systematic Survey';
update investigation_type set definition='These investigations include fieldwork undertaken to identify the archaeological resources in a given area and to collect information sufficient to evaluate the resource(s) and develop treatment recommendations. Such investigations typically determine the number, location, distribution and condition of archaeological resources. This includes the description, analysis, and specialized studies of artifacts and samples recovered during site testing. ' where label='Site Evaluation / Testing';
update investigation_type set definition='An activity involving the review of records, files, and other information about the sites recorded in a particular area.  Typically, such studies involve checking the site files and other archives in an agency''s database, or SHPO''s office, or State Archaeologist''s office.' where label='Records Search / Inventory Checking';
update investigation_type set definition='These investigations include substantial field investigation of an archaeological site (or sites) involving the removal, and systematic recording of, archaeological matrix. These activities often mitigate the adverse effects of a public undertaking. This includes the description, analysis, and specialized studies of artifacts and samples recovered during excavations.' where label='Data Recovery / Excavation';
update investigation_type set definition='These investigations include field and/or document and records reviews to gather data on the presence and type of historic structures and provide a general understanding of architectural and cultural resources in an area. ' where label='Architectural Survey';

-- assigns null to the project_id for all information resources assigned to an independent resources project --
update information_resource set project_id=NULL where id in (select ir.id from information_resource ir where ir.project_id in (select p.id from project p where p.id in (select id from resource r where r.resource_type='INDEPENDENT_RESOURCES_PROJECT')));

-- skip manually edited independent resource projects 
delete from project where id not in (4572, 2505, 4262, 3006, 2993) and id in (select r.id from resource r where r.resource_type='INDEPENDENT_RESOURCES_PROJECT');

-- change previously aforementioned said projects to a regular project type 
update resource set resource_type='PROJECT' where id in (4572, 2505, 4262, 3006, 2993);

delete from full_user where id in (select f.id from full_user f inner join resource r on f.resource_id=r.id where r.resource_type='INDEPENDENT_RESOURCES_PROJECT');
delete from resource where resource_type='INDEPENDENT_RESOURCES_PROJECT';


ALTER TABLE institution ADD COLUMN parentinstitution_id bigint;

CREATE TABLE instiution_synonym
(
  id bigserial NOT NULL,
  "alternateName" character varying(255),
  CONSTRAINT pk_inst_syn_id PRIMARY KEY (id)
)
WITH (OIDS=FALSE);

ALTER TABLE instiution_synonym RENAME TO institution_synonym;

-- 2011-01-12
ALTER TABLE site_type_keyword ADD COLUMN "parent_id" bigint;
ALTER TABLE culture_keyword ADD COLUMN "parent_id" bigint;

update site_type_keyword k2 set parent_id=parent.id from site_type_keyword as k1, site_type_keyword as parent where k2.id=k1.id and k1.index like
'%.%' and (parent.index=regexp_replace(k1.index,E'\..$','') or parent.index=regexp_replace(k1.index,E'\...$',''));

update culture_keyword k2 set parent_id=parent.id from culture_keyword as k1, culture_keyword as parent where k2.id=k1.id and k1.index like '%.%' 
	and parent.index=regexp_replace(k1.index,E'\..$','');

ALTER TABLE ontology_node ADD COLUMN "description" character varying(2048);
ALTER TABLE ontology_node ADD COLUMN "display_name" character varying(255);
ALTER TABLE ontology_node ADD COLUMN "import_order" bigint;

alter sequence datatablecolumn_sequence rename to data_table_column_id_seq;
alter sequence datatable_sequence rename to data_table_id_seq;


-- 2011-01-20

-- Table: resource_managed_geographic_keyword

-- DROP TABLE resource_managed_geographic_keyword;

CREATE TABLE resource_managed_geographic_keyword
(
  resource_id bigint NOT NULL,
  geographic_keyword_id bigint NOT NULL,
  CONSTRAINT resource_managed_geographic_keyword_pkey PRIMARY KEY (resource_id, geographic_keyword_id),
  CONSTRAINT resource_managed_geographic_keyword_geographic_keyword_id_fkey FOREIGN KEY (geographic_keyword_id)
      REFERENCES geographic_keyword (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT resource_managed_geographic_keyword_resource_id_fkey FOREIGN KEY (resource_id)
      REFERENCES resource (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
ALTER TABLE resource_managed_geographic_keyword OWNER TO tdar;

-- Index: geogr_res

-- DROP INDEX geogr_res;

CREATE INDEX mgd_geogr_res
  ON resource_managed_geographic_keyword
  USING btree
  (resource_id, geographic_keyword_id);

CREATE TABLE stats
(
  id bigserial NOT NULL,
  recorded_date date,
  "value" bigint,
  "comment" character varying(2048),
  stat_type character varying(255),
  CONSTRAINT pk_stats PRIMARY KEY (id)
)
WITH (OIDS=FALSE);
ALTER TABLE stats OWNER TO tdar;

-- update display_name on ontology_node
create table ontology_node_synonym (ontologynode_id int8 not null, synonyms varchar(255));
alter table ontology_node_synonym add constraint FK99DDA7AED6698FA8 foreign key (ontologynode_id) references ontology_node;

update ontology_node set display_name=replace(label, '_', ' ');
insert into ontology_node_synonym (synonyms, ontologynode_id) select trim(regexp_split_to_table(display_name,E'(( or )|([\(]))')),ontology_node.id from ontology_node;
update ontology_node_synonym set synonyms = replace(synonyms,')','');

DROP TABLE IF EXISTS institution_synonym;

create table institution_synonym (institution_id int8 not null, synonyms varchar(255));
alter table institution_synonym add constraint FK99DDA7AED6698FA9 foreign key (institution_id) references institution;

CREATE TABLE upgradetask
(
  id bigserial NOT NULL,
  "comment" character varying(255),
  "name" character varying(255),
  recorded_date timestamp without time zone,
  run boolean,
  CONSTRAINT upgradetask_pkey PRIMARY KEY (id)
)
WITH (OIDS=FALSE);
ALTER TABLE upgradetask OWNER TO tdar;

alter table information_resource_file drop column shared;
ALTER TABLE upgradetask RENAME TO upgrade_task;

update data_table_column set column_data_type='VARCHAR' where column_data_type like 'VARCHAR%' or column_data_type like '%text%';
update data_table_column set column_data_type='DOUBLE' where column_data_type like '%double%' or column_data_type like '%float%';
update data_table_column set column_data_type='BOOLEAN' where column_data_type like '%bool%' or column_data_type like 'bool%';
update data_table_column set column_data_type='BIGINT' where column_data_type like '%int%' or column_data_type like 'int%';

update ontology_node set import_order =0 where import_order is null;
alter table ontology_node rename "label" TO "iri";
alter table data_table_column drop "title";
alter table creator drop "creator_type";

-- changed how hibernate handled this, so, renaming column to hibernate generated version
alter table category_variable_synonyms rename category_variable_id to categoryvariable_id;
alter table category_variable_synonyms rename element to synonyms;
alter table person drop column date_created;
alter table resource drop column active;

-- cleaning up bad creators
delete from resource_creator where creator_id not in (select distinct id from person union select distinct id from institution);
delete from creator where id not in (select distinct id from person union select distinct id from institution);


-- add display name to data_table_column
alter table data_table_column add column display_name character varying(255);
update data_table_column set display_name = name;
alter table data_table drop column title;



--renaming unused roles
--update resource_creator set role = 'AUTHOR' where role = 'PRIMARY_AUTHOR';
--update resource_creator set role = 'AUTHOR' where role = 'SECONDARY_AUTHOR';
--update resource_creator set role = 'EDITOR' where role = 'PRIMARY_EDITOR';
--update resource_creator set role = 'EDITOR' where role = 'SECONDARY_EDITOR';

--Map deprecated roles
--first, spit out a list of all the resource creator entries that are being deleted 
-- \f ','
-- \a
-- \t
-- \o deleted_items.csv
select 
    r.id,
    r.resource_type,
    '"' || r.title || '"' title,
    rc.id,
    p.first_name,
    p.last_name, 
    i.name,
    rc.role,
    ('http://core.tdar.org/' || lower(r.resource_type) || '/' || r.id) url
from 
    resource_creator rc 
        left join person p on (rc.creator_id = p.id) 
        left join institution i on (rc.creator_id = i.id)
        join resource r on (rc.resource_id = r.id)
where
    lower(rc.role) in ('series_editor', 'publisher', 'other')
;

--now, spit out a list of all the entries that are being modified
-- \o deleted_items.csv
select 
    r.id,
    r.resource_type,
    '"' || r.title || '"' title,
    rc.id,
    p.first_name,
    p.last_name, 
    i.name,
    rc.role old_role,
    case 
        when rc.role = 'ANALYST' THEN 'CREATOR'
        WHEN RC.ROLE = 'LEAD' THEN 'PREPARER'
        WHEN RC.ROLE = 'PRIMARY_AUTHOR' THEN 'AUTHOR'
        WHEN RC.ROLE = 'SECONDARY_AUTHOR' THEN 'AUTHOR'
        WHEN RC.ROLE = 'PRIMARY_EDITOR' THEN 'EDITOR'
        WHEN RC.ROLE = 'SECONDARY_EDITOR' THEN 'EDITOR'
        WHEN RC.ROLE = 'CREATOR_IMAGE' THEN 'CREATOR'
    end new_role,
    ('http://core.tdar.org/' || lower(r.resource_type) || '/' || r.id) url
from 
    resource_creator rc 
        left join person p on (rc.creator_id = p.id) 
        left join institution i on (rc.creator_id = i.id)
        join resource r on (rc.resource_id = r.id)
where
    lower(rc.role) in ('analyst', 'lead', 'primary_author', 'secondary_author', 'primary_editor', 'secondary_editor')
;

 --delete the deprecated roles
delete from resource_creator where lower(role) in ('series_editor', 'publisher', 'other');
 --rename the mapped roles
update resource_creator set role = 'CREATOR' where role = 'ANALYST';
update resource_creator set role = 'CREATOR' where role = 'CREATOR_IMAGE';
update resource_creator set role = 'PREPARER' where role = 'LEAD';
update resource_creator set role = 'AUTHOR', sequence_number = 1 where role = 'PRIMARY_AUTHOR';
update resource_creator set role = 'AUTHOR', sequence_number = 2 where role = 'SECONDARY_AUTHOR';
update resource_creator set role = 'EDITOR', sequence_number = 3 where role = 'PRIMARY_EDITOR';
update resource_creator set role = 'EDITOR', sequence_number = 4 where role = 'SECONDARY_EDITOR';
update resource_creator set sequence_number = 5 where role = 'TRANSLATOR';



create index other_keyword_label_lc on other_keyword (lower(label));
create index geographic_keyword_label_lc on geographic_keyword (lower(label));
create index site_type_keyword_label_lc on site_type_keyword (lower(label));
create index site_name_keyword_label_lc on site_name_keyword (lower(label));
create index culture_keyword_label_lc on culture_keyword (lower(label));
create index temporal_label_lc on temporal_keyword (lower(label));
create index institution_name_lc on institution (lower(name),id);
create index resource_latlong on latitude_longitude (resource_id , id);
create index person_lc on person (lower(first_name),lower(last_name),id);
create index geog_label_level_id on geographic_keyword (level,label,id);