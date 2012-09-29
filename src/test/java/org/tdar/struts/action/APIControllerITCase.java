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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.keyword.InvestigationTypeService;
import org.tdar.core.service.resource.ResourceService;

/**
 * @author Adam Brin
 * 
 */
public class APIControllerITCase extends AbstractAdminControllerITCase {

    @Autowired
    ResourceService resourceService;

    @Autowired
    InvestigationTypeService investigationTypeService;

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    public final static Long TEST_ID = 3794L;

    @Test
    @Rollback(true)
    public void testAPIController() throws IOException, ClassNotFoundException {
        Resource old = resourceService.find(TEST_ID);
        old.getInvestigationTypes().clear();
        old.getInvestigationTypes().add(investigationTypeService.find(1L));
        old.setAccessCounter(100l);
        Date creationDate = old.getDateRegistered();
        ResourceNote note = new ResourceNote();
        note.setResource(old);
        note.setNote("test");
        note.setType(ResourceNoteType.GENERAL);
        addAuthorizedUser(old, getUser(), GeneralPermissions.MODIFY_RECORD);

        CoverageDate cd = new CoverageDate(CoverageType.CALENDAR_DATE, 0, 1000);
        old.getCoverageDates().add(cd);

        old.getResourceNotes().add(note);
        genericService.save(note);
        genericService.save(cd);
        genericService.saveOrUpdate(old);
        old = resourceService.find(TEST_ID);
        assertEquals(1, old.getInternalResourceCollection().getAuthorizedUsers().size());
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setRecord(FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/documentImport.xml")));
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_DOCUMENT)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_DOCUMENT_NAME));
        String uploadStatus = controller.upload();
        Document importedRecord = (Document) controller.getImportedRecord();
        logger.info(controller.getErrorMessage());
        assertEquals(0, importedRecord.getCoverageDates().size());
        assertNotNull(importedRecord);
        assertEquals(1, importedRecord.getInvestigationTypes().size());
        assertEquals(2, importedRecord.getResourceNotes().size());

        assertEquals(1, importedRecord.getInternalResourceCollection().getAuthorizedUsers().size());
        assertEquals(Long.valueOf(100L), importedRecord.getAccessCounter());
        assertEquals(creationDate, importedRecord.getDateRegistered());
        assertEquals(old.getSubmitter(), importedRecord.getSubmitter());
        assertEquals(getUser(), importedRecord.getUpdatedBy());
        assertEquals(2, importedRecord.getInformationResourceFiles().size());
        assertTrue("field should be inherited", importedRecord.isInheritingCulturalInformation());
        assertTrue("field should be inherited", importedRecord.isInheritingInvestigationInformation());
        assertTrue("field should be inherited", importedRecord.isInheritingMaterialInformation());
        assertTrue("field should be inherited", importedRecord.isInheritingOtherInformation());
        assertTrue("field should be inherited", importedRecord.isInheritingSiteInformation());
        assertTrue("field should be inherited", importedRecord.isInheritingSpatialInformation());
        assertFalse("field should NOT be inherited", importedRecord.isInheritingTemporalInformation());
        assertEquals(APIController.SUCCESS, uploadStatus);
        assertEquals(StatusCode.UPDATED.getResultString(), controller.getStatus());
    }

    @Test
    @Rollback
    public void testNewRecord() throws IOException, ClassNotFoundException {
        APIController controller = generateNewInitializedController(APIController.class);
        String document = FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/documentImport.xml"));
        document = document.replace("<id>" + TEST_ID + "</id>", "");
        controller.setRecord(document);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(APIController.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED.getResultString(), controller.getStatus());
    }

    @Test
    @Rollback
    public void testReplaceRecord() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Document old = (Document) generateInformationResourceWithFile();
        APIController controller = generateNewInitializedController(APIController.class);
        String document = FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/documentImport.xml"));
        document = document.replace("<id>" + TEST_ID + "</id>", "<id>" + old.getId() + "</id>");
        controller.setRecord(document);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(APIController.SUCCESS, uploadStatus);
        assertEquals(StatusCode.UPDATED.getResultString(), controller.getStatus());
        old = (Document) resourceService.find(old.getId());
        assertTrue(old.getInformationResourceFiles().size() == 1);
    }

    @Test
    @Rollback(true)
    public void testInvalidFileType() throws IOException, ClassNotFoundException, APIException {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setRecord(FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/documentImport.xml")));
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_IMAGE)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_IMAGE_NAME));
        String uploadStatus = controller.upload();
        assertEquals(APIController.ERROR, uploadStatus);
        assertEquals(StatusCode.NOT_ALLOWED.getResultString(), controller.getStatus());
    }

    @Test
    @Rollback(true)
    public void testInvalidUser() throws IOException, ClassNotFoundException {
        APIController controller = generateNewController(APIController.class);
        init(controller, getBasicUser());
        controller.setRecord(FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/documentImport.xml")));
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_DOCUMENT)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_DOCUMENT_NAME));
        String uploadStatus = controller.upload();
        assertEquals(APIController.ERROR, uploadStatus);
        assertEquals(StatusCode.UNAUTHORIZED.getResultString(), controller.getStatus());
    }

    @Test
    @Rollback(true)
    public void testInvalidInvestigationType() throws IOException, ClassNotFoundException {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setRecord(FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/bad-document.xml")));
        String uploadStatus = controller.upload();
        assertEquals(APIController.ERROR, uploadStatus);
        assertEquals(StatusCode.NOT_ALLOWED.getResultString(), controller.getStatus());
    }

    @Test
    @Rollback(true)
    public void testBadEnum() throws IOException, ClassNotFoundException {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setRecord(FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/bad-enum-document.xml")));
        String uploadStatus = controller.upload();
        assertEquals(APIController.ERROR, uploadStatus);
        assertEquals(StatusCode.UNKNOWN_ERROR.getResultString(), controller.getStatus());
    }

}
