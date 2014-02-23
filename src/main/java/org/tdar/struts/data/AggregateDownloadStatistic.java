package org.tdar.struts.data;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.tdar.core.bean.resource.InformationResource;

//I don't know that the propOrder here is necessary, and it may complicate things in the future
@XmlType(propOrder = {"name", "date", "count", "fid", "rid"})
public class AggregateDownloadStatistic implements Serializable {

    private static final long serialVersionUID = 1698960536676588440L;

    Date aggregateDate;
    Number count;
    String filename;
    Long informationResourceFileId;
    Long informationResourceId;

    private transient InformationResource informationResource;

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

    @XmlElement(name = "date")
    public Date getAggregateDate() {
        return aggregateDate;
    }

    public void setAggregateDate(Date aggregateDate) {
        this.aggregateDate = aggregateDate;
    }

    @XmlElement(name="count")
    public Number getCount() {
        return count;
    }

    public void setCount(Number count) {
        this.count = count;
    }

    @XmlElement(name="name")
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @XmlElement(name="fid")
    public Long getInformationResourceFileId() {
        return informationResourceFileId;
    }

    public void setInformationResourceFileId(Long informationResourceFileId) {
        this.informationResourceFileId = informationResourceFileId;
    }

    @XmlElement(name="rid")
    public Long getInformationResourceId() {
        return informationResourceId;
    }

    public void setInformationResourceId(Long informationResourceId) {
        this.informationResourceId = informationResourceId;
    }

    public void setInformationResource(InformationResource informationResource) {
        this.informationResource = informationResource;
        this.informationResourceId = informationResource.getId();
    }

    @XmlTransient
    public InformationResource getInformationResource() {
        return this.informationResource;
    }
}
