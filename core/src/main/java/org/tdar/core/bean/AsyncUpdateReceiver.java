package org.tdar.core.bean;

import java.util.List;

import org.tdar.utils.Pair;

/**
 * This interface governs the interactions between asynchronous tasks. It's designed to enable basic communication
 * between the caller and the processor. It allows for status, completion, and errors to be passed back and forth,
 * finally, the "details" can be used to pass record specific info to be shared.
 */
public interface AsyncUpdateReceiver {

    void setPercentComplete(float complete);

    void setStatus(String status);

    void setDetails(List<Pair<Long, String>> details);

    void addDetail(Pair<Long, String> detail);

    List<String> getAsyncErrors();

    List<String> getHtmlAsyncErrors();

    List<Pair<Long, String>> getDetails();

    float getPercentComplete();

    String getStatus();

    void addError(Throwable t);

    void setCompleted();

    public void update(float percent, String status);

}
