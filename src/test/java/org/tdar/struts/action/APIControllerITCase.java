/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.tdar.core.bean.entity.FullUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.exception.APIException;
import org.tdar.core.service.InvestigationTypeService;
import org.tdar.core.service.ResourceService;

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

    @Test
    @Rollback(true)
    public void testAPIController() throws IOException, ClassNotFoundException {
        Resource old = resourceService.find(1L);
        old.getInvestigationTypes().clear();
        old.getInvestigationTypes().add(investigationTypeService.find(1L));
        old.setAccessCounter(100l);
        Date creationDate = old.getDateRegistered();
        ResourceNote note = new ResourceNote();
        note.setResource(old);
        note.setNote("test");
        note.setType(ResourceNoteType.GENERAL);
        FullUser fullUser = new FullUser();
        fullUser.setPerson(getTestPerson());
        fullUser.setResource(old);
        old.getFullUsers().add(fullUser);
        genericService.save(fullUser);

        CoverageDate cd = new CoverageDate(CoverageType.CALENDAR_DATE, 0, 1000);
        old.getCoverageDates().add(cd);

        old.getResourceNotes().add(note);
        genericService.save(note);
        genericService.save(cd);
        genericService.saveOrUpdate(old);
        old = resourceService.find(1L);
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

        assertEquals(1, importedRecord.getFullUsers().size());
        assertEquals(Long.valueOf(100L), importedRecord.getAccessCounter());
        assertEquals(creationDate, importedRecord.getDateRegistered());
        assertEquals(old.getSubmitter(), importedRecord.getSubmitter());
        assertEquals(getUser(), importedRecord.getUpdatedBy());
        assertEquals(1, importedRecord.getInformationResourceFiles().size());
        assertEquals(APIController.UPDATED, uploadStatus);
    }

    @Test
    @Rollback
    public void testNewRecord() throws IOException, ClassNotFoundException {
        APIController controller = generateNewInitializedController(APIController.class);
        String document = FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/documentImport.xml"));
        document = document.replace("<id>1</id>", "");
        controller.setRecord(document);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(APIController.CREATED, uploadStatus);
    }

    @Test
    @Rollback(true)
    public void testInvalidFileType() throws IOException, ClassNotFoundException, APIException {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setRecord(FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/documentImport.xml")));
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_IMAGE)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_IMAGE_NAME));
        String uploadStatus = controller.upload();
        assertEquals(APIController.NOTALLOWED, uploadStatus);
    }

    @Test
    @Rollback(true)
    public void testInvalidUser() throws IOException, ClassNotFoundException {
        APIController controller = generateNewController(APIController.class);
        init(controller, getTestPerson());
        controller.setRecord(FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/documentImport.xml")));
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_DOCUMENT)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_DOCUMENT_NAME));
        String uploadStatus = controller.upload();
        assertEquals(APIController.UNAUTHORIZED, uploadStatus);
    }

    @Test
    @Rollback(true)
    public void testInvalidInvestigationType() throws IOException, ClassNotFoundException {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setRecord(FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/bad-document.xml")));
        String uploadStatus = controller.upload();
        assertEquals(APIController.NOTALLOWED, uploadStatus);
    }

    @Test
    @Rollback(true)
    public void testBadEnum() throws IOException, ClassNotFoundException {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setRecord(FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/bad-enum-document.xml")));
        String uploadStatus = controller.upload();
        assertEquals(APIController.UNKNOWN_ERROR, uploadStatus);
    }

}
