package org.tdar.web.service;

import java.io.Serializable;

public class HomepageDetails implements Serializable {

    private static final long serialVersionUID = 71663668424004219L;

    private String mapJson;
    private String localesJson;
    private String resourceTypeJson;

    public String getMapJson() {
        return mapJson;
    }

    public void setMapJson(String mapJson) {
        this.mapJson = mapJson;
    }

    public String getLocalesJson() {
        return localesJson;
    }

    public void setLocalesJson(String localesJson) {
        this.localesJson = localesJson;
    }

    public String getResourceTypeJson() {
        return resourceTypeJson;
    }

    public void setResourceTypeJson(String resourceTypeJson) {
        this.resourceTypeJson = resourceTypeJson;
    }

}
