package org.tdar.core.bean.coverage;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;


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
@Table(name = "latitude_longitude", indexes = {
        @Index(name = "resource_latlong", columnList = "resource_id, id") })
// @ClassBridge(impl = LatLongClassBridge.class)
@XmlRootElement
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.coverage.LatitudeLongitudeBox")
public class LatitudeLongitudeBox extends AbstractPersistable implements HasResource<Resource>, Obfuscatable {

    private static final long serialVersionUID = 2605563277326422859L;

    public static final double MAX_LATITUDE = 90d;
    public static final double MIN_LATITUDE = -90d;

    public static final double MAX_LONGITUDE = 180d;
    public static final double MIN_LONGITUDE = -180d;
    public static final int LATITUDE = 1;
    public static final int LONGITUDE = 2;
    @Transient
    @XmlTransient
    private transient int hash = -1;
    public static final double ONE_MILE_IN_DEGREE_MINUTES = 0.01472d;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    /** used to record whether this instance has been obfuscated by the obfuscation service or not */
    private transient boolean obfuscated;

    /** if true, then the location does not need to be hidden */
    @Column(nullable = false, name = "is_ok_to_show_exact_location", columnDefinition = "boolean default false")
    private boolean isOkayToShowExactLocation = false;

    @Column(name = "min_obfuscated_lat")
    private Double obfuscatedSouth;
    @Column(name = "min_obfuscated_long")
    private Double obfuscatedWest;
    @Column(name = "max_obfuscated_lat")
    private Double obfuscatedNorth;
    @Column(name = "max_obfuscated_long")
    private Double obfuscatedEast;

    // ranges from -90 (South) to +90 (North)
    @Column(nullable = false, name = "minimum_latitude")
    @DecimalMin(value = "-90.0", inclusive = true)
    @DecimalMax(value = "90.0", inclusive = true)
    @NotNull
    private Double south;

    @Column(nullable = false, name = "maximum_latitude")
    @DecimalMin(value = "-90.0", inclusive = true)
    @DecimalMax(value = "90.0", inclusive = true)
    @NotNull
    private Double north;

    // ranges from -180 (West) to +180 (East)
    @Column(nullable = false, name = "minimum_longitude")
    @DecimalMin(value = "-180.0", inclusive = true)
    @DecimalMax(value = "180.0", inclusive = true)
    @NotNull
    private Double west;

    @Column(nullable = false, name = "maximum_longitude")
    @DecimalMin(value = "-180.0", inclusive = true)
    @DecimalMax(value = "180.0", inclusive = true)
    @NotNull
    private Double east;

    // used in testing and management
    private transient Set<GeographicKeyword> geographicKeywords;

    public LatitudeLongitudeBox() {
    }

    public LatitudeLongitudeBox(Double west, Double south, Double east, Double north) {
        this.west = west;
        this.south = south;
        this.north = north;
        this.east = east;
    }

    @Deprecated
    /**
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getSouth() {
        return south;
    }

    public Double getObfuscatedCenterLatitude() {
        return getCenterLat(getObfuscatedNorth(), getObfuscatedSouth());
    }

    protected Double getCenterLat(Double double1, Double double2) {
        return (double1 + double2) / 2d;
    }

    public Double getObfuscatedCenterLongitude() {
        return getCenterLong(getObfuscatedWest(), getObfuscatedEast());
    }

    @Deprecated
    public Double getCenterLatitude() {
        return getCenterLat(getNorth(), getSouth());
    }

    @Deprecated
    public Double getCenterLongitude() {
        return getCenterLong(getWest(), getEast());
    }

    protected Double getCenterLong(Double minLong, Double maxLong) {
        // print out in degrees
        if (maxLong < minLong) {
            logger.debug("min:" + minLong);
            logger.debug("max:" + maxLong);

            // min is one side of the dateline and max is on the other
            if (maxLong < 0 && minLong > 0) {
                // convert the eastern side to a positive number as if we go to 360º
                double offsetRight = (-180d - maxLong) * -1d + 180d;
                // get the distance 1/2 way
                double ret = (offsetRight + minLong) / 2d;
                // logger.debug("min: {} offset:{} max: {}", minLong, offsetRight, maxLong);
                // logger.debug("toReturn:" + ret);
                // if we're greater than 180º, then subtract 360º to get the negative variant
                if (ret > 180) {
                    ret += -360d;
                }
                // logger.debug("to return: {}", ret);
                return ret;
            }

            Double tmp = (minLong + maxLong * -1d + 180d) / 2d;
            if (tmp > 180) {
                tmp = 180 - tmp;
            }
            return tmp;
        }

        return (minLong + maxLong) / 2d;
        /*
         * // http://stackoverflow.com/questions/4656802/midpoint-between-two-latitude-and-longitude
         * double dLon = Math.toRadians(maxLong - minLong);
         * 
         * double minLong_ = Math.toRadians(minLong);
         * 
         * double Bx = Math.cos(0.0) * Math.cos(dLon);
         * double By = Math.cos(0.0) * Math.sin(dLon);
         * double lon3 = minLong_ + Math.atan2(By, Math.cos(0.0) + Bx);
         * double degrees = Math.toDegrees(lon3);
         * if (degrees > 180) {
         * return -180 + degrees - 180;
         * }
         * return degrees;
         * 
         */
    }

    /**
     * @return a helper method, useful for testing. Returns true if one or more of the obfuscated values differs from the original, false otherwise.
     */

    @Transient
    public boolean isObfuscatedObjectDifferent() {
        if (obfuscatedObjectDifferent == null) {
            logger.trace("should call obfuscate before testing obfuscation");
            return false;
        }
        return obfuscatedObjectDifferent;
    }

    public Double getCenterLatitudeIfNotObfuscated() {
        if (!isOkayToShowExactLocation && isObfuscatedObjectDifferent()) {
            return null;
        }
        return getObfuscatedCenterLatitude();
    }

    public Double getCenterLongitudeIfNotObfuscated() {
        if (!isOkayToShowExactLocation && isObfuscatedObjectDifferent()) {
            return null;
        }
        return getObfuscatedCenterLongitude();
    }

    /**
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
    protected static Double randomizeIfNeedBe(final Double num1, final Double num2, int type, boolean isMin) {
        if ((num1 == null) && (num2 == null)) {
            return null;
        }

        Random r = new Random();
        double salt = ONE_MILE_IN_DEGREE_MINUTES;
        double add = 0;

        Double numOne = ObjectUtils.firstNonNull(num1, num2);

        if (num1 == null) {
            throw new TdarRecoverableRuntimeException("latLong.one_null");
        }
        // if we call setMin setMax etc.. serially, we can get a null pointer exception as num2 is not yet set...
        Double numTwo = ObjectUtils.firstNonNull(num2, numOne + salt / 2d);
        if (Math.abs(numOne.doubleValue() - numTwo.doubleValue()) <= salt) {
            add += salt / 2d;
        } else {
            return numOne;
        }

        if (numOne < numTwo) { // -5 < -3
            add *= -1d;
            salt *= -1d;
        } else {
            // If two points are the same, we want to always scoot the maximum long/lat higher, and the minimum long/lat lower, such that the minimum distance
            // exceeds the salt distance.
            if (numOne.equals(numTwo) && isMin) {
                add *= -1d;
                salt *= -1d;
            }
        }
        // -5 - .05 - .02
        double ret = numOne.doubleValue() + add + salt * r.nextDouble();
        if (type == LONGITUDE) {
            if (ret > MAX_LONGITUDE)
                ret -= 360d;
            if (ret < MIN_LONGITUDE)
                ret += 360d;
        }

        // NOTE: Ideally, this should do something different, but in reality, how
        // many archaeological sites are really going to be in this area???
        if (type == LATITUDE) {
            if (Math.abs(ret) > MAX_LATITUDE)
                ret = MAX_LATITUDE;
        }

        return new Double(ret);
    }

    /**
     * Puts all the logic around the returning of obfuscated values vs actual values into one place.
     * 
     * @param obfuscatedValue
     * @param actualValue
     * @return either the obfuscated value or the actual value passed in, depending on the setting of the isOkayToShowExactLocation switch
     */
    private Double getProtectedResult(final Double obfuscatedValue, final Double actualValue) {
        Double result = obfuscatedValue;
        if (isOkayToShowExactLocation) {
            result = actualValue;
        }
        if (!Objects.equals(actualValue, obfuscatedValue)) {
            setObfuscatedObjectDifferent(true);
        }
        return result;
    }

    private void setObfuscatedSouth() {
        if (isOkayToShowExactLocation) {
            obfuscatedSouth = south;
            return;
        }

        obfuscatedSouth = randomizeIfNeedBe(south, north, LATITUDE, true);
    }

    /**
     * @return <b>either</b> the obfuscated value <b>or</b> the actual minimumLatitude, depending on the setting of the isOkayToShowExactLocation switch
     */
    @JsonView(JsonLookupFilter.class)
    public Double getObfuscatedSouth() {
        return obfuscatedSouth;
    }

    private void setObfuscatedNorth() {
        if (isOkayToShowExactLocation) {
            obfuscatedNorth = north;
            return;
        }

        obfuscatedNorth = randomizeIfNeedBe(north, south, LATITUDE, false);
    }

    /**
     * @return <b>either</b> the obfuscated value <b>or</b> the actual maximumLatitude, depending on the setting of the isOkayToShowExactLocation switch
     */
    @JsonView(JsonLookupFilter.class)
    public Double getObfuscatedNorth() {
        return obfuscatedNorth;
    }

    private void setObfuscatedWest() {
        if (isOkayToShowExactLocation) {
            obfuscatedWest = west;
            return;
        }

        obfuscatedWest = randomizeIfNeedBe(west, east, LONGITUDE, true);
    }

    /**
     * @return <b>either</b> the obfuscated value <b>or</b> the actual minimumLongitude, depending on the setting of the isOkayToShowExactLocation switch
     */
    @JsonView(JsonLookupFilter.class)
    public Double getObfuscatedWest() {
        return obfuscatedWest;
    }

    private void setObfuscatedEast() {
        if (isOkayToShowExactLocation) {
            obfuscatedEast = east;
            return;
        }
        obfuscatedEast = randomizeIfNeedBe(east, west, LONGITUDE, false);
    }

    /**
     * @return <b>either</b> the obfuscated value <b>or</b> the actual maximumLongitude, depending on the setting of the isOkayToShowExactLocation switch
     */
    @JsonView(JsonLookupFilter.class)
    public Double getObfuscatedEast() {
        return obfuscatedEast;
    }

    /**
     * @throws TdarRuntimeException
     *             if the specified latitude falls outside the minimum and maximum latitude settings.
     * @param minimumLatitude
     *            the new minimum latitude
     */
    public void setSouth(Double minimumLatitude) {
        south = minimumLatitude;
    }

    @Transient
    public void setMiny(Double minY) {
        this.south = minY;
    }

    @Deprecated
    /**
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getNorth() {
        return north;
    }

    /**
     * @throws TdarRuntimeException
     *             if the specified latitude falls outside the minimum and maximum latitude settings.
     * @param maximumLatitude
     *            the new maximum latitude
     */
    public void setNorth(Double maximumLatitude) {
        north = maximumLatitude;
    }

    public void obfuscateAll() {
        if (north != null && south != null) {
            List<Double> dbls = Arrays.asList(north,south,east,west);
            int hashCode = dbls.hashCode();
            if (hash != hashCode) {
                hash = hashCode;
                setObfuscatedNorth();
                setObfuscatedSouth();
                setObfuscatedEast();
                setObfuscatedWest();
            }
        }
    }

    @Transient
    public void setMaxy(Double maxY) {
        this.north = maxY;
    }

    @Deprecated
    /**
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getWest() {
        return west;
    }

    /**
     * @throws TdarRuntimeException
     *             if the specified longitude falls outside the minimum and maximum longitude settings.
     * @param minimumLongitude
     *            the new minimum longitude
     */
    public void setWest(Double minimumLongitude) {
        west = minimumLongitude;
    }

    /**
     * Sets the minimum longitude, with no validation checking.
     * 
     * @param minX
     *            the new minimum longitude
     */
    @Transient
    public void setMinx(Double minX) {
        this.west = minX;
    }

    @Deprecated
    /**
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getEast() {
        return east;
    }

    /**
     * @throws TdarRuntimeException
     *             if the specified longitude falls outside the minimum and maximum longitude settings.
     * @param maximumLongitude
     *            the new maximum longitude
     */
    public void setEast(Double maximumLongitude) {
        east = maximumLongitude;
    }

    /**
     * Sets the maximum longitude, with no validation checking.
     * 
     * @param maxX
     *            the new maximum longitude
     */
    @Transient
    public void setMaxx(Double maxX) {
        this.east = maxX;
    }

    @Transient
    public boolean isValidLatitude(Double latitude) {
        // FIXME: verify that this works at extreme case (+/- 90)
        return latitude != null && latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE;
    }

    @Transient
    public boolean isValidLongitude(Double longitude) {
        // FIXME: verify that this works for extreme cases (+/- 180)
        return longitude != null && longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE;
    }

    @Override
    public boolean isValid() {
        if (isValidLatitude(north)
                && isValidLatitude(south)
                && isValidLongitude(west)
                && isValidLongitude(east)
                && north >= south
                && isValidLongitudeSpan(west, east)
                && Math.abs(north - south) < 180) {
            return true;
        }
        return false;
    }

    // this logic assumes no span greater than 180°, and that zero-length span is invalid.
    boolean isValidLongitudeSpan(double min, double max_) {
        double max = max_;
        if (Objects.equals(min, max)) {
            return true;
        }
        if (max < 0 && min > 0) {
            // when spanning IDL, pretend that flat map repeats as it extends past 180°E, e.g. 170°W is now 190°E
            max += 360;
        }
        logger.trace("min:{}\tmax:{}", min, max);
        return min < max;
    }

    @Override
    public String toString() {
        return String.format("Latitude [%s to %s], Longitude [%s to %s]", south, north, west, east);
    }

    public void copyValuesFrom(LatitudeLongitudeBox otherBox) {
        setSouth(otherBox.south);
        setNorth(otherBox.north);
        setWest(otherBox.west);
        setEast(otherBox.east);
    }

    public double getObfuscatedArea() {
        return getObfuscatedAbsoluteLatLength() * getObfuscatedAbsoluteLongLength();
    }

    public double getObfuscatedAbsoluteLatLength() {
        if (getObfuscatedNorth() == null) {
            obfuscateAll();
        }
        return Math.abs(getObfuscatedNorth() - getObfuscatedSouth());
    }

    public double getObfuscatedAbsoluteLongLength() {
        if (getObfuscatedEast() == null) {
            obfuscateAll();
        }
        return Math.abs(getObfuscatedEast() - getObfuscatedWest());
    }

    public double getAbsoluteLatLength() {
        if (getObfuscatedNorth() == null) {
            obfuscateAll();
        }
        return Math.abs(getObfuscatedNorth() - getObfuscatedSouth());
    }

    public double getAbsoluteLongLength() {
        if (getObfuscatedEast() == null) {
            obfuscateAll();
        }
        return Math.abs(getObfuscatedEast() - getObfuscatedWest());
    }

    public double getArea() {
        return getObfuscatedAbsoluteLatLength() * getObfuscatedAbsoluteLongLength();
    }

    /**
     * @return
     */
    public boolean crossesDateline() {
        if (getObfuscatedEast() == null) {
            obfuscateAll();
        }
        return LatitudeLongitudeBox.crossesDateline(getObfuscatedWest(), getObfuscatedEast());
    }

    public boolean crossesPrimeMeridian() {
        if (getObfuscatedEast() == null) {
            obfuscateAll();
        }
        return LatitudeLongitudeBox.crossesPrimeMeridian(getObfuscatedWest(), getObfuscatedEast());
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

    @Override
    public boolean isValidForController() {
        return true;
    }

    @Override
    @XmlTransient
    public boolean isObfuscated() {
        return obfuscated;
    }

    @Override
    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    private transient Boolean obfuscatedObjectDifferent;

    @XmlTransient
    public Boolean getObfuscatedObjectDifferent() {
        return obfuscatedObjectDifferent;
    }

    public void setObfuscatedObjectDifferent(Boolean obfuscatedObjectDifferent) {
        this.obfuscatedObjectDifferent = obfuscatedObjectDifferent;
    }

    @Override
    // @XmlTransient
    public Set<Obfuscatable> obfuscate() {
        // set directly, as we don't want to reset the obfuscated values
        obfuscatedObjectDifferent = false;
        logger.trace("obfuscating latLong");
        if (getObfuscatedNorth() == null ) {
            obfuscateAll();
        }

        Double val = getObfuscatedNorth();
        if (ObjectUtils.notEqual(val, getNorth())) {
            setNorth(val);
            obfuscatedObjectDifferent = true;
        }
        val = getObfuscatedSouth();
        if (ObjectUtils.notEqual(val, getSouth())) {
            setSouth(val);
            obfuscatedObjectDifferent = true;
        }

        val = getObfuscatedEast();
        if (ObjectUtils.notEqual(val, getEast())) {
            setEast(val);
            obfuscatedObjectDifferent = true;
        }
        val = getObfuscatedWest();
        if (ObjectUtils.notEqual(val, getWest())) {
            setWest(val);
            obfuscatedObjectDifferent = true;
        }
        setObfuscated(true);
        return null;
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
    @Transient
    @JsonView(JsonLookupFilter.class)
    public Integer getScale() {
        Integer toReturn = -1;
        if (!isInitialized()) {
            return toReturn;
        }
        double scale = getAbsoluteLatLength();
        if (getAbsoluteLongLength() > scale) {
            scale = getAbsoluteLongLength();
        }
        if (scale < 1.0) { // 1
            toReturn += 2;
        }
        if (scale >= 1.0) { // 2
            toReturn++;
        }

        if (scale > 4.0) { // 3
            toReturn++;
        }
        if (scale > 9.0) { // 4
            toReturn++;
        }
        if (scale > 14.0) { // 5
            toReturn++;
        }

        if (scale > 19.0) { // 6
            toReturn++;
        }

        logger.trace("scale: {} ({})", scale, toReturn);
        return toReturn;
    }

    public boolean isInitialized() {
        return north != null && south != null && east != null && west != null;
    }

    public boolean isInitializedAndValid() {
        return isInitialized() && isValid();
    }

    /**
     * @return the isOkayToShowExactLocation if true, then the contents of this lat/long box are not obfuscated. if false, they are obfuscated.
     */
    @XmlAttribute
    public boolean isOkayToShowExactLocation() {
        return isOkayToShowExactLocation;
    }

    /**
     * @param isOkayToShowExactLocation
     *            if true, then the contents of this lat/long box can be shown without obfuscation:
     *            if false, they must be obfuscated
     */
    public void setOkayToShowExactLocation(boolean isOkayToShowExactLocation) {
        this.isOkayToShowExactLocation = isOkayToShowExactLocation;
    }

    @Deprecated
    public Double getMinimumLatitude() {
        return south;
    }

    @Deprecated
    public Double getMaximumLatitude() {
        return north;
    }

    @Deprecated
    public Double getMinimumLongitude() {
        return west;
    }

    @Deprecated
    public Double getMaximumLongitude() {
        return east;
    }

    @Deprecated
    public void setMinimumLatitude(Double val) {
        this.south = val;
    }

    @Deprecated
    public void setMaximumLatitude(Double val) {
        this.north = val;
    }

    @Deprecated
    public void setMinimumLongitude(Double val) {
        this.west = val;
    }

    @Deprecated
    public void setMaximumLongitude(Double val) {
        this.east = val;
    }

    public void setObfuscatedValues() {
        getObfuscatedNorth();
        getObfuscatedSouth();
        getObfuscatedEast();
        getObfuscatedWest();
        
    }

    public void setObfuscatedNorth(Double obfuscatedNorth) {
        this.obfuscatedNorth = obfuscatedNorth;
    }

    public void setObfuscatedWest(Double obfuscatedWest) {
        this.obfuscatedWest = obfuscatedWest;
    }

    public void setObfuscatedSouth(Double obfuscatedSouth) {
        this.obfuscatedSouth = obfuscatedSouth;
    }

    public void setObfuscatedEast(Double obfuscatedEast) {
        this.obfuscatedEast = obfuscatedEast;
    }

}
