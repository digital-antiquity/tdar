package org.tdar.utils;

import static java.lang.Math.log1p;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Helper class for for hashing messages for the purpose of using them for "tagging" the message in a logging
 * context.  The tags are simply 48-bit hashcodes represented as base64-encoded strings. Collisions are estimated
 * to be 1-in-a-million so long as the range of possible messages is approx. 23,000 unique messages or less.
 */
public class TagHelper {

    HashFunction murmur128 = Hashing.murmur3_128(1);
    Base64 base64 = new Base64(false);

    /**
     * Java implementation of birthday collision prediction algorithm from https://en.wikipedia.org/wiki/Birthday_attack
     * @param probabilityExponent integer n where 10^n is desired probability of a hash value collision (e.g. n=-6
     *                            one-in-a-million odds of collision)
     * @param bits number of bits in hash string
     * @return  estimated number of distinct hashes needed to achieve the desired possibility of any two inputs
     *          yielding the same hashcode
     */
    public static double birthday(int probabilityExponent, int bits) {
        double probability = 10 * probabilityExponent;
        double outputs = pow(2, bits);
        return sqrt( 2 * outputs * - log1p(-probability));
    }

    /*
    def birthday(probability_exponent, bits):
        from math import log1p, sqrt
        probability = 10 ** probability_exponent
        outputs     =  2 ** bits
        return sqrt(2. * outputs * -log1p(-probability))

     */

    /**
     * Generate 128-bit hashcode for the specified string, using
     * @param message
     * @return
     */
    public String tagify(String message) {
        if(StringUtils.isBlank(message)) {
            return "emptyhash";
        }
        HashCode hashCode = murmur128.hashString(message, Charset.defaultCharset());
        String str = base64.encodeToString(hashCode.asBytes());
        return str.substring(0, 8);
    }

    public String tagify(long val) {
        HashCode hashCode = murmur128.hashLong(val);
        String str = base64.encodeToString(hashCode.asBytes());
        return str.substring(0,8);

    }
}



