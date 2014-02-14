package org.tdar.core.bean.billing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Persistable;
import org.tdar.core.dao.external.auth.TdarGroup;

/*
 * An activity represents a specific thing that can be "charged"
 * 
 *  THIS CLASS IS DESIGNED TO BE IMMUTABLE -- SET ONCE, NEVER CAN CHANGE
 */
@Entity
@Table(name = "pos_billing_activity")
public class BillingActivity extends Persistable.Base implements Comparable<BillingActivity> {

    private static final long serialVersionUID = 6891881586235180640L;

    @Transient
    private transient final Logger logger = LoggerFactory.getLogger(getClass());

    public enum BillingActivityType {
        PRODUCTION, TEST;
    }

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String name;
    @Column(updatable = false)
    private Integer numberOfHours = 0;
    @Column(updatable = false)
    private Long numberOfMb = 0L;
    @Column(updatable = false)
    private Long numberOfResources = 0L;
    @Column(updatable = false)
    private Long numberOfFiles = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = 25)
    private BillingActivityType activityType = BillingActivityType.PRODUCTION;

    @Column(name = "sort_order")
    private Integer order;

    @ManyToOne(optional = false)
    @NotNull
    private BillingActivityModel model;

    public BillingActivity() {
    }

    public BillingActivity(String name, Float price, BillingActivityModel model) {
        this();
        this.name = name;
        this.price = price;
        this.model = model;
    }

    public BillingActivity(String name, Float price, Integer numHours, Long numberOfResources, Long numberOfFiles, Long numberOfMb, BillingActivityModel model) {
        this(name, price, model);
        setNumberOfHours(numHours);
        setNumberOfFiles(numberOfFiles);
        setNumberOfMb(numberOfMb);
        setNumberOfResources(numberOfResources);
    }

    // if the rates are based on total # of files; you might have a different rate based on 50 or 500 files
    @Column(updatable = false, name = "min_allowed_files")
    private Long minAllowedNumberOfFiles = 0L;

    // display values may be lower than actual values to give some wiggle room
    private Long displayNumberOfMb;
    private Long displayNumberOfResources;
    private Long displayNumberOfFiles;

    @Column(updatable = false)
    private Float price;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String currency;

    private Boolean enabled = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "groupName", length = FieldLength.FIELD_LENGTH_255)
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

    public Long getMinAllowedNumberOfFiles() {
        return minAllowedNumberOfFiles;
    }

    public void setMinAllowedNumberOfFiles(Long minAllowedNumberOfFiles) {
        this.minAllowedNumberOfFiles = minAllowedNumberOfFiles;
    }

    @Override
    public String toString() {
        return getName();
    }

    public BillingActivityModel getModel() {
        return model;
    }

    public void setModel(BillingActivityModel model) {
        this.model = model;
    }

    public boolean supportsFileLimit() {
        if (getNumberOfFiles() != null && getNumberOfFiles() > 0) {
            return true;
        }
        return false;
    }

    public boolean isProduction() {
        return getActivityType() == BillingActivityType.PRODUCTION;
    }

    public BillingActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(BillingActivityType activityType) {
        this.activityType = activityType;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public int compareTo(BillingActivity o) {
        if (!ObjectUtils.equals(getOrder(), o.getOrder())) {
            return ObjectUtils.compare(getOrder(), o.getOrder());
        } else {
            return ObjectUtils.compare(getName(), o.getName());
        }
    }

    private boolean isNullOrZero(Number number) {
        if (number == null || number.floatValue() == 0.0) {
            return true;
        }
        return false;
    }
    
    public boolean isSpaceOnly() {
        if (isNullOrZero(getNumberOfHours()) && isNullOrZero(getNumberOfResources()) && getNumberOfBytes() != null && getNumberOfBytes() > 0 && isNullOrZero(getNumberOfFiles()) ) {
            return true;
        }
        return false;
    }

    public boolean isFilesOnly() {
        if (isNullOrZero(getNumberOfHours()) && isNullOrZero(getNumberOfResources()) && getNumberOfFiles() != null && getNumberOfFiles() > 0 && isNullOrZero(getNumberOfBytes()) ) {
            return true;
        }
        return false;
    }
}
