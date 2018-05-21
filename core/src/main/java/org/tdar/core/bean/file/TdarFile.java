package org.tdar.core.bean.file;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.ImportFileStatus;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Status;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

@Entity
@DiscriminatorValue(value = "FILE")
public class TdarFile extends AbstractFile {

    private static final long serialVersionUID = 8710509667556337547L;
    @Column(name = "file_size")
    private Long size;
    @Column(length = 15)
    private String extension;
    @Enumerated(EnumType.STRING)

    @Column(length = FieldLength.FIELD_LENGTH_50, name = "status")
    private ImportFileStatus status;
    @Column(length = FieldLength.FIELD_LENGTH_100, name = "md5")
    private String md5;

    @Column(name = "date_reviewed", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateReviewed;

    @Column(name = "requires_ocr", nullable = true)
    private Boolean requiresOcr;

    @Column(name = "curate", nullable = true)
    private Boolean curated = true;

    @Column(length = FieldLength.FIELD_LENGTH_100, name = "note")
    private String note;

    @Column(name = "date_initial_reviewed", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateInitialReviewed;

    @Column(name = "date_external_reviewed", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateExternalReviewed;

    @ManyToOne
    @JoinColumn(name = "initial_reviewer_id")
    private TdarUser initialReviewedBy;

    @ManyToOne
    @JoinColumn(name = "external_reviewer_id")
    private TdarUser externalReviewedBy;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private TdarUser reviewedBy;

    @Column(name = "date_curated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCurated;

    @ManyToOne
    @JoinColumn(name = "curator_id")
    private TdarUser curatedBy;

    @ManyToOne
    @JoinColumn(name = "resource_id")
    private InformationResource resource;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = true, name = "part_of_id")
    private List<TdarFile> parts = new ArrayList<>();

    public TdarFile() {
    }

    public TdarFile(String string, TdarUser tdarUser, BillingAccount act) {
        this.setFilename(string);
        this.setUploader(tdarUser);
        this.setAccount(act);
        this.setDateCreated(new Date());
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long fileSize) {
        this.size = fileSize;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public ImportFileStatus getStatus() {
        return status;
    }

    public void setStatus(ImportFileStatus status) {
        this.status = status;
    }

    public Date getDateReviewed() {
        return dateReviewed;
    }

    public void setDateReviewed(Date dateReviewed) {
        this.dateReviewed = dateReviewed;
    }

    @XmlElement(name = "resourceRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public InformationResource getResource() {
        return resource;
    }

    public String getResourceUrl() {
        if (resource != null) {
            return "/" + resource.getAbsoluteUrl();
        }
        return null;
    }

    public Long getResourceId() {
        if (resource != null) {
            return resource.getId();
        }
        return null;
    }

    public void setResource(InformationResource resource) {
        this.resource = resource;
    }

    @XmlElement(name = "curatorRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getCuratedBy() {
        return curatedBy;
    }

    public void setCuratedBy(TdarUser curatedBy) {
        this.curatedBy = curatedBy;
    }

    public Date getDateCurated() {
        return dateCurated;
    }

    public void setDateCurated(Date dateCurated) {
        this.dateCurated = dateCurated;
    }

    @XmlElement(name = "revewerRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(TdarUser reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewedByName() {
        if (reviewedBy != null) {
            return reviewedBy.getProperName();
        }
        return null;
    }

    public String getReviewedByInitials() {
        if (reviewedBy != null) {
            return reviewedBy.getInitials();
        }
        return null;
    }

    public String getInitialReviewedByName() {
        if (initialReviewedBy != null) {
            return initialReviewedBy.getProperName();
        }
        return null;
    }

    public String getExternalReviewedByName() {
        if (externalReviewedBy != null) {
            return externalReviewedBy.getProperName();
        }
        return null;
    }

    public String getInitialReviewedByInitials() {
        if (initialReviewedBy != null) {
            return initialReviewedBy.getInitials();
        }
        return null;
    }

    public String getExternalReviewedByInitials() {
        if (externalReviewedBy != null) {
            return externalReviewedBy.getInitials();
        }
        return null;
    }

    public String getCuratedByName() {
        if (curatedBy != null) {
            return curatedBy.getProperName();
        }
        return null;
    }

    public String getCuratedByInitials() {
        if (curatedBy != null) {
            return curatedBy.getInitials();
        }
        return null;
    }

    public Boolean getRequiresOcr() {
        return requiresOcr;
    }

    public void setRequiresOcr(Boolean requiresOcr) {
        this.requiresOcr = requiresOcr;
    }

    public String getNote() {
        return note;
    }

    public String getInitialNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getCurated() {
        return curated;
    }

    public void setCurated(Boolean curated) {
        this.curated = curated;
    }

    @XmlElement(name = "externalReviewedByRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getExternalReviewedBy() {
        return externalReviewedBy;
    }

    public void setExternalReviewedBy(TdarUser externalReviewedBy) {
        this.externalReviewedBy = externalReviewedBy;
    }

    @XmlElement(name = "initialReviewedByRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getInitialReviewedBy() {
        return initialReviewedBy;
    }

    public void setInitialReviewedBy(TdarUser initialReviewedBy) {
        this.initialReviewedBy = initialReviewedBy;
    }

    public Date getDateInitialReviewed() {
        return dateInitialReviewed;
    }

    public void setDateInitialReviewed(Date dateInitialReviewed) {
        this.dateInitialReviewed = dateInitialReviewed;
    }

    public Date getDateExternalReviewed() {
        return dateExternalReviewed;
    }

    public Date getDateResourceCreated() {
        if (getResource() == null) {
            return null;
        }
        return getResource().getDateCreated();
    }

    public String getResourceCreatedByName() {
        if (getResource() == null) {
            return null;
        }
        return getResource().getUploader().getProperName();
    }

    public Status getResourceStatus() {
        if (getResource() == null) {
            return null;
        }
        return getResource().getStatus();
    }

    public String getResourceCreatedByInitials() {
        if (getResource() == null) {
            return null;
        }
        return getResource().getUploader().getInitials();
    }

    public void setDateExternalReviewed(Date dateExternalReviewed) {
        this.dateExternalReviewed = dateExternalReviewed;
    }

    public List<TdarFile> getParts() {
        return parts;
    }

    public void setParts(List<TdarFile> parts) {
        this.parts = parts;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getName(), getId());
    }
}
