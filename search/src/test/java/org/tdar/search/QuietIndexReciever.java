package org.tdar.search;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.utils.Pair;

public class QuietIndexReciever implements AsyncUpdateReceiver {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private float percentComplete;
    private List<String> errors;
    private String status;

    @Override
    public void setPercentComplete(float complete) {
        this.percentComplete = complete;

    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void setDetails(List<Pair<Long, String>> details) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addDetail(Pair<Long, String> detail) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getAsyncErrors() {
        return errors;
    }

    @Override
    public List<String> getHtmlAsyncErrors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Pair<Long, String>> getDetails() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float getPercentComplete() {
        return percentComplete;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void addError(Throwable t) {
        logger.error("error in indexing", t);
    }

    @Override
    public void setCompleted() {
        percentComplete = 100f;
        logger.debug("Completed Indexing");
    }

    @Override
    public void update(float percent, String status) {
        percentComplete = percent;
        this.status = status;
    }
}