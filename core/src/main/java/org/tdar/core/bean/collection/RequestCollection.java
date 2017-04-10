package org.tdar.core.bean.collection;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;

@Entity
@Table(name = "collection_request")
public class RequestCollection extends AbstractPersistable {

    private static final long serialVersionUID = 7801330626092697128L;

    @ElementCollection()
    @CollectionTable(name = "collection_request_collection_ids", joinColumns = @JoinColumn(name = "collection_request_id") )
    @Column(name = "collection_id")
    private List<ResourceCollection> collections = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "general_permission", length = FieldLength.FIELD_LENGTH_50)
    private GeneralPermissions permission;
    
    @Column
    @Length(max = FieldLength.FIELD_LENGTH_500)
    private String name;

    @Column(name="description_request")
    @Length(max = FieldLength.FIELD_LENGTH_500)
    private String descriptionRequest;
    
    @Column(name="description_response")
    @Length(max = FieldLength.FIELD_LENGTH_500)
    private String descriptionResponse;
    
    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(name = "contact_id", nullable = false)
    private TdarUser contact;
    
    public List<ResourceCollection> getCollections() {
        return collections;
    }

    public void setCollections(List<ResourceCollection> collections) {
        this.collections = collections;
    }

    public GeneralPermissions getPermission() {
        return permission;
    }

    public void setPermission(GeneralPermissions permission) {
        this.permission = permission;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TdarUser getContact() {
        return contact;
    }

    public void setContact(TdarUser contact) {
        this.contact = contact;
    }

    public String getDescriptionRequest() {
        return descriptionRequest;
    }

    public void setDescriptionRequest(String descriptionRequest) {
        this.descriptionRequest = descriptionRequest;
    }

    public String getDescriptionResponse() {
        return descriptionResponse;
    }

    public void setDescriptionResponse(String descriptionResponse) {
        this.descriptionResponse = descriptionResponse;
    }
}
