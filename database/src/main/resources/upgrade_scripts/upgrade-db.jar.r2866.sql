-- 2013-03-17
create table geospatial (
    id int8 not null,
    primary key (id)
);

alter table geospatial add constraint FK5B47609397FF4D65 foreign key (id) references dataset;

    
alter table information_resource_file add column part_of_composite boolean default false;
alter table information_resource_file_version add column primary_file boolean default false;

alter table sensory_data add column scanner_technology character varying(50);

-- 2013-05-08
alter table creator alter column url type character varying(255);
alter table creator drop column location;
alter table institution drop column location;
alter table institution drop column url;
alter table institution drop column status; -- should only be in creator


alter table culture_keyword add column occurrence bigint default 0;
alter table geographic_keyword add column occurrence  bigint default 0;
alter table investigation_type add column occurrence  bigint default 0;
alter table material_keyword add column occurrence  bigint default 0;
alter table other_keyword add column occurrence  bigint default 0;
alter table temporal_keyword add column occurrence  bigint default 0;
alter table site_name_keyword add column occurrence  bigint default 0;
alter table site_type_keyword add column occurrence  bigint default 0;
alter table creator add column occurrence  bigint default 0;

alter table information_resource_file add column description text;
alter table information_resource_file add column file_created_date date;

--2013-05-22
alter table sensory_data add column rgb_capture character varying(255);
alter table sensory_data add column camera_details character varying(255);


-- 2013-05-24

create table pos_coupon (
    id  bigserial not null,
    code varchar(255) unique,
    date_created timestamp,
    date_expires timestamp,
    number_of_files int8,
    number_of_mb int8,
    one_time_use boolean,
    account_id int8 references pos_account,
    primary key (id)
);


alter table pos_invoice add column coupon_id int8 references pos_coupon;

--2013-05-25
alter table pos_coupon add column user_id int8 references person;
alter table pos_coupon add column date_redeemed timestamp;

--2013-05-29
alter table person add column proxyinstitution_id int8 references institution;
alter table person add column proxy_note text;

--2013-06-13
create table archive (
    id bigint not null,
    constraint archive_pkey primary key (id ),
    constraint archive_fkey foreign key (id)
    references information_resource (id) match SIMPLE
) with (
  OIDS=FALSE
);

alter table archive owner to tdar;

-- 2013-06-25
create table resource_relationship (
    id  bigserial not null,
    relationship_type varchar(255),
    sourceResource_id int8 references resource,
    targetResource_id int8 references resource,
    primary key (id)
);

alter table geospatial add column currentnessUpdateNotes text;
alter table geospatial add column projection varchar(50);
alter table geospatial add column scale varchar(100);
alter table geospatial add column spatial_reference_system varchar(50);

create index ires_provider on information_resource (provider_institution_id);
create index ires_publisher on information_resource (publisher_id);
create index ires_copyright on information_resource (copyright_holder_id);
create index creatorid on resource_creator (creator_id);

-- remove blank creator 2013-07-10
begin transaction;
update information_resource set provider_institution_id=null where provider_institution_id=11015;
update information_resource set publisher_id=null where publisher_id=11015;
update person set institution_id = null where institution_id=11015;
delete from resource_creator where creator_id=11015;
delete from institution where id=11015;
delete from creator where id=11015;
commit;
-- cleanup date normalized issue with API Controller 2013-07-11
update information_resource set date_created_normalized =round(date_created_normalized/10) * 10 where right(cast ( date_created_normalized as varchar),1) != '0';

-- 2013-07-13 secondary sort option
alter table project add secondary_sort_order varchar(25);
alter table collection add secondary_sort_order varchar(25);