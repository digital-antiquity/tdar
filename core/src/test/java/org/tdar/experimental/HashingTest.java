package org.tdar.experimental;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Created by jim on 1/14/16.
 */
public class HashingTest {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testMurmer() {
        HashFunction hc128 = Hashing.murmur3_128();
        HashFunction hc32 = Hashing.murmur3_32();
        Base32 b32 = new Base32();
        Base64 b64 = new Base64(true);


        HashCode hashCode = hc128.hashString("/upload/upload", Charset.defaultCharset());
        logger.debug(hashCode.toString());
        logger.debug(b32.encodeToString(hashCode.asBytes()));
        logger.debug(b64.encodeToString(hashCode.asBytes()));

        hashCode = hc32.hashString("/upload/upload", Charset.defaultCharset());
        logger.debug(hashCode.toString());

        logger.debug(b32.encodeToString(hashCode.asBytes()));
        logger.debug(b64.encodeToString(hashCode.asBytes()));
    }

    @Test
    public void testNanoEncoding() {
        long nanoTime = System.nanoTime();
        Base64 b = new Base64(true);
        byte[] bytes = ByteBuffer.allocate(Long.BYTES).putLong(nanoTime).array();
        logger.debug("{}", nanoTime);
        logger.debug(b.encodeToString(bytes));
    }
}
