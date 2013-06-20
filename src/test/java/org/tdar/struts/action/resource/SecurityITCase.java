/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.resource;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.service.EntityService;
import org.tdar.struts.action.DownloadController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;

import static org.junit.Assert.*;

/**
 * @author Adam Brin
 * 
 */
public class SecurityITCase extends AbstractResourceControllerITCase {

    @Autowired
    EntityService entityService;

    @Test
    @Rollback
    public void testConfidential() throws InstantiationException, IllegalAccessException {
        Document doc = (Document) generateInformationResourceWithFile();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        entityService.save(doc);
        assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    
    @Test
    @Rollback
    public void testEmbargoed() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    private Document setupEmbargoedDoc() throws InstantiationException, IllegalAccessException {
        Document doc = (Document) generateInformationResourceWithFile();
        InformationResourceFile file = doc.getInformationResourceFiles().iterator().next();
        file.setRestriction(FileAccessRestriction.EMBARGOED);
        file.setDateMadePublic(new DateTime().plusYears(4).toDate());
        entityService.save(doc);
        return doc;
    }

    @Test
    @Rollback
    public void testCombination() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        entityService.save(doc);
        assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    @Test
    @Rollback
    public void testReadUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupReadUserDoc();
        assertTrue(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    @Test
    @Rollback
    public void testBadReadUser() throws InstantiationException, IllegalAccessException {
        logger.info("test bad read user");
        Document doc = setupBadReadUserDoc();
        assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    @Test
    @Rollback
    public void testBadFullUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupBadFullUserDoc();
        assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    private Document setupReadUserDoc() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        addAuthorizedUser(doc, getUser(), GeneralPermissions.VIEW_ALL);
        entityService.save(doc);
        return doc;
    }

    private Document setupBadReadUserDoc() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        addAuthorizedUser(doc, getAdminUser(), GeneralPermissions.VIEW_ALL);
        entityService.save(doc);
        return doc;
    }

    @Test
    @Rollback
    public void testFullUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupFullUserDoc();
        assertTrue(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    private Document setupFullUserDoc() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        addAuthorizedUser(doc, getUser(), GeneralPermissions.MODIFY_RECORD);
        return doc;
    }

    private Document setupBadFullUserDoc() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        addAuthorizedUser(doc, getAdminUser(), GeneralPermissions.MODIFY_RECORD);
        return doc;
    }

    /*
     * FIXME: This section used the "accessible files" concept, which is not working well with file level granularity.
     * Revisit and improve
     */

    // @Test
    @Rollback
    public void testAbstractInformationResourceControllerConfidential() throws InstantiationException, IllegalAccessException, TdarActionException {
        logger.info("test confidential");
        Document doc = (Document) generateInformationResourceWithFile();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        entityService.save(doc);
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, doc.getId());
        // assertTrue(controller.getAccessibleFiles().isEmpty());
    }

    // @Test
    @Rollback
    public void testAbstractInformationResourceControllerEmbargoed() throws InstantiationException, IllegalAccessException, TdarActionException {
        logger.info("test embargoed");
        Document doc = setupEmbargoedDoc();
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, doc.getId());
        // assertTrue(controller.getAccessibleFiles().isEmpty());
    }

    // @Test
    @Rollback
    public void testAbstractInformationResourceControllerEmbargoedAndConfidential() throws InstantiationException, IllegalAccessException, TdarActionException {
        logger.info("test combined");
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        entityService.save(doc);
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, doc.getId());
        // assertTrue(controller.getAccessibleFiles().isEmpty());
    }

    // @Test
    @Rollback
    public void testAbstractInformationResourceControllerReadUser() throws InstantiationException, IllegalAccessException, TdarActionException {
        Document doc = setupReadUserDoc();
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, doc.getId());
        // assertFalse(controller.getAccessibleFiles().isEmpty());
    }

    // @Test
    @Rollback
    public void testAbstractInformationResourceControllerFullUser() throws InstantiationException, IllegalAccessException, TdarActionException {
        Document doc = setupFullUserDoc();
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, doc.getId());
        // assertFalse(controller.getAccessibleFiles().isEmpty());
    }

    @Test
    @Rollback
    public void testDownloadControllerEmbargoed() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileId(doc.getInformationResourceFiles().iterator().next().getUploadedVersion(1).getId());
        assertEquals(DownloadController.FORBIDDEN, controller.execute());
    }

    @Test
    @Rollback
    public void testDownloadControllerConfidential() throws InstantiationException, IllegalAccessException {
        Document doc = (Document) generateInformationResourceWithFile();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileId(doc.getInformationResourceFiles().iterator().next().getUploadedVersion(1).getId());
        assertEquals(DownloadController.FORBIDDEN, controller.execute());
    }

    @Test
    @Rollback
    public void testDownloadControllerBadReadUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupBadReadUserDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileId(doc.getInformationResourceFiles().iterator().next().getLatestUploadedVersion().getId());
        assertEquals(DownloadController.FORBIDDEN, controller.execute());
    }

    @Test
    @Rollback
    public void testDownloadControllerBadFullUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupBadFullUserDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileId(doc.getInformationResourceFiles().iterator().next().getLatestUploadedVersion().getId());
        assertEquals(DownloadController.FORBIDDEN, controller.execute());
    }

    @Test
    @Rollback
    public void testDownloadControllerReadUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupReadUserDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileId(doc.getInformationResourceFiles().iterator().next().getLatestUploadedVersion().getId());
        assertEquals(DownloadController.SUCCESS, controller.execute());
    }

    @Test
    @Rollback
    public void testDownloadControllerFullUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupFullUserDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileId(doc.getInformationResourceFiles().iterator().next().getLatestUploadedVersion().getId());
        assertEquals(DownloadController.SUCCESS, controller.execute());
    }

    @Test
    @Rollback
    public void testThumbnailControllerInvalid() throws InstantiationException, IllegalAccessException {
        Document doc = setupBadFullUserDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        InformationResourceFileVersion currentVersion = doc.getInformationResourceFiles().iterator().next()
                .getCurrentVersion(VersionType.WEB_SMALL);
        logger.info("{}", currentVersion.getId());
        controller.setInformationResourceFileId(currentVersion.getId());
        assertEquals(DownloadController.FORBIDDEN, controller.thumbnail());
    }

    @Test
    @Rollback
    public void testThumbnailController() throws InstantiationException, IllegalAccessException {
        Document doc = setupFullUserDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileId(doc.getInformationResourceFiles().iterator().next()
                .getCurrentVersion(VersionType.WEB_SMALL).getId());
        assertEquals(DownloadController.SUCCESS, controller.thumbnail());
    }
    
    // FIXME: not sure where this belongs, it was in DatasetControllerITCase originally
    @Test
    @Rollback
    public void testAuthorizedUserInEquality() {
        //with the equals and hashCode of AuthorizedUser, this is now never going to be true
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), GeneralPermissions.VIEW_ALL);
        AuthorizedUser authorizedUser2 = new AuthorizedUser(getAdminUser(), GeneralPermissions.VIEW_ALL);
        assertNotEquals(authorizedUser, authorizedUser2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractControllerITCase#getController()
     */
    @Override
    protected TdarActionSupport getController() {
        // TODO Auto-generated method stub
        return new DocumentController();
    }

}
