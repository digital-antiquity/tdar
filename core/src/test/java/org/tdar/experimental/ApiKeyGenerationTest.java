package org.tdar.experimental;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.util.UUID;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.DownloadAuthorization;

/**
 * Jdevos: We are not testing java.util.UUID, but rather my assumptions on how it works. Assume that a failed test is a bad test, and ignore it.
 */
public class ApiKeyGenerationTest {

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * generate a uid. any uid.
     */
    @Test
    public void justMakeASimpleUid() {
        UUID uid = UUID.randomUUID();
        String strUid = uid.toString();
        logger.debug("  UUID: {}", uid);
        logger.debug("struid: {}", strUid);
        assertThat(strUid, not(nullValue()));
    }

    /**
     * make sure they're different
     */
    @Test
    public void allUniqueUids() {
        UUID uid1 = UUID.randomUUID();
        UUID uid2 = UUID.randomUUID();

        logger.debug("uid1: {}", uid1);
        logger.debug("uid2: {}", uid2);

        assertThat(uid1, not(equalTo(uid2)));
    }

    @Test
    public void structuredUid() {
        String prefix = "Hello world";
        UUID uid1 = UUID.nameUUIDFromBytes(prefix.getBytes());
        UUID uid2 = UUID.nameUUIDFromBytes(prefix.getBytes());
        UUID uid3 = UUID.nameUUIDFromBytes((prefix + "!").getBytes()); // additional characters at end
        UUID uid4 = UUID.nameUUIDFromBytes((" " + prefix).getBytes()); // additional characters an beginning
        logger.debug("uid1: {}", uid1);
        logger.debug("uid2: {}", uid2);
        logger.debug("uid3: {}", uid3);
        logger.debug("uid4: {}", uid4);

        assertThat(uid1, is(equalTo(uid2)));
    }

    @Test
    public void simpleApiKeyTest() {
        DownloadAuthorization dla = new DownloadAuthorization(null);
        logger.debug("simple key: {}", dla.getApiKey());
        assertThat(dla.getApiKey(), not(containsString("-")));
    }

}
