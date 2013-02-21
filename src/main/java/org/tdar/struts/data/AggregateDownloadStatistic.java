package org.tdar.struts.data;

import java.io.Serializable;
import java.util.Date;

public class AggregateDownloadStatistic implements Serializable {

    private static final long serialVersionUID = 1698960536676588440L;

    Date aggregateDate;
    Number count;
    String filename;
    Long informationResourceFileId;
    Long informationResourceId;

    public AggregateDownloadStatistic() {
    }

    public AggregateDownloadStatistic(Long fileId, Date date, Number count) {
        this.informationResourceFileId = fileId;
        this.aggregateDate = date;
        this.count = count;
    }

    public AggregateDownloadStatistic(Date date, Number count, String filename, Long irfId, Long irId) {
        this.aggregateDate = date;
        this.count = count;
        this.filename = filename;
        this.informationResourceFileId = irfId;
        this.informationResourceId = irId;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getInformationResourceFileId() {
        return informationResourceFileId;
    }

    public void setInformationResourceFileId(Long informationResourceFileId) {
        this.informationResourceFileId = informationResourceFileId;
    }

    public Long getInformationResourceId() {
        return informationResourceId;
    }

    public void setInformationResourceId(Long informationResourceId) {
        this.informationResourceId = informationResourceId;
    }

}
