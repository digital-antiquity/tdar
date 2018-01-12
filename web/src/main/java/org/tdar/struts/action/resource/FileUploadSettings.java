package org.tdar.struts.action.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.FileProxy;

public class FileUploadSettings implements Serializable {

    private static final long serialVersionUID = -2111318200411438432L;
    private Long ticketId;
    private Long resourceId;
    private Long userId;
    private boolean multipleUpload;
    private boolean ableToUpload;
    private boolean dataTableEnabled;
    private List<String> validFormats = new ArrayList<>();
    private boolean sideCarOnly;
    private String url = "/upload/upload";
    private List<FileProxy> files = new ArrayList<>();
    private Integer maxNumberOfFiles;
    private List<RequiredOptionalPairs> requiredOptionalPairs = new ArrayList<>();

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isMultipleUpload() {
        return multipleUpload;
    }

    public void setMultipleUpload(boolean multipleUpload) {
        this.multipleUpload = multipleUpload;
    }

    public boolean isAbleToUpload() {
        return ableToUpload;
    }

    public void setAbleToUpload(boolean ableToUpload) {
        this.ableToUpload = ableToUpload;
    }

    public boolean isDataTableEnabled() {
        return dataTableEnabled;
    }

    public void setDataTableEnabled(boolean dataTableEnabled) {
        this.dataTableEnabled = dataTableEnabled;
    }

    public List<String> getValidFormats() {
        return validFormats;
    }

    public void setValidFormats(List<String> validFormats) {
        this.validFormats = validFormats;
    }

    public boolean isSideCarOnly() {
        return sideCarOnly;
    }

    public void setSideCarOnly(boolean sideCarOnly) {
        this.sideCarOnly = sideCarOnly;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<FileProxy> getFiles() {
        return files;
    }

    public void setFiles(List<FileProxy> files) {
        this.files = files;
    }

    public Integer getMaxNumberOfFiles() {
        return maxNumberOfFiles;
    }

    public void setMaxNumberOfFiles(Integer maxNumberOfFiles) {
        this.maxNumberOfFiles = maxNumberOfFiles;
    }

    public List<RequiredOptionalPairs> getRequiredOptionalPairs() {
        return requiredOptionalPairs;
    }

    public void setRequiredOptionalPairs(List<RequiredOptionalPairs> requiredOptionalPairs) {
        this.requiredOptionalPairs = requiredOptionalPairs;
    }

}
