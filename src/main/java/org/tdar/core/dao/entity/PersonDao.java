package org.tdar.core.dao.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.dao.Dao;

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
    
    public List<Person> findAllRegisteredUsers() {
        return findByCriteria(getDetachedCriteria().add(Restrictions.eq("registered", Boolean.TRUE)));
    }
    
    public List<Person> findAllRegisteredUsersSorted() {
        return findByCriteria(getOrderedDetachedCriteria().add(Restrictions.eq("registered", Boolean.TRUE)));
    }
    
    public List<Person> findAllOtherRegisteredUsers(Person person) { 
        if (person == null) {
            return findAllRegisteredUsersSorted();
        }
        DetachedCriteria criteria = getOrderedDetachedCriteria();
        criteria.add(Restrictions.eq("registered", Boolean.TRUE));
        criteria.add(Restrictions.not(Restrictions.eq("id", person.getId())));
        return findByCriteria(criteria);
    }
    
    /**
     * Searches for a Person with the given email.  Lowercases the email.  
     * @param email
     * @return
     */
    public Person findByEmail(final String email) {
        return (Person) getCriteria().add(Restrictions.eq("email", email.toLowerCase())).uniqueResult();
    }
    
    /*
    @Deprecated
    public Person findByGsoid(final String gsoid) {
        return (Person) getCriteria().add(Restrictions.eq("gsoid", gsoid)).uniqueResult();
    }
    */
    
    @SuppressWarnings("unchecked")
    public Set<Person> findByLastName(String lastName) {
        Criteria criteria = getCriteria().add(Restrictions.eq("lastName", lastName));
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
        final String firstName = names[1].trim(); //hpcao added the trim, otherwise it can not work
        //System.out.println("finding by last name, firstName: |" + lastName + "|, |" + firstName+"|");
        // FIXME: figure out some way to reliably get a person if first name /
        // last name isn't unique enough... perhaps this method should return a
        // List<Person> instead
        
//        DetachedCriteria criteria = getDetachedCriteria();
//        criteria.add(Restrictions.eq("lastName", lastName));
//        criteria.add(Restrictions.eq("firstName", firstName));
//        List<Person> persons = (List<Person>) getHibernateTemplate().findByCriteria(criteria);

        Criteria criteria = getCriteria();
        criteria.add(Restrictions.eq("lastName", lastName));
        criteria.add(Restrictions.eq("firstName", firstName));
        return new HashSet<Person>(criteria.list());        
    }
    
    @Override
    protected String getDefaultOrderingProperty() {
        return "lastName";
    }

}
