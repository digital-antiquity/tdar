package org.tdar.transform.jsonld;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.RelationType;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.UrlService;
import org.tdar.utils.PersistableUtils;

/**
 * Convert a persistable to a proper Schema.org / JSON LD String
 * 
 * @author abrin
 *
 */
public abstract class AbstractSchemaOrgMetadataTransformer implements Serializable {

    private static final String SCHEMA_URL = "schema:url";
    static final String SCHEMA = "schema";
    static final String LOCAL_PREFIX = "tdar";
    static final String HTTP_SCHEMA_ORG = "http://schema.org";
    static final String SCHEMA_DESCRIPTION = "schema:description";
    static final String DATE_PUBLISHED = "schema:datePublished";
    static final String GRAPH = "@graph";
    static final String CONTEXT = "@context";
    static final String NAME = "schema:name";
    static final String TYPE = "@type";
    static final String ID = "@id";
    static final String PUBLISHER = "schema:publisher";
    private List<Map<String, Object>> graph = new ArrayList<>();

    final transient Logger logger = LoggerFactory.getLogger(getClass());
    static final long serialVersionUID = -5903659479081408357L;

    /*
     * the context section adds all of the prefixes and URIs for the various schema being processed.
     */
    protected void addContextSection(Map<String, Object> jsonLd) {
        Map<String, String> context = new HashMap<>();
        context.put(SCHEMA, HTTP_SCHEMA_ORG);
        context.put(LOCAL_PREFIX, TdarConfiguration.getInstance().getBaseUrl());
        for (RelationType type : RelationType.values()) {
            context.put(type.getPrefix(), type.getUri());
        }
        jsonLd.put(CONTEXT, context);

    }

    protected void addGraphSectionSpatial(Set<LatitudeLongitudeBox> llbs) {
        List<Map<String, Object>> all = new ArrayList<>();
        llbs.forEach(llb -> {
            Map<String, Object> js = new HashMap<>();
            js.put("tdar:id", llb.getId());
            if (llb.getObfuscatedNorth() == null) {
                llb.obfuscateAll();
            }
            js.put("schema:longitude", llb.getObfuscatedCenterLongitude());
            js.put("schema:latitude", llb.getObfuscatedCenterLatitude());
            js.put("tdar:note", "possibly obfuscated");
            js.put("tdar:north", llb.getObfuscatedNorth());
            js.put("tdar:south", llb.getObfuscatedSouth());
            js.put("tdar:east", llb.getObfuscatedEast());
            js.put("tdar:west", llb.getObfuscatedWest());
            js.put(TYPE, "GeoCoordinates");
        });
        appendIfNotEmpty("tdar:spatial", all);

    }

    private void appendIfNotEmpty(String nodeName, List<Map<String, Object>> all) {
        if (all.size() > 0) {
            Map<String, Object> obj = new HashMap<>();
            obj.put(nodeName, all);
            getGraph().add(obj);
        }
    }

    protected void addGraphSectionTemporal(Set<CoverageDate> coverages) {
        List<Map<String, Object>> all = new ArrayList<>();
        coverages.forEach(coverage -> {
            Map<String, Object> js = new HashMap<>();
            js.put("tdar:id", coverage.getId());
            js.put("tdar:start", coverage.getStartDate());
            js.put("tdar:end", coverage.getEndDate());
            js.put("tdar:description", coverage.getDescription());
            js.put("tdar:type", coverage.getDateType().name());
            all.add(js);
        });
        appendIfNotEmpty("tdar:temporal", all);

    }

    /*
     * a JSON LD object can have multiple "graphs" each graph with a unique name. This section is for a set of similar keywords
     */
    @SuppressWarnings("unchecked")
    protected void addGraphSection(Set<? extends Keyword> keywords, String nodeName) {
        List<Map<String, Object>> all = new ArrayList<>();
        if (logger.isTraceEnabled()) {
            logger.trace("adding keywords:{}", keywords);
        }
        keywords.forEach(kwd -> {
            Map<String, Object> js = new HashMap<>();
            js.put("tdar:name", kwd.getLabel());
            js.put("tdar:id", kwd.getId());
            js.put("tdar:url", UrlService.absoluteUrl(kwd));
            kwd.getAssertions().forEach(map -> {
                js.put(map.getRelationType().getJsonKey(), map.getRelation());
            });

            for (Keyword syn : (Set<Keyword>) kwd.getSynonyms()) {
                js.put(RelationType.HAS_VERSION.getJsonKey(), syn.getDetailUrl());
            }

            all.add(js);
        });
        appendIfNotEmpty(nodeName, all);

    }

    /*
     * Add a graph section for a resource
     */
    protected void addGraphSection(Resource r) {
        Map<String, Object> jsonLd = new HashMap<>();
        getGraph().add(jsonLd);
        jsonLd.put(NAME, r.getTitle());
        jsonLd.put(SCHEMA_DESCRIPTION, r.getDescription());
        switch (r.getResourceType()) {
            case AUDIO:
                jsonLd.put(TYPE, "AudioObject");
                break;
            case DATASET:
            case GEOSPATIAL:
            case SENSORY_DATA:
                jsonLd.put(TYPE, "Dataset");
                break;
            case DOCUMENT:
                jsonLd.put(TYPE, "Book");
                break;
            case IMAGE:
                jsonLd.put(TYPE, "ImageObject");
                break;
            case PROJECT:
                jsonLd.put(TYPE, "CollectionPage");
                break;
            case VIDEO:
                jsonLd.put(TYPE, "VideoObject");
                break;
            default:
                jsonLd.put(TYPE, "CreativeWork");
                break;

        }

        for (ResourceCreator rc : r.getActiveResourceCreators()) {
            if (rc.getRole().isPartOfSchemaOrg()) {
                jsonLd.put(rc.getRole().getSchemaOrgLabel(), rc.getCreator().getProperName());
            }
        }

        if (StringUtils.isNotBlank(r.getUrl())) {
            add(jsonLd, SCHEMA_URL, r.getUrl());
        } else {
            add(jsonLd, SCHEMA_URL, UrlService.absoluteUrl(r));
        }

        if (r instanceof InformationResource) {
            InformationResource ir = (InformationResource) r;

            List<InformationResourceFile> thumbs = ir.getVisibleFilesWithThumbnails();
            if (CollectionUtils.isNotEmpty(thumbs)) {
                add(jsonLd, "schema:thumbnailUrl", UrlService.thumbnailUrl(thumbs.get(0).getLatestThumbnail()));
            }

            if (ir.getResourceProviderInstitution() != null) {
                add(jsonLd, "schema:provider", ir.getResourceProviderInstitution().getName());
            }

            if (ir.getPublisher() != null) {
                add(jsonLd, PUBLISHER, ir.getPublisher().getName());
            }

            add(jsonLd, "schema:sameAs", ir.getDoi());
            if (PersistableUtils.isNotNullOrTransient(ir.getDate())) {
                jsonLd.put(DATE_PUBLISHED, ir.getDate());
            }

            if (ir instanceof Document) {
                Document doc = (Document) ir;
                jsonLd.put(TYPE, "Book");
                add(jsonLd, "schema:alternateName", doc.getSeriesName());
                add(jsonLd, "schema:isbn", doc.getIsbn());
                add(jsonLd, "schema:bookEdition", doc.getEdition());
                add(jsonLd, "schema:volumeNumber", doc.getVolume());

                if (doc.getDocumentType() == DocumentType.JOURNAL_ARTICLE) {
                    Map<String, Object> isPartOf = new HashMap<>();
                    add(isPartOf, ID, "#periodical");
                    add(isPartOf, TYPE, "Periodical");
                    add(isPartOf, NAME, doc.getJournalName());
                    add(isPartOf, "issn", doc.getIssn());
                    // Google as of 2/11/15 cannot handle multiple types combined, so we need to choose ISSN or volume #
                    // add(isPartOf, "volumeNumber", doc.getVolume());
                    if (ir.getPublisher() != null) {
                        add(isPartOf, PUBLISHER, ir.getPublisher().getName());
                    }
                    jsonLd.remove(PUBLISHER);
                    Map<String, Object> issue = new HashMap<>();
                    Map<String, Object> article = new HashMap<>();
                    getGraph().add(issue);
                    getGraph().add(article);
                    article.put("schema:headline", doc.getTitle());

                    article.put(DATE_PUBLISHED, doc.getDate());
                    issue.put(ID, "#issue");
                    issue.put(TYPE, "PublicationIssue");
                    add(issue, "schema:issueNumber", doc.getSeriesNumber());
                    issue.put(DATE_PUBLISHED, doc.getDate());
                    issue.put("schema:isPartOf", isPartOf);

                    article.put(TYPE, "ScholarlyArticle");
                    article.put("schema:isPartOf", "#issue");
                    add(article, SCHEMA_DESCRIPTION, doc.getDescription());
                    add(article, NAME, doc.getTitle());
                    add(article, "schema:pageStart", doc.getStartPage());
                    add(article, "schema:pageEnd", doc.getEndPage());
                    for (ResourceCreator rc : r.getActiveResourceCreators()) {
                        if (rc.getRole().isPartOfSchemaOrg()) {
                            article.put(rc.getRole().getSchemaOrgLabel(), rc.getCreator().getProperName());
                        }
                    }
                }
            }
        }
    }

    /*
     * Add a key/value pair to a map if the value is not blank
     */
    protected void add(Map<String, Object> map, String key, String val) {
        if (StringUtils.isNotBlank(val)) {
            map.put(key, val);
        }
    }

    public List<Map<String, Object>> getGraph() {
        return graph;
    }

    public void setGraph(List<Map<String, Object>> graph) {
        this.graph = graph;
    }

}
