package org.tdar.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.resource.Resource;

public class AccountEvaluationHelper {

    private Long id;
    private BillingActivityModel model;
    private Long allocatedSpaceInBytes;
    private Long allocatedNumberOfFiles;
    private Long filesUsed;
    private Long spaceUsedInBytes;
    private Account account;
    private List<Resource> newItems = new ArrayList<Resource>();
    private List<Resource> existingItems = new ArrayList<Resource>();
    private Set<Resource> flagged = new HashSet<Resource>();
    private Set<Resource> unflagged = new HashSet<Resource>();


    public AccountEvaluationHelper(Account account, BillingActivityModel model) {
        this.id = account.getId();
        this.model = model;
        this.allocatedSpaceInBytes = account.getTotalSpaceInBytes();
        this.allocatedNumberOfFiles = account.getTotalNumberOfFiles();
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
        return allocatedSpaceInBytes - spaceUsedInBytes;
    }

    public Long getAvailableNumberOfFiles() {
        return allocatedNumberOfFiles - filesUsed;
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

    public List<Resource> getExistingItems() {
        return existingItems;
    }

    public void setExistingItems(List<Resource> existingItems) {
        this.existingItems = existingItems;
    }

    public List<Resource> getNewItems() {
        return newItems;
    }

    public void setNewItems(List<Resource> newItems) {
        this.newItems = newItems;
    }

    public Set<Resource> getUnflagged() {
        return unflagged;
    }

    public void setUnflagged(Set<Resource> unflagged) {
        this.unflagged = unflagged;
    }

    public Set<Resource> getFlagged() {
        return flagged;
    }

    public void setFlagged(Set<Resource> flagged) {
        this.flagged = flagged;
    }
}
