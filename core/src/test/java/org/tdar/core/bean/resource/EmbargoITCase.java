package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.utils.MessageHelper;

public class EmbargoITCase extends AbstractIntegrationTestCase {

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
