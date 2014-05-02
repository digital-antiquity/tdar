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
 
insert into tdar_user (id, affilliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, proxyinstitution_id) select user_id, affilliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, proxyinstitution_id from person;
update tdar_user set username=person.username from tdar_user u, person where u.id=person.id and tdar_user.id=u.id;


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
ALTER table resource ADD foreign key (submitter_id) references tdar_user:
ALTER table resource ADD foreign key (uploader_id) references tdar_user:
ALTER table resource ADD foreign key (updater_id) references tdar_user:
ALTER table resource_revision_log ADD foreign key (person_id) references tdar_user:
ALTER table user_session ADD foreign key (person_id) references tdar_user:
ALTER table collection ADD foreign key (owner_id) references tdar_user:
ALTER table collection ADD foreign key (updater_id) references tdar_user:
ALTER table pos_invoice ADD foreign key (owner_id) references tdar_user:
ALTER table pos_invoice ADD foreign key (executor_id) references tdar_user:
ALTER table pos_account_group ADD foreign key (owner_id) references tdar_user:
ALTER table pos_account_group ADD foreign key (modifier_id) references tdar_user:
ALTER table pos_account ADD foreign key (owner_id) references tdar_user:
ALTER table pos_account ADD foreign key (modifier_id) references tdar_user:
ALTER table pos_members ADD foreign key (user_id) references tdar_user:
ALTER table pos_coupon ADD foreign key (user_id) references tdar_user:
ALTER table authorized_user ADD foreign key (user_id) references tdar_user:

