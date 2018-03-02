-- tdargis upgrade SQL for Obsidian

-- adding an area column, and local "code" for shapes. 
alter table admin1_wgs84 add column area decimal;
alter table admin1_wgs84 add column tdar_code varchar(10);
UPDATE admin1_wgs84 set area =ST_area(the_geom);

alter table continents_wgs84 add column area decimal;
alter table continents_wgs84 add column tdar_code varchar(10);
UPDATE continents_wgs84 set area =ST_area(the_geom);

alter table country_wgs84 add column area decimal;
alter table country_wgs84 add column tdar_code varchar(10);
UPDATE country_wgs84 set area =ST_area(the_geom);

alter table us_counties_wgs84 add column area decimal;
alter table us_counties_wgs84 add column tdar_code varchar(10);
UPDATE us_counties_wgs84 set area =ST_area(the_geom);