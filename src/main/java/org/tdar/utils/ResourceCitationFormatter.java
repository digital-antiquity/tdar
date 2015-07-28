package org.tdar.utils;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;

/**
 * This class extracts out all of the formatting for a citation using the TdarFormat. The goal here is to put logic in one place, but it could strongly benefit
 * from not being a Java class, and have better control flow logic than what's here. (Fremarker?)
 * 
 * @author abrin
 *
 */
public class ResourceCitationFormatter implements Serializable {
    private static final long serialVersionUID = -4055674012404120541L;
    private Resource resource;

    public ResourceCitationFormatter(Resource resource) {
        this.resource = resource;
    }

    public String getFormattedTitleInfo() {
        StringBuilder sb = new StringBuilder();
        appendIfNotBlank(sb, resource.getTitle(), "", "");
        if (resource instanceof Document) {
            appendIfNotBlank(sb, ((Document) resource).getEdition(), ",", "");
        }
        return sb.toString();
    }

    public String getFormattedAuthorList() {
        StringBuilder sb = new StringBuilder();
        for (ResourceCreator creator : resource.getPrimaryCreators()) {
            if ((creator.getRole() == ResourceCreatorRole.AUTHOR) || (creator.getRole() == ResourceCreatorRole.CREATOR)) {
                appendIfNotBlank(sb, creator.getCreator().getProperName(), ",", "");
            }
        }
        for (ResourceCreator creator : resource.getEditors()) {
            if (creator.getRole() == ResourceCreatorRole.EDITOR) {
                appendIfNotBlank(sb, creator.getCreator().getProperName(), ",", "");
            }
        }
        return sb.toString();
    }

    public String getFormattedSourceInformation() {
        StringBuilder sb = new StringBuilder();

        if (resource instanceof Document) {
            Document doc = (Document) resource;
            switch (doc.getDocumentType()) {
                case BOOK:
                    appendIfNotBlank(sb, doc.getSeriesName(), "", "");
                    appendIfNotBlank(sb, doc.getSeriesNumber(), "", ",");
                    if (StringUtils.isNotBlank(doc.getSeriesName()) || StringUtils.isNotBlank(doc.getSeriesNumber())) {
                        sb.append(".");
                    }
                    appendIfNotBlank(sb, doc.getPublisherLocation(), "", "");
                    appendIfNotBlank(sb, doc.getPublisherName(), ":", "");
                    break;
                case BOOK_SECTION:
                    appendIfNotBlank(sb, doc.getBookTitle(), "", "In ");
                    appendIfNotBlank(sb, getPageRange(doc), ".", "Pp. ");
                    appendIfNotBlank(sb, doc.getPublisherLocation(), ".", "");
                    appendIfNotBlank(sb, doc.getPublisherName(), ":", "");
                    break;
                case CONFERENCE_PRESENTATION:
                    appendIfNotBlank(sb, doc.getPublisherName(), "", "Presented at ");
                    appendIfNotBlank(sb, doc.getPublisherLocation(), ",", "");
                    break;
                case JOURNAL_ARTICLE:
                    appendIfNotBlank(sb, doc.getJournalName(), "", "");
                    if (StringUtils.isNotBlank(doc.getJournalNumber()) || StringUtils.isNotBlank(doc.getVolume())) {
                        sb.append(".");
                    }
                    appendIfNotBlank(sb, doc.getVolume(), "", "");
                    if (StringUtils.isNotBlank(doc.getJournalNumber())) {
                        appendIfNotBlank(sb, "(" + doc.getJournalNumber() + ")", "", "");
                    }
                    appendIfNotBlank(sb, getPageRange(doc), ":", "");
                    break;
                case OTHER:
                    break;
                case THESIS:
                    String degreetext = "";
                    if (doc.getDegree() != null) {
                        degreetext = doc.getDegree().getLabel();
                    }
                    appendIfNotBlank(sb, degreetext + ".", "", "");
                    appendIfNotBlank(sb, doc.getPublisherName(), "", "");
                    appendIfNotBlank(sb, doc.getPublisherLocation(), ",", "");
                    break;
            }
            if ((doc.getDate() != null) && (doc.getDate() != -1)) {
                appendIfNotBlank(sb, doc.getDate().toString(), ".", "");
            }
        } else if (resource instanceof InformationResource) {
            InformationResource ir = (InformationResource) resource;
            appendIfNotBlank(sb, ir.getPublisherLocation(), ".", "");
            appendIfNotBlank(sb, ir.getPublisherName(), ":", "");

            if ((ir.getDate() != null) && (ir.getDate().intValue() != -1)) {
                appendIfNotBlank(sb, ir.getDate().toString(), ".", "");
            }
            appendIfNotBlank(sb, ir.getCopyLocation(), ".", "");
        }
        return sb.toString();
    }

    public String getPageRange(Document doc) {
        StringBuilder sb = new StringBuilder();
        appendIfNotBlank(sb, doc.getStartPage(), "", "");
        appendIfNotBlank(sb, doc.getEndPage(), "-", "");
        return sb.toString().replaceAll("\\s", "");
    }

    protected StringBuilder appendIfNotBlank(StringBuilder sb, String str,
            String prefixIfNotAtStart, String textPrefixIfNotBlank) {
        if (StringUtils.isNotBlank(str)) {
            if (sb.length() > 0) {
                sb.append(prefixIfNotAtStart).append(" ");
            }
            if (StringUtils.isNotBlank(textPrefixIfNotBlank)) {
                sb.append(textPrefixIfNotBlank);
            }
            sb.append(str);
        }
        return sb;
    }
}
