package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.common.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.citation.Citation;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.bulk.BulkUploadTemplate;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.Pair;
import org.tdar.utils.TestConfiguration;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
@RunWith(MultipleTdarConfigurationRunner.class)
public class BulkUploadControllerITCase extends AbstractAdminControllerITCase {

    private static final String OTHER_LICENCE_TYPE_BOILERPLATE = "A long and boring piece of waffle";

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    @Test
    @Rollback
    public void testExcelTemplate() throws FileNotFoundException, IOException {
        BulkUploadController bulkUploadController = generateNewInitializedController(BulkUploadController.class);
        bulkUploadController.prepare();
        File file = File.createTempFile("tempTemplate", ".xls");
        String downloadBulkTemplate = bulkUploadController.downloadBulkTemplate();
        logger.info(bulkUploadController.getTemplateFile().getCanonicalPath());
        assertEquals(TdarActionSupport.SUCCESS, downloadBulkTemplate);
        FileInputStream templateInputStream = bulkUploadController.getTemplateInputStream();
        assertFalse(null == templateInputStream);
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.copy(templateInputStream, fos);
        logger.info(file.getCanonicalPath());
        fos.close();
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    @Rollback
    public void testBulkUpload() throws Exception {
        BulkUploadController bulkUploadController = generateNewInitializedController(BulkUploadController.class);
        bulkUploadController.prepare();

        // setup images to upload
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        assertTrue(testImagesDirectory.isDirectory());
        List<File> uploadFiles = new ArrayList<File>();
        uploadFiles.addAll(FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, false));

        Pair<PersonalFilestoreTicket, List<FileProxy>> proxyPair = uploadFilesAsync(uploadFiles);
        final Long ticketId = proxyPair.getFirst().getId();
        bulkUploadController.setTicketId(ticketId);
        bulkUploadController.setProjectId(TestConstants.ADMIN_INDEPENDENT_PROJECT_ID);
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
        bulkUploadController.getResourceNotes().addAll(Arrays.asList(note));
        bulkUploadController.getPersistable().setTitle(BulkUploadTemplate.BULK_TEMPLATE_TITLE);
        // add some source/ comparative collections
        addComparitiveCollections(bulkUploadController);

        bulkUploadController.setFileProxies(proxyPair.getSecond());
        bulkUploadController.setAsync(false);
        // saving
        bulkUploadController.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.SUCCESS_ASYNC, bulkUploadController.save());
        bulkUploadController.checkStatus();
        assertEquals(new Float(100), bulkUploadController.getPercentDone());

        List<Pair<Long, String>> details = bulkUploadController.getDetails();
        boolean manifest_gc = false;
        boolean manifest_book = false;
        logger.info("{}", details);
        logger.info(bulkUploadController.getAsyncErrors());
        assertTrue(StringUtils.isEmpty(bulkUploadController.getAsyncErrors()));
        for (Pair<Long, String> detail : details) {
            Resource resource = resourceService.find(detail.getFirst());
            logger.info("{}", resource);
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
            assertFalse(resource.getResourceCreators().isEmpty());
            assertEquals(TestConstants.ADMIN_INDEPENDENT_PROJECT_ID, ((InformationResource) resource).getProjectId());
            if (resource.getTitle().equals("Grand Canyon")) {
                assertEquals("A photo of the grand canyon", resource.getDescription());
                assertEquals(1, resource.getResourceCreators().size());
                ResourceCreator creator = resource.getResourceCreators().iterator().next();
                assertEquals(ResourceCreatorRole.CREATOR, creator.getRole());
                assertEquals(TestConfiguration.getInstance().getUserId(), creator.getCreator().getId());
                manifest_gc = true;
            }
            if (resource.getTitle().equals("Handbooks of Archaeology and Antiqvities")) {
                assertEquals("old book", resource.getDescription());
                assertEquals(2, resource.getResourceCreators().size());
                // test resource.getResourceCreators() OrderBy.
                // Iterator<ResourceCreator> iterator = resource.getResourceCreators().iterator();
                // ResourceCreator rc = iterator.next();
                // assertEquals(ResourceCreatorRole.SPONSOR, rc.getRole());
                // assertEquals(TestConstants.TEST_INSTITUTION_ID, rc.getCreator().getId());
                // rc = iterator.next();
                // assertEquals(ResourceCreatorRole.LAB_DIRECTOR, rc.getRole());
                // assertEquals(TestConstants.ADMIN_USER_ID, rc.getCreator().getId());
                //
                HashMap<Integer, ResourceCreator> map = new HashMap<Integer, ResourceCreator>();
                for (ResourceCreator rc : resource.getResourceCreators()) {
                    map.put(rc.getSequenceNumber(), rc);
                }
                assertEquals(ResourceCreatorRole.SPONSOR, map.get(0).getRole());
                assertEquals(TestConstants.TEST_INSTITUTION_ID, map.get(0).getCreator().getId());
                assertEquals(ResourceCreatorRole.LAB_DIRECTOR, map.get(1).getRole());
                assertEquals(TestConfiguration.getInstance().getAdminUserId(), map.get(1).getCreator().getId());
                manifest_book = true;
            }
            assertSourceAndComparitiveCollections(resource);
        }
        assertTrue("handbook of archaeology not found", manifest_book);
        assertTrue("grand canyon photo not found", manifest_gc);

    }

    private void addComparitiveCollections(BulkUploadController bulkUploadController) {
        bulkUploadController.getRelatedComparativeCollections().add(createComparitiveColleciton("one"));
        bulkUploadController.getRelatedComparativeCollections().add(createComparitiveColleciton("two"));
        bulkUploadController.getRelatedComparativeCollections().add(createComparitiveColleciton("three"));

        bulkUploadController.getSourceCollections().add(createSourceCollection("sc one"));
        bulkUploadController.getSourceCollections().add(createSourceCollection("sc two"));
        bulkUploadController.getSourceCollections().add(createSourceCollection("sc three"));
    }

    private void assertSourceAndComparitiveCollections(Resource resource) {
        assertTrue(resource.getRelatedComparativeCollections().size() == 3);
        assertTrue(resource.getSourceCollections().size() == 3);

        Set<String> rccs = new HashSet<String>();
        Set<String> scs = new HashSet<String>();

        Transformer t = new Transformer() {
            @Override
            public Object transform(Object input) {
                Citation c = (Citation) input;
                return c.getText();
            }
        };

        CollectionUtils.collect(resource.getRelatedComparativeCollections(), t, rccs);
        CollectionUtils.collect(resource.getSourceCollections(), t, scs);

        assertTrue(scs.contains("sc one"));
        assertTrue(scs.contains("sc two"));
        assertTrue(scs.contains("sc three"));

        assertTrue(rccs.contains("one"));
        assertTrue(rccs.contains("two"));
        assertTrue(rccs.contains("three"));
    }

    private RelatedComparativeCollection createComparitiveColleciton(String text) {
        RelatedComparativeCollection rcc = new RelatedComparativeCollection();
        rcc.setText(text);
        return rcc;
    }

    private SourceCollection createSourceCollection(String text) {
        SourceCollection sc = new SourceCollection();
        sc.setText(text);
        return sc;
    }

    @Test
    @Rollback
    public void testBadBulkUpload() throws Exception {
        BulkUploadController bulkUploadController = setupBasicBulkUploadTest("image_manifest2.xlsx");
        assertEquals(new Float(100), bulkUploadController.getPercentDone());

        List<Pair<Long, String>> details = bulkUploadController.getDetails();
        logger.info("{}", details);
        logger.debug(bulkUploadController.getAsyncErrors());
        assertFalse(StringUtils.isEmpty(bulkUploadController.getAsyncErrors()));
        assertTrue(bulkUploadController.getAsyncErrors().contains("resource creator is not valid"));
    }

    @Test
    @Rollback
    public void testBulkUploadWithFloat() throws Exception {
        BulkUploadController bulkUploadController = setupBasicBulkUploadTest("image_manifest_float_date.xls");
        assertEquals(new Float(100), bulkUploadController.getPercentDone());
        // testing that an Float that is effectively an int 120.00 is ok in an int field
        List<Pair<Long, String>> details = bulkUploadController.getDetails();
        logger.info("{}", details);
        logger.debug(bulkUploadController.getAsyncErrors());
        assertTrue(StringUtils.isEmpty(bulkUploadController.getAsyncErrors()));
        assertTrue(resourceService.find(details.get(0).getFirst()).isActive());
        assertTrue(resourceService.find(details.get(1).getFirst()).isActive());
        assertEquals(new Integer(1234), ((InformationResource) resourceService.find(details.get(0).getFirst())).getDate());
        assertEquals(new Integer(2222), ((InformationResource) resourceService.find(details.get(1).getFirst())).getDate());
    }

    @Test
    @Rollback
    public void testBadBulkUploadNewFormat() throws Exception {
        BulkUploadController bulkUploadController = setupBasicBulkUploadTest("bulk_upload_newer_ok.xls");
        assertEquals(new Float(100), bulkUploadController.getPercentDone());

        List<Pair<Long, String>> details = bulkUploadController.getDetails();
        logger.debug(bulkUploadController.getAsyncErrors());
        assertFalse(StringUtils.isEmpty(bulkUploadController.getAsyncErrors()));
        assertTrue(bulkUploadController.getAsyncErrors().contains(
                "<li>5127663428_42ef7f4463_b.jpg : the fieldname Book Title is not valid for the resource type: IMAGE</li>"));
        // should be two results, one deleted b/c of errors
        assertEquals(0, bulkUploadController.getDetails().size());
//        assertEquals(Status.DELETED, resourceService.find(details.get(0).getFirst()).getStatus());
//        assertEquals(Status.ACTIVE, resourceService.find(details.get(1).getFirst()).getStatus());
    }

    @Test
    @Rollback
    public void testBadBulkUploadMissingDates() throws Exception {
        BulkUploadController bulkUploadController = setupBasicBulkUploadTest("image_manifest_no_dates.xlsx");
        assertEquals(new Float(100), bulkUploadController.getPercentDone());

        List<Pair<Long, String>> details = bulkUploadController.getDetails();
        logger.info("{}", details);
        logger.debug(bulkUploadController.getAsyncErrors());
        assertFalse(StringUtils.isEmpty(bulkUploadController.getAsyncErrors()));
        assertTrue(bulkUploadController.getAsyncErrors().contains("<li>the following columns are required: Date Created (Year)</li>"));
    }

    @Test
    @Rollback
    public void testBadBulkUploadMissingDatesAndDescription() throws Exception {
        BulkUploadController bulkUploadController = setupBasicBulkUploadTest("image_manifest_no_description_or_date.xlsx");
        assertEquals(new Float(100), bulkUploadController.getPercentDone());

        List<Pair<Long, String>> details = bulkUploadController.getDetails();
        logger.info("{}", details);
        logger.debug(bulkUploadController.getAsyncErrors());
        assertFalse(StringUtils.isEmpty(bulkUploadController.getAsyncErrors()));

        assertTrue(bulkUploadController.getAsyncErrors().contains("<li>the following columns are required: Description, Date Created (Year)</li>"));
    }

    @Test
    @Rollback
    public void testBadBulkUploadManifestColumnHasBlanks() throws Exception {
        String manifestFilename = "document_manifest_required_col_has_blanks.xls";
        // this file should fail validation and shouldn't even get to the async saving part.
        // the controller will always return success, however
        BulkUploadController bulkUploadController = setupBasicBulkUploadTest(manifestFilename, BulkUploadController.SUCCESS_ASYNC);
        logger.debug(bulkUploadController.getAsyncErrors());
        assertFalse(StringUtils.isEmpty(bulkUploadController.getAsyncErrors()));
        assertFalse(bulkUploadController.getAsyncErrors().contains("<li>the following columns are required: Title, Description, Date Created (Year)</li>"));
        assertTrue(bulkUploadController
                .getAsyncErrors()
                .contains(
                        "<li>skipping line in excel file as resource with the filename \"Codes E1 txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E2.txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E3.txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E4.txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E5A.txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E5B.txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E6.txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E7.txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E8.txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E9.txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E10.txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E11.txt\" was not found in the import batch</li><li>skipping line in excel file as resource with the filename \"Codes E12.txt\" was not found in the import batch</li>"));
    }

    private BulkUploadController setupBasicBulkUploadTest(String manifestName, String expectedResponse) throws Exception {
        BulkUploadController bulkUploadController = generateNewInitializedController(BulkUploadController.class);
        bulkUploadController.prepare();

        // setup images to upload
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        assertTrue(testImagesDirectory.isDirectory());
        List<File> uploadFiles = new ArrayList<File>();
        uploadFiles.addAll(FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, false));

        Pair<PersonalFilestoreTicket, List<FileProxy>> proxyPair = uploadFilesAsync(uploadFiles);
        final Long ticketId = proxyPair.getFirst().getId();
        bulkUploadController.setTicketId(ticketId);
        bulkUploadController.setProjectId(TestConstants.ADMIN_INDEPENDENT_PROJECT_ID);
        // setup controller
        bulkUploadController.setUploadedFiles(Arrays.asList(new File(TestConstants.TEST_BULK_DIR + manifestName)));
        bulkUploadController.setUploadedFilesFileName(Arrays.asList(manifestName));
        bulkUploadController.setFileProxies(proxyPair.getSecond());
        bulkUploadController.setAsync(false);
        // saving
        bulkUploadController.setServletRequest(getServletPostRequest());
        assertEquals(expectedResponse, bulkUploadController.save());
        bulkUploadController.checkStatus();
        return bulkUploadController;
    }

    private BulkUploadController setupBasicBulkUploadTest(String manifestName) throws Exception {
        return setupBasicBulkUploadTest(manifestName, BulkUploadController.SUCCESS_ASYNC);
    }

    @Test
    @Rollback
    public void testCloneInformationResourceWithNotes() throws Exception {
        Dataset dataset = new Dataset();
        dataset.setTitle("Test dataset becoming document");
        dataset.setDescription(dataset.getTitle());
        dataset.markUpdated(getUser());
        dataset.setDate(1234);
        genericService.saveOrUpdate(dataset);
        dataset.getResourceNotes().add(new ResourceNote(ResourceNoteType.GENERAL, "This is a note, hello"));
        Document document = resourceService.createResourceFrom(dataset, Document.class);
        assertFalse(document.getResourceNotes().isEmpty());
        assertEquals(1, document.getResourceNotes().size());
        ResourceNote documentNote = document.getResourceNotes().iterator().next();
    }

    @Test
    @Rollback
    public void testCloneImageInheritanceSettings() {
        Image expected = new Image();
        expected.setTitle("test image");
        expected.markUpdated(getUser());
        expected.setDescription(expected.getDescription());
        expected.setDate(1234);
        expected.setProject(Project.NULL);
        // assuming all inheritance flags are false by default.

        expected.setInheritingCulturalInformation(true);
        Image image1 = resourceService.createResourceFrom(expected, Image.class);
        assertEquals(expected.isInheritingCulturalInformation(), image1.isInheritingCulturalInformation());

        expected.setInheritingInvestigationInformation(true);
        Image image2 = resourceService.createResourceFrom(expected, Image.class);
        assertEquals(expected.isInheritingInvestigationInformation(), image2.isInheritingInvestigationInformation());

        expected.setInheritingMaterialInformation(true);
        Image image3 = resourceService.createResourceFrom(expected, Image.class);
        assertEquals(expected.isInheritingMaterialInformation(), image3.isInheritingMaterialInformation());

        expected.setInheritingOtherInformation(true);
        Image image4 = resourceService.createResourceFrom(expected, Image.class);
        assertEquals(expected.isInheritingOtherInformation(), image4.isInheritingOtherInformation());

        expected.setInheritingSiteInformation(true);
        Image image5 = resourceService.createResourceFrom(expected, Image.class);
        assertEquals(expected.isInheritingSiteInformation(), image5.isInheritingSiteInformation());

        expected.setInheritingSpatialInformation(true);
        Image image6 = resourceService.createResourceFrom(expected, Image.class);
        assertEquals(expected.isInheritingSpatialInformation(), image6.isInheritingSpatialInformation());

        expected.setInheritingTemporalInformation(true);
        Image image8 = resourceService.createResourceFrom(expected, Image.class);
        assertEquals(expected.isInheritingTemporalInformation(), image8.isInheritingTemporalInformation());
    }

    @Test
    @Rollback
    public void testCloneImageWithRelatedCollections() {
        Image expected = new Image();
        expected.setTitle("test image");
        expected.markUpdated(getUser());
        expected.setDescription(expected.getDescription());
        expected.setDate(1234);
        expected.setProject(Project.NULL);

        RelatedComparativeCollection rcc = new RelatedComparativeCollection();
        rcc.setText("rcc text");
        expected.getRelatedComparativeCollections().add(rcc);

        Image image = resourceService.createResourceFrom(expected, Image.class);
        assertEquals(expected.getRelatedComparativeCollections().iterator().next().getText(),
                image.getRelatedComparativeCollections().iterator().next().getText());
    }

    @Test
    @Rollback
    public void testCloneImageWithSourceCollections() {
        Image expected = new Image();
        expected.setTitle("test image");
        expected.markUpdated(getUser());
        expected.setDescription(expected.getDescription());
        expected.setDate(1234);
        expected.setProject(Project.NULL);

        SourceCollection sc = new SourceCollection();

        sc.setText("source collection text");
        expected.getSourceCollections().add(sc);

        Image image = resourceService.createResourceFrom(expected, Image.class);
        assertEquals(expected.getSourceCollections().iterator().next().getText(),
                image.getSourceCollections().iterator().next().getText());
    }

    @Test
    @Rollback
    public void testCloneImageWithNoLicenceEnabled() {
        Image expected = new Image();
        Image image = resourceService.createResourceFrom(expected, expected.getClass());
        // the assumption is that both of these will default to null
        assertTrue(image.getLicenseType() == null);
        assertTrue(image.getLicenseText() == null);
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.FAIMS })
    public void testCloneImageWithLicencesEnabled() {
        Image expected = new Image();
        expected.setLicenseType(LicenseType.CREATIVE_COMMONS_ATTRIBUTION);
        expected.setLicenseText("This should be ignored");
        Image image = resourceService.createResourceFrom(expected, expected.getClass());
        assertTrue(LicenseType.CREATIVE_COMMONS_ATTRIBUTION.equals(image.getLicenseType()));
        assertTrue(image.getLicenseText() == null);
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.FAIMS })
    public void testCloneImageWithLicencesEnabledOtherLicenceType() {
        Image expected = new Image();
        expected.setLicenseType(LicenseType.OTHER);
        expected.setLicenseText(OTHER_LICENCE_TYPE_BOILERPLATE);
        Image image = resourceService.createResourceFrom(expected, expected.getClass());
        assertTrue(LicenseType.OTHER.equals(image.getLicenseType()));
        assertTrue(OTHER_LICENCE_TYPE_BOILERPLATE.equals(image.getLicenseText()));
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.FAIMS })
    public void testCloneImageWithCopyrightEnabled() {
        Image expected = new Image();
        Creator copyrightHolder = entityService.find(getAdminUserId());
        expected.setCopyrightHolder(copyrightHolder);
        Image image = resourceService.createResourceFrom(expected, expected.getClass());
        assertTrue(copyrightHolder.equals(image.getCopyrightHolder()));
    }

    @Test
    @Rollback
    public void testCloneImageWithNoCopyrightEnabled() {
        Image expected = new Image();
        Image image = resourceService.createResourceFrom(expected, expected.getClass());
        // the assumption is that this will default to null
        assertTrue(image.getCopyrightHolder() == null);
    }

    @Test
    @Rollback
    /*
     * When loading N many resources and creating an adhoc collection, ensure:
     * -the controller only creates one shared collection
     * -the shared collection contains all N resources
     * -the controller creates N internal collections, each containing a single resource w/ edit rights to the creator
     */
    public void testAddingAdHocCollectionToBulkUpload() throws Exception {
        // start by getting the original count of public/private collections
        // int origInternalCount = getCollectionCount(CollectionType.INTERNAL);
        int origSharedCount = getCollectionCount(CollectionType.SHARED);
        int origImageCount = genericService.findAll(Image.class).size();

        BulkUploadController bulkUploadController = generateNewInitializedController(BulkUploadController.class);
        bulkUploadController.prepare();

        // setup images to upload
        File testImagesDirectory = new File(TestConstants.TEST_IMAGE_DIR);
        assertTrue(testImagesDirectory.isDirectory());
        List<File> uploadFiles = new ArrayList<File>();
        uploadFiles.addAll(FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, false));
        assertEquals("sanity check: we just added two files, right?", 2, uploadFiles.size());

        Pair<PersonalFilestoreTicket, List<FileProxy>> proxyPair = uploadFilesAsync(uploadFiles);
        final Long ticketId = proxyPair.getFirst().getId();
        bulkUploadController.setTicketId(ticketId);
        bulkUploadController.setProjectId(TestConstants.ADMIN_INDEPENDENT_PROJECT_ID);

        // setup controller
        bulkUploadController.setUploadedFiles(Arrays.asList(new File(TestConstants.TEST_BULK_DIR + "image_manifest.xlsx")));
        bulkUploadController.setUploadedFilesFileName(Arrays.asList("image_manifest.xlsx"));
        bulkUploadController.setFileProxies(proxyPair.getSecond());
        bulkUploadController.setAsync(false);
        bulkUploadController.getResource().setTitle("test");
        bulkUploadController.getResource().setDescription("test");
        bulkUploadController.getResource().setDate(1234);

        // specify an adhoc collection
        ResourceCollection adHocCollection = new ResourceCollection();
        // NEED TO SET THE TYPE OF THE ADHOC COLLECTION
        adHocCollection.setType(CollectionType.SHARED);
        adHocCollection.setName("collection of bulk-uploaded resource collections");
        bulkUploadController.getAuthorizedUsers().add(new AuthorizedUser(getUser(), GeneralPermissions.MODIFY_RECORD));
        bulkUploadController.getResourceCollections().add(adHocCollection);

        // saving
        bulkUploadController.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.SUCCESS_ASYNC, bulkUploadController.save());
        bulkUploadController.checkStatus();
        assertEquals(new Float(100), bulkUploadController.getPercentDone());

        // int newInternalCount = getCollectionCount(CollectionType.INTERNAL);
        int newSharedCount = getCollectionCount(CollectionType.SHARED);
        int newImageCount = genericService.findAll(Image.class).size();
        Assert.assertNotSame(origImageCount, newImageCount);
        assertTrue((newImageCount - origImageCount) > 0);
        // ensure one shared collection created
        // genericService.synchronize();

        List<Pair<Long, String>> details = bulkUploadController.getDetails();
        logger.info("{}", details);
        Set<ResourceCollection> collections = new HashSet<ResourceCollection>();
        genericService.synchronize();
        logger.debug("inspecting collections created:");
        for (Pair<Long, String> detail : details) {
            Resource resource = resourceService.find(detail.getFirst());
            genericService.refresh(resource);
            Set<ResourceCollection> resourceCollections = resource.getResourceCollections();
            logger.debug("\t resource:{}\t  resourceCollections:{}", resource.getTitle(), resourceCollections.size());
            for (ResourceCollection rc : resourceCollections) {
                logger.debug("\t\t {}",rc);
            }

            collections.addAll(resourceCollections);
        }
        assertEquals("we should have a total of 3 collections (2 internal +1 shared)", 3, collections.size());

        assertEquals("we should have one new adhoc collection", 1, newSharedCount - origSharedCount);
        // ensure N internal collections created
        // String msg = String.format("We should have %s new internal collections.  newcount:%s oldcount:%s", uploadFiles.size(),
        // newInternalCount, origInternalCount);
        // assertEquals(msg, uploadFiles.size(), newInternalCount - origInternalCount );
    }

    private int getCollectionCount(CollectionType type) {
        List<ResourceCollection> col = resourceCollectionDao.findCollectionsOfParent(null, null, type);
        if (type == CollectionType.INTERNAL) {
            logger.info("INTERNAL COLLECTIONS: {} ", col);
        }
        return col.size();
    }

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

}
