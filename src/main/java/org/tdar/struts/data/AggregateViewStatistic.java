package org.tdar.struts.data;

import java.io.Serializable;
import java.util.Date;

import org.tdar.core.bean.resource.Resource;

public class AggregateViewStatistic implements Serializable {

    private static final long serialVersionUID = 1698960536676588440L;

    Date aggregateDate;
    Number count;
    Resource resource;

    public AggregateViewStatistic() {
    }

    public AggregateViewStatistic(Date date, Number count, Resource resource) {
        this.aggregateDate = date;
        this.count = count;
        this.resource = resource;
    }

    public Date getAggregateDate() {
        return aggregateDate;
    }

    public void setAggregateDate(Date aggregateDate) {
        this.aggregateDate = aggregateDate;
    }

    public Number getCount() {
        return count;
    }

    public void setCount(Number count) {
        this.count = count;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

}
