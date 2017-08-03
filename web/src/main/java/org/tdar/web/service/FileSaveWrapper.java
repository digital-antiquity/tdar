package org.tdar.web.service;

import java.io.File;
import java.util.List;

import org.tdar.core.bean.FileProxy;

public class FileSaveWrapper {

    private FileProxy proxy;
    private boolean fileProxyChanges;
    private List<String> uploadedFilesFileName;
    private List<FileProxy> fileProxies;
    private Long ticketId;
    private String fileTextInput;
    private boolean textInput;
    private boolean multipleFileUploadEnabled;
    private boolean bulkUpload;
    private List<File> uploadedFiles;

    public FileProxy getProxy() {
        return proxy;
    }

    public boolean isFileProxyChanges() {
        return fileProxyChanges;
    }

    public void setUploadedFilesFileName(List<String> uploadedFilesFileName) {
        this.uploadedFilesFileName = uploadedFilesFileName;
    }

    public void setFileProxies(List<FileProxy> fileProxies) {
        this.fileProxies = fileProxies;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public void setFileTextInput(String fileTextInput) {
        this.fileTextInput = fileTextInput;
    }

    public void setTextInput(boolean textInput) {
        this.textInput = textInput;
    }

    public void setMultipleFileUploadEnabled(boolean multipleFileUploadEnabled) {
        this.multipleFileUploadEnabled = multipleFileUploadEnabled;
    }

    public void setBulkUpload(boolean bulkUpload) {
        this.bulkUpload = bulkUpload;
    }

    public boolean isBulkUpload() {
        return bulkUpload;
    }

    public boolean isMultipleFileUploadEnabled() {
        return multipleFileUploadEnabled;
    }

    public boolean isTextInput() {
        return textInput;
    }

    public String getFileTextInput() {
        return fileTextInput;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public List<FileProxy> getFileProxies() {
        return fileProxies;
    }

    public List<String> getUploadedFilesFileName() {
        return uploadedFilesFileName;
    }

    public void setFileProxyChanges(boolean b) {
        this.fileProxyChanges = b;
        // TODO Auto-generated method stub

    }

    public void setProxy(FileProxy proxy) {
        this.proxy = proxy;
    }

    public List<File> getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(List<File> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

}
