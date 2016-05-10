package org.tdar.core.bean.billing;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.utils.MathUtils;

/**
 * Keeps track of the Account Usage History by period of time. This snapshot can be used in billing processes to track changes.
 * 
 * @author abrin
 *
 */
@Entity
@Table(name = "pos_account_usage_history")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.billing.AccountHistory")
public class AccountUsageHistory extends AbstractPersistable {

    private static final long serialVersionUID = 1276926972016955137L;

    @Column(name = "files_used")
    private Long filesUsed = 0L;

    @Column(name = "space_used")
    private Long spaceUsedInBytes = 0L;

    @Column(name = "resources_used")
    private Long resourcesUsed = 0L;

    @NotNull
    @Column(name = "date_created")
    @Temporal(TemporalType.DATE)
    private Date date;

    public AccountUsageHistory() {

    }

    public AccountUsageHistory(BillingAccount account) {
        setFilesUsed(account.getFilesUsed());
        setResourcesUsed(account.getResourcesUsed());
        setSpaceUsedInBytes(account.getSpaceUsedInBytes());
        setDate(new Date());
    }

    public Long getFilesUsed() {
        return filesUsed;
    }

    public void setFilesUsed(Long filesUsed) {
        this.filesUsed = filesUsed;
    }

    public Long getSpaceUsedInBytes() {
        return spaceUsedInBytes;
    }

    public void setSpaceUsedInBytes(Long spaceUsedInBytes) {
        this.spaceUsedInBytes = spaceUsedInBytes;
    }

    public Long getResourcesUsed() {
        return resourcesUsed;
    }

    public void setResourcesUsed(Long resourcesUsed) {
        this.resourcesUsed = resourcesUsed;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getSpaceUsedInMb() {
        return MathUtils.divideByRoundUp(spaceUsedInBytes, MathUtils.ONE_MB);
    }
}
