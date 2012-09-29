package org.tdar.core.dao;

import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * $Id$
 * 
 * Manages access to the HibernateSearch API.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Component("hibernateSearchDao")
public class HibernateSearchDao {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public FullTextSession getFullTextSession() {
        return Search.getFullTextSession(sessionFactory.getCurrentSession());
    }

}
