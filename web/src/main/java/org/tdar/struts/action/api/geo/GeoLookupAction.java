package org.tdar.struts.action.api.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.search.geosearch.GeoSearchService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/geo")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class GeoLookupAction extends AbstractJsonApiAction implements Preparable {

    private static final long serialVersionUID = -883747123431228730L;

    @Autowired
    private GeoSearchService geoSearchService;
    private List<String> countries = new ArrayList<String>();
    private Map<String,Object> err = new HashMap<>();
    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    @Override
    public void prepare() throws Exception {
        err.put("status", "no countries");
        if (CollectionUtils.isEmpty(countries)) {
            setJsonObject(err);
            return;
        }
        LatitudeLongitudeBox box = geoSearchService.extractEnvelopeForCountries(countries);
        if (box.getEast() == null || box.getNorth() == null) {
            setJsonObject(err);
            return;
        }
        setJsonObject(box);
    }

    @Action(value="rlookup")
    @Override
    public String execute() throws Exception {
        // TODO Auto-generated method stub
        return super.execute();
    }
}
