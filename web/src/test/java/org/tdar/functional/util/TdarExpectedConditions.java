package org.tdar.functional.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handy expected conditions for tDAR-related workflows
 */
public class TdarExpectedConditions {

    private static final Logger logger = LoggerFactory.getLogger(TdarExpectedConditions.class);
    public static final int STABLE_MILLIS_DEFAULT = 251;

    private TdarExpectedConditions() {
        // static methods only
    }

    /**
     *
     * Returns WebElement for first located element when least one of the elements located by the specified locator are visible.
     *
     * @param locator
     * @return
     */
    public static ExpectedCondition<List<WebElement>> visibilityOfAnyElementsLocatedBy(
            final By locator) {
        return new ExpectedCondition<List<WebElement>>() {
            @Override
            public List<WebElement> apply(WebDriver driver) {
                List<WebElement> elements = driver.findElements(locator);
                List<WebElement> visibleElements = new ArrayList<>();
                for (WebElement element : elements) {
                    if (element.isDisplayed()) {
                        visibleElements.add(element);
                    }
                }
                if (visibleElements.isEmpty()) {
                    return null;
                } else {
                    logger.debug("found visible elements:{}", visibleElements);
                    return visibleElements;
                }
            }

            public String toString() {
                return "visibility of elements located by " + locator;
            }
        };
    }

    /**
     * True when any of the selected elements are no longer visible.
     * 
     * @param selection
     * @return
     */
    public static ExpectedCondition<Boolean> invisibilityOf(final WebElementSelection selection) {

        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(@Nullable WebDriver ignored) {
                for (WebElement element : selection) {
                    if (element.isDisplayed()) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * Return true when bootstrap 'modal' window no longer present in DOM.
     * 
     * @return
     */
    public static ExpectedCondition<Boolean> bootstrapModalGone() {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(@Nullable WebDriver driver) {
                return driver.findElements(By.className("modal-backdrop")).size() == 0;
            }

            @Override
            public String toString() {
                return "modal to disappear";
            }
        };
    }

    public static ExpectedCondition<Boolean> locatedElementCountBetween(final By locator, final int minSizeInclusive, final int maxSizeInclusive) {
        return new ExpectedCondition<Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable WebDriver driver) {
                int size = driver.findElements(locator).size();
                return (minSizeInclusive <= size || minSizeInclusive == -1)
                        && (maxSizeInclusive >= size || maxSizeInclusive == -1);
            }

            @Override
            public String toString() {
                return String.format("elements located by %s to be between %s and %s ", locator, minSizeInclusive, maxSizeInclusive);
            }
        };
    }

    public static ExpectedCondition<Boolean> locatedElementCountEquals(final By locator, final int size) {
        return locatedElementCountBetween(locator, size, size);
    }

    public static ExpectedCondition<Boolean> locatedElementCountGreaterThan(final By locator, final int minSizeInclusive) {
        return locatedElementCountBetween(locator, minSizeInclusive, -1);
    }

    public static ExpectedCondition<Boolean> locatedElementCountLessThan(final By locator, final int maxSizeInclusive) {
        return locatedElementCountBetween(locator, -1, maxSizeInclusive);
    }

    /**
     * Similar to ExpectedConditions#textToBePresentInElementLocated, but will look for text in multiple items found by the specified locator
     * 
     * @param locator
     * @param text
     * @return true if any of the matched elements contain the specified text, otherwise false
     */
    public static ExpectedCondition<Boolean> textToBePresentInElementsLocated(
            final By locator, final String text) {

        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                for (WebElement element : driver.findElements(locator)) {
                    if (!isStale(element)) {
                        //Apparently things can go stale in the instant of time between checking if it's stale.  Oh, Selenium.
                        try {
                            if (element.getText().contains(text)) {
                                return true;
                            }
                        } catch (StaleElementReferenceException ex) {
                            return false;
                        }
                    }
                }
                return false;
            }

            @Override
            public String toString() {
                return String.format("text ('%s') to be present in element found by %s",
                        text, locator);
            }
        };
    }

    /**
     * Return true if element is "stale" (i.e. is deleted or no longer part of the DOM)
     * 
     * @param element
     * @return
     */
    private static boolean isStale(WebElement element) {
        // Beleive it or not, this is how selenium advises checking for staleness (see ExpectedConditions.stalenessOf)
        try {
            element.isEnabled();
            element.getText();
            element.getTagName();
        } catch (StaleElementReferenceException ex) {
            return true;
        }
        return false;
    }

    /**
     * Wait for the element located by the locator to be "stable" for longer than the specified number of milliseconds.  We define "stable"
     * as:
     *  - does not become stale within specified threshold
     *  - the locator does not choose a new/different element within specified threshold
     *  - the locator does not return null
     *
     *  In other words:  wait up to ${timeout} seconds for element to be stable for ${stableMillis}.
     *
     * @param locator
     * @param stableMillis
     * @return
     */
    public static ExpectedCondition<WebElement> stabilityOfElement(By locator, int stableMillis) {

        return new ExpectedCondition<WebElement>() {
            long timeLast = System.currentTimeMillis();
            long timeElapsed = 0;
            WebElement current = null;
            WebElement previous = null;

            @Nullable @Override public WebElement apply(@Nullable WebDriver driver) {
                previous = current;
                current = null;
                long timeNow;

                try {
                    current = driver.findElement(locator);
                } catch(NullPointerException ignored){}

                //locator is unstable if nothing found
                if(current == null) {
                    timeElapsed = 0;
                    //System.out.println("no element found. returning null");
                    return null;
                }

                //System.out.println("element found");



                //locator is unstable if:  current not equal to previous,  or if selenium throws StaleElementException
                try{
                    //System.out.println(" current:" + current);
                    //System.out.println("previous:" + previous);
                    if(!(current.equals(previous) && current.isEnabled())) {
                        //System.out.println("element has changed from what it was before. returning null.");
                        return null;
                    }

                } catch (StaleElementReferenceException ex) {
                    //System.out.println("stale element exception. returning null");
                    return null;
                }


                //all the above conditions must be met for longer than stableMillis
                timeNow = System.currentTimeMillis();

                timeElapsed += timeNow - timeLast;
                timeLast = timeNow;
                //System.out.println("timeElapsed: " + timeElapsed);
                if(timeElapsed < stableMillis) {
                    return null;
                }

                //System.out.println("element is stable");

                return current;
            }
        };
    }


    public static ExpectedCondition<WebElement> stabilityOfElement(String cssSelector) {
        return stabilityOfElement(By.cssSelector(cssSelector), STABLE_MILLIS_DEFAULT);
    }


}
