package org.tdar.balk.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.balk.bean.PollType;
import org.tdar.core.dao.GenericDao;

@Component
public class CursorDao {

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private transient SessionFactory sessionFactory;

    public String getLatestCursorFor(PollType type) {
        String query = "select cursor from DropboxState where type=:type order by lastPolled desc limit 1";
        Query query2 = getCurrentSession().createQuery(query);
        query2.setParameter("type", type);
        return (String) query2.uniqueResult();
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
