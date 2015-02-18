package org.tdar.web.functional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.arquillian.phantom.resolver.ResolvingPhantomJSDriverService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.CrowdRestDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.UserRegistration;
import org.tdar.filestore.Filestore;
import org.tdar.utils.TestConfiguration;
import org.tdar.utils.TestConfiguration.OS;
import org.tdar.web.AbstractWebTestCase;
import org.tdar.web.functional.util.WebElementSelection;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public abstract class AbstractSeleniumWebITCase {

    public static final String AUTO_DOWNLOAD_MIME_TYPES = "application/pdf, text/csv, image/tiff, image/tif";
    //, application/xls, application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

    // default timeout used for waitFor()
    public static final int DEFAULT_WAITFOR_TIMEOUT = 20;

    protected static final TestConfiguration CONFIG = TestConfiguration.getInstance();

    // private TdarConfiguration tdarConfiguration = TdarConfiguration.getInstance();
    public static String PATH_OUTPUT_ROOT = "target/selenium";

    // {@link #getText} is a slow operation. cache the results until next pageload.
    private String cachedPageText = null;
    protected Dimension testSize = new Dimension(1024, 768);
    protected Dimension originalSize;

    private boolean screenshotsAllowed = true;
    // if true, ignore all javascript errors during page navigation events
    private boolean ignoreJavascriptErrors = false;
    // ignore javascript errors that match that match Patterns in this list
    private List<Pattern> jserrorIgnorePatterns = new ArrayList<>();
    private boolean ignoreModals = false;
    private WebDriver driver;
    private Browser currentBrowser;

    // prefix screenshot filename with sequence number, relative to start of test (no need to init in @before)
    private int screenidx = 0;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private boolean ignorePageErrorChecks;

    // predicate that returns true if document.readystate == "complete" (use with FluentWait)
    private Predicate<WebDriver> pageReady = new Predicate<WebDriver>() {
        @Override
        public boolean apply(@Nullable WebDriver webDriver) {
            String readyState = (String) ((JavascriptExecutor) webDriver).executeScript("return document.readyState");
            return "complete".equals(readyState);
        }
    };

    // predicate that returns true if document.readystate != "complete" (use with FluentWait)
    private Predicate<WebDriver> pageNotReady = Predicates.not(pageReady);

    /**
     * Custom "By" criteria for use with {@link #find(By)} - matches all elements that are referred by a label with the specified label text. e.g. <br>
     * <code>find(withLabel("First Name")).val("Bob")</code>
     * 
     * @param labelText
     *            text of the label associated with the element.
     * @return By locator instance
     */

    public AbstractSeleniumWebITCase() {
    }

    public void deleteUserFromCrowd(TdarUser user) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileReader(new File("src/test/resources/crowd.properties")));
        CrowdRestDao crowdRestDao = new CrowdRestDao(props);
        crowdRestDao.deleteUser(user);
    }

    /**
     * The {@link WebDriverEventListener#afterClickOn} element argument is invalid if the clicked-on element caused the browser to navigate to new page, so we
     * we can't inspect it. So we use this field to signal whether afterClickOn() should call afterPageChange()
     */
    private Set<WebElement> clickElems = new HashSet<>();

    private WebDriverEventListener eventListener = new WebDriverEventListener() {
        @Override
        public void afterNavigateTo(String url, WebDriver driver) {
            afterPageChange();
        }

        @Override
        public void beforeNavigateBack(WebDriver driver) {
            beforePageChange();
        }

        @Override
        public void afterNavigateBack(WebDriver driver) {
            afterPageChange();
        }

        @Override
        public void beforeNavigateForward(WebDriver driver) {
            beforePageChange();
        }

        @Override
        public void afterNavigateForward(WebDriver driver) {
            afterPageChange();
        }

        @Override
        public void beforeFindBy(By by, WebElement element, WebDriver driver) {
        }

        @Override
        public void afterFindBy(By by, WebElement element, WebDriver driver) {
        }

        @Override
        public void beforeChangeValueOf(WebElement element, WebDriver driver) {
        }

        @Override
        public void afterChangeValueOf(WebElement element, WebDriver driver) {
        }

        @Override
        public void beforeScript(String script, WebDriver driver) {
        }

        @Override
        public void afterScript(String script, WebDriver driver) {
        }

        @Override
        public void onException(Throwable throwable, WebDriver driver) {
            if (!throwable.getMessage().contains("n is null")) {
                logger.error("hey there was an error", throwable);
            }
            takeScreenshot("ERROR " + throwable.getClass().getSimpleName());
        }

        @Override
        public void beforeClickOn(WebElement element, WebDriver driver) {
            if (elementCausesNavigation(element)) {
                clickElems.add(element);
                beforePageChange();
            }
        }

        @Override
        public void afterClickOn(WebElement element, WebDriver driver) {
            // if beforeClickOn() put this element here, we are on the other side of page change.
            if (clickElems.remove(element)) {
                // FIXME: this fails, I think because the page has not finished rendering? commenting out for now...
                // afterPageChange();
            }
        }

        private boolean elementCausesNavigation(WebElement element) {
            String tag = element.getTagName();
            return (tag.equals("a"))
                    || (tag.equals("input") && "submit".equals(element.getAttribute("type")))
                    || (tag.equals("button") && "submit".equals(element.getAttribute("type")));
        }

        @Override
        public void beforeNavigateTo(String url, WebDriver driver) {
            beforePageChange();
        }
    };

    /**
     * This event fires after the webdriver executes a command that will cause navigation (e.g. link click, back button, gotoPage())
     * but before the navigation occurs.
     */
    protected void beforePageChange() {
        takeScreenshot();
        reportJavascriptErrors();
        cachedPageText = null;
    }

    /**
     * This event fires after the browser navigates to a location following a command from webdriver (e.g. link click, back button, gotoPage())
     */
    protected void afterPageChange() {
        if (ignoreModals) {
            dismissModal();
        }
        applyEditPageHacks();
        takeScreenshot();
        if (!isIgnorePageErrorChecks()) {
            String text = getText();
            String lcText = text.toLowerCase();
            for (String err : AbstractWebTestCase.errorPatterns) {
                if (text.contains(err) || lcText.contains(err)) {
                    fail("page has '" + err + "'");
                }
            }
            setIgnorePageErrorChecks(false);
        }
    }

    /**
     * On tdar edit pages, the floating page navigation bar may obscure an underlying form element. In practice, a user will know to scroll until
     * the element is visible. However, selenium will deem the element unclickable and throw an exception if we try to modify it. This hack
     * removes the floating nav header to avoid that scenario.
     *
     * //fixme: instead, use fluentWait with ElementToBeVisible condition & sleeper that scrolls up 5px per sleep
     */
    protected void applyEditPageHacks() {
        try {
            executeJavascript("var n=document.getElementById('subnavbar');n.parentNode.removeChild(n)");
        } catch (Exception ignored) {
        }
    }

    protected enum Browser {
        FIREFOX, CHROME, SAFARI, IE, PHANTOMJS;
    }

    @Before
    public void before() throws IOException {
        /*
         * We define a specific binary so when running "headless" we can specify a PORT
         */
        String fmt = " ***   RUNNING TEST: {}.{}() ***";
        logger.info(fmt, getClass().getSimpleName(), testName.getMethodName());
        // typekit & google-analytics errors may occur on pretty much any page and are (relatively) harmless, so we ignore them by default
        getJavascriptIgnorePatterns().add(TestConstants.REGEX_TYPEKIT);
        getJavascriptIgnorePatterns().add(TestConstants.REGEX_GOOGLE_ANALYTICS);
        WebDriver driver = null;
        Browser browser = Browser.FIREFOX;
        String xvfbPort = System.getProperty("display.port");
        String browser_ = System.getProperty("browser");
        if (StringUtils.isNotBlank(browser_)) {
            try {
                browser = Browser.valueOf(browser_);
                logger.debug("set browser to: {}", browser);
            } catch (Exception e) {
                logger.error("{}", e);
            }
        }
        Map<String, String> environment = new HashMap<String, String>();
        if (StringUtils.isNotBlank(xvfbPort)) {
            environment.put("DISPLAY", xvfbPort);
        }
        currentBrowser = browser;
        switch (browser) {
            case FIREFOX:
                FirefoxBinary fb = new FirefoxBinary();
                for (String key : environment.keySet()) {
                    fb.setEnvironmentProperty(key, environment.get(key));
                }
                FirefoxProfile profile = new FirefoxProfile();
                if (TestConfiguration.isMac()) {
                    profile.setPreference("focusmanager.testmode", true);
                }
                profile.setPreference("browser.helperApps.alwaysAsk.force", false);
			    profile.setPreference("browser.download.folderList",2);
			    profile.setPreference("browser.download.manager.showWhenStarting",false);
                profile.setPreference("browser.download.manager.showAlertOnComplete",false);
                profile.setPreference("browser.helperApps.alwaysAsk.force", false);
				profile.setPreference("browser.helperApps.neverAsk.saveToDisk",AUTO_DOWNLOAD_MIME_TYPES);
				profile.setPreference("pdfjs.disabled", true);
				// Use this to disable Acrobat plugin for previewing PDFs in Firefox (if you have Adobe reader installed on your computer)
				profile.setPreference("plugin.scan.Acrobat", "99.0");
				profile.setPreference("plugin.scan.plid.all", false);
				
				
				//			    profile.setPreference("browser.download.dir","c:\\downloads");
                driver = new FirefoxDriver(fb, profile);
                break;
            case CHROME:
                // http://peter.sh/experiments/chromium-command-line-switches
                /* yes, this is ugly */
                /* ubuntu install instructions http://www.liberiangeek.net/2011/12/install-google-chrome-using-apt-get-in-ubuntu-11-10-oneiric-ocelot/ */
                File app = new File(CONFIG.getChromeDriverPath());
                logger.info("usign app: {} ", app);
                ChromeDriverService service = new ChromeDriverService
                        .Builder().usingDriverExecutable(app).usingPort(9515).withEnvironment(environment).build();
                ChromeOptions copts = new ChromeOptions();
                // copts.setExperimentalOption("autofill.enabled",false);

                // turn off autocomplete: https://code.google.com/p/chromedriver/issues/detail?id=333
                File dir = new File(PATH_OUTPUT_ROOT, "profiles/chrome");
                // File dir = new File("src/test/resources/c1");
                String profilePath = dir.getAbsolutePath();
                logger.debug("chrome profile path set to: {}", profilePath);

                // http://peter.sh/experiments/chromium-command-line-switches/
                // ignore-certificate-errors ?
                copts.addArguments(
                        "binary=" + CONFIG.getChromeApplicationPath(), // NOTE BINARY is needed for LINUX, may not be for Mac or Windows
                        "user-data-dir=" + profilePath, // use specific profile path (random by default?)
                        // "bwsi" //browse without signin
                        "noerrdialogs");
                driver = new ChromeDriver(service, copts);
                service.start();
                break;
            case IE:
                System.setProperty("webdriver.ie.driver", CONFIG.getIEDriverPath());
                DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
                driver = new InternetExplorerDriver(configureCapabilities(ieCapabilities));
                driver.manage().timeouts().implicitlyWait(90, TimeUnit.SECONDS);
                if (TdarConfiguration.getInstance().isHttpsEnabled()) {
                    fail("please disable https before testing this");
                }
                break;
            case PHANTOMJS:
                driver = new PhantomJSDriver(
                        ResolvingPhantomJSDriverService.createDefaultService(), // service resolving phantomjs binary automatically
                        configureCapabilities(DesiredCapabilities.phantomjs()));
                break;
                default:
                    break;
        }
        EventFiringWebDriver eventFiringWebDriver = new EventFiringWebDriver(driver);
        eventFiringWebDriver.register(eventListener);

        this.driver = eventFiringWebDriver;
    }

    private Capabilities configureCapabilities(DesiredCapabilities caps) {
        caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        caps.setCapability("initialBrowserUrl", "about:blank");
        return caps;
    }

    @Rule
    public TestName testName = new TestName();

    private static boolean reindexed = false;

    /*
     * Shutdown Selenium
     */
    @After
    public final void shutdownSelenium() {
        try {
            driver.quit();
        } catch (UnhandledAlertException uae) {
            logger.error("alert modal present when trying to close driver: {}", uae.getAlertText());
            driver.switchTo().alert().dismiss();
            driver.quit();
        } catch (Exception ex) {
            logger.error("Could not close selenium driver: {}", ex);
        }
        driver = null;
        String fmt = " *** COMPLETED TEST: {}.{}() ***";
        logger.info(fmt, getClass().getCanonicalName(), testName.getMethodName());
        getJavascriptIgnorePatterns().clear();
    }

    protected void takeScreenshot() {
        takeScreenshot(null);
    }

    protected void takeScreenshot(String filename) {
        if (!screenshotsAllowed) {
            return;
        }
        if (screenidx > TestConstants.MAX_SCREENSHOTS_PER_TEST) {
            return;
        }

        screenidx++;
        // this is necessary since we take since onException() calls takeScreenshot()
        screenshotsAllowed = false;
        try {
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            // Now you can do whatever you need to do with it, for example copy somewhere
            File dir = new File("target/screenshots/" + getClass().getSimpleName() + "/" + testName.getMethodName());
            dir.mkdirs();
            String finalFilename = screenshotFilename(filename, "png");
            logger.debug("saving screenshot: dir:{}, name:", dir, finalFilename);
            FileUtils.copyFile(scrFile, new File(dir, finalFilename));
        } catch (Exception e) {
            logger.error("could not take screenshot", e);
        } finally {
            screenshotsAllowed = true;
        }

    }

    private String screenshotFilename(String filename, String ext) {
        // try to use url path for title otherwise testname
        String name = null;
        try {
            URL url = new URL(getDriver().getCurrentUrl());
            name = url.getPath();
            if ("".equals(name) || "/".equals(name)) {
                name = "(root)";
            }
        } catch (MalformedURLException ignored) {
            name = testName.getMethodName();
        }

        if (filename != null) {
            name = filename;
        }

        String fullname = String.format("%03d-%s.%s", screenidx, Filestore.BaseFilestore.sanitizeFilename(name), ext);
        return fullname;
    }

    /**
     * return absolute url based upon context (i.e. base url) and path.
     * 
     * @param base
     * @param path
     * @return
     */
    public String absoluteUrl(String base, String path) throws MalformedURLException {
        String abs = null;
        URL context = new URL(base);
        URL url = new URL(context, path);
        abs = url.toString();
        return abs;
    }

    /**
     * @return string representing a fully qualified URL, based upon TdarConfiguration settings
     */
    public String getBaseUrl(boolean https) {
        String scheme = https ? "https" : "http";
        String host = CONFIG.getHostName();
        int port = https ? CONFIG.getHttpsPort() : CONFIG.getPort();
        String url = String.format("%s://%s:%s/", scheme, host, port);
        return url;
    }

    public String getBaseUrl() {
        return getBaseUrl(CONFIG.isHttpsEnabled());
    }

    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    private static final String CONTEXTUAL_BASE_URL_INDICATOR = "~";

    /**
     * Navigate to a page using specified path. The type of path determines the destination URL
     * 
     * <pre>
     *     - if path is fully-qualified,  this becomes the destination URL
     *     - if path is relative,  the destination is resolved by using the *default* base url and the path
     *     - if path is relative and is prefixed with "~", this method uses the *current*
     *       location of the webdriver as the base url, and resolves the destination using the base URL and the path
     * </pre>
     * 
     * This method fails the current test if the destination URL is malformed.
     * 
     * @param path
     *            string representing relative path, "~" + path, or fully-qualified URL
     */
    public void gotoPage(String path) {
        if (path.startsWith(CONTEXTUAL_BASE_URL_INDICATOR)) {
            // assertCurrentUrl();
            gotoPage(getCurrentUrl(), path.substring(1));
        } else {
            gotoPage(getBaseUrl(), path);
        }
    }

    /**
     * 
     * @param base
     *            fully-qualified URL to use as the "base" URL, if path is relative.
     * @param path
     *            relative path or fully qualified URL
     */
    public void gotoPage(String base, String path) {
        try {
            String url = absoluteUrl(base, path);
            logger.debug("going to: {}", url);
            driver.get(url);
            waitForPageload();
        } catch (MalformedURLException ex) {
            String err = String.format("bad url:: base:%s\tpath:%s", base, path);
            logger.error(err, ex);
            fail(err);
        }
    }


    /**
     * Wait for specified css selector to match at least one element. Uses default timeout.
     * @param cssSelector
     * @return
     */
    public WebElementSelection waitFor(String cssSelector) {
        return waitFor(By.cssSelector(cssSelector));
    }

    /**
     * Wait for specified css selector to match at least one element within specified timeout.
     *
     * @param cssSelector
     * @param timeoutInSeconds
     * @return elements matched by specified selector
     */
    public WebElementSelection waitFor(String cssSelector, int timeoutInSeconds) {
        //FIXME: rewrite in terms of waitFor(ExpectedCondition, int)
        WebDriverWait wait = new WebDriverWait(driver, timeoutInSeconds);
        List<WebElement> elements =  wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(cssSelector)));
        WebElementSelection selection = new WebElementSelection(elements, driver);
        return selection;
    }

    /**
     * Wait for specified number of seconds. Use this as a last resort. Consider using {@link #waitFor(String)} or {@link #waitForPageload()}.
     *
     * @Deprecated 
     * Honestly, the author of this API does not like to engage in hyperbole, but if you use this method there is a strong likelyhood
     * that you are a terrible person.  LOOK AWAY.
     * 
     * @param timeInSeconds
     *            seconds to wait before timeout
     */
    @Deprecated
    public void waitFor(int timeInSeconds) {
        try {
            Thread.sleep(timeInSeconds * TestConstants.MILLIS_PER_SECOND);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Wait for at least one element matched by the specified locator. Uses default timeout.
     * @param elementsLocator A selenium element "locator",  such as {@link By#xpath(String)}  or {@link By#cssSelector(String)}
     * @return the matched elements wrapped in a WebElementSelection.
     */
    public WebElementSelection waitFor(By elementsLocator) {
        List<WebElement> elements =  waitFor(ExpectedConditions.presenceOfAllElementsLocatedBy(elementsLocator));
        WebElementSelection selection = new WebElementSelection(elements, driver);
        return selection;
    }

    /**
     * Wait for the specified expected condition. Uses default timeout.
     * @param expectedCondition
     * @param <T>
     * @return
     */
    public<T> T waitFor(ExpectedCondition<T> expectedCondition) {
        return waitFor(expectedCondition, DEFAULT_WAITFOR_TIMEOUT);
    }

    /**
     * Wait for the specified expected condition within the specified timeout (in seconds)
     * @param expectedCondition ExpectedCondition predicate (e.g. {@link ExpectedConditions#alertIsPresent}, {@link ExpectedConditions#presenceOfAllElementsLocatedBy(org.openqa.selenium.By)}
     * @param timeoutInSeconds amount of time that this method suppresses ElementNotFoundException
     * @param <T> object returned by the ExpectedCondition
     * @return
     */
    public<T> T waitFor(ExpectedCondition<T> expectedCondition, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, timeoutInSeconds);
        T value = wait.until(expectedCondition);
        return value;
    }

    /**
     * Return WebElementSelection containing all elements mathing the specified css selector.
     * 
     * @param selector
     *            css selector
     * @return WebElementSelection containing zero-or-more elments
     */
    public WebElementSelection find(String selector) {
        return find(By.cssSelector(selector));
    }

    /**
     * Return WebElementSelection containing all elements mathing the specified css selector.
     * 
     * @param by
     * @return
     */
    public WebElementSelection find(By by) {
        WebElementSelection selection = new WebElementSelection(driver.findElements(by), driver);
        logger.trace("criteria:{}\t  size:{}", by, selection.size());
        return selection;
    }

    // FIXME: select() seems more appropriate, given the ways you can select stuff. Or, since we're aping jquery.. just $()?
    /**
     * Create a selection out of one or more.
     * 
     * @param elems
     *            WebElement objects
     * @return
     */
    public WebElementSelection find(WebElement... elems) {
        return new WebElementSelection(Arrays.asList(elems), driver);
    }

    public WebElement findFirst(String selector) {
        return find(selector).iterator().next();
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void login() {
        login(CONFIG.getUsername(), CONFIG.getPassword());
    }

    public void login(String username, String password) {

        gotoPage("/login");
        find("#loginUsername").sendKeys(username);
        find("#loginPassword").sendKeys(password);
        find("#btnLogin").click();
        waitForPageload();
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
        if (cachedPageText == null) {
            logger.trace("getting body.innerText for url:{}", getDriver().getCurrentUrl());
            WebElement body = waitFor("body").first();
            cachedPageText = body.getText();
        }
        return cachedPageText;
    }

    @SuppressWarnings("unchecked")
    // this is a convenience so that callers don't have to cast. It's probably a bad idea.
    /**
     * execute a snippet of javascript in an anonymous function.  if your snippet returns a value, Selenium will attempt to cast the most "appropriate"
     * java type (String, Double, Integer, etc)  or a WebElement if you return a DOM node.  
     * @param functionBody
     * @param arguments arguments applied to the anonymous function. you can reference them in your snippet using  javascript's 
     * contextual <code>arguments</code> object.
     * @return selenium's best approximation of the value returned by your snippet, if it exists.
     */
    public <T> T executeJavascript(String functionBody, Object... arguments) {
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        Object result = executor.executeScript(functionBody, arguments);
        return (T) result;
    }

    /**
     * same as {@link #executeJavascript(String, Object...) but with exceptions suppressed}
     * 
     * @param functionBody
     * @param arguments
     * @return
     */
    public <T> T executeJavascriptSilently(String functionBody, Object... arguments) {
        T result = null;
        try {
            result = executeJavascript(functionBody, arguments);
        } catch (Throwable ignored) {
            logger.debug("ignored JS exception: {}", ignored);
        }
        return result;
    }

    // FIXME: implement someday. the tricky part is supporting nested properties e.g. "elem.style.position", especially when property doesn't exist yet
    public void setAttribute(WebElement elem, String property, Object value) {
        throw new RuntimeException("no");
    }

    /**
     * @param fieldName
     * @return true if fieldname follows struts indexed name convention (e.g. person[3].id)
     */
    public boolean isIndexedField(String fieldName) {
        String indexedNamePattern = "(.+)\\[(\\d+)\\](\\..+)?";
        return fieldName.matches(indexedNamePattern);
    }

    /**
     * @param fieldName
     *            name attribute value of field. which is expected to follow the struts naming pattern for collections
     * @return
     */
    private WebElement getZerothElement(String fieldName) {
        String indexedNamePattern = "(.+)\\[(\\d+)\\](\\..+)?";
        String zerothFieldName = fieldName.replaceAll(indexedNamePattern, "$1[0]$3");
        logger.debug("old name:\t{}", fieldName);
        logger.debug("new name:\t{}", zerothFieldName);
        WebElementSelection selection = find(By.name(zerothFieldName));
        if (selection.isEmpty()) {
            return null;
        } else {
            return selection.first();
        }
    }

    /**
     * Return a element with the specified name, which is assumed to follow tDAR's 'repeatable' element convention.
     * If the element doesn't exist, this method tries to locate the 'add another row' button and clicks it until an element with the specified
     * field name exists.
     * 
     * @param fieldName
     *            name attribute of the element to find.
     * @return The element with the specified name attribute, or null if this method could neither find the element or implicitly create it.
     */
    public WebElement findOrCreateIndexedField(String fieldName) {
        WebElement result = null;
        WebElementSelection elem = find(By.name(fieldName));
        if (elem.isEmpty()) {
            WebElement zeroElem = getZerothElement(fieldName);
            if (zeroElem == null) {
                fail(fieldName + " is null");
            }
            String repeatLastRowId = find(zeroElem).parentsWithClass("repeatLastRow").getAttribute("id");
            String buttonSelector = "#" + repeatLastRowId + " + .add-another-control button";
            // selector for button after container (e.g. "#resourceNotesSection + .add-another-control button")
            WebElement button = find(buttonSelector).first();
            // FIXME: create private method that takes indexed fieldname and returns index as int (or -1 if not a valid fieldname)
            int attempts = clickElementUntil(button, By.name(fieldName), 100);
            result = find(By.name(fieldName)).first();
        } else {
            result = elem.first();
        }
        return result;
    }

    /**
     * Click an element zero or more times until another element is present on the page as determined by the specified By criteria (for example, clicking a
     * tdar "add another item" button until the page generates 10 blank person records)
     * 
     * @param findBy
     *            the criteria to use to find a matching element on the page
     * @param max
     *            the maximum number of times this method should click every element if a match has not been found.
     * @return the number of clicks performed (per selected item), or -1 if a matching element was never found.
     */
    public int clickElementUntil(WebElement element, By findBy, int max) {
        int i = 0;
        while ((find(findBy).size() == 0) && (i < max)) {
            element.click();
            i++;
        }
        return i;
    }

    public void loginAdmin() {
        login(CONFIG.getAdminUsername(), CONFIG.getAdminPassword());
    }

    public void loginEditor() {
        login(CONFIG.getEditorUsername(), CONFIG.getEditorPassword());
    }

    public static boolean hasReindexedOnce() {
        return AbstractSeleniumWebITCase.reindexed;
    }

    public static void setReindexed(boolean val) {
        AbstractSeleniumWebITCase.reindexed = val;
    }

    public static void setRequiresIndexing(boolean val) {
        AbstractSeleniumWebITCase.reindexed = !val;
    }

    public boolean testRequiresLucene() {
        return false;
    }
    
    public void reindex() {
        logout();
        loginAdmin();
        gotoPage("/admin/searchindex/build");
        find("#idxBtn").click();
        waitFor("#spanDone", 120);
        logout();
        AbstractSeleniumWebITCase.setReindexed(true);
    }

    /**
     * Submit a "tDAR edit page" style form (if no javascript errors since last pageload)
     * 
     * Considerations: this function assumes a the layout of a typical edit page on tDAR. For example, it expects
     * the submit button ID value is "submitButton".
     */
    public void submitForm() {
        reportJavascriptErrors();
        submitForm("#submitButton,.submitButton,form input[type=submit]");
    }

    /**
     * Click a button with the assumption that it causes a form submission
     * 
     * @param cssSelector
     */
    public void submitForm(String cssSelector) {
        WebElementSelection buttons = find(cssSelector);
        buttons.first().click();
        waitForPageload();
    }

    /**
     * Block until document loaded (readystate === true). This is usually unnecesssary since most click() actions
     * block anyway. Use it for situations where an event handler causes navigation (e.g. jquery validate success)
     * 
     * @param timeout
     *            amount of time(in seconds) to wait before throwing timeout exception
     */
    private void waitForPageload(int timeout) {
        // if called too soon, page navigation might not have happened yet - give it a second.
        if (pageReady.apply(getDriver())) {
            try {
                WebDriverWait wait = new WebDriverWait(getDriver(), 1, 100);
                wait.until(pageNotReady);
            } catch (Exception ignored) {
            }

        }

        WebDriverWait wait = new WebDriverWait(getDriver(), timeout);
        wait.until(pageReady);
    }

    public void waitForPageload() {
        waitForPageload(60);
        clearPageCache();
    }

    public List<String> getJavascriptErrors() {
        List<String> errors = executeJavascript("return window.__errorMessages;");
        if (errors == null) {
            errors = Collections.emptyList();
        }
        return errors;
    }

    public final void setIgnoreJavascriptErrors(boolean ignoreJavascriptErrors) {
        this.ignoreJavascriptErrors = ignoreJavascriptErrors;
    }

    public final boolean getIgnoreJavascriptErrors() {
        return ignoreJavascriptErrors;
    }

    // message: "errorEvent::" + (evt.message || "(no error message)"),
    // filename: evt.filename || "(no filename - probably script from remote host)",
    // line: evt.lineno,
    // tag: "(inline script)"
    /**
     * If any javascript errors have occured since last pageload, log them and (if ignoreJavascriptErrors==false) fail the test.
     * 
     * Note: most actions that cause page navigation will implicitly callreportJavascriptErrors() anyway, such as formSubmit(), gotoPage(), and click events on
     * links & buttons. An example of when you might wish to explicitly call this method is when you expect a javascript function to modify the
     * <code>Window.location</code> property, or if you call {@link WebElement#submit()} rather than submitForm();
     */
    public void reportJavascriptErrors() {
        List<String> errors = getJavascriptErrors();
        List<String> legitErrors = new ArrayList<>();
        logger.trace("javascript error report for {}", driver.getCurrentUrl());

        for (String error : errors) {
            if (isLegitJavascriptError(error)) {
                logger.error("javascript error: {}", error);
                legitErrors.add(error);
            } else {
                logger.info("javascript error(ignored): {}", error);
            }
        }
        if (!ignoreJavascriptErrors && !legitErrors.isEmpty()) {
            fail("Encountered javascript errors on page: " + driver.getCurrentUrl() + "\r\n [" + errors + "]");
        }
    }


    @Deprecated
    //FIXME: api cleanup: hamcrest assertions would work better here
    protected boolean sourceContains(String substring) {
        return getSource().contains(substring);
    }

    //FIXME: api clianup: hamcrest assertions would work better here
    @Deprecated
    protected boolean textContains(String substring) {
        return getText().toLowerCase().contains(substring.toLowerCase());
    }

    public void reindexOnce() {
        if (!hasReindexedOnce()) {
            reindex();
        }
    }

    public boolean getScreenshotsAllowed() {
        return screenshotsAllowed;
    }

    public void setScreenshotsAllowed(boolean val) {
        logger.debug("screenshots allowed:{}", val);
        screenshotsAllowed = val;
    }

    /**
     * Dismiss (by clicking OK/Accept) if browser is displaying a modal
     * 
     * @return true if modal was present and was dismissed, otherwise false
     */
    public boolean dismissModal() {
        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException ignored) {
            return false;
        } catch(WebDriverException wde)  {
            //try a few more times with kludgey version of dismissModal
            return dismissModal(10);
        }
        return true;
    }

    /**
     * Workaround for Selenium <a href="https://code.google.com/p/selenium/issues/detail?id=3544">Issue 3544: WebDriver randomly fails to accept javascript alert windows (timing problem)</a>
     *
     * Keep trying to accept modal dialog every 100ms until successful.  Give up after specified attempts .
     * This method assumes a modal is present and will give you a weird result if modal doesn't exist.
     *
     * @param attempts number of attempts before giving up
     * @return true if accept worked.
     */
    private boolean  dismissModal(int attempts) {
        boolean successful = false;
        for(int i = 1; i <= attempts; i++) {
            logger.debug("dissmiss modal:  attempt {} of {}",i , attempts);
            try {
                Alert statusConfirm = driver.switchTo().alert();
                statusConfirm.accept();
                successful = true;
            } catch(WebDriverException ignored) {
                logger.info("exception while trying to dismiss modal dialog:  attempt {} of {}",i , attempts);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException alsoIgnored) {}
            }
            if(successful) break; //don't judge me.
        }
        return successful;
    }




    /**
     * when indicates whether the test should (try to) ignore modal windows that appear during the course of test.
     * 
     * @return
     */
    public boolean isIgnoreModals() {
        return this.ignoreModals;
    }

    /**
     * when set, this test case will attempt to automatically dismiss any modal windows encountered during navigation
     * 
     * @param ignoreModals
     */
    public void setIgnoreModals(boolean ignoreModals) {
        this.ignoreModals = ignoreModals;
    }

    /**
     * convenience method to fill out form fields in bulk
     * e.g. fillout(name, val, name, val, name, val)
     * 
     * @param namevals
     * @return WebElementSelection containing the fields referenced in the list
     */
    public WebElementSelection fillout(Object... namevals) {
        WebElementSelection selection = new WebElementSelection(new LinkedList<WebElement>(), driver);
        assertEquals("name/value pair array must be even", namevals.length % 2, 0);
        for (int i = 0; i < namevals.length; i += 2) {
            String key = (String) namevals[i];
            Object val = namevals[i + 1];
            if (val == null) {
                val = "";
            }
            WebElementSelection elems = find(key);
            elems.val(val.toString());
            selection.add(elems);
        }

        return selection;
    }

    public void uploadFile(FileAccessRestriction restriction, File uploadFile) {
        find("#inputMethodId").find("[value=file]").click();
        find("#fileUploadField").sendKeys(uploadFile.getAbsolutePath());
    }

    public void uploadFileAsync(FileAccessRestriction restriction, File uploadFile) {
        waitFor( elementToBeClickable( id("fileAsyncUpload")));
        find("#fileAsyncUpload").sendKeys(uploadFile.getAbsolutePath());
        waitFor(".delete-button");
        find("#proxy0_conf").val(restriction.name());
    }

    protected void prepIndexedFields(Collection<String> fieldNames) {
        for (String fieldName : fieldNames) {
            if (isIndexedField(fieldName)) {
                findOrCreateIndexedField(fieldName);
            }
        }
    }

    /**
     * jquery treeview plugin has no method for "expand-all" because it is horrible.
     */
    protected void expandAllTreeviews() {
        int giveupCount = 0;
        // yes, you really have to do this. the api has no "expand all" method.
        while (!find(".expandable-hitarea").visibleElements().isEmpty() && (giveupCount++ < 10)) {
            find(".expandable-hitarea").visibleElements().click();
        }
        assertTrue("trying to expand all listview subtrees", giveupCount < 10);
    }

    protected void addPersonWithRole(Person p, String prefix, ResourceCreatorRole role) {
        setFieldByName(prefix + ".person.firstName", p.getFirstName());
        setFieldByName(prefix + ".person.lastName", p.getLastName());
        setFieldByName(prefix + ".person.email", p.getEmail());
        setFieldByName(prefix + ".person.institution.name", p.getInstitutionName());
        setFieldByName(prefix + ".role", role.name());

        // FIXME: wait for the autocomplete popup (autocomplete not working in selenium at the moment)
        // waitFor(".ui-menu-item a").click();
    }

    protected void addInstitutionWithRole(Institution p, String prefix, ResourceCreatorRole role) {
        setFieldByName(prefix + ".institution.name", p.getName());
        setFieldByName(prefix + ".role", role.name());

        // FIXME: wait for the autocomplete popup (autocomplete not working in selenium at the moment)
        // waitFor(".ui-menu-item a").click();
    }

    protected void setFieldByName(String fld, String value) {
        if (StringUtils.isNotBlank(value)) {
            find(By.name(fld)).val(value);
        }

    }

    /**
     * attempt populate a autocomplete-style control by performing the actions necessary to spawn the autocomplete
     * menu and then choosing (clicking) on the desired choice). This method was written with jQuery autocomplete
     * plugin in mind, and will look for certain elements and class-names used by that plugin.
     * 
     * @param field
     *            an autocomplete field (e.g. a text form field)
     * @param textEntry
     *            the characters that the method will "type" in the text field in order to prompt the autocomplete
     *            options to appear
     * @param partialMenuItemTest
     *            a string containing the text to look for in the menu items. This method will
     *            click on the first menu item with a partial match (case-insensitive).
     * @return true if the method found the menu item and clicked on it, false if the menu-item was not found
     * 
     * @throws org.openqa.selenium.TimeoutException
     *             If the method timed out while waiting for the autocomplete
     *             menu to appear.
     * 
     */
    public boolean selectAutocompleteValue(WebElement field, String textEntry, String partialMenuItemTest, String idSelector) {
        field.sendKeys(textEntry);
        waitFor(TestConfiguration.getInstance().getWaitInt()); // kludge
        field.sendKeys(Keys.ARROW_DOWN);
        WebElementSelection menuItems = null;

        //fixme: this  should be unnecessary -- WebDriverWait repeatedly executes predicate at set interval before timing out
        for (int i = 0; i < 30; i++) {
            if ((menuItems == null) || menuItems.isEmpty()) {
                try {
                    menuItems = waitFor("ul.ui-autocomplete li.ui-menu-item", TestConfiguration.getInstance().getWaitInt());
                } catch (TimeoutException tex) {
                    // ignore
                }
            }
        }
        if ((menuItems == null) || menuItems.isEmpty()) {
            fail("could not set value on  " + field + " because autocomplete never appeared or was dismissed too soon");
        }

        logger.trace("menuItems: {} ({})", menuItems.getHtml(), menuItems.size());
        String partialText = partialMenuItemTest.toLowerCase();
        WebElement firstMatch = null;
        for (WebElement menuItem : menuItems) {
            String text = menuItem.getText().toLowerCase();
            WebElementSelection wes = new WebElementSelection(menuItem, getDriver());
            String html = wes.getHtml();
            if (text.contains(partialText) || (StringUtils.isNotBlank(idSelector) && StringUtils.containsIgnoreCase(html, idSelector))) {
                firstMatch = menuItem;
                break;
            }
            logger.info(text);
        }

        boolean wasFound = firstMatch != null;
        logger.info("match: {} ", firstMatch);
        if (wasFound) {
            (firstMatch.findElement(By.tagName("a"))).click();
            waitFor(TestConfiguration.getInstance().getWaitInt());
        }
        return wasFound;
    }

    /**
     * Convenience wrapper for {@link #setJavascriptIgnorePatterns(List<Pattern>)}.
     * 
     * @param patterns
     */
    public final void setJavascriptIgnorePatterns(Pattern... patterns) {
        List<Pattern> _patterns = new ArrayList<>(Arrays.asList(patterns));
        setJavascriptIgnorePatterns(_patterns);
    }

    /**
     * Set the list of javascript error ignore patterns. The test will check for outstanding javascript errors whenever the test detects a page
     * navigation event. If {@link #getIgnoreJavascriptErrors()} is <code>false</code> and the test detects javascript error messages that are not matched
     * by the list of patterns, this test will call {@link org.junit.Assert#fail()}
     * 
     * @param patterns
     */
    public final void setJavascriptIgnorePatterns(List<Pattern> patterns) {
        jserrorIgnorePatterns = patterns;
    }

    public final List<Pattern> getJavascriptIgnorePatterns() {
        return jserrorIgnorePatterns;
    }

    // return true if this is a legit error (i.e something we aren't ignoring)
    public boolean isLegitJavascriptError(String error) {
        for (Pattern pattern : jserrorIgnorePatterns) {
            if (pattern.matcher(error).find()) {
                return false;
            }
        }
        return true;
    }

    public void addAuthuser(String nameField, String selectField, String name, String email, String selector, GeneralPermissions permissions) {

        WebElement blankField = find(By.name(nameField)).first();
        if (!selectAutocompleteValue(blankField, name, email, selector)) {
            String fmt = "Failed to add authuser %s because selenium failed to select a user from the autocomplete " +
                    "dialog.  Either the autocomplete failed to appear or an appropriate value was not in the " +
                    "menu.";
            fail(String.format(fmt, email));
        }
        find(By.name(selectField)).val(permissions.name());

    }

    //FIXME: I originally exposed this because getText() was such a costly method,  but this 'fix' causes more problems in the form of returning outdated page text
    /**
     * clear the cached result of getText(). The test updates the pageText whenever it detects a navigation event.  However
     * If you invoke navigation via javascript, it may be necessary to manually clear it.
     */
    public void clearPageCache() {
        cachedPageText = null;
    }

    public boolean isIgnorePageErrorChecks() {
        return ignorePageErrorChecks;
    }

    public void setIgnorePageErrorChecks(boolean ignorePageErrorChecks) {
        this.ignorePageErrorChecks = ignorePageErrorChecks;
    }

    /**
     * Tell the webdriver to switch to the next window amongst the current list of windows. The next window is determined by the ascending sort-order of
     * the window handle names. If current window handle is at the end of the list, this method returns the first instead.
     *
     * @return value of previously active window handle.
     * */
    public String switchToNextWindow() {
        List<String> handles = new ArrayList<>();
        handles.addAll(driver.getWindowHandles());
        Collections.sort(handles);
        String previousHandle = driver.getWindowHandle();
        int idx = handles.indexOf(previousHandle);
        int idxNext = (idx + 1) % handles.size();
        String nextHandle = handles.get(idxNext);
        driver.switchTo().window(nextHandle);
        cachedPageText = null;
        return previousHandle;
    }

    /**
     * Spawn a new browser window using the same webdriver instance. Note that the active window handle does not change after this method creates
     * the new window. To switch to the new window, call getDriver().switchTo().window()
     *
     * Fun Fact: The WebDriver API lacks this feature because the Selenium devteam hates you.
     * 
     * @return handle of new window.
     */
    public String spawnWindow() {
        return spawnWindow(Keys.chord(OS.CURRENT.getMetaKey(), "n"));
    }

    /**
     * Spawn a "private browsing". This is a handy way to login with different credentials on the same webdriver instance. Note that
     * all private windows share the same cookies. So you the effective maximum number of unique sessions per webdriver instance is two;
     * 
     * @return
     */
    public String spawnPrivateWindow() {
        if (currentBrowser != Browser.FIREFOX)
            fail("not implemented for " + currentBrowser);
        return spawnWindow(Keys.chord(TestConfiguration.OS.CURRENT.getMetaKey(), "P"));
    }

    /**
     * send specified key combo to current window and assert that the browser created a new window
     * 
     * @param chord
     * @return
     */
    private String spawnWindow(String chord) {
        List<String> origHandles = new ArrayList<>();
        List<String> newHandles = new ArrayList<>();
        origHandles.addAll(driver.getWindowHandles());

        // fixme: is there a platform-independent way to get teh appropriate meta key (ctrl for windows, command for windows, meta for unix)
        find("body").sendKeys(chord);
        // hopefully this spawned a new window
        newHandles.addAll(driver.getWindowHandles());
        newHandles.removeAll(origHandles);
        assertThat("new window should have been created", newHandles, is(not(empty())));
        String newHandle = newHandles.iterator().next();
        return newHandle;

    }

    public void force1024x768() {
        Dimension size2 = getDriver().manage().window().getSize();
        if (size2 != testSize) {
            originalSize = size2;
        }
        getDriver().manage().window().setSize(testSize);
    }
    
    public void resetSize() {
        if (originalSize != null) {
            getDriver().manage().window().setSize(originalSize);
        }
    }

    
    /**
     * think up values for use on a registration attempt that satisfy minimum required fields
     * @return
     */
    public TdarUser createUser(String prefix) {
        TdarUser user = new TdarUser();
        String uuid = prefix + UUID.randomUUID().toString();
        user.setEmail(uuid + "@tdar.org");
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setUsername(uuid);
        return user;
    }


    /**
     * create user-registration info with random username,email that satisfies minimum required fields
     * @param userPrefix prefix applied to username, email, firstname, and lastname
     * @return
     */
    public UserRegistration createUserRegistration(String userPrefix) {
        UserRegistration reg = new UserRegistration();
        TdarUser user = createUser(userPrefix);
        reg.setPerson(user);
        reg.setPassword("testPassword");
        reg.setConfirmPassword(reg.getPassword());
        reg.setConfirmEmail(user.getEmail());
        reg.setRequestingContributorAccess(true);
        reg.setAcceptTermsOfUse(true);
        return reg;
    }


    /**
     * fill out the user registration fields on the cart/review page.
     * @param reg user registration information
     */
    public void fillOutRegistration(UserRegistration reg) {
        //on firefox, autofoxus occurs after pageload(bugzilla: 717361). so we wait
        waitForPageload();
        TdarUser person = reg.getPerson();
        find("#firstName").val(person.getFirstName());
        find("#lastName").val(person.getLastName());
        find("#emailAddress").val(person.getEmail());

        assertThat(find("#confirmEmail").toList().size(), is(equalTo(1)));
        find("#confirmEmail").val(reg.getConfirmEmail());
        find("#password").val(reg.getPassword());
        find("#confirmPassword").val(reg.getConfirmPassword());
        find("#username").val(person.getUsername());
        WebElementSelection touId = find("#tou-id");
        if(reg.isAcceptTermsOfUse() != touId.isSelected()) {
            touId.click();
        }
        
        
        WebElementSelection contribId = null;
        try {
            contribId = find("#contributor-id");
            if(contribId != null && reg.isRequestingContributorAccess() != contribId.isSelected() ) {
                contribId.click();
            }
        } catch (TdarRecoverableRuntimeException e) {
            // doesn't exist
        }
        
    }

    /**
     * Assert that user is logged out.
     */
    public void assertLoggedOut() {
        List<WebElement> selection = find(By.linkText("LOG IN")).toList();
        logger.debug(getCurrentUrl());
        assertThat("login button is missing", selection, is(not(empty())));
    }


}
