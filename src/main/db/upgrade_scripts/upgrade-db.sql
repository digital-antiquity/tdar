-- 22-sep-2011
-- add institution.email
alter table institution add column email varchar(255);
-- account for new embargo options
update information_resource_file set restriction = 'EMBARGOED_FIVE_YEARS' where restriction = 'EMBARGOED';


-- NOTE THIS IS NOW DEPRECATED. IN ITS PLACE USE LIQUIBASE:
-- mvn clean compile -Pliquibase