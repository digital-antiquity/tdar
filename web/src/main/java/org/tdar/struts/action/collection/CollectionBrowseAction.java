package org.tdar.struts.action.collection;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.search.bean.CollectionSearchQueryObject;
import org.tdar.search.exception.SearchException;
import org.tdar.search.service.query.CollectionSearchService;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.utils.PaginationHelper;

import com.opensymphony.xwork2.Preparable;

@Namespaces(value = {
        @Namespace("/collection")
})
@ParentPackage("default")
@Component
@Scope("prototype")
@HttpsOnly
public class CollectionBrowseAction extends AbstractLookupController<ResourceCollection> implements Preparable {

    private static final long serialVersionUID = 1943741795132888657L;

    private PaginationHelper paginationHelper;

    @Autowired
    private transient CollectionSearchService collectionSearchService;

    @Override
    public void prepare() throws Exception {
        setRecordsPerPage(250);
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        csqo.setType(CollectionResourceSection.MANAGED);
        setResults(collectionSearchService.buildResourceCollectionQuery(getAuthenticatedUser(), csqo, this, this).getResults());
    }

    @Override
    @Actions({
            @Action(value = "", results = { @Result(name = SUCCESS, location = "/WEB-INF/content/collection/list.ftl") }),
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

}
