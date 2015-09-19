package org.tdar.struts.action.billing;

import java.util.Date;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.PersistableLoadingAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
public class CouponCreationAction extends AuthenticationAware.Base implements Preparable, PersistableLoadingAction<BillingAccount> {

    private static final long serialVersionUID = -4931747979827504369L;
    public static final String VIEW_ID = "${id}";
    @Autowired
    private BillingAccountService accountService;
    @Autowired
    private AuthorizationService authorizationService;
    
    private Long id;
    private Integer quantity = 1;

    private Long numberOfFiles = 0L;
    private Long numberOfMb = 0L;
    private Date expires = new DateTime().plusYears(1).toDate();
    private BillingAccount account;

    @Override
    public void prepare() throws TdarActionException {
        prepareAndLoad(this, RequestType.EDIT);
    }

    @Action(value = "create-code",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, location = VIEW_ID, type = "redirect"),
                    @Result(name = INPUT, location = "error.ftl")
            })
    @PostOnly
    public String createCouponCode() throws TdarActionException {

        for (int i = 0; i < quantity; i++) {
            accountService.generateCouponCode(getAccount(), getNumberOfFiles(), getNumberOfMb(), getExpires());
        }
        accountService.updateQuota(getAccount());
        return SUCCESS;
    }

    @Override
    public void validate() {
        getLogger().debug("validating coupon:: files:{}  mb:{}  expires:{}", getNumberOfFiles(), getNumberOfMb(),
                getExpires());
        long mb = getNumberOfMb() != null ? getNumberOfMb() : 0;
        long files = getNumberOfFiles() != null ? getNumberOfFiles() : 0;

        // account required
        if (PersistableUtils.isTransient(getAccount())) {
            addActionError(getText("accountService.account_is_null"));
        }

        // either space or files required
        if (mb <= 0 && files <= 0) {
            addActionError(getText("accountService.specify_either_space_or_files"));
        }

        // coupon must not exceed remaining files or remaining space
        if (getAccount().getAvailableNumberOfFiles() < files) {
            addActionError(getText("accountService.not_enough_space_or_files"));
        } else if (getAccount().getAvailableSpaceInMb() < mb) {
            addActionError(getText("accountService.not_enough_space_or_files"));
        }

        // but not both
        if (mb > 0 && files > 0) {
            addActionError(getText("accountService.specify_either_space_or_files"));
        }

        // date required
        // date must be in the future (Think, McFly, think!)
        if (getExpires() == null) {
            addActionError(getText("invoiceService.coupon_expiration_invalid"));
        } else {
            DateTime d = new DateTime(getExpires());
            if (d.isBefore(null)) {
                addActionError(getText("invoiceService.coupon_expiration_invalid"));
            }
        }

        if (hasActionErrors()) {
            getLogger().info("coupon is not valid: errors:{}", getActionErrors());

        } else {
            getLogger().info("coupon appears to be valid");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_BILLING_INFO;
    }

    @Override
    public void setPersistable(BillingAccount persistable) {
        this.account = persistable;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(Long numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public Long getNumberOfMb() {
        return numberOfMb;
    }

    public void setNumberOfMb(Long numberOfMb) {
        this.numberOfMb = numberOfMb;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }

    @Override
    public boolean authorize() throws TdarActionException {
        return authorizationService.canEditAccount(getAccount(), getAuthenticatedUser());
    }

    @Override
    public Persistable getPersistable() {
        return account;
    }

    @Override
    public Class<BillingAccount> getPersistableClass() {
        return BillingAccount.class;
    }

}
