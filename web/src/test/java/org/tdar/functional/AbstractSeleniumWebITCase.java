package org.tdar.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
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
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
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
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.CrowdRestDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.auth.UserRegistration;
import org.tdar.filestore.Filestore;
import org.tdar.functional.util.TdarExpectedConditions;
import org.tdar.functional.util.WebElementSelection;
import org.tdar.utils.ProcessList;
import org.tdar.utils.TestConfiguration;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public abstract class AbstractSeleniumWebITCase {

    public static final String RIGHTS = "PERMISSIONS";

    public static final String AUTO_DOWNLOAD_MIME_TYPES = "application/pdf, text/csv, image/tiff, image/tif";
    // , application/xls, application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

    // default timeout used for waitFor()
    public static final Duration WAITFOR_TIMEOUT_DEFAULT = Duration.of(20, ChronoUnit.SECONDS);
    public static final Duration WAITFOR_TIMEOUT_MAX = Duration.of(2, ChronoUnit.MINUTES);

    protected static final TestConfiguration CONFIG = TestConfiguration.getInstance();

    // private TdarConfiguration tdarConfiguration = TdarConfiguration.getInstance();
    public static String PATH_OUTPUT_ROOT = "target/selenium";

    // {@link #getText} is a slow operation. cache the results until next pageload.
    private String cachedPageText = null;
    protected Dimension testSize = new Dimension(1024, 768);
    protected Dimension originalSize;

    private static boolean quitBrowserBetweenTests = false;
    private boolean screenshotsAllowed = true;
    private Long previousScreenshotSize;
    // if true, ignore all javascript errors during page navigation events
    private boolean ignoreJavascriptErrors = false;
    // ignore javascript errors that match that match Patterns in this list
    private static List<Pattern> jserrorIgnorePatterns = new ArrayList<>();
    private boolean ignoreModals = false;
    private static WebDriver driver;
    private static Browser currentBrowser;

    // prefix screenshot filename with sequence number, relative to start of test (no need to init in @before)
    private int screenidx = 0;

    protected static Logger logger = LoggerFactory.getLogger(AbstractSeleniumWebITCase.class);
    private static WebDriver rawDriver;

    private boolean ignorePageErrorChecks;

    // indicates whether test is in a state where a find() operation may yield volatile results
    private boolean isVolatileFind = false;

    private Map<String, StopWatch> stopWatches = new HashMap<>();
    ScheduledExecutorService someScheduler = Executors.newScheduledThreadPool(100);

    // predicate that returns true if document.readystate == "complete" (use with FluentWait)
    private ExpectedCondition<Boolean> pageReady = new ExpectedCondition<Boolean>() {
        @Override
        public Boolean apply(@Nullable WebDriver webDriver) {
            try {
            String readyState = (String) ((JavascriptExecutor) webDriver).executeScript("return document.readyState");
            return "complete".equals(readyState);
            } catch (Throwable t) {
                logger.error("{}",t,t);
            }
            return false;
        }
    };

    private ExpectedCondition<Boolean> pageNotReady = new ExpectedCondition<Boolean>() {
        @Override
        public Boolean apply(@Nullable WebDriver webDriver) {
            try {
                String readyState = (String) ((JavascriptExecutor) webDriver).executeScript("return document.readyState");
                return !"complete".equals(readyState);
            } catch (Throwable t) {
                logger.error("{}",t,t);
            }
            return true;
        }
    };
    private KillSeleniumAfter someTask;

    public AbstractSeleniumWebITCase() {
    }

    /**
     *
     * Get a stopwatch of a given name. If watch doesn't exist, method implicitly creates one in "suspended" state. We use to track
     * aggregate time spent performing operations such implicit waits and find() operations.
     * 
     * @param name
     * @return
     */
    public StopWatch getStopWatch(String name) {
        StopWatch stopWatch = stopWatches.get(name);

        if (stopWatch == null) {
            stopWatch = new StopWatch();
            stopWatch.start();
            stopWatch.suspend();
            stopWatches.put(name, stopWatch);
        }
        return stopWatch;
    }

    public void deleteUserFromCrowd(TdarUser user) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileReader(new File("src/test/resources/crowd.properties")));
        CrowdRestDao crowdRestDao = new CrowdRestDao(props);
        crowdRestDao.deleteUser(user);
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
            executeJavascript("var n=document.getElementById('subnavbar');if (n != undefined) {n.parentNode.removeChild(n);}");
        } catch (Exception ignored) {
        }
    }

    private final class KillSeleniumAfter implements Runnable {
        private boolean enabled = true;

        @Override
        public void run() {
            if (enabled == false) {
                return;
            }
            fail("killed due to timeout");
            logger.error("KILLING SELENIUM -- been 10 MINUTES");
            AbstractSeleniumWebITCase.shutdownSelenium();
        }

        public void disable() {
            enabled = false;
        }
    }

    protected enum Browser {
        FIREFOX, CHROME, SAFARI, IE;
    }

    @Before
    public void beforeTest() throws IOException {
        if (quitBrowserBetweenTests) {
            initBrowser();
        }
        String fmt = " ***   RUNNING TEST: {}.{}() ***";
        logger.info(fmt, getClass().getSimpleName(), testName.getMethodName());
        getJavascriptIgnorePatterns().add(TestConstants.REGEX_TYPEKIT);
        getJavascriptIgnorePatterns().add(TestConstants.REGEX_GOOGLE_ANALYTICS);
        EventFiringWebDriver eventFiringWebDriver = new EventFiringWebDriver(rawDriver);
        eventFiringWebDriver.register(new TdarEventListener(this));

        this.driver = eventFiringWebDriver;
        force1024x768();

        someTask = new KillSeleniumAfter();
        long timeDelay = 10; // You can specify 3 what
        someScheduler.schedule(someTask, timeDelay, TimeUnit.MINUTES);
    }

    @BeforeClass
    public static void before() throws IOException {
        if (!quitBrowserBetweenTests) {
            initBrowser();
        }
    }

    private static void initBrowser() throws IOException {
        /*
         * We define a specific binary so when running "headless" we can specify a PORT
         */
        // typekit & google-analytics errors may occur on pretty much any page and are (relatively) harmless, so we ignore them by default
        // WebDriver driver = null;
        Browser browser = Browser.CHROME;
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
            logger.debug("SETTING DISPLAY: {}", xvfbPort);
        }
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        currentBrowser = browser;
        File browserProfileDir = setupBrowserProfilePath();

        switch (browser) {
            case FIREFOX:
                FirefoxBinary fb = new FirefoxBinary();
                for (String key : environment.keySet()) {
                    fb.setEnvironmentProperty(key, environment.get(key));
                }
                FirefoxProfile profile = new FirefoxProfile(browserProfileDir);
                if (TestConfiguration.isMac()) {
                    profile.setPreference("focusmanager.testmode", true);
                }
                profile.setPreference("browser.helperApps.alwaysAsk.force", false);
                profile.setPreference("browser.download.folderList", 2);
                profile.setPreference("browser.download.manager.showWhenStarting", false);
                profile.setPreference("browser.download.manager.showAlertOnComplete", false);
                profile.setPreference("browser.helperApps.alwaysAsk.force", false);
                profile.setPreference("browser.helperApps.neverAsk.saveToDisk", AUTO_DOWNLOAD_MIME_TYPES);
                profile.setPreference("pdfjs.disabled", true);
                // Use this to disable Acrobat plugin for previewing PDFs in Firefox (if you have Adobe reader installed on your computer)
                profile.setPreference("plugin.scan.Acrobat", "99.0");
                profile.setPreference("plugin.scan.plid.all", false);
                DesiredCapabilities caps = DesiredCapabilities.firefox();
                caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

                // profile.setPreference("browser.download.dir","c:\\downloads");
                rawDriver = new FirefoxDriver(fb, profile, caps);

                break;
            case CHROME:
                // http://peter.sh/experiments/chromium-command-line-switches
                /* yes, this is ugly */
                /* ubuntu install instructions http://www.liberiangeek.net/2011/12/install-google-chrome-using-apt-get-in-ubuntu-11-10-oneiric-ocelot/ */
                File app = new File(CONFIG.getChromeDriverPath());
                logger.info("using app: {} ", app);
                File chromeDriverLogFile = new File(browserProfileDir, "chromedriver.log");
                logger.debug("chromedriver verbose logfile path:{}", chromeDriverLogFile.getAbsolutePath());
                ChromeDriverService service = new ChromeDriverService.Builder()
                        .usingDriverExecutable(app)
                        .usingPort(9515)
                        .withEnvironment(environment)
                        .withVerbose(true)
                        .withLogFile(chromeDriverLogFile)
                        .build();

                ChromeOptions copts = new ChromeOptions();
                // copts.setExperimentalOption("autofill.enabled",false);

                // turn off autocomplete: https://code.google.com/p/chromedriver/issues/detail?id=333
                // File dir = new File("src/test/resources/c1");
                logger.debug("chrome profile path set to: {}", browserProfileDir.getAbsolutePath());

                // http://peter.sh/experiments/chromium-command-line-switches/
                // ignore-certificate-errors ?
                copts.addArguments(
                        "binary=" + CONFIG.getChromeApplicationPath(), // NOTE BINARY is needed for LINUX, may not be for Mac or Windows
                        "user-data-dir=" + browserProfileDir.getAbsolutePath(), // use specific profile path (random by default?)
                        // "bwsi" //browse without signin
                        "browser.passwords=false",
                        "--dns-prefetch-disable",
                        "noerrdialogs");
                rawDriver = new ChromeDriver(service, copts);

                service.start();
                break;
            case IE:
                System.setProperty("webdriver.ie.driver", CONFIG.getIEDriverPath());
                DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
                rawDriver = new InternetExplorerDriver(configureCapabilities(ieCapabilities));
                rawDriver.manage().timeouts().implicitlyWait(90, TimeUnit.SECONDS);
                if (TdarConfiguration.getInstance().isHttpsEnabled()) {
                    fail("please disable https before testing this");
                }
                break;
            default:
                break;
        }

        listProcesses.clear();
        if (!TestConfiguration.isWindows()) {
            listProcesses.addAll(ProcessList.listProcesses("chromedriver"));
        }
    }

    private static Capabilities configureCapabilities(DesiredCapabilities caps) {
        caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        caps.setCapability("initialBrowserUrl", "about:blank");
        return caps;
    }

    @Rule
    public TestName testName = new TestName();

    private static boolean reindexed = false;
    static Set<Long> listProcesses = new HashSet<>();

    @After
    public void after() {
        report();
        //disable navigation warning
        executeJavascript("$(window).off('beforeunload');");
        try {
            logout();
            driver.get("about://");
            ;
        } catch (UnhandledAlertException uae) {
            logger.error("alert modal present when trying to close driver: {}", uae.getAlertText());
            logout();
            driver.switchTo().alert().accept();
            driver.get("about://");
            ;
        } catch (Throwable ex) {
            logger.error("Could not close selenium driver: {}", ex);
        }
        if (someTask != null) {
            someTask.disable();
        }

        if (quitBrowserBetweenTests) {
            quitBrowser();
        }
    }

    /*
     * Shutdown Selenium
     */
    @AfterClass
    public static final void shutdownSelenium() {
        if (!quitBrowserBetweenTests) {
            quitBrowser();
        }
    }

    private static void quitBrowser() {
        if (!TestConfiguration.isWindows()) {
            listProcesses.addAll(ProcessList.listProcesses("chromedriver"));
        }
        if (driver != null) {
            try {
                driver.quit();
            } catch (UnhandledAlertException uae) {
                logger.error("alert modal present when trying to close driver: {}", uae.getAlertText());
                driver.switchTo().alert().accept();
                driver.close();
                driver.quit();
            } catch (Exception ex) {
                logger.error("Could not close selenium driver: {}", ex);
            }
            driver = null;
        }
        getJavascriptIgnorePatterns().clear();
        performBrowserCleanup();
        if (!TestConfiguration.isWindows()) {
            ProcessList.killProcesses(listProcesses);
        }
    }

    
    public final void report() {
        String fmt = " *** COMPLETED TEST: {}.{}() ***";
        logger.info(fmt, getClass().getCanonicalName(), testName.getMethodName());

        // go through the each stopwatch and report the elapsed time for each
        stopWatches.forEach((name, stopWatch) -> {
            Duration d = Duration.of(stopWatch.getNanoTime(), ChronoUnit.NANOS);
            logger.info("\t stopwatch name:{}\t  total time:{}s {}ms", name, d.getSeconds(), d.minusSeconds(d.getSeconds()).toMillis());
        });
        logger.info("*******");
    }

    private static File getBrowserProfilePath() {
        return Paths.get(PATH_OUTPUT_ROOT, "browser-profiles").toFile();
    }

    /**
     * This function creates a directory named after the currently-running JUnit test.
     *
     * Browsers typically need a 'profile' directory in local storage to save cookies, browser history, and preference. This stateful information
     * can potentially introduce inconsistent test results.
     *
     * @return
     */
    private static File setupBrowserProfilePath() {
        File dir = getBrowserProfilePath();
        if (dir.exists()) {
            try {
                FileUtils.forceDelete(dir);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        dir.mkdirs();
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException ex) {
            logger.error("can't clean directory: {}", dir);
        }
        return dir;
    }

    /**
     * Remove profile artifacts that take up space and provide little probitive value.
     */
    private static void performBrowserCleanup() {
        //
        Paths.get(getBrowserProfilePath().getAbsolutePath(), "Default", "Cache");
        FileUtils.deleteQuietly(Paths.get(getBrowserProfilePath().getAbsolutePath(), "Default", "Cache").toFile());
    }

    protected void takeScreenshot() {
        takeScreenshot(null);
    }

    protected void takeScreenshot(String filename) {
        if (!TestConfiguration.getInstance().screenshotsEnabled() ||
                !screenshotsAllowed ||
                screenidx > TestConstants.MAX_SCREENSHOTS_PER_TEST) {
            return;
        }

        screenidx++;
        // this is necessary since we take since onException() calls takeScreenshot()
        screenshotsAllowed = false;
        try {
            Screenshot takeScreenshot = new AShot()
                    .shootingStrategy(ShootingStrategies.scaling(0.5f))
                    .takeScreenshot(driver);
            String scrFilename = "target/screenshots/" + getClass().getSimpleName() + "/" + testName.getMethodName();
            File dir = new File(scrFilename);
            dir.mkdirs();
            String finalFilename = screenshotFilename(filename, "png");

            File scrFile = File.createTempFile(finalFilename, ".png");
            ImageIO.write(takeScreenshot.getImage(), "png", scrFile);

            if (scrFile != null && Objects.equals(scrFile.length(), previousScreenshotSize)) {
                logger.debug("skipping screenshot, size identical: {}", scrFilename);
                return;
            }
            previousScreenshotSize = scrFile.length();
            // Now you can do whatever you need to do with it, for example copy somewhere
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
            // waitForPageload();
        } catch (MalformedURLException ex) {
            String err = String.format("bad url:: base:%s\tpath:%s", base, path);
            logger.error(err, ex);
            fail(err);
        }
    }

    /**
     * Wait for specified css selector to match at least one element. Uses default timeout.
     * 
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
     * @param timeout
     *            amount of time to wait for specified condition before timing out. Selenium timeouts are measured
     *            in seconds, therefore this function truncates values to seconds.
     * @return elements matched by specified selector
     */
    public WebElementSelection waitFor(String cssSelector, Duration timeout) {
        if (WAITFOR_TIMEOUT_MAX.minus(timeout).isNegative()) {
            fail(String.format("Requested timeout of %s exceeds maximum timeout of", timeout, WAITFOR_TIMEOUT_MAX));
        }
        // FIXME: rewrite in terms of waitFor(ExpectedCondition, int)
        WebDriverWait wait = new WebDriverWait(driver, timeout.getSeconds());
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(cssSelector)));
        WebElementSelection selection = new WebElementSelection(elements, driver);
        return selection;
    }

    /**
     * Wait for specified number of seconds. Use this as a last resort. Consider using {@link #waitFor(String)} or {@link #waitForPageload()}.
     *
     * @Deprecated
     *             Honestly, the author of this API does not like to engage in hyperbole, but if you use this method there is a strong likelyhood
     *             that you are a terrible person. LOOK AWAY.
     * 
     * @param timeInSeconds
     *            seconds to wait before timeout
     */
    @Deprecated
    public void waitFor(int timeInSeconds) {
        getStopWatch("wait").resume();
        try {
            Thread.sleep(timeInSeconds * TestConstants.MILLIS_PER_SECOND);
        } catch (InterruptedException ignored) {
        } finally {
            getStopWatch("wait").suspend();
        }
    }

    /**
     * Wait for at least one element matched by the specified locator. Uses default timeout.
     * 
     * @param elementsLocator
     *            A selenium element "locator", such as {@link By#xpath(String)} or {@link By#cssSelector(String)}
     * @return the matched elements wrapped in a WebElementSelection.
     */
    public WebElementSelection waitFor(By elementsLocator) {
        List<WebElement> elements = waitFor(ExpectedConditions.presenceOfAllElementsLocatedBy(elementsLocator));
        WebElementSelection selection = new WebElementSelection(elements, driver);
        return selection;
    }

    /**
     * Wait for the specified expected condition. Uses default timeout.
     * 
     * @param expectedCondition
     * @param <T>
     * @return
     */
    public <T> T waitFor(ExpectedCondition<T> expectedCondition) {
        return waitFor(expectedCondition, WAITFOR_TIMEOUT_DEFAULT);
    }

    /**
     * Wait for the specified expected condition within the specified timeout (in seconds)
     *
     * @param expectedCondition
     *            ExpectedCondition predicate (e.g. {@link ExpectedConditions#alertIsPresent},
     *            {@link ExpectedConditions#presenceOfAllElementsLocatedBy(org.openqa.selenium.By)}
     * @param timeout
     *            amount of time that this method suppresses ElementNotFoundException
     * @param <T>
     *            object returned by the ExpectedCondition
     *
     * @return
     */
    public <T> T waitFor(ExpectedCondition<T> expectedCondition, Duration timeout) {
        return waitFor(expectedCondition, timeout, null);
    }

    /**
     * Wait for the specified expected condition within the specified timeout (in seconds)
     * 
     * @param expectedCondition
     *            ExpectedCondition predicate (e.g. {@link ExpectedConditions#alertIsPresent},
     *            {@link ExpectedConditions#presenceOfAllElementsLocatedBy(org.openqa.selenium.By)}
     * @param timeout
     *            amount of time that this method suppresses ElementNotFoundException. Always truncated to second
     *            accuracy.
     * @param <T>
     *            object returned by the ExpectedCondition
     * @param pollingEvery
     *            Duration to wait between evaluating the expected condition. Specify a non-zero
     *            amount to use the default (500ms).
     *
     *
     * @return
     */
    public <T> T waitFor(ExpectedCondition<T> expectedCondition, Duration timeout, Duration pollingEvery) {
        T value = null;
        getStopWatch("wait").resume();
        WebDriverWait wait = new WebDriverWait(driver, timeout.getSeconds());

        // change polling interval from default of 500ms to 125ms. This may be a bad idea.

        if (pollingEvery != null && !pollingEvery.isZero()) {
            wait.pollingEvery(pollingEvery.toMillis(), TimeUnit.MILLISECONDS);
        }

        try {
            value = wait.until(expectedCondition);
        } catch (TimeoutException tex) {

            takeScreenshot("timeout exception " + tex.hashCode());
            logger.error("Wait timeout.  Screenshot saved as timeout-exception-" + tex.hashCode());
            throw tex;
        } finally {
            getStopWatch("wait").suspend();
        }

        // after an implicit wait we assume (perhaps incorrectly) that find() calls are now "non-volatile"
        setVolatileFind(false);

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
        WebElementSelection wes = find(By.cssSelector(selector));
        return wes;
    }

    /**
     * Return WebElementSelection containing all elements mathing the specified css selector.
     *
     * @param by
     * @return
     */
    public WebElementSelection find(By by) {
        if (isVolatileFind()) {
            logger.warn("Volatile find: consider replacing with waitFor() (locator:{}  test:{})", by, testName.getMethodName());

        }
        StopWatch stopWatch = getStopWatch("find");
        if (stopWatch.isSuspended()) {
            stopWatch.resume();
        }
        WebElementSelection selection = new WebElementSelection(by, driver);
        logger.trace("criteria:{}\t  size:{}", by, selection.size());
        if (stopWatch.isStarted()) {
            stopWatch.suspend();
        }
        return selection;
    }

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
        WebElementSelection find = find("#logout-button");
        // driver.manage().deleteAllCookies();
        logger.debug("LOGOUT: {} ", find);

        if (find.size() > 0) {
            // handle modal dialogs
            try {
                find.click();
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return;
            } catch (WebDriverException se) {
                logger.error("error trying to logout {}", se);
            }
        }

        gotoPage("/login");
        find = find("#logout-button");
        if (find.size() > 0) {
            find.click();
        }
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getSource() {
        return driver.getPageSource();
    }

    public String getDom() {
        return find("body").getHtml();
    }

    public String getText() {
        if (getCachedPageText() == null) {
            logger.trace("getting body.innerText for url:{}", getDriver().getCurrentUrl());
            WebElement body = waitFor("body").first();
            setCachedPageText(body.getText());
        }
        return getCachedPageText();
    }

    public String getPageCode() {
        return driver.getPageSource();
    }

    @SuppressWarnings("unchecked")
    // this is a convenience so that callers don't have to cast. It's probably a bad idea.
    /**
     * execute a snippet of javascript in an anonymous function. if your snippet returns a value, Selenium will attempt to cast the most "appropriate"
     * java type (String, Double, Integer, etc) or a WebElement if you return a DOM node.
     * 
     * @param functionBody
     * @param arguments
     *            arguments applied to the anonymous function. you can reference them in your snippet using javascript's
     *            contextual <code>arguments</code> object.
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
    @SuppressWarnings("unused")
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
        gotoPage("/admin/searchindex/build?forceClear=true");

        find("#idxBtn").click();
        waitFor("#buildStatus", Duration.of(2, ChronoUnit.MINUTES));
        waitFor("#spanDone", Duration.of(2, ChronoUnit.MINUTES));
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
        WebElementSelection find = find("#submitButton");
        if (find.isEmpty()) {
            submitForm(".submitButton,form:not(.seleniumIgnoreForm) input[type=submit]");
        } else {
            submitForm("#submitButton");
        }
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

    protected boolean sourceContains(String substring) {
        return StringUtils.contains(getSource(), substring);
    }

    protected boolean textContains(String substring) {
        return StringUtils.containsIgnoreCase(getSource(), substring);
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
        } catch (WebDriverException wde) {
            // try a few more times with kludgey version of dismissModal
            return dismissModal(10);
        }
        return true;
    }

    /**
     * Workaround for Selenium <a href="https://code.google.com/p/selenium/issues/detail?id=3544">Issue 3544: WebDriver randomly fails to accept javascript
     * alert windows (timing problem)</a>
     *
     * Keep trying to accept modal dialog every 100ms until successful. Give up after specified attempts .
     * This method assumes a modal is present and will give you a weird result if modal doesn't exist.
     *
     * @param attempts
     *            number of attempts before giving up
     * @return true if accept worked.
     */
    private boolean dismissModal(int attempts) {
        boolean successful = false;
        for (int i = 1; i <= attempts; i++) {
            logger.debug("dissmiss modal:  attempt {} of {}", i, attempts);
            try {
                Alert statusConfirm = driver.switchTo().alert();
                statusConfirm.accept();
                successful = true;
            } catch (WebDriverException ignored) {
                logger.info("exception while trying to dismiss modal dialog:  attempt {} of {}", i, attempts);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException alsoIgnored) {
                }
            }
            if (successful)
                break; // don't judge me.
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
        find("[value=file]").click();
        find("#fileUploadField").sendKeys(uploadFile.getAbsolutePath());
    }

    public void uploadFileAsync(FileAccessRestriction restriction, File uploadFile) {
        waitFor(ExpectedConditions.elementToBeClickable(By.id("fileAsyncUpload")));
        // TEMPORARY FIX
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        waitFor((WebDriver driver) -> driver.findElement(By.id("fileAsyncUpload")).isEnabled());
        find(By.id("fileAsyncUpload")).sendKeys(uploadFile.getAbsolutePath());
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
        WebElementSelection visibleElements = find(".expandable-hitarea").visibleElements();
        while (!visibleElements.isEmpty() && (giveupCount++ < 100)) {
            waitFor(TdarExpectedConditions.stabilityOfElement(".expandable-hitarea"), Duration.of(10, ChronoUnit.SECONDS), Duration.of(125, ChronoUnit.MILLIS))
                    .click();
            // visibleElements.click();
            visibleElements = find(".expandable-hitarea").visibleElements();
        }
        assertTrue("trying to expand all listview subtrees", giveupCount < 100);
    }

    protected void addPersonWithRole(Person p, String prefix, ResourceCreatorRole role) {
        // the creator fields may not yet exist (i.e. user just clicked "add-another" button).
        // So we confirm it's presence before calling val();
        waitFor(By.name(prefix + ".person.firstName")).val(p.getFirstName());
        waitFor(By.name(prefix + ".person.lastName")).val(p.getLastName());
        waitFor(By.name(prefix + ".person.email")).val(p.getEmail());
        String iname = p.getInstitutionName();
        if (iname == null) {
            iname = "";
        }
        find(By.name(prefix + ".person.institution.name")).val(iname);
        find(By.name(prefix + ".role")).visibleElements().val(role.name());

        // FIXME: wait for the autocomplete popup (autocomplete not working in selenium at the moment)
        // waitFor(".ui-menu-item a").click();
    }

    protected void addInstitutionWithRole(Institution p, String prefix, ResourceCreatorRole role) {
        waitFor(By.name(prefix + ".institution.name")).val(p.getName());
        waitFor(By.name(prefix + ".role")).visibleElements().val(role.name());

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
        String partialText = partialMenuItemTest.toLowerCase();
        field.sendKeys(textEntry);
        WebElement autocompletePopup = waitForAutocompletePopup().first();
        field.sendKeys(Keys.ARROW_DOWN);
        WebElementSelection menuItems = find("li.ui-menu-item");

        if (logger.isTraceEnabled()) {
            logger.trace("menuItems: {} ({})", menuItems.getHtml(), menuItems.size());
        }
        logger.debug("menu size:{}", menuItems.size());

        WebElement firstMatch = null;
        logger.debug("idSelector:{}", idSelector);
        if (StringUtils.isNotBlank(idSelector)) {
            if (!find(By.id(idSelector)).isEmpty()) {
                firstMatch = autocompletePopup.findElement(By.id(idSelector));
            }
        }

        if (firstMatch == null) {
            for (WebElement menuItem : menuItems) {
                String text = menuItem.getText().toLowerCase();
                logger.debug("looking in [{}] for [{}]", text, partialText);
                if (text.contains(partialText)) {
                    firstMatch = menuItem;
                    break;
                }
                logger.info(text);
            }
        }

        boolean wasFound = firstMatch != null;
        logger.info("match: {} ", firstMatch);
        if (wasFound) {
            (firstMatch.findElement(By.tagName("a"))).click();
            // waitFor(TestConfiguration.getInstance().getWaitInt());
            waitFor(TdarExpectedConditions.invisibilityOf(find(autocompletePopup)));
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
    public static final void setJavascriptIgnorePatterns(List<Pattern> patterns) {
        jserrorIgnorePatterns = patterns;
    }

    public static final List<Pattern> getJavascriptIgnorePatterns() {
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

    // FIXME: I originally exposed this because getText() was such a costly method, but this 'fix' causes more problems in the form of returning outdated page
    // text
    /**
     * clear the cached result of getText(). The test updates the pageText whenever it detects a navigation event. However
     * If you invoke navigation via javascript, it may be necessary to manually clear it.
     */
    public void clearPageCache() {
        setCachedPageText(null);
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
     */
    public String switchToNextWindow() {
        List<String> handles = new ArrayList<>();
        handles.addAll(driver.getWindowHandles());
        Collections.sort(handles);
        for (String handle : handles) {
            logger.debug("handle: {}", handle);
        }
        String previousHandle = driver.getWindowHandle();
        int idx = handles.indexOf(previousHandle);
        int idxNext = (idx + 1) % handles.size();
        String nextHandle = handles.get(idxNext);
        driver.switchTo().window(nextHandle);
        setCachedPageText(null);
        return previousHandle;
    }

    public String switchToWindow(String url) {
        List<String> handles = new ArrayList<>();
        handles.addAll(driver.getWindowHandles());
        Collections.sort(handles);
        setCachedPageText(null);
        for (String handle : handles) {
            driver.switchTo().window(handle);
            logger.debug("handle: {} ({})", handle, driver.getCurrentUrl());
            if (driver.getCurrentUrl().contains(url)) {
                return handle;
            }
        }
        return null;
    }

    public Keys getMetaKey(TestConfiguration.OS os) {
        switch (os) {
            case OSX:
                return Keys.COMMAND;
            case UNIX:
                return Keys.META;
            default:
                return Keys.CONTROL;

        }
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
        return spawnWindow(Keys.chord(getMetaKey(TestConfiguration.OS.CURRENT), "n"));
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
        return spawnWindow(Keys.chord(getMetaKey(TestConfiguration.OS.CURRENT), "P"));
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
        assertThat("new window should have been created", newHandles, Matchers.is(Matchers.not(Matchers.empty())));
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
     * 
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
     * 
     * @param userPrefix
     *            prefix applied to username, email, firstname, and lastname
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
     * 
     * @param reg
     *            user registration information
     */
    public void fillOutRegistration(UserRegistration reg) {
        // on firefox, autofoxus occurs after pageload(bugzilla: 717361). so we wait
        waitForPageload();
        TdarUser person = reg.getPerson();
        find("#firstName").val(person.getFirstName());
        find("#lastName").val(person.getLastName());
        find("#emailAddress").val(person.getEmail());

        assertThat(find("#confirmEmail").toList().size(), Matchers.is(Matchers.equalTo(1)));
        find("#confirmEmail").val(reg.getConfirmEmail());
        find("#password").val(reg.getPassword());
        find("#confirmPassword").val(reg.getConfirmPassword());
        find("#username").val(person.getUsername());
        WebElementSelection touId = find("#tou-id");
        if (reg.isAcceptTermsOfUse() != touId.isSelected()) {
            touId.click();
        }

        WebElementSelection contribId = null;
        logger.debug(getCurrentUrl());
        logger.debug(getSource());
        try {
            contribId = find("#contributor-id");
            if (contribId != null && contribId.size() > 0 && reg.isRequestingContributorAccess() != contribId.isSelected()) {
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
        waitForPageload();
        List<WebElement> selection = find(By.linkText("LOG IN")).toList();
        if (CollectionUtils.isEmpty(selection)) {
            selection = find(By.linkText("Log In")).toList();
        }
        if (CollectionUtils.isEmpty(selection)) {
            selection = find("#loginButton").toList();
        }
        logger.debug(getCurrentUrl());
        assertThat("login button is missing", selection, Matchers.is(Matchers.not(Matchers.empty())));
    }

    /**
     * Wait for a jquery UI autocomplete window to become visible.
     *
     * @return the first visible autocomplete results window found by this method.
     */
    public WebElementSelection waitForAutocompletePopup() {
        // due to how jquery caches autocomplete results, we can't predict which autocomplete that there is only one. So, we wait until any autocompplete
        // window becomes visible.
        List<WebElement> elements = waitFor(TdarExpectedConditions.visibilityOfAnyElementsLocatedBy(By.cssSelector("ul.ui-autocomplete")));
        return new WebElementSelection(elements, getDriver());
    }

    public void setStyle(WebElement elem, String property, Object value) {
        executeJavascript("arguments[0].style[arguments[1]]=arguments[2]", elem, property, value);
    }

    public void setStyle(WebElementSelection selection, String property, Object value) {
        for (WebElement element : selection) {
            setStyle(element, property, value);
        }
    }

    public void reloadUntilFound(String url, String text, int i) {
        for (int count = 0; count < i; count ++) {
            gotoPage(url);
            if (!getText().contains(text)) {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1l));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }

    }
    
    /**
     * This is a hack that enables selenium to work with the Blueimp jQuery File Upload widget. Typically in selenium you "upload" a file using
     * the sendKeys() method, but this will not work when using the fileupload widget because it uses CSS styles to hide the text-entry box, and selenium
     * will not execute sendkeys() on elements that selenium determines to be invisible to the user.
     */
    public void clearFileInputStyles() {
        // todo: we removed this back in rev 94d504cf5128:7082 as workaround to FirefoxDriver bug.
        // Try removing the workaround and seeing if the firefoxdriver bug is fixed.
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

    // TODO: move this method to WebElementSelection
    // TODO: comment all of the lambda/stream insanity that's going on in this method
    // FIXME: instead of separate method name, selection.val() should intelligently handle real form elements as well as select2 controls
    public final void select2val(WebElementSelection selection, List<String> vals) {
        selection.toList().stream()
                .filter(elem -> elem.getAttribute("class").contains("select2-hidden-accessible"))
                .map(elem -> find(elem.findElement(By.xpath("following-sibling::*[1]"))))
                .forEach(proxy -> {
                    logger.debug("select2 proxy:: length:{} tag:{}  html:{}", proxy.size(), proxy.getTagName(), proxy.getHtml());
                    logger.debug("values to set: {}", vals);
                    proxy.find(".select2-selection__rendered").click();
                    vals.forEach((v) -> {
                        waitFor(driver -> !proxy.find(".select2-search__field").isEmpty());
                        // type value into textbox, wait for result menu , then click on the menu item.
                        WebElementSelection searchField = proxy.find(".select2-search__field").sendKeys(v);
                        String menuOption = String.format("span.select2-container--open span[data-id=\"%s\"]", v);
                        logger.debug("looking for: {}", menuOption);
                        waitFor(menuOption).click();
                        logger.debug("set/added value to: {}", v);
                    });

                });
    }

    /**
     * Clears all values from a select2 control. This only works with multi-valued select2 controls. For single-value controls, click on their delete button
     * instead.
     * 
     * @param selection
     *            Selection that contains the original &lt;select&gt; elements that back a select2 control (not the select2 container facades)
     */
    public final void select2Clear(WebElementSelection selection) {
        selection.toList().stream()
                .filter(elem -> elem.getAttribute("class").contains("select2-hidden-accessible"))
                .map(elem -> find(elem.findElement(By.xpath("following-sibling::*[1]"))))
                .forEach(facade -> facade.find(".select2-selection__choice__remove").click());
    }

    // fixme: jtd: this function is likely unneeded (I wrote it thinking you needed to escape all css strings, instead of just literals).
    /**
     * Escape a css literal. Note that you probably don't need to do this.
     * 
     * @param val
     * @return
     */
    public String escapeCssLiteral(String val) {
        char[] specials = { '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '`', '{',
                '|', '}', '~' };
        char[] valchars = val.toCharArray();
        char esc = '\\';
        StringBuilder sb = new StringBuilder();
        if (Character.isDigit(valchars[0])) {
            sb.append(esc);
        }
        sb.append(valchars[0]);

        for (int i = 1; i < valchars.length; i++) {
            for (int j = 0; j < specials.length; j++) {
                if (valchars[i] == specials[j]) {
                    sb.append(esc);
                    break;
                }
            }
            sb.append(valchars[i]);
        }
        return sb.toString();
    }

    public boolean isVolatileFind() {
        return isVolatileFind;
    }

    public void setVolatileFind(boolean isVolatileFind) {
        this.isVolatileFind = isVolatileFind;
    }

    public String getCachedPageText() {
        return cachedPageText;
    }

    public void setCachedPageText(String cachedPageText) {
        this.cachedPageText = cachedPageText;
    }

}
