package org.tdar.dataone.service;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.utils.TestConfiguration;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

public abstract class AbstractWebTest {

    protected final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
    protected Page internalPage;
    protected HtmlPage htmlPage;
    static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    transient Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * override to test with different URL can use this to point at another
     * instance of tDAR instead of running "integration" tests.
     */
    public String getBaseUrl() {
        return CONFIG.getBaseUrl();
    }

    @Before
    public void prepare() {
        // FIXME: This is far less than ideal, but there's a problem with how
        // the MAC is handling memory
        // and appears to be 'leaking' with jwebunit and gooogle maps. Hence, we
        // need to disable javascript
        // testing on the mac :(
        // if (System.getProperty("os.name").toLowerCase().contains("mac os x"))
        initializeAndConfigureWebClient();
        // reset encoding error exclusions for each test
    }

    private void initializeAndConfigureWebClient() {
        webClient.getOptions().setUseInsecureSSL(true);
        // webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getCurrentWindow().setInnerWidth(1024);
        // if you enable css, then we need to figure out how to deal with CSS Responsive issues (hidden-phone-portrait) on UPLOAD Button 
        webClient.getOptions().setCssEnabled(false);
        CookieManager cookieMan = new CookieManager();
        cookieMan = webClient.getCookieManager();
        cookieMan.setCookiesEnabled(true);
        webClient.getOptions().setTimeout(0);
        // webClient.getOptions().setSSLClientCertificate(certificateUrl, certificatePassword, certificateType)
        webClient.setJavaScriptTimeout(0);
        webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {


            @Override
            public void loadScriptError(HtmlPage arg0, URL scriptUrl, Exception exception) {
                logger.error("load script Error: {} {}", scriptUrl, exception);
                
            }

            @Override
            public void malformedScriptURL(HtmlPage arg0, String scriptUrl, MalformedURLException exception) {
                logger.error("malformed url Error: {} {}", scriptUrl, exception);
                
            }

            @Override
            public void scriptException(HtmlPage scriptUrl, ScriptException exception) {
                logger.error("script exception: {} {}", scriptUrl, exception);
                
            }

            @Override
            public void timeoutError(HtmlPage arg0, long arg1, long arg2) {
                logger.error("timeout Error: {} ", arg0);
                
            }
        });
        webClient.setCssErrorHandler(new ErrorHandler() {
            @Override
            public void warning(CSSParseException exception) throws CSSException {
                String uri = exception.getURI();
                if (uri.contains(getBaseUrl()) && uri.contains("/css/")) {
                    logger.trace("CSS Warning:", exception);
                }
            }

            @Override
            public void fatalError(CSSParseException exception) throws CSSException {
                String uri = exception.getURI();
                if (uri.contains(getBaseUrl()) && uri.contains("/css/")) {
                    logger.warn("CSS Fatal Error:", exception);
                }
            }

            @Override
            public void error(CSSParseException exception) throws CSSException {
                String uri = exception.getURI();
                if (uri.contains(getBaseUrl()) && uri.contains("tdar")) {
                    String msg = String.format("CSS Error: %s ; message: %s line: %s ", exception.getURI(), exception.getMessage(), exception.getLineNumber());
                    logger.error(msg);
                    fail(msg);
                }
            }
        });
    }
    
    /**
     * Go to the specified page, with explicit assertions that the server did not return with a 500 error or contain any inline exception text
     * 
     * @param path
     * @return http return code (if no errors found, otherwise assertions fail and method does not return);
     */
    public int gotoPage(String path) {
        int statusCode = gotoPageWithoutErrorCheck(path);
        assertThat(statusCode, not(anyOf(is(SC_INTERNAL_SERVER_ERROR), is(SC_BAD_REQUEST))));
        return statusCode;
    }
    

    /**
     * Same as gotoPage(), but does not perform any assertions on the server response
     * 
     * @param path
     * @return http return code
     */
    public int gotoPageWithoutErrorCheck(String path) {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        changePage(getPage(path));
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        return internalPage.getWebResponse().getStatusCode();
    }


    public void changePage(Page page) {
        changePage(page, false);
    }

    public void changePage(Page page, boolean expectErrors) {
        if (page == null) {
            fail("changed to a null page for some reason");
            return;
        }
        internalPage = page;
        logger.info("CHANGING url TO: {}", internalPage.getUrl());
        if (internalPage instanceof HtmlPage) {
            htmlPage = (HtmlPage) internalPage;
        }
    }


    public String pathToUrl(String localPath) {
        String prefix = getBaseUrl();
        try {
            URL current = internalPage.getUrl();
            prefix = String.format("%s://%s:%s", current.getProtocol(), current.getHost(), current.getPort());
            logger.info("SETTING URL TO {}{}", prefix, localPath);
        } catch (Exception e) {
            logger.trace("{}", e);
        }

        if (localPath.startsWith("//")) {
            localPath = localPath.substring(1);
        }

        if (prefix.endsWith("/")) {
            while (localPath.startsWith("/")) {
                localPath = localPath.substring(1);
            }
        }

        String url = prefix + localPath;
        return url;
    }

    protected WebClient getWebClient() {
        return webClient;
    }

    public Page getPage(String localPath) {
        try {
            if (localPath.startsWith("http")) {
                return webClient.getPage(localPath);
            } else {
                String url = pathToUrl(localPath);
                return webClient.getPage(url);
            }
        } catch (Exception e) {
            logger.error("couldn't find page at {}", localPath, e);
        }
        return null;
    }

    
}
