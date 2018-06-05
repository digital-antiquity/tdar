package org.tdar.struts.action.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.utils.json.JsonAccountFilter;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/dashboard")
@Component
@Scope("prototype")
public class FileReportAction extends AbstractAuthenticatableAction implements Preparable {


    private static final long serialVersionUID = 3098096649657599549L;

    @Autowired
    private transient BillingAccountService accountService;
    
    @Autowired
    private transient SerializationService serializationService;

    private List<UserNotification> currentNotifications;
    private Set<BillingAccount> accounts = new HashSet<>();
    private String accountJson;
    private String userJson;


    
    @Override
    @Action(value = "fileReports", results = { @Result(name = SUCCESS, location = "fileReports.ftl") })
    public String execute() {

        return SUCCESS;
    }

    @Override
    public void prepare() throws IOException {
        getAccounts().addAll(accountService.listAvailableAccountsForUser(getAuthenticatedUser(), Status.ACTIVE));
        setAccountJson(serializationService.convertToFilteredJson(getAccounts(), JsonAccountFilter.class));
//        users.addAll(acc)
        }

    public List<UserNotification> getCurrentNotifications() {
        return currentNotifications;
    }

    public void setCurrentNotifications(List<UserNotification> currentNotifications) {
        this.currentNotifications = currentNotifications;
    }

    public String getAccountJson() {
        return accountJson;
    }

    public void setAccountJson(String accountJson) {
        this.accountJson = accountJson;
    }

    public String getUserJson() {
        return userJson;
    }

    public void setUserJson(String userJson) {
        this.userJson = userJson;
    }

    public Set<BillingAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<BillingAccount> accounts) {
        this.accounts = accounts;
    }

}
