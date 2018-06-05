package org.tdar.struts.action.api.files.reports;

import java.util.Date;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.dao.RecentFileSummary;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts.action.api.AbstractJsonApiAction;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file/reports")
public class RecentFilesReport extends AbstractJsonApiAction {

    private static final long serialVersionUID = 8104842508893337456L;
    private TdarDir parent;
    private Long parentId;
    private Long accountId;
    private BillingAccount account;
    private Date dateSearch = DateTime.now().minusDays(7).toDate();
    private TdarUser user;
    private Long userId;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    private RecentFileSummary report;

    @Override
    public void prepare() throws Exception {
        super.prepare();
        if (parentId != null) {
            parent = getGenericService().find(TdarDir.class, parentId);
        }
        if (userId != null) {
            user = getGenericService().find(TdarUser.class, userId);
        }
        if (accountId != null) {
            account = getGenericService().find(BillingAccount.class, accountId);
        }
    }

    @Override
    public void validate() {
        super.validate();

        if (account == null || getAuthorizationService().cannotChargeAccount(getAuthenticatedUser(), account)) {
            addActionError("not.allowed");
        }
    }

    @Override
    @Action("recentFiles")
    public String execute() throws Exception {
        report = personalFilestoreService.recentByAccount(account, dateSearch, parent, user);
        setResultObject(report);
        return super.execute();
    }

    public TdarDir getParent() {
        return parent;
    }

    public void setParent(TdarDir parent) {
        this.parent = parent;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }

    public RecentFileSummary getReport() {
        return report;
    }

    public void setReport(RecentFileSummary report) {
        this.report = report;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TdarUser getUser() {
        return user;
    }

    public void setUser(TdarUser user) {
        this.user = user;
    }

    public Date getDateSearch() {
        return dateSearch;
    }

    public void setDateSearch(Date dateSearch) {
        this.dateSearch = dateSearch;
    }
}
