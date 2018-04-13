package org.tdar.core.bean.file;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.ImportFileStatus;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;

@Entity
@DiscriminatorValue(value = "FILE")
public class TdarFile extends AbstractFile {


    private static final long serialVersionUID = 8710509667556337547L;
    @Column(name = "file_size")
    private Long fileSize;
    @Column(length = 15)
    private String extension;
    @Enumerated(EnumType.STRING)

    @Column(length = FieldLength.FIELD_LENGTH_50, name = "status")
    private ImportFileStatus status;
    @Column(length = FieldLength.FIELD_LENGTH_100, name = "md5")
    private String md5;
    
    @ManyToOne
    @JoinColumn(name = "account_id")
    private BillingAccount account;
    

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

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }

    public ImportFileStatus getStatus() {
        return status;
    }

    public void setStatus(ImportFileStatus status) {
        this.status = status;
    }

}
