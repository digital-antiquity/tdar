package org.tdar.dataone.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.regexp.recompile;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.codehaus.plexus.util.StringInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.tdar.utils.SimpleHttpUtils;
import org.tdar.utils.TestConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.hp.hpl.jena.sparql.function.library.e;

public class DataOneWebITCase extends AbstractWebTest {

    private static final String TEST_DOI = "doi:10.6067:XCV8SN0B29" + DataOneService.D1_SEP + DataOneService.D1_FORMAT;
    private static final String TEST_DOI_META = "doi:10.6067:XCV8SN0B29" + DataOneService.D1_SEP + DataOneService.META;

    @Test
    public void replica() throws ClientProtocolException, IOException {
        getRecord("/v1/replica/" + TEST_DOI);
        getRecord("/v1/replica/" + TEST_DOI_META);

    }

    @Test
    public void ping() {
        Assert.assertEquals(200,gotoPage("/v1/monitor/ping"));
    }

    @Test
    public void systemInfo() {
        Assert.assertEquals(200, gotoPage("/v1/"));
        logger.debug(getPageCode());
        Assert.assertEquals(200, gotoPage("/v1/node"));
        logger.debug(getPageCode());
    }

    public String getPageCode() {
        String content = internalPage.getWebResponse().getContentAsString();
        return content;
    }

    @Test
    public void testObject() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        getRecord("/v1/object/" + TEST_DOI);
        getRecord("/v1/object/" + TEST_DOI_META);
    }
    
    
    @Test
    public void testChecksum2() throws ClientProtocolException, IOException, UnsupportedOperationException, ParserConfigurationException, SAXException {
        HttpResponse record = getRecord("/v1/object");
        String body = IOUtils.toString(record.getEntity().getContent());
        Document xmlDocument = getXmlDocument(new InputSource(new StringInputStream(body)));
        logger.trace(body);
        NodeList elementsByTagName = xmlDocument.getElementsByTagName("objectInfo");
        for (int i=0;i < elementsByTagName.getLength(); i++) {
            Node entry = elementsByTagName.item(i);
            String id = null;
            String checksum = null;
            NodeList childNodes = entry.getChildNodes();
            for (int j=0; j < childNodes.getLength(); j++) {
                Node node = childNodes.item(j);
                if (node.getNodeName().equals("identifier")) {
                    id = node.getTextContent();
                }
                if (node.getNodeName().equals("checksum")) {
                    checksum = node.getTextContent();
                }
            }
            logger.debug("{} {}", id, checksum);
            assertNotNull(checksum);
            assertNotNull(id);
            HttpResponse hresponse = headRecord("/v1/object/"+ id);
            logger.debug("headers: {}", hresponse.getAllHeaders());
            Header firstHeader = hresponse.getFirstHeader("DataONE-Checksum");
            String headerChecksum = firstHeader.getValue().replace("MD5,", "");
            HttpResponse response = getRecord("/v1/object/"+ id);
            logger.debug("encoding:{}",response.getEntity().getContentEncoding());
            InputStream content = response.getEntity().getContent();
            String xml = IOUtils.toString(content, "UTF-8");
            String md5Hex = DigestUtils.md5Hex(xml);
            logger.debug("{}",xml);
            logger.debug("{} {} {} {}", id, md5Hex, checksum, headerChecksum);
            assertEquals(md5Hex, checksum);
            
            
        }
        
    }
    
    private HttpResponse getRecord(String path) throws ClientProtocolException, IOException {
        HttpGet getMethod = new HttpGet(TestConfiguration.getInstance().getBaseSecureUrl() + path);
        CloseableHttpClient httpClient = SimpleHttpUtils.createClient();
        HttpResponse httpResponse = httpClient.execute(getMethod);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        

        Assert.assertEquals(200, statusCode);
        for (Header header : httpResponse.getAllHeaders()) {
            logger.debug("headers: {}", header);
        }
        
        return httpResponse;
        
    }

    private org.w3c.dom.Document getXmlDocument(InputSource is) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory
                .newDocumentBuilder();
        org.w3c.dom.Document document = documentBuilder.parse(is);
        return document;
    }

    @Test
    public void testGetObjects() {
        Assert.assertEquals(200, gotoPage("/v1/object?idFilter=" + TEST_DOI));
        // bad doi
        Assert.assertEquals(200, gotoPage("/v1/object?formatId=fake_formatasdasd"));
        Assert.assertEquals(200, gotoPage("/v1/object?"));
        //YYYY-MM-DDTHH:MM:SS.mmm
        Assert.assertEquals(200, gotoPage("/v1/object?fromDate=2010-01-01T01:01:00.000"));
        //formatId??
    }
    
    @Test
    public void testObjectHead() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        String path = "/v1/object/" + TEST_DOI;
        headRecord(path);
    }

    private HttpResponse headRecord(String path) throws IOException, ClientProtocolException {
        HttpHead getMethod = new HttpHead(TestConfiguration.getInstance().getBaseSecureUrl() + path);
        CloseableHttpClient httpClient = SimpleHttpUtils.createClient();
        HttpResponse httpResponse = httpClient.execute(getMethod);
        int statusCode = httpResponse.getStatusLine().getStatusCode();

        Assert.assertEquals(200, statusCode);
        for (Header header : httpResponse.getAllHeaders()) {
            logger.debug("headers: {}", header);
        }
        return httpResponse;
    }

    @Test
    public void testLog() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        testObject();
        Assert.assertEquals(200, gotoPage("/v1/log?idFilter=" + TEST_DOI));
        Assert.assertEquals(200, gotoPage("/v1/log?event=READ"));
        Assert.assertEquals(200, gotoPage("/v1/log?fromDate=2010-01-01T01:01:00.000"));
        //test with date ... YYYY-MM-DDTHH:MM:SS.mmm
        
    }

    @Test
    public void testChecksum() {
        Assert.assertEquals(200,  gotoPage("/v1/checksum/"+ TEST_DOI));
    }
    @Test
    public void testMeta() {
        Assert.assertEquals(200, gotoPage("/v1/meta/"+ TEST_DOI));
        logger.debug(getPageCode());
    }
    @Test
    public void testMetaIvalid() {
        Assert.assertEquals(404, gotoPage("/v1/meta/a"+ TEST_DOI));
        logger.debug(getPageCode());
    }
}
