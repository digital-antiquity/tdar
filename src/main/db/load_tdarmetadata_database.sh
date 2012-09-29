echo "dropping tdarmetadata and tdardata databases"
dropdb tdarmetadata -U tdar
dropdb tdardata -U tdar
echo "creating tdarmetadata and tdardata databases"
createdb tdarmetadata -O tdar  -U tdar
createdb tdardata -O tdar -U tdar
echo "loading schema"
psql -U tdar tdarmetadata -f tdarmetadata_schema.sql > log.txt
echo "loading controlled data"
psql -U tdar tdarmetadata -f tdarmetadata_init.sql >> log.txt
echo "loading sample data"
psql -U tdar tdarmetadata -f tdarmetadata_sample_data.sql >> log.txt
echo "running latest upgrade-db script to bring up to current rev"
psql -U tdar tdarmetadata -f upgrade_scripts/upgrade-db.sql >> log.txt
