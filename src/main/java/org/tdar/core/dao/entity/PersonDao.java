package org.tdar.core.dao.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.TdarNamedQueries;

/**
 * $Id$
 * 
 * Provides DAO access for Person entities, including a variety of methods for
 * looking up a Person in tDAR.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class PersonDao extends Dao.HibernateBase<Person> {

    public PersonDao() {
        super(Person.class);
    }

    @SuppressWarnings("unchecked")
    public List<Person> findAllRegisteredUsers(Integer num) {
        Query query = getCurrentSession().getNamedQuery(QUERY_RECENT_USERS_ADDED);
        if (num != null && num > 0) {
            query.setMaxResults(num);
        }
        return (List<Person>) query.list();
    }

    /**
     * Searches for a Person with the given email. Lowercases the email.
     * 
     * @param email
     * @return
     */
    public Person findByEmail(final String email) {
        return (Person) getCriteria().add(Restrictions.eq("email", email.toLowerCase())).uniqueResult();
    }

    public Person findByUsername(final String username) {
        return (Person) getCriteria().add(Restrictions.eq("username", username.toLowerCase())).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public Set<Person> findByLastName(String lastName) {
        Criteria criteria = getCriteria().add(Restrictions.eq("lastName", lastName));
        return new HashSet<Person>(criteria.list());
    }

    // find people with the same firstName, lastName, or institution (if specified)
    @SuppressWarnings("unchecked")
    public Set<Person> findByPerson(Person person) {
        // if the email address is set then all other fields are moot
        if (StringUtils.isNotBlank(person.getEmail())) {
            Set<Person> hs = new HashSet<Person>();
            hs.add(findByEmail(person.getEmail()));
            return hs;
        }
        Criteria criteria = getCriteria();
        if (StringUtils.isNotBlank(person.getFirstName())) {
            criteria.add(Restrictions.eq("firstName", person.getFirstName()));
        }
        if (StringUtils.isNotBlank(person.getLastName())) {
            criteria.add(Restrictions.eq("lastName", person.getLastName()));
        }
        if (StringUtils.isNotBlank(person.getInstitutionName())) {
            criteria.createCriteria("institution").add(Restrictions.eq("name", person.getInstitutionName()));
        }
        return new HashSet<Person>(criteria.list());
    }

    /**
     * Returns all people with the given full name.
     */
    @SuppressWarnings("unchecked")
    public Set<Person> findByFullName(String fullName) {
        String[] names = Person.split(fullName);
        if (names.length == 0) {
            return null;
        }
        final String lastName = names[0].trim();
        final String firstName = names[1].trim(); // hpcao added the trim, otherwise it can not work
        // System.out.println("finding by last name, firstName: |" + lastName + "|, |" + firstName+"|");
        // FIXME: figure out some way to reliably get a person if first name /
        // last name isn't unique enough... perhaps this method should return a
        // List<Person> instead

        // DetachedCriteria criteria = getDetachedCriteria();
        // criteria.add(Restrictions.eq("lastName", lastName));
        // criteria.add(Restrictions.eq("firstName", firstName));
        // List<Person> persons = (List<Person>) getHibernateTemplate().findByCriteria(criteria);

        Criteria criteria = getCriteria();
        criteria.add(Restrictions.eq("lastName", lastName));
        criteria.add(Restrictions.eq("firstName", firstName));
        return new HashSet<Person>(criteria.list());
    }

    @Override
    protected String getDefaultOrderingProperty() {
        return "lastName";
    }

    @SuppressWarnings("unchecked")
    public List<Person> findRecentLogins() {
        Criteria criteria = getCriteria();
        criteria.add(Restrictions.eq("registered", true));
        criteria.add(Restrictions.isNotNull("lastLogin"));
        criteria.addOrder(Property.forName("lastLogin").desc());
        criteria.setMaxResults(25);
        return criteria.list();
    }

    public Long findNumberOfActualContributors() {
        Criteria criteria = getCriteria(Resource.class);
        criteria.setProjection(Projections.projectionList().add(Projections.countDistinct("submitter.id")));
        return (Long) ((criteria.list()).get(0));
    }

    public Set<Long> findAllContributorIds() {
        Set<Long> ids = new HashSet<Long>();
        for (Object obj_ : getCurrentSession().createSQLQuery(TdarNamedQueries.DISTINCT_SUBMITTERS).list()) {
            Object[] obj = (Object[])obj_;
            ids.add((Long)obj[0]);
        }
        return ids;
    }

    
    public void registerLogin(Person authenticatedUser) {
        authenticatedUser.setLastLogin(new Date());
        authenticatedUser.incrementLoginCount();
        logger.trace("login {} {}", authenticatedUser.getLastLogin(), authenticatedUser.getTotalLogins());
        saveOrUpdate(authenticatedUser);
    }
}
