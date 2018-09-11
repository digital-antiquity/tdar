package org.tdar.core.bean.file;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;

@Entity
@DiscriminatorValue(value = "DIR")
public class TdarDir extends AbstractFile {

    private static final long serialVersionUID = 4135346326567855165L;
    public static final String UNFILED = "unfiled";

    @ManyToOne
    @JoinColumn(name = "collection_id")
    private ResourceCollection collection;

    @Column(name = "collection_id", updatable = false, insertable = false)
    private Long collectionId;

    public TdarDir() {
    }

    public TdarDir(TdarUser basicUser, BillingAccount act, String name) {
        setAccount(act);
        setFilename(name);
        setUploader(basicUser);
        setDateCreated(new Date());
    }

    @Override
    public String toString() {
        return String.format("%s/ (%s)", getName(), getId());
    }

    public ResourceCollection getCollection() {
        return collection;
    }

    public void setCollection(ResourceCollection collection) {
        this.collection = collection;
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

}
