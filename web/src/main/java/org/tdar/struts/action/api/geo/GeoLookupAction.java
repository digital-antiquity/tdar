package org.tdar.struts.action.api.geo;

import java.util.ArrayList;
import java.util.List;

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
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

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

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    @Override
    public void prepare() throws Exception {
        if (!CollectionUtils.isEmpty(countries)) {
        }
        LatitudeLongitudeBox box = geoSearchService.extractEnvelopeForCountries(countries);
        setJsonObject(box);
    }

    @Action(value="rlookup")
    @Override
    public String execute() throws Exception {
        // TODO Auto-generated method stub
        return super.execute();
    }
}
