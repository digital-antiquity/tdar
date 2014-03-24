package org.tdar.web.functional;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.tdar.core.bean.DisplayOrientation;

/**
 * Created by jimdevos on 3/12/14.
 */
public class BasicSeachSeleniumITCase extends AbstractSeleniumWebITCase {



    @Before
    public void setup() {
        reindexOnce();
    }

    @Test
    public void testBrowse() {
        gotoPage("/browse");
        List<String> urls = new ArrayList<>();
        for (WebElement el : find(By.tagName("a"))) {
            urls.add(el.getAttribute("href"));
        }
        for (String url : urls) {
            gotoPage("/browse");
            gotoPage(url);
        }
    }

    @Test
    public void testResults() {
        gotoPage("/search/results");
        List<String> urls = new ArrayList<>();
        for (WebElement el : find(By.tagName("a"))) {
            urls.add(el.getAttribute("href"));
        }
        for (String url : urls) {
            gotoPage("/search/results");
            gotoPage(url);
        }
    }


}
