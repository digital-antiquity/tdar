package org.tdar.core.bean.resource.file;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.FieldLength;

/**
 * Proxy object for InformationResourceFileVersion difference from original is that it has no back-references
 * 
 * @author abrin
 * 
 */
@Entity
@Immutable
@Table(name = "information_resource_file_version")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.InformationResourceFileVersion")
public class InformationResourceFileVersionProxy implements Serializable {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Id
    private Long id;

    private static final long serialVersionUID = 8358699149214481070L;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String filename;

    @Column(name = "file_version")
    private Integer version;

    @Column(name = "mime_type")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String mimeType;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String format;

    @Column(name = "primary_file")
    private Boolean primaryFile = Boolean.FALSE;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String extension;

    @Column(name = "filestore_id")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String filestoreId;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String checksum;

    @Column(name = "checksum_type")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String checksumType;

    @Column(nullable = false, name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "file_type")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "internal_type")
    private VersionType fileVersionType;

    private Integer width;

    private Integer height;

    @Column(name = "size")
    private Long fileLength;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String path;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @XmlAttribute(name = "version")
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFilestoreId() {
        return filestoreId;
    }

    public void setFilestoreId(String filestoreId) {
        this.filestoreId = filestoreId;
    }

    public String getChecksum() {
        return checksum;
    }

    /*
     * Only set the checksum if it is not set
     */
    public void setChecksum(String checksum) {
        if (StringUtils.isEmpty(this.checksum)) {
            this.checksum = checksum;
        } else {
            logger.info("not setting checksum to :" + checksum + " b/c set to :" + this.checksum);
        }
    }

    public void overrideChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksumType() {
        return checksumType;
    }

    public void setChecksumType(String checksumType) {
        this.checksumType = checksumType;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public VersionType getFileVersionType() {
        return fileVersionType;
    }

    public void setFileVersionType(VersionType informationResourceFileVersionType) {
        this.fileVersionType = informationResourceFileVersionType;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Long getFileLength() {
        return fileLength;
    }

    public void setFileLength(Long size) {
        this.fileLength = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, #%d )", filename, fileVersionType, version);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @SuppressWarnings("deprecation")
    public InformationResourceFileVersion generateInformationResourceFileVersion() throws IllegalAccessException, InvocationTargetException {
        InformationResourceFileVersion vers = new InformationResourceFileVersion();
        vers.setId(getId());
        vers.setChecksum(getChecksum());
        vers.setChecksumType(getChecksumType());
        vers.setPath(getPath());
        vers.setFileLength(getFileLength());
        vers.setFilename(getFilename());
        vers.setFilestoreId(getFilestoreId());
        vers.setFileType(getFileType());
        vers.setVersion(getVersion());
        vers.setHeight(getHeight());
        vers.setWidth(getWidth());
        vers.setExtension(getExtension());
        vers.setFileVersionType(getFileVersionType());
        vers.setDateCreated(getDateCreated());
        vers.setFormat(getFormat());
        vers.setMimeType(getMimeType());
        return vers;
    }

}
