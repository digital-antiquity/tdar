package org.tdar.core.bean;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;

@Entity()
@Table(name = "files")
public class TdarFile extends AbstractPersistable {

    private static final long serialVersionUID = 8203692812833995820L;

    @Column(length = FieldLength.FIELD_LENGTH_1024)
    private String filename;
    @Column(name = "display_name", length = FieldLength.FIELD_LENGTH_1024)
    private String displayName;

    @Column(name = "local_path", length = FieldLength.FIELD_LENGTH_2048)
    private String localPath;
    @Column(name = "file_size")
    private Long fileSize;
    @Column(length = 15)
    private String extension;
    @Enumerated(EnumType.STRING)

    @Column(length = FieldLength.FIELD_LENGTH_50, name = "status")
    private ImportFileStatus status;
    @Column(length = FieldLength.FIELD_LENGTH_100, name = "md5")
    private String md5;

    // private List<String> fileIssues;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private TdarFile parentFile;

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

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public TdarFile getParentFile() {
        return parentFile;
    }

    public void setParentFile(TdarFile parentFile) {
        this.parentFile = parentFile;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }
    // public List<String> getFileIssues() {
    // return fileIssues;
    // }
    // public void setFileIssues(List<String> fileIssues) {
    // this.fileIssues = fileIssues;
    // }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

}
