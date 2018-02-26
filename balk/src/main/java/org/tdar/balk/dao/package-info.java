/* FIXME: still unsupported
@org.hibernate.annotations.NamedNativeQueries({
    @org.hibernate.annotations.NamedNativeQuery(
            name = TdarNamedQueries.QUERY_DASHBOARD,
            query = TdarNamedQueries.QUERY_SQL_DASHBOARD
            ),
})
 */

@NamedQueries({
        //    select distinct true from collection_resource, collection, authorized_user where user_id =2 and general_permissions='MODIFY_RECORD' and authorized_user.resource_collection_id=collection.id and collection_resource.collection_id=collection.id
        //    and (resource_id in(657) or resource_id in (2454));
    @NamedQuery(
            name = "item.findbyparent",
            query = "from DropboxDirectory where dropboxId not like 'deleted%' and  lower(path)=lower(:path) and archived is false order by id desc"),
    @NamedQuery(
            name = "item.findtoupload",
            query = "from DropboxFile df where lower(path) like lower('%/Upload to tDAR/%') and not exists (select tr from TdarReference tr where df.dropboxId=tr.dropboxId and df.dropboxId not like 'deleted%')"
        )
    })
package org.tdar.balk.dao;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
