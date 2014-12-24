package org.tdar.core.bean.integration;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;

/**
 * Created by jim on 12/8/14.
 */
@Entity
@Table(name = "data_integration_workflow")
public class DataIntegrationWorkflow extends Persistable.Base implements HasSubmitter {

    private static final long serialVersionUID = -3687383363452908687L;

    @Column(nullable = false)
    private String title;

    @Column(length=2047)
    private String description;

    @Column(name = "json_data")
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String jsonData;

    @Column(name = "date_created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated = new Date();
    
    @Column(name = "last_updated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated = new Date();
    
    @Column(nullable = false)
    private int version = 1;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "user_id")
    @NotNull
    private TdarUser user;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public TdarUser getSubmitter() {
        return user;
    }

    public TdarUser getUser() {
        return user;
    }

    public void setUser(TdarUser user) {
        this.user = user;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated() {
        this.lastUpdated = new Date();
    }
    
    public String toString() {
        return String.format("%s: %s (%s)\nJSON: \t%s", title, user, dateCreated, jsonData); 
    }
}
