package org.tdar.core.bean.file;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

@Entity()
@Table(name = "files")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "file_type")
public abstract class AbstractFile extends AbstractPersistable {

    private static final long serialVersionUID = 8203692812833995820L;

    @Column(length = FieldLength.FIELD_LENGTH_1024, name = "internal_name")
    private String internalName;
    @Column(name = "display_name", length = FieldLength.FIELD_LENGTH_1024)
    private String filename;

    @Column(name = "local_path", length = FieldLength.FIELD_LENGTH_2048)
    private String localPath;

    @ManyToOne
    @JoinColumn(name = "account_id", updatable = true)
    private BillingAccount account;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("dateCreated ASC")
    @JoinColumn(nullable = false, updatable = false, name = "file_id")
    private List<FileComment> comments = new ArrayList<FileComment>();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private TdarDir parent;

    @Column(name = "date_created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;
    
    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private TdarUser uploader;

    // private ResourceType targetFileType;
    // private Resource resource;
    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String filename) {
        this.internalName = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String displayName) {
        this.filename = displayName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @XmlElement(name = "parentRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarDir getParent() {
        return parent;
    }

    public void setParent(TdarDir parentFile) {
        this.parent = parentFile;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @XmlElement(name = "uploaderRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getUploader() {
        return uploader;
    }

    public String getUploaderName() {
        if (uploader != null) {
            return uploader.getProperName();
        }
        return null;
    }

    public String getUploaderInitials() {
        if (uploader != null) {
            return uploader.getInitials();
        }
        return null;
    }

    public String getName() {
        return this.filename;
    }

    public void setUploader(TdarUser uploader) {
        this.uploader = uploader;
    }

    @XmlElement(name = "accountRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }

    public List<FileComment> getComments() {
        return comments;
    }

    public void setComments(List<FileComment> comments) {
        this.comments = comments;
    }

    
}
