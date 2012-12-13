package org.tdar.core.bean.billing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "pos_billing_model")
public class BillingActivityModel extends org.tdar.core.bean.Persistable.Base {

    private static final long serialVersionUID = 6967404358217769922L;

    private Integer version;

    @Column(name = "counting_space")
    private Boolean countingSpace = true;

    @Column(name = "counting_files")
    private Boolean countingFiles = true;

    @Column(name = "counting_resources")
    private Boolean countingResources = true;

    private Boolean active = false;

    private String description;

    @Column(name = "date_created")
    private Date dateCreated;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, targetEntity = BillingActivity.class)
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

}
