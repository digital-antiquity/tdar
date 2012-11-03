package org.tdar.core.bean.billing;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.tdar.core.bean.Persistable;
import org.tdar.core.dao.external.auth.TdarGroup;

/*
 * An activity represents a specific thing that can be "charged"
 */
@Entity
@Table(name="pos_entity")
public class Activity extends Persistable.Base {

    private static final long serialVersionUID = 6891881586235180640L;

    private String name;
    private Integer numberOfHours;
    private Long numberOfMb;
    private Long numberOfResources;
    private Long numberOfFiles;
    private Float price;
    private String currency;
    private Boolean enabled;
    private TdarGroup group;

    public Integer getNumberOfHours() {
        return numberOfHours;
    }

    public void setNumberOfHours(Integer numberOfHours) {
        this.numberOfHours = numberOfHours;
    }

    public Long getNumberOfMb() {
        return numberOfMb;
    }

    public Long getNumberOfBytes() {
        return getNumberOfMb() * 1048576L;
    }
    public void setNumberOfMb(Long numberOfMb) {
        this.numberOfMb = numberOfMb;
    }

    public Long getNumberOfResources() {
        return numberOfResources;
    }

    public void setNumberOfResources(Long numberOfResources) {
        this.numberOfResources = numberOfResources;
    }

    public Long getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(Long numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public TdarGroup getGroup() {
        return group;
    }

    public void setGroup(TdarGroup group) {
        this.group = group;
    }

}
