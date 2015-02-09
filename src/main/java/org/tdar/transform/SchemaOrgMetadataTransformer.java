package org.tdar.transform;

import java.io.IOException;
import java.io.Serializable;
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

    private static final long serialVersionUID = -5903659479081408357L;

    Map<String, Object> jsonLd = new HashMap<String, Object>();

    public String convert(SerializationService ss, Resource r) throws IOException {
        jsonLd.put("@context", "http://schema.org");
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
            jsonLd.put(rc.getRole().getSchemaOrgLabel(), rc.getCreator().getProperName());
        }

        if (r instanceof InformationResource) {
            InformationResource ir = (InformationResource) r;
            if (StringUtils.isNotBlank(ir.getUrl())) {
                jsonLd.put("url", ir.getUrl());
            }

            List<InformationResourceFile> thumbs = ir.getVisibleFilesWithThumbnails();
            if (CollectionUtils.isNotEmpty(thumbs)) {
                jsonLd.put("thumbnailUrl", UrlService.thumbnailUrl(thumbs.get(0).getLatestThumbnail()));
            }

            if (ir.getResourceProviderInstitution() != null) {
                jsonLd.put("provider", ir.getResourceProviderInstitution().getName());
            }

            if (ir.getPublisher() != null) {
                jsonLd.put("publisher", ir.getPublisher().getName());
            }

            if (ir instanceof Document) {
                Document doc = (Document) ir;
                jsonLd.put("@type", "Book");
                if (StringUtils.isNotBlank(doc.getSeriesName())) {
                    jsonLd.put("alternateName", doc.getSeriesName());
                }
                if (doc.getIsbn() != null) {
                    jsonLd.put("isbn", doc.getIsbn());
                }
                if (doc.getIssn() != null) {
                    jsonLd.put("issn", doc.getIssn());
                }
                if (StringUtils.isNotBlank(doc.getEdition())) {
                    jsonLd.put("bookEdition", doc.getEdition());
                }

                if (doc.getDocumentType() == DocumentType.JOURNAL_ARTICLE) {
                    // FIXME: http://schema.org/Article
                    jsonLd.put("@type", "ScholarlyArticle");
                    if (StringUtils.isNotBlank(doc.getJournalName())) {
                        jsonLd.put("alternateName", doc.getJournalName());
                    }

                    if (StringUtils.isNotBlank(doc.getStartPage())) {
                        jsonLd.put("pageStart", doc.getStartPage());
                    }
                    if (StringUtils.isNotBlank(doc.getEndPage())) {
                        jsonLd.put("pageEnd", doc.getEndPage());
                    }
                }
                if (StringUtils.isNotBlank(doc.getVolume())) {
                    jsonLd.put("volumeNumber", doc.getVolume());
                }

                if (StringUtils.isNotBlank(doc.getSeriesNumber())) {
                    jsonLd.put("issueNumber", doc.getSeriesNumber());
                }
            }
        }

        return ss.convertToJson(jsonLd);
    }

}
