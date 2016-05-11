package org.tdar.core.service.resource;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;

public class ResourceExportServiceITCase extends AbstractIntegrationTestCase {

	@Autowired
	ResourceExportService exportService;

    @Test
    @Rollback
    public void testSingleExport() throws Exception {
        Document doc = generateDocumentWithFileAndUser();
        File export = exportService.export("test123.zip", false, Arrays.asList(doc));
        logger.debug("exported:{}", export);
    }

    @Test
    @Rollback
    public void testMultipleExport() throws Exception {
        Document doc = generateDocumentWithFileAndUser();
        Document doc2 = generateDocumentWithFileAndUseDefaultUser();
        File export = exportService.export("test123.zip", false, Arrays.asList(doc,doc2));
        logger.debug("exported:{}", export);
    }

    @Test
    @Rollback
    public void testSingleExportForReimport() throws Exception {
        Document doc = generateDocumentWithFileAndUser();
        File export = exportService.export("test123.zip", true, Arrays.asList(doc));
        logger.debug("exported:{}", export);
    }


    @Test
    @Rollback
    public void testExportEmail() throws Exception {
        ResourceExportProxy prox = new ResourceExportProxy(getAdminUser());
        exportService.sendEmail(prox, getAdminUser());
    }
}
