package org.tdar.struts.action.resource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;

public class CloneDatasetITCase extends AbstractDataIntegrationTestCase {

    @Autowired
    SerializationService serializationService;
    
    @Autowired
    private ImportService importService;

    @Override
    protected String getTestFilePath() {
        return TestConstants.TEST_DATA_INTEGRATION_DIR;
    }

    @Test
    @Rollback
    public void testDatasetClone() throws Exception {
        Dataset dataset = setupAndLoadResource(AbstractDataIntegrationTestCase.SPITAL_DB_NAME, Dataset.class);
        Long id = dataset.getId();
        genericService.synchronize();
        Dataset newDoc = importService.cloneResource(dataset, getAdminUser());
        genericService.synchronize();
        logger.debug(serializationService.convertToXML(newDoc));

    }

}
