package org.tdar.struts.data;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.tdar.core.bean.resource.Resource;

//I don't know that the propOrder here is necessary, and it may complicate things in the future
@XmlType(propOrder = { "id", "date", "count" })
public class AggregateViewStatistic implements Serializable {

    private static final long serialVersionUID = 1698960536676588440L;

    Date aggregateDate;
    Number count;
    Resource resource;
    private Long resourceId;

    public AggregateViewStatistic() {
    }

    public AggregateViewStatistic(Date date, Number count, Resource resource) {
        this.aggregateDate = date;
        this.count = count;
        this.resource = resource;
        this.resourceId = resource.getId();
    }

    public AggregateViewStatistic(Long resourceId, Date date, Number count) {
        this.aggregateDate = date;
        this.count = count;
        this.resourceId = resourceId;
    }

    @XmlElement(name = "date")
    public Date getAggregateDate() {
        return aggregateDate;
    }

    public void setAggregateDate(Date aggregateDate) {
        this.aggregateDate = aggregateDate;
    }

    @XmlElement(name = "count")
    public Number getCount() {
        return count;
    }

    public void setCount(Number count) {
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
