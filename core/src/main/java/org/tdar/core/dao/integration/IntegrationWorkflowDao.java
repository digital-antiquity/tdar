package org.tdar.core.dao.integration;

import java.util.List;

import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.base.Dao;

@Component
public class IntegrationWorkflowDao extends Dao.HibernateBase<DataIntegrationWorkflow> {

    public IntegrationWorkflowDao() {
        super(DataIntegrationWorkflow.class);
    }

    public List<DataIntegrationWorkflow> getWorkflowsForUser(TdarUser authorizedUser, boolean admin) {
        Query<DataIntegrationWorkflow> query = getCurrentSession().createNamedQuery(TdarNamedQueries.WORKFLOWS_BY_USER, DataIntegrationWorkflow.class);
        if (admin) {
            query = getCurrentSession().createNamedQuery(TdarNamedQueries.WORKFLOWS_BY_USER_ADMIN,DataIntegrationWorkflow.class);
        } else {
            query.setParameter("userId", authorizedUser.getId());
        }
        return query.getResultList();
    }
}
