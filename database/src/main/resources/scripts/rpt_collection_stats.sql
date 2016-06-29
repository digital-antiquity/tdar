/* $id$
 * simple script for getting view, download count for resources inside of a specific
 * 
 * usage:
 *   psql tdarmetadata tdar -f THISFILE.sql -v id=123456     (replace 123456 with the collection id)
 * 
 */
create temporary table t_resources as 
select * from collection_resource where collection_id  = :id;

create temporary table t_viewcount as 
select 
    count(*) view_count 
from 
    resource_access_statistics ras
        join t_resources tr on (tr.resource_id = ras.resource_id)
;

create temporary table t_dlcount as 
select 
    count(*) dl_count
from
    information_resource_file_download_statistics stats
        join information_resource_file irf on (irf.id = stats.information_resource_file_id)
            join information_resource ir on (ir.id = irf.information_resource_id)
                join t_resources tr on (tr.resource_id = ir.id)
where
    irf.status <> 'DELETED' or irf.status is null
;

select * from t_viewcount, t_dlcount;
