package org.tdar.odata.server;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@ContextConfiguration(locations = { "classpath:/org/tdar/odata/server/AbstractFitTest-context.xml" })
public abstract class AbstractFitTest {

    private ITestingClient testingClient;

    private ITestingServer testingServer;

    protected final static NamespaceContext ODATA_NAMESPACE_CONTEXT = createODataNameSpaceContext();

    public ITestingClient getTestingClient() {
        return testingClient;
    }

    @Autowired
    public void setTestingClient(HttpTestingClient testingClient) {
        this.testingClient = testingClient;
    }

    public ITestingServer getTestingServer() {
        return testingServer;
    }

    @Autowired
    public void setTestingServer(ITestingServer testingServer) {
        this.testingServer = testingServer;
    }

    protected abstract void createTestScenario();

    @BeforeClass
    public static void configureXmlUnit() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setXpathNamespaceContext(ODATA_NAMESPACE_CONTEXT);
    }

    @Before
    public void setup() throws Exception {
        testingServer.createServer();
        createTestScenario();
        testingServer.startServer();
        testingClient.createClient();
    }

    @After
    public void teardown() throws Exception {
        testingServer.stopServer();
    }

    protected void verifyResponseIsReturned(HttpResponse exchange) throws InterruptedException, IOException {
        Assert.assertEquals(HttpStatus.SC_OK, exchange.getStatusLine().getStatusCode());
        Assert.assertTrue(IOUtils.toString(exchange.getEntity().getContent()).length() > 0);
    }

    protected void assertXmlElementByXPath(String xpathExpression, String inXMLString) throws SAXException, IOException, XpathException {

        XpathEngine xpathEngine = XMLUnit.newXpathEngine();
        xpathEngine.setNamespaceContext(ODATA_NAMESPACE_CONTEXT);

        Document xmlDocument = XMLUnit.buildControlDocument(inXMLString);
        NodeList nodeList = xpathEngine.getMatchingNodes(xpathExpression, xmlDocument);
        assertEquals(1, nodeList.getLength());
        assertEquals(Node.ELEMENT_NODE, nodeList.item(0).getNodeType());
    }

    protected void assertXmlByXmlFileContents(String expectedContentFileClasspath, String actualXMLString) throws SAXException, IOException {

        // This scheme for comparing the files sort of works, except the
        // EntitySet nodes in the OData metadata response have no fixed order.
        // So "/odata_metadata_response.xml" will not work, but others might.

        InputStream resourceAsStream = this.getClass().getResourceAsStream(expectedContentFileClasspath);
        InputSource control = new InputSource(resourceAsStream);
        InputSource test = new InputSource(new ByteArrayInputStream(actualXMLString.getBytes("UTF-8")));
        XMLAssert.assertXMLEqual(control, test);
    }

    protected static NamespaceContext createODataNameSpaceContext() {

        Map<String, String> namespaceMap = new HashMap<String, String>();

        // For the metadata response:

        // The "edmx", "d" and "m" namespaces simply mirror the definitions in the xml doc.
        // These could have been something arbitrary.
        namespaceMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
        namespaceMap.put("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
        namespaceMap.put("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
        // The "c" namespace is defined so we can address the Schema nodes.
        // The Schema nodes define an anonymous namespace which I can't figure out how to use,
        // so we create our own named namespace.
        // namespaceMap.put("c", "http://schemas.microsoft.com/ado/2006/04/edm");
        namespaceMap.put("c", "http://schemas.microsoft.com/ado/2008/09/edm");

        // For the service and feed response:

        namespaceMap.put("_app", "http://www.w3.org/2007/app");
        namespaceMap.put("base", "http://localhost:8888/test.svc/");
        namespaceMap.put("atom", "http://www.w3.org/2005/Atom");
        namespaceMap.put("_atom", "http://www.w3.org/2005/Atom");
        namespaceMap.put("app", "http://www.w3.org/2007/app");

        NamespaceContext nameSpaceContext = new SimpleNamespaceContext(namespaceMap);
        return nameSpaceContext;
    }

    abstract protected Logger getLogger();

    public static Dataset createTestDataset() {
        Dataset dataset = new Dataset();
        dataset.setTitle(Constant.GRECIAN_URNS_DATASET_NAME);
        dataset.setUpdatedBy(new TdarUser("Frankie", "Bloggs", null));
        DataTable dataTable = new DataTable();
        dataset.getDataTables().add(dataTable);
        dataset.setDescription("Mycenae BC 1600 pot sherds");
        dataset.setUrl("http://arbitrary.org");
        dataset.setExternalId("Some foreign identifier");

        dataTable.setDataset(dataset);
        DataTableColumn dataTableColumn0 = new DataTableColumn();
        dataTableColumn0.setName("id");
        dataTableColumn0.setColumnDataType(DataTableColumnType.BIGINT);
        dataTableColumn0.setDisplayName("Identifier");
        DataTableColumnEncodingType columnEncodingType = DataTableColumnEncodingType.UNCODED_VALUE;
        dataTableColumn0.setColumnEncodingType(columnEncodingType);

        dataTable.setName("Pompeii:Insula of Julia Felix");
        dataTable.setDisplayName("Pompeii:Insula of Julia Felix");
        dataTable.setDataTableColumns(Arrays.asList(dataTableColumn0));
        return dataset;
    }

    public HttpResponse setupExchange(String url) throws IOException, InterruptedException {
        HttpResponse exchange = getTestingClient().sendRequest(url);
        return exchange;
    }

}
