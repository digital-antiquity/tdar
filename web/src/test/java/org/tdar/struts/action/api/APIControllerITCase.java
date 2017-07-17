/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.sensory.SensoryDataImage;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.api.ingest.APIController;
import org.tdar.utils.TestConfiguration;

import com.opensymphony.xwork2.Action;

/**
 * @author Adam Brin
 * 
 */
@RunWith(MultipleTdarConfigurationRunner.class)
public class APIControllerITCase extends AbstractAdminControllerITCase {

    @Autowired
    ResourceService resourceService;

    @Autowired
    SerializationService serializationService;

    @Autowired
    GenericKeywordService genericKeywordService;

    @Autowired
    private DatasetDao datasetDao;

    @Autowired
    BillingAccountService billingAccountService;
    public final static Long TEST_ID = 3794L;

    TestConfiguration config = TestConfiguration.getInstance();
    int defaultMaxResults = config.getMaxAPIFindAll();

    @Test
    public void testSerialization() throws Exception {
        FileProxy proxy = new FileProxy();
        String convertToXML = serializationService.convertToXML(proxy);
        logger.debug(convertToXML);
        SharedCollection collection = new SharedCollection();
        collection.setHidden(true);
        String convertToXML4 = serializationService.convertToXML(collection);
        logger.debug(convertToXML4);
        Image img = new Image();
        img.setDescription("absasda sd");
        img.setTitle("this is my title");
        img.getFileProxies().add(new FileProxy());
        String convertToXML2 = serializationService.convertToXML(img);
        logger.debug(convertToXML2);
        Project proj = new Project();
        proj.setDescription("absasda sd");
        proj.setTitle("this is my title");
        String convertToXML3 = serializationService.convertToXML(proj);
        logger.debug(convertToXML3);
    }

    @Test
    public void testAPIController() throws Exception {
        Document fake = resourceService.find(TEST_ID);
        setupFakeRecord(fake);
        String docXml = serializationService.convertToXML(fake);
        logger.info(docXml);
        resourceService.updateTransientAccessCount(fake);
        final Long viewCount = fake.getTransientAccessCount();
        final Date creationDate = fake.getDateCreated();

        // revert back
        Document old = resourceService.find(TEST_ID);
        genericService.markReadOnly(old);
        old.getRelatedComparativeCollections().add(new RelatedComparativeCollection("text"));
        // final String oldDocXml = serializationService.convertToXML(old);
        assertEquals(2, old.getAuthorizedUsers().size());
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
        logger.debug(controller.getErrorMessage());
        assertTrue(controller.getErrorMessage().contains("updated"));
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.UPDATED, controller.getStatus());

        Document importedRecord = resourceService.find(TEST_ID);
        assertImportedRecordMatches(viewCount, creationDate, oldSubmitter, investigationTypesSize, resourceNotesSize, oldId, importedRecord);

    }

    private void assertImportedRecordMatches(final Long viewCount, final Date creationDate, final Person oldSubmitter, final int investigationTypesSize,
            final int resourceNotesSize, final Long oldId, Document importedRecord) {
        assertNotNull(importedRecord);
        assertEquals(1, investigationTypesSize);
        assertEquals(investigationTypesSize, importedRecord.getInvestigationTypes().size());
        assertEquals(0, importedRecord.getRelatedComparativeCollections().size());
        assertEquals(1, importedRecord.getCoverageDates().size());
        assertEquals(resourceNotesSize, importedRecord.getResourceNotes().size());
        assertEquals(oldId, importedRecord.getId());

        assertEquals(2, importedRecord.getAuthorizedUsers().size());
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
        genericService.delete(importedRecord);
        for (SharedCollection rc : importedRecord.getSharedResourceCollections()) {
            logger.debug("{} - {}", rc.getName(), rc.isHidden());
            if (rc instanceof VisibleCollection) {
                if (rc.getName().equals("hidden")) {
                    assertTrue(rc.isHidden());
                } else {
                    assertFalse(rc.isHidden());
                }
            } else {
                assertTrue(rc.isHidden());
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void setupFakeRecord(Document fake) {
        addAuthorizedUser(fake, getUser(), GeneralPermissions.MODIFY_RECORD);
        // setup a fake record, with some new fields off the session
        genericService.saveOrUpdate(fake);
        genericService.synchronize();

        genericService.markReadOnly(fake);
        fake.setDescription(fake.getTitle());
        fake.getInvestigationTypes().clear();
        fake.getInvestigationTypes().add(genericService.find(InvestigationType.class, 1L));
        fake.getResourceNotes().add(new ResourceNote(ResourceNoteType.GENERAL, "test"));
        fake.setInheritingCollectionInformation(false);
        fake.setInheritingCulturalInformation(true);
        fake.setInheritingNoteInformation(true);
        fake.setInheritingMaterialInformation(true);
        SharedCollection coll = new SharedCollection();
        coll.setHidden(true);
        coll.setName("hidden");
        coll.markUpdated(getAdminUser());
        fake.getSharedCollections().add(coll);
        SharedCollection coll2 = new SharedCollection();
        coll2.setHidden(false);
        coll2.setName("visible");
        coll2.markUpdated(getAdminUser());
        fake.getSharedCollections().add(coll2);

        fake.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 0, 1000));
        // fake.getResourceCollections().clear();
        removeInvalidFields(fake);
    }

    @Test
    @Rollback
    public void testNewRecord() throws Exception {
        Document doc = genericService.findAll(Document.class, 1).get(0);
        genericService.markReadOnly(doc);
        doc.setId(null);
        doc.getInformationResourceFiles().clear();
        doc.setMappedDataKeyColumn(null);
        removeInvalidFields(doc);
        String docXml = serializationService.convertToXML(doc);
        logger.info(docXml);
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED, controller.getStatus());
    }

    public static void removeInvalidFields(Resource doc) {
        if (doc instanceof Dataset) {
            ((Dataset) doc).getDataTables().clear();
        }
        if (doc instanceof CodingSheet) {
            ((CodingSheet) doc).getCodingRules().clear();
            ((CodingSheet) doc).getAssociatedDataTableColumns().clear();
            ((CodingSheet) doc).setDefaultOntology(null);
        }
        if (doc instanceof Ontology) {
            ((Ontology) doc).getOntologyNodes().clear();
        }
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR, RunWithTdarConfiguration.FAIMS, RunWithTdarConfiguration.CREDIT_CARD })
    public void testNewConfidentialRecord() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        String text = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/confidentialImage.xml"));
        Project project = genericService.findAll(Project.class, 1).get(0);
        BillingAccount account = setupAccountWithInvoiceTenOfEach(billingAccountService.getLatestActivityModel(), getUser());
        Long actId = account.getId();
        Long totalNumberOfFiles = account.getFilesUsed();
        logger.debug("files: {}", totalNumberOfFiles);
        Long totalSpaceInMb = account.getSpaceUsedInMb();
        logger.debug("mb: {}", totalSpaceInMb);
        controller.setRecord(text);
        logger.info(text);
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_IMAGE)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_IMAGE_NAME));
        controller.setAccountId(actId);
        controller.setProjectId(project.getId());
        account = null;
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED, controller.getStatus());
        Image img = genericService.find(Image.class, controller.getId());
        assertFalse(img.getFilesWithRestrictions(true).isEmpty());
        account = genericService.find(BillingAccount.class, actId);
        logger.debug("files: {}", account.getFilesUsed());
        logger.debug("mb: {}", account.getSpaceUsedInMb());
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            assertNotEquals(totalNumberOfFiles, account.getFilesUsed());
            assertNotEquals(totalSpaceInMb, account.getSpaceUsedInMb());
        }
    }

    @Test
    @Rollback
    public void testMimbres() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        String text = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/mimbres.xml"));
        controller.setRecord(text);
        String uploadStatus = controller.upload();
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED, controller.getStatus());
    }

    @Test
    @Rollback
    public void testLoadHiddenCollection() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        String text = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/hidden-collection.xml"));
        controller.setRecord(text);
        String uploadStatus = controller.upload();
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED, controller.getStatus());
    }

    @Test
    @Rollback
    public void testDataset() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        String text = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/dataset.xml"));
        controller.setRecord(text);
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR, "Workbook1.csv")));
        controller.setUploadFileFileName(Arrays.asList("Workbook1.csv"));
        String uploadStatus = controller.upload();
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED, controller.getStatus());
    }

    @Test
    @Rollback
    // @Ignore("not implemented yet")
    public void testDatasetWithMappings() throws Exception {

        APIController controller = generateNewInitializedController(APIController.class);
        String text = FileUtils.readFileToString(new File(TestConstants.TEST_ROOT_DIR + "/xml/datasetmapping.xml"));
        controller.setRecord(text);
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR, "Workbook1.csv")));
        controller.setUploadFileFileName(Arrays.asList("Workbook1.csv"));
        String uploadStatus = controller.upload();
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED, controller.getStatus());
        ((Dataset) controller.getImportedRecord()).getDataTables().forEach(dt -> assertTrue(datasetDao.checkExists(dt)));
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
        removeInvalidFields(data);
        String docXml = serializationService.convertToXML(data);
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED, controller.getStatus());
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testReplaceRecord() throws Exception {
        Document old = generateDocumentWithFileAndUser();
        TdarUser user = (TdarUser) old.getSubmitter();
        Long oldIRId = old.getFirstInformationResourceFile().getId();
        Long oldId = old.getId();
        String originalXml = serializationService.convertToXML(old);
        genericService.detachFromSession(old);
        old = null;
        String docXml = findADocumentToReplace(oldId);
        APIController controller = generateNewInitializedController(APIController.class, user);
        genericService.detachFromSession(user);
        genericService.synchronize();
        flush();
        evictCache();
        logger.debug("ORIGINAL: {}", originalXml);
        logger.debug("INCOMING: {}", docXml);

        controller.setRecord(docXml);
        String uploadStatus = controller.upload();

        logger.info(controller.getErrorMessage());
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.UPDATED, controller.getStatus());
        controller = null;
        old = (Document) resourceService.find(oldId);
        assertEquals(oldIRId, old.getFirstInformationResourceFile().getId());
    }

    private String findADocumentToReplace(Long oldId) throws Exception {
        Document document = genericService.findAll(Document.class, 1).get(0);
        genericService.markReadOnly(document);
        document.setId(oldId);
        String docXml = serializationService.convertToXML(document);
        genericService.detachFromSession(document);
        return docXml;
    }

    @Test
    @Rollback(true)
    public void testInvalidFileType() throws Exception {
        setIgnoreActionErrors(true);
        APIController controller = generateNewInitializedController(APIController.class);
        Dataset doc = findAResource(Dataset.class);
        removeInvalidFields(doc);

        String datasetXml = serializationService.convertToXML(doc);
        controller.setRecord(datasetXml);
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_IMAGE)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_IMAGE_NAME));
        String uploadStatus = controller.upload();
        assertEquals(Action.ERROR, uploadStatus);
        assertEquals(String.format("Expected Forbidden for %s, but was %s >> %s", doc.getId(), controller.getStatus(), datasetXml),
                StatusCode.FORBIDDEN, controller.getStatus());
    }

    @Test
    @Rollback(true)
    public void testInvalidUser() throws Exception {
        Document doc = generateDocumentWithUser();
        removeInvalidFields(doc);
        String docXml = serializationService.convertToXML(doc);

        APIController controller = generateNewController(APIController.class);
        init(controller, getBasicUser());
        controller.setRecord(docXml);

        String uploadStatus = controller.upload();
        assertEquals(Action.ERROR, uploadStatus);
        assertEquals(String.format("Expected UNAUTHORIZED for %s, but was %s >> %s", doc.getId(), controller.getStatus(), docXml),
                StatusCode.UNAUTHORIZED, controller.getStatus());
    }

    @Test
    @Rollback(true)
    public void testInvalidInvestigationType() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        Resource doc = findAResource(Document.class);
        Long docid = doc.getId();
        genericService.markReadOnly(doc);
        InvestigationType bad = new InvestigationType();
        bad.setLabel("INVAID");
        doc.getInvestigationTypes().add(bad);
        removeInvalidFields(doc);
        String docXml = serializationService.convertToXML(doc);
        doc = null;
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        assertEquals(Action.ERROR, uploadStatus);
        assertEquals(String.format("Expected Forbidden for %s, but was %s >> $s", docid, controller.getStatus(), docXml), StatusCode.FORBIDDEN,
                controller.getStatus());
    }

    private <C> C findAResource(Class<C> cls) {
        for (C c : genericService.findAll(cls, defaultMaxResults)) {
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    public void testBadEnum() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        Document doc = findAResource(Document.class);
        Long docid = doc.getId();
        genericService.markReadOnly(doc);
        doc.setResourceLanguage(Language.ENGLISH);
        removeInvalidFields(doc);
        String docXml = serializationService.convertToXML(doc);
        doc = null;
        docXml = StringUtils.replace(docXml, Language.ENGLISH.name(), "FNGLISH");
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        assertEquals(Action.ERROR, uploadStatus);
        assertEquals(StatusCode.BAD_REQUEST, controller.getStatus());
    }

}
