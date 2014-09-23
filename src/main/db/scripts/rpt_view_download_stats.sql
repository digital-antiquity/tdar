create temporary table cg_stats(id bigint);
insert into cg_stats (select distinct resource_id from collection_resource where collection_id =13947);

-- resource_access_stats
select count(*) as total_views,
   (select count(*) from resource_access_statistics ras2014 where ras2014.resource_id=ras.resource_id and date_part('year',date_accessed) = 2014) as y14,
   (select count(*) from resource_access_statistics ras2013 where ras2013.resource_id=ras.resource_id and date_part('year',date_accessed) = 2013) as y13,
   (select count(*) from resource_access_statistics ras2012 where ras2012.resource_id=ras.resource_id and date_part('year',date_accessed) = 2012) as y12,
   (select count(*) from resource_access_statistics ras2011 where ras2011.resource_id=ras.resource_id and date_part('year',date_accessed) = 2011) as y11,
   (select count(*) from resource_access_statistics ras2010 where ras2010.resource_id=ras.resource_id and date_part('year',date_accessed) = 2010) as y10,
   (select count(*) from resource_access_statistics ras2009 where ras2009.resource_id=ras.resource_id and date_part('year',date_accessed) = 2009) as y09,
   (select count(*) from resource_access_statistics ras2008 where ras2008.resource_id=ras.resource_id and date_part('year',date_accessed) = 2008) as y08,
   left(resource.title,80), left(resource.status,1) as status, 'http://core.tdar.org/' || lower(resource.resource_type) || '/' || resource_id as URL
   from resource, resource_access_statistics ras where resource.id=resource_id and resource_id in (select id from cg_stats) group by resource_id, title, status, resource_type order by count(*) desc;

-- download_stats
select count(*) as total_downloads,
   (select count(*) from information_resource_file_download_statistics ds2014 where ds2014.information_resource_file_id=ds.information_resource_file_id and date_part('year',date_accessed) = 2014) as y14,
   (select count(*) from information_resource_file_download_statistics ds2013 where ds2013.information_resource_file_id=ds.information_resource_file_id and date_part('year',date_accessed) = 2013) as y13,
   (select count(*) from information_resource_file_download_statistics ds2012 where ds2012.information_resource_file_id=ds.information_resource_file_id and date_part('year',date_accessed) = 2012) as y12,
   (select count(*) from information_resource_file_download_statistics ds2011 where ds2011.information_resource_file_id=ds.information_resource_file_id and date_part('year',date_accessed) = 2011) as y11,
   (select count(*) from information_resource_file_download_statistics ds2010 where ds2010.information_resource_file_id=ds.information_resource_file_id and date_part('year',date_accessed) = 2010) as y10,
   (select count(*) from information_resource_file_download_statistics ds2009 where ds2009.information_resource_file_id=ds.information_resource_file_id and date_part('year',date_accessed) = 2009) as y09,
   (select count(*) from information_resource_file_download_statistics ds2008 where ds2008.information_resource_file_id=ds.information_resource_file_id and date_part('year',date_accessed) = 2008) as y08,
   information_resource_file_id as irf_id,
   (select left(title,80) from resource where id in (select information_resource_id from information_resource_file where information_resource_file.id=information_resource_file_id)) as title,
   (select left(status,1) from resource where id in (select information_resource_id from information_resource_file where information_resource_file.id=information_resource_file_id)) as status,
   (select 'http://core.tdar.org/' || lower(resource_type) || '/' || id from resource where id in (select information_resource_id from information_resource_file where information_resource_file.id=information_resource_file_id )) as URL
    from information_resource_file_download_statistics ds where information_resource_file_id in (select id from information_resource_file where information_resource_id in (select id from cg_stats)) group by information_resource_file_id order by count(*) desc;

