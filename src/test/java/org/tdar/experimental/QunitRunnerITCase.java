package org.tdar.experimental;

import static org.junit.Assert.fail;
import static org.tdar.TestConstants.DEFAULT_BASE_URL;
import static org.tdar.TestConstants.DEFAULT_PORT;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QunitRunnerITCase {

    //TODO: use DI or something we wire up Firefox driver normally but PhantomJS driver when running 'headless'j
    WebDriver driver;
    Logger logger = LoggerFactory.getLogger(FirefoxTestCase.class);

    private String url(String path) {
        return String.format("%s/%s", DEFAULT_BASE_URL, path);
    }
    
    
    @Before 
    public void before() {
        driver = new FirefoxDriver();
    }
   
    @After 
    public void after() {
        logger.debug("after");
        try {
            driver.close();
            driver = null;
        }catch (Exception ex) {
            logger.error("Could not close selenium driver: {}", ex);
        }
    }
    
    private void gotoPage(String path) {
        String url = url(path);
        logger.debug("going to {}", url);
        driver.get(url(path));
        
    }

    private WebElement waitFor(String selector) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(selector)));
        WebElement result = null;
        if(!elements.isEmpty()) {
            result = elements.get(0);
        }
        return result;
    }
    
    private List<WebElement> find(String selector) {
        driver.findElements(By.cssSelector(selector));
        return Collections.emptyList();
    }
    
    public void assertSelector(String selector) {
        if(find(selector).isEmpty()) {
            fail("could not find content on page with selector:" + selector);
        }
    }
    
    //for now we just start off simple.  look for an error conditions and fail() if we detect an error
    public void runQunitPage(String path) {
        gotoPage(path);
        
        assertSelector("#qunit-fixture");
        
        //FIXME: this is insufficient because we don't know that this is the very last element it adds to the dom.  
        waitFor("#qunit-testresults");
        
        
        if(!find(".fail").isEmpty()) {
            fail("your qunit tests failed. sorry");
        }
    };
    
    @Test
    public void testFileUploadTests() {
        runQunitPage("/includes/test/fileupload.test.html");
    }
    
    
}
