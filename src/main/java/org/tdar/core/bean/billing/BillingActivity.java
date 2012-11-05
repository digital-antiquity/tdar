package org.tdar.core.bean.billing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.tdar.core.bean.Persistable;
import org.tdar.core.dao.external.auth.TdarGroup;

/*
 * An activity represents a specific thing that can be "charged"
 * 
 *  THIS CLASS IS DESIGNED TO BE IMMUTABLE -- SET ONCE, NEVER CAN CHANGE
 */
@Entity
@Table(name = "pos_billing_activity")
public class BillingActivity extends Persistable.Base {

    private static final long serialVersionUID = 6891881586235180640L;

    private String name;
    @Column(updatable = false)
    private Integer numberOfHours;
    @Column(updatable = false)
    private Long numberOfMb;
    @Column(updatable = false)
    private Long numberOfResources;
    @Column(updatable = false)
    private Long numberOfFiles;
    // display values may be lower than actual values to give some wiggle room
    private Long displayNumberOfMb;
    private Long displayNumberOfResources;
    private Long displayNumberOfFiles;

    @Column(updatable = false)
    private Float price;
    private String currency;
    private Boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "groupName")
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

    public Long getDisplayNumberOfFiles() {
        return displayNumberOfFiles;
    }

    public void setDisplayNumberOfFiles(Long displayNumberOfFiles) {
        this.displayNumberOfFiles = displayNumberOfFiles;
    }

    public Long getDisplayNumberOfResources() {
        return displayNumberOfResources;
    }

    public void setDisplayNumberOfResources(Long displayNumberOfResources) {
        this.displayNumberOfResources = displayNumberOfResources;
    }

    public Long getDisplayNumberOfMb() {
        return displayNumberOfMb;
    }

    public void setDisplayNumberOfMb(Long displayNumberOfMb) {
        this.displayNumberOfMb = displayNumberOfMb;
    }

}
