create index geom_county on us_counties_wgs84 using GIST (the_geom);
create index geom_admin on admin1_wgs84 using GIST (the_geom);
create index geom_continent on continents_wgs84 using GIST (the_geom);
create index geom_country on country_wgs84 using GIST (the_geom);
create index us_fips on us_counties_wgs84 (fips);
create index us_fips_short on us_counties_wgs84 ( substring(fips from 0 for 2));

-- create codes
update tdar.country_wgs84 set tdar_code=iso_3digit;
update admin1_wgs84 set tdar_code = fips_admin;
update us_counties_wgs84 set tdar_code = iso_2digit || state_fips || cnty_fips;
update continents_wgs84 set tdar_code = iso_code;
