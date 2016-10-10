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
}
