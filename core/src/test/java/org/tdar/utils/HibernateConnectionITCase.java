package org.tdar.utils;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.PGProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.PreparedStatementResetWork;
import org.tdar.db.model.PostgresConstants;

import com.mchange.v2.c3p0.impl.NewProxyConnection;

public class HibernateConnectionITCase extends AbstractIntegrationTestCase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    GenericDao genericDao;
    
    @Autowired
    private transient SessionFactory sessionFactory;

    @Test
    @Ignore
    public void testGettingProperty() throws SQLException {
        SessionImpl sessionImpl = (SessionImpl)sessionFactory.getCurrentSession();
        NewProxyConnection npc = (NewProxyConnection)sessionImpl.connection();
        logger.debug("PGP:{}", npc.getClientInfo(PGProperty.PREPARE_THRESHOLD.getName()));
        logger.debug("{}",sessionImpl.connection().getClass());
    }
    
    @Test
    public void preparedStatementWork() {
        PreparedStatementResetWork work = new PreparedStatementResetWork();
        sessionFactory.getCurrentSession().doWork(work);
        assertEquals(5, work.getOldStatements());
    }
}
