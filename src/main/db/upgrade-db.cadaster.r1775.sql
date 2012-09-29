-- add display name for dataTables
alter table data_table add column display_name character varying(255);

update data_table set display_name = name;

alter table data_table_column add measurement_unit character varying(25);
alter table data_table_column add column_encoding_type character varying(25);

update data_table_column set column_encoding_type='CODED_NUMERIC' where column_encoding_type_id=4 or column_encoding_type_id=3;
update data_table_column set column_encoding_type='CODED_TEXT' where column_encoding_type_id=5;
update data_table_column set column_encoding_type='NUMERIC' where column_encoding_type_id=0 or column_encoding_type_id=1;
update data_table_column set column_encoding_type='TEXT' where column_encoding_type_id=2;
update data_table_column set column_encoding_type='OTHER' where column_encoding_type_id=7;
update data_table_column set column_encoding_type='MEASUREMENT' where column_encoding_type_id=6;
update data_table_column set column_encoding_type='COUNT' where column_encoding_type_id=8;

update data_table_column set measurement_unit='KILOGRAM' where measurement_unit_id=0;
update data_table_column set measurement_unit='GRAM' where measurement_unit_id=1;
update data_table_column set measurement_unit='MILLIGRAM' where measurement_unit_id=2;
update data_table_column set measurement_unit='MICROGRAM' where measurement_unit_id=3;
update data_table_column set measurement_unit='KILOMETER' where measurement_unit_id=4;
update data_table_column set measurement_unit='METER' where measurement_unit_id=5;
update data_table_column set measurement_unit='CENTIMETER' where measurement_unit_id=6;
update data_table_column set measurement_unit='MILLIMETER' where measurement_unit_id=7;
update data_table_column set measurement_unit='SQUAR_METER' where measurement_unit_id=8;
update data_table_column set measurement_unit='HECTARE' where measurement_unit_id=9;
update data_table_column set measurement_unit='SQUARE_KM' where measurement_unit_id=10;
update data_table_column set measurement_unit='MILLILITER' where measurement_unit_id=11;
update data_table_column set measurement_unit='CUBIC_CM' where measurement_unit_id=12;
update data_table_column set measurement_unit='LITRE' where measurement_unit_id=13;
update data_table_column set measurement_unit='PARTS_PER_MILLION' where measurement_unit_id=14;

ALTER TABLE data_table_column DROP COLUMN measurement_unit_id;
ALTER TABLE data_table_column DROP COLUMN column_encoding_type_id;
drop table measurement_unit ;
drop table column_encoding_type;

drop table document_author;              
drop table resource_creator_person;     
drop table resource_creator_institution;
drop table resource_provider_contact;

alter table resource_note alter note type character varying(5000);

