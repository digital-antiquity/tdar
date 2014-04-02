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
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.sensory.SensoryDataImage;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
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
    XmlService xmlService;

    @Autowired
    GenericKeywordService genericKeywordService;

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    public final static Long TEST_ID = 3794L;

    TestConfiguration config = TestConfiguration.getInstance();
    int defaultMaxResults = config.getMaxAPIFindAll();

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
        removeInvalidFields(fake);
        String docXml = xmlService.convertToXML(fake);
        logger.info(docXml);

        // revert back
        Document old = resourceService.find(TEST_ID);
        genericService.markReadOnly(old);
        old.getRelatedComparativeCollections().add(new RelatedComparativeCollection("text"));
        // final String oldDocXml = xmlService.convertToXML(old);
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
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        controller.setRecord(docXml);
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_DOCUMENT)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_DOCUMENT_NAME));
        String uploadStatus = controller.upload();
        assertTrue(controller.getErrorMessage().contains("updated"));
        assertEquals(Action.SUCCESS, uploadStatus);
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

    // // return some public resource collections
    // private List<ResourceCollection> getSomeResourceCollections() throws InstantiationException, IllegalAccessException {
    // int count = 5;
    // List<ResourceCollection> resourceCollections = new ArrayList<ResourceCollection>();
    // for (int i = 0; i < count; i++) {
    // String email = "someperson" + i + "@mailinator.com";
    // Person person = entityService.findByEmail(email);
    // if (person == null) {
    // person = createAndSaveNewPerson(email, "someperson");
    // }
    // Document document = createAndSaveNewInformationResource(Document.class, person);
    // ResourceCollection rc = new ResourceCollection(document, getAdminUser());
    // rc.setName("test collection " + i);
    // rc.setSortBy(SortOption.TITLE);
    // resourceCollectionService.saveOrUpdate(rc);
    // resourceCollections.add(rc);
    // }
    // return resourceCollections;
    // }

    @Test
    @Rollback
    public void testNewRecord() throws Exception {
        Document doc = genericService.findAll(Document.class, 1).get(0);
        genericService.markReadOnly(doc);
        doc.setId(null);
        doc.getInformationResourceFiles().clear();
        doc.setMappedDataKeyColumn(null);
        doc.getBookmarks().clear();
        removeInvalidFields(doc);
        String docXml = xmlService.convertToXML(doc);
        logger.info(docXml);
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED.getResultName(), controller.getStatus());
    }

    private void removeInvalidFields(Resource doc) {
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
        if (doc instanceof Project) {
            ((Project) doc).getCachedInformationResources().clear();
        }
        if (doc instanceof InformationResource) {
            ((InformationResource) doc).getRelatedDatasetData().clear();
        }
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR, RunWithTdarConfiguration.FAIMS })
    public void testNewConfidentialRecord() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        String text = "<?xml version=\"1.0\" encoding=\"utf-8\"?><tdar:image xmlns:tdar=\"http://www.tdar.org/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://localhost:8180/schema/current schema.xsd\"><tdar:description>This Bowl is an example of Style III from the Swarts site.  Swarts ruin (sometimes known as Swartz Ruin) is a Mimbres village in Grants County, southwestern New Mexico, excavated during the 1920s by H.S. and C.B. Cosgrove.  The site dates from about A.D. 950 to 1175 and contained the relatively undisturbed remains of numerous pit houses and several Classic Mimbres roomblocks, as well as a large assemblage of ceramics, lithics, and faunal material.  Sometime after the excavations, the site was leveled. Artifacts, photographs and field notes from the Cosgrove excavations are curated in the Peabody Museum of Archaeology and Ethnology at Harvard University. Swarts is described as an example Mimbres site in Brody's books on Mimbres pottery (1977, 2002 http://library.lib.asu.edu/record=b4770839~S3). A comprehensive report on the site (Cosgrove and Cosgrove 1932) has recently been reprinted (http://library.lib.asu.edu/record=b4816690~S3).</tdar:description><tdar:latitudeLongitudeBoxes><tdar:latitudeLongitudeBox okayToShowExactLocation=\"false\"> <tdar:maximumLatitude>32.69975751</tdar:maximumLatitude><tdar:maximumLongitude>-107.8423258</tdar:maximumLongitude><tdar:minimumLatitude>32.69475751</tdar:minimumLatitude><tdar:minimumLongitude>-107.8473258</tdar:minimumLongitude></tdar:latitudeLongitudeBox></tdar:latitudeLongitudeBoxes><tdar:resourceType>IMAGE</tdar:resourceType><tdar:siteNameKeywords><tdar:siteNameKeyword><tdar:label>Swarts</tdar:label></tdar:siteNameKeyword></tdar:siteNameKeywords><tdar:title>Swarts Bowl (Style III)</tdar:title><tdar:date>2012</tdar:date><tdar:dateNormalized>2012</tdar:dateNormalized><tdar:externalReference>false</tdar:externalReference><tdar:inheritingCollectionInformation>true</tdar:inheritingCollectionInformation><tdar:inheritingCulturalInformation>true</tdar:inheritingCulturalInformation><tdar:inheritingIdentifierInformation>true</tdar:inheritingIdentifierInformation><tdar:inheritingIndividualAndInstitutionalCredit>true</tdar:inheritingIndividualAndInstitutionalCredit><tdar:inheritingInvestigationInformation>true</tdar:inheritingInvestigationInformation><tdar:inheritingMaterialInformation>true</tdar:inheritingMaterialInformation><tdar:inheritingNoteInformation>true</tdar:inheritingNoteInformation><tdar:inheritingOtherInformation>true</tdar:inheritingOtherInformation><tdar:inheritingSiteInformation>false</tdar:inheritingSiteInformation><tdar:inheritingSpatialInformation>false</tdar:inheritingSpatialInformation><tdar:inheritingTemporalInformation>true</tdar:inheritingTemporalInformation><tdar:relatedDatasetData/><tdar:resourceLanguage>ENGLISH</tdar:resourceLanguage><tdar:resourceProviderInstitution/></tdar:image>";
        Project project = genericService.findAll(Project.class, 1).get(0);
        Account account = setupAccountForPerson(getUser());
        controller.setRecord(text);
        logger.info(text);
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_IMAGE)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_IMAGE_NAME));
        controller.setAccountId(account.getId());
        controller.setProjectId(project.getId());
        controller.setRestrictedFiles(Arrays.asList(TestConstants.TEST_IMAGE_NAME));
        controller.setFileAccessRestriction(FileAccessRestriction.CONFIDENTIAL);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED.getResultName(), controller.getStatus());
        Image img = genericService.find(Image.class, controller.getId());
        assertFalse(img.getFilesWithRestrictions(true).isEmpty());
    }

    @Test
    public void testMimbres() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        String text = "<?xml version=\"1.0\" encoding=\"utf-8\"?><tdar:image xmlns:tdar=\"http://www.tdar.org/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://localhost:8180/schema/current schema.xsd\"><tdar:description>This Bowl is an example of Style III from the Swarts site.  Swarts ruin (sometimes known as Swartz Ruin) is a Mimbres village in Grants County, southwestern New Mexico, excavated during the 1920s by H.S. and C.B. Cosgrove.  The site dates from about A.D. 950 to 1175 and contained the relatively undisturbed remains of numerous pit houses and several Classic Mimbres roomblocks, as well as a large assemblage of ceramics, lithics, and faunal material.  Sometime after the excavations, the site was leveled. Artifacts, photographs and field notes from the Cosgrove excavations are curated in the Peabody Museum of Archaeology and Ethnology at Harvard University. Swarts is described as an example Mimbres site in Brody's books on Mimbres pottery (1977, 2002 http://library.lib.asu.edu/record=b4770839~S3). A comprehensive report on the site (Cosgrove and Cosgrove 1932) has recently been reprinted (http://library.lib.asu.edu/record=b4816690~S3).</tdar:description><tdar:latitudeLongitudeBoxes><tdar:latitudeLongitudeBox okayToShowExactLocation=\"false\"><tdar:maximumLatitude>32.69975751</tdar:maximumLatitude><tdar:maximumLongitude>-107.8423258</tdar:maximumLongitude><tdar:minimumLatitude>32.69475751</tdar:minimumLatitude><tdar:minimumLongitude>-107.8473258</tdar:minimumLongitude></tdar:latitudeLongitudeBox></tdar:latitudeLongitudeBoxes><tdar:resourceType>IMAGE</tdar:resourceType><tdar:siteNameKeywords><tdar:siteNameKeyword><tdar:label>Swarts</tdar:label></tdar:siteNameKeyword></tdar:siteNameKeywords><tdar:title>Swarts Bowl (Style III)</tdar:title><tdar:date>2012</tdar:date><tdar:dateNormalized>2012</tdar:dateNormalized><tdar:externalReference>false</tdar:externalReference><tdar:inheritingCollectionInformation>true</tdar:inheritingCollectionInformation><tdar:inheritingCulturalInformation>true</tdar:inheritingCulturalInformation><tdar:inheritingIdentifierInformation>true</tdar:inheritingIdentifierInformation><tdar:inheritingIndividualAndInstitutionalCredit>true</tdar:inheritingIndividualAndInstitutionalCredit><tdar:inheritingInvestigationInformation>true</tdar:inheritingInvestigationInformation><tdar:inheritingMaterialInformation>true</tdar:inheritingMaterialInformation><tdar:inheritingNoteInformation>true</tdar:inheritingNoteInformation><tdar:inheritingOtherInformation>true</tdar:inheritingOtherInformation><tdar:inheritingSiteInformation>false</tdar:inheritingSiteInformation><tdar:inheritingSpatialInformation>false</tdar:inheritingSpatialInformation><tdar:inheritingTemporalInformation>true</tdar:inheritingTemporalInformation><tdar:relatedDatasetData/><tdar:resourceLanguage>ENGLISH</tdar:resourceLanguage><tdar:resourceProviderInstitution/></tdar:image>";
        controller.setRecord(text);
        String uploadStatus = controller.upload();
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED.getResultName(), controller.getStatus());
    }

    @Test
    @Rollback
    public void testDataset() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        String text = "<?xml version=\"1.0\" encoding=\"utf-8\"?><tdar:dataset xmlns:tdar=\"http://www.tdar.org/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://localhost:8180/schema/current schema.xsd\"><tdar:description>This Bowl is an example of Style III from the Swarts site.  Swarts ruin (sometimes known as Swartz Ruin) is a Mimbres village in Grants County, southwestern New Mexico, excavated during the 1920s by H.S. and C.B. Cosgrove.  The site dates from about A.D. 950 to 1175 and contained the relatively undisturbed remains of numerous pit houses and several Classic Mimbres roomblocks, as well as a large assemblage of ceramics, lithics, and faunal material.  Sometime after the excavations, the site was leveled. Artifacts, photographs and field notes from the Cosgrove excavations are curated in the Peabody Museum of Archaeology and Ethnology at Harvard University. Swarts is described as an example Mimbres site in Brody's books on Mimbres pottery (1977, 2002 http://library.lib.asu.edu/record=b4770839~S3). A comprehensive report on the site (Cosgrove and Cosgrove 1932) has recently been reprinted (http://library.lib.asu.edu/record=b4816690~S3).</tdar:description><tdar:latitudeLongitudeBoxes><tdar:latitudeLongitudeBox okayToShowExactLocation=\"false\"><tdar:maximumLatitude>32.69975751</tdar:maximumLatitude><tdar:maximumLongitude>-107.8423258</tdar:maximumLongitude><tdar:minimumLatitude>32.69475751</tdar:minimumLatitude><tdar:minimumLongitude>-107.8473258</tdar:minimumLongitude></tdar:latitudeLongitudeBox></tdar:latitudeLongitudeBoxes><tdar:resourceType>DATASET</tdar:resourceType><tdar:siteNameKeywords><tdar:siteNameKeyword><tdar:label>Swarts</tdar:label></tdar:siteNameKeyword></tdar:siteNameKeywords><tdar:title>Swarts Bowl (Style III)</tdar:title><tdar:date>2012</tdar:date><tdar:dateNormalized>2012</tdar:dateNormalized><tdar:externalReference>false</tdar:externalReference><tdar:inheritingCollectionInformation>true</tdar:inheritingCollectionInformation><tdar:inheritingCulturalInformation>true</tdar:inheritingCulturalInformation><tdar:inheritingIdentifierInformation>true</tdar:inheritingIdentifierInformation><tdar:inheritingIndividualAndInstitutionalCredit>true</tdar:inheritingIndividualAndInstitutionalCredit><tdar:inheritingInvestigationInformation>true</tdar:inheritingInvestigationInformation><tdar:inheritingMaterialInformation>true</tdar:inheritingMaterialInformation><tdar:inheritingNoteInformation>true</tdar:inheritingNoteInformation><tdar:inheritingOtherInformation>true</tdar:inheritingOtherInformation><tdar:inheritingSiteInformation>false</tdar:inheritingSiteInformation><tdar:inheritingSpatialInformation>false</tdar:inheritingSpatialInformation><tdar:inheritingTemporalInformation>true</tdar:inheritingTemporalInformation><tdar:relatedDatasetData/><tdar:resourceLanguage>ENGLISH</tdar:resourceLanguage><tdar:resourceProviderInstitution/></tdar:dataset>";
        controller.setRecord(text);
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR, "Workbook1.csv")));
        controller.setUploadFileFileName(Arrays.asList("Workbook1.csv"));
        String uploadStatus = controller.upload();
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED.getResultName(), controller.getStatus());
    }

    @Test
    @Rollback
    @Ignore("not implemented yet")
    public void testDatasetWithMappings() throws Exception {

        APIController controller = generateNewInitializedController(APIController.class);
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        String text = "<?xml version=\"1.0\" encoding=\"utf-8\"?><tdar:dataset xmlns:tdar=\"http://www.tdar.org/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://localhost:8180/schema/current schema.xsd\"><tdar:description>This Bowl is an example of Style III from the Swarts site.  Swarts ruin (sometimes known as Swartz Ruin) is a Mimbres village in Grants County, southwestern New Mexico, excavated during the 1920s by H.S. and C.B. Cosgrove.  The site dates from about A.D. 950 to 1175 and contained the relatively undisturbed remains of numerous pit houses and several Classic Mimbres roomblocks, as well as a large assemblage of ceramics, lithics, and faunal material.  Sometime after the excavations, the site was leveled. Artifacts, photographs and field notes from the Cosgrove excavations are curated in the Peabody Museum of Archaeology and Ethnology at Harvard University. Swarts is described as an example Mimbres site in Brody's books on Mimbres pottery (1977, 2002 http://library.lib.asu.edu/record=b4770839~S3). A comprehensive report on the site (Cosgrove and Cosgrove 1932) has recently been reprinted (http://library.lib.asu.edu/record=b4816690~S3).</tdar:description><tdar:latitudeLongitudeBoxes><tdar:latitudeLongitudeBox okayToShowExactLocation=\"false\"><tdar:maximumLatitude>32.69975751</tdar:maximumLatitude><tdar:maximumLongitude>-107.8423258</tdar:maximumLongitude><tdar:minimumLatitude>32.69475751</tdar:minimumLatitude><tdar:minimumLongitude>-107.8473258</tdar:minimumLongitude></tdar:latitudeLongitudeBox></tdar:latitudeLongitudeBoxes><tdar:resourceType>DATASET</tdar:resourceType><tdar:siteNameKeywords><tdar:siteNameKeyword><tdar:label>Swarts</tdar:label></tdar:siteNameKeyword></tdar:siteNameKeywords><tdar:title>Swarts Bowl (Style III)</tdar:title><tdar:date>2012</tdar:date><tdar:dateNormalized>2012</tdar:dateNormalized><tdar:externalReference>false</tdar:externalReference><tdar:inheritingCollectionInformation>true</tdar:inheritingCollectionInformation><tdar:inheritingCulturalInformation>true</tdar:inheritingCulturalInformation><tdar:inheritingIdentifierInformation>true</tdar:inheritingIdentifierInformation><tdar:inheritingInvestigationInformation>true</tdar:inheritingInvestigationInformation><tdar:inheritingMaterialInformation>true</tdar:inheritingMaterialInformation><tdar:inheritingNoteInformation>true</tdar:inheritingNoteInformation><tdar:inheritingOtherInformation>true</tdar:inheritingOtherInformation><tdar:inheritingSiteInformation>false</tdar:inheritingSiteInformation><tdar:inheritingSpatialInformation>false</tdar:inheritingSpatialInformation><tdar:inheritingTemporalInformation>true</tdar:inheritingTemporalInformation><tdar:relatedDatasetData/><tdar:resourceLanguage>ENGLISH</tdar:resourceLanguage><tdar:resourceProviderInstitution/><tdar:dataTables><tdar:dataTable id=\"-1\"><tdar:dataTableColumns><tdar:dataTableColumn id=\"-1\"><tdar:columnDataType>VARCHAR</tdar:columnDataType><tdar:columnEncodingType>CODED_VALUE</tdar:columnEncodingType><tdar:dataTableRef>DataTable:-1</tdar:dataTableRef><tdar:displayName>Column #1</tdar:displayName><tdar:ignoreFileExtension>true</tdar:ignoreFileExtension><tdar:length>-1</tdar:length><tdar:mappingColumn>false</tdar:mappingColumn><tdar:name>column_1</tdar:name><tdar:visible>true</tdar:visible></tdar:dataTableColumn></tdar:dataTableColumns><tdar:displayName>Table 1</tdar:displayName><tdar:name>table1</tdar:name></tdar:dataTable></tdar:dataTables></tdar:dataset>";
        controller.setRecord(text);
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_DATA_INTEGRATION_DIR, "Workbook1.csv")));
        controller.setUploadFileFileName(Arrays.asList("Workbook1.csv"));
        String uploadStatus = controller.upload();
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED.getResultName(), controller.getStatus());
    }

    @Test
    @Rollback
    public void testTrivialRecord() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        SensoryData data = new SensoryData();
        data.setTitle("Test");
        data.setDescription(" a description");
        data.setProject(Project.NULL);
        SensoryDataImage img = new SensoryDataImage();
        img.setDescription("d");
        img.setFilename("1234");
        data.getSensoryDataImages().add(img);
        removeInvalidFields(data);
        String docXml = xmlService.convertToXML(data);
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.CREATED.getResultName(), controller.getStatus());
    }

    @Test
    @Rollback
    public void testReplaceRecord() throws Exception {
        Document old = generateDocumentWithFileAndUser();
        Long oldIRId = old.getFirstInformationResourceFile().getId();
        Long oldId = old.getId();
        genericService.detachFromSession(old);
        genericService.detachFromSession(getAdminUser());
        old = null;
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        Document document = genericService.findAll(Document.class, 1).get(0);
        genericService.markReadOnly(document);
        document.setId(oldId);
        removeInvalidFields(document);
        String docXml = xmlService.convertToXML(document);
        genericService.detachFromSession(document);
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        logger.info(controller.getErrorMessage());
        assertEquals(Action.SUCCESS, uploadStatus);
        assertEquals(StatusCode.UPDATED.getResultName(), controller.getStatus());
        document = null;
        controller = null;
        old = (Document) resourceService.find(oldId);
        assertEquals(oldIRId, old.getFirstInformationResourceFile().getId());
    }

    @SuppressWarnings("null")
    @Test
    @Rollback(true)
    public void testInvalidFileType() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        Dataset doc = findAResource(Dataset.class);
        removeInvalidFields(doc);

        String datasetXml = xmlService.convertToXML(doc);
        controller.setRecord(datasetXml);
        controller.setUploadFile(Arrays.asList(new File(TestConstants.TEST_IMAGE)));
        controller.setUploadFileFileName(Arrays.asList(TestConstants.TEST_IMAGE_NAME));
        String uploadStatus = controller.upload();
        assertEquals(Action.ERROR, uploadStatus);
        assertEquals(String.format("Expected Forbidden for %s, but was %s >> %s", doc.getId(), controller.getStatus(), datasetXml),
                StatusCode.FORBIDDEN.getResultName(), controller.getStatus());
    }

    @Test
    @Rollback(true)
    public void testInvalidUser() throws Exception {
        Document doc = generateDocumentWithUser();
        removeInvalidFields(doc);
        String docXml = xmlService.convertToXML(doc);

        APIController controller = generateNewController(APIController.class);
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        init(controller, getBasicUser());
        controller.setRecord(docXml);

        String uploadStatus = controller.upload();
        assertEquals(Action.ERROR, uploadStatus);
        assertEquals(String.format("Expected UNAUTHORIZED for %s, but was %s >> %s", doc.getId(), controller.getStatus(), docXml),
                StatusCode.UNAUTHORIZED.getResultName(), controller.getStatus());
    }

    @Test
    @Rollback(true)
    public void testInvalidInvestigationType() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        Resource doc = findAResource(Document.class);
        @SuppressWarnings("null")
        Long docid = doc.getId();
        genericService.markReadOnly(doc);
        InvestigationType bad = new InvestigationType();
        bad.setLabel("INVAID");
        doc.getInvestigationTypes().add(bad);
        removeInvalidFields(doc);
        String docXml = xmlService.convertToXML(doc);
        doc = null;
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        assertEquals(Action.ERROR, uploadStatus);
        assertEquals(String.format("Expected Forbidden for %s, but was %s >> $s", docid, controller.getStatus(), docXml), StatusCode.FORBIDDEN.getResultName(),
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

    @Test
    @Rollback(true)
    public void testBadEnum() throws Exception {
        APIController controller = generateNewInitializedController(APIController.class);
        Document doc = findAResource(Document.class);
        Long docid = doc.getId();
        genericService.markReadOnly(doc);
        doc.setResourceLanguage(Language.ENGLISH);
        removeInvalidFields(doc);
        String docXml = xmlService.convertToXML(doc);
        doc = null;
        docXml = StringUtils.replace(docXml, Language.ENGLISH.name(), "FNGLISH");
        controller.setFileAccessRestriction(FileAccessRestriction.PUBLIC);
        controller.setRecord(docXml);
        String uploadStatus = controller.upload();
        assertEquals(Action.ERROR, uploadStatus);
        assertEquals(StatusCode.BAD_REQUEST.getResultName(), controller.getStatus());
    }

}
