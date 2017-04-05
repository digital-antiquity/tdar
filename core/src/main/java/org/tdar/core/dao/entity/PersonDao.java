package org.tdar.core.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.AgreementTypes;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.base.Dao;
import org.tdar.utils.PersistableUtils;

/**
 * $Id$
 * 
 * Provides DAO access for Person entities.
 * 
 * FIXME: replace with TdarUserDao?
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class PersonDao extends Dao.HibernateBase<Person> {

    private static final Long TDAR_USER_PRIOR_TO_ASKING_AFFILIATION = 5215L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private InstitutionDao institutionDao;
    
    public PersonDao() {
        super(Person.class);
    }

    @SuppressWarnings("unchecked")
    public List<TdarUser> findAllRegisteredUsers(Integer num) {
        Query<TdarUser> query = getCurrentSession().getNamedQuery(QUERY_RECENT_USERS_ADDED);
        if ((num != null) && (num > 0)) {
            query.setMaxResults(num);
        }
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Person> findSimilarPeople(TdarUser user) {
        List<Person> people = new ArrayList<>();
        String initial = user.getFirstName().substring(0, 1).toUpperCase();
        Query<Person> query = getCurrentSession().getNamedQuery(QUERY_SIMILAR_PEOPLE);
        query.setParameter("firstName", user.getFirstName());
        query.setParameter("lastName", user.getLastName());
        query.setParameter("initial", initial);
        query.setParameter("initial2", initial + ".");
        people.addAll(query.getResultList());
        return people;
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

    public TdarUser findByUsername(final String username) {
        return (TdarUser) getCriteria(TdarUser.class).add(Restrictions.eq("username", username.toLowerCase())).uniqueResult();
    }

    public TdarUser findUserByEmail(final String email) {
        return (TdarUser) getCriteria(TdarUser.class).add(Restrictions.eq("email", email.toLowerCase())).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public Set<Person> findByLastName(String lastName) {
        Criteria criteria = getCriteria().add(Restrictions.eq("lastName", lastName));
        return new HashSet<>(criteria.list());
    }

    // find people with the same firstName, lastName, or institution (if specified)
    @SuppressWarnings("unchecked")
    public Set<Person> findByPerson(Person person) {
        // if the email address is set then all other fields are moot
        if (StringUtils.isNotBlank(person.getEmail())) {
            Set<Person> hs = new HashSet<>();
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Person findAuthorityFromDuplicate(Creator<?> dup) {
        Query query = getCurrentSession().createNativeQuery(String.format(QUERY_CREATOR_MERGE_ID, dup.getId()));
        List<Number> result = query.getResultList();
        if (CollectionUtils.isNotEmpty(result)) {
            try {
                return find(result.get(0).longValue());
            } catch (Exception e) {
                logger.error("could not find master for {} {}", dup, result,e);
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
        return new HashSet<>(criteria.list());
    }

    @Override
    protected String getDefaultOrderingProperty() {
        return "lastName";
    }

    @SuppressWarnings("unchecked")
    public List<TdarUser> findRecentLogins() {
        Criteria criteria = getCriteria(TdarUser.class);
        criteria.add(Restrictions.isNotNull("lastLogin"));
        criteria.addOrder(Property.forName("lastLogin").desc());
        criteria.setMaxResults(25);
        return criteria.list();
    }

    public Long findNumberOfActualContributors() {
        Criteria criteria = getCriteria(Resource.class);
        criteria.setProjection(Projections.projectionList().add(Projections.countDistinct("uploader.id")));
        return (Long) ((criteria.list()).get(0));
    }

    @SuppressWarnings("unchecked")
    public Set<Long> findAllContributorIds() {
        Set<Long> ids = new HashSet<>();
        for (Number obj_ : (List<Number>) getCurrentSession().createNativeQuery(TdarNamedQueries.DISTINCT_SUBMITTERS).getResultList()) {
            ids.add(obj_.longValue());
        }
        return ids;
    }

    public void registerLogin(TdarUser authenticatedUser, String userAgent) {
        authenticatedUser.setLastLogin(new Date());
        authenticatedUser.setUserAgent(userAgent);
        authenticatedUser.incrementLoginCount();
        logger.trace("login {} {}", authenticatedUser.getLastLogin(), authenticatedUser.getTotalLogins());
        saveOrUpdate(authenticatedUser);
    }

    public void updateOccuranceValues() {
        Session session = getCurrentSession();
        String roles = getFormattedRoles(null);
        logger.info("clearing creator occurrence values");
        session.createNativeQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_CLEAR_COUNT)).executeUpdate();
        logger.info("beginning updates - resource");
        session.createNativeQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_RESOURCE)).executeUpdate();
        logger.info("beginning updates - resource - inherited");
        session.createNativeQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_RESOURCE_INHERITED, roles)).executeUpdate();
        logger.info("beginning updates - copyright");
        session.createNativeQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_COPYRIGHT)).executeUpdate();
        logger.info("beginning updates - provider");
        session.createNativeQuery(
                String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_PROVIDER, Creator.OCCURRENCE, Creator.OCCURRENCE))
                .executeUpdate();
        logger.info("beginning updates - publisher");
        session.createNativeQuery(
                String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_PUBLISHER, Creator.OCCURRENCE, Creator.OCCURRENCE))
                .executeUpdate();
        logger.info("beginning updates - submitter");
        session.createNativeQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_RESOURCE_SUBMITTER)).executeUpdate();
        logger.info("beginning updates - institution");
        session.createNativeQuery(String.format(TdarNamedQueries.UPDATE_CREATOR_OCCURRENCE_INSTITUTION)).executeUpdate();
        logger.info("completed updates");

        // create a temp table for these users and drop them in (much faster than a single query); the 1st temp table is 1:1 with resources, the second is 1:1
        // with creators. This is much faster.
        session.createNativeQuery(BROWSE_CREATOR_CREATE_TEMP).executeUpdate();
        // populate the resource table
        session.createNativeQuery(BROWSE_CREATOR_ACTIVE_USERS_1).executeUpdate();
        roles = getFormattedRoles(CreatorType.PERSON);
        session.createNativeQuery(String.format(BROWSE_CREATOR_ROLES_2, roles, 1000)).executeUpdate();
        session.createNativeQuery(String.format(BROWSE_CREATOR_IR_ROLES_3, roles, 1000)).executeUpdate();
        // these roles matter less, so they get a negative priority. If someone is "just" the submitter, they are 0, if they have secondary roles or submitter,
        // they will have a negative value, if they have an authorship equivalent role we get a positive result
        roles = getFormattedRoles(CreatorType.INSTITUTION);
        session.createNativeQuery(String.format(BROWSE_CREATOR_ROLES_2, roles, -10)).executeUpdate();
        session.createNativeQuery(String.format(BROWSE_CREATOR_IR_ROLES_3, roles, -10)).executeUpdate();
        session.createNativeQuery(BROWSE_CREATOR_IR_FIELDS_4).executeUpdate();
        // populate the temp_creator table with its values
        session.createNativeQuery(BROWSE_CREATOR_CREATOR_TEMP_5).executeUpdate();
        // migrate the data from the temp_creator table to the real one
        session.createNativeQuery(BROWSE_CREATOR_UPDATE_CREATOR_6).executeUpdate();
        logger.info("completed updates");

    }

    private String getFormattedRoles(CreatorType type) {
        Set<ResourceCreatorRole> roleSet = ResourceCreatorRole.getResourceCreatorRolesForProfilePage(type);
        String roles = String.format("'%s'", StringUtils.join(roleSet, "','"));
        return roles;
    }

    public Long getCreatorViewCount(Creator<?> creator) {
        Query<Number> query = getCurrentSession().createNamedQuery(TdarNamedQueries.CREATOR_VIEW, Number.class);
        query.setParameter("id", creator.getId());
        Number result = query.getSingleResult();
        return result.longValue();
    }

    public TdarUser findConvertPersonToUser(Person person, String username) {
        Long id = person.getId();
        getCurrentSession().createNativeQuery(String.format(TdarNamedQueries.CONVERT_PERSON_TO_USER, id, username)).executeUpdate();
        detachFromSession(person);
        TdarUser toReturn = find(TdarUser.class, id);
        logger.debug("toReturn: {}", toReturn);
        return toReturn;
    }

    @SuppressWarnings("rawtypes")
    public Map<AgreementTypes, Long> getAgreementCounts() {
        HashMap<AgreementTypes, Long> toReturn = new HashMap<>();
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.AGREEMENT_COUNTS);
        Object[] result = (Object[]) query.getSingleResult();
        toReturn.put(AgreementTypes.USER_AGREEMENT, ((Number) result[0]).longValue());
        toReturn.put(AgreementTypes.CONTRIBUTOR_AGREEMENT, ((Number) result[1]).longValue());
        return toReturn;
    }

    @SuppressWarnings("rawtypes")
    public Map<UserAffiliation, Long> getAffiliationCounts(boolean b) {
        HashMap<UserAffiliation, Long> toReturn = new HashMap<>();
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.AFFILIATION_COUNTS);
        if (b) {
            query = getCurrentSession().getNamedQuery(TdarNamedQueries.AFFILIATION_COUNTS_CONTRIBUTOR);
        }
        for (Object row_ : query.getResultList()) {
            Object[] row = (Object[]) row_;
            toReturn.put((UserAffiliation) row[0], ((Number) row[1]).longValue());
        }
        Long noAnswer = toReturn.remove(null);
        if (TdarConfiguration.getInstance().isProductionEnvironment() && TdarConfiguration.getInstance().getHostName().equalsIgnoreCase("core.tdar.org")) {
            noAnswer = noAnswer - TDAR_USER_PRIOR_TO_ASKING_AFFILIATION;
            toReturn.put(UserAffiliation.PRIOR_TO_ASKING, TDAR_USER_PRIOR_TO_ASKING_AFFILIATION);
        }
        toReturn.put(UserAffiliation.NO_RESPONSE, noAnswer);
        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public List<UserInvite> checkInvite(TdarUser person) {
        Query<UserInvite> query = getCurrentSession().createNamedQuery(TdarNamedQueries.CHECK_INVITES);
        query.setParameter("email", person.getEmail());
        return query.getResultList();
        
    }

    public List<ResourceRevisionLog> findChangesForUser(TdarUser user, Date date) {
        Criteria criteria = getCriteria(ResourceRevisionLog.class).add(Restrictions.eq("person", user));
        if (date != null) {
            criteria.add(Restrictions.ge("timestamp", date));
        }
        criteria.addOrder(Order.desc("timestamp"));
        return criteria.list();
        }

    /**
     * FInds or Creates a person (but doesn't save)
     * @param transientPerson
     * @return
     */
    public Person findOrCreatePerson(Person transientPerson) {
        // now find or save the person (if the person was found the institution field is ignored
        // entirely and replaced with the persisted person's institution
        if ((transientPerson == null) || transientPerson.hasNoPersistableValues()) {
            return null;
        }

        if (PersistableUtils.isNotNullOrTransient(transientPerson.getId())) {
            if (sessionContains(transientPerson)) {
                return transientPerson;
            }
            return find(transientPerson.getId());
        }

        Person blessedPerson = null;
        if (transientPerson instanceof TdarUser) {
            String username = ((TdarUser) transientPerson).getUsername();
            if (StringUtils.isNotBlank(username)) {
                blessedPerson = findByUsername(username);
            }
            logger.debug("find by username: {}, {}", username, blessedPerson);
        }
        
        String email = transientPerson.getEmail();
        if (StringUtils.isNotBlank(email)  && blessedPerson == null) {
            blessedPerson = findByEmail(email);
        } else {
            transientPerson.setEmail(null);// make sure it's null and not just blank or empty
        }


        // didn't find by email? cast the net a little wider...
        if (blessedPerson == null) {
            Institution transientInstitution = transientPerson.getInstitution();
            if (transientInstitution != null) {
                Institution foundInstitution = institutionDao.findInstitution(transientInstitution);
                if (foundInstitution != null) {
                    transientPerson.setInstitution(foundInstitution);
                }
            }
            Set<Person> people = findByPerson(transientPerson);
            /*
             * Perhaps this should match only if FirstName and LastName are not empty, but I can see cases
             * where LastName may not be empty but firstName is...
             */
            if (!people.isEmpty()) {
                blessedPerson = people.iterator().next();
            }
        }
        transientPerson.setEmail(email);
        return transientPerson;
    }

}
