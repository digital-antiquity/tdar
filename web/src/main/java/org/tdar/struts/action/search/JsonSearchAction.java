package org.tdar.struts.action.search;

import java.io.ByteArrayInputStream;

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
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;
import org.tdar.utils.json.JsonLookupFilter;

@Namespace("/search")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpOnlyIfUnauthenticated
public class JsonSearchAction extends AbstractAdvancedSearchController {

    private static final long serialVersionUID = -7606256523280755196L;

    @Autowired
    private transient SerializationService serializationService;

    private GeoRssMode geoMode = GeoRssMode.POINT;

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
                ex = serializationService.createGeoJsonFromResourceList(getResult(),getResultsKey(), getRssUrl(), filter,getCallback());
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

}
