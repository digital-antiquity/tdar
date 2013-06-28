package org.tdar.core.dao.entity;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Query;
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

    public Institution findAuthorityFromDuplicate(Institution dup) {
        Query query = getCurrentSession().createSQLQuery(String.format(QUERY_CREATOR_MERGE_ID, dup.getClass().getSimpleName(), dup.getId()));
        List<BigInteger> result = (List<BigInteger>)query.list();
        if (CollectionUtils.isEmpty(result)) {
            return null;
        } else {
            return find(result.get(0).longValue());
        }
    }
}
