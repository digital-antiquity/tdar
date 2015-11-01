package org.tdar.transform;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.service.UrlService;

public class ScholarMetadataTransformer implements Serializable {

    private static final long serialVersionUID = 6885895727458189965L;

    public List<MetaTag> convertResourceToMetaTag(Resource resource) {
        List<MetaTag> toReturn = new ArrayList<MetaTag>();
        addMetaTag(toReturn, "citation_title", resource.getTitle());
        for (ResourceCreator creator : resource.getPrimaryCreators()) {
            addMetaTag(toReturn, "citation_author", creator.getCreator().getProperName());
        }
        if (resource instanceof InformationResource) {
            String publisherField = "DC.publisher";
            InformationResource ir = (InformationResource) resource;
            addMetaTag(toReturn, "citation_date", ir.getDate().toString());

            for (InformationResourceFile file : ir.getInformationResourceFiles()) {
                if (file.getLatestPDF() != null && file.isViewable()) {
                    addMetaTag(toReturn, "citation_pdf_url", UrlService.downloadUrl(file.getLatestPDF()));
                }
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            addMetaTag(toReturn, "citation_online_date", format.format(resource.getDateCreated()));
            if (ir instanceof Document) {
                Document doc = (Document) ir;
                switch (doc.getDocumentType()) {
                    case CONFERENCE_PRESENTATION:
                        publisherField = "citation_conference_title";
                        break;
                    case JOURNAL_ARTICLE:
                        addMetaTagIfNotBlank(toReturn, "citation_journal_title", doc.getJournalName());
                        break;
                    case THESIS:
                        publisherField = "citation_dissertation_institution";
                        break;
                    default:
                        break;
                }
                addMetaTagIfNotBlank(toReturn, "citation_volume", doc.getVolume());
                addMetaTagIfNotBlank(toReturn, "citation_issue", doc.getJournalNumber());
                addMetaTagIfNotBlank(toReturn, "citation_issn", doc.getIssn());
                addMetaTagIfNotBlank(toReturn, "citation_isbn", doc.getIsbn());
                addMetaTagIfNotBlank(toReturn, "citation_firstpage", doc.getStartPage());
                addMetaTagIfNotBlank(toReturn, "citation_lastpage", doc.getEndPage());
            }
            addMetaTagIfNotBlank(toReturn, publisherField, ir.getPublisherName());
        }
        return toReturn;
    }

    private void addMetaTagIfNotBlank(List<MetaTag> toReturn, String name, String val) {
        if (StringUtils.isNotBlank(val)) {
            addMetaTag(toReturn, name, val);
        }
    }

    private void addMetaTag(List<MetaTag> toReturn, String name, String content) {
        toReturn.add(new MetaTag(name, content));
    }

}
