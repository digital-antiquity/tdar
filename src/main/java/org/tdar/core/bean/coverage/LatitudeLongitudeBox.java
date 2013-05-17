package org.tdar.core.bean.coverage;

import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.search.index.bridge.LatLongClassBridge;
import org.tdar.search.index.bridge.TdarPaddedNumberBridge;

/**
 * $Id$
 * 
 * Encapsulates min/max lat-long pairs representing the approximate spatial
 * coverage of a Resource.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "latitude_longitude")
@ClassBridge(impl = LatLongClassBridge.class)
@XmlRootElement
// (name="latitudeLongitudeBox")
public class LatitudeLongitudeBox extends Persistable.Base implements HasResource<Resource>, Obfuscatable {

    private transient boolean obfuscated = false;
    private transient Double minObfuscatedLatitude = null;
    private transient Double minObfuscatedLongitude = null;
    private transient Double maxObfuscatedLatitude = null;
    private transient Double maxObfuscatedLongitude = null;

    private static final String PSQL_POLYGON = "POLYGON((%1$s %2$s,%3$s %2$s,%3$s %4$s,%1$s %4$s,%1$s %2$s))";

    private static final String PSQL_MULTIPOLYGON_DATELINE = "MULTIPOLYGON(((%1$s %2$s,%1$s %3$s,  180 %3$s,  180 %2$s,%1$s %2$s)), ((-180 %3$s, %4$s %3$s,%4$s %2$s,-180 %2$s,-180 %3$s)))";
    // private static final String PSQL_MULTIPOLYGON_DATELINE =
    // "MULTIPOLYGON(((%1$s %2$s,%1$s %3$s, -180 %3$s, -180 %2$s,%1$s %2$s)), (( 180 %3$s, %4$s %3$s,%4$s %2$s, 180 %2$s, 180 %3$s)))";
    private static final long serialVersionUID = 2605563277326422859L;

    public static final double MAX_LATITUDE = 90d;
    public static final double MIN_LATITUDE = -90d;

    public static final double MAX_LONGITUDE = 180d;
    public static final double MIN_LONGITUDE = -180d;
    public static final int LATITUDE = 1;
    public static final int LONGITUDE = 2;

    public static final double ONE_MILE_IN_DEGREE_MINUTES = 0.01472d;

    // ranges from -90 (South) to +90 (North)
    @Field
    @NumericField(precisionStep = 6)
    @Column(nullable = false, name = "minimum_latitude")
    private Double minimumLatitude;

    @Field
    @NumericField(precisionStep = 6)
    @Column(nullable = false, name = "maximum_latitude")
    private Double maximumLatitude;

    // ranges from -180 (West) to +180 (East)
    @Field
    @NumericField(precisionStep = 6)
    @Column(nullable = false, name = "minimum_longitude")
    private Double minimumLongitude;

    @Field
    @NumericField(precisionStep = 6)
    @Column(nullable = false, name = "maximum_longitude")
    private Double maximumLongitude;

    // used in testing and management
    private transient Set<GeographicKeyword> geographicKeywords;

    public LatitudeLongitudeBox() {
    }

    public LatitudeLongitudeBox(Double minxOrMinLong, Double minyOrMinLat, Double maxxOrMaxLong, Double maxyOrMaxLat) {
        this.minimumLongitude = minxOrMinLong;
        this.minimumLatitude = minyOrMinLat;
        this.maximumLatitude = maxyOrMaxLat;
        this.maximumLongitude = maxxOrMaxLong;
    }

    @Deprecated
    /*
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getMinimumLatitude() {
        return minimumLatitude;
    }

    /* fixme ** test */
    public Double getCenterLatitude() {
        return (getMaxObfuscatedLatitude() + getMinObfuscatedLatitude()) / 2.0;
    }

    public Double getCenterLongitude() {
        return (getMaxObfuscatedLongitude() + getMinObfuscatedLongitude()) / 2.0;
    }

    private boolean isActuallyObfuscated() {
        return (getMinObfuscatedLongitude() != getMinimumLongitude() || getMaxObfuscatedLongitude() != getMaximumLongitude()
                || getMinObfuscatedLatitude() != getMinimumLatitude() || getMaxObfuscatedLatitude() != getMaximumLatitude());
    }

    /* fixme ** test */
    public Double getCenterLatitudeIfNotObfuscated() {
        Double centerLatitude = getCenterLatitude(); // init obfuscated properly
        @SuppressWarnings("unused")
        Double centerLongitude = getCenterLongitude(); // init obfuscated properly
        if (isActuallyObfuscated()) {
            return null;
        }
        return centerLatitude;
    }

    public Double getCenterLongitudeIfNotObfuscated() {
        @SuppressWarnings("unused")
        Double centerLatitude = getCenterLatitude(); // init obfuscated properly
        Double centerLongitude = getCenterLongitude();// init obfuscated properly
        if (isActuallyObfuscated()) {
            return null;
        }
        return centerLongitude;
    }

    /*
     * This randomize function is used when displaying lat/longs on a map. It is
     * passed the max and the min lat or long and then uses a salt to randomize.
     * The salt here is approx 1 miles. If the distance between num1 and num2 is
     * less than 1 miles, then this should expand the box by 1 miles + some random
     * 1 mile quantity.
     * 
     * If larger than the "salt" then don't do anything for that "side"
     * 
     * NOTE: the box should always be bigger than the original.
     * 
     * http://www.movable-type.co.uk/scripts/html
     */

    public static Double obfuscate(Double num, Double num2, int type) {
        Random r = new Random();
        double salt = ONE_MILE_IN_DEGREE_MINUTES;
        double add = 0;

        if (Math.abs(num.doubleValue() - num2.doubleValue()) < salt) {
            add += salt / 2;
        } else {
            return num;
        }

        if (num < num2) { // -5 < -3
            add *= -1;
            salt *= -1;
        }
        // -5 - .05 - .02
        double ret = num.doubleValue() + add + salt * r.nextDouble();
        if (type == LONGITUDE) {
            if (ret > MAX_LONGITUDE)
                ret -= 360;
            if (ret < MIN_LONGITUDE)
                ret += 360;
        }

        // NOTE: Ideally, this should do something different, but in reality, how
        // many
        // archaeological sites are really going to be in this area???
        if (type == LATITUDE) {
            if (Math.abs(ret) > MAX_LATITUDE)
                ret = MAX_LATITUDE;
        }

        return new Double(ret);
    }

    public Double getMinObfuscatedLatitude() {
        if (minObfuscatedLatitude == null) {
            minObfuscatedLatitude = obfuscate(minimumLatitude, maximumLatitude, LATITUDE);
        }
        return minObfuscatedLatitude;
    }

    public Double getMaxObfuscatedLatitude() {
        if (maxObfuscatedLatitude == null) {
            maxObfuscatedLatitude = obfuscate(maximumLatitude, minimumLatitude, LATITUDE);
        }
        return maxObfuscatedLatitude;
    }

    public Double getMinObfuscatedLongitude() {
        if (minObfuscatedLongitude == null) {
            minObfuscatedLongitude = obfuscate(minimumLongitude, maximumLongitude, LONGITUDE);
        }
        return minObfuscatedLongitude;
    }

    public Double getMaxObfuscatedLongitude() {
        if (maxObfuscatedLongitude == null) {
            maxObfuscatedLongitude = obfuscate(maximumLongitude, minimumLongitude, LONGITUDE);
        }
        return maxObfuscatedLongitude;
    }

    public void setMinimumLatitude(Double minimumLatitude) {
        if (isValidLatitude(minimumLatitude)) {
            this.minimumLatitude = minimumLatitude;
        } else {
            throw new TdarRuntimeException("specified latitude is not a valid latitude");
        }
    }

    @Deprecated
    /*
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getMaximumLatitude() {
        return maximumLatitude;
    }

    public void setMaximumLatitude(Double maximumLatitude) {
        if (isValidLatitude(maximumLatitude)) {
            this.maximumLatitude = maximumLatitude;
        } else {
            throw new TdarRuntimeException("specified latitude is not a valid latitude");
        }
    }

    @Deprecated
    /*
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getMinimumLongitude() {
        return minimumLongitude;
    }

    public void setMinimumLongitude(Double minimumLongitude) {
        if (isValidLongitude(minimumLongitude)) {
            this.minimumLongitude = minimumLongitude;
        } else {
            throw new TdarRuntimeException("specified latitude is not a valid longitude");
        }
    }

    @Deprecated
    /*
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getMaximumLongitude() {
        return maximumLongitude;
    }

    public void setMaximumLongitude(Double maximumLongitude) {
        if (isValidLongitude(maximumLongitude)) {
            this.maximumLongitude = maximumLongitude;
        } else {
            throw new TdarRuntimeException("specified latitude is not a valid longitude");
        }
    }

    @Transient
    public boolean isValidLatitude(Double latitude) {
        // FIXME: verify that this works for extreme cases (+/- 180)
        return latitude != null && latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE;
    }

    @Transient
    public boolean isValidLongitude(Double longitude) {
        // FIXME: verify that this works at extreme case (+/- 90)
        return longitude != null && longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE;
    }

    public boolean isValid() {
        if (isValidLatitude(maximumLatitude)
                && isValidLatitude(minimumLatitude)
                && isValidLongitude(minimumLongitude)
                && isValidLongitude(maximumLongitude)
                && maximumLatitude >= minimumLatitude
                && isValidLongitudeSpan(minimumLongitude, maximumLongitude)
                && Math.abs(maximumLatitude - minimumLatitude) < 180) {
            return true;
        }
        return false;
    }

    // this logic assumes no span greater than 180°, and that zero-length span is invalid.
    private boolean isValidLongitudeSpan(double min, double max) {
        if (max < 0 && min > 0) {
            // when spanning IDL, pretend that flat map repeats as it extends past 180°E, e.g. 170°W is now 190°E
            max += 360;
        }
        logger.trace("min:{}\tmax:{}", min, max);
        return min < max;
    }

    public String toString() {
        return String.format("Latitude [%s to %s], Longitude [%s to %s]", minimumLatitude, maximumLatitude, minimumLongitude, maximumLongitude);
    }

    public void copyValuesFrom(LatitudeLongitudeBox otherBox) {
        setMinimumLatitude(otherBox.minimumLatitude);
        setMaximumLatitude(otherBox.maximumLatitude);
        setMinimumLongitude(otherBox.minimumLongitude);
        setMaximumLongitude(otherBox.maximumLongitude);
    }

    public String convertToPolygonBox() {
        // if we've got something that goes over the dateline, then we need to split
        // into a multipolygon instead of a standard one. The multipolygon is two polygons
        // each one being on either side of the dateline
        if (!isValid()) {
            throw new TdarRuntimeException("the specified latLong box is not valid");
        }
        if (crossesDateline()) {
            return String.format(PSQL_MULTIPOLYGON_DATELINE, getMinObfuscatedLongitude(), getMinObfuscatedLatitude(),
                    getMaxObfuscatedLatitude(), getMaxObfuscatedLongitude()).toString();
        }
        return String.format(PSQL_POLYGON, getMaxObfuscatedLongitude(), getMaxObfuscatedLatitude(),
                getMinObfuscatedLongitude(), getMinObfuscatedLatitude()).toString();
    }

    public double getArea() {
        return getAbsoluteLatLength() * getAbsoluteLongLength();
    }

    public double getAbsoluteLatLength() {
        return Math.abs(getMaxObfuscatedLatitude() - getMinObfuscatedLatitude());
    }

    public double getAbsoluteLongLength() {
        return Math.abs(getMaxObfuscatedLongitude() - getMinObfuscatedLongitude());
    }

    /**
     * @return
     */
    public boolean crossesDateline() {
        return LatitudeLongitudeBox.crossesDateline(getMinObfuscatedLongitude(), getMaxObfuscatedLongitude());
    }

    public boolean crossesPrimeMeridian() {
        return LatitudeLongitudeBox.crossesPrimeMeridian(getMinObfuscatedLongitude(), getMaxObfuscatedLongitude());
    }

    public static boolean crossesDateline(double minLongitude, double maxLongitude) {
        /*
         * below is the logic that was originally used in PostGIS -- it worked to help identify issues where a box was
         * drawn around Guam and Hawaii, but it's not really needed anymore because all of our logic looks at the box
         * and breaks it in two over the IDL instead of choosing the smaller box.
         * return (getMinObfuscatedLongitude() < -100f && getMaxObfuscatedLongitude() > 100f);
         */
        if (minLongitude > 0f && maxLongitude < 0f) {
            return true;
        }

        return false;
    }

    public static boolean crossesPrimeMeridian(double minLongitude, double maxLongitude) {
        if (minLongitude < 0f && maxLongitude > 0f) {
            return true;
        }

        return false;
    }

    public boolean isValidForController() {
        return true;
    }

    @XmlTransient
    @JSONTransient
    public boolean isObfuscated() {
        return obfuscated;
    }

    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    public List<Obfuscatable> obfuscate() {
        setMaximumLatitude(getMaxObfuscatedLatitude());
        setMinimumLatitude(getMinObfuscatedLatitude());
        setMaximumLongitude(getMaxObfuscatedLongitude());
        setMinimumLongitude(getMinObfuscatedLongitude());
        setObfuscated(true);
        return null;
    }

    @Transient
    public void setMinx(Double minX) {
        this.minimumLongitude = minX;
    }

    @Transient
    public void setMaxx(Double maxX) {
        this.maximumLongitude = maxX;
    }

    @Transient
    public void setMiny(Double minY) {
        this.minimumLatitude = minY;
    }

    @Transient
    public void setMaxy(Double maxY) {
        this.maximumLatitude = maxY;
    }

    public void addGeographicKeywords(Set<GeographicKeyword> allGeographicInfo) {
        this.setGeographicKeywords(allGeographicInfo);
    }

    public Set<GeographicKeyword> getGeographicKeywords() {
        return geographicKeywords;
    }

    public void setGeographicKeywords(Set<GeographicKeyword> geographicKeywords) {
        this.geographicKeywords = geographicKeywords;
    }

    /*
     * scale is a ratio based on the relative size of the latLong box and the overall map
     * it works like zoom level in google, but, it allows us to ignore regions that are a lot
     * bigger or a lot smaller than the bounding box we've created
     * if I draw a box around the city of Chandler, ignore items that have a box around the US.
     * 
     * The goal of this is not to control the bounding region, but to keep the results matching
     * the relative scale of the bounding box. The smaller the scale the smaller the bounding region
     * the more regional the search.
     */
    @FieldBridge(impl = TdarPaddedNumberBridge.class)
    @Field(norms = Norms.NO, store = Store.YES, analyze = Analyze.NO)
    @Transient
    public Integer getScale() {
        Integer toReturn = -1;
        if (!isInitialized()) {
            return toReturn;
        }
        double scale = getAbsoluteLatLength();
        if (getAbsoluteLongLength() > scale) {
            scale = getAbsoluteLongLength();
        }
        if (scale < 1.0) {
            toReturn += 2;
        }
        if (scale >= 1.0) {
            toReturn++;
        }

        if (scale > 4.0) {
            toReturn++;
        }
        if (scale > 9.0) {
            toReturn++;
        }
        if (scale > 14.0) {
            toReturn++;
        }

        if (scale > 19.0) {
            toReturn++;
        }

        logger.trace("scale: {} ({})", scale, toReturn);
        return toReturn;
    }

    public boolean isInitialized() {
        if (maximumLatitude == null || minimumLatitude == null || maximumLongitude == null || minimumLongitude == null) {
            return false;
        }
        return true;
    }
}
