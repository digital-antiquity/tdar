-- 2013-03-17
create table geospatial (
    id int8 not null,
    primary key (id)
);

alter table geospatial 
    add constraint FK5B47609397FF4D65 
    foreign key (id) 
    references dataset;
