package org.tdar.experimental;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class FirefoxTestCase {
    
    WebDriver driver;
    Logger logger = LoggerFactory.getLogger(FirefoxTestCase.class);
    
    @Before 
    public void before() {
        logger.debug("before");
        driver = new FirefoxDriver();
        logger.debug("driver created");
    }
    
    @After 
    public void after() {
        logger.debug("after");
        try {
            driver.close();
            driver = null;
        }catch (Exception ex) {
            logger.error("Something bad happened: {}", ex);
        }
    }
    
    
    
    @Test
    //if cnn.com does not have the word "breaking"  on the page,  something is clearly wrong
    public void testGoogleSearch() {
        logger.debug("starting test");
        // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface, 
        // not the implementation.

        // And now use this to visit Google
        driver.get("http://www.google.com");
        // Alternatively the same thing can be done like this
        // driver.navigate().to("http://www.google.com");

        // Find the text input element by its name
        WebElement element = driver.findElement(By.name("q"));

        // Enter something to search for
        element.sendKeys("Cheese!");

        // Now submit the form. WebDriver will find the form for us from the element
        element.submit();

        // Check the title of the page
        System.out.println("Page title is: " + driver.getTitle());
        
        // Google's search is rendered dynamically with JavaScript.
        // Wait for the page to load, timeout after 10 seconds
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.getTitle().toLowerCase().startsWith("cheese!");
            }
        });

        // Should see: "cheese! - Google Search"
        logger.debug("Page title is: " + driver.getTitle());        
    }

}
