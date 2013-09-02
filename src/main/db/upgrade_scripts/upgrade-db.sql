-- jdevos 8/20/2013
-- upgrade script for knap
-- add sensory data records to the dataset join-table
insert into dataset(id) select id from sensory_data sd where not exists (select * from dataset ds where ds.id = sd.id);

-- abrin 8/22/2013
alter table geospatial drop column projection;
alter table geospatial add column map_source varchar(500);

-- mpaulo 9/02/2013 TDAR-3006
alter table archive add column doimportcontent boolean default false;
alter table archive add column importdone boolean default false;