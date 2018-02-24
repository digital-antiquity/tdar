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
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(smallNeg, smallNeg, smallNeg2, smallNeg2);
        llb.obfuscate();
        double result = llb.getObfuscatedSouth();
        assertTrue("result:" + result + " < " + smallNeg, result < smallNeg);
    }

    @Test
    public void testPosLatLongWithSaltedResult() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(smallPos, smallPos, smallPos2, smallPos2);
        llb.obfuscate();
        double result = llb.getObfuscatedSouth();
        assertTrue("result:" + result + " < " + smallPos, result < smallPos);
    }

    @Test
    public void testNegLatLongWithSaltedResult2() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(smallNeg2, smallNeg2, smallNeg, smallNeg);
        llb.obfuscate();
        double result = llb.getObfuscatedNorth();
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
//        double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallPos2, smallPos, LatitudeLongitudeBox.LONGITUDE, true);
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(smallPos2, smallPos2, smallPos, smallPos);
        llb.obfuscate();
        double result = llb.getObfuscatedEast();
        assertTrue("result:" + result + " < " + smallPos2, result > smallPos2);
    }

    @Test
    public void testLatLongWithoutSaltedResult() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(smallNeg, smallPos, smallNeg, smallPos);
        llb.obfuscate();
        double result = llb.getObfuscatedSouth();
//        double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallNeg, smallPos, LatitudeLongitudeBox.LONGITUDE, true);
        assertTrue("result:" + result + " = " + smallNeg, result == smallNeg);

    }

    @Test
    public void testLatLongWithoutSaltedResult2() {
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox(smallPos, smallPos, smallNeg, smallNeg);
        llb.obfuscate();
        double result = llb.getObfuscatedEast();
//        double result = LatitudeLongitudeBox.randomizeIfNeedBe(smallPos, smallNeg, LatitudeLongitudeBox.LONGITUDE, true);
        assertTrue("result:" + result + " = " + smallPos, result == smallPos);
    }

}
