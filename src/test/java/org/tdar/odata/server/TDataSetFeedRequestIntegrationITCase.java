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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"Placeholder-context.xml"})
@Ignore
public class TDataSetFeedRequestIntegrationITCase extends AbstractLightFitTest {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private InitialisableRepositoryService repositoryService;

    @Test
    @DirtiesContext
    public void testFeedUrl() throws Exception {
        ContentExchange exchange = getTestingClient().sendRequest(Constant.TDATASETS_FEED_URL);
        exchange.waitForDone();
        verifyResponseIsReturned(exchange);
    }

    @Test
    @DirtiesContext
    public void testMetaDataResponseContent() throws Exception {
        ContentExchange exchange = getTestingClient().sendRequest(Constant.TDATASETS_FEED_URL);
        exchange.waitForDone();
        String inXMLString = exchange.getResponseContent();

        // See: tdataset_feed_response.xml

        String xpathExpression = "//_atom:feed/_atom:title[@type='text']";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:id";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:updated";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:link[@title='TDataSets']";
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

        xpathExpression = "//_atom:feed/_atom:entry/_atom:link[@href=\"TDataSets('Grecian Urns')\"]";
        assertXpathExists(xpathExpression, inXMLString);

        xpathExpression = "//_atom:feed/_atom:entry/_atom:link[@href=\"TDataSets('Grecian Urns')/TDataTables\"]";
        assertXpathExists(xpathExpression, inXMLString);
    }
    
    // TODO RR: add test for expanded response query.

    @Override
    protected void createTestScenario() {
        super.createTestScenario();

        LinkedHashSet<DataTable> dataTables = new LinkedHashSet<DataTable>();
        
        @SuppressWarnings("serial")
        DataTableColumn dataTableColumn0 = new DataTableColumn() {{setName("id");}};

        DataTable dataTable = new DataTable();        
        dataTable.setName("Pompeii:Insula of Julia Felix");
        dataTable.setDataTableColumns(Arrays.asList(dataTableColumn0));
        dataTables.add(dataTable);

        Dataset dataset = new Dataset();
        dataset.setTitle(Constant.GRECIAN_URNS_DATASET_NAME);
        dataset.setUpdatedBy(new Person("Frankie", "Bloggs", null));
        dataset.setDataTables(dataTables);
        dataTable.setDataset(dataset);
        
        repositoryService.save(dataTable);
        repositoryService.saveOwnedDatasetByName(Constant.GRECIAN_URNS_DATASET_NAME, dataset);        
        repositoryService.saveOwnedDatasets(Arrays.asList(dataset));
        
        repositoryService.saveOwnedDataTables(new ArrayList<DataTable>(dataTables));
    }

    protected Logger getLogger() {
        return logger;
    }
}
