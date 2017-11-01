package org.tdar.struts.action.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.AsynchronousProcessManager;
import org.tdar.core.service.AsynchronousStatus;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.result.HasJsonDocumentResult;
import org.tdar.utils.activity.IgnoreActivity;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin/searchindex")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
public class BuildSearchIndexController extends AbstractAuthenticatableAction implements HasJsonDocumentResult {

    private static final long serialVersionUID = -8927970945627420725L;

    private String callback;
    private boolean asyncSave = true;
    private boolean forceClear = false;

    private List<LookupSource> indexesToRebuild = new ArrayList<>();

    @Autowired
    private transient SearchIndexService searchIndexService;

    private AsynchronousStatus asyncActivity = new AsynchronousStatus(AsynchronousStatus.INDEXING_EXTERNAL);

    @IgnoreActivity
    @Action(value = "buildIndex", results = {
            @Result(name = SUCCESS, type = JSONRESULT)
    })
    public String startIndex() {
        if (!isReindexing()) {
            List<LookupSource> toReindex = getIndexesToRebuild();
            if (CollectionUtils.isEmpty(toReindex)) {
                toReindex = Arrays.asList(LookupSource.values());
            }

            getLogger().info("to reindex: {}", toReindex);
            TdarUser person = getAuthenticatedUser();
            AsynchronousProcessManager.getInstance().addActivityToQueue(asyncActivity);
            getLogger().info("reindexing");
            if (isAsyncSave()) {
                getLogger().info("reindexing async");
                searchIndexService.indexAllAsync(asyncActivity, toReindex, person);
            } else {
                getLogger().info("reindexing sync");
                searchIndexService.indexAll(asyncActivity, toReindex, person);
            }
        }
        getLogger().info("return: {}", asyncActivity);
        return SUCCESS;
    }

    @IgnoreActivity
    @Action(value = "checkstatus", results = { @Result(name = SUCCESS, type = JSONRESULT) })
    @PostOnly
    @HttpForbiddenErrorResponseOnly
    public String checkStatusAsync() {
        AsynchronousStatus activity = AsynchronousProcessManager.getInstance().findActivity(AsynchronousStatus.INDEXING_EXTERNAL);
        if (activity != null) {
            asyncActivity = activity;
        }

        return SUCCESS;
    }

    @Override
    public Object getResultObject() {
        getLogger().debug("getResultObject:{}", asyncActivity);
        return asyncActivity;
    }

    @Action(value = "build", results = { @Result(name = SUCCESS, location = "build.ftl") })
    public String build() {
        try {
            getLogger().info("{} IS REBUILDING SEARCH INDEXES", getAuthenticatedUser().getEmail().toUpperCase());
            if (forceClear) {
                searchIndexService.clearIndexingActivities();
            }
        } catch (Exception e) {
            getLogger().error("weird exception {} ", e);
        }
        return SUCCESS;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public List<LookupSource> getAllSources() {
        return Arrays.asList(LookupSource.values());
    }

    public boolean isAlreadyRunning() {
        return AsynchronousProcessManager.getInstance().findActivity(AsynchronousStatus.INDEXING) != null;
    }

    public List<LookupSource> getIndexesToRebuild() {
        return indexesToRebuild;
    }

    public void setIndexesToRebuild(List<LookupSource> indexesToRebuild) {
        this.indexesToRebuild = indexesToRebuild;
    }

    public boolean isAsyncSave() {
        return asyncSave;
    }

    public void setAsyncSave(boolean asyncSave) {
        this.asyncSave = asyncSave;
    }

    public boolean isForceClear() {
        return forceClear;
    }

    public void setForceClear(boolean forceClear) {
        this.forceClear = forceClear;
    }

}
