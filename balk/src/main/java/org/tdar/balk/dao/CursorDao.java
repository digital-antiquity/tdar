package org.tdar.balk.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.GenericDao;

import org.tdar.balk.bean.PollType;

@Component
public class CursorDao {

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private transient SessionFactory sessionFactory;

    public String getLatestCursorFor(PollType type) {
        String query = "select cursor from DropboxState where type=:type order by lastPolled desc";
        Query query2 = getCurrentSession().createQuery(query);
        query2.setParameter("type", type);
        query2.setFirstResult(0);
        query2.setMaxResults(1);
        return (String) query2.uniqueResult();
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
