package org.tdar.core.bean;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * Created by jimdevos on 12/9/14.
 */
public class DataIntegrationWorkflowITCase extends AbstractIntegrationTestCase {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Test
    @Rollback
    public void testWorkflowCreate() {
        getLogger().debug("hello world!");
    }

}
