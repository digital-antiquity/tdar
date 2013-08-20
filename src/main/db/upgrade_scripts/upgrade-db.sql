-- upgrade script for knap
-- add sensory data records to the dataset join-table
insert into dataset(id) select id from sensory_data sd where not exists (select * from dataset ds where ds.id = sd.id);
