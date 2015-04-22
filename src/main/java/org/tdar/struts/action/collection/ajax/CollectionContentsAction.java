package org.tdar.struts.action.collection.ajax;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.exception.SearchPaginationException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.search.SearchService;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SimpleSearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Struts action that provides a paginiated viewinto a list of ResourceCollection resources.
 */
@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection/ajax")
@Results(value = {
        @Result(name = TdarActionSupport.SUCCESS, type = TdarActionSupport.JSONRESULT, params = { "stream", "jsonInputStream" }),
        @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.JSONRESULT, params = { "stream", "jsonInputStream", "statusCode", "500" })
})
public class CollectionContentsAction extends AuthenticationAware.Base{

    private InputStream jsonInputStream;
    @Autowired
    private SerializationService serializationService;
    @Autowired
    private SearchService searchService;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    private Long id = -1L;

    //fixme: authenticatedUser may not be initialized (right?)
    private ResourceSearchResultHandler searchResultHandler = new ResourceSearchResultHandler(getAuthenticatedUser());

    private ResourceCollection resourceCollection = null;

    public void prepare() {
        resourceCollection = resourceCollectionService.find(id);
    }

    public void validate() {
    }

    @Action("{id}/list")
    public String execute() {
        return SUCCESS;
    }

    public InputStream getJsonInputStream() {
        return jsonInputStream;
    }

    private void setJsonInputStream(InputStream jsonInputStream) {
        this.jsonInputStream = jsonInputStream;
    }

    /**
     * Convenience method for serializing the specified object and converting it to an inputStream.
     * @param obj object to stringify
     * @param jsonFilter JSON filter view to use during serialization
     * @throws java.io.IOException
     */
    protected final void setJsonObject(Object obj, Class<?> jsonFilter) throws IOException {
        String message = serializationService.convertToFilteredJson(obj, jsonFilter);
        getLogger().trace(message);
        setJsonInputStream(new ByteArrayInputStream(message.getBytes()));
    }

    private void buildLuceneSearch() throws TdarActionException, ParseException {

        ResourceQueryBuilder qb = searchService.buildResourceContainedInSearch(QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS, resourceCollection, getAuthenticatedUser(), this);

        searchResultHandler.setSortField(SortOption.TITLE);
        searchService.handleSearch(qb, searchResultHandler, this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
