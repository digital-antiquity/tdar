-- 3/8/12
----------- COMBINING CONFIDENTIAL AND EMBARGOED ----
ALTER TABLE information_resource_file ADD COLUMN restriction VARCHAR(50) DEFAULT 'PUBLIC';
ALTER TABLE information_resource_file ADD COLUMN date_made_public TIMESTAMP;
UPDATE information_resource_file SET restriction='EMBARGOED', date_made_public=information_resource.date_made_public FROM information_resource, information_resource_file irf2
	WHERE information_resource.id=irf2.information_resource_id AND information_resource.available_to_public IS FALSE AND irf2.id=information_resource_file.id;
UPDATE information_resource_file SET restriction='CONFIDENTIAL' WHERE confidential IS TRUE;

-- cleanup for later
/*
ALTER TABLE information_resource_file DROP COLUMN confidential;
ALTER TABLE information_resource DROP COLUMN date_made_public;
ALTER TABLE information_resource DROP COLUMN available_to_public;
*/

-- removing unused fields
ALTER TABLE person DROP COLUMN password;
ALTER TABLE person DROP COLUMN privileged;
ALTER TABLE person DROP COLUMN rpa;

DROP TABLE data_value_ontology_node_mapping;

ALTER TABLE information_resource ADD COLUMN publisher_id INT8 REFERENCES institution;
ALTER TABLE information_resource ADD COLUMN temp_publisher varchar(255);
ALTER TABLE information_resource ADD COLUMN publisher_location VARCHAR(255);


--- MOVING PUBLISHER AND PUBLISHER LOCATION TO INFORMATION_RESOURCE ---
UPDATE information_resource SET temp_publisher=trim(document.publisher), publisher_location=document.publisher_location FROM information_resource ir, 
	document WHERE ir.id=document.id AND ir.id=information_resource.id;


CREATE OR REPLACE FUNCTION add_publisher_institutions() RETURNS VOID AS $$
DECLARE publisher_ varchar(255);
begin
	for publisher_ in SELECT DISTINCT trim(publisher) FROM document WHERE trim(publisher) != '' AND trim(lower(publisher)) NOT IN (SELECT DISTINCT trim(lower(name)) FROM institution) LOOP
    	INSERT INTO creator (id, date_created) VALUES(nextval('creator_id_seq'), now());
	    INSERT INTO institution(id, name) VALUES(currval('creator_id_seq'), publisher_);
	    RAISE NOTICE 'adding publisher: %', publisher_;
	end loop;

END;
$$ LANGUAGE plpgsql;

SELECT add_publisher_institutions();


UPDATE information_resource SET publisher_id=institution.id FROM information_resource ir, 
	institution WHERE trim(lower(ir.temp_publisher))=trim(lower(institution.name)) AND ir.id=information_resource.id;

ALTER TABLE information_resource DROP COLUMN temp_publisher;
ALTER TABLE document DROP COLUMN publisher;
ALTER TABLE document DROP COLUMN publisher_location;

-- 04/09/12 -- likely superfluous
ALTER TABLE resource_note alter column "note" type varchar(5000);


-- 06/09/12 -- moving to status and deleted versions for dedupables
ALTER TABLE creator ADD COLUMN status varchar(25) default 'ACTIVE';
ALTER TABLE person ADD COLUMN merge_creator_id INT8 REFERENCES person;

ALTER TABLE institution ADD COLUMN status varchar(25) default 'ACTIVE';
ALTER TABLE institution ADD COLUMN merge_creator_id INT8 REFERENCES institution;

ALTER TABLE culture_keyword ADD COLUMN status varchar(25) default 'ACTIVE';
ALTER TABLE culture_keyword ADD COLUMN merge_keyword_id INT8 REFERENCES culture_keyword;

ALTER TABLE geographic_keyword ADD COLUMN status varchar(25) default 'ACTIVE';
ALTER TABLE geographic_keyword ADD COLUMN merge_keyword_id INT8 REFERENCES geographic_keyword;

ALTER TABLE investigation_type ADD COLUMN status varchar(25) default 'ACTIVE';
ALTER TABLE investigation_type ADD COLUMN merge_keyword_id INT8 REFERENCES investigation_type;

ALTER TABLE material_keyword ADD COLUMN status varchar(25) default 'ACTIVE';
ALTER TABLE material_keyword ADD COLUMN merge_keyword_id INT8 REFERENCES material_keyword;

ALTER TABLE other_keyword ADD COLUMN status varchar(25) default 'ACTIVE';
ALTER TABLE other_keyword ADD COLUMN merge_keyword_id INT8 REFERENCES other_keyword;

ALTER TABLE site_name_keyword ADD COLUMN status varchar(25) default 'ACTIVE';
ALTER TABLE site_name_keyword ADD COLUMN merge_keyword_id INT8 REFERENCES site_name_keyword;

ALTER TABLE site_type_keyword ADD COLUMN status varchar(25) default 'ACTIVE';
ALTER TABLE site_type_keyword ADD COLUMN merge_keyword_id INT8 REFERENCES site_type_keyword;

ALTER TABLE temporal_keyword ADD COLUMN status varchar(25) default 'ACTIVE';
ALTER TABLE temporal_keyword ADD COLUMN merge_keyword_id INT8 REFERENCES temporal_keyword;

-- 20/09/12 -- removing unneeded datatable columns
ALTER TABLE data_table DROP COLUMN aggregated;
ALTER TABLE data_table DROP COLUMN category_variable_id;

-- 25/09/12 -- removing unneeded datatable columns
ALTER TABLE resource ADD COLUMN uploader_id INT8 REFERENCES person;
update resource set uploader_id = submitter_id;

-- 25/09/12 
ALTER TABLE collection ADD COLUMN orientation VARCHAR(50) DEFAULT 'LIST';
ALTER TABLE collection ADD COLUMN date_updated timestamp without time zone DEFAULT now();
ALTER TABLE collection ADD COLUMN updater_id bigint REFERENCES person;
update collection set updater_id = owner_id;

--30/10/12
create table creator_address (
    id  bigserial not null primary key,
    city varchar(255),
    phone varchar(255),
    postal varchar(255),
    state varchar(255),
    street1 varchar(255),
    street2 varchar(255),
    type varchar(255),
    creator_id int8 not null references creator
);
    
-- 31/10/12
alter table collection add foreign key (owner_id) references person;


-- 07/11/12
create table pos_billing_activity (
    id  bigserial not null,
    currency varchar(255),
    enabled boolean,
    groupName varchar(255),
    name varchar(255),
    numberOfFiles int8,
    numberOfHours int4,
    numberOfMb int8,
    numberOfResources int8,
    displayNumberOfFiles int8,
    displayNumberOfMb int8,
    displayNumberOfResources int8,
    price float4,
    primary key (id)
);

create table pos_invoice (
    id  bigserial not null,
    date_created timestamp,
    total float4,
    transactionId varchar(255),
    transactionType varchar(50),
    transactionStatus varchar(25),
    address_id int8 references creator_address,
    owner_id int8 references person,
    executor_id int8 references person,
    invoiceNumber varchar(25),
    otherReason varchar(255),
    account_id int8,
    billingPhone int8,
    expirationYear int,
    expirationMonth int,
    creditCardType varchar(25),
    primary key (id)
);

create table pos_item (
    id  bigserial not null,
    quantity int4,
    activity_id int8 not null references pos_billing_activity,
    invoice_id int8 not null references pos_invoice,
    primary key (id)
);
insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price) values (true, 'level1', 5,1,50,5, 10), (true,'level2', 10, 1,250,10,20);
alter table creator_address add column country varchar(255);

create table pos_account_group (
    id int8 not null,
    date_created timestamp,
    description varchar(255),
    date_expires timestamp,
    date_updated timestamp,
    name varchar(255),
    status varchar(255),
    modifier_id int8 not null,
    owner_id int8 not null,
    account_group_id int8 not null,
    primary key (id)
);

create table pos_account (
    id  bigserial not null,
    date_created timestamp,
    description varchar(255),
    date_expires timestamp,
    date_updated timestamp,
    status varchar(25),
    name varchar(255),
    modifier_id int8 not null references person,
    owner_id int8 not null references person,
    account_group_id int8 references pos_account_group,
    primary key (id)
);

create table pos_members (
    user_id int8 not null references person,
    account_id int8 not null references pos_account,
    primary key (user_id, account_id)
);

create table pos_group_members (
    user_id int8 not null references person,
    account_id int8 not null references pos_account_group,
    primary key (user_id, account_id)
);

alter table resource add column account_id int8 references pos_account;
create sequence account_sequence;

drop table pos_account cascade;
drop table pos_account_group cascade;
drop table pos_members cascade;
drop table pos_group_members cascade;
drop sequence account_sequence;

create table pos_account_group (
    id  bigserial not null,
    date_created timestamp,
    description varchar(255),
    date_updated timestamp,
    name varchar(255),
    modifier_id int8 not null references person,
    owner_id int8 not null references person,
    primary key (id)
);

create table pos_account (
    id  bigserial not null,
    date_created timestamp,
    description varchar(255),
    date_expires timestamp,
    date_updated timestamp,
    name varchar(255),
    status varchar(255),
    modifier_id int8 not null references person,
    owner_id int8 not null references person,
    account_group_id int8  references pos_account_group,
    primary key (id)
);

create table pos_group_members (
    user_id int8 not null references person,
    account_id int8 not null references pos_account_group,
    primary key (user_id, account_id)
);

create table pos_members (
    user_id int8 not null references person,
    account_id int8 not null references pos_account,
    primary key (user_id, account_id)
);

alter table pos_invoice drop column transactionType;
alter table pos_invoice add column transaction_type varchar(50);
alter table resource drop column account_id;
alter table resource add column account_id int8 references pos_account;
alter table pos_invoice alter column billingPhone type int8;
alter table pos_invoice add column account_type varchar(50);
alter table pos_invoice add column response text;
alter table pos_invoice add column transaction_date timestamp;
alter table pos_account add column files_used int8 default 0;
alter table pos_account add column space_used int8 default 0;
alter table pos_account add column resources_used int8 default 0;

ALTER TABLE project ADD COLUMN orientation VARCHAR(50) DEFAULT 'LIST';

-- 12-06-12 -- adding some invalid billing values
insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price) values (true, 'good', 15,21,50,5, 505);
insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price) values (true, 'error', 5,1,50,5, 55.21);
insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price) values (true, 'decline', 5,1,50,5, 55.11);
insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price) values (true, 'unknown', 5,1,50,5, 55.31);
insert into pos_billing_activity (enabled, name, numberoffiles, numberofhours, numberofmb, numberofresources, price) values (false, 'inactive', 5,1,50,5, 550);


-- 12-07-12 -- adding new column
alter table pos_billing_activity add column min_allowed_files int8;

-- 12-09-12 
alter table pos_invoice add column number_of_files int8;
alter table pos_invoice add column number_of_mb int8;
insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price) values (true, ' 1-  4', 1, 1, 10, 50);
insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price) values (true, ' 5- 19', 1, 5, 10, 45);
insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price) values (true, '20- 49', 1, 20, 10, 40);
insert into pos_billing_activity (enabled, name, numberoffiles, min_allowed_files, numberofmb, price) values (true, '50-500', 1, 50, 10, 33);
update pos_billing_activity set enabled=false where name like 'level%';
ALTER TABLE pos_account_group ADD COLUMN status varchar(25) default 'ACTIVE';

-- 12-13-12
create table pos_billing_model (
    id  bigserial not null primary key,
    date_created timestamp,
    description varchar(255),
    active boolean,
    counting_files boolean,
    counting_space boolean,
    counting_resources boolean,
    version int
);

alter table pos_billing_activity add column model_id int8 references pos_billing_model;

insert into pos_billing_model (date_created, active, counting_files, counting_space, counting_resources) VALUES (now(), true, true, true, false);
update pos_billing_activity set model_id=1 where model_id is null;
alter table pos_billing_model add column version int;

-- 12-16-12
create table explore_cache_year (
    id bigserial primary key,
    key int4,
    item_count bigint
  );

insert into explore_cache_year (key, item_count) select date_part('year', date_registered), count(id) from resource where status='ACTIVE' and date_registered is not null group by date_part('year', date_registered)  order by date_part('year', date_registered)  asc;
ALTER TABLE resource ADD previous_status varchar(50);

-- 1-9-13
ALTER TABLE information_resource_file ADD COLUMN error_message text;
ALTER TABLE homepage_cache_geographic_keyword ADD keyword_id bigint;

create table pos_transaction_log (
	id bigserial primary key,
    date_created timestamp,
    transactionId varchar(255),
    response text);

alter table pos_invoice drop column response;
alter table pos_invoice drop column transactionId;
-- 1-21-13
ALTER TABLE information_resource_file_version add COLUMN effective_size bigint;
alter table pos_invoice add column transaction_id varchar(255);
alter table pos_invoice add column response_id bigint references pos_transaction_log;
alter table pos_billing_activity add column activity_type varchar(25) default 'PRODUCTION';
update pos_billing_activity set activity_type = 'TEST' where name in ('good','error', 'decline', 'unknown');

-- 1-24-13
ALTER table pos_billing_activity add column sort_order int;

ALTER table resource add column total_files bigint;
ALTER table resource add column total_space_in_bytes bigint;

-- 2-26-13
alter table information_resource add constraint irMappingKey Foreign key (mappeddatakeycolumn_id)  references data_table_column ;

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

--2013-05-27
alter table sensory_data add column rgb_capture character varying(255);
alter table sensory_data add column camera_details character varying(255);
alter table sensory_data add column scanner_technology character varying(50);
