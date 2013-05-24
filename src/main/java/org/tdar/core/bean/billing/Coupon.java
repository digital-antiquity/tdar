package org.tdar.core.bean.billing;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.tdar.core.bean.Persistable.Base;

@Entity
@Table(name = "pos_coupon")
public class Coupon extends Base {

    private static final long serialVersionUID = -8987513032996536732L;

    @Column(name = "number_of_mb")
    private Long numberOfMb = 0L;

    @Column(name = "number_of_files")
    private Long numberOfFiles = 0L;

    // @Column(name = "number_of_dollars")
    // private Float numberOfDollars = 0f;

    @Column(name = "one_time_use")
    private Boolean oneTimeUse = true;

    @Column(unique = true)
    private String code;

    @Column(name = "date_created")
    private Date dateCreated = new Date();

    @Column(name = "date_expires")
    private Date dateExpires;

    public Long getNumberOfMb() {
        return numberOfMb;
    }

    public void setNumberOfMb(Long numberOfMb) {
        this.numberOfMb = numberOfMb;
    }

    public Long getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(Long numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public Boolean getOneTimeUse() {
        return oneTimeUse;
    }

    public void setOneTimeUse(Boolean oneTimeUse) {
        this.oneTimeUse = oneTimeUse;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateExpires() {
        return dateExpires;
    }

    public void setDateExpires(Date dateExpires) {
        this.dateExpires = dateExpires;
    }

    @Override
    public String toString() {
        return String.format("coupon[f=%s s=%s c=%s]", numberOfFiles, numberOfMb, code);
    }
}
