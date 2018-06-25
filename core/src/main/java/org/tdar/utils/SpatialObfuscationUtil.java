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

    private static final double _1D = 1.0d;

    private static final transient Logger logger = LoggerFactory.getLogger(SpatialObfuscationUtil.class);

    private static Double random;
    private static boolean useRandom = true;

    
    public static double degMinutesToMiles(Double degMin){
        double oneMile = (_1D * LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES);;
        return degMin / oneMile;
    }
    
    /**
     * Special constructor for bypassing "random" in tests
     * 
     * @param random
     */

    public static boolean obfuscate(LatitudeLongitudeBox latitudeLongitudeBox) {
        //define a standard measurement.
        logger.debug("Obfuscating the lat long box ({})",latitudeLongitudeBox);
        double oneMile = (_1D * LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES);
        
        // get the width and height of the LLB
        double absoluteLatLength = Math.abs(latitudeLongitudeBox.getNorth() - latitudeLongitudeBox.getSouth());
        double absoluteLongLength = Math.abs(latitudeLongitudeBox.getEast() - latitudeLongitudeBox.getWest());
        boolean obfuscated = false;
        
        //Set how wide you want the bounding box to be if it is obsfucating the original. 
        double boxWidth  = 1.5 * oneMile;
        double boxHeight = 1.5 * oneMile; 
        
        logger.debug("absoluteLatLength = {} degrees, {} mi",absoluteLatLength, degMinutesToMiles(absoluteLatLength));
        logger.debug("absoluteLongLength = {} degrees, {} mi",absoluteLongLength, degMinutesToMiles(absoluteLatLength));

        //This is the delta of how wide/tall the box is. 
        double boxWidthDelta = Math.abs(boxWidth - absoluteLatLength);
        double boxHeightDelta = Math.abs(boxHeight - absoluteLongLength);
        
        logger.debug("The width delta is {} degrees, {} mi",boxWidthDelta, degMinutesToMiles(boxWidthDelta));
        logger.debug("The height delta is {} degrees, {} mi",boxHeightDelta, degMinutesToMiles(boxHeightDelta));
        
        
        
        // if the Latitude is > 1 mile, don't obfuscate it
        if (absoluteLatLength < LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES) {
             logger.debug("Obsfucating the latitude, because it's less than one mile in degrees minutes");
            // get the center
            Double centerLatitude = latitudeLongitudeBox.getCenterLatitude();
                        
            // compute the southern corner as: (center latitude - 1 mile) + (random * 1 mile)
            // this should be something less than 1 mile

            //The upper limit of what the box can shift should be the delta of the two boxes. 
            //Any more or less means that the obfuscating box won't cover
            //the orignal box. The small amount is so that the shift isn't 
            //the delta is made slightly smaller so that that the bound will never actually touch the original bound 
            //The conditions are that obfs > or <, but not equal. 
            //This first shift is to make sure the bounding box is moved sufficently away that it covers the box. 
            double randomShift = (getRandom() * (boxHeightDelta));
            logger.debug("The shift co-efficent (random) is {}",getRandom());
            double maxShift = boxHeightDelta - (.5 * absoluteLatLength);
            
            logger.debug("randomShift is {}, upper limit is {}", randomShift, maxShift);

            //The second check is that amount that is being shifted must be less than the the difference between the Delta and 1/2 of the width of the original box. 
            //If the shift is greater than that difference, then it means that shift will be too far won't cover the boundary. 
            //This second shift is so that the shift back isn't so great that it no longer covers the box. 
            double shift = Math.min(maxShift, randomShift);
                       
            //If the delta is less than half the width of the box, then the initial position of the box won't be far enough down to cover the 
            //southern boundry of the box. This ensures that coverage. 
            double minimumOffset = Math.max(boxHeightDelta, (absoluteLatLength/2)+shift);
            
            logger.debug("The box will be offset southward  {} deg, {} mi ",minimumOffset, degMinutesToMiles(minimumOffset));
            logger.debug("The box will be shifted north {} degrees, {} mi ",shift, degMinutesToMiles(shift));

            
            // x y random1 random2 random3 random4 distance
            //The point is also moved slightly left of center so that the obfuscating box will never touch the original box. 
            double south1 = (centerLatitude - minimumOffset -.001) + shift; // -1*$G2+C2*$G2

            // north is then south plus the height of the box.
            double north1 = south1 + boxHeight;

            // set the values
            latitudeLongitudeBox.setObfuscatedNorth(correctForMeridiansAndPoles(north1, true));
            latitudeLongitudeBox.setObfuscatedSouth(correctForMeridiansAndPoles(south1, true));
            obfuscated = true;
        } else {
            logger.debug("Not obsfucating the latitude");
            // otherwise obfuscated values are set to current values because we don't need to obfuscate
            latitudeLongitudeBox.setObfuscatedNorth(latitudeLongitudeBox.getNorth());
            latitudeLongitudeBox.setObfuscatedSouth(latitudeLongitudeBox.getSouth());
        }

        if (absoluteLongLength < LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES) {
            // get the center longitude
            Double centerLongitude = latitudeLongitudeBox.getCenterLongitude();

                    
            double randomShift = (getRandom() * (boxWidthDelta)); 

            //The box can be shifted up to a maximum of this amount, if it greater, the edge won't be covered.
            double maxShift = boxWidthDelta - (.5 * absoluteLongLength);
            
            logger.debug("randomShift is {}, upper limit is {}", randomShift, maxShift);
            
            //The upper limit of the shift is Delta - 1/2 (width of the bounding box). 
            //If the sift is greater than this, then it means that the box will shift too far right and won't cover the left edge. This sets the upper limit of that
            double shift = Math.min(maxShift, randomShift);
            
            logger.debug("The shift co-efficent (random) is {}",getRandom());
            
            
            //If the delta is less than half the width of the box, then the initial position of the box won't be far enough down to cover the 
            //southern boundry of the box. This ensures that coverage. 
            double minimumOffset = Math.max(boxWidthDelta, (absoluteLongLength/2)+shift);
            logger.debug("The box will first be offset westward {} deg, {} mi", minimumOffset, degMinutesToMiles(minimumOffset));
            logger.debug("The box will be then shifted eastward {} degrees, {} mi ",shift, degMinutesToMiles(shift));
            
            
            // west is center (longitude - 1 mile ) + random * 1 mile
            double west1 = (centerLongitude - minimumOffset -.001) + (shift); // -1*$G2+C2*$G2
            double east1 = west1 + boxWidth;

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
            //logger.error("using TESTING RANDOM (NOT RANDOM) "+ random.doubleValue());
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
