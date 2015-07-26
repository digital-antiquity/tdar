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
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Addressable;

/**
 * Created by jim on 12/8/14.
 */
@Entity
@Table(name = "data_integration_workflow")
public class DataIntegrationWorkflow extends Persistable.Base implements HasSubmitter, Updatable, Addressable {

    private static final long serialVersionUID = -3687383363452908687L;

    @Column(nullable = false, length = FieldLength.FIELD_LENGTH_255)
    private String title;

    @Column(length = FieldLength.FIELD_LENGTH_2048)
    private String description;

    @Column(name = "json_data")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String jsonData;

    @Column(name = "date_created", nullable = false)
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated = new Date();

    @Column(name = "date_updated", nullable = false)
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateUpdated = new Date();

    @Column(nullable = false)
    private int version = 1;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "user_id")
    @NotNull
    private TdarUser submitter;

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
        return submitter;
    }

    public void setSubmitter(TdarUser user) {
        this.submitter = user;
    }

    public String toString() {
        return String.format("%s: %s (%s)\nJSON: \t%s", title, submitter, getDateCreated(), jsonData);
    }

    @Override
    public void markUpdated(TdarUser p) {
        setSubmitter(p);
        if (getDateCreated() == null) {
            setDateCreated(new Date());
        }
        setDateUpdated(new Date());
    }

    @Override
    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String getUrlNamespace() {
        return "workspace/integration";
    }

    // convenience for deletion
    public String getName() {
        return title;
    }

    @Override
    public String getDetailUrl() {
        return String.format("/%s/%s", getUrlNamespace(), getId());
    }

}
