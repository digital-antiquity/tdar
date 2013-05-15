package org.tdar.web.functional;

import static org.junit.Assert.fail;
import static org.tdar.TestConstants.DEFAULT_BASE_URL;

import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;

public abstract class FunctionalWebTestCase {

    WebDriver driver;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void before() throws MalformedURLException {
        /*
         * DesiredCapabilities abilities = DesiredCapabilities.firefox();
         * // abilities.setCapability("version", "16");
         * // abilities.setCapability("platform", Platform.WINDOWS);
         * abilities.setCapability("name", "Testing Selenium-2 Remote WebDriver");
         * 
         * driver = new RemoteWebDriver( new URL("http://localhost:4444/wd/hub"), abilities);
         * // driver = new RemoteWebDriver(remoteAddress, desiredCapabilities)
         */
        FirefoxBinary fb = new FirefoxBinary();
        String xvfbPropsFile = System.getProperty("display.port");
        if (StringUtils.isNotBlank(xvfbPropsFile)) {
            fb.setEnvironmentProperty("display.port", xvfbPropsFile);
        }
        driver = new FirefoxDriver(fb, new FirefoxProfile());
    }

    /*
     * Shutdown Selenium
     */
    @After
    public void after() {
        logger.debug("after");
        try {
            driver.close();
            driver = null;
        } catch (Exception ex) {
            logger.error("Could not close selenium driver: {}", ex);
        }
    }

    /*
     * createObsoluteUrl
     */
    public String url(String path) {
        return String.format("%s%s", DEFAULT_BASE_URL, path);
    }

    public void gotoPage(String path) {
        String url = url(path);
        logger.debug("going to {}", url);
        driver.get(url(path));
    }

    // TODO: find out if this is necessary for repeatrow buttons. Supposedly selenium will wait until domready is complete.
    public WebElement waitFor(String selector) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(selector)));
        WebElement result = null;
        if (!elements.isEmpty()) {
            result = elements.get(0);
        }
        return result;
    }

    /*
     * Find All?
     */
    public WebElementSelection find(String selector) {
        WebElementSelection selection = new WebElementSelection(driver.findElements(By.cssSelector(selector)));
        logger.debug("selector:{}\t size:{}", selector, selection.size());
        return selection;
    }

    /*
     * Find First?
     */
    public WebElement findOne(String selector) {
        return find(selector).iterator().next();
    }

    public void assertSelector(String selector) {
        if (find(selector).isEmpty()) {
            fail("could not find content on page with selector:" + selector);
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void login() {
        login(TestConstants.USERNAME, TestConstants.PASSWORD);
    }

    public void login(String username, String password) {
        gotoPage("/login");
        find("#loginUsername").sendKeys(username);
        find("#loginPassword").sendKeys(password);
        find("#btnLogin").click();
    }

    public void logout() {
        gotoPage("/logout");
    }

    //TODO: jtd, instead of textContains, sourceContains, sourceContains,  maybe just getText, getSource, getDom.  There are junit matchers for text containment.
    
    /*
     * Test TextContains
     */
    // case-insensitive search for body.innerText.
    public boolean textContains(String expected) {
        String text = find("body").getText().toLowerCase();
        String _expected = expected.toLowerCase();
        return text.contains(_expected);
    }
    
    //case-sensitive search for body.innerHtml
    public boolean domContains(String expected) {
        String dom = find("body").getHtml();
        return dom.contains(expected);
    }
    
    //case-sensitive search in page source
    public boolean sourceContains(String expected) {
        return driver.getPageSource().contains(expected);
    }
    
}
