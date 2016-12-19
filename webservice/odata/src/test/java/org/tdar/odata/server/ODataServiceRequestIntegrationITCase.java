package org.tdar.odata.server;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdar.core.bean.resource.datatable.DataTable;

/**
 * Integration test that talks to a stub repository.
 * I can control what's in the stub repository so its easier to figure out the required assertions.
 * 
 * @author Richard Rothwell
 * 
 */

@Configurable(autowire = Autowire.BY_NAME)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "Placeholder-context.xml" })
public class ODataServiceRequestIntegrationITCase extends AbstractLightFitTest {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private InitialisableRepositoryService repositoryService;

    @Test
    @DirtiesContext
    public void testServiceUrl() throws Exception {
        HttpResponse  exchange = setupExchange(Constant.SERVICE_URL);
        verifyResponseIsReturned(exchange);
    }

    @Test
    @DirtiesContext
    public void testMetaDataResponseContent() throws Exception {
        HttpResponse  exchange = setupExchange(Constant.SERVICE_URL);
        String inXMLString = IOUtils.toString(exchange.getEntity().getContent());

        // See: odata_service_response.xml

        String xpathExpression = "//_app:service/_app:workspace/_app:collection[@href='TDataRecords']";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_app:service/_app:workspace/_app:collection[@href='TDataTables']";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_app:service/_app:workspace/_app:collection[@href='TDataSets']";
        assertXpathExists(xpathExpression, inXMLString);

        // TODO RR: add more assertions for concrete data record entities.
    }

    @Override
    protected void createTestScenario() {
        super.createTestScenario();
        repositoryService.saveOwnedDataTables(new ArrayList<DataTable>());
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
