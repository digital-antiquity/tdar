#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER tdar;
    ALTER USER tdar WITH PASSWORD 'tdar';
    CREATE DATABASE tdarmetadata with owner 'tdar';
    CREATE DATABASE tdardata with owner 'tdar';
    CREATE DATABASE tdargis with owner 'tdar';
    CREATE DATABASE tdarbalk with owner 'tdar';
    GRANT ALL PRIVILEGES ON DATABASE tdarmetadata TO tdar;
    GRANT ALL PRIVILEGES ON DATABASE tdardata TO tdar;
    GRANT ALL PRIVILEGES ON DATABASE tdargis TO tdar;
    GRANT ALL PRIVILEGES ON DATABASE balk TO tdar;
EOSQL
mkdir -pv /var/lib/postgresql/backups/data/
echo  "0 1 * * * pg_dump -U tdar tdar > /var/lib/postgresql/data/backups/\`date  +%Y-%m-%d\`.sql" > .crontab 
touch /var/log/cron.log
/etc/init.d/cron start

crontab .crontab
