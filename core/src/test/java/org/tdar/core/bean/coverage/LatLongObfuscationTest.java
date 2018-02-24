package org.tdar.core.bean.coverage;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Adam Brin
 * 
 */
public class LatLongObfuscationTest {

    public final double smallNeg = -35.0050d;
    public final double smallNeg2 = -35.000d;

    public final double smallPos = 35.000d;
    public final double smallPos2 = 35.0050d;

    @Test
    public void testNegLatLongWithSaltedResult() {
        double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallNeg, smallNeg2, LatitudeLongitudeBox.LONGITUDE, true);
        assertTrue("result:" + result + " < " + smallNeg, result < smallNeg);
    }

    @Test
    public void testPosLatLongWithSaltedResult() {
        double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallPos, smallPos2, LatitudeLongitudeBox.LONGITUDE, true);
        assertTrue("result:" + result + " < " + smallPos, result < smallPos);
    }

    @Test
    public void testNegLatLongWithSaltedResult2() {
        double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallNeg2, smallNeg, LatitudeLongitudeBox.LONGITUDE, true);
        assertTrue("result:" + result + " < " + smallNeg2, result > smallNeg2);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testWrappingLong() {
        double two = LatitudeLongitudeBox.MAX_LONGITUDE - (LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES / 2);
        double result = LatitudeLongitudeBox.randomizeIfNeedBe(LatitudeLongitudeBox.MAX_LONGITUDE, two, LatitudeLongitudeBox.LONGITUDE, true);
        assertTrue("result:" + result + " > " + two, result < two);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testWrappingLong2() {
        double two = LatitudeLongitudeBox.MIN_LONGITUDE + (LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES / 2);
        double result = LatitudeLongitudeBox.randomizeIfNeedBe(LatitudeLongitudeBox.MIN_LONGITUDE, two, LatitudeLongitudeBox.LONGITUDE, true);
        assertTrue("result:" + result + " < " + two, result > two);
    }

    @Test
    public void testPosLatLongWithSaltedResult2() {
        double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallPos2, smallPos, LatitudeLongitudeBox.LONGITUDE, true);
        assertTrue("result:" + result + " < " + smallPos2, result > smallPos2);
    }

    @Test
    public void testLatLongWithoutSaltedResult() {
        double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallNeg, smallPos, LatitudeLongitudeBox.LONGITUDE, true);
        assertTrue("result:" + result + " = " + smallNeg, result == smallNeg);
    }

    @Test
    public void testLatLongWithoutSaltedResult2() {
        double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallPos, smallNeg, LatitudeLongitudeBox.LONGITUDE, true);
        assertTrue("result:" + result + " = " + smallPos, result == smallPos);
    }

}
