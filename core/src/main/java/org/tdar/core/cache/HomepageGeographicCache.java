package org.tdar.core.cache;

import java.io.Serializable;

import org.tdar.core.bean.resource.ResourceType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class HomepageGeographicCache implements Serializable {

    private static final long serialVersionUID = -8388269707436604346L;

    private String code;
    private ResourceType resourceType;
    private Integer count;
    private String label;
    private Long id;

    public HomepageGeographicCache(String code, ResourceType resourceType, String label, Integer count, Long id) {
        this.code = code;
        this.resourceType = resourceType;
        this.count = count;
        this.setLabel(label);
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
