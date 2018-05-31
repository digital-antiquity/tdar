package org.tdar.utils;

import static java.lang.Math.abs;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.tdar.core.bean.coverage.LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;

@SuppressWarnings({ "static-method" })
public class LatitudeLongitudeBoxTest {

    private static final double _36 = 36.08765565625065;
    private static final double _36_small = _36 + 0.01;
    private static final double _neg_107 = -107.78792202517137;
    private static final double _neg_107_small = _neg_107 + 0.01;
    private static final double EPSILON = 0;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testNewObfuscation() {
        double half = LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES / 3;
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(half, half, half, half);
        logger.debug("before: {}", llb);
        SpatialObfuscationUtil.obfuscate(llb);
        assertTrue(half > llb.getObfuscatedWest());
        assertTrue(half < llb.getObfuscatedEast());
        assertTrue(half > llb.getObfuscatedSouth());
        assertTrue(half < llb.getObfuscatedNorth());
        logger.debug(" after: {}", String.format("Latitude [%s to %s], Longitude [%s to %s]", llb.getObfuscatedSouth(), llb.getObfuscatedNorth(),
                llb.getObfuscatedWest(), llb.getObfuscatedEast()));
    }

    @Test
    public void testValidity() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox();
        llb.setEast(_neg_107);
        llb.setWest(_neg_107);
        llb.setNorth(36.08765565625065);
        llb.setSouth(36.08765565625065);
        assertTrue("valid north: {}", llb.isValidLatitude(llb.getNorth()));
        assertTrue("valid south: {}", llb.isValidLatitude(llb.getSouth()));
        assertTrue("valid west: {}", llb.isValidLongitude(llb.getWest()));
        assertTrue("valid east: {}", llb.isValidLongitude(llb.getEast()));
        assertTrue("valid n>s: {}", llb.getNorth() >= llb.getSouth());
        assertTrue("valid span: {}", llb.isValidLongitudeSpan(llb.getWest(), llb.getEast()));
        assertTrue("valid n/s span: {}", Math.abs(llb.getNorth() - llb.getSouth()) < 180);
        assertTrue(llb.isValid());
    }

    @Test
    public void testRandomPoint() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox();
        llb.setEast(_neg_107);
        llb.setWest(_neg_107);
        llb.setNorth(_36);
        llb.setSouth(_36);
        logger.debug("before: {}", llb);
        llb.obfuscate();
        logger.debug(" after: {}", llb);
        assertNotEquals(_neg_107, llb.getObfuscatedEast());
        assertNotEquals(_neg_107, llb.getObfuscatedWest());
        assertNotEquals(_36, llb.getObfuscatedNorth());
        assertNotEquals(_36, llb.getObfuscatedSouth());
    }

    @Test
    public void testRandomSmall() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox();
        llb.setEast(_neg_107_small);
        llb.setWest(_neg_107);
        llb.setNorth(_36_small);
        llb.setSouth(_36);
        logger.debug("before: {}", llb);
        
        llb.obfuscate();
        logger.debug(" after: {}", llb);
        assertNotEquals(_neg_107_small, llb.getObfuscatedEast());
        assertNotEquals(_neg_107, llb.getObfuscatedWest());
        assertNotEquals(_36_small, llb.getObfuscatedNorth());
        assertNotEquals(_36, llb.getObfuscatedSouth());
    }

    /**
     * Should always be true.
     */
    @SuppressWarnings("static-method")
    @Test
    public void isActuallyObfuscatedByDefault() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(Double.valueOf(0), Double.valueOf(0), Double.valueOf(0), Double.valueOf(0));
        assertFalse(llb.isOkayToShowExactLocation());
        
        llb.obfuscate();
        assertTrue(llb.isObfuscatedObjectDifferent());
    }

    @Test
    public void testExactLLB() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(10d, 10d, 10d, 10d);
        
        llb.obfuscate();

        assertGreaterThanOneMile(llb);
    }

    @Test
    public void testCenterPointIDL() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(111.97277777777776, -52.052499999999995, -87.71493055555561, 53.33082050317123);
        // logger.debug(" lat {}", llb.getCenterLatitude());
        // logger.debug("long {}", llb.getCenterLongitude());
        // assertNotEquals(0.6391602515856185, llb.getCenterLatitude());
        assertNotEquals(-9.843854166666688, llb.getCenterLongitude());
    }

    @Test
    public void testExactLLB2() {
        // not 100% necessary, but attempting to get at the randomness
        SpatialObfuscationUtil.useRandom(false);
        SpatialObfuscationUtil.setRandom(0.9999);

        for (int i = 0; i < 100; i++) {
            LatitudeLongitudeBox llb = new LatitudeLongitudeBox(-36.845703125d, -3.64903402157866d, -36.845703125d, 3.64903402157866d);

            
            llb.obfuscate();

            assertGreaterThanOneMile(llb);
            llb = new LatitudeLongitudeBox(-9.667d, 25.35d, -9.66666666666667d, 25.35d);

            
            llb.obfuscate();
            assertGreaterThanOneMile(llb);
        }
        SpatialObfuscationUtil.useRandom(true);
        SpatialObfuscationUtil.setRandom(null);

    }

    private void assertGreaterThanOneMile(LatitudeLongitudeBox llb) {
        assertThat(distance(llb.getObfuscatedNorth(), llb.getObfuscatedSouth()), greaterThanOrEqualTo(ONE_MILE_IN_DEGREE_MINUTES));
        assertThat(distance(llb.getObfuscatedEast(), llb.getObfuscatedWest()), greaterThanOrEqualTo(ONE_MILE_IN_DEGREE_MINUTES));
    }

    private double distance(double lht, double rht) {
        return round(abs(lht - rht));
    }

    double round(double num) {
        DecimalFormat df = new DecimalFormat("#.#####");
        df.setRoundingMode(RoundingMode.CEILING);
        double d = Double.parseDouble(df.format(num));
        logger.debug("{} -> {}", num, d);
        return d;

    }

    @SuppressWarnings("deprecation")
    @Test
    public void testPoint() {
        Double lat = 45.954992d;
        Double lng = -71.392991d;
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(lng, lat, lng, lat);

        
        Double south = llb.getSouth();
        llb.obfuscate();
        double min = llb.getObfuscatedSouth().doubleValue();
        double max = llb.getObfuscatedNorth().doubleValue();
        Assert.assertFalse(south.doubleValue() == min);
        Assert.assertFalse(lat.doubleValue() == max);
        Assert.assertFalse(lat.doubleValue() == llb.getObfuscatedWest().doubleValue());
        Assert.assertFalse(lat.doubleValue() == llb.getObfuscatedEast().doubleValue());
        Assert.assertFalse(max == min);
        Assert.assertFalse(llb.getObfuscatedWest().doubleValue() == llb.getObfuscatedEast().doubleValue());
        logger.debug("{} <-> {}", max, min);
        logger.debug("{} <--> {}", max - min, ONE_MILE_IN_DEGREE_MINUTES);
        assertGreaterThanOneMile(llb);
    }

    @SuppressWarnings("static-method")
    @Test
    public void exactLocationCanBeShown() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(Double.valueOf(0), Double.valueOf(0), Double.valueOf(0), Double.valueOf(0));
        llb.setOkayToShowExactLocation(true);
        assertTrue(llb.isOkayToShowExactLocation());
        assertFalse(llb.isObfuscatedObjectDifferent());
    }

    @Test
    public void testInvalid() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox();
        try {
            llb.setNorth(1000.0);
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }

    /**
     * This might seem like a silly test, but is the obfuscation is stable from one call to the next?
     * This test does not ensure that that the obfuscation is stable when the lat long box is persisted and restored, though
     */
    @SuppressWarnings("static-method")
    @Test
    public void obfuscationIsStable() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);

        
        llb.obfuscate();
        Double maxObfuscatedLatitude = llb.getObfuscatedNorth();
        Double minObfuscatedLatitude = llb.getObfuscatedSouth();
        Double maxObfuscatedLongitude = llb.getObfuscatedEast();
        Double minObfuscatedLongitude = llb.getObfuscatedWest();
        assertTrue(maxObfuscatedLatitude.equals(llb.getObfuscatedNorth()));
        assertTrue(minObfuscatedLatitude.equals(llb.getObfuscatedSouth()));
        assertTrue(maxObfuscatedLongitude.equals(llb.getObfuscatedEast()));
        assertTrue(minObfuscatedLongitude.equals(llb.getObfuscatedWest()));
    }

    @SuppressWarnings("static-method")
    @Test
    public void doesNotObfuscateAboveOneMile() {
        Double slightlyMoreThanOneMile = ONE_MILE_IN_DEGREE_MINUTES + 0.00001d;
        Double zero = 0.0;
        LatitudeLongitudeBox llb  = new LatitudeLongitudeBox(slightlyMoreThanOneMile, slightlyMoreThanOneMile, zero, zero);
        llb.obfuscate();
        assertEquals(slightlyMoreThanOneMile ,llb.getObfuscatedWest());
        assertEquals(zero, llb.getObfuscatedEast());
        assertEquals(slightlyMoreThanOneMile ,llb.getObfuscatedSouth());
        assertEquals(zero, llb.getObfuscatedNorth());
    }

    @SuppressWarnings("static-method")
    @Test
    public void doesObfuscateAtLessThanOneMile() {
        Double slightlyLessThanOneMile = ONE_MILE_IN_DEGREE_MINUTES - 0.00001d;
        Double zero = 0.0;
        LatitudeLongitudeBox llb  = new LatitudeLongitudeBox(slightlyLessThanOneMile, slightlyLessThanOneMile, zero, zero);
        llb.obfuscate();
        assertNotEquals(slightlyLessThanOneMile ,llb.getObfuscatedWest());
        assertNotEquals(zero, llb.getObfuscatedEast());
        assertNotEquals(slightlyLessThanOneMile ,llb.getObfuscatedSouth());
        assertNotEquals(zero, llb.getObfuscatedNorth());
    }

    /**
     * and if an original lat long coordinate is changed, the obfuscation for it should be redone (surely).
     */
    @SuppressWarnings("static-method")
    @Test
    // @Ignore
    public void doesReObfuscateOnNewCoordinates() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);

        
        llb.obfuscate();
        Double maxObfuscatedLatitude = llb.getObfuscatedNorth();
        Double minObfuscatedLatitude = llb.getObfuscatedSouth();
        Double maxObfuscatedLongitude = llb.getObfuscatedEast();
        Double minObfuscatedLongitude = llb.getObfuscatedWest();
        llb = new LatitudeLongitudeBox(0.1, 0.1, 0.1, 0.1);
        llb.obfuscate();

        assertNotEquals(maxObfuscatedLatitude, llb.getObfuscatedNorth());
        assertNotEquals(minObfuscatedLatitude, llb.getObfuscatedSouth());
        assertNotEquals(maxObfuscatedLongitude, llb.getObfuscatedEast());
        assertNotEquals(minObfuscatedLongitude, llb.getObfuscatedWest());
    }

    /**
     * and if an original xy coordinate is changed, the obfuscation for it should be redone (surely?).
     */
    @SuppressWarnings("static-method")
    @Test
    // @Ignore
    public void doesReObfuscateOnNewMaxXandYs() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);

        
        llb.obfuscate();
        Double maxObfuscatedLatitude = llb.getObfuscatedNorth();
        Double minObfuscatedLatitude = llb.getObfuscatedSouth();
        Double maxObfuscatedLongitude = llb.getObfuscatedEast();
        Double minObfuscatedLongitude = llb.getObfuscatedWest();

        llb = new LatitudeLongitudeBox(0.1, 0.1, 0.1, 0.1);
        llb.obfuscate();
        assertNotEquals(maxObfuscatedLatitude,llb.getObfuscatedNorth());
        assertNotEquals(minObfuscatedLatitude,llb.getObfuscatedSouth());
        assertNotEquals(maxObfuscatedLongitude,llb.getObfuscatedEast());
        assertNotEquals(minObfuscatedLongitude,llb.getObfuscatedWest());
    }

    @SuppressWarnings({ "static-method", "deprecation" })
    @Test
    public void doesObfuscateAccordingToService() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);

        
        llb.obfuscate();
        Double maxObfuscatedLatitude = llb.getObfuscatedNorth();
        Double minObfuscatedLatitude = llb.getObfuscatedSouth();
        Double maxObfuscatedLongitude = llb.getObfuscatedEast();
        Double minObfuscatedLongitude = llb.getObfuscatedWest();

        assertTrue(llb.isObfuscatedObjectDifferent());
        assertTrue(llb.isObfuscated());
        assertTrue(maxObfuscatedLatitude.equals(llb.getNorth()));
        assertTrue(minObfuscatedLatitude.equals(llb.getSouth()));
        assertTrue(maxObfuscatedLongitude.equals(llb.getEast()));
        assertTrue(minObfuscatedLongitude.equals(llb.getWest()));
    }

    @SuppressWarnings({ "static-method", "deprecation" })
    @Test
    public void doesNotObfuscateAccordingToServiceIfIsOkToShowExactCoords() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);
        llb.setOkayToShowExactLocation(true);

        
        llb.obfuscate();
        // shouldn't be obfuscated
        assertFalse(llb.isObfuscatedObjectDifferent());
        llb.obfuscate();
        // and should be no change.
        assertFalse(llb.isObfuscatedObjectDifferent());
        Assert.assertEquals(llb.getNorth(), llb.getObfuscatedNorth());
        Assert.assertEquals(llb.getNorth(), new Double(0.0));
        Assert.assertEquals(llb.getEast(), llb.getObfuscatedEast());
        Assert.assertEquals(llb.getEast(), new Double(0.0));

    }

    @Test
    public void doesReturnCenterIfBoxGreaterThanOneMileByDefault() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, ONE_MILE_IN_DEGREE_MINUTES + 0.00002,
                ONE_MILE_IN_DEGREE_MINUTES + 0.00002);

        
        llb.obfuscate();
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLatitudeIfNotObfuscated()));
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLongitudeIfNotObfuscated()));
    }

    @Test
    public void doesReturnCenterIfBoxGreaterThanOneMileEvenIfObfuscated() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, ONE_MILE_IN_DEGREE_MINUTES + 0.00002,
                ONE_MILE_IN_DEGREE_MINUTES + 0.00002);
        llb.setOkayToShowExactLocation(false);

        
        llb.obfuscate();
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLatitudeIfNotObfuscated()));
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLongitudeIfNotObfuscated()));
    }

    @Test
    public void doesNotReturnCenterIfLessThanOneMileByDefault() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, ONE_MILE_IN_DEGREE_MINUTES - 0.00005,
                ONE_MILE_IN_DEGREE_MINUTES - 0.00005);
        
        llb.obfuscate();
        assertTrue(null == llb.getCenterLatitudeIfNotObfuscated());
        assertTrue(null == llb.getCenterLongitudeIfNotObfuscated());
    }

    @Test
    public void doesNotReturnCenterIfLessThanOneMileIfObfuscated() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, ONE_MILE_IN_DEGREE_MINUTES - 0.00001,
                ONE_MILE_IN_DEGREE_MINUTES + 0.00001);
        llb.setOkayToShowExactLocation(false);
        
        llb.obfuscate();
        assertTrue(null == llb.getCenterLatitudeIfNotObfuscated());
        assertTrue(null == llb.getCenterLongitudeIfNotObfuscated());
    }

    @SuppressWarnings("static-method")
    @Test
    public void doesObfuscatedCenterIfBoxLessThanOneMileByDefault() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);

        
        llb.obfuscate();
        assertFalse(Double.valueOf(0.0).equals(llb.getObfuscatedCenterLatitude()));
        assertFalse(Double.valueOf(0.0).equals(llb.getObfuscatedCenterLongitude()));
    }

    @Test
    public void avoidChangesOnDoubleCall() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);

        
        llb.obfuscate();
        Double obfuscatedCenterLatitude = llb.getObfuscatedCenterLatitude();
        assertFalse(Double.valueOf(0.0).equals(obfuscatedCenterLatitude));
        Double obfuscatedCenterLongitude = llb.getObfuscatedCenterLongitude();
        assertFalse(Double.valueOf(0.0).equals(obfuscatedCenterLongitude));
        
        llb.obfuscate();
        assertEquals(obfuscatedCenterLatitude, llb.getObfuscatedCenterLatitude());
        assertEquals(obfuscatedCenterLongitude, llb.getObfuscatedCenterLongitude());
    }

    @SuppressWarnings("static-method")
    @Test
    public void doesNoObfuscatedCenterIfOkToShowExactLocation() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);
        llb.setOkayToShowExactLocation(true);

        
        llb.obfuscate();
        assertTrue(Double.valueOf(0.0).equals(llb.getObfuscatedCenterLatitude()));
        assertTrue(Double.valueOf(0.0).equals(llb.getObfuscatedCenterLongitude()));
    }

    @Test
    public void doesNotObfuscateCenterIfBoxGreaterThanOneMile() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, ONE_MILE_IN_DEGREE_MINUTES + 0.00002,
                ONE_MILE_IN_DEGREE_MINUTES + 0.00002);

        
        llb.obfuscate();
        assertTrue(Double.valueOf(0.00737).equals(llb.getObfuscatedCenterLatitude()));
        assertTrue(Double.valueOf(0.00737).equals(llb.getObfuscatedCenterLongitude()));
    }

    @Test
    public void doesNotObfuscateCenterIfBoxGreaterThanOneMileEvenIfNotOkToShowExactLocation() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, ONE_MILE_IN_DEGREE_MINUTES + 0.00002,
                ONE_MILE_IN_DEGREE_MINUTES + 0.00002);
        llb.setOkayToShowExactLocation(false);

        
        llb.obfuscate();
        assertTrue(Double.valueOf(0.00737).equals(llb.getObfuscatedCenterLatitude()));
        assertTrue(Double.valueOf(0.00737).equals(llb.getObfuscatedCenterLongitude()));
    }

    @SuppressWarnings({ "static-method", "deprecation" })
    @Test
    public void doesReturnCoordsInRightPlace() {
        double minimumLongitude = 0.00001;
        double maximumLongitude = 0.00002;
        double minimumLatitude = 0.00003;
        double maximumLatitude = 0.00004;
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(minimumLongitude, minimumLatitude, maximumLongitude, maximumLatitude);
        llb.setOkayToShowExactLocation(true);

        
        llb.obfuscate();
        assertTrue(Double.valueOf(minimumLongitude).equals(llb.getWest()));
        assertTrue(Double.valueOf(maximumLongitude).equals(llb.getEast()));
        assertTrue(Double.valueOf(minimumLatitude).equals(llb.getSouth()));
        assertTrue(Double.valueOf(maximumLatitude).equals(llb.getNorth()));
        assertTrue(Double.valueOf(minimumLongitude).equals(llb.getObfuscatedWest()));
        assertTrue(Double.valueOf(maximumLongitude).equals(llb.getObfuscatedEast()));
        assertTrue(Double.valueOf(minimumLatitude).equals(llb.getObfuscatedSouth()));
        assertTrue(Double.valueOf(maximumLatitude).equals(llb.getObfuscatedNorth()));
    }

    @Test
    public void testCenterWithDateline() {
        double minimumLongitude = -170d;
        double maximumLongitude = 170d;
        double minimumLatitude = 1;
        double maximumLatitude = 11;
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(minimumLongitude, minimumLatitude, maximumLongitude, maximumLatitude);

        
        llb.obfuscate();
        logger.debug("{}", llb.getObfuscatedCenterLatitude());
        logger.debug("{}", llb.getObfuscatedCenterLongitude());
        assertTrue(Double.valueOf(6.0).equals(llb.getObfuscatedCenterLatitude()));
        assertTrue(Double.valueOf(0.0).equals(llb.getObfuscatedCenterLongitude()));
    }

    @SuppressWarnings({ "static-method", "deprecation" })
    @Test
    public void doesReturnCoordsInRightPlaceIfBoxGreaterThanOneMile() {
        double minimumLongitude = 0.00001;
        double maximumLongitude = ONE_MILE_IN_DEGREE_MINUTES + 0.00002;
        double minimumLatitude = 0.00003;
        double maximumLatitude = ONE_MILE_IN_DEGREE_MINUTES + 0.00004;
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(minimumLongitude, minimumLatitude, maximumLongitude, maximumLatitude);

        
        llb.obfuscate();
        assertTrue(Double.valueOf(minimumLongitude).equals(llb.getWest()));
        assertTrue(Double.valueOf(maximumLongitude).equals(llb.getEast()));
        assertTrue(Double.valueOf(minimumLatitude).equals(llb.getSouth()));
        assertTrue(Double.valueOf(maximumLatitude).equals(llb.getNorth()));
        assertTrue(Double.valueOf(minimumLongitude).equals(llb.getObfuscatedWest()));
        assertTrue(Double.valueOf(maximumLongitude).equals(llb.getObfuscatedEast()));
        assertTrue(Double.valueOf(minimumLatitude).equals(llb.getObfuscatedSouth()));
        assertTrue(Double.valueOf(maximumLatitude).equals(llb.getObfuscatedNorth()));
    }

}
