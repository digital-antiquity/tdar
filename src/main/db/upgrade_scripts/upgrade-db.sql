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