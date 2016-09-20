package org.tdar.balk.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.balk.bean.DropboxUserMapping;
import org.tdar.core.bean.entity.TdarUser;

@Component
public class UserDao {

    @Autowired
    private transient SessionFactory sessionFactory;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public DropboxUserMapping findUserForUsername(TdarUser user) {
        String query = "from DropboxUserMapping where lower(username)=lower(:username)";
        Query query2 = getCurrentSession().createQuery(query);
        query2.setParameter("username", user.getUsername());
        query2.setFirstResult(0);
        query2.setMaxResults(1);
        return (DropboxUserMapping) query2.uniqueResult();

    }
}
