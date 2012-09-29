#dump the schema
pg_dump -s -U tdar tdarmetadata > tdarmetadata_schema.sql
#dump the controlled data
pg_dump -a --disable-triggers --column-inserts  -U tdar -t category_variable -t category_variable_synonyms -t culture_keyword -t culture_keyword_synonym -t investigation_type -t investigation_type_synonym -t material_keyword -t material_keyword_synonym -t site_type_keyword -t site_type_keyword_synonym tdarmetadata > tdarmetadata_init.sql
#dump the uncontrolled data
pg_dump -a --disable-triggers --column-inserts  -U tdar -T category_variable -T category_variable_synonyms -T culture_keyword -T culture_keyword_synonym -T investigation_type -T investigation_type_synonym -T material_keyword -T material_keyword_synonym -T site_type_keyword -T site_type_keyword_synonym tdarmetadata > tdarmetadata_sample_data.sql
