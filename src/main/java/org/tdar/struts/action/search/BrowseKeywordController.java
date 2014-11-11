package org.tdar.struts.action.search;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchService;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.HydrateableKeywordQueryPart;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/browse")
@Results(value = {
        @Result(name = TdarActionSupport.SUCCESS, type = TdarActionSupport.FREEMARKER, location = "keywords.ftl"),
        @Result(name = TdarActionSupport.BAD_SLUG, type = TdarActionSupport.REDIRECT,
                location = "/${keywordType.urlNamespace}/${keyword.id}/${keyword.slug}${slugSuffix}", params = { "ignoreParams", "id,keywordPath,slug" })
})
public class BrowseKeywordController extends AbstractLookupController<Resource> implements Preparable, SlugViewAction {

    private static final long serialVersionUID = 5267144668224536569L;

    @Autowired
    private transient SearchService searchService;
    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;
    @Autowired
    private transient GenericKeywordService genericKeywordService;

    private Long id;
    private KeywordType keywordType;
    private Keyword keyword;
    private String slug = "";
    private String slugSuffix = "";
    private String keywordPath = "";
    private DisplayOrientation orientation = DisplayOrientation.LIST_FULL;
    private boolean redirectBadSlug;

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
        if (getKeywordPath() == null) {
            addActionError(getText("simpleKeywordAction.type_required"));
        } else {
            setKeywordType(KeywordType.fromPath(getKeywordPath()));
        }

        if (getKeywordType() == null) {
            addActionError(getText("simpleKeywordAction.type_required"));
        }

        if (Persistable.Base.isNullOrTransient(keyword) || getKeyword().getStatus() != Status.ACTIVE && !isEditor()) {
            throw new TdarActionException(StatusCode.NOT_FOUND, "not found");
        }
        setKeyword(genericKeywordService.find(getKeywordType().getKeywordClass(), getId()));
        getLogger().debug("id:{}  slug:{}", getId(), getSlug());
        if (!handleSlugRedirect(keyword, this)) {
            redirectBadSlug = true;
        } else {
            try {
                prepareLuceneQuery();
            } catch (Exception e) {
                addActionErrorWithException(getText("collectionController.error_searching_contents"), e);
            }
        }
    }

    @Actions(value = {
            @Action(value = "{keywordPath}/{id}"),
            @Action(value = "{keywordPath}/{id}/{slug}")
    })
    public String view() {
        if (redirectBadSlug) {
            return BAD_SLUG;
        }
        if (keywordType == KeywordType.GEOGRAPHIC_KEYWORD) {
            // setOrientation(DisplayOrientation.MAP);
        }
        return SUCCESS;
    }

    private void prepareLuceneQuery() throws ParseException {
        setMode("KeywordBrowse");
        ResourceQueryBuilder rqb = new ResourceQueryBuilder();
        rqb.append(new HydrateableKeywordQueryPart<Keyword>(getKeywordType(), Arrays.asList(getKeyword())));
        rqb.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.ACTIVE));
        setSortField(SortOption.TITLE);
        searchService.handleSearch(rqb, this, this);
        bookmarkedResourceService.applyTransientBookmarked(getResults(), getAuthenticatedUser());
    }

    @Override
    public List getFacetFields() {
        return null;
    }

    public DisplayOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DisplayOrientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Override
    public String getSlugSuffix() {
        return slugSuffix;
    }

    @Override
    public void setSlugSuffix(String slugSuffix) {
        this.slugSuffix = slugSuffix;
    }

    private String getKeywordPath() {
        return keywordPath;
    }

    public void setKeywordPath(String keywordPath) {
        this.keywordPath = keywordPath;
    }

    @Override
    public int getDefaultRecordsPerPage() {
        return DEFAULT_RESULT_SIZE;
    }
}
