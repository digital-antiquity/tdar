package org.tdar.core.service;

import java.util.List;
import java.util.Set;

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
import org.tdar.utils.Pair;

public interface GenericKeywordService {

    /**
     * Find all approved keywords without the cache
     * 
     * @param cls
     * @return
     */
    <W extends SuggestedKeyword> List<W> findAllApproved(Class<W> cls);

    /**
     * Find all Approved keywords (controlled) from the keyword cache
     *
     * @deprecated This method no longer caches results. Consider using Hibernate query caches instead.
     * @param cls
     * @return
     */
    <W extends SuggestedKeyword> List<W> findAllApprovedWithCache(Class<W> cls);

    /**
     * Find all @link HierarchicalKeyword entries that are children of the specified class
     * 
     * @param cls
     * @param keyword
     * @return
     */
    <H extends HierarchicalKeyword<H>> List<H> findAllDescendants(Class<H> cls, H keyword);

    /**
     * Find or create a set of keywords by label
     * 
     * @see #findOrCreateByLabel(Class, String)
     * 
     * @param cls
     * @param labels
     * @return
     */
    <K extends Keyword> Set<K> findOrCreateByLabels(Class<K> cls, List<String> labels);

    /**
     * Find a keyword of class by label
     * 
     * @param cls
     * @param label
     * @return
     */
    <K extends Keyword> K findByLabel(Class<K> cls, String label);

    /**
     * Find or Create a keyword of specified class by Label
     * 
     * @param cls
     * @param label
     * @return
     */
    <K extends Keyword> K findOrCreateByLabel(Class<K> cls, String label_);

    /**
     * Get uncontrolled Culture keyword occurrence stats
     * 
     * @return
     */
    List<Pair<CultureKeyword, Integer>> getUncontrolledCultureKeywordStats();

    /**
     * Get controlled Culture keyword occurrence stats
     * 
     * @return
     */
    List<Pair<CultureKeyword, Integer>> getControlledCultureKeywordStats();

    /**
     * Get Geographic keyword occurrence stats
     * 
     * @return
     */
    List<Pair<GeographicKeyword, Integer>> getGeographicKeywordStats();

    /**
     * Get InvestigationType keyword occurrence stats
     * 
     * @return
     */
    List<Pair<InvestigationType, Integer>> getInvestigationTypeStats();

    /**
     * Get Material keyword occurrence stats
     * 
     * @return
     */
    List<Pair<MaterialKeyword, Integer>> getMaterialKeywordStats();

    /**
     * Get Other keyword occurrence stats
     * 
     * @return
     */
    List<Pair<OtherKeyword, Integer>> getOtherKeywordStats();

    /**
     * Get SiteName keyword occurrence stats
     * 
     * @return
     */
    List<Pair<SiteNameKeyword, Integer>> getSiteNameKeywordStats();

    /**
     * Get controlled SiteType Keyword occurrence stats
     * 
     * @return
     */
    List<Pair<SiteTypeKeyword, Integer>> getControlledSiteTypeKeywordStats();

    /**
     * Get Uncontrolled siteType keyword occurrence stats
     * 
     * @return
     */
    List<Pair<SiteTypeKeyword, Integer>> getUncontrolledSiteTypeKeywordStats();

    /**
     * Get temporal keyword occurrence stats
     * 
     * @return
     */
    List<Pair<TemporalKeyword, Integer>> getTemporalKeywordStats();

    /**
     * Update the occurrence values for all @link Keyword entries. Clear the cache so these numbers are updated too
     */
    void updateOccurranceValues();

    /**
     * Find the Authority version of a @link Keyword based on the duplicate
     * 
     * @param kwd
     * @return
     */
    Keyword findAuthority(Keyword kwd);

    void saveKeyword(String label, String description, Keyword keyword, List<ExternalKeywordMapping> list);

    Number countActiveKeyword(KeywordType type, boolean controlled);

    Number countActiveKeyword(KeywordType type);

    String getSchemaOrgJsonLD(Keyword keyword);

    GeographicKeyword findGeographicKeywordByCode(String code);

}