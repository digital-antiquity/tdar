package org.tdar.balk.dao;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.NamedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.balk.bean.PollType;
import org.tdar.core.dao.base.GenericDao;

@Component
@NamedQuery(
        name = "cursor.latest",
        query = "select cursor from DropboxState where type=:type order by lastPolled desc"
        )
public class CursorDao {


    @Autowired
    private transient SessionFactory sessionFactory;

    public String getLatestCursorFor(PollType type) {
        String query = "cursor.latest";
        Query query2 = getCurrentSession().createQuery("select cursor from DropboxState where type=:type order by lastPolled desc");
        query2.setParameter("type", type);
        query2.setFirstResult(0);
        query2.setMaxResults(1);
        return (String) query2.getSingleResult();
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
