package org.tdar.core.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Table;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Status;
import org.tdar.utils.Pair;

@Component("genericKeywordDao")
public class GenericKeywordDao extends GenericDao {

    private static final String LABEL = "label";
    private static final String INDEX = "index";
    public static final String NAME = "name";
    public static final String INHERITANCE_TOGGLE_FIELDNAME = "INHERITANCE_TOGGLE";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Transactional
    public <K extends HierarchicalKeyword<K>> List<K> findAllDescendants(Class<K> cls, K keyword) {
        String index = keyword.getIndex();
        if (StringUtils.isBlank(index)) {
            return Collections.emptyList();
        }
        index += ".%";
        DetachedCriteria criteria = getDetachedCriteria(cls);
        criteria.add(Restrictions.ilike(INDEX, index));
        return findByCriteria(cls, criteria);
    }

    @Transactional
    public <K extends Keyword> K findByLabel(Class<K> cls, String label) {
        // FIXME: turn this into a generic named query?
        return findByPropertyIgnoreCase(cls, LABEL, StringUtils.trim(label));
    }

    @Transactional
    protected <T extends Keyword> List<Pair<T, Integer>> getKeywordStats(String namedQuery) {
        List<Pair<T, Integer>> list = new ArrayList<Pair<T, Integer>>();
        Query q = getCurrentSession().getNamedQuery(namedQuery);
        for (Object result : q.list()) {
            Object[] cols = (Object[]) result;
            @SuppressWarnings("unchecked")
            T keyword = (T) cols[0];
            Integer count = (int) ((long) ((Long) cols[1])); // this feels dumb
            Pair<T, Integer> pair = new Pair<T, Integer>(keyword, count);
            list.add(pair);
        }
        return list;
    }

    @Transactional
    public List<Pair<CultureKeyword, Integer>> getUncontrolledCultureKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_UNCONTROLLED);
    }

    @Transactional
    public List<Pair<CultureKeyword, Integer>> getControlledCultureKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_CONTROLLED);
    }

    @Transactional
    public List<Pair<GeographicKeyword, Integer>> getGeographicKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_GEOGRAPHIC_KEYWORD);
    }

    @Transactional
    public List<Pair<InvestigationType, Integer>> getInvestigationTypeStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_INVESTIGATION_TYPE);
    }

    @Transactional
    public List<Pair<MaterialKeyword, Integer>> getMaterialKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_MATERIAL_KEYWORD);
    }

    @Transactional
    public List<Pair<OtherKeyword, Integer>> getOtherKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_OTHER_KEYWORD);
    }

    @Transactional
    public List<Pair<SiteNameKeyword, Integer>> getSiteNameKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_NAME_KEYWORD);
    }

    @Transactional
    public List<Pair<SiteTypeKeyword, Integer>> getControlledSiteTypeKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_CONTROLLED);
    }

    @Transactional
    public List<Pair<SiteTypeKeyword, Integer>> getUncontrolledSiteTypeKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_UNCONTROLLED);
    }

    @Transactional
    public List<Pair<TemporalKeyword, Integer>> getTemporalKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_TEMPORAL_KEYWORD);
    }

    @Transactional(readOnly = false)
    public void updateOccuranceValues() {
        for (KeywordType type : KeywordType.values()) {
            String inheritanceField = type.getInheritanceToggleField();
            String tableName = (String) AnnotationUtils.getValue(AnnotationUtils.getAnnotation(type.getKeywordClass(), Table.class), NAME);
            Session session = getCurrentSession();
            logger.info("{} {} ", inheritanceField, tableName);
            session.createSQLQuery(String.format(TdarNamedQueries.UPDATE_KEYWORD_OCCURRENCE_CLEAR_COUNT, tableName)).executeUpdate();
            String format = String.format(TdarNamedQueries.UPDATE_KEYWORD_OCCURRENCE_COUNT, tableName);
            logger.trace(format);
            session.createSQLQuery(format).executeUpdate();
            format = String.format(TdarNamedQueries.UPDATE_KEYWORD_OCCURRENCE_COUNT_INHERITANCE, tableName, inheritanceField);
            logger.trace(format);
            session.createSQLQuery(format).executeUpdate();
            logger.info("completed update on {}", tableName);
        }
    }

    @Transactional
    public Keyword findAuthority(Keyword kwd) {
        Table table = AnnotationUtils.findAnnotation(kwd.getClass(), Table.class);
        Query query = getCurrentSession().createSQLQuery(String.format(TdarNamedQueries.QUERY_KEYWORD_MERGE_ID, table.name(), kwd.getId()));
        @SuppressWarnings("unchecked")
        List<BigInteger> result = query.list();
        if (CollectionUtils.isEmpty(result)) {
            return null;
        } else {
            return find(kwd.getClass(), result.get(0).longValue());
        }
    }

    public Number countActiveWithStatus(KeywordType type, Boolean controlled) {
        Criteria criteria = getCurrentSession().createCriteria(type.getKeywordClass()).add(Restrictions.in("status", Arrays.asList(Status.ACTIVE)));
        if (controlled != null) {
            criteria.add(Restrictions.eq("approved", controlled));
        }
        criteria.setProjection(Projections.rowCount());
        return (Number) criteria.uniqueResult();
    }


}
