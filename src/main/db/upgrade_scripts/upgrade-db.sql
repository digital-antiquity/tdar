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