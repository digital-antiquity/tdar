@NamedQueries({
    @NamedQuery(
            name = "item.findbyparent",
            query = "from DropboxDirectory where dropboxId not like 'deleted%' and  lower(path)=lower(:path) and archived is false order by id desc"),
    @NamedQuery(
            name = "item.findtoupload",
            query = "from DropboxFile df where lower(path) like lower('%/Upload to tDAR/%') and not exists (select tr from TdarReference tr where df.dropboxId=tr.dropboxId and df.dropboxId not like 'deleted%')"
        ),
    @NamedQuery(
            name = "user.findbyusername",
            query = "from DropboxUserMapping map where lower(map.username)=lower(:username)"
            ),
    @NamedQuery(
            name = "user.findbyemail",
            query = "from DropboxUserMapping map where (:id is not null and lower(map.username)=lower(:id)) or (:email is not null and lower(map.email)=lower(:email))"
            ),
    @NamedQuery(
            name = "item.findbypath",
            query = "from DropboxFile where lower(path) like :path and archived is false"
            ),
    @NamedQuery(
            name = "item.findfilebyid",
            query = "from DropboxFile Item where dropbox_id=:id"
            ),
    @NamedQuery(
            name = "item.finddirbyid",
            query = "from DropboxDirectory Item where dropbox_id=:id"
            ),
    @NamedQuery(
            name = "item.findtoplevel",
            query = "select name from DropboxDirectory where parentId in (select dropboxId from DropboxDirectory where lower(name)=lower(:path) ) and dropboxId not like 'deleted%' and archived is false"
            )
    })
package org.tdar.balk.dao;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
