create table personal_filestore_ticket (id  bigserial not null, date_generated timestamp not null, personal_file_type varchar(255) not null, submitter_id bigint not null, primary key (id));
alter table personal_filestore_ticket add constraint FK7F736747FC7475F foreign key (submitter_id) references person;

update data_table_column set column_encoding_type='TEXT' where column_encoding_type in ('OTHER', 'CODED_TEXT', 'CODED_NUMERIC');

create table sensory_data (id bigint not null, primary key (id));
alter table sensory_data add constraint FK32612BEA51D71F47 foreign key (id) references information_resource;

create table data_table_relationship (id  bigserial not null, relationship_type varchar(255), dataset_id int8, foreignTable_id int8, localTable_id int8, primary key (id));
create table data_table_relationship_data_table_column (data_table_relationship_id int8 not null, localColumns_id int8 not null, foreignColumns_id int8 not null, primary key (data_table_relationship_id, foreignColumns_id));

alter table data_table_relationship add constraint FK344B15BEE6F89BEC foreign key (foreignTable_id) references data_table;
alter table data_table_relationship add constraint FK344B15BE19E50A8C foreign key (dataset_id) references dataset;
alter table data_table_relationship add constraint FK344B15BE80A27383 foreign key (localTable_id) references data_table;
alter table data_table_relationship_data_table_column add constraint FK76845B1BB96ECAF3 foreign key (foreignColumns_id) references data_table_column;
alter table data_table_relationship_data_table_column add constraint FK76845B1B9A8E1780 foreign key (data_table_relationship_id) references data_table_relationship;
alter table data_table_relationship_data_table_column add constraint FK76845B1B900118CA foreign key (localColumns_id) references data_table_column;

alter table personal_filestore_ticket add description character varying(500);

/* 
 --some category flattening statements (if we decide to flatten them this way)
update category_variable set parent_id = null where type = 'CATEGORY';
ALTER TABLE category_variable ADD COLUMN old_parent_id bigint;
update category_variable set old_parent_id = parent_id;

create table tmp_3rdlevelcats as (
select 
    t3.id l3id
    ,t3.label l3label
    ,t3.name l3name
    ,t2.id l2id
    ,t2.label l2label
    ,t2.name l2name
    ,t1.id l1id
    ,t1.label l1label
    ,t1.name l1name
    ,(t2.label || '_' || t3.label) new_label
    ,(t2.name || ' - ' || t3.label) new_name
    from 
        category_variable t3
            join category_variable t2 on (t3.parent_id = t2.id)
                join category_variable t1 on (t2.parent_id = t1.id)
);

--rename the 3rd level categories and flatten them to 2nd level categories
update category_variable cv set
     parent_id = (select l1id from tmp_3rdlevelcats   where cv.id = l3id)
     ,label = (select new_label from tmp_3rdlevelcats where cv.id = l3id)
     ,name =  (select new_name from tmp_3rdlevelcats  where cv.id = l3id)
where exists
    (select * from tmp_3rdlevelcats  where cv.id = l3id)
;

--clean up the temp table
--drop table tmp_3rdlevelcats;

*/
