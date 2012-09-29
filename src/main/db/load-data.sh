#!/bin/sh
# assumes that all upgrade scripts are in the same directory as this script

TDAR_METADATA_SQL="tdarmetadata.sql"
UPGRADE_DB_SQL="upgrade-db.sql"
LOAD_KEYWORDS_SQL="keywords.sql"
WORKING_DIRECTORY=`dirname $0`

echo "Current working directory: $WORKING_DIRECTORY"
echo "importing metadata from ${TDAR_METADATA_SQL}"
psql -U tdar tdarmetadata < $WORKING_DIRECTORY/$TDAR_METADATA_SQL
psql -U tdar tdarmetadata < $WORKING_DIRECTORY/$UPGRADE_DB_SQL
psql -U tdar tdarmetadata < $WORKING_DIRECTORY/$LOAD_KEYWORDS_SQL
