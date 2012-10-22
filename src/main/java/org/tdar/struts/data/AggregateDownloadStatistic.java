package org.tdar.struts.data;

import java.io.Serializable;
import java.util.Date;

import org.tdar.core.bean.resource.InformationResourceFile;

public class AggregateDownloadStatistic implements Serializable {

    private static final long serialVersionUID = 1698960536676588440L;

    Date aggregateDate;
    Number count;
    private InformationResourceFile file;

    public AggregateDownloadStatistic() {}
    
    public AggregateDownloadStatistic(Date date, Number count, InformationResourceFile file) {
        this.aggregateDate=  date;
        this.count= count;
        this.file = file;
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

    public InformationResourceFile getFile() {
        return file;
    }

    public void setFile(InformationResourceFile file) {
        this.file = file;
    }

}
