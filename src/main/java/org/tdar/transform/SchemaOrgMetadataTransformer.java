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
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.UrlService;

public class SchemaOrgMetadataTransformer implements Serializable {

    private static final String PUBLISHER = "publisher";

    private static final long serialVersionUID = -5903659479081408357L;

    Map<String, Object> jsonLd = new HashMap<String, Object>();

    public String convert(SerializationService ss, Resource r) throws IOException {
        jsonLd.put("name", r.getTitle());
        jsonLd.put("description", r.getDescription());
        switch (r.getResourceType()) {
            case AUDIO:
                jsonLd.put("@type", "Audio");
                break;
            case DATASET:
            case GEOSPATIAL:
            case SENSORY_DATA:
                jsonLd.put("@type", "Dataset");
                break;
            case DOCUMENT:
                jsonLd.put("@type", "Book");
                break;
            case IMAGE:
                jsonLd.put("@type", "Image");
                break;
            case PROJECT:
                jsonLd.put("@type", "CollectionPage");
                break;
            case VIDEO:
                jsonLd.put("@type", "Video");
                break;
            default:
                jsonLd.put("@type", "CreativeWork");
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
            
            if (ir instanceof Document) {
                Document doc = (Document) ir;
                jsonLd.put("@type", "Book");
                add(jsonLd, "alternateName", doc.getSeriesName());
                add(jsonLd, "isbn", doc.getIsbn());
                add(jsonLd, "bookEdition", doc.getEdition());
                add(jsonLd, "volumeNumber", doc.getVolume());
                add(jsonLd, "issueNumber", doc.getSeriesNumber());

                if (doc.getDocumentType() == DocumentType.JOURNAL_ARTICLE) {
                    Map<String, Object> isPartOf = new HashMap<>();
                    add(isPartOf, "@id", "#periodical");
                    List<String> list = Arrays.asList("PublicationVolume", "Periodical");
                    isPartOf.put("@type", list);
                    add(isPartOf, "name", doc.getJournalName());
                    add(isPartOf, "issn", doc.getIssn());
                    add(isPartOf, "volumeNumber", doc.getVolume());
                    add(isPartOf, PUBLISHER, ir.getPublisher().getName());
                    jsonLd.remove(PUBLISHER);
                    List<Object> graph = new ArrayList<>();
                    Map<String, Object> issue = new HashMap<>();
                    Map<String, Object> article = new HashMap<>();
                    graph.add(issue);
                    graph.add(article);
                    issue.put("@id", "#issue");
                    issue.put("@type", "PublicationIssue");
                    add(issue, "issueNumber", doc.getSeriesNumber());
                    issue.put("datePublished", doc.getDate());
                    issue.put("isPartOf", isPartOf);
                    
                    article.put("@type", "ScholarlyArticle");
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
