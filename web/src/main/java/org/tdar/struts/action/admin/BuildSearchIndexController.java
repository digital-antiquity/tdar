package org.tdar.struts.action.admin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.SerializationService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.Pair;
import org.tdar.utils.activity.Activity;
import org.tdar.utils.activity.IgnoreActivity;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin/searchindex")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
public class BuildSearchIndexController extends AbstractAuthenticatableAction implements AsyncUpdateReceiver {

    private static final long serialVersionUID = -8927970945627420725L;

    private int percentDone = -1;
    private String phase = "Initializing";
    private String callback;
    private boolean asyncSave = true;
    private boolean forceClear = false;
    private LinkedList<Throwable> errors = new LinkedList<>();

    private List<LookupSource> indexesToRebuild = new ArrayList<>();

    @Autowired
    private transient SearchIndexService searchIndexService;

    @Autowired
    private transient SerializationService serializationService;


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

            getLogger().info("reindexing");
            if (isAsyncSave()) {
                getLogger().info("reindexing async");
                searchIndexService.indexAllAsync(null, toReindex, person);
            } else {
                getLogger().info("reindexing sync");
                searchIndexService.indexAll(this, toReindex, person);
            }
        }
        getLogger().info("return");
        map.put("phase", phase);
        map.put("percentDone", percentDone);
        getLogger().debug("phase: {} [{}%]", phase, percentDone);
//        setJsonInputStream(new ByteArrayInputStream(serializationService.convertFilteredJsonForStream(map, null, callback).getBytes()));
        return SUCCESS;
    }

    Map<String, Object> map = new HashMap<>();

    @IgnoreActivity
    @Action(value = "checkstatus", results = { @Result(name = SUCCESS, type = JSONRESULT) })
    @PostOnly
    @HttpForbiddenErrorResponseOnly
    public String checkStatusAsync() {
        Activity activity = ActivityManager.getInstance().findActivity(SearchIndexService.BUILD_LUCENE_INDEX_ACTIVITY_NAME);
        if (activity != null) {
            phase = activity.getMessage();
            percentDone = activity.getPercentComplete().intValue();
        }
        map.put("phase", phase);
        map.put("percentDone", percentDone);
        // getLogger().debug("phase: {} [{}%]", phase, percentDone);
//        setJsonInputStream(new ByteArrayInputStream(serializationService.convertFilteredJsonForStream(map, null, callback).getBytes()));
        return SUCCESS;
    }
    
    public Object getResultObject() {
        return map;
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

    @Override
    public void setPercentComplete(float pct) {
        percentDone = pct < 1f ? pct > 0 ? (int) (pct * 100) : 0 : 100; // this is so wrong, but I couldn't resist
    }

    @Override
    public void setStatus(String status) {
        // getLogger().debug("indexing status: {}", status);
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
        getLogger().error(t.getMessage(), t);
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
    public List<String> getAsyncErrors() {
        List<String> ers = new ArrayList<>();
        for (Throwable t : errors) {
            ers.add(t.getLocalizedMessage());
        }
        return ers;
    }

    @Override
    public List<String> getHtmlAsyncErrors() {
        List<String> ers = new ArrayList<>();
        for (Throwable t : errors) {
            ers.add("<br />" + t.getLocalizedMessage());
        }
        return ers;
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

    public List<LookupSource> getAllSources() {
        return Arrays.asList(LookupSource.values());
    }

    public boolean isAlreadyRunning() {
        return ActivityManager.getInstance().findActivity(SearchIndexService.BUILD_LUCENE_INDEX_ACTIVITY_NAME) == null;
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
