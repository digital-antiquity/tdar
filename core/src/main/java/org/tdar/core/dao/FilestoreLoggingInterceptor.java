package org.tdar.core.dao;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.XmlLoggable;
import org.tdar.utils.jaxb.XMLFilestoreLogger;

public class FilestoreLoggingInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = -8784074853252850219L;
    XMLFilestoreLogger xmlLogger;
    private Set<XmlLoggable> toLog = new HashSet<>();
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    
    public FilestoreLoggingInterceptor() throws ClassNotFoundException {
        logger.debug("hi:" + this);
        xmlLogger = new XMLFilestoreLogger();
    }

    public void onDelete(Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] types) {
        if (entity instanceof XmlLoggable) {
            xmlLogger.logRecordXmlToFilestore((Persistable) entity);
        }
    }

    public boolean onFlushDirty(Object entity,
            Serializable id,
            Object[] currentState,
            Object[] previousState,
            String[] propertyNames,
            Type[] types) {

        if (entity instanceof XmlLoggable) {
            toLog.add((XmlLoggable)entity);
        }
        return true;
    }

    @Override
    public void onCollectionRemove(Object collection, Serializable key) throws org.hibernate.CallbackException {
        logger.debug("collectionRemvoe:{}", collection);
    };

    @Override
    public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
        logger.debug("collectionUpdate:{}", collection);
        super.onCollectionUpdate(collection, key);
    }

    @Override
    public boolean onSave(Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] types) {

        if (entity instanceof XmlLoggable) {
            if (entity instanceof XmlLoggable) {
                toLog.add((XmlLoggable)entity);
            }

        }
        return true;
    }

    @Override
    public void afterTransactionCompletion(Transaction tx) {
        logger.debug("TX:" ,tx);
        logger.debug("status:{}", tx.getStatus());
//        if (tx.getStatus() == TransactionStatus.COMMITTED) {
            Iterator<XmlLoggable> iter = toLog.iterator();
            while (iter.hasNext()) {
                XmlLoggable item = iter.next();
                xmlLogger.logRecordXmlToFilestore(item);
                iter.remove();
            }
//            System.out.println("Creations: " + creates + ", Updates: " + updates, "Loads: " + loads);
//        }
    }

}
