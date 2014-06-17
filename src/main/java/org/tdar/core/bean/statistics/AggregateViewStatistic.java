package org.tdar.core.bean.statistics;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Immutable;
import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.resource.Resource;

//I don't know that the propOrder here is necessary, and it may complicate things in the future
@XmlType(propOrder = { "id", "date", "count" })
@Entity
@Table(name = "resource_access_day_agg")
@Immutable
public class AggregateViewStatistic extends Base implements Serializable {

    private static final long serialVersionUID = 1698960536676588440L;

    @Column(name = "date_accessed")
    private Date aggregateDate;
    private Long count;
    @ManyToOne(optional = true)
    @JoinColumn(nullable = false, name = "resource_id")
    private Resource resource;
    @Transient
    private Long resourceId;

    @XmlElement(name = "date")
    public Date getAggregateDate() {
        return aggregateDate;
    }

    public void setAggregateDate(Date aggregateDate) {
        this.aggregateDate = aggregateDate;
    }

    @XmlElement(name = "count")
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @XmlTransient
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @XmlElement(name = "id")
    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s)", aggregateDate, resourceId, count);
    }
}
