@org.hibernate.annotations.NamedQueries({
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.resources",
            query = "select res from Resource res left join res.resourceCollections as rc left join rc.parentIds parentId where (res.dateUpdated between :start and :end or res.dateCreated between :start and :end) and res.status='ACTIVE' and (:collectionId=-1L or rc.id=:collectionId or parentId=:collectionId) order by res.dateUpdated asc"
            ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.collections",
            query = "select c from ResourceCollection c where c.type!='INTERNAL' and (c.dateUpdated between :start and :end or c.dateCreated between :start and :end) and hidden=false order by c.dateUpdated asc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.people",
            query = "select p from Person p where (p.dateUpdated between :start and :end or p.dateCreated between :start and :end) and hidden=false and status='ACTIVE' order by p.dateUpdated asc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.institutions",
            query = "select i from Institution i where (i.dateUpdated between :start and :end or i.dateCreated between :start and :end) and hidden=false and status='ACTIVE' order by i.dateUpdated asc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.resources_count",
            query = "select count(res) from Resource res left join res.resourceCollections as rc left join rc.parentIds parentId where (res.dateUpdated between :start and :end or res.dateCreated between :start and :end) and res.status='ACTIVE' and (:collectionId=-1L or rc.id=:collectionId or parentId=:collectionId)"
            ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.collections_count",
            query = "select count(c) from ResourceCollection c where c.type!='INTERNAL' and (c.dateUpdated between :start and :end or c.dateCreated between :start and :end) and hidden=false"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.people_count",
            query = "select count(p) from Person p where (p.dateUpdated between :start and :end or p.dateCreated between :start and :end) and hidden=false and status='ACTIVE'"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.institutions_count",
            query = "select count(i) from Institution i where (i.dateUpdated between :start and :end or i.dateCreated between :start and :end) and hidden=false and status='ACTIVE'"
    )
})

package org.tdar.oai.dao;

