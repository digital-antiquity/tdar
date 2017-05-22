package org.tdar.core.dao.base;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResource;

@Component("doiDao")
public class DoiDao extends Dao.HibernateBase<InformationResource> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public DoiDao() {
        super(InformationResource.class);
    }

    public InformationResource findByDoi(String doi) {
        Query<InformationResource> query = getCurrentSession().createNamedQuery(QUERY_BY_DOI, InformationResource.class);
        query.setParameter("doi", doi);
        return (InformationResource) query.getSingleResult();
    }

}
