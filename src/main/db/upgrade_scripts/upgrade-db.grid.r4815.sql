-- THIS IS THE UPGRADE-DB-FILE FOR TDAR --
-- IT WILL UPGRADE YOU TO GRID          --
--
-- NOTE: please date your changes at the beginning with a comment

--10/19/2011  adding indexes
CREATE INDEX authorized_user_cid ON authorized_user (id, resource_collection_id);
CREATE INDEX authorized_user_perm ON authorized_user (general_permission_int, user_id, resource_collection_id);


ALTER TABLE resource ADD COLUMN record_hash character varying(255);

--10/25/2011 -- turns out we didn't need this
ALTER TABLE resource DROP COLUMN record_hash;
ALTER TABLE resource ADD COLUMN external_id character varying(255);

--11/15/2011 -- 1st pass for mimbres data mapping work
ALTER TABLE information_resource ADD COLUMN mappedDataKeyColumn_id BIGINT;
ALTER TABLE information_resource ADD COLUMN mappedDataKeyValue CHARACTER VARYING(255);

--11/21/2011 -- indexes needed for speedy keyword reports
create index rck_culture_keyword_id on resource_culture_keyword (culture_keyword_id);
create index rgk_geographic_keyword_id on resource_geographic_keyword (geographic_keyword_id);
create index rit_investigation_type_id on resource_investigation_type(investigation_type_id);
create index rmk_material_keyword_id on resource_material_keyword(material_keyword_id);
create index rok_other_keyword_id on resource_other_keyword(other_keyword_id);
create index rsnk_site_name_keyword_id on resource_site_name_keyword(site_name_keyword_id);
create index rstk_site_type_keyword_id on resource_site_type_keyword(site_type_keyword_id);
create index rtk_temporal_keyword_id on resource_temporal_keyword(temporal_keyword_id);

--11/22/2011 -- adding columns for mapping data table columns
ALTER TABLE data_table_column ADD COLUMN delimiterValue CHARACTER VARYING(4);
ALTER TABLE data_table_column ADD COLUMN ignoreFileExtension BOOLEAN default true;

--11/28/2011 -- adding columns for user stats
alter table person add column total_login bigint default 0;
alter table person add column last_login timestamp;
alter table person add column penultimate_login timestamp;
--11/29/2011
ALTER TABLE data_table_column ADD COLUMN visible BOOLEAN default true;

-- 12/14/2011
create table creator_synonym (Creator_id int8 not null, synonyms varchar(255));
create table culture_keyword_synonym (CultureKeyword_id int8 not null, synonyms varchar(255));
create table geographic_keyword_synonym (GeographicKeyword_id int8 not null, synonyms varchar(255));
create table investigation_type_synonym (InvestigationType_id int8 not null, synonyms varchar(255));
create table material_keyword_synonym (MaterialKeyword_id int8 not null, synonyms varchar(255));
create table other_keyword_synonym (OtherKeyword_id int8 not null, synonyms varchar(255));
create table site_name_keyword_synonym (SiteNameKeyword_id int8 not null, synonyms varchar(255));
create table site_type_keyword_synonym (SiteTypeKeyword_id int8 not null, synonyms varchar(255));
create table temporal_keyword_synonym (TemporalKeyword_id int8 not null, synonyms varchar(255));

alter table creator_synonym add constraint FKE74A76E867FFC561 foreign key (Creator_id) references creator;
alter table culture_keyword_synonym add constraint FKE42E97145FF8F2D1 foreign key (CultureKeyword_id) references culture_keyword;
alter table geographic_keyword_synonym add constraint FK7E5A05DD644F9DE3 foreign key (GeographicKeyword_id) references geographic_keyword;
alter table investigation_type_synonym add constraint FK5511D21363E4A1E3 foreign key (InvestigationType_id) references investigation_type;
alter table material_keyword_synonym add constraint FK7175948DE769EA23 foreign key (MaterialKeyword_id) references material_keyword;
alter table other_keyword_synonym add constraint FK893DBC76521C07D1 foreign key (OtherKeyword_id) references other_keyword;
alter table site_name_keyword_synonym add constraint FK2DBBBFE9A52C1E3 foreign key (SiteNameKeyword_id) references site_name_keyword;
alter table site_type_keyword_synonym add constraint FK31359698E9EF2043 foreign key (SiteTypeKeyword_id) references site_type_keyword;
alter table temporal_keyword_synonym add constraint FKDF4940889BEBCC03 foreign key (TemporalKeyword_id) references temporal_keyword;
alter table coding_rule alter column description type varchar(2000);

-- 12/21/2011
ALTER TABLE resource_note add column sequence_number integer;
DROP TABLE full_user;
DROP TABLE read_user;

-- 1/6/2012 (edited 1/11/12)
update data_table_column set column_encoding_type='UNCODED_VALUE' where column_encoding_type='NUMERIC' or column_encoding_type='TEXT' or column_encoding_type='' or column_encoding_type is NULL;

-- 1/16/2012
ALTER TABLE data_table_column ADD COLUMN mappingColumn BOOLEAN default false;

-- 1/20/2012 -- tracking update dates of Person and Institution records (for OAI-PMH) 
-- ALTER TABLE creator ADD COLUMN date_updated timestamp without time zone;
-- UPDATE creator SET date_updated=date_created WHERE date_updated IS NULL;

UPDATE creator SET last_updated = date_created WHERE last_updated IS NULL;
UPDATE creator SET last_updated = current_timestamp WHERE last_updated IS NULL;

-- 1/28/2012
--fix enum typo
update stats set stat_type = 'NUM_ACTUAL_CONTRIBUTORS' where stat_type = 'NUM_ACTUAL_CONTIBUTORS';


--backfill contributors
create temporary table tmp_recorded_date as select distinct recorded_date from stats;
insert into stats(recorded_date, stat_type, "value", comment)  
select
    recorded_date,
    'NUM_ACTUAL_CONTRIBUTORS'as stat_type,
    (select count(*) from (select distinct submitter_id from resource r where r.date_registered <= tr.recorded_date ) as distinct_submitters) as "value",
    'backfilled using insert-derived-stats.sql on ' || now() as comment
from tmp_recorded_date tr
where
    --don't backfill stats we already have
    not exists (select * from stats where stat_type = 'NUM_ACTUAL_CONTRIBUTORS' and recorded_date = tr.recorded_date)
order by 1
;

-- 2012-02-08 
--add flags for email and phone number publication
ALTER TABLE person ADD COLUMN phone_public BOOLEAN default false not null;
ALTER TABLE person ADD COLUMN email_public BOOLEAN default false not null;

-- Table: data_table_column_relationship

-- DROP TABLE data_table_column_relationship;

-- 2012-02-29
-- refactored storage of relationships between pairs of columns within table joins
DROP TABLE data_table_relationship_data_table_column;
CREATE TABLE data_table_column_relationship
(
  id bigserial NOT NULL,
  relationship_id bigint,
  local_column_id bigint,
  foreign_column_id bigint,
  CONSTRAINT data_table_column_relationship_pkey PRIMARY KEY (id),
  CONSTRAINT fk_data_table_column_relationship_foreign_column FOREIGN KEY (foreign_column_id)
      REFERENCES data_table_column (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_data_table_column_relationship_local_column FOREIGN KEY (local_column_id)
      REFERENCES data_table_column (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_data_table_column_relationship_relationship FOREIGN KEY (relationship_id)
      REFERENCES data_table_relationship (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- 2012-03-06
-- add licenseType and licenseText to informationResource

ALTER TABLE information_resource ADD COLUMN license_type CHARACTER VARYING(128);
ALTER TABLE information_resource ADD COLUMN license_text TEXT;


-- 2012-03-09
-- a cache of the data for the homepage
create table homepage_cache_geographic_keyword (
    id bigserial primary key,
    label character varying(255) NOT NULL,
    level character varying(50) NOT NULL,
    resource_count bigint
  );

-- PRIMING Cache
truncate homepage_cache_geographic_keyword;
insert into homepage_cache_geographic_keyword (resource_count, label, level) select count(resource.id), label, level from resource, resource_managed_geographic_keyword, geographic_keyword where status='ACTIVE' and resource.id=resource_id and geographic_keyword_id=geographic_keyword.id group by label, level;

create table homepage_cache_resource_type (
    id bigserial primary key,
    resource_type character varying(100) NOT NULL,
    resource_count bigint
  );

-- PRIMING Cache
truncate homepage_cache_resource_type;
insert into homepage_cache_resource_type (resource_count, resource_type) select count(resource.id), resource_type from resource where status='ACTIVE' group by resource_type;


-- NOTE -- FROM HERE MAY BE NEEDED TO UPGRADE FROM GRID->HARRIS

-- 2012-03-15
-- add CopyrightHolder column to informationResource

ALTER TABLE information_resource ADD COLUMN copyright_holder_id BIGINT;

-- 2012-04-04
ALTER TABLE information_resource ADD COLUMN date_created_normalized INTEGER;

ALTER TABLE homepage_cache_resource_type add constraint homepage_cache_resource_type_unique unique(resource_type);

update data_table_column set ignorefileextension=TRUE where ignorefileextension is null;

-- 2012-04-17 -- fixes to deal with hibernate upgrade requirements -- more constraints
update information_resource set date_created=-1 where date_created is null;
update information_resource set date_created_normalized= round(date_created / 10) * 10; 
update resource set description='this should not be null' where description is null or description ='';
update resource set date_updated=date_registered where date_updated is null;
update resource set updater_id=submitter_id where updater_id is null;