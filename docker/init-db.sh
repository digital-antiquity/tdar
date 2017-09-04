#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER tdar;
    ALTER USER dataarc WITH PASSWORD 'dataarc';
    CREATE DATABASE tdarmetadata with owner tdar;
    CREATE DATABASE tdardata with owner tdar;
    CREATE DATABASE tdargis template=template_postgis with owner tdar;
    GRANT ALL PRIVILEGES ON DATABASE tdarmetadata TO tdar;
    GRANT ALL PRIVILEGES ON DATABASE tdardata TO tdar;
    GRANT ALL PRIVILEGES ON DATABASE tdargis TO tdar;
EOSQL
