alter table data_table drop column translated_table_name;
alter table coverage_longitude_latitude rename to latitude_longitude;
alter table coverage_calendar_date rename to calendar_date;
alter table coverage_radiocarbon_date rename to radiocarbon_date;

/* create new keyword tables */
create table culture_keyword (id  bigserial not null, definition text, label varchar(255) not null, approved bool not null, index varchar(255), interval_end int4, interval_start int4, selectable bool not null, primary key (id));

create table geographic_keyword (id  bigserial not null, definition text, label varchar(255) not null, primary key (id));

create table other_keyword (id  bigserial not null, definition text, label varchar(255) not null, primary key (id));

create table site_name_keyword (id  bigserial not null, definition text, label varchar(255) not null, primary key (id));

create table site_type_keyword (id  bigserial not null, definition text, label varchar(255) not null, approved bool not null, index varchar(255), interval_end int4, interval_start int4, selectable bool not null, primary key (id));

create table temporal_keyword (id  bigserial not null, definition text, label varchar(255) not null, primary key (id));

/* many to many tables */
create table resource_culture_keyword (resource_id int8 not null references resource(id), culture_keyword_id int8 not null references culture_keyword(id), primary key (resource_id, culture_keyword_id));
create table resource_geographic_keyword (resource_id int8 not null references resource(id), geographic_keyword_id int8 not null references geographic_keyword(id), primary key (resource_id, geographic_keyword_id));
create table resource_other_keyword (resource_id int8 not null references resource(id), other_keyword_id int8 not null references other_keyword(id), primary key (resource_id, other_keyword_id));
create table resource_site_name_keyword (resource_id int8 not null references resource(id), site_name_keyword_id int8 not null references site_name_keyword(id), primary key (resource_id, site_name_keyword_id));
create table resource_temporal_keyword (resource_id int8 not null references resource(id), temporal_keyword_id int8 not null references temporal_keyword(id), primary key (resource_id, temporal_keyword_id));

/* migrate data from keywords */
insert into geographic_keyword (label) select distinct term from coverage_geographic_term;
insert into resource_geographic_keyword (resource_id, geographic_keyword_id) select cgt.resource_id, gk.id from coverage_geographic_term cgt, geographic_keyword gk where cgt.term=gk.label;

insert into temporal_keyword (label) select distinct term from coverage_temporal_term;
insert into resource_temporal_keyword (resource_id, temporal_keyword_id) select ctt.resource_id, tk.id from coverage_temporal_term ctt, temporal_keyword tk where ctt.term=tk.label;

insert into site_name_keyword (label) select distinct keyword from keyword where dtype='SiteNameSubjectKeyword';
insert into resource_site_name_keyword (resource_id, site_name_keyword_id) select k.resource_id, kk.id from keyword k, site_name_keyword kk where k.keyword=kk.label and k.dtype='SiteNameSubjectKeyword';

insert into other_keyword (label) select distinct keyword from keyword where dtype='OtherSubjectKeyword';
insert into resource_other_keyword (resource_id, other_keyword_id) select k.resource_id, kk.id from keyword k, other_keyword kk where k.keyword=kk.label and k.dtype='OtherSubjectKeyword';

/* now for the hard part: the hierarchical keywords */
create table resource_site_type_keyword (resource_id int8 not null references resource(id), site_type_keyword_id int8 not null references site_type_keyword(id), primary key (resource_id, site_type_keyword_id));

create index ontology_node_interval_start_index on ontology_node(interval_start);
create index ontology_node_interval_end_index on ontology_node(interval_end);

alter table publisher rename to resource_provider_contact;
alter table publisher_id_seq rename to resource_provider_contact_id_seq;
alter table coverage_longitude_latitude_id_seq rename to latitude_longitude_id_seq;
alter table coverage_calendar_date_id_seq rename to calendar_date_id_seq;
alter table coverage_radiocarbon_date_id_seq rename to radiocarbon_date_id_seq;

alter table person add column date_created timestamp;
alter table information_resource_file add column processed bool;
update information_resource_file set processed='t';

alter table document_creator_person rename to document_author;
alter table document_creator_person_id_seq rename to document_author_id_seq;
alter table culture_keyword add constraint unique_culture_keyword unique(label);
alter table geographic_keyword add constraint unique_geographic_keyword unique(label);
alter table other_keyword add constraint unique_other_keyword unique(label);
alter table site_name_keyword add constraint unique_site_name_keyword unique(label);
alter table site_type_keyword add constraint unique_site_type_keyword unique(label);
alter table temporal_keyword add constraint unique_temporal_keyword unique(label);

/* drop old resource_term index tables so that deletes will work */
drop table resource_context_term cascade;
drop table resource_term cascade;
drop table coverage_temporal_term;
drop table coverage_geographic_term;

/* add submitter ids to resource_relationship table */
update resource_relationship set submitter_id=(select r.submitter_id from resource r where r.id=first_id);

update site_type_keyword set selectable = false where index='1';
update site_type_keyword set selectable = false where index='1.1';
update site_type_keyword set selectable = false where index='1.2';
update site_type_keyword set selectable = false where index='2';
update site_type_keyword set selectable = false where index='3';
update site_type_keyword set selectable = false where index='4';
update site_type_keyword set selectable = false where index='5';
update site_type_keyword set selectable = false where index='6';
update site_type_keyword set selectable = false where index='7';

/* remove anasazi as a choice and set it to 'ancestral puebloan' instead */
update resource_culture_keyword set culture_keyword_id = ck.id 
from culture_keyword ck where ck.label = 'Ancestral Puebloan' 
and resource_culture_keyword.culture_keyword_id = (select id from culture_keyword where label = 'Anasazi');

delete from culture_keyword where label = 'Anasazi';
update culture_keyword set index = '9' where label = 'Hohokam';
update culture_keyword set index = '10' where label = 'Mogollon';
update culture_keyword set index = '11' where label = 'Patayan';
update culture_keyword set index = '12' where label = 'Fremont';
update culture_keyword set index = '13' where label = 'Historic';
update culture_keyword set index = '13.1' where label = 'African American';
update culture_keyword set index = '13.2' where label = 'Chinese American';
update culture_keyword set index = '13.3' where label = 'Euroamerican';
update culture_keyword set index = '13.4' where label = 'Japanese American';
update culture_keyword set index = '13.5' where label = 'Native American';
update culture_keyword set index = '13.6' where label = 'Spanish';

/******************************************************************************
 * START: Modify site_type_keywords.
 *****************************************************************************/

start transaction;

-- create temp tables for old data
create temporary table site_type_keyword_temp on commit drop as 
	select * from site_type_keyword;

create temporary table resource_site_type_keyword_temp on commit drop as 
	select * from resource_site_type_keyword;

-- clear tables for new data
truncate site_type_keyword cascade;

-- add new data
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1', 'Domestic Structure or Architectural Complex', 'The locations and/or archaeological remains of a building or buildings used for human habitation. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.1', 'Settlements', 'Locations, or the remains of multiple structures or features, that were inhabited by humans in the past. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.1.1', 'Encampment', 'A relatively small, short-term human habitation occupied by a relatively small group.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.1.2', 'Hamlet / village', 'Relatively small, self-contained groups of dwellings and associated structures providing shelter and a home base for its human inhabitants.  Typically occupied for a number of years or decades, and in some cased for centuries.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.1.3', 'Town / city', 'Larger settlements with more dwellings and a wide variety of other kinds of structures.  These settlements typically have internally organized infrastructure of streets or walkways and water and waste-disposal systems. Typically occupied for decades or centuries.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2', 'Domestic Structures', 'Locations, or the remains of buildings that were inhabited by humans in the past. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.1', 'Brush structure', 'A temporary structure, made out of brush, with a roof and walls, built to provide shelter for occupants or contents (e.g., wikieup, ki).', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.2', 'Cave', 'Natural hollow or opening beneath earth''s surface, with an aperture to the surface, showing evidence of human use. Caves may or may not have been modified for or by human use. A cave differs from a rockshelter in depth, penetration, and the constriction of the opening.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.3', 'House', 'A relatively small dwelling occupied by a single nuclear or extended family. May appear archaeologically as a stone foundation or pattern of post molds.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.4', 'House mound', 'A slightly raised, mounded area of earth or rock built to provide a platform for a single domestic structure.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.5', 'Wattle & daub (jacal) structure', 'The remains of a small surface structure constructed of brush (wattle) and mud (daub).', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.6', 'Long house', 'A long, relatively narrow multi-family dwelling, best known as a typical village dwelling used by Iroquois Confederacy tribes.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.7', 'Pit house / earth lodge', 'Semi-subterranean habitation that may have an oval, round or rectangular shape. Typically with a dome-like covering constructed using a wood frame covered by branches, reeds, other vegetation and earth.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.8', 'Room block / compound / pueblo', 'Remains of a contiguous, multi-room habitation structure. Typically constructed of stone, mud brick or adobe. Usually manifests archaeologically as a surface mound of construction debris, sometimes with visible wall alignments.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.9', 'Rock shelter', 'Overhang, indentation, or alcove formed naturally by rock fall or in a rock face; generally not of great depth. Rockshelters may or may not be modified with structural elements for human use.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.10', 'Shade structure / ramada', 'All temporary shelters (e.g. lean-tos, windbreaks, brush enclosures, sun shades etc.).', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.11', 'Tent ring / tipi ring', 'Circular pattern (sometimes outlined with rocks) left when a tipi or tent is dismantled.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.12', 'Platform mound', 'A relatively high (over 1 meter), flat-topped mound, frequently constructed in several stages on which one or more structures were placed. Platform mounds are constructed using soil, shell, or refuse. They may incorporate earlier, filled-in structures in their substructure.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.13', 'Shell mound', 'A low mounded area of shell built to provide a platform for one or more domestic structures.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.14', 'Wigwam / wetu', 'Relatively small dwellings, typically circular or rectangular and about 3 meters tall, made of wooden frames with bases dug into the soil and covered with woven mats or sheets of birchbark. The frames could be shaped like a dome, a cone, or a rectangle with an arched roof.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('1.2.15', 'Plank house', 'Relatively large, rectangular dwellings made of long, flat planks of cedar wood lashed to a substantial wooden frame. Typical of permanent villages of Indian tribes living in the American Northwest during the historic contact period and earlier.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2', 'Resource Extraction/Production/Transportation Structure or Features', 'The locations and/or archaeological remains of features or sites related to resource extraction, commerce, industry, or transportation. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.1', 'Agricultural or Herding', 'Locations, or the remains of features or facilities, that were used for  horticulture, agriculture, or animal husbandry. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.1.1', 'Agricultural field or field feature', 'An area of land, often enclosed, used for cultivation. Fields are not necessarily formally bounded, and may be identifiable based on diagnostic features such as boundary markers or raised beds.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.1.2', 'Canal or canal feature', 'Ditch or interrelated group of ditches, acequias, head gates, and drains that constitute an irrigation system for individual watering and irrigation features.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.1.3', 'Corral', 'An enclosure for confining livestock. May be constructed of any material and incorporate natural features or vegetation as part of the enclosure.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.1.4', 'Reservoir', 'Natural or artificial lake in which water can be stored for future use.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.1.5', 'Terrace', 'An artificially created, more or less level area cut into the side of a hill. The edge may be bordered by stone or other material to prevent erosion.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.1.6', 'Water control feature', 'A device which controls the flow of water, particularly run-off. Includes check dams, flumes, gabions, head gates, drop structures, and riprap.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.2', 'Commercial or Industrial Structures', 'Locations, or the remains of features or facilities, that were used for commercial or industrial purposes. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.2.1', 'Factory / workshop', 'A relative large structure in which goods were manufactured or prepared for commercial distribution.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.2.2', 'Mill', 'A relatively small facility (one or a few rooms) for processing grain, wood, or other materials.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.2.2.1', 'Grist mill', 'A mill for processing grain, typically powered by water or wind.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.2.2.2', 'Saw mill', 'A mill for processing timber or wood.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.3', 'Fish trap / weir', 'A structure designed for catching fish. Sometimes constructed as a fence or enclosure of wooden stakes or stones, placed in a river, lake, wetland or tidal estuary.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.4', 'Hunting / Trapping', 'Locations, or the remains of features or facilities, that were used for hunting or trapping animals. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.4.1', 'Butchering / kill site', 'Concentration of faunal remains resulting from human hunting activity.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.4.2', 'Hunting blind', 'Small, unroofed structure expediently constructed out of natural rock and/or wood as camouflage.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.4.3', 'Large game jump', 'A cliff or other natural drop off where large game can be stampeded over the edge.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.5', 'Mine', 'Locations used for the extraction of metals, ores, minerals, or other materials.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.5.1', 'Mine tunnels', 'An excavation made in the earth for the purpose of digging out metallic ores, coal, salt, precious stones or other resources. Includes portals, adits, vent shafts, prospects, and haulage tunnels.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.5.2', 'Mine-related structures', 'The remains of facilities or equipment, usually above ground, used for processing or storing mined materials.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.6', 'Quarry', 'Outcrops of lithic material that have been mined or otherwise utilized to obtain lithic raw materials.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.7', 'Road, Trail, and Related Structures or Features', 'The archaeological remains of identifiable paths or routes between two or more locations. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.7.1', 'Bridge', 'A structure with one or more intervals under it to span a river or other space.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.7.2', 'Causeway', 'A road or pathway constructed from packed earth, stone, or shell, usually across a wetland or small water body.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.7.3', 'Linear feature', 'Identifiable, linear archaeological remains of unknown function.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.7.4', 'Railroad', 'Segment(s) of railroad tracks or railroad bed.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.7.5', 'Road', 'A prepared, formal way used for the passage of humans, animals, and/or vehicles. These include examples such as Chacoan roads.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('2.7.6', 'Trail', 'An informal foot path used for the passage of humans, animals, and/or vehicles, defined and worn by use without formal construction or maintenance.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('3', 'Funerary and Burial Structures or Features', 'The archaeological features or locations used for human burial or funerary activities. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('3.1', 'Cemetery', 'A formal location for burying the dead.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('3.2', 'Burial mound', 'An artificial mound constructed using earth, shell, or stone for the purpose of holding one or more burials. Frequently containing several episodes of construction and burials from different periods of time.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('3.3', 'Charnel house', 'A structure in which recently deceased human bodies were placed so that the flesh and other soft tissue would decompose prior to final interment of the remains.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('3.4', 'Isolated burial', 'A location containing a human burial, spatially removed from other archaeological evidence.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('3.5', 'Ossuary', 'A secondary burial of multiple individuals.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('3.6', 'Burial pit', 'An unmarked human interment in a subterranean pit.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('3.7', 'Tomb', 'A prepared, architecturally distinctive structure, normally sub-surface, often containing multiple interments.  Use for features such as shaft tombs.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4', 'Non-Domestic Structures', 'The locations and/or archaeological remains of a building or buildings used for purposes other than human habitation. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.1', 'Ball Court', 'An unroofed structure associated with the playing of the Mesoamerican ball game, found in the American southwest and parts of Mesoamerica.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.2', 'Church / religious structure', 'Buildings, or the archaeological remains of features or facilities, that were used for religious purposes. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.2.1', 'Ancient church / religious structure', 'Remains of a prehistoric building or location designed for public religious services.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.2.2', 'Historic church / religious structure', 'Remains of a historic building or location designed for public religious services.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.3', 'Communal / public structure', 'Locations, or the remains of buildings that were associated with communal or public activities.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.3.1', 'Ancient communal / public structure', 'Specified area containing evidence that is associated with prehistoric communal or public activity.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.3.2', 'Historic communal / public structure', 'Specified area containing evidence that is associated with historic communal or public activity.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.4', 'Great House / Big House', 'A multi-story building with massive masonry or adobe walls, found in the Chacoan and Hohokam regions of the American southwest.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.5', 'Governmental structure', 'Locations, or the remains of buildings that were associated with governmental activities.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.5.1', 'Ancient governmental structure', 'Specified area containing evidence that is associated with prehistoric governmental activity.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.5.2', 'Historic governmental structure', 'Specified area containing evidence that is associated with historic governmental activity.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.6', 'Kiva / Great Kiva', 'Circular or rectangular ceremonial structure. May be subterranean or part of a surface room block.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.7', 'Military structure', 'Military-related structure constructed for various purposes (personnel barracks, testing, aircraft storage or landing, etc.).', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.8', 'Mound / Earthwork', 'An above ground construction of earth, shell or other material, undifferentiated as to function.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.8.1', 'Building substructure', 'An above ground prepared surface on which a non-residential structure is built.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.8.2', 'Ancient earthwork', 'A non-residential cultural construction made from earth, shell or other materials, often formed to enclose or demarcate an area, or, in the case of causeways, to link areas. Examples include shell rings.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.8.3', 'Military earthwork', 'A defensive construction made from earth, shell or other materials.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.8.4', 'Geometric / effigy / zoomorphic mound', 'An above ground construction of earth, shell or other material, built in the shape of geometric, animal or other symbolic forms. Prominent examples include Effigy Mounds National Monument and Serpent Mound.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.8.5', 'Shell ring', 'A large, circular area defined by a perimeter of mounded shell (often several meters in height), for example, Archaic Period shell ring sites in the American southeast.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.9', 'Palace', 'A large and/or ornate building, normally associated with a high ranking family or individual.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.10', 'Palisade', 'An enclosure, constructed of timbers or posts driven into ground, or otherwise walled.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.11', 'Plaza', 'An area which may be partially or completely enclosed by structural remains (standing or collapsed), used for community activities.  May contain temporary structures (e.g. sun shades or ramadas) as well as special activity areas (e.g. milling bins, hearths).', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.12', 'Pyramid', 'A massive structure, typically with triangular outer surfaces that converge at the top. Often flat-topped to accommodate public gatherings and/or buildings.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.13', 'Stairway', 'A series of steps allowing access to a different level. Use for toe/hand holds, stairs, ladders, etc.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.14', 'Structure', 'Architectural remnant of a building of unknown form or function.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.14.1', 'Ancient structure', 'Architectural remnant of a prehistoric building of unknown form or function. Use more specific term if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.14.2', 'Historic structure', 'Architectural remnant of a historic building of unknown form or function. Use more specific term if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.15', 'Sweat house / sweat lodge', 'Small enclosure or hut used for steam baths, usually ephemeral in construction. Often with fire-cracked rock and/or hearths in association.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('4.16', 'Temple', 'Monumental architecture constructed from stone or other materials, and used for religious and/or political purposes.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5', 'Archaeological Feature', 'A localized area containing evidence of human activity. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.1', 'Artifact Scatter', 'Prehistoric lithic and/or ceramic scatters with no features.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.2', 'Cairn', 'Mound or stack of rocks used to mark significant locations (e.g., boundaries or claims).', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.3', 'Fence', 'A structure that creates a boundary, barrier, or enclosure. Construction materials can vary widely and may include unmodified natural materials (such as brush).', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.4', 'Hearth', 'Discolored area of soil, often including charcoal, ash deposits or fire cracked rock, exhibiting evidence of use in association with fire.  May be bounded (e.g., rock ring) or ill-defined.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.5', 'Isolated artifact', 'A find spot containing a single artifact.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.6', 'Isolated feature', 'A find spot containing a single cultural feature.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.7', 'Kiln', 'Oven used to bake food, fire pottery, or thermally alter other materials (e.g., bricks, lithic materials).', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.8', 'Midden', 'An archaeological refuse deposit containing the broken or discarded remains of human activities. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.8.1', 'Burned rock midden', 'A large, dense concentrations, often mounded, of fire cracked rock (FCR), usually associated with large scale plant processing. Although other cultural materials may be present in the midden, FCR is usually predominant.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.8.2', 'Sheet midden', 'A surficial archaeological deposit containing discarded artifacts and other cultural materials. Midden deposits normally contain ashy or charcoal-stained sediments, and domestic-related items such as sherds, lithic debitage, and bone.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.8.3', 'Shell midden', 'An archaeological deposit composed primarily of discarded mollusk shells.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.8.4', 'Trash midden', 'A substantial concentration of refuse, built up as a result of multiple episodes of deposition.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.9', 'Milling feature', 'A facility made or used for grinding or processing plant materials. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.9.1', 'Bedrock grinding feature', 'A pecked or ground concavity in a large boulder or outcrop, including both bedrock mortar and bedrock metate.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.9.2', 'Milling Bin', 'An enclosed container used for milling plant material. May be above ground or partially or completely underground.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.10', 'Pit', 'A discrete excavation directly attributable to human activity. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.10.1', 'Refuse pit', 'A discrete excavation directly attributable to human activity that was used for the disposal of discarded artifacts, ecofacts and other cultural materials.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.10.2', 'Roasting pit / oven / horno', 'An enclosed space used to heat objects placed within its bounds. Includes earth ovens, oven pits, mud ovens, and bread ovens.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.10.3', 'Storage pit', 'A discrete excavation directly attributable to human activity used for storing artifacts, ecofacts and other cultural materials.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.11', 'Post hole / post mold', 'One or more upright posts, remains of posts, or sockets usually associated with a larger feature or structure such as a building, fence, corral, stockade, pen, etc.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('5.12', 'Rock alignment', 'Group of rocks which appear to have some cultural association. Use for possible walls, wall-like phenomena, human produced architectural oddities, rock piles, etc.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('6', 'Rock Art', 'Designs, whether carved, scraped, pecked or painted, applied to free-standing stones, cave walls, or the earth’s surface. Use more specific term(s) if possible.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('6.1', 'Intaglio / geoglyph', 'Designs created on the ground surface by arranging rocks or other materials, or by scraping or altering the earth surface. Usually on a large scale.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('6.2', 'Petroglyph', 'Design scratched, pecked, or scraped into a rock surface.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('6.3', 'Pictograph', 'Design drawn in pigment upon an unprepared or ground rock surface.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('7', 'Water-related', 'The locations and/or archaeological remains of ships, boats, or other vessels, or the facilities related to shipping or sailing.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('7.1', 'Shipping-related structure', 'The remains of facilities or equipment related to boats, ships, or shipping. Use for dock, wharf etc.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('7.2', 'Shipwreck', 'The remains of a ship, boat or other vessel.', true, true);
insert into site_type_keyword (index, label, definition, approved, selectable) values ('7.3', 'Submerged aircraft', 'The underwater remains of an aircraft.', true, true);

-- re-add all user supplied keywords and their resource mappings
insert into site_type_keyword select * from site_type_keyword_temp where approved = false;

-- update references to site_type_keywords based on label 
insert into resource_site_type_keyword select distinct rkt.resource_id, k.id
from resource_site_type_keyword_temp as rkt, site_type_keyword_temp as ktemp, site_type_keyword as k 
where rkt.site_type_keyword_id = ktemp.id and ktemp.label ~* k.label;
	
-- manually update references to those site_type_keywords whose labels changed
create function remapSiteTypeFromLabel (oldLabel text, newLabel text) returns void as $$
    insert into resource_site_type_keyword select rkt.resource_id, k.id
	from resource_site_type_keyword_temp as rkt, site_type_keyword_temp as ktemp, site_type_keyword as k 
	where ktemp.label ~* $1 and k.label ~* $2 and rkt.site_type_keyword_id = ktemp.id;
$$ language sql;

select remapSiteTypeFromLabel('Settlement', 'Settlements');
select remapSiteTypeFromLabel('Pit house / pit structure', 'Pit house / earth lodge');
select remapSiteTypeFromLabel('Room block / pueblo', 'Room block / compound / pueblo');
select remapSiteTypeFromLabel('Rock shelter / cave', 'Rock shelter');
select remapSiteTypeFromLabel('Tent / Tipi Ring', 'Tent ring / tipi ring');
select remapSiteTypeFromLabel('Wattle and daub(jacal) structure', 'Wattle and daub (jacal) structure');
select remapSiteTypeFromLabel('Agricultural', 'Agricultural or Herding');
select remapSiteTypeFromLabel('Agricultural Field or Field Feature (e.g. boundary marker)', 'Agricultural field or field feature');
select remapSiteTypeFromLabel('Canal or Canal Feature( e.g. headgate)', ' Canal or canal feature');
select remapSiteTypeFromLabel('Water Control Feature (e.g. check dam)', 'Water control feature');
select remapSiteTypeFromLabel('Factory/workshop', 'Factory / workshop');
select remapSiteTypeFromLabel('Fishing', 'Fish trap / weir');
select remapSiteTypeFromLabel('Fish trap', 'Fish trap / weir');
select remapSiteTypeFromLabel('Ladder', 'Fish trap / weir');
select remapSiteTypeFromLabel('Wier', 'Fish trap / weir');
select remapSiteTypeFromLabel('Mine (Tunnels)', 'Mine');
select remapSiteTypeFromLabel('Quarry (Surface)', 'Quarry');
select remapSiteTypeFromLabel('Road / Trail', 'Road, Trail, and Related Structures or Features');
select remapSiteTypeFromLabel('Trail (including Chacoan Road)', 'Trail');
select remapSiteTypeFromLabel('Funerary / Burial', 'Funerary and Burial Structures or Features');
select remapSiteTypeFromLabel('Charnal house', 'Charnel house');
select remapSiteTypeFromLabel('Other Structures', 'Non-Domestic Structures');
select remapSiteTypeFromLabel('Communal/Public Structure (interpretive)', 'Settlements');
select remapSiteTypeFromLabel('Communal/Public Structure (interpretive)', 'Settlements');
select remapSiteTypeFromLabel('Historic Communal / Public Structure (interpretive)', 'Settlements');
select remapSiteTypeFromLabel('Great/Big House (Chaco and Hohokam)', 'Great House / Big House');
select remapSiteTypeFromLabel('Stairway (MesoAmerican)', 'Stairway');
select remapSiteTypeFromLabel('Structure (architectural remnant of building of unknown form or function)', 'Structure');
select remapSiteTypeFromLabel('Temple (MesoAmerican)', 'Temple');
select remapSiteTypeFromLabel('Other Archaeological Features', 'Archaeological Feature');
select remapSiteTypeFromLabel('Bedrock grinding', 'Bedrock grinding feature');
select remapSiteTypeFromLabel('Bedrock mortar', 'Bedrock grinding feature');
select remapSiteTypeFromLabel('Bedrock metate', 'Bedrock grinding feature');
select remapSiteTypeFromLabel('Roasting pit / horno', 'Roasting pit / oven / horno');
select remapSiteTypeFromLabel('Oven', 'Roasting pit / oven / horno');
select remapSiteTypeFromLabel('Shipping-related structure (e.g. dock or wharf)', 'Shipping-related structure');

drop function remapSiteTypeFromLabel (oldLabel text, newLabel text);

commit;

/******************************************************************************
 * END: Modify site_type_keywords.
 *****************************************************************************/

-- r707 splitting citation table into separate tables due to bug in postgres-jdbc and hibernate
create table source_collection (id bigserial, fedora_pid varchar(255), text varchar(1024), resource_id bigint references resource(id));
create table related_comparative_collection (id bigserial, fedora_pid varchar(255), text varchar(1024), resource_id bigint references resource(id));
insert into source_collection (text, resource_id) select text, resource_id from citation where dtype='SourceCitation';
insert into related_comparative_collection (text, resource_id) select text, resource_id from citation where dtype='RelatedComparativeCollection';

