package org.tdar.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

import edu.asu.lib.dc.DublinCoreDocument;

public abstract class DcTransformer<R extends Resource> implements Transformer<R, DublinCoreDocument> {

    @Override
    public DublinCoreDocument transform(R source) {
        DublinCoreDocument dc = new DublinCoreDocument();

        dc.getTitle().add(source.getTitle());

        // add creators and contributors
        List<ResourceCreator> sortedResourceCreators = new ArrayList<ResourceCreator>(source.getResourceCreators());
        Collections.sort(sortedResourceCreators);
        for (ResourceCreator resourceCreator : source.getResourceCreators()) {
            String name;
            if (resourceCreator.getCreatorType() == CreatorType.PERSON) {
                // display person names in special format
                name = dcConstructPersonalName(resourceCreator);
            } else {
                // for institution, just display name and role
                name = resourceCreator.toString();
            }

            // FIXME: check this logic
            if (resourceCreator.getRole() == ResourceCreatorRole.AUTHOR) {
                dc.getCreator().add(name);
            } else {
                dc.getContributor().add(name);
            }
        }

        // add geographic subjects
        for (GeographicKeyword geoTerm : source.getActiveGeographicKeywords()) {
            dc.getCoverage().add(geoTerm.getLabel());
        }

        // add temporal subjects
        for (TemporalKeyword temporalTerm : source.getActiveTemporalKeywords()) {
            dc.getCoverage().add(temporalTerm.getLabel());
        }

        // add culture subjects
        for (CultureKeyword cultureTerm : source.getActiveCultureKeywords()) {
            dc.getSubject().add(cultureTerm.getLabel());
        }

        // add site name subjects
        for (SiteNameKeyword siteNameTerm : source.getActiveSiteNameKeywords()) {
            dc.getCoverage().add(siteNameTerm.getLabel());
        }

        // add other subjects
        for (OtherKeyword otherTerm : source.getActiveOtherKeywords()) {
            dc.getSubject().add(otherTerm.getLabel());
        }

        dc.getType().add(source.getResourceType().getLabel());

        for (LatitudeLongitudeBox longLat : source.getActiveLatitudeLongitudeBoxes()) {
            String maxy = "MaxY: ".concat(longLat.getMaxObfuscatedLatitude().toString());
            String miny = "MinY: ".concat(longLat.getMinObfuscatedLatitude().toString());
            String maxx = "MaxX: ".concat(longLat.getMaxObfuscatedLongitude().toString());
            String minx = "MinX: ".concat(longLat.getMinObfuscatedLongitude().toString());
            dc.getCoverage().add(String.format("%s, %s, %s, %s", maxy, miny, maxx, minx));
        }

        for (CoverageDate date : source.getCoverageDates()) {
            dc.getCoverage().add(date.toString());
        }

        // TODO: deal with Url here.

        return dc;
    }

    protected String dcConstructPersonalName(String firstName, String lastName, String role, String affiliation) {
        String name = String.format("%s, %s", lastName, firstName);
        if (!StringUtils.isEmpty(role))
            name += String.format(", %s", role);
        if (!StringUtils.isEmpty(affiliation))
            name += String.format(" (%s)", affiliation);
        return name;
    }

    protected String dcConstructPersonalName(ResourceCreator resourceCreator) {
        if (resourceCreator.getCreatorType() != CreatorType.PERSON)
            return null;
        Person person = (Person) resourceCreator.getCreator();
        String name = String.format("%s, %s", person.getLastName(), person.getFirstName());
        if (!StringUtils.isEmpty("" + resourceCreator.getRole()))
            name += String.format(", %s", resourceCreator.getRole());
        if (!StringUtils.isEmpty("" + person.getInstitution()))
            name += String.format(" (%s)", person.getInstitution());
        return name;
    }

    public static class InformationResourceTransformer<I extends InformationResource> extends DcTransformer<I> {

        @Override
        public DublinCoreDocument transform(I source) {
            DublinCoreDocument dc = super.transform(source);

            for (ResourceCreator resourceCreator : source.getResourceCreators()) {
                if (resourceCreator.getRole() == ResourceCreatorRole.CONTACT) {
                    dc.getPublisher().add(resourceCreator.getCreator().getProperName());
                }
            }
            if (source.getDate() != null) {
                String dateCreated = source.getDate().toString();
                if (dateCreated != null && dateCreated != "") {
                    dc.getDate().add(dateCreated);
                }
            }
            Language resourceLanguage = source.getResourceLanguage();
            if (resourceLanguage != null)
                dc.getLanguage().add(resourceLanguage.getCode());

            if (source.getResourceType().toDcmiTypeString() != null) {
                dc.getType().add(source.getResourceType().toDcmiTypeString());
            }

            for (InformationResourceFileVersion version : source.getLatestUploadedVersions()) {
                dc.getType().add(version.getMimeType());
            }

            Institution resourceProviderInstitution = source.getResourceProviderInstitution();
            if (resourceProviderInstitution != null)
                dc.getContributor().add(resourceProviderInstitution.getName());

            return dc;
        }

    }

    @Component("documentDcTransformer")
    public static class DocumentTransformer extends InformationResourceTransformer<Document> {

        @Override
        public DublinCoreDocument transform(Document source) {
            DublinCoreDocument dc = super.transform(source);

            String abst = source.getDescription();
            if (abst != null)
                dc.getDescription().add(abst);

            String doi = source.getDoi();
            if (doi != null)
                dc.getIdentifier().add(doi);

            String copyLocation = source.getCopyLocation();
            if (copyLocation != null)
                dc.getRelation().add(copyLocation);

            String publisher = source.getPublisher();
            String publisherLocation = source.getPublisherLocation();

            String pub = "";
            if (publisher != null)
                pub += publisher;
            if (publisherLocation != null)
                pub += ", " + publisherLocation;
            if (!pub.isEmpty())
                dc.getPublisher().add(pub);

            String isbn = source.getIsbn();
            if (isbn != null)
                dc.getIdentifier().add(isbn);
            String issn = source.getIssn();
            if (issn != null)
                dc.getIdentifier().add(issn);

            String seriesName = source.getSeriesName();
            String seriesNumber = source.getSeriesNumber();
            String series = "";
            if (seriesName != null)
                series += seriesName;
            if (seriesNumber != null)
                series += " #" + seriesNumber;
            if (!series.isEmpty())
                dc.getRelation().add("Series: " + series);

            String journalName = source.getJournalName();
            String bookTitle = source.getBookTitle();
            String src = "";
            if (journalName != null)
                src += journalName;
            if (bookTitle != null)
                src += bookTitle;

            String volume = source.getVolume();
            String journalNumber = source.getJournalNumber();
            String volIssue = "";
            if (volume != null)
                volIssue += volume;
            if (journalNumber != null)
                volIssue += String.format("(%s)", journalNumber);

            String startPage = source.getStartPage();
            String endPage = source.getEndPage();
            String pages = "";
            if (startPage != null)
                pages += startPage + " - ";
            if (endPage != null)
                pages += endPage;

            if (!volIssue.isEmpty())
                src += ", " + volIssue;
            if (!pages.isEmpty())
                src += ", " + pages;
            if (!src.isEmpty())
                src += ".";

            DocumentType documentType = source.getDocumentType();
            dc.getType().add(documentType.getLabel());
            switch (documentType) {
                case JOURNAL_ARTICLE:
                    if (!src.isEmpty())
                        dc.getSource().add(src);
                    break;
                case BOOK_SECTION:
                    if (!src.isEmpty())
                        dc.getSource().add(src);
                    break;
                case BOOK:
                    String rel = "";
                    String edition = source.getEdition();
                    if (edition != null)
                        rel += "Edition: " + edition;
                    if (volume != null)
                        rel += " Volume: " + volume;
                    dc.getRelation().add(rel.trim());
                    break;
                default:
                    break;
            }

            return dc;
        }

    }

    public static class DatasetTransformer extends InformationResourceTransformer<Dataset> {
    }

    public static class SensoryDataTransformer extends InformationResourceTransformer<SensoryData> {
    }

    public static class CodingSheetTransformer extends InformationResourceTransformer<CodingSheet> {
    }

    public static class ImageTransformer extends InformationResourceTransformer<Image> {
    }

    public static class OntologyTransformer extends InformationResourceTransformer<Ontology> {
    }

    public static class ProjectTransformer extends DcTransformer<Project> {
    }

    public static DublinCoreDocument transformAny(Resource resource) {
        ResourceType resourceType = ResourceType.fromClass(resource.getClass());
        if (resourceType == null) {
            throw new TdarRecoverableRuntimeException("Unsupported / Unknown Resource Type");
        }
        switch (resourceType) {
            case CODING_SHEET:
                return new CodingSheetTransformer().transform((CodingSheet) resource);
            case DATASET:
                return new DatasetTransformer().transform((Dataset) resource);
            case DOCUMENT:
                return new DocumentTransformer().transform((Document) resource);
            case IMAGE:
                return new ImageTransformer().transform((Image) resource);
            case ONTOLOGY:
                return new OntologyTransformer().transform((Ontology) resource);
            case PROJECT:
                return new ProjectTransformer().transform((Project) resource);
            case SENSORY_DATA:
                return new SensoryDataTransformer().transform((SensoryData) resource);
            default:
                break;
        }

        throw new TdarRecoverableRuntimeException("could not provide DC tranformer for class:" + resource.getClass());
    }
}
