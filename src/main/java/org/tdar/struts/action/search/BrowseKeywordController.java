package org.tdar.struts.action.search;

import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchService;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.HydrateableKeywordQueryPart;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/browse")
public class BrowseKeywordController extends AbstractLookupController<Resource> implements Preparable {

    private static final long serialVersionUID = 5267144668224536569L;

    @Autowired
    private transient SearchService searchService;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    private Long id;
    private KeywordType keywordType;
    private Keyword keyword;

    public Keyword getKeyword() {
        return keyword;
    }

    public void setKeyword(Keyword keyword) {
        this.keyword = keyword;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public KeywordType getKeywordType() {
        return keywordType;
    }

    public void setKeywordType(KeywordType keywordType) {
        this.keywordType = keywordType;
    }

    @Override
    public void prepare() throws Exception {
        if (Persistable.Base.isNullOrTransient(getId())) {
            addActionError(getText("simpleKeywordAction.id_required"));
        }
        if (getKeywordType() == null) {
            addActionError(getText("simpleKeywordAction.type_required"));
        }

        setKeyword(genericKeywordService.find(getKeywordType().getKeywordClass(), getId()));
    }

    @Action(value = "keywords", interceptorRefs = { @InterceptorRef("unauthenticatedStack") })
    public String view() {
        if (getKeyword().getStatus() != Status.ACTIVE && !isEditor()) {
            return NOT_FOUND;
        }
        setMode("KeywordBrowse");
        ResourceQueryBuilder rqb = new ResourceQueryBuilder();
        rqb.append(new HydrateableKeywordQueryPart<Keyword>(getKeywordType(), Arrays.asList(getKeyword())));
        rqb.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.ACTIVE));
        try {
            setSortField(SortOption.TITLE);
            searchService.handleSearch(rqb, this, this);
        } catch (Exception e) {
            addActionErrorWithException(getText("collectionController.error_searching_contents"), e);
        }
        return SUCCESS;
    }

    @Override
    public List getFacetFields() {
        return null;
    }

}
