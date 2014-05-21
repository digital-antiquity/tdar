package org.tdar.core.service;

import java.util.Arrays;
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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("genericKeywordDao")
    private GenericKeywordDao genericKeywordDao;

    /**
     * Find all approved keywords without the cache
     * 
     * @param cls
     * @return
     */
    @Transactional
    public <W extends SuggestedKeyword> List<W> findAllApproved(Class<W> cls) {
        return genericKeywordDao.findAllByProperty(cls, "approved", true);
    }

    private Map<Class<?>, List<?>> cache = new ConcurrentHashMap<Class<?>, List<?>>();

    /**
     * Find all Approved keywords (controlled) from the keyword cache
     * 
     * @param cls
     * @return
     */
    @SuppressWarnings("unchecked")
    public synchronized <W extends SuggestedKeyword> List<W> findAllApprovedWithCache(Class<W> cls) {
        if (!cache.containsKey(cls)) {
            cache.put(cls, findAllApproved(cls));
        }
        return (List<W>) cache.get(cls);
    }

    /**
     * Find all @link HierarchicalKeyword entries that are children of the specified class
     * 
     * @param cls
     * @param keyword
     * @return
     */
    @Transactional
    public <H extends HierarchicalKeyword<H>> List<H> findAllDescendants(Class<H> cls, H keyword) {
        return genericKeywordDao.findAllDescendants(cls, keyword);
    }

    /**
     * Find or create a set of keywords by label
     * 
     * @see #findOrCreateByLabel(Class, String)
     * 
     * @param cls
     * @param labels
     * @return
     */
    @Transactional(readOnly = false)
    public <K extends Keyword> Set<K> findOrCreateByLabels(Class<K> cls, List<String> labels) {
        if (CollectionUtils.isEmpty(labels)) {
            return new HashSet<K>();
        }
        Set<K> set = new HashSet<K>();
        for (String label : labels) {
            if (StringUtils.isBlank(label)) {
                logger.trace("Skipping empty keyword label: " + label);
                continue;
            }
            logger.trace("find or create keyword:" + label);
            set.add(findOrCreateByLabel(cls, label));
        }
        return set;
    }

    /**
     * Find a keyword of class by label
     * 
     * @param cls
     * @param label
     * @return
     */
    @Transactional(readOnly = true)
    public <K extends Keyword> K findByLabel(Class<K> cls, String label) {
        if (label == null) {
            return null;
        }
        return genericKeywordDao.findByLabel(cls, label);
    }

    /**
     * Find or Create a keyword of specified class by Label
     * 
     * @param cls
     * @param label
     * @return
     */
    @Transactional(readOnly = false)
    public <K extends Keyword> K findOrCreateByLabel(Class<K> cls, String label) {
        if (StringUtils.isBlank(label)) {
            return null;
        }
        // trim and strip quotes
        label = label.trim();
        if (label.startsWith("\"") && label.endsWith("\"") ) {
          label = label.substring(1,label.length() - 1);
        }
        K keyword = genericKeywordDao.findByLabel(cls, label);
        if (keyword == null) {
            try {
                keyword = cls.newInstance();
            } catch (Exception e) {
                throw new TdarRecoverableRuntimeException("error.could_not_create_class", Arrays.asList(cls));
            }
            keyword.setLabel(label);
            genericKeywordDao.save(keyword);
        } else {
            keyword.setStatus(Status.ACTIVE);
        }
        return keyword;
    }

    /**
     * Get uncontrolled Culture keyword occurrence stats
     * 
     * @return
     */
    @Transactional
    public List<Pair<CultureKeyword, Integer>> getUncontrolledCultureKeywordStats() {
        return genericKeywordDao.getUncontrolledCultureKeywordStats();
    }

    /**
     * Get controlled Culture keyword occurrence stats
     * 
     * @return
     */
    @Transactional
    public List<Pair<CultureKeyword, Integer>> getControlledCultureKeywordStats() {
        return genericKeywordDao.getControlledCultureKeywordStats();
    }

    /**
     * Get Geographic keyword occurrence stats
     * 
     * @return
     */
    @Transactional
    public List<Pair<GeographicKeyword, Integer>> getGeographicKeywordStats() {
        return genericKeywordDao.getGeographicKeywordStats();
    }

    /**
     * Get InvestigationType keyword occurrence stats
     * 
     * @return
     */
    @Transactional
    public List<Pair<InvestigationType, Integer>> getInvestigationTypeStats() {
        return genericKeywordDao.getInvestigationTypeStats();
    }

    /**
     * Get Material keyword occurrence stats
     * 
     * @return
     */
    @Transactional
    public List<Pair<MaterialKeyword, Integer>> getMaterialKeywordStats() {
        return genericKeywordDao.getMaterialKeywordStats();
    }

    /**
     * Get Other keyword occurrence stats
     * 
     * @return
     */
    @Transactional
    public List<Pair<OtherKeyword, Integer>> getOtherKeywordStats() {
        return genericKeywordDao.getOtherKeywordStats();
    }

    /**
     * Get SiteName keyword occurrence stats
     * 
     * @return
     */
    @Transactional
    public List<Pair<SiteNameKeyword, Integer>> getSiteNameKeywordStats() {
        return genericKeywordDao.getSiteNameKeywordStats();
    }

    /**
     * Get controlled SiteType Keyword occurrence stats
     * 
     * @return
     */
    @Transactional
    public List<Pair<SiteTypeKeyword, Integer>> getControlledSiteTypeKeywordStats() {
        return genericKeywordDao.getControlledSiteTypeKeywordStats();
    }

    /**
     * Get Uncontrolled siteType keyword occurrence stats
     * 
     * @return
     */
    @Transactional
    public List<Pair<SiteTypeKeyword, Integer>> getUncontrolledSiteTypeKeywordStats() {
        return genericKeywordDao.getUncontrolledSiteTypeKeywordStats();
    }

    /**
     * Get temporal keyword occurrence stats
     * 
     * @return
     */
    @Transactional
    public List<Pair<TemporalKeyword, Integer>> getTemporalKeywordStats() {
        return genericKeywordDao.getTemporalKeywordStats();
    }

    /**
     * Update the occurrence values for all @link Keyword entries. Clear the cache so these numbers are updated too
     */
    @Transactional
    public void updateOccurranceValues() {
        genericKeywordDao.updateOccuranceValues();
        cache.clear();
    }

    /**
     * Find the Authority version of a @link Keyword based on the duplicate
     * 
     * @param kwd
     * @return
     */
    @Transactional
    public Keyword findAuthority(Keyword kwd) {
        return genericKeywordDao.findAuthority(kwd);
    }

}
