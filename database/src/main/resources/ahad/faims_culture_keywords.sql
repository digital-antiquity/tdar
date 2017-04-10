-- The grand plan is: 
--    1) backup database:   
--          pg_dump -U postgres -o tdardata > tdardata.sql
--          pg_dump -U postgres -o tdarmetadata > tdarmetadata.sql
--          pg_dumpall > outfile    ... dumps everthing in one hit
--    2) drop database:   what about simply connecting to new one? dropdb -U postgres 'faimsmetadata'
--    3) import ahad db (which should have the AHAD keywords base)
--          createdb -U postgres -O tdar -T template0 faimsdata
--          psql -U postgres faimsdata < tdardata.sql
--          createdb -U postgres -O tdar -T template0 faimsmetadata
--          psql -U postgres faimsmetadata < tdarmetadata.sql
--          psql -f infile postgres   ... restores from a dump all...
--    4) run the update.sql's that need to be run
--    5) add the new terms to the culture keyword table 
--    6) migrate existing culture keyword terms to the new one: "Historical Archaeology (incl. Industrial Archaeology) (FOR 210108)"
--    7) drop the current culture keyword terms
--    Steps 5, 6 & 7: (this file)
--          psql -U tdar -f faims_culture_keywords.sql faimsmetadata >> log.txt

-- Step 5) add the new new terms to the culture keyword table
--
-- Data for Name: culture_keyword; Type: TABLE DATA; Schema: public; Owner: tdar
--


update culture_keyword set definition = null, label = 'Archaeology', approved = true, index = '2101', selectable = false, parent_id = null where id = 2101;
--insert into culture_keyword (id, label, approved, index, selectable) values (2101, 'Archaeology', true, '2101', false);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210101, 'Aboriginal and Torres Strait Islander Archaeology (FOR 210101)', true, '2101.1', true, 2101);

insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210102, 'Archaeological Science (FOR 210102)', true, '2101.2', true, 2101);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (300000, 'Geochronology', true, '2101.2.1', true, 210102);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (300001, 'Zooarchaeology', true, '2101.2.2', true, 210102);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (300002, 'Bioarchaeology', true, '2101.2.3', true, 210102);

insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210103, 'Archaeology of Asia, Africa and the Americas (FOR 210103)', true, '2101.3', true, 2101);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (300003, 'Archaeology of Asia', true, '2101.3.1', true, 210103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (300004, 'Archaeology of Africa', true, '2101.3.2', true, 210103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (300005, 'Archaeology of the Americas', true, '2101.3.3', true, 210103);

insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210104, 'Archaeology of Australia (excl. Aboriginal and Torres Strait Islander) (FOR 210104)', true, '2101.4', true, 2101);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210105, 'Archaeology of Europe, the Mediterranean and the Levant (FOR 210105)', true, '2101.5', true, 2101);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210106, 'Archaeology of New Guinea and Pacific Islands (excl. New Zealand) (FOR 210106)', true, '2101.6', true, 2101);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210107, 'Archaeology of New Zealand (excl. Maori) (FOR 210107)', true, '2101.7', true, 2101);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210108, 'Historical Archaeology (incl. Industrial Archaeology) (FOR 210108)', true, '2101.8', true, 2101);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210109, 'Maori Archaeology (FOR 210109)', true, '2101.9', true, 2101);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210110, 'Maritime Archaeology (FOR 210110)', true, '2101.10', true, 2101);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210199, 'Archaeology not elsewhere classified (FOR 210199)', true, '2101.99', true, 2101);

update culture_keyword set definition = null, label = 'Curatorial and Related Studies', approved = true, index = '2102', selectable = false, parent_id = null where id = 2102;
--insert into culture_keyword (id, label, approved, index, selectable) values (2102, 'Curatorial and Related Studies', true, '2102', false);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210201, 'Archival, Repository and Related Studies (FOR 210201)', true, '2102.1', true, 2102);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210202, 'Heritage and Cultural Conservation (FOR 210202)', true, '2102.2', true, 2102);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210203, 'Materials Conservation (FOR 210203)', true, '2102.3', true, 2102);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210204, 'Museum Studies (FOR 210204)', true, '2102.4', true, 2102);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210205, 'Curatorial and Related Studies not elsewhere classified (FOR 210205)', true, '2102.5', true, 2102);

update culture_keyword set definition = null, label = 'Historical Studies', approved = true, index = '2103', selectable = false, parent_id = null where id = 2103;
--insert into culture_keyword (id, label, approved, index, selectable) values (2103, 'Historical Studies', true, '2103', false);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210301, 'Aboriginal and Torres Strait Islander History (FOR 210301)', true, '2103.1', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210302, 'Asian History (FOR 210302)', true, '2103.2', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210303, 'Australian History (excl. Aboriginal and Torres Strait Islander History) (FOR 210303)', true, '2103.3', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210304, 'Biography (FOR 210304)', true, '2103.4', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210305, 'British History (FOR 210305)', true, '2103.5', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210306, 'Classical Greek and Roman History (FOR 210306)', true, '2103.6', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210307, 'European History (excl. British, Classical Greek and Roman) (FOR 210307)', true, '2103.7', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210308, 'Latin American History (FOR 210308)', true, '2103.8', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210309, 'Maori History (FOR 210309)', true, '2103.9', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210310, 'Middle Eastern and African History (FOR 210310)', true, '2103.10', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210311, 'New Zealand History (FOR 210311)', true, '2103.11', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210312, 'North American History (FOR 210312)', true, '2103.12', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210313, 'Pacific History (excl. New Zealand and Maori) (FOR 210313)', true, '2103.13', true, 2103);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (210314, 'Historical Studies not elsewhere classified (FOR 210314)', true, '2103.14', true, 2103);

insert into culture_keyword (id, label, approved, index, selectable) values (2199, 'Other History and Archaeology', true, '2199', false);
insert into culture_keyword (id, label, approved, index, selectable, parent_id) values (219999, 'History and Archaeology not elsewhere classified (FOR 219999)', true, '2199.1', true, 2199);

-- Step 6) Change the existing culture keywords to the new ones
-- this is not as simple as it seems as duplicate keys will result, which will cause problems. So following will fail
-- update resource_culture_keyword set culture_keyword_id = 210108;

-- rather, assume no entries point to new culture keywords
insert into resource_culture_keyword (resource_id, culture_keyword_id) SELECT DISTINCT resource_id, 210108 FROM resource_culture_keyword;
delete from resource_culture_keyword where culture_keyword_id <> 210108;


-- Step 7) Remove the existing culture keywords
delete from culture_keyword_synonym where culturekeyword_id < 2101; 
delete from culture_keyword where id < 2101;
delete from culture_keyword where id = 10002;
