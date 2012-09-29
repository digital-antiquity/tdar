package org.tdar.core.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.tdar.utils.Pair;

public interface AsyncUpdateReceiver {

    public final static AsyncUpdateReceiver DEFAULT_RECEIVER = new AsyncUpdateReceiver() {
        private Logger log = Logger.getLogger(AsyncUpdateReceiver.class);

        public void setPercentComplete(float complete) {
            log.trace("% complete:" + complete);
        }

        public void setStatus(String status) {
            log.debug("index status:" + status);
        }

        public void addError(Throwable t) {
            log.error(t);
        }
        
        public String getAsyncErrors() {
            return "";
        }

        public String getHtmlAsyncErrors() {
            return "";
        }

        public void setDetails(List<Pair<Long, String>> details) {
            log.trace("set details");
        }

        public List<Pair<Long, String>> getDetails() {
            return new ArrayList<Pair<Long, String>>();
        }

        public void addDetail(Pair<Long, String> detail) {
            log.trace(detail);
        }

        @Override
        public float getPercentComplete() {
            return 0;
        }

        @Override
        public String getStatus() {
            // TODO Auto-generated method stub
            return null;
        }
    };

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

    }
}
