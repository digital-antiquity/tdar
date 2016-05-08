package org.tdar.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Archive;
import org.tdar.core.bean.resource.Audio;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.Video;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.UrlService;
import org.tdar.utils.ResourceCitationFormatter;

import edu.asu.lib.qdc.QualifiedDublinCoreDocument;

public abstract class ExtendedDcTransformer<R extends Resource> implements Transformer<R, QualifiedDublinCoreDocument> {

    @Override
    public QualifiedDublinCoreDocument transform(R source) {
        QualifiedDublinCoreDocument dc = new QualifiedDublinCoreDocument();

        dc.addTitle(source.getTitle());

        String abst = source.getDescription();
        if (abst != null) {
            dc.addAbstract(abst);
        }

        dc.addCreated(source.getDateCreated());

        ResourceCitationFormatter rcf = new ResourceCitationFormatter(source);
        String cit = rcf.getFullCitation();
        if (StringUtils.isNotBlank(cit)) {
            dc.addBibliographicCitation(cit);
        }

        // add creators and contributors
        for (ResourceCreator resourceCreator : toSortedList(source.getActiveResourceCreators())) {
            String name = resourceCreator.getCreator().getProperName();
            if (resourceCreator.getCreatorType() == CreatorType.PERSON) {
                // display person names in special format
                name = dcConstructPersonalName(resourceCreator);
            }

            // FIXME: check this logic
            if (resourceCreator.getRole() == ResourceCreatorRole.AUTHOR) {
                dc.addCreator(name);
            } else {
                dc.addContributor(name);
            }
        }

        // add geographic subjects
        for (GeographicKeyword geoTerm : toSortedList(source.getActiveGeographicKeywords())) {
            dc.addSpatial(geoTerm.getLabel());
        }

        // add temporal subjects
        for (TemporalKeyword temporalTerm : toSortedList(source.getActiveTemporalKeywords())) {
            dc.addTemporal(temporalTerm.getLabel());
        }

        // add culture subjects
        for (CultureKeyword cultureTerm : toSortedList(source.getActiveCultureKeywords())) {
            dc.addSubject(cultureTerm.getLabel());
        }

        // add site name subjects
        for (SiteNameKeyword siteNameTerm : toSortedList(source.getActiveSiteNameKeywords())) {
            dc.addCoverage(siteNameTerm.getLabel());
        }

        // add site name subjects
        for (SiteTypeKeyword siteNameTerm : toSortedList(source.getActiveSiteTypeKeywords())) {
            dc.addSubject(siteNameTerm.getLabel());
        }

        // add site name subjects
        for (MaterialKeyword term : toSortedList(source.getActiveMaterialKeywords())) {
            dc.addSubject(term.getLabel());
        }

        for (CoverageDate cov : toSortedList(source.getActiveCoverageDates())) {
            if (cov.getDateType() == CoverageType.CALENDAR_DATE) {
                dc.addDate(String.format("start:%s end:%s", cov.getStartDate(), cov.getEndDate()));
            } else {
                dc.addDate(cov.toString());
            }
        }
        
        for (ResourceCollection coll : toSortedList(source.getSharedVisibleResourceCollections())) {
            dc.addIsPartOf(coll.getName());
        }

        // add other subjects
        for (OtherKeyword otherTerm : toSortedList(source.getActiveOtherKeywords())) {
            dc.addSubject(otherTerm.getLabel());
        }

        dc.addType(source.getResourceType().getLabel());
        
        dc.addIdentifier(source.getId().toString());
        dc.addReferences(UrlService.absoluteUrl(source));
        for (LatitudeLongitudeBox longLat : toSortedList(source.getActiveLatitudeLongitudeBoxes())) {
            String maxy = longLat.getMaxObfuscatedLatitude().toString();
            String miny = longLat.getMinObfuscatedLatitude().toString();
            String maxx = longLat.getMaxObfuscatedLongitude().toString();
            String minx = longLat.getMinObfuscatedLongitude().toString();
            dc.addSpatial(minx, miny, maxx, maxy);
            // dc.addCoverage(String.format("%s, %s, %s, %s", maxy, miny, maxx, minx));
        }

        for (CoverageDate date : toSortedList(source.getCoverageDates())) {
            dc.addTemporal(date.toString());
        }

        return dc;
    }

    private <K extends Persistable> List<K> toSortedList(Set<K> activeCultureKeywords) {
        List<K> list = new ArrayList<>(activeCultureKeywords);
        list.sort(new Comparator<K>() {

            @Override
            public int compare(K o1, K o2) {
                return ObjectUtils.compare(o1.getId(), o2.getId());
            }
        });
        // TODO Auto-generated method stub
        return list;
    }

    protected String dcConstructPersonalName(String firstName, String lastName, String role, String affiliation) {
        String name = String.format("%s, %s", lastName, firstName);
        if (!StringUtils.isEmpty(role)) {
            name += String.format(", %s", role);
        }
        if (!StringUtils.isEmpty(affiliation)) {
            name += String.format(" (%s)", affiliation);
        }
        return name;
    }

    protected String dcConstructPersonalName(ResourceCreator resourceCreator) {
        if (resourceCreator.getCreatorType() != CreatorType.PERSON) {
            return null;
        }
        Person person = (Person) resourceCreator.getCreator();
        String name = String.format("%s, %s", person.getLastName(), person.getFirstName());
        // if (!StringUtils.isEmpty("" + resourceCreator.getRole())) {
        // name += String.format(", %s", resourceCreator.getRole());
        // }
        if (!StringUtils.isEmpty(person.getInstitutionName())) {
            name += String.format(" (%s)", person.getInstitution());
        }
        return name;
    }

    public static class InformationResourceTransformer<I extends InformationResource> extends ExtendedDcTransformer<I> {

        @Override
        public QualifiedDublinCoreDocument transform(I source) {
            QualifiedDublinCoreDocument dc = super.transform(source);

            String doi = source.getDoi();
            if (doi != null) {
                dc.addIdentifier(doi);
            }
            
            if (source.getProject() != Project.NULL) {
                dc.addIsPartOf(source.getProjectTitle());
            }

            String copyLocation = source.getCopyLocation();
            if (copyLocation != null) {
                dc.addRelation(copyLocation);
            }

            for (ResourceCreator resourceCreator : source.getActiveResourceCreators()) {
                if (resourceCreator.getRole() == ResourceCreatorRole.CONTACT) {
                    dc.addPublisher(resourceCreator.getCreator().getProperName());
                }
            }
            if (source.getDate() != null) {
                dc.addCreated(source.getDate().toString());
            }

            Language resourceLanguage = source.getResourceLanguage();
            if (resourceLanguage != null) {
                dc.addLanguageISO639_2(resourceLanguage.getIso639_2());
            }

            if (source.getResourceType().toDcmiTypeString() != null) {
                dc.addType(source.getResourceType().toDcmiTypeString());
            }
            

            for (InformationResourceFileVersion version : source.getLatestUploadedVersions()) {
                dc.addType(version.getMimeType());
            }

            Institution resourceProviderInstitution = source.getResourceProviderInstitution();
            if (resourceProviderInstitution != null) {
                dc.addContributor(resourceProviderInstitution.getName());
            }

            String publisherLocation = source.getPublisherLocation();

            String pub = "";
            String publisher = source.getPublisherName();
            if (publisher != null) {
                pub += publisher;
            }
            if (publisherLocation != null) {
                pub += ", " + publisherLocation;
            }
            if (!pub.isEmpty()) {
                dc.addPublisher(pub);
            }

            return dc;
        }

    }

    public static class DocumentTransformer extends InformationResourceTransformer<Document> {

        @Override
        public QualifiedDublinCoreDocument transform(Document source) {
            QualifiedDublinCoreDocument dc = super.transform(source);

            String isbn = source.getIsbn();
            if (isbn != null) {
                dc.addIdentifier(isbn);
            }
            String issn = source.getIssn();
            if (issn != null) {
                dc.addIdentifier(issn);
            }

                dc.addType(source.getDocumentType().getLabel());
            

            String seriesName = source.getSeriesName();
            String seriesNumber = source.getSeriesNumber();
            String series = "";
            if (seriesName != null) {
                series += seriesName;
            }
            if (seriesNumber != null) {
                series += " #" + seriesNumber;
            }
            if (!series.isEmpty()) {
                dc.addRelation("Series: " + series);
            }

            String journalName = source.getJournalName();
            String bookTitle = source.getBookTitle();
            String src = "";
            if (journalName != null) {
                src += journalName;
            }
            if (bookTitle != null) {
                src += bookTitle;
            }

            String volume = source.getVolume();
            String journalNumber = source.getJournalNumber();
            String volIssue = "";
            if (volume != null) {
                volIssue += volume;
            }
            if (journalNumber != null) {
                volIssue += String.format("(%s)", journalNumber);
            }

            String startPage = source.getStartPage();
            String endPage = source.getEndPage();
            String pages = "";
            if (startPage != null) {
                pages += startPage + " - ";
            }
            if (endPage != null) {
                pages += endPage;
            }

            if (!volIssue.isEmpty()) {
                src += ", " + volIssue;
            }
            if (!pages.isEmpty()) {
                src += ", " + pages;
            }
            if (!src.isEmpty()) {
                src += ".";
            }

            DocumentType documentType = source.getDocumentType();
            dc.addType(documentType.getLabel());
            switch (documentType) {
                case JOURNAL_ARTICLE:
                    if (!src.isEmpty()) {
                        dc.addSource(src);
                    }
                    break;
                case BOOK_SECTION:
                    if (!src.isEmpty()) {
                        dc.addSource(src);
                    }
                    break;
                case BOOK:
                    String rel = "";
                    String edition = source.getEdition();
                    if (edition != null) {
                        rel += "Edition: " + edition;
                    }
                    if (volume != null) {
                        rel += " Volume: " + volume;
                    }
                    dc.addRelation(rel.trim());
                    break;
                default:
                    break;
            }

            return dc;
        }

    }

    public static class DatasetTransformer extends InformationResourceTransformer<Dataset> {
        // marker class
    }

    public static class SensoryDataTransformer extends InformationResourceTransformer<SensoryData> {
        // marker class
    }

    public static class VideoTransformer extends InformationResourceTransformer<Video> {
        // marker class
    }

    public static class GeospatialTransformer extends InformationResourceTransformer<Geospatial> {
        // marker class
    }

    public static class CodingSheetTransformer extends InformationResourceTransformer<CodingSheet> {
        // marker class
    }

    public static class ImageTransformer extends InformationResourceTransformer<Image> {
        // marker class
    }

    public static class ArchiveTransformer extends InformationResourceTransformer<Archive> {
        // marker class
    }

    public static class AudioTransformer extends InformationResourceTransformer<Audio> {
        // marker class
    }

    public static class OntologyTransformer extends InformationResourceTransformer<Ontology> {
        // marker class
    }

    public static class ProjectTransformer extends ExtendedDcTransformer<Project> {
        // marker class
    }

    public static QualifiedDublinCoreDocument transformAny(Resource resource) {
        ResourceType resourceType = ResourceType.fromClass(resource.getClass());
        if (resourceType == null) {
            throw new TdarRecoverableRuntimeException("transformer.unsupported_type");
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
            case VIDEO:
                return new VideoTransformer().transform((Video) resource);
            case GEOSPATIAL:
                return new GeospatialTransformer().transform((Geospatial) resource);
            case ARCHIVE:
                return new ArchiveTransformer().transform((Archive) resource);
            case AUDIO:
                return new AudioTransformer().transform((Audio) resource);
            default:
                break;
        }

        throw new TdarRecoverableRuntimeException("transformer.no_extended_dc_transformer", Arrays.asList(resource.getClass()));
    }
}
