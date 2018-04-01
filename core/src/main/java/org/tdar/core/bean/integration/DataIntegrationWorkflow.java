package org.tdar.core.bean.integration;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.Hideable;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.HasAuthorizedUsers;

/**
 * Created by jim on 12/8/14.
 */
@Entity
@Table(name = "data_integration_workflow")
public class DataIntegrationWorkflow extends AbstractPersistable
        implements HasSubmitter, Updatable, Addressable, HasAuthorizedUsers, Indexable, Viewable, Hideable {

    private static final long serialVersionUID = -3687383363452908687L;
    private transient boolean viewable;

    public DataIntegrationWorkflow() {
    }

    @Column(nullable = false, length = FieldLength.FIELD_LENGTH_255)
    private String title;

    @Column(length = FieldLength.FIELD_LENGTH_2048)
    private String description;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = true;

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

    @Transient
    private transient boolean editable;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = false, name = "user_id")
    @NotNull
    private TdarUser submitter;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "integration_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean,integration.dataIntegrationWorkflow.authorizedUsers")
    private Set<AuthorizedUser> authorizedUsers = new LinkedHashSet<AuthorizedUser>();

    public DataIntegrationWorkflow(String string, boolean b, TdarUser adminUser) {
        this.title = string;
        this.hidden = b;
        this.submitter = adminUser;
    }

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
        return "workspace/integrate";
    }

    // convenience for deletion
    public String getName() {
        return title;
    }

    @Override
    public String getDetailUrl() {
        return String.format("/%s/%s", getUrlNamespace(), getId());
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @XmlTransient
    @Override
    public boolean isViewable() {
        return viewable;
    }

    @Override
    public void setViewable(boolean viewable) {
        this.viewable = viewable;
    }

    public Set<AuthorizedUser> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public void setAuthorizedUsers(Set<AuthorizedUser> authorizedUsers) {
        this.authorizedUsers = authorizedUsers;
    }

    public void copyFrom(DataIntegrationWorkflow workflow, TdarUser user) {
        this.setDateCreated(new Date());
        this.setDateUpdated(new Date());
        this.setDescription(workflow.getDescription());
        this.setTitle(workflow.getTitle() + " (Copy)");
        this.setSubmitter(user);
        this.setHidden(workflow.isHidden());
        this.setJsonData(workflow.getJsonData());
    }

    @Transient
    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

}
