package org.tdar.transform;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.UrlService;
import org.tdar.utils.PersistableUtils;

public class SchemaOrgMetadataTransformer implements Serializable {

    private static final String NAME = "name";
    private static final String TYPE = "@type";
    private static final String ID = "@id";
    private static final String PUBLISHER = "publisher";

    private static final long serialVersionUID = -5903659479081408357L;


    public String convert(SerializationService ss, Resource r) throws IOException {
        Map<String, Object> jsonLd = new HashMap<String, Object>();
        jsonLd.put(NAME, r.getTitle());
        jsonLd.put("description", r.getDescription());
        switch (r.getResourceType()) {
            case AUDIO:
                jsonLd.put(TYPE, "Audio");
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
                jsonLd.put(TYPE, "Image");
                break;
            case PROJECT:
                jsonLd.put(TYPE, "CollectionPage");
                break;
            case VIDEO:
                jsonLd.put(TYPE, "Video");
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

        if (r instanceof InformationResource) {
            InformationResource ir = (InformationResource) r;
            add(jsonLd, "url", ir.getUrl());

            List<InformationResourceFile> thumbs = ir.getVisibleFilesWithThumbnails();
            if (CollectionUtils.isNotEmpty(thumbs)) {
                add(jsonLd, "thumbnailUrl", UrlService.thumbnailUrl(thumbs.get(0).getLatestThumbnail()));
            }

            if (ir.getResourceProviderInstitution() != null) {
                add(jsonLd, "provider", ir.getResourceProviderInstitution().getName());
            }

            if (ir.getPublisher() != null) {
                add(jsonLd, PUBLISHER, ir.getPublisher().getName());
            }
            
            add(jsonLd, "sameAs", ir.getDoi());
            if (PersistableUtils.isNotNullOrTransient(ir.getDate())) {
                jsonLd.put("datePublished", ir.getDate());
            }

            if (ir instanceof Document) {
                Document doc = (Document) ir;
                jsonLd.put(TYPE, "Book");
                add(jsonLd, "alternateName", doc.getSeriesName());
                add(jsonLd, "isbn", doc.getIsbn());
                add(jsonLd, "bookEdition", doc.getEdition());
                add(jsonLd, "volumeNumber", doc.getVolume());

                if (doc.getDocumentType() == DocumentType.JOURNAL_ARTICLE) {
                    Map<String, Object> isPartOf = new HashMap<>();
                    add(isPartOf, ID, "#periodical");
                    List<String> list = Arrays.asList("PublicationVolume", "Periodical");
                    isPartOf.put(TYPE, list);
                    add(isPartOf, NAME, doc.getJournalName());
                    add(isPartOf, "issn", doc.getIssn());
                    add(isPartOf, "volumeNumber", doc.getVolume());
                    add(isPartOf, PUBLISHER, ir.getPublisher().getName());
                    jsonLd.remove(PUBLISHER);
                    List<Object> graph = new ArrayList<>();
                    Map<String, Object> issue = new HashMap<>();
                    Map<String, Object> article = new HashMap<>();
                    graph.add(issue);
                    graph.add(article);
                    issue.put(ID, "#issue");
                    issue.put(TYPE, "PublicationIssue");
                    add(issue, "issueNumber", doc.getSeriesNumber());
                    issue.put("datePublished", doc.getDate());
                    issue.put("isPartOf", isPartOf);
                    
                    article.put(TYPE, "ScholarlyArticle");
                    article.put("isPartOf", "#issue");
                    add(article, "description", doc.getDescription());
                    add(article, "title", doc.getDescription());
                    add(article, "pageStart", doc.getStartPage());
                    add(article, "pageEnd", doc.getEndPage());
                    for (ResourceCreator rc : r.getActiveResourceCreators()) {
                        if (rc.getRole().isPartOfSchemaOrg()) {
                            article.put(rc.getRole().getSchemaOrgLabel(), rc.getCreator().getProperName());
                        }
                    }
                    jsonLd.clear();
                    jsonLd.put("@graph", graph);
                }
            }
            jsonLd.put("@context", "http://schema.org");
        }

        return ss.convertToJson(jsonLd);
    }

    private void add(Map<String, Object> map, String key, String val) {
        if (StringUtils.isNotBlank(val)) {
            map.put(key, val);
        }
    }

}
