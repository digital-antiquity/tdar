package org.tdar.struts.action.admin;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.SearchIndexService;
import org.tdar.struts.RequiresTdarUserGroup;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.utils.Pair;
import org.tdar.utils.activity.IgnoreActivity;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin/searchindex")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
public class BuildSearchIndexController extends AuthenticationAware.Base implements AsyncUpdateReceiver {

    private static final long serialVersionUID = -8927970945627420725L;

    private int percentDone;
    private String phase;
    private String callback;

    LinkedList<Throwable> errors = new LinkedList<Throwable>();

    @Autowired
    private transient SearchIndexService searchIndexService;

    public void setSearchIndexService(SearchIndexService searchIndexService) {
        this.searchIndexService = searchIndexService;
    }

    @IgnoreActivity
    @Action(value = "checkstatus", results = {
            @Result(name = "wait", type = "freemarker", location = "checkstatus-wait.ftl", params = { "contentType", "application/json" }),
            @Result(name = "success", type = "freemarker", location = "checkstatus-done.ftl", params = { "contentType", "application/json" }) },
            interceptorRefs = { @InterceptorRef(value = "editAuthenticatedStack"), @InterceptorRef(value = "execAndWait") })
    public String checkStatus() {
        percentDone = 0;
        phase = "Initializing";
        buildIndex();
        return SUCCESS;
    }

    @Action(value = "build", results = { @Result(name = "success", location = "build.ftl") })
    public String build() {
        try {
            logger.info("{} IS REBUILDING SEARCH INDEX", getAuthenticatedUser().getEmail().toUpperCase());
        } catch (Exception e) {
            logger.error("weird exception {} ", e);
        }
        return SUCCESS;
    }

    private void buildIndex() {
        Date date = new Date();
        searchIndexService.indexAll(this);
        getEmailService().send(String.format("indexing of %s complete.\n Started: %s \n Completed: %s", getHostName(), date, new Date()), "indexing completed");
        percentDone = 100;
    }

    @Override
    public void setPercentComplete(float pct) {
        percentDone = pct < 1f ? pct > 0 ? (int) (pct * 100) : 0 : 100; // this is so wrong, but I couldn't resist
    }

    @Override
    public void setStatus(String status) {
        logger.debug("indexing status: {}", status);
        this.phase = "Current Status: " + status;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    @Override
    public void addError(Throwable t) {
        setStatus(t.getMessage());
        errors.addFirst(t);
        logger.error(t.getMessage(), t);
    }

    @Override
    public float getPercentComplete() {
        return percentDone;
    }

    @Override
    public String getStatus() {
        return phase;
    }

    @Override
    public void setDetails(List<Pair<Long, String>> details) {
        // we ignore details for now
    }

    @Override
    public void addDetail(Pair<Long, String> detail) {
        // we ignore details for now
    }

    @Override
    public List<Pair<Long, String>> getDetails() {
        return Collections.emptyList();
    }

    @Override
    public String getAsyncErrors() {
        StringBuilder sb = new StringBuilder();
        for (Throwable t : errors) {
            sb.append("\n").append(t.getMessage());
        }
        return sb.toString();
    }

    @Override
    public String getHtmlAsyncErrors() {
        StringBuilder sb = new StringBuilder();
        for (Throwable t : errors) {
            sb.append("<br />").append(t.getMessage());
        }
        return sb.toString();
    }

    @Override
    public void setCompleted() {
        setStatus("Complete");
        setPercentComplete(100f);

    }

    @Override
    public void update(float percent, String status) {
        setStatus(status);
        setPercentComplete(percent);
    }

    public boolean isAlreadyRunning() {
        return ActivityManager.getInstance().findActivity(SearchIndexService.BUILD_LUCENE_INDEX_ACTIVITY_NAME) == null;
    }

}
