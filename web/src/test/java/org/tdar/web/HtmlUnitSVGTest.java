package org.tdar.web;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@Ignore
public class HtmlUnitSVGTest {

    @SuppressWarnings({ "resource", "unused" })
    @Test
    public void testSVG() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
        webClient.getOptions().setJavaScriptEnabled(true);
        HtmlPage page = (HtmlPage) webClient.getPage("http://c3js.org");

        System.out.println("executing javascript");
    }
}
