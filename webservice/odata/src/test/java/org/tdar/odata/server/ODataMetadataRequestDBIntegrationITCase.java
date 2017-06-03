package org.tdar.odata.server;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;

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

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericService genericService;

    @Autowired
    private EntityService entityService;

    @Test
    public void testMetaDataUrl() throws Exception {
        HttpResponse exchange = getTestingClient().sendRequest(Constant.META_DATA_URL);
        verifyResponseIsReturned(exchange);
    }

    @Test
    public void testMetaDataResponseContent() throws Exception {
        HttpResponse exchange = getTestingClient().sendRequest(Constant.META_DATA_URL);
        String inXMLString = IOUtils.toString(exchange.getEntity().getContent());

        // See: odata_metadata_response.xml

        String xpathExpression = "//edmx:Edmx/edmx:DataServices/c:Schema[@Namespace='tDAR']/c:EntityContainer[@Name='Datasets']/c:EntitySet[@Name='TDataSets']";
        assertXpathExists(xpathExpression, inXMLString);
    }

    @Override
    protected void createTestScenario() {

        // Set a user to define entity ownership.
        TdarUser knownPerson = (TdarUser) entityService.findByEmail("kintigh@dsu.edu");
        getTestingServer().setPerson(knownPerson);
        // Database setup

        Dataset dataset = createTestDataset();
        dataset.markUpdated(knownPerson);
        dataset.getAuthorizedUsers().add(new AuthorizedUser(null, knownPerson, GeneralPermissions.MODIFY_RECORD));
        Set<DataTable> dataTables = dataset.getDataTables();
        dataset.getDataTables().clear();
        genericService.saveOrUpdate(dataset);
        dataset.setDataTables(dataTables);
        genericService.saveOrUpdate(dataset);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

}
