package org.tdar.core.dao.entity;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.dao.Dao;

/**
 * $Id$
 * 
 * Provides hibernate DAO access for Institution entities (which is just a String).
 *
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class InstitutionDao extends Dao.HibernateBase<Institution> {
    public InstitutionDao() {
        super(Institution.class);
    }
    
    @SuppressWarnings("unchecked")
    public List<Institution> withNameLike(final String name) {
        return (List<Institution>) getCriteria().add(Restrictions.like("name", addWildCards(name))).list();
    }
}
