package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.ResourceCreatorRoleType;

public class ResourceTest {

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
            if (type.isProject())
                continue;
            if (!CollectionUtils.isEmpty(ResourceCreatorRole.getAuthorshipRoles(CreatorType.PERSON, type)) ||
                    !CollectionUtils.isEmpty(ResourceCreatorRole.getAuthorshipRoles(CreatorType.INSTITUTION, type))) {
                ok = true;
            }
            assertTrue("at least one role should be defined for " + type, ok);
        }
    }

    @Test
    public void testTitleRegex() {
        String title = "The is a test";
        Document d = new Document();
        d.setTitle(title);
        assertEquals("is a test", d.getTitleSort());

        title = "This is a test";
        d.setTitle(title);
        assertEquals(title.toLowerCase(), d.getTitleSort());
    }

    @Test
    public void testResourceType() {
        assertEquals(null, ResourceType.fromString(""));
        assertEquals(ResourceType.DOCUMENT, ResourceType.fromString("DOCUMENT"));
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

        assertEquals(ResourceType.DOCUMENT, ResourceType.fromString("DOCUMENT"));
    }

    @Test
    public void testResourceTypeFromClass() {
        assertEquals(ResourceType.DOCUMENT, ResourceType.fromClass(Document.class));
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
    public void testNullProject() {
        String json = Project.NULL.toJSON().toString();
        Assert.assertNotNull(json);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCoreProjectTitle() {
        Project app = new Project(-5L, "The Alan Parsons Project");
        Project cure = new Project(-6L, "The Cure");
        Project gotan = new Project(-7L, "Gotan project");

        String expectedName = "Paleoethnobotany of Otumba";

        Project project = new Project(-8L, expectedName);

        assertEquals("Alan Parsons", app.getCoreTitle());
        assertEquals("Cure", cure.getCoreTitle());
        assertEquals("Gotan", gotan.getCoreTitle());
        assertEquals(expectedName, project.getCoreTitle());

    }
}
