package org.tdar.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.service.processes.upgradeTasks.SetupBillingAccountsProcess;

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

    @BeforeClass
    public static void setup() {
    }

    @BeforeClass
    public static void teardown() {
    }
    
    
    @Test
    public void testNegLatLongWithSaltedResult() {
        logger.debug("****** TEST testNegLatLongWithSaltedResult *******");
        SpatialObfuscationUtil.useRandom(false);
        SpatialObfuscationUtil.setRandom(0.5);
        // note, I fail with negative seeds
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
        SpatialObfuscationUtil.setRandom(null);
        SpatialObfuscationUtil.useRandom(true);
        if (valid < 3) {
            fail("issue with obfuscation, most of the randoms failed");
        }

    }

    @Test
    public void testWrappingLong() {
        logger.debug("****** TEST testWrappingLong *******");
        SpatialObfuscationUtil.useRandom(false);
        SpatialObfuscationUtil.setRandom(0.9999);

        int count = 100;
        int valid = 0;
        // with "random" there's some chance of this being less useful, thus... we do it a few times
        while (count > 0) {

            double two = LatitudeLongitudeBox.MAX_LONGITUDE - (LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES / 2);
            LatitudeLongitudeBox llb = new LatitudeLongitudeBox(two, smallNeg, two, smallNeg2);
            Double east = llb.getEast();
            Double west = llb.getWest();
            logger.debug("before: {} - {}", west, east);
            llb.obfuscate();
            logger.debug(" after: {} - {}", llb.getObfuscatedWest(), llb.getObfuscatedEast());
            if (llb.getObfuscatedWest() < east && (llb.getObfuscatedEast() > 179d || (llb.getObfuscatedEast() < -179d && llb.getObfuscatedEast() < east))) {
                valid++;
            }
            // assertTrue("result:" + result + " > " + two, result < two);
            count--;
        }

        SpatialObfuscationUtil.setRandom(null);
        SpatialObfuscationUtil.useRandom(true);

        if (valid < 75) {
            fail(String.format("issue with obfuscation, most of the randoms failed : %s/%s", valid, 100));
        }
        logger.debug("valid matches: {}", valid);

    }

    @Test
    public void testLatLongWithoutSaltedResult2() {
        logger.debug("****** TEST testLatLongWithoutSaltedResult2 *******");

        // double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallPos, smallNeg, LatitudeLongitudeBox.LONGITUDE, true);
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(smallPos + 1, smallNeg - 1, smallPos, smallNeg2);
        logger.debug("before: {} - {}", llb.getWest(), llb.getEast());
        llb.obfuscate();
        logger.debug(" after: {} - {}", llb.getObfuscatedWest(), llb.getObfuscatedEast());
        assertFalse(llb.getObfuscatedObjectDifferent());
    }

}
