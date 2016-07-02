package org.tdar.core.dao.entity;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.query.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.TdarNamedQueries;

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
        return getCriteria().add(Restrictions.like("name", addWildCards(name))).list();
    }

    public Institution findAuthorityFromDuplicate(Institution dup) {
        Query query = getCurrentSession().createNativeQuery(String.format(QUERY_CREATOR_MERGE_ID, dup.getId()));
        @SuppressWarnings("unchecked")
        List<BigInteger> result = query.getResultList();
        if (CollectionUtils.isEmpty(result)) {
            return null;
        } else {
            return find(result.get(0).longValue());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Institution> findInstitutionsWIthSpaces() {
        return getCriteria().add(
                Restrictions.and(Restrictions.eq("status", Status.ACTIVE), Restrictions.or(Restrictions.like("name", " %"), Restrictions.like("name", "% "))))
                .list();
    }

    public boolean canEditInstitution(TdarUser authenticatedUser, Institution item) {
        Query query = getNamedQuery(TdarNamedQueries.CAN_EDIT_INSTITUTION);
        query.setParameter("institutionId", item.getId());
        query.setParameter("userId", authenticatedUser.getId());
        Boolean result = (Boolean) query.uniqueResult();
        if (result == null) {
            return false;
        }
        return result;
    }
}
