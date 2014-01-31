package org.tdar.core.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.UrlService;

public class URLServiceTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testViewReplace() {
        assertEquals("/project/1234", UrlService.reformatViewUrl("/project/view?id=1234"));
        assertEquals("/project/edit?id=1234", UrlService.reformatViewUrl("/project/edit?id=1234"));
        assertEquals("/project/1234?startRecord=100", UrlService.reformatViewUrl("/project/view?id=1234&startRecord=100"));
        assertEquals("/browse/creators/1234?startRecord=100", UrlService.reformatViewUrl("/browse/creators?id=1234&startRecord=100"));
        assertEquals("/browse/creators/1234", UrlService.reformatViewUrl("/browse/creators?id=1234"));
    }
}
