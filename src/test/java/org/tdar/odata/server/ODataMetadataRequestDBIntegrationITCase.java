package org.tdar.odata.server;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;
import org.eclipse.jetty.client.ContentExchange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;

/**
 * Integration test that talks to the tdar test database.
 * This is used to verify that the spring wiring to the actual database repository
 * is correct.
 * 
 * When running this test ensure that the command line has:
 * -DenableMockAuth=true
 * Or that the authentication system is running.
 * 
 * @author Richard Rothwell
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "Placeholder-context.xml" })
public class ODataMetadataRequestDBIntegrationITCase extends AbstractHeavyFitTest {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private GenericService genericService;

    @Autowired
    private AuthenticationAndAuthorizationService authService;

    @Test
    public void testMetaDataUrl() throws Exception {
        ContentExchange exchange = getTestingClient().sendRequest(Constant.META_DATA_URL);
        exchange.waitForDone();
        verifyResponseIsReturned(exchange);
    }

    @Test
    public void testMetaDataResponseContent() throws Exception {
        ContentExchange exchange = getTestingClient().sendRequest(Constant.META_DATA_URL);
        exchange.waitForDone();
        String inXMLString = exchange.getResponseContent();

        // See: odata_metadata_response.xml

        String xpathExpression = "//edmx:Edmx/edmx:DataServices/c:Schema[@Namespace='tDAR']/c:EntityContainer[@Name='Datasets']/c:EntitySet[@Name='TDataSets']";
        assertXpathExists(xpathExpression, inXMLString);
    }

    @Override
    protected void createTestScenario() {

        // Set a user to define entity ownership.
        // Need to understand the test database and the actual ownership defined there.
        Person authenticatedUser = new Person("Keith", "Kintigh", "kintigh@asu.edu");
        authenticatedUser.setUsername("kintigh@asu.edu");

        Person knownPerson = genericService.findByProperty(Person.class, "username", authenticatedUser.getUsername());
        if (knownPerson == null) {
            knownPerson = authenticatedUser;
            genericService.save(knownPerson);
            getLogger().info("Somehow the test person is missing from the database.");
            // throw new RuntimeException("Somehow the test person is missing from the database." );
        }
        getTestingServer().setPerson(knownPerson);
        // Database setup

        Dataset dataset = createTestDataset();
        dataset.markUpdated(knownPerson);

        genericService.save(dataset);
    }


    protected Logger getLogger() {
        return logger;
    }

}
