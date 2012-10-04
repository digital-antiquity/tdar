#!/bin/bash
echo "dropping tdarmetadata and tdardata databases"
dropdb -U tdar tdarmetadata 
dropdb -U tdar tdardata 
echo "creating tdarmetadata and tdardata databases"
createdb -O tdar  -U tdar tdarmetadata 
createdb -O tdar -U tdar tdardata
echo "loading schema based on the latest stable release"
psql -U tdar -f tdarmetadata_schema.sql tdarmetadata > log.txt
echo "loading controlled data"
psql -U tdar -f tdarmetadata_init.sql tdarmetadata >> log.txt
echo "loading sample data"
psql -U tdar -f tdarmetadata_sample_data.sql tdarmetadata >> log.txt
echo "running latest upgrade-db script to bring up to current rev"
psql -U tdar -f upgrade_scripts/upgrade-db.sql tdarmetadata >> log.txt
