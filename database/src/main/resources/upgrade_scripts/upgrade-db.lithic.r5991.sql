-- abrin 04/28/2014
create index idx_created on resource (date_registered);


-- abrin 04/29/2014
-- separate TdarUser table
create table tdar_user (
    affilliation varchar(255),
    contributor boolean default FALSE not null,
    contributor_agreement_version int default 0 not null,
    contributor_reason varchar(512),
    last_login timestamp,
    penultimate_login timestamp,
    proxy_note text,
    tos_version int default 0 not null,
    total_login int8,
    username varchar(255),
    id int8 not null,
    proxyInstitution_id int8,
    primary key (id)
);
 
insert into tdar_user (id, username, affilliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, proxyinstitution_id) select id, username, affilliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, proxyinstitution_id from person where username is not null;


alter table person ALTER COLUMN contributor drop not null;
alter table person ALTER COLUMN registered drop not null;

--alter table person drop column registered;
--alter table person drop column affilliation;
--alter table person drop column contributor;
--alter table person drop column contributor_agreement_version;
--alter table person drop column contributor_reason;
--alter table person drop column last_login;
--alter table person drop column penultimate_login;
--alter table person drop column proxy_note;
--alter table person drop column tos_version;
--alter table person drop column total_login;
--alter table person drop column username;
--alter table person drop column proxyInstitution_id;


CREATE OR REPLACE FUNCTION remove_fk_person() RETURNS integer AS $$
DECLARE
    sql  RECORD;
BEGIN    RAISE NOTICE 'removing FKs...';
    FOR sql IN SELECT tc.table_name, tc.constraint_name FROM information_schema.table_constraints AS tc JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name WHERE constraint_type = 'FOREIGN KEY' AND ccu.table_name='person' AND tc.table_name != 'person' LOOP
    EXECUTE 'ALTER table ' || sql.table_name || ' DROP constraint ' || sql.constraint_name;
    END LOOP;
    RAISE NOTICE 'done removing FKs...';
    RETURN 1;
END;
$$ LANGUAGE plpgsql;

select remove_fk_person();


ALTER table personal_filestore_ticket ADD foreign key (submitter_id) references tdar_user;
ALTER table bookmarked_resource ADD foreign key (person_id) references tdar_user;
ALTER table resource ADD foreign key (submitter_id) references tdar_user;
ALTER table resource ADD foreign key (uploader_id) references tdar_user;
ALTER table resource ADD foreign key (updater_id) references tdar_user;
ALTER table resource_revision_log ADD foreign key (person_id) references tdar_user;
ALTER table user_session ADD foreign key (person_id) references tdar_user;
ALTER table collection ADD foreign key (owner_id) references tdar_user;
ALTER table collection ADD foreign key (updater_id) references tdar_user;
ALTER table pos_invoice ADD foreign key (owner_id) references tdar_user;
ALTER table pos_invoice ADD foreign key (executor_id) references tdar_user;
ALTER table pos_account_group ADD foreign key (owner_id) references tdar_user;
ALTER table pos_account_group ADD foreign key (modifier_id) references tdar_user;
ALTER table pos_account ADD foreign key (owner_id) references tdar_user;
ALTER table pos_account ADD foreign key (modifier_id) references tdar_user;
ALTER table pos_members ADD foreign key (user_id) references tdar_user;
ALTER table pos_coupon ADD foreign key (user_id) references tdar_user;
ALTER table authorized_user ADD foreign key (user_id) references tdar_user;



-- abrin 05-11-2014
create table email_queue (
    id  bigserial not null,
    date_created timestamp,
    date_sent timestamp,
    error_message varchar(2048),
    from_address varchar(255),
    message text,
    number_of_tries int4,
    status varchar(25),
    subject varchar(1024),
    to_address varchar(1024),
    primary key (id)
);

-- jdevos 05/13/2014
--nullify invalid email address that was introduced prior to email validation
update person set email = null where id = 8009 and email = '';

--add 'not empty' constraint for person email, username
alter table person add constraint person_email_notempty check (email <> '');
alter table tdar_user add constraint person_username_notempty check(username <> '');

-- jdevos 05/19/2014
-- empty-strings::geographic_keyword: remove references, then remove instances, then add not-empty constraint
delete from resource_geographic_keyword rk where exists (select * from geographic_keyword k where k.id = rk.geographic_keyword_id and k.label = '');
delete from geographic_keyword where label = '';
alter table geographic_keyword add constraint geographic_keyword_label_notempty check (label <> '');

-- empty-strings::other_keyword: remove references, then remove instances, then add not-empty constraint
delete from resource_other_keyword rk where exists (select * from other_keyword k where k.id = rk.other_keyword_id and k.label = '');
delete from other_keyword where label = '';
alter table other_keyword add constraint other_keyword_label_notempty check (label <> '');

-- empty-strings::site_name_keyword: remove references, then remove instances, then add not-empty constraint
delete from resource_site_name_keyword rk where exists (select * from site_name_keyword k where k.id = rk.site_name_keyword_id and k.label = '');
delete from site_name_keyword where label = '';
alter table site_name_keyword add constraint site_name_keyword_label_notempty check (label <> '');

-- empty-strings::temporal_keyword: remove references, then remove instances, then add not-empty constraint
delete from resource_temporal_keyword rk where exists (select * from temporal_keyword k where k.id = rk.temporal_keyword_id and k.label = '');
delete from temporal_keyword where label = '';
alter table temporal_keyword add constraint temporal_keyword_label_notempty check (label <> '');

-- empty-strings::investigation_type: remove references, then remove instances, then add not-empty constraint
delete from resource_investigation_type rk where exists (select * from investigation_type k where k.id = rk.investigation_type_id and k.label = '');
delete from investigation_type where label = '';
alter table investigation_type add constraint investigation_type_label_notempty check (label <> '');

-- empty-strings::material_keyword: remove references, then remove instances, then add not-empty constraint
delete from resource_material_keyword rk where exists (select * from material_keyword k where k.id = rk.material_keyword_id and k.label = '');
delete from material_keyword where label = '';
alter table material_keyword add constraint material_keyword_label_notempty check (label <> '');

-- empty-strings::culture_keyword: remove references, then remove instances, then add not-empty constraint
delete from resource_culture_keyword rk where exists (select * from culture_keyword k where k.id = rk.culture_keyword_id and k.label = '');
delete from culture_keyword where label = '';
alter table culture_keyword add constraint culture_keyword_label_notempty check (label <> '');

-- empty-strings::site_type_keyword: remove references, then remove instances, then add not-empty constraint
delete from resource_site_type_keyword rk where exists (select * from site_type_keyword k where k.id = rk.site_type_keyword_id and k.label = '');
delete from site_type_keyword where label = '';
alter table site_type_keyword add constraint site_type_keyword_label_notempty check (label <> '');

-- alllee bookmarked resource unique constraints 6/6/2014
-- deletes most recent duplicate bookmarked resource
delete from bookmarked_resource where id in (select b.id from bookmarked_resource b, bookmarked_resource bb where b.person_id=bb.person_id and b.resource_id=bb.resource_id and b.timestamp > bb.timestamp);
-- create unique index on bookmarked_resource's person/resource combination
create unique index bookmarked_resource_unique_idx on bookmarked_resource (person_id, resource_id);



-- abrin 6/16/2014 -- resource aggregate statistics
create table resource_access_day_agg (
    id bigserial,
    resource_id bigint,
    count bigint,
    date_accessed date,
    year int);
    
create index agg_res_date on resource_access_day_agg (resource_id, date_accessed);
create index agg_res_year on resource_access_day_agg (resource_id, year);

create table file_download_day_agg (
    id bigserial,
    information_resource_file_id bigint,
    count bigint,
    date_accessed date,
    year int);

create index agg_dwnld_date on file_download_day_agg (information_resource_file_id, date_accessed);
create index agg_dwnld_year on file_download_day_agg (information_resource_file_id, year);
alter table file_download_day_agg add constraint file_per_day UNIQUE(date_accessed, information_resource_file_id);

-- alllee user_notifications for TDAR-3908
create table user_notification (
    id  bigserial not null,
    date_created timestamp not null,
    expiration_date date,
    message_key text not null,
    message_type varchar(32) not null,
    user_id int8 references tdar_user,
    primary key (id)
);

alter table tdar_user add column dismissed_notifications_date timestamp;
alter table tdar_user rename column affilliation to affiliation;
insert into user_notification(date_created, message_key, message_type, user_id) 
    SELECT current_timestamp, 'pre.tdar.invoice', 'INFO', tdar_user.id
    FROM pos_account, tdar_user 
    WHERE pos_account.owner_id=tdar_user.id and pos_account.description like '%auto-gen%' and (tdar_user.last_login is null or tdar_user.last_login < '2013-01-01');

alter table user_session rename column person_id to tdar_user_id;

-- abrin 07/14/2014 unique indexes
CREATE UNIQUE INDEX culture_keyword_label_unique on culture_keyword(lower(label));
CREATE UNIQUE INDEX geographic_label_unique on geographic_keyword(lower(label));
CREATE UNIQUE INDEX other_keyword_label_unique on other_keyword(lower(label));
CREATE UNIQUE INDEX site_name_keyword_label_unique on site_name_keyword(lower(label));
CREATE UNIQUE INDEX site_tpe_keyword_label_unique on site_type_keyword(lower(label));
CREATE UNIQUE INDEX temporal_keyword_label_unique on temporal_keyword(lower(label));

-- abrin 07/16/2014 notification type
alter table user_notification add column display_type varchar(32) not null default 'NORMAL';
insert into user_notification(date_created, message_key, message_type,display_type) VALUES (current_timestamp, 'lithic','SYSTEM_BROADCAST','FREEMARKER');
-- abrin 05/11/2014
create index information_resource_file_ir2 on information_resource_file(information_resource_id);

-- abrin 07/24/2014
alter table creator_view_statistics drop constraint creator_view_statistics_creator_id_fkey;

-- abrin 07/27/2014
alter table creator add column hidden_if_unreferenced boolean not null default false;


-- abrin 08/15/2014
alter table information_resource add column external_doi varchar(255);
update information_resource set external_doi=doi from information_resource ir, document where ir.id=information_resource.id and information_resource.id=document.id;

-- abrin 8/19/2014
alter table creator drop column hidden_if_unreferenced;
alter table email_queue add column user_generated boolean default true;



-- abrin --6/16/2014 -- uncomment and run separately for production deployment (slow )
-- insert into resource_access_day_agg (resource_id, count, date_accessed)  select resource_id, count(id), date_trunc('day', date_accessed) from resource_access_statistics group by resource_id, date_trunc('day', date_accessed);
-- update resource_access_day_agg set year = date_part('year', date_accessed);
-- alter table resource_access_day_agg add constraint view_per_day UNIQUE(date_accessed, resource_id);


-- insert into file_download_day_agg (information_resource_file_id, count, date_accessed)  select information_resource_file_id, count(id), date_trunc('day', date_accessed) from information_resource_file_download_statistics group by information_resource_file_id, date_trunc('day', date_accessed);
-- update file_download_day_agg set year = date_part('year', date_accessed);

-- abrin 09/10/2014 adding index and column for month
alter table resource_access_day_agg add column month int;
update resource_access_day_agg set month=date_part('month',date_accessed);
create index agg_res_month on resource_access_day_agg (month);
create index agg_res_month_year on resource_access_day_agg (year, month);

alter table file_download_day_agg add column month int;
update file_download_day_agg set month=date_part('month',date_accessed);
create index agg_dwnld_month on file_download_day_agg (month);

-- abrin 10/17/2014 -- creator cleanup issues
alter table creator add column browse_occurrence bigint default 0;
alter table creator add column hidden boolean default false;