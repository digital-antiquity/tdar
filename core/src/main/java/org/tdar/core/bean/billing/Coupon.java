package org.tdar.core.bean.billing;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdar.core.bean.Persistable.Base;
import org.tdar.core.bean.entity.TdarUser;

/**
 * A coupon or 'credit' for space or files in tDAR.
 * 
 * @author abrin
 * 
 */
@Entity
@Table(name = "pos_coupon")
public class Coupon extends Base {

    private static final long serialVersionUID = -8987513032996536732L;

    @Column(name = "number_of_mb")
    private Long numberOfMb = 0L;

    @Column(name = "number_of_files")
    private Long numberOfFiles = 0L;

    @Column(unique = true)
    private String code;

    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated = new Date();

    @Column(name = "date_expires")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateExpires;

    @Column(name = "date_redeemed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateRedeemed;
    
    @ElementCollection
    @CollectionTable(name = "coupon_resource", joinColumns = @JoinColumn(name = "coupon_id") )
    @Column(name = "resource_id")
    private Set<Long> resourceIds = new HashSet<>();


    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(nullable = true, name = "user_id")
    private TdarUser user;

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

    public TdarUser getUser() {
        return user;
    }

    public void setUser(TdarUser user) {
        this.user = user;
    }

    public Date getDateRedeemed() {
        return dateRedeemed;
    }

    public void setDateRedeemed(Date dateRedeemed) {
        this.dateRedeemed = dateRedeemed;
    }

	public Set<Long> getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(Set<Long> resourceIds) {
		this.resourceIds = resourceIds;
	}
}
