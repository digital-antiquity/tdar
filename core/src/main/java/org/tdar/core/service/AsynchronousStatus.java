package org.tdar.core.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.utils.Pair;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect
public class AsynchronousStatus implements Serializable, AsyncUpdateReceiver {

    private static final long serialVersionUID = 1689999240817782998L;

    public static final String INDEXING = "INDEXING";

    public static final String INDEXING_EXTERNAL = "INDEXING (MANUAL)";

    private List<Throwable> throwables = new ArrayList<Throwable>();
    private List<Pair<Long, String>> details = new ArrayList<Pair<Long, String>>();

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String status;
    private String message;
    private Float percentComplete = 0f;
    private String key;
    private Long startTime = -1L;
    private Long totalTime = -1L;
    private boolean hasEnded = false;

    private String username;

    private Long userId;

    public AsynchronousStatus() {
        startTime = System.currentTimeMillis();

    }

    public AsynchronousStatus(String key) {
        this();
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean hasExpired(long expirationTimeInMillis) {
        return false;
    }

    public boolean hasEnded() {
        return hasEnded;
    }

    @JsonIgnore
    public boolean isIndexingActivity() {
        return StringUtils.equals(INDEXING_EXTERNAL, key);
    }

    public void setUser(String username, Long id) {
        this.setUserId(id);
        this.setUsername(username);

    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void end() {
        hasEnded = true;
        totalTime = System.currentTimeMillis() - startTime;
    }

    @Override
    public float getPercentComplete() {
        return percentComplete;
    }

    @Override
    public void setPercentComplete(float percentComplete) {
        this.percentComplete = percentComplete;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    public List<Throwable> getThrowables() {
        return throwables;
    }

    @Override
    public void addError(Throwable t) {
        setStatus("Error occurred");
        throwables.add(t);
    }

    @Override
    public void setDetails(List<Pair<Long, String>> details) {
        this.details = details;
    }

    @Override
    public List<Pair<Long, String>> getDetails() {
        return details;
    }

    @Override
    public void addDetail(Pair<Long, String> detail) {
        getDetails().add(detail);
    }

    @Override
    public List<String> getAsyncErrors() {
        List<String> messages = new ArrayList<>();
        for (Throwable throwable : getThrowables()) {
            try {
                messages.add(throwable.getLocalizedMessage());
            } catch (Exception e) {
                logger.error(throwable.getMessage());
                logger.error(ExceptionUtils.getStackTrace(throwable));
                messages.add(throwable.getMessage());
            }
        }
        return messages;
    }

    @Override
    public List<String> getHtmlAsyncErrors() {
        List<String> messages = new ArrayList<>();
        for (Throwable throwable : getThrowables()) {
            messages.add(throwable.getLocalizedMessage());
        }
        return messages;
    }

    @Override
    public void update(float percent, String status) {
        setStatus(status);
        setPercentComplete(percent);
    }

    @Override
    public void setCompleted() {
        setPercentComplete(100f);
        setStatus("Complete");
    }

}
