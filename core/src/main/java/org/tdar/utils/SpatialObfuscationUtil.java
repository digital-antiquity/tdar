package org.tdar.utils;

import org.tdar.core.bean.coverage.LatitudeLongitudeBox;

public class SpatialObfuscationUtil {

    public static void obfuscate(LatitudeLongitudeBox latitudeLongitudeBox) {
        double absoluteLatLength = Math.abs(latitudeLongitudeBox.getNorth() - latitudeLongitudeBox.getSouth());
        double absoluteLongLength = Math.abs(latitudeLongitudeBox.getEast() - latitudeLongitudeBox.getWest());
        if (absoluteLatLength < LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES) {
            Double centerLatitude = latitudeLongitudeBox.getCenterLatitude();
            // x y random1 random2 random3 random4 distance
            double south1 = centerLatitude - 1d * LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES
                    + Math.random() * LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES; // -1*$G2+C2*$G2
            double north1 = south1 + LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES;
            latitudeLongitudeBox.setObfuscatedNorth(correctForMeridiansAndPoles(north1, true));
            latitudeLongitudeBox.setObfuscatedSouth(correctForMeridiansAndPoles(south1, true));
        } else {
            latitudeLongitudeBox.setObfuscatedNorth(latitudeLongitudeBox.getNorth());
            latitudeLongitudeBox.setObfuscatedSouth(latitudeLongitudeBox.getSouth());
        }

        if (absoluteLongLength < LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES) {
            Double centerLongitude = latitudeLongitudeBox.getCenterLongitude();
            double west1 = centerLongitude - 1d * LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES
                    + Math.random() * LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES; // -1*$G2+C2*$G2
            double east1 = west1 + LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES;

            latitudeLongitudeBox.setObfuscatedEast(correctForMeridiansAndPoles(east1, false));
            latitudeLongitudeBox.setObfuscatedWest(correctForMeridiansAndPoles(west1, false));
        } else {
            latitudeLongitudeBox.setObfuscatedEast(latitudeLongitudeBox.getEast());
            latitudeLongitudeBox.setObfuscatedWest(latitudeLongitudeBox.getWest());
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

}
