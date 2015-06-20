package org.tdar.odata.server;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdar.core.bean.resource.Dataset;

/**
 * Integration test that talks to a stub repository.
 * I can control what's in the stub repository so its easier to figure out the required assertions.
 * 
 * @author Richard Rothwell
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "Placeholder-context.xml" })
public class ODataMetadataRequestIntegrationITCase extends AbstractLightFitTest {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private InitialisableRepositoryService repositoryService;

    @Test
    public void testMetaDataUrl() throws Exception {
        HttpResponse exchange = setupExchange(Constant.META_DATA_URL);
        verifyResponseIsReturned(exchange);
    }

    @Test
    @DirtiesContext
    public void testMetaDataResponseContent() throws Exception {
        HttpResponse exchange = setupExchange(Constant.META_DATA_URL);
        String inXMLString = IOUtils.toString(exchange.getEntity().getContent());

        // See: odata_metadata_response.xml

        String xpathExpression = "//edmx:Edmx/edmx:DataServices/c:Schema[@Namespace='tDAR']/c:EntityContainer[@Name='Datasets']/c:EntitySet[@Name='TDataSets']";
        assertXpathExists(xpathExpression, inXMLString);
    }

    @DirtiesContext
    @Override
    protected void createTestScenario() {
        super.createTestScenario();

        Dataset dataset = AbstractFitTest.createTestDataset();
        repositoryService.saveOwnedDataTables(dataset.getDataTables());
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
