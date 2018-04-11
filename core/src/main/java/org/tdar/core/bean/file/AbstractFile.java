package org.tdar.core.bean.file;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;

@Entity()
@Table(name = "files")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "file_type")
public abstract class AbstractFile extends AbstractPersistable {

    private static final long serialVersionUID = 8203692812833995820L;

    @Column(length = FieldLength.FIELD_LENGTH_1024)
    private String filename;
    @Column(name = "display_name", length = FieldLength.FIELD_LENGTH_1024)
    private String displayName;

    @Column(name = "local_path", length = FieldLength.FIELD_LENGTH_2048)
    private String localPath;

    @Column(name = "date_created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    // private List<String> fileIssues;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private TdarDir parentFile;

    // private ResourceType targetFileType;
    // private Resource resource;
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public TdarDir getParentFile() {
        return parentFile;
    }

    public void setParentFile(TdarDir parentFile) {
        this.parentFile = parentFile;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

}
