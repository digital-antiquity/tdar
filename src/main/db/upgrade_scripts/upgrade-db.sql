-- jdevos 8/20/2013
-- upgrade script for knap
-- add sensory data records to the dataset join-table
insert into dataset(id) select id from sensory_data sd where not exists (select * from dataset ds where ds.id = sd.id);

-- abrin 8/22/2013
alter table geospatial drop column projection;
alter table geospatial add column map_source varchar(500);

-- mpaulo 09/02/2013 TDAR-3006
alter table archive add column doimportcontent boolean default false;
alter table archive add column importdone boolean default false;

-- mpaulo 09/11/2013 TDAR-3018
CREATE TABLE audio (
    id bigint NOT NULL,
    audio_codec varchar(255),
    software varchar(255),
    bit_depth int4,
    bit_rate int4,
    sample_rate int4,
    CONSTRAINT audio_pkey PRIMARY KEY (id ),
    CONSTRAINT audio_fkey FOREIGN KEY (id)
    REFERENCES information_resource (id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
) WITH (
    OIDS=FALSE
);
ALTER TABLE audio OWNER TO tdar;

-- jdevos 9/5/2013
alter table person add column tos_level integer not null default 0;
alter table person add column creator_agreement_level integer not null default 0;

alter table person rename column tos_level  to tos_version;
alter table person rename column creator_agreement_level  to creator_agreement_version;

-- jdevos 9/11/2013
alter table person rename column creator_agreement_version  to contributor_agreement_version;

-- TDAR-1978 mpaulo 9/23/2013
alter table latitude_longitude add column is_ok_to_show_exact_location boolean default false;

-- TDAR-1978 mpaulo 9/25/2013
alter table latitude_longitude add column min_obfuscated_lat double precision;
alter table latitude_longitude add column min_obfuscated_long double precision;
alter table latitude_longitude add column max_obfuscated_lat double precision;
alter table latitude_longitude add column max_obfuscated_long double precision;