package org.tdar.transform;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.Archive;
import org.tdar.core.bean.resource.Audio;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.Video;
import org.tdar.exception.TdarRecoverableRuntimeException;

import edu.asu.lib.eml.EMLDocument;
import eml.ecoinformatics_org.literature_2_1.CitationType;
import eml.ecoinformatics_org.party_2_1.ObjectFactory;
import eml.ecoinformatics_org.party_2_1.ResponsibleParty;

public abstract class EMLDocumentTransformer<R extends Resource> implements Transformer<R, EMLDocument> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Examples of EML Document...
     * http://dot.ucop.edu/specs/emlcore.html
     * http://sbc.lternet.edu/external/InformationManagement/EML/docs/eml-2.1.0/#N10068
     * 
     */

    @Override
    public EMLDocument transform(R source) {
        EMLDocument doc = new EMLDocument();
        buildCitation(source, doc);
        return doc;
    }

    private void buildCitation(R source, EMLDocument doc) {
        CitationType citation = doc.getCitation();

        citation.setAbstract(doc.createTextType(source.getDescription()));
        citation.getTitle().add(doc.createNonEmptyStringType(source.getTitle()));
        // citation.setPubDate(source.getDate());
        for (ResourceCreator rc : source.getPrimaryCreators()) {
            ObjectFactory partyFactory = new ObjectFactory();
            ResponsibleParty party = new ResponsibleParty();
            Creator<?> creator = rc.getCreator();
            Institution inst = null;
            if (creator instanceof Person) {
                Person person = (Person) creator;
                eml.ecoinformatics_org.party_2_1.Person ePerson = partyFactory.createPerson();
                ePerson.setSurName(doc.createNonEmptyStringType(person.getLastName()));
                ePerson.getGivenName().add(doc.createNonEmptyStringType(person.getFirstName()));
                party.getIndividualNameOrOrganizationNameOrPositionName().add(partyFactory.createResponsiblePartyIndividualName(ePerson));
                if (person.getInstitution() != null) {
                    inst = person.getInstitution();
                }
            }
            // party.getUserId().add(e);
            // party.getId().add(e);
            if (creator instanceof Institution) {
                inst = (Institution) creator;
            }

            if (inst != null) {
                party.getIndividualNameOrOrganizationNameOrPositionName()
                        .add(partyFactory.createResponsiblePartyOrganizationName(doc.createNonEmptyStringType(inst.getProperName())));

            }
            citation.getCreator().add(party);
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

    public static class ProjectTransformer extends EMLDocumentTransformer<Project> {
        // marker class
    }

    public static class InformationResourceTransformer<I extends InformationResource> extends EMLDocumentTransformer<I> {

        @Override
        public EMLDocument transform(I source) {
            EMLDocument dc = super.transform(source);
            return dc;
        }
    }

    public static class DocumentTransformer extends InformationResourceTransformer<Document> {

        @Override
        public EMLDocument transform(Document source) {
            EMLDocument dc = super.transform(source);
            return dc;
        }
    }

    public static EMLDocument transformAny(Resource resource) {
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

        throw new TdarRecoverableRuntimeException("transformer.no_eml_transformer", Arrays.asList(resource.getClass()));
    }

}
