package org.tdar.core.bean.statistics;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.tdar.core.bean.resource.Resource;

@Entity
@Table(name = "resource_access_statistics")
@org.hibernate.annotations.Table( appliesTo ="resource_access_statistics", indexes = {
        @Index(name="resource_access_stats_count_id", columnNames = {"id", "resource_id"}),
        @Index(name="resource_access_stats_id", columnNames = {"resource_id"})
})
public class ResourceAccessStatistic extends AbstractResourceStatistic<Resource> {
    private static final long serialVersionUID = 3754152671288642718L;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "resource_id")
    private Resource reference;

    public Resource getReference() {
        return reference;
    }

    public void setReference(Resource reference) {
        this.reference = reference;
    }

    public ResourceAccessStatistic() {
    };

    public ResourceAccessStatistic(Date date, Resource r) {
        setDate(date);
        setReference(r);
    }

}
