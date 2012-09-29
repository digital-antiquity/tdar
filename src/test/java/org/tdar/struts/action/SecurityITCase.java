/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.FullUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ReadUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.service.EntityService;

/**
 * @author Adam Brin
 * 
 */
public class SecurityITCase extends AbstractControllerITCase {

    @Autowired
    EntityService entityService;

    @Test
    @Rollback
    public void testConfidential() throws InstantiationException, IllegalAccessException {
        Document doc = (Document) generateInformationResourceWithFile();
        doc.getInformationResourceFiles().iterator().next().setConfidential(true);
        entityService.save(doc);
        assertFalse(entityService.canViewConfidentialInformation(getUser(), doc));
    }

    @Test
    @Rollback
    public void testEmbargoed() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        assertFalse(entityService.canViewConfidentialInformation(getUser(), doc));
    }

    private Document setupEmbargoedDoc() throws InstantiationException, IllegalAccessException {
        Document doc = (Document) generateInformationResourceWithFile();
        doc.setAvailableToPublic(false);
        entityService.save(doc);
        return doc;
    }

    @Test
    @Rollback
    public void testCombination() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setConfidential(true);
        entityService.save(doc);
        assertFalse(entityService.canViewConfidentialInformation(getUser(), doc));
    }

    @Test
    @Rollback
    public void testReadUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupReadUserDoc();
        assertTrue(entityService.canViewConfidentialInformation(getUser(), doc));
    }

    @Test
    @Rollback
    public void testBadReadUser() throws InstantiationException, IllegalAccessException {
        logger.info("test bad read user");
        Document doc = setupBadReadUserDoc();
        assertFalse(entityService.canViewConfidentialInformation(getUser(), doc));
    }

    @Test
    @Rollback
    public void testBadFullUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupBadFullUserDoc();
        assertFalse(entityService.canViewConfidentialInformation(getUser(), doc));
    }

    private Document setupReadUserDoc() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setConfidential(true);
        addReadUser(doc, getUser());
        entityService.save(doc);
        return doc;
    }

    private Document setupBadReadUserDoc() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setConfidential(true);
        addReadUser(doc, getAdminUser());
        entityService.save(doc);
        return doc;
    }

    @Test
    @Rollback
    public void testFullUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupFullUserDoc();
        assertTrue(entityService.canViewConfidentialInformation(getUser(), doc));
    }

    private Document setupFullUserDoc() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setConfidential(true);
        addFullUser(doc, getUser());
        return doc;
    }

    private Document setupBadFullUserDoc() throws InstantiationException, IllegalAccessException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setConfidential(true);
        addFullUser(doc, getAdminUser());
        return doc;
    }

    public void addFullUser(Document doc, Person person) {
        FullUser fullUser = new FullUser();
        fullUser.setPerson(person);
        fullUser.setResource(doc);
        entityService.save(fullUser);
        doc.getFullUsers().add(fullUser);
        entityService.save(doc);
    }

    public void addReadUser(Document doc, Person person) {
        entityService.save(doc);
        ReadUser readUser = new ReadUser();
        readUser.setPerson(person);
        readUser.setResource(doc);
        entityService.save(readUser);
        doc.getReadUsers().add(readUser);
        entityService.save(doc);
    }
    
    /*FIXME: This section used the "accessible files" concept, which is not working well with file level granularity.
     *   Revisit and improve
     */

//    @Test
    @Rollback
    public void testAbstractInformationResourceControllerConfidential() throws InstantiationException, IllegalAccessException {
        logger.info("test confidential");
        Document doc = (Document) generateInformationResourceWithFile();
        doc.getInformationResourceFiles().iterator().next().setConfidential(true);
        entityService.save(doc);
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, doc.getId());
//        assertTrue(controller.getAccessibleFiles().isEmpty());
    }

//    @Test
    @Rollback
    public void testAbstractInformationResourceControllerEmbargoed() throws InstantiationException, IllegalAccessException {
        logger.info("test embargoed");
        Document doc = setupEmbargoedDoc();
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, doc.getId());
//        assertTrue(controller.getAccessibleFiles().isEmpty());
    }

//    @Test
    @Rollback
    public void testAbstractInformationResourceControllerEmbargoedAndConfidential() throws InstantiationException, IllegalAccessException {
        logger.info("test combined");
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setConfidential(true);
        entityService.save(doc);
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, doc.getId());
//        assertTrue(controller.getAccessibleFiles().isEmpty());
    }

//    @Test
    @Rollback
    public void testAbstractInformationResourceControllerReadUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupReadUserDoc();
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, doc.getId());
//        assertFalse(controller.getAccessibleFiles().isEmpty());
    }

//    @Test
    @Rollback
    public void testAbstractInformationResourceControllerFullUser() throws InstantiationException, IllegalAccessException {
        Document doc = setupFullUserDoc();
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, doc.getId());
//        assertFalse(controller.getAccessibleFiles().isEmpty());
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
        doc.getInformationResourceFiles().iterator().next().setConfidential(true);
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
        logger.info(currentVersion.getId());
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
