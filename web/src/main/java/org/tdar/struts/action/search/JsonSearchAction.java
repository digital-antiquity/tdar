package org.tdar.struts.action.search;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.SortOption;
import org.tdar.core.service.RssService.GeoRssMode;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.utils.json.JsonLookupFilter;

@Namespace("/search")
@Component
@Scope("prototype")
@ParentPackage("default")
public class JsonSearchAction extends AbstractAdvancedSearchController {

    private static final long serialVersionUID = -7606256523280755196L;

    @Autowired
    private transient SerializationService serializationService;

    private GeoRssMode geoMode = GeoRssMode.POINT;
    private boolean webObfuscation =  false;

    @Action(value = "json", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" }) })
    public String viewJson() throws TdarActionException {
        try {
            if (getSortField() == null) {
                setSecondarySortField(SortOption.TITLE);
            }
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
    public void jsonifyResult(Class<?> filter) {
        prepareResult();
        String ex = "";
        if (!isReindexing()) {
            try {
            	Map<String,Object> params = new HashMap<>();
            	params.put(	"recordsPerPage", this.getRecordsPerPage());
            	params.put(	"totalRecords", this.getTotalRecords());
            	params.put(	"startRecord", this.getStartRecord());
            	params.put(	"description", this.getSearchDescription());
            	params.put(	"title", this.getSearchTitle());
            	params.put(	"url", this.getRssUrl());
                ex = serializationService.createGeoJsonFromResourceList(getResults(), getRssUrl(), params, getGeoMode(),webObfuscation, filter, getCallback());
            } catch (Exception e) {
                getLogger().error("error creating json", e);
            }
        }
        setJsonInputStream(new ByteArrayInputStream(ex.getBytes()));
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

}
