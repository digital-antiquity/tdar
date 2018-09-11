package org.tdar.core.bean.resource.file;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAttribute;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.SortNatural;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.FieldLength;
import org.tdar.filestore.FileType;

/**
 * $Id$
 * 
 * Represents a 1:1 proxy with an InformationResourceFile, but it has no back-references.
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
@Entity
@Immutable
@Table(name = "information_resource_file")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.InformationResourceFile")
@Cacheable
public class InformationResourceFileProxy implements Serializable {

    private static final long serialVersionUID = -1321714940676599837L;
    @SuppressWarnings("unused")
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Column(name = "sequence_number")
    protected Integer sequenceNumber = 0;

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setPartOfComposite(Boolean partOfComposite) {
        this.partOfComposite = partOfComposite;
    }

    @Id
    private Long id;

    @Column(name = "part_of_composite")
    private Boolean partOfComposite = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "general_type", length = FieldLength.FIELD_LENGTH_255)
    private FileType informationResourceFileType;

    @Column(name = "latest_version")
    private Integer latestVersion = 0;

    @Column(name = "filename", length = FieldLength.FIELD_LENGTH_255)
    private String filename;

    @Column(name = "deleted", columnDefinition = "boolean default false")
    private Boolean deleted = Boolean.FALSE;

    @OneToMany()
    @SortNatural
    @JoinColumn(name = "information_resource_file_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.InformationResourceFile.informationResourceFileVersions")
    private List<InformationResourceFileVersionProxy> informationResourceFileVersionProxies = new ArrayList<InformationResourceFileVersionProxy>();

    @Enumerated(EnumType.STRING)
    @Column(length = FieldLength.FIELD_LENGTH_50)
    private FileAccessRestriction restriction = FileAccessRestriction.PUBLIC;

    // a date in standard form that a resource will become public if availableToPublic was set to false.
    // This date may be extended by the publisher but will not extend past the publisher's death unless
    // special arrangements are made.
    @Column(name = "date_made_public")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateMadePublic = new Date();

    @Enumerated(EnumType.STRING)
    @Column(length = FieldLength.FIELD_LENGTH_32)
    private FileStatus status;

    public FileType getInformationResourceFileType() {
        return informationResourceFileType;
    }

    public void setInformationResourceFileType(FileType informationResourceFileType) {
        this.informationResourceFileType = informationResourceFileType;
    }

    @XmlAttribute(name = "latestVersion")
    public Integer getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(Integer latestVersion) {
        this.latestVersion = latestVersion;
    }

    public FileStatus getStatus() {
        return status;
    }

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("(%d, %s) v#:%s: %s (%s versions)", getId(), status, getLatestVersion(), restriction, 1);
    }

    public Date getDateMadePublic() {
        return dateMadePublic;
    }

    public void setDateMadePublic(Date dateMadePublic) {
        this.dateMadePublic = dateMadePublic;
    }

    public FileAccessRestriction getRestriction() {
        return restriction;
    }

    public void setRestriction(FileAccessRestriction restriction) {
        this.restriction = restriction;
    }

    public boolean isPartOfComposite() {
        if (partOfComposite == null) {
            return false;
        }
        return partOfComposite;
    }

    public void setPartOfComposite(boolean partOfComposite) {
        this.partOfComposite = partOfComposite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InformationResourceFile generateInformationResourceFile() throws IllegalAccessException, InvocationTargetException {
        InformationResourceFile file = new InformationResourceFile();
        file.setId(getId());
        file.setPartOfComposite(isPartOfComposite());
        file.setSequenceNumber(getSequenceNumber());
        file.setInformationResourceFileType(getInformationResourceFileType());
        file.setLatestVersion(getLatestVersion());
        file.setRestriction(getRestriction());
        file.setFilename(getFilename());
        file.setStatus(getStatus());
        file.setDateMadePublic(getDateMadePublic());
        file.setDeleted(getDeleted());
        for (InformationResourceFileVersionProxy prox : getInformationResourceFileVersionProxies()) {
            InformationResourceFileVersion version = prox.generateInformationResourceFileVersion();
            file.getInformationResourceFileVersions().add(version);
            version.setInformationResourceFile(file);

        }
        return file;
    }

    public List<InformationResourceFileVersionProxy> getInformationResourceFileVersionProxies() {
        return informationResourceFileVersionProxies;
    }

    public void setInformationResourceFileVersionProxies(List<InformationResourceFileVersionProxy> informationResourceFileVersionProxies) {
        this.informationResourceFileVersionProxies = informationResourceFileVersionProxies;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
