package org.tdar.dataone.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdar.core.bean.FieldLength;

@Entity()
@Table(name = "dataone_object")
public class DataOneObject implements Serializable {

    private static final long serialVersionUID = 1186152334176236518L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = -1L;

    @Column(name = "identifier", length = FieldLength.FIELD_LENGTH_100)
    private String identifier;

    @Column(name = "series_id", length = FieldLength.FIELD_LENGTH_100)
    private String seriesId;

    @Column(name = "obsoletes", length = FieldLength.FIELD_LENGTH_100)
    private String obsoletes;

    @Column(name = "obsoleted_by", length = FieldLength.FIELD_LENGTH_100)
    private String obsoletedBy;

    @Column(name = "checksum", length = FieldLength.FIELD_LENGTH_255)
    private String checksum;

    @Column(name = "format_id", length = FieldLength.FIELD_LENGTH_255)
    private String formatId;

    @Column(name = "submitter", length = FieldLength.FIELD_LENGTH_512)
    private String submitter;

    @Column
    private Long size;

    @Column(name = "tdar_id")
    private Long tdarId;

    @Column(name = "sys_metadata_modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date sysMetadataModified;

    @Column(name = "date_created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", length = FieldLength.FIELD_LENGTH_255)
    private EntryType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public String getObsoletes() {
        return obsoletes;
    }

    public void setObsoletes(String obsoletes) {
        this.obsoletes = obsoletes;
    }

    public String getObsoletedBy() {
        return obsoletedBy;
    }

    public void setObsoletedBy(String obsoletedBy) {
        this.obsoletedBy = obsoletedBy;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Date getSysMetadataModified() {
        return sysMetadataModified;
    }

    public void setSysMetadataModified(Date sysMetadataModified) {
        this.sysMetadataModified = sysMetadataModified;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public Long getTdarId() {
        return tdarId;
    }

    public void setTdarId(Long tdarId) {
        this.tdarId = tdarId;
    }

    public String getFormatId() {
        return formatId;
    }

    public void setFormatId(String formatId) {
        this.formatId = formatId;
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

}
