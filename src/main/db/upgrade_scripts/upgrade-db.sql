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


alter table culture_keyword add column occurrance bigint;
alter table geographic_keyword add column occurrance bigint;
alter table investigation_type add column occurrance bigint;
alter table material_keyword add column occurrance bigint;
alter table other_keyword add column occurrance bigint;
alter table temporal_keyword add column occurrance bigint;
alter table site_name_keyword add column occurrance bigint;
alter table site_type_keyword add column occurrance bigint;

alter table information_resource_file add column description text;
alter table information_resource_file add column file_created_date date;

--2013-05-22
alter table sensory_data add column rgb_capture character varying(255);