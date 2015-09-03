package org.tdar.web;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.SimpleHttpUtils;

/**
 * Created by jimdevos on 10/20/14.
 */
public class HostedDownloadWebITCase extends AbstractWebTestCase {

    String REFERER_URL = "http://www.samplewebsite.info/gallery";
    String API_KEY_KEY = "apikey";
    String API_KEY_VALUE = "abc123";

    long IRFV_ID = 68L;
    String IRF_FILENAME = "tag-fauna-ontology---taxon.owl";

    Logger logger = LoggerFactory.getLogger(getClass());
    TdarConfiguration tdarConfig = TdarConfiguration.getInstance();

    CloseableHttpClient httpclient;

    public HostedDownloadWebITCase() {
    }

    URIBuilder uriBuilder() {
        return new URIBuilder();
    }

    HttpGet httpGet(URI uri) {
        return new HttpGet(uri);
    }

    @Before
    public void setupHostedDownloadTest() {
        httpclient = SimpleHttpUtils.createClient();
    }

    @After
    public void teadownhostedDownloadTest() {
        try {
            httpclient.close();
        } catch (IOException mostlyignored) {
            logger.info("failed to close: {}", mostlyignored);
        }
    }

    @Test
    @Ignore
    /**
     * Perform a hosted download request with valid key, referrer, and file ID.
     */
    public void testHostedDownloadSuccess() throws URISyntaxException, IOException {
        // sanity check: make sure the file exists
        InformationResourceFileVersion irfv = genericService.find(InformationResourceFileVersion.class, IRFV_ID);
        assertNotNull(irfv);

        HttpGet httpget = httpGet(uriBuilder()
                .setScheme("http")
                .setHost(tdarConfig.getHostName())
                .setPort(tdarConfig.getPort())
                .setPath("/download/hosted/" + IRFV_ID)
                .addParameter(API_KEY_KEY, API_KEY_VALUE)
                .build());
        httpget.addHeader(HttpHeaders.REFERER, REFERER_URL);

        try (CloseableHttpResponse response = httpclient.execute(httpget)) {
            //make sure that the server gave us a successful response
            assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));

            HttpEntity entity = response.getEntity();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // assert that a download actually occurred
            assertThat(entity.getContentLength(), greaterThan(0L));
            entity.writeTo(baos);
            assertThat("filesize matches response.entity.contentLength", (long) baos.size(), is(entity.getContentLength()));
        }
    }
}
