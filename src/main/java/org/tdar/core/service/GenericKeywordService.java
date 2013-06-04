package org.tdar.core.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.SuggestedKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.GenericKeywordDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.Pair;

/*
 * A generic service to support the majority of keyword functions.
 */
@Service("genericKeywordSerice")
public class GenericKeywordService extends GenericService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("genericKeywordDao")
    GenericKeywordDao genericKeywordDao;

    public <W extends SuggestedKeyword> List<W> findAllApproved(Class<W> cls) {
        return getDao().findAllByProperty(cls, "approved", true);
    }

    private Map<Class<?>, List<?>> cache = new ConcurrentHashMap<Class<?>, List<?>>();

    @SuppressWarnings("unchecked")
    public synchronized <W extends SuggestedKeyword> List<W> findAllApprovedWithCache(Class<W> cls) {
        if (!cache.containsKey(cls)) {
            cache.put(cls, findAllApproved(cls));
        }
        return (List<W>) cache.get(cls);
    }

    public <H extends HierarchicalKeyword<H>> List<H> findAllDescendants(Class<H> cls, H keyword) {
        return genericKeywordDao.findAllDescendants(cls, keyword);
    }

    @Transactional(readOnly = false)
    public <K extends Keyword> Set<K> findOrCreateByLabels(Class<K> cls, List<String> labels) {
        if (CollectionUtils.isEmpty(labels)) {
            return new HashSet<K>();
        }
        Set<K> set = new HashSet<K>();
        for (String label : labels) {
            if (StringUtils.isBlank(label)) {
                getLogger().trace("Skipping empty keyword label: " + label);
                continue;
            }
            logger.trace("find or create keyword:" + label);
            set.add(findOrCreateByLabel(cls, label));
        }
        return set;
    }

    private Logger getLogger() {
        return logger;
    }

    @Transactional(readOnly = true)
    public <K extends Keyword> K findByLabel(Class<K> cls, String label) {
        if (label == null)
            return null;
        return genericKeywordDao.findByLabel(cls, label);
    }

    @Transactional(readOnly = false)
    public <K extends Keyword> K findOrCreateByLabel(Class<K> cls, String label) {
        if (label == null)
            return null;
        K keyword = genericKeywordDao.findByLabel(cls, label);
        if (keyword == null) {
            try {
                keyword = cls.newInstance();
            } catch (Exception e) {
                throw new TdarRecoverableRuntimeException("could not create keyword class");
            }
            keyword.setLabel(label);
            getDao().save(keyword);
        } else {
            keyword.setStatus(Status.ACTIVE);
        }
        return keyword;
    }

    public List<Pair<CultureKeyword, Integer>> getUncontrolledCultureKeywordStats() {
        return genericKeywordDao.getUncontrolledCultureKeywordStats();
    }

    public List<Pair<CultureKeyword, Integer>> getControlledCultureKeywordStats() {
        return genericKeywordDao.getControlledCultureKeywordStats();
    }

    public List<Pair<GeographicKeyword, Integer>> getGeographicKeywordStats() {
        return genericKeywordDao.getGeographicKeywordStats();
    }

    public List<Pair<InvestigationType, Integer>> getInvestigationTypeStats() {
        return genericKeywordDao.getInvestigationTypeStats();
    }

    public List<Pair<MaterialKeyword, Integer>> getMaterialKeywordStats() {
        return genericKeywordDao.getMaterialKeywordStats();
    }

    public List<Pair<OtherKeyword, Integer>> getOtherKeywordStats() {
        return genericKeywordDao.getOtherKeywordStats();
    }

    public List<Pair<SiteNameKeyword, Integer>> getSiteNameKeywordStats() {
        return genericKeywordDao.getSiteNameKeywordStats();
    }

    public List<Pair<SiteTypeKeyword, Integer>> getControlledSiteTypeKeywordStats() {
        return genericKeywordDao.getControlledSiteTypeKeywordStats();
    }

    public List<Pair<SiteTypeKeyword, Integer>> getUncontrolledSiteTypeKeywordStats() {
        return genericKeywordDao.getUncontrolledSiteTypeKeywordStats();
    }

    public List<Pair<TemporalKeyword, Integer>> getTemporalKeywordStats() {
        return genericKeywordDao.getTemporalKeywordStats();
    }

    @Transactional
    public void updateOccurranceValues() {
        genericKeywordDao.updateOccuranceValues();
        cache.clear();
    }

}
