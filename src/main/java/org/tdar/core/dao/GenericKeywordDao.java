package org.tdar.core.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.utils.Pair;

@Component("genericKeywordDao")
public class GenericKeywordDao extends GenericDao {

    public <K extends HierarchicalKeyword<K>> List<K> findAllDescendants(Class<K> cls,K keyword) {
        String index = keyword.getIndex();
        if (StringUtils.isBlank(index))
            return Collections.emptyList();
        index += ".%";
        DetachedCriteria criteria = getDetachedCriteria(cls);
        criteria.add(Restrictions.ilike("index", index));
        return findByCriteria(cls, criteria);
    }

    public <K extends Keyword> K findByLabel(Class<K> cls, String label) {
        // FIXME: turn this into a generic named query?
        return findByProperty(cls, "label", label);
    }
    
    public <K extends Keyword> List<K> findAllByLabels(Class<K> cls, List<String> labels) {
        return findAllFromList(cls, "label", labels);
    }
    
    protected <T extends Keyword> List<Pair<T, Integer>> getKeywordStats(String namedQuery) {
        List<Pair<T, Integer>> list = new ArrayList<Pair<T, Integer>>();
        Query q = getCurrentSession().getNamedQuery(namedQuery);
        for(Object result: q.list()) {
            Object[] cols = (Object[])result;
            @SuppressWarnings("unchecked")
            T keyword = (T)cols[0];
            Integer count = (int)((long)((Long)cols[1])); //this feels dumb
            Pair<T, Integer> pair =  new Pair<T, Integer>(keyword, count);
            list.add(pair);
        }
        return list;
    }

    public List<Pair<CultureKeyword, Integer>> getUncontrolledCultureKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_UNCONTROLLED);
    }
    
    public List<Pair<CultureKeyword, Integer>> getControlledCultureKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_CONTROLLED);
    }
    
    public List<Pair<GeographicKeyword, Integer>> getGeographicKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_GEOGRAPHIC_KEYWORD);
    }
    
    public List<Pair<InvestigationType, Integer>> getInvestigationTypeStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_INVESTIGATION_TYPE);
    }
    
    public List<Pair<MaterialKeyword, Integer>> getMaterialKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_MATERIAL_KEYWORD);
    }

    public List<Pair<OtherKeyword, Integer>> getOtherKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_OTHER_KEYWORD);
    }

    public List<Pair<SiteNameKeyword, Integer>> getSiteNameKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_NAME_KEYWORD);
    }

    public List<Pair<SiteTypeKeyword, Integer>> getControlledSiteTypeKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_CONTROLLED);
    }
    
    public List<Pair<SiteTypeKeyword, Integer>> getUncontrolledSiteTypeKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_UNCONTROLLED);
    }
    
    public List<Pair<TemporalKeyword, Integer>> getTemporalKeywordStats() {
        return getKeywordStats(TdarNamedQueries.QUERY_KEYWORD_COUNT_TEMPORAL_KEYWORD);
    }
    
}
