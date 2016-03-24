package org.tdar.core.service.resource;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.service.resource.ResourceExportService;

public class ResourceExportServiceITCase extends AbstractIntegrationTestCase {

	@Autowired
	ResourceExportService exportService;

    @Test
    @Rollback
    public void testSingleExport() throws Exception {
        Document doc = generateDocumentWithFileAndUser();
        File export = exportService.export("test123.zip", Arrays.asList(doc));
        logger.debug("exported:{}", export);
    }


    @Test
    @Rollback
    public void testExportEmail() throws Exception {
        ResourceExportProxy prox = new ResourceExportProxy(getAdminUser());
        exportService.sendEmail(prox, getAdminUser());
    }
}
