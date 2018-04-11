package org.tdar.core.bean.coverage;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Adam Brin
 * 
 */
public class LatLongObfuscationTest {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public final double smallNeg = -35.0050d;
    public final double smallNeg2 = -35.000d;

    public final double smallPos = 35.000d;
    public final double smallPos2 = 35.0050d;

    @Test
    public void testNegLatLongWithSaltedResult() {
        int count = 10;
        int valid = 0;
        // with "random" there's some chance of this being less useful, thus... we do it a few times
        while (count > 0) {

            LatitudeLongitudeBox llb = new LatitudeLongitudeBox(smallNeg, smallNeg2, smallNeg2, smallNeg);
            Double east = llb.getEast();
            Double west = llb.getWest();
            logger.debug("before: e:{} w:{}", east, west);
            llb.obfuscate();
            logger.debug(" after: {}", llb);
            logger.debug("east: {} ; obs: {}", east, llb.getObfuscatedEast());
            logger.debug("west: {} ; obs: {}", west, llb.getObfuscatedWest());
            // double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallNeg, smallNeg2, LatitudeLongitudeBox.LONGITUDE, true);
            logger.debug("result:" + llb.getObfuscatedEast() + " > " + east);
            logger.debug("result:" + llb.getObfuscatedWest() + " < " + west);
            if (llb.getObfuscatedEast() > east && llb.getObfuscatedWest() < west) {
                valid++;
            }
            count--;
        }
        if (valid < 3) {
            fail("issue with obfuscation, most of the randoms failed");
        }

    }

    @SuppressWarnings("static-method")
    @Test
    public void testWrappingLong() {
        int count = 10;
        int valid = 0;
        // with "random" there's some chance of this being less useful, thus... we do it a few times
        while (count > 0) {

            double two = LatitudeLongitudeBox.MAX_LONGITUDE - (LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES / 2);
            LatitudeLongitudeBox llb = new LatitudeLongitudeBox(two, smallNeg, two, smallNeg2);
            logger.debug("before: {} - {}", llb.getWest(), llb.getEast());
            llb.obfuscate();
            logger.debug(" after: {} - {}", llb.getObfuscatedWest(), llb.getObfuscatedEast());
            if (llb.getObfuscatedWest() > llb.getObfuscatedEast() && llb.getObfuscatedEast() < 170d) {
                valid++;
            }
            // assertTrue("result:" + result + " > " + two, result < two);
            count--;
        }

        if (valid < 3) {
            fail("issue with obfuscation, most of the randoms failed");
        }

    }

    @SuppressWarnings("static-method")
    @Test
    public void testWrappingLong2() {
        int count = 10;
        int valid = 0;
        // with "random" there's some chance of this being less useful, thus... we do it a few times
        while (count > 0) {
            double two = LatitudeLongitudeBox.MIN_LONGITUDE + (LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES / 2);
            LatitudeLongitudeBox llb = new LatitudeLongitudeBox(two, smallNeg, two, smallNeg2);
            logger.debug("before: {} - {}", llb.getWest(), llb.getEast());
            llb.obfuscate();
            logger.debug(" after: {} - {}", llb.getObfuscatedWest(), llb.getObfuscatedEast());
            if (llb.getObfuscatedWest() > llb.getObfuscatedEast() && llb.getObfuscatedEast() < 170d) {
                valid++;
            }
            count--;
        }

        if (valid < 3) {
            fail("issue with obfuscation, most of the randoms failed");
        }
        // double result = LatitudeLongitudeBox.randomizeIfNeedBe(LatitudeLongitudeBox.MIN_LONGITUDE, two, LatitudeLongitudeBox.LONGITUDE, true);
        // assertTrue("result:" + result + " < " + two, result > two);
    }

    @Test
    public void testLatLongWithoutSaltedResult2() {
        // double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallPos, smallNeg, LatitudeLongitudeBox.LONGITUDE, true);
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(smallPos + 1, smallNeg - 1, smallPos, smallNeg2);
        logger.debug("before: {} - {}", llb.getWest(), llb.getEast());
        llb.obfuscate();
        logger.debug(" after: {} - {}", llb.getObfuscatedWest(), llb.getObfuscatedEast());
        assertFalse(llb.getObfuscatedObjectDifferent());
    }

}
