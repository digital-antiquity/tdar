package org.tdar.core.bean.file;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.tdar.core.bean.FieldLength;
import org.tdar.filestore.VersionType;

@Entity
@DiscriminatorValue(value = "FILE_VERSION")
public class TdarFileVersion extends AbstractFile {

    private static final long serialVersionUID = 8132978641749368123L;

    @Column(name = "version_of_id", updatable = false, insertable = false)
    private Long versionOfId;

    @Enumerated(EnumType.STRING)
    @Column(name = "version_type")
    private VersionType fileVersionType;

    @Column(length = FieldLength.FIELD_LENGTH_100, name = "md5")
    private String md5;
    @Column(name = "file_size")
    private Long size;
    @Column(length = 15)
    private String extension;

    @Column
    private Integer length;
    @Column
    private Integer height;
    @Column
    private Integer width;

    public String getMd5() {
        return md5;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Long getVersionOfId() {
        return versionOfId;
    }

    public VersionType getFileVersionType() {
        return fileVersionType;
    }

    public void setFileVersionType(VersionType fileVersionType) {
        this.fileVersionType = fileVersionType;
    }

}
