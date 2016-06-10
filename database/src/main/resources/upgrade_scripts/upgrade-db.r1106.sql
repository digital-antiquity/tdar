-- drop tables
DROP TABLE citation;
DROP TABLE keyword;

-- drop sequences
DROP SEQUENCE hibernate_sequence CASCADE;
DROP SEQUENCE keyword_id_seq CASCADE;
DROP SEQUENCE context_sequence CASCADE;
DROP SEQUENCE document_id_seq CASCADE;

-- drop columns
ALTER TABLE information_resource DROP COLUMN information_resource_format_id;
ALTER TABLE information_resource DROP COLUMN original_format_id;
ALTER TABLE information_resource_format DROP COLUMN mime_type;

--add new language columns to information_resource
ALTER TABLE information_resource ADD COLUMN resource_language character varying(100);
ALTER TABLE information_resource ADD COLUMN metadata_language character varying(100);

--backfill language values for newly created columns
UPDATE information_resource SET
    resource_language = (select upper(language) from language where language.id = information_resource.resource_language_id),
    metadata_language = (select upper(language) from language where language.id = information_resource.metadata_language_id);
    
--remove old language_id columns, and then drop the language table altogether
ALTER TABLE information_resource DROP CONSTRAINT information_resource_resource_language_id_fkey;
ALTER TABLE information_resource DROP CONSTRAINT information_resource_metadata_language_id_fkey;
ALTER TABLE information_resource DROP COLUMN resource_language_id;
ALTER TABLE information_resource DROP COLUMN metadata_language_id;
DROP TABLE language CASCADE;

    
--add new role columns to resource_creator_person and resource_creator_institution
ALTER TABLE resource_creator_person ADD COLUMN "role" character varying(100);
ALTER TABLE resource_creator_institution ADD COLUMN "role" character varying(100);

--backfill role names from old ID values    
UPDATE resource_creator_person SET
    "role" = (select replace(upper("name"), ' ', '_')  
        from creator_person_role 
        where resource_creator_person.role_id = creator_person_role.id);

UPDATE resource_creator_institution SET
    "role" = (select replace(upper("name"), ' ', '_')  
        from creator_institution_role 
        where resource_creator_institution.role_id = creator_institution_role.id);

UPDATE resource_creator_institution set role = 'OTHER' WHERE role is null;

--remove old role_id columns, and then the role lookup tables.
ALTER TABLE resource_creator_person DROP CONSTRAINT fkeba31b19329956a2;
ALTER TABLE resource_creator_person DROP COLUMN role_id;
ALTER TABLE resource_creator_institution DROP CONSTRAINT fk73b76074ac1d07ff;
ALTER TABLE resource_creator_institution DROP COLUMN role_id;
DROP TABLE creator_person_role CASCADE;
DROP TABLE creator_institution_role CASCADE;


ALTER TABLE dataset DROP COLUMN translated_filename CASCADE;
DROP SEQUENCE information_resource_file_id_seq CASCADE;
ALTER TABLE information_resource_file DROP CONSTRAINT information_resource_file_translated_file_id_fkey CASCADE;
ALTER TABLE information_resource_file DROP CONSTRAINT information_resource_file_pkey CASCADE;
ALTER TABLE information_resource_file RENAME to information_resource_file_old;


CREATE SEQUENCE information_resource_file_seq;
CREATE SEQUENCE information_resource_file_version_seq;


CREATE TABLE information_resource_file
(
  id bigint DEFAULT nextval('information_resource_file_seq'),
  download_count integer,
  general_type character varying(255),
  latest_version integer,
  processed boolean NOT NULL,
  "sequence" integer,
  shared boolean NOT NULL,
  information_resource_id bigint,
  CONSTRAINT information_resource_file_pkey PRIMARY KEY (id),
  CONSTRAINT fk2bc70d3a7b2d0e85 FOREIGN KEY (information_resource_id)
      REFERENCES information_resource (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
ALTER TABLE information_resource_file OWNER TO tdar;



CREATE TABLE information_resource_file_version
(
  id bigint DEFAULT nextval('information_resource_file_version_seq'),
  checksum character varying(255),
  checksum_type character varying(255),
  date_created timestamp without time zone NOT NULL,
  extension character varying(255),
  file_type character varying(255),
  filename character varying(255),
  filestore_id character varying(255),
  format character varying(255),
  height integer,
  internal_type character varying(255),
  mime_type character varying(255),
  path character varying(255),
  premisid character varying(255),
  size bigint,
  file_version integer,
  width integer,
  information_resource_file_id bigint,
  CONSTRAINT information_resource_file_version_pkey PRIMARY KEY (id),
  CONSTRAINT fk276ff6d3ff692808 FOREIGN KEY (information_resource_file_id)
      REFERENCES information_resource_file (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT information_resource_file_vers_information_resource_file_id_key UNIQUE (information_resource_file_id, file_version, internal_type)
)
WITH (OIDS=FALSE);
ALTER TABLE information_resource_file_version OWNER TO tdar;

-- migrate files
-- CHECK THIS
insert into information_resource_file (id,download_count,general_type,latest_version,processed,"sequence",shared,information_resource_id) select id,download_count,information_resource_format_id,0,processed,0,'y',information_resource_id from information_resource_file_old;

update information_resource_file set general_type='DOCUMENT' WHERE general_type='1' or general_type='6';
update information_resource_file set general_type='IMAGE' WHERE general_type='9' or general_type='10' or general_type='11' or general_type='12';
update information_resource_file set general_type='COLUMNAR_DATA' WHERE general_type='7' or general_type='0' or general_type='2' or general_type='3';
update information_resource_file set general_type='OTHER' WHERE general_type='5' or general_type='8';


-- transfer all files into the new table
insert into information_resource_file_version 
    (checksum, checksum_type, filename, filestore_id, mime_type,information_resource_file_id, internal_type,extension,date_created,file_version) 
    select checksum, checksum_type, filename, filestore_id, mime_type, id, 'UPLOADED',
    substring(filename from '%.#"__%#"$' for '#'),now(),0
        from information_resource_file_old;

-- repoint all of the references for the translated ones to point to the same file, then change their types
update information_resource_file_version set information_resource_file_id=oldtable.id, internal_type='TRANSLATED' FROM 
    information_resource_file_version as vers  INNER JOIN information_resource_file_old as oldtable 
    ON (oldtable.translated_file_id=vers.information_resource_file_id) WHERE oldtable.translated_file_id=information_resource_file_version.information_resource_file_id;

DROP TABLE information_resource_format_file_extension CASCADE;
DROP TABLE information_resource_format  CASCADE;
DROP TABLE information_resource_file_old CASCADE;

-- ALTER TABLE information_resource_file_old DROP CONSTRAINT information_resource_file_information_resource_id_fkey;

SELECT setval('information_resource_file_seq', (SELECT MAX(id) FROM information_resource_file)+1);
SELECT setval('information_resource_file_version_seq', (SELECT MAX(id) FROM information_resource_file_version)+1);

UPDATE investigation_type set label='Methodological, Synthetic or Theoretical Research' where label='Methological, Synthetic or Theoretical Research';

ALTER TABLE information_resource DROP COLUMN filename CASCADE;

UPDATE DOCUMENT SET DOCUMENT_TYPE='OTHER' WHERE DOCUMENT_TYPE='MANUSCRIPT';


ALTER TABLE resource ADD COLUMN updater_id bigint;
ALTER TABLE resource ADD COLUMN date_updated timestamp without time zone;
UPDATE resource set date_updated=date_registered;
UPDATE resource set updater_id=submitter_id;
