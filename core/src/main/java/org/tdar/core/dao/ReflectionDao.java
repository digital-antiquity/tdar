package org.tdar.core.dao;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.lang.StringUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

@Component
public class ReflectionDao {

    public transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    // find all the instances of the specified type that refer to instances of the target specified type
    public ScrollableResults findReferrers(Field field, Collection<Long> idlist) {
        String hql;
        String fmt;
        String targetClass = field.getDeclaringClass().getSimpleName();
        if (field.getAnnotation(ManyToMany.class) != null) {
            fmt = TdarNamedQueries.QUERY_HQL_MANY_TO_MANY_REFERENCES;
        } else if (field.getAnnotation(OneToOne.class) != null) {
            logger.warn("encountered a one-to-one relationship  on {} when looking for references.  Treating the same as many-to-one", field);
            fmt = TdarNamedQueries.QUERY_HQL_MANY_TO_ONE_REFERENCES;
        } else if (field.getAnnotation(ManyToOne.class) != null) {
            fmt = TdarNamedQueries.QUERY_HQL_MANY_TO_ONE_REFERENCES;
        } else if (field.getAnnotation(OneToMany.class) != null) {
            logger.warn("encountered a one-to-many relationship  on {} when looking for references.  Treating the same as many-to-many", field);
            fmt = TdarNamedQueries.QUERY_HQL_MANY_TO_MANY_REFERENCES;
        } else {
            throw new IllegalArgumentException(MessageHelper.getMessage("reflectionDao.field_must_be_jpa", Arrays.asList(field)));
        }
        hql = String.format(fmt, targetClass, field.getName());

        // FIXME: bug in hibernate won't do parameter replacement properly and throws NPE:
/*
 * java.lang.NullPointerException
    at org.hibernate.param.NamedParameterSpecification.bind(NamedParameterSpecification.java:53)
    at org.hibernate.loader.hql.QueryLoader.bindParameterValues(QueryLoader.java:628)
    at org.hibernate.loader.Loader.prepareQueryStatement(Loader.java:1949)
    at org.hibernate.loader.Loader.executeQueryStatement(Loader.java:1902)
    at org.hibernate.loader.Loader.executeQueryStatement(Loader.java:1880)
    at org.hibernate.loader.Loader.scroll(Loader.java:2684)
    at org.hibernate.loader.hql.QueryLoader.scroll(QueryLoader.java:571)
    at org.hibernate.hql.internal.ast.QueryTranslatorImpl.scroll(QueryTranslatorImpl.java:423)
    at org.hibernate.engine.query.spi.HQLQueryPlan.performScroll(HQLQueryPlan.java:350)
    at org.hibernate.internal.SessionImpl.scroll(SessionImpl.java:1543)
    at org.hibernate.query.internal.AbstractProducedQuery.doScroll(AbstractProducedQuery.java:1310)
    at org.hibernate.query.internal.AbstractProducedQuery.scroll(AbstractProducedQuery.java:1300)
    at org.hibernate.query.internal.AbstractProducedQuery.scroll(AbstractProducedQuery.java:99)
    at org.tdar.core.dao.ReflectionDao.findReferrers(ReflectionDao.java:60)

 */
//        hql = hql.replace(":idlist", StringUtils.join(idlist,","));
        Query query = getCurrentSession().createQuery(hql);
        query.setParameter("idlist", idlist);

        query.setFetchSize(TdarConfiguration.getInstance().getScrollableFetchSize());
        return query.scroll(ScrollMode.FORWARD_ONLY);
    }

    /**
     * Returns the count of objects that refer to the specified object via the specified Field. In other words,
     * this method returns a count of the instances of the field's declaring class.
     * 
     * @param field
     *            field which refers to the persitable class that the method should return a reference count for.
     * @param idlist
     *            list of id's of the objects to find a reference count for
     * @return reference count
     */
    public long getReferrerCount(Field field, List<Long> idlist) {
        String hql, fmt;
        String targetClass = field.getDeclaringClass().getSimpleName();

        if (field.getAnnotation(ManyToMany.class) != null) {
            fmt = TdarNamedQueries.QUERY_HQL_COUNT_MANY_TO_MANY_REFERENCES;
        } else if (field.getAnnotation(OneToOne.class) != null) {
            logger.warn("encountered a one-to-one relationship  on {} when looking for references.  Treating the same as many-to-one", field);
            fmt = TdarNamedQueries.QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES;
        } else if (field.getAnnotation(ManyToOne.class) != null) {
            fmt = TdarNamedQueries.QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES;
        } else if (field.getAnnotation(OneToMany.class) != null) {
            logger.warn("encountered a one-to-many relationship  on {} when looking for references.  Treating the same as many-to-many", field);
            fmt = TdarNamedQueries.QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES;
        } else {
            throw new IllegalArgumentException(MessageHelper.getMessage("reflectionDao.field_must_be_jpa_relationship", Arrays.asList(field)));
        }

        hql = String.format(fmt, targetClass, field.getName());
        Query query = getCurrentSession().createQuery(hql);
        query.setParameterList("idlist", idlist);
        return (Long) query.uniqueResult();
    }

    /**
     * Returns the count of objects that refer to the specified object via the specified Field. In other words,
     * this method returns a count of the instances of the field's declaring class.
     * 
     * @param field
     *            field which refers to the persitable class that the method should return a reference count for.
     * @param id
     *            identifier value of the object to find a reference count for
     * @return reference count
     */
    @Deprecated
    public long getReferrerCount(Field field, long id) {
        return getReferrerCount(field, Arrays.asList(id));
    }

    /**
     * same as referrer count, but returns a list of map<id,count> that breaks down reference counts broken down by id
     * 
     * @param field
     * @param idlist
     * @return
     */
    // TODO: add one-to-many!!!
    @SuppressWarnings("unchecked")
    public List<Map<String, Long>> getReffererCountMap(Field field, List<Long> idlist) {
        String hql, fmt;
        String targetClass = field.getDeclaringClass().getSimpleName();

        if (field.getAnnotation(ManyToMany.class) != null) {
            fmt = TdarNamedQueries.QUERY_HQL_COUNT_MANY_TO_MANY_REFERENCES_MAP;
        } else if (field.getAnnotation(OneToOne.class) != null) {
            logger.warn("encountered a one-to-one relationship  on {} when looking for references.  Treating the same as many-to-one", field);
            fmt = TdarNamedQueries.QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES_MAP;
        } else if (field.getAnnotation(ManyToOne.class) != null) {
            fmt = TdarNamedQueries.QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES_MAP;
        } else if (field.getAnnotation(OneToMany.class) != null) {
            throw new IllegalArgumentException(MessageHelper.getMessage("reflectionDao.one_to_many_not_implemented", Arrays.asList(field)));
        } else {
            throw new IllegalArgumentException(MessageHelper.getMessage("reflectionDao.many_to_many_not_implemented", Arrays.asList(field)));
        }

        hql = String.format(fmt, targetClass, field.getName());
        Query query = getCurrentSession().createQuery(hql);
        query.setParameterList("idlist", idlist);
        return query.list();
    }

    public void updateReferrersToAuthority(Field field, List<Long> dupeIds, Long authorityId) {

    }

}
