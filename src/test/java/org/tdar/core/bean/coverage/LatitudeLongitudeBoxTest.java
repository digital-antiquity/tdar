package org.tdar.core.bean.coverage;

import static java.lang.Math.abs;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.tdar.core.bean.coverage.LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatitudeLongitudeBoxTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

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

    @SuppressWarnings("static-method")
    @Ignore
    @Test
    public void testExactLLB() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(Double.valueOf(10), Double.valueOf(10), Double.valueOf(10), Double.valueOf(10));
        // FIXME: when enabled why does next assertion fail?
        // llb.obfuscate();

        Assert.assertNotEquals(llb.getMaximumLatitude().doubleValue(), llb.getMaxObfuscatedLatitude().doubleValue());
        Assert.assertNotEquals(llb.getMaximumLongitude().doubleValue(), llb.getMaxObfuscatedLongitude().doubleValue());
        Assert.assertNotEquals(llb.getMinimumLatitude().doubleValue(), llb.getMinObfuscatedLatitude().doubleValue());
        Assert.assertNotEquals(llb.getMinimumLongitude().doubleValue(), llb.getMinObfuscatedLongitude().doubleValue());
        // Assert that the minimum distance in any direction for an obfuscated value should be one Mile
        assertTrue(abs(abs(llb.getMaxObfuscatedLatitude()) - abs(llb.getMinObfuscatedLatitude())) > ONE_MILE_IN_DEGREE_MINUTES);
        assertTrue(abs(abs(llb.getMaxObfuscatedLongitude()) - abs(llb.getMinObfuscatedLongitude())) > ONE_MILE_IN_DEGREE_MINUTES);
    }

    @Test
    @Ignore
    public void testPoint() {
        Double lat = 45.954992d;
        Double lng = -71.392991d;
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(lng, lat, lng, lat);
        double min = llb.getMinObfuscatedLatitude().doubleValue();
        double max = llb.getMaxObfuscatedLatitude().doubleValue();
        Assert.assertNotEquals(llb.getMinimumLatitude().doubleValue(), min);
        Assert.assertNotEquals(lat.doubleValue(), max);
        Assert.assertNotEquals(lat.doubleValue(), llb.getMinObfuscatedLongitude().doubleValue());
        Assert.assertNotEquals(lat.doubleValue(), llb.getMaxObfuscatedLongitude().doubleValue());
        Assert.assertNotEquals(max, min);
        Assert.assertNotEquals(llb.getMinObfuscatedLongitude().doubleValue(), llb.getMaxObfuscatedLongitude().doubleValue());
        logger.debug("{} <-> {}", max, min);
        logger.debug("{} <--> {}", max - min, ONE_MILE_IN_DEGREE_MINUTES);
        Assert.assertFalse(abs(abs(max) - abs(min)) < ONE_MILE_IN_DEGREE_MINUTES);
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
            llb.setMaximumLatitude(1000.0);
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
        Double maxObfuscatedLatitude = llb.getMaxObfuscatedLatitude();
        Double minObfuscatedLatitude = llb.getMinObfuscatedLatitude();
        Double maxObfuscatedLongitude = llb.getMaxObfuscatedLongitude();
        Double minObfuscatedLongitude = llb.getMinObfuscatedLongitude();
        assertTrue(maxObfuscatedLatitude.equals(llb.getMaxObfuscatedLatitude()));
        assertTrue(minObfuscatedLatitude.equals(llb.getMinObfuscatedLatitude()));
        assertTrue(maxObfuscatedLongitude.equals(llb.getMaxObfuscatedLongitude()));
        assertTrue(minObfuscatedLongitude.equals(llb.getMinObfuscatedLongitude()));
    }

    @SuppressWarnings("static-method")
    @Test
    public void doesNotObfuscateAboveOneMile() {
        Double slightlyMoreThanOneMile = ONE_MILE_IN_DEGREE_MINUTES + 0.00001d;
        Double zero = 0.0;
        assertTrue(slightlyMoreThanOneMile.equals(LatitudeLongitudeBox.randomizeIfNeedBe(slightlyMoreThanOneMile, zero, LatitudeLongitudeBox.LATITUDE)));
        assertTrue(slightlyMoreThanOneMile.equals(LatitudeLongitudeBox.randomizeIfNeedBe(slightlyMoreThanOneMile, zero, LatitudeLongitudeBox.LONGITUDE)));
        assertTrue(zero.equals(LatitudeLongitudeBox.randomizeIfNeedBe(zero, slightlyMoreThanOneMile, LatitudeLongitudeBox.LATITUDE)));
        assertTrue(zero.equals(LatitudeLongitudeBox.randomizeIfNeedBe(zero, slightlyMoreThanOneMile, LatitudeLongitudeBox.LONGITUDE)));
    }

    @SuppressWarnings("static-method")
    @Test
    public void doesObfuscateAtLessThanOneMile() {
        Double slightlyLessThanOneMile = ONE_MILE_IN_DEGREE_MINUTES - 0.00001d;
        Double zero = 0.0;
        assertFalse(slightlyLessThanOneMile.equals(LatitudeLongitudeBox.randomizeIfNeedBe(slightlyLessThanOneMile, zero, LatitudeLongitudeBox.LATITUDE)));
        assertFalse(slightlyLessThanOneMile.equals(LatitudeLongitudeBox.randomizeIfNeedBe(slightlyLessThanOneMile, zero, LatitudeLongitudeBox.LONGITUDE)));
        assertFalse(zero.equals(LatitudeLongitudeBox.randomizeIfNeedBe(zero, slightlyLessThanOneMile, LatitudeLongitudeBox.LATITUDE)));
        assertFalse(zero.equals(LatitudeLongitudeBox.randomizeIfNeedBe(zero, slightlyLessThanOneMile, LatitudeLongitudeBox.LONGITUDE)));
    }

    /**
     * and if an original lat long coordinate is changed, the obfuscation for it should be redone (surely).
     */
    @SuppressWarnings("static-method")
    @Test
    public void doesReObfuscateOnNewCoordinates() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);
        Double maxObfuscatedLatitude = llb.getMaxObfuscatedLatitude();
        Double minObfuscatedLatitude = llb.getMinObfuscatedLatitude();
        Double maxObfuscatedLongitude = llb.getMaxObfuscatedLongitude();
        Double minObfuscatedLongitude = llb.getMinObfuscatedLongitude();
        llb.setMaximumLatitude(0.1);
        llb.setMinimumLatitude(0.1);
        llb.setMaximumLongitude(0.1);
        llb.setMinimumLongitude(0.1);
        assertFalse(maxObfuscatedLatitude.equals(llb.getMaxObfuscatedLatitude()));
        assertFalse(minObfuscatedLatitude.equals(llb.getMinObfuscatedLatitude()));
        assertFalse(maxObfuscatedLongitude.equals(llb.getMaxObfuscatedLongitude()));
        assertFalse(minObfuscatedLongitude.equals(llb.getMinObfuscatedLongitude()));
    }

    /**
     * and if an original xy coordinate is changed, the obfuscation for it should be redone (surely?).
     */
    @SuppressWarnings("static-method")
    @Test
    public void doesReObfuscateOnNewMaxXandYs() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);
        Double maxObfuscatedLatitude = llb.getMaxObfuscatedLatitude();
        Double minObfuscatedLatitude = llb.getMinObfuscatedLatitude();
        Double maxObfuscatedLongitude = llb.getMaxObfuscatedLongitude();
        Double minObfuscatedLongitude = llb.getMinObfuscatedLongitude();
        llb.setMaxx(0.1);
        llb.setMinx(0.1);
        llb.setMaxy(0.1);
        llb.setMiny(0.1);
        assertFalse(maxObfuscatedLatitude.equals(llb.getMaxObfuscatedLatitude()));
        assertFalse(minObfuscatedLatitude.equals(llb.getMinObfuscatedLatitude()));
        assertFalse(maxObfuscatedLongitude.equals(llb.getMaxObfuscatedLongitude()));
        assertFalse(minObfuscatedLongitude.equals(llb.getMinObfuscatedLongitude()));
    }

    @SuppressWarnings({ "static-method", "deprecation" })
    @Test
    public void doesObfuscateAccordingToService() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);
        Double maxObfuscatedLatitude = llb.getMaxObfuscatedLatitude();
        Double minObfuscatedLatitude = llb.getMinObfuscatedLatitude();
        Double maxObfuscatedLongitude = llb.getMaxObfuscatedLongitude();
        Double minObfuscatedLongitude = llb.getMinObfuscatedLongitude();
        assertFalse(llb.isObfuscated());
        llb.obfuscate();
        assertTrue(llb.isObfuscatedObjectDifferent());
        assertTrue(llb.isObfuscated());
        assertTrue(maxObfuscatedLatitude.equals(llb.getMaximumLatitude()));
        assertTrue(minObfuscatedLatitude.equals(llb.getMinimumLatitude()));
        assertTrue(maxObfuscatedLongitude.equals(llb.getMaximumLongitude()));
        assertTrue(minObfuscatedLongitude.equals(llb.getMinimumLongitude()));
    }

    @SuppressWarnings({ "static-method" })
    @Test
    public void doesNotObfuscateAccordingToServiceIfIsOkToShowExactCoords() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);
        llb.setOkayToShowExactLocation(true);
        // shouldn't be obfuscated
        assertFalse(llb.isObfuscatedObjectDifferent());
        llb.obfuscate();
        // and should be no change.
        assertTrue(llb.isObfuscatedObjectDifferent());
        Assert.assertEquals(llb.getMaximumLatitude(), llb.getMaxObfuscatedLatitude());
        Assert.assertEquals(llb.getMaximumLatitude(), new Double(0.0));
        Assert.assertEquals(llb.getMaximumLongitude(), llb.getMaxObfuscatedLongitude());
        Assert.assertEquals(llb.getMaximumLongitude(), new Double(0.0));

    }

    @SuppressWarnings({ "static-method" })
    @Test
    public void doesReturnCenterIfBoxGreaterThanOneMileByDefault() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, ONE_MILE_IN_DEGREE_MINUTES + 0.00002,
                ONE_MILE_IN_DEGREE_MINUTES + 0.00002);
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLatitudeIfNotObfuscated()));
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLongitudeIfNotObfuscated()));
    }

    @SuppressWarnings({ "static-method" })
    @Test
    public void doesReturnCenterIfBoxGreaterThanOneMileEvenIfObfuscated() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, ONE_MILE_IN_DEGREE_MINUTES + 0.00002,
                ONE_MILE_IN_DEGREE_MINUTES + 0.00002);
        llb.setOkayToShowExactLocation(false);
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLatitudeIfNotObfuscated()));
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLongitudeIfNotObfuscated()));
    }

    @SuppressWarnings({ "static-method" })
    @Test
    public void doesNotReturnCenterIfLessThanOneMileByDefault() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, ONE_MILE_IN_DEGREE_MINUTES - 0.00005,
                ONE_MILE_IN_DEGREE_MINUTES - 0.00005);
        llb.obfuscate();
        assertTrue(null == llb.getCenterLatitudeIfNotObfuscated());
        assertTrue(null == llb.getCenterLongitudeIfNotObfuscated());
    }

    @SuppressWarnings({ "static-method" })
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
        assertFalse(Double.valueOf(0.0).equals(llb.getCenterLatitude()));
        assertFalse(Double.valueOf(0.0).equals(llb.getCenterLongitude()));
    }

    @SuppressWarnings("static-method")
    @Test
    public void doesNoObfuscatedCenterIfOkToShowExactLocation() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, 0.0, 0.0);
        llb.setOkayToShowExactLocation(true);
        assertTrue(Double.valueOf(0.0).equals(llb.getCenterLatitude()));
        assertTrue(Double.valueOf(0.0).equals(llb.getCenterLongitude()));
    }

    @SuppressWarnings({ "static-method" })
    @Test
    public void doesNotObfuscateCenterIfBoxGreaterThanOneMile() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, ONE_MILE_IN_DEGREE_MINUTES + 0.00002,
                ONE_MILE_IN_DEGREE_MINUTES + 0.00002);
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLatitude()));
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLongitude()));
    }

    @SuppressWarnings({ "static-method" })
    @Test
    public void doesNotObfuscateCenterIfBoxGreaterThanOneMileEvenIfNotOkToShowExactLocation() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(0.0, 0.0, ONE_MILE_IN_DEGREE_MINUTES + 0.00002,
                ONE_MILE_IN_DEGREE_MINUTES + 0.00002);
        llb.setOkayToShowExactLocation(false);
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLatitude()));
        assertTrue(Double.valueOf(0.00737).equals(llb.getCenterLongitude()));
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
        assertTrue(Double.valueOf(minimumLongitude).equals(llb.getMinimumLongitude()));
        assertTrue(Double.valueOf(maximumLongitude).equals(llb.getMaximumLongitude()));
        assertTrue(Double.valueOf(minimumLatitude).equals(llb.getMinimumLatitude()));
        assertTrue(Double.valueOf(maximumLatitude).equals(llb.getMaximumLatitude()));
        assertTrue(Double.valueOf(minimumLongitude).equals(llb.getMinObfuscatedLongitude()));
        assertTrue(Double.valueOf(maximumLongitude).equals(llb.getMaxObfuscatedLongitude()));
        assertTrue(Double.valueOf(minimumLatitude).equals(llb.getMinObfuscatedLatitude()));
        assertTrue(Double.valueOf(maximumLatitude).equals(llb.getMaxObfuscatedLatitude()));
    }

    @SuppressWarnings({ "static-method", "deprecation" })
    @Test
    public void doesReturnCoordsInRightPlaceIfBoxGreaterThanOneMile() {
        double minimumLongitude = 0.00001;
        double maximumLongitude = ONE_MILE_IN_DEGREE_MINUTES + 0.00002;
        double minimumLatitude = 0.00003;
        double maximumLatitude = ONE_MILE_IN_DEGREE_MINUTES + 0.00004;
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(minimumLongitude, minimumLatitude, maximumLongitude, maximumLatitude);
        assertTrue(Double.valueOf(minimumLongitude).equals(llb.getMinimumLongitude()));
        assertTrue(Double.valueOf(maximumLongitude).equals(llb.getMaximumLongitude()));
        assertTrue(Double.valueOf(minimumLatitude).equals(llb.getMinimumLatitude()));
        assertTrue(Double.valueOf(maximumLatitude).equals(llb.getMaximumLatitude()));
        assertTrue(Double.valueOf(minimumLongitude).equals(llb.getMinObfuscatedLongitude()));
        assertTrue(Double.valueOf(maximumLongitude).equals(llb.getMaxObfuscatedLongitude()));
        assertTrue(Double.valueOf(minimumLatitude).equals(llb.getMinObfuscatedLatitude()));
        assertTrue(Double.valueOf(maximumLatitude).equals(llb.getMaxObfuscatedLatitude()));
    }

}
