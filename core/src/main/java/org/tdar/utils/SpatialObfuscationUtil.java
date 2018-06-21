package org.tdar.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;

/**
 * This class is the basis of our Obfuscation Algorithm and is used to calculate a 1 mile bounding box (max) around the box
 * 
 * @author abrin
 *
 */
public class SpatialObfuscationUtil {

    private static final double _1D = 1d;

    private static final transient Logger logger = LoggerFactory.getLogger(SpatialObfuscationUtil.class);

    private static Double random;
    private static boolean useRandom = true;

    /**
     * Special constructor for bypassing "random" in tests
     * 
     * @param random
     */

    public static boolean obfuscate(LatitudeLongitudeBox latitudeLongitudeBox) {
        // get the width and height of the LLB
        double absoluteLatLength = Math.abs(latitudeLongitudeBox.getNorth() - latitudeLongitudeBox.getSouth());
        double absoluteLongLength = Math.abs(latitudeLongitudeBox.getEast() - latitudeLongitudeBox.getWest());
        boolean obfuscated = false;

        // if the Latitude is > 1 mile, don't obfuscate it
        if (absoluteLatLength < LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES) {
            // get the center
            Double centerLatitude = latitudeLongitudeBox.getCenterLatitude();

            // compute the southern corner as: (center latitude - 1 mile) + (random * 1 mile)
            // this should be something less than 1 mile

            // x y random1 random2 random3 random4 distance
            double south1 = (centerLatitude - (_1D * LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES))
                    + (getRandom() * LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES); // -1*$G2+C2*$G2

            // north is then south plus 1 mile
            double north1 = south1 + LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES;

            // set the values
            latitudeLongitudeBox.setObfuscatedNorth(correctForMeridiansAndPoles(north1, true));
            latitudeLongitudeBox.setObfuscatedSouth(correctForMeridiansAndPoles(south1, true));
            obfuscated = true;
        } else {
            // otherwise obfuscated values are set to current values because we don't need to obfuscate
            latitudeLongitudeBox.setObfuscatedNorth(latitudeLongitudeBox.getNorth());
            latitudeLongitudeBox.setObfuscatedSouth(latitudeLongitudeBox.getSouth());
        }

        if (absoluteLongLength < LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES) {
            // get the center longitude
            Double centerLongitude = latitudeLongitudeBox.getCenterLongitude();

            
            double offset = (getRandom() * LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES); 
            double oneMile = (_1D * LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES);
            // west is center (longitude - 1 mile ) + random * 1 mile
            double west1 = (centerLongitude - oneMile) + (offset); // -1*$G2+C2*$G2

            // east is 1 mile plus west
            double east1 = west1 + LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES;

            latitudeLongitudeBox.setObfuscatedEast(correctForMeridiansAndPoles(east1, false));
            latitudeLongitudeBox.setObfuscatedWest(correctForMeridiansAndPoles(west1, false));
            obfuscated = true;
        } else {
            latitudeLongitudeBox.setObfuscatedEast(latitudeLongitudeBox.getEast());
            latitudeLongitudeBox.setObfuscatedWest(latitudeLongitudeBox.getWest());
        }
        return obfuscated;
    }

    public static double getRandom() {
        if (useRandom) {
            return Math.random();
        } else {
            logger.error("using TESTING RANDOM (NOT RANDOM) "+ random.doubleValue());
            return  random.doubleValue();
        }
    }

    public static double correctForMeridiansAndPoles(final double ret_, boolean latitude) {
        double ret = ret_;
        if (latitude == false) {
            if (ret > LatitudeLongitudeBox.MAX_LONGITUDE) {
                ret -= 360d;
            }
            if (ret < LatitudeLongitudeBox.MIN_LONGITUDE) {
                ret += 360d;
            }
            return ret;
        }

        // NOTE: Ideally, this should do something different, but in reality, how
        // many archaeological sites are really going to be in this area???
        if (Math.abs(ret) > LatitudeLongitudeBox.MAX_LATITUDE) {
            ret = LatitudeLongitudeBox.MAX_LATITUDE;
        }
        return ret;

    }

    /**
     * pockage protected for testing
     * 
     * @param random
     */
    protected static void setRandom(Double random) {
        SpatialObfuscationUtil.random = random;
    }

    /**
     * pockage protected for testing
     * 
     * @param random
     */
    protected static void useRandom(boolean random) {
        SpatialObfuscationUtil.useRandom = random;
    }

}
