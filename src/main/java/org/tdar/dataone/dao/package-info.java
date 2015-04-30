@org.hibernate.annotations.NamedQueries({
    @org.hibernate.annotations.NamedQuery(
            name = "query.dataone_list_objects_t1",
            query = "        select new org.tdar.dataone.bean.ListObjectEntry(externalId, 'D1',    id,      dateUpdated, null,       null,      null        , null) from InformationResource res where externalId is not null and (res.dateUpdated between :start and :end or res.dateCreated between :start and :end) and status='ACTIVE' "
                    + "union select new org.tdar.dataone.bean.ListObjectEntry(externalId, 'TDAR',  id,      dateUpdated, null,       null,      null        , null) from InformationResource res where externalId is not null and (res.dateUpdated between :start and :end or res.dateCreated between :start and :end) and status='ACTIVE' "
    ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.dataone_list_objects_t3",
            query = "        select new org.tdar.dataone.bean.ListObjectEntry(externalId, 'D1',    id,      dateUpdated, null,       null,      null        , null) from InformationResource res where externalId is not null and (res.dateUpdated between :start and :end or res.dateCreated between :start and :end) and status='ACTIVE' "
                    + "union select new org.tdar.dataone.bean.ListObjectEntry(externalId, 'TDAR',  id,      dateUpdated, null,       null,      null        , null) from InformationResource res where externalId is not null and (res.dateUpdated between :start and :end or res.dateCreated between :start and :end) and status='ACTIVE' "
                    + "union select new org.tdar.dataone.bean.ListObjectEntry(externalId, 'FILE', irf.id , dateUpdated, fileLength, checksum, latestVersion , mimeType) from InformationResource res inner join res.informationResourceFiles as irf inner join irf.informationResourceFileVersions as irfv where "
                    + "(res.dateUpdated between :start and :end or res.dateCreated between :start and :end) and res.externalId is not null and status='ACTIVE' and irfv.fileVersionType like '%UPLOADED%'"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.dataone_list_logs",
            query = " from LogEntryImpl lg where dateLogged between :start and :end and (:idFilter is NULL or identifier=:idFilter) and (:event is NULL or event=:event)"
    )
})

package org.tdar.dataone.dao;

