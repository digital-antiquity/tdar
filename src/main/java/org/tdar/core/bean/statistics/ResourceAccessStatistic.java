package org.tdar.core.bean.statistics;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.tdar.core.bean.resource.Resource;

@Entity
@Table(name = "resource_access_statistics", indexes = {
        @Index(name = "resource_access_stats_count_id", columnList = "id, resource_id"),
        @Index(name = "resource_access_stats_id", columnList = "resource_id")
})
/**
 * Tracks anonymous view statistics for tDAR
 * 
 * @author abrin
 *
 */
public class ResourceAccessStatistic extends AbstractResourceStatistic<Resource> {

    private static final long serialVersionUID = -980531380055105219L;
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "resource_id")
    private Resource reference;

    @Override
    public Resource getReference() {
        return reference;
    }

    @Override
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
