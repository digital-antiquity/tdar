package org.tdar.utils;

import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivityModel;

public class AccountEvaluationHelper {

    private Long id;
    private BillingActivityModel model;
    private Long availableSpaceInBytes;
    private Long availableNumberOfFiles;
    private Long filesUsed;
    private Long spaceUsedInBytes;
    private Account account;
    
    public AccountEvaluationHelper(Account account, BillingActivityModel model) {
        this.id = account.getId();
        this.availableSpaceInBytes = account.getAvailableSpaceInBytes();
        this.availableNumberOfFiles = account.getAvailableNumberOfFiles();
        this.spaceUsedInBytes = account.getSpaceUsedInBytes();
        this.filesUsed = account.getFilesUsed();
        this.account = account;
    }

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public BillingActivityModel getModel() {
        return model;
    }


    public void setModel(BillingActivityModel model) {
        this.model = model;
    }


    public Long getAvailableSpaceInBytes() {
        return availableSpaceInBytes;
    }


    public void setAvailableSpaceInBytes(Long availableSpaceInBytes) {
        this.availableSpaceInBytes = availableSpaceInBytes;
    }


    public Long getAvailableNumberOfFiles() {
        return availableNumberOfFiles;
    }


    public void setAvailableNumberOfFiles(Long availableNumberOfFiles) {
        this.availableNumberOfFiles = availableNumberOfFiles;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void updateAccount() {
        account.setSpaceUsedInBytes(spaceUsedInBytes);
        account.setFilesUsed(filesUsed);
        
        
    }
}
