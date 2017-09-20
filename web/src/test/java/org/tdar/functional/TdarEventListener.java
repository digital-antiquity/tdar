package org.tdar.functional;

import static org.junit.Assert.fail;

import java.util.Date;
import java.util.logging.Level;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.functional.util.WebDriverEventAdapter;
import org.tdar.web.AbstractWebTestCase;

public class TdarEventListener extends WebDriverEventAdapter {
    
    protected  Logger logger = LoggerFactory.getLogger(AbstractSeleniumWebITCase.class);
    private AbstractSeleniumWebITCase test;

    public TdarEventListener(AbstractSeleniumWebITCase test) {
        this.test = test;
    }
    
    /**
     * The {@link WebDriverEventListener#afterClickOn} element argument is invalid if the clicked-on element caused the browser to navigate to new page, so we
     * we can't inspect it. So we use this field to signal whether afterClickOn() should call afterPageChange()
     */
        @Override
        public void afterNavigateTo(String url, WebDriver driver) {
            logger.trace("afterNavigateTo");
            afterPageChange();
        }

        @Override
        public void beforeNavigateBack(WebDriver driver) {
            logger.trace("beforeNavigateBack");
            beforePageChange();
        }

        @Override
        public void afterNavigateBack(WebDriver driver) {
            logger.trace("afterNavigateBack");
            afterPageChange();
        }

        @Override
        public void beforeNavigateForward(WebDriver driver) {
            logger.trace("beforeNavigateForward");
            beforePageChange();
        }

        @Override
        public void afterNavigateForward(WebDriver driver) {
            logger.trace("afterNavigateForward");
            afterPageChange();
        }

        @Override
        public void onError(Throwable throwable, WebDriver driver) {
            logger.error("--------------------------------------------------------");
            logger.error("hey there was an error {}", throwable, throwable);
            getBrowserConsoleLogEntries(driver);
            if (!throwable.getMessage().contains("n is null")) {
                logger.error("hey there was an error", throwable, throwable);
            }
            test.takeScreenshot("ERROR " + throwable.getClass().getSimpleName());
        }

        private void getBrowserConsoleLogEntries(WebDriver driver) {
            LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
            for (LogEntry entry : logEntries) {
                if (entry.getLevel() == Level.SEVERE) {
                    logger.error("Browser: " + new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
                }
                // do something useful with the data
            }
        }

        @Override
        public void beforeClickOn(WebElement element, WebDriver driver) {
            logger.trace("beforeClickOn");
            if (elementCausesNavigation(element)) {
                beforePageChange();
            } else {
                // if the click didn't cause navigation,  we assume that the click serves the purpose of modifying the current page somehow, the next
                // find might be volatile if not preceded by an implicit wait.
                test.setVolatileFind(true);
            }
            if (driver instanceof HasCapabilities) {
                Capabilities cp = ((HasCapabilities) driver).getCapabilities();
                if (cp.getBrowserName().equals("chrome")) {
                    try {
                        ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].scrollIntoView(true);", element);
                    } catch (Exception e) {
                        
                    }
                }
            }
//            driver.manage().window().maximize();
        }

        @Override
        public void afterClickOn(WebElement element, WebDriver driver) {
            logger.trace("afterClickOn");
        }

        private boolean elementCausesNavigation(WebElement element) {
            String tag = element.getTagName();
            return (tag.equals("a"))
                    || (tag.equals("input") && "submit".equals(element.getAttribute("type")))
                    || (tag.equals("button") && "submit".equals(element.getAttribute("type")));
        }

        @Override
        public void beforeNavigateTo(String url, WebDriver driver) {
            logger.trace("beforeNavigateTo");
            beforePageChange();
        }

        @Override
        public void afterNavigateRefresh(WebDriver arg0) {
            logger.trace("afterNavigateRefresh");
            
        }

        @Override
        public void beforeNavigateRefresh(WebDriver arg0) {
            logger.trace("beforeNavigateRefresh");
            
        }

        @Override
        public void afterChangeValueOf(WebElement arg0, WebDriver arg1, CharSequence[] arg2) {
            logger.trace("afterChangeValueOf");
            
        }

        @Override
        public void beforeChangeValueOf(WebElement arg0, WebDriver arg1, CharSequence[] arg2) {
            logger.trace("beforeChangeValueOf");
            
        }

        
        /**
         * This event fires after the webdriver executes a command that will cause navigation (e.g. link click, back button, gotoPage())
         * but before the navigation occurs.
         */
        protected void beforePageChange() {
            test.takeScreenshot();
            test.reportJavascriptErrors();
            test.setCachedPageText(null);
        }

        /**
         * This event fires after the browser navigates to a location following a command from webdriver (e.g. link click, back button, gotoPage())
         */
        protected void afterPageChange() {
            if (test.isIgnoreModals()) {
                test.dismissModal();
            }
            test.applyEditPageHacks();
            test.takeScreenshot();
            if (!test.isIgnorePageErrorChecks()) {
                String text = test.getText();
                String lcText = text.toLowerCase();
                for (String err : AbstractWebTestCase.errorPatterns) {
                    if (text.contains(err) || lcText.contains(err)) {
                        fail("page has '" + err + "'");
                    }
                }
                test.setIgnorePageErrorChecks(false);
            }
        }

        @Override
        public void afterAlertAccept(WebDriver arg0) {
            logger.trace("afterAlertAccept");
            
        }

        @Override
        public void afterAlertDismiss(WebDriver arg0) {
            logger.trace("afterAlertDismiss");
            
        }

        @Override
        public void beforeAlertAccept(WebDriver arg0) {
            logger.trace("beforeAlertAccept");
            
        }

        @Override
        public void beforeAlertDismiss(WebDriver arg0) {
            logger.trace("beforeAlertDismiss");
        }


}
