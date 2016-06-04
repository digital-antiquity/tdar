package org.tdar.core.bean.coverage;

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
//@ClassBridge(impl = LatLongClassBridge.class)
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

    public static final double ONE_MILE_IN_DEGREE_MINUTES = 0.01472d;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    /** used to record whether this instance has been obfuscated by the obfuscation service or not */
    private transient boolean obfuscated;

    /** if true, then the location does not need to be hidden */
    @Column(nullable = false, name = "is_ok_to_show_exact_location", columnDefinition = "boolean default false")
    private boolean isOkayToShowExactLocation = false;

    @Column(name = "min_obfuscated_lat")
    private Double minObfuscatedLatitude = null;
    @Column(name = "min_obfuscated_long")
    private Double minObfuscatedLongitude = null;
    @Column(name = "max_obfuscated_lat")
    private Double maxObfuscatedLatitude = null;
    @Column(name = "max_obfuscated_long")
    private Double maxObfuscatedLongitude = null;

    // ranges from -90 (South) to +90 (North)
    @Column(nullable = false, name = "minimum_latitude")
    @DecimalMin(value="-90.0",inclusive=true)
    @DecimalMax(value="90.0",inclusive=true)
    @NotNull
    private Double minimumLatitude;

    @Column(nullable = false, name = "maximum_latitude")
    @DecimalMin(value="-90.0",inclusive=true)
    @DecimalMax(value="90.0",inclusive=true)
    @NotNull
    private Double maximumLatitude;

    // ranges from -180 (West) to +180 (East)
    @Column(nullable = false, name = "minimum_longitude")
    @DecimalMin(value="-180.0",inclusive=true)
    @DecimalMax(value="180.0",inclusive=true)
    @NotNull
    private Double minimumLongitude;

    @Column(nullable = false, name = "maximum_longitude")
    @DecimalMin(value="-180.0",inclusive=true)
    @DecimalMax(value="180.0",inclusive=true)
    @NotNull
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
    /**
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getMinimumLatitude() {
        return minimumLatitude;
    }

    public Double getObfuscatedCenterLatitude() {
        return getCenterLat(getMaxObfuscatedLatitude(), getMinObfuscatedLatitude());
    }

    protected Double getCenterLat(Double double1, Double double2) {
        return (double1 + double2 )/ 2d;
    }

    public Double getObfuscatedCenterLongitude() {
        return getCenterLong(getMinObfuscatedLongitude(), getMaxObfuscatedLongitude());
    }

    @Deprecated
    public Double getCenterLatitude() {
        return getCenterLat(getMaximumLatitude(), getMinimumLatitude());
    }

    @Deprecated
    public Double getCenterLongitude() {
        return getCenterLong(getMinimumLongitude(), getMaximumLongitude());
    }

    
    protected Double getCenterLong(Double minLong, Double maxLong) {
        if (maxLong < minLong) {
            Double tmp = (minLong + maxLong * -1d + 180d) / 2d;
            if (tmp > 180) {
                tmp = 180 - tmp;
            }
            return tmp;
        } 
        return (minLong + maxLong) / 2d;
 
    }
    /**
     * @return a helper method, useful for testing. Returns true if one or more of the obfuscated values differs from the original, false otherwise.
     */

    @Transient
    public boolean isObfuscatedObjectDifferent() {
        if (obfuscatedObjectDifferent == null) {
            logger.debug("should call obfuscate before testing obfuscation");
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

    private void setMinObfuscatedLatitude() {
        minObfuscatedLatitude = randomizeIfNeedBe(minimumLatitude, maximumLatitude, LATITUDE, true);
    }

    /**
     * @return <b>either</b> the obfuscated value <b>or</b> the actual minimumLatitude, depending on the setting of the isOkayToShowExactLocation switch
     */
    @JsonView(JsonLookupFilter.class)
    public Double getMinObfuscatedLatitude() {
        if (minObfuscatedLatitude == null) {
            setMinObfuscatedLatitude();
        }
        return getProtectedResult(minObfuscatedLatitude, minimumLatitude);
    }

    private void setMaxObfuscatedLatitude() {
        maxObfuscatedLatitude = randomizeIfNeedBe(maximumLatitude, minimumLatitude, LATITUDE, false);
    }

    /**
     * @return <b>either</b> the obfuscated value <b>or</b> the actual maximumLatitude, depending on the setting of the isOkayToShowExactLocation switch
     */
    @JsonView(JsonLookupFilter.class)
    public Double getMaxObfuscatedLatitude() {
        if (maxObfuscatedLatitude == null) {
            setMaxObfuscatedLatitude();
        }
        return getProtectedResult(maxObfuscatedLatitude, maximumLatitude);
    }

    private void setMinObfuscatedLongitude() {
        minObfuscatedLongitude = randomizeIfNeedBe(minimumLongitude, maximumLongitude, LONGITUDE, true);
    }

    /**
     * @return <b>either</b> the obfuscated value <b>or</b> the actual minimumLongitude, depending on the setting of the isOkayToShowExactLocation switch
     */
    @JsonView(JsonLookupFilter.class)
    public Double getMinObfuscatedLongitude() {
        if (minObfuscatedLongitude == null) {
            setMinObfuscatedLongitude();
        }
        return getProtectedResult(minObfuscatedLongitude, minimumLongitude);
    }

    private void setMaxObfuscatedLongitude() {
        maxObfuscatedLongitude = randomizeIfNeedBe(maximumLongitude, minimumLongitude, LONGITUDE, false);
    }

    /**
     * @return <b>either</b> the obfuscated value <b>or</b> the actual maximumLongitude, depending on the setting of the isOkayToShowExactLocation switch
     */
    @JsonView(JsonLookupFilter.class)
    public Double getMaxObfuscatedLongitude() {
        if (maxObfuscatedLongitude == null) {
            setMaxObfuscatedLongitude();
        }
        return getProtectedResult(maxObfuscatedLongitude, maximumLongitude);
    }

    /**
     * @throws TdarRuntimeException
     *             if the specified latitude falls outside the minimum and maximum latitude settings.
     * @param minimumLatitude
     *            the new minimum latitude
     */
    public void setMinimumLatitude(Double minimumLatitude) {
//        if ((minimumLatitude != null) && !isValidLatitude(minimumLatitude)) {
//            throw new TdarValidationException("latLong.lat_invalid");
//        }
        setMiny(minimumLatitude);
    }

    @Transient
    public void setMiny(Double minY) {
        this.minimumLatitude = minY;
        setMinObfuscatedLatitude();
    }

    @Deprecated
    /**
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getMaximumLatitude() {
        return maximumLatitude;
    }

    /**
     * @throws TdarRuntimeException
     *             if the specified latitude falls outside the minimum and maximum latitude settings.
     * @param maximumLatitude
     *            the new maximum latitude
     */
    public void setMaximumLatitude(Double maximumLatitude) {
//        if ((maximumLatitude != null) & !isValidLatitude(maximumLatitude)) {
//            throw new TdarValidationException("latLong.lat_invalid");
//        }
        setMaxy(maximumLatitude);
    }

    @Transient
    public void setMaxy(Double maxY) {
        this.maximumLatitude = maxY;
        setMaxObfuscatedLatitude();
    }

    @Deprecated
    /**
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getMinimumLongitude() {
        return minimumLongitude;
    }

    /**
     * @throws TdarRuntimeException
     *             if the specified longitude falls outside the minimum and maximum longitude settings.
     * @param minimumLongitude
     *            the new minimum longitude
     */
    public void setMinimumLongitude(Double minimumLongitude) {
//        if ((minimumLongitude != null) && !isValidLongitude(minimumLongitude)) {
//            throw new TdarValidationException("latLong.long_invalid");
//        }
        setMinx(minimumLongitude);
    }

    /**
     * Sets the minimum longitude, with no validation checking.
     * 
     * @param minX
     *            the new minimum longitude
     */
    @Transient
    public void setMinx(Double minX) {
        this.minimumLongitude = minX;
        setMinObfuscatedLongitude();
    }

    @Deprecated
    /**
     * This property should only be used by hibernate or if you REALLY need the unobfuscated version
     */
    public Double getMaximumLongitude() {
        return maximumLongitude;
    }

    /**
     * @throws TdarRuntimeException
     *             if the specified longitude falls outside the minimum and maximum longitude settings.
     * @param maximumLongitude
     *            the new maximum longitude
     */
    public void setMaximumLongitude(Double maximumLongitude) {
//        if ((maximumLongitude != null) && !isValidLongitude(maximumLongitude)) {
//            throw new TdarValidationException("latLong.long_invalid");
//        }
        setMaxx(maximumLongitude);
    }

    /**
     * Sets the maximum longitude, with no validation checking.
     * 
     * @param maxX
     *            the new maximum longitude
     */
    @Transient
    public void setMaxx(Double maxX) {
        this.maximumLongitude = maxX;
        setMaxObfuscatedLongitude();
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
    private boolean isValidLongitudeSpan(double min, double max_) {
        double max = max_;
        if (max < 0 && min > 0) {
            // when spanning IDL, pretend that flat map repeats as it extends past 180°E, e.g. 170°W is now 190°E
            max += 360;
        }
        logger.trace("min:{}\tmax:{}", min, max);
        return min < max;
    }

    @Override
    public String toString() {
        return String.format("Latitude [%s to %s], Longitude [%s to %s]", minimumLatitude, maximumLatitude, minimumLongitude, maximumLongitude);
    }

    public void copyValuesFrom(LatitudeLongitudeBox otherBox) {
        setMinimumLatitude(otherBox.minimumLatitude);
        setMaximumLatitude(otherBox.maximumLatitude);
        setMinimumLongitude(otherBox.minimumLongitude);
        setMaximumLongitude(otherBox.maximumLongitude);
    }

    public double getObfuscatedArea() {
        return getObfuscatedAbsoluteLatLength() * getObfuscatedAbsoluteLongLength();
    }

    public double getObfuscatedAbsoluteLatLength() {
        return Math.abs(getMaxObfuscatedLatitude() - getMinObfuscatedLatitude());
    }

    public double getObfuscatedAbsoluteLongLength() {
        return Math.abs(getMaxObfuscatedLongitude() - getMinObfuscatedLongitude());
    }

    public double getAbsoluteLatLength() {
        return Math.abs(getMaxObfuscatedLatitude() - getMinObfuscatedLatitude());
    }

    public double getAbsoluteLongLength() {
        return Math.abs(getMaxObfuscatedLongitude() - getMinObfuscatedLongitude());
    }

    public double getArea() {
        return getObfuscatedAbsoluteLatLength() * getObfuscatedAbsoluteLongLength();
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
        Double val = getMaxObfuscatedLatitude();
        if (ObjectUtils.notEqual(val, getMaximumLatitude())) {
            setMaximumLatitude(val);
            obfuscatedObjectDifferent = true;
        }
        val = getMinObfuscatedLatitude();
        if (ObjectUtils.notEqual(val, getMinimumLatitude())) {
            setMinimumLatitude(val);
            obfuscatedObjectDifferent = true;
        }

        val = getMaxObfuscatedLongitude();
        if (ObjectUtils.notEqual(val, getMaximumLongitude())) {
            setMaximumLongitude(val);
            obfuscatedObjectDifferent = true;
        }
        val = getMinObfuscatedLongitude();
        if (ObjectUtils.notEqual(val, getMinimumLongitude())) {
            setMinimumLongitude(val);
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
        return maximumLatitude != null && minimumLatitude != null && maximumLongitude != null && minimumLongitude != null;
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
}
