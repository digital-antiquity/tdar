package org.tdar.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;

/**
 * @author Adam Brin
 * 
 */
public class LatLongObfuscationTest {

    private static final double NEARLY_ONE_MILE = 0.01470d;

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
    public void testRandomWithVerySmall(){
        logger.debug("****** TEST testNegLatLongWithSaltedResult *******");
        SpatialObfuscationUtil.useRandom(false);
        // NOTE: THESE FAIL... why???
        SpatialObfuscationUtil.setRandom(0.0000001d);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg2, smallNeg2, smallNeg);
    }

    @Test
    public void testRandomWithVeryLarge(){
        logger.debug("****** TEST testNegLatLongWithSaltedResult *******");
        SpatialObfuscationUtil.useRandom(false);
        // NOTE: THESE FAIL... why???
        SpatialObfuscationUtil.setRandom(0.9999999d);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg2, smallNeg2, smallNeg);
    }
    
    @Test
    public void testNegLatLongWithSaltedResult() {
        logger.debug("****** TEST testNegLatLongWithSaltedResult *******");
        SpatialObfuscationUtil.useRandom(false);
        // note, I fail with negative seeds
        // with "random" there's some chance of this being less useful, thus... we do it a few times

        SpatialObfuscationUtil.setRandom(0.2);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg2, smallNeg2, smallNeg);
        
        SpatialObfuscationUtil.setRandom(0.5);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg2, smallNeg2, smallNeg);
        
        SpatialObfuscationUtil.setRandom(0.8);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg2, smallNeg2, smallNeg);

        // NOTE: THESE FAIL... why???
        SpatialObfuscationUtil.setRandom(0.1);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg2, smallNeg2, smallNeg);
        
        SpatialObfuscationUtil.setRandom(0.9);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg2, smallNeg2, smallNeg);

        SpatialObfuscationUtil.setRandom(null);
        SpatialObfuscationUtil.useRandom(true);
    }
    
    @Test
    public void testAlmostOneMileSquareLatLongWithSaltedResult() {
        logger.debug("****** TEST testLatLongWithSaltedResult *******");
        SpatialObfuscationUtil.useRandom(false);
        // note, I fail with negative seeds
        // with "random" there's some chance of this being less useful, thus... we do it a few times

        SpatialObfuscationUtil.setRandom(0.2);
        _testPositiveLatLongWithSalt(smallPos, smallPos + NEARLY_ONE_MILE, smallPos + NEARLY_ONE_MILE, smallPos);
        
        SpatialObfuscationUtil.setRandom(0.5);
        _testPositiveLatLongWithSalt(smallPos, smallPos + NEARLY_ONE_MILE, smallPos + NEARLY_ONE_MILE, smallPos);
        
        SpatialObfuscationUtil.setRandom(0.8);
        _testPositiveLatLongWithSalt(smallPos, smallPos + NEARLY_ONE_MILE, smallPos + NEARLY_ONE_MILE, smallPos);

        // NOTE: THESE FAIL... why???
        SpatialObfuscationUtil.setRandom(0.1);
        _testPositiveLatLongWithSalt(smallPos, smallPos + NEARLY_ONE_MILE, smallPos + NEARLY_ONE_MILE, smallPos);
        
        SpatialObfuscationUtil.setRandom(0.9);
        _testPositiveLatLongWithSalt(smallPos, smallPos + NEARLY_ONE_MILE, smallPos + NEARLY_ONE_MILE, smallPos);

        SpatialObfuscationUtil.setRandom(null);
        SpatialObfuscationUtil.useRandom(true);
    }
    
    @Test
    public void testAlmostOneMileSquareNegLatLongWithSaltedResult() {
        logger.debug("****** TEST testNegLatLongWithSaltedResult *******");
        SpatialObfuscationUtil.useRandom(false);
        // note, I fail with negative seeds
        // with "random" there's some chance of this being less useful, thus... we do it a few times

        SpatialObfuscationUtil.setRandom(0.2);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg + NEARLY_ONE_MILE, smallNeg + NEARLY_ONE_MILE, smallNeg);
        
        SpatialObfuscationUtil.setRandom(0.5);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg + NEARLY_ONE_MILE, smallNeg + NEARLY_ONE_MILE, smallNeg);
        
        SpatialObfuscationUtil.setRandom(0.8);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg + NEARLY_ONE_MILE, smallNeg + NEARLY_ONE_MILE, smallNeg);

        // NOTE: THESE FAIL... why???
        SpatialObfuscationUtil.setRandom(0.1);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg + NEARLY_ONE_MILE, smallNeg + NEARLY_ONE_MILE, smallNeg);
        
        SpatialObfuscationUtil.setRandom(0.9);
        _testNegativeLatLongWithSalt(smallNeg, smallNeg + NEARLY_ONE_MILE, smallNeg + NEARLY_ONE_MILE, smallNeg);

        SpatialObfuscationUtil.setRandom(null);
        SpatialObfuscationUtil.useRandom(true);
    }

    private void _testNegativeLatLongWithSalt(double  w, double s , double e, double n) {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(w,s,e,n);
        Double east = llb.getEast(); // Original East boudnary (right most). 
        Double west = llb.getWest(); // Original West boundary (left most)
        logger.debug("before: e:{} w:{}", east, west);
        String geoJsonRaw = toGeoJson(llb, false);
        logger.debug(" after: {}", geoJsonRaw);
        llb.obfuscate();
        logger.debug("east: {} ; obs: {}", east, llb.getObfuscatedEast());
        logger.debug("west: {} ; obs: {}", west, llb.getObfuscatedWest());
        // double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallNeg, smallNeg2, LatitudeLongitudeBox.LONGITUDE, true);
        String geoJsonObs = toGeoJson(llb, true);
        logger.debug(" after: {}", geoJsonObs);
        logger.debug("result e:" + llb.getObfuscatedEast() + " > " + east +" ({})", llb.getObfuscatedEast() > east  );
        logger.debug("result w:" + llb.getObfuscatedWest() + " < " + west+" ({})", llb.getObfuscatedWest() < west);
        assertNotEquals(geoJsonRaw, geoJsonObs);
        
        assertTrue("failed with random of " + SpatialObfuscationUtil.getRandom(), llb.getObfuscatedEast() > east);
        assertTrue("failed with random of " + SpatialObfuscationUtil.getRandom(), llb.getObfuscatedWest() < west);
    }


    /** FIXME, not sure I WORK **/
    private void _testPositiveLatLongWithSalt(double  w, double s , double e, double n) {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(w,s,e,n);
        Double east = llb.getEast(); // Original East boudnary (right most). 
        Double west = llb.getWest(); // Original West boundary (left most)
        logger.debug("before: e:{} w:{}", east, west);
        String geoJsonRaw = toGeoJson(llb, false);
        logger.debug(" after: {}", geoJsonRaw);
        llb.obfuscate();
        logger.debug("east: {} ; obs: {}", east, llb.getObfuscatedEast());
        logger.debug("west: {} ; obs: {}", west, llb.getObfuscatedWest());
        // double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallNeg, smallNeg2, LatitudeLongitudeBox.LONGITUDE, true);
        String geoJsonObs = toGeoJson(llb, true);
        logger.debug(" after: {}", geoJsonObs);
        logger.debug("result e:" + llb.getObfuscatedEast() + " > " + east);
        logger.debug("result w:" + llb.getObfuscatedWest() + " < " + west);
        assertNotEquals(geoJsonRaw, geoJsonObs);
        
        assertTrue("failed with random of " + SpatialObfuscationUtil.getRandom(), llb.getObfuscatedEast() > east);
        assertTrue("failed with random of " + SpatialObfuscationUtil.getRandom(), llb.getObfuscatedWest() < west);
    }


    public String toGeoJson(LatitudeLongitudeBox llb, boolean obfs) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"Feature\",");
        sb.append(String.format("\"properties\":{\"obfuscated\":%s},",obfs));
        sb.append(String.format("\"geometry\": {\"%s\":\"%s\",","type", "Polygon"));
        sb.append("\"coordinates\": [[");
        if (!obfs) {
            sb.append(String.format("[%s,%s],", llb.getEast(), llb.getSouth()));
            sb.append(String.format("[%s,%s],", llb.getEast(), llb.getNorth()));
            sb.append(String.format("[%s,%s],", llb.getWest(), llb.getNorth()));
            sb.append(String.format("[%s,%s],", llb.getWest(), llb.getSouth()));
            sb.append(String.format("[%s,%s]]]}}", llb.getEast(), llb.getSouth()));
        } else {
            sb.append(String.format("[%s,%s],", llb.getObfuscatedEast(), llb.getObfuscatedSouth()));
            sb.append(String.format("[%s,%s],", llb.getObfuscatedEast(), llb.getObfuscatedNorth()));
            sb.append(String.format("[%s,%s],", llb.getObfuscatedWest(), llb.getObfuscatedNorth()));
            sb.append(String.format("[%s,%s],", llb.getObfuscatedWest(), llb.getObfuscatedSouth()));
            sb.append(String.format("[%s,%s]]]}}", llb.getObfuscatedEast(), llb.getObfuscatedSouth()));
        }
        return sb.toString();
    }
    
    @Test
    public void testWrappingLong() {
        logger.debug("****** TEST testWrappingLong *******");
        SpatialObfuscationUtil.useRandom(false);
            SpatialObfuscationUtil.setRandom(0.2);
            _testWrappingLong();
            SpatialObfuscationUtil.setRandom(0.5);
            _testWrappingLong();
            SpatialObfuscationUtil.setRandom(0.8);
            _testWrappingLong();

            SpatialObfuscationUtil.setRandom(0.1);
            _testWrappingLong();
            
            SpatialObfuscationUtil.setRandom(0.9);
            
            _testWrappingLong();
            SpatialObfuscationUtil.setRandom(null);
            SpatialObfuscationUtil.useRandom(true);
    }

    private void _testWrappingLong() {
        double two = LatitudeLongitudeBox.MAX_LONGITUDE - (LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES / 2);
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(two, smallNeg, two, smallNeg2);
        Double east = llb.getEast();
        Double west = llb.getWest();
        logger.debug("before: {} - {}", west, east);
        llb.obfuscate();
        logger.debug(" after: {} - {}", llb.getObfuscatedWest(), llb.getObfuscatedEast());
        assertTrue("failed with random of " + SpatialObfuscationUtil.getRandom(), llb.getObfuscatedWest() < east);
        assertTrue("failed with random of " + SpatialObfuscationUtil.getRandom(), llb.getObfuscatedEast() > 179d || (llb.getObfuscatedEast() < -179d && llb.getObfuscatedEast() < east));
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
