package org.tdar.core.dao;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.dao.base.HibernateBase;

@Component
public class EmailDao extends HibernateBase<Email> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public EmailDao() {
        super(Email.class);
    }

    @SuppressWarnings("unchecked")
    public List<Email> findEmailByGuid(String guid) {
        logger.debug("Searching for messages with guid {} ", guid);
        Query<Email> query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_EMAIL_BY_GUID);
        query.setParameter("guid", guid);
        return query.getResultList();
    }

}
