package org.tdar.web.functional;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Hamcrest matchers for web tests.
 */
public class WebMatchers {

    public static Matcher<WebElementSelection> visible() {
        return new TypeSafeMatcher<WebElementSelection>() {

            protected boolean matchesSafely(WebElementSelection item) {
                //size==0 is still true because you can see all the elements that exist.
                return item.visibleElements().size() == item.size();
            }

            public void describeTo(Description description) {
                description.appendText("contains only visible elements");
            }
        };
    }

    public static Matcher<WebElementSelection> emptySelection() {
        return new TypeSafeMatcher<WebElementSelection>() {
            protected boolean matchesSafely(WebElementSelection item) {
                return item.isEmpty();
            }

            public void describeTo(Description description) {
                description.appendText("an empty collection");
            }
        };
    }

}
