package org.tdar.core.bean.billing;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.entity.Person;

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
    @Column(unique = true)
    private String code;

    @Column(name = "date_created")
    private Date dateCreated = new Date();

    @Column(name = "date_expires")
    private Date dateExpires;

    @Column(name = "date_redeemed")
    private Date dateRedeemed;

    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = true, name = "user_id")
    private Person user;

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

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public Date getDateRedeemed() {
        return dateRedeemed;
    }

    public void setDateRedeemed(Date dateRedeemed) {
        this.dateRedeemed = dateRedeemed;
    }
}
