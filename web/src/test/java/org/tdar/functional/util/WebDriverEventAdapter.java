package org.tdar.functional.util;

import java.util.Arrays;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An abstract adapter class for receiving WebDriver events. The methods in this class are (mostly) empty. It
 * exists simply as a convenience for creating WebDriverEventListener objects.
 */
public abstract class WebDriverEventAdapter implements WebDriverEventListener {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Override public void beforeNavigateTo(String url, WebDriver driver) {}

    @Override public void afterNavigateTo(String url, WebDriver driver) {}

    @Override public void beforeNavigateBack(WebDriver driver) {}

    @Override public void afterNavigateBack(WebDriver driver) {}

    @Override public void beforeNavigateForward(WebDriver driver) {}

    @Override public void afterNavigateForward(WebDriver driver) {}

    @Override public void beforeFindBy(By by, WebElement element, WebDriver driver) {}

    @Override public void afterFindBy(By by, WebElement element, WebDriver driver) {}

    @Override public void beforeClickOn(WebElement element, WebDriver driver) {}

    @Override public void afterClickOn(WebElement element, WebDriver driver) {}

    @Override public void beforeChangeValueOf(WebElement element, WebDriver driver) {}

    @Override public void afterChangeValueOf(WebElement element, WebDriver driver) {}

    @Override public void beforeScript(String script, WebDriver driver) {}

    @Override public void afterScript(String script, WebDriver driver) {}

    //Return true if this exception is a "false positive" because it will ultimately be caught by ExpectedConditions#stalenessOf
    //HACK: Do not regard any of what this method does to be a good idea. It is a hack that is specifically written around a bug.
    private boolean isFalsePositive(Throwable throwable) {
        if (!(throwable instanceof StaleElementReferenceException)) {
            return false;
        } else {

            logger.trace("Stale element exception - checking to see if we can ignore it.");

            // Things get tricky here:  We aren't actually looking for ExpectedConditions#stalenessOf, but rather
            // the *anonymous inner class* it returns. Since it's anonymous we don't know what name to look for at
            // compile time, so we create an instance of the inner class and check if that instance name matches any
            // name in the stack frames.
            ExpectedCondition<Boolean> innerClassInstance = ExpectedConditions.stalenessOf(null);
            Class<?> innerClass = innerClassInstance.getClass();
            Stream<StackTraceElement> stackFrames = Arrays.stream(throwable.getStackTrace());

            //if stalenessOf is not in any of the  frames, we deem this throwable relevant
            return stackFrames.anyMatch(frame -> {
                logger.trace("comparing {} to {}", frame.getClassName(), innerClass.getName());
                return frame.getClassName().equals(innerClass.getName());
            });
        }
    }
    @Override public final void onException(Throwable throwable, WebDriver driver) {
        if(isFalsePositive(throwable)) {
            logger.info("ignoring StaleElementException because it will be caught by ExpectedConditions");
        } else {
            onError(throwable, driver);
        }
    }


    /**
     * Override onError instead of onException squelch 'irrelevent' reports of StaleElementReferenceException.
     *
     * @see {link: https://github.com/SeleniumHQ/selenium/issues/1184}
     * @param throwable
     * @param driver
     */
    public void onError(Throwable throwable, WebDriver driver){}
}
