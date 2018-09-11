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
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.fileprocessing.workflows.RequiredOptionalPairs;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.utils.json.JsonAccountFilter;

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
public class FileListAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -224826703370233994L;

    @Autowired
    private transient BillingAccountService accountService;
    
    @Autowired
    private transient SerializationService serializationService;

    @Autowired
    private transient FileAnalyzer analyzer;

    private List<UserNotification> currentNotifications;
    private List<BillingAccount> accounts = new ArrayList<>();

    private String accountJson;

    private Set<String> extensions;

    private String validFormats;
    
    @Override
    @Action(value = "files", results = { @Result(name = SUCCESS, location = "files.ftl") })
    public String execute() {

        return SUCCESS;
    }

    @Override
    public void prepare() throws IOException {
        getAccounts().addAll(accountService.listAvailableAccountsForUser(getAuthenticatedUser(), Status.ACTIVE));
        setAccountJson(serializationService.convertToFilteredJson(accounts, JsonAccountFilter.class));
        Set<RequiredOptionalPairs> extensionsForType = analyzer.getExtensionsForType(ResourceType.activeValues().toArray(new ResourceType[0]));
        Set<String> exts = new HashSet<>();
        for (RequiredOptionalPairs pair : extensionsForType) {
            exts.addAll(pair.getOptional());
            exts.addAll(pair.getRequired());
        }
        List<String> exts_ = new ArrayList<>(exts);
        for (int i=0; i < exts_.size(); i++) {
            exts_.set(i, "." + exts_.get(i));
        }

        setExtensions(new HashSet<>(exts_));
        setValidFormats(serializationService.convertToJson(extensions));
        }

    public List<UserNotification> getCurrentNotifications() {
        return currentNotifications;
    }

    public void setCurrentNotifications(List<UserNotification> currentNotifications) {
        this.currentNotifications = currentNotifications;
    }

    public List<BillingAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<BillingAccount> accounts) {
        this.accounts = accounts;
    }

    public String getAccountJson() {
        return accountJson;
    }

    public void setAccountJson(String accountJson) {
        this.accountJson = accountJson;
    }

    public Set<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(Set<String> extensions) {
        this.extensions = extensions;
    }

    public String getValidFormats() {
        return validFormats;
    }

    public void setValidFormats(String validFormats) {
        this.validFormats = validFormats;
    }

}
