package org.tdar.struts.action.resource;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.search.exception.SearchException;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.utils.PaginationHelper;

import com.opensymphony.xwork2.Preparable;

@Namespaces(value = {
        @Namespace("/document"),
        @Namespace("/image"),
        @Namespace("/project"),
        @Namespace("/dataset"),
        @Namespace("/coding-sheet"),
        @Namespace("/geospatial"),
        @Namespace("/sensory-data"),
        @Namespace("/ontology")
})
@ParentPackage("default")
@Component
@Scope("prototype")
@HttpsOnly
public class ResourceTypeBrowseAction extends AbstractLookupController<Resource> implements Preparable {

    private static final long serialVersionUID = 1L;

    private PaginationHelper paginationHelper;

    private ResourceType resourceType;

    @Autowired
    private transient ResourceSearchService resourceSearchService;

    @Override
    public void prepare() throws Exception {
        String rt = StringUtils.replace(getNamespace(), "/", "");
        resourceType = ResourceType.fromNamespace(rt);
        setRecordsPerPage(250);
        setResults(resourceSearchService.findByResourceType(resourceType, this, this).getResults());

    }

    @Override
    @Actions({
            @Action(value = "", results = { @Result(name = SUCCESS, location = "/WEB-INF/content/resource/list.ftl") }),
    })
    public String execute() throws SearchException, IOException {

        return SUCCESS;
    }

    public PaginationHelper getPaginationHelper() {
        if (paginationHelper == null) {
            paginationHelper = PaginationHelper.withSearchResults(this);
        }
        return paginationHelper;
    }

    public void setPaginationHelper(PaginationHelper paginationHelper) {
        this.paginationHelper = paginationHelper;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

}
