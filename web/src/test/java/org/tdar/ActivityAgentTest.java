package org.tdar;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.utils.activity.Activity;

public class ActivityAgentTest {

    transient Logger logger = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testBots() {
        List<String> bots = Arrays.asList("Googlebot/2.1; +http://www.google.com/bot",
                "Mozilla/5.0+(compatible; UptimeRobot/2.0; http://www.uptimerobot.com",
                "SemrushBot/1.2~bl; +http://www.semrush.com/bot.htm",
                "YandexBot/3.0; +http://yandex.com/bot",
                "bingbot/2.0; +http://www.bing.com/bingbot.ht",
                "Baiduspider/2.0; +http://www.baidu.com/search/spider.htm",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/600.2.5 (KHTML, like Gecko) Version/8.0.2 Safari/600.2.5 (Applebot/0.1; +http://www.apple.com/go/applebot)",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 7_0 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)",
                "BLEXBot/1.0; +http://webmeup-crawler.com",
                "msnbot-media/1.1 (+http://search.msn.com/msnbot.htm)");
        bots.forEach(bot -> {
            logger.debug("{} - {}", bot, Activity.testUserAgent(bot));
            assertTrue(Activity.testUserAgent(bot));
        });
    }

    @Test
    public void testUsers() {
    List<String> users = Arrays.asList("Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML, like Gecko) Version/10.0 Mobile/14B100 Safari/602.1",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.1 Safari/603.1.30",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:29.0) Gecko/20100101 Firefox/49");
    users.forEach(bot -> {
        logger.debug("{} - {}", bot, Activity.testUserAgent(bot));
        assertFalse(Activity.testUserAgent(bot));
    });
    }
}
