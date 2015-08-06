@org.hibernate.annotations.NamedQueries({
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.resources",
            query = "from Resource res where (res.dateUpdated between :start and :end or res.dateCreated between :start and :end) and res.status='ACTIVE' order by dateUpdated asc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.collections",
            query = "from ResourceCollection c where c.type!='INTERNAL' and (c.dateUpdated between :start and :end or c.dateCreated between :start and :end) and hidden=false order by dateUpdated asc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.people",
            query = "from Person p where (p.dateUpdated between :start and :end or p.dateCreated between :start and :end) and hidden=false and status='ACTIVE' order by dateUpdated asc"
    ),
    @org.hibernate.annotations.NamedQuery(
            name = "query.oai.institutions",
            query = "from Institution i where (i.dateUpdated between :start and :end or i.dateCreated between :start and :end) and hidden=false and status='ACTIVE' order by i.dateUpdated asc"
    )
})

package org.tdar.oai.dao;

