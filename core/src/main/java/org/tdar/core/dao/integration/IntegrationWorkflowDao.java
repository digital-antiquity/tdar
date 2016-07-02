package org.tdar.core.dao.integration;

import java.util.List;

import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.TdarNamedQueries;

@Component
public class IntegrationWorkflowDao extends Dao.HibernateBase<DataIntegrationWorkflow> {

    public IntegrationWorkflowDao() {
        super(DataIntegrationWorkflow.class);
    }

    @SuppressWarnings("unchecked")
    public List<DataIntegrationWorkflow> getWorkflowsForUser(TdarUser authorizedUser, boolean admin) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.WORKFLOWS_BY_USER);
        if (admin) {
            query = getCurrentSession().getNamedQuery(TdarNamedQueries.WORKFLOWS_BY_USER_ADMIN);
        } else {
            query.setParameter("userId", authorizedUser.getId());
        }
        return query.getResultList();
    }
}
