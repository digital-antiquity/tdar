package org.tdar.core.bean.resource;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.dao.resource.InformationResourceFileDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.processes.daily.EmbargoedFilesUpdateProcess;
import org.tdar.utils.MessageHelper;

public class EmbargoITCase extends AbstractIntegrationTestCase {

    @Autowired
    private InformationResourceFileDao informationResourceFileDao;
    
    @Test
    @Rollback
    public void testEmbargoWarning() throws InstantiationException, IllegalAccessException {
        Document doc = generateDocumentWithFileAndUser();
        Document doc2 = generateDocumentWithFileAndUseDefaultUser();
        long id = doc.getId();

        InformationResourceFile irf = doc.getFirstInformationResourceFile();
        InformationResourceFile irf2 = doc2.getFirstInformationResourceFile();
        irf.setRestriction(FileAccessRestriction.EMBARGOED_SIX_MONTHS);
        irf.setDateMadePublic(DateTime.now().plusDays(1).toDate());
        genericService.saveOrUpdate(irf);
        irf2.setRestriction(FileAccessRestriction.EMBARGOED_SIX_MONTHS);
        irf2.setDateMadePublic(DateTime.now().minusDays(1).toDate());
        genericService.saveOrUpdate(irf2);
        List<InformationResourceFile> expiring = informationResourceFileDao.findAllEmbargoFilesExpiring();
        List<InformationResourceFile> expired = informationResourceFileDao.findAllExpiredEmbargoes();
        logger.debug("expiring: {}", expiring);
        logger.debug(" expired: {}", expired);
        assertTrue("expired should contain irf2", expired.contains(irf2));
        assertFalse("expired should not contain irf", expired.contains(irf));
        assertTrue("expiring should contain irf", expiring.contains(irf));
        assertFalse("expiring should not contain irf", expiring.contains(irf2));
        // increment expiring by 1 day
        irf.setDateMadePublic(DateTime.now().withTimeAtStartOfDay().toDate());
        genericService.saveOrUpdate(irf);
        expired = informationResourceFileDao.findAllExpiredEmbargoes();
        expiring = informationResourceFileDao.findAllEmbargoFilesExpiring();
        logger.debug("expiring: {}", expiring);
        logger.debug(" expired: {}", expired);
        assertFalse("expired should not contain irf", expired.contains(irf));
        assertFalse("expiring should contain irf", expiring.contains(irf));
        
        // increment expiring by 1 day
        irf.setDateMadePublic(DateTime.now().minusDays(1).toDate());
        genericService.saveOrUpdate(irf);
        expired = informationResourceFileDao.findAllExpiredEmbargoes();
        expiring = informationResourceFileDao.findAllEmbargoFilesExpiring();
        logger.debug("expiring: {}", expiring);
        logger.debug(" expired: {}", expired);
        assertTrue("expired should contain irf", expired.contains(irf));
        assertFalse("expiring should  not contain irf", expiring.contains(irf));

    }
    
    @Test
    @Rollback
    public void testChangedEmbargoExpiry() throws InstantiationException, IllegalAccessException, IOException {
        Document document = generateDocumentWithFileAndUseDefaultUser();
        InformationResourceFile file = document.getFirstInformationResourceFile();
        file.setRestriction(FileAccessRestriction.EMBARGOED_FIVE_YEARS);
        DateTime now = DateTime.now();
        genericService.saveOrUpdate(file);
        file.setDateMadePublic(now.plusYears(1).toDate());
        FileProxy proxy2 = new FileProxy();
        proxy2.setRestriction(FileAccessRestriction.EMBARGOED_TWO_YEARS);
        proxy2.setAction(FileAction.MODIFY_METADATA);
        proxy2.setFileId(file.getId());
        String msg = null;
        try {
            ErrorTransferObject listener = informationResourceService.importFileProxiesAndProcessThroughWorkflow(document, null, null, Arrays.asList(proxy2));
        } catch (TdarRecoverableRuntimeException trre) {
            msg = trre.getMessage();
            logger.error(msg);
        }
        assertEquals(MessageHelper.getInstance().getText("abstractInformationResourceService.expiry_occurs_before_today"), msg);
    }

}
