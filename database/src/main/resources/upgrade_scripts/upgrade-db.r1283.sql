-- remove vestigial fedora components -- 
ALTER TABLE resource DROP COLUMN fedora_pid;
ALTER TABLE related_comparative_collection DROP COLUMN fedora_pid;
ALTER TABLE source_collection DROP COLUMN fedora_pid;

-- add serialized data field to log --
ALTER TABLE resource_revision_log ADD COLUMN payload text;
ALTER TABLE resource_revision_log DROP CONSTRAINT fk1b9fd23232793d68;

-- transition data_table and data_table_column from being "resources" to being stand-alone objects
ALTER TABLE data_table ADD COLUMN title character varying(255);
ALTER TABLE data_table ADD COLUMN description text;

ALTER TABLE data_table_column ADD COLUMN title character varying(255);
ALTER TABLE data_table_column ADD COLUMN description text;

ALTER TABLE data_table DROP CONSTRAINT fk608fa6f9e8e3bf97;
ALTER TABLE data_table_column DROP CONSTRAINT fke5d0f5ce8e3bf97;

update data_table set title=resource.title,description=resource.description from resource where resource.id=data_table.id;
update data_table_column set title=resource.title,description=resource.description from resource where resource.id=data_table_column.id;

CREATE SEQUENCE datatable_sequence
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE datatable_sequence OWNER TO tdar;

CREATE SEQUENCE datatablecolumn_sequence
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE datatablecolumn_sequence OWNER TO tdar;

SELECT setval('datatable_sequence', (SELECT MAX(id) FROM data_table)+1);
SELECT setval('datatablecolumn_sequence', (SELECT MAX(id) FROM data_table_column)+1);

ALTER TABLE data_table
   ALTER COLUMN id SET DEFAULT nextval('datatable_sequence'::regclass);

ALTER TABLE data_table_column
   ALTER COLUMN id SET DEFAULT nextval('datatablecolumn_sequence'::regclass);


delete from resource where resource_type like 'TAB%';

--add resource status - default is active, 
-- historic is active as adding new resources in interface have a default of "false" --
ALTER TABLE resource ADD COLUMN status character varying(50) NOT NULL DEFAULT 'ACTIVE';
update resource r set status = 'ACTIVE';

ALTER TABLE information_resource_file ADD COLUMN queued boolean;
update information_resource_file set queued = false;
update person set contributor=TRUE where id in (select distinct applicant_id from contributor_request) or id in (select distinct submitter_id from resource) or id in (select distinct person_id from full_user);

-- ALTER TABLE resource DROP COLUMN dcmi_type_id;
ALTER TABLE information_resource DROP COLUMN dcmi_type_id;
DROP TABLE dcmi_type;


ALTER TABLE information_resource ADD COLUMN copy_location character varying(255);
update information_resource set copy_location=document.copy_location from document where information_resource.id=document.id;
ALTER TABLE document DROP COLUMN copy_location;


CREATE TABLE image
(
  id bigint NOT NULL,
  CONSTRAINT image_pkey PRIMARY KEY (id),
  CONSTRAINT fk5faa95b51d71f47 FOREIGN KEY (id)
      REFERENCES information_resource (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
ALTER TABLE image OWNER TO tdar;


CREATE TABLE resource_note
(
  id bigserial NOT NULL,
  note character varying(2048),
  note_type character varying(255),
  resource_id bigint NOT NULL,
  CONSTRAINT resource_note_pkey PRIMARY KEY (id),
  CONSTRAINT fk11beb35032793d68 FOREIGN KEY (resource_id)
      REFERENCES resource (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
ALTER TABLE resource_note OWNER TO tdar;


--add downward inheritance tracking fields
ALTER TABLE information_resource ADD COLUMN inheriting_investigation_information boolean NOT NULL DEFAULT FALSE;
ALTER TABLE information_resource ADD COLUMN inheriting_site_information boolean NOT NULL DEFAULT FALSE;
ALTER TABLE information_resource ADD COLUMN inheriting_material_information boolean NOT NULL DEFAULT FALSE;
ALTER TABLE information_resource ADD COLUMN inheriting_other_information boolean NOT NULL DEFAULT FALSE;
ALTER TABLE information_resource ADD COLUMN inheriting_cultural_information boolean NOT NULL DEFAULT FALSE;
ALTER TABLE information_resource ADD COLUMN inheriting_spatial_information boolean NOT NULL DEFAULT FALSE;
ALTER TABLE information_resource ADD COLUMN inheriting_temporal_information boolean NOT NULL DEFAULT FALSE;


