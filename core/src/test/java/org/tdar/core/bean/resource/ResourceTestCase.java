package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;

public class ResourceTestCase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void fromStringTest() {
        Status active = Status.fromString("ACTIVE");
        Status draft = Status.fromString("DRAFT");
        Status deleted = Status.fromString("DELETED");
        assertEquals(Status.ACTIVE, active);
        assertEquals(Status.DRAFT, draft);
        assertEquals(Status.DELETED, deleted);
    }

    @Test
    public void valueOfTest() {
        Status active = Status.valueOf("ACTIVE");
        Status draft = Status.valueOf("DRAFT");
        Status deleted = Status.valueOf("DELETED");
        assertEquals(Status.ACTIVE, active);
        assertEquals(Status.DRAFT, draft);
        assertEquals(Status.DELETED, deleted);
    }

    @Test
    public void testResourceCreatorRoles() {
        for (ResourceType type : ResourceType.values()) {
            boolean ok = false;
            if (type.isProject()) {
                continue;
            }
            if (!CollectionUtils.isEmpty(ResourceCreatorRole.getAuthorshipRoles(CreatorType.PERSON, type)) ||
                    !CollectionUtils.isEmpty(ResourceCreatorRole.getAuthorshipRoles(CreatorType.INSTITUTION, type))) {
                ok = true;
            }
            assertTrue("at least one role should be defined for " + type, ok);
        }
    }

    /**
     * This test serves as a reminder to consider copyright when adding new resource types.
     */
    @SuppressWarnings("static-method")
    @Test
    public void testResourceCopyrightHolderRoles() {
        for (ResourceType type : ResourceType.values()) {
            boolean ok = false;
            if (type.isProject()) {
                continue;
            }
            if (ResourceCreatorRole.COPYRIGHT_HOLDER.isRelevantFor(type)) {
                ok = true;
            }
            assertTrue("at least one role should be defined for " + type, ok);
        }
    }

    @Test
    public void testTitleRegex() {
        String title = "The is a test";
        assertEquals("is a test", Sortable.getTitleSort(title));

        title = "This is a test";
        assertEquals(title.toLowerCase(), Sortable.getTitleSort(title));
    }

    @Test
    public void testResourceType() {
        assertEquals(null, ResourceType.fromString(""));
        assertEquals(ResourceType.DOCUMENT, ResourceType.fromString("DOCUMENT"));
        assertEquals(ResourceType.AUDIO, ResourceType.fromString("AUDIO"));
        assertEquals(ResourceType.ARCHIVE, ResourceType.fromString("ARCHIVE"));
    }

    @Test
    public void testResourceTypes() {
        ResourceType type = ResourceType.DOCUMENT;
        assertTrue(type.isDocument());
        assertFalse(type.isDataset());
        type = ResourceType.DATASET;
        assertTrue(type.isDataset());
        assertFalse(type.isDocument());
        type = ResourceType.CODING_SHEET;
        assertTrue(type.isCodingSheet());
        assertFalse(type.isDataset());
        type = ResourceType.ONTOLOGY;
        assertTrue(type.isOntology());
        assertFalse(type.isDataset());
        type = ResourceType.IMAGE;
        assertTrue(type.isImage());
        type = ResourceType.PROJECT;
        assertTrue(type.isProject());
        assertFalse(type.isDataset());
        type = ResourceType.SENSORY_DATA;
        assertTrue(type.isSensoryData());
        assertFalse(type.isDataset());
        type = ResourceType.ARCHIVE;
        assertTrue(type.isArchive());
        assertFalse(type.isDataset());
        type = ResourceType.VIDEO;
        assertTrue(type.isVideo());
        assertFalse(type.isDataset());
    }

    @Test
    public void testResourceTypeFromClass() {
        assertEquals(ResourceType.DOCUMENT, ResourceType.fromClass(Document.class));
        assertEquals(ResourceType.AUDIO, ResourceType.fromClass(Audio.class));
        assertEquals(ResourceType.ARCHIVE, ResourceType.fromClass(Archive.class));
    }

    @Test
    public void testDocumentType() {
        DocumentType type = DocumentType.fromString("CONFERENCE_PRESENTATION");
        assertEquals(DocumentType.CONFERENCE_PRESENTATION, type);
        assertEquals("conference-presentation", type.toUrlFragment());
    }

    @Test
    public void testLanguageEnum() {
        Language type = Language.fromString("MULTIPLE");
        Language type2 = Language.fromISO("fra");
        assertEquals(Language.MULTIPLE, type);
        assertEquals(Language.FRENCH, type2);
    }

    @Test
    public void testCreatorType() {
        assertEquals(CreatorType.INSTITUTION, CreatorType.valueOf("INSTITUTION"));
        assertEquals(CreatorType.PERSON, CreatorType.valueOf(Person.class));
    }

    @Test
    public void testAnnotationType() {
        assertTrue(ResourceAnnotationDataType.FORMAT_STRING.isFormatString());
    }

    @Test
    public void testCoreProjectTitle() {
        Project app = new Project(-5L, "The Alan Parsons Project"); // What goes up, must come down. An insightful lyric, as ever...
        Project cure = new Project(-6L, "The Cure"); // last seen falling off a cliff?
        Project gotan = new Project(-7L, "Gotan project"); // http://www.gotanproject.com/

        String expectedName = "Paleoethnobotany of Otumba";

        Project project = new Project(-8L, expectedName);

        assertEquals("Alan Parsons", app.getCoreTitle());
        assertEquals("Cure", cure.getCoreTitle());
        assertEquals("Gotan", gotan.getCoreTitle());
        assertEquals(expectedName, project.getCoreTitle());

    }
}
