package org.tdar.struts.action.browse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.struts.action.TdarBaseActionSupport;
import org.tdar.struts_base.action.TdarActionSupport;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/geographic")
@Results(value = {
        @Result(name=TdarActionSupport.SUCCESS, type=TdarActionSupport.REDIRECT, location="/browse/geographic-keyword/${keyword.id}/${keyword.slug}"),
        @Result(name=GeographicRedirectAction.SUCCESS_TYPE, type=TdarActionSupport.REDIRECT, location="/browse/geographic-keyword/${keyword.id}/${keyword.slug}?resourceTypes=${resourceTypes}"),
        @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.REDIRECT, location = "/not-found", params = { "status", "404" })
})
public class GeographicRedirectAction extends TdarBaseActionSupport implements Preparable {

    public static final String SUCCESS_TYPE = "success_type";
    private static final long serialVersionUID = 5689969933025476573L;
    @Autowired
    private transient GenericKeywordService genericKeywordService;
    private String code;
    private GeographicKeyword keyword;
    private ResourceType resourceTypes;
    
    @Override
    public void prepare() throws Exception {
        if (StringUtils.isNotBlank(getCode())) {
            setKeyword(genericKeywordService.findGeographicKeywordByCode(getCode()));
        }
    }

    @Action("{code}")
    @Override
    public String execute() throws Exception {
        getLogger().debug("{}", keyword);
        if (keyword == null) {
            return INPUT;
        }
        if (resourceTypes != null) {
            return GeographicRedirectAction.SUCCESS_TYPE;
        }
        return TdarActionSupport.SUCCESS;
    }
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public GeographicKeyword getKeyword() {
        return keyword;
    }

    public void setKeyword(GeographicKeyword keyword) {
        this.keyword = keyword;
    }

    public ResourceType getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(ResourceType resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

}
