package org.tdar.web.functional;

import static org.tdar.TestConstants.DEFAULT_BASE_URL;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

public abstract class SeleniumWebITCase {

    WebDriver driver;
    protected Logger logger = LoggerFactory.getLogger(getClass());
        
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
        Assert.fail("testing...");
        driver = new FirefoxDriver(fb, newFirefoxProfile());
    }
    
    public FirefoxProfile newFirefoxProfile() {
        return new FirefoxProfile();
    }
    
    /**
     * @return firefox profile that has CSS rendering disabled.  
     */
    public final FirefoxProfile firefoxProfileNoCss() {
        //http://stackoverflow.com/questions/3526361/firefoxdriver-how-to-disable-javascript-css-and-make-sendkeys-type-instantly
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("permissions.default.stylesheet", 2);
        //profile.setPreference("permissions.default.image", 2);
        return profile;
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
        String currentUrl = driver.getCurrentUrl();
        logger.debug("current url: {}", currentUrl);
        if(currentUrl.length() == 0 || StringUtils.equalsIgnoreCase("about:blank", currentUrl)) {
            currentUrl = DEFAULT_BASE_URL;
        }
        URL url = null;
        try {
            url = new URL(currentUrl);
        } catch (MalformedURLException e) {
            Assert.fail("could not go to url: " + currentUrl);
        }
        String absoluteUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + path;
        return absoluteUrl;
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
    
    //FIXME: select() seems more appropriate, given the ways you can select stuff.  Or, since we're aping jquery.. just $()?
    /**
     * Create a selection out of one or more.
     * @param elems WebElement objects
     * @return
     */
    public WebElementSelection find(WebElement ... elems) {
        return new WebElementSelection(Arrays.asList(elems));
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
        
    @SuppressWarnings("unchecked") //this is a convenience so that callers don't have to cast. It's probably a bad idea.
    /**
     * execute a snippet of javascript in an anonymous function.  if your snippet returns a value, Selenium will attempt to cast the most "appropriate"
     * java type (String, Double, Integer, etc)  or a WebElement if you return a DOM node.  
     * @param functionBody
     * @param arguments arguments applied to the anonymous function. you can reference them in your snippet using  javascript's 
     * contextual <code>arguments</code> object.
     * @return selenium's best approximation of the value returned by your snippet, if it exists.
     */
    public <T> T executeJavascript(String functionBody, Object...arguments) {
        JavascriptExecutor executor = (JavascriptExecutor)getDriver();
        Object result = executor.executeScript(functionBody, arguments);
        return (T)result;
    }
    
    //FIXME: implement someday.  the tricky part is supporting nested properties e.g. "elem.style.position", especially when property doesn't exist yet
    public void setAttribute(WebElement elem, String property, Object value) {
        throw new RuntimeException("no");
    }
    
    public void setStyle(WebElement elem, String property, Object value){
        executeJavascript("arguments[0].style[arguments[1]]=arguments[2]", elem, property, value);
    }

    /**
     * This is a hack that enables selenium to work with the Blueimp jQuery File Upload widget.  Typically in selenium you "upload" a file using 
     * the sendKeys()  method,  but this will not work when using the fileupload widget because it uses CSS styles to hide the text-entry box, and selenium
     * will not execute sendkeys() on elements that selenium determines to be invisible to the user.
     */
    public void clearFileInputStyles() {
        WebElement input = find("#fileAsyncUpload").first();
        clearFileInputStyles(input);
    }
    
    /**
     * This is a hack that enables selenium to work with the Blueimp jQuery File Upload widget.  Typically in selenium you "upload" a file using 
     * the sendKeys()  method,  but this will not work when using the fileupload widget because it uses CSS styles to hide the text-entry box, and selenium
     * will not execute sendkeys() on elements that selenium determines to be invisible to the user.
     * @param input the actual file input element (not the div that renders the jquery file upload widget)
     */
    public void clearFileInputStyles(WebElement input) {
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
     * @return true if fieldname follows struts indexed name convention (e.g.  person[3].id)
     */
    public boolean isIndexedField(String fieldName) {
        String indexedNamePattern = "(.+)\\[(\\d+)\\](\\..+)?";
        return fieldName.matches(indexedNamePattern);
    }
    
    /**
     * @param fieldName name attribute value of field. which is expected to follow the struts naming pattern for collections
     * @return 
     */
    private WebElement getZerothElement(String fieldName) {
        String indexedNamePattern = "(.+)\\[(\\d+)\\](\\..+)?";
        String zerothFieldName = fieldName.replaceAll(indexedNamePattern, "$1[0]$3");
        logger.debug("old name:\t{}", fieldName);
        logger.debug("new name:\t{}", zerothFieldName);
        WebElementSelection selection = find(By.name(zerothFieldName));
        if(selection.isEmpty()) {
            return null;
        } else {
            return selection.first();
        }
    }
    
    /**
     * Return a element with the specified name, which is assumed to follow tDAR's 'repeatable' element convention.  
     * If the element doesn't exist, this method tries to locate the 'add another row' button and clicks it until an element with the specified
     * field name exists.
     * @param fieldName name attribute of the element to find.
     * @return The element with the specified name attribute, or null if this method could neither find the element or implicitly create it.
     */
    public WebElement findOrCreateIndexedField(String fieldName) {
        WebElement result = null;
        WebElementSelection elem = find(By.name(fieldName));
        if(elem.isEmpty()) {
            WebElement zeroElem = getZerothElement(fieldName);
            String repeatLastRowId  = find(zeroElem).parentsWithClass("repeatLastRow").getAttribute("id");
            String buttonSelector = "#" + repeatLastRowId + " + .add-another-control button";
            //selector for button after container (e.g. "#resourceNotesSection + .add-another-control button")
            WebElement button = find(buttonSelector).first();
            //FIXME: create private method that takes indexed fieldname and returns index as int (or -1 if not a valid fieldname)
            int attempts = clickElementUntil(button, By.name(fieldName), 100);
            result = find(By.name(fieldName)).first();
        }else {
            result = elem.first();
        }
        return result;
    }
    
    /**
     * Click an element zero or more times until another element is present on the page as determined by the specified By criteria (for example, clicking a 
     * tdar "add another item" button until the page generates 10 blank person records)
     * @param findBy the criteria to use to find a matching element on the page
     * @param max the maximum number of times this method should click every element if a match has not been found.
     * @return the number of clicks  performed (per selected item),  or -1 if a matching element was never found.
     */
    public int clickElementUntil(WebElement element, By findBy, int max) {
        int i = 0;
        while(find(findBy).size() == 0 && i < max) {
            element.click();
            i++;
        }
        return i;
    }
    
    
    
}
