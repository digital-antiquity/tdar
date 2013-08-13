package org.tdar.web.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.phantom.resolver.ResolvingPhantomJSDriverService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.*;
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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.Filestore;
import org.tdar.utils.TestConfiguration;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public abstract class AbstractSeleniumWebITCase {

    public static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    // private TdarConfiguration tdarConfiguration = TdarConfiguration.getInstance();
    public static String PATH_OUTPUT_ROOT = "target/selenium";

    private String pageText = null;

    // package privates
    boolean screenshotsAllowed = true;
    boolean ignoreJavascriptErrors = false;
    private boolean ignoreModals = false;
    WebDriver driver;

    // prefix screenshot filename with sequence number, relative to start of test (no need to init in @before)
    private int screenidx = 0;

    // protect against infinite loops killing our disk space
    static int MAX_SCREENSHOTS_PER_TEST = 100;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    // Predicates that can be used as arguments for FluentWait.until.
    private Predicate<WebDriver> pageReady = new Predicate<WebDriver>() {
        @Override
        public boolean apply(@Nullable WebDriver webDriver) {
            String readyState = (String) ((JavascriptExecutor) webDriver).executeScript("return document.readyState");
            return "complete".equals(readyState);
        }
    };
    private Predicate<WebDriver> pageNotReady = Predicates.not(pageReady);

    /**
     * afterClickOn() element is invalid if the clicked-on element caused the browser to navigate to new page, so we
     * we can't inspect it. So we use this field to signal whether afterClickOn() should call afterPageChange()
     */
    private Set<WebElement> clickElems = new HashSet<>();

    private WebDriverEventListener eventListener = new WebDriverEventListener() {
        public void afterNavigateTo(String url, WebDriver driver) {
            afterPageChange();
        }

        public void beforeNavigateBack(WebDriver driver) {
            beforePageChange();
        }

        public void afterNavigateBack(WebDriver driver) {
            afterPageChange();
        }

        public void beforeNavigateForward(WebDriver driver) {
            beforePageChange();
        }

        public void afterNavigateForward(WebDriver driver) {
            afterPageChange();
        }

        public void beforeFindBy(By by, WebElement element, WebDriver driver) {
        }

        public void afterFindBy(By by, WebElement element, WebDriver driver) {
        }

        public void beforeChangeValueOf(WebElement element, WebDriver driver) {
        }

        public void afterChangeValueOf(WebElement element, WebDriver driver) {
        }

        public void beforeScript(String script, WebDriver driver) {
        }

        public void afterScript(String script, WebDriver driver) {
        }

        public void onException(Throwable throwable, WebDriver driver) {
            logger.error("hey there was an error", throwable);
            takeScreenshot("ERROR " + throwable.getClass().getSimpleName());
        }

        public void beforeClickOn(WebElement element, WebDriver driver) {
            if (elementCausesNavigation(element)) {
                clickElems.add(element);
                beforePageChange();
            }
        }

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

        public void beforeNavigateTo(String url, WebDriver driver) {
            beforePageChange();
        }
    };

    private void beforePageChange() {
        takeScreenshot();
        reportJavascriptErrors();
        clearPageCache();
    }

    private void afterPageChange() {
        if (ignoreModals) {
            dismissModal();
        }
        removeAffix();
        takeScreenshot();
    }


    //temporary hack
    private void removeAffix() {
        try {
            executeJavascript("$('#subnavbar').remove()");
        } catch(Exception ex) {
            logger.warn("js error", ex);
        }
    }

    private void clearPageCache() {
        pageText = null;
    }

    private enum Browser {
        FIREFOX, CHROME, SAFARI, IE, PHANTOMJS;
    }

    @Before
    public void before() throws IOException {
        /*
         * We define a specific binary so when running "headless" we can specify a PORT
         */
        String fmt = " ***   RUNNING TEST: {}.{}() ***";
        logger.info(fmt, getClass().getSimpleName(), testName.getMethodName());
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

        switch (browser) {
            case FIREFOX:
                FirefoxBinary fb = new FirefoxBinary();
                for (String key : environment.keySet()) {
                    fb.setEnvironmentProperty(key, environment.get(key));
                }
                driver = new FirefoxDriver(fb, new FirefoxProfile());
                break;
            case CHROME:
                // http://peter.sh/experiments/chromium-command-line-switches
                /* yes, this is ugly */
                /* ubuntu install instructions http://www.liberiangeek.net/2011/12/install-google-chrome-using-apt-get-in-ubuntu-11-10-oneiric-ocelot/ */
                File app = new File("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
                app = new File(CONFIG.getChromeDriverPath());
                if (!app.exists()) {
                    app = new File("/usr/local/bin/chromedriver");
                }
                if (!app.exists()) {
                    app = new File("/usr/bin/google-chrome");
                }
                logger.info("usign app: {} ", app);
                ChromeDriverService service = new ChromeDriverService
                        .Builder().usingDriverExecutable(app).usingPort(9515).withEnvironment(environment).build();
                ChromeOptions copts = new ChromeOptions();
                File dir = new File(PATH_OUTPUT_ROOT, "profiles/chrome");
                String profilePath = dir.getAbsolutePath();
                logger.debug("chrome profile path set to: {}", profilePath);
                copts.addArguments(
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

    // /**
    // * @return firefox profile that has CSS rendering disabled.
    // */
    // public final FirefoxProfile firefoxProfileNoCss() {
    // // http://stackoverflow.com/questions/3526361/firefoxdriver-how-to-disable-javascript-css-and-make-sendkeys-type-instantly
    // FirefoxProfile profile = new FirefoxProfile();
    // profile.setPreference("permissions.default.stylesheet", 2);
    // // profile.setPreference("permissions.default.image", 2);
    // return profile;
    // }
    //

    @Rule
    public TestName testName = new TestName();
    private static boolean reindexed = false;

    /*
     * Shutdown Selenium
     */
    @After
    public final void shutdownSelenium() {
        logger.debug("after");
        takeScreenshot();
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
    }

    protected void takeScreenshot() {
        takeScreenshot(null);
    }

    protected void takeScreenshot(String filename) {
        if (!screenshotsAllowed)
            return;
        if (screenidx > MAX_SCREENSHOTS_PER_TEST)
            return;

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
     * returns absolute url based on getBaseUrl() and provided path. If path is actually a complete url itself, ignore
     * the base URL.
     * 
     * @see URL#URL(java.net.URL, String)
     */
    // private String absoluteUrl(String path) throws MalformedURLException {
    // URL baseUrl = new URL(getBaseUrl());
    // URL url = new URL(baseUrl, path);
    // return url.toString();
    // }

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

    /**
     * for our purposes, any protocol other than http[s] is considered invalid
     */
    private void assertCurrentUrl() {
        String url = getCurrentUrl();
        Assert.assertTrue("gotoPage url should be http or https", url.startsWith("http:") || url.startsWith("https:"));
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
        } catch (MalformedURLException ex) {
            String err = String.format("bad url:: base:%s\tpath:%s", base, path);
            logger.error(err, ex);
            fail(err);
        }
    }

    // public void gotoPage(String path) {
    // String url = null;
    // try {
    // url = absoluteUrl(path);
    // logger.debug("going to {}", url);
    // driver.get(absoluteUrl(path));
    // } catch (Exception e) {
    // Assert.fail(String.format("gotoPage() failed. base:%s   path:%s", getBaseUrl(), path));
    // }
    // }

    public WebElementSelection waitFor(String selector) {
        return waitFor(selector, 10);
    }

    public WebElementSelection waitFor(String cssSelector, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, timeoutInSeconds);
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(cssSelector)));
        WebElementSelection selection = new WebElementSelection(elements, driver);
        return selection;
    }

    public void waitFor(int timeInSeconds) {
        try {
            Thread.sleep(timeInSeconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
    }

    public WebElementSelection find(String selector) {
        return find(By.cssSelector(selector));
    }

    public WebElementSelection find(By by) {
        logger.trace("find start: {}", by);
        WebElementSelection selection = new WebElementSelection(driver.findElements(by), driver);
        logger.debug("criteria:{}\t  size:{}", by, selection.size());
        logger.trace("find   end: {}", by);
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
        if (pageText == null) {
            logger.trace("getting body.innerText for url:{}", getDriver().getCurrentUrl());
            WebElement body = waitFor("body").first();
            pageText = body.getText();
        }
        return pageText;
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

    public void setStyle(WebElement elem, String property, Object value) {
        executeJavascript("arguments[0].style[arguments[1]]=arguments[2]", elem, property, value);
    }

    public void setStyle(WebElementSelection selection, String property, Object value) {
        for (WebElement element : selection) {
            setStyle(element, property, value);
        }
    }

    /**
     * This is a hack that enables selenium to work with the Blueimp jQuery File Upload widget. Typically in selenium you "upload" a file using
     * the sendKeys() method, but this will not work when using the fileupload widget because it uses CSS styles to hide the text-entry box, and selenium
     * will not execute sendkeys() on elements that selenium determines to be invisible to the user.
     */
    public void clearFileInputStyles() {
        WebElement input = find("#fileAsyncUpload").first();
        showAsyncFileInput(input);
    }

    /**
     * This is a hack that enables selenium to work with the Blueimp jQuery File Upload widget. Typically in selenium you "upload" a file using
     * the sendKeys() method, but this will not work when using the fileupload widget because it uses CSS styles to hide the text-entry box, and selenium
     * will not execute sendkeys() on elements that selenium determines to be invisible to the user.
     * 
     * @param input
     *            the actual file input element (not the div that renders the jquery file upload widget)
     */
    public void showAsyncFileInput(WebElement input) {
        setStyle(input, "position", "static");
        setStyle(input, "top", "auto");
        setStyle(input, "right", "auto");
        setStyle(input, "margin", 0);
        setStyle(input, "opacity", 1);
        setStyle(input, "transform", "none");
        setStyle(input, "direction", "ltr");
        setStyle(input, "cursor", "auto");
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
        while (find(findBy).size() == 0 && i < max) {
            element.click();
            i++;
        }
        return i;
    }

    public void loginAdmin() {
        login(CONFIG.getAdminUsername(), CONFIG.getAdminPassword());
    }

    public boolean hasReindexedOnce() {
        return reindexed;
    }

    public void reindex() {
        logout();
        loginAdmin();
        gotoPage("/admin/searchindex/build");
        find("#idxBtn").click();
        waitFor("#spanDone", 120);
        logout();
        reindexed = true;
    }

    /**
     * Submit a "tDAR edit page" style form (if no javascript errors since last pageload)
     * 
     * Considerations: this function assumes a the layout of a typical edit page on tDAR. For example, it expects
     * the submit button ID value is "submitButton".
     */
    public void submitForm() {
        reportJavascriptErrors();
        WebElementSelection button = find("#submitButton");
        if (button.size() == 0) {
            button = find(".submitButton");
        }
        // important for the class selector above
        button.first().click();
        waitForPageload();
    }

    /**
     * Block until document loaded (readystate === true). This is usually unnecesssary since most click() actions
     * block anyway. Use it for situations where an event handler causes navigation (e.g. jquery validate success)
     * 
     * @param timeout
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
    }

    public List<String> getJavascriptErrors() {
        return executeJavascript("return window.__errorMessages;");
    }

    public void setIgnoreJavascriptErrors(boolean ignoreJavascriptErrors) {
        this.ignoreJavascriptErrors = ignoreJavascriptErrors;
    }

    /**
     * If any javascript errors have occured since last pageload, log them and (if ignoreJavascriptErrors==false) fail the test.
     * 
     * Note: most actions that cause page navigation will implicitly callreportJavascriptErrors() anyway, such as formSubmit(), gotoPage(), and click events on
     * links & buttons. An example of when you might wish to explicitly call this method is when you expect a javascript function to modify the
     * <code>Window.location</code> property, or if you call {@link WebElement#submit()} rather than submitForm();
     */
    public void reportJavascriptErrors() {
        List<String> errors = getJavascriptErrors();
        if (CollectionUtils.isEmpty(errors))
            return;
        logger.error("javascript error report for {}", driver.getCurrentUrl());
        for (String error : errors) {
            logger.error("javascript error: {}", error);
        }
        if (!ignoreJavascriptErrors) {
            //FIXME: uncomment the next line after we upgrade selenium to 2.35 (currently at 2.34)
            //fail("ENCOUNTERED JAVASCRIPT ERRORS ON PAGE: " + driver.getCurrentUrl() + "\r\n [" + errors + "]");
        }
    }

    protected boolean sourceContains(String substring) {
        return getSource().contains(substring);
    }

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
        }
        return true;
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
        clearFileInputStyles();
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
        while (!find(".expandable-hitarea").visibleElements().isEmpty() && giveupCount++ < 10) {
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
    public boolean selectAutocompleteValue(WebElement field, String textEntry, String partialMenuItemTest) {
        field.sendKeys(textEntry);
        waitFor(4); // kludge
        field.sendKeys(Keys.ARROW_DOWN);
        WebElementSelection menuItems = null;
        try {
            menuItems = waitFor("ul.ui-autocomplete li.ui-menu-item", 20);
        } catch (TimeoutException tex) {
            fail("could not set value on  " + field + " because autocomplete never appeared or was dismissed too soon");
        }

        logger.info("menuItems: {} ({})", menuItems, menuItems.size());
        String partialText = partialMenuItemTest.toLowerCase();
        WebElement firstMatch = null;
        for (WebElement menuItem : menuItems) {
            String text = menuItem.getText().toLowerCase();
            logger.info(text);
            if (text.contains(partialText)) {
                firstMatch = menuItem;
                break;
            }
        }

        boolean wasFound = firstMatch != null;
        logger.info("match: {} ", firstMatch);
        if (wasFound) {
            (firstMatch.findElement(By.tagName("a"))).click();
            waitFor(2);
        }
        return wasFound;
    }

}
