package org.tdar.core.bean.billing;

import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.TdarGroup;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * An activity represents a specific thing that can be "charged"
 * 
 *  THIS CLASS IS DESIGNED TO BE IMMUTABLE -- SET ONCE, NEVER CAN CHANGE
 */
@Entity
@Table(name = "pos_billing_activity")
public class BillingActivity extends AbstractPersistable implements Comparable<BillingActivity> {

    private static final long serialVersionUID = 6891881586235180640L;

    private static final long BYTES_IN_MB = 1_048_576L;

    public enum BillingActivityType {
        PRODUCTION,
        TEST,
        ACCESSION,
        WAIVER
    }

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String name;
    @Column(updatable = false, name = "num_hours")
    private Integer numberOfHours = 0;
    @Column(updatable = false, name = "num_mb")
    private Long numberOfMb = 0L;
    @Column(updatable = false, name = "num_resources")
    private Long numberOfResources = 0L;
    @Column(updatable = false, name = "num_files")
    private Long numberOfFiles = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = FieldLength.FIELD_LENGTH_25)
    private BillingActivityType activityType = BillingActivityType.PRODUCTION;

    @Column(name = "sort_order")
    private Integer order;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @NotNull
    private BillingActivityModel model;

    // if the rates are based on total # of files; you might have a different rate based on 50 or 500 files
    @Column(updatable = false, name = "min_allowed_files")
    private Long minAllowedNumberOfFiles = 0L;

    // display values may be lower than actual values to give some wiggle room
    @Column(name = "display_num_mb")
    private Long displayNumberOfMb;
    @Column(name = "display_num_resources")
    private Long displayNumberOfResources;
    @Column(name = "display_num_files")
    private Long displayNumberOfFiles;

    @Column(updatable = false)
    private Float price;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String currency;

    @Column(name = "active")
    private Boolean active = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_name", length = FieldLength.FIELD_LENGTH_255)
    private TdarGroup group;

    public BillingActivity() {
    }

    public BillingActivity(String name, Float price, BillingActivityModel model) {
        this.name = name;
        this.price = price;
        this.model = model;
    }

    /**
     * Creates a new BilingActivity that contains the quota limits for usage.
     * 
     * @param name
     * @param price
     *            The cost for the quota limits
     * @param numHours
     *            The number of hours available
     * @param numberOfResources
     *            The number of resources available
     * @param numberOfFiles
     *            The limit of the number of files
     * @param numberOfMb
     *            The limit of the amount of space, in megabytes
     * @param model
     */
    public BillingActivity(String name, Float price, Integer numHours, Long numberOfResources, Long numberOfFiles, Long numberOfMb,
            BillingActivityModel model) {
        this(name, price, model);
        setNumberOfHours(numHours);
        setNumberOfFiles(numberOfFiles);
        setNumberOfMb(numberOfMb);
        setNumberOfResources(numberOfResources);
    }

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
        return getNumberOfMb() * BYTES_IN_MB;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean enabled) {
        this.active = enabled;
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

    @JsonIgnore
    public BillingActivityModel getModel() {
        return model;
    }

    public void setModel(BillingActivityModel model) {
        this.model = model;
    }

    public boolean supportsFileLimit() {
        return (getNumberOfFiles() != null) && (getNumberOfFiles() > 0);
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
        if (!Objects.equals(getOrder(), o.getOrder())) {
            return ObjectUtils.compare(getOrder(), o.getOrder());
        } else {
            return ObjectUtils.compare(getName(), o.getName());
        }
    }

    private boolean isNullOrZero(Number number) {
        return (number == null) || (number.floatValue() == 0.0);
    }

    public boolean isSpaceOnly() {
        return (isNullOrZero(getNumberOfHours()) && isNullOrZero(getNumberOfResources())
                && (getNumberOfBytes() != null) && (getNumberOfBytes() > 0)
                && isNullOrZero(getNumberOfFiles()));

    }

    public boolean isFilesOnly() {
        return (isNullOrZero(getNumberOfHours()) && isNullOrZero(getNumberOfResources())
                && (getNumberOfFiles() != null) && (getNumberOfFiles() > 0)
                && isNullOrZero(getNumberOfBytes()));
    }

    public boolean isAccessionFee() {
        return getActivityType() == BillingActivityType.ACCESSION;
    }

    public boolean isWaiver() {
        return getActivityType() == BillingActivityType.WAIVER;
    }

    public boolean isTest() {return getActivityType() == BillingActivityType.TEST;}
}
