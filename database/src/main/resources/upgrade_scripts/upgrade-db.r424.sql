alter table information_resource_format add column accepted_format bool default false;
update information_resource_format set accepted_format=true;
-- don't allow postgres/shapefile uploads yet...
update information_resource_format set accepted_format=false where id in (7, 8);

CREATE TABLE information_resource_source_citation (
    information_resource_id bigint NOT NULL references information_resource(id),
    document_id bigint NOT NULL references document(id),
    primary key(information_resource_id, document_id)
);

CREATE TABLE information_resource_related_citation (
    information_resource_id bigint NOT NULL references information_resource(id),
    document_id bigint NOT NULL references document(id),
    primary key(information_resource_id, document_id)
);


