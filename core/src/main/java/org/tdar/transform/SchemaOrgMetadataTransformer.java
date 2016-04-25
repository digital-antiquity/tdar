package org.tdar.transform;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.UrlService;
import org.tdar.utils.PersistableUtils;

public class SchemaOrgMetadataTransformer implements Serializable {

    private static final String SCHEMA_ORG = "http://schema.org";
    private static final String CONTEXT = "@context";
    private static final String NAME = "name";
    private static final String TYPE = "@type";
    private static final String ID = "@id";
    private static final String PUBLISHER = "publisher";

    private static final long serialVersionUID = -5903659479081408357L;

    public String convert(SerializationService ss, Creator<?> creator, String imageUrl) throws IOException {
        Map<String, Object> jsonLd = new HashMap<String, Object>();
        if (creator == null) {
            return ss.convertToJson(jsonLd);
        }
        jsonLd.put(CONTEXT, SCHEMA_ORG);
        jsonLd.put(TYPE, "Organization");
        if (StringUtils.isNotBlank(creator.getUrl())) {
            add(jsonLd, "url", creator.getUrl());
        } else {
            add(jsonLd, "url", UrlService.absoluteUrl(creator));
        }
        add(jsonLd, NAME, creator.getProperName());
        add(jsonLd, "description", creator.getDescription());
        add(jsonLd, "image", imageUrl);
        if (creator instanceof Person) {
            Person person = (Person) creator;
            jsonLd.put(TYPE, "Person");
            if (person.getEmailPublic()) {
                add(jsonLd, "email", person.getEmail());
            }
            if (person.getPhonePublic()) {
                add(jsonLd, "telephone", person.getPhone());
            }
            add(jsonLd, "affiliation", person.getInstitutionName());

        } else {
            add(jsonLd, "logo", imageUrl);
        }
        if (CollectionUtils.isNotEmpty(creator.getAddresses())) {
            for (Address address : creator.getAddresses()) {
                if (address.getType() == AddressType.MAILING) {
                    Map<String, Object> addLd = new HashMap<String, Object>();
                    add(addLd, TYPE, "PostalAddress");
                    add(addLd, "addressLocality", address.getCity());
                    add(addLd, "addressRegion", address.getState());
                    add(addLd, "postalCode", address.getPostal());
                    String street = address.getStreet1();
                    if (StringUtils.isNotBlank(address.getStreet2())) {
                        street += "\n" + address.getStreet2();
                    }
                    add(addLd, "streetAddress", street);
                    jsonLd.put("address", addLd);
                    break;
                }
            }
        }

        return ss.convertToJson(jsonLd);
    }

    public String convert(SerializationService ss, Resource r) throws IOException {
        Map<String, Object> jsonLd = new HashMap<String, Object>();
        jsonLd.put(NAME, r.getTitle());
        jsonLd.put("description", r.getDescription());
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
            add(jsonLd, "url", r.getUrl());
        } else {
            add(jsonLd, "url", UrlService.absoluteUrl(r));
        }

        if (r instanceof InformationResource) {
            InformationResource ir = (InformationResource) r;

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
                    add(isPartOf, TYPE, "Periodical");
                    add(isPartOf, NAME, doc.getJournalName());
                    add(isPartOf, "issn", doc.getIssn());
                    // Google as of 2/11/15 cannot handle multiple types combined, so we need to choose ISSN or volume #
                    // add(isPartOf, "volumeNumber", doc.getVolume());
                    if (ir.getPublisher() != null) {
                        add(isPartOf, PUBLISHER, ir.getPublisher().getName());
                    }
                    jsonLd.remove(PUBLISHER);
                    List<Object> graph = new ArrayList<>();
                    Map<String, Object> issue = new HashMap<>();
                    Map<String, Object> article = new HashMap<>();
                    graph.add(issue);
                    graph.add(article);
                    article.put("headline", doc.getTitle());

                    article.put("datePublished", doc.getDate());
                    issue.put(ID, "#issue");
                    issue.put(TYPE, "PublicationIssue");
                    add(issue, "issueNumber", doc.getSeriesNumber());
                    issue.put("datePublished", doc.getDate());
                    issue.put("isPartOf", isPartOf);

                    article.put(TYPE, "ScholarlyArticle");
                    article.put("isPartOf", "#issue");
                    add(article, "description", doc.getDescription());
                    add(article, NAME, doc.getTitle());
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
            jsonLd.put(CONTEXT, SCHEMA_ORG);
        }
        return ss.convertToJson(jsonLd);
    }

    private void add(Map<String, Object> map, String key, String val) {
        if (StringUtils.isNotBlank(val)) {
            map.put(key, val);
        }
    }

}
