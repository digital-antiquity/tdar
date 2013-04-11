package org.tdar.core.bean.resource;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;

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
	    LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
		double result = LatitudeLongitudeBox.obfuscate(smallNeg, smallNeg2, LatitudeLongitudeBox.LONGITUDE, latLong);
		assertTrue("result:"+ result +  " < " + smallNeg, result < smallNeg);
	}

	@Test
	public void testPosLatLongWithSaltedResult() {
	       LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
		double result = LatitudeLongitudeBox.obfuscate(smallPos, smallPos2, LatitudeLongitudeBox.LONGITUDE,latLong);
		assertTrue("result:"+ result +  " < " + smallPos, result < smallPos);
	}

	@Test
	public void testNegLatLongWithSaltedResult2() {
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
		double result = LatitudeLongitudeBox.obfuscate(smallNeg2, smallNeg, LatitudeLongitudeBox.LONGITUDE,latLong);
		assertTrue("result:"+ result +  " < " + smallNeg2, result > smallNeg2);
	}

	@Test
	public void testWrappingLong() {
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
		double two = LatitudeLongitudeBox.MAX_LONGITUDE - LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES / 2;
		double result = LatitudeLongitudeBox.obfuscate(LatitudeLongitudeBox.MAX_LONGITUDE, two , LatitudeLongitudeBox.LONGITUDE,latLong);
		assertTrue("result:"+ result +  " > " + two, result < two);
	}

	@Test
	public void testWrappingLong2() {
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
		double two = LatitudeLongitudeBox.MIN_LONGITUDE + LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES / 2;
		double result = LatitudeLongitudeBox.obfuscate(LatitudeLongitudeBox.MIN_LONGITUDE, two , LatitudeLongitudeBox.LONGITUDE,latLong);
		assertTrue("result:"+ result +  " < " + two, result > two);
	}

	
	@Test
	public void testPosLatLongWithSaltedResult2() {
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
		double result = LatitudeLongitudeBox.obfuscate(smallPos2, smallPos, LatitudeLongitudeBox.LONGITUDE,latLong);
		assertTrue("result:"+ result +  " < " + smallPos2, result > smallPos2);
	}

	@Test
	@edu.umd.cs.findbugs.annotations.SuppressWarnings
	public void testLatLongWithoutSaltedResult() {
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
		double result = LatitudeLongitudeBox.obfuscate(smallNeg, smallPos, LatitudeLongitudeBox.LONGITUDE,latLong);
		assertTrue("result:"+ result +  " = " + smallNeg, result == smallNeg);
	}

	@Test
    @edu.umd.cs.findbugs.annotations.SuppressWarnings
	public void testLatLongWithoutSaltedResult2() {
	       LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
		double result = LatitudeLongitudeBox.obfuscate(smallPos, smallNeg, LatitudeLongitudeBox.LONGITUDE,latLong);
		assertTrue("result:"+ result +  " = " + smallPos, result == smallPos);
	}

}
