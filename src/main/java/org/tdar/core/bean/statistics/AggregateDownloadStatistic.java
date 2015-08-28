package org.tdar.core.bean.statistics;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Immutable;
import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.file.InformationResourceFile;

//I don't know that the propOrder here is necessary, and it may complicate things in the future
@Entity
@Table(name = "file_download_day_agg")
@Immutable
@XmlType(propOrder = { "name", "date", "count", "fid", "rid" })
public class AggregateDownloadStatistic extends Base implements Serializable {

    private static final long serialVersionUID = 1698960536676588440L;

    @Column(name = "date_accessed")
    @Temporal(TemporalType.DATE)
    private Date aggregateDate;

    private Long count;

    @ManyToOne(optional = true)
    @JoinColumn(nullable = true, name = "information_resource_file_id")
    private InformationResourceFile file;

    private Integer month;

    private Integer year;

    @Transient
    private String filename;
    @Transient
    private Long informationResourceFileId;
    @Transient
    private Long informationResourceId;

    private transient InformationResource informationResource;

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

    @XmlElement(name = "name")
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @XmlElement(name = "fid")
    public Long getInformationResourceFileId() {
        return informationResourceFileId;
    }

    public void setInformationResourceFileId(Long informationResourceFileId) {
        this.informationResourceFileId = informationResourceFileId;
    }

    @XmlElement(name = "rid")
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

    public InformationResourceFile getFile() {
        return file;
    }

    public void setFile(InformationResourceFile file) {
        this.file = file;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }
}
