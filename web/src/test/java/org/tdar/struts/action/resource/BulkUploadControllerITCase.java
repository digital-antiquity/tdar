package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.common.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.citation.Citation;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
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
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.SerializationService;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.bulk.BulkUpdateStatusAction;
import org.tdar.struts.action.bulk.BulkUploadController;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.Pair;

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

    @Autowired
    SerializationService serializationService;

    @Test
    @Rollback()
    public void testBulkUpload() throws Exception {
        BulkUploadController bulkUploadController = generateNewInitializedController(BulkUploadController.class);
        bulkUploadController.prepare();

        // setup images to upload
        File testImagesDirectory = TestConstants.getFile(TestConstants.TEST_IMAGE_DIR);
        assertTrue(testImagesDirectory.isDirectory());
        List<File> uploadFiles = new ArrayList<File>();
        uploadFiles.addAll(FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, false));

        Pair<PersonalFilestoreTicket, List<FileProxy>> proxyPair = uploadFilesAsync(uploadFiles);
        final Long ticketId = proxyPair.getFirst().getId();
        bulkUploadController.setTicketId(ticketId);
        Long projectId = 3462L;
        bulkUploadController.setProjectId(projectId);
        // setup controller
        List<Long> materialKeywordIds = genericService.findRandomIds(MaterialKeyword.class, 3);
        Collections.sort(materialKeywordIds);
        bulkUploadController.setApprovedMaterialKeywordIds(materialKeywordIds);
        List<Long> siteTypeKeywordIds = genericService.findRandomIds(SiteTypeKeyword.class, 3);
        Collections.sort(siteTypeKeywordIds);
        bulkUploadController.getPersistable().setInheritingCulturalInformation(true);
        bulkUploadController.getPersistable().setInheritingIndividualAndInstitutionalCredit(true);
        bulkUploadController.setApprovedSiteTypeKeywordIds(siteTypeKeywordIds);
        ResourceNote note = new ResourceNote(ResourceNoteType.GENERAL, "A harrowing tale of note");
        bulkUploadController.getResourceNotes().addAll(Arrays.asList(note));
        bulkUploadController.getPersistable().setTitle("test");
        // add some source/ comparative collections
        addComparitiveCollections(bulkUploadController);

        bulkUploadController.setFileProxies(proxyPair.getSecond());
        bulkUploadController.setAsync(false);
        // saving
        bulkUploadController.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.SUCCESS_ASYNC, bulkUploadController.save());
        BulkUpdateStatusAction basa = checkStatus(ticketId);
        assertEquals(new Float(100), basa.getPercentDone());
        evictCache();
        List<Pair<Long, String>> details = basa.getDetails();
        boolean manifest_gc = false;
        boolean manifest_book = false;
        logger.info("{}", details);
        logger.info(basa.getAsyncErrors());
        assertTrue(StringUtils.isEmpty(basa.getAsyncErrors()));
        for (Pair<Long, String> detail : details) {
            Resource resource = resourceService.find(detail.getFirst());
            logger.info("{}", resource);
            List<Long> ids = genericService.extractIds(resource.getMaterialKeywords());
            Collections.sort(ids);
            assertEquals(materialKeywordIds, ids);
            ids = genericService.extractIds(resource.getSiteTypeKeywords());
            Collections.sort(ids);
            assertEquals(siteTypeKeywordIds, ids);
            logger.debug(serializationService.convertToXML(resource));
            assertEquals(1, resource.getResourceNotes().size());
            ResourceNote resourceNote = resource.getResourceNotes().iterator().next();
            assertEquals(note.getType(), resourceNote.getType());
            assertEquals(note.getNote(), resourceNote.getNote());
            assertTrue(resource.getResourceCreators().isEmpty());
            assertEquals(projectId, ((InformationResource) resource).getProjectId());
            assertSourceAndComparitiveCollections(resource);
        }
    }

    private void addComparitiveCollections(BulkUploadController bulkUploadController) {
        bulkUploadController.getRelatedComparativeCollections().add(createComparitiveColleciton("one"));
        bulkUploadController.getRelatedComparativeCollections().add(createComparitiveColleciton("two"));
        bulkUploadController.getRelatedComparativeCollections().add(createComparitiveColleciton("three"));

        bulkUploadController.getSourceCollections().add(createSourceCollection("sc one"));
        bulkUploadController.getSourceCollections().add(createSourceCollection("sc two"));
        bulkUploadController.getSourceCollections().add(createSourceCollection("sc three"));
    }

    @SuppressWarnings("unused")
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

        for (RelatedComparativeCollection rcc : resource.getRelatedComparativeCollections()) {
            rccs.add(rcc.getText());
        }
        for (SourceCollection rcc : resource.getSourceCollections()) {
            scs.add(rcc.getText());
        }

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
    // @Ignore
    public void testDatasetBulkUpload() throws Exception {
        List<File> files = new ArrayList<>();
        File file = TestConstants.getFile(TestConstants.TEST_DATA_INTEGRATION_DIR, "Pundo faunal remains.xls");
        files.add(file);
        assertTrue(file.exists());
        BulkUploadController bulkUploadController = setupBasicBulkUploadTest( TdarActionSupport.SUCCESS_ASYNC, files);
        BulkUpdateStatusAction basa = checkStatus(bulkUploadController.getTicketId());
        assertEquals(new Float(100), basa.getPercentDone());

        List<Pair<Long, String>> details = basa.getDetails();
        logger.info("{}", details);
        logger.debug(basa.getAsyncErrors());
        assertTrue(StringUtils.isEmpty(basa.getAsyncErrors()));
    }

    @Test
    @Rollback
    public void testBulkUploadWithFloat() throws Exception {
        BulkUploadController bulkUploadController = setupBasicBulkUploadTest(TdarActionSupport.SUCCESS_ASYNC);
        BulkUpdateStatusAction basa = checkStatus(bulkUploadController.getTicketId());

        assertEquals(new Float(100), basa.getPercentDone());
        // testing that an Float that is effectively an int 120.00 is ok in an int field
        List<Pair<Long, String>> details = basa.getDetails();
        logger.info("{}", details);
        logger.debug(basa.getAsyncErrors());
        assertTrue(StringUtils.isEmpty(basa.getAsyncErrors()));
        Resource find1 = resourceService.find(details.get(0).getFirst());
        assertEquals(Status.ACTIVE, find1.getStatus());
        assertTrue(resourceService.find(details.get(1).getFirst()).isActive());
//        assertEquals(new Integer(1234), ((InformationResource) resourceService.find(details.get(0).getFirst())).getDate());
//        assertEquals(new Integer(2222), ((InformationResource) resourceService.find(details.get(1).getFirst())).getDate());
    }

    private BulkUploadController setupBasicBulkUploadTest(String expectedResponse, List<File> uploadFiles) throws Exception {
        TdarUser user = createAndSaveNewUser();
        BulkUploadController bulkUploadController = generateNewController(BulkUploadController.class);
        init(bulkUploadController, user);
        bulkUploadController.prepare();

        Pair<PersonalFilestoreTicket, List<FileProxy>> proxyPair = uploadFilesAsync(uploadFiles);
        final Long ticketId = proxyPair.getFirst().getId();
        bulkUploadController.setTicketId(ticketId);
        bulkUploadController.setProjectId(TestConstants.ADMIN_INDEPENDENT_PROJECT_ID);
        // setup controller
        bulkUploadController.setFileProxies(proxyPair.getSecond());
        bulkUploadController.setAsync(false);
        // saving
        bulkUploadController.setServletRequest(getServletPostRequest());
        assertEquals(expectedResponse, bulkUploadController.save());
        checkStatus(ticketId);
        return bulkUploadController;
    }

    private BulkUpdateStatusAction checkStatus(final Long ticketId) {
        BulkUpdateStatusAction basa = generateNewInitializedController(BulkUpdateStatusAction.class);
        basa.setTicketId(ticketId);
        basa.prepare();
        basa.checkStatus();
        return basa;
    }


    private BulkUploadController setupBasicBulkUploadTest( String successAsync) throws Exception {
        File testImagesDirectory = TestConstants.getFile(TestConstants.TEST_IMAGE_DIR);
        assertTrue(testImagesDirectory.isDirectory());
        List<File> uploadFiles = new ArrayList<File>();
        uploadFiles.addAll(FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, false));
        return setupBasicBulkUploadTest(successAsync, uploadFiles);
    }

    @SuppressWarnings({ "unused" })
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
        Document document = resourceService.createResourceFrom(getAdminUser(),dataset, Document.class, true);
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
        Image image1 = resourceService.createResourceFrom(getAdminUser(),expected, Image.class, true);
        assertEquals(expected.isInheritingCulturalInformation(), image1.isInheritingCulturalInformation());

        expected.setInheritingInvestigationInformation(true);
        Image image2 = resourceService.createResourceFrom(getAdminUser(),expected, Image.class, true);
        assertEquals(expected.isInheritingInvestigationInformation(), image2.isInheritingInvestigationInformation());

        expected.setInheritingMaterialInformation(true);
        Image image3 = resourceService.createResourceFrom(getAdminUser(),expected, Image.class, true);
        assertEquals(expected.isInheritingMaterialInformation(), image3.isInheritingMaterialInformation());

        expected.setInheritingOtherInformation(true);
        Image image4 = resourceService.createResourceFrom(getAdminUser(),expected, Image.class, true);
        assertEquals(expected.isInheritingOtherInformation(), image4.isInheritingOtherInformation());

        expected.setInheritingSiteInformation(true);
        Image image5 = resourceService.createResourceFrom(getAdminUser(),expected, Image.class, true);
        assertEquals(expected.isInheritingSiteInformation(), image5.isInheritingSiteInformation());

        expected.setInheritingSpatialInformation(true);
        Image image6 = resourceService.createResourceFrom(getAdminUser(),expected, Image.class, true);
        assertEquals(expected.isInheritingSpatialInformation(), image6.isInheritingSpatialInformation());

        expected.setInheritingTemporalInformation(true);
        Image image8 = resourceService.createResourceFrom(getAdminUser(),expected, Image.class, true);
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

        Image image = resourceService.createResourceFrom(getAdminUser(),expected, Image.class, true);
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

        Image image = resourceService.createResourceFrom(getAdminUser(),expected, Image.class, true);
        assertEquals(expected.getSourceCollections().iterator().next().getText(),
                image.getSourceCollections().iterator().next().getText());
    }

    @Test
    @Rollback
    public void testCloneImageWithNoLicenceEnabled() {
        Image expected = new Image();
        Image image = resourceService.createResourceFrom(getAdminUser(),expected, expected.getClass(), true);
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
        expected.markUpdated(getAdminUser());
        Image image = resourceService.createResourceFrom(getAdminUser(),expected, expected.getClass(), true);
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
        Image image = resourceService.createResourceFrom(getAdminUser(),expected, expected.getClass(), true);
        assertTrue(LicenseType.OTHER.equals(image.getLicenseType()));
        assertTrue(OTHER_LICENCE_TYPE_BOILERPLATE.equals(image.getLicenseText()));
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.FAIMS })
    public void testCloneImageWithCopyrightEnabled() {
        Image expected = new Image();
        Person copyrightHolder = entityService.find(getAdminUserId());
        expected.setCopyrightHolder(copyrightHolder);
        Image image = resourceService.createResourceFrom(getAdminUser(),expected, expected.getClass(), true);
        assertTrue(copyrightHolder.equals(image.getCopyrightHolder()));
    }

    @Test
    @Rollback
    public void testCloneImageWithNoCopyrightEnabled() {
        Image expected = new Image();
        Image image = resourceService.createResourceFrom(getAdminUser(),expected, expected.getClass(), true);
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
        int origSharedCount = getCollectionCount(CollectionType.SHARED, SharedCollection.class);
        int origImageCount = genericService.findAll(Image.class).size();

        BulkUploadController bulkUploadController = generateNewInitializedController(BulkUploadController.class);
        bulkUploadController.prepare();

        // setup images to upload
        File testImagesDirectory = TestConstants.getFile(TestConstants.TEST_IMAGE_DIR);
        assertTrue(testImagesDirectory.isDirectory());
        List<File> uploadFiles = new ArrayList<File>();
        uploadFiles.addAll(FileUtils.listFiles(testImagesDirectory, new String[] { "jpg" }, false));
        assertEquals("sanity check: we just added two files, right?", 2, uploadFiles.size());

        Pair<PersonalFilestoreTicket, List<FileProxy>> proxyPair = uploadFilesAsync(uploadFiles);
        final Long ticketId = proxyPair.getFirst().getId();
        bulkUploadController.setTicketId(ticketId);
        bulkUploadController.setProjectId(TestConstants.ADMIN_INDEPENDENT_PROJECT_ID);

        // setup controller
        bulkUploadController.setFileProxies(proxyPair.getSecond());
        bulkUploadController.setAsync(false);
        bulkUploadController.getResource().setTitle("test");
        bulkUploadController.getResource().setDescription("test");
        bulkUploadController.getResource().setDate(1234);

        // specify an adhoc collection
        SharedCollection adHocCollection = new SharedCollection();
        // NEED TO SET THE TYPE OF THE ADHOC COLLECTION
        adHocCollection.setName("collection of bulk-uploaded resource collections");
        bulkUploadController.getShares().add(adHocCollection);

        // saving
        bulkUploadController.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.SUCCESS_ASYNC, bulkUploadController.save());
        BulkUpdateStatusAction basa = checkStatus(bulkUploadController.getTicketId());
        basa.checkStatus();
        assertEquals(new Float(100), basa.getPercentDone());

        // int newInternalCount = getCollectionCount(CollectionType.INTERNAL);
        int newSharedCount = getCollectionCount(CollectionType.SHARED, SharedCollection.class);
        int newImageCount = genericService.findAll(Image.class).size();
        Assert.assertNotSame(origImageCount, newImageCount);
        assertTrue((newImageCount - origImageCount) > 0);
        // ensure one shared collection created
        // evictCache();

        List<Pair<Long, String>> details = basa.getDetails();
        logger.info("{}", details);
        Set<RightsBasedResourceCollection> collections = new HashSet<>();
        evictCache();
        logger.debug("inspecting collections created:");
        for (Pair<Long, String> detail : details) {
            Resource resource = resourceService.find(detail.getFirst());
            genericService.refresh(resource);
            Set<RightsBasedResourceCollection> resourceCollections = resource.getRightsBasedResourceCollections();
            logger.debug("\t resource:{}\t  resourceCollections:{}", resource.getTitle(), resourceCollections.size());
            for (RightsBasedResourceCollection rc : resourceCollections) {
                logger.debug("\t\t {}", rc);
            }

            collections.addAll(resourceCollections);
        }
        assertEquals("we should have a total of 3 collections (0 internal +2 shared)", 2, collections.size());
//        int internalCount = 0;
        for (RightsBasedResourceCollection col : collections) {
            logger.debug("{} : {}", col, col.getResources());
//            if (col instanceof InternalCollection) {
//                assertEquals(1, col.getResources().size());
//                internalCount++;
//            } else {
                assertEquals(2, col.getResources().size());
//            }
        }
        assertEquals("we should have one new adhoc collection", 2, newSharedCount - origSharedCount);
        // ensure N internal collections created
         String msg = String.format("We should have %s new internal collections.  newcount:%s oldcount:%s", uploadFiles.size(),
          0, 0);
        // assertEquals(msg, uploadFiles.size(), newInternalCount - origInternalCount );
    }

    private <C extends HierarchicalCollection> int getCollectionCount(CollectionType type, Class<C> cls) {
        List<C> col = resourceCollectionDao.findCollectionsOfParent(null, null, cls);
        return col.size();
    }

}
