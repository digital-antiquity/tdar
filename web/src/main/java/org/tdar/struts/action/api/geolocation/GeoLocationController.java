package org.tdar.struts.action.api.geolocation;

import java.io.InputStream;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.tools.ant.filters.StringInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.service.SerializationService;
import org.tdar.search.geosearch.GeoSearchService;
import org.tdar.struts.action.TdarBaseActionSupport;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/api/geolocation")
public class GeoLocationController extends TdarBaseActionSupport {

    private static final long serialVersionUID = -7316298111150245236L;

    @Autowired
    private GeoSearchService geoSearchService;

    @Autowired
    private SerializationService serializationService;

    private Double minX;
    private Double minY;
    private Double maxX;
    private Double maxY;

    private InputStream inputStream;

    @Action(value = "lookup", results = {
            @Result(name = SUCCESS, type = "stream", params = {
                    "contentType", "text/xml",
                    "inputName", "inputStream"
            })
    })
    public String lookup() {
        getLogger().debug("beginning external latLong lookup");
        LatitudeLongitudeBox latLongBox = new LatitudeLongitudeBox();
        latLongBox.setMinx(minX);
        latLongBox.setMiny(minY);
        latLongBox.setMaxx(maxX);
        latLongBox.setMaxy(maxY);
        try {
            Set<GeographicKeyword> allGeographicInfo = geoSearchService.extractAllGeographicInfo(latLongBox);
            latLongBox.addGeographicKeywords(allGeographicInfo);
            String xml = serializationService.convertToXML(latLongBox);
            setInputStream(new StringInputStream(xml));
            getLogger().info(xml);
            getLogger().debug("completed external latLong lookup");
            return SUCCESS;
        } catch (Exception e) {
            getLogger().error("an error occured", e);
        }
        return ERROR;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Double getMaxY() {
        return maxY;
    }

    public void setMaxY(Double maxY) {
        this.maxY = maxY;
    }

    public Double getMaxX() {
        return maxX;
    }

    public void setMaxX(Double maxX) {
        this.maxX = maxX;
    }

    public Double getMinY() {
        return minY;
    }

    public void setMinY(Double minY) {
        this.minY = minY;
    }

    public Double getMinX() {
        return minX;
    }

    public void setMinX(Double minX) {
        this.minX = minX;
    }
}
