package org.tdar.web;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.opensymphony.xwork2.interceptor.annotations.After;
import com.opensymphony.xwork2.interceptor.annotations.Before;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by jimdevos on 10/20/14.
 */
public class HostedDownloadWebITCase extends AbstractWebTestCase {

    String HEADER_KEY = "Referer";
    String HEADER_VALUE = "http://www.samplewebsite.info/gallery";
    String API_KEY_KEY = "apikey";
    String API_KEY_VALUE = "abc123";
    String IRFV_ID = "5692";
    String IRF_FILENAME = "fig-27-benigno-t-argote-stone-tool_lg.jpg";



    @Before
    public void setupHostedDownloadTest() {
        //TODO: code that allows hosted downloads for specified apikey goes here
    }

    @After
    public void teadownhostedDownloadTest() {
        //TODO: code that removes hosted download entry for specified apikey
    }

    @Test
    public void testHostedDownloadSuccess() throws IOException {
        WebClient client = new WebClient(BrowserVersion.FIREFOX_24);
        //TODO: figure out how to modify request header only for a single request only  - this call adds the header to every request.
        client.addRequestHeader(HEADER_KEY, HEADER_VALUE);

        String url = getBaseUrl() + "hosted-download/" + IRFV_ID + "?" + API_KEY_KEY + "=" + API_KEY_VALUE ;
        getLogger().info("url: {}", url);

        //if not successful, htmlunit throws exception
        Page page = client.getPage(url);
    }

}


