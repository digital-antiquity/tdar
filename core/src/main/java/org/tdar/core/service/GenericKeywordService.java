package org.tdar.core.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.ExternalKeywordMapping;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.SuggestedKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.GenericKeywordDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.transform.jsonld.SchemaOrgKeywordTransformer;
import org.tdar.utils.Pair;
import org.tdar.utils.PersistableUtils;

/*
 * A generic service to support the majority of keyword functions.
 */
@Service("genericKeywordSerice")
public class GenericKeywordService {

    private static final String APPROVED = "approved";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("genericKeywordDao")
    private GenericKeywordDao genericKeywordDao;

    @Autowired
    private SerializationService serializationService;

    /**
     * Find all approved keywords without the cache
     * 
     * @param cls
     * @return
     */
    @Transactional
    public <W extends SuggestedKeyword> List<W> findAllApproved(Class<W> cls) {
        return genericKeywordDao.findAllByProperty(cls, APPROVED, true);
    }

    @Deprecated
    /**
     * Find all Approved keywords (controlled) from the keyword cache
     *
     * @deprecated This method no longer caches results. Consider using Hibernate query caches instead.
     * @param cls
     * @return
     */
    public synchronized <W extends SuggestedKeyword> List<W> findAllApprovedWithCache(Class<W> cls) {
        return findAllApproved(cls);
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
    public <K extends Keyword> K findOrCreateByLabel(Class<K> cls, String label_) {
        String label = label_;
        if (StringUtils.isBlank(label)) {
            return null;
        }
        // trim and strip quotes
        label = label.trim();
        if (label.startsWith("\"") && label.endsWith("\"")) {
            label = label.substring(1, label.length() - 1);
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

    @Transactional(readOnly = false)
    public void saveKeyword(String label, String description, Keyword keyword, List<ExternalKeywordMapping> list) {
        keyword.setLabel(label);
        keyword.setDefinition(description);
        // FIXME: likely because of the weird-multi-managed relationship on keyword mapping, a manual delete needs to be handled
        Map<Long, ExternalKeywordMapping> incoming = PersistableUtils.createIdMap(list);
        for (ExternalKeywordMapping existing : keyword.getAssertions()) {
            ExternalKeywordMapping in = incoming.get(existing.getId());
            if (in != null) {
                logger.debug("updating existing to: {} {} ", in.getRelation(), in.getRelationType());
                existing.setRelation(in.getRelation());
                existing.setRelationType(in.getRelationType());
                genericKeywordDao.saveOrUpdate(existing);
            } else {
                logger.debug("deleting: {} ", existing.getId());
                genericKeywordDao.delete(existing);
            }
        }
        incoming = null;
        genericKeywordDao.saveOrUpdate(keyword);
        for (ExternalKeywordMapping map : list) {
            logger.debug("evaluating: {}", map);
            if (map != null && PersistableUtils.isNullOrTransient(map.getId())) {
                if (map.isValidForController()) {
                    logger.debug("adding: {}", map);
                    keyword.getAssertions().add(map);
                    genericKeywordDao.saveOrUpdate(map);
                } else {
                    logger.debug("skipping: {}", map);
                }
            }
        }
        logger.debug("result: {} -- {}", keyword, keyword.getAssertions());
        genericKeywordDao.saveOrUpdate(keyword);
    }

    @Transactional(readOnly = true)
    public Number countActiveKeyword(KeywordType type, boolean controlled) {
        return genericKeywordDao.countActiveWithStatus(type, controlled);
    }

    @Transactional(readOnly = true)
    public Number countActiveKeyword(KeywordType type) {
        return genericKeywordDao.countActiveWithStatus(type, null);
    }

    @Transactional(readOnly=true)
    public String getSchemaOrgJsonLD(Keyword keyword) {
        try {
            SchemaOrgKeywordTransformer transformer = new SchemaOrgKeywordTransformer();
            return transformer.convert(serializationService, keyword);
        } catch (Exception e) {
            logger.error("error converting to json-ld", e);
        }

        return null;
    }

}
