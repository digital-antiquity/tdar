package org.tdar.utils.json;

import java.io.Serializable;

import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.FeedSearchHelper;
import org.tdar.core.service.RssService.GeoRssMode;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class LatitudeLongitudeBoxWrapper implements Serializable {

    private static final long serialVersionUID = 9175448938321937035L;

    private double south;
    private double north;
    private double west;
    private double east;
    private double centerLatitude;
    private double centerLongitude;
    private GeoRssMode mode = GeoRssMode.ENVELOPE; 
    private Resource resource;

    private Class<?> jsonView = null;
    private boolean spatial;

    @SuppressWarnings("deprecation")
    public LatitudeLongitudeBoxWrapper(Resource resource, FeedSearchHelper helper) {
        this.jsonView = helper.getJsonFilter();
        if (helper.getGeoMode() != null) {
            this.mode = helper.getGeoMode();
        }
        if (resource != null) {
            this.resource = resource;
            LatitudeLongitudeBox llb = resource.getFirstActiveLatitudeLongitudeBox();
            if (llb != null) {
                if (helper.isOverrideAndObfuscate() == true || resource.isLatLongVisible()) {
                    setSpatial(true);
                    this.centerLatitude = llb.getObfuscatedCenterLatitude();
                    this.centerLongitude = llb.getObfuscatedCenterLongitude();
                    this.south = llb.getObfuscatedSouth();
                    this.west = llb.getObfuscatedWest();
                    this.north = llb.getObfuscatedNorth();
                    this.east = llb.getObfuscatedEast();
                }
                
                if (helper.isOverrideAndObfuscate() == false && resource.isConfidentialViewable()) {
                    setSpatial(true);
                    this.south = llb.getSouth();
                    this.west = llb.getWest();
                    this.north = llb.getNorth();
                    this.east = llb.getEast();
                    this.centerLatitude = llb.getCenterLatitude();
                    this.centerLongitude = llb.getCenterLongitude();
                    
                }
            }
        }
    }

    public double getSouth() {
        return south;
    }

    public void setSouth(double minLatitude) {
        this.south = minLatitude;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(double maxLatitude) {
        this.north = maxLatitude;
    }

    public double getWest() {
        return west;
    }

    public void setWest(double minLongitude) {
        this.west = minLongitude;
    }

    public double getEast() {
        return east;
    }

    public void setEast(double maxLongitude) {
        this.east = maxLongitude;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public GeoRssMode getMode() {
        return mode;
    }

    public void setMode(GeoRssMode mode) {
        this.mode = mode;
    }

    public double getCenterLongitude() {
        return centerLongitude;
    }

    public void setCenterLongitude(double centerLongitude) {
        this.centerLongitude = centerLongitude;
    }

    public double getCenterLatitude() {
        return centerLatitude;
    }

    public void setCenterLatitude(double centerLatitude) {
        this.centerLatitude = centerLatitude;
    }

    public boolean isSpatial() {
        return spatial;
    }

    public void setSpatial(boolean spatial) {
        this.spatial = spatial;
    }

    public Class<?> getJsonView() {
        return jsonView;
    }

    public void setJsonView(Class<?> jsonView) {
        this.jsonView = jsonView;
    }
    
}
