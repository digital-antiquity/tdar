/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.service.EntityService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.download.DownloadController;
import org.tdar.struts.action.download.ThumbnailDownloadAction;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.Action;

/**
 * @author Adam Brin
 * 
 */
@SuppressWarnings("deprecation")
public class SecurityITCase extends AbstractControllerITCase {

    @Autowired
    EntityService entityService;

    @Test
    @Rollback
    public void testConfidential() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document doc = (Document) generateInformationResourceWithFile();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        genericService.save(doc);
        assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    @Test
    @Rollback
    public void testEmbargoed() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document doc = setupEmbargoedDoc();
        assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    private Document setupEmbargoedDoc() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document doc = (Document) generateInformationResourceWithFile();
        InformationResourceFile file = doc.getInformationResourceFiles().iterator().next();
        file.setRestriction(FileAccessRestriction.EMBARGOED_FIVE_YEARS);
        file.setDateMadePublic(new DateTime().plusYears(4).toDate());
        genericService.save(doc);
        return doc;
    }

    @Test
    @Rollback
    public void testCombination() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        genericService.save(doc);
        assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    @Test
    @Rollback
    public void testReadUser() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document doc = setupReadUserDoc();
        assertTrue(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    @Test
    @Rollback
    public void testBadReadUser() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        logger.info("test bad read user");
        Document doc = setupBadReadUserDoc();
        assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    @Test
    @Rollback
    public void testBadFullUser() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document doc = setupBadFullUserDoc();
        assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    private Document setupReadUserDoc() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        addAuthorizedUser(doc, getUser(), Permissions.VIEW_ALL);
        genericService.save(doc);
        return doc;
    }

    private Document setupBadReadUserDoc() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        addAuthorizedUser(doc, getAdminUser(), Permissions.VIEW_ALL);
        genericService.save(doc);
        return doc;
    }

    @Test
    @Rollback
    public void testFullUser() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document doc = setupFullUserDoc();
        assertTrue(authenticationAndAuthorizationService.canViewConfidentialInformation(getUser(), doc));
    }

    private Document setupFullUserDoc() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        addAuthorizedUser(doc, getUser(), Permissions.MODIFY_RECORD);
        return doc;
    }

    private Document setupBadFullUserDoc() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document doc = setupEmbargoedDoc();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        addAuthorizedUser(doc, getAdminUser(), Permissions.MODIFY_RECORD);
        return doc;
    }

    @Test
    @Rollback
    public void testDownloadControllerEmbargoed() throws InstantiationException, IllegalAccessException, TdarActionException, FileNotFoundException {
        Document doc = setupEmbargoedDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileVersionId(doc.getInformationResourceFiles().iterator().next().getUploadedVersion(1).getId());
        controller.prepare();
        assertEquals(DownloadController.FORBIDDEN, controller.execute());
    }

    @Test
    @Rollback
    public void testDownloadControllerConfidential() throws InstantiationException, IllegalAccessException, TdarActionException, FileNotFoundException {
        Document doc = (Document) generateInformationResourceWithFile();
        doc.getInformationResourceFiles().iterator().next().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileVersionId(doc.getInformationResourceFiles().iterator().next().getUploadedVersion(1).getId());
        controller.prepare();
        assertEquals(DownloadController.FORBIDDEN, controller.execute());
    }

    @Test
    @Rollback
    public void testDownloadControllerBadReadUser() throws InstantiationException, IllegalAccessException, TdarActionException, FileNotFoundException {
        Document doc = setupBadReadUserDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileVersionId(doc.getInformationResourceFiles().iterator().next().getLatestUploadedVersion().getId());
        controller.prepare();
        assertEquals(DownloadController.FORBIDDEN, controller.execute());
    }

    @Test
    @Rollback
    public void testDownloadControllerBadFullUser() throws InstantiationException, IllegalAccessException, TdarActionException, FileNotFoundException {
        Document doc = setupBadFullUserDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileVersionId(doc.getInformationResourceFiles().iterator().next().getLatestUploadedVersion().getId());
        controller.prepare();
        assertEquals(DownloadController.FORBIDDEN, controller.execute());
    }

    @Test
    @Rollback
    public void testDownloadControllerReadUser() throws InstantiationException, IllegalAccessException, TdarActionException, FileNotFoundException {
        Document doc = setupReadUserDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileVersionId(doc.getInformationResourceFiles().iterator().next().getLatestUploadedVersion().getId());
        controller.prepare();
        assertEquals(Action.SUCCESS, controller.execute());
    }

    @Test
    @Rollback
    public void testDownloadControllerFullUser() throws InstantiationException, IllegalAccessException, TdarActionException, FileNotFoundException {
        Document doc = setupFullUserDoc();
        DownloadController controller = generateNewInitializedController(DownloadController.class);
        controller.setInformationResourceFileVersionId(doc.getInformationResourceFiles().iterator().next().getLatestUploadedVersion().getId());
        controller.prepare();
        assertEquals(Action.SUCCESS, controller.execute());
    }

    @Test
    @Rollback
    public void testThumbnailControllerInvalid() throws InstantiationException, IllegalAccessException, TdarActionException, FileNotFoundException {
        Document doc = setupBadFullUserDoc();
        ThumbnailDownloadAction controller = generateNewInitializedController(ThumbnailDownloadAction.class);
        InformationResourceFile irFile = doc.getInformationResourceFiles().iterator().next();
        InformationResourceFileVersion currentVersion = irFile.getCurrentVersion(VersionType.WEB_SMALL);
        logger.info("{}", currentVersion.getId());
        if ((irFile.getInformationResourceFileVersions().size() == 3) && (irFile.getCurrentVersion(VersionType.WEB_SMALL) == null)) {
            Assert.fail("Transient failure due to wrong JPEG Processor being used by PDFBox");
        }

        controller.setInformationResourceFileVersionId(currentVersion.getId());
        controller.prepare();
        assertEquals(DownloadController.FORBIDDEN, controller.thumbnail());
    }

    @Test
    @Rollback
    public void testThumbnailController() throws InstantiationException, IllegalAccessException, TdarActionException, FileNotFoundException {
        Document doc = setupFullUserDoc();
        ThumbnailDownloadAction controller = generateNewInitializedController(ThumbnailDownloadAction.class);
        InformationResourceFile irFile = doc.getInformationResourceFiles().iterator().next();
        if ((irFile.getInformationResourceFileVersions().size() == 3) && (irFile.getCurrentVersion(VersionType.WEB_SMALL) == null)) {
            Assert.fail("Transient failure due to wrong JPEG Processor being used by PDFBox");
        }
        controller.setInformationResourceFileVersionId(irFile.getCurrentVersion(VersionType.WEB_SMALL).getId());
        controller.prepare();
        assertEquals(Action.SUCCESS, controller.thumbnail());
    }

}
