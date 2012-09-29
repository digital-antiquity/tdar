package org.tdar.struts.action;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.AsyncUpdateReceiver;
import org.tdar.utils.Pair;

@Component
@Scope("prototype")
@ParentPackage("secured-admin")
@Namespace("/searchindex")
public class BuildSearchIndexAction extends AuthenticationAware.Base implements AsyncUpdateReceiver {

    private static final long serialVersionUID = -8927970945627420725L;

    private int percentDone;
    private String phase;
    private String callback;

    @Autowired
    private transient SearchIndexService searchIndexService;

    public void setSearchIndexService(SearchIndexService searchIndexService) {
        this.searchIndexService = searchIndexService;
    }

    @Action(value = "checkstatus", results = {
            @Result(name = "wait", type = "freemarker", location = "checkstatus-wait.ftl", params = { "contentType", "application/json" }),
            @Result(name = "success", type = "freemarker", location = "checkstatus-done.ftl", params = { "contentType", "application/json" }) },
	 interceptorRefs = { @InterceptorRef(value="editAuthenticatedStack"),@InterceptorRef(value = "execAndWait") })
    public String checkStatus() {
        percentDone = 0;
        phase = "Initializing";
        buildIndex();
        return SUCCESS;
    }

    @Action(value = "build", results = { @Result(name = "success", location = "build.ftl") })
    public String build() {
        return SUCCESS;
    }

    public int getPercentDone() {
        return percentDone;
    }

    public String getPhase() {
        return phase;
    }

    private void buildIndex() {
        // TODO: break this out into multi-step process so that this process is
        // more informative
        searchIndexService.indexAll(this);
        percentDone = 100;
    }

    @Override
    public void setPercentComplete(float pct) {
        if (pct > 1f) {
            percentDone = 100;
        } else if (pct < 0f) {
            percentDone = 0;
        } else {
            percentDone = (int) (pct * 100);
        }
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
        logger.error(t.getMessage());
    }

    @Override
    public float getPercentComplete() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getStatus() {
        // TODO Auto-generated method stub
        return null;
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
    public List<Pair<Long, String>> getDetails() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAsyncErrors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getHtmlAsyncErrors() {
        // TODO Auto-generated method stub
        return null;
    }


}
