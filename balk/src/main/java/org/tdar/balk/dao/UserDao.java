package org.tdar.balk.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.balk.bean.DropboxUserMapping;
import org.tdar.core.bean.entity.TdarUser;

import com.dropbox.core.v2.users.BasicAccount;

@Component
public class UserDao {

    @Autowired
    private transient SessionFactory sessionFactory;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public DropboxUserMapping findUserForUsername(TdarUser user) {
        String query = "from DropboxUserMapping map where lower(map.username)=lower(:username)";
        Query query2 = getCurrentSession().createQuery(query);
        query2.setParameter("username", user.getUsername());
        query2.setFirstResult(0);
        query2.setMaxResults(1);
        List list = query2.list();
        if (list.size() == 0) {
            return null;
        }
        return (DropboxUserMapping) list.get(0);

    }

    public DropboxUserMapping getUserForDropboxAccount(BasicAccount account) {
        String query = "from DropboxUserMapping map where (:id is not null and lower(map.username)=lower(:id)) or (:email is not null and lower(map.email)=lower(:email))";
        Query query2 = getCurrentSession().createQuery(query);
        query2.setParameter("id", account.getAccountId());
        query2.setParameter("email", account.getEmail());
        query2.setFirstResult(0);
        query2.setMaxResults(1);
        List list = query2.list();
        if (list.size() == 0) {
            return null;
        }
        return (DropboxUserMapping) list.get(0);

    }
}
