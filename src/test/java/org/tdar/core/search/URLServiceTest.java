package org.tdar.core.search;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.tdar.core.bean.util.UrlUtils.slugify;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.keyword.CultureKeyword;
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

    @Test
    public void testKeywordSlug() {
        CultureKeyword keyword = new CultureKeyword("a b / / d - ? \\ % ^ å ");
        logger.debug("{}",keyword.getSlug());
        assertEquals("a-b-d-a", keyword.getSlug());
    }

    @Test
    public void testSlugify() {
        assertThat( slugify("Hey, does this smell funny to you?"), is("hey-does-this-smell-funny-to-you"));
        assertThat( slugify("Well, what do you mean?  Are you talking, like, funny \"ha ha\" or funny \"that's weird\"?"), is("well-what-do-you-mean-are-you-talking-like-funny-ha-ha-or-funny-thats-weird"));
        assertThat( slugify("OBVIOUSLY THE LATTER!!!"), is("obviously-the-latter"));
        assertThat( slugify("...oh, okay.  No, it doesn't smell ha-ha funny."), is("oh-okay-no-it-doesnt-smell-ha-ha-funny"));
        assertThat( slugify("Thanks"), is("thanks"));
    }

    @Test
    public void testAsciiNormalizationSlugs() {
        assertThat( slugify("àbçdêfghîjklmñôpqrśtüvwxÿž"), is("abcdefghijklmnopqrstuvwxyz"));
        assertThat( slugify("ÀBÇDÊFGHÎJKLMÑÔPQRŚTÜVWXŸŽ"), is("abcdefghijklmnopqrstuvwxyz"));
    }

    @Test
    public void testUnsluggableWords() {
        assertThat( slugify("技術的には可能、しかしそうではない"), is(""));
    }

}
