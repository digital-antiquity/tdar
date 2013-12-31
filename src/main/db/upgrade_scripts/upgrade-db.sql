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

-- jdevos 9/11/2013
alter table person rename column creator_agreement_version  to contributor_agreement_version;

-- jdevos 10/13/2013: adding some indexes per TDAR-3403.
create index authorized_user_resource_collection_id_idx on authorized_user(resource_collection_id);
create index authorized_user_user_id_idx on authorized_user(user_id);
create index bookmarked_resource_person_id_idx on bookmarked_resource(person_id);
create index bookmarked_resource_resource_id_idx on bookmarked_resource(resource_id);
create index coding_rule_coding_sheet_id_idx on coding_rule(coding_sheet_id);
create index coding_rule_ontology_node_id_idx on coding_rule(ontology_node_id);
create index coding_sheet_default_ontology_id_idx on coding_sheet(default_ontology_id);
create index collection_owner_id_idx on collection(owner_id);
create index collection_parent_id_idx on collection(parent_id);
create index data_table_column_data_table_id_idx on data_table_column(data_table_id);
create index data_table_column_default_coding_sheet_id_idx on data_table_column(default_coding_sheet_id);
create index data_table_column_default_ontology_id_idx on data_table_column(default_ontology_id);
create index related_comparative_collection_resource_id_idx on related_comparative_collection(resource_id);
create index source_collection_resource_id_idx on source_collection(resource_id);

-- abrin 12/4/2013 -- adding foreign keys that should have been there
alter table sensory_data_image
    add constraint FK_l4o8gyxxc17q6w3g8ew9ivhlh
    foreign key (sensory_data_id)
    references sensory_data;

alter table sensory_data_scan
    add constraint FK_bbetp1cmjicvtydwd0hfepab1
    foreign key (sensory_data_id)
    references sensory_data;

alter table person add column orcid_id varchar(50) default null;