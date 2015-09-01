package org.tdar.dataone.service;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Test;
import org.tdar.utils.SimpleHttpUtils;
import org.tdar.utils.TestConfiguration;
import org.tdar.web.AbstractWebTestCase;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

public class DataOneWebITCase extends AbstractWebTestCase {

    private static final String TEST_DOI = "doi:10.6067:XCV8SN0B29" + DataOneService.D1_SEP + DataOneService.D1_FORMAT;
    private static final String TEST_DOI_META = "doi:10.6067:XCV8SN0B29" + DataOneService.D1_SEP + DataOneService.META;

    @Test
    public void ping() {
        Assert.assertEquals(200,gotoPage("/dataone/v1/monitor/ping"));
    }
    @Test
    public void replica() throws ClientProtocolException, IOException {
        getRecord("dataone/v1/replica/" + TEST_DOI);
        getRecord("dataone/v1/replica/" + TEST_DOI_META);

    }
    
    @Test
    public void systemInfo() {
        Assert.assertEquals(200, gotoPage("/dataone/v1/"));
        logger.debug(getPageCode());
        Assert.assertEquals(200, gotoPage("/dataone/v1/node"));
        logger.debug(getPageCode());
    }

    @Test
    public void testObject() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        getRecord("dataone/v1/object/" + TEST_DOI);
        getRecord("dataone/v1/object/" + TEST_DOI_META);
        
    }

    private void getRecord(String path) throws ClientProtocolException, IOException {
        HttpGet getMethod = new HttpGet(TestConfiguration.getInstance().getBaseSecureUrl() + path);
        CloseableHttpClient httpClient = SimpleHttpUtils.createClient();
        HttpResponse httpResponse = httpClient.execute(getMethod);
        int statusCode = httpResponse.getStatusLine().getStatusCode();

        Assert.assertEquals(200, statusCode);
        for (Header header : httpResponse.getAllHeaders()) {
            logger.debug("headers: {}", header);
        }
        
    }
    
    @Test
    public void testGetObjects() {
        Assert.assertEquals(200, gotoPage("/dataone/v1/object?idFilter=" + TEST_DOI));
        // bad doi
        Assert.assertEquals(200, gotoPage("/dataone/v1/object?formatId=fake_formatasdasd"));
        Assert.assertEquals(200, gotoPage("/dataone/v1/object?"));
        //YYYY-MM-DDTHH:MM:SS.mmm
        Assert.assertEquals(200, gotoPage("/dataone/v1/object?fromDate=2010-01-01T01:01:00.000"));
        //formatId??
    }
    
    @Test
    public void testObjectHead() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        String path = "dataone/v1/object/" + TEST_DOI;
        HttpHead getMethod = new HttpHead(TestConfiguration.getInstance().getBaseSecureUrl() + path);
        CloseableHttpClient httpClient = SimpleHttpUtils.createClient();
        HttpResponse httpResponse = httpClient.execute(getMethod);
        int statusCode = httpResponse.getStatusLine().getStatusCode();

        Assert.assertEquals(200, statusCode);
        for (Header header : httpResponse.getAllHeaders()) {
            logger.debug("headers: {}", header);
        }
    }

    @Test
    public void testLog() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        testObject();
        Assert.assertEquals(200, gotoPage("dataone/v1/log?idFilter=" + TEST_DOI));
        Assert.assertEquals(200, gotoPage("dataone/v1/log?event=READ"));
        Assert.assertEquals(200, gotoPage("dataone/v1/log?fromDate=2010-01-01T01:01:00.000"));
        //test with date ... YYYY-MM-DDTHH:MM:SS.mmm
        
    }

    @Test
    public void testChecksum() {
        Assert.assertEquals(200,  gotoPage("dataone/v1/checksum/"+ TEST_DOI));
    }
    @Test
    public void testMeta() {
        Assert.assertEquals(200, gotoPage("dataone/v1/meta/"+ TEST_DOI));
        logger.debug(getPageCode());
    }
    @Test
    public void testMetaIvalid() {
        Assert.assertEquals(404, gotoPage("dataone/v1/meta/a"+ TEST_DOI));
        logger.debug(getPageCode());
    }
}
