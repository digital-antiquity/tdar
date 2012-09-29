package org.tdar.core.dao.resource;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.FullUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.Dao;

/**
 * $Id$
 * 
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Component
public class FullUserDao extends Dao.HibernateBase<FullUser> {

    public FullUserDao() {
        super(FullUser.class);
    }

    public boolean isFullUser(Person person, Resource resource) {
        // FIXME: change to NamedQuery, should be faster.
        List<FullUser> fullUsers = findByCriteria(getDetachedCriteria().add(Restrictions.eq("person", person)).add(Restrictions.eq("resource", resource)));
        return fullUsers != null && !fullUsers.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public Set<Dataset> findDatasetsByPerson(Person person) {
        if (person == null)
            return Collections.emptySet();
        Query query = getCurrentSession().getNamedQuery(QUERY_FULLUSER_DATASET);
        query.setLong("personId", person.getId());
        return new LinkedHashSet<Dataset>(query.list());
    }

    @SuppressWarnings("unchecked")
    public Set<Resource> findResourcesByPerson(Person person) {
        if (person == null) {
            return Collections.emptySet();
        }
        Query query = getCurrentSession().getNamedQuery(QUERY_FULLUSER_RESOURCES);
        query.setLong("personId", person.getId());
        return new LinkedHashSet<Resource>(query.list());
    }

    @SuppressWarnings("unchecked")
    public List<Project> findSparseTitleIdProjectListByPerson(Person person) {
        if (person == null) {
            return Collections.emptyList();
        }
        Query query = getCurrentSession().getNamedQuery(QUERY_READ_ONLY_FULLUSER_PROJECTS);
        query.setLong("personId", person.getId());
        query.setReadOnly(true);
        List<Project> projects = (List<Project>) query.list();
        if (projects == null) {
            return Collections.emptyList();
        }
        return projects;
    }
}
