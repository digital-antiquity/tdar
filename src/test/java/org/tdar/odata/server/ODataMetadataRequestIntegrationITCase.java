package org.tdar.odata.server;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;
import org.eclipse.jetty.client.ContentExchange;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;

/**
 * Integration test that talks to a stub repository.
 * I can control what's in the stub repository so its easier to figure out the required assertions.
 * @author Richard Rothwell
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"Placeholder-context.xml"})
@Ignore
public class ODataMetadataRequestIntegrationITCase extends AbstractLightFitTest {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private InitialisableRepositoryService repositoryService;

    @Test
    public void testMetaDataUrl() throws Exception {
        ContentExchange exchange = getTestingClient().sendRequest(Constant.META_DATA_URL);
        exchange.waitForDone();
        verifyResponseIsReturned(exchange);
    }

    @Test
    @DirtiesContext
    public void testMetaDataResponseContent() throws Exception {
        ContentExchange exchange = getTestingClient().sendRequest(Constant.META_DATA_URL);
        exchange.waitForDone();
        String inXMLString = exchange.getResponseContent();

        // See: odata_metadata_response.xml

        String xpathExpression = "//edmx:Edmx/edmx:DataServices/c:Schema[@Namespace='tDAR']/c:EntityContainer[@Name='Datasets']/c:EntitySet[@Name='TDataSets']";
        assertXpathExists(xpathExpression, inXMLString);
    }    

    @DirtiesContext
    @Override
    protected void createTestScenario() {
        super.createTestScenario();
        
        LinkedHashSet<DataTable> dataTables = new LinkedHashSet<DataTable>();

        Dataset dataset = new Dataset();
        dataset.setTitle(Constant.GRECIAN_URNS_DATASET_NAME);
        dataset.setUpdatedBy(new Person("Frankie", "Bloggs", null));
        
        DataTable dataTable = new DataTable();
        
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn0 = new DataTableColumn() {{setName("id");}};
        dataTable.setDataTableColumns(Arrays.asList(dataTableColumn0));
        
        dataTable.setName("Pompeii:Insula of Julia Felix");
        dataTable.setDataset(dataset);
        dataTables.add(dataTable);
        
        dataset.setDataTables(dataTables);
        
        repositoryService.saveOwnedDataTables(new ArrayList<DataTable>(dataTables));
    }


    protected Logger getLogger() {
        return logger;
    }
}
