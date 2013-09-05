package org.tdar.odata.server;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import org.apache.log4j.Logger;
import org.eclipse.jetty.client.ContentExchange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdar.core.bean.resource.Dataset;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "Placeholder-context.xml" })
public class TDataTableFeedRequestIntegrationITCase extends AbstractLightFitTest {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private InitialisableRepositoryService repositoryService;

    @Test
    @DirtiesContext
    public void testFeedUrl() throws Exception {
        ContentExchange exchange = setupExchange(Constant.TDATATABLES_FEED_URL);
        verifyResponseIsReturned(exchange);
    }

    @Test
    @DirtiesContext
    public void testMetaDataResponseContent() throws Exception {
        ContentExchange exchange = setupExchange(Constant.TDATATABLES_FEED_URL);
        String inXMLString = exchange.getResponseContent();

        // See: tdataset_feed_response.xml

        String xpathExpression = "//_atom:feed/_atom:title[@type='text']";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:id";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:updated";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:link[@title='TDataTables']";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:entry/_atom:id";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:entry/_atom:title";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:entry/_atom:summary";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:entry/_atom:updated";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:entry/_atom:author";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:entry/_atom:link[@href=\"TDataTables('Pompeii:Insula of Julia Felix')\"]";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:entry/_atom:link[@href=\"TDataTables('Pompeii:Insula of Julia Felix')/TDataRecords\"]";
        assertXpathExists(xpathExpression, inXMLString);
    }

    // TODO RR: add test for expanded response query.

    @Override
    protected void createTestScenario() {
        super.createTestScenario();

        Dataset dataset = createTestDataset();

        repositoryService.saveOwnedDataTables(dataset.getDataTables());
    }

    protected Logger getLogger() {
        return logger;
    }
}
