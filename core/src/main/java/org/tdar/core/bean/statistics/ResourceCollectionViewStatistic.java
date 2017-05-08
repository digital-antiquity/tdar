package org.tdar.core.bean.statistics;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.tdar.core.bean.collection.ResourceCollection;

@Entity
@Table(name = "resource_collection_view_statistics", indexes = {
        @Index(name = "resource_collection_view_stats_count_id", columnList = "resource_collection_id, id")
})
public class ResourceCollectionViewStatistic extends AbstractResourceStatistic<ResourceCollection> {

    private static final long serialVersionUID = -2287260111716354232L;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "resource_collection_id", nullable=true, foreignKey = @javax.persistence.ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    @NotFound(action = NotFoundAction.IGNORE)
    // should be able to be removed with Hibernate 5
//    @ForeignKey(name = "none")
    private ResourceCollection reference;

    public ResourceCollectionViewStatistic() {
    };

    public ResourceCollectionViewStatistic(Date date, ResourceCollection r, boolean isBot) {
        setDate(date);
        setReference(r);
        setBot(isBot);
    }

    @Override
    public ResourceCollection getReference() {
        return reference;
    }

    @Override
    public void setReference(ResourceCollection reference) {
        this.reference = reference;
    }

}
