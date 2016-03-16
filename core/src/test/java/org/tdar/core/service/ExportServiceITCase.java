package org.tdar.core.service;

import java.io.File;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.service.resource.ResourceExportService;

public class ExportServiceITCase extends AbstractIntegrationTestCase {

	@Autowired
	ResourceExportService exportService;

	@Test
	@Rollback
	public void testSingleExport() throws Exception {
		Document doc = generateDocumentWithFileAndUser();
		File export = exportService.export(doc);
		logger.debug("exported:{}", export);
	}
}
