package org.tdar.core.service.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.tdar.core.bean.resource.Resource;

public class ActionMessageErrorListener implements Serializable, ActionMessageErrorSupport {

    private static final long serialVersionUID = 4957723037281137599L;

    private List<String> actionErrors = new ArrayList<>();
    private List<String> actionMessages = new ArrayList<>();
    private List<String> stackTraces = new ArrayList<>();
    private List<String> errorCodes = new ArrayList<>();
    private List<String> moreInfoUrlKeys = new ArrayList<>();
    private boolean shouldDetach = false;
    private Resource resource;

    @Override
    public void addActionError(String message) {
        getActionErrors().add(message);
    }

    @Override
    public void addActionMessage(String message) {
        getActionMessages().add(message);
    }

    @Override
    public List<String> getStackTraces() {
        return stackTraces;
    }

    public void setStackTraces(List<String> stackTraces) {
        this.stackTraces = stackTraces;
    }

    public List<String> getActionMessages() {
        return actionMessages;
    }

    public void setActionMessages(List<String> actionMessages) {
        this.actionMessages = actionMessages;
    }

    public List<String> getActionErrors() {
        return actionErrors;
    }

    public void setActionErrors(List<String> actionErrors) {
        this.actionErrors = actionErrors;
    }

    public boolean hasActionErrors() {
        return CollectionUtils.isNotEmpty(actionErrors);
    }

    @Override
    public String toString() {
        return String.format("Action Errors: %s \r\n Action Messages: %s \r\n StackTraces: %s\r\n", actionErrors, actionMessages, stackTraces);
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public List<String> getMoreInfoUrlKeys() {
        return moreInfoUrlKeys;
    }

    public void setMoreInfoUrlKeys(List<String> moreInfoUrlKeys) {
        this.moreInfoUrlKeys = moreInfoUrlKeys;
    }

    @Override
    public void setMoreInfoUrlKey(String url) {
        getMoreInfoUrlKeys().add(url);
    }

    @Override
    public boolean isShouldDetach() {
        return shouldDetach;
    }

    @Override
    public void setShouldDetach(boolean shouldDetach) {
        this.shouldDetach = shouldDetach;
    }

}
