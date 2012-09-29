package org.tdar.core.service;

import java.util.ArrayList;
import java.util.List;

import org.tdar.utils.Pair;

public interface AsyncUpdateReceiver {

    public final static AsyncUpdateReceiver DEFAULT_RECEIVER = new DefaultReceiver();

    public void setPercentComplete(float complete);

    public void setStatus(String status);

    public void setDetails(List<Pair<Long, String>> details);

    public void addDetail(Pair<Long, String> detail);

    public String getAsyncErrors();

    public String getHtmlAsyncErrors();

    public List<Pair<Long, String>> getDetails();

    public float getPercentComplete();

    public String getStatus();

    public void addError(Throwable t);
    
    public void setCompleted();

    public class DefaultReceiver implements AsyncUpdateReceiver {
        private float percentComplete;
        private String status = "initialized";
        private List<Throwable> throwables = new ArrayList<Throwable>();
        private List<Pair<Long, String>> details = new ArrayList<Pair<Long, String>>();

        public float getPercentComplete() {
            return percentComplete;
        }

        public void setPercentComplete(float percentComplete) {
            this.percentComplete = percentComplete;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<Throwable> getThrowables() {
            return throwables;
        }

        @Override
        public void addError(Throwable t) {
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
        
        public String getAsyncErrors() {
            StringBuilder sb = new StringBuilder();
            for (Throwable throwable: getThrowables()) {
                sb.append(throwable.getMessage());
            }
            return sb.toString();
        }

        public String getHtmlAsyncErrors() {
            StringBuilder sb = new StringBuilder();
            for (Throwable throwable: getThrowables()) {
                sb.append("<li>").append(throwable.getMessage()).append("</li>");
            }
            return sb.toString();
        }

        public void update(float percent, String status) {
            setStatus(status);
            setPercentComplete(percent);
        }

        public void setCompleted() {
            setPercentComplete(100f);
            setStatus("Complete");
        }


    }

    public void update(float percent, String status);
}
