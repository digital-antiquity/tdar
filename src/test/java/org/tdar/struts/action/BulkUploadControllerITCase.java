package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.service.BulkUploadService;
import org.tdar.core.service.ResourceService;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.Pair;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public class BulkUploadControllerITCase extends AbstractAdminControllerITCase {

    @Autowired
    BulkUploadService bulkUploadService;

    @Autowired
    ResourceService resourceService;

    @Test
    @Rollback
    public void testBulkUpload() throws Exception {
        BulkUploadController bulkUploadController = generateNewInitializedController(BulkUploadController.class);
        bulkUploadController.prepare();

        // setup images to upload
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        assertTrue(testImagesDirectory.isDirectory());
        List<File> uploadFiles = new ArrayList<File>();
        uploadFiles.addAll(FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, true));

        Pair<PersonalFilestoreTicket, List<FileProxy>> proxyPair = uploadFilesAsync(uploadFiles);
        final Long ticketId = proxyPair.getFirst().getId();
        bulkUploadController.setTicketId(ticketId);

        // setup controller
        bulkUploadController.setUploadedFiles(Arrays.asList(new File(TestConstants.TEST_BULK_DIR + "image_manifest.xlsx")));
        bulkUploadController.setUploadedFilesFileName(Arrays.asList("image_manifest.xlsx"));
        List<Long> materialKeywordIds = genericService.findRandomIds(MaterialKeyword.class, 3);
        Collections.sort(materialKeywordIds);
        bulkUploadController.setMaterialKeywordIds(materialKeywordIds);
        List<Long> siteTypeKeywordIds = genericService.findRandomIds(SiteTypeKeyword.class, 3);
        Collections.sort(siteTypeKeywordIds);
        bulkUploadController.setApprovedSiteTypeKeywordIds(siteTypeKeywordIds);
        ResourceNote note = new ResourceNote(ResourceNoteType.GENERAL, "A harrowing tale of note");
        bulkUploadController.setResourceNotes(Arrays.asList(note));
        bulkUploadController.setFileProxies(proxyPair.getSecond());
        bulkUploadController.setAsync(false);
        // saving
        assertEquals(TdarActionSupport.SUCCESS_ASYNC, bulkUploadController.save());
        bulkUploadController.checkStatus();
        assertEquals(new Float(100), bulkUploadController.getPercentDone());

        List<Pair<Long, String>> details = bulkUploadController.getDetails();
        boolean manifest_gc = false;
        boolean manifest_book = false;
        logger.info(details);
        for (Pair<Long, String> detail : details) {
            Resource resource = resourceService.find(detail.getFirst());
            logger.info(resource);
            List<Long> ids = genericService.extractIds(resource.getMaterialKeywords());
            Collections.sort(ids);
            assertEquals(materialKeywordIds, ids);
            ids = genericService.extractIds(resource.getSiteTypeKeywords());
            Collections.sort(ids);
            assertEquals(siteTypeKeywordIds, ids);
            assertEquals(1, resource.getResourceNotes().size());
            ResourceNote resourceNote = resource.getResourceNotes().iterator().next();
            assertEquals(note.getType(), resourceNote.getType());
            assertEquals(note.getNote(), resourceNote.getNote());
            assertTrue(!resource.getResourceCreators().isEmpty());
            if (resource.getTitle().equals("Grand Canyon")) {
                assertEquals("A photo of the grand canyon", resource.getDescription());
                assertEquals(1, resource.getResourceCreators().size());
                ResourceCreator creator = resource.getResourceCreators().iterator().next();
                assertEquals(ResourceCreatorRole.CREATOR, creator.getRole());
                assertEquals(TestConstants.USER_ID, creator.getCreator().getId());
                manifest_gc = true;
            }
            if (resource.getTitle().equals("Handbooks of Archaeology and Antiqvities")) {
                assertEquals("old book", resource.getDescription());
                assertEquals(2, resource.getResourceCreators().size());
                
                Iterator<ResourceCreator> iterator = resource.getResourceCreators().iterator();
                HashMap<Integer, ResourceCreator> map = new HashMap<Integer, ResourceCreator>();
                for (ResourceCreator rc : resource.getResourceCreators()) {
                    map.put(rc.getSequenceNumber(), rc);
                }
                assertEquals(ResourceCreatorRole.SPONSOR, map.get(0).getRole());
                assertEquals(TestConstants.TEST_INSTITUTION_ID, map.get(0).getCreator().getId());
                assertEquals(ResourceCreatorRole.LAB_DIRECTOR, map.get(1).getRole());
                assertEquals(TestConstants.ADMIN_USER_ID, map.get(1).getCreator().getId());
                manifest_book = true;
            }
        }
        assertTrue("handbook of archaeology not found", manifest_book);
        assertTrue("grand canyon photo not found", manifest_gc);

    }

    @Test
    @Rollback
    public void testCloneCollection() throws Exception {
        Dataset dataset = new Dataset();
        dataset.setTitle("Test dataset becoming document");
        dataset.setDateRegistered(new Date());
        dataset.setSubmitter(getTestPerson());
        ResourceNote note = new ResourceNote();
        note.setResource(dataset);
        note.setNote("This is a note, hello");
        note.setType(ResourceNoteType.GENERAL);
        dataset.setResourceNotes(new HashSet<ResourceNote>(Arrays.asList(note)));
        genericService.saveOrUpdate(dataset);
        Document document = informationResourceService.createResourceFrom(dataset, Document.class);
        assertFalse(document.getResourceNotes().isEmpty());
        assertEquals(1, document.getResourceNotes().size());
        ResourceNote documentNote = document.getResourceNotes().iterator().next();
        assertEquals(document.getId(), documentNote.getResource().getId());
        logger.debug("documentNote.resource.id: " + documentNote.getResource().getId() + " vs dataset.id: " + dataset.getId());
    }

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

}
