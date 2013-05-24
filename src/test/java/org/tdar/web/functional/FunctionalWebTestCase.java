package org.tdar.web.functional;

import static org.tdar.TestConstants.DEFAULT_BASE_URL;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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

public abstract class FunctionalWebTestCase {

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
    
    
    
    
}
