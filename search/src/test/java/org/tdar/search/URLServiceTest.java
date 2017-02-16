package org.tdar.search;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.tdar.core.bean.util.UrlUtils.sanitizeRelativeUrl;
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
        logger.debug("{}", keyword.getSlug());
        assertEquals("a-b-d-a", keyword.getSlug());
    }

    @Test
    public void testSlugify() {
        assertThat(slugify("Hey, does this smell funny to you?"), is("hey-does-this-smell-funny-to-you"));
        assertThat(slugify("Well, what do you mean?  Are you talking, like, funny \"ha ha\" or funny \"that's weird\"?"),
                is("well-what-do-you-mean-are-you-talking-like-funny-ha-ha-or-funny-thats-weird"));
        assertThat(slugify("OBVIOUSLY THE LATTER!!!"), is("obviously-the-latter"));
        assertThat(slugify("...oh, okay.  No, it doesn't smell ha-ha funny."), is("oh-okay-no-it-doesnt-smell-ha-ha-funny"));
        assertThat(slugify("Thanks"), is("thanks"));
        assertThat(slugify("Odontophoridae_(family_New_World_quails)"), is("odontophoridae_family_new_world_quails"));
        assertThat(slugify("first-second"), is("first-second"));
    }

    @Test
    public void testAsciiNormalizationSlugs() {
        assertThat(slugify("àbçdêfghîjklmñôpqrśtüvwxÿž"), is("abcdefghijklmnopqrstuvwxyz"));
        assertThat(slugify("ÀBÇDÊFGHÎJKLMÑÔPQRŚTÜVWXŸŽ"), is("abcdefghijklmnopqrstuvwxyz"));
    }

    @Test
    public void testUnsluggableWords() {
        assertThat(slugify("技術的には可能、しかしそうではない"), is(""));
    }

    @Test
    public void testSanitizeScammyUrls() {
        // technically invalid syntax that is forgiven by most browsers
        assertThat(sanitizeRelativeUrl("               http://scammysite.ru/register"), is("/register"));
        assertThat(sanitizeRelativeUrl("HTTP://scammysite.ru/register"), is("/register"));

        // redirect to remote that mimics authenticated content
        assertThat(sanitizeRelativeUrl("http://scammysite.ru/cart/add"), is("/cart/add"));
        assertThat(sanitizeRelativeUrl("HTTP://scammysite.ru/cart/add"), is("/cart/add"));
        assertThat(sanitizeRelativeUrl("HTTPs://scammysite.ru/cart/add"), is("/cart/add"));

        // attempt to avoid ssl warning by redirecting to site w/ same scheme as trusted host
        assertThat(sanitizeRelativeUrl("//not-really-tdar.ru/foo/bar"), is("/foo/bar"));

        // http credentials that look like url prefix of trusted host (this feature is deprecated in most browsers)
        assertThat(
                sanitizeRelativeUrl("http://core.tdar.org:%2Fjibberish%2Fcharacters%2Fthat%2Fyou%2Fprobably%2Fwont%2Fread@scammysite.ru/gimme-your-mastercard"),
                is("/gimme-your-mastercard"));

        assertThat(sanitizeRelativeUrl("http://not-really-tdar.ru:1234/foo/bar"), is("/foo/bar"));
        // initiate file download from untrusted remote host (that appears to be trusted host)
        assertThat(sanitizeRelativeUrl("ftp://scammysite.ru/virii/clickme.exe"), is("/virii/clickme.exe"));

    }

    @Test
    public void testSantizeUrlWithHttp() {
        assertThat(sanitizeRelativeUrl("/docuemnt/1234/http-test"), is("/docuemnt/1234/http-test"));
    }

    @Test
    public void testSanitizeLegitUrls() {
        // typical login redirects
        assertThat(sanitizeRelativeUrl("/document/1234"), is("/document/1234"));
        assertThat(sanitizeRelativeUrl("/dataset/save?id=12345"), is("/dataset/save?id=12345"));

        // 'complete' relative url w/ path, query, and hash
        String url = "/dir/subdir/file?queryparm1=queryval1&queryparm2=queryval2#fragment";
        assertThat(sanitizeRelativeUrl(url), is(url));

        // technically not "legit", but not malicious either. Usually caused by typo in .ftl or broken urlrewrite rule
        assertThat(sanitizeRelativeUrl("//admin"), is(""));
        assertThat(sanitizeRelativeUrl("/dataset//12345/view"), is("/dataset//12345/view"));
    }

}
