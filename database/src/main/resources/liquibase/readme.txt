# Notes on setting up a database instance of tDAR.

TDAR's database is managed by liquibase (http://www.liquibase.org).  We use liquibase to setup the schema, test data,
and production data, as well as manage upgrades.  This is managed through a series of maven goals, and locations.

Prior to running these scripts, create 3 databases:
 * tdarmetadata
 * tdardata
 * tdargis (not currently managed via liquibase, sql in tdar.support)

## Data locations:
 - src/main/db/changelog.xml -- production upgrade script
 - src/main/db/release-name.xml -- release specific upgrades
 - src/main/db/setup-production-database.xml -- setup production database from scratch
 
 - src/test/db/ -- test data location
 - src/test/db/setup-dev-instance.xml -- set up development instance of tDAR
 

## Maven Profiles:
 - liquibase-setup-dev-instance -- sets up a developer instance of tdar with test data
 - liquibase-setup-production-instance -- sets up an empty, production instance of tDAR
 - liquibase -- upgrades a local instance (tdarmetadata) of the database with any outstanding changelogs
 - liquibase-export -- exports a copy of the tdarmetadata database to a xml dump
 - changelogSync -- clears the liquibase changesets and resets to "ok"
 

 NOTE: maven uses the hibernate.properties to read the database connection info.
 
 ### example execution:


     mvn -Pliquibase clean compile
