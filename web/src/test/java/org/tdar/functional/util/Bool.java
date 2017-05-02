package org.tdar.functional.util;

import org.openqa.selenium.WebElement;

/**
 * For passing to WebElementSelection#find
 */
public interface Bool {
    boolean apply(WebElement element);
}
