package org.tdar.core.dao.resource;

import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ReadUser;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.Dao;

/**
 * $Id$
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Component
public class ReadUserDao extends Dao.HibernateBase<ReadUser> {

    public ReadUserDao() {
        super(ReadUser.class);
    }

    public boolean isReadUser(Person person, Resource resource) {
        // FIXME: change to NamedQuery, should be faster.
        List<ReadUser> readUsers = findByCriteria(getDetachedCriteria().add(Restrictions.eq("person", person)).add(Restrictions.eq("resource", resource)));
        return readUsers != null && !readUsers.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public List<Project> findProjectsByPerson(Person person) {
        if (person == null) {
            return Collections.emptyList();
        }
        Query query = getCurrentSession().getNamedQuery(QUERY_READUSER_PROJECTS);
        query.setLong("personId", person.getId());
        return (List<Project>) query.list();
    }
}
