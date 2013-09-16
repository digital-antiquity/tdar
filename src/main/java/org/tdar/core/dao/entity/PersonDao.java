package org.tdar.core.dao.entity;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
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
        criteria.add(Restrictions.eq("firstName", person.getFirstName()));
        criteria.add(Restrictions.eq("lastName", person.getLastName()));

        if (StringUtils.isNotBlank(person.getInstitutionName())) {
            criteria.createCriteria("institution").add(Restrictions.eq("name", person.getInstitutionName()));
        }
        return new HashSet<Person>(criteria.list());
    }

    public Person findAuthorityFromDuplicate(Person dup) {
        Query query = getCurrentSession().createSQLQuery(String.format(QUERY_CREATOR_MERGE_ID, dup.getClass().getSimpleName(), dup.getId()));
        @SuppressWarnings("unchecked")
        List<BigInteger> result = (List<BigInteger>) query.list();
        if (CollectionUtils.isNotEmpty(result)) {
            try {
                return find(result.get(0).longValue());
            } catch (Exception e) {
                logger.error("could not find master for {} {}", dup, result);
            }
        }
        return null;
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

    @SuppressWarnings("unchecked")
    public Set<Long> findAllContributorIds() {
        Set<Long> ids = new HashSet<Long>();
        for (Number obj_ : (List<Number>) getCurrentSession().createSQLQuery(TdarNamedQueries.DISTINCT_SUBMITTERS).list()) {
            ids.add(obj_.longValue());
        }
        return ids;
    }

    public void registerLogin(Person authenticatedUser) {
        authenticatedUser.setLastLogin(new Date());
        authenticatedUser.incrementLoginCount();
        logger.trace("login {} {}", authenticatedUser.getLastLogin(), authenticatedUser.getTotalLogins());
        saveOrUpdate(authenticatedUser);
    }

    public void updateOccuranceValues() {
        Session session = getCurrentSession();
        logger.info("clearing creator occurrence values");
        session.createSQLQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_CLEAR_COUNT)).executeUpdate();
        logger.info("beginning updates - resource");
        session.createSQLQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_RESOURCE)).executeUpdate();
        logger.info("beginning updates - copyright");
        session.createSQLQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_COPYRIGHT)).executeUpdate();
        logger.info("beginning updates - provider");
        session.createSQLQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_PROVIDER)).executeUpdate();
        logger.info("beginning updates - publisher");
        session.createSQLQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_PUBLISHER)).executeUpdate();
        logger.info("beginning updates - submitter");
        session.createSQLQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_RESOURCE_SUBMITTER)).executeUpdate();
        logger.info("completed updates");
    }

}
