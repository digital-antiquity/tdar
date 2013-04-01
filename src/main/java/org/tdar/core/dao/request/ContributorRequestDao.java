package org.tdar.core.dao.request;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.request.ContributorRequest;
import org.tdar.core.dao.Dao;

/**
 * $Id$
 * 
 * <p>
 * Provides hibernate dao access to contributor requests.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Component
public class ContributorRequestDao extends Dao.HibernateBase<ContributorRequest> {

    public ContributorRequestDao() {
        super(ContributorRequest.class);
    }

    @SuppressWarnings("unchecked")
    public List<ContributorRequest> findAllPending() {
        Query query = getCurrentSession().getNamedQuery(QUERY_CONTRIBUTORREQUEST_PENDING);
        return query.list();
    }

    public List<ContributorRequest> findAllApproved() {
        return findAllWithApprovalStatus(Boolean.TRUE);
    }

    public List<ContributorRequest> findAllUnapproved() {
        return findAllWithApprovalStatus(Boolean.FALSE);
    }

    public List<ContributorRequest> findAllWithApprovalStatus(Boolean approved) {
        return findByCriteria(getDetachedCriteria().add(Restrictions.eq("approved", approved)));
    }

    public ContributorRequest findByPerson(Person approved) {
        List<ContributorRequest> result = findByCriteria(getDetachedCriteria().add(Restrictions.eq("applicant", approved)));
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return result.get(0);
    }

    @Override
    protected String getDefaultOrderingProperty() {
        return "timestamp desc";
    }

}
