-- update all institution ids, add +10000 to generate unique keys within the unified
-- creator->(person,institution) hierarchy
alter table resource_creator_institution drop constraint "resource_creator_institution_institution_id_fkey";
alter table person drop constraint "person_institution_id_fkey";
alter table information_resource drop constraint "information_resource_provider_institution_id_fkey";

update institution set id=id+11000;

update resource_creator_institution set institution_id=institution_id+11000;
update person set institution_id=institution_id+11000;
update information_resource set provider_institution_id=provider_institution_id+11000;

alter table resource_creator_institution add constraint resource_creator_institution_fk foreign key (institution_id) references institution;
alter table person add constraint person_institution_fk foreign key (institution_id) references institution;
alter table information_resource add constraint information_resource_institution_fk foreign key (provider_institution_id) references institution;

  
CREATE SEQUENCE creator_id_seq;

create table creator (id bigint DEFAULT nextval('creator_id_seq'), creator_type varchar(64), date_created date, last_updated timestamp, location varchar(255), url varchar(64), primary key (id));

insert into creator (id, date_created) select id, date_created from person;

insert into creator (id) select id from institution;

alter table person add constraint person_creator_fk foreign key (id) references creator;
alter table institution add constraint institution_creator_fk foreign key (id) references creator;

-- readd person uniqueness constraint
update person set email=NULL where email='';
-- based on select email from person group by email having (count(email) > 1);
-- we have one person with a duped email.  alter them manually.
alter table person add constraint email_unique_constraint unique(email);


CREATE TABLE resource_creator
(
  id bigserial NOT NULL,
  "role" character varying(255),
  sequence_number integer,
  creator_id bigint NOT NULL,
  resource_id bigint NOT NULL,
  CONSTRAINT resource_creator_pkey PRIMARY KEY (id),
  CONSTRAINT fk5b43fcfb32793d68 FOREIGN KEY (resource_id)
      REFERENCES resource (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk5b43fcfb67ffc561 FOREIGN KEY (creator_id)
      REFERENCES creator (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
ALTER TABLE resource_creator OWNER TO tdar;

SELECT setval('creator_id_seq', (SELECT MAX(id) FROM creator)+1);

CREATE TABLE resource_annotation_key
(
  id bigserial NOT NULL,
  annotation_data_type character varying(255),
  format_string character varying(128),
  "key" character varying(128),
  resource_annotation_type character varying(255),
  submitter_id bigint,
  CONSTRAINT resource_annotation_key_pkey PRIMARY KEY (id),
  CONSTRAINT fk9b5fd5a0fc7475f FOREIGN KEY (submitter_id)
      REFERENCES person (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
ALTER TABLE resource_annotation_key OWNER TO tdar;



CREATE TABLE resource_annotation
(
  id bigserial NOT NULL,
  date_created date,
  last_updated timestamp without time zone,
  "value" text,
  resource_id bigint NOT NULL,
  resourceannotationkey_id bigint NOT NULL,
  CONSTRAINT resource_annotation_pkey PRIMARY KEY (id),
  CONSTRAINT fkfdf7080032793d68 FOREIGN KEY (resource_id)
      REFERENCES resource (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkfdf70800a279d68c FOREIGN KEY (resourceannotationkey_id)
      REFERENCES resource_annotation_key (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
ALTER TABLE resource_annotation OWNER TO tdar;

