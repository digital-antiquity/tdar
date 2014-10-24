create temporary table cg_stats(id bigint);
insert into cg_stats (select distinct resource_id from collection_resource where collection_id =13947);

select id, substr(title,0,50) as "Title", status, resource_type,  'http://core.tdar.org/' || lower(resource.resource_type) || '/' || resource.id as URL, 
           (select sum(count) from resource_access_day_agg where resource_id=resource.id and year='2010') as "2010 Views", 
           (select sum(count) from resource_access_day_agg where resource_id=resource.id and year='2011') as "2011 Views", 
           (select sum(count) from resource_access_day_agg where resource_id=resource.id and year='2012') as "2012 Views", 
           (select sum(count) from resource_access_day_agg where resource_id=resource.id and year='2013') as "2013 Views", 
           (select sum(count) from resource_access_day_agg where resource_id=resource.id and year='2014') as "2014 Views",
           (select sum(count) from resource_access_day_agg where resource_id=resource.id and year='2015') as "2015 Views",
           (select sum(count) from resource_access_day_agg where resource_id=resource.id) as "Total Views",
           (select sum(count) from file_download_day_agg, information_resource_file where information_resource_id=resource.id and information_resource_file.id=file_download_day_agg.information_resource_file_id and year='2010' group by year) as "2010 Downloads",
           (select sum(count) from file_download_day_agg, information_resource_file where information_resource_id=resource.id and information_resource_file.id=file_download_day_agg.information_resource_file_id and year='2011' group by year) as "2011 Downloads",
           (select sum(count) from file_download_day_agg, information_resource_file where information_resource_id=resource.id and information_resource_file.id=file_download_day_agg.information_resource_file_id and year='2012' group by year) as "2012 Downloads",
           (select sum(count) from file_download_day_agg, information_resource_file where information_resource_id=resource.id and information_resource_file.id=file_download_day_agg.information_resource_file_id and year='2013' group by year) as "2013 Downloads",
           (select sum(count) from file_download_day_agg, information_resource_file where information_resource_id=resource.id and information_resource_file.id=file_download_day_agg.information_resource_file_id and year='2014' group by year) as "2014 Downloads",
           (select sum(count) from file_download_day_agg, information_resource_file where information_resource_id=resource.id and information_resource_file.id=file_download_day_agg.information_resource_file_id and year='2015' group by year) as "2015 Downloads",
           (select sum(count) from file_download_day_agg, information_resource_file where information_resource_id=resource.id and information_resource_file.id=file_download_day_agg.information_resource_file_id) as "Total Downloads"
       from resource where id in (select * from cg_stats);