package org.tdar.struts.action.dashboard;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.UserNotificationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id$
 * 
 * Manages requests to create/delete/edit a Project and its associated metadata
 * (including Datasets, etc).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Namespace("/dashboard")
@Component
@Scope("prototype")
public class BillingAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -2489487996000481630L;
    private Set<BillingAccount> accounts = new HashSet<BillingAccount>();
    private Set<BillingAccount> overdrawnAccounts = new HashSet<BillingAccount>();
    private final transient BillingAccountService accountService;
    private final transient UserNotificationService userNotificationService;

    private List<UserNotification> currentNotifications;

    @Autowired
    public BillingAction(BillingAccountService accountService, UserNotificationService userNotificationService) {
        this.accountService = accountService;
        this.userNotificationService = userNotificationService;
    }

    @Override
    public void validate() {
        if (PersistableUtils.isNullOrTransient(getAuthenticatedUser())) {
            addActionError(getText("dashboardController.user_must_login"));
        }
        super.validate();
    }

    @Override
    @Action(value = "billing", results = { @Result(name = SUCCESS, location = "billing.ftl") })
    public String execute() throws SolrServerException, IOException {

        return SUCCESS;
    }

    public void prepare() {
        setCurrentNotifications(userNotificationService.getCurrentNotifications(getAuthenticatedUser()));
        getAccounts().addAll(accountService.listAvailableAccountsForUser(getAuthenticatedUser(), Status.ACTIVE,
                Status.FLAGGED_ACCOUNT_BALANCE));
        for (BillingAccount account : getAccounts()) {
            if (account.getStatus() == Status.FLAGGED_ACCOUNT_BALANCE) {
                overdrawnAccounts.add(account);
            }
        }
    }

    public Set<BillingAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<BillingAccount> accounts) {
        this.accounts = accounts;
    }

    public Set<BillingAccount> getOverdrawnAccounts() {
        return overdrawnAccounts;
    }

    public void setOverdrawnAccounts(Set<BillingAccount> overdrawnAccounts) {
        this.overdrawnAccounts = overdrawnAccounts;
    }

    public List<UserNotification> getCurrentNotifications() {
        return currentNotifications;
    }

    public void setCurrentNotifications(List<UserNotification> currentNotifications) {
        this.currentNotifications = currentNotifications;
    }

    @Override
    public boolean isRightSidebar() {
        return true;
    }
}
