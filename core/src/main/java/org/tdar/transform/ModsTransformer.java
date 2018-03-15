package org.tdar.transform;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.coverage.CoverageDate;
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

import edu.asu.lib.mods.ModsDocument;
import edu.asu.lib.mods.ModsElementContainer;
import edu.asu.lib.mods.ModsElementContainer.DateElement;
import edu.asu.lib.mods.ModsElementContainer.Name;
import edu.asu.lib.mods.ModsElementContainer.NamePartTypeValue;
import edu.asu.lib.mods.ModsElementContainer.OriginDateType;
import edu.asu.lib.mods.ModsElementContainer.RelatedItem;
import edu.asu.lib.mods.ModsElementContainer.RelatedItemTypeValues;
import edu.asu.lib.mods.ModsElementContainer.Subject;
import edu.asu.lib.mods.ModsElementContainer.TitleInfo;
import edu.asu.lib.mods.ModsElementContainer.TypeOfResourceValue;
import gov.loc.mods.v3.NameTypeAttribute;

public abstract class ModsTransformer<R extends Resource> implements
        Transformer<R, ModsDocument> {

    @SuppressWarnings("unused")
    private final transient Logger log = LoggerFactory.getLogger(getClass());
    private XmlEscapeHelper x;

    @Override
    public ModsDocument transform(R source) {
        ModsDocument mods = new ModsDocument();
        setX(new XmlEscapeHelper(source.getId()));

        TitleInfo title = mods.createTitleInfo();
        title.addTitle(getX().stripNonValidXMLCharacters(source.getTitle()));

        // add resource creators
        ArrayList<ResourceCreator> creators = new ArrayList<ResourceCreator>(source.getResourceCreators());
        Collections.sort(creators);

        // FIXME: (1) mods roles may not be creator roles
        // (2) this does not properly handle editors (see references below)
        // (3) what about the populate Author method that's defined below
        for (ResourceCreator resourceCreator : creators) {
            addResourceCreator(mods, resourceCreator);
        }

        // add geographic subjects
        Set<GeographicKeyword> geoTerms = source.getActiveGeographicKeywords();
        for (GeographicKeyword geoTerm : geoTerms) {
            createTopic(mods, geoTerm);
        }

        // add temporal subjects
        Set<TemporalKeyword> locTemporalTerms = source.getActiveTemporalKeywords();
        for (TemporalKeyword temporalTerm : locTemporalTerms) {
            Subject sub = mods.createSubject();
            // sub.getElementType().setHref(UrlService.absoluteSecureUrl(temporalTerm));
            sub.setAttribute("href", UrlService.absoluteSecureUrl(temporalTerm));
            sub.addTemporal(getX().stripNonValidXMLCharacters(temporalTerm.getLabel()));
        }

        // add culture subjects
        Set<CultureKeyword> cultureTerms = source.getActiveCultureKeywords();
        for (CultureKeyword cultureTerm : cultureTerms) {
            createTopic(mods, cultureTerm);
        }

        // add site name subjects
        Set<SiteNameKeyword> siteNameTerms = source.getActiveSiteNameKeywords();
        for (SiteNameKeyword siteNameTerm : siteNameTerms) {
            createTopic(mods, siteNameTerm);
        }

        // add site name subjects
        Set<SiteTypeKeyword> siteTypeTerms = source.getActiveSiteTypeKeywords();
        for (SiteTypeKeyword siteTypeTerm : siteTypeTerms) {
            createTopic(mods, siteTypeTerm);
        }

        // add site name subjects
        Set<MaterialKeyword> materialKeywords = source.getActiveMaterialKeywords();
        for (MaterialKeyword materialKeyword : materialKeywords) {
            createTopic(mods, materialKeyword);
        }

        // add other subjects
        Set<OtherKeyword> otherTerms = source.getActiveOtherKeywords();
        for (OtherKeyword otherTerm : otherTerms) {
            createTopic(mods, otherTerm);
        }

        // add other subjects
        Set<InvestigationType> investigationTypes = source.getActiveInvestigationTypes();
        for (InvestigationType otherTerm : investigationTypes) {
            createTopic(mods, otherTerm);
        }

        for (LatitudeLongitudeBox longLat : source.getActiveLatitudeLongitudeBoxes()) {
            Subject sub = mods.createSubject();
            List<String> coords = new ArrayList<>();
            coords.add("MaxY: ".concat(longLat.getObfuscatedNorth().toString()));
            coords.add("MinY: ".concat(longLat.getObfuscatedSouth().toString()));
            coords.add("MaxX: "
                    .concat(longLat.getObfuscatedEast().toString()));
            coords.add("MinX: "
                    .concat(longLat.getObfuscatedWest().toString()));
            sub.addCartographics(coords, "wgs84", null);
        }

        mods.addIdentifier(getX().stripNonValidXMLCharacters(UrlService.absoluteUrl(source)), "uri", false, null);
        mods.addIdentifier(source.getId().toString(), "tdarId", false, null);
        if (source instanceof InformationResource) {
            InformationResource ir = (InformationResource) source;
            if (StringUtils.isNotBlank(ir.getDoi())) {
                mods.addIdentifier(ir.getDoi(), "doi", false, null);
            }
            if (StringUtils.isNotBlank(ir.getExternalId())) {
                mods.addIdentifier(ir.getExternalId(), "doi", false, null);
            }
        }
        for (CoverageDate date : source.getCoverageDates()) {
            Subject sub = mods.createSubject();
            sub.addTemporal(getX().stripNonValidXMLCharacters(date.toString()));
        }

        // TODO: add URL pointer here.
        getX().logChange();
        return mods;
    }

    private void createTopic(ModsDocument mods, Keyword otherTerm) {
        Subject sub = mods.createSubject();
        // sub.getElementType().setHref(UrlService.absoluteUrl(otherTerm));
        sub.setAttribute("href", UrlService.absoluteSecureUrl(otherTerm));
        sub.addTopic(getX().stripNonValidXMLCharacters(otherTerm.getLabel()));
    }

    protected void addResourceCreator(ModsElementContainer mods, ResourceCreator resourceCreator) {
        Name name = mods.createName();
        if (resourceCreator.getRole() != null) {
            name.addRole(resourceCreator.getRole().getLabel(), false, null);
        }

        Creator creator = resourceCreator.getCreator();
        name.addDisplayForm(creator.getProperName());
        name.setAttribute("href", UrlService.absoluteSecureUrl(creator));

        if (creator.getCreatorType() == CreatorType.PERSON) {
            name.setNameType(NameTypeAttribute.PERSONAL);
            Person person = (Person) creator;
            name.addNamePart(getX().stripNonValidXMLCharacters(person.getFirstName()), NamePartTypeValue.given);
            name.addNamePart(getX().stripNonValidXMLCharacters(person.getLastName()), NamePartTypeValue.family);
            if (person.getInstitution() != null) {
                name.addAffiliation(getX().stripNonValidXMLCharacters(person.getInstitution().getName()));
            }
        } else {
            name.setNameType(NameTypeAttribute.CORPORATE);
            name.addNamePart(getX().stripNonValidXMLCharacters(creator.getProperName()), null);
        }
    }

    public static class InformationResourceTransformer<I extends InformationResource>
            extends ModsTransformer<I> {

        @Override
        public ModsDocument transform(I source) {
            ModsDocument mods = super.transform(source);

            for (ResourceCreator resourceCreator : source.getResourceCreators()) {
                if (resourceCreator.getRole() == ResourceCreatorRole.CONTACT) {
                    mods.getOriginInfo().addPublisher(getX().stripNonValidXMLCharacters(resourceCreator.getCreator().getProperName()));
                }
            }

            if ((source.getDate() != null) && (source.getDate() != -1)) {
                DateElement createDate = mods.getOriginInfo().createDate(OriginDateType.CREATED);
                createDate.setValue(source.getDate().toString());
            }

            if (source.getResourceLanguage() != null) {
                mods.addLanguage(source.getResourceLanguage().getCode(), false,
                        null, null);
            }
            if (source.getResourceType().toDcmiTypeString() != null) {
                mods.addTypeOfResource(
                        DcmiModsTypeMapper.getType(source.getResourceType().toDcmiTypeString()),
                        false, false);
            }

            // FIXME: fixme
            // if (informationResourceFormat != null)
            // mods.createPhysicalDescription().addInternetMediaType(informationResourceFormat.getMimeType());
            populateAuthorSection(source, mods);
            getX().logChange();
            return mods;
        }

        protected void populateAuthorSection(I source, ModsDocument mods) {
            if (source.getResourceProviderInstitution() != null) {
                Name name = mods.createName();
                name.addNamePart(getX().stripNonValidXMLCharacters(source.getResourceProviderInstitution()
                        .getName()), null);
            }
        }

    }

    public static class DocumentTransformer
            extends InformationResourceTransformer<Document> {

        @Override
        protected void populateAuthorSection(Document source, ModsDocument mods) {
            // TODO we dont' do anything here. but we override it so the parent doesn't implement this
        }

        @SuppressWarnings("deprecation")
        @Override
        public ModsDocument transform(Document source) {
            ModsDocument mods = super.transform(source);

            String abst = source.getDescription();
            if (abst != null) {
                mods.addAbstract(getX().stripNonValidXMLCharacters(abst), null);
            }

            if (source.getDoi() != null) {
                mods.addIdentifier(getX().stripNonValidXMLCharacters(source.getDoi()), "doi", false, null);
            }

            DocumentType type = source.getDocumentType();
            mods.addGenre(type.getLabel(),
                    Arrays.asList(mods.new Attribute("authority", "local")));

            switch (type) {
                case BOOK:
                    addSeriesInfo(mods, source.getSeriesName(), source.getSeriesNumber());
                    addVolume(mods, source.getVolume());
                    addExtent(mods, source.getNumberOfPages(), source.getStartPage(), source.getEndPage());
                    addEdition(mods, source.getEdition());
                    addPublisher(mods, source.getPublisherName(), source.getPublisherLocation());
                    addIsbn(mods, source.getIsbn());
                    addPhysicalLocation(mods, source.getCopyLocation());
                    // no other good place to put these if entered
                    break;
                case BOOK_SECTION:
                    RelatedItem bookHost = mods.createRelatedItem();
                    bookHost.setType(RelatedItemTypeValues.host);
                    bookHost.getTitleInfo().addTitle(getX().stripNonValidXMLCharacters(source.getBookTitle()));
                    addSeriesInfo(bookHost, source.getSeriesName(), source.getSeriesNumber());
                    addVolume(bookHost, source.getVolume());
                    addExtent(bookHost, source.getNumberOfPages(), source.getStartPage(), source.getEndPage());
                    addEdition(bookHost, source.getEdition());
                    addPublisher(bookHost, source.getPublisherName(), source.getPublisherLocation());
                    addIsbn(bookHost, source.getIsbn());
                    addPhysicalLocation(bookHost, source.getCopyLocation());
                    // assume that the editors are editors of the host book???
                    break;
                case JOURNAL_ARTICLE:
                    RelatedItem artHost = mods.createRelatedItem();
                    artHost.setType(RelatedItemTypeValues.host);

                    if (source.getJournalName() != null) {
                        artHost.getTitleInfo().addTitle(getX().stripNonValidXMLCharacters(source.getJournalName()));
                    }
                    addVolume(artHost, source.getVolume());
                    if (source.getJournalNumber() != null) {
                        artHost.getPart().addDetail(getX().stripNonValidXMLCharacters(source.getJournalNumber()),
                                null, null, "issue", null);
                    }

                    addPublisher(artHost, source.getPublisherName(), source.getPublisherLocation());
                    addExtent(artHost, source.getNumberOfPages(), source.getStartPage(), source.getEndPage());

                    if (source.getIssn() != null) {
                        artHost.addIdentifier(source.getIssn(), "issn", false, null);
                    }
                    addPhysicalLocation(artHost, source.getCopyLocation());

                    // again, is this a good assumption?
                    break;
                case THESIS:
                    RelatedItem thesisHost = mods.createRelatedItem();
                    thesisHost.setType(RelatedItemTypeValues.host);
                    addPhysicalLocation(thesisHost, source.getCopyLocation());
                    // add the degree grantor
                    Name degreeGrantor = thesisHost.createName();
                    degreeGrantor.setNameType(NameTypeAttribute.CORPORATE);
                    if (source.getPublisherName() != null) {
                        degreeGrantor.addNamePart(getX().stripNonValidXMLCharacters(source.getPublisherName()), null); // institution
                        if (source.getPublisherLocation() != null) {
                            degreeGrantor.addNamePart(getX().stripNonValidXMLCharacters(source.getPublisherLocation()), null); // department
                        }
                        degreeGrantor.addRole("Degree grantor", false, null);
                    }
                    break;
                case CONFERENCE_PRESENTATION:
                    if (source.getPublisherName() != null) {
                        Name conf = mods.createName();
                        conf.setNameType(NameTypeAttribute.CONFERENCE);
                        conf.addNamePart(getX().stripNonValidXMLCharacters(source.getPublisherName()), null);
                        conf.addRole("creator", false, null);
                    }
                    if (source.getPublisherName() != null) {
                        mods.getOriginInfo().addPlace(getX().stripNonValidXMLCharacters(source.getPublisherLocation()), false, null);
                    }
                    break;
                case OTHER:
                    addExtent(mods, source.getNumberOfPages(), source.getStartPage(), source.getEndPage());
                    addPhysicalLocation(mods, source.getCopyLocation());
                    break;
            }

            getX().logChange();
            return mods;
        }

        private void addVolume(ModsElementContainer elem, String volume) {
            if (volume != null) {
                elem.getPart().addDetail(getX().stripNonValidXMLCharacters(volume), null, null, "volume", null);
            }
        }

        private void addPhysicalLocation(ModsElementContainer elem, String copyLocation) {
            if (copyLocation != null) {
                elem.getLocation().addPhysicalLocation(getX().stripNonValidXMLCharacters(copyLocation), null);
            }
        }

        private void addPublisher(ModsElementContainer elem, String publisher, String publisherLocation) {
            if (publisher != null) {
                elem.getOriginInfo().addPublisher(getX().stripNonValidXMLCharacters(publisher));
            }
            if (publisherLocation != null) {
                elem.getOriginInfo().addPlace(getX().stripNonValidXMLCharacters(publisherLocation), false, null);
            }
        }

        private void addIsbn(ModsElementContainer elem, String isbn) {
            if (isbn != null) {
                elem.addIdentifier(getX().stripNonValidXMLCharacters(isbn), "isbn", false, null);
            }
        }

        private void addEdition(ModsElementContainer elem, String edition) {
            if (edition != null) {
                elem.getOriginInfo().addEdition(getX().stripNonValidXMLCharacters(edition));
            }
        }

        private void addSeriesInfo(ModsElementContainer elem, String seriesName, String seriesNumber) {
            if ((seriesName != null) || (seriesNumber != null)) {
                RelatedItem series = elem.createRelatedItem();
                series.setType(RelatedItemTypeValues.series);
                if (seriesName != null) {
                    series.getTitleInfo().addTitle(getX().stripNonValidXMLCharacters(seriesName));
                }
                if (seriesNumber != null) {
                    series.getTitleInfo().addPartNumber(getX().stripNonValidXMLCharacters(seriesNumber));
                }
            }
        }

        private void addExtent(ModsElementContainer elem, Integer numberOfPages, String startPage, String endPage) {
            BigInteger numPages = (numberOfPages != null) ? new BigInteger(
                    numberOfPages.toString()) : null;
            String sPage = (startPage != null) ? startPage.toString() : null;
            String ePage = (endPage != null) ? endPage.toString() : null;
            if ((numPages != null) || (sPage != null) || (ePage != null)) {
                elem.getPart().addExtent(getX().stripNonValidXMLCharacters(sPage), getX().stripNonValidXMLCharacters(ePage), null, "pages", numPages);
            }
        }

    }

    public static class DatasetTransformer extends ModsTransformer<Dataset> {
        // marker class
    }

    public static class CodingSheetTransformer extends ModsTransformer<CodingSheet> {
        // marker class
    }

    public static class ImageTransformer extends ModsTransformer<Image> {
        // marker class
    }

    public static class SensoryDataTransformer extends ModsTransformer<SensoryData> {
        // marker class
    }

    public static class OntologyTransformer extends ModsTransformer<Ontology> {
        // marker class
    }

    public static class ProjectTransformer extends ModsTransformer<Project> {
        // marker class
    }

    public static class ArchiveTransformer extends ModsTransformer<Archive> {
        // marker class
    }

    public static class AudioTransformer extends ModsTransformer<Audio> {
        // marker class
    }

    public static class VideoTransformer extends ModsTransformer<Video> {
        // marker class
    }

    public static class GeospatialTransformer extends ModsTransformer<Geospatial> {
        // marker class
    }

    public static class DcmiModsTypeMapper {

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

    public static ModsDocument transformAny(Resource resource) {
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

        throw new TdarRecoverableRuntimeException("transformer.no_mods_transformer", Arrays.asList(resource.getClass()));
    }

    public XmlEscapeHelper getX() {
        return x;
    }

    public void setX(XmlEscapeHelper x) {
        this.x = x;
    }

}
