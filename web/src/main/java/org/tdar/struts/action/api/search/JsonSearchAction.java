package org.tdar.struts.action.api.search;

import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.SortOption;
import org.tdar.core.service.FeedSearchHelper;
import org.tdar.core.service.GeoRssMode;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.AbstractAdvancedSearchController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.json.JacksonView;
import org.tdar.utils.json.JsonLookupFilter;

@Namespaces(value = {
        @Namespace("/search"),
        @Namespace("/api/search") })
@Component
@Scope("prototype")
@ParentPackage("default")
public class JsonSearchAction extends AbstractAdvancedSearchController {

    private static final long serialVersionUID = -7606256523280755196L;

    @Autowired
    private transient SerializationService serializationService;

    private GeoRssMode geoMode = GeoRssMode.POINT;
    private boolean webObfuscation = false;

    private Map<String, Object> resultObject;

    @Action(value = "json", results = {
            @Result(name = SUCCESS, type = JSONRESULT) })
    public String viewJson() throws TdarActionException {
        try {
            if (getSortField() == null) {
                setSecondarySortField(SortOption.TITLE);
            }
            setOrientation(DisplayOrientation.MAP);
            setMode("json");
            performResourceSearch();
            jsonifyResult(JsonLookupFilter.class);
        } catch (TdarActionException tdae) {
            return tdae.getResponse();
        } catch (Exception e) {
            getLogger().error("rss error", e);
            addActionErrorWithException(getText("advancedSearchController.could_not_process"), e);
        }
        return SUCCESS;
    }

    @Override
    public void jsonifyResult(Class<?  extends JacksonView> filter) {
        prepareResult();
        if (!isReindexing()) {
            try {
                setFilter(filter);
                FeedSearchHelper feedSearchHelper = new FeedSearchHelper(getRssUrl(), this, getGeoMode(), getAuthenticatedUser());
                this.setFilter(filter);
                resultObject = serializationService.createGeoJsonFromResourceList(feedSearchHelper);
            } catch (Exception e) {
                getLogger().error("error creating json", e);
            }
        }
    }

    public GeoRssMode getGeoMode() {
        return geoMode;
    }

    public void setGeoMode(GeoRssMode geoMode) {
        this.geoMode = geoMode;
    }

    public boolean isWebObfuscation() {
        return webObfuscation;
    }

    public void setWebObfuscation(boolean webObfuscation) {
        this.webObfuscation = webObfuscation;
    }

    public Map<String, Object> getResultObject() {
        return resultObject;
    }

}
