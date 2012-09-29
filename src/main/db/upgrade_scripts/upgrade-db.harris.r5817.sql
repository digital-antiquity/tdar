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

ALTER TABLE information_resource ADD COLUMN license_type CHARACTER VARYING(128);
ALTER TABLE information_resource ADD COLUMN license_text TEXT;


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
update resource set description='no description provided' where description is null or description ='';
update resource set date_updated=date_registered where date_updated is null;
update resource set updater_id=submitter_id where updater_id is null;

-- NOTE ThESE TWO MAY ERROR OUT, THAT'S OKAY
alter table resource add column access_counter bigint default 0;
alter table information_resource_file add column download_count null default 0;

-- 5/4/2012 correct latlong records that violate simplified rules (minlat < maxlat, minlong < maxlong)
insert into  resource_revision_log ( timestamp, resource_id, log_message)
    select
        now(),
        resource_id,
        'flipping longitude because minlong:' || minimum_longitude || ' greater than maxlong:' || maximum_longitude
    from
        latitude_longitude
    where
        minimum_longitude > maximum_longitude;
        
update  latitude_longitude set minimum_longitude = maximum_longitude,  maximum_longitude = minimum_longitude where minimum_longitude > maximum_longitude;
   
insert into  resource_revision_log ( timestamp, resource_id, log_message)
    select
        now(),
        resource_id,
        'flipping latitude because minlat:' || minimum_latitude || ' greater than maxlat:' || maximum_latitude
    from
        latitude_longitude
    where
        minimum_latitude > maximum_latitude;
        
update  latitude_longitude set minimum_latitude = maximum_latitude,  maximum_latitude = minimum_latitude where minimum_latitude > maximum_latitude;

-- 5/7/2012
ALTER TABLE information_resource ADD COLUMN inheriting_identifier_information boolean default false;
ALTER TABLE information_resource ADD COLUMN inheriting_note_information boolean default false;
ALTER TABLE information_resource ADD COLUMN inheriting_collection_information boolean default false;

-- backported from grid
update data_table_column set mappingcolumn=false where mappingcolumn is null;
update data_table_column set visible=true where visible is null;
update data_table_column set ignorefileextension=true where ignorefileextension is null;


CREATE TABLE resource_access_statistics
(
  id bigserial NOT NULL,
  date_accessed timestamp,
  resource_id bigint
);

CREATE TABLE information_resource_file_download_statistics
(
  id bigserial NOT NULL,
  date_accessed timestamp,
  information_resource_file_id bigint
);


truncate table information_resource_file_download_statistics;
truncate table resource_access_statistics;

CREATE LANGUAGE plpgsql;

DROP FUNCTION if exists tdar_refresh_access_counts() ;
CREATE FUNCTION tdar_refresh_access_counts() RETURNS VOID AS $$
DECLARE record RESOURCE;
begin

for record in select id, access_counter from resource where access_counter  > 0 loop
	for i in 0 .. record.access_counter LOOP
		insert into resource_access_statistics(resource_id, date_accessed) VALUES (record.id,now());
	end loop;
end loop;

END;
$$ LANGUAGE plpgsql;


DROP FUNCTION if exists tdar_refresh_download_counts() ;
CREATE FUNCTION tdar_refresh_download_counts() RETURNS VOID AS $$
DECLARE record INFORMATION_RESOURCE_FILE;
begin

for record in select id, download_count from information_resource_file where download_count > 0 loop
	for i in 0 .. record.download_count LOOP
		insert into information_resource_file_download_statistics(information_resource_file_id, date_accessed) VALUES (record.id,now());
	end loop;
end loop;

END;
$$ LANGUAGE plpgsql;

select tdar_refresh_access_counts();
select tdar_refresh_download_counts();
DROP FUNCTION if exists tdar_refresh_download_counts() ;
DROP FUNCTION if exists tdar_refresh_access_counts() ;

--05-22-2012
alter table document add column degree varchar(50);

-- 2012-05-30 
-- making sensory data 'description' fields TEXT to avoid maxlength issues
alter table sensory_data alter column rgb_data_capture_info type TEXT;
alter table sensory_data alter column final_dataset_description type TEXT;
alter table sensory_data_scan alter column scan_notes type TEXT;
alter table sensory_data alter column point_deletion_summary type TEXT;
alter table sensory_data alter column mesh_processing_notes type TEXT;

alter table information_resource_file add column number_of_parts bigint;

alter table project add column sort_order varchar(50) default 'RESOURCE_TYPE';

-- 2012-05-31
-- adding applied resource integration templates and integration templates
drop table if exists applied_resource_integration_template cascade;
/*
create table applied_resource_integration_template (
        id  bigserial not null,
        date_created timestamp,
        coding_sheet_id int8 references coding_sheet,
        creator_id int8 not null references person,
        template_id int8 not null references integration_template,
        primary key (id)
);
*/
drop table if exists integration_template cascade;
/*
create table integration_template (
        id  bigserial not null,
        date_created timestamp,
        name varchar(255),
        creator_id int8 references person,
        ontology_id int8 references ontology,
        original_source_template_id int8 references integration_template,
        primary key (id)
);
*/
drop table if exists integration_node_mapping cascade;
/*
create table integration_node_mapping (
        id  bigserial not null,
        key character varying,
        ontology_node_id int8 not null references ontology_node,
        template_id int8 not null references integration_template,
        primary key (id)
);
*/
alter table data_table_column drop column default_integration_template_id; 
alter table coding_sheet drop column default_integration_template_id; 
alter table coding_sheet add column default_ontology_id int8 references ontology;

-- adding ontology node refs to coding rules
alter table coding_rule add column ontology_node_id int8 references ontology_node;

create table video (
    audio_channels varchar(255),
    audio_codec varchar(255),
    audio_kbps int4,
    fps int4,
    height int4,
    kbps int4,
    sample_frequency int4,
    video_codec varchar(255),
    width int4,
    id int8 not null,
    primary key (id)
);

alter table information_resource_file_version add column total_time bigint;
alter table video add constraint FK6B0147B51D71F47 foreign key (id) references information_resource;

-- adding generated column to coding sheets for identity coding sheets
alter table coding_sheet add generated bool default false;
update coding_sheet set generated=false;
alter table resource drop access_counter;
alter table person add username varchar(255) UNIQUE;
update person set username=email where registered=true;
alter table person drop password;


create table explore_cache_decade (
    id bigserial primary key,
    key int4,
    item_count bigint
  );

ALTER TABLE explore_cache_decade rename column resource_count to item_count;

create table homepage_featured_item_cache (
    id bigserial primary key,
    resource_id int8 references information_resource
    );
    
truncate table explore_cache_decade;
INSERT into explore_cache_decade (key, item_count) select date_created_normalized, count(res.id) from information_resource ir, resource res where ir.id=res.id and res.status='ACTIVE' and ir.date_created_normalized between 1 and 2900 group by date_created_normalized;


TRUNCATE table homepage_featured_item_cache;
INSERT into homepage_featured_item_cache VALUES (1,366237);