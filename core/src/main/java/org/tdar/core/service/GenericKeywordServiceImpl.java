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
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.transform.jsonld.SchemaOrgKeywordTransformer;
import org.tdar.utils.Pair;
import org.tdar.utils.PersistableUtils;

/*
 * A generic service to support the majority of keyword functions.
 */
@Service("genericKeywordSerice")
public class GenericKeywordServiceImpl implements GenericKeywordService {

    private static final String APPROVED = "approved";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("genericKeywordDao")
    private GenericKeywordDao genericKeywordDao;

    @Autowired
    private SerializationService serializationService;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#findAllApproved(java.lang.Class)
     */
    @Override
    @Transactional
    public <W extends SuggestedKeyword> List<W> findAllApproved(Class<W> cls) {
        return genericKeywordDao.findAllByProperty(cls, APPROVED, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#findAllApprovedWithCache(java.lang.Class)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#findAllDescendants(java.lang.Class, H)
     */
    @Override
    @Transactional
    public <H extends HierarchicalKeyword<H>> List<H> findAllDescendants(Class<H> cls, H keyword) {
        return genericKeywordDao.findAllDescendants(cls, keyword);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#findOrCreateByLabels(java.lang.Class, java.util.List)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#findByLabel(java.lang.Class, java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public <K extends Keyword> K findByLabel(Class<K> cls, String label) {
        if (label == null) {
            return null;
        }
        return genericKeywordDao.findByLabel(cls, label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#findOrCreateByLabel(java.lang.Class, java.lang.String)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#getUncontrolledCultureKeywordStats()
     */
    @Override
    @Transactional
    public List<Pair<CultureKeyword, Integer>> getUncontrolledCultureKeywordStats() {
        return genericKeywordDao.getUncontrolledCultureKeywordStats();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#getControlledCultureKeywordStats()
     */
    @Override
    @Transactional
    public List<Pair<CultureKeyword, Integer>> getControlledCultureKeywordStats() {
        return genericKeywordDao.getControlledCultureKeywordStats();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#getGeographicKeywordStats()
     */
    @Override
    @Transactional
    public List<Pair<GeographicKeyword, Integer>> getGeographicKeywordStats() {
        return genericKeywordDao.getGeographicKeywordStats();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#getInvestigationTypeStats()
     */
    @Override
    @Transactional
    public List<Pair<InvestigationType, Integer>> getInvestigationTypeStats() {
        return genericKeywordDao.getInvestigationTypeStats();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#getMaterialKeywordStats()
     */
    @Override
    @Transactional
    public List<Pair<MaterialKeyword, Integer>> getMaterialKeywordStats() {
        return genericKeywordDao.getMaterialKeywordStats();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#getOtherKeywordStats()
     */
    @Override
    @Transactional
    public List<Pair<OtherKeyword, Integer>> getOtherKeywordStats() {
        return genericKeywordDao.getOtherKeywordStats();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#getSiteNameKeywordStats()
     */
    @Override
    @Transactional
    public List<Pair<SiteNameKeyword, Integer>> getSiteNameKeywordStats() {
        return genericKeywordDao.getSiteNameKeywordStats();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#getControlledSiteTypeKeywordStats()
     */
    @Override
    @Transactional
    public List<Pair<SiteTypeKeyword, Integer>> getControlledSiteTypeKeywordStats() {
        return genericKeywordDao.getControlledSiteTypeKeywordStats();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#getUncontrolledSiteTypeKeywordStats()
     */
    @Override
    @Transactional
    public List<Pair<SiteTypeKeyword, Integer>> getUncontrolledSiteTypeKeywordStats() {
        return genericKeywordDao.getUncontrolledSiteTypeKeywordStats();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#getTemporalKeywordStats()
     */
    @Override
    @Transactional
    public List<Pair<TemporalKeyword, Integer>> getTemporalKeywordStats() {
        return genericKeywordDao.getTemporalKeywordStats();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#updateOccurranceValues()
     */
    @Override
    @Transactional
    public void updateOccurranceValues() {
        genericKeywordDao.updateOccuranceValues();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#findAuthority(org.tdar.core.bean.keyword.Keyword)
     */
    @Override
    @Transactional
    public Keyword findAuthority(Keyword kwd) {
        return genericKeywordDao.findAuthority(kwd);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#saveKeyword(java.lang.String, java.lang.String, org.tdar.core.bean.keyword.Keyword, java.util.List)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#countActiveKeyword(org.tdar.core.bean.keyword.KeywordType, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public Number countActiveKeyword(KeywordType type, boolean controlled) {
        return genericKeywordDao.countActiveWithStatus(type, controlled);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#countActiveKeyword(org.tdar.core.bean.keyword.KeywordType)
     */
    @Override
    @Transactional(readOnly = true)
    public Number countActiveKeyword(KeywordType type) {
        return genericKeywordDao.countActiveWithStatus(type, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#getSchemaOrgJsonLD(org.tdar.core.bean.keyword.Keyword)
     */
    @Override
    @Transactional(readOnly = true)
    public String getSchemaOrgJsonLD(Keyword keyword) {
        try {
            SchemaOrgKeywordTransformer transformer = new SchemaOrgKeywordTransformer();
            return transformer.convert(serializationService, keyword);
        } catch (Exception e) {
            logger.error("error converting to json-ld", e);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericKeywordService#findGeographicKeywordByCode(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public GeographicKeyword findGeographicKeywordByCode(String code) {
        return genericKeywordDao.findByProperty(GeographicKeyword.class, "code", StringUtils.upperCase(code));
    }

}
