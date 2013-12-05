package org.tdar.core.dao;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: jimdevos
 * Date: 12/4/13
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class TdarHibernateInterceptor extends EmptyInterceptor{

    Logger logger = LoggerFactory.getLogger(TdarHibernateInterceptor.class);

    @Override
    public boolean onSave(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types) {

        logger.trace("hibernate interceptor, reporting for duty");


        return false;

    }


}
