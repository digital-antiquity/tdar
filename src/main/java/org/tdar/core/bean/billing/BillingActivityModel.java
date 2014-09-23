package org.tdar.core.bean.billing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;

@Entity
@Table(name = "pos_billing_model")
public class BillingActivityModel extends org.tdar.core.bean.Persistable.Base {

    /*
     * A 1:1 representation of a billing model and a set of activites. As a billing model changes, a new version should be published with new activities. At
     * that point, a new invoice may need to be generated which makes the billing model "whole" for previous customers.
     * 
     * e.g.
     * Model 1: assumes at Resources are "Free" (2010)
     * Model 2: charges per Resource. (2014)
     * 
     * In order to implement Model 2, you will need to issue an invoice for every user which "credits" them for all of the previous resources they've used in
     * the time between 2010 and 2014.
     */

    private static final long serialVersionUID = 6967404358217769922L;

    private Integer version;

    @Column(name = "counting_space")
    private Boolean countingSpace = true;

    @Column(name = "counting_files")
    private Boolean countingFiles = true;

    @Column(name = "counting_resources")
    private Boolean countingResources = true;

    private Boolean active = false;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String description;

    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, mappedBy = "model")
    private List<BillingActivity> activities = new ArrayList<BillingActivity>();

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getCountingSpace() {
        return countingSpace;
    }

    public void setCountingSpace(Boolean countingSpace) {
        this.countingSpace = countingSpace;
    }

    public Boolean getCountingFiles() {
        return countingFiles;
    }

    public void setCountingFiles(Boolean countingFiles) {
        this.countingFiles = countingFiles;
    }

    public Boolean getCountingResources() {
        return countingResources;
    }

    public void setCountingResources(Boolean countingResources) {
        this.countingResources = countingResources;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public List<BillingActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<BillingActivity> activities) {
        this.activities = activities;
    }

    @Override
    public String toString() {
        return String.format("%s r:%s f:%s s:%s", getVersion(), countingFiles, countingResources, countingSpace);
    }

}
