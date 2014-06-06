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
--alter table person drop column id;
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
ALTER table resource_revision_log ADD foreign key (person_id) references tdar_user
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
alter table person add constraint person_username_notempty check(username <> '');

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
