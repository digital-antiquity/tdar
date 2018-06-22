package org.tdar.core.service.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.exception.ExceptionWrapper;
import org.tdar.fileprocessing.workflows.WorkflowContext;

public class WorkflowResult implements Serializable {

    private static final long serialVersionUID = 1717047792001171581L;

    private Boolean fatalErrors = Boolean.FALSE;
    private List<FileProxy> fileProxies = new ArrayList<>();
    private List<ExceptionWrapper> exceptions = new ArrayList<>();

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private boolean success = true; // deliberately assume the happy case

    public WorkflowResult(List<FileProxy> fileProxiesToProcess) {
        if (CollectionUtils.isNotEmpty(fileProxiesToProcess)) {
            for (FileProxy proxy : fileProxiesToProcess) {
                InformationResourceFile file = proxy.getInformationResourceFile();
                if (file != null) {
                    copyContextResults(file.getWorkflowContext());
                }
            }
            if (CollectionUtils.isNotEmpty(getExceptions())) {
                logger.warn("EXCEPTIONS: {}", getExceptions());
            }
        }
    }

    public WorkflowResult(WorkflowContext context) {
        copyContextResults(context);
    }

    private void copyContextResults(WorkflowContext context) {
        if (context != null) {
            success = (!success) ? false : context.isProcessedSuccessfully();
            if (context.isErrorFatal()) {
                setFatalErrors(Boolean.TRUE);
            }
            getExceptions().addAll(context.getExceptions());
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public ErrorTransferObject getActionErrorsAndMessages() {
        ErrorTransferObject eto = new ErrorTransferObject();
        for (ExceptionWrapper exception : getExceptions()) {
            if (exception.isFatal() || fatalErrors) {
                eto.getActionErrors().add(exception.getMessage());
                logger.error("error processing file [code:{}]: {} ", exception.getErrorCode(), exception.getStackTrace());
            } else {
                eto.getActionMessages().add(exception.getMessage());
                logger.warn("issue processing file [code:{}]: {} ", exception.getErrorCode(), exception.getStackTrace());
            }
            eto.getStackTraces().add(exception.getErrorCode());
            if (StringUtils.isNotBlank(exception.getMoreInfoUrlKey())) {
                eto.setMoreInfoUrlKey(exception.getMoreInfoUrlKey());
            }
        }
        return eto;
    }

    public Boolean getFatalErrors() {
        return fatalErrors;
    }

    public void setFatalErrors(Boolean fatalErrors) {
        this.fatalErrors = fatalErrors;
    }

    public List<FileProxy> getFileProxies() {
        return fileProxies;
    }

    public void setFileProxies(List<FileProxy> fileProxies) {
        this.fileProxies = fileProxies;
    }

    public List<ExceptionWrapper> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<ExceptionWrapper> exceptions) {
        this.exceptions = exceptions;
    }

}
