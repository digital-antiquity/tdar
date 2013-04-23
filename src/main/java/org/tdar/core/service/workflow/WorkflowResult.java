package org.tdar.core.service.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.filestore.WorkflowContext;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.ExceptionWrapper;

public class WorkflowResult implements Serializable {

    private static final long serialVersionUID = 1717047792001171581L;

    private Boolean fatalErrors = Boolean.FALSE;
    private List<FileProxy> fileProxies = new ArrayList<>();
    private List<ExceptionWrapper> exceptions = new ArrayList<>();

    @Transient
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public WorkflowResult(List<FileProxy> fileProxiesToProcess) {
        if (CollectionUtils.isNotEmpty(fileProxiesToProcess)) {
            for (FileProxy proxy : fileProxiesToProcess) {
                InformationResourceFile file = proxy.getInformationResourceFile();
                if (file != null) {
                    WorkflowContext context = file.getWorkflowContext();
                    if (context != null) {
                        if (context.isErrorFatal()) {
                            setFatalErrors(Boolean.TRUE);
                        }
                        getExceptions().addAll(context.getExceptions());
                        logger.warn("EXCEPTIONS: {}", context.getExceptions());
                    }
                }
            }
        }
    }

    public void addActionErrorsAndMessages(ActionMessageErrorSupport actionSupport) {
        for (ExceptionWrapper exception : getExceptions()) {
            if (getFatalErrors()) {
                actionSupport.addActionError(exception.getMessage());
            } else {
                actionSupport.addActionMessage(exception.getMessage());
            }
            actionSupport.getStackTraces().add(exception.getStackTrace());
        }

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
