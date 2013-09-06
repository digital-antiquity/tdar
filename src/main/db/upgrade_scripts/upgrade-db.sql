-- jdevos 8/20/2013
-- upgrade script for knap
-- add sensory data records to the dataset join-table
insert into dataset(id) select id from sensory_data sd where not exists (select * from dataset ds where ds.id = sd.id);

-- abrin 8/22/2013
alter table geospatial drop column projection;
alter table geospatial add column map_source varchar(500);

-- jdevos 9/5/2013
alter table person add column tos_level integer not null default 0;
alter table person add column creator_agreement_level integer not null default 0;

alter table person rename column tos_level  to tos_version;
alter table person rename column creator_agreement_level  to creator_agreement_version;
