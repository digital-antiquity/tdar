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
     
insert into tdar_user (id, affilliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, proxyinstitution_id) select user_id, affilliation, contributor, contributor_agreement_version, contributor_reason, last_login, penultimate_login, proxy_note, tos_version, total_login, proxyinstitution_id from user_info;
update tdar_user set username=person.username from tdar_user u, person where u.id=person.id and tdar_user.id=u.id;

--update person drop column registered;
--update person drop column username;
