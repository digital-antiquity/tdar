/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.sensory.SensoryDataImage;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.query.SortOption;

/**
 * @author Adam Brin
 * 
 */
public class APIControllerITCase extends AbstractAdminControllerITCase {

    @Autowired
    ResourceService resourceService;

    @Autowired
    XmlService xmlService;

    @Autowired
    GenericKeywordService genericKeywordService;

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    public final static Long TEST_ID = 3794L;

    @Test
    public void testAPIController() throws Exception {
        Document fake = resourceService.find(TEST_ID);
        // setup a fake record, with some new fields off the session
        genericService.markReadOnly(fake);
        fake.setDescription(fake.getTitle());
        fake.getInvestigationTypes().clear();
        fake.getInvestigationTypes().add(genericKeywordService.find(InvestigationType.class, 1L));
        resourceService.updateTransientAccessCount(fake);
        final Long viewCount = fake.getTransientAccessCount();
        final Date creationDate = fake.getDateCreated();
        fake.getResourceNotes().add(new ResourceNote(ResourceNoteType.GENERAL, "test"));
        fake.setInheritingCollectionInformation(false);
        fake.setInheritingCulturalInformation(true);
        fake.setInheritingNoteInformation(true);
        fake.setInheritingMaterialInformation(true);
        addAuthorizedUser(fake, getUser(), GeneralPermissions.MODIFY_RECORD);

        // fake.getResourceCollections().addAll(getSomeResourceCollections());

        fake.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 0, 1000));
        // fake.getResourceCollections().clear();
        String docXml = xmlService.convertToXML(fake);
        logger.info(docXml);

        // revert back
        Document old = resourceService.find(TEST_ID);
        genericService.markReadOnly(old);
        old.getRelatedComparativeCollections().add(new RelatedComparativeCollection("text"));
        final String oldDocXml = xmlService.convertToXML(old);
        assertEquals(1, old.getInternalResourceCollection().getAuthorizedUsers().size());
        genericService.detachFromSession(old);
        final Person oldSubmitter = old.getSubmitter();
        logger.info("DATES: {} ", old.getCoverageDates());
        genericService.detachFromSession(fake);
        final int investigationTypesSize = old.getInvestigationTypes().size();
        final int resourceNotesSize = old.getResourceNotes().size();
        final Long oldId = old.getId();
        old = null;
        fake = null;
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setRecord(docXml);
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_DOCUMENT)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_DOCUMENT_NAME));
        String uploadStatus = controller.upload();
        assertNull(controller.getErrorMessage());
        assertEquals(APIController.SUCCESS, uploadStatus);
        assertEquals(StatusCode.UPDATED.getResultName(), controller.getStatus());

        Document importedRecord = resourceService.find(TEST_ID);
        assertNotNull(importedRecord);
        assertEquals(1, investigationTypesSize);
        assertEquals(investigationTypesSize, importedRecord.getInvestigationTypes().size());
        assertEquals(0, importedRecord.getRelatedComparativeCollections().size());
        assertEquals(1, importedRecord.getCoverageDates().size());
        assertEquals(resourceNotesSize, importedRecord.getResourceNotes().size());
        assertEquals(oldId, importedRecord.getId());

        assertEquals(1, importedRecord.getInternalResourceCollection().getAuthorizedUsers().size());
        resourceService.updateTransientAccessCount(importedRecord);
        assertEquals(viewCount, importedRecord.getTransientAccessCount());
        assertEquals(creationDate, importedRecord.getDateCreated());
        assertEquals(oldSubmitter, importedRecord.getSubmitter());
        assertEquals(getUser(), importedRecord.getUpdatedBy());
        assertEquals(2, importedRecord.getInformationResourceFiles().size());

        assertTrue("field should be inherited", importedRecord.isInheritingCulturalInformation());
        assertFalse("field should be inherited", importedRecord.isInheritingInvestigationInformation());
        assertTrue("field should be inherited", importedRecord.isInheritingMaterialInformation());
        assertTrue("field should be inherited", importedRecord.isInheritingNoteInformation());
        assertFalse("field should be inherited", importedRecord.isInheritingCollectionInformation());
        genericKeywordService.delete(importedRecord);

    }

    // return some public resource collections
    private List<ResourceCollection> getSomeResourceCollections() throws InstantiationException, IllegalAccessException {
        int count = 5;
        List<ResourceCollection> resourceCollections = new ArrayList<ResourceCollection>();
        for (int i = 0; i < count; i++) {
            String email = "someperson" + i + "@mailinator.com";
            Person person = entityService.findByEmail(email);
            if (person == null) {
                person = createAndSaveNewPerson(email, "someperson");
            }
            Document document = createAndSaveNewInformationResource(Document.class, person);
            ResourceCollection rc = new ResourceCollection(document, getAdminUser());
            rc.setName("test collection " + i);
            rc.setSortBy(SortOption.TITLE);
            resourceCollectionService.saveOrUpdate(rc);
            resourceCollections.add(rc);
        }
        return resourceCollections;
    }

    @Test
    @Rollback
    public void testNewRecord() throws Exception {
        Document doc = genericService.findRandom(Document.class, 1).get(0);
        genericService.markReadOnly(doc);
        doc.setId(null);
        String docXml = xmlService.convertToXML(doc);
        logger.info(docXml);
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(APIController.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED.getResultName(), controller.getStatus());
    }

    @Test
    @Rollback
    public void testTrivialRecord() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        SensoryData data = new SensoryData();
        data.setTitle("Test");
        data.setDescription(" a description");
        data.setProject(Project.NULL);
        SensoryDataImage img = new SensoryDataImage();
        img.setDescription("d");
        img.setFilename("1234");
        data.getSensoryDataImages().add(img);
        String docXml = xmlService.convertToXML(data);
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(APIController.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED.getResultName(), controller.getStatus());
    }

    @Test
    @Rollback
    public void testReplaceRecord() throws Exception {
        Document old = (Document) generateInformationResourceWithFileAndUser();
        Long oldIRId = old.getFirstInformationResourceFile().getId();
        Long oldId = old.getId();
        genericService.detachFromSession(old);
        old = null;
        APIController controller = generateNewInitializedController(APIController.class);
        Document document = genericService.findRandom(Document.class, 1).get(0);
        genericService.markReadOnly(document);
        document.setId(oldId);
        String docXml = xmlService.convertToXML(document);
        genericService.detachFromSession(document);
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(APIController.SUCCESS, uploadStatus);
        assertEquals(StatusCode.UPDATED.getResultName(), controller.getStatus());
        document = null;
        controller = null;
        old = (Document) resourceService.find(oldId);
        assertEquals(oldIRId, old.getFirstInformationResourceFile().getId());
    }

    @Test
    @Rollback(true)
    public void testInvalidFileType() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        Dataset doc = genericService.findRandom(Dataset.class, 1).get(0);
        String datasetXml = xmlService.convertToXML(doc);
        controller.setRecord(datasetXml);
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_IMAGE)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_IMAGE_NAME));
        String uploadStatus = controller.upload();
        assertEquals(APIController.ERROR, uploadStatus);
        assertEquals(StatusCode.FORBIDDEN.getResultName(), controller.getStatus());
    }

    @Test
    @Rollback(true)
    public void testInvalidUser() throws Exception {
        Document doc = generateDocumentWithUser();
        String docXml = xmlService.convertToXML(doc);

        APIController controller = generateNewController(APIController.class);
        init(controller, getBasicUser());
        controller.setRecord(docXml);

        String uploadStatus = controller.upload();
        assertEquals(APIController.ERROR, uploadStatus);
        assertEquals(StatusCode.UNAUTHORIZED.getResultName(), controller.getStatus());
    }

    @Test
    @Rollback(true)
    public void testInvalidInvestigationType() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        Document doc = genericService.findRandom(Document.class, 1).get(0);
        genericService.markReadOnly(doc);
        InvestigationType bad = new InvestigationType();
        bad.setLabel("INVAID");
        doc.getInvestigationTypes().add(bad);
        String docXml = xmlService.convertToXML(doc);
        doc = null;
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        assertEquals(APIController.ERROR, uploadStatus);
        assertEquals(StatusCode.FORBIDDEN.getResultName(), controller.getStatus());
    }

    @Test
    @Rollback(true)
    public void testBadEnum() throws IOException, ClassNotFoundException {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setRecord(FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/bad-enum-document.xml")));
        String uploadStatus = controller.upload();
        assertEquals(APIController.ERROR, uploadStatus);
        assertEquals(StatusCode.UNKNOWN_ERROR.getResultName(), controller.getStatus());
    }

}
