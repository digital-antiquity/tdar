package org.tdar.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.datacite.v41.ContributorType;
import org.datacite.v41.DescriptionType;
import org.datacite.v41.RelatedIdentifierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
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
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.Video;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.UrlService;
import org.tdar.utils.XmlEscapeHelper;

import edu.asu.lib.datacite.DataCiteDocument;
import edu.asu.lib.mods.ModsElementContainer.TypeOfResourceValue;

public abstract class DataCiteTransformer<R extends Resource> implements
        Transformer<R, DataCiteDocument> {

    @SuppressWarnings("unused")
    private final transient Logger log = LoggerFactory.getLogger(getClass());
    private XmlEscapeHelper x;

    @Override
    public DataCiteDocument transform(R source) {
        DataCiteDocument dcd = new DataCiteDocument();
        setX(new XmlEscapeHelper(source.getId()));

        dcd.addTitle(getX().stripNonValidXMLCharacters(source.getTitle()));

        String abst = source.getDescription();
        if (abst != null) {
            dcd.addDescription(getX().stripNonValidXMLCharacters(abst), DescriptionType.ABSTRACT);
        }

        switch(source.getResourceType()) {
            case ARCHIVE:
            case PROJECT:
                dcd.setResourceType(org.datacite.v41.ResourceType.COLLECTION);
                break;
            case AUDIO:
            case IMAGE:
            case VIDEO:
                dcd.setResourceType(org.datacite.v41.ResourceType.AUDIOVISUAL);
                break;
            case CODING_SHEET:
            case ONTOLOGY:
                dcd.setResourceType(org.datacite.v41.ResourceType.OTHER);
                break;
            case DATASET:
            case GEOSPATIAL:
            case SENSORY_DATA:
                dcd.setResourceType(org.datacite.v41.ResourceType.DATASET);
                break;
            case DOCUMENT:
                dcd.setResourceType(org.datacite.v41.ResourceType.TEXT);
                break;
            default:
                break;
        }

        int year = source.getDateCreated().getYear() + 1900;
        String publisher = null;
        if (source instanceof InformationResource) {
            InformationResource ir = (InformationResource) source;
            if (ir.getDate() != null && ir.getDate() > 1) {
                year = ir.getDate();
            }
            publisher = ir.getPublisherName();
            if (publisher == null && ir.getResourceProviderInstitution() != null) {
                publisher = ir.getResourceProviderInstitution().getName();
            }
        }
        if (publisher == null) {
            List<ResourceCreator> creators = new ArrayList<>(source.getResourceCreators(ResourceCreatorRole.PREPARER));
            creators.addAll(source.getResourceCreators(ResourceCreatorRole.SUBMITTER));
            creators.addAll(source.getPrimaryCreators());
            
            if (CollectionUtils.isNotEmpty(creators)) {
                publisher = creators.get(0).getCreator().getProperName();
            } else {
                publisher = source.getSubmitter().getProperName();
            }
        }
        dcd.setPublicationYear(year);
        dcd.setPublisher(publisher);

        // add resource creators
        ArrayList<ResourceCreator> creators = new ArrayList<ResourceCreator>(source.getResourceCreators());
        Collections.sort(creators);

        boolean seenCreator = false;
        for (ResourceCreator resourceCreator : creators) {
            if (addResourceCreator(dcd, resourceCreator)) {
                seenCreator = true;
            }
        }

        if (seenCreator == false) {
            dcd.addPersonalCreator(source.getSubmitter().getFirstName(), source.getSubmitter().getLastName(), source.getSubmitter().getOrcidId());
        }

        // add geographic subjects
        Set<GeographicKeyword> geoTerms = source.getActiveGeographicKeywords();
        for (GeographicKeyword geoTerm : geoTerms) {
            createTopic(dcd, geoTerm);
        }

        // add temporal subjects
        Set<TemporalKeyword> locTemporalTerms = source.getActiveTemporalKeywords();
        for (TemporalKeyword temporalTerm : locTemporalTerms) {
            createTopic(dcd, temporalTerm);
        }

        // add culture subjects
        Set<CultureKeyword> cultureTerms = source.getActiveCultureKeywords();
        for (CultureKeyword cultureTerm : cultureTerms) {
            createTopic(dcd, cultureTerm);
        }

        // add site name subjects
        Set<SiteNameKeyword> siteNameTerms = source.getActiveSiteNameKeywords();
        for (SiteNameKeyword siteNameTerm : siteNameTerms) {
            createTopic(dcd, siteNameTerm);
        }

        // add site name subjects
        Set<SiteTypeKeyword> siteTypeTerms = source.getActiveSiteTypeKeywords();
        for (SiteTypeKeyword siteTypeTerm : siteTypeTerms) {
            createTopic(dcd, siteTypeTerm);
        }

        // add site name subjects
        Set<MaterialKeyword> materialKeywords = source.getActiveMaterialKeywords();
        for (MaterialKeyword materialKeyword : materialKeywords) {
            createTopic(dcd, materialKeyword);
        }

        // add other subjects
        Set<OtherKeyword> otherTerms = source.getActiveOtherKeywords();
        for (OtherKeyword otherTerm : otherTerms) {
            createTopic(dcd, otherTerm);
        }

        // add other subjects
        Set<InvestigationType> investigationTypes = source.getActiveInvestigationTypes();
        for (InvestigationType otherTerm : investigationTypes) {
            createTopic(dcd, otherTerm);
        }

        for (LatitudeLongitudeBox longLat : source.getActiveLatitudeLongitudeBoxes()) {
            dcd.addGeoLocation(longLat.getObfuscatedNorth(), longLat.getObfuscatedSouth(), longLat.getObfuscatedEast(), longLat.getObfuscatedWest());
        }

        dcd.addIdentifier(getX().stripNonValidXMLCharacters(UrlService.absoluteUrl(source)), "uri");
        dcd.addIdentifier(source.getId().toString(), "tdarId");
        if (StringUtils.isNotBlank(source.getExternalId())) {
            dcd.setIdentifier(source.getExternalId().replace("doi:", ""));
        } else {
            dcd.setIdentifier("10.0.0/000");
        }
        if (source instanceof InformationResource) {
            InformationResource ir = (InformationResource) source;
            if (StringUtils.isNotBlank(ir.getDoi())) {
                dcd.addRelatedIdentifier(getX().stripNonValidXMLCharacters(ir.getDoi()), RelatedIdentifierType.DOI);
            }
        }

        getX().logChange();
        return dcd;
    }

    private void createTopic(DataCiteDocument dcd, Keyword otherTerm) {
        dcd.addSubject(otherTerm.getLabel(), UrlService.absoluteUrl(otherTerm), null, null);
    }

    protected boolean addResourceCreator(DataCiteDocument dcd, ResourceCreator resourceCreator) {
        Creator creator = resourceCreator.getCreator();
        if (!creator.isActive()) {
            return false;
        }
        if (resourceCreator.getRole() == ResourceCreatorRole.AUTHOR || resourceCreator.getRole() == ResourceCreatorRole.CREATOR) {
            if (creator.getCreatorType() == CreatorType.PERSON) {
                dcd.addPersonalCreator(((Person) creator).getFirstName(), ((Person) creator).getLastName(), ((Person) creator).getOrcidId());
            } else {
                dcd.addInstitutionalCreator(creator.getProperName());
            }
            return true;
        }
        switch (resourceCreator.getRole()) {
            case CONTACT:
                addContributor(dcd, creator, ContributorType.CONTACT_PERSON);
                break;
            case EDITOR:
                addContributor(dcd, creator, ContributorType.EDITOR);
                break;
            case SPONSOR:
                addContributor(dcd, creator, ContributorType.SPONSOR);
                break;
            default:
                break;
        }
        return false;
    }

    private void addContributor(DataCiteDocument dcd, Creator creator, ContributorType editor) {
        if (creator.getCreatorType() == CreatorType.PERSON) {
            dcd.addPersonalContributor(((Person) creator).getFirstName(), ((Person) creator).getLastName(), editor);
        } else {
            dcd.addInstitutionalContributor(creator.getProperName(), editor);
        }
    }

    public static class InformationResourceTransformer<I extends InformationResource>
            extends DataCiteTransformer<I> {

        @Override
        public DataCiteDocument transform(I source) {
            DataCiteDocument dcd = super.transform(source);

            // if ((source.getDate() != null) && (source.getDate() != -1)) {
            // DateElement createDate = dcd.getOriginInfo().createDate(OriginDateType.CREATED);
            // createDate.setValue(source.getDate().toString());
            // }

            if (source.getResourceLanguage() != null) {
                dcd.setLanguage(source.getResourceLanguage().getCode());
            }

            getX().logChange();
            return dcd;
        }

    }

    public static class DocumentTransformer
            extends InformationResourceTransformer<Document> {

        @SuppressWarnings("deprecation")
        @Override
        public DataCiteDocument transform(Document source) {
            DataCiteDocument dcd = super.transform(source);
            //
            DocumentType type = source.getDocumentType();
            switch (type) {
                case BOOK:
                    // addSeriesInfo(dcd, source.getSeriesName(), source.getSeriesNumber());
                    // addVolume(dcd, source.getVolume());
                    // addExtent(dcd, source.getNumberOfPages(), source.getStartPage(), source.getEndPage());
                    // addEdition(dcd, source.getEdition());
                    // addPublisher(dcd, source.getPublisherName(), source.getPublisherLocation());
                    addIsbn(dcd, source.getIsbn());
                    // addPhysicalLocation(dcd, source.getCopyLocation());
                    // // no other good place to put these if entered
                    break;
                case BOOK_SECTION:
                    // RelatedItem bookHost = dcd.createRelatedItem();
                    // bookHost.setType(RelatedItemTypeValues.host);
                    // bookHost.getTitleInfo().addTitle(getX().stripNonValidXMLCharacters(source.getBookTitle()));
                    // addSeriesInfo(bookHost, source.getSeriesName(), source.getSeriesNumber());
                    // addVolume(bookHost, source.getVolume());
                    // addExtent(bookHost, source.getNumberOfPages(), source.getStartPage(), source.getEndPage());
                    // addEdition(bookHost, source.getEdition());
                    // addPublisher(bookHost, source.getPublisherName(), source.getPublisherLocation());
                    addIsbn(dcd, source.getIsbn());
                    // addPhysicalLocation(bookHost, source.getCopyLocation());
                    // // assume that the editors are editors of the host book???
                    break;
                case JOURNAL_ARTICLE:
                    // RelatedItem artHost = dcd.createRelatedItem();
                    // artHost.setType(RelatedItemTypeValues.host);
                    //
                    // if (source.getJournalName() != null) {
                    // artHost.getTitleInfo().addTitle(getX().stripNonValidXMLCharacters(source.getJournalName()));
                    // }
                    // addVolume(artHost, source.getVolume());
                    // if (source.getJournalNumber() != null) {
                    // artHost.getPart().addDetail(getX().stripNonValidXMLCharacters(source.getJournalNumber()),
                    // null, null, "issue", null);
                    // }
                    //
                    // addPublisher(artHost, source.getPublisherName(), source.getPublisherLocation());
                    // addExtent(artHost, source.getNumberOfPages(), source.getStartPage(), source.getEndPage());
                    //
                    if (source.getIssn() != null) {
                        dcd.addRelatedIdentifier(getX().stripNonValidXMLCharacters(source.getIssn()), RelatedIdentifierType.ISSN);
                    }
                    // addPhysicalLocation(artHost, source.getCopyLocation());
                    //
                    // // again, is this a good assumption?
                    break;
                // case THESIS:
                // RelatedItem thesisHost = dcd.createRelatedItem();
                // thesisHost.setType(RelatedItemTypeValues.host);
                // addPhysicalLocation(thesisHost, source.getCopyLocation());
                // // add the degree grantor
                // Name degreeGrantor = thesisHost.createName();
                // degreeGrantor.setNameType(NameTypeAttribute.CORPORATE);
                // if (source.getPublisherName() != null) {
                // degreeGrantor.addNamePart(getX().stripNonValidXMLCharacters(source.getPublisherName()), null); // institution
                // if (source.getPublisherLocation() != null) {
                // degreeGrantor.addNamePart(getX().stripNonValidXMLCharacters(source.getPublisherLocation()), null); // department
                // }
                // degreeGrantor.addRole("Degree grantor", false, null);
                // }
                // break;
                // case CONFERENCE_PRESENTATION:
                // if (source.getPublisherName() != null) {
                // Name conf = dcd.createName();
                // conf.setNameType(NameTypeAttribute.CONFERENCE);
                // conf.addNamePart(getX().stripNonValidXMLCharacters(source.getPublisherName()), null);
                // conf.addRole("creator", false, null);
                // }
                // if (source.getPublisherName() != null) {
                // dcd.getOriginInfo().addPlace(getX().stripNonValidXMLCharacters(source.getPublisherLocation()), false, null);
                // }
                // break;
                // case OTHER:
                // case REPORT:
                // addExtent(dcd, source.getNumberOfPages(), source.getStartPage(), source.getEndPage());
                // addPhysicalLocation(dcd, source.getCopyLocation());
                // break;
            }

            getX().logChange();
            return dcd;
        }

        // private void addVolume(DataCiteDocument elem, String volume) {
        // if (volume != null) {
        // elem.getPart().addDetail(getX().stripNonValidXMLCharacters(volume), null, null, "volume", null);
        // }
        // }
        //
        //// private void addPhysicalLocation(DataCiteDocument elem, String copyLocation) {
        // if (copyLocation != null) {
        // elem.getLocation().addPhysicalLocation(getX().stripNonValidXMLCharacters(copyLocation), null);
        // }
        // }

        // private void addPublisher(DataCiteDocument elem, String publisher, String publisherLocation) {
        // if (publisher != null) {
        // elem.getOriginInfo().addPublisher(getX().stripNonValidXMLCharacters(publisher));
        // }
        // if (publisherLocation != null) {
        // elem.getOriginInfo().addPlace(getX().stripNonValidXMLCharacters(publisherLocation), false, null);
        // }
        // }

        private void addIsbn(DataCiteDocument elem, String isbn) {
            if (isbn != null) {
                elem.addRelatedIdentifier(getX().stripNonValidXMLCharacters(isbn), RelatedIdentifierType.ISBN);
            }
        }

        // private void addEdition(DataCiteDocument elem, String edition) {
        // if (edition != null) {
        // elem.getOriginInfo().addEdition(getX().stripNonValidXMLCharacters(edition));
        // }
        // }
        //
        // private void addSeriesInfo(DataCiteDocument elem, String seriesName, String seriesNumber) {
        // if ((seriesName != null) || (seriesNumber != null)) {
        // RelatedItem series = elem.createRelatedItem();
        // series.setType(RelatedItemTypeValues.series);
        // if (seriesName != null) {
        // series.getTitleInfo().addTitle(getX().stripNonValidXMLCharacters(seriesName));
        // }
        // if (seriesNumber != null) {
        // series.getTitleInfo().addPartNumber(getX().stripNonValidXMLCharacters(seriesNumber));
        // }
        // }
        // }

        // private void addExtent(DataCiteDocument elem, Integer numberOfPages, String startPage, String endPage) {
        // BigInteger numPages = (numberOfPages != null) ? new BigInteger(
        // numberOfPages.toString()) : null;
        // String sPage = (startPage != null) ? startPage.toString() : null;
        // String ePage = (endPage != null) ? endPage.toString() : null;
        // if ((numPages != null) || (sPage != null) || (ePage != null)) {
        // elem.getPart().addExtent(getX().stripNonValidXMLCharacters(sPage), getX().stripNonValidXMLCharacters(ePage), null, "pages", numPages);
        // }
        // }

    }

    public static class DatasetTransformer extends DataCiteTransformer<Dataset> {
        // marker class
    }

    public static class CodingSheetTransformer extends DataCiteTransformer<CodingSheet> {
        // marker class
    }

    public static class ImageTransformer extends DataCiteTransformer<Image> {
        // marker class
    }

    public static class SensoryDataTransformer extends DataCiteTransformer<SensoryData> {
        // marker class
    }

    public static class OntologyTransformer extends DataCiteTransformer<Ontology> {
        // marker class
    }

    public static class ProjectTransformer extends DataCiteTransformer<Project> {
        // marker class
    }

    public static class ArchiveTransformer extends DataCiteTransformer<Archive> {
        // marker class
    }

    public static class AudioTransformer extends DataCiteTransformer<Audio> {
        // marker class
    }

    public static class VideoTransformer extends DataCiteTransformer<Video> {
        // marker class
    }

    public static class GeospatialTransformer extends DataCiteTransformer<Geospatial> {
        // marker class
    }

    public static class DcmidcdTypeMapper {

        private static final Map<String, TypeOfResourceValue> typeMap = initTypeMap();

        private static Map<String, TypeOfResourceValue> initTypeMap() {
            Map<String, TypeOfResourceValue> map = new HashMap<>();
            map.put("Software", TypeOfResourceValue.SOFTWARE_MULTIMEDIA);
            map.put("Still Image", TypeOfResourceValue.STILL_IMAGE);
            map.put("Sound", TypeOfResourceValue.SOUND_RECORDING);
            map.put("Interactive Resource", TypeOfResourceValue.SOFTWARE_MULTIMEDIA);
            map.put("Dataset", TypeOfResourceValue.SOFTWARE_MULTIMEDIA);
            map.put("Moving Image", TypeOfResourceValue.MOVING_IMAGE);
            map.put("Text", TypeOfResourceValue.TEXT);
            return map;
        }

        public static TypeOfResourceValue getType(String key) {
            return typeMap.get(key);
        }

    }

    public static DataCiteDocument transformAny(Resource resource) {
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

        throw new TdarRecoverableRuntimeException("transformer.no_dcd.transformer", Arrays.asList(resource.getClass()));
    }

    public XmlEscapeHelper getX() {
        return x;
    }

    public void setX(XmlEscapeHelper x) {
        this.x = x;
    }

}
