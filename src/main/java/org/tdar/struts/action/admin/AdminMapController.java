package org.tdar.struts.action.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.search.geosearch.GeoSearchDao.SpatialTables;
import org.tdar.search.geosearch.GeoSearchService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.data.SvgMapWrapper;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

@Component
@Scope("prototype")
// @ParentPackage("secured")
@Namespace("/admin/map")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
public class AdminMapController extends AuthenticationAware.Base {

    private static final long serialVersionUID = -3003338706569210521L;

    @Autowired
    private transient GeoSearchService geoSearchService;

    private SpatialTables table = SpatialTables.COUNTRY;
    private String limit;
    private List<List<String>> cssValues = new ArrayList<List<String>>();

    private SvgMapWrapper svgWrapper;

    @Action(value = "view")
    public String view() {
        return SUCCESS;

    }

    @Action(value = "map", results = {
            @Result(name = "success", location = "svg.ftl", type = "freemarker", params = { "contentType", "image/svg+xml" }) })
    @Override
    public String execute() {
        List<HomepageGeographicKeywordCache> caches = getGenericService().findAll(HomepageGeographicKeywordCache.class);
        setSvgWrapper(geoSearchService.toSvg(.05, "/search/results?geographicKeywords=", " (ISO%20Country%20Code)", table, limit));
        Double count = 0.0;
        Double max = 0.0;
        for (HomepageGeographicKeywordCache cache : caches) {
            count += cache.getLogCount();
            if (cache.getLogCount() > max) {
                max = cache.getLogCount();
            }
        }

        for (int i = 0; i <= 10; i++) {
            getCssValues().add(new ArrayList<String>());
            getCssValues().get(i).add("key" + i);
        }

        for (HomepageGeographicKeywordCache cache : caches) {
            // <#assign percent = ((codes[logCode]/countryLogTotal) * 100)?floor />
            int percent = (int) ((100 * cache.getLogCount()) / max);

            String code = cache.getKey().toUpperCase();
            if (code.length() > 2) {
                if (table == SpatialTables.COUNTRY) {
                    code = code.substring(0, 2);
                }
                int floor = (int) Math.floor(percent / 10);
                getCssValues().get(floor).add(code);
            }
        }
        return SUCCESS;
    }

    public SpatialTables getTable() {
        return table;
    }

    public void setTable(SpatialTables table) {
        this.table = table;
    }

    public List<List<String>> getCssValues() {
        return cssValues;
    }

    public void setCssValues(List<List<String>> cssValues) {
        this.cssValues = cssValues;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public SvgMapWrapper getSvgWrapper() {
        return svgWrapper;
    }

    public void setSvgWrapper(SvgMapWrapper svgWrapper) {
        this.svgWrapper = svgWrapper;
    }

}
