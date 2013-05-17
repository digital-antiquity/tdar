package org.tdar.web.functional;

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
         * We define a specific binary so when running "headless" we can specify a PORT
         */
        FirefoxBinary fb = new FirefoxBinary();
        String xvfbPropsFile = System.getProperty("display.port");
        if (StringUtils.isNotBlank(xvfbPropsFile)) {
            fb.setEnvironmentProperty("DISPLAY", xvfbPropsFile);
        }
        driver = new FirefoxDriver(fb, new FirefoxProfile());
    }

    /*
     * Shutdown Selenium
     */
    @After
    public final void shutdownSelenium() {
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
    public String absoluteUrl(String path) {
        return String.format("%s%s", DEFAULT_BASE_URL, path);
    }

    public void gotoPage(String path) {
        String url = absoluteUrl(path);
        logger.debug("going to {}", url);
        driver.get(absoluteUrl(path));
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


    public WebElementSelection find(String selector) {
        return find(By.cssSelector(selector));
    }

    public WebElementSelection find(By by) {
        WebElementSelection selection = new WebElementSelection(driver.findElements(by));
        logger.debug("criteria:{}\t  size:{}", by, selection.size());
        return selection;
    }

    public WebElement findFirst(String selector) {
        return find(selector).iterator().next();
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

    public String getSource() {
        return driver.getPageSource();
    }

    public String getDom() {
        return find("body").getHtml();
    }

    public String getText() {
        return find("body").getText();
    }
}
