-- add ontologies to information_resource table
insert into information_resource (id) select o.id from ontology o where o.id not in (select id from information_resource);
-- ontology resource_id needs to be copied to project.id
update information_resource set project_id=o.resource_id
from ontology o
where o.id=information_resource.id and o.resource_id in (select id from project);

alter table ontology drop column resource_id;

-- replace FK to resource table with FK to information_resource table.
alter table ontology drop constraint fk91d1094fe8e3bf97;
alter table ontology add foreign key (id) references information_resource(id);

-- move documents to correct table
-- remove nullability constraint on document_type
alter table document alter column document_type drop not null;

-- insert all information resources that have TEXT dcmi_type
insert into document (id) select id from information_resource where dcmi_type_id=5;

-- fix 1 degenerate data case submitted by Matt, a test dataset with a text dcmi_type
update information_resource set dcmi_type_id=1 where id=2531;

-- remove publisher FKs from dataset and resource, replace with FK to information_resource
alter table publisher drop constraint fkea2f1af519e50a8c;
alter table publisher drop constraint fkea2f1af56ac97cbe;
alter table publisher add foreign key (resource_id) references information_resource(id);
-- remove TEXT dcmi_types from dataset
delete from dataset where id in (select id from information_resource where dcmi_type_id=5);

update information_resource set available_to_public='t' where available_to_public is null;

-- fix orphaned information resources with no project, set their project_id to the
-- independent resources project for now
update information_resource
set project_id=project.id
from resource r, resource rr, person, project
where information_resource.project_id is null and r.id=information_resource.id
and r.submitter_id=person.id and rr.resource_type='INDEPENDENT_RESOURCES_PROJECT'
and rr.submitter_id=r.submitter_id and rr.id=project.id;

-- fix a few cases where Keith's uploaded reports are registered as datasets.
update resource set resource_type='DOCUMENT' where id=1624;

-- fix resource_type set on datasets that have been changed to documents
update resource set resource_type='DOCUMENT' 
from information_resource 
where resource.id=information_resource.id and information_resource.dcmi_type_id=5 and resource.resource_type not like 'DOCUMENT';

-- delete duplicate publisher contacts, discovered by
-- select distinct p.id 
-- from publisher p, publisher pp 
-- where p.person_id=pp.person_id and p.resource_id=pp.resource_id and p.id > pp.id;
delete from publisher where id in (5342, 5343, 5344, 5345, 7486);
