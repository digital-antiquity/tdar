package org.tdar.core.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

/**
 * Used to move error data between different layers in tDAR, roughly follows the ValidationAware paradigm with a few extensions, could implement the interface
 * in the future.
 * 
 * @author abrin
 *
 */
public class ErrorTransferObject implements Serializable {

    private static final long serialVersionUID = 3170420989984554614L;

    private List<String> actionErrors = new ArrayList<>();
    private List<String> actionMessages = new ArrayList<>();
    private List<String> stackTraces = new ArrayList<>();
    private Map<String, List<String>> fieldErrors = new HashMap<>();
    private String moreInfoUrlKey = new String();

    public List<String> getActionMessages() {
        return actionMessages;
    }

    public void setActionMessages(List<String> actionMessages) {
        this.actionMessages = actionMessages;
    }

    public List<String> getStackTraces() {
        return stackTraces;
    }

    public void setStackTraces(List<String> stackTraces) {
        this.stackTraces = stackTraces;
    }

    public String getMoreInfoUrlKey() {
        return moreInfoUrlKey;
    }

    public void setMoreInfoUrlKey(String moreInfoUrl) {
        this.moreInfoUrlKey = moreInfoUrl;
    }

    public List<String> getActionErrors() {
        return actionErrors;
    }

    public void setActionErrors(List<String> actionErrors) {
        this.actionErrors = actionErrors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, List<String>> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public void addFieldError(String key, String message) {
        List<String> vals = fieldErrors.get(key);
        if (vals == null) {
            vals = new ArrayList<>();
        }
        vals.add(message);
        fieldErrors.put(key, vals);

    }

    public boolean isNotEmpty() {
        return (CollectionUtils.isNotEmpty(actionErrors) || MapUtils.isNotEmpty(fieldErrors));
    }

    @Override
    public String toString() {
        return String.format("actionErrors: %s ; fieldErrors: %s", getActionErrors(), getFieldErrors());
    }
}
