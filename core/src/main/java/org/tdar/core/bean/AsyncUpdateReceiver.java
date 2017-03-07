package org.tdar.core.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.utils.Pair;

/**
 * This interface governs the interactions between asynchronous tasks. It's designed to enable basic communication
 * between the caller and the processor. It allows for status, completion, and errors to be passed back and forth,
 * finally, the "details" can be used to pass record specific info to be shared.
 */
public interface AsyncUpdateReceiver {

    static AsyncUpdateReceiver DEFAULT_RECEIVER = new DefaultReceiver();

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

    public class DefaultReceiver implements AsyncUpdateReceiver {
        private float percentComplete;
        private String status = "initialized";
        private List<Throwable> throwables = new ArrayList<Throwable>();
        private List<Pair<Long, String>> details = new ArrayList<Pair<Long, String>>();

        private final transient Logger logger = LoggerFactory.getLogger(getClass());
        
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

    public void update(float percent, String status);
}
